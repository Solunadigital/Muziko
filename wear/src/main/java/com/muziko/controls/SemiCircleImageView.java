package com.muziko.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.muziko.R;

/**
 * Created by Bradley on 14/03/2017.
 */

public class SemiCircleImageView extends CircleImageView {

    Paint paint;
    RectF rect;

    public SemiCircleImageView(Context context) {
        super(context);
        init();
    }

    public SemiCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SemiCircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        rect = new RectF();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw background circle anyway
        int left = 0;
        int width = getWidth();
        int top = 0;
        rect.set(left, top, left + width, top + width);

        canvas.drawArc(rect, 0, 180, true, paint);
    }
}
