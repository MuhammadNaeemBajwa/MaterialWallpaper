package com.app.materialwallpaper.utils;

import static com.app.materialwallpaper.utils.Constant.DOWNLOAD;
import static com.app.materialwallpaper.utils.Constant.SET_GIF;
import static com.app.materialwallpaper.utils.Constant.SET_MP4;
import static com.app.materialwallpaper.utils.Constant.SET_WITH;
import static com.app.materialwallpaper.utils.Constant.SHARE;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.app.materialwallpaper.BuildConfig;
import com.app.materialwallpaper.Config;
import com.app.materialwallpaper.R;
import com.app.materialwallpaper.activities.ActivityNotificationDetail;
import com.app.materialwallpaper.activities.ActivityWebView;
import com.app.materialwallpaper.activities.MainActivity;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.services.SetGIFAsWallpaperService;
import com.app.materialwallpaper.services.SetMp4AsWallpaperService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

@SuppressWarnings("deprecation")
public class Tools {

    public static final String TAG = "Tools";
    Context context;

    public Tools(Context context) {
        this.context = context;
    }

    public static void getTheme(Context context) {
        SharedPref sharedPref = new SharedPref(context);
        if (sharedPref.getIsDarkTheme()) {
            context.setTheme(R.style.AppDarkTheme);
        } else {
            context.setTheme(R.style.AppTheme);
        }
    }

    public static RequestBuilder<Drawable> requestBuilder(Context context) {
        return Glide.with(context).asDrawable().sizeMultiplier(0.1f);
    }

    public static void notificationOpenHandler(Context context, Intent getIntent) {
        long unique_id = getIntent.getLongExtra("unique_id", 0);
        long post_id = getIntent.getLongExtra("post_id", 0);
        String title = getIntent.getStringExtra("title");
        String link = getIntent.getStringExtra("link");
        if (post_id == 0) {
            if (link != null && !link.equals("")) {
                if (link.contains("play.google.com")) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                } else {
                    Intent intent = new Intent(context, ActivityWebView.class);
                    intent.putExtra("title", title);
                    intent.putExtra("url", link);
                    context.startActivity(intent);
                }
            }
        } else if (post_id > 0) {
            Intent intent = new Intent(context, ActivityNotificationDetail.class);
            intent.putExtra("id", String.valueOf(post_id));
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        }
    }

    public static void getRtlDirection(Activity activity) {
        if (Config.ENABLE_RTL_MODE) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public static void darkNavigationStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.color_dark_toolbar));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void darkNavigation(Activity activity) {
        activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.color_dark_toolbar));
    }

    public static void lightNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.color_white));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    public static void lightToolbar(Activity activity, Toolbar toolbar) {
        toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_light_primary));
    }

    public static void darkToolbar(Activity activity, Toolbar toolbar) {
        toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_dark_toolbar));
    }

    public static void transparentStatusBar(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        //activity.getWindow().setNavigationBarColor(Color.BLACK);
        activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.color_dark_toolbar));
    }

    public static void transparentStatusBarNavigation(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static String withSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c", count / Math.pow(1000, exp), "KMGTPE".charAt(exp - 1));
    }

    public static long timeStringtoMilis(String time) {
        long milis = 0;
        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sd.parse(time);
            milis = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return milis;
    }

    public static String decode(String code) {
        return decodeBase64(decodeBase64(decodeBase64(code)));
    }

    public static String decodeBase64(String code) {
        byte[] valueDecoded = Base64.decode(code.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        return new String(valueDecoded);
    }

    public static String getUserAgent() {

        StringBuilder result = new StringBuilder(64);
        result.append("Dalvik/");
        result.append(System.getProperty("java.vm.version"));
        result.append(" (Linux; U; Android ");

        String version = Build.VERSION.RELEASE;
        result.append(version.length() > 0 ? version : "1.0");

        if ("REL".equals(Build.VERSION.CODENAME)) {
            String model = Build.MODEL;
            if (model.length() > 0) {
                result.append("; ");
                result.append(model);
            }
        }

        String id = Build.ID;

        if (id.length() > 0) {
            result.append(" Build/");
            result.append(id);
        }

        result.append(")");
        return result.toString();
    }

    public static void setAction(Context context, byte[] bytes, String imgName, String action) {
        try {
            File dir;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + context.getString(R.string.app_name));
            } else {
                dir = new File(Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.app_name));
            }
            boolean success = true;
            if (!dir.exists()) {
                success = dir.mkdirs();
            }
            if (success) {
                File imageFile = new File(dir, imgName);
                FileOutputStream fileWriter = new FileOutputStream(imageFile);
                fileWriter.write(bytes);
                fileWriter.flush();
                fileWriter.close();

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File file = new File(imageFile.getAbsolutePath());
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                context.sendBroadcast(mediaScanIntent);

//                StrictMode.ThreadPolicy policy = new StrictMode
//                        .ThreadPolicy
//                        .Builder()
//                        .permitAll()
//                        .build();
//                StrictMode.setThreadPolicy(policy);

                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                switch (action) {
                    case DOWNLOAD:
                        //do nothing
                        break;

                    case SHARE:
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("image/*");
                        share.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.share_text) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imageFile.getAbsolutePath()));
                        context.startActivity(Intent.createChooser(share, "Share Image"));
                        break;

                    case SET_WITH:
                        Intent setWith = new Intent(Intent.ACTION_ATTACH_DATA);
                        setWith.addCategory(Intent.CATEGORY_DEFAULT);
                        setWith.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "image/*");
                        setWith.putExtra("mimeType", "image/*");
                        context.startActivity(Intent.createChooser(setWith, "Set as:"));
                        break;

                    case SET_GIF:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Constant.gifPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + context.getString(R.string.app_name);
                        } else {
                            Constant.gifPath = Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.app_name);
                        }
                        Constant.gifName = file.getName();

                        SharedPref sharedPref = new SharedPref(context);
                        sharedPref.saveGif(Constant.gifPath, Constant.gifName);

                        try {
                            WallpaperManager.getInstance(context).clear();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent setGif = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                        setGif.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(context, SetGIFAsWallpaperService.class));
                        context.startActivity(setGif);

                        Log.d("GIF_PATH", Constant.gifPath);
                        Log.d("GIF_NAME", Constant.gifName);
                        break;

                    case SET_MP4:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Constant.mp4Path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + context.getString(R.string.app_name);
                        } else {
                            Constant.mp4Path = Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.app_name);
                        }
                        Constant.mp4Name = file.getName();

                        SharedPref sharedPrefs = new SharedPref(context);
                        sharedPrefs.saveMp4(Constant.mp4Path, Constant.mp4Name);

                        try {
                            WallpaperManager.getInstance(context).clear();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent setMp4 = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                        setMp4.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(context, SetMp4AsWallpaperService.class));
                        context.startActivity(setMp4);

                        Log.d(TAG, "" + Constant.mp4Path);
                        Log.d(TAG, "" + Constant.mp4Name);
                        break;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String createName(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File is too large!");
        }
        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        InputStream is = new FileInputStream(file);
        try {
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        return bytes;
    }

    public static byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final int bufLen = 4 * 0x400; // 4KB
        byte[] buf = new byte[bufLen];
        int readLen;
        IOException exception = null;
        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                    outputStream.write(buf, 0, readLen);

                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            exception = e;
            throw e;
        } finally {
            if (exception == null) inputStream.close();
            else try {
                inputStream.close();
            } catch (IOException e) {
                exception.addSuppressed(e);
            }
        }
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivity = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connectivity.getAllNetworks();
        NetworkInfo networkInfo;
        for (Network mNetwork : networks) {
            networkInfo = connectivity.getNetworkInfo(mNetwork);
            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConnect(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.isConnected() || activeNetworkInfo.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String getJSONString(String url) {
        String jsonString = null;
        HttpURLConnection linkConnection = null;
        try {
            URL linkurl = new URL(url);
            linkConnection = (HttpURLConnection) linkurl.openConnection();
            int responseCode = linkConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream linkinStream = linkConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int j = 0;
                while ((j = linkinStream.read()) != -1) {
                    baos.write(j);
                }
                byte[] data = baos.toByteArray();
                jsonString = new String(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (linkConnection != null) {
                linkConnection.disconnect();
            }
        }
        return jsonString;
    }

    public static boolean isVpnConnectionAvailable() {
        String iface = "";
        try {
            for (NetworkInterface networkInst : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInst.isUp())
                    iface = networkInst.getName();
                if (iface.contains("tun") || iface.contains("ppp") || iface.contains("pptp")) {
                    return true;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void showWarningDialog(Activity activity, String title, String message) {

        // added on 8/7/2023 By Hasnain to resolve Crash
        if (activity == null) {
            // Log an error or handle the case when the context is null
            return;
        }
        // till here



        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(activity.getString(R.string.dialog_option_ok), (dialog, which) -> activity.finish())
                .setCancelable(false)
                .show();
    }

}
