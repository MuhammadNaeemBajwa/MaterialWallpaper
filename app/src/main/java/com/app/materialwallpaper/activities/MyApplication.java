package com.app.materialwallpaper.activities;

import static com.app.materialwallpaper.Config.ALLOW_VPN_ACCESS;
import static com.app.materialwallpaper.utils.Constant.LOCALHOST_ADDRESS;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_MAX;
import static com.solodroid.ads.sdk.util.Constant.GOOGLE_AD_MANAGER;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.window.SplashScreen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.anjlab.android.iab.v3.SkuDetails;
import com.app.materialwallpaper.BuildConfig;
import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.callbacks.CallbackSettings;
import com.app.materialwallpaper.databases.prefs.AdsPref;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.models.Settings;
import com.app.materialwallpaper.rests.RestAdapter;
import com.app.materialwallpaper.utils.Constant;
import com.app.materialwallpaper.utils.Tools;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.onesignal.OneSignal;
import com.solodroid.ads.sdk.format.AppOpenAdAppLovin;
import com.solodroid.ads.sdk.format.AppOpenAdManager;
import com.solodroid.ads.sdk.format.AppOpenAdMob;
import com.solodroid.ads.sdk.util.OnShowAdCompleteListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks {

    public static final String TAG = "MyApplication";
    public static final String SKU_PREMIUM_UPGRADE = "premium";

    private AppOpenAdMob appOpenAdMob;
    private AppOpenAdManager appOpenAdManager;
    private AppOpenAdAppLovin appOpenAdAppLovin;
    String message = "";
    String bigPicture = "";
    String title = "";
    String link = "";
    long postId = -1;
    long uniqueId = -1;
    Settings settings;
    FirebaseAnalytics firebaseAnalytics;
    AdsPref adsPref;
    Activity currentActivity;
    public BillingProcessor bp;
    private SharedPref sharedPref;

    public static MyApplication getApp() {
       return app;
    }
    private static MyApplication app;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        this.registerActivityLifecycleCallbacks(this);
        sharedPref = new SharedPref(MyApplication.this);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);
        adsPref = new AdsPref(this);
        appOpenAdMob = new AppOpenAdMob();
        appOpenAdManager = new AppOpenAdManager();
        appOpenAdAppLovin = new AppOpenAdAppLovin();
        if (ALLOW_VPN_ACCESS) {
            initNotification();
        } else {
            if (Tools.isVpnConnectionAvailable()) {
                Tools.showWarningDialog(currentActivity, getString(R.string.vpn_detected), getString(R.string.close_vpn));
            } else {
                initNotification();
            }
        }

        bp = new BillingProcessor(this, getString(R.string.iap_license_key), new BillingProcessor.IBillingHandler() {

            @Override
            public void onProductPurchased(@NonNull String productId, @Nullable PurchaseInfo details) {
                if (productId.equals(SKU_PREMIUM_UPGRADE)) {
                    askUserAndRestart(getString(R.string.premium_upgrade_purchased_restart));
                }

            }

            @Override
            public void onPurchaseHistoryRestored() {

            }

            @Override
            public void onBillingError(int errorCode, @Nullable Throwable error) {

            }

            @Override
            public void onBillingInitialized() {
                bp.loadOwnedPurchasesFromGoogleAsync(new BillingProcessor.IPurchasesResponseListener() {
                    @Override
                    public void onPurchasesSuccess() {

                    }

                    @Override
                    public void onPurchasesError() {

                    }
                });

            }
        });
        bp.initialize();

        bp.loadOwnedPurchasesFromGoogleAsync(new BillingProcessor.IPurchasesResponseListener() {
            @Override
            public void onPurchasesSuccess() {

            }

            @Override
            public void onPurchasesError() {

            }
        });


    }

    public boolean isPremium() {
        return bp.isPurchased(SKU_PREMIUM_UPGRADE);
    }

    private void askUserAndRestart(String message) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(R.string.app_name);
//        builder.setMessage(message);
//        builder.setPositiveButton("Restart", (dialog, which) -> {
        Toast.makeText(this, getString(R.string.thanks_for_purchase), Toast.LENGTH_SHORT).show();
            restartApp();
//        });
//        builder.create().show();
    }

    private void restartApp() {
        Intent intent = new Intent(this, ActivitySplash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    public void purchasePremium(Activity activity){
        bp.purchase(activity, SKU_PREMIUM_UPGRADE);
    }

    private void  initNotification() {
        OneSignal.disablePush(false);
        Log.d(TAG, "OneSignal Notification is enabled");

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        requestTopic();

        OneSignal.setNotificationOpenedHandler(
                result -> {
                    title = result.getNotification().getTitle();
                    message = result.getNotification().getBody();
                    bigPicture = result.getNotification().getBigPicture();
                    Log.d(TAG, title + ", " + message + ", " + bigPicture);
                    try {
                        uniqueId = result.getNotification().getAdditionalData().getLong("unique_id");
                        postId = result.getNotification().getAdditionalData().getLong("post_id");
                        link = result.getNotification().getAdditionalData().getString("link");
                        Log.d(TAG, postId + ", " + uniqueId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("unique_id", uniqueId);
                    intent.putExtra("post_id", postId);
                    intent.putExtra("title", title);
                    intent.putExtra("link", link);
                    startActivity(intent);
                });

        OneSignal.unsubscribeWhenNotificationsAreDisabled(true);
    }

    @SuppressWarnings("ConstantConditions")
    private void requestTopic() {
        if (!Config.SERVER_KEY.contains("XXXX")) {
            String data = Tools.decode(Config.SERVER_KEY);
            String[] results = data.split("_applicationId_");
            String baseUrl = results[0].replace("http://localhost", LOCALHOST_ADDRESS);

            Call<CallbackSettings> callbackCall = RestAdapter.createAPI(baseUrl).getSettings(BuildConfig.APPLICATION_ID);
            callbackCall.enqueue(new Callback<CallbackSettings>() {
                public void onResponse(Call<CallbackSettings> call, Response<CallbackSettings> response) {
                    CallbackSettings resp = response.body();
                    if (resp != null && resp.status.equals("ok")) {
                        settings = resp.settings;
                        FirebaseMessaging.getInstance().subscribeToTopic(settings.fcm_notification_topic);
                        OneSignal.setAppId(settings.onesignal_app_id);

                        Log.d(TAG, "OneSignal Device State : " +  OneSignal.getDeviceState().getPushToken());

                        Log.d(TAG, "OneSignal App ID : " + settings.onesignal_app_id);
                        Log.d(TAG, "FCM notification topic : " + settings.fcm_notification_topic);
                    }
                }

                public void onFailure(Call<CallbackSettings> call, Throwable th) {
                    Log.e("onFailure", "" + th.getMessage());
                }
            });
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    LifecycleObserver lifecycleObserver = new DefaultLifecycleObserver() {
        @Override
        public void onStart(@NonNull LifecycleOwner owner) {
            Log.d(TAG, "onMoveToForeground");
            if (Constant.isAppOpen) {
                if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAppOpenAd() != 0) {
                    switch (adsPref.getAdType()) {
                        case ADMOB:
                            if (!adsPref.getAdMobAppOpenAdId().equals("0")) {
                                if (!currentActivity.getIntent().hasExtra("unique_id")) {
                                    appOpenAdMob.showAdIfAvailable(currentActivity, adsPref.getAdMobAppOpenAdId());
                                }
                            }
                            break;
                        case GOOGLE_AD_MANAGER:
                            if (!adsPref.getAdManagerAppOpenAdId().equals("0")) {
                                if (!currentActivity.getIntent().hasExtra("unique_id")) {
                                    appOpenAdManager.showAdIfAvailable(currentActivity, adsPref.getAdManagerAppOpenAdId());
                                }
                            }
                            break;
                        case APPLOVIN:
                        case APPLOVIN_MAX:
                            if (!adsPref.getAppLovinAppOpenAdUnitId().equals("0")) {
                                if (!currentActivity.getIntent().hasExtra("unique_id")) {
                                    appOpenAdAppLovin.showAdIfAvailable(currentActivity, adsPref.getAppLovinAppOpenAdUnitId());
                                }
                            }
                            break;
                    }
                }
            }
            DefaultLifecycleObserver.super.onStart(owner);
        }
    };

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log.d(TAG, "onActivityStarted");
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAppOpenAd() != 0) {
            switch (adsPref.getAdType()) {
                case ADMOB:
                    if (!adsPref.getAdMobAppOpenAdId().equals("0")) {
                        if (!appOpenAdMob.isShowingAd) {
                            currentActivity = activity;
                        }
                    }
                    break;
                case GOOGLE_AD_MANAGER:
                    if (!adsPref.getAdManagerAppOpenAdId().equals("0")) {
                        if (!appOpenAdManager.isShowingAd) {
                            currentActivity = activity;
                        }
                    }
                    break;
                case APPLOVIN:
                case APPLOVIN_MAX:
                    if (!adsPref.getAppLovinAppOpenAdUnitId().equals("0")) {
                        if (!appOpenAdAppLovin.isShowingAd) {
                            currentActivity = activity;
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        Log.d(TAG, "onActivityResumed");
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log.d(TAG, "onActivityPaused");
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log.d(TAG, "onActivityStopped");
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log.d(TAG, "onActivityDestroyed");
    }

    public void showAdIfAvailable(@NonNull Activity activity, @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication class
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAppOpenAd() != 0) {
            switch (adsPref.getAdType()) {
                case ADMOB:
                    if (!adsPref.getAdMobAppOpenAdId().equals("0")) {
                        appOpenAdMob.showAdIfAvailable(activity, adsPref.getAdMobAppOpenAdId(), onShowAdCompleteListener);
                        Constant.isAppOpen = true;
                    }
                    break;
                case GOOGLE_AD_MANAGER:
                    if (!adsPref.getAdManagerAppOpenAdId().equals("0")) {
                        appOpenAdManager.showAdIfAvailable(activity, adsPref.getAdManagerAppOpenAdId(), onShowAdCompleteListener);
                        Constant.isAppOpen = true;
                    }
                    break;
                case APPLOVIN:
                case APPLOVIN_MAX:
                    if (!adsPref.getAppLovinAppOpenAdUnitId().equals("0")) {
                        appOpenAdAppLovin.showAdIfAvailable(activity, adsPref.getAppLovinAppOpenAdUnitId(), onShowAdCompleteListener);
                        Constant.isAppOpen = true;
                    }
                    break;
            }
        }
    }

}
