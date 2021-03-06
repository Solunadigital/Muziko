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
import com.onedrive.sdk.extensions.Thumbnail;
import com.onedrive.sdk.http.IHttpRequest;

// **NOTE** This file was generated by a tool and any changes will be overwritten.

/**
 * The interface for the Base Thumbnail Request.
 */
public interface IBaseThumbnailRequest extends IHttpRequest {

    void get(final ICallback<Thumbnail> callback);

    Thumbnail get() throws ClientException;

    /**
     * @deprecated As of release 1.1.3, replaced by {@link #patch(Thumbnail, ICallback)}
     */
    @Deprecated
    void update(final Thumbnail sourceThumbnail, final ICallback<Thumbnail> callback);

    /**
     * @deprecated As of release 1.1.3, replaced by {@link #patch(Thumbnail)}
     */
    @Deprecated
    Thumbnail update(final Thumbnail sourceThumbnail) throws ClientException;

    void patch(final Thumbnail sourceThumbnail, final ICallback<Thumbnail> callback);

    Thumbnail patch(final Thumbnail sourceThumbnail) throws ClientException;

    void delete(final ICallback<Void> callback);

    void delete() throws ClientException;

    /**
     * @deprecated As of release 1.1.3, replaced by {@link #post(Thumbnail, ICallback)}
     */
    @Deprecated
    void create(final Thumbnail newThumbnail, final ICallback<Thumbnail> callback);

    /**
     * @deprecated As of release 1.1.3, replaced by {@link #post(Thumbnail)}
     */
    @Deprecated
    Thumbnail create(final Thumbnail newThumbnail) throws ClientException;

    void post(final Thumbnail newThumbnail, final ICallback<Thumbnail> callback);

    Thumbnail post(final Thumbnail newThumbnail) throws ClientException;

    IBaseThumbnailRequest select(final String value);

    IBaseThumbnailRequest top(final int value);

    IBaseThumbnailRequest expand(final String value);
}
