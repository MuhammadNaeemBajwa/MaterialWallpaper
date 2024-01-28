package com.app.materialwallpaper.activities;

import static com.app.materialwallpaper.activities.MyApplication.TAG;
import static com.app.materialwallpaper.utils.Constant.BOTH;
import static com.app.materialwallpaper.utils.Constant.HOME_SCREEN;
import static com.app.materialwallpaper.utils.Constant.LOCK_SCREEN;
import static com.app.materialwallpaper.utils.Constant.wallpapers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.BuildConfig;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

//import com.app.materialwallpaper.BuildConfig;
import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.adapters.AdapterTags;
import com.app.materialwallpaper.adapters.AdapterWallpaperDetail;
import com.app.materialwallpaper.databases.prefs.AdsPref;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.databases.sqlite.DBHelper;
import com.app.materialwallpaper.models.Wallpaper;
import com.app.materialwallpaper.utils.AdsManager;
import com.app.materialwallpaper.utils.Constant;
import com.app.materialwallpaper.utils.Tools;
import com.app.materialwallpaper.utils.WallpaperHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dingmouren.videowallpaper.VideoWallpaper;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityWallpaperDetail extends AppCompatActivity {

    Toolbar toolbar;
    ActionBar actionBar;
    private String singleChoiceSelected;
    CoordinatorLayout parentView;
    private BottomSheetDialog mBottomSheetDialog;
    SharedPref sharedPref;
    DBHelper dbHelper;
    AdsPref adsPref;
    private AdView adView;
    boolean flag = true;
    LinearLayout lytBottom;
    RelativeLayout bgShadowTop;
    RelativeLayout bgShadowBottom;
    AdsManager adsManager;
    WallpaperHelper wallpaperHelper;
    ProgressDialog progressDialog;
    ViewPager2 viewPager2;
    AdapterWallpaperDetail adapterWallpaperDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        if (adsPref.getBannerAdStatusDetail() != 0) {
            Tools.transparentStatusBar(this);
            if (sharedPref.getIsDarkTheme()) {
                Tools.darkNavigation(this);
            }
        } else {
            Tools.transparentStatusBarNavigation(this);
        }
        setContentView(R.layout.activity_wallpaper_detail);


        if (!BuildConfig.DEBUG) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        progressDialog = new ProgressDialog(this);
        wallpaperHelper = new WallpaperHelper(this);

        Tools.getRtlDirection(this);
        parentView = findViewById(R.id.coordinatorLayout);
        lytBottom = findViewById(R.id.lyt_bottom);
        bgShadowTop = findViewById(R.id.bg_shadow_top);
        bgShadowBottom = findViewById(R.id.bg_shadow_bottom);

        dbHelper = new DBHelper(this);

        setupToolbar();
        loadView(Constant.wallpapers, Constant.position);


        setupViewPager(Constant.wallpapers);

        adsManager = new AdsManager(this);
//        adsManager.loadBannerAd(adsPref.getBannerAdStatusDetail());
//        adsManager.loadInterstitialAd(adsPref.getInterstitialAdDetail(), 1);

        // Initialize AdMob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // Initialization is complete. You can now request ads.
                loadBannerAd();
            }
        });

        // Load banner ad
        loadBannerAd();
    }








    private void loadBannerAd() {
        adView = findViewById(R.id.adView);  // Make sure to replace with your AdView ID
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    public void setupViewPager(final List<Wallpaper> wallpapers) {
        viewPager2 = findViewById(R.id.view_pager2);
        adapterWallpaperDetail = new AdapterWallpaperDetail(this, wallpapers);
        viewPager2.setAdapter(adapterWallpaperDetail);

//        viewPager2.setOffscreenPageLimit(wallpapers.size());

        //added on 10/24/203 by hasnain to remove crash above code is pevious one comment out
        viewPager2.setOffscreenPageLimit(1); // Or any value greater than 0, depending on your requirements


        viewPager2.setCurrentItem(Constant.position, false);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                loadView(wallpapers, position);


            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

    }

    public void loadView(final List<Wallpaper> wallpapers, int position) {
        Log.d(TAG, "loadView: "+ position);
        if (position < 0 || position >= wallpapers.size()) {
            Log.d(TAG, "loadView: "+ (position < 0 || position >= wallpapers.size()));
            // Handle the case where the position is out of bounds.
            Log.d(TAG, "loadView: " + position);
            return;
        }

        Wallpaper wallpaper = wallpapers.get(position);
        boolean isPremium = wallpaper.isPremium();


        if(wallpaper.isVideoWallpaper()) {
            findViewById(R.id.btn_favorite).setVisibility(View.INVISIBLE);
            findViewById(R.id.btn_info).setVisibility(View.INVISIBLE);
            findViewById(R.id.btn_delete_wallpaper).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.btn_favorite).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_info).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_delete_wallpaper).setVisibility(View.GONE);
        }

        findViewById(R.id.btn_delete_wallpaper).setOnClickListener((v)->{
            deleteWallpaper(this, wallpaper);
        });


        String UPLOAD_URL = sharedPref.getBaseUrl() + "/upload/" + wallpaper.image_upload;
        String DIRECT_URL = wallpaper.image_url;

        if (wallpaper.image_name != null) {

            TextView title_toolbar = findViewById(R.id.title_toolbar);
            TextView sub_title_toolbar = findViewById(R.id.sub_title_toolbar);
            View premium = findViewById(R.id.viewPremium);
            if(MyApplication.getApp().isPremium()) {
                ImageView image = findViewById(R.id.imgPremium);
                image.setImageResource(R.drawable.green_smiley);
            }

            premium.setVisibility(wallpaper.isPremium() ? View.VISIBLE : View.GONE);
            premium.setOnClickListener((v)->{
                BuyPremiumActivity.start(ActivityWallpaperDetail.this, wallpaper.image_url);
            });

            if (!Config.ENABLE_DISPLAY_WALLPAPER_NAME) {
                title_toolbar.setVisibility(View.GONE);
                sub_title_toolbar.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_large));
            }

            if (!Config.ENABLE_DISPLAY_WALLPAPER_CATEGORY) {
                sub_title_toolbar.setVisibility(View.GONE);
            }

            if (wallpaper.image_name.equals("")) {
                title_toolbar.setText(wallpaper.category_name);
                sub_title_toolbar.setVisibility(View.GONE);
            } else {
                title_toolbar.setText(wallpaper.image_name);
                sub_title_toolbar.setText(wallpaper.category_name);
            }

            findViewById(R.id.btn_info).setOnClickListener(view -> showBottomSheetDialog(wallpaper));



            findViewById(R.id.btn_save).setVisibility(View.GONE);
//            findViewById(R.id.btn_save).setOnClickListener(view -> {
//                if (!verifyPermissions()) {
//                    return;
//                }
//                if (wallpaper.type.equals("upload")) {
//                    wallpaperHelper.downloadWallpaper(wallpaper, progressDialog, adsManager, UPLOAD_URL);
//                } else if (wallpaper.type.equals("url")) {
//                    wallpaperHelper.downloadWallpaper(wallpaper, progressDialog, adsManager, DIRECT_URL);
//                }
//            });

            findViewById(R.id.btn_share).setOnClickListener(view -> {
                if (!verifyPermissions()) {
                    return;
                }
                if (wallpaper.type.equals("upload")) {
                    wallpaperHelper.shareWallpaper(progressDialog, UPLOAD_URL);
                } else if (wallpaper.type.equals("url")) {
                    wallpaperHelper.shareWallpaper(progressDialog, DIRECT_URL);
                }
            });

            findViewById(R.id.btn_set_wallpaper).setOnClickListener(view -> {


//                if (!verifyPermissions()) {
//                    return;
//                }
                if (wallpaper.type.equals("upload")) {
                    wallpaperHelper.downloadWallpaper(wallpaper, progressDialog, adsManager, UPLOAD_URL);
                } else if (wallpaper.type.equals("url")) {
                    wallpaperHelper.downloadWallpaper(wallpaper, progressDialog, adsManager, DIRECT_URL);
                }

//                if (!verifyAccess(wallpaper)) {
//                    return;
//                }
                if(wallpaper.isVideoWallpaper()) {

                    VideoWallpaper.setToWallPaper(ActivityWallpaperDetail.this, wallpaper.image_url);
                    return;
                }

                if (!verifyPermissions()) {
                    return;
                }


                if (wallpaper.image_upload.endsWith(".gif") || wallpaper.image_url.endsWith(".gif")) {
                    if (wallpaper.type.equals("upload")) {
                        wallpaperHelper.setGif(parentView, progressDialog, UPLOAD_URL);
                    } else if (wallpaper.type.equals("url")) {
                        wallpaperHelper.setGif(parentView, progressDialog, DIRECT_URL);
                    }
                } else if (wallpaper.image_upload.endsWith(".mp4") || wallpaper.image_url.endsWith(".mp4")) {
                    if (wallpaper.type.equals("upload")) {
                        wallpaperHelper.setMp4(parentView, progressDialog, UPLOAD_URL);
                    } else if (wallpaper.type.equals("url")) {
                        wallpaperHelper.setMp4(parentView, progressDialog, DIRECT_URL);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= 24) {
                        if (wallpaper.type.equals("upload")) {
                            dialogOptionSetWallpaper(UPLOAD_URL, wallpaper);
                        } else if (wallpaper.type.equals("url")) {
                            dialogOptionSetWallpaper(DIRECT_URL, wallpaper);
                        }
                    } else {
                        if (wallpaper.type.equals("upload")) {
                            wallpaperHelper.setWallpaper(parentView, progressDialog, adsManager, UPLOAD_URL);
                        } else if (wallpaper.type.equals("url")) {
                            wallpaperHelper.setWallpaper(parentView, progressDialog, adsManager, DIRECT_URL);
                        }
                    }
                }
            });

            favToggle(wallpaper);
            findViewById(R.id.btn_favorite).setOnClickListener(view -> {
                if (dbHelper.isFavoritesExist(wallpaper.image_id)) {
                    dbHelper.deleteFavorites(wallpaper);
                    Snackbar.make(parentView, getString(R.string.snack_bar_favorite_removed), Snackbar.LENGTH_SHORT).show();
                } else {
                    dbHelper.addOneFavorite(wallpaper);
                    Snackbar.make(parentView, getString(R.string.snack_bar_favorite_added), Snackbar.LENGTH_SHORT).show();
                }
                favToggle(wallpaper);
            });

            wallpaperHelper.updateView(wallpaper.image_id);

            lytBottom.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            fullScreenMode(false);
            showShadow(true);
        } else {
            fullScreenMode(false);
            lytBottom.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
            if (!sharedPref.getIsDarkTheme()) {
                Tools.darkNavigationStatusBar(ActivityWallpaperDetail.this);
            }
            showShadow(false);
        }

    }

    private void deleteWallpaper(ActivityWallpaperDetail activityWallpaperDetail, Wallpaper wallpaper) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityWallpaperDetail);
        builder.setTitle("Delete Wallpaper");
        builder.setMessage("Are you sure you want to delete this wallpaper?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            WallpaperHelper wallpaperHelper = new WallpaperHelper(activityWallpaperDetail);
            wallpaperHelper.deleteVideoWallpaper(wallpaper);
            dialog.dismiss();
            Snackbar.make(parentView, R.string.wallpaper_deleted, Snackbar.LENGTH_SHORT).show();
            finish();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private boolean verifyAccess(Wallpaper wallpaper) {

        if(!wallpaper.isPremium()) return true;
        // check if purchased

        if (!MyApplication.getApp().isPremium()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(R.layout.dialog_buy_premium);
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.findViewById(R.id.btnCancel).setOnClickListener((v)->dialog.dismiss());
            dialog.findViewById(R.id.btnLearnMore).setOnClickListener((v)->{
                dialog.dismiss();
                BuyPremiumActivity.start(ActivityWallpaperDetail.this,wallpaper !=null ? wallpaper.image_url:null);
            });


        }else{
            return true;
        }

        return false;

    }

    private void showShadow(boolean show) {
        if (show) {
            bgShadowTop.setVisibility(View.VISIBLE);
            bgShadowBottom.setVisibility(View.VISIBLE);
        } else {
            bgShadowTop.setVisibility(View.GONE);
            bgShadowBottom.setVisibility(View.GONE);
        }
    }

    private void favToggle(Wallpaper wallpaper) {
        ImageView img_favorite = findViewById(R.id.img_favorite);
        if (dbHelper.isFavoritesExist(wallpaper.image_id)) {
            img_favorite.setImageResource(R.drawable.ic_action_fav);
        } else {
            img_favorite.setImageResource(R.drawable.ic_action_fav_outline);
        }
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @SuppressWarnings("rawtypes")
    private void showBottomSheetDialog(Wallpaper wallpaper) {
        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.include_info, null);
        FrameLayout lyt_bottom_sheet = view.findViewById(R.id.bottom_sheet);

        if (sharedPref.getIsDarkTheme()) {
            lyt_bottom_sheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_dark));
        } else {
            lyt_bottom_sheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_default));
        }

        if (wallpaper.image_name.equals("")) {
            ((TextView) view.findViewById(R.id.txt_wallpaper_name)).setText("-");
        } else {
            ((TextView) view.findViewById(R.id.txt_wallpaper_name)).setText(wallpaper.image_name);
        }

        ((TextView) view.findViewById(R.id.txt_category_name)).setText(wallpaper.category_name);
        ((ImageView) view.findViewById(R.id.flag)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog reportDialog = new Dialog(ActivityWallpaperDetail.this);

                reportDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                reportDialog.setContentView(R.layout.dialog_flag_layout);
                reportDialog.setTitle("Report Content");
                // Initialize RadioGroup here
                RadioGroup radioGroupReport = reportDialog.findViewById(R.id.radio_group_report);

                // Access the wallpaper object or its name
                Wallpaper wallpaper = wallpapers.get(viewPager2.getCurrentItem());
                TextView nameEnter = reportDialog.findViewById(R.id.name_enter);
                nameEnter.setText(wallpaper.image_name);

                TextView btnCancel = reportDialog.findViewById(R.id.button_cancel);
                TextView btnReport = reportDialog.findViewById(R.id.report_TV);

                btnReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//
                        int selectedId = radioGroupReport.getCheckedRadioButtonId();

                        // If no option is selected, do nothing
                        if (selectedId == -1) {
                            Toast.makeText(getApplicationContext(), "Please select an option", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Check which radio button was clicked
                        switch (selectedId) {
                            case R.id.radio_sexually_explicit:
                            case R.id.radio_offensive:
                            case R.id.radio_not_of_public_interest:
                            case R.id.radio_bad_quality:
                                // Show second dialog here
                                showSecondDialog();
                                break;
                            case R.id.radio_copyrighted:
                                // Do nothing or close the dialog
                                break;
                        }
                        reportDialog.dismiss();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reportDialog.dismiss();
                    }
                });
                reportDialog.show();
            }
        });
        LinearLayout lyt_tags = view.findViewById(R.id.lyt_tags);
        if (wallpaper.tags.equals("")) {
            lyt_tags.setVisibility(View.GONE);
        } else {
            lyt_tags.setVisibility(View.VISIBLE);
        }

        @SuppressWarnings("unchecked") ArrayList<String> arrayListTags = new ArrayList(Arrays.asList(wallpaper.tags.split(",")));
        AdapterTags adapterTags = new AdapterTags(this, arrayListTags);
        RecyclerView recycler_view_tags = view.findViewById(R.id.recycler_view_tags);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setAlignItems(AlignItems.FLEX_START);
        recycler_view_tags.setLayoutManager(layoutManager);
        recycler_view_tags.setAdapter(adapterTags);

        adapterTags.setOnItemClickListener((v, keyword, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
            intent.putExtra("tags", keyword);
            intent.putExtra(Constant.EXTRA_OBJC, "wallpaper");
            startActivity(intent);

            mBottomSheetDialog.dismiss();
            adsManager.destroyBannerAd();
        });

        if (sharedPref.getIsDarkTheme()) {
            mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogDark);
        } else {
            mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogLight);
        }
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        BottomSheetBehavior bottomSheetBehavior = mBottomSheetDialog.getBehavior();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);
    }

    private void showSecondDialog(){
        Dialog reportDialog = new Dialog(ActivityWallpaperDetail.this);
        reportDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        reportDialog.setContentView(R.layout.dialog_thank_you);

        TextView buttonClose = reportDialog.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportDialog.dismiss();
            }
        });
        reportDialog.show();
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void dialogOptionSetWallpaper(String imageURL, Wallpaper wp) {
        String[] items = getResources().getStringArray(R.array.dialog_set_wallpaper);
        singleChoiceSelected = items[0];
        int itemSelected = 0;
        new AlertDialog.Builder(ActivityWallpaperDetail.this)
                .setTitle(R.string.dialog_set_title)
                .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                .setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {

                    progressDialog.setMessage(getString(R.string.msg_preparing_wallpaper));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> Glide.with(this)
                            .load(imageURL.replace(" ", "%20"))
                            .into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                    if (singleChoiceSelected.equals(getResources().getString(R.string.set_home_screen))) {
                                        wallpaperHelper.setWallpaper(parentView, progressDialog, adsManager, bitmap, HOME_SCREEN);
                                        progressDialog.setMessage(getString(R.string.msg_apply_wallpaper));
                                    } else if (singleChoiceSelected.equals(getResources().getString(R.string.set_lock_screen))) {
                                        wallpaperHelper.setWallpaper(parentView, progressDialog, adsManager, bitmap, LOCK_SCREEN);
                                        progressDialog.setMessage(getString(R.string.msg_apply_wallpaper));
                                    } else if (singleChoiceSelected.equals(getResources().getString(R.string.set_both))) {
                                        wallpaperHelper.setWallpaper(parentView, progressDialog, adsManager, bitmap, BOTH);
                                        progressDialog.setMessage(getString(R.string.msg_apply_wallpaper));
                                    } else if (singleChoiceSelected.equals(getResources().getString(R.string.set_crop))) {
                                        if (wp.type.equals("upload")) {
                                            Intent intent = new Intent(getApplicationContext(), ActivityCropWallpaper.class);
                                            intent.putExtra("image_url", sharedPref.getBaseUrl() + "/upload/" + wp.image_upload);
                                            startActivity(intent);
                                        } else if (wp.type.equals("url")) {
                                            Intent intent = new Intent(getApplicationContext(), ActivityCropWallpaper.class);
                                            intent.putExtra("image_url", wp.image_url);
                                            startActivity(intent);
                                        }
                                        progressDialog.dismiss();
                                    } else if (singleChoiceSelected.equals(getResources().getString(R.string.set_with))) {
                                        if (wp.type.equals("upload")) {
                                            wallpaperHelper.setWallpaperFromOtherApp(sharedPref.getBaseUrl() + "/upload/" + wp.image_upload);
                                        } else if (wp.type.equals("url")) {
                                            wallpaperHelper.setWallpaperFromOtherApp(wp.image_url);
                                        }
                                        progressDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    super.onLoadFailed(errorDrawable);
                                    Snackbar.make(parentView, getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }), Constant.DELAY_SET);

                })
                .setNegativeButton(R.string.dialog_option_cancel, null)
                .show();
    }

    public Boolean verifyPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            int permissionExternalMemory = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
                String[] STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, 1);
                return false;
            }
            return true;
        }
        return true;
    }

    public void showFullScreen() {
        if (flag) {
            fullScreenMode(true);
            flag = false;
        } else {
            fullScreenMode(false);
            flag = true;
        }
    }

    public void fullScreenMode(boolean on) {
        if (on) {
            toolbar.setVisibility(View.GONE);
            toolbar.animate().translationY(-112);
            lytBottom.setVisibility(View.GONE);
            lytBottom.animate().translationY(lytBottom.getHeight());

            bgShadowTop.setVisibility(View.GONE);
            bgShadowTop.animate().translationY(-112);

            bgShadowBottom.setVisibility(View.GONE);
            bgShadowBottom.animate().translationY(lytBottom.getHeight());

            Tools.transparentStatusBarNavigation(this);

            hideSystemUI();

        } else {
            toolbar.setVisibility(View.VISIBLE);
            toolbar.animate().translationY(0);
            lytBottom.setVisibility(View.VISIBLE);
            lytBottom.animate().translationY(0);

            bgShadowTop.setVisibility(View.VISIBLE);
            bgShadowTop.animate().translationY(0);

            bgShadowBottom.setVisibility(View.VISIBLE);
            bgShadowBottom.animate().translationY(0);

            if (adsPref.getBannerAdStatusDetail() != 0) {
                Tools.transparentStatusBar(this);
            } else {
                Tools.transparentStatusBarNavigation(this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        adsManager.destroyBannerAd();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        adsManager.resumeBannerAd(adsPref.getBannerAdStatusDetail());
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        adsManager.destroyBannerAd();
//    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


}
