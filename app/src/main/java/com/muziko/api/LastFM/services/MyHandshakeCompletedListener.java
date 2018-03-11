package com.muziko.api.LastFM.services;

import android.util.Log;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

/**
 * Created by Debugs on 8/13/2016.
 */
class MyHandshakeCompletedListener implements HandshakeCompletedListener {

	private static final String TAG = "HandShakeListenR";

	@Override
	public void handshakeCompleted(HandshakeCompletedEvent event) {
		SSLSession session = event.getSession();
		String protocol = session.getProtocol();
		String cipherSuite = session.getCipherSuite();
		String peerName = null;


		try {
			peerName = session.getPeerPrincipal().getName();
			Log.d(TAG, "peerName: " + peerName);
		} catch (SSLPeerUnverifiedException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "session: " + session);
		Log.d(TAG, "protocol: " + protocol);
		Log.d(TAG, "cipherSuite: " + cipherSuite);

	}
}

