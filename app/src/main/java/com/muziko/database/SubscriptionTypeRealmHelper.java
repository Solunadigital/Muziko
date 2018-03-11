package com.muziko.database;

import com.muziko.models.MuzikoSubscriptionType;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by dev on 31/08/2016.
 */
public class SubscriptionTypeRealmHelper {

    public static void insert(MuzikoSubscriptionType subscriptionType) {

        Realm myRealm = Realm.getDefaultInstance();
        SubscriptionTypeRealm subscriptionTypeRealm = new SubscriptionTypeRealm();
        subscriptionTypeRealm.setSubscriptionTypeID(subscriptionType.getSubscriptionTypeID());
        subscriptionTypeRealm.setSubscriptionName(subscriptionType.getSubscriptionName());
        subscriptionTypeRealm.setSongLimit(subscriptionType.getSongLimit());
        subscriptionTypeRealm.setCreated(subscriptionType.getCreated());

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }
        myRealm.insertOrUpdate(subscriptionTypeRealm);
        myRealm.commitTransaction();

        myRealm.close();
        return;
    }


    public static ArrayList<MuzikoSubscriptionType> getSubscriptionTypes() {

        ArrayList<MuzikoSubscriptionType> list = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<SubscriptionTypeRealm> realmResults = myRealm.where(SubscriptionTypeRealm.class).findAll();

        for (SubscriptionTypeRealm subscriptionTypeRealm : realmResults) {
            MuzikoSubscriptionType subscriptionType = new MuzikoSubscriptionType();
            subscriptionType.setSubscriptionTypeID(subscriptionTypeRealm.getSubscriptionTypeID());
            subscriptionType.setSubscriptionName(subscriptionTypeRealm.getSubscriptionName());
            subscriptionType.setSongLimit(subscriptionTypeRealm.getSongLimit());
            subscriptionType.setCreated(subscriptionTypeRealm.getCreated());
            list.add(subscriptionType);
        }

        myRealm.close();
        return list;
    }

}
