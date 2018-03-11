package com.muziko.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;

import io.techery.properratingbar.ProperRatingBar;

/**
 * Created by dev on 17/08/2016.
 */
public class RatingDialog extends Activity {

	private QueueItem queueItem;
	private ProperRatingBar ratingBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		WindowManager.LayoutParams wmlp = getWindow().getAttributes();
		wmlp.gravity = Gravity.CENTER;
		setContentView(R.layout.dialog_rating);

        ratingBar = findViewById(R.id.ratingBar);
        TextView cancel = findViewById(R.id.cancel);
        TextView ok = findViewById(R.id.ok);

		cancel.setOnClickListener(v -> finish());

		ok.setOnClickListener(v -> {

			queueItem.rating = ratingBar.getRating();
			TrackRealmHelper.updateRating(queueItem);
			finish();

		});
		queueItem = (QueueItem) getIntent().getSerializableExtra("item");

		ratingBar.setRating(queueItem.rating);

	}

}