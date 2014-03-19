package me.pagekite.glen3b.library.bukkit.log;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

/**
 * Allows for logging of messages, and toggling of debug messages.
 * @author Glen Husman
 */
public class LogHelper {

	private Plugin _logged;
	private boolean _debugLoggingEnabled = false;
	private boolean _stripLogColor = true;
	
	/**
	 * Gets the color stripping state. Defaults to true.
	 * @return Whether to strip color from log messages.
	 */
	public boolean isColorStrippingEnabled(){
		return _stripLogColor;
	}
	
	/**
	 * Sets the color stripping state.
	 * @param value Whether to strip color from log messages.
	 */
	public void setColorStrippingEnabled(boolean value){
		_stripLogColor = value;
	}
	
	/**
	 * Gets the debug logging state. Defaults to false.
	 * @return Whether to log debug messages.
	 */
	public boolean isDebugLoggerEnabled(){
		return _debugLoggingEnabled;
	}
	
	/**
	 * Sets the debug logging state.
	 * @param value Whether to log debug messages.
	 */
	public void setDebugLoggerEnabled(boolean value){
		_debugLoggingEnabled = value;
	}
	
	public void log(Level severity, Throwable error, String message, String... format){
		throw new RuntimeException("Not implemented.");
	}
	
	/**
	 * Logs a debug message to the plugin logger, if debug logs are enabled. Logs at priority info.
	 * @param message The message to log.
	 * @param format The arguments to pass to the string formatter, if not null and length > 0.
	 */
	public void debugLog(String message, Object... format){
		debugLog(message, null, format);
	}
	
	/**
	 * Logs a debug message to the plugin logger, if debug logs are enabled. Logs at priority info.
	 * @param message The message to log.
	 * @param error The error that occurred, if any.
	 * @param format The arguments to pass to the string formatter, if not null and length > 0.
	 */
	public void debugLog(String message, Throwable error, Object... format){
		if(message == null){
			throw new IllegalArgumentException("The message must not be null.");
		}
		
		if(!isDebugLoggerEnabled()){
			return;
		}
		
		if(format != null && format.length > 0){
			message = String.format(message, format);
		}
		
		if(isColorStrippingEnabled()){
			message = ChatColor.stripColor(message);
		}
		
		if(error == null){
			_logged.getLogger().log(Level.INFO, message);
		}else{
			_logged.getLogger().log(Level.INFO, message, error);
		}
	}
	
	/**
	 * Creates a log helper for the specified plugin.
	 * @param plugin The plugin for which to log.
	 */
	public LogHelper(Plugin plugin) {
		if(plugin == null){
			throw new IllegalArgumentException("The plugin must not be null.");
		}
		
		_logged = plugin;
	}

}
