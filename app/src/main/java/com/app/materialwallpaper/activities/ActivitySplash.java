package com.app.materialwallpaper.activities;

import static com.app.materialwallpaper.Config.ALLOW_VPN_ACCESS;
import static com.app.materialwallpaper.utils.Constant.LOCALHOST_ADDRESS;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_MAX;
import static com.solodroid.ads.sdk.util.Constant.GOOGLE_AD_MANAGER;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.materialwallpaper.BuildConfig;
import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.callbacks.CallbackSettings;
import com.app.materialwallpaper.databases.prefs.AdsPref;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.models.AdStatus;
import com.app.materialwallpaper.models.Ads;
import com.app.materialwallpaper.models.App;
import com.app.materialwallpaper.models.Settings;
import com.app.materialwallpaper.rests.RestAdapter;
import com.app.materialwallpaper.utils.AdsManager;
import com.app.materialwallpaper.utils.Constant;
import com.app.materialwallpaper.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    private static final String TAG = "ActivitySplash";
    ProgressBar progressBar;
    ImageView imgSplash;
    Call<CallbackSettings> callbackCall = null;
    SharedPref sharedPref;
    AdsManager adsManager;
    AdsPref adsPref;
    AdStatus adStatus;
    Settings settings;
    Ads ads;
    App app;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.transparentStatusBarNavigation(ActivitySplash.this);
        setContentView(R.layout.activity_splash);

        Tools.getRtlDirection(this);
        sharedPref = new SharedPref(this);

        adsManager = new AdsManager(this);
        adsManager.initializeAd();

        adsPref = new AdsPref(this);
        imgSplash = findViewById(R.id.img_splash);
//        if (sharedPref.getIsDarkTheme()) {
//            imgSplash.setImageResource(R.drawable.bg_splash_dark);
//        } else {
//            imgSplash.setImageResource(R.drawable.bg_splash_default);
//        }

        progressBar = findViewById(R.id.progressBar);


        adsPref = new AdsPref(this);


        loadAdsInfo();


    }

    private void loadAdsInfo() {
        if (ALLOW_VPN_ACCESS) {
            requestAction();
        } else {
            if (Tools.isVpnConnectionAvailable()) {
                Tools.showWarningDialog(ActivitySplash.this, getString(R.string.vpn_detected), getString(R.string.close_vpn));
            } else {
                requestAction();
            }
        }
    }

    private void requestAction() {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAppOpenAd() != 0) {
            Application application = getApplication();
            new Handler().postDelayed(() -> {
                switch (adsPref.getAdType()) {
                    case ADMOB:
                        if (!adsPref.getAdMobAppOpenAdId().equals("0")) {
                            ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::requestConfig);
                        } else {
                            requestConfig();
                        }
                        break;
                    case GOOGLE_AD_MANAGER:
                        if (!adsPref.getAdManagerAppOpenAdId().equals("0")) {
                            ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::requestConfig);
                        } else {
                            requestConfig();
                        }
                        break;
                    case APPLOVIN:
                    case APPLOVIN_MAX:
                        if (!adsPref.getAppLovinAppOpenAdUnitId().equals("0")) {
                            ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::requestConfig);
                        } else {
                            requestConfig();
                        }
                        break;
                    default:
                        requestConfig();
                        break;
                }
            }, 1500);
        } else {
            requestConfig();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void requestConfig() {
        if (Config.SERVER_KEY.contains("XXXXX")) {
            new AlertDialog.Builder(this)
                    .setTitle("App not configured")
                    .setMessage("Please put your Server Key and Rest API Key from settings menu in your admin panel to AppConfig, you can see the documentation for more detailed instructions.")
                    .setPositiveButton(getString(R.string.dialog_option_ok), (dialogInterface, i) -> finish())
                    .setCancelable(false)
                    .show();
        } else {
            String data = Tools.decode(Config.SERVER_KEY);
            String[] results = data.split("_applicationId_");
            String baseUrl = results[0].replace("http://localhost", LOCALHOST_ADDRESS);
            String applicationId = results[1];
            sharedPref.setBaseUrl(baseUrl);
            if (applicationId.equals(BuildConfig.APPLICATION_ID)) {
                requestAPI(baseUrl);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Whoops! invalid access key or applicationId, please check your configuration")
                        .setPositiveButton("Ok", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }
            Log.d(TAG, "Start request config");
        }
    }

    private void requestAPI(String baseUrl) {
        Log.d(TAG, "requestAPI: ");
        this.callbackCall = RestAdapter.createAPI(baseUrl).getSettings(BuildConfig.APPLICATION_ID);
        this.callbackCall.enqueue(new Callback<CallbackSettings>() {


            public void onResponse(Call<CallbackSettings> call, Response<CallbackSettings> response) {
                CallbackSettings resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    Log.d(TAG, "onResponse: check: " + (resp != null && resp.status.equals("ok")));
                    ads = resp.ads;
                    adStatus = resp.ads_status;
                    settings = resp.settings;
                    app = resp.app;

                    sharedPref.saveConfig(settings.privacy_policy, settings.more_apps_url, settings.copyright);
                    adsManager.saveAds(ads);
                    adsManager.saveAdStatus(adStatus);
                    sharedPref.saveMenuList(resp.menus);

                    // Check if the 'app' object is not null before accessing the 'status' field
                    if (app != null && app.status != null && app.status.equals("0")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityRedirect.class);
                        intent.putExtra("redirect_url", app.redirect_url);
                        startActivity(intent);
                        finish();
                        Log.d(TAG, "App is inactive, call redirect method");
                    } else {
                        startMainActivity();
                        Log.d(TAG, "App is active");
                    }
                } else {
                    // Handle the case when the response is null or the 'status' is not "ok"
                    // For example, show an error dialog or handle the error gracefully
                    Log.e(TAG, "Invalid response or status is not 'ok'");
                    startMainActivity(); // or any other appropriate action
                }
            }

            public void onFailure(Call<CallbackSettings> call, Throwable th) {
                // Handle the network failure case here
                Log.e("onFailure", "" + th.getMessage());
                startMainActivity(); // or any other appropriate action
            }

//            public void onResponse(Call<CallbackSettings> call, Response<CallbackSettings> response) {
//                CallbackSettings resp = response.body();
//                if (resp != null && resp.status.equals("ok")) {
//                    ads = resp.ads;
//                    adStatus = resp.ads_status;
//                    settings = resp.settings;
//                    app = resp.app;
//                    sharedPref.saveConfig(settings.privacy_policy, settings.more_apps_url, settings.copyright);
//                    adsManager.saveAds(ads);
//                    adsManager.saveAdStatus(adStatus);
//                    sharedPref.saveMenuList(resp.menus);
//                }
//                if (app.status != null && app.status.equals("0")) {
//                    Intent intent = new Intent(getApplicationContext(), ActivityRedirect.class);
//                    intent.putExtra("redirect_url", app.redirect_url);
//                    startActivity(intent);
//                    finish();
//                    Log.d(TAG, "App is inactive, call redirect method");
//                } else {
//                    startMainActivity();
//                    Log.d(TAG, "App is active");
//                }
//            }
//
//            public void onFailure(Call<CallbackSettings> call, Throwable th) {
//                Log.e("onFailure", "" + th.getMessage());
//                startMainActivity();
//            }
        });
    }


    private void startMainActivity() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, Config.DELAY_SPLASH);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
