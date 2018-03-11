package com.muziko.manager;

import com.google.gson.Gson;

/**
 * Created by Bradley on 16/05/2017.
 */

public class GsonManager {

    private static GsonManager instance;
    private Gson gson;

    //no outer class can initialize this class's object
    private GsonManager() {
    }

    public static GsonManager Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new GsonManager();
        }
        return instance;
    }

    public Gson getGson() {
        return gson;
    }

    public void init() {

        gson = new Gson();
    }
}
