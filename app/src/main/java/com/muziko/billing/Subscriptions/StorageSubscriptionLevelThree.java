package com.muziko.billing.Subscriptions;

import android.app.Activity;
import android.content.Intent;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.muziko.BuildConfig;
import com.muziko.R;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.PrefsManager;
import com.muziko.models.MuzikoSubscription;

import java.util.List;

import static com.muziko.manager.MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_THREE;

/**
 * Created by dev on 20/10/2016.
 */

public class StorageSubscriptionLevelThree {

    private final String TAG = StorageSubscriptionLevelThree.class.getSimpleName();
    private final Activity mContext;
    private BillingProcessor billingProcessor;
    private onUpdatedListener mListener;

    // default constructor
    public StorageSubscriptionLevelThree(final Activity activity, onUpdatedListener listener) {
        mContext = activity;
        mListener = listener;
    }

    public void destroy() {
        if (billingProcessor != null)
            billingProcessor.release();
    }

    public void initBillProcessor() {
        billingProcessor = new BillingProcessor(mContext, BuildConfig.LICENSE_KEY, new PremiumListener());
    }

    public void buyProduct() {
        boolean isAvailable = BillingProcessor.isIabServiceAvailable(mContext);
        if (isAvailable) {
            billingProcessor.subscribe(mContext, STORAGE_SUBSCRIPTION_LEVEL_THREE);
        } else {
            Utils.alert(mContext, mContext.getString(R.string.app_name), "In-app billing not available!", null);
        }
    }

    public interface onUpdatedListener {

        void onPremiumChanged();

        void onPurchased();
    }

    private class PremiumListener implements BillingProcessor.IBillingHandler {
        @Override
        public void onProductPurchased(String productId, TransactionDetails details) {
            /*
             * Called when requested PRODUCT ID was successfully purchased
             */

            new MaterialDialog.Builder(mContext)
                    .title("Debug")
                    .content("ProductID is " + productId)
                    .negativeText("OK")
                    .onNegative((dialog, which) -> new MaterialDialog.Builder(mContext)
                            .title("Debug")
                            .content(details.purchaseInfo.responseData)
                            .negativeText("OK")
                            .onNegative((dialog13, which13) -> {

                                if (billingProcessor.isValidTransactionDetails(details)) {
                                    new MaterialDialog.Builder(mContext)
                                            .title("Debug")
                                            .content("Details are valid")
                                            .negativeText("OK")
                                            .onNegative((dialog12, which12) -> {

                                            })
                                            .show();
                                } else {
                                    new MaterialDialog.Builder(mContext)
                                            .title("Debug")
                                            .content("Details are not valid")
                                            .negativeText("OK")
                                            .onNegative((dialog1, which1) -> {

                                            })
                                            .show();
                                }


                            })
                            .show())
                    .show();

            if (productId != null && productId.equals(STORAGE_SUBSCRIPTION_LEVEL_THREE) && billingProcessor.isValidTransactionDetails(details)) {
                PrefsManager.Instance().setSubscription(STORAGE_SUBSCRIPTION_LEVEL_THREE);
                MuzikoSubscription subscription = new MuzikoSubscription(details.productId, details.orderId, details.purchaseToken, details.purchaseTime.getTime(), details.purchaseInfo);
                FirebaseManager.Instance().saveSubscription(subscription);
                new MaterialDialog.Builder(mContext).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Subscription Upgrade").content("Congratulations, You purchased level three").negativeText("OK").show();
            }

            mContext.sendBroadcast(new Intent(AppController.INTENT_PREMIUM_CHANGED));

            if (mListener != null) {
                mListener.onPurchased();
            }
        }

        @Override
        public void onPurchaseHistoryRestored() {
		    /*
		     * Called when purchase history was restored and the list of all owned PRODUCT ID's
             * was loaded from Google Play
             */

            List<String> products = billingProcessor.listOwnedProducts();
            if (products != null && products.indexOf(STORAGE_SUBSCRIPTION_LEVEL_THREE) != -1) {
                PrefsManager.Instance().setSubscription(STORAGE_SUBSCRIPTION_LEVEL_THREE);
            }

            mContext.sendBroadcast(new Intent(AppController.INTENT_PREMIUM_CHANGED));

            if (mListener != null) {
                mListener.onPremiumChanged();
            }

            //billingProcessor.consumePurchase(PRODUCT_ID);
        }

        @Override
        public void onBillingError(int errorCode, Throwable error) {
            String msg = "Billing error!";

            switch (errorCode) {

                case Constants.BILLING_RESPONSE_RESULT_OK:                      // 0    Success
                    return;

                case Constants.BILLING_RESPONSE_RESULT_USER_CANCELED:           // 1    User pressed back or canceled a dialog
                case Constants.BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE:     // 2    Network connection is down
                case Constants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:     // 3    Billing API version is not supported for the type requested
                case Constants.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:        // 4    Requested product is not available for purchase
                case Constants.BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:         // 5    Invalid arguments provided to the API. This error can also indicate that the application was not correctly signed or properly set up for In-app Billing in Google Play, or does not have the necessary permissions in its manifest
                case Constants.BILLING_RESPONSE_RESULT_ERROR:                   // 6    Fatal error during the API action
                case Constants.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:      // 7    Failure to purchase since item is already owned
                case Constants.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED:          // 8    Failure to consume since item is not owned
                    break;

                case Constants.BILLING_ERROR_FAILED_LOAD_PURCHASES: //100;
                    msg = "Unable to load purchases!";
                    break;

                case Constants.BILLING_ERROR_FAILED_TO_INITIALIZE_PURCHASE: //101;
                    msg = "Unable to initialize purchases!";
                    break;

                case Constants.BILLING_ERROR_INVALID_SIGNATURE: //102;
                    break;

                case Constants.BILLING_ERROR_LOST_CONTEXT: //103;
                    break;

                case Constants.BILLING_ERROR_INVALID_MERCHANT_ID: //104;
                    break;

                case Constants.BILLING_ERROR_OTHER_ERROR: //110;
                    if (billingProcessor.loadOwnedPurchasesFromGoogle() && billingProcessor.isPurchased(STORAGE_SUBSCRIPTION_LEVEL_THREE)) {
                        // https://github.com/anjlab/android-inapp-billing-v3/issues/156
                        //Promo code purchase
                        PrefsManager.Instance().setSubscription(STORAGE_SUBSCRIPTION_LEVEL_THREE);
                    }
                    break;

                case Constants.BILLING_ERROR_CONSUME_FAILED: //111;
                    break;

                case Constants.BILLING_ERROR_SKUDETAILS_FAILED: //112;
                    break;

            }
            //Utils.alert(MainActivity.this, getString(R.string.app_name), msg, null);

            mContext.sendBroadcast(new Intent(AppController.INTENT_PREMIUM_CHANGED));

            if (mListener != null) {
                mListener.onPremiumChanged();
            }
        }

        @Override
        public void onBillingInitialized() {
            /*
		     * Called when BillingProcessor was initialized and it's ready to purchase
             */
            boolean isAvailable = BillingProcessor.isIabServiceAvailable(mContext);
            if (isAvailable) {
                billingProcessor.subscribe(mContext, STORAGE_SUBSCRIPTION_LEVEL_THREE);
            } else {
                Utils.alert(mContext, mContext.getString(R.string.app_name), "In-app billing not available!", null);
            }

            onPurchaseHistoryRestored();
        }
    }
}
