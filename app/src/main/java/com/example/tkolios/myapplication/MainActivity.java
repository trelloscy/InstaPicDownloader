package com.example.tkolios.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
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
import java.net.URL;
import java.io.*;
import java.net.HttpURLConnection;
import android.app.ProgressDialog;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static android.os.Environment.getExternalStoragePublicDirectory;

 public class MainActivity extends AppCompatActivity {

    Button btn;
    EditText txtUsername;
    String imgUrl = "";
    Button btnDownloadImage;

    public void downloadSourceCode_Click(View v) {

        // Check for empty username
        String strUserName = txtUsername.getText().toString();
        if(TextUtils.isEmpty(strUserName)) {
            txtUsername.setError("Please specify a valid username");
            return;
        }

        //Toast.makeText(getApplicationContext(), "Button clicked!", Toast.LENGTH_SHORT).show();

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        // Define instagram url
        String url = "https://www.instagram.com/" + strUserName + "/";

        try {
            // Async retrieval of html source
            new DownloadSourceCodeTask().execute(url);
        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
        }

        /*
         * Test code
         *
        Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
        //webView.loadUrl(url);
        //webView.loadUrl("https://instagram.fnic3-1.fna.fbcdn.net/t51.2885-19/16464703_427799464234112_4272271048130428928_a.jpg");

        webView.loadDataWithBaseURL(null, "<html><head></head><body><table style=\"width:100%; height:100%;\"><tr><td style=\"vertical-align:middle;\"><img src=\"" + imgUrl + "\"></td></tr></table></body></html>", "html/css", "utf-8", null);
        */
    }

    public void downloadImage_Click(View v) {

        String strUserName = txtUsername.getText().toString();

        //Toast.makeText(getApplicationContext(), "Download Image button clicked!", Toast.LENGTH_LONG).show();

        String imgUrlFullSize = imgUrl.replaceFirst("\\/s[0-9]+.*\\/", "/");

        // TEMP
        //String filePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + strUserName + ".jpg";
        // openImage(filePath);

        /*
        // Might not work if photo is in a private path?
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.parse(filePath);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        */

        //Uri uri =  Uri.parse(filePath);
        //Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        //String mime = "*/*";
        //MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        //if (mimeTypeMap.hasExtension(
        //        mimeTypeMap.getFileExtensionFromUrl(uri.toString())))
        //    mime = mimeTypeMap.getMimeTypeFromExtension(
        //            mimeTypeMap.getFileExtensionFromUrl(uri.toString()));
        //intent.setDataAndType(uri,mime);
        //startActivity(intent);

        try {
            // Async retrieval of html source
            new DownloadImageTask2().execute(imgUrlFullSize, strUserName);
        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
        }
    }

    /*
     // Doesn't work?
     public final void notifyMediaStoreScanner(final File file) {
         try {
             MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), file.getAbsolutePath(), file.getName(), null);
             getApplicationContext().sendBroadcast(new Intent(
                     Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
     }
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.btnDownloadSourceCode);
        //webView = (WebView) findViewById(R.id.webView);
        //webView.setWebViewClient(new WebViewClient());
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        btnDownloadImage = (Button) findViewById(R.id.btnDownloadImage);

        // TEMP - set default value
        txtUsername.setText("themos.k");

        // TEMP - Set button visible
        btnDownloadImage.setVisibility(View.VISIBLE);

        /*
        // TEST - clear focus from textbox
        txtUsername.setFocusableInTouchMode(false);
        txtUsername.setFocusable(false);
        txtUsername.setFocusableInTouchMode(true);
        txtUsername.setFocusable(true);
        */

        /*
          // R.id to access all IDs from Resources
          Button btn = (Button)findViewById(R.id.btnPopup);
          btn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Toast.makeText(getApplicationContext(), "eisai o kalliteros", Toast.LENGTH_LONG).show();
              }
          });

        //webView.loadUrl("https://google.com");
        */
    }

    /***************************************************************************/

    public class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            // Set message of the dialog
            asyncDialog.setMessage("Loading image...");
            // Show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        public LoadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
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

            bmImage.setImageBitmap(result);
    }
 }

    /***************************************************************************/

 public class DownloadSourceCodeTask extends AsyncTask<String, Void, String> {

        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            // Set message of the dialog
            asyncDialog.setMessage("Decrypting Instagram source code...");
            // Show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            // Hide the dialog
            asyncDialog.dismiss();

            //super.onPostExecute(s);

            if (s == null) {
                Toast.makeText(getApplicationContext(), "Source code decryption failed", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(getApplicationContext(), "Source code decryption completed", Toast.LENGTH_LONG).show();

            // Source code retrieval successful
            // Proceed with pattern matching
            imgUrl = "";
            Pattern p = Pattern.compile("<meta +property=\\\"og:image\\\" +content=\\\"(http.+?)\\\"");
            Matcher matcher = p.matcher(s);

            //Toast.makeText(getApplicationContext(), "Pattern matching completed", Toast.LENGTH_LONG).show();

            if (matcher.find()) {
                imgUrl = matcher.group(1); // First capturing group <3

                //Toast.makeText(getApplicationContext(), imgUrl, Toast.LENGTH_LONG).show();

                //imgUrl = "https://instagram.fnic3-1.fna.fbcdn.net/t51.2885-19/s150x150/16464703_427799464234112_4272271048130428928_a.jpg";
                //String imgUrlFullSize = imgUrl.replaceFirst("\\/s.*\\/", "");

                // Download and show The image in a ImageView
                new LoadImageTask((ImageView) findViewById(R.id.imageView1)).execute(imgUrl);

                // Set button visible
                btnDownloadImage.setVisibility(View.VISIBLE);

            } else {
                Toast.makeText(getApplicationContext(), "Pattern matching failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    /***************************************************************************/

    public class DownloadImageTask extends AsyncTask<String, Void, String> {

        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            // Set message of the dialog
            asyncDialog.setMessage("Downloading Image...");
            // Show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {

            //File path = new File(Environment.getExternalStorageDirectory().getPath()+"/"+"InstaDownloader"+"/");
            //path.mkdirs();
            //File pic = new File(path, "themosk.jpg");

            /*
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Download");
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("App", "failed to create directory");
                    return null;
                }
            }
            */

            String filePath;

            try
            {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                //String pic = String.valueOf(String.format(Environment.getExternalStorageDirectory().getPath()+"/"+getPackageName()+"/%d.jpg", System.currentTimeMillis()));

                // New 19/09/2017
                //filePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + urls[1] + ".jpg";
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                filePath = String.format("%s/%s (%s).jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES), urls[1], date);

                File pic = new File(filePath);

                /*
                FileOutputStream foStream;
                try {
                    foStream = getApplicationContext().openFileOutput(String.format("%d.jpg", System.currentTimeMillis()), Context.MODE_PRIVATE);
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, foStream);
                    foStream.close();
                } catch (Exception e) {
                    Log.d("saveImage", "Exception 2, Something went wrong!");
                    e.printStackTrace();
                }
                */

                FileOutputStream stream = new FileOutputStream(pic);

                ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                byte[] byteArray = outstream.toByteArray();

                stream.write(byteArray);
                stream.close();

                // Return the path so it can be opened within onPostExecute()
                return filePath;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            // Hide the dialog
            asyncDialog.dismiss();

            if (s == null) {
                Toast.makeText(getApplicationContext(), "Image failed to download", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Image download completed", Toast.LENGTH_LONG).show();
                openImage(s);
            }
        }

        // Not sure in which class to include this function?
        public void openImage(String filePath) {

            // Start new Intent to open the newly created image
            // Problem #1: image doesn't show up in Gallery
            // Problem #2: would be nice if you could swipe left/right to see previous images
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + filePath), "image/*");
            //intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION); //must for reading data from directory
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

            // Doesn't work??
            //notifyMediaStoreScanner(new File(filePath));
        }
    }

    /***************************************************************************/

    public class DownloadImageTask2 extends AsyncTask<String, Void, String> {

        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            // Set message of the dialog
            asyncDialog.setMessage("Downloading Image...");
            // Show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {

            //File path = new File(Environment.getExternalStorageDirectory().getPath()+"/"+"InstaDownloader"+"/");
            //path.mkdirs();
            //File pic = new File(path, "themosk.jpg");

            /*
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Download");
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("App", "failed to create directory");
                    return null;
                }
            }
            */

            String filePath;

            try
            {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                //String pic = String.valueOf(String.format(Environment.getExternalStorageDirectory().getPath()+"/"+getPackageName()+"/%d.jpg", System.currentTimeMillis()));

                // New 19/09/2017
                //filePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + urls[1] + ".jpg";
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                filePath = String.format("%s/%s profile %s.jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES), urls[1], date);
                //filePath = String.format("%s/%s profile %s.jpg", getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), urls[1], date);

                // TEMP
                File pic = new File(filePath);

                /*
                File pic = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"InstaPicDownloader"
                );
                */

                /*
                FileOutputStream foStream;
                try {
                    foStream = getApplicationContext().openFileOutput(String.format("%d.jpg", System.currentTimeMillis()), Context.MODE_PRIVATE);
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, foStream);
                    foStream.close();
                } catch (Exception e) {
                    Log.d("saveImage", "Exception 2, Something went wrong!");
                    e.printStackTrace();
                }
                */

                FileOutputStream stream = new FileOutputStream(pic);

                ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                byte[] byteArray = outstream.toByteArray();

                stream.write(byteArray);
                stream.close();

                // Return the path so it can be opened within onPostExecute()
                return filePath;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            // Hide the dialog
            asyncDialog.dismiss();

            if (s == null) {
                Toast.makeText(getApplicationContext(), "Image failed to download", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Image download completed", Toast.LENGTH_LONG).show();
                galleryAddPic(s);
                openImage(s);
            }
        }

        // Not sure in which class to include this function?
        public void openImage(String filePath) {

            // Start new Intent to open the newly created image
            // Problem #1: image doesn't show up in Gallery
            // Problem #2: would be nice if you could swipe left/right to see previous images
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + filePath), "image/*");
            //intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION); //must for reading data from directory
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

            // Doesn't work??
            //notifyMediaStoreScanner(new File(filePath));
        }

        public void galleryAddPic(String filePath) {

            /*
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.parse("file://" + filePath);
            //Uri contentUri = Uri.parse(filePath);
            mediaScanIntent.setData(contentUri);
                MainActivity.this.sendBroadcast(mediaScanIntent);
            */

                //MediaScannerConnection.scanFile(MainActivity.this, new String[] { "file://" + filePath }, new String[] { "image/jpeg" }, null);

            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File("file://" + getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);
            }
            else
            {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + getExternalFilesDir(Environment.DIRECTORY_PICTURES))));
            }
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + getExternalFilesDir(Environment.DIRECTORY_PICTURES))));
            */

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            } else {
                MediaScannerConnection.scanFile(MainActivity.this,
                        new String[] { filePath }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });
            }
        }
    }

     /***************************************************************************/
}
