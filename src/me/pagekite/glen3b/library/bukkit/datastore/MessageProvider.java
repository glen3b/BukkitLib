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

package me.pagekite.glen3b.library.bukkit.datastore;

import java.util.Set;

/**
 * A provider of user-friendly color-formatted messages.
 * To access a message provided by a {@link me.pagekite.glen3b.library.bukkit.datastore.MessageProvider}, it is recommended that you use the static methods present on the {@link me.pagekite.glen3b.library.bukkit.datastore.Message} class.
 * @author Glen Husman
 */
public interface MessageProvider {

	/**
	 * Gets the message with the specified message ID.
	 * @return The message with the specified ID, or {@code null} if it is not provided by this {@link me.pagekite.glen3b.library.bukkit.datastore.MessageProvider}.
	 */
	public Message getMessage(String messageId);
	
	/**
	 * Gets the set of provided messages. This is a collection of keys, not values.
	 * @return A {@link java.util.Set} of keys of messages provided by this {@link me.pagekite.glen3b.library.bukkit.datastore.MessageProvider}.
	 */
	public Set<String> getProvidedMessages();
	
	/**
	 * Determines the read/write status of this message provider.
	 * @return If this is a read-only message provider.
	 */
	public boolean isReadOnly();
	
	/**
	 * Saves all messages stored by this provider to the backend, if applicable.
	 * @exception java.lang.IllegalStateException Thrown if this is a read-only message provider.
	 */
	public void saveMessages();
	
	/**
	 * Adds a message to this provider.
	 * @param value The {@link Message} instance to set.
	 * @exception java.lang.IllegalStateException Thrown if this is a read-only message provider.
	 */
	public void setMessage(Message value) throws IllegalStateException;
	
	/**
	 * Adds a message to this provider.
	 * @param key The identifying key of the message.
	 * @param value The value of the message, which will have color codes preceded with the ampersand character translated appropriately.
	 * @exception java.lang.IllegalStateException Thrown if this is a read-only message provider.
	 */
	public void setMessage(String key, String value) throws IllegalStateException;
}
