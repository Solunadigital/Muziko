package com.muziko.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.muziko.BuildConfig;
import com.muziko.R;
import com.muziko.adapter.AlbumArtInternetAdapter;
import com.muziko.api.GoogleCustomSearch.GoogleImageSearchFilters;
import com.muziko.api.GoogleCustomSearch.models.GoogleImageSearchResults;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.EndlessRecyclerOnScrollListener;
import com.muziko.interfaces.BasicRecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.MuzikoConstants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.muziko.MyApplication.IMAGE_MEDIUM_SIZE;
import static com.muziko.MyApplication.networkState;

public class AlbumArtInternetActivity extends BaseActivity implements BasicRecyclerItemListener, SearchView.OnQueryTextListener {
    private final ArrayList<GoogleImageSearchResults> images = new ArrayList<>();

    private RelativeLayout progressLayout;
    private TextView progress;
    private TextView search_no_results;
    private RecyclerView mrecyclerView;
    private AlbumArtInternetAdapter mAdapter;
    private Toolbar toolbar;
    private QueueItem item;
    private MenuItem menuItemSearch;
    private SearchView searchView = null;
    private GoogleImageSearchFilters filters;
    private String searchQuery = "";
    private int selectedItemPosition;
    private GoogleImageSearchResults image;
    private Target picassoTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // loading of the bitmap was a success
            AsyncTask.execute(() -> {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                byte[] byteArray = stream.toByteArray();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("item", item);
                returnIntent.putExtra("index", selectedItemPosition);
                returnIntent.putExtra("filepath", image.imageUrl);
                returnIntent.putExtra("image", byteArray);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            });
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
        setStartActivity(false);
        setContentView(R.layout.activity_album_art_internet);

        findViewsById();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_blue));
        }

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        item = (QueueItem) getIntent().getSerializableExtra("item");
        selectedItemPosition = getIntent().getIntExtra("index", 0);
        if (item == null) {
            AppController.toast(this, getString(R.string.album_not_found));
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
            return;
        }
        actionBar.setTitle(item.album_name);


        progressLayout = findViewById(R.id.progressLayout);
        search_no_results = findViewById(R.id.search_no_results);
        progress = findViewById(R.id.progress);

        mAdapter = new AlbumArtInternetAdapter(this, images, this);
        GridLayoutManager gridlm = new GridLayoutManager(this, 2);
        mrecyclerView.setLayoutManager(gridlm);
        int spanCount = 2; // 3 columns
        int spacing = 30; // 50px
        boolean includeEdge = true;
//		mrecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        mrecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(gridlm) {

            @Override
            public void onLoadMore(int current_page) {

                if (networkState == NetworkInfo.State.CONNECTED) {
                    do_search(searchQuery, current_page);
                } else {
                    AppController.toast(AlbumArtInternetActivity.this, getString(R.string.no_network_connection));
                }

            }
        });
        mrecyclerView.setAdapter(mAdapter);

        filters = new GoogleImageSearchFilters();
//		filters.setImageSiteFilter("Medium");

        progressLayout.setVisibility(View.VISIBLE);
        mrecyclerView.setVisibility(View.GONE);

        if (networkState != NetworkInfo.State.CONNECTED) {
            AppController.toast(this, getString(R.string.no_network_connection));
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
            return;
        }
        searchQuery = item.album_name;
        do_search(searchQuery, 0);
    }

    @Override
    public void onBackPressed() {
        if (menuItemSearch != null) {
            SearchView searchView = (SearchView) menuItemSearch.getActionView();

            if (searchView != null && !searchView.isIconified()) {
                searchView.setIconified(true);
                return;
            }
        }

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
//		super.onBackPressed();

    }

    private void findViewsById() {
        toolbar = findViewById(R.id.toolbar);
        mrecyclerView = findViewById(R.id.itemList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_albumartinternet, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (networkState == NetworkInfo.State.CONNECTED) {
            searchQuery = query;
            images.clear();
            mAdapter.notifyDataSetChanged();
            do_search(searchQuery, 0);
            searchView.clearFocus();
        } else {
            AppController.toast(AlbumArtInternetActivity.this, getString(R.string.no_network_connection));
        }

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onItemClicked(final int position) {
        progress.setText(R.string.saving_album_art);

        progressLayout.setVisibility(View.VISIBLE);
        mrecyclerView.setVisibility(View.GONE);

        image = mAdapter.getItem(position);

        mrecyclerView.setAdapter(null);

        Picasso.with(this)
                .load(image.imageUrl)
                .resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
                .into(picassoTarget);

    }

    private String generateQueryString(String query, int page) {
        String uri = "";
        String start = "";

        if (page > 0) {
            start = String.format("&start=%d", page * 20); // TODO: Make this use the nextpage value from the json
        }

        try {
            uri = String.format("%s?key=%s&cx=%s&searchType=image&q=%s%s%s",
                    MuzikoConstants.googlecustomSearch,
                    BuildConfig.googlecustomSearchAPI,
                    BuildConfig.googlecustomSearchCX,
                    URLEncoder.encode(query, "UTF-8"),
                    start,
                    filters.getFilterParams()
            );
        } catch (UnsupportedEncodingException exception) {
            // TODO: Handle this
        }

        return uri;
    }


    private void do_search(String query, int page) {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        String url = generateQueryString(query, page);


        Log.d("blah", url);

        client.get(url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Log.d("blah", response.toString());
                    images.addAll(GoogleImageSearchResults.parseJSON(response));
                    Log.d("blah", "Stream Size: " + images.size());
                    mAdapter.notifyDataSetChanged();

                    progressLayout.setVisibility(View.GONE);
                    mrecyclerView.setVisibility(View.VISIBLE);

                    if (images.isEmpty()) {
                        search_no_results.setVisibility(View.VISIBLE);
                    } else {
                        search_no_results.setVisibility(View.GONE);
                    }

                } catch (JSONException ex) {
                    Log.d("GIS JSONException", "JSON Exception: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                AppController.toast(AlbumArtInternetActivity.this, getString(R.string.problem_connecting_google));
                if (errorResponse != JSONObject.NULL)
//					Log.d("blah", "Obj: " + errorResponse.toString());
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONArray errorResponse) {
                AppController.toast(AlbumArtInternetActivity.this, getString(R.string.problem_connecting_google));
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                AppController.toast(AlbumArtInternetActivity.this, getString(R.string.problem_connecting_google));
                super.onFailure(statusCode, headers, responseString, throwable);
            }

        });
    }

}
