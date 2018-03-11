package com.muziko.helpers;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaScannerConnection;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.AnyRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.provider.DocumentFile;
//import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.crashlytics.android.Crashlytics;
import com.muziko.BuildConfig;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.MediaHelper;
import com.muziko.models.EqualizerItem;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class Utils {
    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final String TAG = Utils.class.getSimpleName();

    public static String generateSha1Hash(String toHash) {
        String hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            // This is ~55x faster than looping and String.formating()
            hash = bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            Crashlytics.logException(e);
        } catch (UnsupportedEncodingException e) {
            Crashlytics.logException(e);
        }
        if (hash == null) {
            hash = toHash.substring(0, 30);
        }
        return hash;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getYoutubeVideoUrl(String videoId) {

        return "https://www.googleapis.com/youtube/v3/videos?part=contentDetails%2Cstatistics&id=" + videoId + "&key=" + BuildConfig.GoogleAPIKey;

    }

    /**
     * get uri to any resource type
     *
     * @param context - context
     * @param resId   - resource id
     * @return - Uri to resource by given id
     * @throws Resources.NotFoundException if the given ID does not exist.
     */
    public static Uri getUriToResource(@NonNull Context context,
                                       @AnyRes int resId)
            throws Resources.NotFoundException {
        Resources res = context.getResources();
        Uri resUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId));
        return resUri;
    }

    public static Uri formatDropBoxUrl(Uri uri) {
        final Uri.Builder newUri = uri.buildUpon().clearQuery();
        newUri.appendQueryParameter("dl", "1");
        newUri.appendQueryParameter("raw", "1");
        return newUri.build();
    }


    public static Uri addUriParameter(Uri uri, String key, String newValue) {
        final Set<String> params = uri.getQueryParameterNames();
        final Uri.Builder newUri = uri.buildUpon().clearQuery();
        newUri.appendQueryParameter(key, newValue);
        return newUri.build();
    }

    public static Uri replaceUriParameter(Uri uri, String key, String newValue) {
        final Set<String> params = uri.getQueryParameterNames();
        final Uri.Builder newUri = uri.buildUpon().clearQuery();
        for (String param : params) {
            String value;
            if (param.equals(key)) {
                value = newValue;
            } else {
                value = uri.getQueryParameter(param);
            }

            newUri.appendQueryParameter(param, value);
        }

        return newUri.build();
    }

    public static int randInt() {
        Random rand = new Random();
        int randomNum = rand.nextInt((100000 - 1) + 1) + 1;
        return randomNum;
    }

    public static long randLong() {
        Random rand = new Random();
        long randomNum = rand.nextInt((1000000 - 100000) + 1) + 100000;
        return randomNum;
    }


    public static Long getYouTubeDuration(String durationText) {
        String time = durationText.substring(2);
        long duration = 0L;
        Object[][] indexs = new Object[][]{{"H", 3600}, {"M", 60}, {"S", 1}};
        for (int i = 0; i < indexs.length; i++) {
            int index = time.indexOf((String) indexs[i][0]);
            if (index != -1) {
                String value = time.substring(0, index);
                duration += Integer.parseInt(value) * (int) indexs[i][1] * 1000;
                time = time.substring(value.length() + 1);
            }
        }
        return duration;
    }

    public static <V extends View> Collection<V> findChildrenByClass(ViewGroup viewGroup, Class<V> clazz) {

        return gatherChildrenByClass(viewGroup, clazz, new ArrayList<>());
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private static <V extends View> Collection<V> gatherChildrenByClass(ViewGroup viewGroup, Class<V> clazz, Collection<V> childrenFound) {

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            final View child = viewGroup.getChildAt(i);
            if (clazz.isAssignableFrom(child.getClass())) {
                childrenFound.add((V) child);
            }
            if (child instanceof ViewGroup) {
                gatherChildrenByClass((ViewGroup) child, clazz, childrenFound);
            }
        }

        return childrenFound;
    }

    public static Drawable getTintedDrawable(@NonNull Context context, @NonNull Drawable inputDrawable, @ColorInt int color) {
        Drawable wrapDrawable = DrawableCompat.wrap(inputDrawable);
        DrawableCompat.setTint(wrapDrawable, color);
        DrawableCompat.setTintMode(wrapDrawable, PorterDuff.Mode.SRC_IN);
        return wrapDrawable;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    @SuppressWarnings("deprecation")
    public static String toHtml(Spanned source) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.toHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.toHtml(source);
        }
    }

    public static int levenshteinDistance(String a, String b) {
        a = a.toLowerCase(Locale.US);
        b = b.toLowerCase(Locale.US);
        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    public static void hideKeyboard(Context context, View vw) {
        if (context == null) return;

        if (vw == null) return;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(vw.getWindowToken(), 0);
    }

    public static boolean contentURIExists(Context context, String contentUri) {

        boolean ret = false;
        ContentResolver cr = context.getContentResolver();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cur = cr.query(Uri.parse(contentUri), projection, null, null, null);
        if (cur != null) {
            if (cur.moveToFirst()) {
                String filePath = cur.getString(0);

                ret = new File(filePath).exists();
            } else {
                ret = false;
            }
            cur.close();
        } else {
            ret = false;
        }

        return ret;
    }

    public static String getSHA1(final String s) {
        if (s == null) return "";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA1");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean searchStringStartsWith(String needle, String haystack) {
        return haystack.toLowerCase().startsWith(needle.toLowerCase());
    }

    public static boolean searchStringContains(String needle, String haystack) {
        return haystack.toLowerCase().contains(needle.toLowerCase());
    }


    public static String getLongDuration(long milliseconds) {
        String message = "";

        if (milliseconds >= 1000) {
            int seconds = (int) (milliseconds / 1000) % 60;
            int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
            int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
            int days = (int) (milliseconds / (1000 * 60 * 60 * 24));
            if ((days == 0) && (hours != 0)) {
                message = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            } else if ((hours == 0) && (minutes != 0)) {
                message = String.format("%02d:%02d", minutes, seconds);
            } else if ((days == 0) && (hours == 0) && (minutes == 0)) {
                message = String.format("%d second%s", seconds, seconds != 1 ? "s" : "");
            } else {
                message = String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
            }
        } else {
            message = "0 seconds";
        }
        return message;
    }

    public static String getByteFormat(long data) {

        if ((float) data >= 1024 * 1024 * 1024)
            return String.format("%.2f ", (float) data / (1024 * 1024 * 1024)) + "GB";
        else if ((float) data >= 1024 * 1024)
            return String.format("%.2f ", (float) data / (1024 * 1024)) + "MB";
        else if ((float) data >= 1024)
            return String.format("%.2f ", (float) data / 1024) + "KB";
        else
            return String.format("%d ", data) + "B";
    }


    public static String getDuration(long milliseconds) {
        long sec = (milliseconds / 1000) % 60;
        long min = (milliseconds / (60 * 1000)) % 60;
        long hour = milliseconds / (60 * 60 * 1000);

        String s = (sec < 10) ? "0" + sec : "" + sec;
        String m = (min < 10) ? "0" + min : "" + min;
        String h = "" + hour;

        String time = "";
        if (hour > 0) {
            time = h + ":" + m + ":" + s;
        } else {
            time = m + ":" + s;
        }
        return time;
    }

    public static String convertMillisecondstoDuration(long seconds) {

        seconds = seconds / 1000;

        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }


    public static ArrayList<QueueItem> musicFromMuziko(Context context) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor c = context.getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_RINGTONE + " != 0", null, null);
        ArrayList<QueueItem> listOfSongs = new ArrayList<>();
        c.moveToFirst();

        while (c.moveToNext()) {
            QueueItem songData = new QueueItem();
            String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            long duration = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION));
            String data = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
            long albumId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String composer = c.getString(c.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
            String dateAdded = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));

            if (artist != null && artist.contains("Muziko-Ringtone")) {

                songData.title = (title);
                songData.album_name = (album);
                songData.artist_name = (artist);
                songData.duration = (duration + "");
                songData.album = (albumId);
                songData.date = Long.valueOf((dateAdded));
                songData.data = (data);
                listOfSongs.add(songData);

            }
        }
        c.close();
        Log.d("SIZE", "SIZE: " + listOfSongs.size());
        return listOfSongs;
    }

    public static Bitmap stopRecyclingBitmap(Bitmap bitmap) {

        if (bitmap != null) {
            // RemoteControlClient wants to recycle the currentAlbumArts thrown at it, so we need
            // to make sure not to hand out our cache copy
            Bitmap.Config config = bitmap.getConfig();
            if (config == null) {
                config = Bitmap.Config.ARGB_8888;
            }
            bitmap = bitmap.copy(config, false);
        }

        return bitmap;
    }


    public static boolean isServiceRunning(String serviceName, Context context) {

        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceName.equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
        return false;
    }

    public static boolean currentVersionSupportBigNotification() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        return sdkVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean currentVersionSupportLockScreenControls() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        return sdkVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static String trimString(String str, int count) {
        String string = str;

        if (string.length() > count) {
            string = string.substring(0, count) + "...";
        }

        return string;
    }

    public static void deleteSong(Context context, String path) {
        boolean deleted;

        Uri rootUri1 = MediaStore.Audio.Media.getContentUriForPath(path);
        //Uri rootUri2 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        int d1 = context.getContentResolver().delete(rootUri1, MediaStore.MediaColumns.DATA + "=?", new String[]{path});
        //int d2 = context.getContentResolver().delete(rootUri2, MediaStore.MediaColumns.DATA + "=?", new String[]{path});
        //context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA + "='" + path + "'", null);

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));

        File file = new File(path);

        deleted = file.delete();

        if (!deleted) {
            // Try with Storage Access Framework.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && FileHelper.isOnExtSdCard(context, file)) {

                DocumentFile targetDocument = SAFHelpers.getDocumentFile(new File(path), false);
                if (targetDocument != null) {
                    deleted = targetDocument.delete();
                }
            }

            // Try the Kitkat workaround.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                ContentResolver resolver = context.getContentResolver();

                try {
                    Uri uri = MediaStoreHack.getUriFromFile(file.getAbsolutePath(), context);
                    resolver.delete(uri, null, null);
                    deleted = !file.exists();
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            }
        }
    }

    public static void scanMedia(Context mContext, String data) {
//		c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));

        MediaScannerConnection.scanFile(mContext,
                new String[]{Uri.parse("file://" + data).toString()}, null,
                (path, uri) -> {

                    MediaHelper.Instance().loadMusicFromTrack(Uri.parse("file://" + data).toString(), false);

                    QueueItem queueItem = TrackRealmHelper.getTrack(Uri.parse("file://" + data).toString());

                    if (queueItem != null) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
                        boolean prefArtworkDownload = prefs.getBoolean("prefArtworkDownload", false);
                        if (queueItem != null && prefArtworkDownload) {
                            ArtworkHelper artworkHelper = new ArtworkHelper();
                            artworkHelper.autoPickAlbumArt(mContext, queueItem, true);
                        }
                    }

                    EventBus.getDefault().post(new RefreshEvent(1000));
                });
    }

    public static void askDelete(Context context, String title, String msg, final Runnable runnable) {
        new MaterialDialog.Builder(context).cancelable(false).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title(title).content(msg).positiveText("YES").onPositive((dialog, which) -> {
            if (runnable != null) runnable.run();
        }).negativeText("NO").show();
    }

    public static int getInt(String input, int def) {
        int val = def;
        try {
            val = Integer.parseInt(input);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return val;
    }


    public static boolean renameFile(Context c, String src, String songTitle) {

        boolean ret = false;

        File from = new File(src);
        try {

            MusicMetadataSet src_set = new MyID3().read(from); // read metadata
            if (src_set != null) {

                MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
                String artist = metadata.getArtist();
                String album = metadata.getAlbum();
                String song_title = metadata.getSongTitle();
                Number track_number = metadata.getTrackNumber();

                System.out.println("ARTIST : " + artist);
                System.out.println("SONG NAME : " + song_title);
                System.out.println("ALBUM : " + album);
                System.out.println("TRACK NUMBER : " + track_number);

                metadata.setSongTitle(songTitle);
                File dst = new File(src);
                //new MyID3().write(from, dst, src_set, metadata);  // write updated metadata
                new MyID3().update(dst, src_set, metadata, false);

                AppController.Instance().refreshMusicData(src);
                //                dst.createNewFile();

                ret = true;
            } else {

                System.out.println("SRC SET IS EMPTY!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }

        viewGroup.setAlpha(enabled ? 1.0f : 0.75f);

    }

    public static void GetScreenSize(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        MyApplication.IMAGE_SIZE = (int) (width * 0.30);

        MyApplication.IMAGE_WIDTH = width;
        MyApplication.IMAGE_HEIGHT = height;


    }

    // 2016.06.30 dev Locke
    // Get screen size for optimized coverart

    public static void alert(Context context, String title, String msg, final Runnable runnable) {
        new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .titleColorRes(R.color.normal_blue)
                .negativeColorRes(R.color.dialog_negetive_button)
                .positiveColorRes(R.color.normal_blue)
                .title(title)
                .content(msg)
                .positiveText("OK").onPositive((dialog, which) -> {
            if (runnable != null) runnable.run();
        }).show();
    }

    public static void alertNoDismiss(Context context, String title, String msg, final Runnable runnable) {
        new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .titleColorRes(R.color.normal_blue)
                .negativeColorRes(R.color.dialog_negetive_button)
                .positiveColorRes(R.color.normal_blue)
                .title(title)
                .cancelable(false)
                .content(msg)
                .positiveText("OK").onPositive((dialog, which) -> {
            if (runnable != null) runnable.run();
        }).show();
    }

    public static int gethashCode(String accountName, int cloudProvider) {
        int result = accountName.hashCode();
        result = 31 * result + cloudProvider;
        return result;
    }

    public static String getPath(Context context, Uri uri) {
        try {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                String[] projection = {"_data"};
                Cursor cursor = null;

                try {
                    cursor = context.getContentResolver().query(uri, projection, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index);
                    }
                    cursor.close();
                } catch (Exception e) {
                    // Eat it
                }
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }

        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        return null;
    }


    public static ArrayList<EqualizerItem> loadEqualizer(Equalizer eq) {
        ArrayList<EqualizerItem> items = new ArrayList<>();
        EqualizerItem first = EqualizerItem.getDefault();
        if (first == null) {
            first = new EqualizerItem(0, "Default");
            first.id = -1;
        }

        items.add(first);

        if (eq != null) {
            short m = eq.getNumberOfPresets();
            for (int k = 0; k < m; k++) {
                EqualizerItem item = new EqualizerItem(k, eq.getPresetName((short) k));
                try {
                    short pres = (short) item.position;
                    eq.usePreset(pres);

                    item.id = -1;
                    item.band1 = eq.getBandLevel((short) 0);
                    item.band2 = eq.getBandLevel((short) 1);
                    item.band3 = eq.getBandLevel((short) 2);
                    item.band4 = eq.getBandLevel((short) 3);
                    item.band5 = eq.getBandLevel((short) 4);
                    item.band6 = eq.getBandLevel((short) 5);
                    item.band7 = eq.getBandLevel((short) 6);
                    item.band8 = eq.getBandLevel((short) 7);
                    item.band9 = eq.getBandLevel((short) 8);
                    item.band10 = eq.getBandLevel((short) 9);

                    item.bass = 0;
                    item.threed = 0;
                    item.loudness = 0;
                    item.position = k;
                } catch (Exception e) {
                    Log.i(TAG, e.getMessage());
                }

                items.add(item);
            }
        }

        List<EqualizerItem> list = EqualizerItem.loadAll();
        for (EqualizerItem item : list) {
            item.position = -1;
            items.add(item);
        }

        return items;
    }


    public static int[] getRates(String path) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            MediaExtractor mediaExtractor = new MediaExtractor();
            try {
                mediaExtractor.setDataSource(path);
                int numTracks = mediaExtractor.getTrackCount();
                for (int i = 0; i < numTracks; i++) {
                    MediaFormat format = mediaExtractor.getTrackFormat(i);
                    if (format == null) continue;

                    String mime = format.getString(MediaFormat.KEY_MIME);

                    int bitRate = format.containsKey(android.media.MediaFormat.KEY_BIT_RATE) ? format.getInteger(MediaFormat.KEY_BIT_RATE) : 0;
                    int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    long duration = format.containsKey(android.media.MediaFormat.KEY_DURATION) ? format.getLong(android.media.MediaFormat.KEY_DURATION) / 1000 : 0;
                    return new int[]{bitRate, sampleRate, (int) duration};
                }
                mediaExtractor.release();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }

        return null;
    }

    public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }


    public static Bitmap decodeBitmapArray(byte[] data, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param context Context to get resources and device specific display metrics
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static int convertDpToPixel(Context context, float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * Converts dp to px
     *
     * @param res Resources
     * @param dp  the value in dp
     * @return int
     */
    public static int toPixels(Resources res, float dp) {
        return (int) (dp * res.getDisplayMetrics().density);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl(Resources res) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) &&
                (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    public static void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }


    public static String md5(final String toEncrypt) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(toEncrypt.getBytes());
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(String.format("%02X", aByte));
            }
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            return ""; // Impossibru!
        }
    }


    public static boolean isBitmapDark(Bitmap bitmap) {
        boolean dark = false;

        float darkThreshold = bitmap.getWidth() * bitmap.getHeight() * 0.45f;
        int darkPixels = 0;

        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            double luminance = (0.299 * r + 0.0f + 0.587 * g + 0.0f + 0.114 * b + 0.0f);
            if (luminance < 150) {
                darkPixels++;
            }
        }
        if (darkPixels >= darkThreshold) {
            dark = true;
        }
        return dark;
    }

    public static boolean isEmptyString(String text) {
        return (text == null || text.trim().equals("null") || text.trim()
                .length() <= 0);
    }

    public static Locale getLocale(String language) {

        Locale locale = new Locale(language);
        return locale;
    }

}
