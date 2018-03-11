package com.muziko.database;

import com.muziko.models.CloudAccount;
import com.muziko.models.StorageFolder;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by dev on 31/08/2016.
 */
public class StorageFolderRealmHelper {

    public static void toggleHomeFolder(int cloudProvider, String path, String folder_path, String title, boolean isHome) {
        Realm myRealm = Realm.getDefaultInstance();
        int folderId = gethashCode(path, cloudProvider);

        StorageFolderRealm storageFolderRealm = myRealm.where(StorageFolderRealm.class)
                .equalTo("uid", folderId).findFirst();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        if (storageFolderRealm != null) {
            storageFolderRealm.setPath(path);
            storageFolderRealm.setFolder_path(folder_path);
            storageFolderRealm.setHomeFolder(isHome);
            storageFolderRealm.setExcludedFolder(storageFolderRealm.isExcludedFolder());
            storageFolderRealm.setHiddenFolder(storageFolderRealm.isHiddenFolder());
        } else {
            storageFolderRealm = new StorageFolderRealm();
            storageFolderRealm.setUid(folderId);
            storageFolderRealm.setPath(path);
            storageFolderRealm.setFolder_path(folder_path);
            storageFolderRealm.setTitle(title);
            storageFolderRealm.setCloudAccountId(cloudProvider);
            storageFolderRealm.setHomeFolder(isHome);
        }

        myRealm.insertOrUpdate(storageFolderRealm);
        myRealm.commitTransaction();

        myRealm.close();
    }

    public static void toggleExcludedFolder(int cloudProvider, String path, String folder_path, String title, boolean isExcluded) {
        Realm myRealm = Realm.getDefaultInstance();
        int folderId = gethashCode(path, cloudProvider);

        StorageFolderRealm storageFolderRealm = myRealm.where(StorageFolderRealm.class)
                .equalTo("uid", folderId).findFirst();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        if (storageFolderRealm != null) {
            storageFolderRealm.setHomeFolder(storageFolderRealm.isHomeFolder());
            storageFolderRealm.setExcludedFolder(isExcluded);
            storageFolderRealm.setHiddenFolder(storageFolderRealm.isHiddenFolder());
        } else {
            storageFolderRealm = new StorageFolderRealm();
            storageFolderRealm.setUid(folderId);
            storageFolderRealm.setPath(path);
            storageFolderRealm.setFolder_path(folder_path);
            storageFolderRealm.setTitle(title);
            storageFolderRealm.setCloudAccountId(cloudProvider);
            storageFolderRealm.setExcludedFolder(isExcluded);
        }

        myRealm.insertOrUpdate(storageFolderRealm);
        myRealm.commitTransaction();

        myRealm.close();
    }


    public static void toggleHiddenFolder(int cloudProvider, String path, String folder_path, String title, boolean isHidden) {
        Realm myRealm = Realm.getDefaultInstance();
        int folderId = gethashCode(path, cloudProvider);

        StorageFolderRealm storageFolderRealm = myRealm.where(StorageFolderRealm.class)
                .equalTo("uid", folderId).findFirst();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        if (storageFolderRealm != null) {
            storageFolderRealm.setHomeFolder(storageFolderRealm.isHomeFolder());
            storageFolderRealm.setExcludedFolder(storageFolderRealm.isExcludedFolder());
            storageFolderRealm.setHiddenFolder(isHidden);
        } else {
            storageFolderRealm = new StorageFolderRealm();
            storageFolderRealm.setUid(folderId);
            storageFolderRealm.setPath(path);
            storageFolderRealm.setFolder_path(folder_path);
            storageFolderRealm.setTitle(title);
            storageFolderRealm.setCloudAccountId(cloudProvider);
            storageFolderRealm.setHiddenFolder(isHidden);
        }

        myRealm.insertOrUpdate(storageFolderRealm);
        myRealm.commitTransaction();

        myRealm.close();
    }


    private static int gethashCode(String accountName, int cloudProvider) {
        int result = accountName.hashCode();
        result = 31 * result + cloudProvider;
        return result;
    }


    public static StorageFolder getFolder(int cloudProvider, String path) {

        Realm myRealm = Realm.getDefaultInstance();

        StorageFolderRealm storageFolderRealm = myRealm.where(StorageFolderRealm.class)
                .equalTo("uid", gethashCode(path, cloudProvider)).findFirst();

        if (storageFolderRealm != null) {
            StorageFolder storageFolder = new StorageFolder();
            storageFolder.setUid(storageFolderRealm.getUid());
            storageFolder.setPath(storageFolderRealm.getPath());
            storageFolder.setFolder_path(storageFolderRealm.getFolder_path());
            storageFolder.setTitle(storageFolderRealm.getTitle());
            storageFolder.setCloudAccountId(storageFolderRealm.getCloudAccountId());
            storageFolder.setHomeFolder(storageFolderRealm.isHomeFolder());
            storageFolder.setExcludedFolder(storageFolderRealm.isExcludedFolder());
            storageFolder.setHiddenFolder(storageFolderRealm.isHiddenFolder());
            myRealm.close();
            return storageFolder;
        } else {
            myRealm.close();
            return new StorageFolder();
        }
    }

    public static ArrayList<StorageFolder> getHomeFolders() {

        ArrayList<StorageFolder> list = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<StorageFolderRealm> realmResults = myRealm.where(StorageFolderRealm.class).equalTo("homeFolder", true).sort("title", Sort.ASCENDING).findAll();

        for (StorageFolderRealm storageFolderRealm : realmResults) {
            CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccount(storageFolderRealm.getCloudAccountId());
            if (cloudAccount != null || storageFolderRealm.getCloudAccountId() == 0) {
                StorageFolder storageFolder = new StorageFolder();
                storageFolder.setUid(storageFolderRealm.getUid());
                storageFolder.setPath(storageFolderRealm.getPath());
                storageFolder.setFolder_path(storageFolderRealm.getFolder_path());
                storageFolder.setTitle(storageFolderRealm.getTitle());
                storageFolder.setCloudAccountId(storageFolderRealm.getCloudAccountId());
                storageFolder.setHomeFolder(storageFolderRealm.isHomeFolder());
                storageFolder.setExcludedFolder(storageFolderRealm.isExcludedFolder());
                storageFolder.setHiddenFolder(storageFolderRealm.isHiddenFolder());
                list.add(storageFolder);
            }
        }


        myRealm.close();
        return list;
    }

    public static ArrayList<StorageFolder> getExcludedFolders() {

        ArrayList<StorageFolder> list = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<StorageFolderRealm> realmResults = myRealm.where(StorageFolderRealm.class).equalTo("excludedFolder", true).findAll();

        for (StorageFolderRealm storageFolderRealm : realmResults) {
            StorageFolder storageFolder = new StorageFolder();
            storageFolder.setUid(storageFolderRealm.getUid());
            storageFolder.setPath(storageFolderRealm.getPath());
            storageFolder.setFolder_path(storageFolderRealm.getFolder_path());
            storageFolder.setTitle(storageFolderRealm.getTitle());
            storageFolder.setCloudAccountId(storageFolderRealm.getCloudAccountId());
            storageFolder.setHomeFolder(storageFolderRealm.isHomeFolder());
            storageFolder.setExcludedFolder(storageFolderRealm.isExcludedFolder());
            storageFolder.setHiddenFolder(storageFolderRealm.isHiddenFolder());
            list.add(storageFolder);
        }

        myRealm.close();
        return list;
    }


    public static ArrayList<StorageFolder> getHiddenFolders() {

        ArrayList<StorageFolder> list = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<StorageFolderRealm> realmResults = myRealm.where(StorageFolderRealm.class).equalTo("hiddenFolder", true).findAll();

        for (StorageFolderRealm storageFolderRealm : realmResults) {
            StorageFolder storageFolder = new StorageFolder();
            storageFolder.setUid(storageFolderRealm.getUid());
            storageFolder.setPath(storageFolderRealm.getPath());
            storageFolder.setFolder_path(storageFolderRealm.getFolder_path());
            storageFolder.setTitle(storageFolderRealm.getTitle());
            storageFolder.setCloudAccountId(storageFolderRealm.getCloudAccountId());
            storageFolder.setHomeFolder(storageFolderRealm.isHomeFolder());
            storageFolder.setExcludedFolder(storageFolderRealm.isExcludedFolder());
            storageFolder.setHiddenFolder(storageFolderRealm.isHiddenFolder());
            list.add(storageFolder);
        }

        myRealm.close();
        return list;
    }

    public static void deleteCloudFolders(long id) {
        Realm myRealm = Realm.getDefaultInstance();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        myRealm.where(StorageFolderRealm.class).equalTo("cloudAccountId", id).findAll().deleteAllFromRealm();

        myRealm.commitTransaction();

        myRealm.close();
    }
}
