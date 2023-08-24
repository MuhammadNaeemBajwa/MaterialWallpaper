package com.app.materialwallpaper.activities;

import static com.app.materialwallpaper.fragments.FragmentWallpaper2.WALLPAPER_PER_PAGE;
import static com.app.materialwallpaper.utils.Constant.EXTRA_OBJC;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_MAX;
import static com.solodroid.ads.sdk.util.Constant.FAN;
import static com.solodroid.ads.sdk.util.Constant.GOOGLE_AD_MANAGER;
import static com.solodroid.ads.sdk.util.Constant.STARTAPP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.VideoAd.VideoAd;
import com.app.materialwallpaper.adapters.AdapterCategory;
import com.app.materialwallpaper.adapters.AdapterSearch;
import com.app.materialwallpaper.adapters.AdapterWallpaper;
import com.app.materialwallpaper.callbacks.CallbackCategory;
import com.app.materialwallpaper.callbacks.CallbackWallpaper;
import com.app.materialwallpaper.components.ItemOffsetDecoration;
import com.app.materialwallpaper.databases.prefs.AdsPref;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.databases.sqlite.DBHelper;
import com.app.materialwallpaper.fragments.FragmentWallpaper2;
import com.app.materialwallpaper.models.Category;
import com.app.materialwallpaper.models.Wallpaper;
import com.app.materialwallpaper.rests.ApiInterface;
import com.app.materialwallpaper.rests.RestAdapter;
import com.app.materialwallpaper.utils.AdsManager;
import com.app.materialwallpaper.utils.Constant;
import com.app.materialwallpaper.utils.Tools;
import com.app.materialwallpaper.view.HorizontalPagingIndicator;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySearch extends AppCompatActivity {

    private EditText edtSearch;
    private EditText edtIndex;
    private RecyclerView recyclerViewWallpaper;
    private RecyclerView recyclerViewCategory;
    private RecyclerView recyclerViewSuggestion;
    private AdapterWallpaper adapterWallpaper;
    private AdapterCategory adapterCategory;
    private AdapterSearch mAdapterSuggestion;
    private LinearLayout lytSuggestion;
    private ImageButton btClear;
    private ShimmerFrameLayout lytShimmer;
    private RelativeLayout viewShimmerWallpaper;
    private RelativeLayout viewShimmerCategory;
    private int postTotal = 0;
    private int failedPage = 0;
    Call<CallbackWallpaper> callbackCallWallpaper = null;
    Call<CallbackCategory> callbackCallCategory = null;
    String tags = "";
    List<Wallpaper> wallpapers = new ArrayList<>();
    List<Category> categories = new ArrayList<>();
    SharedPref sharedPref;
    AdsPref adsPref;
    CoordinatorLayout parentView;
    RadioGroup radioGroupSearch;
    String data;
    String flagType;
    AdsManager adsManager;
    LinearLayout lytBannerAd;
    private int currentPage = 1;

    private boolean isLoading = false;
    private final boolean isLastPage = false;
    private final List<Wallpaper> wallpaperList = new ArrayList<>();
    private final int PAGE_SIZE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        adsPref = new AdsPref(this);
        sharedPref = new SharedPref(this);
        if (sharedPref.getIsDarkTheme()) {
            Tools.darkNavigation(this);
        } else {
            Tools.lightNavigation(this);
        }
        setContentView(R.layout.activity_search);
        Tools.getRtlDirection(this);
        initComponent();
        initShimmerLayout();
        setupToolbar();


        adsManager = new AdsManager(this);
        adsManager.loadBannerAd(adsPref.getBannerAdStatusSearch());
        adsManager.loadInterstitialAd(adsPref.getInterstitialAdClickWallpaper(), adsPref.getInterstitialAdInterval());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initComponent() {

        Intent intent = getIntent();
        data = intent.getStringExtra(Constant.EXTRA_OBJC);

        parentView = findViewById(R.id.coordinatorLayout);
        viewShimmerWallpaper = findViewById(R.id.view_shimmer_wallpaper);
        viewShimmerCategory = findViewById(R.id.view_shimmer_category);
        radioGroupSearch = findViewById(R.id.radioGroupSearch);
        edtIndex = findViewById(R.id.edt_index);

        initRecyclerView();

        lytBannerAd = findViewById(R.id.lyt_banner_ad);
        lytSuggestion = findViewById(R.id.lyt_suggestion);
        edtSearch = findViewById(R.id.et_search);
        btClear = findViewById(R.id.bt_clear);
        btClear.setVisibility(View.GONE);
        lytShimmer = findViewById(R.id.shimmer_view_container);

        edtSearch.addTextChangedListener(textWatcher);
        if (getIntent().hasExtra("tags")) {
            tags = getIntent().getStringExtra("tags");
            hideKeyboard();
            searchActionTags(1);
        } else {
            edtSearch.requestFocus();
            swipeProgress(false);
        }

//        recyclerViewSuggestion.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSuggestion.setLayoutManager(new StaggeredGridLayoutManager(sharedPref.getWallpaperColumns(), StaggeredGridLayoutManager.VERTICAL));
        //recycler_view_suggestion.setHasFixedSize(true);

        //set data and list adapter suggestion
        mAdapterSuggestion = new AdapterSearch(this);
        recyclerViewSuggestion.setAdapter(mAdapterSuggestion);
        showSuggestionSearch();
        mAdapterSuggestion.setOnItemClickListener((view, viewModel, pos) -> {
            edtSearch.setText(viewModel);
            lytSuggestion.setVisibility(View.GONE);
            hideKeyboard();
            currentPage = 1;
            searchActionWallpaper( currentPage);
        });

        btClear.setOnClickListener(view -> edtSearch.setText(""));

        edtSearch.setOnTouchListener((view, motionEvent) -> {
            showSuggestionSearch();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return false;
        });

        if (data.equals("category")) {
            radioGroupSearch.check(radioGroupSearch.getChildAt(1).getId());
            requestSearchCategory();
        } else {
            radioGroupSearch.check(radioGroupSearch.getChildAt(0).getId());
            requestSearchWallpaper();
        }

        flagType = edtIndex.getText().toString();
        edtIndex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                flagType = edtIndex.getText().toString();
                showKeyboard();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        radioGroupSearch.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.radio_button_wallpaper:
                    requestSearchWallpaper();
                    recyclerViewWallpaper.setVisibility(View.VISIBLE);
                    recyclerViewCategory.setVisibility(View.GONE);
                    findViewById(R.id.lyt_no_item).setVisibility(View.GONE);
                    break;
                case R.id.radio_button_category:
                    requestSearchCategory();
                    recyclerViewWallpaper.setVisibility(View.GONE);
                    recyclerViewCategory.setVisibility(View.VISIBLE);
                    findViewById(R.id.lyt_no_item).setVisibility(View.GONE);
                    break;
            }
        });

    }

    public void initRecyclerView() {
        recyclerViewWallpaper = findViewById(R.id.recycler_view_wallpaper);
        recyclerViewCategory = findViewById(R.id.recycler_view_category);
        recyclerViewSuggestion = findViewById(R.id.recycler_view_suggestion);
    }

    public void requestSearchWallpaper() {
        edtIndex.setText("0");
        recyclerViewWallpaper.setVisibility(View.VISIBLE);
        recyclerViewCategory.setVisibility(View.GONE);
        viewShimmerWallpaper.setVisibility(View.VISIBLE);
        viewShimmerCategory.setVisibility(View.GONE);

        recyclerViewWallpaper.setLayoutManager(new StaggeredGridLayoutManager(sharedPref.getWallpaperColumns(), StaggeredGridLayoutManager.VERTICAL));
        //recycler_view_wallpaper.setHasFixedSize(true);

        //set data and list adapter
        adapterWallpaper = new AdapterWallpaper(this, recyclerViewWallpaper);
        adapterWallpaper.insertData(1, wallpapers);
        adapterWallpaper.setCurPage(1);
        recyclerViewWallpaper.setAdapter(adapterWallpaper);
        adapterWallpaper.setOnItemClickListener((view, obj, position) -> {
            Constant.wallpapers.clear();
            Constant.wallpapers.addAll(adapterWallpaper.getCurrentItems());
            Constant.position = position;
            if(Constant.wallpapers.get(position).isPremium() && !MyApplication.getApp().isPremium()){
//                BuyPremiumActivity.start(ActivitySearch.this, null);
                VideoAd.start(ActivitySearch.this, null);
            }else {
                Intent intent = new Intent(ActivitySearch.this, ActivityWallpaperDetail.class);
                startActivity(intent);
            }

//            adsManager.showInterstitialAd();
//            adsManager.destroyBannerAd();
        });

//        recyclerViewWallpaper.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
//                super.onScrollStateChanged(v, state);
//            }
//        });

        recyclerViewWallpaper.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                int[] lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = getFirstVisibleItem(lastVisibleItemPositions);


                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                        // Load more data when the user scrolls to the end of the list

                        loadMoreData();
                    }
                }
            }
        });


        // detect when scroll reach bottom
        adapterWallpaper.setOnLoadMoreListener(current_page -> {
            if (adsPref.getNativeAdWallpaperList() != 0) {
                if (adsPref.getAdType().equals("unity")) {
                    setLoadMore(current_page);
                } else {
                    setLoadMoreNativeAd(current_page);
                }
            } else {
                setLoadMore(current_page);
            }
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (edtSearch.getText().toString().equals("")) {
                    Snackbar.make(parentView, getString(R.string.msg_search_input), Snackbar.LENGTH_SHORT).show();
                    hideKeyboard();
                    swipeProgress(false);
                } else {
                    adapterWallpaper.resetListData();
                    hideKeyboard();
                    currentPage = 1;
                    searchActionWallpaper( currentPage);
                }
                return true;
            }
            return false;
        });



    }

    private int getFirstVisibleItem(int[] positions) {
        int firstVisibleItem = positions[0];
        for (int position : positions) {
            if (position < firstVisibleItem) {
                firstVisibleItem = position;
            }
        }
        return firstVisibleItem;
    }
    private void loadMoreData() {
        if (!isLoading && !isLastPage) {
            requestAction(currentPage);
        }
    }
    private void requestAction(final int page_no) {
        isLoading = true;
        currentPage = page_no;
        showFailedView(false, "");
        showNoItemView(false);
        swipeProgress(true);
        requestSearchApiWallpaper(currentPage,"10"); // Directly call the method with page number 1
    }
    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerViewWallpaper.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerViewWallpaper.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failedPage));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.msg_no_item);
        if (show) {
            recyclerViewWallpaper.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerViewWallpaper.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    public void setLoadMoreNativeAd(int current_page) {
        Log.d("page", "currentPage: " + current_page);
        // Assuming final total items equal to real post items plus the ad
        int totalItemBeforeAds = (adapterWallpaper.getItemCount() - current_page);
        if (postTotal > totalItemBeforeAds && current_page != 0) {
            // int next_page = current_page + 1;
            searchActionWallpaper(current_page);
        } else {
            adapterWallpaper.setLoaded();
        }
    }

    public void setLoadMore(int current_page) {
        if (adapterWallpaper.isPageLoaded(currentPage)) {
            adapterWallpaper.setCurPage(currentPage);
        } else {
            searchActionWallpaper(currentPage);
        }
    }

    public void requestSearchCategory() {
        edtIndex.setText("1");
        recyclerViewWallpaper.setVisibility(View.GONE);
        recyclerViewCategory.setVisibility(View.VISIBLE);
        viewShimmerWallpaper.setVisibility(View.GONE);
        viewShimmerCategory.setVisibility(View.VISIBLE);

        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.grid_space_wallpaper);
        int padding = getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper);
        recyclerViewCategory.setPadding(padding, padding, padding, padding);
        recyclerViewCategory.setLayoutManager(new StaggeredGridLayoutManager(Config.DEFAULT_CATEGORY_COLUMN, LinearLayoutManager.VERTICAL));
        if (0 == recyclerViewCategory.getItemDecorationCount()) {
            recyclerViewCategory.addItemDecoration(itemDecoration);
        }

        //set data and list adapter
        adapterCategory = new AdapterCategory(this, categories);
        recyclerViewCategory.setAdapter(adapterCategory);

        // on item list clicked
        adapterCategory.setOnItemClickListener((v, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityCategoryDetails.class);
            intent.putExtra(EXTRA_OBJC, obj);
            startActivity(intent);
            adsManager.showInterstitialAd();
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (edtSearch.getText().toString().equals("")) {
                    Snackbar.make(parentView, getString(R.string.msg_search_input), Snackbar.LENGTH_SHORT).show();
                    hideKeyboard();
                    swipeProgress(false);
                } else {
                    adapterCategory.resetListData();
                    hideKeyboard();
                    searchActionCategory();
                }
                return true;
            }
            return false;
        });

    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (sharedPref.getIsDarkTheme()) {
            Tools.darkToolbar(this, toolbar);
        } else {
            Tools.lightToolbar(this, toolbar);
        }
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                btClear.setVisibility(View.GONE);
            } else {
                btClear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };
    private void displayApiResult(final List<Wallpaper> wallpapers) {
        insertData(wallpapers);
        swipeProgress(false);
        if (wallpapers.size() == 0) {
            showNoItemView(true);
        }

    }
    private void insertData(List<Wallpaper> wallpapers) {
        adapterWallpaper.insertData(currentPage, wallpapers);

    }

    private void requestSearchApiWallpaper(final int page_no, final String query) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());

//        if (sharedPref.getWallpaperColumns() == 3) {
//            callbackCallWallpaper = apiInterface.getSearch(page_no, FragmentWallpaper2.WALLPAPER_PER_PAGE, query, Constant.ORDER_RECENT);
//        } else {
            callbackCallWallpaper = apiInterface.getSearch(page_no, WALLPAPER_PER_PAGE, query, Constant.ORDER_RECENT);
        //}
        swipeProgress(true);
        callbackCallWallpaper.enqueue(new Callback<CallbackWallpaper>() {
            @Override
            public void onResponse(Call<CallbackWallpaper> call, Response<CallbackWallpaper> response) {
                CallbackWallpaper resp = response.body();
                swipeProgress(false);
                if (resp != null && resp.status.equals("ok")) {
                    postTotal = resp.count_total;
                    isLoading = false;
                    currentPage++;
                    postTotal = resp.count_total;
                    displayApiResult(resp.posts);
                    wallpaperList.addAll(resp.posts);
                    displayApiResult(wallpaperList);

                        adapterWallpaper.insertData(currentPage, resp.posts);
//                    }
                    if (resp.posts.size() == 0) {
                        showNotFoundViewWallpaper(true);
                    } else {
                        lytBannerAd.setVisibility(View.VISIBLE);
                    }
                } else {
                    onFailRequestWallpaper(page_no);
                }
                swipeProgress(false);
            }

            @Override
            public void onFailure(Call<CallbackWallpaper> call, Throwable t) {
                onFailRequestWallpaper(page_no);
                swipeProgress(false);
            }

        });
    }

    private void requestSearchApiCategory(final String query) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        callbackCallCategory = apiInterface.getSearchCategory(query);
        callbackCallCategory.enqueue(new Callback<CallbackCategory>() {
            @Override
            public void onResponse(Call<CallbackCategory> call, Response<CallbackCategory> response) {
                CallbackCategory resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    adapterCategory.insertData(resp.categories);
                    swipeProgress(false);
                    if (resp.categories.size() == 0) {
                        showNotFoundViewCategory(true);
                    } else {
                        lytBannerAd.setVisibility(View.VISIBLE);
                    }
                } else {
                    onFailRequestCategory();
                }
            }

            @Override
            public void onFailure(Call<CallbackCategory> call, Throwable t) {
                onFailRequestCategory();
                swipeProgress(false);
            }

        });
    }

    private void onFailRequestWallpaper(int page_no) {
        failedPage = page_no;
        adapterWallpaper.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(this)) {
            showFailedViewWallpaper(true, getString(R.string.failed_text));
        } else {
            showFailedViewWallpaper(true, getString(R.string.failed_text));
        }
    }

    private void onFailRequestCategory() {
        swipeProgress(false);
        if (Tools.isConnect(this)) {
            showFailedViewCategory(true, getString(R.string.failed_text));
        } else {
            showFailedViewCategory(true, getString(R.string.failed_text));
        }
    }

    private void searchActionWallpaper(final int page_no) {
        lytSuggestion.setVisibility(View.GONE);
        showFailedViewWallpaper(false, "");
        showNotFoundViewWallpaper(false);
        final String query = edtSearch.getText().toString().trim();
        if (!query.equals("")) {
            if (page_no == 1) {
                swipeProgress(true);
            } else {
                adapterWallpaper.setLoading();
            }
            new Handler(Looper.getMainLooper()).postDelayed(() -> requestSearchApiWallpaper(page_no, query), Constant.DELAY_TIME);
        } else {
            Snackbar.make(parentView, getString(R.string.msg_search_input), Snackbar.LENGTH_SHORT).show();
            swipeProgress(false);
        }
    }

    private void searchActionCategory() {
        lytSuggestion.setVisibility(View.GONE);
        showFailedViewCategory(false, "");
        showNotFoundViewCategory(false);
        final String query = edtSearch.getText().toString().trim();
        if (!query.equals("")) {
            swipeProgress(true);
            new Handler(Looper.getMainLooper()).postDelayed(() -> requestSearchApiCategory(query), Constant.DELAY_TIME);
        } else {
            Snackbar.make(parentView, getString(R.string.msg_search_input), Snackbar.LENGTH_SHORT).show();
            swipeProgress(false);
        }
    }

    private void searchActionTags(final int page_no) {
        lytSuggestion.setVisibility(View.GONE);
        showFailedViewWallpaper(false, "");
        showNotFoundViewWallpaper(false);
        edtSearch.setText(tags);
        final String query = edtSearch.getText().toString().trim();
        if (!query.equals("")) {
            if (page_no == 1) {
                swipeProgress(true);
            } else {
                adapterWallpaper.setLoading();
            }
            new Handler(Looper.getMainLooper()).postDelayed(() -> requestSearchApiWallpaper(page_no, query), Constant.DELAY_TIME);
        } else {
            Snackbar.make(parentView, getString(R.string.msg_search_input), Snackbar.LENGTH_SHORT).show();
            swipeProgress(false);
        }
    }

    private void showSuggestionSearch() {
        mAdapterSuggestion.refreshItems();
        lytSuggestion.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        edtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void showFailedViewWallpaper(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerViewWallpaper.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerViewWallpaper.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> searchActionWallpaper(failedPage));
    }

    private void showFailedViewCategory(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerViewCategory.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerViewCategory.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> searchActionCategory());
    }

    private void showNotFoundViewWallpaper(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_search_wallpaper_found);
        if (show) {
            recyclerViewWallpaper.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerViewWallpaper.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void showNotFoundViewCategory(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_search_category_found);
        if (show) {
            recyclerViewCategory.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerViewCategory.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            recyclerViewWallpaper.setVisibility(View.VISIBLE);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
        } else {
            recyclerViewWallpaper.setVisibility(View.GONE);

            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        }
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

    @Override
    public void onBackPressed() {
        if (getIntent().hasExtra("tags")) {
            super.onBackPressed();
            adsManager.destroyBannerAd();
        } else {
            if (edtSearch.length() > 0) {
                edtSearch.setText("");
            } else {
                super.onBackPressed();
                adsManager.destroyBannerAd();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            hideKeyboard();
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
        adsManager.resumeBannerAd(adsPref.getBannerAdStatusSearch());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adsManager.destroyBannerAd();
    }

}
