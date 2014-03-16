package me.pagekite.glen3b.gbukkitlib;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * A type representing a queued player teleportation.
 * @author Glen
 */
public interface QueuedTeleport {

	/**
	 * Cancel this queued teleport, if not cancelled already. This method is expected not to notify the user automatically.
	 */
	public void cancel();
	
	/**
	 * Determines if this queued teleportation is cancelled. This method will also return true if the teleportation has successfully completed.
	 */
	public boolean isCancelled();
	
	/**
	 * Gets the {@link org.bukkit.Location} which is the target of this teleportation.
	 * @return The {@link org.bukkit.Location} instance representing the destination of this teleport.
	 */
	public Location getTo();
	
	/**
	 * Gets the remaining time, in seconds, of the teleportation delay.
	 * @return The remaining time, in seconds, after which teleportation will commence.
	 */
	public int getRemainingDelay();
	
	/**
	 * Gets the {@link org.bukkit.entity.Player} which is to be teleported. 
	 * @return The {@link org.bukkit.entity.Player} which is to be teleported after the delay.
	 * @exception java.lang.IllegalStateException Thrown if the teleportation is cancelled when this method is called.
	 */
	public Player getEntity() throws IllegalStateException;
}
