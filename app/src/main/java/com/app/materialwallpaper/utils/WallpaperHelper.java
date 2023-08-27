package com.app.materialwallpaper.utils;

import static com.app.materialwallpaper.utils.Constant.BOTH;
import static com.app.materialwallpaper.utils.Constant.DELAY_SET;
import static com.app.materialwallpaper.utils.Constant.DOWNLOAD;
import static com.app.materialwallpaper.utils.Constant.HOME_SCREEN;
import static com.app.materialwallpaper.utils.Constant.LOCK_SCREEN;
import static com.app.materialwallpaper.utils.Constant.SET_GIF;
import static com.app.materialwallpaper.utils.Constant.SET_MP4;
import static com.app.materialwallpaper.utils.Constant.SET_WITH;
import static com.app.materialwallpaper.utils.Constant.SHARE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.materialwallpaper.R;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.models.Wallpaper;
import com.app.materialwallpaper.rests.ApiInterface;
import com.app.materialwallpaper.rests.RestAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WallpaperHelper {

    private static final String TAG = "WallpaperHelper";
    Activity activity;
    SharedPref sharedPref;

    public WallpaperHelper(Activity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void setWallpaper(View view, ProgressDialog progressDialog, AdsManager adsManager, Bitmap bitmap, String setAs) {
        switch (setAs) {
            case HOME_SCREEN:
                try {
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                    onWallpaperApplied(progressDialog, adsManager, activity.getString(R.string.msg_success_applied));
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(view, activity.getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                break;

            case LOCK_SCREEN:
                try {
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                    onWallpaperApplied(progressDialog, adsManager, activity.getString(R.string.msg_success_applied));
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(view, activity.getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                break;

            case BOTH:
                try {
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
                    wallpaperManager.setBitmap(bitmap);
                    onWallpaperApplied(progressDialog, adsManager, activity.getString(R.string.msg_success_applied));
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(view, activity.getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                break;
        }
    }

    public void setWallpaper(View view, ProgressDialog progressDialog, AdsManager adsManager, String imageURL) {

        progressDialog.setMessage(activity.getString(R.string.msg_preparing_wallpaper));
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(() -> Glide.with(activity)
                .load(imageURL.replace(" ", "%20"))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                        try {
                            WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
                            wallpaperManager.setBitmap(bitmap);
                            progressDialog.setMessage(activity.getString(R.string.msg_apply_wallpaper));
                            onWallpaperApplied(progressDialog, adsManager, activity.getString(R.string.msg_success_applied));
                        } catch (IOException e) {
                            e.printStackTrace();
                            Snackbar.make(view, activity.getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Snackbar.make(view, activity.getString(R.string.snack_bar_error), Snackbar.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }), DELAY_SET);
    }

    public void onWallpaperApplied(ProgressDialog progressDialog, AdsManager adsManager, String message) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showSuccessDialog(message, adsManager);
            progressDialog.dismiss();
        }, DELAY_SET);
    }

    public void showSuccessDialog(String message, AdsManager adsManager) {

        View lytSuccess = activity.findViewById(R.id.lyt_success);
        TextView msgSuccess = activity.findViewById(R.id.msg_success);
        LinearLayout lytPleaseWait = activity.findViewById(R.id.lyt_please_wait);
        Button btnDone = activity.findViewById(R.id.btn_done);

        if (sharedPref.getIsDarkTheme()) {
            lytSuccess.setBackgroundColor(activity.getResources().getColor(R.color.color_dark_background));
        } else {
            lytSuccess.setBackgroundColor(activity.getResources().getColor(R.color.color_light_background));
        }

        lytSuccess.setVisibility(View.VISIBLE);
        lytSuccess.setOnClickListener(view -> {

        });

        msgSuccess.setText(message);

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);
                if (second == 1) {
                    adsManager.showInterstitialAd();
                }
                if (second == 3) {
                    lytPleaseWait.setVisibility(View.VISIBLE);
                }
            }

            public void onFinish() {
                lytPleaseWait.setVisibility(View.GONE);
                btnDone.setVisibility(View.VISIBLE);
            }

        }.start();

        btnDone.setOnClickListener(view -> {
            lytSuccess.animate()
                    .translationY(lytSuccess.getHeight())
                    //.alpha(0.0f)
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            lytSuccess.setVisibility(View.GONE);
                        }
                    });

            //activity.finish();
            //adsManager.destroyBannerAd();
        });

    }

    public void setGif(View view, ProgressDialog progressDialog, String imageURL) {

        progressDialog.setMessage(activity.getString(R.string.msg_preparing_wallpaper));
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(() -> Glide.with(activity)
                .download(imageURL.replace(" ", "%20"))
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        progressDialog.dismiss();
                        Snackbar.make(view, activity.getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            Tools.setAction(activity, Tools.getBytesFromFile(resource), Tools.createName(imageURL), SET_GIF);
                            progressDialog.dismiss();
                        } catch (IOException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Snackbar.make(view, activity.getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                })
                .submit(), DELAY_SET);
    }

    public void setMp4(View view, ProgressDialog progressDialog, String imageURL) {

        progressDialog.setMessage(activity.getString(R.string.msg_preparing_wallpaper));
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(() -> Glide.with(activity)
                .download(imageURL.replace(" ", "%20"))
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        progressDialog.dismiss();
                        Snackbar.make(view, activity.getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            Tools.setAction(activity, Tools.getBytesFromFile(resource), Tools.createName(imageURL), SET_MP4);
                            progressDialog.dismiss();
                        } catch (IOException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Snackbar.make(view, activity.getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                })
                .submit(), DELAY_SET);
    }

//    public void loadFile(View view, ProgressDialog progressDialog, String fileUrl) {
//
//        progressDialog.setMessage(activity.getString(R.string.msg_preparing_wallpaper));
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//
//        ApiInterface apiInterface = RestAdapter.createDownloadApi();
//        Call<ResponseBody> call = apiInterface.downloadFileWithDynamicUrl(fileUrl);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "Got the body for the file");
//                    Log.d(TAG, "Url: " + fileUrl);
//                    if (response.body() != null) {
//                        try {
//                            InputStream inputStream = new BufferedInputStream(response.body().byteStream());
//                            Tools.setAction(activity, Tools.getBytesFromInputStream(inputStream), Tools.createName(fileUrl), SET_MP4);
//                            progressDialog.dismiss();
//                            Log.d(TAG, "Berhasil");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            progressDialog.dismiss();
//                            Log.d(TAG, "gagal " + e.getMessage());
//                        }
//                    } else {
//                        progressDialog.dismiss();
//                    }
//                } else {
//                    Log.d(TAG, "Connection failed " + response.errorBody());
//                    progressDialog.dismiss();
//                    Snackbar.make(view, activity.getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                t.printStackTrace();
//                Log.e(TAG, t.getMessage());
//                progressDialog.dismiss();
//                Snackbar.make(view, activity.getString(R.string.snack_bar_failed), Snackbar.LENGTH_SHORT).show();
//            }
//        });
//    }

    public void setWallpaperFromOtherApp(String imageURL) {

        new Handler().postDelayed(() -> Glide.with(activity)
                .download(imageURL.replace(" ", "%20"))
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            Tools.setAction(activity, Tools.getBytesFromFile(resource), Tools.createName(imageURL), SET_WITH);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                })
                .submit(), DELAY_SET);

    }

    public void downloadWallpaper(Wallpaper wallpaper, ProgressDialog progressDialog, AdsManager adsManager, String imageURL) {
        Log.d(TAG, "downloadWallpaper: ");


        progressDialog.setMessage(activity.getString(R.string.snack_bar_saving));
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(() -> Glide.with(activity)
                .download(imageURL.replace(" ", "%20"))
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        progressDialog.dismiss();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            Tools.setAction(activity, Tools.getBytesFromFile(resource), Tools.createName(imageURL), DOWNLOAD);
                            updateDownload(wallpaper.image_id);
                            onWallpaperApplied(progressDialog, adsManager, activity.getString(R.string.msg_success_saved));

                        } catch (IOException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }
                        return true;
                    }
                })
                .submit(), DELAY_SET);

    }

    public void shareWallpaper(ProgressDialog progressDialog, String imageURL) {

        progressDialog.setMessage(activity.getString(R.string.msg_preparing_wallpaper));
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(() -> Glide.with(activity)
                .download(imageURL.replace(" ", "%20"))
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        progressDialog.dismiss();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            Tools.setAction(activity, Tools.getBytesFromFile(resource), Tools.createName(imageURL), SHARE);
                            progressDialog.dismiss();
                        } catch (IOException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }
                        return true;
                    }
                })
                .submit(), DELAY_SET);

    }

    public void updateView(String image_id) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        Call<Wallpaper> call = apiInterface.updateView(image_id);
        call.enqueue(new Callback<Wallpaper>() {
            @Override
            public void onResponse(Call<Wallpaper> call, Response<Wallpaper> response) {
                Log.d(TAG, "success update view");
            }

            @Override
            public void onFailure(Call<Wallpaper> call, Throwable t) {
                Log.d(TAG, "failed update view");
            }
        });
    }

    public void updateDownload(String image_id) {
        Log.d(TAG, "updateDownload: called with image_id: " + image_id);
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        Call<Wallpaper> call = apiInterface.updateDownload(image_id);
        call.enqueue(new Callback<Wallpaper>() {
            @Override
            public void onResponse(Call<Wallpaper> call, Response<Wallpaper> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "updateDownload API success");
                } else {
                    Log.d(TAG, "updateDownload API failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Wallpaper> call, Throwable t) {
                Log.e(TAG, "updateDownload API failure: " + t.getMessage());
            }
        });
    }

    public void downloadWallpaperManager(View view, String filename, String image_url, String extension, String mime) {
        try {
            DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(image_url);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType(mime) // Your file type. You can use this code to download other file types also.
                    //.setMimeType("image/jpeg") // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename + "." + extension);
            dm.enqueue(request);
            //Toast.makeText(activity, "Image download started.", Toast.LENGTH_SHORT).show();
            Snackbar.make(view, activity.getString(R.string.start_download), Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            //Toast.makeText(activity, "Image download failed.", Toast.LENGTH_SHORT).show();
            Snackbar.make(view, activity.getString(R.string.failed_download), Snackbar.LENGTH_SHORT).show();
        }
    }

    public void deleteVideoWallpaper(Wallpaper wallpaper) {

        File videoFile = new File(wallpaper.image_url);
        if (videoFile.exists()) {
            videoFile.delete();
        }
        SingletonEventBus.getInstance().post(new EventWallpaper(wallpaper, EventWallpaper.ACTION_DELETE_WALLPAPER));
    }
}