package com.muziko.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.R;
import com.muziko.adapter.SettingsAdapter;
import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.ScrobblesDatabase;
import com.muziko.api.LastFM.Utils.Util;
import com.muziko.api.LastFM.services.NetApp;
import com.muziko.api.LastFM.services.ScrobblingService;
import com.muziko.common.models.SettingModel;
import com.muziko.controls.SimpleSectionedRecyclerViewAdapter;
import com.muziko.dialogs.LastFMLogin;
import com.muziko.interfaces.SettingsRecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.SettingsManager;

import java.util.ArrayList;
import java.util.List;

public class LastFMSettingsActivity extends BaseActivity implements SettingsRecyclerItemListener {

    private final String TAG = "LastFMSettingsActivity";
    private final String KEY_SCROBBLE_ALL_NOW = "scrobble_all_now";
    private final String KEY_VIEW_SCROBBLE_CACHE = "view_scrobble_cache";
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SettingsAdapter adapter;
    private ArrayList<SettingModel> items = new ArrayList<>();
    private AppSettings settings;
    private NetApp mNetApp;
    private ScrobblesDatabase mDb;
    private boolean auth = false;
    private BroadcastReceiver onAuthChange = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            if (mNetApp == NetApp.valueOf(b.getString("netapp"))) {
                LastFMSettingsActivity.this.update();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_last_fmsettings);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_blue));
        }
        findViewsById();
        setSupportActionBar(toolbar);
        toolbar.setTitle("Last.fm Settings");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        settings = new AppSettings(this);
        mNetApp = NetApp.valueOf("LASTFM");
        mDb = new ScrobblesDatabase(this);

        try {
            mDb.open();
        } catch (SQLException e) {
            Log.e(TAG, "Cannot open database!");
            Log.e(TAG, e.getMessage());
            mDb = null;
        }
        checkNetwork();
        auth = credsCheck();

        adapter = new SettingsAdapter(this, items, this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//		recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));


        //This is the code to provide a sectioned list
        List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                new ArrayList<>();

        //Sections
        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0, "Last FM"));

        //Add your adapter to the sectionAdapter
        SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        SimpleSectionedRecyclerViewAdapter mSectionedAdapter = new
                SimpleSectionedRecyclerViewAdapter(this, R.layout.section_header, R.id.sectiontitle, adapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        recyclerView.setAdapter(mSectionedAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter ifs = new IntentFilter();
        ifs.addAction(ScrobblingService.BROADCAST_ONAUTHCHANGED);
        registerReceiver(onAuthChange, ifs);

        update();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(onAuthChange);
    }

    private void update() {
        String status = Util.getStatusSummary(this, settings, mNetApp);
        AppController.toast(this, status);
        boolean hasCreds = settings.hasCreds(mNetApp);

        items.clear();
        adapter.notifyDataSetChanged();
        items.addAll(SettingsManager.Instance().getLastFMSettings(hasCreds, status));
        adapter.notifyDataSetChanged();

//		mClearCreds.setEnabled(hasCreds);
    }

    private void findViewsById() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.itemList);
    }

    @Override
    public void onItemClicked(Context context, SettingModel settingModel) {

        switch (settingModel.key) {

            case "preflastfmusercred":

                LastFMLogin pl = new LastFMLogin(mNetApp, settings);
                pl.open(this);

                break;

            case "preflastfmusercredclear":

                if (settings.isAnyAuthenticated()) {

                    new MaterialDialog.Builder(context).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title(R.string.confirm_clear_all_creds).positiveText("Clear").onPositive((dialog, which) -> sendClearCreds()).negativeText("Cancel").show();

                } else {
                    sendClearCreds();
                }

                break;
            case "preflastfmsignup":

                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(mNetApp
                        .getSignUpUrl()));
                startActivity(browser);

                break;

            case "preflastfmstatus":

                Intent activityIntent = new Intent(LastFMSettingsActivity.this, LastFMStatusActivity.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

                break;

        }
    }

    @Override
    public void onItemChecked(SettingModel settingModel) {

    }

    private void checkNetwork() {
        this.sendBroadcast(new Intent(AppSettings.ACTION_NETWORK_OPTIONS_CHANGED));
        if (Util.checkForOkNetwork(this) != Util.NetworkStatus.OK) {
            AppController.toast(this, getString(R.string.limited_network));
        }
    }

    private boolean credsCheck() {
        //Credentials Check
        if (settings.getUsername(NetApp.LASTFM).equals("")
                && settings.getUsername(NetApp.LIBREFM).equals("")
                && settings.getPassword(NetApp.LASTFM).equals("")
                && settings.getPassword(NetApp.LIBREFM).equals("")) {
            AppController.toast(this, getString(R.string.creds_required));
            return false;
        } else {
            return true;
        }
    }

    private void sendClearCreds() {
        Intent service = new Intent(this, ScrobblingService.class);
        service.setAction(ScrobblingService.ACTION_CLEARCREDS);
        service.putExtra("clearall", true);
        startService(service);
    }
}
