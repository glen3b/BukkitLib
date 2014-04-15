package me.pagekite.glen3b.library.bukkit;

/**
 * Represents known minecraft server constants.
 * @author Glen Husman
 */
public final class Constants {

	private Constants(){
		// No instances should be created
	}
	
	/**
	 * The target number of server ticks per second.
	 * This is not guaranteed to be accurate, it is only the value <i>targeted</i> as the framerate by the minecraft server.
	 * One server tick, on a server which is running at perfect target framerate, is 0.05 seconds.
	 */
	public static final long TICKS_PER_SECOND = 20L;
	
	/**
	 * The target number of server ticks per minute.
	 * @see Constants#TICKS_PER_SECOND
	 */
	public static final long TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;
	
}
