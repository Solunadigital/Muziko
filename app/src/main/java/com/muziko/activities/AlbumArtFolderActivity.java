package com.muziko.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.GridSpacingItemDecoration;
import com.muziko.interfaces.BasicRecyclerItemListener;
import com.muziko.manager.AppController;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

public class AlbumArtFolderActivity extends BaseActivity implements BasicRecyclerItemListener {

	private final ArrayList<String> imageFiles = new ArrayList<>();
	private File[] listFile;
	private RecyclerView mrecyclerView;
	private ImageAdapter mAdapter;
	private Toolbar toolbar;
	private ActionBar actionBar;
	private QueueItem item;
	private int selectedItemPosition;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_album_art_folder);

		findViewsById();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_blue));
		}

		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		item = (QueueItem) getIntent().getSerializableExtra("item");
		selectedItemPosition = getIntent().getIntExtra("index", 0);
		if (item == null) {
            AppController.toast(this, "Album not found!");
            finish();
			return;
		}
		getFromSdcard(item);

		mAdapter = new ImageAdapter(this, imageFiles, this);
		GridLayoutManager gridlm = new GridLayoutManager(this, 2);
		mrecyclerView.setLayoutManager(gridlm);
		int spanCount = 2; // 3 columns
		int spacing = 30; // 50px
		mrecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
		mrecyclerView.setAdapter(mAdapter);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	private void findViewsById() {
        toolbar = findViewById(R.id.toolbar);
        mrecyclerView = findViewById(R.id.itemList);
    }

	private void getFromSdcard(QueueItem queueItem) {
//		File file = new File(android.os.Environment.getExternalStorageDirectory(), "MapleBear");
		File file = new File(queueItem.folder_path);
		if (file.isDirectory()) {
			listFile = file.listFiles();

			for (File aListFile : listFile) {

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				Bitmap bitmap = BitmapFactory.decodeFile(aListFile.getAbsolutePath(), options);
				if (options.outWidth != -1 && options.outHeight != -1) {
					// This is an image file.
					imageFiles.add(aListFile.getAbsolutePath());
				}
			}
		}

		actionBar.setTitle(imageFiles.size() + " files");
	}


	@Override
	public void onItemClicked(int position) {

		String imageFile = mAdapter.getItem(position);
		Intent returnIntent = new Intent();
		returnIntent.putExtra("item", item);
		returnIntent.putExtra("index", selectedItemPosition);
		returnIntent.putExtra("filepath", imageFile);
		setResult(Activity.RESULT_OK, returnIntent);
		finish();
	}

	private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
		final BasicRecyclerItemListener mListener;
		private final Context mContext;
		private final ArrayList<String> mqueueItemArrayList;
		private byte[] bytearray;
		private Vector converart;
		private Bitmap bm;

		// Provide a suitable constructor (depends on the kind of dataset)
		public ImageAdapter(Context context, ArrayList<String> queueItemArrayList, BasicRecyclerItemListener listener) {
			mContext = context;
			mListener = listener;
			this.mqueueItemArrayList = queueItemArrayList;
		}

		public void add(int position, String item) {
			mqueueItemArrayList.add(position, item);
			notifyItemInserted(position);
		}

		public void remove(String item) {
			int position = mqueueItemArrayList.indexOf(item);
			mqueueItemArrayList.remove(position);
			notifyItemRemoved(position);
		}

		// Create new views (invoked by the layout manager)
		@Override
		public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
		                                                  int viewType) {
            // createActivityListener a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_coverartimage_item, parent, false);
			// set the view's size, margins, paddings and layout parameters
			return new ViewHolder(mContext, v, mListener);
		}

		// Replace the contents of a view (invoked by the layout manager)
		@Override
		public void onBindViewHolder(final ViewHolder holder, int position) {
			// - get element from your dataset at this position
			// - replace the contents of the view with that element
			final String image = mqueueItemArrayList.get(position);

//			holder.textTitle.setText(queueItem);
			holder.textTitle.setVisibility(View.GONE);

			Picasso.with(mContext)
					.load(new File(image))
					.placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
					.resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
					.centerCrop()
					.into(holder.albumIcon);

		}

		// Return the size of your dataset (invoked by the layout manager)
		@Override
		public int getItemCount() {
			return mqueueItemArrayList.size();
		}

		public String getItem(int position) {
			if (position >= 0 && position < mqueueItemArrayList.size())
				return mqueueItemArrayList.get(position);
			else
				return null;
		}

		// Provide a reference to the views for each data item
		// Complex data items may need more than one view per item, and
		// you provide access to all the views for a data item in a view holder
		public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
			// each data item is just a string in this case
			public final ImageView albumIcon;
			public final TextView textTitle;
			final BasicRecyclerItemListener listener;
			private final Context context;

			public ViewHolder(Context context, View view, final BasicRecyclerItemListener listener) {
				super(view);
				this.context = context;
				this.listener = listener;

                albumIcon = view.findViewById(R.id.albumIcon);
                textTitle = view.findViewById(R.id.textTitle);

				view.setOnClickListener(this);
			}

			@Override
			public void onClick(View view) {
				if (listener != null) {
					listener.onItemClicked(getAdapterPosition());
				}
			}
		}

	}
}
