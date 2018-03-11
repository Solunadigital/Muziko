package com.muziko.salut.Callbacks;

public interface SalutUploadCallback {
	void onUploadSuccess(String message);

	void onUploadFailure(String message);
}
