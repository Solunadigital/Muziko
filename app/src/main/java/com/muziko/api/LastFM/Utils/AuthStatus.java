package com.muziko.api.LastFM.Utils;

/**
 * Status is a "namespace class" that contains the definitions of various status
 * types. This class is not meant to be instantiated.
 */
public class AuthStatus {
	public static final int AUTHSTATUS_NOAUTH = 0;
	public static final int AUTHSTATUS_UPDATING = 1;
	public static final int AUTHSTATUS_BADAUTH = 2;
	public static final int AUTHSTATUS_FAILED = 3;
	public static final int AUTHSTATUS_RETRYLATER = 4;
	public static final int AUTHSTATUS_OK = 5;
	public static final int AUTHSTATUS_CLIENTBANNED = 6;
	public static final int AUTHSTATUS_NETWORKUNFIT = 7;

	/**
	 * This class is not meant to be instantiated.
	 */
	private AuthStatus() {
	}

	public static class StatusException extends Exception {
		private static final long serialVersionUID = 7204759787220898684L;

		public StatusException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class ClientBannedException extends StatusException {
		private static final long serialVersionUID = 3452632996181451232L;

		public ClientBannedException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class BadAuthException extends StatusException {
		private static final long serialVersionUID = 1512908282760585728L;

		public BadAuthException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class BadSessionException extends StatusException {
		private static final long serialVersionUID = -1360166232828945639L;

		public BadSessionException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class TemporaryFailureException extends StatusException {
		private static final long serialVersionUID = -2810766166898051179L;

		public TemporaryFailureException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class UnknownResponseException extends StatusException {
		private static final long serialVersionUID = 7351097754868391707L;

		public UnknownResponseException(String detailMessage) {
			super(detailMessage);
		}
	}
}