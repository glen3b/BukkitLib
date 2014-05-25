package me.pagekite.glen3b.library.bungeecord;

/**
 * Represents a handler of a received result.
 * This interface is intended to be used for results which may not be returned immediately. <b>APIs using this interface will remove the reference to the object after calling it once.</b> This means you must re-register it to receive another notification of receiving data.
 * @author Glen Husman
 * @param <S> The type of the source of the result.
 * @param <O> The type of the result.
 */
public interface ResultReceived<S, O>{

	/**
	 * Called upon receiving the result of an operation.
	 * @param src The source of the result.
	 * @param result The result.
	 */
	public void onReceive(S src, O result);
}