package me.pagekite.glen3b.gbukkitlib;

import java.util.ArrayList;
import java.util.Collection;

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
	 * Creates an instance of Message with the specified key and value. Does not add to any {@link MessageProvider}.
	 * @param key The {@link MessageProvider} key for this message.
	 * @param value The value of the message.
	 */
	public Message(String key, String value){
		if(key == null){
			throw new IllegalArgumentException("Key cannot be null.");
		}
		
		if(value == null){
			throw new IllegalArgumentException("Value cannot be null.");
		}
		
		_key = key;
		_value = value;
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
	
	/**
	 * Gets the key of this message.
	 * @return The {@link MessageProvider} backend storage key.
	 */
	public String getKey(){
		return _key;
	}
	
	private String _key;
	private String _value;
	
	/**
	 * Search all {@link MessageProvider}s registered via the {@link org.bukkit.plugin.ServicesManager}, sorting by priority, to find the specified message.
	 * @param key The key of the message to retrieve.
	 * @return The message associated with the specified key, or {@code null} if not found.
	 */
	public static String get(String key) {
		if(key == null){
			throw new IllegalArgumentException("Key cannot be null.");
		}
		
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
	
	private static boolean write(ArrayList<MessageProvider> list, String key, String value){
		for(MessageProvider provider : list){
			if(!provider.isReadOnly()){
				provider.setMessage(key, value);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets the specified key to map to the specified value in all registered read/write {@link MessageProvider} instances.
	 * @param key The key of the message to set.
	 * @param value The value of the message to set.
	 */
	public static void setAll(String key, String value){
		if(key == null){
			throw new IllegalArgumentException("Key cannot be null.");
		}
		if(value == null){
			throw new IllegalArgumentException("Value cannot be null.");
		}
		
		Collection<RegisteredServiceProvider<MessageProvider>> registeredMsgs = Bukkit.getServer().getServicesManager().getRegistrations(MessageProvider.class);
		
		for(RegisteredServiceProvider<MessageProvider> message : registeredMsgs){
			if(!message.getProvider().isReadOnly()){
				message.getProvider().setMessage(key, value);
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
		if(key == null){
			throw new IllegalArgumentException("Key cannot be null.");
		}
		if(value == null){
			throw new IllegalArgumentException("Value cannot be null.");
		}
		
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
}
