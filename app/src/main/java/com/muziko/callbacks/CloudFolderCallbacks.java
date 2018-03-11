package com.muziko.callbacks;

import com.muziko.common.models.QueueItem;

import java.util.ArrayList;

/**
 * Created by Bradley on 26/04/2017.
 */

public interface CloudFolderCallbacks {

    void onFoldersReturned(ArrayList<QueueItem> items);

    void onFoldersFailed();
}
