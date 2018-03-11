package com.muziko.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.activities.SubscriptionUpgradeActivity;
import com.muziko.manager.FirebaseManager;

/**
 * Created by dev on 22/08/2016.
 */
public class UpgradeStorage implements View.OnClickListener {

    private Activity mActivity;
    private AlertDialog upgradeStorageDialog = null;
    private TextView cancelText;
    private TextView upgradeText;
    private TextView upgradeMessageText;

    public void open(Activity activity) {
        this.mActivity = activity;


        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_upgrade_storage, null, false);
        cancelText = view.findViewById(R.id.cancelText);
        upgradeText = view.findViewById(R.id.upgradeText);
        upgradeMessageText = view.findViewById(R.id.upgradeMessageText);
        double progress = (100.0 * FirebaseManager.Instance().getFirebaseTrackCount() / FirebaseManager.Instance().getSubscriptionType().getSongLimit());
        int storageUsed = (int) progress;
        upgradeMessageText.setText("Your cloud storage is " + storageUsed + "% (" + FirebaseManager.Instance().getFirebaseTrackCount() + "/" + FirebaseManager.Instance().getSubscriptionType().getSongLimit() + ") full. Please upgrade to a plan that suits your needs");
        cancelText.setOnClickListener(this);
        upgradeText.setOnClickListener(this);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.mActivity);
        dialogBuilder.setView(view);
        upgradeStorageDialog = dialogBuilder.create();
        upgradeStorageDialog.show();

        upgradeStorageDialog.show();
    }

    public void close() {

        if (upgradeStorageDialog != null) {
            upgradeStorageDialog.dismiss();
        }
    }


    @Override
    public void onClick(View v) {
        if (v == upgradeText) {
            upgradeStorageDialog.dismiss();
            Intent activityIntent = new Intent(mActivity, SubscriptionUpgradeActivity.class);
            mActivity.startActivity(activityIntent);
            mActivity.overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        } else {
            upgradeStorageDialog.dismiss();
        }
    }
}
