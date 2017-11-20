package com.mafiaplayer.instapicdownloader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<CreateList> galleryList;
    private Context context;

    public MyAdapter(Context context, ArrayList<CreateList> galleryList) {
        this.galleryList = galleryList;
        this.context = context;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder viewHolder, int i) {

        File f = new File(galleryList.get(i).getImage_Location());

        Glide
                .with(context)
                .load(Uri.fromFile(f))
                .override(140, 140)

                //.thumbnail(0.5f)

                .skipMemoryCache(true)
                //.diskCacheStrategy(DiskCacheStrategy.NONE)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //use this to cache
                .centerCrop()
                //.fitCenter()
                .crossFade()
                //.placeholder(R.drawable.placeholder)
                //.error(R.drawable.imagenotfound)
                .into(viewHolder.img);
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener /*, View.OnLongClickListener*/ {
        private TextView title;
        private ImageView img;

        public ViewHolder(View view) {
            super(view);

            title = (TextView)view.findViewById(R.id.title);
            img = (ImageView) view.findViewById(R.id.img);
            img.setOnClickListener(this);
            img.setOnCreateContextMenuListener(this);
            //img.setOnLongClickListener(this);
        }

        /*
        @Override
        public boolean onLongClick(View view) {

            int position = getAdapterPosition();
            Toast.makeText(context, galleryList.get(position).getImage_title(), Toast.LENGTH_LONG).show();

            return true;
        }
        */

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {

            menu.setHeaderTitle("Select Action");

            // Edit
            menu.add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    try {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {

                            File file = new File(galleryList.get(position).getImage_Location());
                            String filePath = "file://" + galleryList.get(position).getImage_Location();

                            final Intent intent = new Intent(Intent.ACTION_EDIT)
                                    .setDataAndType(
                                            Build.VERSION.SDK_INT > Build.VERSION_CODES.M
                                                    ? android.support.v4.content.FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file)
                                                    : Uri.parse(filePath), "image/*")
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(intent);
                        }
                    }
                    catch (Exception ex) { }
                    return true;
                }
            });

            // Share
            menu.add("Share").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    try {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {

                            File file = new File(galleryList.get(position).getImage_Location());
                            String filePath = "file://" + galleryList.get(position).getImage_Location();

                            final Intent intent = new Intent(Intent.ACTION_SEND)
                                    .setDataAndType(
                                            Build.VERSION.SDK_INT > Build.VERSION_CODES.M
                                                    ? android.support.v4.content.FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file)
                                                    : Uri.parse(filePath), "image/*")
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(intent);
                        }
                    }
                    catch (Exception ex) { }
                    return true;
                }
            });

            // Delete
            menu.add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    try {
                        final int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {

                            // TODO: show confirmation?
                            //new AlertDialog.Builder(context).setTitle("Confirm Delete")
                                    //.setMessage("Do you want to delete this blank?")
                                    //.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    //    @Override
                                    //    public void onClick(DialogInterface dialogInterface, int i) {

                                            // Delete the selected file
                                            File file = new File(galleryList.get(position).getImage_Location());
                                            file.delete();

                                            // TODO: Refresh gallery
                                            if(context instanceof GalleryActivity){
                                                ((GalleryActivity)context).reloadAdapter();
                                            }

                            //}
                                    //})
                                    //.setNeutralButton("Cancel", null) // don't need to do anything but dismiss here
                                    //.create()
                                    //.show();
                        }
                    }
                    catch (Exception ex) { }
                    return true;
                }
            });
        }

        @Override
        public void onClick(View view) {

            try {

                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {

                    //Toast.makeText(context, galleryList.get(position).getImage_title(), Toast.LENGTH_SHORT).show();

                    File file = new File(galleryList.get(position).getImage_Location());
                    String filePath = "file://" + galleryList.get(position).getImage_Location();

                    // New
                    String mimeType = (filePath.endsWith(".mp4")) ? "video/*" : "image/*";

                    final Intent intent = new Intent(Intent.ACTION_VIEW)
                            .setDataAndType(
                                        Build.VERSION.SDK_INT > Build.VERSION_CODES.M
                                        ? android.support.v4.content.FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file)
                                        : Uri.parse(filePath), mimeType)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent);
                }

                /*
                 * Open photo inside imageView in a new Intent
                if(position != RecyclerView.NO_POSITION) {
                    SpacePhoto spacePhoto = mSpacePhotos[position];
                    Intent intent = new Intent(context, SpacePhotoActivity.class);
                    intent.putExtra(SpacePhotoActivity.EXTRA_SPACE_PHOTO, spacePhoto);
                    startActivity(intent);
                }
                */
            }
            catch (Exception ex) {
                Toast.makeText(context, "Failed to open image, try opening from Gallery app", Toast.LENGTH_SHORT).show();
            }
        }
    }
}