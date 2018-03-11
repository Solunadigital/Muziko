package com.muziko.common.events.buswear;

import android.graphics.Bitmap;

/**
 * Created by Bradley on 10/02/2017.
 */
public class BitmapEvent {
    private Bitmap bitmap;

    public BitmapEvent(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

}
