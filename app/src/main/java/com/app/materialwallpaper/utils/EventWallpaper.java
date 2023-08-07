package com.app.materialwallpaper.utils;

import com.app.materialwallpaper.models.Wallpaper;

public class EventWallpaper {
    public static final String ACTION_DELETE_WALLPAPER = "delete_wallpaper";
    private Wallpaper wallpaper;
    private String action;

    public EventWallpaper(Wallpaper wallpaper, String action) {
        this.wallpaper = wallpaper;
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public Wallpaper getWallpaper() {
        return wallpaper;
    }
}
