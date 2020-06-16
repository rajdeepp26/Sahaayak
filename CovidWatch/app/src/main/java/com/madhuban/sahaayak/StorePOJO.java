package com.madhuban.sahaayak;

import java.io.Serializable;

public class StorePOJO implements Serializable {


    public String storeName;
    public String  image;
    public String location;
    public String uploader;
    public String time;

    public StorePOJO() {
    }

    public StorePOJO(String name, String image, String location, String uploader, String time) {
        this.storeName = name;
        this.image = image;
        this.location = location;
        this.uploader = uploader;
        this.time = time;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setTitle(String title) {
        this.storeName = storeName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String album) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String artist) {
        this.location = location;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.storeName = time;
    }

}