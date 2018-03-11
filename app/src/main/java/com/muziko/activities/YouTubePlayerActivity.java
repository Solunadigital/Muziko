package com.muziko.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.muziko.R;
import com.muziko.controls.YouTubeFragment;
import com.muziko.manager.AppController;

public class YouTubePlayerActivity extends BaseActivity {

    private FragmentManager mFragmentManager;
    private String videoId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);

        setContentView(R.layout.activity_you_tube_player);
        mFragmentManager = getSupportFragmentManager();

        videoId = getIntent().getStringExtra("videoId");

        if (videoId == null) {
            AppController.toast(this, "Video not found!");
            finish();
            return;
        }

        final YouTubeFragment youTubeFragment = YouTubeFragment.newInstance(videoId);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.addToBackStack("youTubeFragment");
        ft.add(R.id.mainLayout, youTubeFragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return false;   //super.onOptionsItemSelected(item);
        }
    }
}
