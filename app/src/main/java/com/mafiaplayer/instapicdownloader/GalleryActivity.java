package com.mafiaplayer.instapicdownloader;

import android.content.Intent;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class GalleryActivity extends AppCompatActivity {

    String appName;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        // SOS
        Intent callerIntent = getIntent();
        appName = callerIntent.getStringExtra("appName");

        // Load an ad into the AdMob banner view 2.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("0109EABF5055E4716546558907BEA085") // REMOVE THIS IN PROD!!!!!!!
                .build();
        adView.loadAd(adRequest);

        try {
            recyclerView = (RecyclerView)findViewById(R.id.imagegallery);
            recyclerView.setHasFixedSize(false);

            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 4);
            recyclerView.setLayoutManager(layoutManager);

            reloadAdapter();

            //mSwipeRefreshLayout.setDistanceToTriggerSync(10);// in dips
            //mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);// LARGE also can be used

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    reloadAdapter();
                }
            });
        }
        catch (Exception ex) {
            // Do nothing?
        }
    }

    // Public function
    public void reloadAdapter() {

        ArrayList<CreateList> createLists = prepareData();
        MyAdapter adapter = new MyAdapter(GalleryActivity.this, createLists);
        recyclerView.setAdapter(adapter);

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        if (createLists.size() == 0) {
            Toast.makeText(getApplicationContext(), "Gallery is empty", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "Go back and download some photos!", Toast.LENGTH_SHORT).show();
        }
    }

    // Get list of photos
    private ArrayList<CreateList> prepareData(){

        String path = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        //String extension = ".jpg";
        File f = new File(path, appName);
        f.mkdirs(); // SOS: Make sure dir exists!
        File file[] = f.listFiles();

        // New - sort files by date
        Arrays.sort(file, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });

        ArrayList<CreateList> imageList = new ArrayList<>();
        for (int i=0; i < file.length; i++)
        {
            // (file[i].getName().endsWith(extension)) {
                CreateList createList = new CreateList();
                createList.setImage_title(file[i].getName());
                createList.setImage_Location(file[i].getAbsolutePath());
                imageList.add(createList);
            //}
        }
        return imageList;
    }
}