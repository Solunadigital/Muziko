package com.muziko.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.badoo.mobile.util.WeakHandler;
import com.muziko.R;
import com.muziko.adapter.SearchListAdapter;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.PlaylistQueueItem;
import com.muziko.common.models.QueueItem;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;

import java.util.ArrayList;

import static com.muziko.MyApplication.ARG_FAV;
import static com.muziko.MyApplication.ARG_ID;
import static com.muziko.MyApplication.ARG_IGNORE;
import static com.muziko.MyApplication.ARG_TITLE;

public class SearchSongsActivity extends BaseActivity implements View.OnClickListener, SearchView.OnQueryTextListener {
    private final ArrayList<QueueItem> songList = new ArrayList<>();
    private final ArrayList<QueueItem> selectList = new ArrayList<>();
    private final WeakHandler handler = new WeakHandler();
    private SearchListAdapter adapter;
    private RelativeLayout addButton;
    private ProgressBar progressBar;
    private TextView addText;
    private EditText editSearch;
    private TextWatcher tw;
    private ImageView backButton;
    private ListView queueListView;
    private CoordinatorLayout coordinatorlayout;
    private MenuItem menuItemSearch;
    private long id = 0;
    private boolean isFav = false;
    private boolean isBusy = false;
    private SearchSongsLoader taskLoad;
    private SearchSongsAdder taskAdd;
    private SearchView searchView;
    private boolean isIgnore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_song_search);
        overridePendingTransition(R.anim.slide_up, R.anim.fade_out);
        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        addButton = findViewById(R.id.addButton);
        progressBar = findViewById(R.id.progressBar);

        addText = findViewById(R.id.addText);
        editSearch = findViewById(R.id.editSearch);
        backButton = findViewById(R.id.backButton);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_blue));
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);

        Intent in = getIntent();

        isIgnore = in.getBooleanExtra(ARG_IGNORE, false);
        isFav = in.getBooleanExtra(ARG_FAV, false);
        String title = "";
        if (isIgnore) {
            title = "Add To Ignore";
        } else if (isFav) {
            title = "Add To Favourites";
        } else {
            id = in.getLongExtra(ARG_ID, 0);
            title = in.getStringExtra(ARG_TITLE);

            if (id == 0 || title == null) {
                AppController.toast(SearchSongsActivity.this, "Playlist not found!");

                finish();
                return;
            }
            title = String.format("Add to '%s'", title);
        }

        actionBar.setTitle(title);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        queueListView = findViewById(R.id.songs_list);
        adapter = new SearchListAdapter(this, songList, selectList);
        queueListView.setAdapter(adapter);

        addButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

        queueListView.setTextFilterEnabled(true);
        editSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            public void afterTextChanged(Editable s) {
            }
        });

        Utils.hideKeyboard(this, editSearch);
        editSearch.clearFocus();
        load();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        if (handler != null) handler.removeCallbacksAndMessages(null);
        unload();
        unadd();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isBusy) {
            AppController.toast(SearchSongsActivity.this, "Please wait...");
        } else if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == backButton) {
            editSearch.setText("");
            editSearch.clearFocus();
        } else if (v == addButton) {
            if (selectList.size() == 0)
                AppController.toast(SearchSongsActivity.this, "Select the songs first!");
            else
                addSongs();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_search, menu);

        menuItemSearch = menu.findItem(R.id.action_search);
        menuItemSearch.setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menuItemSearch != null) {
            searchView = (SearchView) menuItemSearch.getActionView();
            searchView.setQueryHint("Search song...");
            searchView.setOnQueryTextListener(this);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.action_share:
                AppController.Instance().shareApp();
                return true;
            case R.id.sharing_wifi:
                Intent shareIntent =
                        new Intent(SearchSongsActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.action_exit:
                AppController.Instance().exit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);

        return false;
    }

    private void addSongs() {
        if (isBusy) {
            AppController.toast(SearchSongsActivity.this, "Please wait...");
        } else {
            unadd();
            add();
        }
    }

    private void unload() {
        if (taskLoad != null) {
            taskLoad.cancel(true);
            taskLoad = null;
            isBusy = false;
        }
    }

    private void load() {
        if (isBusy) return;

        taskLoad = new SearchSongsLoader(this);
        taskLoad.execute();
    }


    private void reload() {
        unload();
        load();
    }

    private void unadd() {
        if (taskAdd != null) {
            taskAdd.cancel(true);
            taskAdd = null;
            isBusy = false;
        }
    }

    private void add() {
        if (isBusy) return;

        taskAdd = new SearchSongsAdder(this);
        taskAdd.execute();
    }

    public class SearchSongsLoader extends AsyncTask<Void, int[], Boolean> {
        final ArrayList<QueueItem> songs = new ArrayList<>();
        private final Context ctx;

        public SearchSongsLoader(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (isFav) {
//                MyApplication.favorites = Utils.loadFavs(ctx);
            }

            for (QueueItem track : TrackRealmHelper.getTracks(0).values()) {

                if (isIgnore) {
                    if (!track.removed) {

                        track.selected = false;
                        songs.add(track);
                    }
                } else if (isFav) {
                    if (!track.favorite) {

                        track.selected = false;
                        songs.add(track);
                    }
                } else {
                    track.selected = false;
                    songs.add(track);
                }
            }

            return true;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            queueListView.setVisibility(View.GONE);

            isBusy = true;
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(Boolean s) {
            isBusy = false;


            songList.clear();
            songList.addAll(songs);

            selectList.clear();

            adapter.notifyDataSetChanged();

            progressBar.setVisibility(View.GONE);
            queueListView.setVisibility(View.VISIBLE);

            addText.setText(selectList.size() == 0 ? "ADD" : "SAVE");
            super.onPostExecute(s);
        }
    }

    public class SearchSongsAdder extends AsyncTask<Void, int[], Boolean> {
        private final Context ctx;
        int counter = 0;

        public SearchSongsAdder(Context context) {
            this.ctx = context;
        }

        @Override
        protected void onPreExecute() {
            addText.setText(R.string.please_wait_with_dots);
            isBusy = true;

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (QueueItem queueItem : selectList) {
                if (isIgnore) {
                    if (TrackRealmHelper.movetoNegative(queueItem)) {
                        counter++;
                    }
                } else if (isFav) {
                    if (TrackRealmHelper.addFavorite(queueItem.data)) {
                        FirebaseManager.Instance().uploadFav(queueItem);
                        counter++;
                    }
                } else {
                    PlaylistQueueItem item = new PlaylistQueueItem(queueItem);
                    item.playlist = id;
                    if (PlaylistSongRealmHelper.insert(item, false) > 0) {
                        PlaylistItem playlistItem = PlaylistRealmHelper.getPlaylist(item.playlist);
//                        FirebaseManager.Instance().uploadPlaylistTrack(queueItem, playlistItem.hash);
                        counter++;
                    }
                }
            }

            return counter > 0;
        }

        @Override
        protected void onPostExecute(Boolean s) {
            isBusy = false;
            addText.setText(R.string.done);

            if (counter > 0)
                AppController.toast(SearchSongsActivity.this, String.format("%d song%s added", counter, counter != 1 ? "s" : ""));
            else
                AppController.toast(SearchSongsActivity.this, "No songs added!");
            finish();
            super.onPostExecute(s);
        }
    }
}
