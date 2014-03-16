package me.pagekite.glen3b.gbukkitlib;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * A type specifying methods allowing controlled, delayed teleportation. Expected to have an instance registered via the {@link org.bukkit.plugin.ServicesManager}.
 * @author Glen Husman
 */
public interface TeleportationManager {

	/**
	 * Queues the specified player to be teleported to the specified location after the default delay. Queued teleportation cancelled upon movement or receiving damage. If the player has the permission node gbukkitlib.tpdelay.bypass, the teleportation will be instant.
	 * @param player The player to teleport.
	 * @param targetLoc The location to teleport to.
	 */
	public void teleportPlayer(Player player, Location targetLoc);
	
	/**
	 * Gets the queued teleportation of the specified player.
	 * @param teleport The player who's teleport will be returned.
	 * @return The last queued teleport of the specified player, or null if none.
	 */
	public QueuedTeleport getTeleport(Player teleport);
	
	/**
	 * Queues the specified player to be teleported to the specified location after the specified delay. Queued teleportation cancelled upon movement or receiving damage.
	 * @param player The player to teleport.
	 * @param targetLoc The location to teleport to.
	 * @param teleportDelay The delay, in seconds, after which the player will be teleported. If this value is 0 OR the player has the permission node gbukkitlib.tpdelay.bypass, the teleportation will be instant.
	 */
	public void teleportPlayer(Player player, Location targetLoc, int teleportDelay);
}
