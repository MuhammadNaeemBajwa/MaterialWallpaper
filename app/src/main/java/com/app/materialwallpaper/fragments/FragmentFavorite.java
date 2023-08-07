package com.app.materialwallpaper.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.materialwallpaper.R;
import com.app.materialwallpaper.activities.ActivityWallpaperDetail;
import com.app.materialwallpaper.activities.MainActivity;
import com.app.materialwallpaper.adapters.AdapterWallpaper;
import com.app.materialwallpaper.databases.prefs.AdsPref;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.databases.sqlite.DBHelper;
import com.app.materialwallpaper.models.Wallpaper;
import com.app.materialwallpaper.utils.AdsManager;
import com.app.materialwallpaper.utils.Constant;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {

    View rootView;
    RelativeLayout parentView;
    RecyclerView recyclerView;
    private AdapterWallpaper adapterWallpaper;
    SharedPref sharedPref;
    DBHelper dbHelper;
    View lytNoFavorite;
    AdsManager adsManager;
    AdsPref adsPref;
    Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        lytNoFavorite = rootView.findViewById(R.id.lyt_not_found);
        dbHelper = new DBHelper(activity);
        sharedPref = new SharedPref(activity);
        adsManager = new AdsManager(activity);
        adsPref = new AdsPref(activity);

        parentView = rootView.findViewById(R.id.parent_view);
        if (sharedPref.getIsDarkTheme()) {
            parentView.setBackgroundColor(getResources().getColor(R.color.color_dark_background));
        } else {
            parentView.setBackgroundColor(getResources().getColor(R.color.color_light_background));
        }

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(sharedPref.getWallpaperColumns(), StaggeredGridLayoutManager.VERTICAL));

        adapterWallpaper = new AdapterWallpaper(activity, recyclerView);
        adapterWallpaper = new AdapterWallpaper(activity, recyclerView);
        recyclerView.setAdapter(adapterWallpaper);

        displayData();

        return rootView;
    }

    private void displayData() {
        List<Wallpaper> wallpapers = dbHelper.getAllFavorite(DBHelper.TABLE_FAVORITE);
        adapterWallpaper.setItems(1,wallpapers);
        adapterWallpaper.setCurPage(1);
        if (wallpapers.size() == 0) {
            lytNoFavorite.setVisibility(View.VISIBLE);
        } else {
            lytNoFavorite.setVisibility(View.GONE);
        }

        adapterWallpaper.setOnItemClickListener((v, obj, position) -> {
            Constant.wallpapers.clear();
            Constant.wallpapers.addAll(adapterWallpaper.getCurrentItems());
            Constant.position = position;
            Intent intent = new Intent(activity, ActivityWallpaperDetail.class);
            startActivity(intent);

            ((MainActivity) activity).showInterstitialAd();
            ((MainActivity) activity).destroyBannerAd();
        });
        adapterWallpaper.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayData();
    }

}
