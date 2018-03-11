package com.muziko.activities;

import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.muziko.R;
import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.ScrobblesDatabase;
import com.muziko.api.LastFM.Utils.Util;
import com.muziko.api.LastFM.services.NetApp;
import com.muziko.fragments.LastFMStatusFragment;


public class LastFMStatusActivity extends BaseActivity {

	private final int MENU_SCROBBLE_NOW_ID = 0;
	private final int MENU_VIEW_CACHE_ID = 1;
	private final int MENU_RESET_STATS_ID = 2;
	private final String TAG = "LastFMStatusActivity";
	private AppSettings settings;
	private ScrobblesDatabase mDb;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_last_fmstatus);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setTitle("Last.fm Status");

		settings = new AppSettings(this);

		mDb = new ScrobblesDatabase(this);

		try {
			mDb.open();
		} catch (SQLException e) {
			Log.e(TAG, "Cannot open database!");
			Log.e(TAG, e.getMessage());
			mDb = null;
		}

		for (NetApp napp : NetApp.values()) {

			LastFMStatusFragment fr = LastFMStatusFragment.newInstance(napp.getValue());
			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.fragmentLayout, fr)
					.commit();
			break;
		}
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
		mDb.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.fragment_lastfm_status, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case MENU_SCROBBLE_NOW_ID:
				int numInCache = mDb.queryNumberOfTracks();
				Util.scrobbleAllIfPossible(this, numInCache);
				return true;
			case R.id.MENU_RESET_STATS_ID:
				for (NetApp napp : NetApp.values()) {
					settings.clearSubmissionStats(napp);
					// TODO: refill data on clearStats
				}
				this.finish();
				startActivity(getIntent());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
