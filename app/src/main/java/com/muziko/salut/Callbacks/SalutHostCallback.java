package com.muziko.salut.Callbacks;

import com.muziko.salut.SalutDevice;

public interface SalutHostCallback {
	void onHostSuccess();

	void onHostError(String message);

	void onClientConnected(SalutDevice device);

	void onClientDisconnected(SalutDevice device);
}
