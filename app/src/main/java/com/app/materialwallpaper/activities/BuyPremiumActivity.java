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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, BuyPremiumActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }
    public void onCloseClicked(View view) {
        finish();
    }

    public void onPurchaseClicked(View view) {
       MyApplication.getApp().purchasePremium(this);
    }


}