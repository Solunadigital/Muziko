// ------------------------------------------------------------------------------
// Copyright (c) 2015 Microsoft Corporation
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
// ------------------------------------------------------------------------------

package com.onedrive.sdk.extensions;

import android.app.Activity;

import com.onedrive.sdk.authentication.ClientAuthenticatorException;
import com.onedrive.sdk.authentication.IAccountInfo;
import com.onedrive.sdk.authentication.IAuthenticator;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.concurrency.IExecutors;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.core.IClientConfig;
import com.onedrive.sdk.core.OneDriveErrorCodes;
import com.onedrive.sdk.generated.BaseOneDriveClient;
import com.onedrive.sdk.http.IHttpProvider;
import com.onedrive.sdk.logger.ILogger;
import com.onedrive.sdk.serializer.ISerializer;

// This file is available for extending, afterwards please submit a pull request.

/**
 * The class for the One Drive Client.
 */
public class OneDriveClient extends BaseOneDriveClient implements IOneDriveClient {

    /**
     * Restricted constructor
     */
    protected OneDriveClient() {
    }

    /**
     * Gets a request builder for the default drive
     *
     * @return The request builder
     */
    @Override
    public IDriveRequestBuilder getDrive() {
        return new DriveRequestBuilder(getServiceRoot() + "/drive", this, null);
    }

    /**
     * The builder for this OneDriveClient
     */
    public static class Builder {

        /**
         * The client under construction
         */
        private final OneDriveClient mClient = new OneDriveClient();

        /**
         * Sets the serializer
         *
         * @param serializer The serializer
         * @return the instance of this builder
         */
        public Builder serializer(final ISerializer serializer) {
            mClient.setSerializer(serializer);
            return this;
        }

        /**
         * Sets the httpProvider
         *
         * @param httpProvider The httpProvider
         * @return the instance of this builder
         */
        public Builder httpProvider(final IHttpProvider httpProvider) {
            mClient.setHttpProvider(httpProvider);
            return this;
        }

        /**
         * Sets the authenticator
         *
         * @param authenticator The authenticator
         * @return the instance of this builder
         */
        public Builder authenticator(final IAuthenticator authenticator) {
            mClient.setAuthenticator(authenticator);
            return this;
        }

        /**
         * Sets the executors
         *
         * @param executors The executors
         * @return the instance of this builder
         */
        public Builder executors(final IExecutors executors) {
            mClient.setExecutors(executors);
            return this;
        }

        /**
         * Sets the logger
         *
         * @param logger The logger
         * @return the instance of this builder
         */
        private Builder logger(final ILogger logger) {
            mClient.setLogger(logger);
            return this;
        }

        /**
         * Set this builder based on the client configuration
         *
         * @param clientConfig The client configuration
         * @return the instance of this builder
         */
        public Builder fromConfig(final IClientConfig clientConfig) {
            return this.authenticator(clientConfig.getAuthenticator())
                    .executors(clientConfig.getExecutors())
                    .httpProvider(clientConfig.getHttpProvider())
                    .logger(clientConfig.getLogger())
                    .serializer(clientConfig.getSerializer());
        }

        /**
         * Login a user and then returns the OneDriveClient asynchronously
         *
         * @param activity The activity the UI should be from
         * @param callback The callback when the client has been built
         */
        public void loginAndBuildClient(final Activity activity, final ICallback<IOneDriveClient> callback) {
            mClient.validate();

            mClient.getExecutors().performOnBackground(new Runnable() {
                @Override
                public void run() {
                    final IExecutors executors = mClient.getExecutors();
                    try {
                        executors.performOnForeground(loginAndBuildClient(activity), callback);
                    } catch (final ClientException e) {
                        executors.performOnForeground(e, callback);
                    }
                }
            });
        }

        /**
         * Login a user and then returns the OneDriveClient
         *
         * @param activity The activity the UI should be from
         * @throws ClientException if there was an exception creating the client
         */
        private IOneDriveClient loginAndBuildClient(final Activity activity) throws ClientException {
            mClient.validate();

            mClient.getAuthenticator()
                    .init(mClient.getExecutors(), mClient.getHttpProvider(), activity, mClient.getLogger());

            IAccountInfo silentAccountInfo = null;
            try {
                silentAccountInfo = mClient.getAuthenticator().loginSilent();
            } catch (final Exception ignored) {
            }

            if (silentAccountInfo == null
                    && mClient.getAuthenticator().login(null) == null) {
                throw new ClientAuthenticatorException("Unable to authenticate silently or interactively",
                        OneDriveErrorCodes.AuthenticationFailure);
            }

            return mClient;
        }

        /**
         * Login a user and then returns the OneDriveClient asynchronously
         *
         * @param activity The activity the UI should be from
         * @param callback The callback when the client has been built
         */
        public void loginExistingAndBuildClient(final Activity activity, final String email, final ICallback<IOneDriveClient> callback) {
            mClient.validate();

            mClient.getExecutors().performOnBackground(new Runnable() {
                @Override
                public void run() {
                    final IExecutors executors = mClient.getExecutors();
                    try {
                        executors.performOnForeground(loginExistingAndBuildClient(activity, email), callback);
                    } catch (final ClientException e) {
                        executors.performOnForeground(e, callback);
                    }
                }
            });
        }

        /**
         * Login a user and then returns the OneDriveClient
         *
         * @param activity The activity the UI should be from
         * @throws ClientException if there was an exception creating the client
         */
        private IOneDriveClient loginExistingAndBuildClient(final Activity activity, String email) throws ClientException {
            mClient.validate();

            mClient.getAuthenticator().init(mClient.getExecutors(), mClient.getHttpProvider(), activity, mClient.getLogger());

            IAccountInfo silentAccountInfo = null;
            try {
                silentAccountInfo = mClient.getAuthenticator().loginSilent();
            } catch (final Exception ignored) {
            }

            if (silentAccountInfo == null
                    && mClient.getAuthenticator().login(email) == null) {
                throw new ClientAuthenticatorException("Unable to authenticate silently or interactively",
                        OneDriveErrorCodes.AuthenticationFailure);
            }

            return mClient;
        }
    }
}
