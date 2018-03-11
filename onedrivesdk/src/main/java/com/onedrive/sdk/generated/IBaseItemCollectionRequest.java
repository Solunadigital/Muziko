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

package com.onedrive.sdk.generated;

import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.IItemCollectionPage;
import com.onedrive.sdk.extensions.IItemCollectionRequest;
import com.onedrive.sdk.extensions.Item;

// **NOTE** This file was generated by a tool and any changes will be overwritten.

/**
 * The interface for the Base Item Collection Request.
 */
public interface IBaseItemCollectionRequest {

    void get(final ICallback<IItemCollectionPage> callback);

    IItemCollectionPage get() throws ClientException;

    /**
     * @deprecated As of release 1.1.3, replaced by {@link #post(Item, ICallback)}
     */
    @Deprecated
    void create(final Item newItem, final ICallback<Item> callback);

    /**
     * @deprecated As of release 1.1.3, replaced by {@link #post(Item)}
     */
    @Deprecated
    Item create(final Item newItem) throws ClientException;

    void post(final Item newItem, final ICallback<Item> callback);

    Item post(final Item newItem) throws ClientException;

    IItemCollectionRequest expand(final String value);

    IItemCollectionRequest select(final String value);

    IItemCollectionRequest top(final int value);
}