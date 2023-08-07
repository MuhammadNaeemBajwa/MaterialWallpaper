package com.app.materialwallpaper.activities;

import static com.app.materialwallpaper.utils.Constant.BOTH;
import static com.app.materialwallpaper.utils.Constant.DELAY_SET;
import static com.app.materialwallpaper.utils.Constant.HOME_SCREEN;
import static com.app.materialwallpaper.utils.Constant.LOCK_SCREEN;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.app.materialwallpaper.R;
import com.app.materialwallpaper.databases.prefs.AdsPref;
import com.app.materialwallpaper.utils.AdsManager;
import com.app.materialwallpaper.utils.Tools;
import com.app.materialwallpaper.utils.WallpaperHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.canhub.cropper.CropImageView;
import com.google.android.material.snackbar.Snackbar;

public class ActivityCropWallpaper extends AppCompatActivity {

    String imageUrl;
    Bitmap bitmap = null;
    CropImageView cropImageView;
    private String singleChoiceSelected;
    CoordinatorLayout parentView;
    AdsPref adsPref;
    ProgressDialog progressDialog;
    WallpaperHelper wallpaperHelper;
    AdsManager adsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        Tools.transparentStatusBarNavigation(ActivityCropWallpaper.this);
        setContentView(R.layout.activity_set_wallpaper);
        Tools.getRtlDirection(this);

        adsPref = new AdsPref(this);
        progressDialog = new ProgressDialog(this);
        wallpaperHelper = new WallpaperHelper(this);
        adsManager = new AdsManager(this);

        adsManager.loadInterstitialAd(adsPref.getInterstitialAdDetail(), 1);

        Intent intent = getIntent();
        imageUrl = intent.getStringExtra("image_url");

        cropImageView = findViewById(R.id.cropImageView);
        parentView = findViewById(R.id.coordinatorLayout);

        loadWallpaper();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void loadWallpaper() {
        Glide.with(this)
                .load(imageUrl.replace(" ", "%20"))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        bitmap = ((BitmapDrawable) resource).getBitmap();
                        cropImageView.setImageBitmap(bitmap);

                        findViewById(R.id.btn_set_wallpaper).setOnClickListener(view -> dialogOptionSetWallpaper());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Snackbar.make(parentView, getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void dialogOptionSetWallpaper() {
        String[] items = getResources().getStringArray(R.array.dialog_set_crop_wallpaper);
        singleChoiceSelected = items[0];
        int itemSelected = 0;
        bitmap = cropImageView.getCroppedImage();
        new AlertDialog.Builder(ActivityCropWallpaper.this)
                .setTitle(R.string.dialog_set_title)
                .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                .setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {

                    progressDialog.setMessage(getString(R.string.msg_preparing_wallpaper));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    new Handler().postDelayed(() -> {
                        if (singleChoiceSelected.equals(getResources().getString(R.string.set_home_screen))) {
                            wallpaperHelper.setWallpaper(parentView, progressDialog, adsManager, bitmap, HOME_SCREEN);
                            progressDialog.setMessage(getString(R.string.msg_apply_wallpaper));
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.set_lock_screen))) {
                            wallpaperHelper.setWallpaper(parentView, progressDialog, adsManager, bitmap, LOCK_SCREEN);
                            progressDialog.setMessage(getString(R.string.msg_apply_wallpaper));
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.set_both))) {
                            wallpaperHelper.setWallpaper(parentView, progressDialog, adsManager, bitmap, BOTH);
                            progressDialog.setMessage(getString(R.string.msg_apply_wallpaper));
                        }

                    }, DELAY_SET);

                })
                .setNegativeButton(R.string.dialog_option_cancel, null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
