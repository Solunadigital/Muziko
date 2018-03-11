package com.muziko.helpers;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;

import com.muziko.MyApplication;
import com.muziko.manager.PrefsManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dev on 25/08/2016.
 */
public class SAFHelpers {

	public static DocumentFile getDocumentFile(final File file, final boolean isDirectory) {
		String baseFolder = getExtSdCardFolder(file);

		if (baseFolder == null) {
			return null;
		}

		String relativePath = null;
		try {
			String fullPath = file.getCanonicalPath();
			relativePath = fullPath.substring(baseFolder.length() + 1);
		} catch (IOException e) {
			return null;
		}

        Uri treeUri = Uri.parse(PrefsManager.Instance().getStoragePermsURi());

		if (treeUri == null) {
			return null;
		}

		// start with root of SD card and then parse through document tree.
		DocumentFile document = DocumentFile.fromTreeUri(MyApplication.getInstance().getApplicationContext(), treeUri);

		String[] parts = relativePath.split("\\/");
		for (int i = 0; i < parts.length; i++) {
			DocumentFile nextDocument = document.findFile(parts[i]);

			if (nextDocument == null) {
				if ((i < parts.length - 1) || isDirectory) {
					nextDocument = document.createDirectory(parts[i]);
				} else {
					nextDocument = document.createFile("audio", parts[i]);
				}
			}
			document = nextDocument;
		}

		return document;
	}

	private static String getExtSdCardFolder(final File file) {
		String[] extSdPaths = getExtSdCardPaths();
		try {
			for (String extSdPath : extSdPaths) {
				if (file.getCanonicalPath().startsWith(extSdPath)) {
					return extSdPath;
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
	private static String[] getExtSdCardPaths() {
		List<String> paths = new ArrayList<>();
		for (File file : MyApplication.getInstance().getApplicationContext().getExternalFilesDirs("external")) {
			if (file != null && !file.equals(MyApplication.getInstance().getApplicationContext().getExternalFilesDir("external"))) {
				int index = file.getAbsolutePath().lastIndexOf("/Android/data");
				if (index < 0) {
//					Log.w(Application.TAG, "Unexpected external file dir: " + file.getAbsolutePath());
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
		return paths.toArray(new String[paths.size()]);
	}
}
