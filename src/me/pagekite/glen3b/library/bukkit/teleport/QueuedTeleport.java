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
 * A type representing a queued player teleportation.
 * @param <D> The class of the destination type appropriate to this teleportation.
 * @author Glen
 */
public interface QueuedTeleport<D> {

	/**
	 * Cancel this queued teleport, if not cancelled already. This method is expected not to notify the user automatically.
	 */
	public void cancel();
	
	/**
	 * Determines if this queued teleportation is cancelled. This method will also return true if the teleportation has successfully completed.
	 */
	public boolean isCancelled();
	
	/**
	 * Gets the target of this teleportation.
	 * @return The {@link D} instance representing the destination of this teleport.
	 */
	public D getDestination();
	
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
