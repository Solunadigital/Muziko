package com.muziko.controls;

import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.muziko.R;

public class AdMobBanner {

    private final RelativeLayout mLayout;
    private final AdView mAdView;
	private OnAdLoadedListener mListener; //listener field

    public AdMobBanner(RelativeLayout layout, OnAdLoadedListener listener) {
        mLayout = layout;
		mListener = listener;

        mAdView = mLayout.findViewById(R.id.admob);

		load();
	}

	public void resume() {
		mAdView.resume();
	}

	public void pause() {
		mAdView.pause();
	}

	public void stop() {
		mAdView.setEnabled(false);
	}

	private void load() {
		AdRequest adRequest = new AdRequest.Builder().build();
		//adRequest.addTestDevice("B9DC157D59F400D0F283A9180667BC8A");
		//adView.setAdSize(AdSize.SMART_BANNER);
		//adView.setAdUnitId(FillerAds.getBan());
		mAdView.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				super.onAdClosed();
//				if (mListener != null) {
//					mListener.onAdClosed();
//				}

			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				super.onAdFailedToLoad(errorCode);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (mListener != null) {
                    mListener.onAdLoaded();
                }

			}
		});
		mAdView.loadAd(adRequest);

	}

	public interface OnAdLoadedListener {
		void onAdLoaded();

		void onAdClosed();
	}

}
