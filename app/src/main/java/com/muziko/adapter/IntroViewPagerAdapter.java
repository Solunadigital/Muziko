package com.muziko.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.muziko.fragments.Intro.Intro1;
import com.muziko.fragments.Intro.Intro2;
import com.muziko.fragments.Intro.Intro3;
import com.muziko.fragments.Intro.Intro4;
import com.muziko.fragments.Intro.Intro5;


public class IntroViewPagerAdapter extends FragmentPagerAdapter {

	// Tab Titles
	private final String[] tabTitles = new String[]{"splash1", "splash2", "splash3", "splash4", "splash5"};

	public IntroViewPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return Intro1.newInstance();
			case 1:
				return Intro2.newInstance();
			case 2:
				return Intro3.newInstance();
			case 3:
				return Intro4.newInstance();
			case 4:
				return Intro5.newInstance();
		}
		return null;
	}

	@Override
	public int getCount() {
		int PAGE_COUNT = 5;
		return PAGE_COUNT;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return tabTitles[position];
	}

}
