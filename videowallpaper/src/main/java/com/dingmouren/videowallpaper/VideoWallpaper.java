package com.dingmouren.videowallpaper;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

import static com.dingmouren.videowallpaper.Constant.ACTION_VOICE_NORMAL;
import static com.dingmouren.videowallpaper.Constant.ACTION_VOICE_SILENCE;


/**
 * Created by dingmouren on 2017/7/10.
 */

public class VideoWallpaper extends WallpaperService {
    private static final String TAG = VideoWallpaper.class.getName();
    private static String sVideoPath;

    /**
     * 设置静音
     *
     * @param context
     */
    public static void setVoiceSilence(Context context) {
        Intent intent = new Intent(Constant.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(Constant.ACTION, ACTION_VOICE_SILENCE);
        context.sendBroadcast(intent);
    }

    /**
     * 设置有声音
     *
     * @param context
     */
    public static void setVoiceNormal(Context context) {
        Intent intent = new Intent(Constant.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(Constant.ACTION, ACTION_VOICE_NORMAL);
        context.sendBroadcast(intent);
    }

    /**
     * 设置壁纸
     *
     * @param context
     */
    public static void setToWallPaper(Context context, String videoPath) {
        try {
            context.clearWallpaper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sVideoPath = videoPath;
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(context, VideoWallpaper.class));
        context.startActivity(intent);
    }


    @Override
    public Engine onCreateEngine() {
        return new VideoWallpagerEngine();
    }

    class VideoWallpagerEngine extends Engine {
        private MediaPlayer mMediaPlayer;
        private BroadcastReceiver mVideoVoiceControlReceiver;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            IntentFilter intentFilter = new IntentFilter(Constant.VIDEO_PARAMS_CONTROL_ACTION);
            mVideoVoiceControlReceiver = new VideoVoiceControlReceiver();
            registerReceiver(mVideoVoiceControlReceiver, intentFilter);
        }

        @Override
        public void onDestroy() {
            unregisterReceiver(mVideoVoiceControlReceiver);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (TextUtils.isEmpty(sVideoPath)) {
                Log.d("==w", "videoPath为空");
                return;
            }

            //Added on 8/7/2023 By hasnain to avoid crash
            // Ensure mMediaPlayer is not null before calling its methods
            //this
            if (mMediaPlayer != null) {

                if (visible) {
                    mMediaPlayer.start();
                } else {
                    mMediaPlayer.pause();
                }

            }
            //till here
        }

//        @Override
//        public void onSurfaceCreated(SurfaceHolder holder) {
//            super.onSurfaceCreated(holder);
//            if (TextUtils.isEmpty(sVideoPath)) {
//                //throw  new NullPointerException("videoPath must not be null ");
//                Log.d("==w", "videoPath为空: " + sVideoPath);
//            } else {
//                mMediaPlayer = new MediaPlayer();
//                mMediaPlayer.setSurface(holder.getSurface());
//
//                try {
//
//                    mMediaPlayer.setDataSource(sVideoPath);
//                    mMediaPlayer.setLooping(true);
//                    mMediaPlayer.setVolume(0f, 0f);
//                    mMediaPlayer.prepare();
//                    mMediaPlayer.start();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }

//I have added this to resolve crash

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            if (TextUtils.isEmpty(sVideoPath)) {
                Log.d("==w", "videoPath为空: " + sVideoPath);
                return;
            }

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setSurface(holder.getSurface());

            try {
                mMediaPlayer.setDataSource(sVideoPath);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.setVolume(0f, 0f);
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mMediaPlayer.start();
                    }
                });
                mMediaPlayer.prepareAsync(); // Use prepareAsync to avoid blocking the UI thread
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Till here


        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }

        class VideoVoiceControlReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                int action = intent.getIntExtra(Constant.ACTION, -1);
                switch (action) {
                    case ACTION_VOICE_NORMAL:
                        mMediaPlayer.setVolume(1.0f, 1.0f);
                        break;
                    case ACTION_VOICE_SILENCE:
                        mMediaPlayer.setVolume(0, 0);
                        break;
                }
            }
        }
    }


}