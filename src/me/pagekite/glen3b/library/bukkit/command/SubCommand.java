package me.pagekite.glen3b.library.bukkit.command;

import java.util.List;

import org.bukkit.command.CommandSender;

/**
 * Represents a command that can be used via a base command.
 * TODO: TSender type argument (validates sender instance) and argument list.
 */
public abstract class SubCommand {

	private List<String> _aliases;
	
	/**
	 * Creates a subcommand with the given aliases.
	 * @param aliases All command aliases, including the main alias as the first element.
	 */
	public SubCommand(String... aliases){
		if(aliases == null || aliases.length == 0){
			throw new IllegalArgumentException("There must be at least one alias.");
		}
	}
	
	/**
	 * Gets a list of strings which act as aliases for this command.
	 * @return A {@code List<String>} instance of aliases, including the primary name (first element) of this command.
	 */
	public final List<String> getAliases(){
		return _aliases;
	}
	
	/**
	 * Gets the usage of the command.
	 * @return A string representing command usage, usually expressed as {@code mainAlias <arg1> <arg2> [arg3]} or {@code mainAlias <arg1> <arg2> <args...>}.
	 */
	public abstract String getUsage();
	
	/**
	 * Gets a description of the command.
	 * @return A description of the command to be displayed in the base command's help page.
	 */
	public abstract String getDescription();
	
	/**
	 * Execute this subcommand.
	 * @param sender The sender of this command.
	 * @param arguments The arguments, including the command name, that were passed to this command. Index 0 will always be the alias of the command that was used in execution.
	 */
	public abstract void execute(CommandSender sender, String[] arguments);
	
}
