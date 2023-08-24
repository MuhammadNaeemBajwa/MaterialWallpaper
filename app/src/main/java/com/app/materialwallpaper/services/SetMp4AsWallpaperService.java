package com.app.materialwallpaper.services;

import android.media.MediaPlayer;
import android.net.Uri;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import com.app.materialwallpaper.databases.prefs.SharedPref;

public class SetMp4AsWallpaperService extends WallpaperService {

    protected static int playHeadTime = 0;

    public WallpaperService.Engine onCreateEngine() {
        return new VideoEngine();
    }

    class VideoEngine extends WallpaperService.Engine {

        private final String TAG = "LiveWallpaper";
//        private final MediaPlayer mediaPlayer;
        MediaPlayer mediaPlayer = new MediaPlayer();
        public VideoEngine() {
            super();
            Log.i(TAG, "(VideoEngine)");
            SharedPref sharedPref = new SharedPref(getApplicationContext());
            String url = sharedPref.getMp4Path() + "/" + sharedPref.getMp4Name();
            MediaPlayer create = MediaPlayer.create(SetMp4AsWallpaperService.this, Uri.parse(url));
            this.mediaPlayer = create;
//            create.setLooping(true);
            //added on 8/7/2023 to resolve crash
            if (mediaPlayer != null) {
                create.setLooping(true);
            }
        }

//        public void onSurfaceCreated(SurfaceHolder holder) {
//            Log.i(this.TAG, "onSurfaceCreated");
//            this.mediaPlayer.setSurface(holder.getSurface());
//            this.mediaPlayer.start();
//        }


        //added on 8/22/2023 by hasnain to avoid crash
        public void onSurfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "onSurfaceCreated");
            if (mediaPlayer != null) {
                mediaPlayer.setSurface(holder.getSurface());
                mediaPlayer.start();
            }
        }

//        public void onSurfaceDestroyed(SurfaceHolder holder) {
//            Log.i(this.TAG, "( INativeWallpaperEngine ): onSurfaceDestroyed");
//            SetMp4AsWallpaperService.playHeadTime = this.mediaPlayer.getCurrentPosition();
//            this.mediaPlayer.reset();
//            this.mediaPlayer.release();
//        }

        public void onSurfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "onSurfaceDestroyed");
            if (mediaPlayer != null) {
                SetMp4AsWallpaperService.playHeadTime = mediaPlayer.getCurrentPosition();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }
}