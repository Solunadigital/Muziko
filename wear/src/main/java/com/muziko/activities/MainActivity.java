package com.muziko.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableRecyclerView;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.muziko.PlayerConstants;
import com.muziko.R;
import com.muziko.adapters.MainMenuAdapter;
import com.muziko.common.CommonConstants;
import com.muziko.common.events.buswear.CustomObject;
import com.muziko.common.events.buswear.GotUpdateEvent;
import com.muziko.common.events.buswear.WearActionEvent;
import com.muziko.helpers.OffsettingHelper;
import com.muziko.helpers.ShapeWear;
import com.muziko.models.MainMenuModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import pl.tajchert.buswear.EventBus;

public class MainActivity extends WearableActivity implements MainMenuAdapter.ItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private ArrayList<MainMenuModel> mainMenuModels;
    private WearableRecyclerView wearableRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewsById();

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

        mainMenuModels = new ArrayList<>();
        mainMenuModels.add(new MainMenuModel(R.string.albums, R.drawable.album));
        mainMenuModels.add(new MainMenuModel(R.string.artists, R.drawable.artist));
        mainMenuModels.add(new MainMenuModel(R.string.songs, R.drawable.songs));
        mainMenuModels.add(new MainMenuModel(R.string.playlists, R.drawable.playlist));
        mainMenuModels.add(new MainMenuModel(R.string.now_playing, R.drawable.now_playing));

        MainMenuAdapter mainMenuAdapter = new MainMenuAdapter(MainActivity.this, mainMenuModels);
        wearableRecyclerView.setAdapter(mainMenuAdapter);

        mainMenuAdapter.setListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault(this).register(this);
        if (PlayerConstants.QUEUE_LIST.size() == 0) {
            EventBus.getDefault(MainActivity.this).postRemote(new WearActionEvent(CommonConstants.ACTION_WEAR_UPDATE, 0));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault(this).unregister(this);
    }

    private void findViewsById() {
        wearableRecyclerView = (WearableRecyclerView) findViewById(R.id.mainmenuList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CustomObject customObjectReceived) {
        Toast.makeText(MainActivity.this, "Object: " + customObjectReceived.getName(), Toast.LENGTH_SHORT).show();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGotUpdateEvent(GotUpdateEvent gotUpdateEvent) {

        SuperToast superToast = new SuperToast(this)
                .setText("Got it")
                .setDuration(Style.DURATION_SHORT)
                .setFrame(Style.FRAME_STANDARD)
                .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_LIGHT_BLUE))
                .setAnimations(Style.ANIMATIONS_SCALE);
        superToast.show();
    }

    @Override
    public void onItemSelected(int position) {
        EventBus.getDefault(MainActivity.this).postRemote(new CustomObject(String.valueOf(position)));

        switch (position) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                Intent activityIntent = new Intent(MainActivity.this, SongsActivity.class);
                startActivity(activityIntent);
                break;
            case 3:
                break;
            case 4:
                Intent nowPlayingyIntent = new Intent(MainActivity.this, NowPlayingActivity.class);
                startActivity(nowPlayingyIntent);
                break;
        }
    }
}
