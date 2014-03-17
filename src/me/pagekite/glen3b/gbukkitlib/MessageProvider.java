package me.pagekite.glen3b.gbukkitlib;

import java.util.Set;

/**
 * A provider of user-friendly color-formatted messages.
 * To access a message provided by a {@link me.pagekite.glen3b.gbukkitlib.MessageProvider}, it is recommended that you use the static methods present on the {@link me.pagekite.glen3b.gbukkitlib.Message} class.
 * @author Glen Husman
 */
public interface MessageProvider {

	/**
	 * Gets the set of provided messages. This is a collection of keys, not values.
	 * @return A {@link java.util.Set} of keys of messages provided by this {@link me.pagekite.glen3b.gbukkitlib.MessageProvider}.
	 */
	public Set<String> getProvidedMessages();
	
	/**
	 * Gets the message with the specified message ID.
	 * @return The color-formatted message with the specified ID, or {@code null} if it is not provided by this {@link me.pagekite.glen3b.gbukkitlib.MessageProvider}.
	 */
	public String getMessage(String messageId);
	
	/**
	 * Adds a message to this provider.
	 * @param key The identifying key of the message.
	 * @param value The value of the message, which will have color codes preceded with the ampersand character translated appropriately.
	 * @exception java.lang.IllegalStateException Thrown if this is a read-only message provider.
	 */
	public void setMessage(String key, String value) throws IllegalStateException;
	
	/**
	 * Saves all messages stored by this provider to the backend, if applicable.
	 * @exception java.lang.IllegalStateException Thrown if this is a read-only message provider.
	 */
	public void saveMessages();
	
	/**
	 * Determines the read/write status of this message provider.
	 * @return If this is a read-only message provider.
	 */
	public boolean isReadOnly();
}
