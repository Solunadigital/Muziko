package pl.tajchert.buswear.wear;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

import static pl.tajchert.buswear.wear.WearBusTools.DATA_CHANGED_BITMAP;
import static pl.tajchert.buswear.wear.WearBusTools.DATA_CHANGED_CLASS;
import static pl.tajchert.buswear.wear.WearBusTools.DATA_CHANGED_KEY;
import static pl.tajchert.buswear.wear.WearBusTools.DATA_CHANGED_TIME;

public class SendAssetToNode extends Thread {

    private final Context context;
    private final Asset asset;
    private final String key;
    private final boolean sticky;
    private final Class clazzToSend;

    /**
     * Internal BusWear method, using it outside of library is possible but not supported or tested
     */
    public SendAssetToNode(Asset asset, String key, Class classToSend, Context ctx, boolean isSticky) {
        this.asset = asset;
        this.key = key;
        this.clazzToSend = classToSend;
        sticky = isSticky;
        context = ctx;
    }

    public void run() {
        GoogleApiClient googleApiClient = SendWearManager.getInstance(context);
        googleApiClient.blockingConnect(WearBusTools.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/image").setUrgent();
        putDataMapRequest.getDataMap().putAsset(DATA_CHANGED_BITMAP, asset);
        putDataMapRequest.getDataMap().putLong(DATA_CHANGED_TIME, System.currentTimeMillis());
        putDataMapRequest.getDataMap().putString(DATA_CHANGED_KEY, key);
        putDataMapRequest.getDataMap().putString(DATA_CHANGED_CLASS, clazzToSend.getName());
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();

        for (Node node : nodes.getNodes()) {
            DataApi.DataItemResult result;
            result = Wearable.DataApi.putDataItem(googleApiClient, putDataRequest).await();
            if (!result.getStatus().isSuccess()) {
                Log.v(WearBusTools.BUSWEAR_TAG, "ERROR: failed to send Message via Google Play Services to node " + node.getDisplayName());
            }
        }
    }
}