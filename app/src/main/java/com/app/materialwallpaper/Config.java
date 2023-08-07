package com.app.materialwallpaper;

import com.app.materialwallpaper.utils.Constant;

public class Config {

    //server key obtained from the admin panel
    public static final String SERVER_KEY = "WVVoU01HTklUVFpNZVRsM1lqSk9jbHBZVW1oalNFSm9Xa2N4Y0dKcE5XcGlNakIyV0RKR2QyTkhlSEJaTWtZd1lWYzVkVk5YVW1aWk1qbDBURzFHZDJORE5YZGlNazV5V2xoU00xbFhlSE5qUjBaM1dsaEpQUT09";

    //default theme in first launch
    public static final boolean SET_DARK_MODE_AS_DEFAULT_THEME = false;

    //if true, all ad unit ids are configured from ads.xml
    public static final boolean ENABLE_OFFLINE_ADS_MODE = true;

    //column count
    public static final int DEFAULT_WALLPAPER_COLUMN = Constant.WALLPAPER_TWO_COLUMNS;
    public static final int DEFAULT_CATEGORY_COLUMN = Constant.CATEGORY_COLUMNS;

    //UI Config
    public static final boolean ENABLE_CENTER_CROP_IN_DETAIL_WALLPAPER = true;
    public static final boolean ENABLE_DISPLAY_WALLPAPER_NAME = true;
    public static final boolean ENABLE_DISPLAY_WALLPAPER_CATEGORY = true;
    public static final boolean ENABLE_WALLPAPER_COUNT_ON_CATEGORY = true;

    //display grid wallpaper style
    public static final int DISPLAY_WALLPAPER = Constant.DISPLAY_WALLPAPER_RECTANGLE;

    //set category as main screen
    public static final boolean DISPLAY_CATEGORY_AS_MAIN_SCREEN = false;

    //RTL Mode
    public static final boolean ENABLE_RTL_MODE = false;

    //Show dialog close app
    public static final boolean ENABLE_EXIT_DIALOG = true;

    //GDPR Consent
    public static final boolean LEGACY_GDPR = true;

    //splash duration
    public static final int DELAY_SPLASH = 2000;

    //set false if you don't want the app accessed using vpn
    public static final boolean ALLOW_VPN_ACCESS = false;

}
