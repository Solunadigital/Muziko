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
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.PrefsManager;
import com.muziko.models.MuzikoSubscriptionType;

import java.util.ArrayList;
import java.util.List;

import static com.muziko.manager.MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_UNLIMITED;

/**
 * Created by dev on 20/10/2016.
 */

public class StorageSubscriptionsGet {

    private final String TAG = StorageSubscriptionsGet.class.getSimpleName();
    private final Activity mContext;
    private BillingProcessor billingProcessor;
    private onUpdatedListener mListener;

    // default constructor
    public StorageSubscriptionsGet(final Activity activity, onUpdatedListener listener) {
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

    public void getSubscriptions() {

        if (FirebaseManager.Instance().isSubscriptionsReady()) {
            ArrayList<String> subs = new ArrayList<>();
            boolean isAvailable = BillingProcessor.isIabServiceAvailable(mContext);
            if (isAvailable) {
                billingProcessor.loadOwnedPurchasesFromGoogle();
                List<String> mysubs = billingProcessor.listOwnedSubscriptions();
                subs = new ArrayList<>(mysubs);
            } else {
                Utils.alert(mContext, mContext.getString(R.string.app_name), "In-app billing not available!", null);
            }

            MuzikoSubscriptionType highestSub = FirebaseManager.Instance().getFreeSubscriptionType();
//            for (String sub : subs) {
//                MuzikoSubscriptionType muzikoSubscription = FirebaseManager.Instance().getSubscriptionByType(sub);
//                if (muzikoSubscription == null) continue;
//                if (muzikoSubscription.getSongLimit() > highestSub.getSongLimit()) {
//                    highestSub = muzikoSubscription;
//                }
//                TransactionDetails details = billingProcessor.getSubscriptionTransactionDetails(sub);
//                MuzikoSubscription subscription = new MuzikoSubscription(details.productId, details.orderId, details.purchaseToken, details.purchaseTime.getTime(), details.purchaseInfo);
//                FirebaseManager.Instance().saveSubscription(subscription);
//            }

            if (highestSub != null) {
                PrefsManager.Instance().setSubscription(highestSub.getSubscriptionTypeID());
            }

            if (mListener != null) {
                mListener.onGotSubscriptions(subs);
            }
        }
    }

    public interface onUpdatedListener {

        void onGotSubscriptions(ArrayList<String> subs);
    }

    private class PremiumListener implements BillingProcessor.IBillingHandler {
        @Override
        public void onProductPurchased(String productId, TransactionDetails details) {
            /*
             * Called when requested PRODUCT ID was successfully purchased
             */

            if (productId != null && productId.equals(MuzikoConstants.PRODUCT_ID) && billingProcessor.isValidTransactionDetails(details)) {
                PrefsManager.Instance().setPremium(true);
                new MaterialDialog.Builder(mContext).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Remove Ads").content("Congratulations, Ads have been removed.").negativeText("OK").show();
            }

            mContext.sendBroadcast(new Intent(AppController.INTENT_PREMIUM_CHANGED));

        }

        @Override
        public void onPurchaseHistoryRestored() {

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
                    if (billingProcessor.loadOwnedPurchasesFromGoogle() && billingProcessor.isPurchased(STORAGE_SUBSCRIPTION_LEVEL_UNLIMITED)) {
                        // https://github.com/anjlab/android-inapp-billing-v3/issues/156
                        //Promo code purchase
                        PrefsManager.Instance().setPremium(true);
                    }
                    break;

                case Constants.BILLING_ERROR_CONSUME_FAILED: //111;
                    break;

                case Constants.BILLING_ERROR_SKUDETAILS_FAILED: //112;
                    break;

            }
        }

        @Override
        public void onBillingInitialized() {
            /*
             * Called when BillingProcessor was initialized and it's ready to purchase
             */

            if (FirebaseManager.Instance().isSubscriptionsReady()) {
                ArrayList<String> subs = new ArrayList<>();
                boolean isAvailable = BillingProcessor.isIabServiceAvailable(mContext);
                if (isAvailable) {
                    billingProcessor.loadOwnedPurchasesFromGoogle();
                    List<String> mysubs = billingProcessor.listOwnedSubscriptions();
                    subs = new ArrayList<>(mysubs);
                } else {
                    Utils.alert(mContext, mContext.getString(R.string.app_name), "In-app billing not available!", null);
                }

                MuzikoSubscriptionType highestSub = FirebaseManager.Instance().getFreeSubscriptionType();
//                for (String sub : subs) {
//                    MuzikoSubscriptionType muzikoSubscription = FirebaseManager.Instance().getSubscriptionByType(sub);
//                    if (muzikoSubscription == null) continue;
//                    if (muzikoSubscription.getSongLimit() > highestSub.getSongLimit()) {
//                        highestSub = muzikoSubscription;
//                    }
//                    TransactionDetails details = billingProcessor.getSubscriptionTransactionDetails(sub);
//                    MuzikoSubscription subscription = new MuzikoSubscription(details.productId, details.orderId, details.purchaseToken, details.purchaseTime.getTime(), details.purchaseInfo);
//                    FirebaseManager.Instance().saveSubscription(subscription);
//                }

                if (highestSub != null) {
                    PrefsManager.Instance().setSubscription(highestSub.getSubscriptionTypeID());
                }

                if (mListener != null) {
                    mListener.onGotSubscriptions(subs);
                }
            }
        }
    }
}
