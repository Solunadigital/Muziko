package pl.tajchert.buswear.events;

import android.graphics.Bitmap;

/**
 * Created by Bradley on 10/02/2017.
 */
public class ImageAssetEvent {
    private Bitmap bitmap;
    private String key;

    public ImageAssetEvent(Bitmap bitmap, String key) {
        this.bitmap = bitmap;
        this.key = key;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
