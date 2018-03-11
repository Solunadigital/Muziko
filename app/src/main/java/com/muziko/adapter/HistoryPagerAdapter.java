package com.muziko.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.muziko.MyApplication;
import com.muziko.fragments.History.ReceivedFragment;
import com.muziko.fragments.History.SentFragment;
import com.muziko.fragments.History.UnknownFragment;

public class HistoryPagerAdapter extends FragmentPagerAdapter {

	// Tab Titles
	private final String[] tabTitles = new String[]{"Sent", "Received", "Unknown"};

	private ReceivedFragment fragReceived = null;
	private SentFragment fragSent = null;
	private UnknownFragment fragUnknown = null;

	public HistoryPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case MyApplication.SHARING_RECEIVED:
				if (fragReceived == null)
					fragReceived = new ReceivedFragment();
				return fragReceived;

			case MyApplication.SHARING_SENT:
				if (fragSent == null)
					fragSent = new SentFragment();
				return fragSent;

			case MyApplication.SHARING_UNKNOWN:
				if (fragUnknown == null)
					fragUnknown = new UnknownFragment();
				return fragUnknown;
		}
		return null;
	}

	@Override
	public int getCount() {
		return tabTitles.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return "";
//		return tabTitles[position];
	}

}
