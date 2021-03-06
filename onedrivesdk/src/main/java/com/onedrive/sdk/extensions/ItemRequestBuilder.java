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

import com.onedrive.sdk.generated.BaseItemRequestBuilder;
import com.onedrive.sdk.options.Option;

import java.util.List;

// This file is available for extending, afterwards please submit a pull request.

/**
 * The class for the Item Request Builder.
 */
public class ItemRequestBuilder extends BaseItemRequestBuilder implements IItemRequestBuilder {

    /**
     * The request builder for the Item
     *
     * @param requestUrl The request url
     * @param client     The service client
     * @param options    The options for this request
     */
    public ItemRequestBuilder(final String requestUrl, final IOneDriveClient client, final List<Option> options) {
        super(requestUrl, client, options);
    }

    /**
     * Gets the item request builder for the specified item path.
     *
     * @param path The path to the item.
     * @return The request builder for the specified item.
     */
    public IItemRequestBuilder getItemWithPath(final String path) {
        return new ItemRequestBuilder(getRequestUrl() + ":/" + path + ":", getClient(), null);
    }
}
