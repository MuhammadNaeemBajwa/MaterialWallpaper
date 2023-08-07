package com.app.materialwallpaper.models;

import java.io.Serializable;

public class Wallpaper implements Serializable {

    public static final String TYPE_IMAGE = "image";
    public static String TYPE_VIDEO = "video";

    public boolean isVideoWallpaper() {
        return TYPE_VIDEO.equalsIgnoreCase(type);
    }

    public enum Price {
        FREE("Free"),
        PREMIUM("Premium"),
        ALL("All");

        public String value;

        Price(String value) {
            this.value = value;
        }
    }

    public String image_id;
    public String image_name;
    public String image_thumb;
    public String image_upload;
    public String image_url;
    public String type;
    public String resolution;
    public String size;
    public String mime;
    public int views;
    public int downloads;
    public String featured;
    public String tags;
    public String category_id;
    public String category_name;
    public String last_update;
    public String price;


    @Override
    public String toString() {
        return "Wallpaper{" +
                "image_id='" + image_id + '\'' +
                ", image_name='" + image_name + '\'' +
                ", price='" + price + '\'' +
                '}';
    }

    public boolean isPremium() {
        return Price.PREMIUM.value.equalsIgnoreCase(price);
    }

}
