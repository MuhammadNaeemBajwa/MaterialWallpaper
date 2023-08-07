package com.app.materialwallpaper.activities;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.app.materialwallpaper.R;

import com.app.materialwallpaper.utils.Tools;


public class BuyPremiumActivity extends AppCompatActivity {

//    private BillingManager billingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Tools.getTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_premium);

        View lytPurchase = findViewById(R.id.lytPurchase);
        View alreadyPurchased = findViewById(R.id.lytAlreadyPurchased);
        if (MyApplication.getApp().isPremium()) {
            lytPurchase.setVisibility(View.GONE);
            alreadyPurchased.setVisibility(View.VISIBLE);
        } else {
            lytPurchase.setVisibility(View.VISIBLE);
            alreadyPurchased.setVisibility(View.GONE);
        }

    }


//    @Override
//    public void onPremiumUpgradePurchased() {
//       showShortToast(this, getString(R.string.premium_upgrade_purchased));
//    }

    private void showShortToast(BuyPremiumActivity context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, BuyPremiumActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

//    private String url;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        Tools.getTheme(this);
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_buy_premium);
////        url = getIntent().getStringExtra("url");
////        if (url != null) {
////            Glide.with(this)
////                    .load(url.replace(" ", "%20"))
////                    .diskCacheStrategy(DiskCacheStrategy.ALL)
////                    .placeholder(R.drawable.ic_transparent)
////                    .into((ImageView) findViewById(R.id.imgWallpaper));
////        }
//
//    }

    public void onCloseClicked(View view) {
        finish();
    }

    public void onPurchaseClicked(View view) {
       MyApplication.getApp().purchasePremium(this);
    }


}