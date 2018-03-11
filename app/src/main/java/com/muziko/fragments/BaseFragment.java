package com.muziko.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
//import android.support.v7.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.R;
import com.muziko.manager.PrefsManager;

import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_STORAGE_ACCESS;

public class BaseFragment extends Fragment {
    static final long LOAD_DELAY = 0;
    private Runnable pendingRunnable;

    public void checkStoragePermissions(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
            new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Grant SD card permissions").content(message).positiveText("OK").onPositive((dialog, which) -> triggerStorageAccessFramework()).negativeText("Cancel").show();

        }
    }

    public void checkStoragePermissions(String message, Runnable runnable) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
//            pendingRunnable = runnable;
//            new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Grant SD card permissions").content(message).positiveText("OK").onPositive((dialog, which) -> triggerStorageAccessFramework()).negativeText("Cancel").show();
//
//        } else
            {
            runnable.run();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        getActivity().startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
        if (requestCode == REQUEST_CODE_STORAGE_ACCESS) {
            Uri treeUri;
            if (resultCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = resultData.getData();

                // Persist URI in shared preference so that you can use it later.
                // Use your own framework here instead of PreferenceUtil.
                String uri = String.valueOf(treeUri);
                PrefsManager.Instance().setStoragePermsURI(uri);

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("prefStoragePerms", true);
                editor.apply();

                getActivity().grantUriPermission(getActivity().getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getActivity().getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                if (pendingRunnable != null) {
                    pendingRunnable.run();
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public void onRefresh() {

    }

    public void onListingChanged() {

    }

}

