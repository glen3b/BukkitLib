package me.pagekite.glen3b.library.bukkit.command;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

/**
 * Represents a specific type of command sender.
 * @see CommandSender
 * @author Glen Husman
 */
public enum CommandSenderType {

	/**
	 * Represents the local console.
	 * @see ConsoleCommandSender
	 */
	LOCAL_CONSOLE(ConsoleCommandSender.class),
	/**
	 * Represents a remote console connection.
	 * @see RemoteConsoleCommandSender
	 */
	REMOTE_CONSOLE(RemoteConsoleCommandSender.class),
	/**
	 * Represents a command block.
	 * @see BlockCommandSender
	 */
	COMMAND_BLOCK(BlockCommandSender.class),
	/**
	 * Represents a command block minecart.
	 * @see CommandMinecart
	 */
	COMMAND_BLOCK_MINECART(CommandMinecart.class),
	/**
	 * Represents a player.
	 * @see Player
	 */
	PLAYER(Player.class),
	/**
	 * Represents any and all command senders.
	 */
	ALL(CommandSender.class);
	
	private Class<? extends CommandSender> _clazz;
	
	private CommandSenderType(Class<? extends CommandSender> clazz){
		_clazz = clazz;
	}
	
	/**
	 * Gets the {@link java.lang.Class Class} instance which represents this type of command sender.
	 * @return The java type representing all instances of this classification of command sender.
	 */
	public Class<? extends CommandSender> getSenderType(){
		return _clazz;
	}
	
	/**
	 * Determines if the specified {@code Class} falls under this classification.
	 * @param type The class of {@code CommandSender} to check.
	 * @return {@code true} if and only if type is a non-{@code null} {@code Class} representing the type returned by {@link CommandSenderType#getSenderType() getSenderType}.
	 */
	public boolean isInstance(Class<?> type){
		return type != null && getSenderType().isAssignableFrom(type);
	}
	
	/**
	 * Determines if the specified {@code CommandSender} falls under this classification.
	 * @param sender The {@code CommandSender} to check.
	 * @return {@code true} if and only if sender is a non-{@code null} instance of the type returned by {@link CommandSenderType#getSenderType() getSenderType}.
	 */
	public boolean isInstance(CommandSender sender){
		if(sender == null){
			return false;
		}
		
		return getSenderType().isAssignableFrom(sender.getClass());
	}
	
}
