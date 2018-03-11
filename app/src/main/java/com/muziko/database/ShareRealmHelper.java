package com.muziko.database;

import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.Share;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by dev on 27/08/2016.
 */
public class ShareRealmHelper {

	public static void saveReceivedShare(QueueItem queueItem, String devicename) {

		Realm myRealm = Realm.getDefaultInstance();

		if (!myRealm.isInTransaction()) {
			myRealm.beginTransaction();
		}
		final String uuid = UUID.randomUUID().toString();
		ShareRealm shareRealm = new ShareRealm();
		shareRealm.setUid(uuid);
		shareRealm.setTitle(queueItem.title);
		shareRealm.setArtist(queueItem.artist_name);
		shareRealm.setAlbum(queueItem.album_name);
		shareRealm.setSenderId(devicename);
		shareRealm.setTimestamp(System.currentTimeMillis());
		shareRealm.setType(1);
		myRealm.insertOrUpdate(shareRealm);

		myRealm.commitTransaction();
		myRealm.close();
	}

	public static void saveSendingShare(QueueItem queueItem, String devicename) {

		Realm myRealm = Realm.getDefaultInstance();

		if (!myRealm.isInTransaction()) {
			myRealm.beginTransaction();
		}
		final String uuid = UUID.randomUUID().toString();
		ShareRealm shareRealm = new ShareRealm();
		shareRealm.setUid(uuid);
		shareRealm.setTitle(queueItem.title);
		shareRealm.setArtist(queueItem.artist_name);
		shareRealm.setAlbum(queueItem.album_name);
		shareRealm.setReceiverId(devicename);
		shareRealm.setTimestamp(System.currentTimeMillis());
		shareRealm.setType(1);
		myRealm.insertOrUpdate(shareRealm);

		myRealm.commitTransaction();
		myRealm.close();
	}


	public static ArrayList<Share> getSentShares() {

		ArrayList<Share> shares = new ArrayList<>();

		Realm myRealm = Realm.getDefaultInstance();

		RealmResults<ShareRealm> realmResults = myRealm.where(ShareRealm.class).findAll();

		for (int i = 0; i < realmResults.size(); i++) {

			if (realmResults.get(i).getSenderId() == null) {
				Share share = new Share();
				share.setUid(realmResults.get(i).getUid());
				share.setReceiverId(realmResults.get(i).getReceiverId());
				share.setTitle(realmResults.get(i).getTitle());
				share.setArtist(realmResults.get(i).getArtist());
				share.setAlbum(realmResults.get(i).getAlbum());
				share.setTimestamp(realmResults.get(i).getTimestamp());
				share.setType(realmResults.get(i).getType());
				shares.add(share);
			}
		}


		myRealm.close();
		return shares;
	}

	public static ArrayList<Share> getReceivedShares() {

		ArrayList<Share> shares = new ArrayList<>();

		Realm myRealm = Realm.getDefaultInstance();

		RealmResults<ShareRealm> realmResults = myRealm.where(ShareRealm.class).findAll();

		for (int i = 0; i < realmResults.size(); i++) {
			if (realmResults.get(i).getReceiverId() == null) {
				Share share = new Share();
				share.setUid(realmResults.get(i).getUid());
				share.setSenderId(realmResults.get(i).getSenderId());
				share.setTitle(realmResults.get(i).getTitle());
				share.setArtist(realmResults.get(i).getArtist());
				share.setAlbum(realmResults.get(i).getAlbum());
				share.setTimestamp(realmResults.get(i).getTimestamp());
				share.setType(realmResults.get(i).getType());
				shares.add(share);
			}
		}


		myRealm.close();
		return shares;
	}
}
