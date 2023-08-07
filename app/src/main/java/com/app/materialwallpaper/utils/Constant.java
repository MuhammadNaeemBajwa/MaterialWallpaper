package com.app.materialwallpaper.utils;

import com.app.materialwallpaper.models.Wallpaper;

import java.util.ArrayList;

public class Constant {

    public static final int DELAY_TIME = 50;
    public static final int DELAY_SET = 2500;
    public static final int LOAD_MORE_2_COLUMNS = 4; // 20 to 4 as per client's requirement
    public static final int LOAD_MORE_3_COLUMNS = 9; // 24 to 9
    public static final int NATIVE_AD_INDEX_2_COLUMNS = 6;
    public static final int NATIVE_AD_INDEX_3_COLUMNS = 9;
    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    public static final String FILTER_FREE = "g.price != 'premium'";
    public static final String FILTER_PREMIUM = "g.price = 'premium'";

    public static ArrayList<Wallpaper> wallpapers = new ArrayList<>();
    public static int position = 0;
    public static final int THUMBNAIL_WIDTH = 250;
    public static final int THUMBNAIL_HEIGHT = 375;
    public static final String NATIVE_SIZE_MEDIUM = "default";
    public static final String NATIVE_SIZE_LARGE = "large";

    //do not make any changes to the code below
    public static String FILTER = "";
    public static final String FILTER_DEFAULT = "g.image_extension != 'all'";
    public static final String FILTER_ALL = "g.image_extension != 'all'";
    public static final String FILTER_WALLPAPER = "(g.image_extension != 'image/gif' AND g.image_extension != 'application/octet-stream')";
    public static final String FILTER_LIVE = "(g.image_extension = 'image/gif' OR g.image_extension = 'application/octet-stream')";

    public static String ORDER = "";
    public static final String ORDER_DEFAULT = "ORDER BY g.id DESC";
    public static final String ORDER_RECENT = "ORDER BY g.id DESC";
    public static final String ORDER_FEATURED = "AND g.featured = 'yes' ORDER BY g.last_update DESC";
    public static final String ORDER_POPULAR = "ORDER BY g.view_count DESC";
    public static final String ORDER_RANDOM = "ORDER BY RAND()";
    public static final String ORDER_LIVE = "ORDER BY g.id DESC ";

    public static int LAST_SELECTED_ITEM_POSITION = 0;

    public static final int DISPLAY_WALLPAPER_RECTANGLE = 1;
    public static final int DISPLAY_WALLPAPER_SQUARE = 2;
    public static final int DISPLAY_WALLPAPER_DYNAMIC = 3;

    public static final int SORT_RECENT = 0;
    public static final int SORT_FEATURED = 1;
    public static final int SORT_POPULAR = 2;
    public static final int SORT_RANDOM = 3;
    public static final int SORT_LIVE = 4;

    public static final int CATEGORY_COLUMNS = 1;
    public static final int WALLPAPER_TWO_COLUMNS = 2;
    public static final int WALLPAPER_THREE_COLUMNS = 3;

    public static final String DOWNLOAD = "download";
    public static final String SHARE = "share";
    public static final String SET_WITH = "setWith";
    public static final String SET_GIF = "setGif";
    public static final String SET_MP4 = "setMp4";

    public static final String HOME_SCREEN = "home_screen";
    public static final String LOCK_SCREEN = "lock_screen";
    public static final String BOTH = "both";

    public static String gifName = "";
    public static String gifPath = "";

    public static String mp4Name = "";
    public static String mp4Path = "";

    public static final String LOCALHOST_ADDRESS = "http://192.168.1.2";

    public static final int IMMEDIATE_APP_UPDATE_REQ_CODE = 124;

    public static boolean isAppOpen = false;

}
