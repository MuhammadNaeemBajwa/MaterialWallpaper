package com.app.materialwallpaper.VideoAd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.app.materialwallpaper.R;
import com.app.materialwallpaper.activities.ActivitySearch;
import com.app.materialwallpaper.activities.ActivityWallpaperDetail;
import com.app.materialwallpaper.activities.BuyPremiumActivity;
import com.app.materialwallpaper.activities.MainActivity;
import com.app.materialwallpaper.activities.MyApplication;

import com.app.materialwallpaper.databinding.ActivityVideoAdBinding;
import com.app.materialwallpaper.models.Ads;
import com.app.materialwallpaper.utils.AdsManager;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class VideoAd extends AppCompatActivity {
    ActivityVideoAdBinding binding;
    private RewardedAd rewardedAd;
    private boolean isButtonClickable = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoAdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.color_black));
        }


        initialize();
        setListener();
    }

    private void initialize() {
        // Initialize AdMob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // SDK initialization is complete, you can load and show ads here

            }
        });

        loadRewardedAd();

    }

    private void setListener() {
        binding.arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.watchAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onClick: ");
                if (isButtonClickable && rewardedAd != null) {
                    isButtonClickable = false; // Disable the button
                    Activity activityContext = VideoAd.this;
                    rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            Log.d("TAG", "onUserEarnedReward: ");

                        }
                    });

                    rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d("TAG", "Ad was clicked.");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {

                            Intent intent = new Intent(getApplicationContext(), ActivityWallpaperDetail.class);
                            startActivity(intent);
                            loadRewardedAd();

                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            // Called when ad fails to show.
                            Log.e("TAG", "Ad failed to show fullscreen content.");
                            rewardedAd = null;
                            Intent intent = new Intent(getApplicationContext(), ActivityWallpaperDetail.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d("TAG", "Ad recorded an impression.");
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d("TAG", "Ad showed fullscreen content.");
                        }


                    });
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isButtonClickable = true;
                        }
                    }, 1000);


                } else {
                    Log.d("TAG", "onClick: ");



                }
            }
        });

    }

    public void onPurchaseClicked(View view) {
        MyApplication.getApp().purchasePremium(this);
    }

    private void loadRewardedAd() {
        // Load rewarded ad
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedAd = null;

                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;

                    }
                });
    }
    public static void start(Context context, String url) {
        Intent intent = new Intent(context, VideoAd.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }


}
