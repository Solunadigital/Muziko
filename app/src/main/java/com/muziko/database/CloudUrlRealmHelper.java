package com.muziko.database;


import com.muziko.common.models.CloudUrl;

import io.realm.Realm;

import static com.muziko.manager.MuzikoConstants.halfDayMilliseconds;

/**
 * Created by dev on 17/07/2016.
 */
public class CloudUrlRealmHelper {

    public static CloudUrl getCloudUrl(String cloudId, int cloudProvider) {
        CloudUrl cloudUrl = null;
        Realm myRealm = Realm.getDefaultInstance();
        CloudUrlRealm cloudUrlRealm = myRealm.where(CloudUrlRealm.class).beginGroup().equalTo("cloudId", cloudId).equalTo("cloudProvider", cloudProvider).endGroup().findFirst();

        if (cloudUrlRealm != null && cloudUrlRealm.getCreated() + halfDayMilliseconds > System.currentTimeMillis()) {
            cloudUrl = new CloudUrl(cloudUrlRealm.getCloudId(), cloudUrlRealm.getCloudProvider(), cloudUrlRealm.getUrl(), cloudUrlRealm.getCreated());
        }
        myRealm.close();

        return cloudUrl;
    }

    public static void insertCloudUrl(String cloudId, int cloudProvider, String url) {

        Realm myRealm = Realm.getDefaultInstance();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        CloudUrlRealm cloudUrlRealm = new CloudUrlRealm();
        cloudUrlRealm.setCloudId(cloudId);
        cloudUrlRealm.setCloudProvider(cloudProvider);
        cloudUrlRealm.setUrl(url);
        cloudUrlRealm.setCreated(System.currentTimeMillis());
        myRealm.insertOrUpdate(cloudUrlRealm);

        myRealm.commitTransaction();
        myRealm.close();

    }

}