package com.muziko.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muziko.BuildConfig;
import com.muziko.R;
import com.muziko.api.AcrCloud.TrackModel;
import com.muziko.api.YouTube.ItemsItem;
import com.muziko.api.YouTube.YouTubeAPI;
import com.muziko.api.YouTube.YouTubeDetails;
import com.muziko.controls.FloatingMusicNotesView;
import com.muziko.controls.YouTubeFragment;
import com.muziko.events.ACREvent;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.OkHttpManager;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import pl.tajchert.buswear.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.muziko.MyApplication.IMAGE_LARGE_SIZE;

public class IdentifySongActivity extends BaseActivity implements View.OnClickListener {
    private final WeakHandler handler = new WeakHandler();
    private final Runnable getPosition = () -> {
        AppController.Instance().serviceACRFingerPrint();
//            handler.postDelayed(this, 5000);
    };
    private RelativeLayout topLayout;
    private RelativeLayout bottomLayout;
    private LinearLayout bottomErrorLayout;
    private TextView topText;
    private RelativeLayout successLayout;
    private RelativeLayout footer;
    private Toolbar toolbar;
    private ImageView coverArtImage;
    private ImageView identifyImage;
    private TextView trackText;
    private TextView artistText;
    private FrameLayout mainLayout;
    private int x;
    private int y;
    private TrackModel trackModel;
    private FloatingMusicNotesView floatingMusicNotesView;
    private ImageButton playButton;
    private MenuItem menuIdentifyStart;
    private FragmentManager mFragmentManager;
    private TextView durationText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_identify_song);

        x = getIntent().getIntExtra("x", 0);
        y = getIntent().getIntExtra("y", 0);

        findViewsById();
        toolbar.setTitle("Identify Song");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        footer.setOnClickListener(this);
        identifyImage.setOnClickListener(this);
        playButton.setOnClickListener(this);

        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            mainLayout.setVisibility(View.INVISIBLE);

            ViewTreeObserver viewTreeObserver = mainLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        circularRevealActivity();
                        mainLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        }
        toggleView(0);

        handler.postDelayed(getPosition, 500);

        EventBus.getDefault(this).register(this);

        floatingMusicNotesView.start();
    }


    @Override
    public void onStop() {
        floatingMusicNotesView.stopNotesFall();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault(this).unregister(this);
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {

        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStackImmediate();
        } else {
            int initialRadius = mainLayout.getWidth();
            Animator circularReveal = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                circularReveal = ViewAnimationUtils.createCircularReveal(mainLayout, x, y, initialRadius, 0);
                circularReveal.setDuration(1000);
                circularReveal.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mainLayout.setVisibility(View.INVISIBLE);

                    }
                });
                circularReveal.start();
                handler.postDelayed(() -> finish(), 800);
            } else {
                finish();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onACREvent(ACREvent acrEvent) {
        if (acrEvent.isSuccess()) {
            trackModel = acrEvent.getTrackModel();
            toggleView(2);
            updateUI();
        } else {
            trackModel = new TrackModel();
            removeCoverArt();
            toggleView(1);
        }
    }

    private void findViewsById() {
        toolbar = findViewById(R.id.nowPlayingToolbar);
        mainLayout = findViewById(R.id.mainLayout);
        identifyImage = findViewById(R.id.identifyImage);
        coverArtImage = findViewById(R.id.coverArtImage);
        playButton = findViewById(R.id.playButton);
        durationText = findViewById(R.id.durationText);
        trackText = findViewById(R.id.trackText);
        artistText = findViewById(R.id.artistText);
        topLayout = findViewById(R.id.topLayout);
        bottomLayout = findViewById(R.id.bottomLayout);
        bottomErrorLayout = findViewById(R.id.bottomErrorLayout);
        topText = findViewById(R.id.topText);
        successLayout = findViewById(R.id.successLayout);
        footer = findViewById(R.id.footer);
        floatingMusicNotesView = findViewById(R.id.floatingMusicNotesView);
    }

    private void toggleView(int mode) {
        switch (mode) {
            case 0:
                if (menuIdentifyStart != null) {
                    menuIdentifyStart.setVisible(false);
                }
                topLayout.setVisibility(View.VISIBLE);
                topText.setVisibility(View.GONE);
                bottomLayout.setVisibility(View.VISIBLE);
                floatingMusicNotesView.setVisibility(View.VISIBLE);
                bottomErrorLayout.setVisibility(View.GONE);
                successLayout.setVisibility(View.GONE);
                playButton.setVisibility(View.GONE);
                durationText.setVisibility(View.GONE);

                break;
            case 1:
                if (menuIdentifyStart != null) {
                    menuIdentifyStart.setVisible(false);
                }
                topLayout.setVisibility(View.VISIBLE);
                topText.setVisibility(View.VISIBLE);
                bottomLayout.setVisibility(View.VISIBLE);
                floatingMusicNotesView.setVisibility(View.GONE);
                bottomErrorLayout.setVisibility(View.VISIBLE);
                successLayout.setVisibility(View.GONE);

                break;
            case 2:
                if (menuIdentifyStart != null) {
                    menuIdentifyStart.setVisible(true);
                }
                topLayout.setVisibility(View.GONE);
                bottomLayout.setVisibility(View.GONE);
                successLayout.setVisibility(View.VISIBLE);
                if (trackModel.getVideoId() != null) {
                    playButton.setVisibility(View.VISIBLE);

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    Retrofit baseRetrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.updaterAPI)
                            .client(OkHttpManager.Instance().getOkHttpClient())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();

                    YouTubeAPI youTubeAPI = baseRetrofit.create(YouTubeAPI.class);

                    Call<YouTubeDetails> youTubeDetailsCall = youTubeAPI.getVideoDetails(Utils.getYoutubeVideoUrl(trackModel.getVideoId()));
                    youTubeDetailsCall.enqueue(new Callback<YouTubeDetails>() {
                        @Override
                        public void onResponse(Call<YouTubeDetails> call, retrofit2.Response<YouTubeDetails> rawResponse) {
                            try {

                                if (rawResponse.isSuccessful()) {

                                    if (rawResponse.body().getItems().size() > 0) {
                                        ItemsItem itemsItem = rawResponse.body().getItems().get(0);
                                        if (itemsItem.getContentDetails() != null) {
                                            String duration = itemsItem.getContentDetails().getDuration();
                                            Long durationmilli = Utils.getYouTubeDuration(duration);
                                            String formattedDuration = Utils.getDuration(durationmilli);
                                            if (!formattedDuration.isEmpty()) {
                                                durationText.setVisibility(View.VISIBLE);
                                                durationText.setText(formattedDuration);
                                            }

                                        }
                                    }
                                }

                            } catch (Exception e) {
                                Crashlytics.logException(e);

                            }
                        }

                        @Override
                        public void onFailure(Call<YouTubeDetails> call, Throwable throwable) {
                            Crashlytics.logException(throwable);
                        }
                    });
                } else {
                    playButton.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.identify_menu, menu);

        menuIdentifyStart = menu.findItem(R.id.identify_start);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menuIdentifyStart.setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.identify_start:
                toggleView(0);
                AppController.Instance().serviceACRFingerPrint();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void circularRevealActivity() {

        float finalRadius = Math.max(mainLayout.getWidth(), mainLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            circularReveal = ViewAnimationUtils.createCircularReveal(mainLayout, x, y, 0, finalRadius);
            circularReveal.setDuration(1000);
        }
        // make the view visible and start the animation
        mainLayout.setVisibility(View.VISIBLE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            circularReveal.start();
        }
    }


    private void showCoverArt(String url) {
        Picasso.with(this)
                .load(url)
                .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                .centerCrop()
                .into(coverArtImage);

    }

    private void removeCoverArt() {
        String url = "blah";

        Picasso.with(this)
                .load(url)
                .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                .centerCrop()
                .into(coverArtImage);
    }


    private void updateUI() {

        trackText.setText(trackModel.title);
        artistText.setText(trackModel.artist_name);

        showCoverArt(trackModel.coverUrl);
    }

    @Override
    public void onClick(View v) {
        if (v == footer) {
            String query = null;
            try {
                query = URLEncoder.encode(trackModel.title + " " + trackModel.artist_name, "utf-8");
            } catch (UnsupportedEncodingException e) {
                Crashlytics.logException(e);
            }
            String url = "http://www.google.com/search?q=" + query;
            AppController.Instance().openUrl(url);
        } else if (v == identifyImage) {
            toggleView(0);
            AppController.Instance().serviceACRFingerPrint();
        } else if (v == playButton) {
            final YouTubeFragment youTubeFragment = YouTubeFragment.newInstance(trackModel.getVideoId());
            mFragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.addToBackStack("youTubeFragment");
            ft.add(R.id.mainLayout, youTubeFragment);
            ft.commit();
        }
    }
}
