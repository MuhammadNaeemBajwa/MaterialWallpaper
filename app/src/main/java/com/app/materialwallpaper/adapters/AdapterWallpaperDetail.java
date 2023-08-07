package com.app.materialwallpaper.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.activities.ActivityVideoPlayer;
import com.app.materialwallpaper.activities.ActivityWallpaperDetail;
import com.app.materialwallpaper.databases.prefs.AdsPref;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.models.Wallpaper;
import com.app.materialwallpaper.utils.AdsManager;
import com.app.materialwallpaper.utils.Constant;
import com.app.materialwallpaper.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.solodroid.ads.sdk.format.NativeAdViewHolder;

import java.util.List;

public class AdapterWallpaperDetail extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_AD = 2;
    private List<Wallpaper> items;
    private Context context;
    private OnItemClickListener mOnItemClickListener;
    private boolean loading;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;

    public interface OnItemClickListener {
        void onItemClick(View view, Wallpaper obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterWallpaperDetail(Context context, List<Wallpaper> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
        this.adsPref = new AdsPref(context);
        this.adsManager = new AdsManager((Activity) context);
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        private final VideoView videoView;
        public RelativeLayout lytView;
        public PhotoView wallpaperImage;
        public ImageView videoThumbnail;
        public ProgressBar progressBar;

        public OriginalViewHolder(View v) {
            super(v);
            lytView = v.findViewById(R.id.lyt_view);
            wallpaperImage = v.findViewById(R.id.image_view);
            videoThumbnail = v.findViewById(R.id.video_thumbnail);
            progressBar = v.findViewById(R.id.progress_bar);
            videoView = v.findViewById(R.id.video_view);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_AD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_ad_details, parent, false);
            vh = new NativeAdViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slider_wallpaper, parent, false);
            vh = new OriginalViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Wallpaper wallpaper = items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            if (Config.ENABLE_CENTER_CROP_IN_DETAIL_WALLPAPER) {
                vItem.wallpaperImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

            if(wallpaper.isVideoWallpaper()) {
            }else if (wallpaper.image_url.endsWith(".png") || (wallpaper.image_upload !=null && wallpaper.image_upload.endsWith(".png"))) {
                if (sharedPref.getIsDarkTheme()) {
                    vItem.lytView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_dark_toolbar));
                } else {
                    vItem.lytView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_background_image));
                }
            }

            vItem.wallpaperImage.setOnClickListener(v -> ((ActivityWallpaperDetail) context).showFullScreen());

            if(wallpaper.isVideoWallpaper()) {
                vItem.videoView.setVisibility(View.VISIBLE);
                vItem.videoThumbnail.setVisibility(View.GONE);
                vItem.wallpaperImage.setVisibility(View.GONE);
                vItem.progressBar.setVisibility(View.GONE);
                vItem.videoView.setVideoPath(wallpaper.image_url);
                vItem.videoView.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    mp.setVolume(0, 0);
                    mp.start();
                });
                vItem.videoView.start();
            }else if (wallpaper.type.equals("url")) {
                vItem.videoView.setVisibility(View.GONE);
                String imageUrl;
                if (wallpaper.mime.contains("octet-stream")) {
                    imageUrl = sharedPref.getBaseUrl() + "/upload/thumbs/" + wallpaper.image_thumb;
                } else {
                    imageUrl = wallpaper.image_url;
                }
                Glide.with(context)
                        .load(imageUrl.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_transparent)
                        .thumbnail(Tools.requestBuilder(context))
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                vItem.progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                vItem.progressBar.setVisibility(View.GONE);
                                if (wallpaper.mime.contains("octet-stream")) {
                                    vItem.videoThumbnail.setVisibility(View.VISIBLE);
                                    vItem.videoThumbnail.setOnClickListener(view -> {
                                        Intent intent = new Intent(context, ActivityVideoPlayer.class);
                                        intent.putExtra("video_url", wallpaper.image_url);
                                        context.startActivity(intent);
                                    });
                                } else {
                                    vItem.videoThumbnail.setVisibility(View.GONE);
                                }
                                return false;
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(vItem.wallpaperImage);
            } else {
                String imageUrl;
                if (wallpaper.mime.contains("octet-stream")) {
                    imageUrl = sharedPref.getBaseUrl() + "/upload/thumbs/" + wallpaper.image_thumb;
                } else {
                    imageUrl = sharedPref.getBaseUrl() + "/upload/" + wallpaper.image_upload;
                }
                Glide.with(context)
                        .load(imageUrl.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_transparent)
                        .thumbnail(Tools.requestBuilder(context))
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                vItem.progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                vItem.progressBar.setVisibility(View.GONE);
                                if (wallpaper.mime.contains("octet-stream")) {
                                    vItem.videoThumbnail.setVisibility(View.VISIBLE);
                                    vItem.videoThumbnail.setOnClickListener(view -> {
                                        Intent intent = new Intent(context, ActivityVideoPlayer.class);
                                        intent.putExtra("video_url", sharedPref.getBaseUrl() + "/upload/" + wallpaper.image_upload);
                                        context.startActivity(intent);
                                    });
                                } else {
                                    vItem.videoThumbnail.setVisibility(View.GONE);
                                }
                                return false;
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(vItem.wallpaperImage);
            }


        } else if (holder instanceof NativeAdViewHolder) {
            final NativeAdViewHolder vItem = (NativeAdViewHolder) holder;
            final AdsPref adsPref = new AdsPref(context);
            final SharedPref sharedPref = new SharedPref(context);

            vItem.loadNativeAd(context,
                    adsPref.getAdStatus(),
                    adsPref.getNativeAdWallpaperList(),
                    adsPref.getAdType(),
                    adsPref.getBackupAds(),
                    adsPref.getAdMobNativeId(),
                    adsPref.getAdManagerNativeId(),
                    adsPref.getFanNativeUnitId(),
                    adsPref.getAppLovinNativeAdManualUnitId(),
                    sharedPref.getIsDarkTheme(),
                    Config.LEGACY_GDPR,
                    Constant.NATIVE_SIZE_LARGE,
                    android.R.color.transparent,
                    android.R.color.transparent
            );

            if (sharedPref.getIsDarkTheme()) {
                vItem.setNativeAdBackgroundResource(R.drawable.bg_native_dark);
            } else {
                vItem.setNativeAdBackgroundResource(R.drawable.bg_native_light);
            }

            vItem.setNativeAdMargin(
                    context.getResources().getDimensionPixelOffset(R.dimen.spacing_middle),
                    context.getResources().getDimensionPixelOffset(R.dimen.spacing_middle),
                    context.getResources().getDimensionPixelOffset(R.dimen.spacing_middle),
                    context.getResources().getDimensionPixelOffset(R.dimen.spacing_middle)
            );

            vItem.setNativeAdPadding(
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper),
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper),
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper),
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper)
            );

        }
    }

    public void insertData(List<Wallpaper> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Wallpaper post = items.get(position);
        if (post != null) {
            if (post.image_name == null) {
                return VIEW_AD;
            }
        }
        return VIEW_ITEM;
    }

//    private MediaSource buildMediaSource(Uri uri) {
//        MediaItem mMediaItem = MediaItem.fromUri(Uri.parse(String.valueOf(uri)));
//        return new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(mMediaItem);
//    }

}