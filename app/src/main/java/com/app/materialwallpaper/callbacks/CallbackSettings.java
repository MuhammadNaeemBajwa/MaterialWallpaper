package com.app.materialwallpaper.callbacks;

import com.app.materialwallpaper.models.AdStatus;
import com.app.materialwallpaper.models.Ads;
import com.app.materialwallpaper.models.App;
import com.app.materialwallpaper.models.Menu;
import com.app.materialwallpaper.models.Settings;

import java.util.ArrayList;
import java.util.List;

public class CallbackSettings {

    public String status;
    public App app = null;
    public List<Menu> menus = new ArrayList<>();
    public Settings settings = null;
    public Ads ads = null;
    public AdStatus ads_status = null;

}
