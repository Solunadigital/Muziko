package com.muziko.cloud.Amazon;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.amazon.clouddrive.auth.ApplicationScope;
import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import com.amazon.identity.auth.device.shared.APIListener;
import com.crashlytics.android.Crashlytics;
import com.muziko.callbacks.CloudFolderCallbacks;
import com.muziko.cloud.GenericCloudApi;
import com.muziko.common.models.QueueItem;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.manager.CloudManager;
import com.onedrive.sdk.extensions.IOneDriveClient;

import hugo.weaving.DebugLog;

import static com.muziko.manager.CloudManager.connectedCloudDrives;

/**
 * Created by Bradley on 31/08/2017.
 */

public class AmazonApi extends GenericCloudApi {

    private static final String TAG = AmazonAuthorizationManager.class.getSimpleName();
    // Authorization scopes used for getting information from
    // LWA and Amazon Drive
    private static final String[] APP_AUTHORIZATION_SCOPES = {
            ApplicationScope.CLOUDDRIVE_READ,
            ApplicationScope.CLOUDDRIVE_WRITE,
            "profile"};
    private AmazonAuthorizationManager mAuthManager;
    private AmazonConnectionCallbacks amazonConnectionCallbacks;

    public void getRootFolderItems(QueueItem queueItem, CloudFolderCallbacks cloudFolderCallbacksy) {
    }

    public void getFolderItems(QueueItem queueItem, CloudFolderCallbacks cloudFolderCallbacks) {
    }

    public String newFolder(String folder_path, String s) {
        return null;
    }

    public IOneDriveClient getOneDriveClient() {
        return null;
    }

    public void connect(Activity activity, String accountName, String sharedPrefKey, CloudManager cloudManager) {

    }

    public boolean deleteFile(String folder_path) {
        return false;

    }

    public synchronized void initialize(final Context context, AmazonConnectionCallbacks amazonConnectionCallbacks) {
        this.context = context;
        this.amazonConnectionCallbacks = amazonConnectionCallbacks;
        try {
            mAuthManager = new AmazonAuthorizationManager(context, Bundle.EMPTY);
        } catch (IllegalArgumentException e) {
            Crashlytics.logException(e);
        }
        mAuthManager.authorize(APP_AUTHORIZATION_SCOPES, Bundle.EMPTY, new AuthorizationListener() {
            @Override
            public void onCancel(Bundle bundle) {
                if (amazonConnectionCallbacks != null) {
                    amazonConnectionCallbacks.onAmazonConnectionFailed("Error");
                }
            }

            @Override
            public void onSuccess(Bundle response) {
                mAuthManager.getProfile(new APIListener() {
                    @Override
                    public void onSuccess(Bundle bundle) {
                        Bundle profileBundle = response.getBundle(AuthzConstants.BUNDLE_KEY.PROFILE.val);
                        if (profileBundle == null) {
                            if (amazonConnectionCallbacks != null) {
                                amazonConnectionCallbacks.onAmazonConnectionFailed("Error getting profile");
                            }
                        } else {
                            if (amazonConnectionCallbacks != null) {
                                mConnected = true;
                                accountName = profileBundle.getString(AuthzConstants.PROFILE_KEY.EMAIL.val);
                                cloudAccountId = CloudAccountRealmHelper.insert(accountName, "", CloudManager.AMAZON, false);
                                connectedCloudDrives.add(cloudAccountId);
                                CloudManager.Instance().addAmazonApi(accountName, AmazonApi.this);
                                amazonConnectionCallbacks.onAmazonConnected(cloudAccountId);
                            }
                        }
                    }

                    @Override
                    public void onError(AuthError authError) {
                        if (amazonConnectionCallbacks != null) {
                            amazonConnectionCallbacks.onAmazonConnectionFailed(authError);
                        }
                    }
                });
            }

            @Override
            public void onError(AuthError authError) {
                if (amazonConnectionCallbacks != null) {
                    amazonConnectionCallbacks.onAmazonConnectionFailed(authError);
                }
            }
        });
    }

    @DebugLog
    public interface AmazonConnectionCallbacks {
        void onAmazonConnectionFailed(String message);

        void onAmazonConnectionFailed(Exception ex);

        void onAmazonConnected(int cloudAccountId);
    }
}
