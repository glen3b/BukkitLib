package me.pagekite.glen3b.library.bukkit.command.annotation;

/**
 * Represents a type of tab completion.
 * @author Glen Husman
 */
public enum TabCompleteMode {

	/**
	 * Represents tab completing from a constant list of strings.
	 */
	CONSTANT_LIST,
	/**
	 * Represents tab completing from the server player list.
	 * This is the default mode.
	 */
	PLAYER_LIST,
	/**
	 * Represents tab completing from the world player list of the sender.
	 * The server player list will be used if the sender cannot be mapped to a world.
	 */
	WORLD_PLAYER_LIST,
	/**
	 * Represents that the tab completion of an argument should be delegated to the {@link org.bukkit.command.TabCompleter#onTabComplete(org.bukkit.command.CommandSender, org.bukkit.command.Command, String, String[]) onTabComplete} method.
	 */
	DELEGATE_TO_METHOD;
	
}
