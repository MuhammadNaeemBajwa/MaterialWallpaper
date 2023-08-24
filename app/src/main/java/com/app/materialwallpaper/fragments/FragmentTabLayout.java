package com.app.materialwallpaper.fragments;

import static com.app.materialwallpaper.activities.MainActivity.TAG;
import static com.app.materialwallpaper.utils.Constant.FILTER_ALL;
import static com.app.materialwallpaper.utils.Constant.FILTER_DEFAULT;
import static com.app.materialwallpaper.utils.Constant.FILTER_LIVE;
import static com.app.materialwallpaper.utils.Constant.FILTER_WALLPAPER;
import static com.app.materialwallpaper.utils.Constant.ORDER_DEFAULT;
import static com.app.materialwallpaper.utils.Constant.ORDER_FEATURED;
import static com.app.materialwallpaper.utils.Constant.ORDER_LIVE;
import static com.app.materialwallpaper.utils.Constant.ORDER_POPULAR;
import static com.app.materialwallpaper.utils.Constant.ORDER_RANDOM;
import static com.app.materialwallpaper.utils.Constant.ORDER_RECENT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.VideoAd.VideoAd;
import com.app.materialwallpaper.activities.BuyPremiumActivity;
import com.app.materialwallpaper.components.CustomTabLayout;
import com.app.materialwallpaper.components.RtlViewPager;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.models.Menu;
import com.app.materialwallpaper.models.Wallpaper;
import com.app.materialwallpaper.utils.SingletonEventBus;
import com.app.materialwallpaper.view.CustomFilterDropDown;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.List;

public class FragmentTabLayout extends Fragment {

    public RelativeLayout tabBackground;
    public CustomTabLayout smartTabLayout;
    public ViewPager viewPager;
    public RtlViewPager viewPagerRTL;
    AppBarLayout tabAppbarLayout;
    SharedPref sharedPref;
    CoordinatorLayout parentView;
    View view;
    Activity activity;
    String order;
    String filter;
    List<Menu> menus = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Config.ENABLE_RTL_MODE) {
            view = inflater.inflate(R.layout.fragment_tab_layout_rtl, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_tab_layout, container, false);
        }

        sharedPref = new SharedPref(activity);
        menus = sharedPref.getMenuList();

        CustomFilterDropDown filterDropDown = view.findViewById(R.id.filterDropDown);
        filterDropDown.setOnItemChangedListener(new CustomFilterDropDown.OnItemChangedListener() {
            @Override
            public void onItemChanged(String text) {
                SingletonEventBus.getInstance().post(text);
                Log.d(TAG, "onItemChanged: text: " +text);

            }

            @Override
            public void onBuySelected() {
//                BuyPremiumActivity.start(getContext(), null);
                VideoAd.start(getContext(), null);

            }
        });


        tabAppbarLayout = view.findViewById(R.id.tab_appbar_layout);
        tabBackground = view.findViewById(R.id.tab_background);
        smartTabLayout = view.findViewById(R.id.tab_layout);
        parentView = view.findViewById(R.id.tab_coordinator_layout);

        if (menus != null && menus.size() > 1) {
            tabAppbarLayout.setVisibility(View.VISIBLE);
        } else {
            tabAppbarLayout.setVisibility(View.GONE);
        }

        if (sharedPref.getIsDarkTheme()) {
            parentView.setBackgroundColor(getResources().getColor(R.color.color_dark_background));
            tabBackground.setBackgroundColor(getResources().getColor(R.color.color_dark_toolbar));
            smartTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.color_dark_accent));
        } else {
            parentView.setBackgroundColor(getResources().getColor(R.color.color_light_background));
            tabBackground.setBackgroundColor(getResources().getColor(R.color.color_light_primary));
        }
        setupViewPager(menus);
        return view;

    }

    private void setupViewPager(List<Menu> menus) {
        viewPager = view.findViewById(R.id.tab_view_pager);
        viewPagerRTL = view.findViewById(R.id.view_pager_rtl);
        MenuViewPagerAdapter adapter = new MenuViewPagerAdapter(getChildFragmentManager());
        if (menus != null && menus.size() > 0) {
            for (int i = 0; i < menus.size(); i++) {
                FragmentWallpaper2 fragment = new FragmentWallpaper2();
                Bundle args = new Bundle();
                wallpaperOrderAndFilter(menus, i);
                args.putString(FragmentWallpaper2.ARG_ORDER, order);
                args.putString(FragmentWallpaper2.ARG_FILTER, filter);
                fragment.setArguments(args);
                adapter.addFrag(fragment, menus.get(i).menu_title);
            }
        } else {
            FragmentWallpaper2 fragment = new FragmentWallpaper2();
            Bundle args = new Bundle();
            args.putString(FragmentWallpaper2.ARG_ORDER, ORDER_RECENT);
            args.putString(FragmentWallpaper2.ARG_FILTER, FILTER_ALL);
            fragment.setArguments(args);
            adapter.addFrag(fragment, "");
        }

        FragmentWallpaper2 fragment = new FragmentWallpaper2();
        Bundle args = new Bundle();
        args.putString(FragmentWallpaper2.ARG_ORDER, ORDER_RECENT);
        args.putString(FragmentWallpaper2.ARG_TYPE, Wallpaper.TYPE_VIDEO);
        args.putString(FragmentWallpaper2.ARG_FILTER, FILTER_ALL);
        fragment.setArguments(args);
        adapter.addFrag(fragment, getString(R.string.video_wallpaper));

        if (Config.ENABLE_RTL_MODE) {
            viewPagerRTL.setAdapter(adapter);
            viewPagerRTL.setOffscreenPageLimit(menus.size());
            smartTabLayout.post(() -> smartTabLayout.setViewPager(viewPagerRTL));
        } else {
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(menus == null ? 0 : menus.size());
            smartTabLayout.post(() -> smartTabLayout.setViewPager(viewPager));
        }
    }

    private void wallpaperOrderAndFilter(List<Menu> menus, int position) {
        order = menus.get(position).menu_order;
        filter = menus.get(position).menu_filter;

        switch (order) {
            case "recent":
                order = ORDER_RECENT;
                break;
            case "featured":
                order = ORDER_FEATURED;
                break;
            case "popular":
                order = ORDER_POPULAR;
                break;
            case "random":
                order = ORDER_RANDOM;
                break;
            case "live":
                order = ORDER_LIVE;
                break;
            default:
                order = ORDER_DEFAULT;
                break;
        }

        switch (filter) {
            case "wallpaper":
                filter = FILTER_WALLPAPER;
                break;
            case "live":
                filter = FILTER_LIVE;
                break;
            case "both":
                filter = FILTER_ALL;
                break;
            default:
                filter = FILTER_DEFAULT;
                break;
        }

    }

    @SuppressWarnings("deprecation")
    public static class MenuViewPagerAdapter extends FragmentStatePagerAdapter {

        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> strings = new ArrayList<>();

        public MenuViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFrag(Fragment fragment, String title) {
            fragments.add(fragment);
            strings.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return strings.get(position);
        }
    }

}

