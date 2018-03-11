package com.muziko.cloud.Drive;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DriveApiHelpers {
    public static final String MYROOT = "DEMORoot";
    public static final String MIME_TEXT = "text/plain";
    public static final String MIME_FLDR = "application/vnd.google-apps.folder";
    public static final String MIME_AUDIO = "audio/mp3";
    public static final String MIME_MPEG = "audio/mpeg";
    public static final String MIME_XMPEG = "audio/x-mpegurl";
    public static final String TITL = "titl";
    public static final String GDID = "gdid";
    public static final String MIME = "mime";
    private static final String L_TAG = "_X_";
    private static final String TITL_FMT = "yyMMdd-HHmmss";
    static Context acx;
    private static SharedPreferences pfs;

    private DriveApiHelpers() {
    }

    public static void init(Context ctx) {
        acx = ctx.getApplicationContext();
        pfs = PreferenceManager.getDefaultSharedPreferences(acx);
    }

    static ContentValues newCVs(String titl, String gdId, String mime) {
        ContentValues cv = new ContentValues();
        if (titl != null) cv.put(TITL, titl);
        if (gdId != null) cv.put(GDID, gdId);
        if (mime != null) cv.put(MIME, mime);
        return cv;
    }

    private static File cchFile(String flNm) {
        File cche = DriveApiHelpers.acx.getExternalCacheDir();
        return (cche == null || flNm == null) ? null : new File(cche.getPath() + File.separator + flNm);
    }

    static File str2File(String str, String name) {
        if (str == null) return null;
        byte[] buf = str.getBytes();
        File fl = cchFile(name);
        if (fl == null) return null;
        BufferedOutputStream bs = null;
        try {
            bs = new BufferedOutputStream(new FileOutputStream(fl));
            bs.write(buf);
        } catch (Exception e) {
            le(e);
        } finally {
            if (bs != null) try {
                bs.close();
            } catch (Exception e) {
                le(e);
            }
        }
        return fl;
    }

    static byte[] is2Bytes(InputStream is) {
        byte[] buf = null;
        BufferedInputStream bufIS = null;
        if (is != null) try {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            bufIS = new BufferedInputStream(is);
            buf = new byte[4096];
            int cnt;
            while ((cnt = bufIS.read(buf)) >= 0) {
                byteBuffer.write(buf, 0, cnt);
            }
            buf = byteBuffer.size() > 0 ? byteBuffer.toByteArray() : null;
        } catch (Exception ignore) {
        } finally {
            try {
                if (bufIS != null) bufIS.close();
            } catch (Exception ignore) {
            }
        }
        return buf;
    }

    static String time2Titl(Long milis) {       // time -> yymmdd-hhmmss
        Date dt = (milis == null) ? new Date() : (milis >= 0) ? new Date(milis) : null;
        return (dt == null) ? null : new SimpleDateFormat(TITL_FMT, Locale.US).format(dt);
    }

    static String titl2Month(String titl) {
        return titl == null ? null : ("20" + titl.substring(0, 2) + "-" + titl.substring(2, 4));
    }

    static void le(Throwable ex) {
        Log.e(L_TAG, Log.getStackTraceString(ex));
    }

    public static void lg(String msg) {
        Log.d(L_TAG, msg);
    }

    public static String getDownloadUrl(String myurl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();
            String contentAsString = readIt(is);
            return contentAsString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static String readIt(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("fmt_stream_map")) {
                sb.append(line + "\n");
                break;
            }
        }
        reader.close();
        String result = decode(sb.toString());
        String[] url = result.split("\\|");
        return url[1];
    }

    public static String decode(String in) {
        String working = in;
        int index;
        index = working.indexOf("\\u");
        while (index > -1) {
            int length = working.length();
            if (index > (length - 6)) break;
            int numStart = index + 2;
            int numFinish = numStart + 4;
            String substring = working.substring(numStart, numFinish);
            int number = Integer.parseInt(substring, 16);
            String stringStart = working.substring(0, index);
            String stringEnd = working.substring(numFinish);
            working = stringStart + ((char) number) + stringEnd;
            index = working.indexOf("\\u");
        }
        return working;
    }

    public static class AM {
        private static final String ACC_NAME = "account_name";
        private static String mEmail = null;

        private AM() {
        }

        public static String getEmail() {
            return mEmail != null ? mEmail : (mEmail = DriveApiHelpers.pfs.getString(ACC_NAME, null));
        }

        public static void setEmail(String email) {
            DriveApiHelpers.pfs.edit().putString(ACC_NAME, (mEmail = email)).apply();
        }
    }
}


