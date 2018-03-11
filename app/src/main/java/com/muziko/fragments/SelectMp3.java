package com.muziko.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.badoo.mobile.util.WeakHandler;
import com.muziko.R;
import com.muziko.activities.MP3CutterActivity;
import com.muziko.adapter.Mp3CutterListAdapter;
import com.muziko.callbacks.Mp3Callback;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.AppController;

import java.util.ArrayList;

public class SelectMp3 extends BaseFragment implements Mp3CutterListAdapter.ClickListenerOnElement, OnRefreshListener, Mp3Callback {
    private final WeakHandler handler = new WeakHandler();
    private Mp3CutterListAdapter adapter;
	private ArrayList<QueueItem> songList;
	private SelectLoader task;

	public SelectMp3() {
		// Required empty public constructor
	}

	public static SelectMp3 newInstance() {
		return new SelectMp3();
	}

    @Override
    public void onAttach(Context context) {
        ((MP3CutterActivity) getActivity()).callbackSelect = this;
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

        songList = new ArrayList<>();
        adapter = new Mp3CutterListAdapter(getActivity(), songList);

        ListView listView = rootView.findViewById(R.id.songsList);
        listView.setAdapter(adapter);

        adapter.addClickListner(this);

        return rootView;
    }

	@Override
    public void onClick(QueueItem queue) {
        AppController.Instance().cutSong(queue);
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
		task = new SelectLoader(getActivity());
		task.execute();
	}

	private void reload() {
		unload();
		load();
	}

	public class SelectLoader extends AsyncTask<Void, int[], Boolean> {
		private final Context ctx;
		private final ArrayList<QueueItem> list = new ArrayList<>();

		public SelectLoader(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			list.addAll(TrackRealmHelper.getTracks(0).values());

			return true;
		}

		@Override
		protected void onPostExecute(Boolean s) {
			super.onPostExecute(s);

			songList.clear();
			songList.addAll(list);

			adapter.notifyDataSetChanged();
		}
	}
}
