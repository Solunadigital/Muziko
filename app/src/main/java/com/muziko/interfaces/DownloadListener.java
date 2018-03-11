package com.muziko.interfaces;

import com.muziko.common.models.QueueItem;

/**
 * Created by Bradley on 16/05/2017.
 */

public interface DownloadListener {

    void onDownloadComplete(QueueItem queueItem);

    void onError();
}
