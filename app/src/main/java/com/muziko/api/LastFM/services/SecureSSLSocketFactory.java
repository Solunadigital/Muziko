package com.muziko.api.LastFM.services;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Debugs on 8/13/2016.
 *
 * @author
 */
class SecureSSLSocketFactory extends SSLSocketFactory {

	private static final String TAG = "SecSSLSockFactory";

	private final SSLSocketFactory delegate;
	private HandshakeCompletedListener handshakeListener;

	public SecureSSLSocketFactory(
			SSLSocketFactory delegate, HandshakeCompletedListener handshakeListener) {
		this.delegate = delegate;
		this.handshakeListener = handshakeListener;
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose)
			throws IOException {
		SSLSocket socket = (SSLSocket) this.delegate.createSocket(s, host, port, autoClose);

		if (null != this.handshakeListener) {
			socket.addHandshakeCompletedListener(this.handshakeListener);
		}

		return socket;
	}

	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException {

		SSLSocket socket = (SSLSocket) this.delegate.createSocket(arg0, arg1);

		if (null != this.handshakeListener) {
			socket.addHandshakeCompletedListener(this.handshakeListener);
		}

		return socket;
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {

		SSLSocket socket = (SSLSocket) this.delegate.createSocket(arg0, arg1);

		if (null != this.handshakeListener) {
			socket.addHandshakeCompletedListener(this.handshakeListener);
		}

		return socket;
	}

	@Override
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
			throws IOException {

		SSLSocket socket = (SSLSocket) this.delegate.createSocket(arg0, arg1, arg2, arg3);

		if (null != this.handshakeListener) {
			socket.addHandshakeCompletedListener(this.handshakeListener);
		}

		return socket;
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
	                           int arg3) throws IOException {

		SSLSocket socket = (SSLSocket) this.delegate.createSocket(arg0, arg1, arg2, arg3);

		if (null != this.handshakeListener) {
			socket.addHandshakeCompletedListener(this.handshakeListener);
		}

		return socket;
	}
// and so on for all the other createSocket methods of SSLSocketFactory.

	@Override
	public String[] getDefaultCipherSuites() {
		// TODO: or your own preferences
		return this.delegate.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		// TODO: or your own preferences
		return this.delegate.getSupportedCipherSuites();
	}
}
