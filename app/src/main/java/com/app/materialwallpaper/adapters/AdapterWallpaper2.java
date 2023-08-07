package com.app.materialwallpaper.adapters;

import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.FAN;
import static com.solodroid.ads.sdk.util.Constant.GOOGLE_AD_MANAGER;
import static com.solodroid.ads.sdk.util.Constant.STARTAPP;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.activities.MyApplication;
import com.app.materialwallpaper.databases.prefs.AdsPref;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.models.LocalVideo;
import com.app.materialwallpaper.models.Wallpaper;
import com.app.materialwallpaper.utils.Constant;
import com.app.materialwallpaper.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.solodroid.ads.sdk.format.NativeAdViewHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterWallpaper2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOCAL_VIDEO = "Local Video";
    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_AD = 2;
    Context context;
    private OnItemClickListener mOnItemClickListener;
    SharedPref sharedPref;
    AdsPref adsPref;
    List<Wallpaper> list;


    public AdapterWallpaper2(Context context, List<Wallpaper> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.mOnItemClickListener = listener;
        this.sharedPref = new SharedPref(context);
        this.adsPref = new AdsPref(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            if (Config.DISPLAY_WALLPAPER == 2) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallpaper_square, parent, false);
                vh = new OriginalViewHolder(v);
            } else if (Config.DISPLAY_WALLPAPER == 3) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallpaper_dynamic, parent, false);
                vh = new OriginalViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallpaper_rectangle, parent, false);
                vh = new OriginalViewHolder(v);
            }
        } else if (viewType == VIEW_AD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_native_ad_medium, parent, false);
            vh = new NativeAdViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Wallpaper p = list.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.wallpaper_name.setText(p.image_name);
            vItem.category_name.setText(p.category_name);
            if(MyApplication.getApp().isPremium()){
                vItem.premium_lock.setImageResource(R.drawable.unlock);
            }
            vItem.premium_lock.setVisibility(p.isPremium() ? View.VISIBLE : View.GONE);

            Log.i("Wallpaper", p.toString());

            if (!Config.ENABLE_DISPLAY_WALLPAPER_NAME) {
                vItem.wallpaper_name.setVisibility(View.GONE);
                vItem.category_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_size_medium));
            }

            if (!Config.ENABLE_DISPLAY_WALLPAPER_CATEGORY) {
                vItem.category_name.setVisibility(View.GONE);
            }

            if (sharedPref.getIsDarkTheme()) {
                vItem.card_view.setCardBackgroundColor(context.getResources().getColor(R.color.color_dark_toolbar));
            } else {
                vItem.card_view.setCardBackgroundColor(context.getResources().getColor(R.color.color_grey_soft));
            }

            if (!Config.ENABLE_DISPLAY_WALLPAPER_NAME && !Config.ENABLE_DISPLAY_WALLPAPER_CATEGORY) {
                vItem.bg_shadow.setBackgroundResource(R.drawable.ic_transparent);
            }
            vItem.wallpaper_image.setVisibility(View.VISIBLE);
            vItem.videoView.setVisibility(View.GONE);
            if(Wallpaper.TYPE_VIDEO.equalsIgnoreCase(p.type)) {
                Glide.with(context)
                        .asBitmap()
                        .load(p.image_url)
                        .into(vItem.wallpaper_image);
                p.image_name = getNameFromUrl(p.image_url);
                p.category_name = LOCAL_VIDEO;
                vItem.wallpaper_name.setText(p.image_name);
                vItem.category_name.setText(p.category_name);

//               vItem.wallpaper_image.setVisibility(View.GONE);
//               vItem.videoView.setVisibility(View.VISIBLE);
//                vItem.videoView.setVideoPath(p.image_url);
//                vItem.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mp) {
//                        float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
//                        float screenRatio =  vItem.videoView.getWidth() / (float)
//                                vItem.videoView.getHeight();
//                        float scaleX = videoRatio / screenRatio;
//                        if (scaleX >= 1f) {
//                            vItem.videoView.setScaleX(scaleX);
//                        } else {
//                            vItem.videoView.setScaleY(1f / scaleX);
//                        }
//
//                        mp.setVolume(0,0);
//                       mp.start();
//                    }
//                });
            }else if (p.type.equals("url")) {
                String imageUrl;
                if (p.mime.contains("octet-stream")) {
                    imageUrl = sharedPref.getBaseUrl() + "/upload/thumbs/" + p.image_thumb;
                } else {
                    imageUrl = p.image_url;
                }
                Glide.with(context)
                        .load(imageUrl.replace(" ", "%20"))
                        .thumbnail(Tools.requestBuilder(context))
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                vItem.progress_bar.setVisibility(View.GONE);
                                if (p.mime.contains("gif") || p.mime.contains("octet-stream")) {
                                    vItem.lyt_live.setVisibility(View.VISIBLE);
                                } else {
                                    vItem.lyt_live.setVisibility(View.GONE);
                                }
                                return false;
                            }
                        })
                        .apply(new RequestOptions().override(Constant.THUMBNAIL_WIDTH, Constant.THUMBNAIL_HEIGHT))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_transparent)
                        .into(vItem.wallpaper_image);
            }
            else {
                String imageUrl;
                if (p.mime.contains("webp") || p.mime.contains("bmp")) {
                    imageUrl = sharedPref.getBaseUrl() + "/upload/" + p.image_upload;
                } else if (p.mime.contains("octet-stream")) {
                    imageUrl = sharedPref.getBaseUrl() + "/upload/thumbs/" + p.image_thumb;
                } else {
                    if (!p.image_thumb.equals("")) {
                        imageUrl = sharedPref.getBaseUrl() + "/upload/thumbs/" + p.image_thumb;
                    } else {
                        imageUrl = sharedPref.getBaseUrl() + "/upload/thumbs/" + p.image_upload;
                    }
                }
                Glide.with(context)
                        .load(imageUrl.replace(" ", "%20"))
                        .thumbnail(Tools.requestBuilder(context))
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                vItem.progress_bar.setVisibility(View.GONE);
                                if (p.mime.contains("gif") || p.mime.contains("octet-stream")) {
                                    vItem.lyt_live.setVisibility(View.VISIBLE);
                                } else {
                                    vItem.lyt_live.setVisibility(View.GONE);
                                }
                                return false;
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_transparent)
                        .into(vItem.wallpaper_image);
            }

            vItem.lyt_parent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });

        }
        else if (holder instanceof NativeAdViewHolder) {

            final NativeAdViewHolder vItem = (NativeAdViewHolder) holder;

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
                    Constant.NATIVE_SIZE_MEDIUM,
                    android.R.color.transparent,
                    android.R.color.transparent
            );

            if (sharedPref.getIsDarkTheme()) {
                vItem.setNativeAdBackgroundResource(R.drawable.bg_native_dark);
            } else {
                vItem.setNativeAdBackgroundResource(R.drawable.bg_native_light);
            }

            vItem.setNativeAdMargin(
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper),
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper),
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper),
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper)
            );

            vItem.setNativeAdPadding(
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper),
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper),
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper),
                    context.getResources().getDimensionPixelOffset(R.dimen.grid_space_wallpaper)
            );

        }
        else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        if (getItemViewType(position) == VIEW_PROG || getItemViewType(position) == VIEW_AD) {
            layoutParams.setFullSpan(true);
        }
        else {
            layoutParams.setFullSpan(false);
        }

    }

    private String getNameFromUrl(String image_url) {
        try {
            String[] separated = image_url.split("/");
            return separated[separated.length - 1].split("\\.")[0];
        } catch (Exception e) {

        }
        return "";

    }

    @Override
    public int getItemCount() {
        return  list.size();
    }

    @Override
    public int getItemViewType(int position) {
        Wallpaper wallpaper = list.get(position);
        if (wallpaper != null) {
            // Real Wallpaper should contain some data such as title, desc, and so on.
            // A Wallpaper having no title etc is assumed to be a fake Wallpaper which represents an Native Ad view
            if (wallpaper.image_name == null && wallpaper.image_url == null) {
                return VIEW_AD;
            }
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }


    public static class OriginalViewHolder extends RecyclerView.ViewHolder {

        private final VideoView videoView;
        public ImageView wallpaper_image;
        public TextView wallpaper_name;
        public TextView category_name;
        public RelativeLayout lyt_live;
        public CardView card_view;
        LinearLayout bg_shadow;
        ProgressBar progress_bar;
        FrameLayout lyt_parent;
        AppCompatImageButton premium_lock;

        public OriginalViewHolder(View v) {
            super(v);
            wallpaper_image = v.findViewById(R.id.wallpaper_image);
            wallpaper_name = v.findViewById(R.id.wallpaper_name);
            category_name = v.findViewById(R.id.category_name);
            lyt_live = v.findViewById(R.id.lyt_live);
            card_view = v.findViewById(R.id.card_view);
            bg_shadow = v.findViewById(R.id.bg_shadow_bottom);
            progress_bar = v.findViewById(R.id.progress_bar);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            premium_lock = v.findViewById(R.id.premium_lock);
            videoView = v.findViewById(R.id.video_view);
        }

    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.load_more);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Wallpaper obj, int position);
    }
}