package com.muziko.helpers;

import com.google.gson.reflect.TypeToken;
import com.muziko.manager.GsonManager;
import com.muziko.manager.PrefsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dev on 27/08/2016.
 */
public class ClipboardHelper {


    public static void saveClip(String url) {

        ArrayList<String> clipHistory = loadClipHistory();
        clipHistory.add(url);

        String json = GsonManager.Instance().getGson().toJson(clipHistory);
        PrefsManager.Instance().setClipHistory(json);
    }

    public static ArrayList<String> loadClipHistory() {

        String json = PrefsManager.Instance().getClipHistory();
        ArrayList<String> clipHistory =
                GsonManager.Instance().getGson().fromJson(json, new TypeToken<List<String>>() {
                }.getType());

        if (clipHistory != null) {
            return clipHistory;
        } else {
            return new ArrayList<>();
        }
    }
}
