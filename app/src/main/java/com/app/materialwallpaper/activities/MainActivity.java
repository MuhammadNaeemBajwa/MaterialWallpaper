package com.app.materialwallpaper.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.materialwallpaper.BuildConfig;
import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.components.AppBarLayoutBehavior;
import com.app.materialwallpaper.components.RtlViewPager;
import com.app.materialwallpaper.databases.prefs.AdsPref;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.fragments.FragmentCategory;
import com.app.materialwallpaper.fragments.FragmentFavorite;
import com.app.materialwallpaper.fragments.FragmentTabLayout;
import com.app.materialwallpaper.utils.AdsManager;
import com.app.materialwallpaper.utils.Constant;
import com.app.materialwallpaper.utils.Tools;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

public class MainActivity extends AppCompatActivity {


    public static final String TAG = "MainActivity";
    AppBarLayout appBarLayout;
    private ViewPager viewPager;
    private RtlViewPager viewPagerRTL;
    private long exitTime = 0;
    private CoordinatorLayout coordinatorLayout;
    MenuItem prevMenuItem;
    int pagerNumber = 3;
    private BottomNavigationView bottomNavigationView;
    AdsPref adsPref;
    Toolbar toolbar;
    SharedPref sharedPref;
    RelativeLayout bgLine;
    private AdView adView;
    AdsManager adsManager;
    LinearLayout viewBannerAd;
    private AppUpdateManager appUpdateManager;
    private BottomSheetDialog mBottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        sharedPref = new SharedPref(this);
        if (sharedPref.getIsDarkTheme()) {
            Tools.darkNavigation(this);
            getWindow().setBackgroundDrawableResource(R.drawable.bg_primary_dark);
        } else {
            Tools.lightNavigation(this);
            getWindow().setBackgroundDrawableResource(R.drawable.bg_primary_light);
        }
        adsPref = new AdsPref(this);
        if (Config.ENABLE_RTL_MODE) {
            setContentView(R.layout.activity_main_rtl);
        } else {
            setContentView(R.layout.activity_main);
        }

        adsManager = new AdsManager(this);
//        adsManager.initializeAd();
//        adsManager.updateConsentStatus();
//        adsManager.loadBannerAd(adsPref.getBannerAdStatusHome());
//        adsManager.loadInterstitialAd(adsPref.getInterstitialAdClickWallpaper(), adsPref.getInterstitialAdInterval());

        Tools.getRtlDirection(this);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        appBarLayout = findViewById(R.id.appbarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        setupToolbar();

        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.getMenu().clear();
        if (Config.DISPLAY_CATEGORY_AS_MAIN_SCREEN) {
            bottomNavigationView.inflateMenu(R.menu.navigation_category);
        } else {
            bottomNavigationView.inflateMenu(R.menu.navigation_wallpaper);
        }

        bgLine = findViewById(R.id.bg_line);
        viewBannerAd = findViewById(R.id.view_banner_ad);
        if (sharedPref.getIsDarkTheme()) {
            bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.color_dark_toolbar));
            bgLine.setBackgroundColor(getResources().getColor(R.color.color_dark_toolbar));
            viewBannerAd.setBackgroundColor(getResources().getColor(R.color.color_dark_toolbar));
        } else {
            bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.color_light_background));
            bgLine.setBackgroundColor(getResources().getColor(R.color.color_light_background));
            viewBannerAd.setBackgroundColor(getResources().getColor(R.color.color_light_background));
        }
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        initViewPager();

        Tools.notificationOpenHandler(this, getIntent());
        requestNotificationPermission();

        if (!BuildConfig.DEBUG) {
            appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
            inAppUpdate();
            inAppReview();
        }


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
        Constant.isAppOpen = false;
    }


    public void showInterstitialAd() {
        adsManager.showInterstitialAd();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        if (!sharedPref.getIsDarkTheme()) {
            toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        } else {
            Tools.darkToolbar(this, toolbar);
            toolbar.getContext().setTheme(R.style.ThemeOverlay_AppCompat_Dark);
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void initViewPager() {

        if (Config.ENABLE_RTL_MODE) {
            viewPagerRTL = findViewById(R.id.view_pager_rtl);
            viewPagerRTL.setAdapter(new MyAdapter(getSupportFragmentManager()));
            viewPagerRTL.setOffscreenPageLimit(pagerNumber);

            if (Config.DISPLAY_CATEGORY_AS_MAIN_SCREEN) {
                bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.navigation_category:
                            viewPagerRTL.setCurrentItem(0);
                            return true;
                        case R.id.navigation_home:
                            viewPagerRTL.setCurrentItem(1);
                            return true;
                        case R.id.navigation_favorite:
                            viewPagerRTL.setCurrentItem(2);
                            return true;
                    }
                    return false;
                });
            } else {
                bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            viewPagerRTL.setCurrentItem(0);
                            return true;
                        case R.id.navigation_category:
                            viewPagerRTL.setCurrentItem(1);
                            return true;
                        case R.id.navigation_favorite:
                            viewPagerRTL.setCurrentItem(2);
                            return true;
                    }
                    return false;
                });
            }

            viewPagerRTL.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        bottomNavigationView.getMenu().getItem(0).setChecked(false);
                    }
                    bottomNavigationView.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = bottomNavigationView.getMenu().getItem(position);

                    if (viewPagerRTL.getCurrentItem() == 0) {
                        toolbar.setTitle(getResources().getString(R.string.app_name));
                    } else if (viewPagerRTL.getCurrentItem() == 1) {
                        if (Config.DISPLAY_CATEGORY_AS_MAIN_SCREEN) {
                            toolbar.setTitle(getResources().getString(R.string.title_nav_home));
                        } else {
                            toolbar.setTitle(getResources().getString(R.string.title_nav_category));
                        }
                    } else if (viewPagerRTL.getCurrentItem() == 2) {
                        toolbar.setTitle(getResources().getString(R.string.title_nav_favorite));
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            viewPager = findViewById(R.id.view_pager);
            viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
                @Override
                public void onPageSelected(int position) {
                    //pagingIndicator.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                    super.onPageSelected(position);
                }
            });
            viewPager.setOffscreenPageLimit(pagerNumber);

            if (Config.DISPLAY_CATEGORY_AS_MAIN_SCREEN) {
                bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.navigation_category:
                            viewPager.setCurrentItem(0);
                            return true;
                        case R.id.navigation_home:
                            viewPager.setCurrentItem(1);
                            return true;
                        case R.id.navigation_favorite:
                            viewPager.setCurrentItem(2);
                            return true;
                    }
                    return false;
                });
            } else {
                bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            viewPager.setCurrentItem(0);
                            return true;
                        case R.id.navigation_category:
                            viewPager.setCurrentItem(1);
                            return true;
                        case R.id.navigation_favorite:
                            viewPager.setCurrentItem(2);
                            return true;
                    }
                    return false;
                });
            }

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        bottomNavigationView.getMenu().getItem(0).setChecked(false);
                    }
                    bottomNavigationView.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = bottomNavigationView.getMenu().getItem(position);

                    if (viewPager.getCurrentItem() == 0) {
                        toolbar.setTitle(getResources().getString(R.string.app_name));
                    } else if (viewPager.getCurrentItem() == 1) {
                        if (Config.DISPLAY_CATEGORY_AS_MAIN_SCREEN) {
                            toolbar.setTitle(getResources().getString(R.string.title_nav_home));
                        } else {
                            toolbar.setTitle(getResources().getString(R.string.title_nav_category));
                        }
                    } else if (viewPager.getCurrentItem() == 2) {
                        toolbar.setTitle(getResources().getString(R.string.title_nav_favorite));
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    @SuppressWarnings("deprecation")
    public class MyAdapter extends FragmentPagerAdapter {

        MyAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (Config.DISPLAY_CATEGORY_AS_MAIN_SCREEN) {
                if (position == 0) {
                    return new FragmentCategory();
                } else if (position == 1) {
                    return new FragmentTabLayout();
                } else {
                    return new FragmentFavorite();
                }
            } else {
                if (position == 0) {
                    return new FragmentTabLayout();
                } else if (position == 1) {
                    return new FragmentCategory();
                } else {
                    return new FragmentFavorite();
                }
            }
        }

        @Override
        public int getCount() {
            return pagerNumber;
        }

    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    @Override
    public void onBackPressed() {
        if (Config.ENABLE_RTL_MODE) {
            if (viewPagerRTL.getCurrentItem() != 0) {
                viewPagerRTL.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        } else {
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        }
    }

    public void exitApp() {
        if (Config.ENABLE_EXIT_DIALOG) {
            showBottomSheetExitDialog();
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                showSnackBar(getString(R.string.press_again_to_exit));
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                destroyBannerAd();
                Constant.isAppOpen = false;
            }
        }
    }

//    private void showBottomSheetExitDialog() {
//        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.dialog_exit, null);
//
//        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);
//        Button btnRate = view.findViewById(R.id.btn_rate);
//        Button btnShare = view.findViewById(R.id.btn_share);
//        Button btnExit = view.findViewById(R.id.btn_exit);
//
//        if (this.sharedPref.getIsDarkTheme()) {
//            lytBottomSheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_dark));
//        } else {
//            lytBottomSheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_default));
//        }
//
//
//        btnRate.setOnClickListener(v -> {
//            final String package_name = BuildConfig.APPLICATION_ID;
//            try {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));
//            } catch (android.content.ActivityNotFoundException anfe) {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + package_name)));
//            }
//            mBottomSheetDialog.dismiss();
//        });
//
//        btnShare.setOnClickListener(v -> {
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_SEND);
//            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
//            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
//            intent.setType("text/plain");
//            startActivity(intent);
//            mBottomSheetDialog.dismiss();
//        });
//
//        btnExit.setOnClickListener(v -> {
//            finish();
//            destroyBannerAd();
//            Constant.isAppOpen = false;
//            mBottomSheetDialog.dismiss();
//        });
//
//        if (Config.ENABLE_RTL_MODE) {
//            if (sharedPref.getIsDarkTheme()) {
//                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogDarkRtl);
//            } else {
//                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogLightRtl);
//            }
//        } else {
//            if (sharedPref.getIsDarkTheme()) {
//                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogDark);
//            } else {
//                mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogLight);
//            }
//        }
//        mBottomSheetDialog.setContentView(view);
//
//        mBottomSheetDialog.show();
//        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);
//
//    }



    //added on 9/14/2023 to avoid a crash




    private void showBottomSheetExitDialog() {
        if (mBottomSheetDialog == null) {
            @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.dialog_exit, null);

            FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);
            Button btnRate = view.findViewById(R.id.btn_rate);
            Button btnShare = view.findViewById(R.id.btn_share);
            Button btnExit = view.findViewById(R.id.btn_exit);

            if (this.sharedPref.getIsDarkTheme()) {
                lytBottomSheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_dark));
            } else {
                lytBottomSheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_default));
            }

            btnRate.setOnClickListener(v -> {
                final String package_name = BuildConfig.APPLICATION_ID;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + package_name)));
                }
                mBottomSheetDialog.dismiss();
            });

            btnShare.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                intent.setType("text/plain");
                startActivity(intent);
                mBottomSheetDialog.dismiss();
            });

            btnExit.setOnClickListener(v -> {
                finish();
                destroyBannerAd();
                Constant.isAppOpen = false;
                mBottomSheetDialog.dismiss();
            });

            if (Config.ENABLE_RTL_MODE) {
                if (sharedPref.getIsDarkTheme()) {
                    mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogDarkRtl);
                } else {
                    mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogLightRtl);
                }
            } else {
                if (sharedPref.getIsDarkTheme()) {
                    mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogDark);
                } else {
                    mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialogLight);
                }
            }
            mBottomSheetDialog.setContentView(view);
        }

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);
    }





    public void showSnackBar(String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            if (Config.ENABLE_RTL_MODE) {
                if (Config.DISPLAY_CATEGORY_AS_MAIN_SCREEN) {
                    if (viewPagerRTL.getCurrentItem() == 0) {
                        Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                        intent.putExtra(Constant.EXTRA_OBJC, "category");
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                        intent.putExtra(Constant.EXTRA_OBJC, "wallpaper");
                        startActivity(intent);
                    }
                } else {
                    if (viewPagerRTL.getCurrentItem() == 1) {
                        Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                        intent.putExtra(Constant.EXTRA_OBJC, "category");
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                        intent.putExtra(Constant.EXTRA_OBJC, "wallpaper");
                        startActivity(intent);
                    }
                }
            } else {
                if (Config.DISPLAY_CATEGORY_AS_MAIN_SCREEN) {
                    if (viewPager.getCurrentItem() == 0) {
                        Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                        intent.putExtra(Constant.EXTRA_OBJC, "category");
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                        intent.putExtra(Constant.EXTRA_OBJC, "wallpaper");
                        startActivity(intent);
                    }
                } else {
                    if (viewPager.getCurrentItem() == 1) {
                        Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                        intent.putExtra(Constant.EXTRA_OBJC, "category");
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                        intent.putExtra(Constant.EXTRA_OBJC, "wallpaper");
                        startActivity(intent);
                    }
                }
            }
            destroyBannerAd();
        } else if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(getApplicationContext(), ActivitySettings.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_rate) {
            final String package_name = BuildConfig.APPLICATION_ID;
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + package_name)));
            }
//        } else if (item.getItemId() == R.id.menu_more) {
//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPref.getMoreAppsUrl())));
        } else if (item.getItemId() == R.id.menu_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_about) {
            aboutDialog();
        }

        else if (item.getItemId() == R.id.contact) {
            contactDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    public void aboutDialog() {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View view = inflater.inflate(R.layout.dialog_about, null);

        TextView txt_app_version = view.findViewById(R.id.txt_app_version);
        txt_app_version.setText(getString(R.string.msg_about_version) + " " + BuildConfig.VERSION_NAME);

        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setView(view);
        alert.setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    public void contactDialog() {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View view = inflater.inflate(R.layout.dialog_setting_contact, null);




        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setView(view);
        alert.setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        destroyBannerAd();
//        Constant.isAppOpen = false;
//    }

    @Override
    protected void onResume() {
        super.onResume();
        adsManager.resumeBannerAd(adsPref.getBannerAdStatusHome());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void destroyBannerAd() {
        adsManager.destroyBannerAd();
    }

    private void inAppReview() {
        if (sharedPref.getInAppReviewToken() <= 3) {
            sharedPref.updateInAppReviewToken(sharedPref.getInAppReviewToken() + 1);
            Log.d(TAG, "in app update token");
        } else {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(e -> {
                    }).addOnCompleteListener(complete -> {
                                Log.d(TAG, "Success");
                            }
                    ).addOnFailureListener(failure -> {
                        Log.d(TAG, "Rating Failed");
                    });
                }
            }).addOnFailureListener(failure -> Log.d(TAG, "In-App Request Failed " + failure));
            Log.d(TAG, "in app token complete, show in app review if available");
        }
        Log.d(TAG, "in app review token : " + sharedPref.getInAppReviewToken());
    }

    private void inAppUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(appUpdateInfo);
            }
        });
    }

    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, Constant.IMMEDIATE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                showSnackBar(getString(R.string.msg_cancel_update));
            } else if (resultCode == RESULT_OK) {
                showSnackBar(getString(R.string.msg_success_update));
            } else {
                showSnackBar(getString(R.string.msg_failed_update));
                inAppUpdate();
            }
        }
    }

    private void requestNotificationPermission() {
        //only for Android 13 or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (this.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 22);
            }
        }
    }

}