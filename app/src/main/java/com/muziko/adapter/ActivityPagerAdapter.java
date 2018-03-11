package com.muziko.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.fragments.Recent.AddedFragment;
import com.muziko.fragments.Recent.MostFragment;
import com.muziko.fragments.Recent.RecentFragment;

public class ActivityPagerAdapter extends FragmentPagerAdapter {

	// Tab Titles
//	private String tabTitles[] = new String[]{"Recently Played", "Most Played", "Recently Added"};

	private final String[] tabTitles = new String[]{MyApplication.getInstance().getApplicationContext().getString(R.string.recent), MyApplication.getInstance().getApplicationContext().getString(R.string.most), MyApplication.getInstance().getApplicationContext().getString(R.string.new_tab)};

	private RecentFragment fragPlayed = null;
	private MostFragment fragMost = null;
	private AddedFragment fragAdded = null;

	public ActivityPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case MyApplication.ACTIVITY_PAGE_PLAYED:
				if (fragPlayed == null)
					fragPlayed = new RecentFragment();
				return fragPlayed;

			case MyApplication.ACTIVITY_PAGE_MOST:
				if (fragMost == null)
					fragMost = new MostFragment();
				return fragMost;

			case MyApplication.ACTIVITY_PAGE_ADDED:
				if (fragAdded == null)
					fragAdded = new AddedFragment();
				return fragAdded;

		}
		return null;
	}

	@Override
	public int getCount() {
		return tabTitles.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return tabTitles[position];
	}

}
