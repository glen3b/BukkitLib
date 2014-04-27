package me.pagekite.glen3b.library.bukkit;

import org.apache.commons.lang.Validate;

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

	/**
	 * The number of server ticks for which an untouched {@link org.bukkit.entity.Item} will survive before being removed.
	 */
	public static final long ITEM_ENTITY_LIFE = TICKS_PER_MINUTE * 5;
	
	/**
	 * The default maximum food level of a {@link org.bukkit.entity.Player}, representing a full hunger bar.
	 */
	public static final int MAXIMUM_FOOD_LEVEL = 20;
	
	/**
	 * Gets the number of ticks within the specified number of seconds, rounded to the nearest whole number.
	 * @param seconds The amount of seconds.
	 * @return The amount of seconds expressed in server ticks.
	 */
	public static long getTicks(double seconds){
		Validate.isTrue(!Double.isInfinite(seconds) && !Double.isNaN(seconds), "The specified value is an invalid number.");
		
		return Math.round(seconds * TICKS_PER_SECOND);
	}
	
}
