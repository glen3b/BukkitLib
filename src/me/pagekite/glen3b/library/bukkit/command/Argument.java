package me.pagekite.glen3b.library.bukkit.command;

import org.apache.commons.lang.Validate;

public class Argument {

	private String _name;
	private boolean _required;
	
	/**
	 * Creates a mandatory argument.
	 * @param argName The name of the argument.
	 */
	public Argument(String argName){
		this(argName, true);
	}
	
	/**
	 * Gets the number of arguments this argument represents at maximum.
	 * This method will return {@code -1} if it is an unbounded number of arguments.
	 * @return The highest possible number of arguments that this {@code Argument} will occupy in an array.
	 */
	public int getMaximumArgumentCount(){
		return 1;
	}
	
	/**
	 * Gets the number of arguments this argument represents at minimum.
	 * @return The minimum number of arguments that this {@code Argument} will occupy in an array.
	 */
	public int getArgumentCount(){
		return 1;
	}
	
	/**
	 * Creates an argument.
	 * @param argName The name of the argument.
	 * @param mandatory Whether the argument is mandatory.
	 */
	public Argument(String argName, boolean mandatory){
		Validate.notEmpty(argName, "The argument name cannot be null or empty.");
		
		_name = argName;
		_required = mandatory;
	}
	
	/**
	 * Gets the name of the argument.
	 * @return The name of the command argument.
	 */
	public String getName(){
		return _name;
	}
	
	/**
	 * Determines if this is a required argument.
	 * @return Whether this command argument is required.
	 */
	public boolean isRequired(){
		return _required;
	}
	
	/**
	 * Gets a string showing usage of the argument.
	 * @return A user-displayed formatted string representing argument usage.
	 */
	public String getUsageString(){
		return String.format(isRequired() ? "<%s>" : "[%s]", getName().toLowerCase().trim());
	}
}
