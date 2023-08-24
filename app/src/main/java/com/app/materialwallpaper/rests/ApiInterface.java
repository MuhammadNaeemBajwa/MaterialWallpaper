package com.app.materialwallpaper.rests;

import com.app.materialwallpaper.callbacks.CallbackAds;
import com.app.materialwallpaper.callbacks.CallbackCategory;
import com.app.materialwallpaper.callbacks.CallbackDetail;
import com.app.materialwallpaper.callbacks.CallbackSettings;
import com.app.materialwallpaper.callbacks.CallbackWallpaper;
import com.app.materialwallpaper.models.Wallpaper;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface ApiInterface {
    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: Material Wallpaper";

    @Headers({CACHE, AGENT})
    @GET("api.php?get_wallpapers")
    Call<CallbackWallpaper> getWallpapers(
            @Query("page") int page,
            @Query("count") int count,
            @Query("filter") String filter,
            @Query("order") String order
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_one_wallpaper")
    Call<CallbackDetail> getOneWallpaper(
            @Query("id") String id
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_categories")
    Call<CallbackCategory> getCategories();

    @Headers({CACHE, AGENT})
    @GET("api.php?get_category_details")
    Call<CallbackWallpaper> getCategoryDetails(
            @Query("page") int page,
            @Query("count") int count,
            @Query("id") String id,
            @Query("filter") String filter,
            @Query("order") String order
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_search")
    Call<CallbackWallpaper> getSearch(@Query("page") int page, @Query("count") int count, @Query("search") String search, @Query("order") String order
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_search_category")
    Call<CallbackCategory> getSearchCategory(@Query("search") String search);

    @FormUrlEncoded
    @POST("api.php?update_view")
    Call<Wallpaper> updateView(@Field("image_id") String image_id);

    @FormUrlEncoded
    @POST("api.php?update_download")
    Call<Wallpaper> updateDownload(@Field("image_id") String image_id);

    @Headers({CACHE, AGENT})
    @GET("api.php?get_ads")
    Call<CallbackAds> getAds();

    @Headers({CACHE, AGENT})
    @GET("api.php?get_settings")
    Call<CallbackSettings> getSettings(@Query("package_name") String package_name);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrl(@Url String fileUrl);
}
