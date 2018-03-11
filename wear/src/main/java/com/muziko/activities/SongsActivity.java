package com.muziko.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.muziko.MuzikoWearApp;
import com.muziko.PlayerConstants;
import com.muziko.R;
import com.muziko.adapters.SongsAdapter;
import com.muziko.common.CommonConstants;
import com.muziko.common.controls.MuzikoArrayList;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.events.buswear.RequestQueueUpdateEvent;
import com.muziko.common.events.buswear.WearActionEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.OffsettingHelper;
import com.muziko.helpers.ShapeWear;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import pl.tajchert.buswear.EventBus;

public class SongsActivity extends WearableActivity implements SongsAdapter.ItemSelectedListener, View.OnClickListener {

    private final static String TAG = SongsActivity.class.getSimpleName();
    private SongsAdapter songsAdapter;
    private MuzikoArrayList<QueueItem> queueItems = new MuzikoArrayList<>();
    private ImageView gotoImage;
    private WearableRecyclerView wearableRecyclerView;
    private RelativeLayout emptyQueueLayout;
    private CircledImageView emptyQueueShuffleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_songs);
        findViewsById();

        gotoImage.setOnClickListener(this);
        emptyQueueShuffleButton.setOnClickListener(this);

        wearableRecyclerView.setHasFixedSize(true);
        wearableRecyclerView.setCenterEdgeItems(true);
        ShapeWear.initShapeWear(this);
        ShapeWear.setOnShapeChangeListener(screenShape -> {
            //Do your stuff here for example:
            switch (screenShape) {
                case RECTANGLE:
                    break;
                case ROUND:
                    OffsettingHelper offsettingHelper = new OffsettingHelper();
                    wearableRecyclerView.setOffsettingHelper(offsettingHelper);
                    break;
                case MOTO_ROUND:
                    //as it is special case of ROUND - cut at the bottom.
                    break;
            }
        });

        queueItems.addAll(PlayerConstants.QUEUE_LIST);
        songsAdapter = new SongsAdapter(SongsActivity.this, queueItems);
        wearableRecyclerView.setAdapter(songsAdapter);

        songsAdapter.setListener(this);

        EventBus.getDefault(this).register(this);

        if (PlayerConstants.QUEUE_LIST.size() == 0) {
            EventBus.getDefault(SongsActivity.this).postRemote(new RequestQueueUpdateEvent("Songs"));
        }
        showLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault(this).unregister(this);
    }

    private void findViewsById() {
        wearableRecyclerView = (WearableRecyclerView) findViewById(R.id.songsList);
        emptyQueueLayout = (RelativeLayout) findViewById(R.id.emptyQueueLayout);
        emptyQueueShuffleButton = (CircledImageView) findViewById(R.id.emptyQueueShuffleButton);
        gotoImage = (ImageView) findViewById(R.id.gotoImage);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {

        queueItems.clear();
        queueItems.addAll(PlayerConstants.QUEUE_LIST);
        songsAdapter.notifyDataSetChanged();
        showLayout();
    }

    private void showLayout() {
        if (PlayerConstants.QUEUE_LIST.size() == 0) {
            wearableRecyclerView.setVisibility(View.GONE);
            gotoImage.setVisibility(View.GONE);
            emptyQueueLayout.setVisibility(View.VISIBLE);
        } else {
            wearableRecyclerView.setVisibility(View.VISIBLE);
            gotoImage.setVisibility(View.VISIBLE);
            emptyQueueLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public void onItemClicked(int position) {
        MuzikoWearApp.wearPosition = position;
        EventBus.getDefault(SongsActivity.this).postRemote(new WearActionEvent(CommonConstants.ACTION_WEAR_PLAY, position));
    }

    @Override
    public void onItemLongClicked(int position) {
        MuzikoWearApp.wearPosition = position;
        Intent intent = new Intent(SongsActivity.this, MenuOneActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (v == emptyQueueShuffleButton) {
            EventBus.getDefault(SongsActivity.this).postRemote(new WearActionEvent(CommonConstants.ACTION_WEAR_SHUFFLE_ALL, 0));
        } else if (v == gotoImage) {
            wearableRecyclerView.scrollToPosition(PlayerConstants.QUEUE_INDEX);
        }
    }
}
