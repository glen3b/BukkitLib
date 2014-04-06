/*
   This file is part of GBukkitLib.

    GBukkitLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GBukkitLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with GBukkitLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.pagekite.glen3b.library.bukkit.teleport;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * A type specifying methods allowing controlled, delayed teleportation. Expected to have an instance registered via the {@link org.bukkit.plugin.ServicesManager}.
 * @author Glen Husman
 */
public interface TeleportationManager {

	/**
	 * Queues the specified player to be teleported to the specified location after the default delay. Queued teleportation cancelled upon movement or receiving damage. If the player has the permission node {@code gbukkitlib.tpdelay.bypass}, the teleportation will be instant.
	 * @param player The player to teleport.
	 * @param targetLoc The location to teleport to.
	 * @return The queued teleport, or {@code null} if it was instant.
	 */
	public QueuedTeleport<Location> teleportPlayer(Player player, Location targetLoc);
	
	/**
	 * Gets the queued teleportation of the specified player.
	 * @param teleport The player who's teleport will be returned.
	 * @return The last queued teleport of the specified player, or null if none.
	 */
	public QueuedTeleport<Location> getTeleport(Player teleport);
	
	/**
	 * Queues the specified player to be teleported to the specified location after the specified delay. Queued teleportation cancelled upon movement or receiving damage.
	 * @param player The player to teleport.
	 * @param targetLoc The location to teleport to.
	 * @param teleportDelay The delay, in seconds, after which the player will be teleported. If this value is 0 OR the player has the permission node {@code gbukkitlib.tpdelay.bypass}, the teleportation will be instant.
	 * @return The queued teleport, or {@code null} if it was instant.
	 */
	public QueuedTeleport<Location> teleportPlayer(Player player, Location targetLoc, int teleportDelay);
}
