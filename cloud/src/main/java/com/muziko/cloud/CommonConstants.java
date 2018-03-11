package com.muziko.cloud;

/**
 * Created by Bradley on 10/03/2017.
 */

public class CommonConstants {

    /**
     * Api Keys can be obtained from the google cloud console
     */
    public static final String API_KEY = System.getProperty("gcm.api.key");

    public enum CloudFileActions {UPLOAD, DOWNLOAD, DELETE, RETRY, CONTINUE}
}
