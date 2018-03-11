package com.muziko.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andexert.library.RippleView;
import com.muziko.R;
import com.muziko.adapters.IconicItemClickAdapter;
import com.muziko.adapters.IconicNavigationAdapter;
import com.muziko.animation.AnimationsUtil;
import com.muziko.common.CommonConstants;
import com.muziko.helpers.Utils;
import com.muziko.helpers.WearUtils;
import com.sababado.circularview.CircularView;
import com.sababado.circularview.Marker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;
import static com.muziko.MuzikoWearApp.wearAction;

public class MenuOneActivity extends Activity {

    public static final int DURATION = 600;
    private CircularView circularView;
    private RippleView ripple;

    private Animator.AnimatorListener defaultAnimListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            circularView.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu_one);

        findViewsById();

        IconicNavigationAdapter<NavigationItem> mAdapter = new IconicNavigationAdapter(this, getNavigationItems());
        circularView.setAdapter(mAdapter);
        circularView.setOnCircularViewObjectClickListener(new IconicItemClickAdapter<NavigationItem>() {
            @Override
            public void onItemClick(CircularView cView, NavigationItem item, Marker marker) {
                wearAction = item.action;
                if (item.action.equals("Back")) {
                    finish();
                } else {
                    Intent intent = new Intent(MenuOneActivity.this, MenuConfirmActivity.class);
                    intent.putExtra("message", item.message);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // This handler is used for smooth start animation
        Handler myHandler = new Handler();
        myHandler.postDelayed(this::startAnimation, 600);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAnimation();
    }

    private void findViewsById() {
        circularView = (CircularView) findViewById(R.id.circular_view);
        ripple = (RippleView) findViewById(R.id.ripple);
    }

    private void updateRippleColor(int color) {
        try {
            Field field = RippleView.class.getDeclaredField("rippleColor");
            field.setAccessible(true);
            field.setInt(ripple, color);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startAnimation() {
        if (circularView != null && circularView.getVisibility() != View.VISIBLE) {
            AnimationsUtil.animateScaleIn(circularView, DURATION, defaultAnimListener);
            AnimationsUtil.animateSpinIn(circularView, DURATION, null);
        }
    }

    public List<IconicNavigationAdapter.DrawableItem> getNavigationItems() {
        List<IconicNavigationAdapter.DrawableItem> result = new ArrayList<>();
        result.add(new NavigationItem(R.drawable.delete_white, "Delete", R.color.white, CommonConstants.ACTION_WEAR_DELETE, "Remove from queue"));
        result.add(new NavigationItem(R.drawable.back, "Back", R.color.white, "Back", ""));
        result.add(new NavigationItem(R.drawable.queue, "Add to queue", R.color.white, CommonConstants.ACTION_WEAR_ADD_TO_QUEUE, "Add to queue"));
        result.add(new NavigationItem(R.drawable.play_next, "Play next", R.color.white, CommonConstants.ACTION_WEAR_PLAY_NEXT, "Play next"));
        return result;
    }

    private class NavigationItem implements IconicNavigationAdapter.DrawableItem {
        private final int image;
        private final String name;
        private final int colorRes;
        private final String action;
        private final String message;

        public NavigationItem(int image, String name, int colorRes, String action, String message) {
            this.image = image;
            this.name = name;
            this.colorRes = colorRes;
            this.action = action;
            this.message = message;
        }

        public int getColor() {
            return colorRes;
        }

        public int getId() {
            return id;
        }

        @Override
        public Drawable getDrawable(Context context) {
            // I prefer iconic fonts instead of images.

            View view = LayoutInflater.from(context).inflate(R.layout.menu_item, null, false);

            ImageView menuImage = (ImageView) view.findViewById(R.id.menuImage);
            TextView menuText = (TextView) view.findViewById(R.id.menuText);

            menuImage.setImageResource(image);
            menuText.setText(name);

            view.measure(View.MeasureSpec.makeMeasureSpec(Utils.convertDpToPixel(context, 300), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(Utils.convertDpToPixel(context, 300), View.MeasureSpec.EXACTLY));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

            Bitmap bitmap = WearUtils.getChartBitmap(view);

            Drawable drawable = new BitmapDrawable(getResources(), bitmap);

            return drawable;


//            return new PrintDrawable.Builder(context)
//                    .iconText(iconicText)
//                    .iconColor(colorRes)
//                    .iconSize(R.dimen.icon_font_size)
//                    .iconFont("fonts/material-icon-font.ttf")
//                    .build();
        }
    }
}
