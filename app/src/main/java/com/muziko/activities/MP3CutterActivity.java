package com.muziko.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.badoo.mobile.util.WeakHandler;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.muziko.R;
import com.muziko.adapter.Mp3ViewPagerAdapter;
import com.muziko.callbacks.Mp3Callback;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.AdvancedSearchButton;
import com.muziko.controls.MiniPlayer;
import com.muziko.events.BufferingEvent;
import com.muziko.fragments.CutTones;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.mediaplayer.PlayerConstants;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MP3CutterActivity extends BaseActivity implements SearchView.OnCloseListener, SearchView.OnQueryTextListener, View.OnClickListener {

	private final WeakHandler handler = new WeakHandler();
	public Mp3Callback callbackSelect;
	public Mp3Callback callbackTones;
	private SlidingUpPanelLayout slidingUpPanelLayout;
	private Toolbar toolbar;
	private MiniPlayer miniPlayer;
	private MainReceiver mainReceiver;
	private boolean alreadyResumed = false;
	private ViewPager pager;
	private TabLayout tabs;
	private AdView mAdView;
	private CoordinatorLayout coordinatorlayout;
	private MenuItem menuItemSearch;
	private InterstitialAd mInterstitialAd;
	private AdListener mAdlistener;
	private ImageButton advancedSearch;
	private int advancedSearchInset;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_mp3_cutter);
		overridePendingTransition(R.anim.slide_up, R.anim.fade_out);

        toolbar = findViewById(R.id.toolbar);
        advancedSearchInset = toolbar.getContentInsetStartWithNavigation();
		toolbar.setTitle("Mp3 Cutter");
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		findViewsById();

		pager.setAdapter(new Mp3ViewPagerAdapter(getSupportFragmentManager()));
		tabs.setupWithViewPager(pager);
		tabs.getTabAt(0).setIcon(R.drawable.cutter_select);
		tabs.getTabAt(1).setIcon(R.drawable.cutter_tones);
		tabs.setTabGravity(TabLayout.GRAVITY_FILL);

		setupMainPlayer();

		EventBus.getDefault().register(this);
	}

    @Override
    public void onDestroy() {

        handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
        unregister();
        stopPlay();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

	@Override
	public void onBackPressed() {

		if (slidingUpPanelLayout != null &&
				(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
			miniPlayer.close();
			miniPlayer.open();
		} else if (menuItemSearch != null) {
			SearchView searchView = (SearchView) menuItemSearch.getActionView();

			if (searchView != null && !searchView.isIconified()) {
				searchView.setIconified(true);
				return;
			}
			super.onBackPressed();
		} else {
			finish();
		}
	}

    @Override
    public void onPause() {
        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        unregister();
        if (CutTones.mp != null && CutTones.mp.isPlaying()) {
            stopPlay();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        mainUpdate();
        register();

        alreadyResumed = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBufferingEvent(BufferingEvent event) {

        if (miniPlayer != null) {
            miniPlayer.showBufferingMessage(event.getMessage(), event.isClose());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkEvent networkEvent) {

        if (miniPlayer != null) {
            miniPlayer.updateUI();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressEvent(ProgressEvent event) {

        miniPlayer.updateProgress(event.getProgress(), event.getDuration());
    }

    private void onLayoutChanged(Float bottomMargin) {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);

        Resources resources = getResources();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentlayout.getLayoutParams();
        params.setMargins(0, 0, 0, Utils.toPixels(resources, bottomMargin));
        contentlayout.requestLayout();
    }

	private void setupMainPlayer() {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        final LinearLayout mainPlayerLayout = findViewById(R.id.mainPlayerLayout);

		miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout);
	}

	private void findViewsById() {
        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        pager = findViewById(R.id.vpPager);
        // Bind the tabs to the ViewPager
        tabs = findViewById(R.id.tabs);


	}

	private void mainUpdate() {
		miniPlayer.updateUI();

		alreadyResumed = true;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mp3cutter_menu, menu);
		menuItemSearch = menu.findItem(R.id.mp3_search);
		return super.onCreateOptionsMenu(menu);
	}


	public boolean onPrepareOptionsMenu(Menu menu) {
		{
			if (menuItemSearch == null) return false;
			SearchView searchView = (SearchView) menuItemSearch.getActionView();
			AdvancedSearchButton advancedSearchButton = new AdvancedSearchButton();
			Resources resources = getResources();
			advancedSearch = advancedSearchButton.addButton(this, resources, searchView);
			advancedSearch.setOnClickListener(this);
			ActionBar.LayoutParams searchviewParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
			searchView.setLayoutParams(searchviewParams);
			searchView.setMaxWidth(Integer.MAX_VALUE);
			searchView.setQueryHint("Search song or artist");
			searchView.setOnQueryTextListener(this);
			searchView.setOnSearchClickListener(this);
			searchView.setOnCloseListener(this);

			return super.onPrepareOptionsMenu(menu);
		}
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.mp3_play_songs:
                AppController.Instance().playAll();
                return true;

            case R.id.mp3_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.mp3_share:
                AppController.Instance().shareApp();
                return true;
			case R.id.sharing_wifi:
				Intent shareIntent =
						new Intent(MP3CutterActivity.this, ShareWifiActivity.class);
				startActivity(shareIntent);
				return true;            case R.id.mp3_exit:
                AppController.Instance().exit();
                return true;
            default:
                return false;   //super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (callbackSelect != null) {
			callbackSelect.onSearch(newText);
		}

		if (callbackTones != null) {
			callbackTones.onSearch(newText);
		}

		PlayerConstants.QUEUE_TYPE = 0;

		return false;
	}

	@Override
	public boolean onClose() {
		advancedSearch.setVisibility(View.GONE);
		toolbar.setContentInsetStartWithNavigation(advancedSearchInset);
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v == advancedSearch) {
			Intent intent = new Intent(this, SearchActivity.class);
			startActivity(intent);
		} else {
			advancedSearch.setVisibility(View.VISIBLE);
			toolbar.setContentInsetStartWithNavigation(0);
		}
	}

	private void stopPlay() {
		if (CutTones.mp != null) {
			CutTones.mp.stop();
			CutTones.mp.reset();
		}
	}

	private void register() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(AppController.INTENT_EXIT);
		filter.addAction(AppController.INTENT_CLEAR);

		filter.addAction(AppController.INTENT_TRACK_EDITED);
		filter.addAction(AppController.INTENT_TRACK_SEEKED);
		filter.addAction(AppController.INTENT_QUEUE_STOPPED);
		filter.addAction(AppController.INTENT_QUEUE_CHANGED);
		filter.addAction(AppController.INTENT_QUEUE_CLEARED);

		mainReceiver = new MainReceiver();
		registerReceiver(mainReceiver, filter);
	}

	private void unregister() {

		if (mainReceiver != null) {
			unregisterReceiver(mainReceiver);
			mainReceiver = null;
		}
	}

	private class MainReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String action = intent.getAction();

				switch (action) {
					case AppController.INTENT_TRACK_SEEKED:
						mainUpdate();
						break;
					case AppController.INTENT_QUEUE_CHANGED:
						mainUpdate();
						break;
					case AppController.INTENT_QUEUE_STOPPED:
                        miniPlayer.layoutMiniPlayer();
                        break;
					case AppController.INTENT_TRACK_REPEAT:
						mainUpdate();
						break;
					case AppController.INTENT_TRACK_SHUFFLE:
						mainUpdate();
						break;
					case AppController.INTENT_QUEUE_CLEARED:
                        miniPlayer.layoutMiniPlayer();

						break;
					case AppController.INTENT_CLEAR:

						finish();
						break;
					case AppController.INTENT_EXIT:

						finish();
						break;
					case AppController.INTENT_TRACK_EDITED:
						int index = intent.getIntExtra("index", -1);
						String tag = intent.getStringExtra("tag");
						QueueItem item = (QueueItem) intent.getSerializableExtra("item");
						if (item != null) {
							mainUpdate();
						}
						break;
				}
			}
		}
	}
}
