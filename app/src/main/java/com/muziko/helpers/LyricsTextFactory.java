package com.muziko.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.muziko.R;

import java.util.Hashtable;

public class LyricsTextFactory implements ViewSwitcher.ViewFactory {

	private final Context mContext;

	public LyricsTextFactory(Context context) {
		this.mContext = context;
	}

	@Override
	public View makeView() {
		TextView t = new TextView(mContext);
		t.setGravity(Gravity.CENTER_HORIZONTAL);
		t.setTypeface(FontCache.get("regular", mContext));
		t.setTextColor(ContextCompat.getColor(mContext, R.color.white));
		t.setLineSpacing(mContext.getResources().getDimensionPixelSize(R.dimen.line_spacing), 1);
		setSelectable(t);
		t.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.txt_size));
		return t;
	}

	@SuppressLint("newAPI")
	private void setSelectable(TextView t) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			t.setTextIsSelectable(true);
	}

	public static class FontCache {

		private static Hashtable<String, Typeface> fontCache = new Hashtable<>();

		public static Typeface get(String name, Context context) {
			Typeface tf = fontCache.get(name);
			if (tf == null) {
				try {
					switch (name) {
						case "bold":
                            tf = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
                            break;
						case "thin":
                            tf = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
                            break;
						default:
                            tf = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
                            break;
					}
				} catch (Exception e) {
					return null;
				}
				fontCache.put(name, tf);
			}
			return tf;
		}
	}
}
