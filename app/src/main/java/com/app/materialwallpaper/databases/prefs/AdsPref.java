package com.app.materialwallpaper.databases.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class AdsPref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public AdsPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("ads_setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveAds(String ad_status, String ad_type, String backup_ads, String admob_publisher_id, String admob_banner_unit_id, String admob_interstitial_unit_id, String admob_native_unit_id, String admob_app_open_ad_unit_id, String ad_manager_banner_unit_id, String ad_manager_interstitial_unit_id, String ad_manager_native_unit_id, String ad_manager_app_open_ad_unit_id, String fan_banner_unit_id, String fan_interstitial_unit_id, String fan_native_unit_id, String startapp_app_id, String unity_game_id, String unity_banner_placement_id, String unity_interstitial_placement_id, String applovin_banner_ad_unit_id, String applovin_interstitial_ad_unit_id, String applovin_native_ad_manual_unit_id, String applovin_app_open_ad_unit_id, String applovin_banner_zone_id, String applovin_interstitial_zone_id, String ironsource_app_key, String ironsource_banner_id, String ironsource_interstitial_id, int interstitial_ad_interval) {
        editor.putString("ad_status", ad_status);
        editor.putString("ad_type", ad_type);
        editor.putString("backup_ads", backup_ads);
        editor.putString("admob_publisher_id", admob_publisher_id);
        editor.putString("admob_banner_unit_id", admob_banner_unit_id);
        editor.putString("admob_interstitial_unit_id", admob_interstitial_unit_id);
        editor.putString("admob_native_unit_id", admob_native_unit_id);
        editor.putString("admob_app_open_ad_unit_id", admob_app_open_ad_unit_id);
        editor.putString("ad_manager_banner_unit_id", ad_manager_banner_unit_id);
        editor.putString("ad_manager_interstitial_unit_id", ad_manager_interstitial_unit_id);
        editor.putString("ad_manager_native_unit_id", ad_manager_native_unit_id);
        editor.putString("ad_manager_app_open_ad_unit_id", ad_manager_app_open_ad_unit_id);
        editor.putString("fan_banner_unit_id", fan_banner_unit_id);
        editor.putString("fan_interstitial_unit_id", fan_interstitial_unit_id);
        editor.putString("fan_native_unit_id", fan_native_unit_id);
        editor.putString("startapp_app_id", startapp_app_id);
        editor.putString("unity_game_id", unity_game_id);
        editor.putString("unity_banner_placement_id", unity_banner_placement_id);
        editor.putString("unity_interstitial_placement_id", unity_interstitial_placement_id);
        editor.putString("applovin_banner_ad_unit_id", applovin_banner_ad_unit_id);
        editor.putString("applovin_interstitial_ad_unit_id", applovin_interstitial_ad_unit_id);
        editor.putString("applovin_native_ad_manual_unit_id", applovin_native_ad_manual_unit_id);
        editor.putString("applovin_app_open_ad_unit_id", applovin_app_open_ad_unit_id);
        editor.putString("applovin_banner_zone_id", applovin_banner_zone_id);
        editor.putString("applovin_interstitial_zone_id", applovin_interstitial_zone_id);
        editor.putString("ironsource_app_key", ironsource_app_key);
        editor.putString("ironsource_banner_id", ironsource_banner_id);
        editor.putString("ironsource_interstitial_id", ironsource_interstitial_id);
        editor.putInt("interstitial_ad_interval", interstitial_ad_interval);
        editor.apply();
    }

    public void saveAdStatus(int banner_ad_on_home_page, int banner_ad_on_search_page, int banner_ad_on_wallpaper_detail, int banner_ad_on_wallpaper_by_category, int interstitial_ad_on_click_wallpaper, int interstitial_ad_on_wallpaper_detail, int native_ad_on_wallpaper_list, int native_ad_on_exit_dialog, int app_open_ad, String last_update_ads_status) {
        editor.putInt("banner_ad_on_home_page", banner_ad_on_home_page);
        editor.putInt("banner_ad_on_search_page", banner_ad_on_search_page);
        editor.putInt("banner_ad_on_wallpaper_detail", banner_ad_on_wallpaper_detail);
        editor.putInt("banner_ad_on_wallpaper_by_category", banner_ad_on_wallpaper_by_category);
        editor.putInt("interstitial_ad_on_click_wallpaper", interstitial_ad_on_click_wallpaper);
        editor.putInt("interstitial_ad_on_wallpaper_detail", interstitial_ad_on_wallpaper_detail);
        editor.putInt("native_ad_on_wallpaper_list", native_ad_on_wallpaper_list);
        editor.putInt("native_ad_on_exit_dialog", native_ad_on_exit_dialog);
        editor.putInt("app_open_ad", app_open_ad);
        editor.putString("last_update_ads_status", last_update_ads_status);
        editor.apply();
    }

    public String getAdStatus() {
        return sharedPreferences.getString("ad_status", "0");
    }

    public String getAdType() {
        return sharedPreferences.getString("ad_type", "0");
    }

    public String getBackupAds() {
        return sharedPreferences.getString("backup_ads", "none");
    }

    public String getAdMobPublisherId() {
        return sharedPreferences.getString("admob_publisher_id", "0");
    }

    public String getAdMobAppId() {
        return sharedPreferences.getString("admob_app_id", "0");
    }

    public String getAdMobBannerId() {
        return sharedPreferences.getString("admob_banner_unit_id", "0");
    }

    public String getAdMobInterstitialId() {
        return sharedPreferences.getString("admob_interstitial_unit_id", "0");
    }

    public String getAdMobNativeId() {
        return sharedPreferences.getString("admob_native_unit_id", "0");
    }

    public String getAdMobAppOpenAdId() {
        return sharedPreferences.getString("admob_app_open_ad_unit_id", "0");
    }

    public String getAdManagerBannerId() {
        return sharedPreferences.getString("ad_manager_banner_unit_id", "0");
    }

    public String getAdManagerInterstitialId() {
        return sharedPreferences.getString("ad_manager_interstitial_unit_id", "0");
    }

    public String getAdManagerNativeId() {
        return sharedPreferences.getString("ad_manager_native_unit_id", "0");
    }

    public String getAdManagerAppOpenAdId() {
        return sharedPreferences.getString("ad_manager_app_open_ad_unit_id", "0");
    }

    public String getFanBannerUnitId() {
        return sharedPreferences.getString("fan_banner_unit_id", "0");
    }

    public String getFanInterstitialUnitId() {
        return sharedPreferences.getString("fan_interstitial_unit_id", "0");
    }

    public String getFanNativeUnitId() {
        return sharedPreferences.getString("fan_native_unit_id", "0");
    }

    public String getStartappAppID() {
        return sharedPreferences.getString("startapp_app_id", "0");
    }

    public String getUnityGameId() {
        return sharedPreferences.getString("unity_game_id", "0");
    }

    public String getUnityBannerPlacementId() {
        return sharedPreferences.getString("unity_banner_placement_id", "banner");
    }

    public String getUnityInterstitialPlacementId() {
        return sharedPreferences.getString("unity_interstitial_placement_id", "video");
    }

    public String getAppLovinBannerAdUnitId() {
        return sharedPreferences.getString("applovin_banner_ad_unit_id", "0");
    }

    public String getAppLovinInterstitialAdUnitId() {
        return sharedPreferences.getString("applovin_interstitial_ad_unit_id", "0");
    }

    public String getAppLovinNativeAdManualUnitId() {
        return sharedPreferences.getString("applovin_native_ad_manual_unit_id", "0");
    }

    public String getAppLovinAppOpenAdUnitId() {
        return sharedPreferences.getString("applovin_app_open_ad_unit_id", "0");
    }

    public String getAppLovinBannerZoneId() {
        return sharedPreferences.getString("applovin_banner_zone_id", "0");
    }

    public String getAppLovinInterstitialZoneId() {
        return sharedPreferences.getString("applovin_interstitial_zone_id", "0");
    }

    public String getIronSourceAppKey() {
        return sharedPreferences.getString("ironsource_app_key", "0");
    }

    public String getIronSourceBannerId() {
        return sharedPreferences.getString("ironsource_banner_id", "0");
    }

    public String getIronSourceInterstitialId() {
        return sharedPreferences.getString("ironsource_interstitial_id", "0");
    }

    public int getInterstitialAdInterval() {
        return sharedPreferences.getInt("interstitial_ad_interval", 0);
    }

    public int getNativeAdInterval() {
        return sharedPreferences.getInt("native_ad_interval", 0);
    }

    public int getNativeAdIndex() {
        return sharedPreferences.getInt("native_ad_index", 0);
    }

    public String getLastUpdateAds() {
        return sharedPreferences.getString("last_update_ads", "0");
    }

    //ads status
    public int getBannerAdStatusHome() {
        return sharedPreferences.getInt("banner_ad_on_home_page", 0);
    }

    public int getBannerAdStatusSearch() {
        return sharedPreferences.getInt("banner_ad_on_search_page", 0);
    }

    public int getBannerAdStatusDetail() {
        return sharedPreferences.getInt("banner_ad_on_wallpaper_detail", 0);
    }

    public int getBannerAdStatusCategoryDetail() {
        return sharedPreferences.getInt("banner_ad_on_wallpaper_by_category", 0);
    }

    public int getInterstitialAdClickWallpaper() {
        return sharedPreferences.getInt("interstitial_ad_on_click_wallpaper", 0);
    }

    public int getInterstitialAdDetail() {
        return sharedPreferences.getInt("interstitial_ad_on_wallpaper_detail", 0);
    }

    public int getNativeAdWallpaperList() {
        return 0;//sharedPreferences.getInt("native_ad_on_wallpaper_list", 0);
    }

    public int getNativeAdExitDialog() {
        return sharedPreferences.getInt("native_ad_on_exit_dialog", 0);
    }

    public int getAppOpenAd() {
        return sharedPreferences.getInt("app_open_ad", 0);
    }

    public String getLastUpdateAdStatus() {
        return sharedPreferences.getString("last_update_ads_status", "0");
    }

}
