package me.pagekite.glen3b.library.bukkit.command;

import org.bukkit.command.CommandSender;

/**
 * Represents an item that can be restricted via permissions.
 * @author Glen Husman
 */
public interface PermissionConstrainable {

	/**
	 * Determines if the specified player has access to use this item. Implementations of this method should return false if, for example, the sender is not a player but must be for the command to succeed.
	 * @param sender The player (or command executor) of whom to check permissions.
	 * @return {@code true} if the specified player has access to this command, {@code false} otherwise.
	 */
	public boolean hasAccess(CommandSender sender);
	
}
