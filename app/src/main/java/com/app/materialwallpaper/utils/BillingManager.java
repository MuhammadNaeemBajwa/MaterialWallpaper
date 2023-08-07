//package com.app.materialwallpaper.utils;
//
//import android.app.Activity;
//import android.content.Context;
//import android.util.Log;
//
//import com.android.billingclient.api.AcknowledgePurchaseParams;
//import com.android.billingclient.api.BillingClient;
//import com.android.billingclient.api.BillingClientStateListener;
//import com.android.billingclient.api.BillingFlowParams;
//import com.android.billingclient.api.BillingResult;
//import com.android.billingclient.api.ProductDetails;
//import com.android.billingclient.api.ProductDetailsResponseListener;
//import com.android.billingclient.api.Purchase;
//import com.android.billingclient.api.PurchasesUpdatedListener;
//import com.android.billingclient.api.QueryProductDetailsParams;
//import com.android.billingclient.api.QueryPurchasesParams;
//
//
//import java.util.List;
//
//import android.app.Activity;
//import android.content.Context;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.android.billingclient.api.AcknowledgePurchaseParams;
//import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
//import com.android.billingclient.api.BillingClient;
//import com.android.billingclient.api.BillingClientStateListener;
//import com.android.billingclient.api.BillingFlowParams;
//import com.android.billingclient.api.BillingResult;
//import com.android.billingclient.api.ProductDetails;
//import com.android.billingclient.api.ProductDetailsResponseListener;
//import com.android.billingclient.api.Purchase;
//import com.android.billingclient.api.PurchasesUpdatedListener;
//import com.android.billingclient.api.QueryProductDetailsParams;
//import com.android.billingclient.api.SkuDetails;
//import com.android.billingclient.api.SkuDetailsParams;
//import com.android.billingclient.api.SkuDetailsResponseListener;
//import com.google.common.collect.ImmutableList;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class BillingManager implements PurchasesUpdatedListener {
//
//    public boolean isPremiumPurchased() {
//        return false;
//    }
//
//
//
//    public interface BillingManagerListener {
//        void onPremiumUpgradePurchased();
//
//        void onPremiumUpgradeRestored();
//    }
//
//    private static final String SKU_PREMIUM_UPGRADE = "sku-premium_upgrade";
//
//    private BillingClient billingClient;
//    private BillingManagerListener listener;
//    private Activity activity;
//
//    public BillingManager(Activity activity, BillingManagerListener listener) {
//        this.activity = activity;
//        this.listener = listener;
//        setupBillingClient();
//    }
//
//    private void setupBillingClient() {
//        billingClient = BillingClient.newBuilder(activity)
//                .setListener(this)
//                .enablePendingPurchases()
//                .build();
//
//        billingClient.startConnection(new BillingClientStateListener() {
//            @Override
//            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
//                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                    //checkIfItemIsPurchased();
//
//                }
//            }
//
//            @Override
//            public void onBillingServiceDisconnected() {
//                // Try to restart the connection on the next request to
//                // Google Play by calling the startConnection() method.
//            }
//        });
//    }
//
//
//    public void purchasePremiumUpgrade() {
//
//
//        if (billingClient.isReady()) {
//            QueryProductDetailsParams queryProductDetailsParams =
//                    QueryProductDetailsParams.newBuilder()
//                            .setProductList(
//                                    ImmutableList.of(
//                                            QueryProductDetailsParams.Product.newBuilder()
//                                                    .setProductId(SKU_PREMIUM_UPGRADE)
//                                                    .setProductType(BillingClient.ProductType.INAPP)
//                                                    .build()))
//                            .build();
//
//            billingClient.queryProductDetailsAsync(
//                    queryProductDetailsParams,
//                    (billingResult, productDetailsList) -> {
//
//                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
//                            if (productDetailsList.size()  == 0){
//                                showErrorDialog("No product found");
//                                return;
//                            }
//                            for (ProductDetails skuDetails : productDetailsList) {
//                                if (SKU_PREMIUM_UPGRADE.equals(skuDetails.getProductId())) {
//                                    BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
//                                            .setProductDetails(skuDetails)
//                                            .build();
//                                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
//                                            .setProductDetailsParamsList(Arrays.asList(productDetailsParams))
//                                            .build();
//                                    billingClient.launchBillingFlow(activity, billingFlowParams);
//                                    break;
//                                }
//                            }
//                        } else {
//                            showBillingError(billingResult.getResponseCode());
//                        }
//                    });
//
//
//        } else {
//            showErrorDialog("Billing client is not ready");
//        }
//    }
//
//    private void showBillingError(int responseCode) {
//        switch (responseCode) {
//            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
//                showErrorDialog("Service Disconnected");
//                break;
//            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
//               showErrorDialog("Service Unavailable");
//                break;
//            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
//                showErrorDialog("Billing Unavailable");
//                break;
//            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
//                showErrorDialog("Item Unavailable");
//                break;
//            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
//                showErrorDialog("Developer Error");
//                break;
//            case BillingClient.BillingResponseCode.ERROR:
//                showErrorDialog("Error");
//                break;
//            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
//                showErrorDialog("Item Already Owned");
//                break;
//            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
//                showErrorDialog("Item Not Owned");
//                break;
//        }
//    }
//
//    /**
//     * Show error dialog
//     * @param error Error message
//     *  This method shows an error dialog with the given message
//     */
//    private void showErrorDialog(String error) {
//        activity.runOnUiThread(() ->new android.app.AlertDialog.Builder(activity)
//                .setTitle("Error")
//                .setMessage(error)
//                .setPositiveButton("OK", null)
//                .show());
//
//    }
//
//
//
//    @Override
//    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
//        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
//            for (Purchase purchase : purchases) {
//                if (purchase.getSkus().get(0).equals(SKU_PREMIUM_UPGRADE)) {
//                    handlePurchase(purchase);
//                }
//            }
//        }
//    }
//
//    private void handlePurchase(Purchase purchase) {
//        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
//            if (!purchase.isAcknowledged()) {
//                acknowledgePurchase(purchase.getPurchaseToken());
//            }
//            listener.onPremiumUpgradePurchased();
//        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
//            // Handle pending purchases
//        }
//    }
//
//    private void acknowledgePurchase(String purchaseToken) {
//        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
//                .setPurchaseToken(purchaseToken)
//                .build();
//
//        billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
//            @Override
//            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
//                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                    listener.onPremiumUpgradeRestored();
//                }
//            }
//        });
//    }
//
//    public void onDestroy() {
//        if (billingClient != null && billingClient.isReady()) {
//            billingClient.endConnection();
//        }
//    }
//
//}
//
