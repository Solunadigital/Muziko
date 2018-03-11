package com.muziko.fragments.Intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.muziko.R;


public class Intro5 extends android.support.v4.app.Fragment {

	public Intro5() {
		// Required empty public constructor
	}

	public static Intro5 newInstance() {
		return new Intro5();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
		}

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_intro, container, false);

        ImageView introIcon = rootView.findViewById(R.id.introIcon);

        TextView introText = rootView.findViewById(R.id.introText);
        TextView introExplain = rootView.findViewById(R.id.introExplain);

		introIcon.setImageResource(R.drawable.intro5);
		introText.setText(R.string.inviteandearn);
		introExplain.setText(R.string.inviteandearn_desc);

		return rootView;
	}


}
