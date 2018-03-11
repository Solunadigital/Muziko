package com.muziko.models;

import android.support.annotation.Keep;

import com.muziko.fragments.BaseFragment;

@Keep
public class FragmentModel {

    private String title;

    private int position;

    private BaseFragment fragment;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public BaseFragment getFragment() {
        return fragment;
    }

    public void setFragment(BaseFragment fragment) {
        this.fragment = fragment;
    }
}
