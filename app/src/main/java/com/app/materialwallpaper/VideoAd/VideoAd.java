package com.app.materialwallpaper.VideoAd;

import static com.app.materialwallpaper.activities.MyApplication.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.app.materialwallpaper.R;
import com.app.materialwallpaper.activities.ActivityWallpaperDetail;
import com.app.materialwallpaper.activities.MyApplication;


import com.app.materialwallpaper.databinding.ActivityVideoAdBinding;
import com.app.materialwallpaper.models.Wallpaper;
import com.app.materialwallpaper.utils.Constant;
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

import java.util.List;

public class VideoAd extends AppCompatActivity {
    ActivityVideoAdBinding binding;
    private RewardedAd rewardedAd;
    private ProgressDialog progressDialog;
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
//        loadRewardedAd();
        setText(Constant.wallpapers, Constant.position);
    }



    private void initialize() {
        // Initialize AdMob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d(TAG, "ads: onInitializationComplete: ");
                // SDK initialization is complete, you can load and show ads here
                loadRewardedAd();
            }
        });
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
                Log.d("TAG", "ads: onClick: watchAd: isButtonClickable: "+isButtonClickable+" rewardedAd: "+rewardedAd);

                if (isButtonClickable && rewardedAd != null) {
                    isButtonClickable = false; // Disable the button
                    binding.watchAd.setEnabled(false); // Disable the button temporarily
                    Activity activityContext = VideoAd.this;
                    rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            Log.d("TAG", "onUserEarnedReward: rewardItem: " + rewardItem);
                        }
                    });

                    rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.d(TAG, "ads: onAdDismissedFullScreenContent: ");
                            // Enable the button after the ad is dismissed
                            binding.watchAd.setEnabled(true);
                            Intent intent = new Intent(getApplicationContext(), ActivityWallpaperDetail.class);
                            startActivity(intent);
                            finish();
                            loadRewardedAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            Log.d(TAG, "ads: onAdFailedToShowFullScreenContent: adError:  " + adError);
                            // Called when ad fails to show.
                            Log.e("TAG", "Ad failed to show fullscreen content.");
                            rewardedAd = null;
//                            Intent intent = new Intent(getApplicationContext(), ActivityWallpaperDetail.class);
//                            startActivity(intent);
                        }

                        @Override
                        public void onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d("TAG", "ads: Ad recorded an impression.");
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d("TAG", "ads: Ad showed fullscreen content.");
                        }


                    });


                }
                else {
                    Log.d("TAG", "onClick: else: ");
                }
            }
        });
    }
    private void loadRewardedAd() {
        Log.d("TAG", "ads: loadRewardedAd: ");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading ad...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Load rewarded ad
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-4564681694529671/2337635793",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(TAG, "ads: onAdFailedToLoad: LoadAdError: " + loadAdError);
                        rewardedAd = null;
                        progressDialog.dismiss();
                    }
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        Log.d(TAG, "ads: onAdLoaded: RewardedAd: " + ad);
                        rewardedAd = ad;
                        binding.watchAd.setEnabled(true);
                        progressDialog.dismiss();
                    }
                });
    }
    public void onPurchaseClicked(View view) {
        MyApplication.getApp().purchasePremium(this);
    }


    public static void start(Context context, String url) {
        Intent intent = new Intent(context, VideoAd.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }
    private void setText(final List<Wallpaper> wallpapers, int position){
        Log.d(TAG, "loadView: ");
        Wallpaper wallpaper = wallpapers.get(position);

        if (wallpaper.image_name.equals("")) {
            binding.sunsetCity.setText(wallpaper.category_name);
            binding.lifestyle.setVisibility(View.GONE);
        } else {
            binding.sunsetCity.setText(wallpaper.image_name);
            binding.lifestyle.setText(wallpaper.category_name);
        }

    }


}
