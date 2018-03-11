package com.muziko.fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.muziko.R;
import com.muziko.activities.MP3CutterActivity;
import com.muziko.adapter.CutTonesListAdapter;
import com.muziko.callbacks.Mp3Callback;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.mediaplayer.PlayerConstants;

import java.io.IOException;
import java.util.ArrayList;

public class CutTones extends BaseFragment implements CutTonesListAdapter.OnPlaySongRequestedListner, OnRefreshListener, Mp3Callback {
    public static MediaPlayer mp;
    private final WeakHandler handler = new WeakHandler();
	private ListView listView;
	private CutTonesListAdapter adapter;

	private ArrayList<QueueItem> toneList;

	private CutTonesLoader task;

	public CutTones() {
		// Required empty public constructor
	}

	public static CutTones newInstance() {
		return new CutTones();
	}

    @Override
    public void onAttach(Context context) {
        ((MP3CutterActivity) getActivity()).callbackTones = this;
        super.onAttach(context);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
		}

	}

    @Override
    public void onResume() {
        super.onResume();

        handler.postDelayed(this::reload, LOAD_DELAY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        unload();
    }

    @Override
    public void onDetach() {

        super.onDetach();

        unload();

    }

    @Override
    public void onRefresh() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_mp3cutter, container, false);

        listView = rootView.findViewById(R.id.songsList);
        toneList = new ArrayList<>();
		adapter = new CutTonesListAdapter(getActivity(), toneList);
		listView.setAdapter(adapter);

		mp = new MediaPlayer();

		adapter.addPlayListner(this);

		return rootView;
	}

	@Override
	public void onPlaySong(QueueItem queue) {
		if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
			AppController.Instance().serviceStop();
		}

		if (!play(queue)) {
            AppController.toast(getActivity(), "Unable to play ringtone!");
        }

	}

	@Override
	public void onDelete(final QueueItem queue) {
		new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Delete Song").content("This will delete Ringtone permanently from this device, do you want to proceed ?").positiveText("YES").onPositive((dialog, which) -> {
			Utils.deleteSong(getActivity(), queue.data);

			reload();
		}).negativeText("NO").show();
	}

	@Override
	public void onEditSong(QueueItem dataBean) {
		AppController.Instance().cutSong(dataBean);
	}

	@Override
	public void onSetRingtone(QueueItem dataBean) {
		AppController.Instance().ringtone(getActivity(), "audio/wav", dataBean);
	}

	@Override
	public void onScrollDown(int position) {
		if (position == toneList.size() - 1) {
			listView.post(() -> listView.setSelection(listView.getCount() - 1));
		}
	}

    private boolean play(QueueItem queue) {
        boolean ret = false;

        if (mp != null && mp.isPlaying()) {
            mp.stop();
        }

        try {
            mp.reset();
            mp.setDataSource(queue.data);
            mp.prepare();
            mp.start();

            ret = true;
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

	@Override
	public void onSearch(String query) {
		adapter.getFilter().filter(query);
	}


	private void unload() {
		if (task != null) {
			task.cancel(true);
			task = null;
		}
	}

	private void load() {
		task = new CutTonesLoader(getActivity());
		task.execute();
	}

	private void reload() {
		unload();
		load();
	}

	public class CutTonesLoader extends AsyncTask<Void, int[], Boolean> {
		private final Context ctx;
		private ArrayList<QueueItem> list = new ArrayList<>();

		public CutTonesLoader(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			list = Utils.musicFromMuziko(ctx);

			return true;
		}

		@Override
		protected void onPostExecute(Boolean s) {
			super.onPostExecute(s);

			toneList.clear();
			toneList.addAll(list);

			adapter.notifyDataSetChanged();
		}
	}
}
