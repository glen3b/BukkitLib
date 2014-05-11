package me.pagekite.glen3b.library.bukkit.command;

import org.bukkit.entity.Player;

/**
 * Represents an object that can handle the execution of a preprocessed command.
 * @author Glen Husman
 */
public interface PreprocessedCommandHandler {

	/**
	 * Handles a command that was executed.
	 * @param sender The executor of the command.
	 * @param command The command being executed.
	 * @param alias The alias used to execute the command.
	 * @param arguments The arguments passed to the command, not counting the command itself.
	 * @return Whether to cancel the preprocessor event. This value should generally be {@code true}.
	 */
	public boolean onCommand(Player sender, PreprocessableCommand command, String alias, String[] arguments);
	
}
