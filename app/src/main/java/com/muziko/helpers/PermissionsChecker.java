package com.muziko.helpers;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

/**
 * Created by dev on 17/11/2016.
 */

class PermissionsChecker {

	@TargetApi(23)
	public static boolean requestPermission(Activity activity, String permission, int rationale, int requestCode) {
		if (hasPermission(activity, permission))
			return true;
		if (activity.shouldShowRequestPermissionRationale(permission) && rationale != 0)
			Toast.makeText(activity, rationale, Toast.LENGTH_LONG).show();

		activity.requestPermissions(new String[]{permission}, requestCode);
		return false;
	}

	@TargetApi(23)
	private static boolean hasPermission(Activity activity, String permission) {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
				|| activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
	}

}
