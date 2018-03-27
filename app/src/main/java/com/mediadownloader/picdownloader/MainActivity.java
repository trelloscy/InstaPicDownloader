package com.mediadownloader.picdownloader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import java.net.HttpURLConnection;
import android.app.ProgressDialog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import static android.os.Environment.getExternalStoragePublicDirectory;

 public class MainActivity extends AppCompatActivity {

     String appName = "PicDownloader";
     String instagramUrl = "https://www.instagram.com/";
     String publicDirectoryPrefix = "p/";
     EditText txtUsername;
     String profilePicUrl = "";
     ArrayList<String> picturesUrlList = new ArrayList<>();
     Button btnSearch;
     Button btnDownloadProfilePic;
     Button btnDownloadImages;
     Button btnOpenGallery;
     Button btnRandomAccount;
     Button btnShareDownload;
     ImageView imgPreview;
     ProgressDialog dialog;

     public void search_Click(View v) {

         // Check for empty username
         String userInput = sanitizeUsername();
         if (TextUtils.isEmpty(userInput)) {
             txtUsername.setError("Please specify a valid Instagram Username/Profile url/Share url");
             return;
         }

         //Toast.makeText(getApplicationContext(), "Button clicked!", Toast.LENGTH_SHORT).show();

         // Hide keyboard
         InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

         // Define instagram url
         String fullUrl = instagramUrl + userInput + "/";

         try {
             new DownloadSourceCodeTask().execute(fullUrl);
         } catch (Exception e) {
             Log.d("Error", e.getMessage());
         }

         // New
         if (!userInput.startsWith(publicDirectoryPrefix)) {
             // Problem: This includes INVALID searches
             String metadataUrl = "http://centraldbwebapi.azurewebsites.net/api/metadata/" + userInput;
             try {
                 new ProcessMetadataTask().execute(metadataUrl);
             } catch (Exception e) {
                 Log.d("Error", e.getMessage());
             }
         }
     }

     public void cancel_Click(View v) {
         txtUsername.setText("");

         // Disable buttons
         btnDownloadProfilePic.setEnabled(false);
         btnDownloadImages.setEnabled(false);

         // Clear imageView
         clearImage();
     }

     public void clearImage() {

         imgPreview.setImageResource(android.R.color.transparent);
         imgPreview.setBackgroundColor(Color.parseColor("#f6f6f6"));
     }

     public String sanitizeUsername() {
         String result = txtUsername.getText().toString().trim();

         result = result.replace(instagramUrl, "");
         if (result.endsWith("/")) {
             result = result.substring(0, result.lastIndexOf('/'));
         }

         return result;
     }

     public void downloadProfilePic_Click(View v) {

         String strUserName = sanitizeUsername();
         if (TextUtils.isEmpty(strUserName)) {
             txtUsername.setError("Please specify a valid Instagram Username/Profile url/Share url");

            // New - disable buttons
             btnDownloadProfilePic.setEnabled(false);
             btnDownloadImages.setEnabled(false);

             return;
         }

         // OLD
         //String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
         //String customName = String.format("%s profile pic (%s).jpg", strUserName, date);

         // New - prevent java.lang.StringIndexOutOfBoundsException
         if (profilePicUrl.length() < 1) {
             Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_LONG).show(); // TOAST!

             btnDownloadProfilePic.setEnabled(false);
             btnDownloadImages.setEnabled(false);
             return;
         }

         // NEW
         String fileName = profilePicUrl.substring(profilePicUrl.lastIndexOf('/')+1, profilePicUrl.length());
         //String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
         String customName = String.format("%s %s", strUserName.replace(publicDirectoryPrefix, ""), fileName);

         try {

             // New - show dialog in main thread
             dialog.setMessage("Downloading...");
             dialog.show();
             new DownloadImageTask().execute(profilePicUrl, customName, Integer.toString(1));

         } catch (Exception e) {
             Log.d("Error", e.getMessage());
         }
     }

     public void downloadImages_Click(View v) {

         String strUserName = sanitizeUsername();
         if (TextUtils.isEmpty(strUserName)) {
             txtUsername.setError("Please specify a valid username or Profile url");

             // New - disable buttons
             btnDownloadProfilePic.setEnabled(false);
             btnDownloadImages.setEnabled(false);

             return;
         }

         /*
          * NEXT VERSION
          * Maybe display number of pics found?
          * Could also display the number of pics in the Gallery!
          */

         int counter = picturesUrlList.size();

         // New - show dialog in main thread
         dialog.setMessage("Downloading latest images...");
         dialog.show();

         for (String url: picturesUrlList) {

             // String "url" is the url of each pic
             // e.g.: https://scontent-ams3-1.cdninstagram.com/t51.2885-15/s640x640/sh0.08/e35/c135.0.810.810/21980629_914563065361670_1351262531695411200_n.jpg

             String fileName = url.substring(url.lastIndexOf('/')+1, url.length());
             //String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
             String customName = String.format("%s %s", strUserName, fileName);

             try {
                 new DownloadImageTask().execute(url, customName, Integer.toString(counter--));

             } catch (Exception e) {
                 Log.d("Error", e.getMessage());
             }
         }
     }

     public void getRandomAccount_Click(View v) {

         // METHOD 1
         String[] array = getApplicationContext().getResources().getStringArray(R.array.accounts_array);
         String randomStr = array[new Random().nextInt(array.length)];

         txtUsername.setText(randomStr);
         btnSearch.performClick();

         /*
          * METHOD 2
          *
         // Define REST API url
         String url = "http://centraldbwebapi.azurewebsites.net/api/randomaccount";

         try {
             new ProcessRestResponseTask().execute(url);
         } catch (Exception e) {
             Log.d("Error", e.getMessage());
         }
         */
     }

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);

         //// Display logo in header
         // getSupportActionBar().setDisplayUseLogoEnabled(true);
         // getSupportActionBar().setDisplayShowHomeEnabled(true);
         // getSupportActionBar().setIcon(R.mipmap.ic_launcher);

         btnSearch = (Button) findViewById(R.id.btnSearch);
         txtUsername = (EditText) findViewById(R.id.txtUsername);
         btnDownloadProfilePic = (Button) findViewById(R.id.btnDownloadProfilePic);
         btnDownloadImages = (Button) findViewById(R.id.btnDownloadImages);
         btnRandomAccount = (Button) findViewById(R.id.btnRandomAccount);
         imgPreview = (ImageView) findViewById(R.id.imgPreview);
         dialog = new ProgressDialog(this);
         clearImage();

         // TEMP - set default value
         //txtUsername.setText("themos.k");

         // TEMP - Set button visible
         //btnDownloadProfilePic.setVisibility(View.VISIBLE);

         // Load an ad into the AdMob banner view.
         AdView adView = (AdView) findViewById(R.id.adView);
         AdRequest adRequest = new AdRequest.Builder()
                 //.addTestDevice("0109EABF5055E4716546558907BEA085") // REMOVE THIS IN PROD!!!!!!!
                 .build();
         adView.loadAd(adRequest);

         // New
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             getPermissions();
         }

         btnOpenGallery =(Button) findViewById(R.id.btnOpenGallery);
         btnOpenGallery.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent openGalleryActivity = new Intent(MainActivity.this, GalleryActivity.class);

                 openGalleryActivity.putExtra("appName", appName);

                 startActivity(openGalleryActivity);
             }
         });
     }

     /*************************************************** PERMISSIONS SOS! ************************************************/

     public static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1;

     // TODO: EXPLICITLY VERIFY/REQUEST PERMISSIONS
     // https://developer.android.com/training/permissions/requesting.html
     public void getPermissions() {

         // Here, thisActivity is the current activity
         if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

             // Should we show an explanation?
             //if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

             // Show an explanation to the user *asynchronously* -- don't block
             // this thread waiting for the user's response! After the user
             // sees the explanation, try again to request the permission.

             //} else {

             // No explanation needed, we can request the permission.

             ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
             //}
         }
     }

     @Override
     public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
         switch (requestCode) {
             case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                 // If request is cancelled, the result arrays are empty.
                 if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                     // permission was granted, yay! Do the storage-related task you need to do.
                     btnDownloadProfilePic.setEnabled(true);
                     btnDownloadImages.setEnabled(true);

                 } else {

                     // permission denied, boo! Disable the functionality that depends on this permission.
                     btnDownloadProfilePic.setEnabled(false);
                     btnDownloadImages.setEnabled(false);
                     Toast.makeText(getApplicationContext(), "Please enable 'STORAGE' permission in Settings > Apps > Pic Downloader > Permissions", Toast.LENGTH_LONG).show(); // TOAST!
                 }
                 return;
             }

             // other 'case' lines to check for other permissions this app might request
         }
     }

     /********************************************************************************************************************/

     private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
     private long mBackPressed;

     @Override
     public void onBackPressed() {
         if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
         {
             super.onBackPressed();
             return;
         }
         else {
             Toast.makeText(getBaseContext(), "Press again to exit", Toast.LENGTH_SHORT).show(); // TOAST!
         }

         mBackPressed = System.currentTimeMillis();
     }

     /***************************************************************************/

     public class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
         ImageView imageView;

         ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

         @Override
         protected void onPreExecute() {
             // Set message of the dialog
             asyncDialog.setMessage("Loading image...");
             // Show dialog
             asyncDialog.show();
         }

         public LoadImageTask(ImageView bmImage) {
             this.imageView = bmImage;
         }

         protected Bitmap doInBackground(String... urls) {
             String urldisplay = urls[0];
             Bitmap mIcon11 = null;
             try {
                 InputStream in = new java.net.URL(urldisplay).openStream();
                 mIcon11 = BitmapFactory.decodeStream(in);
             } catch (Exception e) {
                 Log.e("Error", e.getMessage());
                 e.printStackTrace();
             }
             return mIcon11;
         }

         protected void onPostExecute(Bitmap result) {

             // Hide the dialog
             asyncDialog.dismiss();

             imageView.setBackgroundColor(Color.parseColor("#ffffff"));
             imageView.setImageBitmap(result);
         }
     }

     /***************************************************************************/

     public class DownloadSourceCodeTask extends AsyncTask<String, Void, String> {

         ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

         @Override
         protected void onPreExecute() {
             // Set message of the dialog
             asyncDialog.setMessage("Processing request...");
             // Show dialog
             asyncDialog.show();
         }

         @Override
         protected String doInBackground(String... urls) {

             URL url;
             HttpURLConnection urlConnection = null;

             try {
                 url = new URL(urls[0]);
                 urlConnection = (HttpURLConnection) url.openConnection();

                 int responseCode = urlConnection.getResponseCode();

                 if(responseCode == HttpURLConnection.HTTP_OK){
                     return readStream(urlConnection.getInputStream()); // HAPPY PATH!
                 }
                 else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                     return "Error: user not found";
                 }

             } catch (MalformedURLException e) {
                 e.printStackTrace();
                 return "Error: Malformed URL Exception";
             } catch (IOException e) {
                 e.printStackTrace();
                 return "Error: Network Exception";
             }

             return null;
         }

         private String readStream(InputStream in) {
             BufferedReader reader = null;
             StringBuffer response = new StringBuffer();
             try {
                 reader = new BufferedReader(new InputStreamReader(in));
                 String line = "";
                 while ((line = reader.readLine()) != null) {
                     response.append(line);
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 if (reader != null) {
                     try {
                         reader.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
             return response.toString();
         }

         @Override
         protected void onPostExecute(String s) {

             // Hide the dialog
             asyncDialog.dismiss();

             if (s == null) {
                 Toast.makeText(getApplicationContext(), "Failed to process request", Toast.LENGTH_LONG).show(); // TOAST!
                 return;
             }
             else if (s.startsWith(("Error:"))) {
                 Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show(); // TOAST!
                 return;
             }

             //Toast.makeText(getApplicationContext(), "Request processed successfully", Toast.LENGTH_LONG).show(); // TOAST!

             // Source code retrieval successful
             // Proceed with pattern matching

             // Match profile pic url
             profilePicUrl = "";
             Pattern imagePattern = Pattern.compile("<meta +property=\\\"og:image\\\" +content=\\\"(http.+?)\\\"");
             Matcher imageMatcher = imagePattern.matcher(s);

             // Handle video
             Pattern videoPattern = Pattern.compile("<meta +property=\\\"og:video\\\" +content=\\\"(http.+?)\\\"");
             Matcher videoMatcher = videoPattern.matcher(s);

             // New - Handle hd profile pic (320x320) :-(
             Pattern hdProfilePattern = Pattern.compile("\\\"profile_pic_url_hd\\\":\\\"(https.+?)\\\"");
             Matcher hdProfileMatcher = hdProfilePattern.matcher(s);

             // Image matcher will match both Image and Video!
             if (imageMatcher.find()) {

                 //profilePicUrl = "https://instagram.fnic3-1.fna.fbcdn.net/t51.2885-19/s150x150/16464703_427799464234112_4272271048130428928_a.jpg";
                 //String imgUrlFullSize = profilePicUrl.replaceFirst("\\/s.*\\/", "");

                 profilePicUrl = imageMatcher.group(1); // First capturing group <3

                 // download and show The image in a ImageView
                 new LoadImageTask(imgPreview).execute(profilePicUrl);

                 // New: check for vid + update pic url
                 if (videoMatcher.find()) {
                     profilePicUrl = videoMatcher.group(1); // First capturing group <3
                     btnDownloadProfilePic.setText("Download\nVideo");
                 }
                 else {
                     btnDownloadProfilePic.setText("Download\nPicture");

                     // New - check for hd profile match, and update pic url
                     if (hdProfileMatcher.find()) {
                         profilePicUrl = hdProfileMatcher.group(1); // First capturing group <3
                     }
                 }

                 // Set button visible
                 //btnDownloadProfilePic.setVisibility(View.VISIBLE);
                 //btnDownloadImages.setVisibility(View.VISIBLE);
                 btnDownloadProfilePic.setEnabled(true);

                 // NEW!!!

                 // Match x24 images
                 Pattern p2 = Pattern.compile("\"thumbnail_src\":?\"(https:.+?)\"");
                 Matcher matcher2 = p2.matcher(s);

                 // Clear pictures array
                 picturesUrlList.clear();

                 // Add urls to array
                 while (matcher2.find()) {
                     picturesUrlList.add(matcher2.group(1));
                 }

                 if (picturesUrlList.isEmpty()) {
                     btnDownloadImages.setEnabled(false);
                 }
                 else {
                     btnDownloadImages.setEnabled(true);
                 }

             } else {
                 Toast.makeText(getApplicationContext(), "Pattern matching failed", Toast.LENGTH_LONG).show(); // TOAST!

                 // Maybe: clear pic?

                 // Set buttons HIDDEN - not sure about this?
                 btnDownloadProfilePic.setEnabled(false);
                 btnDownloadImages.setEnabled(false);
             }
         }
     }

     /*************************************************************************/

    // Source: https://stackoverflow.com/questions/8654876/http-get-using-android-httpurlconnection
     public class ProcessRestResponseTask extends AsyncTask<String, Void, String> {

         ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

         @Override
         protected void onPreExecute() {
             // Set message of the dialog
             asyncDialog.setMessage("Loading random account...");
             // Show dialog
             asyncDialog.show();
         }

         @Override
         protected String doInBackground(String... urls) {

             URL url;
             HttpURLConnection urlConnection = null;

             try {
                 url = new URL(urls[0]);
                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setRequestMethod("GET");
                 urlConnection.setRequestProperty("Content-Type", "text/xml");

                 int responseCode = urlConnection.getResponseCode();

                 if(responseCode == HttpURLConnection.HTTP_OK){

                     // HAPPY PATH!
                     //InputStream responseBody = urlConnection.getInputStream();

                     BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                     StringBuilder sb = new StringBuilder();
                     String line;
                     if ((line = br.readLine()) != null) {
                         sb.append(line);
                     }
                     br.close();
                     return sb.toString();
                 }
                 else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                     return "Error: service not found";
                 }

             } catch (MalformedURLException e) {
                 e.printStackTrace();
                 return "Error: Malformed URL Exception";
             } catch (IOException e) {
                 e.printStackTrace();
                 return "Error: Network Exception";
             }

             return null;
         }

         @Override
         protected void onPostExecute(String s) {

             // Hide the dialog
             asyncDialog.dismiss();

             if (s == null) {
                 Toast.makeText(getApplicationContext(), "Failed to process request", Toast.LENGTH_LONG).show(); // TOAST!
                 return;
             }
             else if (s.startsWith(("Error:"))) {
                 Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show(); // TOAST!
                 return;
             }

             //Toast.makeText(getApplicationContext(), "Request processed successfully", Toast.LENGTH_LONG).show(); // TOAST!

             // Populate textbox and search programmatically
             txtUsername.setText(s);
             btnSearch.performClick();
         }
     }

     /***************************************************************************/

     public class ProcessMetadataTask extends AsyncTask<String, Void, String> {

         @Override
         protected String doInBackground(String... urls) {

             URL url;
             HttpURLConnection urlConnection = null;

             try {
                 url = new URL(urls[0]);
                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setRequestMethod("GET");
                 urlConnection.setRequestProperty("Content-Type", "text/xml");

                 int responseCode = urlConnection.getResponseCode();

                 if (responseCode == HttpURLConnection.HTTP_OK) {

                     // HAPPY PATH!
                     return "Success";

                 } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                     return "Error: service not found";
                 }

             } catch (MalformedURLException e) {
                 e.printStackTrace();
                 return "Error: Malformed URL Exception";
             } catch (IOException e) {
                 e.printStackTrace();
                 return "Error: Network Exception";
             }

             return null;
         }
     }

     /***************************************************************************/

     public class DownloadImageTask extends AsyncTask<String, Void, String> {

         @Override
         protected void onPreExecute() {
         }

         @Override
         protected String doInBackground(String... params) {

             String filePath;

             // Moved here to make it more reusable
             //String imgUrlFullSize = params[0].replaceFirst("\\/s[0-9]+.*\\/", "/"); // Old code
             String imgUrlFullSize = params[0];//.replaceFirst("vp+.*\\/", "vp/"); // New code
             String fileName = params[1];
             String index  = params[2];

             try {
                 URL url = new URL(imgUrlFullSize);
                 HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                 connection.setDoInput(true);
                 connection.connect();
                 InputStream input = connection.getInputStream();

                 // New 29/09/2017
                 String path = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                 File myDir = new File(path, appName);
                 myDir.mkdirs(); // Maybe check for false??

                 //String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                 //filePath = String.format("%s/%s (%s).jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES), urls[1], date);
                 //filePath = String.format("%s/%s/%s (%s).jpg", path, appName, urls[1], date);
                 //filePath = String.format("%s/%s %s.jpg", path, userName, date);
                 filePath = String.format("%s/%s/%s", path, appName, fileName);

                 // Define file using full path
                 File pic = new File(filePath);

                 // Only download if file doesn't exist!
                 if (!pic.exists()) {

                     //if (imgUrlFullSize.endsWith(".mp4")) {

                         FileOutputStream stream = new FileOutputStream(pic);
                         byte[] buffer = new byte[1024];
                         int len1;
                         while ((len1 = input.read(buffer)) != -1) {
                             stream.write(buffer, 0, len1);
                         }
                         stream.close();
                         input.close();
                     //}
                    //else { // JPG

                    //     FileOutputStream stream = new FileOutputStream(pic);
                    //     Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    //     myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    //     stream.flush();
                    //     stream.close();
                    //     input.close();
                    //}
                 }

                 // Return the path so it can be opened within onPostExecute()
                 return filePath + "#" + index;

             } catch (Exception e) {
                 e.printStackTrace();
             }

             return null;
         }

         @Override
         protected void onPostExecute(String s) {

             if (s == null) {
                 Toast.makeText(getApplicationContext(), "Image failed to download", Toast.LENGTH_SHORT).show(); // TOAST!
                 Toast.makeText(getApplicationContext(), "Our development team has been notified of the issue, we will try to resolve it as soon as possible", Toast.LENGTH_LONG).show(); // TOAST!

                 // Not sure if I want to also log the user input?
                 String errorUrl = "http://centraldbwebapi.azurewebsites.net/api/error/";// + userInput;
                 try {
                     new ProcessMetadataTask().execute(errorUrl);
                 } catch (Exception e) {
                     Log.d("Error", e.getMessage());
                 }

                 dialog.dismiss();
             } else {
                 /*
                 // Doesn't work?
                 notifyMediaScanner(s);
                */

                 String path = s.split("#")[0];
                 String index = s.split("#")[1];

                 // Index will be 1 if profile pic, or when the LAST image in the array has been downloaded
                 if (index.equals("1")) {
                     dialog.dismiss();
                     Toast.makeText(getApplicationContext(), "Download completed successfully", Toast.LENGTH_SHORT).show(); // TOAST!
                     btnOpenGallery.performClick();
                 }

                 //if (path.contains("profile pic")) {
                     //Toast.makeText(getApplicationContext(), "Image downloaded successfully!", Toast.LENGTH_SHORT).show(); // TOAST!
                     //openImage(path);

                     //btnOpenGallery.performClick();
                 //}
             }
         }

         public void openImage(String filePath) {

             try {
                 // Start new Intent to open the newly created image
                 // Problem #1: image doesn't show up in Gallery - fixed
                 // Problem #2: would be nice if you could swipe left/right to see previous images
                 Intent intent = new Intent();
                 intent.setAction(Intent.ACTION_VIEW);
                 intent.setDataAndType(Uri.parse("file://" + filePath), "image/*");
                 //intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION); //must for reading data from directory
                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                 startActivity(intent);

             } catch (Exception e) {
                 e.printStackTrace();

                 Toast.makeText(getApplicationContext(), "Image saved to Gallery!", Toast.LENGTH_LONG).show(); // TOAST!
             }
         }
         public void notifyMediaScanner(String filePath) {

             try {
                 if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                     getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                             Uri.parse("file://" + getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))));
                 } else {
                     MediaScannerConnection.scanFile(MainActivity.this,
                             new String[]{filePath}, null,
                             new MediaScannerConnection.OnScanCompletedListener() {
                                 public void onScanCompleted(String path, Uri uri) {
                                     Log.i("ExternalStorage", "Scanned " + path + ":");
                                     Log.i("ExternalStorage", "-> uri=" + uri);
                                 }
                             });
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }

    /***************************************************************************/
 }