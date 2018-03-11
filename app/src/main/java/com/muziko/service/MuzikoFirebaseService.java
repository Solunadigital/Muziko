package com.muziko.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.Iterators;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.reflect.TypeToken;
import com.muziko.MyApplication;
import com.muziko.activities.HistoryActivity;
import com.muziko.common.events.FirebaseRefreshEvent;
import com.muziko.common.events.FirebaseShareCountEvent;
import com.muziko.common.events.FirebaseSharesRefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.common.models.firebase.Contact;
import com.muziko.common.models.firebase.Person;
import com.muziko.common.models.firebase.Share;
import com.muziko.common.models.firebase.TrackFingerprint;
import com.muziko.database.SubscriptionTypeRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.FileHelper;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.ContactManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.GsonManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.NotificationController;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.ThreadManager;
import com.muziko.models.MuzikoSubscriptionType;
import com.oasisfeng.condom.CondomContext;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import hugo.weaving.DebugLog;
import pl.tajchert.buswear.EventBus;

import static br.com.zbra.androidlinq.Linq.stream;
import static com.muziko.MyApplication.firebaseContactList;
import static com.muziko.MyApplication.fullUerList;
import static com.muziko.MyApplication.phoneContactList;
import static com.muziko.MyApplication.shareList;
import static com.muziko.MyApplication.userList;
import static com.muziko.service.SongService.ARG_DATA;

/**
 * Created by dev on 7/11/2016.
 */
public class MuzikoFirebaseService extends Service implements ContactManager.ContactHelperListener {
    private static final String TAG = MuzikoFirebaseService.class.getSimpleName();
    public static boolean sharesReady;
    private DatabaseReference personRef;
    private ValueEventListener personListener;
    private DatabaseReference contactRef;
    private ValueEventListener contactListener;
    private DatabaseReference shareRef;
    private ValueEventListener shareListener;
    private DatabaseReference libraryRef;
    private DatabaseReference libraryOneTimeRef;
    private ValueEventListener libraryListener;
    private DatabaseReference favsRef;
    private ValueEventListener favsListener;
    private DatabaseReference playlistsRef;
    private ValueEventListener playlistsListener;
    private DatabaseReference playlistTracksRef;
    private ValueEventListener playlistTracksListener;
    private DatabaseReference acrRef;
    private ValueEventListener acrListener;
    private DatabaseReference subscriptionTypeRef;
    private ValueEventListener subscriptionTypeListener;
    private DatabaseReference subscriptionRef;
    private ValueEventListener subscriptionListener;
    private DatabaseReference userConnectedRef;
    private int currentShareCount = 0;
    private android.app.NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private boolean gotContacts;
    private boolean gotPeople;
    private final Runnable filterUserRunnable =
            () -> {
                try {
                    userList.clear();
                    // set blocked or allowed
                    ArrayList<Contact> contactList = new ArrayList<>();
                    contactList.addAll(firebaseContactList);

                    ArrayList<Person> fullList = new ArrayList<>();
                    fullList.addAll(fullUerList.values());

                    ArrayList<Person> phoneList = new ArrayList<>();
                    phoneList.addAll(phoneContactList);

                    for (Contact contact : contactList) {
                        Person person = getPerson(fullList, contact.getUid());
                        if (person != null) {
                            if (!contact.isBlocked()) {
                                person.setBlocked(false);
                                userList.put(person.getUid(), person);
                            } else {
                                person.setBlocked(true);
                                userList.put(person.getUid(), person);
                            }
                        }
                    }

                    // Match with phone contacts
                    if (phoneList.size() > 0) {
                        Person currentPerson = null;
                        for (Person person : fullList) {
                            currentPerson = person;
                            Person matchedPerson = matchPerson(phoneList, person.getEmail(), person.getPhone());
                            if (matchedPerson != null) {
                                currentPerson.setFriend(true);
                                userList.put(currentPerson.getUid(), currentPerson);
                            }
                        }
                    }

                    if (!sharesReady && gotPeople && gotContacts && PrefsManager.Instance().getLastContactSync() > 0) {
                        sharesReady = true;
                        shareRef.addValueEventListener(shareListener);
                    }

                    EventBus.getDefault(this).postLocal(new FirebaseSharesRefreshEvent(0));
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            };
    private String data;

    private static Person getPerson(ArrayList<Person> persons, String uid) {
        for (Person person : persons) {
            if (person.getUid().equals(uid)) {
                return person;
            }
        }
        return null;
    }

    private static Person matchPerson(ArrayList<Person> persons, String email, String phone) {
        Person matched = null;
        for (Person person : persons) {
            if (!phone.isEmpty()) {
                if (person.getPhone().equals(phone)) {
                    matched = person;
                }
            }
            if (!email.isEmpty()) {
                if (person.getEmail().equals(email)) {
                    matched = person;
                }
            }
        }
        return matched;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ContactManager.Instance().init(CondomContext.wrap(this, "Muziko"));
        ContactManager.Instance().addListener(this);

        if (PrefsManager.Instance().getLastContactSync() + MuzikoConstants.dayMilliseconds
                < System.currentTimeMillis()
                || phoneContactList.size() == 0) {
            ContactManager.Instance().getContacts();
            PrefsManager.Instance().setLastContactSync(System.currentTimeMillis());
        }

        watchFirebase();

        listenToSubscriptions();
        connectionHeartbeat();
    }

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {

                switch (action) {
                    case AppController.ACTION_UPDATE_FIREBASE:
                        ThreadManager.Instance().submitToBackgroundThreadPool(filterUserRunnable);
                        break;

                    case AppController.ACTION_DOWNLOAD:
                        data = intent.getStringExtra(AppController.ARG_ITEM);
                        Share share = GsonManager.Instance().getGson().fromJson(data, Share.class);
                        FirebaseManager.Instance().downloadShare(share);

                        break;
                    case AppController.ACTION_UPLOAD:
                        QueueItem queueItem = (QueueItem) intent.getSerializableExtra(AppController.ARG_ITEM);
                        String people = intent.getStringExtra(AppController.ARG_PEOPLE);
                        ArrayList<Person> personList =
                                GsonManager.Instance()
                                        .getGson()
                                        .fromJson(people, new TypeToken<List<Person>>() {
                                        }.getType());

                        FirebaseManager.Instance().uploadShare(queueItem, personList);

                        break;

                    case AppController.ACTION_REFRESH_LIBRARY:
                        getFirebaseLibraryUpdate();

                        break;
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        if (personRef != null && personListener != null) {
            personRef.removeEventListener(personListener);
        }

        if (contactRef != null && contactListener != null) {
            contactRef.removeEventListener(contactListener);
        }

        if (shareRef != null && shareListener != null) {
            shareRef.removeEventListener(shareListener);
        }

        if (libraryRef != null && libraryListener != null) {
            libraryRef.removeEventListener(libraryListener);
        }

        if (favsRef != null && favsListener != null) {
            favsRef.removeEventListener(favsListener);
        }

        if (playlistsRef != null && playlistsListener != null) {
            playlistsRef.removeEventListener(playlistsListener);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void addBaseSubscriptionTypes() {
        MuzikoSubscriptionType subscriptionType1 = new MuzikoSubscriptionType();
        subscriptionType1.setSubscriptionTypeID("storage_subscription_level_1");
        subscriptionType1.setSubscriptionName("Storage Subscription Level 1");
        subscriptionType1.setSongLimit(200);
        subscriptionType1.setCreated(System.currentTimeMillis());

        MuzikoSubscriptionType subscriptionType2 = new MuzikoSubscriptionType();
        subscriptionType2.setSubscriptionTypeID("storage_subscription_level_2");
        subscriptionType2.setSubscriptionName("Storage Subscription Level 2");
        subscriptionType2.setSongLimit(300);
        subscriptionType2.setCreated(System.currentTimeMillis());

        MuzikoSubscriptionType subscriptionType3 = new MuzikoSubscriptionType();
        subscriptionType3.setSubscriptionTypeID("storage_subscription_level_3");
        subscriptionType3.setSubscriptionName("Storage Subscription Level 3");
        subscriptionType3.setSongLimit(400);
        subscriptionType3.setCreated(System.currentTimeMillis());

        MuzikoSubscriptionType subscriptionType4 = new MuzikoSubscriptionType();
        subscriptionType4.setSubscriptionTypeID("storage_subscription_level_unlimited");
        subscriptionType4.setSubscriptionName("Storage Subscription Level Unlimited");
        subscriptionType4.setSongLimit(1000000);
        subscriptionType4.setCreated(System.currentTimeMillis());

        subscriptionTypeRef = FirebaseManager.Instance().getSubscriptionTypesRef();
        subscriptionTypeRef.child(subscriptionType1.getSubscriptionTypeID()).setValue(subscriptionType1, (error, firebase) -> {
            if (error != null) {
                Log.i(TAG, "Subscription Type upload error");
            }
        });

        subscriptionTypeRef.child(subscriptionType2.getSubscriptionTypeID()).setValue(subscriptionType2, (error, firebase) -> {
            if (error != null) {
                Log.i(TAG, "Subscription Type upload error");
            }
        });

        subscriptionTypeRef.child(subscriptionType3.getSubscriptionTypeID()).setValue(subscriptionType3, (error, firebase) -> {
            if (error != null) {
                Log.i(TAG, "Subscription Type upload error");
            }
        });

        subscriptionTypeRef.child(subscriptionType4.getSubscriptionTypeID()).setValue(subscriptionType4, (error, firebase) -> {
            if (error != null) {
                Log.i(TAG, "Subscription Type upload error");
            }
        });
    }

    private void watchFirebase() {
        contactRef = FirebaseManager.Instance().getContactsRef();

        contactListener =
                new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        AsyncJob.doInBackground(() -> {
                            firebaseContactList.clear();


                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Contact contact = postSnapshot.getValue(Contact.class);
                                firebaseContactList.add(contact);
                            }

                            gotContacts = true;

                            ThreadManager.Instance().submitToBackgroundThreadPool(filterUserRunnable);
                        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Crashlytics.logException(databaseError.toException());
                    }
                };

        contactRef.addValueEventListener(contactListener);

        personRef = FirebaseManager.Instance().getPeopleRef();

        personListener =
                new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        AsyncJob.doInBackground(() -> {

                            fullUerList.clear();
                            int a= (int)dataSnapshot.getChildrenCount();
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Person person = postSnapshot.getValue(Person.class);

                                if (person != null) {
                                    if (!person.getEmail().isEmpty()) {
                                        if (!person.getUid().equals(FirebaseManager.Instance().getCurrentUserId())) {
                                            fullUerList.put(person.getUid(), person);
                                        } else {
                                            PrefsManager.Instance().setProfileUrl(person.getPhotoUrl());
                                            FirebaseManager.Instance().setFirebaseMe(person);
                                        }
                                    }
                                }
                               /* else
                                    {
                                    try{
                                        person=new Person();
                                        try {
                                             person.setPhotoUrl((String) postSnapshot.child("photoUrl").getValue());
                                        }catch (Exception e){
                                            person.setPhotoUrl("");
                                        }
                                        try {
                                             person.setUid((String) postSnapshot.child("uid").getValue());
                                        }catch (Exception e){
                                            person.setUid("");
                                        }
                                        try {
                                             person.setDisplayName((String) postSnapshot.child("displayName").getValue());
                                        }catch (Exception e){
                                            person.setDisplayName("");
                                        }
                                        try {
                                             person.setEmail((String) postSnapshot.child("email").getValue());
                                        }catch (Exception e){
                                            person.setEmail("");
                                        }
                                        try {
                                             person.setPhone((String) postSnapshot.child("phone").getValue());
                                        }catch (Exception e){
                                            person.setPhone("");
                                        }
                                        if (!person.getEmail().isEmpty()) {
                                            if (!person.getUid().equals(FirebaseManager.Instance().getCurrentUserId())) {
                                                fullUerList.put(person.getUid(), person);
                                            } else {
                                                PrefsManager.Instance().setProfileUrl(person.getPhotoUrl());
                                                FirebaseManager.Instance().setFirebaseMe(person);
                                            }
                                        }
                                    }catch (Exception e){

                                    }
                                }*/
                            }

                            gotPeople = true;

                            ThreadManager.Instance().submitToBackgroundThreadPool(filterUserRunnable);
                        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Crashlytics.logException(databaseError.toException());
                    }
                };

        personRef.addValueEventListener(personListener);

        shareRef =
                FirebaseManager.Instance()
                        .getShareRef()
                        .child(FirebaseManager.Instance().getCurrentUserId());

        shareListener =
                new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int count = 0;
                        shareList.clear();

                        List<String> blockedContacts = new ArrayList<>();
                        blockedContacts.addAll(
                                stream(MyApplication.userList.values())
                                        .where(c -> c.isBlocked())
                                        .select(c -> c.getUid())
                                        .toList());

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Share share = postSnapshot.getValue(Share.class);

                            if (share != null) {

                                // remove received files that are expired

                                if (!share.getSenderId().equals(FirebaseManager.Instance().getCurrentUserId())
                                        && share.getLocalfile() == null
                                        && (Long) share.getTimestamp() + MuzikoConstants.weekMilliseconds
                                        < System.currentTimeMillis()) {
                                    shareRef.child(share.getUid()).removeValue();
                                    FirebaseStorage storageRef = FirebaseStorage.getInstance();
                                    final StorageReference fileref =
                                            storageRef.getReferenceFromUrl(share.getShareUrl());
                                    fileref
                                            .delete()
                                            .addOnSuccessListener((OnSuccessListener) o -> {
                                            })
                                            .addOnFailureListener(
                                                    exception -> {
                                                        Crashlytics.logException(exception);
                                                    });

                                    continue;
                                }

                                if (!share.getSenderId().equals(FirebaseManager.Instance().getCurrentUserId())
                                        && share.getLocalfile() == null
                                        && !blockedContacts.contains(share.getSenderId())) {
                                    count++;
                                }

                                if (share.getSenderId().equals(FirebaseManager.Instance().getCurrentUserId())) {
                                    shareList.add(share);
                                } else {
                                    // check against user list
                                    ArrayList<Person> personList = new ArrayList<>();
                                    personList.addAll(userList.values());

                                    Person person = getPerson(personList, share.getSenderId());
                                    if (person != null) {
                                        if (!person.isBlocked()) {
                                            //									if (person.isFriend()) {
                                            //										share.setFriend(true);
                                            //									} else {
                                            //										share.setFriend(false);
                                            //									}
                                            share.setFriend(true);
                                            shareList.add(share);

                                            if (!share.isNotified()) {
                                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                                r.play();

                                                int min = 0;
                                                int max = 1000;

                                                Random random = new Random();
                                                int nID = random.nextInt(max - min + 1) + min;

                                                String name = person.getDisplayName();
                                                SpannableString boldName = new SpannableString(name);
                                                boldName.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, name.length(), 0);

                                                String title = share.getTitle();
                                                SpannableString boldTitle = new SpannableString(title);
                                                boldTitle.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(), 0);

                                                mNotifyManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                                mBuilder = new NotificationCompat.Builder(MuzikoFirebaseService.this, "Muziko");
                                                mBuilder.setContentTitle(boldName + " sent you a song")
                                                        .setContentText(boldTitle + " - " + share.getArtist())
                                                        .setAutoCancel(true)
                                                        .setSmallIcon(
                                                                NotificationController.Instance().getSharedNotificationIcon());
                                                Intent myIntent = new Intent(getApplicationContext(), HistoryActivity.class);
                                                myIntent.putExtra(ARG_DATA, true);
                                                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                                mBuilder.setContentIntent(pendingIntent);

                                                int pixels = Utils.convertDpToPixel(getApplicationContext(), 192);

                                                Picasso.with(MuzikoFirebaseService.this)
                                                        .load(person.getPhotoUrl())
                                                        .resize(pixels, pixels)
                                                        .into(
                                                                new Target() {
                                                                    @Override
                                                                    public void onBitmapLoaded(
                                                                            Bitmap bitmap, Picasso.LoadedFrom from) {
                                                                        // not being called the first time
                                                                        mBuilder.setLargeIcon(bitmap);
                                                                        mNotifyManager.notify(nID, mBuilder.build());
                                                                    }

                                                                    @Override
                                                                    public void onBitmapFailed(Drawable errorDrawable) {
                                                                        mNotifyManager.notify(nID, mBuilder.build());
                                                                    }

                                                                    @Override
                                                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                                                    }
                                                                });

                                                // Update download count for sender share
                                                Map<String, Object> senderShareValues = new HashMap<>();
                                                senderShareValues.put("notified", true);

                                                shareRef.child(share.getUid()).updateChildren(senderShareValues,
                                                        (firebaseError, databaseReference) -> {
                                                            if (firebaseError != null) {
                                                                Crashlytics.logException(firebaseError.toException());
                                                            }
                                                        });
                                            }
                                        }
                                    } else {
                                        share.setFriend(false);
                                        shareList.add(share);
                                    }
                                }
                            }
                        }

                        currentShareCount = count;
                        EventBus.getDefault(MuzikoFirebaseService.this).postLocal(new FirebaseShareCountEvent(count));
                        EventBus.getDefault(MuzikoFirebaseService.this).postLocal(new FirebaseRefreshEvent(1000));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Crashlytics.logException(databaseError.toException());
                    }
                };

        libraryRef = FirebaseManager.Instance().getLibraryRef().child(FirebaseManager.Instance().getCurrentUserId());

        libraryListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseManager.Instance().setLibraryLoaded(false);
                FirebaseManager.Instance().getFirebaseLibraryList().clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        CloudTrack cloudTrack = postSnapshot.getValue(CloudTrack.class);
                        if (cloudTrack != null) {
                            if (!cloudTrack.isDeleted()) {
                                FirebaseManager.Instance().getFirebaseLibraryList().add(cloudTrack);
                                if (!FileHelper.localorFirebaseFileExists(cloudTrack)) {
                                    QueueItem queueItem = FirebaseManager.Instance().cloudTrackToQueueItem(cloudTrack);
                                    TrackRealmHelper.insertTrack(queueItem);
                                    TrackRealmHelper.toggleLibrary(queueItem, true);
                                } else {
                                    QueueItem queueItem = TrackRealmHelper.getTrackByMD5(cloudTrack.getMd5());
                                    boolean shouldSkip = false;
                                    for (QueueItem item : FirebaseManager.Instance().getFirebaseRemovedList()) {
                                        if (item.data.equalsIgnoreCase(queueItem.data)) {
                                            shouldSkip = true;
                                            break;
                                        }
                                    }
                                    if (shouldSkip) {
                                        FirebaseManager.Instance().deleteLibrary(queueItem);
                                        FirebaseManager.Instance().getFirebaseRemovedList().remove(queueItem);
                                    } else {
                                        TrackRealmHelper.toggleLibrary(queueItem, true);
                                    }
                                }
                            } else {
                                QueueItem queueItem = TrackRealmHelper.getTrackByMD5(cloudTrack.getMd5());
                                if (queueItem != null) {
                                    if (queueItem.storage == CloudManager.FIREBASE) {
                                        TrackRealmHelper.deleteTrack(queueItem.data);
                                    } else if (queueItem.isLibrary()) {
                                        TrackRealmHelper.toggleLibrary(queueItem, false);
                                    }
                                }
                            }
                        }
                    }
                    EventBus.getDefault(MuzikoFirebaseService.this).postStickyLocal(new FirebaseRefreshEvent(1000));
                }

                FirebaseManager.Instance().setLibraryLoaded(true);
                FirebaseManager.Instance().isOverLimit();
                FirebaseManager.Instance().checkforTransfers();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Crashlytics.logException(databaseError.toException());
            }
        };

        libraryRef.addValueEventListener(libraryListener);

        favsRef = FirebaseManager.Instance().getFavRef().child(FirebaseManager.Instance().getCurrentUserId());

        favsListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseManager.Instance().setFavsLoaded(false);
                FirebaseManager.Instance().getFirebaseFavsList().clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        CloudTrack cloudTrack = postSnapshot.getValue(CloudTrack.class);
                        if (cloudTrack != null) {
                            if (!cloudTrack.isDeleted()) {
                                FirebaseManager.Instance().getFirebaseFavsList().add(cloudTrack);
                                if (!FileHelper.localorFirebaseFileExists(cloudTrack)) {
                                    QueueItem queueItem = FirebaseManager.Instance().cloudTrackToQueueItem(cloudTrack);
                                    TrackRealmHelper.insertTrack(queueItem);
                                    TrackRealmHelper.toggleFavorite(queueItem);
                                } else {
                                    QueueItem queueItem = TrackRealmHelper.getTrackByMD5(cloudTrack.getMd5());
                                    if (queueItem != null && !queueItem.isFavorite()) {
                                        TrackRealmHelper.toggleFavorite(queueItem);
                                        TrackRealmHelper.toggleSync(queueItem, true);
                                    }

                                }
                            } else {
                                QueueItem queueItem = TrackRealmHelper.getTrackByMD5(cloudTrack.getMd5());
                                if (queueItem != null) {
                                    if (queueItem.storage == CloudManager.FIREBASE) {
                                        TrackRealmHelper.deleteTrack(queueItem.data);
                                    } else if (queueItem.isFavorite()) {
                                        TrackRealmHelper.toggleSync(queueItem, false);
                                    }
                                }
                            }
                        }
                    }

                    EventBus.getDefault(MuzikoFirebaseService.this).postStickyLocal(new FirebaseRefreshEvent(1000));
                }

                FirebaseManager.Instance().setFavsLoaded(true);
                FirebaseManager.Instance().isOverLimit();
                FirebaseManager.Instance().checkforTransfers();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Crashlytics.logException(databaseError.toException());
            }
        };

        favsRef.addValueEventListener(favsListener);

//        playlistsRef = FirebaseManager.Instance().getPlaylistsRef().child(FirebaseManager.Instance().getCurrentUserId());
//
//        playlistsListener = new ValueEventListener() {
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                FirebaseManager.Instance().setPlaylistsLoaded(false);
//                FirebaseManager.Instance().getFirebasePlaylistsList().clear();
//                if (dataSnapshot.hasChildren()) {
//                    long childCount = dataSnapshot.getChildrenCount();
//                    Log.i(TAG, "Cloud playlist count " + childCount);
//                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                        CloudPlaylist cloudPlaylist = postSnapshot.getValue(CloudPlaylist.class);
//                        if (!cloudPlaylist.isDeleted()) {
//                            FirebaseManager.Instance().getFirebasePlaylistsList().add(cloudPlaylist);
//                            PlaylistItem playlistItem = PlaylistRealmHelper.getPlaylist(cloudPlaylist.getPlaylistid());
//                            if (playlistItem == null) {
//                                playlistItem = new PlaylistItem();
//                                playlistItem.id = cloudPlaylist.getPlaylistid();
//                                playlistItem.title = cloudPlaylist.getTitle();
//                                playlistItem.hash = cloudPlaylist.getUid();
//                                if (cloudPlaylist.getCloudTracks().size() > 0) {
//                                    QueueItem queueItem = TrackRealmHelper.getTrackByMD5(cloudPlaylist.getCloudTracks().get(0));
//                                    {
//                                        if (queueItem != null) {
//                                            playlistItem.data = queueItem.data;
//                                        }
//                                    }
//                                }
//                                playlistItem.songs = cloudPlaylist.getCloudTracks().size();
//                                playlistItem.duration = cloudPlaylist.getDuration();
//                                playlistItem.date = cloudPlaylist.getDateModified();
//                                playlistItem.storage = CloudManager.FIREBASE;
//                                PlaylistRealmHelper.insert(playlistItem);
//                                PlaylistRealmHelper.toggleSync(playlistItem.id, true);
//                            }
//                        } else {
//                            PlaylistItem playlistItem = PlaylistRealmHelper.getPlaylist(cloudPlaylist.getPlaylistid());
//                            if (playlistItem != null) {
//                                if (playlistItem.storage == CloudManager.FIREBASE) {
//                                    PlaylistRealmHelper.delete(playlistItem.id);
//                                } else if (playlistItem != null) {
//                                    PlaylistRealmHelper.toggleSync(playlistItem.id, false);
//                                }
//                            }
//                        }
//                    }
//
//                    EventBus.getDefault(MuzikoFirebaseService.this).postStickyLocal(new FirebaseRefreshEvent(1000));
//                }
//                FirebaseManager.Instance().setPlaylistsLoaded(true);
//                FirebaseManager.Instance().isOverLimit();
//                FirebaseManager.Instance().checkforTransfers();
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Crashlytics.logException(databaseError.toException());
//            }
//        };
//
//        playlistsRef.addValueEventListener(playlistsListener);
//
//        playlistTracksRef = FirebaseManager.Instance().getPlaylistsTracksRef().child(FirebaseManager.Instance().getCurrentUserId());
//
//        playlistTracksListener = new ValueEventListener() {
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                FirebaseManager.Instance().setPlaylistTracksLoaded(false);
//                FirebaseManager.Instance().getFirebasePlaylistTracksList().clear();
//                if (dataSnapshot.hasChildren()) {
//                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                        CloudTrack cloudTrack = postSnapshot.getValue(CloudTrack.class);
//
//                        if (cloudTrack != null) {
//                            if (!cloudTrack.isDeleted()) {
//                                FirebaseManager.Instance().getFirebasePlaylistTracksList().add(cloudTrack);
//                                if (!FileHelper.localorFirebaseFileExists(cloudTrack)) {
//                                    QueueItem queueItem = FirebaseManager.Instance().cloudTrackToQueueItem(cloudTrack);
//                                    TrackRealmHelper.insertTrack(queueItem);
//                                }
//                            } else {
//                                QueueItem queueItem = TrackRealmHelper.getTrackByMD5(cloudTrack.getMd5());
//                                if (queueItem != null) {
//                                    if (queueItem.storage == CloudManager.FIREBASE) {
//                                        TrackRealmHelper.deleteTrack(queueItem.data);
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    for (CloudPlaylist cloudPlaylist : FirebaseManager.Instance().getFirebasePlaylistsList()) {
//                        PlaylistSongRealmHelper.deleteByPlaylist(cloudPlaylist.getPlaylistid());
//                        for (String md5 : cloudPlaylist.getCloudTracks()) {
//                            QueueItem queue = TrackRealmHelper.getTrackByMD5(md5);
//                            if (queue != null) {
//                                PlaylistQueueItem item = new PlaylistQueueItem(queue);
//                                item.playlist = cloudPlaylist.getPlaylistid();
//                                PlaylistSongRealmHelper.insert(item, false);
//                            }
//                        }
//                    }
//
//                    EventBus.getDefault(MuzikoFirebaseService.this).postStickyLocal(new FirebaseRefreshEvent(1000));
//                }
//
//                FirebaseManager.Instance().setPlaylistTracksLoaded(true);
//                FirebaseManager.Instance().isOverLimit();
//                FirebaseManager.Instance().checkforTransfers();
//
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Crashlytics.logException(databaseError.toException());
//            }
//        };
//
//        playlistTracksRef.addValueEventListener(playlistTracksListener);

        acrRef = FirebaseManager.Instance().getTracksRef().child(AppController.Instance().getAndroidID());

        acrListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                AsyncJob.doInBackground(() -> {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        TrackFingerprint trackFingerprint =
                                postSnapshot.getValue(TrackFingerprint.class);
                        TrackRealmHelper.updateACRKey(trackFingerprint);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Crashlytics.logException(databaseError.toException());
            }
        };

        if (PrefsManager.Instance().getLoginCount() <= 1) {
            acrRef.addListenerForSingleValueEvent(acrListener);
        }

    }

    private void listenToSubscriptions() {
        subscriptionTypeRef = FirebaseManager.Instance().getSubscriptionTypesRef();
        subscriptionTypeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AsyncJob.doInBackground(() -> {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        MuzikoSubscriptionType subscriptionType = postSnapshot.getValue(MuzikoSubscriptionType.class);
                        SubscriptionTypeRealmHelper.insert(subscriptionType);
                    }
                    FirebaseManager.Instance().isOverLimit();
                    FirebaseManager.Instance().setSubscriptionsReady(true);
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Crashlytics.logException(databaseError.toException());
            }
        };

        subscriptionTypeRef.addValueEventListener(subscriptionTypeListener);

        subscriptionRef = FirebaseManager.Instance().getSubscriptionsRef();
        subscriptionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AsyncJob.doInBackground(() -> {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        MuzikoSubscriptionType subscriptionType = postSnapshot.getValue(MuzikoSubscriptionType.class);
                        FirebaseManager.Instance().getSubscriptionTypes().add(subscriptionType);
                    }
                    FirebaseManager.Instance().isOverLimit();
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Crashlytics.logException(databaseError.toException());
            }
        };

        subscriptionRef.addValueEventListener(subscriptionListener);
    }


    private void getFirebaseLibraryUpdate() {
        if (libraryOneTimeRef != null) {
            libraryOneTimeRef.addListenerForSingleValueEvent(libraryListener);
        }
    }

    @DebugLog
    private void connectionHeartbeat() {

        DatabaseReference connectionsRef = FirebaseManager.Instance().getConnectionsRef();
        userConnectedRef = FirebaseManager.Instance().getPeopleRef().child(FirebaseManager.Instance().getCurrentUserId());

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    // add this device to my connections list
                    // this value could contain info about the device or a timestamp too

                    Map<String, Object> connectedValues = new HashMap<>();
                    connectedValues.put("connected", true);
                    connectedValues.put("lastOnline", ServerValue.TIMESTAMP);

                    userConnectedRef.updateChildren(connectedValues, (firebaseError, databaseReference) -> {
                        if (firebaseError != null) {
                            Crashlytics.logException(firebaseError.toException());
                        }
                    });

                    Map<String, Object> disconnectedValues = new HashMap<>();
                    disconnectedValues.put("connected", false);
                    connectedValues.put("lastOnline", ServerValue.TIMESTAMP);

                    // when this device disconnects, remove it
                    userConnectedRef.onDisconnect().updateChildren(disconnectedValues, (firebaseError, databaseReference) -> {
                        if (firebaseError != null) {
                            Crashlytics.logException(firebaseError.toException());
                        }
                    });
                }

                EventBus.getDefault(MuzikoFirebaseService.this).postLocal(new FirebaseRefreshEvent(0));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Crashlytics.logException(databaseError.toException());
            }
        };

        connectionsRef.addValueEventListener(userListener);
    }

    @DebugLog
    @Override
    public void onContactsLoaded() {

        ThreadManager.Instance().submitToBackgroundThreadPool(filterUserRunnable);
    }
}
