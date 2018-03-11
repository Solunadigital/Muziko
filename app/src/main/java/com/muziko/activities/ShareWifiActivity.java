package com.muziko.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.google.gson.reflect.TypeToken;
import com.jaredrummler.android.device.DeviceName;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.adapter.UserShareWifiAdapter;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.ShareSalut;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.controls.MiniPlayer;
import com.muziko.database.ShareRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.ArtworkHelper;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.PicassoScrollListener;
import com.muziko.interfaces.ShareListener;
import com.muziko.manager.AppController;
import com.muziko.manager.GsonManager;
import com.muziko.manager.MediaHelper;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.salut.Callbacks.SalutCallback;
import com.muziko.salut.Callbacks.SalutDataCallback;
import com.muziko.salut.Callbacks.SalutDeviceCallback;
import com.muziko.salut.Callbacks.SalutHostCallback;
import com.muziko.salut.Callbacks.SalutRegisterCallback;
import com.muziko.salut.Callbacks.SalutUploadCallback;
import com.muziko.salut.Salut;
import com.muziko.salut.SalutDataReceiver;
import com.muziko.salut.SalutDevice;
import com.muziko.salut.SalutFileDownloader;
import com.muziko.salut.SalutFileUploader;
import com.muziko.salut.SalutServiceData;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static com.muziko.MyApplication.isHost;
import static com.muziko.R.id.reverse;
import static com.muziko.manager.AppController.INTENT_SHARE_DOWNLOADED;

public class ShareWifiActivity extends BaseActivity implements View.OnClickListener, SalutDataCallback, ShareListener, SearchView.OnQueryTextListener, SalutUploadCallback, SalutRegisterCallback, SalutCallback, SalutHostCallback, SalutDeviceCallback {
    private final String TAG = ShareWifiActivity.class.getName();
	private final WeakHandler handler = new WeakHandler();
	private SlidingUpPanelLayout slidingUpPanelLayout;
	private MiniPlayer miniPlayer;
	private int clientCount = 0;
	private MainReceiver mainReceiver;
	private FastScrollRecyclerView mRecyclerView;
	private CoordinatorLayout coordinatorlayout;
	private RelativeLayout emptyLayout;
	private MenuItem menuItemSearch;
	private UserShareWifiAdapter mAdapter;
	private String data;
	private boolean reverseSort;
	private int sortId;
	private SalutDataReceiver dataReceiver;
	private SalutServiceData serviceData;
	private Salut network;
	private ArrayList<SalutDevice> registeredClients = new ArrayList<>();
	private final Runnable clientListener = new Runnable() {
		@Override
		public void run() {

			if (network != null) {
				if (isHost && clientCount != network.getReadableRegisteredDevices().size()) {
					clientCount = network.getReadableRegisteredDevices().size();
					registeredClients.clear();
					registeredClients.addAll(network.getReadableRegisteredDevices());
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	};
	private ShareSalut shareSalut;
	private Button wifiButton;
	private RelativeLayout trackLayout;
	private ImageView imageThumb;
	private TextView textTitle;
	private TextView textDesc;
	private QueueItem queueItem;
	private RelativeLayout progressLayout;
	private TextView progress;
	private ArrayList<QueueItem> queueItems = new ArrayList<>();
	private int downloadCount = 0;
	private String sendername;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_share_wifi);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		toolbar.setTitle("Share Wifi");
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		findViewsById();

		wifiButton.setOnClickListener(this);

		if (getIntent() != null) {

			data = getIntent().getStringExtra(MyApplication.ARG_DATA);
			queueItems = GsonManager.Instance().getGson().fromJson(data, new TypeToken<List<QueueItem>>() {
			}.getType());

			if (data != null) {

				trackLayout.setVisibility(View.GONE);
				isHost = true;

//				queueItem = TrackRealmHelper.getTrack(queueItems.get(0).getData());
//				textTitle.setText(queueItem.title);
//				textDesc.setText(queueItem.artist_name);
//				ImageManager.Instance().loadImageListSmall(queueItem, imageThumb, "share");

			} else {
				isHost = false;
				trackLayout.setVisibility(View.GONE);
			}


		}

		NpaLinearLayoutManager layoutList = new NpaLinearLayoutManager(this);

		emptyLayout.setVisibility(View.GONE);

		mAdapter = new UserShareWifiAdapter(this, registeredClients, isHost, TAG, this);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.addOnScrollListener(new PicassoScrollListener(this, TAG));
		mRecyclerView.setLayoutManager(layoutList);
		mRecyclerView.setAdapter(mAdapter);
		setupMainPlayer();

        /*Create a data receiver object that will bind the callback
		with some instantiated object from our app. */
		dataReceiver = new SalutDataReceiver(this, this);


        /*Populate the details for our awesome service. */
		serviceData = new SalutServiceData(getString(R.string.app_name), 60606,
				getString(R.string.app_name) + " - " + DeviceName.getDeviceInfo(this).model);

        /*Create an instance of the Salut class, with all of the necessary data from before.
        * We'll also provide a callback just in case a device doesn't support WiFi Direct, which
        * Salut will tell us about before we start trying to use methods.*/
		network = new Salut(this, dataReceiver, serviceData, this);

		EventBus.getDefault().register(this);
	}

    @Override
    public void onResume() {
        super.onResume();

        reload();
        register();
        mainUpdate();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
        unregister();
        EventBus.getDefault().unregister(this);
        if (isHost)
            network.stopNetworkService(true);
        else
            network.unregisterClient(false);

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
		}


		if (isHost) {

			if (MyApplication.shareUploaderList.size() > 0) {
				new MaterialDialog.Builder(ShareWifiActivity.this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Share in progress").content("This will cancel current uploads. Are you sure?").positiveText("YES").onPositive((dialog, which) -> {

					for (SalutFileUploader salutFileUploader : MyApplication.shareUploaderList.values()) {
						if (salutFileUploader != null) {
							salutFileUploader.cancel(true);
						}
					}
					MyApplication.shareUploaderList.clear();
					progressLayout.setVisibility(View.GONE);
					registeredClients.clear();
					mAdapter.notifyDataSetChanged();
					emptied();
					network.stopNetworkService(false);
					wifiButton.setText(R.string.start_wifi_direct);
					finish();
				}).negativeText("NO").show();

			} else {
				finish();
			}
		} else if (!isHost) {

			if (MyApplication.shareDownloaderList.size() > 0) {
				new MaterialDialog.Builder(ShareWifiActivity.this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Download in progress").content("This will cancel current downloads. Are you sure?").positiveText("YES").onPositive((dialog, which) -> {

					for (SalutFileDownloader salutFileDownloader : MyApplication.shareDownloaderList.values()) {
						if (salutFileDownloader != null) {
							salutFileDownloader.cancel(true);
						}
					}
					MyApplication.shareDownloaderList.clear();
					progressLayout.setVisibility(View.GONE);
					registeredClients.clear();
					mAdapter.notifyDataSetChanged();
					emptied();
					network.stopServiceDiscovery(true);
					wifiButton.setText(R.string.start_wifi_direct);
					finish();
				}).negativeText("NO").show();

			} else {
				finish();
			}
		} else {
			finish();
		}

	}

    @Override
    public void onPause() {

        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        super.onPause();
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

	private void findViewsById() {

        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        emptyLayout = findViewById(R.id.emptyLayout);
        mRecyclerView = findViewById(R.id.itemList);
        wifiButton = findViewById(R.id.wifibutton);

        trackLayout = findViewById(R.id.trackLayout);
        imageThumb = findViewById(R.id.imageThumb);
        textTitle = findViewById(R.id.textTitle);
        textDesc = findViewById(R.id.textDesc);
        progressLayout = findViewById(R.id.progressLayout);
        progress = findViewById(R.id.progress);
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
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.clear();
		getMenuInflater().inflate(R.menu.share_wifi_menu, menu);
		menuItemSearch = menu.findItem(R.id.share_search);

		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

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

			case R.id.share_search:
				return true;

			case reverse:
				if (item.isChecked()) {
					item.setChecked(false);
					reverseSort = false;
				} else {
					item.setChecked(true);
					reverseSort = true;
				}

				switch (sortId) {
					case R.id.contact_sort_title:
						if (!reverseSort) {
							mAdapter.sortTitleLowest();
						} else {
							mAdapter.sortTitleHighest();
						}
						break;
				}
				return true;

			case R.id.share_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

			case R.id.share_share:
				AppController.Instance().shareApp();
				return true;

			case R.id.share_exit:
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
        mAdapter.getFilter().filter(newText);

        return false;
    }

	private void setupNetwork() {
		if (!network.isRunningAsHost) {
			progressLayout.setVisibility(View.VISIBLE);
			emptyLayout.setVisibility(View.GONE);
			mRecyclerView.setVisibility(View.GONE);
			progress.setText(R.string.listening_for_friends);
			network.startNetworkService(this, this, this);
			wifiButton.setText(R.string.stop_wifi_direct);
		} else {

			if (MyApplication.shareUploaderList.size() > 0) {
				new MaterialDialog.Builder(ShareWifiActivity.this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Share in progress").content("This will cancel current uploads. Are you sure?").positiveText("YES").onPositive((dialog, which) -> {

					for (SalutFileUploader salutFileUploader : MyApplication.shareUploaderList.values()) {
						if (salutFileUploader != null) {
							salutFileUploader.cancel(true);
						}
					}
					progressLayout.setVisibility(View.GONE);
					MyApplication.shareUploaderList.clear();
					registeredClients.clear();
					mAdapter.notifyDataSetChanged();
					emptied();
					network.stopNetworkService(false);
					wifiButton.setText(R.string.start_wifi_direct);
				}).negativeText("NO").show();
			} else {
				progressLayout.setVisibility(View.GONE);
				registeredClients.clear();
				mAdapter.notifyDataSetChanged();
				emptied();
				network.stopNetworkService(false);
				wifiButton.setText(R.string.start_wifi_direct);
			}
		}
	}

	@Override
	public void onClick(View v) {

		if (!Salut.isWiFiEnabled(getApplicationContext())) {
            AppController.toast(this, "Please enable WiFi first");
            return;
		}

		if (v.getId() == R.id.wifibutton) {

			if (isHost) {
				setupNetwork();
			} else {
				discoverServices();
			}
		}
	}


	@Override
	public void onItemClicked(int position) {

		if (!isHost) {
			SalutDevice host = mAdapter.getItem(position);

			if (network.registeredHost != null) {
				String deviceName = network.registeredHost.deviceName;
                if (host.deviceName.equals(deviceName)) {
                    network.unregisterClient(false);
				} else {
					network.registerWithHost(host, this, this);
					sendername = host.deviceName;
				}
			} else {
				network.registerWithHost(host, this, this);
				sendername = host.deviceName;
			}
		}
	}

	@Override
	public void onMenuClicked(Context context, int position) {

	}

	@Override
	public void onBlockClicked(final int position) {

	}

	@Override
	public boolean onItemLongClicked(int position) {

		return false;
	}

	private void reload() {

		emptied();
	}

	private void emptied() {
		if (mAdapter.getItemCount() > 0) {
			mRecyclerView.setVisibility(View.VISIBLE);
			emptyLayout.setVisibility(View.GONE);
		} else {
			mRecyclerView.setVisibility(View.GONE);
			emptyLayout.setVisibility(View.VISIBLE);
		}

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
		filter.addAction(AppController.INTENT_SHARE_DOWNLOADED);

		mainReceiver = new MainReceiver();
		registerReceiver(mainReceiver, filter);
	}

	private void unregister() {

		if (mainReceiver != null) {
			unregisterReceiver(mainReceiver);
			mainReceiver = null;
		}
	}

	@Override
	public void onDataReceived(String data) {

//		network.unregisterClient(false);
//		registeredClients.clear();
		mAdapter.notifyDataSetChanged();
		emptied();

		MediaScannerConnection.scanFile(this,
				new String[]{data}, null,
				(path, uri) -> {

					MediaHelper.Instance().loadMusicFromTrack(data, false);

					QueueItem track = TrackRealmHelper.getTrack(data);

					if (track != null) {
						ShareRealmHelper.saveReceivedShare(track, sendername);

						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
						boolean prefArtworkDownload = prefs.getBoolean("prefArtworkDownload", false);
						if (prefArtworkDownload) {
							ArtworkHelper artworkHelper = new ArtworkHelper();
							artworkHelper.autoPickAlbumArt(ShareWifiActivity.this, track, true);
						}

						sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));
						sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
						sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
						sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));
					}

					EventBus.getDefault().post(new RefreshEvent(1000));

					Intent shareintent = new Intent(INTENT_SHARE_DOWNLOADED);
					shareintent.putExtra("data", track.data);
					sendBroadcast(shareintent);
				});
	}

	private void discoverServices() {
		if (!network.isRunningAsHost && !network.isDiscovering) {
			progressLayout.setVisibility(View.VISIBLE);
			progress.setText("Searching for friends...");
			emptyLayout.setVisibility(View.GONE);
			mRecyclerView.setVisibility(View.GONE);
			network.discoverNetworkServices(this, true);
			wifiButton.setText(R.string.stop_wifi_direct);
		} else {

			if (MyApplication.shareDownloaderList.size() > 0) {
				new MaterialDialog.Builder(ShareWifiActivity.this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Download in progress").content("This will cancel current downloads. Are you sure?").positiveText("YES").onPositive((dialog, which) -> {

					for (SalutFileDownloader salutFileDownloader : MyApplication.shareDownloaderList.values()) {
						if (salutFileDownloader != null) {
							salutFileDownloader.cancel(true);
						}
					}
					MyApplication.shareDownloaderList.clear();
					progressLayout.setVisibility(View.GONE);
					registeredClients.clear();
					mAdapter.notifyDataSetChanged();
					emptied();
					network.stopServiceDiscovery(true);
					wifiButton.setText(R.string.start_wifi_direct);
				}).negativeText("NO").show();
			} else {

				progressLayout.setVisibility(View.GONE);
				registeredClients.clear();
				mAdapter.notifyDataSetChanged();
				emptied();
				network.stopServiceDiscovery(true);
				wifiButton.setText(R.string.start_wifi_direct);
			}
		}
	}

	@Override
	public void onUploadSuccess(String message) {
        AppController.toast(this, message);
    }

	@Override
	public void onUploadFailure(String message) {
        AppController.toast(this, message);
    }

	@Override
	public void onRegisterSuccess(String message) {
        AppController.toast(this, message);
    }

	@Override
	public void onRegisterFailed(String message) {
        AppController.toast(this, message);
    }

	@Override
	public void onUnregisterSuccess(String message) {
        AppController.toast(this, message);
    }

	@Override
	public void onUnregisterFailed(String message) {
        AppController.toast(this, message);
    }

	@Override
	public void call() {

	}

	@Override
	public void onError(String message) {
        AppController.toast(this, message);
    }

	@Override
	public void onHostSuccess() {
//		Utils.toast(this, message);
		handler.postDelayed(clientListener, PlayerConstants.UPDATE_TIME);
	}

	@Override
	public void onHostError(String message) {
        AppController.toast(this, message);

//		WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//		wifiManager.setWifiEnabled(false);
//
//		wifiManager.setWifiEnabled(true);
//
//		network.startNetworkService(this, this, this);
	}

	@Override
	public void onClientConnected(SalutDevice device) {
		Log.d(TAG, device.readableName + " has connected!");
		registeredClients.clear();
		registeredClients.addAll(network.getReadableRegisteredDevices());
		mAdapter.notifyDataSetChanged();
		progressLayout.setVisibility(View.GONE);
		emptied();
		for (QueueItem queueItem : queueItems) {
			network.startUpload(ShareWifiActivity.this, device, queueItem, this);
			ShareRealmHelper.saveSendingShare(queueItem, device.deviceName);
		}

	}

	@Override
	public void onClientDisconnected(SalutDevice device) {
		Log.d(TAG, device.readableName + " has disconnected!");
		registeredClients.clear();
		registeredClients.addAll(network.getReadableRegisteredDevices());
		mAdapter.notifyDataSetChanged();
		progressLayout.setVisibility(View.GONE);
		emptied();
	}

	@Override
	public void onDeviceFound(SalutDevice device) {

		Log.d(TAG, "A device has connected with the name " + device.deviceName);
		registeredClients.add(device);
		mAdapter.notifyDataSetChanged();

		progressLayout.setVisibility(View.GONE);
		emptied();
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

						mAdapter.updateProgress(url, progress);

						break;
					case INTENT_SHARE_DOWNLOADED:
						String data = intent.getStringExtra("data");
						QueueItem queueItem = TrackRealmHelper.getTrack(data);

						if (queueItem != null && hasWindowFocus()) {
							new MaterialDialog.Builder(ShareWifiActivity.this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("New track downloaded").content(queueItem.title + " - " + queueItem.artist_name + "." + System.getProperty("line.separator") + "Would you like to play it now?").positiveText("YES").onPositive((dialog, which) -> {
								ArrayList<QueueItem> list = new ArrayList<>();
								AppController.Instance().playCurrentSong(queueItem);

							}).negativeText("NO").show();
						}
						break;
				}
			}
		}
	}
}
