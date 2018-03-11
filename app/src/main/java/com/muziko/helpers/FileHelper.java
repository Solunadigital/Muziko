package com.muziko.helpers;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.CloudManager;
import com.muziko.manager.SettingsManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dev on 25/07/2016.
 */
public class FileHelper {

    public static void updateDateModified(Context context, String path) {
        try {
            File file = new File(path);

            if (FileHelper.isOnExtSdCard(context, file)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    DocumentFile targetDocument = null;
                    try {
                        targetDocument = SAFHelpers.getDocumentFile(new File(path), false);
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }

                    if (targetDocument != null) {
                        file.setLastModified(System.currentTimeMillis());
                    }
                } else {
                    file.setLastModified(System.currentTimeMillis());
                }
            } else {
                file.setLastModified(System.currentTimeMillis());
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access Framework Documents,
     * as well as the _data field for the MediaStore and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri =
                            ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for MediaStore Uris, and other
     * file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(
            Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isOnExtSdCard(Context mcontext, final File file) {
        return getExtSdCardFolder(file, mcontext) != null;
    }

    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD
     * card. Otherwise, null is returned.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getExtSdCardFolder(final File file, Context context) {
        String[] extSdPaths = getExtSdCardPaths(context);
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("AmazeFileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }

    public static String getExtensionFromFilename(String filename) {
        try {
            return filename.substring(filename.lastIndexOf('.'), filename.length());
        } catch (Exception e) {
            return "mp3";
        }
    }

    public static boolean fileExists(QueueItem queueItem) {
        return doesFileExist(queueItem.title);
    }

    public static boolean localorFirebaseFileExists(CloudTrack cloudTrack) {

        QueueItem queueItem = TrackRealmHelper.getTrackByMD5(cloudTrack.getMd5());
        return queueItem != null;
    }

    public static boolean localFileExists(CloudTrack cloudTrack) {

        QueueItem queueItem = TrackRealmHelper.getTrackByMD5(cloudTrack.getMd5());
        return queueItem != null && queueItem.storage != CloudManager.FIREBASE;
    }

    public static String makeRingtoneFilename(Context context, CharSequence title) {

        String subdir;
        String externalRootDir = Environment.getExternalStorageDirectory().getPath();
        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }
        subdir = "Muziko/";
        String parentdir = externalRootDir + subdir;

        // Create the parent directory
        File parentDirFile = new File(parentdir);
        if (!parentDirFile.exists()) {
            parentDirFile.mkdirs();
        }
        return parentdir + "/" + title + "." + "mp3";
    }

    public static String getStreamSaveFolderString(Context context, String fileid) {
        File directory = context.getDir("stream", Context.MODE_PRIVATE);
        File streamPath = new File(directory, fileid + ".mp3");
        return streamPath.getAbsolutePath();
    }

    public static boolean doesFileExist(String fileName) {

        String subdir;
        String externalRootDir = null;
        if (SettingsManager.Instance().getPrefSyncLocation() == 0) {
            externalRootDir = Environment.getExternalStorageDirectory().getPath();
        } else {
            List<StorageHelper.StorageVolume> storageVolumes = StorageHelper.getStorages();
            for (StorageHelper.StorageVolume storageVolume : storageVolumes) {
                if (storageVolume.isRemovable()) {
                    externalRootDir = storageVolume.file.getPath();
                    break;
                }
            }
        }

        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }
        subdir = "Muziko/";
        String parentdir = externalRootDir + subdir;

        // Create the parent directory
        File directory = new File(parentdir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File streamPath = new File(directory, fileName);

        if (SettingsManager.Instance().getPrefSyncLocation() > 0) {
            DocumentFile targetDocument = SAFHelpers.getDocumentFile(streamPath, false);
            if (targetDocument == null) {
                return false;
            }
        }

        return streamPath.exists();
    }

    public static File getDownloadFolder(String fileName) {

        String subdir;
        String externalRootDir = null;
        if (SettingsManager.Instance().getPrefSyncLocation() == 0) {
            externalRootDir = Environment.getExternalStorageDirectory().getPath();
        } else {
            List<StorageHelper.StorageVolume> storageVolumes = StorageHelper.getStorages();
            for (StorageHelper.StorageVolume storageVolume : storageVolumes) {
                if (storageVolume.isRemovable()) {
                    externalRootDir = storageVolume.file.getPath();
                    break;
                }
            }
        }

        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }
        subdir = "Muziko/";
        String parentdir = externalRootDir + subdir;

        // Create the parent directory
        File directory = new File(parentdir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File streamPath = new File(directory, fileName);

        if (SettingsManager.Instance().getPrefSyncLocation() > 0) {
            DocumentFile targetDocument = SAFHelpers.getDocumentFile(streamPath, false);
            if (targetDocument == null) {
                return null;
            }
        }

        if (!streamPath.exists()) {
            try {
                streamPath.createNewFile();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        } else {
            streamPath.delete();
            try {
                streamPath.createNewFile();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }
        return streamPath;
    }

    public static File getDownloadFolder(Context mContext, String path, String fileName) {

        if (!path.endsWith("/")) {
            path += "/";
        }

        File directory = new File(path);
        File savePath = new File(directory, fileName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && FileHelper.isOnExtSdCard(mContext, directory)) {
            DocumentFile targetDocument = null;
            try {
                targetDocument = SAFHelpers.getDocumentFile(savePath, false);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }

            if (targetDocument == null) {
                return null;
            }
        }

        if (!savePath.exists()) {
            try {
                savePath.createNewFile();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        } else {
            savePath.delete();
            try {
                savePath.createNewFile();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }
        return savePath;
    }

    public static File getStreamSaveFolder(Context context, String fileid) {

        File directory = context.getDir("stream", Context.MODE_PRIVATE);


        if (!directory.exists()) {
            directory.mkdir();
        }

        File streamPath = new File(directory, fileid + ".mp3");
        if (!streamPath.exists()) {
            try {
                streamPath.createNewFile();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        } else {
            streamPath.delete();
            try {
                streamPath.createNewFile();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }
        return streamPath;
    }

    public static File getMuzikoFolder() {

        String subdir;
        String externalRootDir = Environment.getExternalStorageDirectory().getPath();
        if (SettingsManager.Instance().getPrefSyncLocation() == 0) {
            externalRootDir = Environment.getExternalStorageDirectory().getPath();
        } else {
            List<StorageHelper.StorageVolume> storageVolumes = StorageHelper.getStorages();
            for (StorageHelper.StorageVolume storageVolume : storageVolumes) {
                if (storageVolume.isRemovable()) {
                    externalRootDir = storageVolume.file.getPath();
                    break;
                }
            }
        }

        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }
        subdir = "Muziko/";
        String parentdir = externalRootDir + subdir;

        // Create the parent directory
        File parentDirFile = new File(parentdir);
        if (!parentDirFile.exists()) {
            parentDirFile.mkdirs();
        }

        return parentDirFile;
    }

    public static File getMuzikoSyncFolder() {

        String subdir;
        String externalRootDir = null;
        if (SettingsManager.Instance().getPrefSyncLocation() == 0) {
            externalRootDir = Environment.getExternalStorageDirectory().getPath();
        } else {
            List<StorageHelper.StorageVolume> storageVolumes = StorageHelper.getStorages();
            for (StorageHelper.StorageVolume storageVolume : storageVolumes) {
                if (storageVolume.isRemovable()) {
                    externalRootDir = storageVolume.file.getPath();
                    break;
                }
            }
        }

        if (externalRootDir == null) {
            return null;
        }

        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }
        subdir = "Muziko/";
        String parentdir = externalRootDir + subdir;

        // Create the parent directory
        File parentDirFile = new File(parentdir);
        if (!parentDirFile.exists()) {
            parentDirFile.mkdirs();
        }

        return parentDirFile;
    }


    public static OutputStream getOutputStreamForFile(Context mcontext, File downloadFile) {
        OutputStream outputStream = null;
        if (isOnExtSdCard(mcontext, downloadFile)) {
            DocumentFile targetDocument = SAFHelpers.getDocumentFile(downloadFile, false);
            if (targetDocument == null) {
                return null;
            }
            try {
                outputStream = mcontext.getContentResolver().openOutputStream(targetDocument.getUri());
            } catch (FileNotFoundException e) {
                Crashlytics.logException(e);
            }
        } else {
            try {
                outputStream = new FileOutputStream(downloadFile);
            } catch (FileNotFoundException e) {
                Crashlytics.logException(e);
            }
        }
        return outputStream;
    }
}
