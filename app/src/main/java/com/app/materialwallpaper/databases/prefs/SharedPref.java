package com.app.materialwallpaper.databases.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.models.Menu;
import com.app.materialwallpaper.utils.Constant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class SharedPref {

    private static final String P_SEC = "sjB*74bj#";
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setBaseUrl(String baseUrl) {
        editor.putString("base_url", baseUrl);
        editor.apply();
    }

    public String getBaseUrl() {
        return sharedPreferences.getString("base_url", "http://10.0.2.2/material_wallpaper");

    }

    public void saveConfig( String privacyPolicy, String moreAppsUrl, String copyright) {
        editor.putString("privacy_policy", privacyPolicy);
        editor.putString("more_apps_url", moreAppsUrl);
        editor.putString("copyright", copyright);
        editor.apply();
    }

    public String getPrivacyPolicy() {
        return sharedPreferences.getString("privacy_policy", "");
    }

    public String getCopyright() {
        return sharedPreferences.getString("copyright", "");
    }

    public String getMoreAppsUrl() {
        return sharedPreferences.getString("more_apps_url", "");
    }

    public void saveGif(String gifPath, String gifName) {
        editor.putString("gif_path", gifPath);
        editor.putString("gif_name", gifName);
        editor.apply();
    }

    public void saveMp4(String mp4Path, String mp4Name) {
        editor.putString("mp4_path", mp4Path);
        editor.putString("mp4_name", mp4Name);
        editor.apply();
    }

    public String getGifPath() {
        return sharedPreferences.getString("gif_path", "0");
    }

    public String getGifName() {
        return sharedPreferences.getString("gif_name", "0");
    }

    public String getMp4Path() {
        return sharedPreferences.getString("mp4_path", "0");
    }

    public String getMp4Name() {
        return sharedPreferences.getString("mp4_name", "0");
    }

    public Integer getDisplayPosition(int default_value) {
        return sharedPreferences.getInt("display_position", default_value);
    }

    public void updateDisplayPosition(int position) {
        editor.putInt("display_position", position);
        editor.apply();
    }

    public Integer getWallpaperColumns() {
        return sharedPreferences.getInt("wallpaper_columns", Config.DEFAULT_WALLPAPER_COLUMN);
    }

    public void updateWallpaperColumns(int columns) {
        editor.putInt("wallpaper_columns", columns);
        editor.apply();
    }

    public void setDefaultSortWallpaper() {
        editor.putInt("sort_act", Constant.SORT_RECENT);
        editor.apply();
    }

    public Integer getCurrentSortWallpaper() {
        return sharedPreferences.getInt("sort_act", 0);
    }

    public void updateSortWallpaper(int position) {
        editor.putInt("sort_act", position);
        editor.apply();
    }

    public Boolean getIsDarkTheme() {
        return true;//sharedPreferences.getBoolean("theme", Config.SET_DARK_MODE_AS_DEFAULT_THEME);
    }

    public void setIsDarkTheme(Boolean isDarkTheme) {
        editor.putBoolean("theme", isDarkTheme);
        editor.apply();
    }

    public Boolean getIsPremium() {
        return getSha256(P_SEC).equals(sharedPreferences.getString("p_u", "NA"));
    }

    private String getSha256(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
//    public void setIsPremium(Boolean isPremium) {
//        if(isPremium) {
//            editor.putString("p_u", getSha256(P_SEC));
//        } else {
//            editor.putString("p_u", "NA");
//        }
//        editor.apply();
//    }

    public Boolean getIsNotification() {
        return sharedPreferences.getBoolean("noti", true);
    }

    public void setIsNotification(Boolean isNotification) {
        editor.putBoolean("noti", isNotification);
        editor.apply();
    }

//    public Integer getAppOpenToken() {
//        return sharedPreferences.getInt("app_open_token", 0);
//    }
//
//    public void updateAppOpenToken(int value) {
//        editor.putInt("app_open_token", value);
//        editor.apply();
//    }

    public Integer getInAppReviewToken() {
        return sharedPreferences.getInt("in_app_review_token", 0);
    }

    public void updateInAppReviewToken(int value) {
        editor.putInt("in_app_review_token", value);
        editor.apply();
    }

    public void saveMenuList(List<Menu> apps) {
        Gson gson = new Gson();
        String json = gson.toJson(apps);
        editor.putString("menu", json);
        editor.apply();
    }

    public List<Menu> getMenuList() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("menu", null);
        Type type = new TypeToken<ArrayList<Menu>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

}
