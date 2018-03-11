package com.muziko.controls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.muziko.R;

/**
 * The Class CircularSeekBar.
 */
public class CircularSeekBarSmall extends View {

	/**
	 * The context
	 */
	private final Context mContext;
	/**
	 * The color of the progress ring
	 */
	private final Paint circleColor;
	/**
	 * the color of the inside circle. Acts as background color
	 */
	private final Paint innerColor;
	/**
	 * The progress circle ring background
	 */
	private final Paint circleRing;
	/**
	 * The rectangle containing our circles and arcs.
	 */
	private final RectF rect = new RectF();
	/**
	 * The listener to listen for changes
	 */
	private OnSeekChangeListener mListener;
	/**
	 * The angle of progress
	 */
	private int angle = 0;
	/**
	 * The width of the progress ring
	 */
	private int barWidth = 5;
	/**
	 * The maximum progress amount
	 */
	private int maxProgress = 100;
	/**
	 * The current progress
	 */
	private int progress;
	/**
	 * The progress percent
	 */
	private int progressPercent;
	/**
	 * The radius of the inner circle
	 */
	private float innerRadius;
	/**
	 * The radius of the outer circle
	 */
	private float outerRadius;
	/**
	 * The circle's center X coordinate
	 */
	private float cx;
	/**
	 * The circle's center Y coordinate
	 */
	private float cy;
	/**
	 * The X coordinate for the top left corner of the marking drawable
	 */
	private float dx;
	/**
	 * The Y coordinate for the top left corner of the marking drawable
	 */
	private float dy;
	/**
	 * The X coordinate for the current position of the marker, pre adjustment
	 * to center
	 */
	private float markPointX;
	/**
	 * The Y coordinate for the current position of the marker, pre adjustment
	 * to center
	 */
	private float markPointY;
	/**
	 * The adjustment factor. This adds an adjustment of the specified size to
	 * both sides of the progress bar, allowing touch events to be processed
	 * more user friendly (yes, I know that's not a word)
	 */
	private float adjustmentFactor = 100;
	/**
	 * The progress mark when the view isn't being progress modified
	 */
	private Bitmap progressMark;
	/**
	 * The progress mark when the view is being progress modified.
	 */
	private Bitmap progressMarkPressed;
	/**
	 * The flag to see if view is pressed
	 */
	private boolean IS_PRESSED = false;
	/**
	 * The flag to see if the setProgress() method was called from our own
	 * View's setAngle() method, or externally by a user.
	 */
	private boolean CALLED_FROM_ANGLE = false;
	private boolean SHOW_SEEKBAR = true;

	{
		mListener = () -> {

		};

		circleColor = new Paint();
		innerColor = new Paint();
		circleRing = new Paint();

		circleColor.setColor(Color.parseColor("#009cda")); // Set default
		// progress
		// color to holo
		// blue.
		innerColor.setColor(Color.WHITE); // Set default background color to
		// black
		circleRing.setColor(Color.parseColor("#e4e4e4"));// Set default background color to Gray

		circleColor.setAntiAlias(true);
		innerColor.setAntiAlias(true);
		circleRing.setAntiAlias(true);

		circleColor.setStrokeWidth(5);
		innerColor.setStrokeWidth(5);
		circleRing.setStrokeWidth(5);

		circleColor.setStyle(Paint.Style.FILL);
	}

	/**
	 * Instantiates a new circular seek bar.
	 *
	 * @param context  the context
	 * @param attrs    the attrs
	 * @param defStyle the def style
	 */
	public CircularSeekBarSmall(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initDrawable();
	}

	/**
	 * Instantiates a new circular seek bar.
	 *
	 * @param context the context
	 * @param attrs   the attrs
	 */
	public CircularSeekBarSmall(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initDrawable();
	}

	/**
	 * Instantiates a new circular seek bar.
	 *
	 * @param context the context
	 */
	public CircularSeekBarSmall(Context context) {
		super(context);
		mContext = context;
		initDrawable();
	}

	/**
	 * Inits the drawable.
	 */
	private void initDrawable() {
		int iW = 30;
		int iH = 30;
		Bitmap mOriginalBitmap;
		BitmapFactory.Options options = new BitmapFactory.Options();
		//Load image from resource
		mOriginalBitmap = BitmapFactory.decodeResource(getResources(),
				R.mipmap.blue_circle, options);
		//Scale to target size
		mOriginalBitmap = Bitmap.createScaledBitmap(mOriginalBitmap, iW, iH, true);
		progressMark = mOriginalBitmap;

		progressMarkPressed = mOriginalBitmap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		/*
	  The width of the view
	 */
		int width = getWidth();
		/*
	  The height of the view
	 */
		int height = getHeight();

		int size = (width > height) ? height : width; // Choose the smaller
		// between width and
		// height to make a
		// square

		cx = width / 2; // Center X for circle
		cy = height / 2; // Center Y for circle

		outerRadius = size / 2.5f; // Radius of the outer circle
		innerRadius = outerRadius - barWidth; // Radius of the inner circle

		/*
	  The left bound for the circle RectF
	 */
		float left = cx - outerRadius;
		/*
	  The right bound for the circle RectF
	 */
		float right = cx + outerRadius;
		/*
	  The top bound for the circle RectF
	 */
		float top = cy - outerRadius;
		/*
	  The bottom bound for the circle RectF
	 */
		float bottom = cy + outerRadius;

		/*
	  The X coordinate for 12 O'Clock
	 */
		float startPointX = cx;
		/*
	  The Y coordinate for 12 O'Clock
	 */
		float startPointY = cy - outerRadius;
		markPointX = startPointX;// Initial locatino of the marker X coordinate
		markPointY = startPointY;// Initial locatino of the marker Y coordinate

		rect.set(left, top, right, bottom); // assign size to rect
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {


		canvas.drawCircle(cx, cy, outerRadius, circleRing);
		/*
	  The start angle (12 O'clock
	 */
		int startAngle = 270;
		canvas.drawArc(rect, startAngle, angle, true, circleColor);
		canvas.drawCircle(cx, cy, innerRadius, innerColor);
		if (SHOW_SEEKBAR) {
			dx = getXFromAngle();
			dy = getYFromAngle();
			drawMarkerAtProgress(canvas);
		}
		super.onDraw(canvas);
	}

	/**
	 * Draw marker at the current progress point onto the given canvas.
	 *
	 * @param canvas the canvas
	 */
	private void drawMarkerAtProgress(Canvas canvas) {
		if (IS_PRESSED) {
			canvas.drawBitmap(progressMarkPressed, dx, dy, null);
		} else {
			canvas.drawBitmap(progressMark, dx, dy, null);
		}
	}

	/**
	 * Gets the X coordinate of the arc's end arm's point of intersection with
	 * the circle
	 *
	 * @return the X coordinate
	 */
	private float getXFromAngle() {
		int size1 = progressMark.getWidth();
		int size2 = progressMarkPressed.getWidth();
		int adjust = (size1 > size2) ? size1 : size2;
		float x = markPointX - (adjust / 2);
		return x;
	}

	/**
	 * Gets the Y coordinate of the arc's end arm's point of intersection with
	 * the circle
	 *
	 * @return the Y coordinate
	 */
	private float getYFromAngle() {
		int size1 = progressMark.getHeight();
		int size2 = progressMarkPressed.getHeight();
		int adjust = (size1 > size2) ? size1 : size2;
		float y = markPointY - (adjust / 2);
		return y;
	}

	/**
	 * Get the angle.
	 *
	 * @return the angle
	 */
	public int getAngle() {
		return angle;
	}

	/**
	 * Set the angle.
	 *
	 * @param angle the new angle
	 */
	private void setAngle(int angle) {
		this.angle = angle;
		float donePercent = (((float) this.angle) / 360) * 100;
		float progress = (donePercent / 100) * getMaxProgress();
		setProgressPercent(Math.round(donePercent));
		CALLED_FROM_ANGLE = true;
		setProgress(Math.round(progress));
	}

	/**
	 * Gets the seek bar change listener.
	 *
	 * @return the seek bar change listener
	 */
	public OnSeekChangeListener getSeekBarChangeListener() {
		return mListener;
	}

	/**
	 * Sets the seek bar change listener.
	 *
	 * @param listener the new seek bar change listener
	 */
	public void setSeekBarChangeListener(OnSeekChangeListener listener) {
		mListener = listener;
	}

	/**
	 * Gets the bar width.
	 *
	 * @return the bar width
	 */
	public int getBarWidth() {
		return barWidth;
	}

	/**
	 * Sets the bar width.
	 *
	 * @param barWidth the new bar width
	 */
	public void setBarWidth(int barWidth) {
		this.barWidth = barWidth;
	}

	/**
	 * Gets the max progress.
	 *
	 * @return the max progress
	 */
	private int getMaxProgress() {
		return maxProgress;
	}

	/**
	 * Sets the max progress.
	 *
	 * @param maxProgress the new max progress
	 */
	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}

	/**
	 * Gets the progress.
	 *
	 * @return the progress
	 */
	private int getProgress() {
		return progress;
	}

	/**
	 * Sets the progress.
	 *
	 * @param progress the new progress
	 */
	private void setProgress(int progress) {
		if (this.progress != progress) {
			this.progress = progress;
			if (!CALLED_FROM_ANGLE) {
				int newPercent = (this.progress * 100) / this.maxProgress;
				int newAngle = (newPercent * 360) / 100;
				this.setAngle(newAngle);
				this.setProgressPercent(newPercent);
			}
			mListener.onProgressChange();
			CALLED_FROM_ANGLE = false;
		}
	}

	/**
	 * Gets the progress percent.
	 *
	 * @return the progress percent
	 */
	public int getProgressPercent() {
		return progressPercent;
	}

	/**
	 * Sets the progress percent.
	 *
	 * @param progressPercent the new progress percent
	 */
	private void setProgressPercent(int progressPercent) {
		this.progressPercent = progressPercent;
	}

	/**
	 * Sets the ring background color.
	 *
	 * @param color the new ring background color
	 */
	public void setRingBackgroundColor(int color) {
		circleRing.setColor(color);
	}

	/**
	 * Sets the back ground color.
	 *
	 * @param color the new back ground color
	 */
	public void setBackGroundColor(int color) {
		innerColor.setColor(color);
	}

	/**
	 * Sets the progress color.
	 *
	 * @param color the new progress color
	 */
	public void setProgressColor(int color) {
		circleColor.setColor(color);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		boolean up = false;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				moved(x, y, up);
				break;
			case MotionEvent.ACTION_MOVE:
				moved(x, y, up);
				break;
			case MotionEvent.ACTION_UP:
				up = true;
				moved(x, y, up);
				break;
		}
		return true;
	}

	/**
	 * Moved.
	 *
	 * @param x  the x
	 * @param y  the y
	 * @param up the up
	 */
	private void moved(float x, float y, boolean up) {
		float distance = (float) Math.sqrt(Math.pow((x - cx), 2) + Math.pow((y - cy), 2));
		if (distance < outerRadius + adjustmentFactor && distance > innerRadius - adjustmentFactor && !up) {
			IS_PRESSED = true;

			markPointX = (float) (cx + outerRadius * Math.cos(Math.atan2(x - cx, cy - y) - (Math.PI / 2)));
			markPointY = (float) (cy + outerRadius * Math.sin(Math.atan2(x - cx, cy - y) - (Math.PI / 2)));

			float degrees = (float) ((float) ((Math.toDegrees(Math.atan2(x - cx, cy - y)) + 360.0)) % 360.0);
			// and to make it count 0-360
			if (degrees < 0) {
				degrees += 2 * Math.PI;
			}

			setAngle(Math.round(degrees));
			invalidate();

		} else {
			IS_PRESSED = false;
			invalidate();
		}

	}

	/**
	 * Gets the adjustment factor.
	 *
	 * @return the adjustment factor
	 */
	public float getAdjustmentFactor() {
		return adjustmentFactor;
	}

	/**
	 * Sets the adjustment factor.
	 *
	 * @param adjustmentFactor the new adjustment factor
	 */
	public void setAdjustmentFactor(float adjustmentFactor) {
		this.adjustmentFactor = adjustmentFactor;
	}

	/**
	 * To display seekbar
	 */
	public void ShowSeekBar() {
		SHOW_SEEKBAR = true;
	}

	/**
	 * To hide seekbar
	 */
	public void hideSeekBar() {
		SHOW_SEEKBAR = false;
	}

	/**
	 * The listener interface for receiving onSeekChange events. The class that
	 * is interested in processing a onSeekChange event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>setSeekBarChangeListener(OnSeekChangeListener)<code> method. When
	 * the onSeekChange event occurs, that object's appropriate
	 * method is invoked.
	 */
	public interface OnSeekChangeListener {

		/**
		 * On progress change.
		 *  */
		void onProgressChange();
	}
}
