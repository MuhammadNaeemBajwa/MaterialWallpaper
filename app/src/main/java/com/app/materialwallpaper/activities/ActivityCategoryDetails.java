package com.app.materialwallpaper.activities;
import static com.app.materialwallpaper.utils.Constant.EXTRA_OBJC;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.adapters.AdapterMenu;
import com.app.materialwallpaper.adapters.AdapterWallpaper;
import com.app.materialwallpaper.callbacks.CallbackWallpaper;
import com.app.materialwallpaper.databases.prefs.AdsPref;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.databases.sqlite.DBHelper;
import com.app.materialwallpaper.models.Category;
import com.app.materialwallpaper.models.Wallpaper;
import com.app.materialwallpaper.rests.ApiInterface;
import com.app.materialwallpaper.rests.RestAdapter;
import com.app.materialwallpaper.utils.AdsManager;
import com.app.materialwallpaper.utils.Constant;
import com.app.materialwallpaper.utils.Tools;
import com.app.materialwallpaper.view.CustomFilterDropDown;
import com.facebook.shimmer.ShimmerFrameLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityCategoryDetails extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdapterWallpaper adapterWallpaper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout lytShimmer;
    private Call<CallbackWallpaper> callbackCall = null;
    private int postTotal = 0;
    private int failedPage = 0;
    List<Wallpaper> wallpapers = new ArrayList<>();
    Category category;
    SharedPref sharedPref;
    DBHelper dbHelper;
    AdsPref adsPref;
    AdsManager adsManager;
    ImageButton btn_sort;
    private int currentPage = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        if (sharedPref.getIsDarkTheme()) {
            Tools.darkNavigation(this);
        } else {
            Tools.lightNavigation(this);
        }
        setContentView(R.layout.activity_category_details);
        Tools.getRtlDirection(this);
        dbHelper = new DBHelper(this);
        sharedPref.setDefaultSortWallpaper();

        category = (Category) getIntent().getSerializableExtra(EXTRA_OBJC);

        Constant.FILTER = Constant.FILTER_DEFAULT;
        Constant.ORDER = Constant.ORDER_DEFAULT;
        Constant.LAST_SELECTED_ITEM_POSITION = 0;

        adsManager = new AdsManager(this);
        adsManager.loadBannerAd(adsPref.getBannerAdStatusCategoryDetail());
        adsManager.loadInterstitialAd(adsPref.getInterstitialAdClickWallpaper(), adsPref.getInterstitialAdInterval());

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary);
        swipeRefreshLayout.setEnabled(false);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        initShimmerLayout();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(sharedPref.getWallpaperColumns(), StaggeredGridLayoutManager.VERTICAL));

        //set data and list adapter
        adapterWallpaper = new AdapterWallpaper(this, recyclerView);
        adapterWallpaper.insertData(1, wallpapers);
        recyclerView.setAdapter(adapterWallpaper);

        // on item list clicked
        adapterWallpaper.setOnItemClickListener((v, obj, position) -> {
            Constant.wallpapers.clear();
            Constant.wallpapers.addAll(adapterWallpaper.getCurrentItems());
            Constant.position = position;

            if(Constant.wallpapers.get(position).isPremium() && !MyApplication.getApp().isPremium()){
                BuyPremiumActivity.start(ActivityCategoryDetails.this, null);
            }else {
                Intent intent = new Intent(ActivityCategoryDetails.this, ActivityWallpaperDetail.class);
                startActivity(intent);
            }

            adsManager.showInterstitialAd();
            adsManager.destroyBannerAd();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
            }
        });

        requestAction();
        setupToolbar();
        onOptionMenuClicked();

    }


    public void setLoadMore(int currentPage) {
        if (adapterWallpaper.isPageLoaded(currentPage)) {
            adapterWallpaper.setCurPage(currentPage);
        } else {
            requestAction();
        }
    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        if (sharedPref.getIsDarkTheme()) {
            Tools.darkToolbar(this, toolbar);
        } else {
            Tools.lightToolbar(this, toolbar);
        }
        final TextView title_toolbar = findViewById(R.id.title_toolbar);
        setSupportActionBar(toolbar);
        CustomFilterDropDown filterDropDown = findViewById(R.id.filterDropDown);
        filterDropDown.setOnItemChangedListener(new CustomFilterDropDown.OnItemChangedListener() {
            @Override
            public void onItemChanged(String text) {
                if (text.equalsIgnoreCase(Wallpaper.Price.ALL.value)) {
                    Constant.FILTER = Constant.FILTER_ALL;
                } else if (text.equalsIgnoreCase(Wallpaper.Price.FREE.value)) {
                    Constant.FILTER = Constant.FILTER_FREE;
                } else if (text.equalsIgnoreCase(Wallpaper.Price.PREMIUM.value)) {
                    Constant.FILTER = Constant.FILTER_PREMIUM;;
                }
                adapterWallpaper.clear();
                setLoadMore(1);

            }

            @Override
            public void onBuySelected() {
                try {
                    BuyPremiumActivity.start(ActivityCategoryDetails.this, wallpapers.get(Constant.position).image_url);
                }catch (Exception e){
                    BuyPremiumActivity.start(ActivityCategoryDetails.this, null);
                }

            }
        });
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            title_toolbar.setText("" + category.category_name);
        }
    }

    private void displayApiResult(final List<Wallpaper> wallpapers) {
        insertData(wallpapers);
        swipeProgress(false);
        if (wallpapers.size() == 0) {
            showNoItemView(true);
        }

    }

    private void requestListPostApi(final int page_no) {
        currentPage = page_no;
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());

        if (sharedPref.getWallpaperColumns() == 3) {
            callbackCall = apiInterface.getCategoryDetails(page_no, Constant.LOAD_MORE_3_COLUMNS, category.category_id, Constant.FILTER, Constant.ORDER);
        } else {
            callbackCall = apiInterface.getCategoryDetails(page_no, Constant.LOAD_MORE_2_COLUMNS, category.category_id, Constant.FILTER, Constant.ORDER);
        }

        callbackCall.enqueue(new Callback<CallbackWallpaper>() {
            @Override
            public void onResponse(Call<CallbackWallpaper> call, Response<CallbackWallpaper> response) {
                CallbackWallpaper resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    postTotal = resp.count_total;
                    displayApiResult(resp.posts);
                    if (page_no == 1)
                        dbHelper.truncateTableWallpaper(DBHelper.TABLE_CATEGORY_DETAIL);
                    dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_CATEGORY_DETAIL);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(Call<CallbackWallpaper> call, Throwable t) {
                swipeProgress(false);
                loadDataFromDatabase(call, page_no);
            }
        });
    }

    private void loadDataFromDatabase(Call<CallbackWallpaper> call, final int page_no) {
        List<Wallpaper> wallpapers = dbHelper.getAllWallpaperByCategory(DBHelper.TABLE_CATEGORY_DETAIL, category.category_id);
        insertData(wallpapers);
        if (wallpapers.size() == 0) {
            if (!call.isCanceled()) onFailRequest(page_no);
        }
    }

    private void insertData(List<Wallpaper> wallpapers) {
            adapterWallpaper.insertData(currentPage,wallpapers);

    }


    private void onFailRequest(int page_no) {
        failedPage = page_no;
        adapterWallpaper.setLoaded();
        swipeProgress(false);
        showFailedView(true, getString(R.string.failed_text));
    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        swipeProgress(true);
        requestListPostApi(1); // Directly call the method with page number 1
    }


    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.msg_no_item);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            hideShimmerLayout();
            return;
        } else {
            showShimmerLayout();
        }
    }
    private void showShimmerLayout() {
        recyclerView.setVisibility(View.GONE);
        lytShimmer.setVisibility(View.VISIBLE);
        lytShimmer.startShimmer();
    }

    private void hideShimmerLayout() {
        recyclerView.setVisibility(View.VISIBLE);
        lytShimmer.setVisibility(View.GONE);
        lytShimmer.stopShimmer();
    }
    public void initShimmerLayout() {
        View view_shimmer_2_columns = findViewById(R.id.view_shimmer_2_columns);
        View view_shimmer_3_columns = findViewById(R.id.view_shimmer_3_columns);
        View view_shimmer_2_columns_square = findViewById(R.id.view_shimmer_2_columns_square);
        View view_shimmer_3_columns_square = findViewById(R.id.view_shimmer_3_columns_square);

        if (Config.DISPLAY_WALLPAPER == 1) {
            if (sharedPref.getWallpaperColumns() == 3) {
                view_shimmer_3_columns.setVisibility(View.VISIBLE);
            } else {
                view_shimmer_2_columns.setVisibility(View.VISIBLE);
            }
        } else {
            if (sharedPref.getWallpaperColumns() == 3) {
                view_shimmer_3_columns_square.setVisibility(View.VISIBLE);
            } else {
                view_shimmer_2_columns_square.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onOptionMenuClicked() {

        findViewById(R.id.btn_search).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
            intent.putExtra(Constant.EXTRA_OBJC, "wallpaper");
            startActivity(intent);
        });

        btn_sort = findViewById(R.id.btn_sort);
        if (sharedPref.getMenuList() != null && sharedPref.getMenuList().size() > 1) {
            btn_sort.setOnClickListener(v -> {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ActivityCategoryDetails.this);
                View view = layoutInflaterAndroid.inflate(R.layout.dialog_sort, null);

                RecyclerView recyclerView = view.findViewById(R.id.recycler_view_sort);
                recyclerView.setLayoutManager(new LinearLayoutManager(ActivityCategoryDetails.this));
                AdapterMenu adapterMenu = new AdapterMenu(ActivityCategoryDetails.this, new ArrayList<>());
                adapterMenu.setListData(sharedPref.getMenuList());
                recyclerView.setAdapter(adapterMenu);
                recyclerView.postDelayed(() -> Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(Constant.LAST_SELECTED_ITEM_POSITION)).itemView.performClick(), 0);

                final AlertDialog.Builder alert = new AlertDialog.Builder(ActivityCategoryDetails.this);
                alert.setView(view);
                alert.setCancelable(false);
                alert.setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> {
                    if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
                    adapterWallpaper.resetListData();
                    if (Tools.isConnect(this)) {
                        dbHelper.deleteWallpaperByCategory(DBHelper.TABLE_CATEGORY_DETAIL, category.category_id);
                    }
                    requestAction();
                    dialog.dismiss();
                });
                alert.setNegativeButton(R.string.dialog_option_cancel, (dialog, i) -> {
                    Constant.ORDER = Constant.ORDER_DEFAULT;
                    Constant.FILTER = Constant.FILTER_DEFAULT;
                    Constant.LAST_SELECTED_ITEM_POSITION = 0;
                    dialog.dismiss();
                });
                alert.show();
            });
        } else {
            btn_sort.setVisibility(View.GONE);
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
        adsManager.resumeBannerAd(adsPref.getBannerAdStatusCategoryDetail());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
        adsManager.destroyBannerAd();
    }

}
