package com.muziko.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.badoo.mobile.util.WeakHandler;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.adapter.ActivityPagerAdapter;
import com.muziko.callbacks.ActivityCallback;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.AdvancedSearchButton;
import com.muziko.controls.MiniPlayer;
import com.muziko.database.TrackRealmHelper;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RecentActivity extends BaseActivity implements ViewPager.OnPageChangeListener, SearchView.OnCloseListener, View.OnClickListener, SearchView.OnQueryTextListener {

	private final WeakHandler handler = new WeakHandler();
	public ActivityCallback callbackPlayed;
	public ActivityCallback callbackMost;
	public ActivityCallback callbackAdded;
	private MenuItem menuItemView;
	private SlidingUpPanelLayout slidingUpPanelLayout;
	private MiniPlayer miniPlayer;
	private MainReceiver mainReceiver;
	private boolean alreadyResumed = false;
	private int TAB_VALUE = 0;
	private SmartTabLayout tabs;
	private ViewPager pager;
	private MenuItem menuItemSearch;
	private MenuItem menuItemAdvancedSearch;
	private MenuItem menuItemReset;
	private CoordinatorLayout coordinatorlayout;
	private MenuItem menuItemStorage;
	private MenuItem menuItemStorageAll;
	private MenuItem menuItemStorageInternal;
	private MenuItem menuItemStorageSD;
	private Toolbar toolbar;
	private InterstitialAd mInterstitialAd;
	private AdListener mAdlistener;
	private AppBarLayout appBarLayout;
	private ImageButton advancedSearch;
	private int advancedSearchInset;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recent);
		findViewsById();

		advancedSearchInset = toolbar.getContentInsetStartWithNavigation();
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle("Activity");

		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

		ActivityPagerAdapter adapter = new ActivityPagerAdapter(getSupportFragmentManager());
		pager.setOffscreenPageLimit(3);
		pager.setAdapter(adapter);
		tabs.setViewPager(pager);
		pager.addOnPageChangeListener(this);

		setupMainPlayer();

		EventBus.getDefault().register(this);
	}

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
        unregister();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
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
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        PlayerConstants.QUEUE_TYPE = 0;
//		updateResetMenu();
        updateStorageMenu();
        updateStorage();

        register();
        mainUpdate();

        if (pager != null) {
            pager.setCurrentItem(PrefsManager.Instance().getLastRecentActivityTab());
        }

        alreadyResumed = true;
    }

    public void fastScrolling(boolean start) {
        if (start) {
            appBarLayout.setExpanded(false, true);
        }
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

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onRefreshEvent(RefreshEvent event) {

		mainUpdate();
	}

    private void findViewsById() {
        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolbar);
        pager = findViewById(R.id.vpPager);
        tabs = findViewById(R.id.tabs);
    }

    private void setupMainPlayer() {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        final LinearLayout mainPlayerLayout = findViewById(R.id.mainPlayerLayout);

        miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout);
    }

    private void mainUpdate() {
        miniPlayer.updateUI();

        if (callbackPlayed != null)
            callbackPlayed.onReload(this);

        if (callbackMost != null)
            callbackMost.onReload(this);

        if (callbackAdded != null)
            callbackAdded.onReload(this);

        alreadyResumed = true;
    }

	@Override
	public void onStart() {
		super.onStart();

	}

	private void updateStorageMenu() {
		if (menuItemStorage != null) {
			Activity act = this;

            if (PrefsManager.Instance().getStorageViewType() == 0) {
                menuItemStorage.setTitle("All");
//				Drawable filterDrawable = ContextCompat.getDrawable(this, R.drawable.ic_filter_list_white_24dp);
//				final PorterDuffColorFilter colorFilter
//						= new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
//				filterDrawable.setColorFilter(colorFilter);
				menuItemStorage.setIcon(R.drawable.ic_filter_list_white_24dp);
				menuItemStorageAll.setChecked(true);
            } else if (PrefsManager.Instance().getStorageViewType() == 1) {
                menuItemStorage.setTitle("Internal Storage");
				menuItemStorage.setIcon(R.drawable.ic_storage_white_36dp);
				menuItemStorageInternal.setChecked(true);
			} else {
				menuItemStorage.setTitle("SD Card");
				menuItemStorage.setIcon(R.drawable.ic_sd_storage_white_36dp);
				menuItemStorageSD.setChecked(true);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		menuItemView = menu.findItem(R.id.activity_view);
		menuItemSearch = menu.findItem(R.id.activity_search);
		menuItemStorage = menu.findItem(R.id.storage_filter);
		menuItemStorageAll = menu.findItem(R.id.player_storage_all);
		menuItemStorageInternal = menu.findItem(R.id.player_storage_internal);
		menuItemStorageSD = menu.findItem(R.id.player_storage_sd);

		updateStorageMenu();

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//		updateResetMenu();
		updateStorageMenu();
		menuItemView.setVisible(false);

		if (menuItemSearch != null) {
			SearchView searchView = (SearchView) menuItemSearch.getActionView();
			AdvancedSearchButton advancedSearchButton = new AdvancedSearchButton();
			Resources resources = getResources();
			advancedSearch = advancedSearchButton.addButton(this, resources, searchView);
			advancedSearch.setOnClickListener(this);
			ActionBar.LayoutParams searchviewParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
			searchView.setLayoutParams(searchviewParams);
			searchView.setMaxWidth(Integer.MAX_VALUE);
			searchView.setQueryHint("Search...");
			searchView.setOnQueryTextListener(this);
			searchView.setOnSearchClickListener(this);
			searchView.setOnCloseListener(this);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;

			case R.id.activity_search:
				return true;

			case R.id.activity_sort_date_lastest:
			case R.id.activity_sort_date_earliest:
			case R.id.activity_sort_duration_largest:
			case R.id.activity_sort_duration_smallest:
			case R.id.activity_sort_atoz:
			case R.id.activity_sort_ztoa:
				if (callbackPlayed != null) {
					callbackPlayed.onFilterValue(item.getItemId());
				}
				if (callbackMost != null) {
					callbackMost.onFilterValue(item.getItemId());
				}
				if (callbackAdded != null) {
					callbackAdded.onFilterValue(item.getItemId());
				}
				return true;

			case R.id.player_storage_all:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);
                PrefsManager.Instance().setStorageViewType(0);
                updateStorageMenu();
				updateStorage();
				return true;

			case R.id.player_storage_internal:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);
                PrefsManager.Instance().setStorageViewType(1);
                updateStorageMenu();
				updateStorage();
				return true;

			case R.id.player_storage_sd:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);
                PrefsManager.Instance().setStorageViewType(2);
                updateStorageMenu();
				updateStorage();
				return true;

//			case R.id.activity_reset_count:
//				resetAll();
//				return true;

			case R.id.activity_play_songs:
				AppController.Instance().playAll();
				return true;

			case R.id.activity_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

			case R.id.activity_share:
				AppController.Instance().shareApp();
				return true;
			case R.id.sharing_wifi:
				Intent shareIntent =
						new Intent(RecentActivity.this, ShareWifiActivity.class);
				startActivity(shareIntent);
				return true;
			case R.id.activity_exit:
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
        PlayerConstants.QUEUE_TYPE = 0;

        if (callbackPlayed != null)
            callbackPlayed.onSearchQuery(newText);

        if (callbackMost != null)
            callbackMost.onSearchQuery(newText);

        if (callbackAdded != null)
            callbackAdded.onSearchQuery(newText);

        return false;
    }

    @Override
    public boolean onClose() {
        advancedSearch.setVisibility(View.GONE);
        toolbar.setContentInsetStartWithNavigation(advancedSearchInset);
        return false;
    }

//	@Override
//	public boolean onCanGoBack() {
//		if (searchView != null && !searchView.isIconified()) {
//			searchView.setIconified(true);
//			return false;
//		}
//
//		return true;
//	}

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

	private void resetAll() {
		if (TrackRealmHelper.resetPlayedCount()) {
			if (callbackMost != null)
				callbackMost.onReload(this);
		} else {
            AppController.toast(this, "Unable to reset play counts!");
        }
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(final int position) {


		handler.postDelayed(() -> {
			TAB_VALUE = position;
            PrefsManager.Instance().setLastRecentActivityTab(TAB_VALUE);
            updateStorage();

		}, 400);


	}

	@Override
    public void onPageScrollStateChanged(int state) {

	}

	private void updateResetMenu() {
		if (menuItemReset != null)
			menuItemReset.setVisible(TAB_VALUE == MyApplication.ACTIVITY_PAGE_MOST);
	}

	private void updateStorage() {
		if (callbackPlayed != null)
			callbackPlayed.onStorageChanged();

		if (callbackMost != null)
			callbackMost.onStorageChanged();

		if (callbackAdded != null)
			callbackAdded.onStorageChanged();
	}

	public void enableTabs(boolean b) {
		Utils.enableDisableViewGroup(tabs, b);
	}

	private void register() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(AppController.INTENT_EXIT);
		filter.addAction(AppController.INTENT_CLEAR);

		filter.addAction(AppController.INTENT_TRACK_EDITED);
		filter.addAction(AppController.INTENT_TRACK_SEEKED);
		filter.addAction(AppController.INTENT_QUEUE_STOPPED);
		filter.addAction(AppController.INTENT_TRACK_SHUFFLE);
		filter.addAction(AppController.INTENT_TRACK_REPEAT);
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
