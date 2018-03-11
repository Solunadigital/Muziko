package com.muziko.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.muziko.R;
import com.muziko.billing.Subscriptions.StorageSubscriptionLevelOne;
import com.muziko.billing.Subscriptions.StorageSubscriptionLevelThree;
import com.muziko.billing.Subscriptions.StorageSubscriptionLevelTwo;
import com.muziko.billing.Subscriptions.StorageSubscriptionLevelUnlimited;
import com.muziko.billing.Subscriptions.StorageSubscriptionsGet;
import com.muziko.common.models.QueueItem;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.models.MuzikoSubscriptionType;

import java.util.ArrayList;

public class SubscriptionUpgradeActivity extends BaseActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private TextView currentPlanLimitText;
    private TextView currentPlanNameText;
    private LinearLayout planOneLayout;
    private LinearLayout planTwoLayout;
    private LinearLayout planThreeLayout;
    private LinearLayout planFourLayout;
    private TextView planOneLimitText;
    private TextView planOneNameText;
    private TextView planTwoLimitText;
    private TextView planTwoNameText;
    private TextView planThreeLimitText;
    private TextView planThreeNameText;
    private TextView planFourLimitText;
    private TextView planFourNameText;
    private StorageSubscriptionsGet storageSubscriptionsGet;
    private StorageSubscriptionLevelOne subscriptionLevelOne;
    private StorageSubscriptionLevelTwo subscriptionLevelTwo;
    private StorageSubscriptionLevelThree subscriptionLevelThree;
    private StorageSubscriptionLevelUnlimited subscriptionLevelUnlimited;
    private MuzikoSubscriptionType currentSub;
    private boolean alreadyShownMultipleWarning = false;
    private ArrayList<String> subscriptionList = new ArrayList<>();
    private WeakHandler handler = new WeakHandler();
    private final Runnable checkSubs =
            new Runnable() {
                @Override
                public void run() {
                    storageSubscriptionsGet.getSubscriptions();
                    handler.postDelayed(this, 2000);
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_subscription_upgrade);
        findViewsById();

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Upgrade Limit");

        currentSub = FirebaseManager.Instance().getSubscriptionType();

        currentPlanLimitText.setText(String.valueOf(currentSub.getSongLimit()));
        currentPlanNameText.setText(currentSub.getSubscriptionName());

        planOneLimitText.setText(String.valueOf(FirebaseManager.Instance().getLevelOneSubscriptionType().getSongLimit()));
        planOneNameText.setText(FirebaseManager.Instance().getLevelOneSubscriptionType().getSubscriptionName());

        planTwoLimitText.setText(String.valueOf(FirebaseManager.Instance().getLevelTwoSubscriptionType().getSongLimit()));
        planTwoNameText.setText(FirebaseManager.Instance().getLevelTwoSubscriptionType().getSubscriptionName());

        planThreeLimitText.setText(String.valueOf(FirebaseManager.Instance().getLevelThreeSubscriptionType().getSongLimit()));
        planThreeNameText.setText(FirebaseManager.Instance().getLevelThreeSubscriptionType().getSubscriptionName());

        planFourLimitText.setText("Unlimited");
        planFourNameText.setText(FirebaseManager.Instance().getLevelFourSubscriptionType().getSubscriptionName());

        planOneLayout.setOnClickListener(this);
        planTwoLayout.setOnClickListener(this);
        planThreeLayout.setOnClickListener(this);
        planFourLayout.setOnClickListener(this);

        storageSubscriptionsGet = new StorageSubscriptionsGet(SubscriptionUpgradeActivity.this, subs -> {
            currentSub = FirebaseManager.Instance().getSubscriptionType();
            subscriptionList.clear();
            subscriptionList.addAll(subs);
            updateSubscriptionViews(subscriptionList);
            if (subs.size() > 1 && !alreadyShownMultipleWarning) {
                if (!this.isFinishing()) {
                    alreadyShownMultipleWarning = true;
                    AppController.Instance().showMultipleSubscriptionsDialog(SubscriptionUpgradeActivity.this, subs);
                }
            }
        });
        storageSubscriptionsGet.initBillProcessor();

        handler.postDelayed(checkSubs, 4000);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        storageSubscriptionsGet.destroy();

        if (subscriptionLevelOne != null) {
            subscriptionLevelOne.destroy();
        }
        if (subscriptionLevelTwo != null) {
            subscriptionLevelTwo.destroy();
        }
        if (subscriptionLevelThree != null) {
            subscriptionLevelThree.destroy();
        }
        if (subscriptionLevelUnlimited != null) {
            subscriptionLevelUnlimited.destroy();
        }
    }

    private void updateSubscriptionViews(ArrayList<String> subs) {
        if (subs.contains(MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_ONE)) {
            planOneLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_black_percent_70));
        }

        if (subs.contains(MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_TWO)) {
            planTwoLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_black_percent_70));
        }

        if (subs.contains(MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_THREE)) {
            planThreeLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_black_percent_70));
        }

        if (subs.contains(MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_UNLIMITED)) {
            planFourLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_black_percent_70));
        }
    }

    private void findViewsById() {

        toolbar = findViewById(R.id.toolbar);
        currentPlanLimitText = findViewById(R.id.currentPlanLimitText);
        currentPlanNameText = findViewById(R.id.currentPlanNameText);
        planOneLayout = findViewById(R.id.planOneLayout);
        planOneLimitText = findViewById(R.id.planOneLimitText);
        planOneNameText = findViewById(R.id.planOneNameText);
        planTwoLayout = findViewById(R.id.planTwoLayout);
        planTwoLimitText = findViewById(R.id.planTwoLimitText);
        planTwoNameText = findViewById(R.id.planTwoNameText);
        planThreeLayout = findViewById(R.id.planThreeLayout);
        planThreeLimitText = findViewById(R.id.planThreeLimitText);
        planThreeNameText = findViewById(R.id.planThreeNameText);
        planFourLayout = findViewById(R.id.planFourLayout);
        planFourLimitText = findViewById(R.id.planFourLimitText);
        planFourNameText = findViewById(R.id.planFourNameText);
    }

    private void showAlreadySubscribedDialog() {
        new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue)
                .title("Storage Subscription")
                .content("Already subscribed to this plan")
                .negativeText("OK")
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.subscriptions_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.settings_showtracks:
                ArrayList<String> cloudStrings = new ArrayList<>();
                for (QueueItem queueItem : FirebaseManager.Instance().getFirebaseFavTracks()) {
                    cloudStrings.add("Favs - " + queueItem.getTitle());
                }
                for (QueueItem queueItem : FirebaseManager.Instance().getFirebaseLibraryTracks()) {
                    cloudStrings.add("Library - " + queueItem.getTitle());
                }
                for (QueueItem queueItem : FirebaseManager.Instance().getFirebasePlaylistTracks()) {
                    cloudStrings.add("Playlists - " + queueItem.getTitle());
                }
                new MaterialDialog.Builder(this)
                        .title("Cloud Tracks")
                        .items(cloudStrings)
                        .negativeText("OK")
                        .show();

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        if (v == planOneLayout) {
            if (currentSub.getSubscriptionTypeID().equals(MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_ONE)) {
                showAlreadySubscribedDialog();
            } else {
                subscriptionLevelOne = new StorageSubscriptionLevelOne(SubscriptionUpgradeActivity.this, new StorageSubscriptionLevelOne.onUpdatedListener() {
                    @Override
                    public void onPremiumChanged() {
                        updateSubscriptionViews(subscriptionList);
                    }

                    @Override
                    public void onPurchased() {
                        updateSubscriptionViews(subscriptionList);
                    }
                });
                subscriptionLevelOne.initBillProcessor();
            }
        } else if (v == planTwoLayout) {
            if (currentSub.getSubscriptionTypeID().equals(MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_TWO)) {
                showAlreadySubscribedDialog();
            } else {
                subscriptionLevelTwo = new StorageSubscriptionLevelTwo(SubscriptionUpgradeActivity.this, new StorageSubscriptionLevelTwo.onUpdatedListener() {
                    @Override
                    public void onPremiumChanged() {
                        updateSubscriptionViews(subscriptionList);
                    }

                    @Override
                    public void onPurchased() {
                        updateSubscriptionViews(subscriptionList);
                    }
                });
                subscriptionLevelTwo.initBillProcessor();
            }
        } else if (v == planThreeLayout) {
            if (currentSub.getSubscriptionTypeID().equals(MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_THREE)) {
                showAlreadySubscribedDialog();
            } else {
                subscriptionLevelThree = new StorageSubscriptionLevelThree(SubscriptionUpgradeActivity.this, new StorageSubscriptionLevelThree.onUpdatedListener() {
                    @Override
                    public void onPremiumChanged() {
                        updateSubscriptionViews(subscriptionList);
                    }

                    @Override
                    public void onPurchased() {
                        updateSubscriptionViews(subscriptionList);
                    }
                });
                subscriptionLevelThree.initBillProcessor();
            }
        } else if (v == planFourLayout) {
            if (currentSub.getSubscriptionTypeID().equals(MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_UNLIMITED)) {
                showAlreadySubscribedDialog();
            } else {
                subscriptionLevelUnlimited = new StorageSubscriptionLevelUnlimited(SubscriptionUpgradeActivity.this, new StorageSubscriptionLevelUnlimited.onUpdatedListener() {
                    @Override
                    public void onPremiumChanged() {
                        updateSubscriptionViews(subscriptionList);
                    }

                    @Override
                    public void onPurchased() {
                        updateSubscriptionViews(subscriptionList);
                    }
                });
                subscriptionLevelUnlimited.initBillProcessor();
            }
        }

    }
}
