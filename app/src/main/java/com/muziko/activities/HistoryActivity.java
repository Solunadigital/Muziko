package com.muziko.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.badoo.mobile.util.WeakHandler;
import com.google.android.gms.ads.InterstitialAd;
import com.muziko.R;
import com.muziko.adapter.HistoryPagerAdapter;
import com.muziko.callbacks.SharingCallback;
import com.muziko.common.events.FirebaseSharesRefreshEvent;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.MiniPlayer;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import hugo.weaving.DebugLog;

import static com.muziko.MyApplication.ARG_DATA;

public class HistoryActivity extends BaseActivity implements ViewPager.OnPageChangeListener, SearchView.OnQueryTextListener {

    private final WeakHandler handler = new WeakHandler();
    public SharingCallback callbackReceived;
    public SharingCallback callbackSent;
    public SharingCallback callbackUnknown;
    private MenuItem menuItemView;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private MiniPlayer miniPlayer;
    private MainReceiver mainReceiver;
    private String TAB_VALUE = "";
    private TabLayout tabs;
    private ViewPager pager;
    private MenuItem menuItemSearch;
    private MenuItem menu_sort_sent;
    private MenuItem menu_sort_reverse;
    private CoordinatorLayout coordinatorlayout;
    private Toolbar toolbar;
    private InterstitialAd mInterstitialAd;
    private boolean reverseSort;
    private int sortId;
    private HistoryPagerAdapter adapter;
    private boolean fromNotification;
    private AppBarLayout appBarLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_history);
        findViewsById();

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(getString(R.string.my_history));

        fromNotification = getIntent().getBooleanExtra(ARG_DATA, false);

        adapter = new HistoryPagerAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(4);
        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);
        tabs.getTabAt(0).setIcon(R.drawable.history_tabs_sent);
        tabs.getTabAt(1).setIcon(R.drawable.history_tabs_received);
        tabs.getTabAt(2).setIcon(R.drawable.history_tabs_unknown);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        pager.addOnPageChangeListener(this);


        setupMainPlayer();

        EventBus.getDefault().register(this);

    }

    @Override
    public void onResume() {
        super.onResume();

        register();
        mainUpdate();

        if (pager != null) {
            if (fromNotification) {
                pager.setCurrentItem(1);
            } else {
                pager.setCurrentItem(0);
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
        EventBus.getDefault().unregister(this);
        unregister();
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
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFirebaseSharesRefreshEvent(FirebaseSharesRefreshEvent event) {

        if (callbackReceived != null) {
            callbackReceived.onReload();
        }
        if (callbackSent != null) {
            callbackSent.onReload();
        }

        if (callbackUnknown != null) {
            callbackUnknown.onReload();
        }


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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sharing_menu, menu);
        MenuItem menuItemFilter = menu.findItem(R.id.sharing_filter);
        menuItemSearch = menu.findItem(R.id.sharing_search);
        MenuItem menu_sort_title = menu.findItem(R.id.sharing_sort_title);
        MenuItem menu_sort_online = menu.findItem(R.id.sharing_sort_online);
        menu_sort_sent = menu.findItem(R.id.sharing_sort_sent);
        menu_sort_reverse = menu.findItem(R.id.reverse);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu_sort_sent.setChecked(true);
        menu_sort_reverse.setChecked(true);

        if (callbackReceived != null) {
            callbackReceived.onFilterValue(R.id.sharing_sort_sent, true);
        }
        if (callbackSent != null) {
            callbackSent.onFilterValue(R.id.sharing_sort_sent, true);
        }
        if (callbackUnknown != null) {
            callbackUnknown.onFilterValue(R.id.sharing_sort_sent, true);
        }

        if (menuItemSearch != null) {
            SearchView searchView = (SearchView) menuItemSearch.getActionView();
            searchView.setQueryHint("Search...");
            searchView.setOnQueryTextListener(this);
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

            case R.id.sharing_search:
                return true;

            case R.id.sharing_sort_sent:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                if (callbackReceived != null) {
                    callbackReceived.onFilterValue(item.getItemId(), reverseSort);
                }
                if (callbackSent != null) {
                    callbackSent.onFilterValue(item.getItemId(), reverseSort);
                }
                if (callbackUnknown != null) {
                    callbackUnknown.onFilterValue(item.getItemId(), reverseSort);
                }

                return true;

            case R.id.sharing_sort_title:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                if (callbackReceived != null) {
                    callbackReceived.onFilterValue(item.getItemId(), reverseSort);
                }
                if (callbackSent != null) {
                    callbackSent.onFilterValue(item.getItemId(), reverseSort);
                }
                if (callbackUnknown != null) {
                    callbackUnknown.onFilterValue(item.getItemId(), reverseSort);
                }

                return true;

            case R.id.sharing_sort_online:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                if (callbackReceived != null) {
                    callbackReceived.onFilterValue(item.getItemId(), reverseSort);
                }
                if (callbackSent != null) {
                    callbackSent.onFilterValue(item.getItemId(), reverseSort);
                }
                if (callbackUnknown != null) {
                    callbackUnknown.onFilterValue(item.getItemId(), reverseSort);
                }

                return true;

            case R.id.sharing_sort_downloaded:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                if (callbackReceived != null) {
                    callbackReceived.onFilterValue(item.getItemId(), reverseSort);
                }
                if (callbackSent != null) {
                    callbackSent.onFilterValue(item.getItemId(), reverseSort);
                }
                if (callbackUnknown != null) {
                    callbackUnknown.onFilterValue(item.getItemId(), reverseSort);
                }

                return true;

            case R.id.reverse:
                if (item.isChecked()) {
                    item.setChecked(false);
                    reverseSort = false;
                } else {
                    item.setChecked(true);
                    reverseSort = true;
                }
                if (callbackReceived != null) {
                    callbackReceived.onFilterValue(sortId, reverseSort);
                }
                if (callbackSent != null) {
                    callbackSent.onFilterValue(sortId, reverseSort);
                }
                if (callbackUnknown != null) {
                    callbackUnknown.onFilterValue(sortId, reverseSort);
                }

                return true;

            case R.id.sharing_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.sharing_share:
                AppController.Instance().shareApp();
                return true;
            case R.id.sharing_wifi:
                Intent shareIntent =
                        new Intent(HistoryActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.sharing_exit:
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
        search(newText);
        return false;
    }

    private void search(String newText) {

        if (callbackReceived != null) {
            callbackReceived.onSearchQuery(newText);
        }
        if (callbackSent != null) {
            callbackSent.onSearchQuery(newText);
        }
        if (callbackUnknown != null) {
            callbackUnknown.onSearchQuery(newText);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(final int position) {


        handler.postDelayed(() -> TAB_VALUE = (String) adapter.getPageTitle(position), 400);


    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @DebugLog
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
        filter.addAction(AppController.INTENT_DOWNLOAD_PROGRESS);

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
                    case AppController.INTENT_DOWNLOAD_PROGRESS:
                        String url = intent.getStringExtra("url");
                        int progress = intent.getIntExtra("progress", -1);

                        if (callbackReceived != null) {
                            callbackReceived.onDownloadProgress(url, progress);
                        }

                        break;
                }
            }
        }
    }
}
