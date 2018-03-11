package com.muziko.salut.Callbacks;

public interface SalutRegisterCallback {
	void onRegisterSuccess(String message);

	void onRegisterFailed(String message);

	void onUnregisterSuccess(String message);

	void onUnregisterFailed(String message);
}
