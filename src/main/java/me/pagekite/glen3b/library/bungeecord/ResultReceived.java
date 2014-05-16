package me.pagekite.glen3b.library.bungeecord;

/**
 * Represents a handler of a received result. This interface is intended to be used for results which may not be returned immediately. <b>APIs using this interface will remove the reference to the object after calling it once.</b> This means you must re-register it to receive another notification of receiving data.
 * @author Glen Husman
 * @param <T> The type of the result.
 */
public interface ResultReceived<T>{

	/**
	 * Called upon receiving the result of an operation.
	 * @param result The result.
	 */
	public void onReceive(T result);
}