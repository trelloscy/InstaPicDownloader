package com.mediadownloader.picdownloader;

public class CreateList {

    private String image_title;
    private Integer image_id;
    private String image_path;

    public String getImage_title() {
        return image_title;
    }

    public void setImage_title(String name) {
        this.image_title = name;
    }

    public Integer getImage_ID() {
        return image_id;
    }

    public void setImage_ID(Integer android_image_url) {
        this.image_id = android_image_url;
    }

    public void setImage_Location(String path) {
        this.image_path = path;
    }

    public String getImage_Location() {
        return image_path;
    }
}