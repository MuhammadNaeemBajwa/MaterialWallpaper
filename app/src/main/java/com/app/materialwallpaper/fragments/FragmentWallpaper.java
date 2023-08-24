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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.adapters.AdapterWallpaper2;
import com.app.materialwallpaper.callbacks.CallbackWallpaper;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.databases.sqlite.DBHelper;
import com.app.materialwallpaper.models.Wallpaper;
import com.app.materialwallpaper.rests.ApiInterface;
import com.app.materialwallpaper.rests.RestAdapter;
import com.app.materialwallpaper.utils.AdsManager;
import com.app.materialwallpaper.utils.Constant;
import com.app.materialwallpaper.utils.SingletonEventBus;
import com.facebook.shimmer.ShimmerFrameLayout;

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

public class FragmentWallpaper extends Fragment {
    private static final int SELECT_VIDEO_REQUEST_CODE = 101;
    private static final String TAG = "FragmentWallpaper";

    public static final String ARG_ORDER = "order";

    public static final String ARG_TYPE = "type";
    public static final String ARG_FILTER = "filter";
    public static int WALLPAPER_PER_PAGE = Constant.LOAD_MORE_2_COLUMNS;
    View rootView;
    private RecyclerView recyclerView;
    private AdapterWallpaper2 adapterWallpaper;
    private ShimmerFrameLayout lytShimmer;
    private Call<CallbackWallpaper> callbackCall = null;
    private int postTotal = 0;

    private SharedPref sharedPref;
    String order, filter;
    DBHelper dbHelper;
    AdsManager adsManager;
    Activity activity;
    private String wallpaperType = Wallpaper.TYPE_IMAGE;
    private int currentPage = 1;
    private final int PAGE_SIZE = 30;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    private void loadLocalVideoFiles() {
        List<Wallpaper> localVideos = getVideoFilesFromCache();
        // if (localVideos != null) {
        dataList.addAll(localVideos);
        adapterWallpaper.notifyDataSetChanged();
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

    private void copyVideoToAppFolder(Uri videoUri) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(videoUri);
            if (inputStream != null) {
                File appVideosFolder = new File(activity.getCacheDir(), "videos");
                if (!appVideosFolder.exists()) {
                    appVideosFolder.mkdirs();
                }
                DocumentFile documentFile = DocumentFile.fromSingleUri(activity, videoUri);
                if (documentFile != null) {
                    String fileName = documentFile.getName();
                    File copiedVideoFile = new File(appVideosFolder, fileName);
                    OutputStream outputStream = new FileOutputStream(copiedVideoFile);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();

                    currentPage = 1;
                    showNoItemView(false);
                    loadLocalVideoFiles();
//                    addVideoToSharedPreferences(copiedVideoFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private List<Wallpaper> getVideoFilesFromCache() {
        File appVideosFolder = new File(activity.getCacheDir(), "videos");
        if (!appVideosFolder.exists()) {
            appVideosFolder.mkdirs();
        }
        File[] files = appVideosFolder.listFiles();
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
        wallpaperType = getArguments() != null ? getArguments().getString(FragmentWallpaper.ARG_TYPE) : Wallpaper.TYPE_IMAGE;
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
        adapterWallpaper = new AdapterWallpaper2(activity, dataList, (view, obj, position) -> Log.d(TAG, "onItemClick: " + obj.image_name));
        recyclerView.setAdapter(adapterWallpaper);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        // Load more data when the user scrolls to the end of the list
                        loadMoreData();
                    }
                }
            }
        });
        loadData();

        SingletonEventBus.getInstance().register(this);

        // on item list clicked

        return rootView;
    }

    private void loadMoreData() {
        if (!isLoading && !isLastPage) {
            loadData();
        }
    }

    private void loadData() {
        isLoading = true;
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        if (sharedPref.getWallpaperColumns() == 3) {
            Log.d(TAG, "requestListPostApi: shared: " + (sharedPref.getWallpaperColumns() == 3));
            callbackCall = apiInterface.getWallpapers(currentPage, Constant.LOAD_MORE_3_COLUMNS, filter, order);
        } else {
            callbackCall = apiInterface.getWallpapers(currentPage, Constant.LOAD_MORE_2_COLUMNS, filter, order);
        }
        callbackCall.enqueue(new Callback<CallbackWallpaper>() {
            @Override
            public void onResponse(Call<CallbackWallpaper> call, Response<CallbackWallpaper> response) {
                Log.d(TAG, "onResponse: response: " + response);
                CallbackWallpaper resp = response.body();
                if (response.isSuccessful()) {
                    if (resp != null && resp.status.equals("ok") && resp.posts != null) {
                        Log.d(TAG, "onResponse: posts: "+resp.posts);
                        dataList.addAll(resp.posts);
                        adapterWallpaper.notifyDataSetChanged();
                        currentPage++;
                        isLoading = false;
                        switch (order) {
                            case Constant.ORDER_RECENT:
                                if (currentPage == 1)
                                    dbHelper.truncateTableWallpaper(DBHelper.TABLE_RECENT);
                                dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_RECENT);
                                break;
                            case Constant.ORDER_FEATURED:
                                if (currentPage == 1)
                                    dbHelper.truncateTableWallpaper(DBHelper.TABLE_FEATURED);
                                dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_FEATURED);
                                break;
                            case Constant.ORDER_POPULAR:
                                if (currentPage == 1)
                                    dbHelper.truncateTableWallpaper(DBHelper.TABLE_POPULAR);
                                dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_POPULAR);
                                break;
                            case Constant.ORDER_RANDOM:
                                if (currentPage == 1)
                                    dbHelper.truncateTableWallpaper(DBHelper.TABLE_RANDOM);
                                dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_RANDOM);
                                break;
                            case Constant.ORDER_LIVE:
                                if (currentPage == 1)
                                    dbHelper.truncateTableWallpaper(DBHelper.TABLE_GIF);
                                dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_GIF);
                                break;
                        }
                    } else {
                        onFailRequest();
                    }
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackWallpaper> call, Throwable t) {
                Log.d(TAG, "onFailure: message: " + t.getMessage());
                swipeProgress(false);
                isLoading = false;
            }
        });
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    private final List<Wallpaper> dataList = new ArrayList<>();

    private void requestListPostApi(final int currentPage) {
        Log.d(TAG, "requestListPostApi: currentPage: " + currentPage);
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());


        if (sharedPref.getWallpaperColumns() == 3) {
            Log.d(TAG, "requestListPostApi: shared: " + (sharedPref.getWallpaperColumns() == 3));
            callbackCall = apiInterface.getWallpapers(currentPage, Constant.LOAD_MORE_3_COLUMNS, filter, order);
        } else {
            callbackCall = apiInterface.getWallpapers(currentPage, Constant.LOAD_MORE_2_COLUMNS, filter, order);
        }

        callbackCall.enqueue(new Callback<CallbackWallpaper>() {
            @Override
            public void onResponse(Call<CallbackWallpaper> call, Response<CallbackWallpaper> response) {
                Log.d(TAG, "onResponse: response: " + response);
                CallbackWallpaper resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    Log.d(TAG, "onResponse: responseCheck: " + (resp != null && resp.status.equals("ok")));
                    postTotal = resp.count_total;
                    //Log.d("Results : ", ARG_ORDER + " " + post_total);
                    dataList.addAll(resp.posts);
                    adapterWallpaper.notifyDataSetChanged();
                    switch (order) {
                        case Constant.ORDER_RECENT:
                            if (currentPage == 1)
                                dbHelper.truncateTableWallpaper(DBHelper.TABLE_RECENT);
                            dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_RECENT);
                            break;
                        case Constant.ORDER_FEATURED:
                            if (currentPage == 1)
                                dbHelper.truncateTableWallpaper(DBHelper.TABLE_FEATURED);
                            dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_FEATURED);
                            break;
                        case Constant.ORDER_POPULAR:
                            if (currentPage == 1)
                                dbHelper.truncateTableWallpaper(DBHelper.TABLE_POPULAR);
                            dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_POPULAR);
                            break;
                        case Constant.ORDER_RANDOM:
                            if (currentPage == 1)
                                dbHelper.truncateTableWallpaper(DBHelper.TABLE_RANDOM);
                            dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_RANDOM);
                            break;
                        case Constant.ORDER_LIVE:
                            if (currentPage == 1)
                                dbHelper.truncateTableWallpaper(DBHelper.TABLE_GIF);
                            dbHelper.addListWallpaper(resp.posts, DBHelper.TABLE_GIF);
                            break;
                    }
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackWallpaper> call, Throwable t) {
                Log.d(TAG, "onFailure: message: " + t.getMessage());
                swipeProgress(false);
                loadDataFromDatabase(call);
            }
        });
    }

    private void loadDataFromDatabase(Call<CallbackWallpaper> call) {
        Log.d(TAG, "loadDataFromDatabase: ");

// added on 8/1/2023 by Hasnain`
        if (!isAdded()) {
            Log.d(TAG, "loadDataFromDatabase: isAdded: " + (!isAdded()));
            // Fragment is not attached to the activity, return and handle later
            return;
        }
        // till here

        switch (order) {
            case Constant.ORDER_RECENT: {
                List<Wallpaper> wallpapers = dbHelper.getAllWallpaper(DBHelper.TABLE_RECENT);
                dataList.addAll(wallpapers);
                adapterWallpaper.notifyDataSetChanged();
                if (wallpapers.size() == 0) {
                    if (!call.isCanceled()) onFailRequest();
                }
                break;
            }
            case Constant.ORDER_FEATURED: {
                List<Wallpaper> wallpapers = dbHelper.getAllWallpaper(DBHelper.TABLE_FEATURED);
                dataList.addAll(wallpapers);
                adapterWallpaper.notifyDataSetChanged();
                if (wallpapers.size() == 0) {
                    if (!call.isCanceled()) onFailRequest();
                }
                break;
            }
            case Constant.ORDER_POPULAR: {
                List<Wallpaper> wallpapers = dbHelper.getAllWallpaper(DBHelper.TABLE_POPULAR);
                dataList.addAll(wallpapers);
                adapterWallpaper.notifyDataSetChanged();
                if (wallpapers.size() == 0) {
                    if (!call.isCanceled()) onFailRequest();
                }
                break;
            }
            case Constant.ORDER_RANDOM: {
                List<Wallpaper> wallpapers = dbHelper.getAllWallpaper(DBHelper.TABLE_RANDOM);
                dataList.addAll(wallpapers);
                adapterWallpaper.notifyDataSetChanged();
                if (wallpapers.size() == 0) {
                    if (!call.isCanceled()) onFailRequest();
                }
                break;
            }
            case Constant.ORDER_LIVE: {
                List<Wallpaper> wallpapers = dbHelper.getAllWallpaper(DBHelper.TABLE_GIF);
                dataList.addAll(wallpapers);
                adapterWallpaper.notifyDataSetChanged();
                if (wallpapers.size() == 0) {
                    if (!call.isCanceled()) onFailRequest();
                }
                break;
            }
        }
    }

    private void onFailRequest() {
        swipeProgress(false);
        showFailedView(true, getString(R.string.failed_text));
    }

    public void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        swipeProgress(true);
//        } else {

//            adapterWallpaper.setLoading();
//        }
        if (isVideoWallpaperType()) {
            loadLocalVideoFiles();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> requestListPostApi(currentPage), Constant.DELAY_TIME);
        }
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
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
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