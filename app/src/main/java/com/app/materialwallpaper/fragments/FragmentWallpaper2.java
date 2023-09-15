package com.app.materialwallpaper.fragments;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.VideoAd.VideoAd;
import com.app.materialwallpaper.activities.ActivityWallpaperDetail;
import com.app.materialwallpaper.activities.BuyPremiumActivity;
import com.app.materialwallpaper.activities.MainActivity;
import com.app.materialwallpaper.activities.MyApplication;
import com.app.materialwallpaper.adapters.AdapterWallpaper;
import com.app.materialwallpaper.callbacks.CallbackWallpaper;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.databases.sqlite.DBHelper;
import com.app.materialwallpaper.models.Wallpaper;
import com.app.materialwallpaper.rests.ApiInterface;
import com.app.materialwallpaper.rests.RestAdapter;
import com.app.materialwallpaper.utils.AdsManager;
import com.app.materialwallpaper.utils.Constant;
import com.app.materialwallpaper.utils.EventWallpaper;
import com.app.materialwallpaper.utils.SingletonEventBus;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentWallpaper2 extends Fragment {
    private static final int SELECT_VIDEO_REQUEST_CODE = 101;
    public static final String ARG_ORDER = "order";
    public static final String ARG_TYPE = "type";
    public static final String ARG_FILTER = "filter";
    public static int WALLPAPER_PER_PAGE = Constant.LOAD_MORE_2_COLUMNS;
    View rootView;
    private RecyclerView recyclerView;
    private AdapterWallpaper adapterWallpaper;
    private ShimmerFrameLayout lytShimmer;
    private Call<CallbackWallpaper> callbackCall = null;
    private int postTotal = 0;
    private int failedPage = 0;
    private SharedPref sharedPref;
    String order, filter;
    DBHelper dbHelper;
    AdsManager adsManager;
    Activity activity;
    private int currentPage = 1;
    private String wallpaperType = Wallpaper.TYPE_IMAGE;
    private boolean isLoading = false;
    private final boolean isLastPage = false;
    private final List<Wallpaper> wallpaperList=new ArrayList<>();
    private final int PAGE_SIZE = 1480;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    private void loadLocalVideoFiles() {
        Log.d(TAG, "loadLocalVideoFiles: ");
        List<Wallpaper> localVideos = getVideoFilesFromCache();
        // if (localVideos != null) {
        displayApiResult(localVideos);
        // }
    }

    private boolean isVideoWallpaperType() {
        return Wallpaper.TYPE_VIDEO.equals(wallpaperType);
    }

    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, SELECT_VIDEO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_VIDEO_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedVideoUri = data.getData();
            if (selectedVideoUri != null) {
                copyVideoToAppFolder(selectedVideoUri);
            }
        }
    }





//    private void copyVideoToAppFolder(Uri videoUri) {
//        try {
//            InputStream inputStream = activity.getContentResolver().openInputStream(videoUri);
//            if (inputStream != null) {
//                File appVideosFolder = new File(activity.getCacheDir(), "videos");
//                if (!appVideosFolder.exists()) {
//                    appVideosFolder.mkdirs();
//                }
//                DocumentFile documentFile = DocumentFile.fromSingleUri(activity, videoUri);
//                if (documentFile != null) {
//                    String fileName = documentFile.getName();
//                    assert fileName != null;
//                    File copiedVideoFile = new File(appVideosFolder, fileName);
//                    OutputStream outputStream = new FileOutputStream(copiedVideoFile);
//
//                    byte[] buffer = new byte[1024];
//                    int bytesRead;
//                    while ((bytesRead = inputStream.read(buffer)) != -1) {
//                        outputStream.write(buffer, 0, bytesRead);
//                    }
//                    inputStream.close();
//                    outputStream.close();
//                    currentPage = 1;
//                    showNoItemView(false);
//                    loadLocalVideoFiles();
////                    addVideoToSharedPreferences(copiedVideoFile.getAbsolutePath());
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


// added on 8/22/2023 to remove crash

    private void copyVideoToAppFolder(Uri videoUri) {
        // Check if activity or videoUri is null
        if (activity == null || videoUri == null) {
            Log.e(TAG, "copyVideoToAppFolder: Activity or Uri is null.");
            return;
        }

        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(videoUri);
            if (inputStream != null) {
                File appVideosFolder = new File(activity.getCacheDir(), "videos");
                if (!appVideosFolder.exists()) {
                    if (appVideosFolder.mkdirs()) {
                        Log.d(TAG, "copyVideoToAppFolder: Created videos folder");
                    } else {
                        Log.e(TAG, "copyVideoToAppFolder: Failed to create videos folder");
                    }
                }

                // Using DocumentFile to handle permissions
                DocumentFile documentFile = DocumentFile.fromSingleUri(activity, videoUri);
                if (documentFile != null) {
                    String fileName = documentFile.getName();
                    File copiedVideoFile = new File(appVideosFolder, fileName);

                    try (OutputStream outputStream = new FileOutputStream(copiedVideoFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        Log.d(TAG, "copyVideoToAppFolder: Video copied successfully");

                        // Reset page and load videos
                        currentPage = 1;
                        showNoItemView(false);
                        loadLocalVideoFiles();
                    } catch (IOException e) {
                        Log.e(TAG, "copyVideoToAppFolder: Error copying video: " + e.getMessage());
                    } finally {
                        inputStream.close();
                    }
                } else {
                    Log.e(TAG, "copyVideoToAppFolder: DocumentFile is null");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "copyVideoToAppFolder: IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private List<Wallpaper> getVideoFilesFromCache() {
        File appVideosFolder = new File(activity.getCacheDir(), "videos");
        if (!appVideosFolder.exists()) {
            appVideosFolder.mkdirs();
        }
        File[] files = appVideosFolder.listFiles();
        assert files != null;
        postTotal = files.length;
        List<Wallpaper> localVideos = new ArrayList<>();
        int i = (currentPage - 1) * WALLPAPER_PER_PAGE;
        int end = Math.min(i + WALLPAPER_PER_PAGE, postTotal);
        for (; i < end; i++) {
            Wallpaper wallpaper = new Wallpaper();
            wallpaper.image_url = files[i].getAbsolutePath();
            wallpaper.type = Wallpaper.TYPE_VIDEO;
            localVideos.add(wallpaper);
        }

        return localVideos;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        order = getArguments() != null ? getArguments().getString(ARG_ORDER) : "";
        filter = getArguments() != null ? getArguments().getString(ARG_FILTER) : "";
        wallpaperType = getArguments() != null ? getArguments().getString(FragmentWallpaper2.ARG_TYPE) : Wallpaper.TYPE_IMAGE;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_wallpaper, container, false);

        View addWallpaperButton = rootView.findViewById(R.id.add_wallpaper);
        if (isVideoWallpaperType()) {

            addWallpaperButton.setVisibility(View.VISIBLE);
            addWallpaperButton.setOnClickListener(v -> openVideoPicker());
        } else {
            addWallpaperButton.setVisibility(View.GONE);

        }


        setHasOptionsMenu(true);
        sharedPref = new SharedPref(activity);

        if (sharedPref.getWallpaperColumns() == 3) {
            WALLPAPER_PER_PAGE = Constant.LOAD_MORE_3_COLUMNS;
        } else {
            WALLPAPER_PER_PAGE = Constant.LOAD_MORE_2_COLUMNS;
        }
        dbHelper = new DBHelper(activity);
        adsManager = new AdsManager(activity);

        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);

        initShimmerLayout();

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(sharedPref.getWallpaperColumns(), StaggeredGridLayoutManager.VERTICAL));

        //set data and list adapter
        adapterWallpaper = new AdapterWallpaper(activity, recyclerView);
        recyclerView.setAdapter(adapterWallpaper);

        SingletonEventBus.getInstance().register(this);

        // on item list clicked
        adapterWallpaper.setOnItemClickListener((v, obj, position) -> {
            Constant.wallpapers.clear();
            Wallpaper selectedItem = adapterWallpaper.getCurrentItems().get(position);
//            if(isVideoWallpaperType()) {
            Constant.wallpapers.add(selectedItem);
            Constant.position = 0;

//            } else {
//                Constant.wallpapers.addAll(adapterWallpaper.getCurrentItems());
//                Constant.position = position;
//            }

            if (selectedItem.isPremium() && !MyApplication.getApp().isPremium()) {
//                BuyPremiumActivity.start(activity, null);
                VideoAd.start(activity, null);

            } else {
                Intent intent = new Intent(activity, ActivityWallpaperDetail.class);
                startActivity(intent);
            }

            //Display add after click on item
//            ((MainActivity) activity).showInterstitialAd();
//            ((MainActivity) activity).destroyBannerAd();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                        loadMoreData();
                    }
                }
            }
        });

        requestAction(currentPage);


        return rootView;
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    private void displayApiResult(final List<Wallpaper> wallpapers) {
        insertData(wallpapers);
        swipeProgress(false);
        if (wallpapers != null && wallpapers.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListPostApi(final int page_no) {
        Log.d(TAG, "requestListPostApi: Requesting Wallpapers Page: " + page_no);
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());

//        Log.d(TAG, "requestListPostApi: onItemChanged: filter: "+filter);
        callbackCall = apiInterface.getWallpapers(page_no, PAGE_SIZE, filter, order);

        callbackCall.enqueue(new Callback<CallbackWallpaper>() {
            @Override
            public void onResponse(Call<CallbackWallpaper> call, Response<CallbackWallpaper> response) {
                CallbackWallpaper resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    isLoading = false;
//                    currentPage++;
                    postTotal = resp.count_total;
                    //Log.d("Results : ", ARG_ORDER + " " + post_total);
                    wallpaperList.addAll(resp.posts);
                    displayApiResult(wallpaperList);
                    switch (order) {
                        case Constant.ORDER_RECENT:
                            if (page_no == 1)
                                dbHelper.truncateTableWallpaper(DBHelper.TABLE_RECENT);
                            dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_RECENT);
                            break;
                        case Constant.ORDER_FEATURED:
                            if (page_no == 1)
                                dbHelper.truncateTableWallpaper(DBHelper.TABLE_FEATURED);
                            dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_FEATURED);
                            break;
                        case Constant.ORDER_POPULAR:
                            if (page_no == 1)
                                dbHelper.truncateTableWallpaper(DBHelper.TABLE_POPULAR);
                            dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_POPULAR);
                            break;
                        case Constant.ORDER_RANDOM:
                            if (page_no == 1)
                                dbHelper.truncateTableWallpaper(DBHelper.TABLE_RANDOM);
                            dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_RANDOM);
                            break;
                        case Constant.ORDER_LIVE:
                            if (page_no == 1)
                                dbHelper.truncateTableWallpaper(DBHelper.TABLE_GIF);
                            dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_GIF);
                            break;
                    }
                }
                else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(Call<CallbackWallpaper> call, Throwable t) {
                Log.d("FragmentWallpaper", "failure: " + t.getMessage());
                swipeProgress(false);
                loadDataFromDatabase(call, page_no);
                isLoading = false;
            }
        });
    }

    private void loadDataFromDatabase(Call<CallbackWallpaper> call, final int page_no) {
        switch (order) {
            case Constant.ORDER_RECENT: {
                List<Wallpaper> wallpapers = dbHelper.getAllWallpaper(DBHelper.TABLE_RECENT);
                insertData(wallpapers);
                if (wallpapers.size() == 0) {
                    if (!call.isCanceled()) onFailRequest(page_no);
                }
                break;
            }
            case Constant.ORDER_FEATURED: {
                List<Wallpaper> wallpapers = dbHelper.getAllWallpaper(DBHelper.TABLE_FEATURED);
                insertData(wallpapers);
                if (wallpapers.size() == 0) {
                    if (!call.isCanceled()) onFailRequest(page_no);
                }
                break;
            }
            case Constant.ORDER_POPULAR: {
                List<Wallpaper> wallpapers = dbHelper.getAllWallpaper(DBHelper.TABLE_POPULAR);
                insertData(wallpapers);
                if (wallpapers.size() == 0) {
                    if (!call.isCanceled()) onFailRequest(page_no);
                }
                break;
            }
            case Constant.ORDER_RANDOM: {
                List<Wallpaper> wallpapers = dbHelper.getAllWallpaper(DBHelper.TABLE_RANDOM);
                insertData(wallpapers);
                if (wallpapers.size() == 0) {
                    if (!call.isCanceled()) onFailRequest(page_no);
                }
                break;
            }
            case Constant.ORDER_LIVE: {
                List<Wallpaper> wallpapers = dbHelper.getAllWallpaper(DBHelper.TABLE_GIF);
                insertData(wallpapers);
                if (wallpapers.size() == 0) {
                    if (!call.isCanceled()) onFailRequest(page_no);
                }
                break;
            }
        }
    }

    private void insertData(List<Wallpaper> wallpapers) {
//        if (adsPref.getNativeAdWallpaperList() != 0) {
//            switch (adsPref.getAdType()) {
//                case ADMOB:
//                case GOOGLE_AD_MANAGER:
//                case FAN:
//                case STARTAPP:
//                    adapterWallpaper.insertDataWithNativeAd(currentPage, wallpapers);
//                    break;
//                default:
//                    adapterWallpaper.insertData(currentPage, wallpapers);
//                    break;
//            }
//        } else {
        if (wallpapers != null)
            adapterWallpaper.insertData(currentPage, wallpapers);


//        }

    }

    private void onFailRequest(int page_no) {
        failedPage = page_no;
        adapterWallpaper.setLoaded();
        swipeProgress(false);
        showFailedView(true, getString(R.string.failed_text));
    }


    private void loadMoreData() {
        Log.d(TAG, "loadMoreData: onItemChanged: ");
        if (!isLoading && !isLastPage) {
            requestAction(currentPage);
        }
    }

    public void requestAction(final int page_no) {
        isLoading = true;
        currentPage = page_no;
        showFailedView(false, "");
        showNoItemView(false);
//        if (page_no == 1) {
        swipeProgress(true);
//        } else {

//            adapterWallpaper.setLoading();
//        }
        if (isVideoWallpaperType()) {
            loadLocalVideoFiles();
        } else {
            Log.d(TAG, "requestAction: "+page_no);
            new Handler(Looper.getMainLooper()).postDelayed(() -> requestListPostApi(currentPage), Constant.DELAY_TIME);
        }
    }

    @Subscribe
    public void onEvent(EventWallpaper event) {
        if (event.getAction() == EventWallpaper.ACTION_DELETE_WALLPAPER) {
            currentPage = 1;
            adapterWallpaper.clear();
            requestAction(currentPage);
        }
    }

    @Subscribe
    public void onFilterChanged(String filter) {
        if (filter.equalsIgnoreCase(Wallpaper.Price.ALL.value)) {
            this.filter = Constant.FILTER_ALL;
        } else if (filter.equalsIgnoreCase(Wallpaper.Price.FREE.value)) {
            this.filter = Constant.FILTER_FREE;
        } else if (filter.equalsIgnoreCase(Wallpaper.Price.PREMIUM.value)) {
            this.filter = Constant.FILTER_PREMIUM;
        }

//        pagingIndicator.setVisibility(View.GONE);
        adapterWallpaper.clear();
        wallpaperList.clear();
        Log.d(TAG, "onFilterChanged: call: onItemChanged: ");
//        setLoadMore(1);
        loadMoreData();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
        SingletonEventBus.getInstance().unregister(this);
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = rootView.findViewById(R.id.lyt_failed);
        ((TextView) rootView.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failedPage));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = rootView.findViewById(R.id.lyt_no_item);
        ((TextView) rootView.findViewById(R.id.no_item_title)).setText(isVideoWallpaperType() ? R.string.whops_video : R.string.whops);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(isVideoWallpaperType() ? R.string.msg_no_item_video : R.string.msg_no_item);

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
//            swipeRefreshLayout.setRefreshing(show);
            hideShimmerLayout();
            return;
        } else {
            showShimmerLayout();
        }
    }

    public void initShimmerLayout() {
        View view_shimmer_2_columns = rootView.findViewById(R.id.view_shimmer_2_columns);
        View view_shimmer_3_columns = rootView.findViewById(R.id.view_shimmer_3_columns);
        View view_shimmer_2_columns_square = rootView.findViewById(R.id.view_shimmer_2_columns_square);
        View view_shimmer_3_columns_square = rootView.findViewById(R.id.view_shimmer_3_columns_square);

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

    @Override
    public void onResume() {
        View filterDropDown = activity.findViewById(R.id.filterDropDown);

        if (filterDropDown != null)
            filterDropDown.setVisibility(isVideoWallpaperType() ? View.GONE : View.VISIBLE);


        super.onResume();
    }

}