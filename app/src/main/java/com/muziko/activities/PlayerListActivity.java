package com.muziko.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.adapter.PlayerListPagerAdapter;
import com.muziko.callbacks.PlayerCallback;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.AdvancedSearchButton;
import com.muziko.controls.CustomToolbar;
import com.muziko.controls.MiniPlayer;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.SetRingtone;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.ArtworkHelper;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.ImageManager;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.tasks.FavoriteEdit;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static android.R.attr.tag;
import static android.R.attr.type;
import static com.muziko.MyApplication.IMAGE_LARGE_SIZE;
import static com.muziko.MyApplication.IMAGE_MEDIUM_SIZE;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_ALBUM_FOLDER;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_GALLERY;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_ID3_TAGS;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_INTERNET;
import static com.muziko.manager.MuzikoConstants.CODE_WRITE_SETTINGS_PERMISSION;

public class PlayerListActivity extends BaseActivity implements View.OnClickListener, ActionMode.Callback, SearchView.OnCloseListener, SearchView.OnQueryTextListener, ViewPager.OnPageChangeListener {
    private final String TAG = PlayerListActivity.class.getName();
	private final WeakHandler handler = new WeakHandler();
	public PlayerCallback callbackTrack;
	public PlayerCallback callbackAlbum;
	private SlidingUpPanelLayout slidingUpPanelLayout;
	private MiniPlayer miniPlayer;
	private MainReceiver mainReceiver;
    private ImageView coverArtImage;
    private TextView albumName, albumDetail, songLength, albumCount;
    private RelativeLayout playButton;
	private ImageButton changeButton;
	private FloatingActionButton shuffle;
	private MenuItem menuItemSearch;
	private MenuItem menuItemgridone;
	private MenuItem menuItemgridtwo;
	private MenuItem menuItemgridthree;
	private MenuItem menuItemgridfour;
	private CoordinatorLayout coordinatorlayout;
	private String TAB_VALUE = "";
	private CollapsingToolbarLayout collapsingToolbarLayout;
	private FrameLayout coverArtLayout;
	private AppBarLayout appBarLayout;
	private ActionMode actionMode = null;
	private long playArt = 0;
	private int playType = 0;
	private String playName = "";
	private String playData;
	private int playDuration = 0;
	private int playSongs = 0;
	private boolean isFaving = false;
	private ArtistsReceiver receiver;
	private boolean reverseSort;
	private int sortId;
	private QueueItem queueitem;
	private InterstitialAd mInterstitialAd;
	private AdListener mAdlistener;
	private QueueItem selectedItem;
	private int selectedItemPosition;
	private TabLayout tabs;
	private ViewPager pager;
	private PlayerListPagerAdapter adapter;
	private CustomToolbar toolbar;
	private ImageButton advancedSearch;
	private int advancedSearchInset;
	private QueueItem queueItem;
	private Target picassoTarget = new Target() {
		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			// loading of the bitmap was a success
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, MyApplication.JPEG_COMPRESS, stream);
			byte[] byteArray = stream.toByteArray();
			ArtworkHelper artworkHelper = new ArtworkHelper();
			artworkHelper.setArt(PlayerListActivity.this, queueItem, byteArray);
            ImageManager.Instance().loadImage(playArt, coverArtImage);
            EventBus.getDefault().post(new RefreshEvent(1000));
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			// loading of the bitmap failed
			// TODO do some action/warning/error message
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {

		}
	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player_list);
		findViewsById();
        toolbar = findViewById(R.id.toolbar);
        advancedSearchInset = toolbar.getContentInsetStartWithNavigation();
		toolbar.setTitle("Muziko");
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, R.color.normal_blue));
		collapsingToolbarLayout.setStatusBarScrimColor(ContextCompat.getColor(this, R.color.normal_blue));

		if (getIntent() != null) {
			long playId = getIntent().getLongExtra(MyApplication.ARG_ID, 0);
			playArt = getIntent().getLongExtra(MyApplication.ARG_ART, 0);
			playType = getIntent().getIntExtra(MyApplication.ARG_TYPE, 0);
			playName = getIntent().getStringExtra(MyApplication.ARG_NAME);
			playData = getIntent().getStringExtra(MyApplication.ARG_DATA);
			String duration = getIntent().getStringExtra(MyApplication.ARG_DURATION);
			if (duration != null) {
				playDuration = Integer.parseInt(duration);
			}

			playSongs = getIntent().getIntExtra(MyApplication.ARG_SONGS, 0);

			if (Utils.isEmptyString(playData)) {
                playArt = PrefsManager.Instance().getLastPlayerListArt();
                playType = PrefsManager.Instance().getLastPlayerListType();
                playName = PrefsManager.Instance().getLastPlayerListName();
                playData = PrefsManager.Instance().getLastPlayerListData();
                playDuration = PrefsManager.Instance().getLastPlayerListtDuration();
                playSongs = PrefsManager.Instance().getLastPlayerListSongs();
            } else {
                PrefsManager.Instance().setLastPlayerListArt(playArt);
                PrefsManager.Instance().setLastPlayerListType(playType);
                PrefsManager.Instance().setLastPlayerListName(playName);
                PrefsManager.Instance().setLastPlayerListData(playData);
                PrefsManager.Instance().setLastPlayerListDuration(playDuration);
                PrefsManager.Instance().setLastPlayerListSongs(playSongs);
            }
		}
		String mainTitle;
		if (playName.length() > 0)
			mainTitle = playName;
		else
			mainTitle = "Muziko";

		toolbar.setTitle(mainTitle);


		adapter = new PlayerListPagerAdapter(getSupportFragmentManager(), playType, playName, playData, playType == PlayerConstants.QUEUE_TYPE_ALBUMS ? 1 : 2);
		pager.setOffscreenPageLimit(2);
		pager.setAdapter(adapter);
		tabs.setupWithViewPager(pager);
		pager.addOnPageChangeListener(this);
		TAB_VALUE = MyApplication.TRACKS;


		setupMainPlayer();
		mainUpdate();

		isFaving = false;

		shuffle.setOnClickListener(this);
		changeButton.setOnClickListener(this);
		EventBus.getDefault().register(this);
	}

    @Override
    public void onResume() {
        super.onResume();
        reload();

        register();

    }

	@Override
	public void onDestroy() {
		handler.removeCallbacksAndMessages(null);
		if (miniPlayer != null) miniPlayer.onDestroy();
		unregister();
		callbackTrack = null;
		callbackAlbum = null;
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARTWORK_PICK_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = Utils.getPath(PlayerListActivity.this, uri);
                    if (path != null) {

                        Bitmap bMap = BitmapFactory.decodeFile(path);
                        Bitmap out = Utils.resize(bMap, IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        out.compress(Bitmap.CompressFormat.JPEG, MyApplication.JPEG_COMPRESS, stream);
                        bMap.recycle();
                        out.recycle();
                        byte[] bitmapData = stream.toByteArray();

                        if (bitmapData != null) {
                            ArtworkHelper artworkHelper = new ArtworkHelper();
                            artworkHelper.setArt(PlayerListActivity.this, queueitem, bitmapData);
                            loadcoverArt();
//							playerlistAdapter.notifyDataSetChanged();
                            EventBus.getDefault().post(new RefreshEvent(1000));
                        } else {
                            AppController.toast(PlayerListActivity.this, "Unable to read file!");
                        }
                    } else {
                        AppController.toast(PlayerListActivity.this, "File not available on device!");
                    }
                }

                break;

            case ARTWORK_PICK_FROM_INTERNET:
                if (resultCode == Activity.RESULT_OK) {


                    queueItem = (QueueItem) data.getSerializableExtra("item");
                    final int position = data.getIntExtra("index", 0);
                    String filePath = data.getStringExtra("filepath");
                    if (filePath != null) {

                        Picasso.with(PlayerListActivity.this)
                                .load(filePath)
                                .resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
                                .into(picassoTarget);
                    } else {
                        AppController.toast(PlayerListActivity.this, "Album Art not found");
                    }

                }
                if (resultCode == Activity.RESULT_CANCELED) {

                }

                break;

            case ARTWORK_PICK_FROM_ALBUM_FOLDER:
                if (resultCode == Activity.RESULT_OK) {
                    queueItem = (QueueItem) data.getSerializableExtra("item");
                    final int position = data.getIntExtra("index", 0);
                    String filePath = data.getStringExtra("filepath");

                    if (filePath != null) {

                        Picasso.with(PlayerListActivity.this)
                                .load(filePath)
                                .resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
                                .into(picassoTarget);
                    } else {
                        AppController.toast(PlayerListActivity.this, "Album Art not found");
                    }

                }
                if (resultCode == Activity.RESULT_CANCELED) {

                }

                break;

            case ARTWORK_PICK_FROM_ID3_TAGS:
                if (resultCode == Activity.RESULT_OK) {

                    QueueItem queueItem = (QueueItem) data.getSerializableExtra("item");
                    final int position = data.getIntExtra("index", 0);
                    ArtworkHelper artworkHelper = new ArtworkHelper();
                    byte[] bitmapData = artworkHelper.pickAlbumArt(PlayerListActivity.this, queueItem);

                    if (queueItem != null && bitmapData != null) {
                        artworkHelper.setArt(PlayerListActivity.this, queueItem, bitmapData);
                        loadcoverArt();
//						playerlistAdapter.notifyDataSetChanged();
                        EventBus.getDefault().post(new RefreshEvent(1000));
                    } else {
                        AppController.toast(PlayerListActivity.this, "Album Art not found");
                    }

                }
                if (resultCode == Activity.RESULT_CANCELED) {

                }

                break;


            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == Activity.RESULT_OK) {

                    Uri uri = result.getUri();
                    String path = Utils.getPath(PlayerListActivity.this, uri);
                    if (path != null) {

                        Bitmap bMap = BitmapFactory.decodeFile(path);
                        Bitmap out = Utils.resize(bMap, IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        out.compress(Bitmap.CompressFormat.JPEG, MyApplication.JPEG_COMPRESS, stream);
                        bMap.recycle();
                        out.recycle();
                        byte[] bitmapData = stream.toByteArray();

                        if (bitmapData != null) {
                            ArtworkHelper artworkHelper = new ArtworkHelper();
                            artworkHelper.setArt(PlayerListActivity.this, queueitem, bitmapData);
                            loadcoverArt();
//							playerlistAdapter.notifyDataSetChanged();
                            EventBus.getDefault().post(new RefreshEvent(1000));
                        } else {
                            AppController.toast(PlayerListActivity.this, "Unable to read file!");
                        }
                    } else {
                        AppController.toast(PlayerListActivity.this, "File not available on device!");
                    }

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Crashlytics.logException(error);
                    AppController.toast(PlayerListActivity.this, "Album art was not changed");
                }

                break;
            case CODE_WRITE_SETTINGS_PERMISSION:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(this)) {
                        SetRingtone createRingtone = new SetRingtone();
                        createRingtone.open(PlayerListActivity.this, selectedItem);
                    } else {
                        AppController.toast(this, getString(R.string.ringtone_permissions));
                    }
                } else {

                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(PlayerListActivity.this, selectedItem);
                }
                break;
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
    protected void onPause() {

        super.onPause();
        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        unregister();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.playerlist_menu, menu);

		menuItemSearch = menu.findItem(R.id.player_search);
		menuItemgridone = menu.findItem(R.id.grid_one);
		menuItemgridtwo = menu.findItem(R.id.grid_two);
		menuItemgridthree = menu.findItem(R.id.grid_three);
		menuItemgridfour = menu.findItem(R.id.grid_four);

//		menuItemCast = CastButtonFactory.setUpMediaRouteButton(this, menu,
//				R.id.media_route_menu_item);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		setupMenu();
		updateGridMenu();

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

			case R.id.player_search:
				return true;

			case R.id.grid_one:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);
				switch (TAB_VALUE) {
					case MyApplication.TRACKS:
                        PrefsManager.Instance().setPlayerListTracksViewType(0);
                        break;
					case MyApplication.ALBUMS:
                        PrefsManager.Instance().setPlayerListAlbumsViewType(0);
                        break;
				}

				updateListing();

				return true;

			case R.id.grid_two:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);
				switch (TAB_VALUE) {
					case MyApplication.TRACKS:
                        PrefsManager.Instance().setPlayerListTracksViewType(1);
                        break;
					case MyApplication.ALBUMS:
                        PrefsManager.Instance().setPlayerListAlbumsViewType(1);
                        break;
				}
				updateListing();

				return true;

			case R.id.grid_three:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);
				switch (TAB_VALUE) {
					case MyApplication.TRACKS:
                        PrefsManager.Instance().setPlayerListTracksViewType(2);
                        break;
					case MyApplication.ALBUMS:
                        PrefsManager.Instance().setPlayerListAlbumsViewType(2);
                        break;
				}
				updateListing();

				return true;

			case R.id.grid_four:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);
				switch (TAB_VALUE) {
					case MyApplication.TRACKS:
                        PrefsManager.Instance().setPlayerListTracksViewType(3);
                        break;
					case MyApplication.ALBUMS:
                        PrefsManager.Instance().setPlayerListAlbumsViewType(3);
                        break;
				}
				updateListing();

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
                        new Intent(PlayerListActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
			case R.id.player_exit:
				AppController.Instance().exit();

				return true;
			default:
				return false;   //super.onOptionsItemSelected(item);
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

    private void reload() {

        if (playType == PlayerConstants.QUEUE_TYPE_ALBUMS) {
            changeButton.setVisibility(View.VISIBLE);
            queueitem = TrackRealmHelper.getTrackByAlbum(playArt);
        }

        loadcoverArt();

        albumName.setText(playName);
        String message = Utils.getLongDuration(playDuration);

        albumDetail.setText(getString(R.string.duration_title) + message);
        songLength.setText(String.format("%d song%s", playSongs, playSongs != 1 ? "s" : ""));
    }

    public void updateFragments() {
        if (callbackTrack != null) {
            callbackTrack.onReload();
        }

        if (callbackAlbum != null) {
            callbackAlbum.onReload();
        }

    }

    private void loadcoverArt() {

        QueueItem queue = TrackRealmHelper.getTrackByAlbum(playArt);

        if (queue != null) {
            Picasso.with(this)
                    .load("content://media/external/audio/albumart/" + queue.album)
                    .tag(tag)
                    .fit()
                    .error(R.mipmap.placeholder)
                    .centerCrop()
                    .into(coverArtImage);
        }
        toolbar.setItemColor(Color.WHITE);
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

    private void setupMainPlayer() {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        final LinearLayout mainPlayerLayout = findViewById(R.id.mainPlayerLayout);

        miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout);
    }

    private void findViewsById() {
        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        coverArtLayout = findViewById(R.id.coverArtLayout);
        appBarLayout = findViewById(R.id.appBarLayout);
        coverArtImage = findViewById(R.id.coverArtImage);
        pager = findViewById(R.id.vpPager);
        tabs = findViewById(R.id.tabs);
        albumName = findViewById(R.id.albumName);
        albumDetail = findViewById(R.id.albumDetails);
        songLength = findViewById(R.id.songLength);
        changeButton = findViewById(R.id.changeButton);
        shuffle = findViewById(R.id.fab);
    }

    private void mainUpdate() {
        miniPlayer.updateUI();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        MyApplication.searchString = newText;
        search(newText);
        return false;
    }

    @Override
    public boolean onClose() {
        advancedSearch.setVisibility(View.GONE);
        toolbar.setContentInsetStartWithNavigation(advancedSearchInset);
        return false;
    }

	private void updateGridMenu() {
		switch (TAB_VALUE) {
			case MyApplication.TRACKS:
                if (PrefsManager.Instance().getPlayerListTracksViewType() == 0) {
                    menuItemgridone.setChecked(true);
                } else if (PrefsManager.Instance().getPlayerListTracksViewType() == 1) {
                    menuItemgridtwo.setChecked(true);
                } else if (PrefsManager.Instance().getPlayerListTracksViewType() == 2) {
                    menuItemgridthree.setChecked(true);
                } else if (PrefsManager.Instance().getPlayerListTracksViewType() == 3) {
                    menuItemgridfour.setChecked(true);
				}
				break;
			case MyApplication.ALBUMS:
                if (PrefsManager.Instance().getPlayerListAlbumsViewType() == 0) {
                    menuItemgridone.setChecked(true);
                } else if (PrefsManager.Instance().getPlayerListAlbumsViewType() == 1) {
                    menuItemgridtwo.setChecked(true);
                } else if (PrefsManager.Instance().getPlayerListAlbumsViewType() == 2) {
                    menuItemgridthree.setChecked(true);
                } else if (PrefsManager.Instance().getPlayerListAlbumsViewType() == 3) {
                    menuItemgridfour.setChecked(true);
				}
				break;
		}
	}

	private void updateListing() {
		if (callbackTrack != null)
			callbackTrack.onListingChanged();
		if (callbackAlbum != null)
			callbackAlbum.onListingChanged();

	}

	private void search(String newText) {
		PlayerConstants.QUEUE_TYPE = 0;

		if (callbackTrack != null) {
			callbackTrack.onSearchQuery(newText);
		}

		if (callbackAlbum != null) {
			callbackAlbum.onSearchQuery(newText);
		}

	}

	private void setupMenu() {
		SearchView searchView = null;

		if (menuItemSearch != null)
			searchView = (SearchView) menuItemSearch.getActionView();

		if (TAB_VALUE.isEmpty()) {
			TAB_VALUE = MyApplication.TRACKS;
		}

		switch (TAB_VALUE) {

			case MyApplication.TRACKS:
				if (searchView != null) searchView.setQueryHint("Search song or artist");

				break;

			case MyApplication.ALBUMS:

				if (searchView != null) {
					if (TAB_VALUE.equals(MyApplication.ALBUMS))
						searchView.setQueryHint("Search album");
				}
				break;

		}
	}

	private void onFilterValue(int value, boolean reverse) {

	}

	@Override
	public void onClick(View view) {
		final ArtworkHelper artworkHelper = new ArtworkHelper();

		if (view == playButton) {
//			MyApplication.play(PlayerListActivity.this, 0, 0, playerlistAdapter.getTrackList());
		} else if (view == changeButton) {
			new MaterialDialog.Builder(this)
					.theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).title("Manage Artwork")
					.items(R.array.manage_artwork)
					.itemsCallback((dialog, view1, which, text) -> {

						switch (which) {

							case 0:

								artworkHelper.autoPickAlbumArt(PlayerListActivity.this, queueitem, false);
								loadcoverArt();
								EventBus.getDefault().post(new RefreshEvent(1000));
								break;

							case 1:

								Intent internetintent = new Intent(PlayerListActivity.this, AlbumArtInternetActivity.class);
								internetintent.putExtra("tag", TAG);
								internetintent.putExtra("item", queueitem);
								internetintent.putExtra("index", 0);
								startActivityForResult(internetintent, ARTWORK_PICK_FROM_INTERNET);

								break;

							case 2:

								Intent intent = new Intent(Intent.ACTION_PICK);
								intent.setType("image/*");

								try {
									startActivityForResult(Intent.createChooser(intent, "Select album art"), ARTWORK_PICK_FROM_GALLERY);
								} catch (android.content.ActivityNotFoundException ex) {
									// Potentially direct the user to the Market with a Dialog
                                    AppController.toast(PlayerListActivity.this, "Please install a File Manager.");
                                }
								break;

							case 3:
								Intent folderintent = new Intent(PlayerListActivity.this, AlbumArtFolderActivity.class);
								folderintent.putExtra("tag", TAG);
								folderintent.putExtra("item", queueitem);
								folderintent.putExtra("index", 0);
								startActivityForResult(folderintent, ARTWORK_PICK_FROM_ALBUM_FOLDER);

								break;

							case 4:

								Intent albumartIntent = new Intent(PlayerListActivity.this, AlbumArtID3Activity.class);
								albumartIntent.putExtra("tag", TAG);
								albumartIntent.putExtra("item", queueitem);
								albumartIntent.putExtra("index", 0);
								startActivityForResult(albumartIntent, ARTWORK_PICK_FROM_ID3_TAGS);
								break;

							case 5:
								Uri myUri = Uri.parse("content://media/external/audio/albumart/" + queueitem.album);
								CropImage.activity(myUri)
										.setGuidelines(CropImageView.Guidelines.ON)
										.setActivityTitle(queueitem.album_name)
										.start(PlayerListActivity.this);

								break;

							case 6:
								artworkHelper.removeArt(PlayerListActivity.this, queueitem);
								loadcoverArt();
								updateListing();
								EventBus.getDefault().post(new RefreshEvent(1000));
								break;

						}
					})
					.show();
		} else if (view == shuffle) {


			ArrayList<QueueItem> playList = new ArrayList<>();
			if (playType != 0) {
				switch (playType) {
					case PlayerConstants.QUEUE_TYPE_ALBUMS:

						playList.clear();
						playList.addAll(TrackRealmHelper.getTracksForAlbum(playData));
						break;
					case PlayerConstants.QUEUE_TYPE_ARTISTS:

						playList.clear();
						playList.addAll(TrackRealmHelper.getTracksForArtist(playData));

						break;
					case PlayerConstants.QUEUE_TYPE_GENRES:

						playList.clear();
						playList.addAll(TrackRealmHelper.getTracksForGenre(playData));

						break;
				}
			}

			long seed = System.nanoTime();
			Collections.shuffle(playList, new Random(seed));

			AppController.Instance().play(PlayerConstants.QUEUE_TYPE_TRACKS, 0, playList);
			updateListing();
		} else if (view == advancedSearch) {
			Intent intent = new Intent(this, SearchActivity.class);
			startActivity(intent);
		} else {
			advancedSearch.setVisibility(View.VISIBLE);
			toolbar.setContentInsetStartWithNavigation(0);
		}
	}

	@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE_WRITE_SETTINGS_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(PlayerListActivity.this, selectedItem);
                } else {
                    AppController.toast(this, "Write settings permission wasn't provided. Muziko can't set default ringtone");
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

	@Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.context_default, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		actionMode = mode;

		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//		ArrayList<QueueItem> list = playerlistAdapter.getSelectedItems();
//		if (list.size() > 0) {
//
//			switch (item.getItemId()) {
//
//				case R.id.play:
//					MyApplication.clearAddToQueue(this, list);
//					break;
//
//				case R.id.share:
//					MyApplication.shareSongs(this, list);
//					break;
//
//				case R.id.send:
//					MyApplication.sendTracks(this, list);
//					break;
//
//				case R.id.send_wifi:
//					MyApplication.sendTracksWifi(this, list);
//					break;
//
//				case R.id.add_to_queue:
//					MyApplication.addToQueue(PlayerListActivity.this, list, false);
//					break;
//
//				case R.id.play_next:
//					MyApplication.addToQueue(PlayerListActivity.this, list, true);
//					break;
//
//				case R.id.add_to_playlist:
//					MyApplication.addToPlaylist(PlayerListActivity.this, list, false);
//					break;
//
//				case R.id.delete:
//					deleteItems(list);
//					break;
//
//				case R.id.multi_tag_edit:
//					MyApplication.multiTagEdit(this, list);
//					break;
//
//				case R.id.trash:
//					movetoNegative(list);
//					break;
//
//				case R.id.favourite:
//					favorite(list);
//					break;
//
//				default:
//					return false;
//			}
//		}
//
//		mode.finish();
//		actionMode = null;
		return true;
	}

	@Override
    public void onDestroyActionMode(ActionMode mode) {
//		((SelectableAdapter) recyclerView.getAdapter()).setMultiSelect(false);
        actionMode = null;
    }

	public void enableTabs(boolean b) {
		Utils.enableDisableViewGroup(tabs, b);
	}

	private void favorite(final ArrayList<QueueItem> queueItems) {

		for (int i = 0; i < queueItems.size(); i++) {
			QueueItem queueItem = queueItems.get(i);
			TrackRealmHelper.addFavorite(queueItem.data);
		}

        AppController.toast(this, "Songs added to Favorites");
        sendBroadcast(new Intent(AppController.INTENT_FAVOURITE_CHANGED));
		if (type != PlayerConstants.QUEUE_TYPE_TRACKS) {
            sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
            sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));
        }
	}

	private void movetoNegative(final ArrayList<QueueItem> queueItems) {
		Utils.askDelete(this, "Move to Ignore Folder", "You can later restore it from ignore folder, are you sure you want to ignore it?", () -> {

			for (int i = 0; i < queueItems.size(); i++) {
				QueueItem queueItem = queueItems.get(i);
				TrackRealmHelper.movetoNegative(queueItem);
			}
			EventBus.getDefault().post(new RefreshEvent(1000));
		});
	}

	private void movetoNegative(final int position, final QueueItem queue) {
		Utils.askDelete(this, "Move to Ignore Folder", "You can later restore it from ignore folder, are you sure you want to ignore it?", () -> {

			TrackRealmHelper.movetoNegative(queue);
//			playerlistAdapter.removeIndex(position);
			EventBus.getDefault().post(new RefreshEvent(1000));
		});
	}

	private void favorite(final int position, QueueItem queue) {
		if (isFaving) return;
		isFaving = true;

		FavoriteEdit fe = new FavoriteEdit(this, 0, s -> {
			isFaving = false;

//				playerlistAdapter.notifyItemChanged(position);
		});
		fe.execute(queue);
	}

	private void register() {
		receiver = new ArtistsReceiver();

		IntentFilter artistfilter = new IntentFilter();
		artistfilter.addAction(AppController.INTENT_TRACK_EDITED);
		registerReceiver(receiver, artistfilter);

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
		if (receiver != null) {
			unregisterReceiver(receiver);
			receiver = null;
		}

		if (mainReceiver != null) {
			unregisterReceiver(mainReceiver);
			mainReceiver = null;
		}
	}

	private class ArtistsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent != null) {
					String action = intent.getAction();

					if (action.equals(AppController.INTENT_TRACK_EDITED)) {
						reload();
					}
				}
			} catch (Exception ex) {
				Crashlytics.logException(ex);
			}
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
