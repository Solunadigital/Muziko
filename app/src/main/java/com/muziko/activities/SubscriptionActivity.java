package com.muziko.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.badoo.mobile.util.WeakHandler;
import com.muziko.R;
import com.muziko.billing.Subscriptions.StorageSubscriptionsGet;
import com.muziko.common.models.QueueItem;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.models.MuzikoSubscriptionType;

import java.util.ArrayList;

public class SubscriptionActivity extends BaseActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private TextView usageText;
    private FrameLayout changePlanLayout;
    private MuzikoSubscriptionType currentSub;
    private StorageSubscriptionsGet storageSubscriptionsGet;
    private WeakHandler handler = new WeakHandler();
    private final Runnable checkSubs =
            new Runnable() {
                @Override
                public void run() {
                    storageSubscriptionsGet.getSubscriptions();
                    handler.postDelayed(this, 2000);
                }
            };
    private boolean alreadyShownMultipleWarning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_subscription);
        findViewsById();

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Subscription");
        currentSub = FirebaseManager.Instance().getSubscriptionType();
        updateSubscriptionViews();

        storageSubscriptionsGet = new StorageSubscriptionsGet(SubscriptionActivity.this, subs -> {
            currentSub = FirebaseManager.Instance().getSubscriptionType();
            updateSubscriptionViews();
            if (subs.size() > 1 && !alreadyShownMultipleWarning) {
                if (!this.isFinishing()) {
                    alreadyShownMultipleWarning = true;
                    AppController.Instance().showMultipleSubscriptionsDialog(SubscriptionActivity.this, subs);
                }
            }
        });
        storageSubscriptionsGet.initBillProcessor();
        handler.postDelayed(checkSubs, 4000);

        usageText.setText(FirebaseManager.Instance().getFirebaseTrackCount() + " of " + currentSub.getSongLimit());

        changePlanLayout.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        storageSubscriptionsGet.destroy();
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

    private void updateSubscriptionViews() {
        usageText.setText(FirebaseManager.Instance().getFirebaseTrackCount() + " of " + currentSub.getSongLimit());
    }

    private void findViewsById() {

        toolbar = findViewById(R.id.toolbar);
        usageText = findViewById(R.id.usageText);
        changePlanLayout = findViewById(R.id.changePlanLayout);
    }

    @Override
    public void onClick(View v) {

        if (v == changePlanLayout) {

            Intent activityIntent = new Intent(SubscriptionActivity.this, SubscriptionUpgradeActivity.class);
            startActivity(activityIntent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        }

    }
}
