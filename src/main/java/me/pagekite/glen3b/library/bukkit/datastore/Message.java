/*
   This file is part of GBukkitCore.

    GBukkitCore is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GBukkitCore is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with GBukkitCore.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.pagekite.glen3b.library.bukkit.datastore;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * A class representing a retrieved message.
 * Static methods in the class enumerate through all registered {@link MessageProvider} instances to find a specified message.
 * @author Glen Husman
 */
public final class Message {

	/**
	 * Search all {@link MessageProvider}s registered via the {@link org.bukkit.plugin.ServicesManager}, sorting by priority, to find the specified message.
	 * @param key The key of the message to retrieve.
	 * @return The color formatted message associated with the specified key, or {@code null} if not found.
	 */
	public static String get(String key) {
		Validate.notEmpty(key, "The key cannot be null or empty.");
		
		//Prioritized lists
		ArrayList<MessageProvider> highest = new ArrayList<MessageProvider>();
		ArrayList<MessageProvider> high = new ArrayList<MessageProvider>();
		ArrayList<MessageProvider> normal = new ArrayList<MessageProvider>();
		ArrayList<MessageProvider> low = new ArrayList<MessageProvider>();
		ArrayList<MessageProvider> lowest = new ArrayList<MessageProvider>();
		
		populateLists(highest, high, normal, low, lowest);
		
		//Prioritized list results
		String highestMsg = getMessageFromList(highest, key);
		String highMsg = getMessageFromList(high, key);
		String normalMsg = getMessageFromList(normal, key);
		String lowMsg = getMessageFromList(low, key);
		String lowestMsg = getMessageFromList(lowest, key);
		
		//Check each list for a message
		if(highestMsg != null)
			return highestMsg;
		
		if(highMsg != null)
			return highMsg;
		
		if(normalMsg != null)
			return normalMsg;
		
		if(lowMsg != null)
			return lowMsg;
		
		//At this point we don't care if it's null
		return lowestMsg;
		
	}
	
	private static String getMessageFromList(ArrayList<MessageProvider> list, String key){
		for(MessageProvider provider : list){
			if(provider.getMessage(key) != null){
				return provider.getMessage(key).getValue();
			}
		}
		
		return null;
	}
	
	private static void populateLists(ArrayList<MessageProvider> highest, ArrayList<MessageProvider> high, ArrayList<MessageProvider> normal, ArrayList<MessageProvider> low, ArrayList<MessageProvider> lowest){
		Collection<RegisteredServiceProvider<MessageProvider>> registeredMsgs = Bukkit.getServer().getServicesManager().getRegistrations(MessageProvider.class);
		//Populate prioritized lists
				for(RegisteredServiceProvider<MessageProvider> provider : registeredMsgs){
					switch(provider.getPriority()){
					case Highest:
						highest.add(provider.getProvider());
						break;
					case High:
						high.add(provider.getProvider());
						break;
					case Normal:
						normal.add(provider.getProvider());
						break;
					case Low:
						low.add(provider.getProvider());
						break;
					case Lowest:
						lowest.add(provider.getProvider());
						break;
						
					}
				}
	}
	
	/**
	 * Set the specified key to map to the specified value in the highest-priority read/write {@link MessageProvider} registered via the {@link org.bukkit.plugin.ServicesManager}.
	 * @param key The key of the message to set.
	 * @param value The value of the message to set.
	 * @return True if the set was successful; false otherwise.
	 */
	public static boolean set(String key, String value) {
		Validate.notEmpty(key, "The key cannot be null or empty.");
		Validate.notNull(value, "The message cannot be null.");
		
		//Prioritized lists
		ArrayList<MessageProvider> highest = new ArrayList<MessageProvider>();
		ArrayList<MessageProvider> high = new ArrayList<MessageProvider>();
		ArrayList<MessageProvider> normal = new ArrayList<MessageProvider>();
		ArrayList<MessageProvider> low = new ArrayList<MessageProvider>();
		ArrayList<MessageProvider> lowest = new ArrayList<MessageProvider>();
		
		populateLists(highest, high, normal, low, lowest);
		
		if(write(highest, key, value))
			return true;
		
		if(write(high, key, value))
			return true;
		
		if(write(normal, key, value))
			return true;
		
		if(write(low, key, value))
			return true;
		
		return write(lowest, key, value);
	}
	
	/**
	 * Sets the specified key to map to the specified value in all registered read/write {@link MessageProvider} instances.
	 * @param key The key of the message to set.
	 * @param value The value of the message to set.
	 */
	public static void setAll(String key, String value){
		Validate.notEmpty(key, "The key cannot be null or empty.");
		Validate.notNull(value, "The message cannot be null.");
		
		Collection<RegisteredServiceProvider<MessageProvider>> registeredMsgs = Bukkit.getServer().getServicesManager().getRegistrations(MessageProvider.class);
		
		for(RegisteredServiceProvider<MessageProvider> message : registeredMsgs){
			if(!message.getProvider().isReadOnly()){
				message.getProvider().setMessage(key, value);
			}
		}
	}
	private static boolean write(ArrayList<MessageProvider> list, String key, String value){
		for(MessageProvider provider : list){
			if(!provider.isReadOnly()){
				provider.setMessage(key, value);
				return true;
			}
		}
		return false;
	}
	
	private String _key;
	
	private String _value;
	
	/**
	 * Creates an instance of Message with the specified key and value. Does not add to any {@link MessageProvider}.
	 * @param key The {@link MessageProvider} key for this message.
	 * @param value The value of the message.
	 */
	public Message(String key, String value){
		Validate.notEmpty(key, "The key cannot be null or empty.");
		Validate.notNull(value, "The message cannot be null.");
		
		_key = key;
		_value = value;
	}
	
	/**
	 * Gets the key of this message.
	 * @return The {@link MessageProvider} backend storage key.
	 */
	public String getKey(){
		return _key;
	}
	
	/**
	 * Gets the value of this message, as input.
	 * @return The unformatted value of the message.
	 */
	public String getUnformattedValue(){
		return _value;
	}
	
	/**
	 * Gets the value of this message, color code formatted with the & character.
	 * @return The color formatted value of the message.
	 */
	public String getValue(){
		return ChatColor.translateAlternateColorCodes('&', getUnformattedValue());
	}
}
