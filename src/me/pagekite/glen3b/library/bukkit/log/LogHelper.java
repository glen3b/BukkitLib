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
	 * Process the log statement, stripping color if need be.
	 * @param msg The statement to process.
	 * @param format The message formatting items.
	 * @return The processed statement.
	 */
	protected String processLogStatement(String msg, Object... format){
		if(format != null && format.length > 0){
			msg = String.format(msg, format);
		}
		
		if(isColorStrippingEnabled()){
			msg = ChatColor.stripColor(msg);
		}
		
		return msg;
	}
	
	/**
	 * Logs a severe error.
	 * @param message The message to log.
	 */
	public void logError(String message){
		logError(message, null);
	}
	
	/**
	 * Logs a severe error, including a stack trace if error is not null.
	 * @param message The message to log.
	 * @param error The error to include with the log entry.
	 * @param format The arguments to use to format the string.
	 */
	public void logError(String message, Throwable error, Object... format){
		log(Level.SEVERE, error, message, format);
	}
	
	/**
	 * Logs a notable warning, including a stack trace if error is not null.
	 * @param message The message to log.
	 * @param error The error to include with the log entry.
	 * @param format The arguments to use to format the string.
	 */
	public void logWarn(String message, Throwable error, Object... format){
		log(Level.WARNING, error, message, format);
	}
	
	/**
	 * Logs a notable warning.
	 * @param message The message to log.
	 */
	public void logWarn(String message){
		logWarn(message, null);
	}
	
	/**
	 * Sets the debug logging state.
	 * @param value Whether to log debug messages.
	 */
	public void setDebugLoggerEnabled(boolean value){
		_debugLoggingEnabled = value;
	}
	
	/**
	 * Logs a message with the specified severity, formatting the string with the specified formatters.
	 * @param severity The severity of the log message.
	 * @param message The message to log.
	 * @param format The objects to pass to String.format, if any.
	 */
	public void log(Level severity, String message, Object... format){
		log(severity, null, message, format);
	}
	
	/**
	 * Logs a message with info severity, formatting the string with the specified formatters.
	 * @param message The message to log.
	 * @param format The objects to pass to String.format, if any.
	 */
	public void log(String message, Object... format){
		log(Level.INFO, message, format);
	}
	
	/**
	 * Logs a message with the specified severity.
	 * @param severity The severity of the log message.
	 * @param message The message to log.
	 */
	public void log(Level severity, String message){
		log(severity, message, (Object[])null);
	}
	
	/**
	 * Logs a message with the specified severity, formatting the string with the specified formatters.
	 * @param severity The severity of the log message.
	 * @param error The error to record, or null if none.
	 * @param message The message to log.
	 * @param format The objects to use to format the string, if any.
	 */
	public void log(Level severity, Throwable error, String message, Object... format){
		if(severity == null){
			throw new IllegalArgumentException("The severity must not be null.");
		}
		
		if(message == null){
			throw new IllegalArgumentException("The message must not be null.");
		}
		
		message = processLogStatement(message, format);
		
		if(error == null){
			_logged.getLogger().log(severity, message);
		}else{
			_logged.getLogger().log(severity, message, error);
		}
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
		
		message = processLogStatement(message, format);
		
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
