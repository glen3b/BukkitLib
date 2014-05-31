package me.pagekite.glen3b.library.bukkit.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;

/**
 * Represents a command that can be used via a base command.
 * TODO: TSender type argument (validates sender instance, such as players only command) and an Argument class and list.
 * @author Glen Husman
 */
public abstract class SubCommand implements PermissionConstrainedCommand {

	/**
	 * Returns all values in {@code possibilities} that have names which start with or equal (ignoring case) the string {@code argument}, and which {@code sender} has access to as determined by the interface method requirement.
	 * This method eliminates possibilities that do not start with the argument as typed so far (disregarding case), as well as arguments which are not permitted to be used by the sender.
	 * @param sender The sender of this tab completion request. If this argument is {@code null}, no permission checks will be performed on elements of {@code possibilities} before adding them to the returned list.
	 * @param argument The argument in the command as typed so far. May be {@code null}.
	 * @param possibilities All possibilities for the argument, not accounting for the argument so far.
	 * @return All possibilities for the argument, accounting for the argument as typed so far. The returned list is guaranteed to be modifiable.
	 */
	public static List<String> getTabCompletions(CommandSender sender, String argument, Collection<? extends PermissionConstrainedCommand> possibilities){
		Validate.noNullElements(possibilities, "There must not be a null tab completion argument.");
		
		if(possibilities.size() == 0){
			return Lists.newArrayListWithCapacity(0);
		}
		
		String arg = argument == null ? "" : argument.trim().toLowerCase();
		ArrayList<String> retVal = Lists.newArrayListWithExpectedSize(possibilities.size());
		for(PermissionConstrainedCommand strSeq : possibilities){
			String str = strSeq.getName();
			if(str != null && StringUtil.startsWithIgnoreCase(str, arg) && (sender == null || strSeq.hasAccess(sender))){
				retVal.add(str);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Returns all values in {@code possibilities} that start with or equal (ignoring case) the string {@code argument}.
	 * This method eliminates possibilities that do not start with the argument as typed so far (disregarding case). The collection of CharSequence objects will have each element converted to a string using {@link CharSequence#toString()} before adding to the returned list.
	 * @param argument The argument in the command as typed so far. May be {@code null}.
	 * @param possibilities All possibilities for the argument, not accounting for the argument so far.
	 * @return All possibilities for the argument, accounting for the argument as typed so far. The returned list is guaranteed to be modifiable.
	 * @deprecated Use {@link StringUtil#copyPartialMatches(String, Iterable, Collection)} instead of this method.
	 */
	@Deprecated
	public static List<String> getTabCompletions(String argument, Collection<? extends CharSequence> possibilities){
		Validate.noNullElements(possibilities, "There must not be a null tab completion argument.");
		
		if(possibilities.size() == 0){
			return Lists.newArrayListWithCapacity(0);
		}
		
		String arg = argument == null ? "" : argument.trim().toLowerCase();
		ArrayList<String> retVal = Lists.newArrayListWithExpectedSize(possibilities.size());
		for(CharSequence strSeq : possibilities){
			String str = strSeq.toString();
			if(str != null && StringUtil.startsWithIgnoreCase(str, arg)){
				retVal.add(str);
			}
		}
		
		return retVal;
	}
	
	private List<String> _aliases;
	
	/**
	 * Creates a subcommand with the given aliases.
	 * @param aliases All command aliases, including the main alias as the first element.
	 */
	public SubCommand(String... aliases){
		Validate.notEmpty(aliases, "There must be at least one alias.");
		_aliases = Lists.newArrayList(aliases);
	}
	
	/**
	 * Execute this subcommand.
	 * @param sender The sender of this command.
	 * @param arguments The arguments, including the command name, that were passed to this command. Index 0 will always be the alias of the command that was used in execution.
	 */
	public abstract void execute(CommandSender sender, String[] arguments);
	
	/**
	 * Gets a list of strings which act as aliases for this command.
	 * @return A new read only {@code List<String>} instance of aliases, including the primary name (first element) of this command.
	 */
	public final List<String> getAliases(){
		return Collections.unmodifiableList(_aliases);
	}
	
	/**
	 * Gets a description of the command.
	 * @return A description of the command to be displayed in the base command's help page.
	 */
	public abstract String getDescription();
	
	/**
	 * Gets the name of the command, also known as the primary alias.
	 * @return The primary alias of the command.
	 */
	public final String getName(){
		return _aliases.size() == 0 ? null : _aliases.get(0);
	}
	
	/**
	 * Gets a user-friendly, color-formatted string stating the usage of the command.<br/>
	 * <b>This method will eventually be replaced with a commands argument API, and this method will become final.</b>
	 * @return A string representing command usage, usually expressed as {@code mainAlias <arg1> <arg2> [arg3]} or {@code mainAlias <arg1> <arg2> <args...>}.
	 */
	public abstract String getUsage();
	
	/**
	 * Gets all possible tab completion arguments, given the arguments so far and the sender of the command.
	 * The first element of the {@code arguments} array will always be the alias of this {@code SubCommand} that is used in invokation.
	 * The default implementation of this method returns all online players that start with the argument so far.
	 * @param sender The requester of tab completion options.
	 * @param arguments The arguments passed to the command so far.
	 * @return A list of strings which are possibilities for the tab completion argument.
	 * @see SubCommand#getTabCompletions(String, Collection)
	 */
	public List<String> tabComplete(CommandSender sender, String[] arguments){
		// Get all online players, and return them as the list
		String argSoFar = arguments.length >= 2 ? arguments[1].trim().toLowerCase() : "";
		Player[] onlinePlayers = Bukkit.getOnlinePlayers();
		
		ArrayList<String> retVal = Lists.newArrayListWithExpectedSize(onlinePlayers.length / (argSoFar == null || argSoFar.length() == 0 ? 1 : (argSoFar.length() * 2) / 3)); // TODO: Check function performance, maybe make function less "magic"?
		for(Player playr : onlinePlayers){
			if(argSoFar == null || StringUtil.startsWithIgnoreCase(playr.getName(), argSoFar)){
				retVal.add(playr.getName());
			}
		}
		
		return retVal;
	}
	
}
