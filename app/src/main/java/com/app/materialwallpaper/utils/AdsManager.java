package com.app.materialwallpaper.utils;

import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;
import static com.solodroid.ads.sdk.util.Constant.IRONSOURCE;

import android.app.Activity;

import com.app.materialwallpaper.BuildConfig;
import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.activities.MyApplication;
import com.app.materialwallpaper.databases.prefs.AdsPref;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.models.AdStatus;
import com.app.materialwallpaper.models.Ads;
import com.solodroid.ads.sdk.format.AdNetwork;
import com.solodroid.ads.sdk.format.BannerAd;
import com.solodroid.ads.sdk.format.InterstitialAd;
import com.solodroid.ads.sdk.format.NativeAd;
import com.solodroid.ads.sdk.format.NativeAdFragment;
import com.solodroid.ads.sdk.gdpr.GDPR;
import com.solodroid.ads.sdk.gdpr.LegacyGDPR;

public class AdsManager {

    Activity activity;
    AdNetwork.Initialize adNetwork;
    BannerAd.Builder bannerAd;
    InterstitialAd.Builder interstitialAd;
    NativeAd.Builder nativeAd;
    NativeAdFragment.Builder nativeAdView;
    SharedPref sharedPref;
    AdsPref adsPref;
    LegacyGDPR legacyGDPR;
    GDPR gdpr;

    public AdsManager(Activity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
        this.adsPref = new AdsPref(activity);
        this.legacyGDPR = new LegacyGDPR(activity);
        this.gdpr = new GDPR(activity);
        adNetwork = new AdNetwork.Initialize(activity);
        bannerAd = new BannerAd.Builder(activity);
        interstitialAd = new InterstitialAd.Builder(activity);
        nativeAd = new NativeAd.Builder(activity);
        nativeAdView = new NativeAdFragment.Builder(activity);
    }

    public void initializeAd() {
        if(MyApplication.getApp().isPremium()) {
            return;
        }
        adNetwork.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setStartappAppId(adsPref.getStartappAppID())
                .setUnityGameId(adsPref.getUnityGameId())
                .setIronSourceAppKey(adsPref.getIronSourceAppKey())
                .setDebug(BuildConfig.DEBUG)
                .build();
    }

    public void loadBannerAd(int placement) {
        if(MyApplication.getApp().isPremium()) {
            return;
        }
        bannerAd.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobBannerId(adsPref.getAdMobBannerId())
                .setGoogleAdManagerBannerId(adsPref.getAdManagerBannerId())
                .setFanBannerId(adsPref.getFanBannerUnitId())
                .setUnityBannerId(adsPref.getUnityBannerPlacementId())
                .setAppLovinBannerId(adsPref.getAppLovinBannerAdUnitId())
                .setAppLovinBannerZoneId(adsPref.getAppLovinBannerZoneId())
                .setIronSourceBannerId(adsPref.getIronSourceBannerId())
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .setPlacementStatus(placement)
                .setLegacyGDPR(Config.LEGACY_GDPR)
                .build();
    }

    public void loadInterstitialAd(int placement, int interval) {
        if(MyApplication.getApp().isPremium()) {
            return;
        }
        interstitialAd.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobInterstitialId(adsPref.getAdMobInterstitialId())
                .setGoogleAdManagerInterstitialId(adsPref.getAdManagerInterstitialId())
                .setFanInterstitialId(adsPref.getFanInterstitialUnitId())
                .setUnityInterstitialId(adsPref.getUnityInterstitialPlacementId())
                .setAppLovinInterstitialId(adsPref.getAppLovinInterstitialAdUnitId())
                .setAppLovinInterstitialZoneId(adsPref.getAppLovinInterstitialZoneId())
                .setIronSourceInterstitialId(adsPref.getIronSourceInterstitialId())
                .setInterval(interval)
                .setPlacementStatus(placement)
                .setLegacyGDPR(Config.LEGACY_GDPR)
                .build();
    }

    public void showInterstitialAd() {
        if(MyApplication.getApp().isPremium()) {
            return;
        }
        interstitialAd.show();
    }

    public void destroyBannerAd() {
        if(MyApplication.getApp().isPremium()) {
            return;
        }
        bannerAd.destroyAndDetachBanner();
    }

    public void resumeBannerAd(int placement) {
        if(MyApplication.getApp().isPremium()) {
            return;
        }
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && !adsPref.getIronSourceBannerId().equals("0")) {
            if (adsPref.getAdType().equals(IRONSOURCE) || adsPref.getBackupAds().equals(IRONSOURCE)) {
                loadBannerAd(placement);
            }
        }
    }

    public void saveAds(Ads ads) {
        if(MyApplication.getApp().isPremium()) {
            return;
        }
        if (Config.ENABLE_OFFLINE_ADS_MODE) {
            adsPref.saveAds(
                    activity.getString(R.string.ad_status).replace("on", "1"),
                    activity.getString(R.string.main_ads),
                    activity.getString(R.string.backup_ads),
                    activity.getString(R.string.admob_publisher_id),
                    activity.getString(R.string.admob_banner_unit_id),
                    activity.getString(R.string.admob_interstitial_unit_id),
                    activity.getString(R.string.admob_native_unit_id),
                    activity.getString(R.string.admob_app_open_unit_id),
                    activity.getString(R.string.ad_manager_banner_unit_id),
                    activity.getString(R.string.ad_manager_interstitial_unit_id),
                    activity.getString(R.string.ad_manager_native_unit_id),
                    activity.getString(R.string.ad_manager_app_open_unit_id),
                    activity.getString(R.string.fan_banner_unit_id),
                    activity.getString(R.string.fan_interstitial_unit_id),
                    activity.getString(R.string.fan_native_unit_id),
                    activity.getString(R.string.startapp_app_id),
                    activity.getString(R.string.unity_game_id),
                    activity.getString(R.string.unity_banner_placement_id),
                    activity.getString(R.string.unity_interstitial_placement_id),
                    activity.getString(R.string.applovin_banner_unit_id),
                    activity.getString(R.string.applovin_interstitial_unit_id),
                    activity.getString(R.string.applovin_native_manual_unit_id),
                    activity.getString(R.string.applovin_open_ad_unit_id),
                    activity.getString(R.string.applovin_banner_zone_id),
                    activity.getString(R.string.applovin_interstitial_zone_id),
                    activity.getString(R.string.ironsource_app_key),
                    activity.getString(R.string.ironsource_banner_placement_name),
                    activity.getString(R.string.ironsource_interstitial_placement_name),
                    Integer.parseInt(activity.getString(R.string.interstitial_ad_interval))
            );
        } else {
            adsPref.saveAds(
                    ads.ad_status.replace("on", "1"),
                    ads.ad_type,
                    ads.backup_ads,
                    ads.admob_publisher_id,
                    ads.admob_banner_unit_id,
                    ads.admob_interstitial_unit_id,
                    ads.admob_native_unit_id,
                    ads.admob_app_open_ad_unit_id,
                    ads.ad_manager_banner_unit_id,
                    ads.ad_manager_interstitial_unit_id,
                    ads.ad_manager_native_unit_id,
                    ads.ad_manager_app_open_ad_unit_id,
                    ads.fan_banner_unit_id,
                    ads.fan_interstitial_unit_id,
                    ads.fan_native_unit_id,
                    ads.startapp_app_id,
                    ads.unity_game_id,
                    ads.unity_banner_placement_id,
                    ads.unity_interstitial_placement_id,
                    ads.applovin_banner_ad_unit_id,
                    ads.applovin_interstitial_ad_unit_id,
                    ads.applovin_native_ad_manual_unit_id,
                    ads.applovin_app_open_ad_unit_id,
                    ads.applovin_banner_zone_id,
                    ads.applovin_interstitial_zone_id,
                    ads.ironsource_app_key,
                    ads.ironsource_banner_placement_name,
                    ads.ironsource_interstitial_placement_name,
                    ads.interstitial_ad_interval
            );
        }
    }

    public void saveAdStatus(AdStatus adStatus) {
        if(MyApplication.getApp().isPremium()) {
            return;
        }
        adsPref.saveAdStatus(
                adStatus.banner_ad_on_home_page,
                adStatus.banner_ad_on_search_page,
                adStatus.banner_ad_on_wallpaper_detail,
                adStatus.banner_ad_on_wallpaper_by_category,
                adStatus.interstitial_ad_on_click_wallpaper,
                adStatus.interstitial_ad_on_wallpaper_detail,
                adStatus.native_ad_on_wallpaper_list,
                adStatus.native_ad_on_exit_dialog,
                adStatus.app_open_ad,
                adStatus.last_update_ads_status
        );
    }

    public void updateConsentStatus() {
        if(MyApplication.getApp().isPremium()) {
            return;
        }
        if (Config.LEGACY_GDPR) {
            legacyGDPR.updateLegacyGDPRConsentStatus(adsPref.getAdMobPublisherId(), sharedPref.getBaseUrl() + "/privacy.php");
        } else {
            gdpr.updateGDPRConsentStatus();
        }
    }

}
