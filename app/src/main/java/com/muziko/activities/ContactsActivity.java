package com.muziko.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.muziko.R;
import com.muziko.adapter.ContactsPagerAdapter;
import com.muziko.callbacks.ContactsCallback;
import com.muziko.common.events.FirebaseRefreshEvent;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.MiniPlayer;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.ContactManager;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.service.MuzikoFirebaseService;
import com.oasisfeng.condom.CondomContext;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hugo.weaving.DebugLog;

import static com.muziko.manager.MuzikoConstants.INSERT_CONTACT_REQUEST;
import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;

public class ContactsActivity extends BaseActivity implements ViewPager.OnPageChangeListener, ContactManager.ContactHelperListener, SearchView.OnQueryTextListener {

	private final WeakHandler handler = new WeakHandler();
	public ContactsCallback callbackContacts;
	public ContactsCallback callbackAllowed;
	public ContactsCallback callbackBlocked;
	private MenuItem menuItemView;
	private SlidingUpPanelLayout slidingUpPanelLayout;
	private MiniPlayer miniPlayer;
	private MainReceiver mainReceiver;
	private boolean alreadyResumed = false;
	private String TAB_VALUE = "";
	private TabLayout tabs;
	private ViewPager pager;
	private MenuItem menuItemSearch;
	private MenuItem menu_sort_title;
	private MenuItem menu_sort_online;
	private MenuItem menuItemFilter;
	private CoordinatorLayout coordinatorlayout;
	private Toolbar toolbar;
	private boolean reverseSort;
	private int sortId;
	private ContactsPagerAdapter adapter;
	private AppBarLayout appBarLayout;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_contacts);
		findViewsById();

		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle("My Contacts");

		adapter = new ContactsPagerAdapter(getSupportFragmentManager());
		pager.setOffscreenPageLimit(3);
		pager.setAdapter(adapter);
		tabs.setupWithViewPager(pager);
		tabs.getTabAt(0).setIcon(R.drawable.contacts);
		tabs.getTabAt(1).setIcon(R.drawable.allowed);
		tabs.getTabAt(2).setIcon(R.drawable.blocked);
		tabs.setTabGravity(TabLayout.GRAVITY_FILL);
		pager.addOnPageChangeListener(this);

		setupMainPlayer();
		EventBus.getDefault().register(this);

		check();
	}

	@Override
	public void onResume() {
		super.onResume();

		PlayerConstants.QUEUE_TYPE = 0;

		register();
		mainUpdate();

		if (pager != null) {
            pager.setCurrentItem(PrefsManager.Instance().getLastRecentActivityTab());
        }

		alreadyResumed = true;
	}

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
        unregister();
        EventBus.getDefault().unregister(this);
        ContactManager.Instance().removeListener();
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == INSERT_CONTACT_REQUEST) {
            contactSync();
        }
    }

//	private boolean isAppBarCollapsed() {
//		final int appBarVisibleHeight = (int) (appBarLayout.getY() + appBarLayout.getHeight());
//		final int toolbarHeight = toolbar.getHeight();
//		return (appBarVisibleHeight == toolbarHeight);
//	}
//
//	@Override
//	public void setExpanded(boolean expanded) {
//		if (expanded) {
//			if (isAppBarCollapsed()) {
//				appBarLayout.setExpanded(expanded, true);
//			}
//		} else {
//			if (!isAppBarCollapsed()) {
//				appBarLayout.setExpanded(expanded, true);
//			}
//		}
//	}

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

    private void check() {
        // Here, thisActivity is the current activity
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
            permissionsNeeded.add("Read Contacts");
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                Utils.alertNoDismiss(this, getString(R.string.app_name), message, () -> {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(ContactsActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                });

                return;
            }
            ActivityCompat.requestPermissions(ContactsActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } else {
            ContactManager.Instance().init(CondomContext.wrap(this, "Muziko"));
            ContactManager.Instance().addListener(this);
        }
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
    }

    public void fastScrolling(boolean start) {
        if (start) {
            appBarLayout.setExpanded(false, true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted

                    ContactManager.Instance().init(CondomContext.wrap(this, "Muziko"));
                    ContactManager.Instance().addListener(this);

                } else {
                    // Permission Denied
                    new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).positiveColorRes(R.color.normal_blue).title("Permission not provided").content("Read contacts permission is required to share tracks with friends.").positiveText("OK").onPositive((dialog, which) -> {
                        finish();
                    }).cancelable(false).show();
                }
            }
            break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

	private void findViewsById() {
		coordinatorlayout = findViewById(R.id.coordinatorlayout);
		appBarLayout = findViewById(R.id.appBarLayout);
		toolbar = findViewById(R.id.toolbar);
		pager = findViewById(R.id.vpPager);
		// Bind the tabs to the ViewPager
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
		alreadyResumed = true;
	}

	// This method will be called when a MessageEvent is posted (in the UI thread for Toast)
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onFirebaseEvent(FirebaseRefreshEvent event) {

		handler.postDelayed(() -> {

			if (callbackContacts != null) {
				callbackContacts.onReload();
			}
			if (callbackAllowed != null) {
				callbackAllowed.onReload();
			}

			if (callbackBlocked != null) {
				callbackBlocked.onReload();
			}
		}, event.delay);

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

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBufferingEvent(BufferingEvent event) {

        if (miniPlayer != null) {
            miniPlayer.showBufferingMessage(event.getMessage(), event.isClose());
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_menu, menu);
		menuItemFilter = menu.findItem(R.id.contact_filter);
		menuItemSearch = menu.findItem(R.id.contact_search);
		menu_sort_title = menu.findItem(R.id.contact_sort_title);
		menu_sort_online = menu.findItem(R.id.contact_sort_online);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu_sort_online.setChecked(true);

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


			case R.id.contact_add:

				Intent contactIntent = new Intent(Intent.ACTION_INSERT);
				contactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
				contactIntent.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
				startActivityForResult(contactIntent, INSERT_CONTACT_REQUEST);

				return true;

			case R.id.contact_search:
				return true;

			case R.id.contact_sort_title:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);

				sortId = item.getItemId();
				if (callbackContacts != null) {
					callbackContacts.onFilterValue(item.getItemId(), reverseSort);
				}
				if (callbackAllowed != null) {
					callbackAllowed.onFilterValue(item.getItemId(), reverseSort);
				}
				if (callbackBlocked != null) {
					callbackBlocked.onFilterValue(item.getItemId(), reverseSort);
				}

				return true;

			case R.id.contact_sort_online:
				if (item.isChecked()) item.setChecked(false);
				else item.setChecked(true);

				sortId = item.getItemId();
				if (callbackContacts != null) {
					callbackContacts.onFilterValue(item.getItemId(), reverseSort);
				}
				if (callbackAllowed != null) {
					callbackAllowed.onFilterValue(item.getItemId(), reverseSort);
				}
				if (callbackBlocked != null) {
					callbackBlocked.onFilterValue(item.getItemId(), reverseSort);
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
				if (callbackContacts != null) {
					callbackContacts.onFilterValue(sortId, reverseSort);
				}
				if (callbackAllowed != null) {
					callbackAllowed.onFilterValue(sortId, reverseSort);
				}
				if (callbackBlocked != null) {
					callbackBlocked.onFilterValue(sortId, reverseSort);
				}

				return true;

			case R.id.contact_sync:
				contactSync();
				return true;

			case R.id.contact_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

			case R.id.contact_share:
				AppController.Instance().shareApp();
				return true;
			case R.id.sharing_wifi:
				Intent shareIntent =
						new Intent(ContactsActivity.this, ShareWifiActivity.class);
				startActivity(shareIntent);
				return true;
			case R.id.contact_exit:
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

        if (callbackContacts != null) {
            callbackContacts.onSearchQuery(newText);
        }
        if (callbackAllowed != null) {
            callbackAllowed.onSearchQuery(newText);
        }
        if (callbackBlocked != null) {
            callbackBlocked.onSearchQuery(newText);
        }
    }

	private void contactSync() {
        ContactManager.Instance().getContacts();
        PrefsManager.Instance().setLastContactSync(System.currentTimeMillis());
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

	@Override
	public void onContactsLoaded() {
        AppController.toast(this, "Contacts synced");
        Intent intent = new Intent(this, MuzikoFirebaseService.class);
		intent.setAction(AppController.ACTION_UPDATE_FIREBASE);
		startService(intent);
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
					case AppController.INTENT_TRACK_REPEAT:
						mainUpdate();
						break;
					case AppController.INTENT_TRACK_SHUFFLE:
						mainUpdate();
						break;
					case AppController.INTENT_QUEUE_STOPPED:
                        miniPlayer.layoutMiniPlayer();

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
