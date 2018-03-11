package com.muziko.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
//import android.support.v7.preference.PreferenceManager;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.GridSpacingItemDecoration;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.StorageUtils;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.BasicRecyclerItemListener;
import com.muziko.manager.AppController;

import org.apache.commons.io.FilenameUtils;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AlbumArtID3Activity extends BaseActivity implements BasicRecyclerItemListener {

	private final ArrayList<MusicMetadataSet> multiTagMetaDataSetList = new ArrayList<>();
	private final ArrayList<MusicMetadata> multiTagMetaDataList = new ArrayList<>();
	private final ArrayList<QueueItem> coverartList = new ArrayList<>();
    private final ArrayList<MusicMetadata> musicMetadataList = new ArrayList<>();
    private final ArrayList<byte[]> bitmapList = new ArrayList<>();
	private final MyID3 id3 = new MyID3();
	private RecyclerView mrecyclerView;
	private ImageAdapter mAdapter;
	private Toolbar toolbar;
	private ActionBar actionBar;
	private int selectedItemPosition;
    private MusicMetadataSet dataset;
    private MusicMetadata metadata;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_album_art_id3);

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

        QueueItem item = (QueueItem) getIntent().getSerializableExtra("item");
        selectedItemPosition = getIntent().getIntExtra("index", 0);
        if (item == null) {
            AppController.toast(this, "Album not found!");
            finish();
            return;
        }

        coverartList.addAll(TrackRealmHelper.getTracksForAlbum(item.album_name));
        mAdapter = new ImageAdapter(this, musicMetadataList, this);
        GridLayoutManager gridlm = new GridLayoutManager(this, 2);
        mrecyclerView.setLayoutManager(gridlm);
        int spanCount = 2; // 3 columns
        int spacing = 30; // 50px
        mrecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        mrecyclerView.setAdapter(mAdapter);

        List<StorageUtils.StorageInfo> storageInfoList = StorageUtils.getStorageList();
        LoadRunnable loadRunnable = new LoadRunnable();
        if (storageInfoList.size() > 1) {
            checkStoragePermissions("Do you want to grant SD card permissions so tags can be changed?", loadRunnable);
        } else {
            loadRunnable.run();
        }
    }

    private void InvalidFileExtensionError(String extension) {
        new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).positiveColorRes(R.color.normal_blue).title("Invalid file format")
                .content("This is a ." + extension + " file. Only mp3 files are supported.").positiveText("OK").onPositive((dialog, which) -> {

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("prefStoragePerms", false);
            editor.apply();
            finish();
        }).show();

    }

    private void GeneralTagReadError() {
        new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).positiveColorRes(R.color.normal_blue).title("Error Reading Tags")
                .content("Unable to read tags from this track").positiveText("OK").onPositive((dialog, which) -> {

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("prefStoragePerms", false);
            editor.apply();
            finish();
        }).show();

    }

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public void onResume() {
		super.onResume();

		multiTagMetaDataSetList.clear();
		bitmapList.clear();

		ArrayList<QueueItem> removelist = new ArrayList<>();

//		for (int i = 0; i < coverartList.size(); i++) {
//
//			QueueItem queueItem = coverartList.get(i);
//			MusicMetadataSet itemDataset = readData(queueItem);
//
//			if (itemDataset == null) {
//                AppController.toast(this, "Unable to read song metadata from " + queueItem.data);
//                removelist.add(queueItem);
//				continue;
//			}
//
//			MusicMetadata itemMetadata = (MusicMetadata) itemDataset.getSimplified();
//
//			if (itemMetadata == null) {
//                AppController.toast(this, "Unable to read song metadata from " + queueItem.data);
//                removelist.add(queueItem);
//				continue;
//			}
//
//			multiTagMetaDataSetList.add(itemDataset);
//			multiTagMetaDataList.add(itemMetadata);
//
//		}
//
//		actionBar.setTitle(coverartList.size() + " tracks");
//
//		mAdapter.notifyDataSetChanged();

    }

    private void findViewsById() {
        toolbar = findViewById(R.id.toolbar);
        mrecyclerView = findViewById(R.id.itemList);
    }

    private MusicMetadataSet readData(QueueItem queueItem) {
        MusicMetadataSet data = null;
        try {
            File from = new File(queueItem.data);
            data = id3.read(from);      //read metadata
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }

        return data;
    }

	@Override
	public void onItemClicked(int position) {

        QueueItem queueItem = coverartList.get(position);
        Intent returnIntent = new Intent();
		returnIntent.putExtra("item", queueItem);
		returnIntent.putExtra("index", selectedItemPosition);
		setResult(Activity.RESULT_OK, returnIntent);
		finish();
	}

    private class LoadRunnable implements Runnable {
        @Override
        public void run() {
            actionBar.setTitle(coverartList.size() + " tracks");
            for (QueueItem queueItem : coverartList) {
                File from = new File(queueItem.data);
                String extension = FilenameUtils.getExtension(from.getAbsolutePath());
                try {
                    dataset = id3.read(from);      //read metadata
                } catch (Exception ex) {
                    Crashlytics.logException(ex);
                }

                if (dataset != null) {
                    metadata = (MusicMetadata) dataset.getSimplified();
                }
                musicMetadataList.add(metadata);
                mAdapter.notifyDataSetChanged();

                // Create a fake result (MUST be final)
                final boolean result = true;

                // Send the result to the UI thread and show it on a Toast
                AsyncJob.doOnMainThread(() -> {
                    if (metadata == null || dataset == null) {
                        if (extension.equals("mp3")) {
                            GeneralTagReadError();
                        } else {
                            InvalidFileExtensionError(extension);
                        }
                    }
                });
            }

        }
    }

	private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
		final BasicRecyclerItemListener mListener;
		private final Context mContext;
        private final ArrayList<MusicMetadata> metadataArrayList;
        private byte[] bytearray;
		private Vector converart;
		private Bitmap bm;

		// Provide a suitable constructor (depends on the kind of dataset)
        public ImageAdapter(Context context, ArrayList<MusicMetadata> metadataArrayList, BasicRecyclerItemListener listener) {
            mContext = context;
			mListener = listener;
            this.metadataArrayList = metadataArrayList;
        }

        public void add(int position, MusicMetadata item) {
            metadataArrayList.add(position, item);
            notifyItemInserted(position);
		}

        public void remove(MusicMetadata item) {
            int position = metadataArrayList.indexOf(item);
            metadataArrayList.remove(position);
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
            final MusicMetadata musicMetadata = metadataArrayList.get(position);

            holder.textTitle.setText(coverartList.get(position).title);

            if (musicMetadata != null) {

                converart = musicMetadata.getPictureList();

				if (converart.size() > 0) {
					bytearray = ((ImageData) converart.get(0)).imageData;

					bm = Utils.decodeBitmapArray(bytearray, MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE);
					if (bm != null) {
						holder.albumIcon.setImageBitmap(bm);
					} else {
						holder.albumIcon.setImageResource(R.mipmap.placeholder);
					}

				} else {
					holder.albumIcon.setImageResource(R.mipmap.placeholder);
				}
			} else {
				holder.albumIcon.setImageResource(R.mipmap.placeholder);
			}
		}

		// Return the size of your dataset (invoked by the layout manager)
		@Override
		public int getItemCount() {
            return metadataArrayList.size();
        }

        public MusicMetadata getItem(int position) {
            if (position >= 0 && position < metadataArrayList.size())
                return metadataArrayList.get(position);
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
