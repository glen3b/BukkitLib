package me.pagekite.glen3b.library.bukkit.command;

/**
 * Represents a command of which access can be controlled via permissions.
 * @author Glen Husman
 */
public interface PermissionConstrainedCommand extends PermissionConstrainable {

	/**
	 * Gets the name of this command, which will be used as a display and execution name. This is used by GBukkitCore for tab completions.
	 * @return The executable name of this command.
	 */
	public String getName();
	
}
