package com.muziko.controls;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.preference.PreferenceManager;
import android.util.Log;

import java.math.BigDecimal;

/**
 * Created by dev on 31/08/2016.
 */
class DirectionShaker implements SensorEventListener {

	private static final String TAG = "DirectionShaker";
	private final float[] gravity = {1, 2, 3};
	private final float[] linear_acceleration = {1, 2, 3};
	private final float[] previous_linear_acceleration = {1, 2, 3};
	private final Context mContext;
	private long lastShake;
	private int lastShakeDirection;
	private boolean updating = false;
	private float mLastX, mLastY, mLastZ;
	private SensorManager mSensorManager;
	private OnShakeListener mShakeListener;

	public DirectionShaker(Context context) {
		mContext = context;
		resume();
	}

	public static float round(float d, int decimalPlace) {
		return BigDecimal.valueOf(d).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).floatValue();
	}

	public void setOnShakeListener(OnShakeListener listener) {
		mShakeListener = listener;
	}

	private void resume() {
		boolean mInitialized = false;
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//		mSensorManager.registerListener(this, mAccelerometer, 500000);
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
		lastShake = System.currentTimeMillis();

	}

	public void pause() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
			mSensorManager = null;
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean prefShake = prefs.getBoolean("prefShake", false);
		int prefShakeThreshold = prefs.getInt("prefShakeThreshold", 16);

		if (prefShake && !updating) {

			updating = true;

			float NOISE = 0;
			switch (prefShakeThreshold) {
				case 0:
					NOISE = 14;
					break;
				case 1:
					NOISE = 8;
					break;
				case 2:
					NOISE = 2;
					break;
			}

			final float alpha = (float) 0.8;

			// Isolate the force of gravity with the low-pass filter.


			gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

			// Remove the gravity contribution with the high-pass filter.

			linear_acceleration[0] = event.values[0] - gravity[0];
			linear_acceleration[1] = event.values[1] - gravity[1];
			linear_acceleration[2] = event.values[2] - gravity[2];

			float x1 = linear_acceleration[0];
			float y1 = linear_acceleration[1];
			float z1 = linear_acceleration[2];

			float prev_x1 = previous_linear_acceleration[0];
			float prev_y1 = previous_linear_acceleration[1];
			float prev_z1 = previous_linear_acceleration[2];

			long actualTime = System.currentTimeMillis();
			long diffTime = (actualTime - lastShake);

			float MAXNOISE = (float) 40;
			int shaketimeoutChange = 1000;
			if (y1 < -2 && x1 > NOISE && x1 < MAXNOISE && x1 > prev_x1 && prev_x1 > 0)//y down, x up = left tilt
			{
				if (lastShakeDirection == 1 && diffTime > shaketimeoutChange || lastShakeDirection == 0) {
					if (mShakeListener != null) {
						mShakeListener.onLeftShake();
						Log.i(TAG, "Left " + String.valueOf(x1) + " lastShakeDirection = " + lastShakeDirection + " Difftime = " + diffTime);
//						Utils.toast(mContext, "Left " + String.valueOf(x1) + " lastShakeDirection = " + lastShakeDirection + " Difftime = " + diffTime);
					}
				}
				lastShakeDirection = 0;
			} else if (y1 < -2 && x1 < -NOISE && x1 > -MAXNOISE && x1 < prev_x1 && prev_x1 < 0)//y down, x down = right tilt
			{
				if (lastShakeDirection == 0 && diffTime > shaketimeoutChange || lastShakeDirection == 1) {
					if (mShakeListener != null) {
						mShakeListener.onRightShake();
						Log.i(TAG, "Right " + String.valueOf(x1) + " lastShakeDirection = " + lastShakeDirection + " Difftime = " + diffTime);
//						Utils.toast(mContext, "Right " + String.valueOf(x1) + " lastShakeDirection = " + lastShakeDirection + " Difftime = " + diffTime);
					}
				}
				lastShakeDirection = 1;
			}

			lastShake = actualTime;
			previous_linear_acceleration[0] = linear_acceleration[0];
			previous_linear_acceleration[1] = linear_acceleration[1];
			previous_linear_acceleration[2] = linear_acceleration[2];

			updating = false;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public interface OnShakeListener {
		void onLeftShake();

		void onRightShake();
	}
}
