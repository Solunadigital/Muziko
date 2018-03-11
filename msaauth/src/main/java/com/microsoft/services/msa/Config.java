// ------------------------------------------------------------------------------
// Copyright (c) 2014 Microsoft Corporation
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.
// ------------------------------------------------------------------------------

package com.microsoft.services.msa;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Config is a singleton class that contains the values used throughout the SDK.
 */
enum Config {
    INSTANCE;

    private Uri apiUri;
    private String apiVersion;

    Config() {
        // initialize default values for constants
        apiUri = Uri.parse("https://apis.live.net/v5.0");
        apiVersion = "5.0";
    }

    public Uri getApiUri() {
        return apiUri;
    }

    public void setApiUri(Uri apiUri) {
        assert apiUri != null;
        this.apiUri = apiUri;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        if (TextUtils.isEmpty(apiVersion)) throw new AssertionError();
        this.apiVersion = apiVersion;
    }
}
