package com.muziko.database;

import com.muziko.models.CloudAccount;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.muziko.helpers.Utils.gethashCode;

/**
 * Created by dev on 31/08/2016.
 */
public class CloudAccountRealmHelper {

    public static int insert(String accountName, String accessToken, int cloudProvider, boolean isDefault) {

        Realm myRealm = Realm.getDefaultInstance();
        int cloudAccountId;
        CloudAccountRealm cloudAccountRealm = new CloudAccountRealm();
        cloudAccountId = gethashCode(accountName, cloudProvider);
        cloudAccountRealm.setCloudAccountId(cloudAccountId);
        cloudAccountRealm.setName(accountName);
        cloudAccountRealm.setAccountName(accountName);
        cloudAccountRealm.setAccessToken(accessToken);
        cloudAccountRealm.setCloudProvider(cloudProvider);
        cloudAccountRealm.setDefault(isDefault);

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }
        myRealm.insertOrUpdate(cloudAccountRealm);
        myRealm.commitTransaction();

        myRealm.close();
        return cloudAccountId;
    }

    public static int insert(String accountName, String accessToken, int cloudProvider, boolean isDefault, String sharedPrefKey) {
        Realm myRealm = Realm.getDefaultInstance();
        int cloudAccountId;
        CloudAccountRealm cloudAccountRealm = new CloudAccountRealm();
        cloudAccountId = gethashCode(accountName, cloudProvider);
        cloudAccountRealm.setCloudAccountId(cloudAccountId);
        cloudAccountRealm.setName(accountName);
        cloudAccountRealm.setAccountName(accountName);
        cloudAccountRealm.setAccessToken(accessToken);
        cloudAccountRealm.setCloudProvider(cloudProvider);
        cloudAccountRealm.setDefault(isDefault);
        cloudAccountRealm.setSharedPrefKey(sharedPrefKey);

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }
        myRealm.insertOrUpdate(cloudAccountRealm);
        myRealm.commitTransaction();

        myRealm.close();
        return cloudAccountId;
    }

    public static boolean renameCloudAccount(long id, String name) {

        Realm myRealm = Realm.getDefaultInstance();

        CloudAccountRealm cloudAccountRealm = myRealm.where(CloudAccountRealm.class).equalTo("cloudAccountId", id).findFirst();

        if (cloudAccountRealm != null) {
            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            cloudAccountRealm.setName(name);
            myRealm.commitTransaction();
            myRealm.close();
            return true;
        } else {
            myRealm.close();
            return false;
        }
    }


    public static void refreshedCloudAccount(long id) {

        Realm myRealm = Realm.getDefaultInstance();

        CloudAccountRealm cloudAccountRealm = myRealm.where(CloudAccountRealm.class).equalTo("cloudAccountId", id).findFirst();

        if (cloudAccountRealm != null) {
            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            cloudAccountRealm.setLastUpdated(System.currentTimeMillis());
            myRealm.commitTransaction();
            myRealm.close();
        } else {
            myRealm.close();
        }
    }

    public static void setDefaultCloudAccount(long id) {

        ArrayList<CloudAccount> list = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<CloudAccountRealm> realmResults = myRealm.where(CloudAccountRealm.class).findAll();
        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }
        for (CloudAccountRealm cloudAccountRealm : realmResults) {
            if (cloudAccountRealm.getCloudAccountId() == id) {
                cloudAccountRealm.setDefault(true);
            } else {
                cloudAccountRealm.setDefault(false);
            }
        }
        myRealm.commitTransaction();
        myRealm.close();
    }

    public static void deleteCloudAccount(long id) {


        Realm myRealm = Realm.getDefaultInstance();

        CloudAccountRealm cloudAccountRealm = myRealm.where(CloudAccountRealm.class).equalTo("cloudAccountId", id).findFirst();

        if (cloudAccountRealm != null) {
            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            cloudAccountRealm.deleteFromRealm();
            myRealm.commitTransaction();
            myRealm.close();
        } else {
            myRealm.close();
        }
    }

    public static CloudAccount getCloudAccountToUpdate() {

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<CloudAccountRealm> realmResults = myRealm.where(CloudAccountRealm.class).findAllSorted("lastUpdated", Sort.ASCENDING);

        if (realmResults != null) {
            CloudAccountRealm cloudAccountRealm = realmResults.get(0);
            CloudAccount cloudAccount = new CloudAccount();
            cloudAccount.setCloudAccountId(cloudAccountRealm.getCloudAccountId());
            cloudAccount.setName(cloudAccountRealm.getName());
            cloudAccount.setAccountName(cloudAccountRealm.getAccountName());
            cloudAccount.setAccessToken(cloudAccountRealm.getAccessToken());
            cloudAccount.setCloudProvider(cloudAccountRealm.getCloudProvider());
            cloudAccount.setDefault(cloudAccountRealm.isDefault());
            cloudAccount.setSharedPrefKey(cloudAccountRealm.getSharedPrefKey());
            myRealm.close();
            return cloudAccount;
        } else {
            myRealm.close();
            return null;
        }
    }

    public static CloudAccount getCloudAccount(int cloudAccountId) {

        Realm myRealm = Realm.getDefaultInstance();

        CloudAccountRealm cloudAccountRealm = myRealm.where(CloudAccountRealm.class)
                .equalTo("cloudAccountId", cloudAccountId).findFirst();

        if (cloudAccountRealm != null) {
            CloudAccount cloudAccount = new CloudAccount();
            cloudAccount.setCloudAccountId(cloudAccountRealm.getCloudAccountId());
            cloudAccount.setName(cloudAccountRealm.getName());
            cloudAccount.setAccountName(cloudAccountRealm.getAccountName());
            cloudAccount.setAccessToken(cloudAccountRealm.getAccessToken());
            cloudAccount.setCloudProvider(cloudAccountRealm.getCloudProvider());
            cloudAccount.setDefault(cloudAccountRealm.isDefault());
            cloudAccount.setSharedPrefKey(cloudAccountRealm.getSharedPrefKey());
            myRealm.close();
            return cloudAccount;
        } else {
            myRealm.close();
            return null;
        }
    }

    public static CloudAccount getCloudAccount(String accountName, int cloudProvider) {

        Realm myRealm = Realm.getDefaultInstance();

        CloudAccountRealm cloudAccountRealm = myRealm.where(CloudAccountRealm.class)
                .beginGroup().equalTo("accountName", accountName)
                .equalTo("cloudProvider", cloudProvider)
                .endGroup().findFirst();

        if (cloudAccountRealm != null) {
            CloudAccount cloudAccount = new CloudAccount();
            cloudAccount.setCloudAccountId(cloudAccountRealm.getCloudAccountId());
            cloudAccount.setName(cloudAccountRealm.getName());
            cloudAccount.setAccountName(cloudAccountRealm.getAccountName());
            cloudAccount.setAccessToken(cloudAccountRealm.getAccessToken());
            cloudAccount.setCloudProvider(cloudAccountRealm.getCloudProvider());
            cloudAccount.setDefault(cloudAccountRealm.isDefault());
            cloudAccount.setSharedPrefKey(cloudAccountRealm.getSharedPrefKey());
            myRealm.close();
            return cloudAccount;
        } else {
            myRealm.close();
            return null;
        }
    }

    public static ArrayList<CloudAccount> getCloudAccountsForProvider(int cloudProvider) {

        ArrayList<CloudAccount> list = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<CloudAccountRealm> realmResults = myRealm.where(CloudAccountRealm.class).equalTo("cloudProvider", cloudProvider).findAll();

        for (CloudAccountRealm cloudAccountRealm : realmResults) {
            CloudAccount cloudAccount = new CloudAccount();
            cloudAccount.setCloudAccountId(cloudAccountRealm.getCloudAccountId());
            cloudAccount.setName(cloudAccountRealm.getName());
            cloudAccount.setAccountName(cloudAccountRealm.getAccountName());
            cloudAccount.setAccessToken(cloudAccountRealm.getAccessToken());
            cloudAccount.setCloudProvider(cloudAccountRealm.getCloudProvider());
            cloudAccount.setDefault(cloudAccountRealm.isDefault());
            cloudAccount.setSharedPrefKey(cloudAccountRealm.getSharedPrefKey());
            list.add(cloudAccount);
        }

        myRealm.close();
        return list;
    }

    public static ArrayList<CloudAccount> getCloudAccounts() {

        ArrayList<CloudAccount> list = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<CloudAccountRealm> realmResults = myRealm.where(CloudAccountRealm.class).findAll();

        for (CloudAccountRealm cloudAccountRealm : realmResults) {
            CloudAccount cloudAccount = new CloudAccount();
            cloudAccount.setCloudAccountId(cloudAccountRealm.getCloudAccountId());
            cloudAccount.setName(cloudAccountRealm.getName());
            cloudAccount.setAccountName(cloudAccountRealm.getAccountName());
            cloudAccount.setAccessToken(cloudAccountRealm.getAccessToken());
            cloudAccount.setCloudProvider(cloudAccountRealm.getCloudProvider());
            cloudAccount.setDefault(cloudAccountRealm.isDefault());
            cloudAccount.setSharedPrefKey(cloudAccountRealm.getSharedPrefKey());
            list.add(cloudAccount);
        }

        myRealm.close();
        return list;
    }

}
