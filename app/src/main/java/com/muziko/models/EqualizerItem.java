package com.muziko.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Keep;

import com.muziko.database.EqualizerRealm;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

@Keep
public class EqualizerItem {
    public static final String DB_TABLE = "equalizer";
    private static final String TAG = EqualizerItem.class.getSimpleName();
    private static final String KEY_ID = "_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_BAND1 = "band1";
    private static final String KEY_BAND2 = "band2";
    private static final String KEY_BAND3 = "band3";
    private static final String KEY_BAND4 = "band4";
    private static final String KEY_BAND5 = "band5";
    private static final String KEY_BAND6 = "band6";
    private static final String KEY_BAND7 = "band7";
    private static final String KEY_BAND8 = "band8";
    private static final String KEY_BAND9 = "band9";
    private static final String KEY_BAND10 = "band10";

    private static final String KEY_BASS = "bass";
    private static final String KEY_THREED = "threed";
    private static final String KEY_LOUDNESS = "loudness";
    private static final String KEY_REVERB = "reverb";
    public long id;
    public String title;
    public int band1;
    public int band2;
    public int band3;
    public int band4;
    public int band5;
    public int band6;
    public int band7;
    public int band8;
    public int band9;
    public int band10;

    public int bass;
    public int threed;
    public int loudness;
    public int reverb;

    public int position;

    public EqualizerItem(Context context) {
    }

    public EqualizerItem() {
        id = 0;
        title = "";

        band1 = 0;
        band2 = 0;
        band3 = 0;
        band4 = 0;
        band5 = 0;
        band6 = 0;
        band7 = 0;
        band8 = 0;
        band9 = 0;
        band10 = 0;

        bass = 0;
        threed = 0;
        loudness = 0;
        reverb = 0;

        position = -1;
    }

    private EqualizerItem(String s, int band1, int band2, int band3, int band4, int band5, int band6, int band7, int band8, int band9, int band10, int bass, int virtualizer, int loudness, int reverb) {
        this.title = s;
        this.band1 = band1;
        this.band2 = band2;
        this.band3 = band3;
        this.band4 = band4;
        this.band5 = band5;
        this.band6 = band6;
        this.band7 = band7;
        this.band8 = band8;
        this.band9 = band9;
        this.band10 = band10;

        this.bass = bass;
        this.threed = virtualizer;
        this.loudness = loudness;
        this.reverb = reverb;
    }

    public EqualizerItem(int position, String title) {
        this.position = position;
        this.title = title;
    }

    public static boolean delete(long id) {
        final Realm myRealm = Realm.getDefaultInstance();

        final EqualizerRealm equalizerRealm = myRealm.where(EqualizerRealm.class)
                .equalTo("id", id)
                .findFirst();

        if (equalizerRealm != null) {

            myRealm.beginTransaction();


            equalizerRealm.deleteFromRealm();
            myRealm.commitTransaction();

            myRealm.close();

            return true;
        } else {

            myRealm.close();
            return false;
        }
    }

    public static boolean deleteAll() {
        final Realm myRealm = Realm.getDefaultInstance();

        myRealm.beginTransaction();


        myRealm.where(EqualizerRealm.class).findAll().deleteAllFromRealm();
        myRealm.commitTransaction();

        myRealm.close();

        return true;
    }

    public static boolean deleteByTitle(String tt) {
        Realm myRealm = Realm.getDefaultInstance();

        final EqualizerRealm equalizerRealm = myRealm.where(EqualizerRealm.class)
                .equalTo("title", tt)
                .findFirst();

        if (equalizerRealm != null) {

            myRealm.beginTransaction();
            equalizerRealm.deleteFromRealm();
            myRealm.commitTransaction();

            myRealm.close();

            return true;
        } else {

            myRealm.close();
            return false;
        }
    }

    public static int getCount() {
        int count = 0;
        Realm myRealm = Realm.getDefaultInstance();
        try {
            RealmResults<EqualizerRealm> realmResults = myRealm.where(EqualizerRealm.class).findAll();

            if (realmResults != null) {
                count = realmResults.size();
            }
        } catch (Exception e) {
            myRealm.close();
        } finally {
            myRealm.close();
        }
        return count;
    }

    public static ArrayList<EqualizerItem> loadAll() {
        ArrayList<EqualizerItem> list = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<EqualizerRealm> realmResults = myRealm.where(EqualizerRealm.class).findAll();

        for (int i = 0; i < realmResults.size(); i++) {
            EqualizerItem item = new EqualizerItem();
            item.id = realmResults.get(i).getId();
            item.title = realmResults.get(i).getTitle();
            item.band1 = realmResults.get(i).getBand1();
            item.band2 = realmResults.get(i).getBand2();
            item.band3 = realmResults.get(i).getBand3();
            item.band4 = realmResults.get(i).getBand4();
            item.band5 = realmResults.get(i).getBand5();
            item.band6 = realmResults.get(i).getBand6();
            item.band7 = realmResults.get(i).getBand7();
            item.band8 = realmResults.get(i).getBand8();
            item.band9 = realmResults.get(i).getBand9();
            item.band10 = realmResults.get(i).getBand10();

            item.bass = realmResults.get(i).getBass();
            item.threed = realmResults.get(i).getThreed();
            item.loudness = realmResults.get(i).getLoudness();
            item.reverb = realmResults.get(i).getReverb();
            list.add(item);
        }

        myRealm.close();
        return list;
    }

    public static EqualizerItem getDefault() {
        EqualizerItem equalizerItem = null;

        Realm myRealm = Realm.getDefaultInstance();

        EqualizerRealm equalizerRealm = myRealm.where(EqualizerRealm.class)
                .equalTo("title", "Select Preset")
                .findFirst();

        if (equalizerRealm != null) {
            equalizerItem = new EqualizerItem();
            equalizerItem.id = equalizerRealm.getId();
            equalizerItem.title = equalizerRealm.getTitle();
            equalizerItem.band1 = equalizerRealm.getBand1();
            equalizerItem.band2 = equalizerRealm.getBand2();
            equalizerItem.band3 = equalizerRealm.getBand3();
            equalizerItem.band4 = equalizerRealm.getBand4();
            equalizerItem.band5 = equalizerRealm.getBand5();
            equalizerItem.band6 = equalizerRealm.getBand6();
            equalizerItem.band7 = equalizerRealm.getBand7();
            equalizerItem.band8 = equalizerRealm.getBand8();
            equalizerItem.band9 = equalizerRealm.getBand9();
            equalizerItem.band10 = equalizerRealm.getBand10();
            equalizerItem.bass = equalizerRealm.getBass();
            equalizerItem.threed = equalizerRealm.getThreed();
            equalizerItem.loudness = equalizerRealm.getLoudness();
            equalizerItem.reverb = equalizerRealm.getReverb();
        }

        myRealm.close();
        return equalizerItem;
    }

    public boolean copy(Cursor cursor) {
        boolean ret = false;
        try {
            id = cursor.getLong(cursor.getColumnIndex(KEY_ID));

            title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));

            band1 = cursor.getInt(cursor.getColumnIndex(KEY_BAND1));
            band2 = cursor.getInt(cursor.getColumnIndex(KEY_BAND2));
            band3 = cursor.getInt(cursor.getColumnIndex(KEY_BAND3));
            band4 = cursor.getInt(cursor.getColumnIndex(KEY_BAND4));
            band5 = cursor.getInt(cursor.getColumnIndex(KEY_BAND5));
            band6 = cursor.getInt(cursor.getColumnIndex(KEY_BAND6));
            band7 = cursor.getInt(cursor.getColumnIndex(KEY_BAND7));
            band8 = cursor.getInt(cursor.getColumnIndex(KEY_BAND8));
            band9 = cursor.getInt(cursor.getColumnIndex(KEY_BAND9));
            band10 = cursor.getInt(cursor.getColumnIndex(KEY_BAND10));

            bass = cursor.getInt(cursor.getColumnIndex(KEY_BASS));
            threed = cursor.getInt(cursor.getColumnIndex(KEY_THREED));
            loudness = cursor.getInt(cursor.getColumnIndex(KEY_LOUDNESS));
            reverb = cursor.getInt(cursor.getColumnIndex(KEY_REVERB));

            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();

        values.put(KEY_TITLE, title);
        values.put(KEY_BAND1, band1);
        values.put(KEY_BAND2, band2);
        values.put(KEY_BAND3, band3);
        values.put(KEY_BAND4, band4);
        values.put(KEY_BAND5, band5);
        values.put(KEY_BAND6, band6);
        values.put(KEY_BAND7, band7);
        values.put(KEY_BAND8, band8);
        values.put(KEY_BAND9, band9);
        values.put(KEY_BAND10, band10);

        values.put(KEY_BASS, bass);
        values.put(KEY_THREED, threed);
        values.put(KEY_LOUDNESS, loudness);
        values.put(KEY_REVERB, reverb);

        return values;
    }

    public boolean get(long kid) {
        boolean ret = false;
        Realm myRealm = Realm.getDefaultInstance();
        try {
            EqualizerRealm equalizerRealm = myRealm.where(EqualizerRealm.class)
                    .equalTo("id", kid)
                    .findFirst();

            if (equalizerRealm != null) {
                return true;
            }
        } catch (Exception e) {
            myRealm.close();
        } finally {
            myRealm.close();
        }
        return ret;
    }

    public boolean getByTitle(String dt) {

        boolean ret = false;

        Realm myRealm = Realm.getDefaultInstance();
        try {

            EqualizerRealm equalizerRealm = myRealm.where(EqualizerRealm.class)
                    .equalTo("title", dt)
                    .findFirst();

            if (equalizerRealm != null) {
                return true;
            }
        } catch (Exception e) {
            myRealm.close();
        } finally {
            myRealm.close();
        }
        return ret;
    }

    public long insert(final EqualizerItem equalizerItem) {

        int nextid;

        final Realm myRealm = Realm.getDefaultInstance();

        RealmResults<EqualizerRealm> realmResults = myRealm.where(EqualizerRealm.class).findAll();

        if (realmResults.size() == 0) {
            nextid = 1;
        } else

        {
            nextid = (myRealm.where(EqualizerRealm.class).max("id").intValue() + 1);
        }

        EqualizerRealm equalizerRealm = new EqualizerRealm();

        myRealm.beginTransaction();

        equalizerRealm.setId(nextid);
        equalizerRealm.setTitle(equalizerItem.title);
        equalizerRealm.setBand1(equalizerItem.band1);
        equalizerRealm.setBand2(equalizerItem.band2);
        equalizerRealm.setBand3(equalizerItem.band3);
        equalizerRealm.setBand4(equalizerItem.band4);
        equalizerRealm.setBand5(equalizerItem.band5);
        equalizerRealm.setBand6(equalizerItem.band6);
        equalizerRealm.setBand7(equalizerItem.band7);
        equalizerRealm.setBand8(equalizerItem.band8);
        equalizerRealm.setBand9(equalizerItem.band9);
        equalizerRealm.setBand10(equalizerItem.band10);

        equalizerRealm.setBass(equalizerItem.bass);
        equalizerRealm.setThreed(equalizerItem.threed);
        equalizerRealm.setLoudness(equalizerItem.loudness);
        equalizerRealm.setReverb(equalizerItem.reverb);

        myRealm.copyToRealmOrUpdate(equalizerRealm);

        myRealm.commitTransaction();

        myRealm.close();

        return nextid;

    }

    public boolean update(final EqualizerItem equalizerItem) {
        final Realm myRealm = Realm.getDefaultInstance();

        final EqualizerRealm equalizerRealm = myRealm.where(EqualizerRealm.class)
                .equalTo("id", equalizerItem.id)
                .findFirst();

        if (equalizerRealm != null) {

            myRealm.beginTransaction();

            equalizerRealm.setBand1(equalizerItem.band1);
            equalizerRealm.setBand2(equalizerItem.band2);
            equalizerRealm.setBand3(equalizerItem.band3);
            equalizerRealm.setBand4(equalizerItem.band4);
            equalizerRealm.setBand5(equalizerItem.band5);
            equalizerRealm.setBand6(equalizerItem.band6);
            equalizerRealm.setBand7(equalizerItem.band7);
            equalizerRealm.setBand8(equalizerItem.band8);
            equalizerRealm.setBand9(equalizerItem.band9);
            equalizerRealm.setBand10(equalizerItem.band10);

            equalizerRealm.setBass(equalizerItem.bass);
            equalizerRealm.setThreed(equalizerItem.threed);
            equalizerRealm.setLoudness(equalizerItem.loudness);
            equalizerRealm.setReverb(equalizerItem.reverb);

            myRealm.copyToRealmOrUpdate(equalizerRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return true;
        }
        myRealm.close();
        return false;
    }

    public void addUpdateDefault(final EqualizerItem equalizerItem) {
        final Realm myRealm = Realm.getDefaultInstance();

        EqualizerRealm equalizerRealm = myRealm.where(EqualizerRealm.class)
                .equalTo("title", "Select Preset")
                .findFirst();

        if (equalizerRealm != null) {
            myRealm.beginTransaction();

            equalizerRealm.setBand1(equalizerItem.band1);
            equalizerRealm.setBand2(equalizerItem.band2);
            equalizerRealm.setBand3(equalizerItem.band3);
            equalizerRealm.setBand4(equalizerItem.band4);
            equalizerRealm.setBand5(equalizerItem.band5);
            equalizerRealm.setBand6(equalizerItem.band6);
            equalizerRealm.setBand7(equalizerItem.band7);
            equalizerRealm.setBand8(equalizerItem.band8);
            equalizerRealm.setBand9(equalizerItem.band9);
            equalizerRealm.setBand10(equalizerItem.band10);

            equalizerRealm.setBass(equalizerItem.bass);
            equalizerRealm.setThreed(equalizerItem.threed);
            equalizerRealm.setLoudness(equalizerItem.loudness);
            equalizerRealm.setReverb(equalizerItem.reverb);

            myRealm.copyToRealmOrUpdate(equalizerRealm);
            myRealm.commitTransaction();

            myRealm.close();
        } else {
            insert(equalizerItem);
        }
        myRealm.close();
    }
}
