package com.app.materialwallpaper.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.materialwallpaper.BuildConfig;
import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.utils.Tools;
import com.app.materialwallpaper.utils.WallpaperHelper;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;

public class ActivityVideoPlayer extends AppCompatActivity {

    String videoUrl;
    private StyledPlayerView playerView;
    private ExoPlayer exoPlayer;
    private DefaultDataSource.Factory dataSourceFactory;
    private ProgressBar progressBar;
    Button btnSetWallpaper;
    ProgressDialog progressDialog;
    WallpaperHelper wallpaperHelper;
    RelativeLayout parentView;
    Toolbar toolbar;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_video_player);
        Tools.transparentStatusBarNavigation(this);
        if (!BuildConfig.DEBUG) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        videoUrl = getIntent().getStringExtra("video_url");

        wallpaperHelper = new WallpaperHelper(this);
        progressDialog = new ProgressDialog(this);

        parentView = findViewById(R.id.parent_view);
        progressBar = findViewById(R.id.progressBar);
        btnSetWallpaper = findViewById(R.id.btn_set_wallpaper);

        HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setUserAgent(Tools.getUserAgent());

        dataSourceFactory = new DefaultDataSource.Factory(getApplicationContext(), httpDataSourceFactory);

        LoadControl loadControl = new DefaultLoadControl();

        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this, trackSelectionFactory);

        exoPlayer = new ExoPlayer.Builder(this).setTrackSelector(trackSelector).setLoadControl(loadControl).build();
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == PlaybackStateCompat.STATE_PLAYING) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                exoPlayer.stop();
                errorDialog();
            }
        });

        playerView = findViewById(R.id.exoPlayerView);
        playerView.setPlayer(exoPlayer);
        playerView.setUseController(true);
        playerView.requestFocus();

        Uri uri = Uri.parse(videoUrl);

        MediaSource mediaSource = buildMediaSource(uri);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);

        if (Config.ENABLE_CENTER_CROP_IN_DETAIL_WALLPAPER) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        }

        btnSetWallpaper.setOnClickListener(view -> wallpaperHelper.setMp4(parentView, progressDialog, videoUrl));

        setupToolbar();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        MediaItem mMediaItem = MediaItem.fromUri(Uri.parse(String.valueOf(uri)));
        return new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(mMediaItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exoPlayer.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.setPlayWhenReady(false);
        exoPlayer.getPlaybackState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.getPlaybackState();
    }

    public void errorDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.whops))
                .setCancelable(false)
                .setMessage("Error when loading live wallpaper")
                .setPositiveButton(getString(R.string.btn_retry), (dialog, which) -> retryLoad())
                .setNegativeButton(getString(R.string.dialog_option_cancel), (dialogInterface, i) -> finish())
                .show();
    }

    public void retryLoad() {
        Uri uri = Uri.parse(videoUrl);
        MediaSource mediaSource = buildMediaSource(uri);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }

}
