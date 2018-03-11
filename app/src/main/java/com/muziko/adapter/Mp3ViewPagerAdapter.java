package com.muziko.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.fragments.CutTones;
import com.muziko.fragments.SelectMp3;

public class Mp3ViewPagerAdapter extends FragmentPagerAdapter {

	// Tab Titles
    private final String[] tabTitles = new String[]{MyApplication.getInstance().getApplicationContext().getString(R.string.select_mp3s), MyApplication.getInstance().getApplicationContext().getString(R.string.cut_tones)};
    private SelectMp3 fragSelect = null;
	private CutTones fragTones = null;

	public Mp3ViewPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				if (fragSelect == null)
					fragSelect = SelectMp3.newInstance();
				return fragSelect;
			case 1:
				if (fragTones == null)
					fragTones = CutTones.newInstance();
				return fragTones;
		}
		return null;
	}

	@Override
	public int getCount() {
		int PAGE_COUNT = 2;
		return PAGE_COUNT;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return null;
//		return tabTitles[position];
	}

}
