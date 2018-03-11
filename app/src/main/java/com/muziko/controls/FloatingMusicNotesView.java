package com.muziko.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.badoo.mobile.util.WeakHandler;
import com.muziko.R;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;


public class FloatingMusicNotesView extends View {

    private static final long UPDATE_TIME = 30;
    private final UpdateViewRunnable updateViewRunnable = new UpdateViewRunnable();
    private final WeakHandler handler = new WeakHandler();
    private Notes[] mNotes;
    private int mNotesIconHeight;
    private int mNotesViewWidth;
    private int mNotesViewHeight;
    private Drawable[] images;
    private Random mRandom;
    private int[] myImageList;
    private float mFallSpeed;
    private int mNotesCount;
    private FallNotesState mFallNotesState;

    public FloatingMusicNotesView(Context context) {
        this(context, null);
    }


    public FloatingMusicNotesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingMusicNotesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
        initRandom();
    }

    private static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public FloatingMusicNotesView setImages(int[] imageArray) {
        myImageList = imageArray;
        if (myImageList != null) {
            images = new Drawable[myImageList.length];
            for (int i = 0; i < myImageList.length; i++) {
                images[i] = resize(ContextCompat.getDrawable(getContext(), myImageList[i]));
            }
        }
        return this;
    }

    public void start() {

        this.postDelayed(() -> {
            mFallNotesState = FallNotesState.START;

            mNotes = new Notes[mNotesCount];
            for (int i = 0; i < mNotes.length; i++) {
                mNotes[i] = new Notes();
            }

            if (mFallNotesState == FallNotesState.START) {
                handler.postDelayed(updateViewRunnable, UPDATE_TIME);
                mFallNotesState = FallNotesState.RUNNING;

            }
        }, 1000);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FloatingMusicNotesView);
        mFallSpeed = typedArray.getFloat(R.styleable.FloatingMusicNotesView_fallSpeed, (float) 0.1);
        mNotesCount = typedArray.getInteger(R.styleable.FloatingMusicNotesView_fallCount, 40);
        typedArray.recycle();
        checkAttrs();
    }

    private void checkAttrs() {
        if (mNotesCount <= 0) {
            throw new RuntimeException("notes count must be > 0");
        }

//        mFallSpeed = mFallSpeed * -1;
        if (mFallSpeed <= 0) {
            throw new RuntimeException("fall speed must be > 0");
        }
    }

    private void initRandom() {
        mRandom = new Random();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mNotes == null) {
            return;
        }

        for (Notes notes : mNotes) {
            canvas.save();
            notes.mNotesIconDrawable.setBounds((int) notes.X, (int) notes.Y, (int) (notes.X + mNotesIconHeight * notes.scale), (int) (notes.Y + mNotesIconHeight * notes.scale));
            notes.mNotesIconDrawable.setAlpha(notes.alpha);
            notes.mNotesIconDrawable.draw(canvas);
            canvas.restore();
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mNotesViewWidth = r - l;
        mNotesViewHeight = b - t;
    }

    private void calculateNotesNextAttr() {
        for (Notes notes : mNotes) {
            notes.calculateNextStep();
        }
    }

    /**
     * Resize every drawable to same size, else the size wil flicker randomly
     */
    private Drawable resize(Drawable image) {
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 96, 96, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }

    public void startNotesFall() {
        this.postDelayed(() -> {
            mFallNotesState = FallNotesState.START;
            mNotes = new Notes[mNotesCount];
            for (int i = 0; i < mNotes.length; i++) {
                mNotes[i] = new Notes();
            }

            if (mFallNotesState == FallNotesState.START) {
                handler.removeCallbacksAndMessages(updateViewRunnable);
                handler.postDelayed(updateViewRunnable, UPDATE_TIME);
                mFallNotesState = FallNotesState.RUNNING;

            }
        }, 1000);

    }

    //Use this to Pause the Animation
    public void pauseNotesFall() {
        if (mFallNotesState == FallNotesState.RUNNING) {
            mFallNotesState = FallNotesState.PAUSE;
        }
    }

    //Use this to Resume the Animation
    public void resumeNotesFall() {
        if (mFallNotesState == FallNotesState.PAUSE) {
            mFallNotesState = FallNotesState.RUNNING;
        }
    }


    //Stop the animation
    public void stopNotesFall() {
        mFallNotesState = FallNotesState.STOP;
        updateViewRunnable.stop();
        handler.removeCallbacksAndMessages(updateViewRunnable);
    }

    private enum FallNotesState {START, PAUSE, RUNNING, STOP}

    private class Notes {
        public float scale;
        public int alpha;
        public float X;
        public float Y;
        private Drawable mNotesIconDrawable;

        public Notes() {
            init();
        }

        private void init() {

            if (myImageList == null) {
                myImageList = new int[]{R.drawable.note1, R.drawable.note2};
                images = new Drawable[myImageList.length];

                for (int i = 0; i < myImageList.length; i++) {

                    images[i] = resize(ContextCompat.getDrawable(getContext(), myImageList[i]));
                }

            }

            alpha = mRandom.nextInt(200) + 55; //get a random Aplha 55~255
//            scale = (mRandom.nextFloat() + 1) / 2;  //ran
            scale = (mRandom.nextFloat() + 1) / 2;  /// dom Sizes 0.5~1.5
            X = mRandom.nextInt(mNotesViewWidth);
//            Y = randInt(-mNotesIconHeight-2000,0);

            Y = randInt(mNotesViewHeight, mNotesViewHeight + 2000);

            if (images.length == 1) {
                mNotesIconDrawable = images[0];
            } else if (images.length == 2) {
                if (randInt(1, 100) % 2 == 0) {
                    mNotesIconDrawable = images[0];

                } else
                    mNotesIconDrawable = images[1];
            } else {
                mNotesIconDrawable = images[randInt(0, images.length - 1)];
            }
            mNotesIconHeight = mNotesIconDrawable.getIntrinsicHeight();
        }

        public void calculateNextStep() {
            if (Y < 0) {
                init();
            } else {
                Y = Y + scale * mNotesIconHeight * -mFallSpeed;

            }

//            if (Y > mNotesViewHeight / scale) {
//                init();
//            } else {
//                Y = Y + scale * mNotesIconHeight * mFallSpeed;
//
//            }
        }

        public void fadeAlpha(int alpha) {
            //for now not used, I was planning something else previously
            alpha--;
        }
    }

    private class UpdateViewRunnable implements Runnable {

        private final AtomicBoolean stop = new AtomicBoolean(false);

        public void stop() {
            stop.set(true);
        }

        public void run() {

            if (!stop.get()) {
                switch (mFallNotesState) {
                    case RUNNING:
                        calculateNotesNextAttr();
                        postInvalidate();
                        break;
                }

                invalidate();

                postDelayed(this, UPDATE_TIME);
            }
        }
    }
}
