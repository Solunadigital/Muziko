package com.muziko.manager;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.util.Log;

import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muziko.cloud.registration.Registration;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudPlaylist;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.common.models.firebase.Person;
import com.muziko.common.models.firebase.Share;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.database.SubscriptionTypeRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.FileHelper;
import com.muziko.helpers.MD5;
import com.muziko.interfaces.DownloadListener;
import com.muziko.models.MuzikoSubscription;
import com.muziko.models.MuzikoSubscriptionType;
import com.muziko.service.MuzikoFirebaseService;
import com.muziko.tasks.FirebaseDeleteForUpdateTask;
import com.muziko.tasks.FirebaseDeleteTask;
import com.muziko.tasks.FirebaseDownloadTask;
import com.muziko.tasks.FirebasePlaylistAddTask;
import com.muziko.tasks.FirebasePlaylistDeleteTask;
import com.muziko.tasks.FirebaseUploadTask;
import com.muziko.tasks.ShareTrackDownloader;
import com.muziko.tasks.ShareTrackUploader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import hugo.weaving.DebugLog;

import static com.muziko.MyApplication.hasWifi;
import static com.muziko.MyApplication.networkState;
import static com.muziko.manager.MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_FREE;
import static com.muziko.manager.MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_ONE;
import static com.muziko.manager.MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_THREE;
import static com.muziko.manager.MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_TWO;
import static com.muziko.manager.MuzikoConstants.STORAGE_SUBSCRIPTION_LEVEL_UNLIMITED;
import static com.muziko.manager.MuzikoConstants.halfHourMilliseconds;


/**
 * Created by Bradley on 16/05/2017.
 */
public class FirebaseManager implements DownloadListener {
    public static final String favs = "favs";
    public static final String library = "library";
    public static final String playlists = "playlists";
    public static final HashMap<String, ShareTrackDownloader> firebaseShareDownloaderTasks = new HashMap<>();
    public static final HashMap<String, ShareTrackUploader> firebaseShareUploaderTasks = new HashMap<>();
    public static final HashMap<String, Long> firebaseCancelledUploads = new HashMap<>();
    private static final String TAG = FirebaseManager.class.getSimpleName();
    private static final String playlist_tracks = "playlist_tracks";
    public static String cloudTracks = "cloudTracks";
    private static FirebaseManager instance;
    private final HashMap<String, FirebaseDownloadTask> firebaseDownloadTasks = new HashMap<>();
    private final HashMap<String, FirebaseUploadTask> firebaseUploadTasks = new HashMap<>();
    private final HashMap<String, FirebasePlaylistAddTask> playlistUploadTasks = new HashMap<>();
    //Firebase
    private Person firebaseMe;
    private ArrayList<QueueItem> firebaseRemovedList = new ArrayList<>();
    private ArrayList<CloudTrack> FirebaseLibraryList = new ArrayList<>();
    private ArrayList<CloudTrack> FirebaseFavsList = new ArrayList<>();
    private ArrayList<CloudTrack> FirebasePlaylistTracksList = new ArrayList<>();
    private ArrayList<CloudPlaylist> FirebasePlaylistsList = new ArrayList<>();
    private ArrayList<MuzikoSubscriptionType> subscriptionTypes = new ArrayList<>();
    private ArrayList<QueueItem> libraryTracks = new ArrayList<>();
    private ArrayList<QueueItem> favTracks = new ArrayList<>();
    private ArrayList<PlaylistItem> playlistsTracks = new ArrayList<>();
    private long lastGotFavsFromDb = 0;
    private long lastGotLibraryFromDb = 0;
    private long lastGotPlaylistsFromDb = 0;
    private Context mContext;
    private boolean firebaseStarted = false;
    private boolean playlistsLoaded = false;
    private boolean playlistTracksLoaded = false;
    private boolean favsLoaded = false;
    private boolean libraryLoaded = false;
    private boolean uploadRunning = false;
    private boolean downloadRunning = false;
    private boolean subscriptionsReady = false;
    private ValueEventListener subscriptionListener;
    //no outer class can initialize this class's object
    private FirebaseManager() {
    }

    public static FirebaseManager Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public static void sendRegistrationToServer(String token) {
        AsyncJob.doInBackground(() -> {
            try {
//            Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
//                    new AndroidJsonFactory(), null)
//                    // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
//                    // otherwise they can be skipped
//                    .setRootUrl("http://10.1.1.104:8080/_ah/api/")
//                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
//                        @Override
//                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
//                                throws IOException {
//                            abstractGoogleClientRequest.setDisableGZipContent(true);
//                        }
//                    });
                Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl("https://muziko-48de4.appspot.com/_ah/api/");
                Registration regService = builder.build();

                regService.register(token).execute();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        });
    }

    public ArrayList<QueueItem> getFirebaseRemovedList() {
        return firebaseRemovedList;
    }

    public boolean isSubscriptionsReady() {
        return subscriptionsReady;
    }

    public void setSubscriptionsReady(boolean subscriptionsReady) {
        this.subscriptionsReady = subscriptionsReady;
    }

    public Person getFirebaseMe() {
        return firebaseMe;
    }

    public void setFirebaseMe(Person firebaseMe) {
        this.firebaseMe = firebaseMe;
    }

    public void saveSubscription(MuzikoSubscription subscription) {
        DatabaseReference subscriptionRef = FirebaseManager.Instance().getSubscriptionsRef();
        subscriptionRef.child(getCurrentUserId()).child(subscription.getSubscriptionTypeID()).setValue(subscription, (error, firebase) -> {
            if (error != null) {
                Log.i(TAG, "Subscription save error");
            }
        });
    }

    public MuzikoSubscriptionType getSubscriptionByType(String subscriptionId) {
        MuzikoSubscriptionType subscriptionType = null;
        for (MuzikoSubscriptionType muzikoSubscriptionType : getSubscriptionTypes()) {
            if (muzikoSubscriptionType.getSubscriptionTypeID() != null) {
                if (muzikoSubscriptionType.getSubscriptionTypeID().equals(subscriptionId)) {
                    subscriptionType = muzikoSubscriptionType;
                    break;
                }
            }
        }

        return subscriptionType;
    }

    public ArrayList<MuzikoSubscriptionType> getSubscriptionTypes() {
        return SubscriptionTypeRealmHelper.getSubscriptionTypes();
    }

    public boolean isPlaylistTracksLoaded() {
        return playlistTracksLoaded;
    }

    public void setPlaylistTracksLoaded(boolean playlistTracksLoaded) {
        this.playlistTracksLoaded = playlistTracksLoaded;
    }

    public ArrayList<CloudTrack> getFirebasePlaylistTracksList() {
        return FirebasePlaylistTracksList;
    }

    public void setFirebasePlaylistTracksList(ArrayList<CloudTrack> firebasePlaylistTracksList) {
        FirebasePlaylistTracksList = firebasePlaylistTracksList;
    }

    public boolean isDownloadRunning() {
        return downloadRunning;
    }

    public void setDownloadRunning(boolean downloadRunning) {
        this.downloadRunning = downloadRunning;
    }

    public boolean isUploadRunning() {
        return uploadRunning;
    }

    public void setUploadRunning(boolean uploadRunning) {
        this.uploadRunning = uploadRunning;
    }

    public boolean isPlaylistsLoaded() {
        return playlistsLoaded;
    }

    public void setPlaylistsLoaded(boolean playlistsLoaded) {
        this.playlistsLoaded = playlistsLoaded;
    }

    public boolean isFavsLoaded() {
        return favsLoaded;
    }

    public void setFavsLoaded(boolean favsLoaded) {
        this.favsLoaded = favsLoaded;
    }

    public boolean isLibraryLoaded() {
        return libraryLoaded;
    }

    public void setLibraryLoaded(boolean libraryLoaded) {
        this.libraryLoaded = libraryLoaded;
    }

    private DatabaseReference getBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public DatabaseReference getPromosRef() {
        return getBaseRef().child("promocodes");
    }

    public DatabaseReference getRegistrationsRef() {
        return getBaseRef().child("registrations");
    }

    public String getPeoplePath() {
        return "people/";
    }

    public DatabaseReference getFavRef() {
        return getBaseRef().child(favs);
    }

    public DatabaseReference getPlaylistsRef() {
        return getBaseRef().child(playlists);
    }

    public DatabaseReference getPlaylistsTracksRef() {
        return getBaseRef().child(playlist_tracks);
    }

    public DatabaseReference getPeopleRef() {
        return getBaseRef().child("people");
    }

    public DatabaseReference getShareRef() {
        return getBaseRef().child("share");
    }

    public DatabaseReference getLibraryRef() {
        return getBaseRef().child(library);
    }

    public DatabaseReference getTracksRef() {
        return getBaseRef().child("tracks");
    }

    public DatabaseReference getMostPlayedRef() {
        return getBaseRef().child("mostplayed");
    }

    public DatabaseReference getConnectionsRef() {
        return getBaseRef().child(".info/connected");
    }

    public DatabaseReference getSubscriptionTypesRef() {
        return getBaseRef().child("subscriptionTypes");
    }

    public DatabaseReference getSubscriptionsRef() {
        return getBaseRef().child("subscriptions");
    }

    public HashMap<String, FirebaseDownloadTask> getFirebaseDownloadTasks() {
        return firebaseDownloadTasks;
    }

    public void addFirebaseDownloadTask(String key, FirebaseDownloadTask firebaseDownloadTask) {
        this.firebaseDownloadTasks.put(key, firebaseDownloadTask);
    }

    public void removeFirebaseDownloadTask(String key) {
        this.firebaseDownloadTasks.remove(key);
    }


    public HashMap<String, FirebaseUploadTask> getFirebaseUploadTasks() {
        return firebaseUploadTasks;
    }

    public void addFirebaseUploadTask(String key, FirebaseUploadTask firebaseUploadTask) {
        this.firebaseUploadTasks.put(key, firebaseUploadTask);
    }

    public void removeFirebaseUploadTask(String key) {
        this.firebaseUploadTasks.remove(key);
    }

    private HashMap<String, FirebasePlaylistAddTask> getPlaylistUploadTasks() {
        return playlistUploadTasks;
    }

    private void addPlaylistUploadTask(String key, FirebasePlaylistAddTask firebasePlaylistAddTask) {
        this.playlistUploadTasks.put(key, firebasePlaylistAddTask);
    }

    public void removePlaylistUploadTask(String key) {
        this.playlistUploadTasks.remove(key);
    }

    public ArrayList<CloudTrack> getFirebaseLibraryList() {
        return FirebaseLibraryList;
    }

    public void setFirebaseLibraryList(ArrayList<CloudTrack> firebaseLibraryList) {
        FirebaseLibraryList = firebaseLibraryList;
    }

    public ArrayList<CloudTrack> getFirebaseFavsList() {
        return FirebaseFavsList;
    }

    public void setFirebaseFavsList(ArrayList<CloudTrack> firebaseFavsList) {
        FirebaseFavsList = firebaseFavsList;
    }

    public ArrayList<CloudPlaylist> getFirebasePlaylistsList() {
        return FirebasePlaylistsList;
    }

    public void setFirebasePlaylistsList(ArrayList<CloudPlaylist> firebasePlaylistsList) {
        FirebasePlaylistsList = firebasePlaylistsList;
    }

    public ArrayList<QueueItem> getFirebaseFavTracks() {
        if (lastGotFavsFromDb + 30000 < System.currentTimeMillis()) {
            lastGotFavsFromDb = System.currentTimeMillis();
            favTracks = TrackRealmHelper.getSyncFavorites();
        }
        return favTracks;
    }

    public ArrayList<QueueItem> getFirebaseLibraryTracks() {
        if (lastGotLibraryFromDb + 30000 < System.currentTimeMillis()) {
            lastGotLibraryFromDb = System.currentTimeMillis();
            libraryTracks = TrackRealmHelper.getLibrary();
        }
        return libraryTracks;
    }

    public ArrayList<QueueItem> getFirebasePlaylistTracks() {
        ArrayList<QueueItem> playlistTracks = new ArrayList<>();
        if (lastGotPlaylistsFromDb + 30000 < System.currentTimeMillis()) {
            lastGotPlaylistsFromDb = System.currentTimeMillis();
            playlistsTracks = PlaylistRealmHelper.loadAllWithSync();

        }

        for (PlaylistItem playlist : playlistsTracks) {
            ArrayList<QueueItem> tracks = PlaylistSongRealmHelper.loadAllByPlaylist(0, playlist.getId());
            playlistTracks.addAll(tracks);
        }
        return playlistTracks;
    }

    public int getFirebaseTrackCount() {
        return getFirebaseFavTracks().size() + getFirebaseLibraryTracks().size() + getFirebasePlaylistTracks().size();
    }

    public boolean isOverLimit() {
        try {
            if (getSubscriptionType() != null) {
                if (getSubscriptionType() != null && getFirebaseTrackCount() > 0 && getFirebaseTrackCount() > getSubscriptionType().getSongLimit()) {
                    Intent shareintent = new Intent(AppController.ACTION_FIREBASE_OVERLIMIT);
                    mContext.sendBroadcast(shareintent);
                    return true;
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return false;
    }

    public boolean isOverLimitNow(int newSongCount) {
        try {
            if (getSubscriptionType() != null) {
                if (getSubscriptionType() != null && getFirebaseTrackCount() > 0 && (getFirebaseTrackCount() + newSongCount > getSubscriptionType().getSongLimit())) {
                    Intent shareintent = new Intent(AppController.ACTION_FIREBASE_OVERLIMIT_NOW);
                    mContext.sendBroadcast(shareintent);
                    return true;
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return false;
    }

    public MuzikoSubscriptionType getSubscriptionType() {
        MuzikoSubscriptionType mySubscriptionType = getFreeSubscriptionType();
        for (MuzikoSubscriptionType subscriptionType : FirebaseManager.Instance().getSubscriptionTypes()) {
            if (subscriptionType != null) {
                if (subscriptionType.getSubscriptionTypeID().equals(PrefsManager.Instance().getSubscription())) {
                    mySubscriptionType = subscriptionType;
                    break;
                }
            }
        }
        return mySubscriptionType;
    }

    public MuzikoSubscriptionType getFreeSubscriptionType() {

        MuzikoSubscriptionType mySubscriptionType = new MuzikoSubscriptionType();
        mySubscriptionType.setSubscriptionTypeID(STORAGE_SUBSCRIPTION_LEVEL_FREE);
        mySubscriptionType.setSubscriptionName("Free Plan");
        mySubscriptionType.setSongLimit(50);
//        try {
//            for (MuzikoSubscriptionType subscriptionType : FirebaseManager.Instance().getSubscriptionTypes()) {
//                if (subscriptionType.getSubscriptionTypeID().equals(STORAGE_SUBSCRIPTION_LEVEL_FREE)) {
//                    mySubscriptionType = subscriptionType;
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            Crashlytics.logException(e);
//        }
        return mySubscriptionType;
    }

    public MuzikoSubscriptionType getLevelOneSubscriptionType() {
        MuzikoSubscriptionType mySubscriptionType = getFreeSubscriptionType();
        for (MuzikoSubscriptionType subscriptionType : FirebaseManager.Instance().getSubscriptionTypes()) {
            if (subscriptionType.getSubscriptionTypeID().equals(STORAGE_SUBSCRIPTION_LEVEL_ONE)) {
                mySubscriptionType = subscriptionType;
                break;
            }
        }
        return mySubscriptionType;
    }

    public MuzikoSubscriptionType getLevelTwoSubscriptionType() {
        MuzikoSubscriptionType mySubscriptionType = getFreeSubscriptionType();
        for (MuzikoSubscriptionType subscriptionType : FirebaseManager.Instance().getSubscriptionTypes()) {
            if (subscriptionType.getSubscriptionTypeID().equals(STORAGE_SUBSCRIPTION_LEVEL_TWO)) {
                mySubscriptionType = subscriptionType;
                break;
            }
        }
        return mySubscriptionType;
    }

    public MuzikoSubscriptionType getLevelThreeSubscriptionType() {
        MuzikoSubscriptionType mySubscriptionType = getFreeSubscriptionType();
        for (MuzikoSubscriptionType subscriptionType : FirebaseManager.Instance().getSubscriptionTypes()) {
            if (subscriptionType.getSubscriptionTypeID().equals(STORAGE_SUBSCRIPTION_LEVEL_THREE)) {
                mySubscriptionType = subscriptionType;
                break;
            }
        }
        return mySubscriptionType;
    }

    public MuzikoSubscriptionType getLevelFourSubscriptionType() {
        MuzikoSubscriptionType mySubscriptionType = getFreeSubscriptionType();
        for (MuzikoSubscriptionType subscriptionType : FirebaseManager.Instance().getSubscriptionTypes()) {
            if (subscriptionType.getSubscriptionTypeID().equals(STORAGE_SUBSCRIPTION_LEVEL_UNLIMITED)) {
                mySubscriptionType = subscriptionType;
                break;
            }
        }
        return mySubscriptionType;
    }

    public boolean isAnonymous() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null || user.isAnonymous();
    }

    public String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return "";
    }

    public DatabaseReference getCurrentUserRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseRef().child("people").child(getCurrentUserId());
        }
        return null;
    }

    public DatabaseReference getContactsRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseRef().child("contacts").child(getCurrentUserId());
        }
        return null;
    }

    public void init(Context context) {
        mContext = context;
    }

    public boolean isFirebaseStarted() {
        return firebaseStarted;
    }

    public void startFirebase() {
        this.firebaseStarted = true;
        if (!AppController.Instance().isMyServiceRunning(MuzikoFirebaseService.class)) {
            mContext.startService(new Intent(mContext, MuzikoFirebaseService.class));
        }
    }

    public void stopFirebase() {
        this.firebaseStarted = false;
        if (AppController.Instance().isMyServiceRunning(MuzikoFirebaseService.class)) {
            mContext.stopService(new Intent(mContext, MuzikoFirebaseService.class));
        }
    }

    @DebugLog
    public void downloadShare(Share share) {
        AsyncJob.doInBackground(() -> {
            ShareTrackDownloader shareTrackDownloader = new ShareTrackDownloader(mContext, share, FirebaseManager.this);
            firebaseShareDownloaderTasks.put(share.getShareUrl(), shareTrackDownloader);
            shareTrackDownloader.startDownload();
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public void uploadShare(QueueItem queueItem, ArrayList<Person> personList) {
        ShareTrackUploader shareTrackUploader = new ShareTrackUploader(mContext, queueItem, personList);
        firebaseShareUploaderTasks.put(queueItem.data, shareTrackUploader);
        shareTrackUploader.startUpload();

    }

//    public void uploadPlaylist(String playlistHash) {
//        AsyncJob.doInBackground(() -> {
//            if (checkPrerequisites() && PlaylistRealmHelper.getPlaylist(playlistHash).isSync() && !isUploadRunning()) {
//                FirebasePlaylistAddTask firebasePlaylistAddTask = getPlaylistUploadTasks().get(playlistHash);
//                if (firebasePlaylistAddTask == null) {
//                    firebasePlaylistAddTask = new FirebasePlaylistAddTask(mContext, playlistHash);
//                    addPlaylistUploadTask(playlistHash, firebasePlaylistAddTask);
//                    firebasePlaylistAddTask.executeOnExecutor(ThreadManager.Instance().getMuzikoTransferPool());
//                }
//            }
//        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
//
//    }

    @DebugLog
    public void uploadFav(QueueItem queueItem) {
        AsyncJob.doInBackground(() -> {
            if (checkPrerequisites() && checkFileIsLocal(queueItem) && checkShouldUpload(queueItem) && !isUploadRunning()) {

                FirebaseUploadTask firebaseUploadTask = firebaseUploadTasks.get(queueItem.data);
                if (firebaseUploadTask == null) {
                    firebaseUploadTask = new FirebaseUploadTask(mContext, queueItem, MuzikoConstants.FirebaseFileMode.FAVS);
                    firebaseUploadTask.executeOnExecutor(ThreadManager.Instance().getMuzikoTransferPool());
//                    firebaseUploadTasks.put(queueItem.data, firebaseUploadTask);
                }
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());


    }

    @DebugLog
    public void uploadLibrary(QueueItem queueItem) {
        AsyncJob.doInBackground(() -> {
            boolean reqs = checkPrerequisites();
            boolean isLocal = checkFileIsLocal(queueItem);
            boolean shouldUpload = checkShouldUpload(queueItem);
            if (reqs && isLocal && shouldUpload && !isUploadRunning()) {
                FirebaseUploadTask firebaseUploadTask = firebaseUploadTasks.get(queueItem.data);
                if (firebaseUploadTask == null) {
                    firebaseUploadTask = new FirebaseUploadTask(mContext, queueItem, MuzikoConstants.FirebaseFileMode.LIBRARY);
                    firebaseUploadTasks.put(queueItem.data, firebaseUploadTask);
                    firebaseUploadTask.executeOnExecutor(ThreadManager.Instance().getMuzikoTransferPool());
                }
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

//    @DebugLog
//    public void uploadPlaylistTrack(QueueItem queueItem, String playlistHash) {
//        AsyncJob.doInBackground(() -> {
//            if (checkPrerequisites() && checkFileIsLocal(queueItem) && checkShouldUpload(queueItem) && !isUploadRunning()) {
//                FirebaseUploadTask firebaseUploadTask = firebaseUploadTasks.get(queueItem.data);
//                if (firebaseUploadTask == null) {
//                    firebaseUploadTask = new FirebaseUploadTask(mContext, queueItem, playlistHash, MuzikoConstants.FirebaseFileMode.PLAYLISTS);
//                    firebaseUploadTask.executeOnExecutor(ThreadManager.Instance().getMuzikoTransferPool());
////                    firebaseUploadTasks.put(queueItem.data, firebaseUploadTask);
//                }
//            }
//        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
//
//    }

    @DebugLog
    public void downloadLibrary(CloudTrack cloudTrack) {
        AsyncJob.doInBackground(() -> {
            if (checkPrerequisites()) {
                FirebaseDownloadTask firebaseDownloadTask = firebaseDownloadTasks.get(cloudTrack.getUrl());
                if (firebaseDownloadTask == null) {
                    firebaseDownloadTask = new FirebaseDownloadTask(mContext, cloudTrack, MuzikoConstants.FirebaseFileMode.LIBRARY);
                    firebaseDownloadTasks.put(cloudTrack.getUrl(), firebaseDownloadTask);
                }
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    private void downloadFav(CloudTrack cloudTrack) {
        AsyncJob.doInBackground(() -> {
            if (checkPrerequisites()) {
                FirebaseDownloadTask firebaseDownloadTask = firebaseDownloadTasks.get(cloudTrack.getUrl());
                if (firebaseDownloadTask == null) {
                    firebaseDownloadTask =
                            new FirebaseDownloadTask(mContext, cloudTrack, MuzikoConstants.FirebaseFileMode.FAVS);
                    firebaseDownloadTasks.put(cloudTrack.getUrl(), firebaseDownloadTask);
                }
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public void downloadFav(CloudTrack cloudTrack, String downloadPath) {
        AsyncJob.doInBackground(() -> {
            if (checkPrerequisites()) {
                FirebaseDownloadTask firebaseDownloadTask = firebaseDownloadTasks.get(cloudTrack.getUrl());
                if (firebaseDownloadTask == null) {
                    firebaseDownloadTask = new FirebaseDownloadTask(mContext, cloudTrack, MuzikoConstants.FirebaseFileMode.FAVS, downloadPath);
                    firebaseDownloadTasks.put(cloudTrack.getUrl(), firebaseDownloadTask);
                }
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    private void downloadPlaylistTrack(CloudTrack cloudTrack, String playlistHash) {
        AsyncJob.doInBackground(() -> {
            if (checkPrerequisites()) {
                FirebaseDownloadTask firebaseDownloadTask = firebaseDownloadTasks.get(cloudTrack.getUrl());
                if (firebaseDownloadTask == null) {
                    firebaseDownloadTask = new FirebaseDownloadTask(mContext, cloudTrack, playlistHash, MuzikoConstants.FirebaseFileMode.PLAYLISTS);
                    firebaseDownloadTasks.put(cloudTrack.getUrl(), firebaseDownloadTask);
                }
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public void downloadPlaylistTrack(CloudTrack cloudTrack, String playlistHash, String downloadPath) {
        AsyncJob.doInBackground(() -> {
            if (checkPrerequisites()) {
                FirebaseDownloadTask firebaseDownloadTask = firebaseDownloadTasks.get(cloudTrack.getUrl());
                if (firebaseDownloadTask == null) {
                    firebaseDownloadTask = new FirebaseDownloadTask(mContext, cloudTrack, playlistHash, MuzikoConstants.FirebaseFileMode.PLAYLISTS, downloadPath);
                    firebaseDownloadTasks.put(cloudTrack.getUrl(), firebaseDownloadTask);
                }
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public void deleteFavForUpdate(QueueItem queueItem) {
        AsyncJob.doInBackground(() -> {
            FirebaseDeleteForUpdateTask deleteForUpdateTask = new FirebaseDeleteForUpdateTask(queueItem, MuzikoConstants.FirebaseFileMode.FAVS);
            deleteForUpdateTask.executeOnExecutor(ThreadManager.Instance().getMuzikoBackgroundThreadPool());
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public void deleteLibraryForUpdate(QueueItem queueItem) {
        AsyncJob.doInBackground(() -> {
            FirebaseDeleteForUpdateTask deleteForUpdateTask = new FirebaseDeleteForUpdateTask(queueItem, MuzikoConstants.FirebaseFileMode.LIBRARY);
            deleteForUpdateTask.executeOnExecutor(ThreadManager.Instance().getMuzikoBackgroundThreadPool());
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public void deleteFav(QueueItem queueItem) {
        AsyncJob.doInBackground(() -> {
            FirebaseDeleteTask libraryDeleteTask = new FirebaseDeleteTask(queueItem, MuzikoConstants.FirebaseFileMode.FAVS);
            libraryDeleteTask.executeOnExecutor(ThreadManager.Instance().getMuzikoBackgroundThreadPool());
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public void deleteLibrary(QueueItem queueItem) {
        AsyncJob.doInBackground(() -> {
            FirebaseDeleteTask libraryDeleteTask = new FirebaseDeleteTask(queueItem, MuzikoConstants.FirebaseFileMode.LIBRARY);
            libraryDeleteTask.executeOnExecutor(ThreadManager.Instance().getMuzikoBackgroundThreadPool());
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public void deletePlaylistTrack(QueueItem queueItem, String playlistHash) {
        AsyncJob.doInBackground(() -> {
            FirebaseDeleteTask libraryDeleteTask = new FirebaseDeleteTask(mContext, queueItem, playlistHash, MuzikoConstants.FirebaseFileMode.PLAYLISTS);
            libraryDeleteTask.executeOnExecutor(ThreadManager.Instance().getMuzikoBackgroundThreadPool());
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public void deletePlaylist(PlaylistItem playlistItem) {
        AsyncJob.doInBackground(() -> {
            FirebasePlaylistDeleteTask deleteLocalPlaylistTask = new FirebasePlaylistDeleteTask(mContext, playlistItem);
            deleteLocalPlaylistTask.executeOnExecutor(ThreadManager.Instance().getMuzikoBackgroundThreadPool());
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public void checkforTransfers() {
        AsyncJob.doInBackground(() -> {
            if (isFirebaseStarted()) {
                if (TrackRealmHelper.getTracksWithoutMD5().size() == 0) {
                    checkforLibraryTransfers();
                    checkforFavsTransfers();
//                    checkforPlaylistTransfers();
                } else {
//                    FirebaseCloudEvent firebaseCloudEvent = new FirebaseCloudEvent(mContext.getString(R.string.sync_pending), mContext.getString(R.string.sync_pending_desc));
//                    EventBus.getDefault().post(firebaseCloudEvent);
                    AppController.Instance().startMd5Updater();
                }
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public void checkforLibraryTransfers() {
        AsyncJob.doInBackground(() -> {
            if (isFirebaseStarted()) {
                checkForLibraryDownloads();
            }
        });
    }

    @DebugLog
    public void checkforFavsTransfers() {
        AsyncJob.doInBackground(() -> {
            if (isFirebaseStarted()) {
                checkForFavsDownloads();
            }
        });
    }

//    @DebugLog
//    public void checkforPlaylistTransfers() {
//        AsyncJob.doInBackground(() -> {
//            if (isFirebaseStarted()) {
//                checkForPlaylistDownloads();
//            }
//        });
//    }

    @DebugLog
    private void checkForFavsUploads() {
        AsyncJob.doInBackground(() -> {
            if (isFirebaseStarted() && favsLoaded && SettingsManager.Instance().getPrefSyncFavourites()) {
                ArrayList<QueueItem> queueItems = TrackRealmHelper.getSyncFavorites();
                ArrayList<CloudTrack> cloudTracks = new ArrayList<>();
                cloudTracks.addAll(FirebaseFavsList);
                for (QueueItem queueItem : queueItems) {
                    boolean found = false;
                    if (queueItem.md5 == null) {
                        queueItem.md5 = MD5.calculateMD5(new File(queueItem.data));
                        TrackRealmHelper.updateMD5Hash(queueItem);
                    }
                    for (CloudTrack cloudTrack : cloudTracks) {
                        if (queueItem.md5.equals(cloudTrack.getMd5())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        FirebaseManager.Instance().uploadFav(queueItem);
                    }
                }
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());


    }

    @DebugLog
    private void checkForLibraryUploads() {
        AsyncJob.doInBackground(() -> {
            try {
                if (isFirebaseStarted() && libraryLoaded && SettingsManager.Instance().getPrefAutoSyncLibrary()) {
                    ArrayList<QueueItem> queueItems = TrackRealmHelper.getLocalLibrary();
                    ArrayList<CloudTrack> cloudTracks = new ArrayList<>();
                    cloudTracks.addAll(FirebaseLibraryList);
                    for (QueueItem queueItem : queueItems) {
                        boolean found = false;
                        if (queueItem.md5 == null) {
                            queueItem.md5 = MD5.calculateMD5(new File(queueItem.data));
                            TrackRealmHelper.updateMD5Hash(queueItem);
                        }
                        for (CloudTrack cloudTrack : cloudTracks) {
                            if (queueItem.md5.equals(cloudTrack.getMd5())) {
                                found = true;
                            }
                        }
                        if (!found) {
                            FirebaseManager.Instance().uploadLibrary(queueItem);
                        }
                    }
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());


    }


//    @DebugLog
//    private synchronized void checkForPlaylistUploads() {
//        AsyncJob.doInBackground(() -> {
//            if (isFirebaseStarted() && playlistsLoaded && playlistTracksLoaded && SettingsManager.Instance().getPrefSyncPlaylist()) {
//                ArrayList<PlaylistItem> playlistItems = PlaylistRealmHelper.loadAllWithSync();
//
//                ArrayList<CloudPlaylist> cloudPlaylists = new ArrayList<>();
//                cloudPlaylists.addAll(FirebasePlaylistsList);
//
//                ArrayList<CloudTrack> cloudPlaylistTracks = new ArrayList<>();
//                cloudPlaylistTracks.addAll(FirebasePlaylistTracksList);
//
//                for (PlaylistItem playlistItem : playlistItems) {
//                    if (cloudPlaylists.size() > 0) {
//                        boolean playlistFound = false;
//                        for (CloudPlaylist cloudPlaylist : cloudPlaylists) {
//                            if (cloudPlaylist.getUid().equals(playlistItem.getHash())) {
//                                playlistFound = true;
//                                break;
//                            }
//                        }
//                        if (!playlistFound) {
//                            FirebaseManager.Instance().uploadPlaylist(playlistItem.hash);
//                        }
//                    } else {
//                        FirebaseManager.Instance().uploadPlaylist(playlistItem.hash);
//                    }
//
//                    for (QueueItem queueItem : PlaylistSongRealmHelper.loadAllByPlaylist(0, playlistItem.id)) {
//                        boolean found = false;
//                        if (queueItem.md5 == null) {
//                            queueItem.md5 = MD5.calculateMD5(new File(queueItem.data));
//                            TrackRealmHelper.updateMD5Hash(queueItem);
//                        }
//                        for (CloudTrack cloudTrack : cloudPlaylistTracks) {
//                            if (queueItem.md5.equals(cloudTrack.getMd5())) {
//                                found = true;
//                            }
//                        }
//                        if (!found) {
//                            FirebaseManager.Instance().uploadPlaylistTrack(queueItem, playlistItem.hash);
//                        }
//                    }
//                }
//            }
//        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
//
//
//    }

    @DebugLog
    private void checkForFavsDownloads() {
        AsyncJob.doInBackground(() -> {
            if (isFirebaseStarted() && favsLoaded && SettingsManager.Instance().getPrefSyncFavourites()) {
                ArrayList<CloudTrack> cloudTracks = new ArrayList<>();
                cloudTracks.addAll(FirebaseFavsList);
                for (CloudTrack cloudTrack : cloudTracks) {
                    if (!FileHelper.localFileExists(cloudTrack)) {
                        FirebaseManager.Instance().downloadFav(cloudTrack);
                    } else {
                        QueueItem queueItem = TrackRealmHelper.getTrackByMD5(cloudTrack.getMd5());
                        if (!queueItem.isFavorite()) {
                            TrackRealmHelper.toggleFavorite(queueItem);
                        }
                        if (!queueItem.isSync()) {
                            TrackRealmHelper.toggleSync(queueItem, true);
                        }
                    }
                }
                checkForFavsUploads();
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());


    }

    @DebugLog
    private void checkForLibraryDownloads() {
        AsyncJob.doInBackground(() -> {
            if (isFirebaseStarted() && libraryLoaded && SettingsManager.Instance().getPrefAutoSyncLibrary()) {
                ArrayList<CloudTrack> cloudTracks = new ArrayList<>();
                cloudTracks.addAll(FirebaseLibraryList);
                for (CloudTrack cloudTrack : cloudTracks) {
                    if (!FileHelper.localFileExists(cloudTrack)) {
                        FirebaseManager.Instance().downloadLibrary(cloudTrack);
                    }
                }
                checkForLibraryUploads();
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());


    }

//    @DebugLog
//    private synchronized void checkForPlaylistDownloads() {
//        AsyncJob.doInBackground(() -> {
//            if (isFirebaseStarted() && playlistsLoaded && playlistTracksLoaded && SettingsManager.Instance().getPrefSyncPlaylist()) {
//
//                ArrayList<CloudPlaylist> cloudPlaylists = new ArrayList<>();
//                cloudPlaylists.addAll(FirebasePlaylistsList);
//
//                ArrayList<CloudTrack> cloudPlaylistTracks = new ArrayList<>();
//                cloudPlaylistTracks.addAll(FirebasePlaylistTracksList);
//
//                for (CloudPlaylist cloudPlaylist : cloudPlaylists) {
//                    for (String md5 : cloudPlaylist.getCloudTracks()) {
//
//                        for (CloudTrack cloudTrack : cloudPlaylistTracks) {
//                            if (cloudTrack.getMd5().equals(md5)) {
//                                if (!FileHelper.localFileExists(cloudTrack)) {
//                                    FirebaseManager.Instance().downloadPlaylistTrack(cloudTrack, cloudPlaylist.getUid());
//                                }
//                            }
//                        }
//                    }
//                }
//                checkForPlaylistUploads();
//            }
//        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
//
//    }

    public QueueItem cloudTrackToQueueItem(CloudTrack cloudTrack) {

        QueueItem queueItem = new QueueItem();
        queueItem.data = cloudTrack.getUrl();
        queueItem.id = cloudTrack.getUid().hashCode();
        queueItem.song = cloudTrack.getUid().hashCode();
        queueItem.album = CloudManager.FIREBASE;
        queueItem.artist = CloudManager.FIREBASE;
        queueItem.name = cloudTrack.getTitle();
        queueItem.title = cloudTrack.getTitle();
        queueItem.artist_name = cloudTrack.getArtist();
        queueItem.album_name = cloudTrack.getAlbum();
        queueItem.genre_name = "";
        queueItem.hash = cloudTrack.getMd5();
        queueItem.duration = cloudTrack.getDuration();
        queueItem.composer = "";
        queueItem.date = cloudTrack.getDateModified();
        queueItem.dateModified = cloudTrack.getDateModified();
        queueItem.track = 0;
        queueItem.year = 0;
        queueItem.md5 = cloudTrack.getMd5();
        queueItem.storage = CloudManager.FIREBASE;
        queueItem.url = "";
        queueItem.noCover = false;
        queueItem.coverUpdated = "";
        queueItem.lastPlayed = "";
        queueItem.rating = 0;
        queueItem.size = 0;
        queueItem.folder_path = "";
        queueItem.folder_name = "";
        return queueItem;


    }

    public boolean checkShouldUpload(QueueItem queueItem) {
        if (firebaseCancelledUploads.containsKey(queueItem.data)) {
            long cancelled = firebaseCancelledUploads.get(queueItem.data);
            return cancelled + halfHourMilliseconds <= System.currentTimeMillis();

        } else {
            return true;
        }
    }

    private boolean checkFileIsLocal(QueueItem queueItem) {
        return queueItem.storage == 0 || queueItem.storage == 1 || queueItem.storage == 2;
    }

    private boolean checkPrerequisites() {
        if (isOverLimit()) {
            return false;
        } else {
            return isAuthenticated() && networkState == NetworkInfo.State.CONNECTED && (hasWifi || !hasWifi && !SettingsManager.Instance().getPrefUpdateLibraryOnlyWifi());
        }
    }

    public boolean isAuthenticated() {
        return FirebaseAuth.getInstance().getCurrentUser() != null && !FirebaseAuth.getInstance().getCurrentUser().isAnonymous();
    }

    @Override
    public void onDownloadComplete(QueueItem queueItem) {
        Intent shareintent = new Intent(AppController.INTENT_SHARE_DOWNLOADED);
        shareintent.putExtra("data", queueItem.data);
        mContext.sendBroadcast(shareintent);
    }

    @Override
    public void onError() {
    }
}
