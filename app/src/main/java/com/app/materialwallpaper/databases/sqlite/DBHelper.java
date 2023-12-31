package com.app.materialwallpaper.databases.sqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.app.materialwallpaper.models.Category;
import com.app.materialwallpaper.models.Wallpaper;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("Range")
public class DBHelper extends SQLiteOpenHelper {

    private static String DATABASE_NAME = "material_wallpaper_5.db";
    private static int DATABASE_VERSION = 2;
    private SQLiteDatabase db;
    private final Context context;
    private String DB_PATH;
    String outFileName = "";
    SharedPreferences.Editor spEdit;
    public static final String TABLE_RECENT = "tbl_recent";
    public static final String TABLE_FEATURED = "tbl_featured";
    public static final String TABLE_POPULAR = "tbl_popular";
    public static final String TABLE_RANDOM = "tbl_random";
    public static final String TABLE_GIF = "tbl_gif";
    public static final String TABLE_CATEGORY = "tbl_category";
    public static final String TABLE_CATEGORY_DETAIL = "tbl_category_detail";
    public static final String TABLE_FAVORITE = "tbl_favorite";
    public static final String ID = "id";
    public static final String IMAGE_ID = "image_id";
    public static final String IMAGE_NAME = "image_name";
    public static final String IMAGE_UPLOAD = "image_upload";
    public static final String IMAGE_URL = "image_url";
    public static final String IMAGE_THUMB = "image_thumb";
    public static final String TYPE = "type";
    public static final String RESOLUTION = "resolution";
    public static final String SIZE = "size";
    public static final String MIME = "mime";
    public static final String VIEWS = "views";
    public static final String DOWNLOADS = "downloads";
    public static final String FEATURED = "featured";
    public static final String TAGS = "tags";
    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_NAME = "category_name";
    public static final String CATEGORY_IMAGE = "category_image";
    public static final String TOTAL_WALLPAPER = "total_wallpaper";
    public static final String LAST_UPDATE = "last_update";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.db = this.getWritableDatabase();
        Log.d("DB", "Constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB", "onCreate");
        createTableWallpaper(db, TABLE_RECENT);
        createTableWallpaper(db, TABLE_FEATURED);
        createTableWallpaper(db, TABLE_POPULAR);
        createTableWallpaper(db, TABLE_RANDOM);
        createTableWallpaper(db, TABLE_GIF);
        createTableWallpaper(db, TABLE_FAVORITE);
        createTableWallpaper(db, TABLE_CATEGORY_DETAIL);
        createTableCategory(db, TABLE_CATEGORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEATURED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POPULAR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RANDOM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GIF);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY_DETAIL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        createTableWallpaper(db, TABLE_RECENT);
        createTableWallpaper(db, TABLE_FEATURED);
        createTableWallpaper(db, TABLE_POPULAR);
        createTableWallpaper(db, TABLE_RANDOM);
        createTableWallpaper(db, TABLE_GIF);
        createTableWallpaper(db, TABLE_FAVORITE);
        createTableWallpaper(db, TABLE_CATEGORY_DETAIL);
        createTableCategory(db, TABLE_CATEGORY);
    }

    public void truncateTableWallpaper(String table) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
        createTableWallpaper(db, table);
    }

    public void truncateTableCategory(String table) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
        createTableCategory(db, table);
    }

    private void createTableCategory(SQLiteDatabase db, String table) {
        String CREATE_TABLE = "CREATE TABLE " + table + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CATEGORY_ID + " TEXT, "
                + CATEGORY_NAME + " TEXT, "
                + CATEGORY_IMAGE + " TEXT, "
                + TOTAL_WALLPAPER + " TEXT "
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    private void createTableWallpaper(SQLiteDatabase db, String table) {
        String CREATE_TABLE = "CREATE TABLE " + table + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + IMAGE_ID + " TEXT, "
                + IMAGE_NAME + " TEXT, "
                + IMAGE_UPLOAD + " TEXT, "
                + IMAGE_URL + " TEXT, "
                + IMAGE_THUMB + " TEXT, "
                + TYPE + " TEXT, "
                + RESOLUTION + " TEXT, "
                + SIZE + " TEXT, "
                + MIME + " TEXT, "
                + VIEWS + " INTEGER, "
                + DOWNLOADS + " INTEGER, "
                + FEATURED + " TEXT, "
                + TAGS + " TEXT, "
                + CATEGORY_ID + " TEXT, "
                + CATEGORY_NAME + " TEXT, "
                + LAST_UPDATE + " TEXT "
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.disableWriteAheadLogging();
        }
    }

    public void addListCategory(List<Category> categories, String table) {
        for (Category category : categories) {
            addOneCategory(db, category, table);
        }
        getAllCategory(table);
    }

//    public void addListWallpaper(List<Wallpaper> wallpapers, String table) {
//        for (Wallpaper wallpaper : wallpapers) {
////            addOneWallpaper(db, wallpaper, table);
//
//            // added on 10/24/2023  by hasnain to remove the ANR issue in the crash rate comment out the above function
//            addOneWallpaper(wallpaper, table);
//        }
//        getAllWallpaper(table);
//    }


    // added on 10/24/2023 to resolve ANR comment out the above code
    public void addListWallpaper(final List<Wallpaper> wallpapers, final String table) {
        new AsyncTask<List<Wallpaper>, Void, Void>() {
            @Override
            protected Void doInBackground(List<Wallpaper>... params) {
                for (Wallpaper wallpaper : params[0]) {
                    addOneWallpaper(wallpaper, table);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getAllWallpaper(table);
            }
        }.execute(wallpapers);
    }


    public void addOneCategory(SQLiteDatabase db, Category category, String table) {
        ContentValues values = new ContentValues();
        values.put(CATEGORY_ID, category.category_id);
        values.put(CATEGORY_NAME, category.category_name);
        values.put(CATEGORY_IMAGE, category.category_image);
        values.put(TOTAL_WALLPAPER, category.total_wallpaper);
        db.insert(table, null, values);
    }

//    public void addOneWallpaper(SQLiteDatabase db, Wallpaper wallpaper, String table) {
//        ContentValues values = new ContentValues();
//        values.put(IMAGE_ID, wallpaper.image_id);
//        values.put(IMAGE_NAME, wallpaper.image_name);
//        values.put(IMAGE_UPLOAD, wallpaper.image_upload);
//        values.put(IMAGE_URL, wallpaper.image_url);
//        values.put(IMAGE_THUMB, wallpaper.image_thumb);
//        values.put(TYPE, wallpaper.type);
//        values.put(RESOLUTION, wallpaper.resolution);
//        values.put(SIZE, wallpaper.size);
//        values.put(MIME, wallpaper.mime);
//        values.put(VIEWS, wallpaper.views);
//        values.put(DOWNLOADS, wallpaper.downloads);
//        values.put(FEATURED, wallpaper.featured);
//        values.put(TAGS, wallpaper.tags);
//        values.put(CATEGORY_ID, wallpaper.category_id);
//        values.put(CATEGORY_NAME, wallpaper.category_name);
//        values.put(LAST_UPDATE, wallpaper.last_update);
//        db.insert(table, null, values);
//    }




// added on 10/24/2023  by hasnain to remove the ANR issue in the crash rate comment out the above function
    public void addOneWallpaper(final Wallpaper wallpaper, final String table) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ContentValues values = new ContentValues();
                values.put(IMAGE_ID, wallpaper.image_id);
                values.put(IMAGE_NAME, wallpaper.image_name);
                values.put(IMAGE_UPLOAD, wallpaper.image_upload);
                values.put(IMAGE_URL, wallpaper.image_url);
                values.put(IMAGE_THUMB, wallpaper.image_thumb);
                values.put(TYPE, wallpaper.type);
                values.put(RESOLUTION, wallpaper.resolution);
                values.put(SIZE, wallpaper.size);
                values.put(MIME, wallpaper.mime);
                values.put(VIEWS, wallpaper.views);
                values.put(DOWNLOADS, wallpaper.downloads);
                values.put(FEATURED, wallpaper.featured);
                values.put(TAGS, wallpaper.tags);
                values.put(CATEGORY_ID, wallpaper.category_id);
                values.put(CATEGORY_NAME, wallpaper.category_name);
                values.put(LAST_UPDATE, wallpaper.last_update);
                db.insert(table, null, values);
                return null;
            }
        }.execute();
    }



    public void addOneFavorite(Wallpaper wallpaper) {
        ContentValues values = new ContentValues();

        values.put(IMAGE_ID, wallpaper.image_id);
        values.put(IMAGE_NAME, wallpaper.image_name);
        values.put(IMAGE_UPLOAD, wallpaper.image_upload);
        values.put(IMAGE_URL, wallpaper.image_url);
        values.put(IMAGE_THUMB, wallpaper.image_thumb);
        values.put(TYPE, wallpaper.type);
        values.put(RESOLUTION, wallpaper.resolution);
        values.put(SIZE, wallpaper.size);
        values.put(MIME, wallpaper.mime);
        values.put(VIEWS, wallpaper.views);
        values.put(DOWNLOADS, wallpaper.downloads);
        values.put(FEATURED, wallpaper.featured);
        values.put(TAGS, wallpaper.tags);
        values.put(CATEGORY_ID, wallpaper.category_id);
        values.put(CATEGORY_NAME, wallpaper.category_name);
        values.put(LAST_UPDATE, wallpaper.last_update);
        db.insert(TABLE_FAVORITE, null, values);
    }

    public void deleteAll(String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + table);
    }

    public void deleteWallpaperByCategory(String table, String category_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + table + " WHERE " + CATEGORY_ID + " = " + category_id);
    }

    public void deleteFavorites(Wallpaper wallpaper) {
        db.delete(TABLE_FAVORITE, IMAGE_ID + " = ?", new String[]{wallpaper.image_id + ""});
    }

    public List<Category> getAllCategory(String table) {
        return getAllCategories(table);
    }

    public List<Wallpaper> getAllWallpaper(String table) {
        return getAllWallpapers(table);
    }

    public List<Wallpaper> getAllWallpaperByCategory(String table, String category_id) {
        return getAllWallpapersByCategory(table, category_id);
    }

    public List<Wallpaper> getAllFavorite(String table) {
        return getAllFavorites(table);
    }

    private List<Category> getAllCategories(String table) {
        List<Category> list;
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + table + " ORDER BY " + "id" + " ASC", null);
        list = getAllCategoryFormCursor(cursor);
        return list;
    }

    private List<Wallpaper> getAllWallpapers(String table) {
        List<Wallpaper> list;
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + table + " ORDER BY " + "id" + " ASC LIMIT 100", null);
        list = getAllWallpaperFormCursor(cursor);
        return list;
    }

    private List<Wallpaper> getAllWallpapersByCategory(String table, String category_id) {
        List<Wallpaper> list;
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + table + " WHERE " + CATEGORY_ID + " = " + category_id + " ORDER BY " + "id" + " ASC LIMIT 100", null);
        list = getAllWallpaperFormCursor(cursor);
        return list;
    }

    private List<Wallpaper> getAllFavorites(String table) {
        List<Wallpaper> list;
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + table + " ORDER BY " + "id" + " DESC", null);
        list = getAllWallpaperFormCursor(cursor);
        return list;
    }

    public boolean isFavoritesExist(String id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAVORITE + " WHERE " + IMAGE_ID + " = ?", new String[]{id + ""});
        int count = cursor.getCount();
        cursor.close();
        return (count > 0);
    }

    private List<Category> getAllCategoryFormCursor(Cursor cursor) {
        List<Category> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.category_id = cursor.getString(cursor.getColumnIndex(CATEGORY_ID));
                category.category_name = cursor.getString(cursor.getColumnIndex(CATEGORY_NAME));
                category.category_image = cursor.getString(cursor.getColumnIndex(CATEGORY_IMAGE));
                category.total_wallpaper = cursor.getString(cursor.getColumnIndex(TOTAL_WALLPAPER));
                list.add(category);
            } while (cursor.moveToNext());
        }
        return list;
    }

    private List<Wallpaper> getAllWallpaperFormCursor(Cursor cursor) {
        List<Wallpaper> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Wallpaper wallpaper = new Wallpaper();
                wallpaper.image_id = cursor.getString(cursor.getColumnIndex(IMAGE_ID));
                wallpaper.image_name = cursor.getString(cursor.getColumnIndex(IMAGE_NAME));
                wallpaper.image_upload = cursor.getString(cursor.getColumnIndex(IMAGE_UPLOAD));
                wallpaper.image_url = cursor.getString(cursor.getColumnIndex(IMAGE_URL));
                wallpaper.image_thumb = cursor.getString(cursor.getColumnIndex(IMAGE_THUMB));
                wallpaper.type = cursor.getString(cursor.getColumnIndex(TYPE));
                wallpaper.resolution = cursor.getString(cursor.getColumnIndex(RESOLUTION));
                wallpaper.size = cursor.getString(cursor.getColumnIndex(SIZE));
                wallpaper.mime = cursor.getString(cursor.getColumnIndex(MIME));
                wallpaper.views = cursor.getInt(cursor.getColumnIndex(VIEWS));
                wallpaper.downloads = cursor.getInt(cursor.getColumnIndex(DOWNLOADS));
                wallpaper.featured = cursor.getString(cursor.getColumnIndex(FEATURED));
                wallpaper.tags = cursor.getString(cursor.getColumnIndex(TAGS));
                wallpaper.category_id = cursor.getString(cursor.getColumnIndex(CATEGORY_ID));
                wallpaper.category_name = cursor.getString(cursor.getColumnIndex(CATEGORY_NAME));
                wallpaper.last_update = cursor.getString(cursor.getColumnIndex(LAST_UPDATE));
                list.add(wallpaper);
            } while (cursor.moveToNext());
        }
        return list;
    }

}  