package com.muziko.salut.Callbacks;

public interface SalutDownloadCallback {
	void onDownloadSuccess(String message);

	void onDownloadFailure(String message);
}
