package pl.tajchert.buswear.wear;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import pl.tajchert.buswear.EventBus;

import static pl.tajchert.buswear.wear.WearBusTools.CONNECTION_TIME_OUT_MS;
import static pl.tajchert.buswear.wear.WearBusTools.DATA_CHANGED_BITMAP;
import static pl.tajchert.buswear.wear.WearBusTools.DATA_CHANGED_KEY;

public class EventCatcher extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().equals("/image")) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset profileAsset = dataMapItem.getDataMap().getAsset(DATA_CHANGED_BITMAP);
                String data = dataMapItem.getDataMap().getString(DATA_CHANGED_KEY);
                Bitmap bitmap = loadBitmapFromAsset(profileAsset);
                EventBus.getDefault(getApplicationContext()).postAssetEvent(bitmap, data);
            }
        }
        super.onDataChanged(dataEventBuffer);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        EventBus.getDefault(getApplicationContext()).syncEvent(messageEvent);
        super.onMessageReceived(messageEvent);
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        GoogleApiClient googleApiClient = SendWearManager.getInstance(getApplicationContext());
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        ConnectionResult result = googleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }

        // Convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(googleApiClient, asset).await().getInputStream();
        googleApiClient.disconnect();

        if (assetInputStream == null) {
            return null;
        }

        // Decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }
}
