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

import org.bukkit.entity.Player;

/**
 * A type specifying methods allowing controlled, delayed teleportation across a BungeeCord server network. Expected to have an instance registered via the {@link org.bukkit.plugin.ServicesManager}.
 * @author Glen Husman
 * @see me.pagekite.glen3b.library.bungeecord.ServerTransportManager
 */
public interface ServerTeleportationManager {

	/**
	 * Queues the specified player to be teleported to the specified server after the default delay. Queued teleportation cancelled upon movement or receiving damage. If the player has the permission node gbukkitlib.tpdelay.bypass, the teleportation will be instant.
	 * @param player The player to teleport.
	 * @param targetServer The server to teleport to.
	 * @return The queued teleport, or {@code null} if it was instant.
	 */
	public QueuedTeleport<String> teleportPlayer(Player player, String targetServer);
	
	/**
	 * Gets the queued teleportation of the specified player.
	 * Queued teleports that have already executed (for cross-server operations) may return {@code null} on some methods.
	 * @param teleport The player who's teleport will be returned.
	 * @return The last queued teleport of the specified player, or null if none. The destination of the teleport will be the name of the server.
	 */
	public QueuedTeleport<String> getTeleport(Player teleport);
	
	/**
	 * Queues the specified player to be teleported to the specified server after the specified delay. Queued teleportation cancelled upon movement or receiving damage.
	 * @param player The player to teleport.
	 * @param targetServer The server to teleport to.
	 * @param teleportDelay The delay, in seconds, after which the player will be teleported. If this value is 0 OR the player has the permission node gbukkitlib.tpdelay.bypass, the teleportation will be instant.
	 * @return The queued teleport, or {@code null} if it was instant.
	 */
	public QueuedTeleport<String> teleportPlayer(Player player, String targetServer, int teleportDelay);
}
