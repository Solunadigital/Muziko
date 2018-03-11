package com.muziko.helpers;


import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class ShapeWear {
    private static int screenWidthPX = 0;
    private static int screenHeightPX = 0;
    private static OnSizeChangeListener onSizeChangeListener;
    private static ScreenShape shape = ScreenShape.UNDETECTED;
    private static OnShapeChangeListener onShapeChangeListener;

    /**
     * Initialized to determine screen shape
     *
     * @param view
     */
    private static void initShapeDetection(View view) {
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            if (insets.isRound()) {
                shape = ScreenShape.ROUND;
                if (screenWidthPX == 320 && screenHeightPX == 290) {
                    shape = ScreenShape.MOTO_ROUND;
                }
            } else {
                shape = ScreenShape.RECTANGLE;
            }
            if (onShapeChangeListener != null) {
                onShapeChangeListener.shapeDetected(getShape());
            }
            return insets;
        });
    }

    /**
     * Initialized at any moment of app life cycle to determine screen shape and size
     *
     * @param activity
     */
    public static void initShapeWear(Activity activity) {
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        getScreenSize(wm);
        initShapeDetection(activity.getWindow().getDecorView().findViewById(android.R.id.content));
    }

    /**
     * Initialized at any moment of app life cycle to determine screen shape and size
     *
     * @param context
     */
    public static void initShapeWear(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        getScreenSize(wm);
        initShapeDetection(((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content));
    }

    private static void getScreenSize(WindowManager wm) {
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidthPX = size.x;
        screenHeightPX = size.y;
        if (onSizeChangeListener != null) {
            onSizeChangeListener.sizeDetected(screenWidthPX, screenHeightPX);
        }
    }

    /**
     * Method used to get most common (for now) parameter, is screen is round or not. Will throw an Exception is it is not detected.
     *
     * @return boolean is screen is round or not
     * @throws ScreenShapeNotDetectedException
     * @deprecated use {@link #getShape()} instead, as it allows to handle all shapes correctly, and Google specifies that more shapes can be introduced in the future.
     */
    @Deprecated
    public static boolean isRound() throws ScreenShapeNotDetectedException {
        if (shape == null || shape.equals(ScreenShape.UNDETECTED)) {
            throw new ScreenShapeNotDetectedException("ShapeWear still doesn't have correct screen shape at this point, subscribe to OnShapeChangeListener or call this method later on. Also you can call getShape() to get String representation, will return SHAPE_UNSURE if not specified.");
        } else return shape.equals(ScreenShape.ROUND);
    }

    /**
     * Safe-proof method, but will return ScreenShape.UNDETECTED instead of throwing Exception.
     *
     * @return String name of screen type
     */
    public static ScreenShape getShape() {
        return shape;
    }

    public static int getScreenWidthPX() {
        return screenWidthPX;
    }

    public static int getScreenHeightPX() {
        return screenHeightPX;
    }

    public static OnShapeChangeListener getOnShapeChangeListener() {
        return onShapeChangeListener;
    }

    public static void setOnShapeChangeListener(OnShapeChangeListener onShapeChangeListener) {
        ShapeWear.onShapeChangeListener = onShapeChangeListener;
        if (!getShape().equals(ScreenShape.UNDETECTED) && ShapeWear.onShapeChangeListener != null) {
            ShapeWear.onShapeChangeListener.shapeDetected(getShape());
        }
    }

    public static void setOnSizeChangeListener(OnSizeChangeListener onSizeChangeListener) {
        ShapeWear.onSizeChangeListener = onSizeChangeListener;
        if (ShapeWear.onSizeChangeListener != null && screenWidthPX != 0 && screenHeightPX != 0) {
            ShapeWear.onSizeChangeListener.sizeDetected(screenWidthPX, screenHeightPX);
        }
    }

    public enum ScreenShape {ROUND, MOTO_ROUND, RECTANGLE, UNDETECTED}

    public interface OnShapeChangeListener {
        void shapeDetected(ScreenShape screenShape);
    }

    public interface OnSizeChangeListener {
        void sizeDetected(int widthPx, int heightPx);
    }

    public static class ScreenShapeNotDetectedException extends Exception {
        public ScreenShapeNotDetectedException() {
        }

        public ScreenShapeNotDetectedException(String detailMessage) {
            super(detailMessage);
        }
    }
}
