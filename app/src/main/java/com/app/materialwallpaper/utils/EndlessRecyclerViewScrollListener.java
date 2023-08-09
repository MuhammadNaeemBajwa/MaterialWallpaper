package com.app.materialwallpaper.utils;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    private final StaggeredGridLayoutManager layoutManager;
    private int visibleThreshold = 5;
    private int currentPage = 0;
    private int previousTotalItemCount = 0;
    private boolean loading = true;

    public EndlessRecyclerViewScrollListener(StaggeredGridLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        visibleThreshold *= layoutManager.getSpanCount();
    }

    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        int totalItemCount = layoutManager.getItemCount();
        int[] lastVisibleItems = layoutManager.findLastVisibleItemPositions(null);
        int lastVisibleItem = getLastVisibleItem(lastVisibleItems);

        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = 0;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }

        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        if (!loading && (lastVisibleItem + visibleThreshold) > totalItemCount) {
            currentPage++;
            onLoadMore(currentPage);
            loading = true;
        }
    }

    private int getLastVisibleItem(int[] lastVisibleItems) {
        int max = lastVisibleItems[0];
        for (int i = 1; i < lastVisibleItems.length; i++) {
            if (lastVisibleItems[i] > max) {
                max = lastVisibleItems[i];
            }
        }
        return max;
    }

    public abstract void onLoadMore(int currentPage);
}
