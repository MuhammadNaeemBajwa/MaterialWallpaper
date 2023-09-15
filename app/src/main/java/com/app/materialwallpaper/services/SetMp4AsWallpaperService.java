package com.app.materialwallpaper.services;

import android.media.MediaPlayer;
import android.net.Uri;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import com.app.materialwallpaper.databases.prefs.SharedPref;

import java.io.IOException;

public class SetMp4AsWallpaperService extends WallpaperService {

    protected static int playHeadTime = 0;

    public WallpaperService.Engine onCreateEngine() {
        return new VideoEngine();
    }

    class VideoEngine extends WallpaperService.Engine {

        private final String TAG = "LiveWallpaper";
        private  MediaPlayer mediaPlayer;

        public VideoEngine() {
            super();
            Log.i(TAG, "(VideoEngine)");
//            SharedPref sharedPref = new SharedPref(getApplicationContext());
//            String url = sharedPref.getMp4Path() + "/" + sharedPref.getMp4Name();
//            MediaPlayer create = MediaPlayer.create(SetMp4AsWallpaperService.this, Uri.parse(url));
//            this.mediaPlayer = create;
//            create.setLooping(true);
        }


        //added on 9/14/2023 to remove crash
        public void onCreate(SurfaceHolder surfaceHolder) {
            SharedPref sharedPref = new SharedPref(getApplicationContext());
            String url = sharedPref.getMp4Path() + "/" + sharedPref.getMp4Name();

            mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(SetMp4AsWallpaperService.this, Uri.parse(url));
                mediaPlayer.setSurface(surfaceHolder.getSurface());
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare(); // You may need to handle IOException
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the IOException
            }
        }

        public void onSurfaceCreated(SurfaceHolder holder) {
            Log.i(this.TAG, "onSurfaceCreated");
//            this.mediaPlayer.setSurface(holder.getSurface());
//            this.mediaPlayer.start();
              mediaPlayer.start();
        }

        public void onSurfaceDestroyed(SurfaceHolder holder) {
            Log.i(this.TAG, "( INativeWallpaperEngine ): onSurfaceDestroyed");
            SetMp4AsWallpaperService.playHeadTime = this.mediaPlayer.getCurrentPosition();
//            this.mediaPlayer.reset();
//            this.mediaPlayer.release();

            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }
}