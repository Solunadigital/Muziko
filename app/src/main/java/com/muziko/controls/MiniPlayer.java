package com.muziko.controls;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.badoo.mobile.util.WeakHandler;
import com.google.android.gms.ads.AdView;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.NowPlayingActivity;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.adapter.QueueAdapter;
import com.muziko.adapter.SelectableAdapter;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.FastScroller.OnFastScrollStateChangeListener;
import com.muziko.controls.LayoutManagers.PreCachingLayoutManager;
import com.muziko.controls.SwipeToDismiss.OnItemClickListener;
import com.muziko.controls.SwipeToDismiss.RecyclerViewAdapter;
import com.muziko.controls.SwipeToDismiss.SwipeToDismissTouchListener;
import com.muziko.controls.SwipeToDismiss.SwipeableItemClickListener;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.DownloadFile;
import com.muziko.dialogs.PreviewSong;
import com.muziko.dialogs.SetRingtone;
import com.muziko.dialogs.ShareRingtone;
import com.muziko.helpers.FileHelper;
import com.muziko.helpers.ItemTouchHelpers;
import com.muziko.helpers.QueueHelper;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.ImageManager;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.MuzikoExoPlayer;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.objects.MenuObject;
import com.muziko.service.SongService;
import com.muziko.tasks.FavoriteEdit;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tr4android.support.extension.drawable.MediaControlDrawable;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

import static com.muziko.MyApplication.networkState;
import static com.muziko.R.id.share;
import static com.muziko.helpers.Utils.getTintedDrawable;
import static com.muziko.objects.MenuObject.ADD_TO_LIBRARY;
import static com.muziko.objects.MenuObject.ADD_TO_PLAYLIST;
import static com.muziko.objects.MenuObject.ADD_TO_QUEUE;
import static com.muziko.objects.MenuObject.CUT;
import static com.muziko.objects.MenuObject.DELETE_FROM_QUEUE;
import static com.muziko.objects.MenuObject.DETAILS;
import static com.muziko.objects.MenuObject.DONT_SYNC_FAV_OR_PLAYLIST;
import static com.muziko.objects.MenuObject.DOWNLOAD;
import static com.muziko.objects.MenuObject.FAV;
import static com.muziko.objects.MenuObject.GO_TO_ALBUM;
import static com.muziko.objects.MenuObject.GO_TO_ARTIST;
import static com.muziko.objects.MenuObject.MOVE_TO_IGNORE;
import static com.muziko.objects.MenuObject.PLAY_NEXT;
import static com.muziko.objects.MenuObject.PLAY_X_TIMES;
import static com.muziko.objects.MenuObject.PREVIEW_SONG;
import static com.muziko.objects.MenuObject.SEND_AUDIO_CLIP;
import static com.muziko.objects.MenuObject.SET_RINGTONE;

public class MiniPlayer
        implements View.OnClickListener,
        View.OnTouchListener,
        RecyclerItemListener,
        View.OnLongClickListener,
        MaterialMenuAdapter.Callback,
        OnFastScrollStateChangeListener,
        AdMobBanner.OnAdLoadedListener {
    private final String TAG = "MiniPlayer";
    private final Activity mContext;
    private final LinearLayout mMainPlayerLayout;
    private final MainPlayerTouchHelper touchCallback = new MainPlayerTouchHelper();
    private final ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
    private final WeakHandler handler = new WeakHandler();
    private final RelativeLayout mContentLayout;
    private final SlidingUpPanelLayout mSlidingUpPanelLayout;
    private boolean shouldLoadAd = false;
    private PlayerConstants.MiniPlayerState playerMode = PlayerConstants.MiniPlayerState.PLAYER_MINI;
    private RelativeLayout popuplayout;
    private RelativeLayout emptyQueueLayout;
    private Button emptyQueueShuffleButton;
    private LinearLayout controlButtonLayout;
    private TextView popuptrack;
    private final Runnable closePopUp =
            new Runnable() {
                @Override
                public void run() {
                    if (!AppController.isBuffering) {
                        popuptrack.setVisibility(View.GONE);
                    }
                    handler.postDelayed(this, 3000);
                }
            };
    private ImageButton buttonQueueMain;
    private ImageButton buttonRepeatMain;
    private ImageView buttonPlayMain;
    private ImageButton buttonPrevMain;
    private ImageButton buttonNextMain;
    private ImageButton buttonShuffleMain;
    private ImageButton buttonDelete;
    private ImageButton buttonCurrent;
    private ImageButton buttonSavePlaylist;
    private ImageView coverThumbnail;
    private boolean skippingIsRunning = false;
    private QueueAdapter queueAdapter;
    private FastScrollRecyclerView queueList;
    private boolean isVisible = true;
    private boolean loaded = false;
    private boolean noDragging = false;
    private final Runnable allowDragging = () -> noDragging = false;
    private Toolbar mainPlayerToolbar;
    private boolean dragging;
    private final Runnable stopDragging =
            new Runnable() {
                @Override
                public void run() {
                    dragging = false;
                    updateQueue();
                }
            };
    private boolean openQueue = false;
    private onLayoutListener mListener;
    private boolean adapterIsUpdating = false;
    private ProgressBar miniProgressBar;
    private MediaControlDrawable mediaControlDrawable;
    private float scale;
    private QueueItem selectedItem;
    private int selectedItemPosition;
    private SwipeableItemClickListener swipeableItemClickListener;
    private RelativeLayout admobLayout;
    private AdView admob;
    private AdMobBanner adMobBanner;
    private boolean adLoaded = false;
    private CircleProgressBar bufferProgressBar;
    private boolean buffering;

    // default constructor
    public MiniPlayer(
            final Activity activity,
            LinearLayout mainPlayerLayout,
            final SlidingUpPanelLayout slidingUpPanelLayout,
            RelativeLayout contentLayout) {
        mContext = activity;
        mMainPlayerLayout = mainPlayerLayout;
        mSlidingUpPanelLayout = slidingUpPanelLayout;
        mContentLayout = contentLayout;

        initialize();
    }

    // Listening Screen contructor
    public MiniPlayer(
            final Activity activity,
            LinearLayout mainPlayerLayout,
            final SlidingUpPanelLayout slidingUpPanelLayout,
            RelativeLayout contentLayout,
            boolean shouldLoadAd,
            onLayoutListener listener) {
        mContext = activity;
        mMainPlayerLayout = mainPlayerLayout;
        mSlidingUpPanelLayout = slidingUpPanelLayout;
        mContentLayout = contentLayout;
        mListener = listener;
        this.shouldLoadAd = shouldLoadAd;
        initialize();
    }

    private void initialize() {

        findViewsById(this.mMainPlayerLayout);

        scale = mContext.getResources().getDisplayMetrics().density;

        this.mSlidingUpPanelLayout.addPanelSlideListener(
                new SlidingUpPanelLayout.PanelSlideListener() {
                    @Override
                    public void onPanelSlide(View panel, float slideOffset) {
                    }

                    @Override
                    public void onPanelStateChanged(
                            View panel,
                            SlidingUpPanelLayout.PanelState previousState,
                            SlidingUpPanelLayout.PanelState newState) {
                        if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {

                            if (playerMode != PlayerConstants.MiniPlayerState.PLAYER_NOW) {
                                updateMode(PlayerConstants.MiniPlayerState.PLAYER_QUEUE);
                            }

                        } else if (newState == SlidingUpPanelLayout.PanelState.DRAGGING) {

                            if (playerMode == PlayerConstants.MiniPlayerState.PLAYER_QUEUE
                                    && previousState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                                updateMode(PlayerConstants.MiniPlayerState.PLAYER_MINI);
                            } else if (playerMode == PlayerConstants.MiniPlayerState.PLAYER_NOW
                                    && previousState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                                updateMode(PlayerConstants.MiniPlayerState.PLAYER_MINI);
                            } else if (playerMode == PlayerConstants.MiniPlayerState.PLAYER_MINI
                                    && previousState == SlidingUpPanelLayout.PanelState.COLLAPSED
                                    && !noDragging) {
                                updateMode(PlayerConstants.MiniPlayerState.PLAYER_QUEUE);
                            }

                        } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {

                            if (openQueue) {
                                openQueue = false;
                                updateMode(PlayerConstants.MiniPlayerState.PLAYER_QUEUE);
                                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                            } else {
                                if (playerMode != PlayerConstants.MiniPlayerState.PLAYER_MINI) {
                                    updateMode(PlayerConstants.MiniPlayerState.PLAYER_MINI);
                                }
                            }
                        } else if (newState == SlidingUpPanelLayout.PanelState.ANCHORED) {

                            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }
                    }
                });
        mediaControlDrawable =
                new MediaControlDrawable.Builder(mContext)
                        .setColor(ContextCompat.getColor(mContext, R.color.light_blue))
                        .setPadding(8 * scale)
                        .setInitialState(MediaControlDrawable.State.PLAY)
                        .build();
        buttonPlayMain.setImageDrawable(mediaControlDrawable);

        buttonPlayMain.setOnClickListener(this);
        buttonPrevMain.setOnClickListener(this);
        buttonNextMain.setOnClickListener(this);
        buttonQueueMain.setOnClickListener(this);
        buttonRepeatMain.setOnClickListener(this);
        buttonShuffleMain.setOnClickListener(this);

        buttonDelete.setOnClickListener(this);
        buttonCurrent.setOnClickListener(this);
        buttonSavePlaylist.setOnClickListener(this);
        emptyQueueShuffleButton.setOnClickListener(this);
        coverThumbnail.setOnClickListener(this);

        buttonPrevMain.setOnLongClickListener(this);

        popuptrack.setVisibility(View.GONE);

        miniProgressBar.setMax(100);
        miniProgressBar.setProgress(0);

        queueAdapter = new QueueAdapter(mContext, PlayerConstants.QUEUE_LIST, this);

        PreCachingLayoutManager preCachingLayoutManager = new PreCachingLayoutManager(mContext, 360);
        queueList.setAdapter(queueAdapter);
        queueList.setHasFixedSize(true);
        queueList.setLayoutManager(preCachingLayoutManager);
        final SwipeToDismissTouchListener<RecyclerViewAdapter> touchListener =
                new SwipeToDismissTouchListener<>(
                        new RecyclerViewAdapter(queueList),
                        new SwipeToDismissTouchListener.DismissCallbacks<RecyclerViewAdapter>() {
                            @Override
                            public boolean canDismiss() {
                                return true;
                            }

                            @Override
                            public void onPendingDismiss(int position) {

                                queueAdapter.setPendingDimiss(true, position);
                                queueAdapter.notifyItemChanged(position);
                            }

                            @Override
                            public void onDismiss(final int position) {
                                queueAdapter.setPendingDimiss(false, position);
                                removeFromQueue(position);
                            }
                        });
        int TIME_TO_AUTOMATICALLY_DISMISS_ITEM = 3000;
        touchListener.setDismissDelay(TIME_TO_AUTOMATICALLY_DISMISS_ITEM);
        queueList.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        queueList.addOnScrollListener((RecyclerView.OnScrollListener) touchListener.makeScrollListener());
        swipeableItemClickListener =
                new SwipeableItemClickListener(
                        mContext,
                        new OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                if (view.getId() == R.id.txt_removed) {
                                    touchListener.processPendingDismisses();
                                    queueAdapter.setPendingDimiss(false, position);
                                } else if (view.getId() == R.id.txt_undo) {
                                    touchListener.undoPendingDismiss();
                                    queueAdapter.setPendingDimiss(false, position);
                                    queueAdapter.notifyItemChanged(position);
                                } else { // R.id.txt_data
                                    if (view.getId() == R.id.imageMenu) onMenuClicked(position);
                                    else onItemClicked(position);
                                }
                            }

                            @Override
                            public void onItemLongPress(View view, int position) {
                                // Do another thing when an item is long pressed.
                                if (view.getId() != R.id.imageGrabber) {
                                    onItemLongClicked(position);
                                }
                            }
                        });
        queueList.addOnItemTouchListener(swipeableItemClickListener);
        touchHelper.attachToRecyclerView(queueList);

        queueList.setOnFlingListener(
                new RecyclerView.OnFlingListener() {

                    @Override
                    public boolean onFling(int velocityX, final int velocityY) {

                        if (!queueList.canScrollVertically(-1) && velocityY < 0) {
                            MiniPlayer.this.mSlidingUpPanelLayout.setPanelState(
                                    SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }

                        return false;
                    }
                });
        queueList.setStateChangeListener(this);

        setupMenu();

        Resources resources = mContext.getResources();
        LinearLayout.LayoutParams parms =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Utils.toPixels(resources, 30));
        admobLayout.setLayoutParams(parms);
        admobLayout.requestLayout();
        admob.setVisibility(View.GONE);

        if (AppController.Instance().shouldShowAd() && shouldLoadAd) {
            adMobBanner = new AdMobBanner(admobLayout, this);
        }

        switch (PrefsManager.Instance().getPlayRepeat()) {
            case PlayerConstants.REPEAT_OFF:
                buttonRepeatMain.setImageResource((R.drawable.repeat_icon));
                break;

            case PlayerConstants.REPEAT_ALL:
                buttonRepeatMain.setImageResource((R.drawable.repeat_icon_blue));
                break;

            case PlayerConstants.REPEAT_ONE:
                buttonRepeatMain.setImageResource((R.drawable.repeatone_icon_blue));
        }

        if (PrefsManager.Instance().getPlayShuffle(mContext)) {
            buttonShuffleMain.setImageResource((R.drawable.shuffle_icon_blue));
        } else {
            buttonShuffleMain.setImageResource((R.drawable.shuffle_icon));
        }

        updateMode(PlayerConstants.MiniPlayerState.PLAYER_MINI);
        layoutMiniPlayer();
        updateUI();
    }


    private void findViewsById(LinearLayout layout) {
        mainPlayerToolbar = layout.findViewById(R.id.mainPlayerToolbar);
        popuplayout = layout.findViewById(R.id.popuplayout);
        controlButtonLayout = layout.findViewById(R.id.controlButtonLayout);
        popuptrack = layout.findViewById(R.id.popuptrack);
        buttonPlayMain = layout.findViewById(R.id.buttonPlayMain);
        bufferProgressBar = layout.findViewById(R.id.bufferProgressBar);
        buttonPrevMain = layout.findViewById(R.id.buttonPrevMain);
        buttonNextMain = layout.findViewById(R.id.buttonNextMain);
        buttonQueueMain = layout.findViewById(R.id.buttonQueueMain);
        buttonRepeatMain = layout.findViewById(R.id.buttonRepeatMain);
        buttonShuffleMain = layout.findViewById(R.id.buttonShuffleMain);
        miniProgressBar = layout.findViewById(R.id.miniProgressBar);
        queueList = layout.findViewById(R.id.queueList);
        emptyQueueLayout = layout.findViewById(R.id.emptyQueueLayout);
        emptyQueueShuffleButton = layout.findViewById(R.id.emptyQueueShuffleButton);
        buttonDelete = layout.findViewById(R.id.buttonDelete);
        buttonCurrent = layout.findViewById(R.id.buttonCurrent);
        buttonSavePlaylist = layout.findViewById(R.id.buttonSavePlaylist);
        coverThumbnail = layout.findViewById(R.id.coverThumbnail);
        admobLayout = layout.findViewById(R.id.admobLayout);
        admob = layout.findViewById(R.id.admob);
    }

    private void setupMenu() {

        if (queueAdapter.getItemCount() > 0) {
            buttonDelete.setVisibility(View.VISIBLE);
            buttonCurrent.setVisibility(View.VISIBLE);
            buttonSavePlaylist.setVisibility(View.VISIBLE);
        } else {
            buttonDelete.setVisibility(View.INVISIBLE);
            buttonCurrent.setVisibility(View.INVISIBLE);
            buttonSavePlaylist.setVisibility(View.INVISIBLE);
        }
    }

    public void onDestroy() {
        if (adMobBanner != null) {
            adMobBanner.stop();
        }
        if (handler != null) handler.removeCallbacksAndMessages(null);
    }

    public void pause() {
    }

    private void clear() {
        queueAdapter.reset();
        AppController.Instance().serviceClear();
    }

    private boolean hasTrack() {
        return PlayerConstants.QUEUE_SONG.title.length() > 0;
    }

    public void close() {
        updateMode(PlayerConstants.MiniPlayerState.PLAYER_MINI);
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    public void open() {
        updateMode(PlayerConstants.MiniPlayerState.PLAYER_MINI);
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    private void updateMode(PlayerConstants.MiniPlayerState mode) {

        playerMode = mode;
        // SET THE MODE AND CALL THIS

        int pixels;
        RelativeLayout.LayoutParams smalllayout;

        switch (playerMode) {

            case PLAYER_CLOSED:

                admobLayout.setVisibility(View.GONE);
                popuplayout.setVisibility(View.GONE);
                buttonQueueMain.setVisibility(View.GONE);
                controlButtonLayout.setVisibility(View.GONE);
                mainPlayerToolbar.setVisibility(View.GONE);
                queueList.setVisibility(View.GONE);

                mSlidingUpPanelLayout.setTouchEnabled(false);
                mSlidingUpPanelLayout.setDragView(R.id.controlButtonLayout);

                break;

            case PLAYER_MINI:
                mMainPlayerLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.transparent));

                controlButtonLayout.setVisibility(View.VISIBLE);
                mainPlayerToolbar.setVisibility(View.VISIBLE);
                miniProgressBar.setVisibility(View.VISIBLE);

                popuplayout.setVisibility(View.VISIBLE);
                admobLayout.setVisibility(View.VISIBLE);
                buttonQueueMain.setVisibility(View.VISIBLE);
                coverThumbnail.setVisibility(View.VISIBLE);
                buttonRepeatMain.setVisibility(View.GONE);
                buttonShuffleMain.setVisibility(View.GONE);

                mSlidingUpPanelLayout.setTouchEnabled(true);
                mSlidingUpPanelLayout.setDragView(R.id.controlButtonLayout);

                break;

            case PLAYER_QUEUE:
                mMainPlayerLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));

                mainPlayerToolbar.setVisibility(View.VISIBLE);
                miniProgressBar.setVisibility(View.GONE);
                popuplayout.setVisibility(View.GONE);

                admobLayout.setVisibility(View.GONE);
                buttonQueueMain.setVisibility(View.GONE);
                coverThumbnail.setVisibility(View.GONE);
                buttonRepeatMain.setVisibility(View.VISIBLE);
                buttonShuffleMain.setVisibility(View.VISIBLE);

                mSlidingUpPanelLayout.setTouchEnabled(true);
                mSlidingUpPanelLayout.setDragView(R.id.controlButtonLayout);

                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == buttonNextMain) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    buttonNextMain.setPressed(true);

                    return false;
                case MotionEvent.ACTION_UP:
                    buttonNextMain.setPressed(false);
                    if (skippingIsRunning) skippingIsRunning = false;
                    return true;
            }
            return false;
        } else if (v == buttonPrevMain) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    buttonPrevMain.setPressed(true);

                    return false;
                case MotionEvent.ACTION_UP:
                    buttonPrevMain.setPressed(false);
                    if (skippingIsRunning) skippingIsRunning = false;
                    return true;
            }
            return false;
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        if (v == buttonNextMain) {
            if (hasTrack()) {
                adapterIsUpdating = true;
                AppController.Instance().serviceNext();
            } else {
                AppController.toast(mContext, mContext.getString(R.string.no_song_in_queue));
            }

        } else if (v == buttonPrevMain) {
            if (hasTrack()) {
                adapterIsUpdating = true;
                AppController.Instance().servicePrev();
            } else {
                AppController.toast(mContext, mContext.getString(R.string.no_song_in_queue));
            }
        } else if (v == buttonPlayMain) {
            if (hasTrack()) {
                AppController.Instance().serviceToggle();
            } else {
                AppController.toast(mContext, mContext.getString(R.string.no_song_in_queue));
            }
        } else if (v == buttonQueueMain) {
            switch (playerMode) {
                case PLAYER_MINI:
                    updateMode(PlayerConstants.MiniPlayerState.PLAYER_QUEUE);
                    mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    break;
                case PLAYER_NOW:
                    openQueue = true;
                    updateMode(PlayerConstants.MiniPlayerState.PLAYER_MINI);
                    mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    break;
            }

        } else if (v == buttonRepeatMain) {
            toggleRepeat(true);
        } else if (v == buttonShuffleMain) {
            toggleShuffle(true);

        } else if (v == buttonDelete) {
            if (queueAdapter.isMultiSelect()) {
                ArrayList<QueueItem> list = queueAdapter.getSelectedItems();
                deleteItems(list);
            } else {
                new MaterialDialog.Builder(mContext)
                        .theme(Theme.LIGHT)
                        .titleColorRes(R.color.normal_blue)
                        .negativeColorRes(R.color.dialog_negetive_button)
                        .positiveColorRes(R.color.normal_blue)
                        .title("Clear Queue")
                        .content("Are you sure you want to clear the queue?")
                        .positiveText("Clear")
                        .onPositive((dialog, which) -> clear())
                        .negativeText("Cancel")
                        .show();
            }
        } else if (v == buttonCurrent) {
            queueList.scrollToPosition(queueAdapter.getItemPosition(PlayerConstants.QUEUE_SONG));
        } else if (v == buttonSavePlaylist) {
            AppController.Instance().addToPlaylist(mContext, queueAdapter.getList(), true);
        } else if (v == coverThumbnail) {
            if (hasTrack()) {
                Intent activityIntent = new Intent(mContext, NowPlayingActivity.class);
                mContext.startActivity(activityIntent);
                mContext.overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            } else {
                AppController.toast(mContext, mContext.getString(R.string.no_song_in_queue));
            }
        } else if (v == emptyQueueShuffleButton) {
            ArrayList<QueueItem> tracks = new ArrayList<>();
            tracks.addAll(TrackRealmHelper.getTracks(0).values());

            long seed = System.nanoTime();
            Collections.shuffle(tracks, new Random(seed));

            AppController.Instance().play(PlayerConstants.QUEUE_TYPE_TRACKS, 0, tracks);
        }
    }

    private void toggleRepeat(boolean change) {
        if (change) {
            int repeat = PrefsManager.Instance().getPlayRepeat();
            repeat++;
            if (repeat >= PlayerConstants.REPEAT_TOTAL) repeat = 0;
            PrefsManager.Instance().setPlayRepeat(repeat);
            AppController.Instance().serviceNotification(SongService.NOTIFICATION_REPEAT);
        }

        switch (PrefsManager.Instance().getPlayRepeat()) {
            case PlayerConstants.REPEAT_OFF:
                buttonRepeatMain.setImageResource((R.drawable.repeat_icon));
                break;

            case PlayerConstants.REPEAT_ALL:
                buttonRepeatMain.setImageResource((R.drawable.repeat_icon_blue));
                break;

            case PlayerConstants.REPEAT_ONE:
                buttonRepeatMain.setImageResource((R.drawable.repeatone_icon_blue));
                break;
        }
    }

    private void toggleShuffle(boolean change) {
        if (change) {
            PrefsManager.Instance().setPlayShuffle(!PrefsManager.Instance().getPlayShuffle(mContext));
            AppController.Instance().serviceNotification(SongService.NOTIFICATION_SHUFFLE);
        }

        if (PrefsManager.Instance().getPlayShuffle(mContext)) {
            buttonShuffleMain.setImageResource((R.drawable.shuffle_icon_blue));
        } else {
            buttonShuffleMain.setImageResource((R.drawable.shuffle_icon));
        }
    }

    private void updatePlayButton() {
        Log.i(TAG, String.valueOf(PlayerConstants.QUEUE_STATE));
        if (MuzikoExoPlayer.Instance().isPlaying()) {
            PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_PLAYING;
        }
        if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
            mediaControlDrawable.setMediaControlState(MediaControlDrawable.State.PAUSE);
        } else {
            mediaControlDrawable.setMediaControlState(MediaControlDrawable.State.PLAY);
        }
    }

    public void updateProgress(int progress, int duration) {

        handler.postDelayed(
                () -> {
                    if (playerMode == PlayerConstants.MiniPlayerState.PLAYER_QUEUE) {
                        if (!dragging && !adapterIsUpdating) {
                            queueAdapter.updateProgress(PlayerConstants.QUEUE_INDEX, progress, duration);
                        }
                    } else {
                        miniProgressBar.setMax(duration);
                        miniProgressBar.setProgress(progress);
                    }
                },
                50);

        if (popuptrack.getVisibility() == View.VISIBLE && progress > 2000) {
            int popUpDuration = 3000;
            handler.postDelayed(closePopUp, popUpDuration);
        }
    }

    public void showBufferingMessage(String message, boolean close) {
        if (!close & !buffering) {
            popuptrack.setText(message);
            popuptrack.setSelected(true);
            popuptrack.setVisibility(View.VISIBLE);
            buffering = true;
            bufferProgressBar.setVisibility(View.VISIBLE);
            buttonPlayMain.setVisibility(View.GONE);
        } else {
            if (buffering) {
                buffering = false;
                bufferProgressBar.setVisibility(View.GONE);
                buttonPlayMain.setVisibility(View.VISIBLE);

            }
            updateMiniPlayer();
        }
    }

    private void updateMiniPlayer() {

        ImageManager.Instance().loadImageListSmall(PlayerConstants.QUEUE_SONG, coverThumbnail, "mini");

        if (!MuzikoExoPlayer.Instance().isStreaming()) {
            popuptrack.setText(PlayerConstants.QUEUE_SONG.title + " - " + PlayerConstants.QUEUE_SONG.artist_name);
            popuptrack.setSelected(true);
        }

        if (playerMode == PlayerConstants.MiniPlayerState.PLAYER_MINI) {
            if (!MyApplication.currentTrack.data.equals(PlayerConstants.QUEUE_SONG.data)
                    && PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {

                handler.removeCallbacksAndMessages(closePopUp);
                popuptrack.setVisibility(View.VISIBLE);
                MyApplication.currentTrack = PlayerConstants.QUEUE_SONG;
                int popUpDuration = 3000;
                handler.postDelayed(closePopUp, popUpDuration);
            }
        }

    }

    public void updateUI() {

        if (adapterIsUpdating) {
            adapterIsUpdating = false;
        }

        // update queue
        queueAdapter.notifyDataSetChanged();
        if (!queueAdapter.isMultiSelect()) {
            setupMenu();
        }

        //update miniplayer
        updateMiniPlayer();
        updatePlayButton();
        updateProgress(PlayerConstants.QUEUE_TIME, Integer.parseInt(PlayerConstants.QUEUE_SONG.duration));
        toggleRepeat(false);
        toggleShuffle(false);

        if (!loaded) {
            loaded = true;
            layoutMiniPlayer();
        }

        toggleQueueVisible();
    }

    private void toggleQueueVisible() {
        if (PlayerConstants.QUEUE_LIST.size() == 0) {
            emptyQueueLayout.setVisibility(View.VISIBLE);
            queueList.setVisibility(View.GONE);
        } else {
            emptyQueueLayout.setVisibility(View.GONE);
            queueList.setVisibility(View.VISIBLE);
        }
    }

    public void layoutMiniPlayer() {

        final Resources resources = mContext.getResources();
        noDragging = true;
        handler.removeCallbacksAndMessages(allowDragging);
        handler.postDelayed(allowDragging, 1000);
        float height;

//        if (adLoaded) {
//            height = (int) mContext.getResources().getDimension(R.dimen.miniplayerHeightWithAd);
//        } else {
//            height = (int) mContext.getResources().getDimension(R.dimen.slidingLayoutHeight);
//        }
//
//        popuplayout.setVisibility(View.VISIBLE);
//
//        mSlidingUpPanelLayout.setPanelHeight((int) height);
//
//        if (mListener != null) {
//            if (adLoaded) {
//                mListener.onLayoutChanged(50);
//            } else {
//                mListener.onLayoutChanged(0);
//            }
//        }


    }

    @Override
    public void onDragTouched(RecyclerView.ViewHolder viewHolder) {
        if (touchHelper != null) touchHelper.startDrag(viewHolder);
    }

    @Override
    public void onMenuClicked(final int position) {
        final QueueItem item = queueAdapter.getItem(position);
        if (item == null) return;

        selectedItem = item;
        selectedItemPosition = position;

        final ArrayList<MenuObject> items = new ArrayList<>();
        items.add(new MenuObject(PLAY_NEXT));
        items.add(new MenuObject(PLAY_X_TIMES));
        items.add(new MenuObject(ADD_TO_QUEUE));
        items.add(new MenuObject(ADD_TO_PLAYLIST));
        items.add(
                new MenuObject(
                        FAV,
                        (TrackRealmHelper.getFavoritesList().indexOf(item.data) == -1)
                                ? mContext.getString(R.string.add_to_favs)
                                : mContext.getString(R.string.remove_from_favs)));

        items.add(new MenuObject(GO_TO_ALBUM));
        items.add(new MenuObject(GO_TO_ARTIST));
        items.add(new MenuObject(SET_RINGTONE));
        items.add(new MenuObject(SEND_AUDIO_CLIP));
        items.add(new MenuObject(PREVIEW_SONG));
        items.add(new MenuObject(MOVE_TO_IGNORE));
        items.add(new MenuObject(MenuObject.SHARE_ITEM));
        items.add(new MenuObject(CUT));
        items.add(new MenuObject(MenuObject.EDIT_TAGS));
        items.add(new MenuObject(DETAILS));
        items.add(new MenuObject(MenuObject.DELETE_FROM_QUEUE));

        final ArrayList<MenuObject> cloudItems = new ArrayList<>();
        cloudItems.add(new MenuObject(PLAY_NEXT));
        cloudItems.add(new MenuObject(PLAY_X_TIMES));
        cloudItems.add(new MenuObject(ADD_TO_QUEUE));
        cloudItems.add(new MenuObject(ADD_TO_PLAYLIST));
        cloudItems.add(
                new MenuObject(
                        FAV,
                        (TrackRealmHelper.getFavoritesList().indexOf(item.data) == -1)
                                ? mContext.getString(R.string.add_to_favs)
                                : mContext.getString(R.string.remove_from_favs)));
        cloudItems.add(new MenuObject(MOVE_TO_IGNORE));
        cloudItems.add(new MenuObject(MenuObject.SHARE_ITEM));
        cloudItems.add(new MenuObject(DETAILS));
        cloudItems.add(new MenuObject(DOWNLOAD));
        cloudItems.add(new MenuObject(MenuObject.DELETE_FROM_QUEUE));

        final ArrayList<MenuObject> firebaseItems = new ArrayList<>();
        if (item.storage == CloudManager.FIREBASE && FirebaseManager.Instance().isOverLimit()) {
            if (item.isLibrary()) {
                firebaseItems.add(new MenuObject(ADD_TO_LIBRARY, mContext.getString(R.string.remove_from_library)));
            }
            if (item.isSync()) {
                firebaseItems.add(new MenuObject(DONT_SYNC_FAV_OR_PLAYLIST));
            }
        } else {
            firebaseItems.add(new MenuObject(PLAY_NEXT));
            firebaseItems.add(new MenuObject(PLAY_X_TIMES));
            firebaseItems.add(new MenuObject(ADD_TO_QUEUE));
            firebaseItems.add(new MenuObject(ADD_TO_PLAYLIST));
            firebaseItems.add(
                    new MenuObject(
                            FAV,
                            (TrackRealmHelper.getFavoritesList().indexOf(item.data) == -1)
                                    ? mContext.getString(R.string.add_to_favs)
                                    : mContext.getString(R.string.remove_from_favs)));
            firebaseItems.add(new MenuObject(MOVE_TO_IGNORE));
            firebaseItems.add(new MenuObject(DOWNLOAD));
            firebaseItems.add(new MenuObject(DETAILS));
            if (item.isLibrary()) {
                firebaseItems.add(new MenuObject(ADD_TO_LIBRARY, mContext.getString(R.string.remove_from_library)));
            }
            if (item.isSync()) {
                firebaseItems.add(new MenuObject(DONT_SYNC_FAV_OR_PLAYLIST));
            }
        }

        MaterialMenuAdapter materialMenuAdapter = null;
        switch (item.storage) {
            case 0:
                materialMenuAdapter = new MaterialMenuAdapter(items, this);
                break;
            case 1:
                materialMenuAdapter = new MaterialMenuAdapter(items, this);
                break;
            case 2:
                materialMenuAdapter = new MaterialMenuAdapter(items, this);
                break;
            case CloudManager.FIREBASE:
                materialMenuAdapter = new MaterialMenuAdapter(firebaseItems, this);
                break;
            default:
                materialMenuAdapter = new MaterialMenuAdapter(cloudItems, this);
                break;
        }

        new MaterialDialog.Builder(mContext)
                .adapter(materialMenuAdapter, new LinearLayoutManager(mContext))
                .show();
    }

    @Override
    public void onItemClicked(int position) {
        if (this.queueAdapter.isMultiSelect()) {
            toggleSelection(position);
        } else {

            handler.postDelayed(
                    () ->
                            AppController.Instance().play(PlayerConstants.QUEUE_TYPE_QUEUE, position, queueAdapter.getList()),
                    mContext.getResources().getInteger(R.integer.ripple_duration_delay));
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (dragging) return false;

        if (queueList.isDragging()) {
            return false;
        }

        if (!this.queueAdapter.isMultiSelect()) {
            final Drawable overflowIcon = mainPlayerToolbar.getOverflowIcon();
            @ColorInt int color = Color.parseColor("#cccccc");
            mainPlayerToolbar.setOverflowIcon(getTintedDrawable(mContext, overflowIcon, color));
            mainPlayerToolbar.getMenu().clear();
            mainPlayerToolbar.inflateMenu(R.menu.menu_main_player_context);
            buttonCurrent.setVisibility(View.GONE);
            buttonSavePlaylist.setVisibility(View.GONE);
            mainPlayerToolbar.setOnMenuItemClickListener(
                    item -> {
                        ArrayList<QueueItem> list = queueAdapter.getSelectedItems();
                        if (list.size() > 0) {
                            // Handle presses on the action bar items
                            switch (item.getItemId()) {
                                case R.id.play:
                                    AppController.Instance().clearAddToQueue(mContext, list);
                                    break;

                                case share:
                                    AppController.Instance().shareSongs(list);
                                    break;

                                case R.id.add_to_queue:
                                    AppController.Instance().addToQueue(mContext, list, false);
                                    break;

                                case R.id.play_next:
                                    AppController.Instance().addToQueue(mContext, list, true);
                                    break;

                                case R.id.add_to_playlist:
                                    AppController.Instance().addToPlaylist(mContext, list, false);
                                    break;

                                case R.id.multi_tag_edit:
                                    AppController.Instance().multiTagEdit(mContext, list);
                                    break;

                                case R.id.trash:
                                    movetoNegative(list);
                                    break;

                                case R.id.favourite:
                                    favorite(list);
                                    break;

                                default:
                                    return false;
                            }
                        }

                        closeMultiSelect();
                        return true;
                    });
            mainPlayerToolbar.setNavigationIcon(
                    ContextCompat.getDrawable(mContext, R.drawable.ic_close_gray_24dp));
            mainPlayerToolbar.setNavigationOnClickListener(v -> closeMultiSelect());
            this.queueAdapter.setMultiSelect(true);
            toggleSelection(position);
            return true;
        }
        return false;
    }

    @Override
    public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

        handler.postDelayed(
                () -> {
                    switch (item.id) {
                        case ADD_TO_QUEUE: //add to q
                            PlayerConstants.QUEUE_TYPE = 0;
                            AppController.Instance().addToQueue(mContext, selectedItem, false);
                            break;

                        case ADD_TO_PLAYLIST: //add to p
                            AppController.Instance().addToPlaylist(mContext, selectedItem);
                            break;

                        case FAV: //add to f
                            favorite(selectedItem);
                            break;

                        case PLAY_NEXT: //play next
                            AppController.Instance().addToQueue(mContext, selectedItem, true);
                            break;

                        case GO_TO_ARTIST: //goto ar
                            MyApplication.transitionPosition = selectedItemPosition;
                            MyApplication.queueLast = true;

                            AppController.Instance().gotoArtist(mContext, selectedItem, null);

                            break;

                        case GO_TO_ALBUM: //goto al
                            MyApplication.transitionPosition = selectedItemPosition;
                            MyApplication.queueLast = true;

                            AppController.Instance().gotoAlbum(mContext, selectedItem, null);

                            break;

                        case SET_RINGTONE: //createRingtone
                            SetRingtone createRingtone = new SetRingtone();
                            createRingtone.open(mContext, selectedItem);
                            break;

                        case SEND_AUDIO_CLIP: //share ringtone
                            ShareRingtone shareRingtone = new ShareRingtone();
                            shareRingtone.open(mContext, selectedItem);
                            break;

                        case CUT: //cut
                            AppController.Instance().cutSong(selectedItem);
                            break;

                        case PREVIEW_SONG: //preview
                            PreviewSong previewSong = new PreviewSong();
                            previewSong.open(mContext, selectedItem);
                            break;

                        case MenuObject.EDIT_TAGS: //edit
                            AppController.Instance().editSong(mContext, TAG, selectedItemPosition, selectedItem);
                            break;

                        case DETAILS: //details
                            AppController.Instance().details(mContext, selectedItem);
                            break;

                        case MenuObject.SHARE_ITEM: //share
                            AppController.Instance().shareSong(mContext, selectedItem);
                            break;

                        case PLAY_X_TIMES: //remove
                            AppController.Instance().removeAfterExisting(mContext, selectedItemPosition);
                            break;

                        case MOVE_TO_IGNORE: //negative
                            movetoNegative(selectedItemPosition, selectedItem);
                            break;

                        case DELETE_FROM_QUEUE: //remove
                            delete(selectedItemPosition, selectedItem);
                            break;

                        case DOWNLOAD:
                            if (networkState != NetworkInfo.State.CONNECTED) {
                                AppController.toast(mContext, mContext.getString(R.string.no_network_connection));
                                return;
                            }
                            if (FileHelper.fileExists(selectedItem)) {
                                AppController.toast(mContext, mContext.getString(R.string.file_exists));
                                return;
                            }

                            DownloadFile downloadFile = new DownloadFile();
                            downloadFile.init(mContext, selectedItem);

                            break;
                    }

                    dialog.dismiss();
                },
                600);
    }

    private void favorite(final ArrayList<QueueItem> queueItems) {

        for (int i = 0; i < queueItems.size(); i++) {
            QueueItem queueItem = queueItems.get(i);
            TrackRealmHelper.addFavorite(queueItem.data);
        }

        AppController.toast(mContext, "Songs added to Favorites");
        mContext.sendBroadcast(new Intent(AppController.INTENT_FAVOURITE_CHANGED));
        mContext.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
        mContext.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));
    }

    private void movetoNegative(final ArrayList<QueueItem> queueItems) {
        Utils.askDelete(
                mContext,
                "Move to Ignore Folder",
                "You can later restore it from ignore folder, are you sure you want to ignore it?",
                () -> {
                    for (int i = 0; i < queueItems.size(); i++) {
                        QueueItem queueItem = queueItems.get(i);
                        TrackRealmHelper.movetoNegative(queueItem);
                    }

                    EventBus.getDefault().post(new RefreshEvent(1000));
                });
    }

    private void movetoNegative(final int position, final QueueItem queue) {
        Utils.askDelete(
                mContext,
                "Move to Ignore Folder",
                "You can later restore it from ignore folder, are you sure you want to ignore it?",
                () -> {
                    TrackRealmHelper.movetoNegative(queue);
                    queueAdapter.removeIndex(position);
                    EventBus.getDefault().post(new RefreshEvent(1000));
                });
    }

    private void closeMultiSelect() {
        mainPlayerToolbar.hideOverflowMenu();
        mainPlayerToolbar.setNavigationIcon(null);
        mainPlayerToolbar.getMenu().clear();
        queueAdapter.clearSelection();
        destroyActionMode();
        setupMenu();
        mainPlayerToolbar.setTitle("");
    }

    private void destroyActionMode() {
        handler.post(() -> {
            if (!queueList.isComputingLayout()) {
                ((SelectableAdapter) queueList.getAdapter()).setMultiSelect(false);
            } else {
                destroyActionMode();
            }
        });
    }

    private void delete(final int position, final QueueItem queue) {
        Utils.askDelete(
                mContext,
                "Delete From Queue",
                "Are you sure you want to delete this song from Queue ?",
                () -> {
                    removeFromQueue(position);

                });
    }

    private void removeFromQueue(int position) {
        queueAdapter.removeIndex(position);
        AsyncJob.doInBackground(
                () -> {
                    PlayerConstants.QUEUE_LIST = queueAdapter.getList();
                    QueueHelper.saveQueue(mContext);
                    final long level = PrefsManager.Instance().getQueueLevel() + 1;
                    PrefsManager.Instance().setQueueLevel(level);
                });
        if (PlayerConstants.QUEUE_INDEX == position) {
            AppController.Instance().play(PlayerConstants.QUEUE_TYPE_QUEUE, position, queueAdapter.getList());
        } else if (PlayerConstants.QUEUE_INDEX == position + 1) {
            PlayerConstants.QUEUE_INDEX = PlayerConstants.QUEUE_INDEX - 1;
            queueAdapter.notifyDataSetChanged();
        } else {
            PlayerConstants.QUEUE_TYPE = 0;
            QueueItem queueItem;
            if (position == queueAdapter.getList().size()) {
                queueItem = queueAdapter.getItem(position - 1);
            } else {
                queueItem = queueAdapter.getItem(position);
            }
            if (queueItem != null) {
                AppController.Instance().serviceUnqueue(queueItem.hash);
            }
            AppController.Instance().serviceDirty();
        }
    }

    private void deleteItems(final ArrayList<QueueItem> list) {
        Utils.askDelete(
                mContext,
                "Delete From Queue",
                String.format(
                        "Are you sure you want to delete song%s from Queue ?", list.size() != 1 ? "s" : ""),
                () -> {
                    for (QueueItem item : list) {
                        if (item == null) continue;

                        AppController.Instance().serviceUnqueue(item.hash);
                    }

                    queueAdapter.removeAll(list);
                    closeMultiSelect();

                    PlayerConstants.QUEUE_TYPE = 0;

                    mContext.sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
                    mContext.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
                    mContext.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));

                    AppController.toast(
                            mContext, String.format("Song%s deleted from Queue", list.size() != 1 ? "s" : ""));

                    AppController.Instance().serviceDirty();
                });
    }

    private void favorite(final QueueItem queue) {
        FavoriteEdit fe =
                new FavoriteEdit(
                        mContext, PlayerConstants.QUEUE_TYPE_QUEUE, s -> queueAdapter.notifyDataSetChanged());
        fe.execute(queue);
    }

    private void toggleSelection(int position) {
        ((SelectableAdapter) queueList.getAdapter()).toggleSelection(position);

        int count = ((SelectableAdapter) queueList.getAdapter()).getSelectedItemCount();

        if (count == 0) mainPlayerToolbar.setTitle("");
        else
            mainPlayerToolbar.setTitle(String.format(Locale.ENGLISH, "%d song%s", count, count != 1 ? "s" : ""));
    }

    private void updateQueue() {
        AsyncJob.doInBackground(
                () -> {
                    PlayerConstants.QUEUE_LIST = queueAdapter.getList();
                    QueueHelper.saveQueue(mContext);
                    final long level = PrefsManager.Instance().getQueueLevel() + 1;
                    PrefsManager.Instance().setQueueLevel(level);
                });
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == buttonPrevMain) {

            QueueItem queueItem = TrackRealmHelper.getSecondMostRecentlyPlayed();

            if (queueItem == null) {
                AppController.toast(mContext, "No last played song to play");
            } else {
                int index = -1;
                for (int i = 0; i < PlayerConstants.QUEUE_LIST.size(); i++) {

                    if (PlayerConstants.QUEUE_LIST.get(i).data.equals(queueItem.data)) {
                        index = i;
                        break;
                    }
                }

                if (index < 0) {
                    //					MyApplication.playCurrentSong(this, queueItem);
                    PlayerConstants.QUEUE_LIST.add(PlayerConstants.QUEUE_INDEX + 1, queueItem);
                    PlayerConstants.QUEUE_INDEX = PlayerConstants.QUEUE_INDEX + 1;
                    handler.postDelayed(
                            () -> {
                                queueAdapter.notifyDataSetChanged();
                                queueList.scrollToPosition(PlayerConstants.QUEUE_INDEX);
                            },
                            200);

                    AppController.Instance().servicePlay(false);
                    AppController.Instance().serviceDirty();

                } else {
                    AppController.Instance()
                            .play(PlayerConstants.QUEUE_TYPE_QUEUE, index, PlayerConstants.QUEUE_LIST);
                    final int finalIndex = index;
                    handler.postDelayed(() -> queueList.scrollToPosition(finalIndex), 200);
                }
            }
        }
        return true;
    }

    @Override
    public void onFastScrollStart() {

        this.dragging = true;
    }

    @Override
    public void onFastScrollStop() {
        this.dragging = false;
    }

    @Override
    public void onAdLoaded() {
        adLoaded = true;
        Resources resources = mContext.getResources();
        LinearLayout.LayoutParams parms =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Utils.toPixels(resources, 50));
        admobLayout.setLayoutParams(parms);
        admobLayout.requestLayout();
        showAdMob();
        layoutMiniPlayer();
    }

    @Override
    public void onAdClosed() {
        adLoaded = false;
        Resources resources = mContext.getResources();
        LinearLayout.LayoutParams
                parms =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Utils.toPixels(resources, 30));
        admobLayout.setLayoutParams(parms);
        admobLayout.requestLayout();
        showAdMob();
        layoutMiniPlayer();
    }

    private void showAdMob() {

        if (adLoaded) {
            admob.setVisibility(View.VISIBLE);
        } else {
            admob.setVisibility(View.GONE);
        }
    }

    public void removeAds() {
        if (!AppController.Instance().shouldShowAd()) {
            adLoaded = false;
            if (adMobBanner != null) {
                adMobBanner.stop();
            }
            Resources resources = mContext.getResources();
            LinearLayout.LayoutParams parms =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, Utils.toPixels(resources, 30));
            admobLayout.setLayoutParams(parms);
            admobLayout.requestLayout();
            admob.setVisibility(View.GONE);
            layoutMiniPlayer();
        }
    }

    public interface onLayoutListener {

        void onLayoutChanged(float margin);
    }

    private class MainPlayerTouchHelper extends ItemTouchHelper.Callback {

        private static final long DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS = 2000; // default
        int dragFrom = -1;
        int dragTo = -1;
        private int mCachedMaxScrollSpeed = -1; // default

        //defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(
                    ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
        }

        //and in your imlpementaion of
        public boolean onMove(
                RecyclerView recyclerView,
                RecyclerView.ViewHolder viewHolder,
                RecyclerView.ViewHolder target) {
            dragging = true;
            dragFrom = viewHolder.getAdapterPosition();
            dragTo = target.getAdapterPosition();
            if (queueAdapter.moveTo(dragFrom, dragTo)) {
                if (PlayerConstants.QUEUE_INDEX == dragFrom) {
                    PlayerConstants.QUEUE_INDEX = dragTo;
                } else {
                    AppController.Instance().serviceDirty();
                    AppController.Instance().updateQueueIndex();
                }
            }
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);

            dragFrom = dragTo = -1;
            handler.removeCallbacksAndMessages(stopDragging);
            handler.postDelayed(stopDragging, 2000);
        }

        @Override
        public int interpolateOutOfBoundsScroll(
                RecyclerView recyclerView,
                int viewSize,
                int viewSizeOutOfBounds,
                int totalSize,
                long msSinceStartScroll) {
            final int maxScroll = getMaxDragScroll(recyclerView);
            final int absOutOfBounds = Math.abs(viewSizeOutOfBounds);
            final int direction = (int) Math.signum(viewSizeOutOfBounds);
            // might be negative if other direction
            float outOfBoundsRatio = Math.min(1f, 1f * absOutOfBounds / viewSize);
            final int cappedScroll =
                    (int)
                            (direction
                                    * maxScroll
                                    * ItemTouchHelpers.sDragViewScrollCapInterpolator.getInterpolation(
                                    outOfBoundsRatio));
            final float timeRatio;
            if (msSinceStartScroll > DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS) {
                timeRatio = 1f;

                //            timeRatio = 3f;
            } else {
                timeRatio = (float) msSinceStartScroll / DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS;
            }
            final int value =
                    (int)
                            (cappedScroll * ItemTouchHelpers.sDragScrollInterpolator.getInterpolation(timeRatio));
            if (value == 0) {
                return viewSizeOutOfBounds > 0 ? 1 : -1;
            }
            return value;
        }

        private int getMaxDragScroll(RecyclerView recyclerView) {
            if (mCachedMaxScrollSpeed == -1) {
                mCachedMaxScrollSpeed =
                        recyclerView
                                .getResources()
                                .getDimensionPixelSize(
                                        android.support.v7.recyclerview.R.dimen.item_touch_helper_max_drag_scroll_per_frame);
            }
            return mCachedMaxScrollSpeed;
        }
    }
}
