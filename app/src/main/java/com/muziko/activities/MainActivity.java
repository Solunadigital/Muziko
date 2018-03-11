package com.muziko.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.register.RegisterActivity;
import com.muziko.adapter.PlayerPagerAdapter;
import com.muziko.billing.Subscriptions.StorageSubscriptionsGet;
import com.muziko.callbacks.PlayerCallback;
import com.muziko.common.events.FirebaseRefreshEvent;
import com.muziko.common.events.FirebaseShareCountEvent;
import com.muziko.common.events.ManageTabsEvent;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.events.TrackAddedEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.AdvancedSearchButton;
import com.muziko.controls.MiniPlayer;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.RateUs;
import com.muziko.events.AudioEvent;
import com.muziko.events.BufferingEvent;
import com.muziko.fragments.Listening.AlbumsFragment;
import com.muziko.fragments.Listening.TracksFragment;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.models.CloudAccount;
import com.muziko.service.SongService;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.OnBoomListener;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.skyfishjy.library.RippleBackground;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.aviran.cookiebar2.CookieBar;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import hugo.weaving.DebugLog;
import pl.tajchert.buswear.EventBus;

import static com.muziko.MyApplication.IMAGE_MEDIUM_SIZE;
import static com.muziko.MyApplication.networkState;
import static com.muziko.manager.AppController.ACTION_FIREBASE_OVERLIMIT;
import static com.muziko.manager.AppController.INTENT_QUEUE_CHANGED;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_ALBUM_FOLDER;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_GALLERY;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_ID3_TAGS;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_INTERNET;
import static com.muziko.manager.MuzikoConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;

public class MainActivity extends BaseActivity
        implements View.OnClickListener,
        ViewPager.OnPageChangeListener,
        SearchView.OnCloseListener,
        SearchView.OnQueryTextListener,
        MiniPlayer.onLayoutListener,
        NavigationView.OnNavigationItemSelectedListener,
        OnBoomListener {
    private static MainActivity instance;
    private final String TAG = MainActivity.class.getSimpleName();
    private final WeakHandler handler = new WeakHandler();
    //	private final SessionManagerListener<CastSession> mSessionManagerListener =
    //			new MySessionManagerListener();
    public PlayerCallback callbackHome;
    public PlayerCallback callbackTrack;
    public PlayerCallback callbackArtist;
    public PlayerCallback callbackAlbum;
    public PlayerCallback callbackGenre;
    public int selectedNavMenuItem = -1;
    private CoordinatorLayout coordinatorlayout;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private MiniPlayer miniPlayer;
    private Toolbar toolbar;
    private ImageButton advancedSearch;
    private int advancedSearchInset;
    private SearchView searchView;
    private MenuItem menuItemCast;
    private MenuItem menuItemFilter;
    private MenuItem menuShuffle;
    private MenuItem menu_sort_tracktitle;
    private MenuItem menu_sort_tracks;
    private MenuItem menu_sort_title;
    private MenuItem menu_sort_filename;
    private MenuItem menu_sort_album;
    private MenuItem menu_sort_artist;
    private MenuItem menu_sort_trackduration;
    private MenuItem menu_sort_duration;
    private MenuItem menu_sort_year;
    private MenuItem menu_sort_trackdate;
    private MenuItem menu_sort_date;
    private MenuItem menu_sort_songs;
    private MenuItem menu_sort_rating;
    private MenuItem menu_sort_reverse;
    private MenuItem menuItemStorage;
    private MenuItem menuItemSearch;
    private MenuItem menuItemStorageAll;
    private MenuItem menuItemStorageInternal;
    private MenuItem menuItemStorageSD;
    private MenuItem menuItemStorageCloud;
    private MenuItem menuItemStorageCloudFilter;
    private MenuItem menuItemgridone;
    private MenuItem menuItemgridtwo;
    private MenuItem menuItemgridthree;
    private MenuItem menuItemgridfour;
    private boolean reverseSort;
    private ImageView micImage;
    private int sortId;
    //	private CastContext mCastContext;
    //	private CastSession mCastSession;
    //	private IntroductoryOverlay mIntroductoryOverlay;
    //	private CastStateListener mCastStateListener;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavigationView footer_navigation_view;
    private LinearLayout nameLayout;
    private ImageView headerbg;
    private CircleImageView navavatar;
    private TextView nameText;
    private TextView emailtext;
    private boolean resetNavMenu;
    private MainReceiver mainReceiver;
    private int drawerId = -1;
    private Runnable mPendingRunnable;
    private int backCount;
    private PlayerPagerAdapter adapter;
    private ViewPager pager;
    private SmartTabLayout tabs;
    private TextView shareCount;
    private int boomMenuDelay = 450;
    private RippleBackground listeningRipple;
    private int locationX;
    private int locationY;
    private AppBarLayout appBarLayout;
    private AudioManager audioManager;
    private URL clipboardUrl;
    private boolean hasStreamClip = false;
    private StorageSubscriptionsGet storageSubscriptionsGet;

    public static MainActivity getMainActivityInstance() {

        return instance;

    }

    @DebugLog
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        drawerId = -1;

        setContentView(R.layout.activity_main);
        findViewsById();

        advancedSearchInset = toolbar.getContentInsetStartWithNavigation();
        toolbar.setTitle("Listening");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new PlayerPagerAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(5);
        pager.setAdapter(adapter);
        tabs.setViewPager(pager);
        pager.addOnPageChangeListener(this);

        setupMainPlayer();

        setupDrawer();
        updateNavDrawerHeader();

        EventBus.getDefault(this).register(this);
        PrefsManager.Instance().setFirstLogin((PrefsManager.Instance().getLoginCount() + 1));
        if (MyApplication.IMAGE_SIZE == 0) {
            Utils.GetScreenSize(this);
        }

        storageSubscriptionsGet = new StorageSubscriptionsGet(MainActivity.this, subs -> {

        });
        storageSubscriptionsGet.initBillProcessor();
    }

    @DebugLog
    @Override
    public void onResume() {
        super.onResume();

        register();

        if (miniPlayer != null) {
            miniPlayer.updateUI();
        }

        if (resetNavMenu) {
            navigationView.setCheckedItem(drawerId);
        }

        if (!AppController.Instance().isMyServiceRunning(SongService.class)) {
            startService(new Intent(getBaseContext(), SongService.class));
        }

    }

    @DebugLog
    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
        EventBus.getDefault(this).unregister(this);
        super.onDestroy();
    }

    @DebugLog
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case MuzikoConstants.REQUEST_REGISTER_USER:
                if (resultCode == Activity.RESULT_OK) {
                    Intent activityIntent;

                    switch (selectedNavMenuItem) {
                        case R.id.menu_library:
                            activityIntent = new Intent(MainActivity.this, LibraryActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            break;
                        case R.id.menu_myhistory:
                            activityIntent = new Intent(MainActivity.this, HistoryActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            break;
                        case R.id.menu_contacts:
                            activityIntent = new Intent(MainActivity.this, ContactsActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            break;
                        case R.id.menu_profile:
                            activityIntent = new Intent(MainActivity.this, ProfileActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            break;
                        default:
                            updateNavDrawerHeader();
                            break;
                    }
                }

                break;

            //TracksFragment
            case MuzikoConstants.REQUEST_REGISTER_USER_TRACKS:
            case CODE_WRITE_SETTINGS_PERMISSION:
                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof TracksFragment) {
                        fragment.onActivityResult(requestCode, resultCode, data);
                    }
                }
                break;

            case ARTWORK_PICK_FROM_GALLERY:
            case ARTWORK_PICK_FROM_INTERNET:
            case ARTWORK_PICK_FROM_ALBUM_FOLDER:
            case ARTWORK_PICK_FROM_ID3_TAGS:
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof AlbumsFragment) {
                        fragment.onActivityResult(requestCode, resultCode, data);
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @DebugLog
    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            //Closing drawer on item click
            drawerLayout.closeDrawers();
            return;
        }

        if (slidingUpPanelLayout != null
                && (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED
                || slidingUpPanelLayout.getPanelState()
                == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            miniPlayer.close();
            miniPlayer.open();

        } else if (backCount == 1) {
            AppController.Instance().exitKeepPlaying();

        } else if (menuItemSearch != null) {
            SearchView searchView = (SearchView) menuItemSearch.getActionView();

            if (searchView != null && !searchView.isIconified()) {
                searchView.setIconified(true);
                return;
            }

            if (backCount == 1) {
                AppController.Instance().exitKeepPlaying();

            } else {
                backCount++;
                CookieBar.Build(MainActivity.this)
                        .setDuration(3000)
                        .setMessage("Press back once more to exit")
                        .setMessageColor(R.color.white)
                        .setBackgroundColor(R.color.normal_blue)
                        .setLayoutGravity(Gravity.BOTTOM)
                        .show();

//                Utils.toast(this, "Press back once more to exit");
                handler.postDelayed(() -> backCount = 0, 4000);
            }
        }
    }

    @DebugLog
    @Override
    protected void onPause() {
        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        handler.removeCallbacksAndMessages(null);
        PrefsManager.Instance().setLastActivity(getClass().getName());
        PrefsManager.Instance().setLastMainActivityTab(adapter.getCurrentTab());
        unregister();
        super.onPause();
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFirebaseEvent(FirebaseShareCountEvent event) {
        updateNavDrawerHeader();
        initializeCountDrawer(event.count);
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFirebaseRefreshEvent(FirebaseRefreshEvent event) {

        updateNavDrawerHeader();
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {

        handler.postDelayed(() -> refresh(), event.delay);
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackAddedEvent(TrackAddedEvent trackAddedEvent) {

        trackAdded(trackAddedEvent.trackData);
    }


    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onManageTabsEvent(ManageTabsEvent event) {

        adapter = new PlayerPagerAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(5);
        pager.setAdapter(adapter);
        tabs.setViewPager(pager);
        //		tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        pager.addOnPageChangeListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressEvent(ProgressEvent event) {

        if (miniPlayer != null) miniPlayer.updateProgress(event.getProgress(), event.getDuration());

        if (callbackTrack != null) {
            callbackTrack.onNowPlaying();
        }
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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAudioEvent(AudioEvent audioEvent) {

        if (listeningRipple != null) {
            if (audioEvent.isMusicPlaying()) {
                if (!listeningRipple.isRippleAnimationRunning()) {
                    listeningRipple.startRippleAnimation();
                }
            } else {
                if (listeningRipple.isRippleAnimationRunning()) {
                    listeningRipple.stopRippleAnimation();
                }
            }
        }
    }

    @DebugLog
    private void trackAdded(String data) {
        if (callbackTrack != null) {
            callbackTrack.onTrackAdded(data);
        }
    }

    @DebugLog
    private void refresh() {
        mainUpdate();
        if (callbackHome != null) {
            callbackHome.onReload();
        }
        if (callbackTrack != null) {
            callbackTrack.onReload();
        }
        if (callbackArtist != null) {
            callbackArtist.onReload();
        }
        if (callbackAlbum != null) {
            callbackAlbum.onReload();
        }
        if (callbackGenre != null) {
            callbackGenre.onReload();
        }
    }

    @DebugLog
    void setupMainPlayer() {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        final LinearLayout mainPlayerLayout = findViewById(R.id.mainPlayerLayout);

        miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout, true, this);
    }

    @DebugLog
    public void enableTabs(boolean b) {
        Utils.enableDisableViewGroup(tabs, b);
    }

    @DebugLog
    private void findViewsById() {
        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        appBarLayout = findViewById(R.id.appBarLayout);
        pager = findViewById(R.id.vpPager);
        tabs = findViewById(R.id.tabs);
        navigationView = findViewById(R.id.navigation_view);
        footer_navigation_view = findViewById(R.id.footer_navigation_view);
        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolbar);
    }

    @DebugLog
    private void initializeCountDrawer(int count) {
        //Gravity property aligns the text
        shareCount.setGravity(Gravity.CENTER_VERTICAL);
        shareCount.setTypeface(null, Typeface.BOLD);
        shareCount.setTextColor(ContextCompat.getColor(this, R.color.normal_blue));

        if (count == 0) {
            shareCount.setText("");
        } else {
            shareCount.setText(String.valueOf(count));
        }
    }

    @DebugLog
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    FirebaseManager.Instance().startFirebase();
                    updateNavDrawerHeader();
                } else {
                    // Permission Denied
                    new MaterialDialog.Builder(this)
                            .theme(Theme.LIGHT)
                            .titleColorRes(R.color.normal_blue)
                            .positiveColorRes(R.color.normal_blue)
                            .title("Permission not provided")
                            .content(
                                    "Read contacts permission is required to share tracks with friends.")
                            .positiveText("OK")
                            .onPositive(
                                    (dialog, which) -> {
                                        finish();
                                    })
                            .cancelable(false)
                            .show();
                }
            }
            break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @DebugLog
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.clear();
        getMenuInflater().inflate(R.menu.listening_menu, menu);

        menuItemStorage = menu.findItem(R.id.storage_filter);
        menuItemFilter = menu.findItem(R.id.player_filter);
        menuItemSearch = menu.findItem(R.id.player_search);

        menuShuffle = menu.findItem(R.id.shuffle_all);
        menu_sort_tracktitle = menu.findItem(R.id.player_sort_tracktitle);
        menu_sort_tracks = menu.findItem(R.id.player_sort_tracks);
        menu_sort_title = menu.findItem(R.id.player_sort_title);
        menu_sort_filename = menu.findItem(R.id.player_sort_filename);
        menu_sort_album = menu.findItem(R.id.player_sort_album);
        menu_sort_artist = menu.findItem(R.id.player_sort_artist);
        menu_sort_trackduration = menu.findItem(R.id.player_sort_trackduration);
        menu_sort_duration = menu.findItem(R.id.player_sort_duration);
        menu_sort_year = menu.findItem(R.id.player_sort_year);
        menu_sort_trackdate = menu.findItem(R.id.player_sort_trackdate);
        menu_sort_date = menu.findItem(R.id.player_sort_date);
        menu_sort_songs = menu.findItem(R.id.player_sort_songs);
        menu_sort_rating = menu.findItem(R.id.player_sort_rating);
        menu_sort_reverse = menu.findItem(R.id.reverse);

        menuItemStorageAll = menu.findItem(R.id.player_storage_all);
        menuItemStorageInternal = menu.findItem(R.id.player_storage_internal);
        menuItemStorageSD = menu.findItem(R.id.player_storage_sd);
        menuItemStorageCloud = menu.findItem(R.id.player_storage_cloud);
        menuItemStorageCloudFilter = menu.findItem(R.id.player_storage_cloud_filter);

        menuItemgridone = menu.findItem(R.id.grid_one);
        menuItemgridtwo = menu.findItem(R.id.grid_two);
        menuItemgridthree = menu.findItem(R.id.grid_three);
        menuItemgridfour = menu.findItem(R.id.grid_four);

        listeningRipple = (RippleBackground) menu.findItem(R.id.player_identify).getActionView();
        micImage = listeningRipple.findViewById(R.id.micImage);
        listeningRipple.setOnClickListener(this);

        //		menuItemCast = CastButtonFactory.setUpMediaRouteButton(this, menu,
        //				R.id.media_route_menu_item);

        return super.onCreateOptionsMenu(menu);
    }

    @DebugLog
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        setupMenu();
        updateStorageMenu();
        updateGridMenu();

        if (menuItemSearch != null) {
            searchView = (SearchView) menuItemSearch.getActionView();

            AdvancedSearchButton advancedSearchButton = new AdvancedSearchButton();
            Resources resources = getResources();
            advancedSearch = advancedSearchButton.addButton(this, resources, searchView);
            advancedSearch.setOnClickListener(this);
            ActionBar.LayoutParams searchviewParams =
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT);
            searchView.setLayoutParams(searchviewParams);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint("Search...");
            searchView.setOnQueryTextListener(this);
            searchView.setOnSearchClickListener(this);
            searchView.setOnCloseListener(this);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @DebugLog
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items

        ArrayList<CloudAccount> cloudAccounts = CloudAccountRealmHelper.getCloudAccounts();

        for (CloudAccount cloudAccount : cloudAccounts) {
            if (cloudAccount.getCloudAccountId() == item.getItemId()) {
                PrefsManager.Instance().setStorageViewType(cloudAccount.getCloudAccountId());
                updateStorage();
                return true;
            }
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.shuffle_all:
                ArrayList<QueueItem> tracks = new ArrayList<>();
                tracks.addAll(TrackRealmHelper.getTracks(0).values());

                long seed = System.nanoTime();
                Collections.shuffle(tracks, new Random(seed));

                AppController.Instance().play(PlayerConstants.QUEUE_TYPE_TRACKS, 0, tracks);
                adapter.notifyDataSetChanged();

                return true;

            case R.id.player_search:
                return true;

            case R.id.player_sort_tracks:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                PrefsManager.Instance().setTrackSort(item.getItemId());

                if (callbackTrack != null) {
                    callbackTrack.onFilterValue(item.getItemId(), reverseSort);
                }

                return true;

            case R.id.player_sort_tracktitle:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();

                PrefsManager.Instance().setTrackSort(item.getItemId());
                if (callbackTrack != null) {
                    callbackTrack.onFilterValue(item.getItemId(), reverseSort);
                }
                return true;

            case R.id.player_sort_title:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();

                switch (adapter.getCurrentTab()) {
                    case MyApplication.ARTISTS:
                        PrefsManager.Instance().setArtistSort(item.getItemId());
                        if (callbackArtist != null) {
                            callbackArtist.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                    case MyApplication.ALBUMS:
                        PrefsManager.Instance().setAlbumSort(item.getItemId());
                        if (callbackAlbum != null) {
                            callbackAlbum.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                    case MyApplication.GENRES:
                        PrefsManager.Instance().setGenreSort(item.getItemId());
                        if (callbackGenre != null) {
                            callbackGenre.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                }
                return true;

            case R.id.player_sort_filename:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                PrefsManager.Instance().setTrackSort(item.getItemId());
                if (callbackTrack != null) {
                    callbackTrack.onFilterValue(item.getItemId(), reverseSort);
                }

                return true;

            case R.id.player_sort_album:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                PrefsManager.Instance().setTrackSort(item.getItemId());
                if (callbackTrack != null) {
                    callbackTrack.onFilterValue(item.getItemId(), reverseSort);
                }

                return true;

            case R.id.player_sort_artist:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                PrefsManager.Instance().setTrackSort(item.getItemId());
                if (callbackTrack != null) {
                    callbackTrack.onFilterValue(item.getItemId(), reverseSort);
                }

                return true;

            case R.id.player_sort_trackduration:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();

                PrefsManager.Instance().setTrackSort(item.getItemId());
                if (callbackTrack != null) {
                    callbackTrack.onFilterValue(item.getItemId(), reverseSort);
                }

                return true;

            case R.id.player_sort_duration:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();

                switch (adapter.getCurrentTab()) {
                    case MyApplication.ARTISTS:
                        PrefsManager.Instance().setArtistSort(item.getItemId());
                        if (callbackArtist != null) {
                            callbackArtist.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                    case MyApplication.ALBUMS:
                        PrefsManager.Instance().setAlbumSort(item.getItemId());
                        if (callbackAlbum != null) {
                            callbackAlbum.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                    case MyApplication.GENRES:
                        PrefsManager.Instance().setGenreSort(item.getItemId());
                        if (callbackGenre != null) {
                            callbackGenre.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                }

                return true;

            case R.id.player_sort_year:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                PrefsManager.Instance().setTrackSort(item.getItemId());

                if (callbackTrack != null) {
                    callbackTrack.onFilterValue(item.getItemId(), reverseSort);
                }

                return true;

            case R.id.player_sort_trackdate:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();

                PrefsManager.Instance().setTrackSort(item.getItemId());
                if (callbackTrack != null) {
                    callbackTrack.onFilterValue(item.getItemId(), reverseSort);
                }

                return true;

            case R.id.player_sort_date:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                switch (adapter.getCurrentTab()) {
                    case MyApplication.ARTISTS:
                        PrefsManager.Instance().setArtistSort(item.getItemId());
                        if (callbackArtist != null) {
                            callbackArtist.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                    case MyApplication.ALBUMS:
                        PrefsManager.Instance().setAlbumSort(item.getItemId());
                        if (callbackAlbum != null) {
                            callbackAlbum.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                    case MyApplication.GENRES:
                        PrefsManager.Instance().setGenreSort(item.getItemId());
                        if (callbackGenre != null) {
                            callbackGenre.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                }

                return true;

            case R.id.player_sort_songs:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                switch (adapter.getCurrentTab()) {
                    case MyApplication.ARTISTS:
                        PrefsManager.Instance().setArtistSort(item.getItemId());
                        if (callbackArtist != null) {
                            callbackArtist.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                    case MyApplication.ALBUMS:
                        PrefsManager.Instance().setAlbumSort(item.getItemId());
                        if (callbackAlbum != null) {
                            callbackAlbum.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                    case MyApplication.GENRES:
                        PrefsManager.Instance().setGenreSort(item.getItemId());
                        if (callbackGenre != null) {
                            callbackGenre.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
                }

                return true;

            case R.id.player_sort_rating:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                sortId = item.getItemId();
                switch (adapter.getCurrentTab()) {
                    case MyApplication.TRACKS:
                        PrefsManager.Instance().setTrackSort(item.getItemId());
                        if (callbackTrack != null) {
                            callbackTrack.onFilterValue(item.getItemId(), reverseSort);
                        }
                        break;
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

                switch (adapter.getCurrentTab()) {
                    case MyApplication.TRACKS:
                        PrefsManager.Instance().setTrackSortReverse(reverseSort);
                        if (callbackTrack != null) {
                            callbackTrack.onFilterValue(sortId, reverseSort);
                        }
                        break;
                    case MyApplication.ARTISTS:
                        PrefsManager.Instance().setArtistSortReverse(reverseSort);
                        if (callbackArtist != null) {
                            callbackArtist.onFilterValue(sortId, reverseSort);
                        }
                        break;
                    case MyApplication.ALBUMS:
                        PrefsManager.Instance().setAlbumSortReverse(reverseSort);
                        if (callbackAlbum != null) {
                            callbackAlbum.onFilterValue(sortId, reverseSort);
                        }
                        break;
                    case MyApplication.GENRES:
                        PrefsManager.Instance().setGenreSortReverse(reverseSort);
                        if (callbackGenre != null) {
                            callbackGenre.onFilterValue(sortId, reverseSort);
                        }
                        break;
                }

                return true;

            case R.id.grid_one:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                switch (adapter.getCurrentTab()) {
                    case MyApplication.HOME:
                        PrefsManager.Instance().setHomeViewType(0);
                        break;
                    case MyApplication.TRACKS:
                        PrefsManager.Instance().setTracksViewType(0);
                        break;
                    case MyApplication.ARTISTS:
                        PrefsManager.Instance().setArtistsViewType(0);
                        break;
                    case MyApplication.ALBUMS:
                        PrefsManager.Instance().setAlbumsViewType(0);
                        break;
                    case MyApplication.GENRES:
                        PrefsManager.Instance().setGenresViewType(0);
                        break;
                }

                updateListing();

                return true;

            case R.id.grid_two:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                switch (adapter.getCurrentTab()) {
                    case MyApplication.HOME:
                        PrefsManager.Instance().setHomeViewType(1);
                        break;
                    case MyApplication.TRACKS:
                        PrefsManager.Instance().setTracksViewType(1);
                        break;
                    case MyApplication.ARTISTS:
                        PrefsManager.Instance().setArtistsViewType(1);
                        break;
                    case MyApplication.ALBUMS:
                        PrefsManager.Instance().setAlbumsViewType(1);
                        break;
                    case MyApplication.GENRES:
                        PrefsManager.Instance().setGenresViewType(1);
                        break;
                }
                updateListing();

                return true;

            case R.id.grid_three:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                switch (adapter.getCurrentTab()) {
                    case MyApplication.HOME:
                        PrefsManager.Instance().setHomeViewType(2);
                        break;
                    case MyApplication.TRACKS:
                        PrefsManager.Instance().setTracksViewType(2);
                        break;
                    case MyApplication.ARTISTS:
                        PrefsManager.Instance().setArtistsViewType(2);
                        break;
                    case MyApplication.ALBUMS:
                        PrefsManager.Instance().setAlbumsViewType(2);
                        break;
                    case MyApplication.GENRES:
                        PrefsManager.Instance().setGenresViewType(2);
                        break;
                }
                updateListing();

                return true;

            case R.id.grid_four:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                switch (adapter.getCurrentTab()) {
                    case MyApplication.HOME:
                        PrefsManager.Instance().setHomeViewType(3);
                        break;
                    case MyApplication.TRACKS:
                        PrefsManager.Instance().setTracksViewType(3);
                        break;
                    case MyApplication.ARTISTS:
                        PrefsManager.Instance().setArtistsViewType(3);
                        break;
                    case MyApplication.ALBUMS:
                        PrefsManager.Instance().setAlbumsViewType(3);
                        break;
                    case MyApplication.GENRES:
                        PrefsManager.Instance().setGenresViewType(3);
                        break;
                }
                updateListing();

                return true;

            case R.id.player_storage_all:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                PrefsManager.Instance().setStorageViewType(0);
                updateStorage();
                return true;

            case R.id.player_storage_internal:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                PrefsManager.Instance().setStorageViewType(1);
                updateStorage();
                return true;

            case R.id.player_storage_sd:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                PrefsManager.Instance().setStorageViewType(2);
                updateStorage();
                return true;

            case R.id.player_storage_cloud:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                PrefsManager.Instance().setStorageViewType(-1);
                updateStorage();
                return true;

            case R.id.player_storage_cloud_filter:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                return true;

            case R.id.player_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.player_share:
                AppController.Instance().shareApp();
                return true;

            case R.id.player_play_songs:
                AppController.Instance().playAll();
                return true;
            case R.id.sharing_wifi:
                Intent shareIntent =
                        new Intent(MainActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.player_exit:
                AppController.Instance().exit();
                return true;

            default:
                return false; //super.onOptionsItemSelected(item);
        }
    }

    @DebugLog
    private void setupMenu() {

        switch (adapter.getCurrentTab()) {
            case MyApplication.HOME:
                if (menuItemFilter != null) {
                    menuItemFilter.setVisible(false);
                }
                if (menuShuffle != null) {
                    menuShuffle.setVisible(true);
                }
                if (menuItemStorage != null) {
                    menuItemStorage.setVisible(true);
                }

                break;
            case MyApplication.TRACKS:
                if (menuItemFilter != null) {
                    menuItemFilter.setVisible(true);
                    menuItemFilter.getSubMenu().setGroupVisible(R.id.sort_tracks, true);
                    menuItemFilter.getSubMenu().setGroupVisible(R.id.sort_albums, false);
                }
                if (menuShuffle != null) {
                    menuShuffle.setVisible(true);
                }
                if (menuItemStorage != null) {
                    menuItemStorage.setVisible(true);
                }
                sortId = PrefsManager.Instance().getTrackSort();
                reverseSort = PrefsManager.Instance().getTrackSortReverse();

                break;

            case MyApplication.ARTISTS:
                if (menuItemFilter != null) {
                    menuItemFilter.setVisible(true);
                    menuItemFilter.getSubMenu().setGroupVisible(R.id.sort_tracks, false);
                    menuItemFilter.getSubMenu().setGroupVisible(R.id.sort_albums, true);
                }
                if (menuShuffle != null) {
                    menuShuffle.setVisible(false);
                }
                if (menuItemStorage != null) {
                    menuItemStorage.setVisible(false);
                }
                sortId = PrefsManager.Instance().getArtistSort();
                reverseSort = PrefsManager.Instance().getArtistSortReverse();
                break;

            case MyApplication.ALBUMS:
                if (menuItemFilter != null) {
                    menuItemFilter.setVisible(true);
                    menuItemFilter.getSubMenu().setGroupVisible(R.id.sort_tracks, false);
                    menuItemFilter.getSubMenu().setGroupVisible(R.id.sort_albums, true);
                }
                if (menuShuffle != null) {
                    menuShuffle.setVisible(false);
                }
                if (menuItemStorage != null) {
                    menuItemStorage.setVisible(false);
                }
                sortId = PrefsManager.Instance().getAlbumSort();
                reverseSort = PrefsManager.Instance().getAlbumSortReverse();
                break;

            case MyApplication.GENRES:
                if (menuItemFilter != null) {
                    menuItemFilter.setVisible(true);
                    menuItemFilter.getSubMenu().setGroupVisible(R.id.sort_tracks, false);
                    menuItemFilter.getSubMenu().setGroupVisible(R.id.sort_albums, true);
                }
                if (menuShuffle != null) {
                    menuShuffle.setVisible(false);
                }
                if (menuItemStorage != null) {
                    menuItemStorage.setVisible(false);
                }
                sortId = PrefsManager.Instance().getGenreSort();
                reverseSort = PrefsManager.Instance().getGenreSortReverse();

                break;
        }

        onFilterValue(sortId, reverseSort);
    }

    @DebugLog
    private void onFilterValue(int value, boolean reverse) {

        if (menu_sort_reverse == null) {
            return;
        }

        if (reverse) {
            menu_sort_reverse.setChecked(true);
        } else {
            menu_sort_reverse.setChecked(false);
        }

        switch (value) {
            case R.id.player_sort_tracks:
                menu_sort_tracks.setChecked(true);
                break;
            case R.id.player_sort_tracktitle:
                menu_sort_tracktitle.setChecked(true);
                break;
            case R.id.player_sort_title:
                menu_sort_title.setChecked(true);
                break;
            case R.id.player_sort_filename:
                menu_sort_filename.setChecked(true);
                break;
            case R.id.player_sort_album:
                menu_sort_album.setChecked(true);
                break;
            case R.id.player_sort_artist:
                menu_sort_artist.setChecked(true);
                break;
            case R.id.player_sort_trackduration:
                menu_sort_trackduration.setChecked(true);
                break;
            case R.id.player_sort_duration:
                menu_sort_duration.setChecked(true);
                break;
            case R.id.player_sort_year:
                menu_sort_year.setChecked(true);
                break;
            case R.id.player_sort_trackdate:
                menu_sort_trackdate.setChecked(true);
                break;
            case R.id.player_sort_date:
                menu_sort_date.setChecked(true);
                break;
            case R.id.player_sort_songs:
                menu_sort_songs.setChecked(true);
                break;
            case R.id.player_sort_rating:
                menu_sort_rating.setChecked(true);
                break;
        }
    }

    @DebugLog
    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @DebugLog
    @Override
    public boolean onQueryTextChange(String newText) {
        MyApplication.searchString = newText;
        search(newText);
        return false;
    }

    @DebugLog
    @Override
    public boolean onClose() {
        advancedSearch.setVisibility(View.GONE);
        toolbar.setContentInsetStartWithNavigation(advancedSearchInset);
        return false;
    }

    @DebugLog
    private void updateStorageMenu() {
        if (menuItemStorage != null) {
            if (PrefsManager.Instance().getStorageViewType() == -1) {
                menuItemStorageCloud.setChecked(true);
            } else if (PrefsManager.Instance().getStorageViewType() == 0) {
                menuItemStorageAll.setChecked(true);
            } else if (PrefsManager.Instance().getStorageViewType() == 1) {
                menuItemStorageInternal.setChecked(true);
            } else if (PrefsManager.Instance().getStorageViewType() == 2) {
                menuItemStorageSD.setChecked(true);
            } else {
                menuItemStorageCloudFilter.setChecked(true);
            }

            ArrayList<CloudAccount> cloudAccounts = CloudAccountRealmHelper.getCloudAccounts();
            if (cloudAccounts.size() == 0) {
                menuItemStorageCloudFilter.setVisible(false);
            } else {
                menuItemStorageCloudFilter.setVisible(true);
                SubMenu subMenu = menuItemStorageCloudFilter.getSubMenu();
                subMenu.clear();
                int group = Utils.randInt();
                int storageFilter = PrefsManager.Instance().getStorageViewType();
                for (CloudAccount cloudAccount : cloudAccounts) {
                    SpannableString cloudNameSS = null;
                    Drawable drawable = null;
                    switch (cloudAccount.getCloudProvider()) {
                        case (CloudManager.GOOGLEDRIVE):
                            cloudNameSS = new SpannableString("      " + cloudAccount.getName());
                            drawable = ContextCompat.getDrawable(this, R.drawable.drive_small);
                            drawable.setBounds(0, 0, 60, 60);
                            break;
                        case (CloudManager.DROPBOX):
                            cloudNameSS = new SpannableString("      " + cloudAccount.getName());
                            drawable = ContextCompat.getDrawable(this, R.drawable.dropbox_blue_small);
                            drawable.setBounds(0, 0, 60, 60);
                            break;
                        case (CloudManager.BOX):
                            cloudNameSS = new SpannableString("    " + cloudAccount.getName());
                            drawable = ContextCompat.getDrawable(this, R.drawable.box_blue_small);
                            drawable.setBounds(0, 0, 90, 60);
                            break;
                        case (CloudManager.ONEDRIVE):
                            cloudNameSS = new SpannableString("    " + cloudAccount.getName());
                            drawable = ContextCompat.getDrawable(this, R.drawable.onedrive_blue_small);
                            drawable.setBounds(0, 0, 90, 60);
                            break;
                        case (CloudManager.AMAZON):
                            cloudNameSS = new SpannableString("    " + cloudAccount.getName());
                            drawable = ContextCompat.getDrawable(this, R.drawable.amazon_small);
                            drawable.setBounds(0, 0, 90, 60);
                            break;
                    }

                    ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
                    cloudNameSS.setSpan(imageSpan, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    subMenu.add(group, cloudAccount.getCloudAccountId(), Menu.NONE, cloudNameSS);
                    if (storageFilter == cloudAccount.getCloudAccountId()) {
                        menuItemStorageCloudFilter.setChecked(true);
                        subMenu.findItem(cloudAccount.getCloudAccountId()).setChecked(true);
                    }
                }
                subMenu.setGroupCheckable(group, true, true);
            }
        }
    }

    @DebugLog
    private void updateGridMenu() {
        if (menuItemStorage != null) {

            switch (adapter.getCurrentTab()) {
                case MyApplication.TRACKS:
                    if (PrefsManager.Instance().getTracksViewType() == 0) {
                        menuItemgridone.setChecked(true);
                    } else if (PrefsManager.Instance().getTracksViewType() == 1) {
                        menuItemgridtwo.setChecked(true);
                    } else if (PrefsManager.Instance().getTracksViewType() == 2) {
                        menuItemgridthree.setChecked(true);
                    } else if (PrefsManager.Instance().getTracksViewType() == 3) {
                        menuItemgridfour.setChecked(true);
                    }
                    break;
                case MyApplication.ARTISTS:
                    if (PrefsManager.Instance().getArtistsViewType() == 0) {
                        menuItemgridone.setChecked(true);
                    } else if (PrefsManager.Instance().getArtistsViewType() == 1) {
                        menuItemgridtwo.setChecked(true);
                    } else if (PrefsManager.Instance().getArtistsViewType() == 2) {
                        menuItemgridthree.setChecked(true);
                    } else if (PrefsManager.Instance().getArtistsViewType() == 3) {
                        menuItemgridfour.setChecked(true);
                    }
                    break;
                case MyApplication.ALBUMS:
                    if (PrefsManager.Instance().getAlbumsViewType() == 0) {
                        menuItemgridone.setChecked(true);
                    } else if (PrefsManager.Instance().getAlbumsViewType() == 1) {
                        menuItemgridtwo.setChecked(true);
                    } else if (PrefsManager.Instance().getAlbumsViewType() == 2) {
                        menuItemgridthree.setChecked(true);
                    } else if (PrefsManager.Instance().getAlbumsViewType() == 3) {
                        menuItemgridfour.setChecked(true);
                    }
                    break;
                case MyApplication.GENRES:
                    if (PrefsManager.Instance().getGenresViewType() == 0) {
                        menuItemgridone.setChecked(true);
                    } else if (PrefsManager.Instance().getGenresViewType() == 1) {
                        menuItemgridtwo.setChecked(true);
                    } else if (PrefsManager.Instance().getGenresViewType() == 2) {
                        menuItemgridthree.setChecked(true);
                    } else if (PrefsManager.Instance().getGenresViewType() == 3) {
                        menuItemgridfour.setChecked(true);
                    }
                    break;
            }
        }
    }

    @DebugLog
    private void updateStorage() {
        if (callbackTrack != null) callbackTrack.onStorageChanged();
    }

    @DebugLog
    private void updateListing() {
        if (callbackHome != null) callbackHome.onListingChanged();
        if (callbackTrack != null) callbackTrack.onListingChanged();
        if (callbackArtist != null) callbackArtist.onListingChanged();
        if (callbackAlbum != null) callbackAlbum.onListingChanged();
        if (callbackGenre != null) callbackGenre.onListingChanged();
    }

    @DebugLog
    private void search(String newText) {
        PlayerConstants.QUEUE_TYPE = 0;

        if (callbackTrack != null) {
            callbackTrack.onSearchQuery(newText);
        }
        if (callbackArtist != null) {
            callbackArtist.onSearchQuery(newText);
        }
        if (callbackAlbum != null) {
            callbackAlbum.onSearchQuery(newText);
        }
        if (callbackGenre != null) {
            callbackGenre.onSearchQuery(newText);
        }
    }

    @DebugLog
    @Override
    public void onClick(View v) {

        if (v == navavatar || v == nameLayout) {
            if (FirebaseManager.Instance().isAuthenticated() && !FirebaseManager.Instance().isAnonymous()) {
                Intent activityIntent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            } else {
                if (networkState == NetworkInfo.State.CONNECTED) {
                    Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivityForResult(registerIntent, MuzikoConstants.REQUEST_REGISTER_USER);
                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                } else {
                    AppController.toast(this, getString(R.string.no_internet_for_register));
                }
            }
        } else if (v == advancedSearch) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (v == listeningRipple) {
            int[] location = new int[2];
            micImage.getLocationOnScreen(location);
            locationX = location[0];
            locationY = location[1];
            Intent activityIntent = new Intent(MainActivity.this, IdentifySongActivity.class);
            activityIntent.putExtra("x", locationX);
            activityIntent.putExtra("y", locationY);
            startActivity(activityIntent);
        } else {
            advancedSearch.setVisibility(View.VISIBLE);
            toolbar.setContentInsetStartWithNavigation(0);
        }
    }

    @DebugLog
    void updateNavDrawerHeader() {

        if (FirebaseManager.Instance().isAuthenticated() && !FirebaseManager.Instance().isAnonymous()) {

            navigationView.getMenu().setGroupVisible(R.id.menu_user, true);
            headerbg.setImageResource(R.drawable.nav_header_bg);
            nameText.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            nameText.setTextColor(Color.WHITE);
            emailtext.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            emailtext.setVisibility(View.GONE);

            if (!PrefsManager.Instance().getProfileUrl().isEmpty()) {
                Picasso.with(this)
                        .load(PrefsManager.Instance().getProfileUrl())
                        .placeholder(R.drawable.profile_placeholder)
                        .error(R.drawable.profile_placeholder)
                        .resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
                        .centerCrop()
                        .into(navavatar);
            } else {
                Picasso.with(this)
                        .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                        .placeholder(R.drawable.profile_placeholder)
                        .error(R.drawable.profile_placeholder)
                        .resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
                        .centerCrop()
                        .into(navavatar);
            }

        } else {
            headerbg.setImageResource(0);
            navigationView.getMenu().setGroupVisible(R.id.menu_user, false);
            nameText.setTextColor(Color.BLACK);
            nameText.setText(R.string.sign_in);
            emailtext.setVisibility(View.GONE);
            navavatar.setImageResource(R.drawable.profile_placeholder);
            initializeCountDrawer(0);
        }
        setupDrawer();
    }

    @DebugLog
    void setupDrawer() {

        shareCount =
                (TextView)
                        MenuItemCompat.getActionView(
                                navigationView.getMenu().findItem(R.id.menu_myhistory));

        navigationView.setCheckedItem(R.id.menu_listening);
        drawerId = R.id.menu_listening;
        nameLayout = navigationView.getHeaderView(0).findViewById(R.id.nameLayout);
        headerbg = navigationView.getHeaderView(0).findViewById(R.id.headerbackground);
        navavatar = navigationView.getHeaderView(0).findViewById(R.id.navavatar);
        nameText = navigationView.getHeaderView(0).findViewById(R.id.nameText);
        emailtext = navigationView.getHeaderView(0).findViewById(R.id.emailtext);

        nameLayout.setOnClickListener(this);
        navavatar.setOnClickListener(this);

        // hide premium
//        if (PrefsManager.Instance().getPremium() || PrefsManager.Instance().getEarnedPremium()) {
//            footer_navigation_view.getMenu().findItem(R.id.menu_premium).setVisible(false);
//        }

        if (FirebaseManager.Instance().isAuthenticated()) {
            navigationView.getMenu().findItem(R.id.menu_subscriptions).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.menu_subscriptions).setVisible(false);
        }

        footer_navigation_view.getMenu().findItem(R.id.menu_premium).setVisible(false);

        navigationView.setNavigationItemSelectedListener(this);
        footer_navigation_view.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(
                        this,
                        drawerLayout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close) {

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank

                        // If mPendingRunnable is not null, then add to the message queue
                        if (mPendingRunnable != null) {
                            handler.post(mPendingRunnable);
                            mPendingRunnable = null;
                        }
                        super.onDrawerClosed(drawerView);
                    }
                };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @DebugLog
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        //Checking if the item is in checked state or not, if not make it in checked state
        if (menuItem.isChecked()) menuItem.setChecked(false);
        else menuItem.setChecked(true);

        //Closing drawer on item click
        drawerLayout.closeDrawers();

        if (drawerId == menuItem.getItemId() && drawerId != R.id.menu_listening) {

            return true;
        }

        if (menuItem.getItemId() == R.id.menu_listening) {
            drawerId = menuItem.getItemId();
        }

        //Check to see which item was being clicked and perform appropriate action
        switch (menuItem.getItemId()) {

            //Replacing the main content with ContentFragment Which is our Inbox View;
            case R.id.menu_listening:
                return true;

            case R.id.menu_trending:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            Intent activityIntent =
                                    new Intent(MainActivity.this, TrendingActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        };

                return true;

            case R.id.menu_trash_folder:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            Intent activityIntent =
                                    new Intent(MainActivity.this, IgnoreActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        };

                return true;

            case R.id.menu_library:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            if (FirebaseManager.Instance().isAuthenticated()) {
                                Intent activityIntent =
                                        new Intent(MainActivity.this, LibraryActivity.class);
                                startActivity(activityIntent);
                                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            } else {
                                if (networkState == NetworkInfo.State.CONNECTED) {
                                    selectedNavMenuItem = R.id.menu_library;
                                    Intent registerIntent =
                                            new Intent(MainActivity.this, RegisterActivity.class);
                                    startActivityForResult(
                                            registerIntent, MuzikoConstants.REQUEST_REGISTER_USER);
                                } else {
                                    AppController.toast(this, getString(R.string.no_internet_for_register));
                                }

                            }
                        };

                return true;

            case R.id.menu_mp3cutter:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            Intent activityIntent =
                                    new Intent(MainActivity.this, MP3CutterActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        };

                return true;

            case R.id.menu_sleeptimer:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            Intent activityIntent =
                                    new Intent(MainActivity.this, SleepActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        };

                return true;

            case R.id.menu_equalizer:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            Intent activityIntent =
                                    new Intent(MainActivity.this, EqualizerActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        };

                return true;

            case R.id.menu_subscriptions:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            Intent activityIntent = new Intent(MainActivity.this, SubscriptionActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        };

                return true;

            case R.id.menu_myhistory:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            if (FirebaseManager.Instance().isAuthenticated()) {
                                Intent activityIntent =
                                        new Intent(MainActivity.this, HistoryActivity.class);
                                startActivity(activityIntent);
                                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            } else {
                                if (networkState == NetworkInfo.State.CONNECTED) {
                                    selectedNavMenuItem = R.id.menu_myhistory;
                                    Intent registerIntent =
                                            new Intent(MainActivity.this, RegisterActivity.class);
                                    startActivityForResult(
                                            registerIntent, MuzikoConstants.REQUEST_REGISTER_USER);
                                } else {
                                    AppController.toast(this, getString(R.string.no_internet_for_register));
                                }
                            }
                        };

                return true;

        /*    case R.id.menu_sharing_wifi:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            Intent shareIntent =
                                    new Intent(MainActivity.this, ShareWifiActivity.class);
                            startActivity(shareIntent);
                        };

                return true;*/

            case R.id.menu_contacts:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            if (FirebaseManager.Instance().isAuthenticated()) {
                                Intent activityIntent =
                                        new Intent(MainActivity.this, ContactsActivity.class);
                                startActivity(activityIntent);
                                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            } else {
                                if (networkState == NetworkInfo.State.CONNECTED) {
                                    selectedNavMenuItem = R.id.menu_myhistory;
                                    Intent registerIntent =
                                            new Intent(MainActivity.this, RegisterActivity.class);
                                    startActivityForResult(
                                            registerIntent, MuzikoConstants.REQUEST_REGISTER_USER);
                                } else {
                                    AppController.toast(this, getString(R.string.no_internet_for_register));
                                }
                            }
                        };

                return true;

            case R.id.menu_invite:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            Intent activityIntent =
                                    new Intent(MainActivity.this, InviteActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        };

                return true;

            case R.id.menu_profile:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            if (FirebaseManager.Instance().isAuthenticated()) {
                                Intent activityIntent =
                                        new Intent(MainActivity.this, ProfileActivity.class);
                                startActivity(activityIntent);
                                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                            } else {
                                if (networkState == NetworkInfo.State.CONNECTED) {
                                    selectedNavMenuItem = R.id.menu_myhistory;
                                    Intent registerIntent =
                                            new Intent(MainActivity.this, RegisterActivity.class);
                                    startActivityForResult(
                                            registerIntent, MuzikoConstants.REQUEST_REGISTER_USER);
                                } else {
                                    AppController.toast(this, getString(R.string.no_internet_for_register));
                                }
                            }
                        };

                return true;

            case R.id.menu_logout:
                mPendingRunnable =
                        () -> {

                            new MaterialDialog.Builder(this)
                                    .theme(Theme.LIGHT)
                                    .titleColorRes(R.color.normal_blue)
                                    .negativeColorRes(R.color.dialog_negetive_button)
                                    .positiveColorRes(R.color.normal_blue)
                                    .title("Sign out?")
                                    .content("Are you sure you want to log out?")
                                    .positiveText("OK")
                                    .onPositive((dialog, which) -> {
                                        signOut();
                                    })
                                    .negativeText("Cancel")
                                    .show();

                            drawerId = R.id.menu_listening;
                            navigationView.setCheckedItem(drawerId);
                        };

                return true;

            case R.id.menu_settings:
                resetNavMenu = true;

                mPendingRunnable =
                        () -> {
                            Intent activityIntent =
                                    new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(activityIntent);
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        };

                return true;

            case R.id.menu_premium:
                BaseActivity baseActivity = BaseActivity.getBaseActivityInstance();
                if (baseActivity != null) {
                    baseActivity.buyPremium();
                }
                return true;

            case R.id.menu_rateus:
                resetNavMenu = true;

                RateUs rateUs = new RateUs();
                rateUs.open(this);

                drawerId = R.id.menu_listening;
                navigationView.setCheckedItem(drawerId);

                return true;

            case R.id.menu_about:
                resetNavMenu = true;

                Intent activityIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

                return true;

            default:
                return true;
        }
    }

    @DebugLog
    private void mainUpdate() {
        if (miniPlayer != null) {
            miniPlayer.updateUI();
        }
        updateNavDrawerHeader();
        setupDrawer();
    }

    @DebugLog
    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppController.INTENT_EXIT);
        filter.addAction(AppController.INTENT_CLEAR);

        filter.addAction(AppController.INTENT_TRACK_EDITED);
        filter.addAction(AppController.INTENT_TRACK_SEEKED);
        filter.addAction(AppController.INTENT_TRACK_SHUFFLE);
        filter.addAction(AppController.INTENT_TRACK_REPEAT);
        filter.addAction(AppController.INTENT_QUEUE_STOPPED);
        filter.addAction(INTENT_QUEUE_CHANGED);
        filter.addAction(AppController.INTENT_QUEUE_CLEARED);
        filter.addAction(AppController.INTENT_DOWNLOAD_PROGRESS);
        filter.addAction(AppController.ACTION_FIREBASE_OVERLIMIT);
        mainReceiver = new MainReceiver();
        registerReceiver(mainReceiver, filter);
    }

    @DebugLog
    private void unregister() {
        if (mainReceiver != null) {
            unregisterReceiver(mainReceiver);
            mainReceiver = null;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(final int position) {

        if (adapter.getCurrentItem() != position) {
            adapter.setCurrentItem(position);
            handler.postDelayed(() -> setupMenu(), 150);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    //	private void showIntroductoryOverlay() {
    //		if (mIntroductoryOverlay != null) {
    //			mIntroductoryOverlay.remove();
    //		}
    //		if ((menuItemCast != null) && menuItemCast.isVisible()) {
    //			new Handler().post(() -> {
    //				mIntroductoryOverlay = new IntroductoryOverlay.Builder(
    //						MainActivity.this, menuItemCast)
    //						.setTitleText("Introducing Google Cast")
    //						.setOverlayColor(R.color.normal_blue)
    //						.setSingleTime()
    //						.setOnOverlayDismissedListener(
    //								() -> mIntroductoryOverlay = null)
    //						.build();
    //				mIntroductoryOverlay.show();
    //			});
    //		}
    //	}

    @DebugLog
    @Override
    public void onLayoutChanged(float bottomMargin) {

        updateNavDrawerHeader();

        if (callbackTrack != null) {
            callbackTrack.onLayoutChanged(bottomMargin);
        }
        if (callbackArtist != null) {
            callbackArtist.onLayoutChanged(bottomMargin);
        }

        if (callbackAlbum != null) {
            callbackAlbum.onLayoutChanged(bottomMargin);
        }
        if (callbackGenre != null) {
            callbackGenre.onLayoutChanged(bottomMargin);
        }
    }

    @DebugLog
    public void updateFragments() {

        invalidateOptionsMenu();

        if (callbackTrack != null) {
            callbackTrack.onReload();
        }
        if (callbackArtist != null) {
            callbackArtist.onReload();
        }

        if (callbackAlbum != null) {
            callbackAlbum.onReload();
        }
        if (callbackGenre != null) {
            callbackGenre.onReload();
        }
    }

    @DebugLog
    @Override
    public void onClicked(int index, BoomButton boomButton) {
        if (index == 0) {
            handler.postDelayed(
                    () -> {
                        Intent activityIntent = new Intent(MainActivity.this, RecentActivity.class);
                        startActivity(activityIntent);
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                    },
                    boomMenuDelay);

        } else if (index == 1) {
            handler.postDelayed(
                    () -> {
                        Intent activityIntent =
                                new Intent(MainActivity.this, StorageActivity.class);
                        startActivity(activityIntent);
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                    },
                    boomMenuDelay);

        } else if (index == 2) {
            handler.postDelayed(
                    () -> {
                        Intent activityIntent =
                                new Intent(MainActivity.this, FavouritesActivity.class);
                        startActivity(activityIntent);
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                    },
                    boomMenuDelay);

        } else if (index == 3) {
            handler.postDelayed(
                    () -> {
                        Intent activityIntent =
                                new Intent(MainActivity.this, PlaylistActivity.class);
                        startActivity(activityIntent);
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                    },
                    boomMenuDelay);
        }
    }

    @Override
    public void onBackgroundClick() {
    }

    @Override
    public void onBoomWillHide() {
    }

    @Override
    public void onBoomDidHide() {
    }

    @Override
    public void onBoomWillShow() {
    }

    @Override
    public void onBoomDidShow() {
    }

    private class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();

                switch (action) {
                    case AppController.INTENT_TRACK_SEEKED:
                        if (miniPlayer != null) {
                            miniPlayer.updateUI();
                        }
                        break;
                    case INTENT_QUEUE_CHANGED:
                        if (miniPlayer != null) {
                            miniPlayer.updateUI();
                        }
                        if (callbackTrack != null) {
                            callbackTrack.onQueueChanged();
                        }
                        if (callbackHome != null) {
                            callbackHome.onQueueChanged();
                        }
                        break;
                    case AppController.INTENT_TRACK_REPEAT:
                        if (miniPlayer != null) {
                            miniPlayer.updateUI();
                        }
                        break;
                    case AppController.INTENT_TRACK_SHUFFLE:
                        if (miniPlayer != null) {
                            miniPlayer.updateUI();
                        }
                        break;
                    case AppController.INTENT_QUEUE_STOPPED:
                        if (miniPlayer != null) {
                            miniPlayer.layoutMiniPlayer();
                        }
                        break;
                    case AppController.INTENT_QUEUE_CLEARED:
                        if (miniPlayer != null) {
                            miniPlayer.layoutMiniPlayer();
                        }

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
                        String url = intent.getStringExtra("clipboardUrl");
                        int progress = intent.getIntExtra("progress", -1);

                        if (callbackTrack != null) {
                            callbackTrack.onDownloadProgress(url, progress);
                        }

                        break;

                    case ACTION_FIREBASE_OVERLIMIT:
                        if (callbackTrack != null) {
                            callbackTrack.onFirebaseLimitChanged();
                        }
                        break;
                }
            }
        }
    }

    //	private class MySessionManagerListener implements SessionManagerListener<CastSession> {
    //
    //		@Override
    //		public void onSessionEnded(CastSession session, int error) {
    //			if (session == mCastSession) {
    //				mCastSession = null;
    //			}
    //			invalidateOptionsMenu();
    //		}
    //
    //		@Override
    //		public void onSessionResumed(CastSession session, boolean wasSuspended) {
    //			mCastContext.addCastStateListener(mCastStateListener);
    //			mCastContext.getSessionManager().addSessionManagerListener(
    //					mSessionManagerListener, CastSession.class);
    //			if (mCastSession == null) {
    //				mCastSession = CastContext.getSharedInstance(MainActivity.this).getSessionManager()
    //						.getCurrentCastSession();
    //			}
    //			invalidateOptionsMenu();
    //		}
    //
    //		@Override
    //		public void onSessionStarted(CastSession session, String sessionId) {
    //			mCastSession = session;
    //			invalidateOptionsMenu();
    //		}
    //
    //		@Override
    //		public void onSessionStarting(CastSession session) {
    //		}
    //
    //		@Override
    //		public void onSessionStartFailed(CastSession session, int error) {
    //		}
    //
    //		@Override
    //		public void onSessionEnding(CastSession session) {
    //		}
    //
    //		@Override
    //		public void onSessionResuming(CastSession session, String sessionId) {
    //		}
    //
    //		@Override
    //		public void onSessionResumeFailed(CastSession session, int error) {
    //		}
    //
    //		@Override
    //		public void onSessionSuspended(CastSession session, int reason) {
    //		}
    //	}
}
