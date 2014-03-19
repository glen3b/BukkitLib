package me.pagekite.glen3b.gbukkitlib;

/**
 * A class housing static properties of unchanging constants.
 * @author Glen Husman
 */
public final class Constants {

	/**
	 * The target number of server ticks per second.
	 */
	public static final long TICKS_PER_SECOND = 20L;
	
	/**
	 * The target number of server ticks per minute.
	 */
	public static final long TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;
	
	private Constants(){
		//No instance should be created
	}
	
}
