package me.pagekite.glen3b.library.bukkit.command;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;

/**
 * Represents the context of an invoked command.
 * @author Glen Husman
 * @param <TSender> The guaranteed type of the command sender.
 * @param <TCommand> The type of the command.
 */
public final class CommandInvocationContext<TSender extends CommandSender, TCommand> {
	
	private TSender _sender;
	private TCommand _cmd;
	private String _alias;
	
	/**
	 * Get the command that was executed.
	 * @return The {@code Command} instance which was executed, and is represented by this context.
	 */
	public TCommand getCommand(){
		return _cmd;
	}
	
	/**
	 * Get the alias used in the invocation of this command.
	 * @return The string alias used to invoke this command in this context.
	 */
	public String getInvocationAlias(){
		return _alias;
	}
	
	/**
	 * Get the sender of this command.
	 * @return The {@code CommandSender} instance which sent this command.
	 */
	public TSender getSender(){
		return _sender;
	}
	
	/**
	 * Creates an invocation context with the following parameters.
	 * @param sender The sender of the command.
	 * @param command The command being executed.
	 * @param alias The alias of the command being used for execution.
	 */
	public CommandInvocationContext(TSender sender, TCommand command, String alias){
		Validate.notNull(sender, "The sender of the command must not be null.");
		Validate.notNull(command, "The command must not be null.");
		Validate.notEmpty(alias, "The command alias must be specified.");
		
		_sender = sender;
		_cmd = command;
		_alias = alias;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_alias == null) ? 0 : _alias.hashCode());
		result = prime * result + ((_cmd == null) ? 0 : _cmd.hashCode());
		result = prime * result + ((_sender == null) ? 0 : _sender.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CommandInvocationContext)) {
			return false;
		}
		CommandInvocationContext<?, ?> other = (CommandInvocationContext<?, ?>) obj;
		if (_alias == null) {
			if (other._alias != null) {
				return false;
			}
		} else if (!_alias.equals(other._alias)) {
			return false;
		}
		if (_cmd == null) {
			if (other._cmd != null) {
				return false;
			}
		} else if (!_cmd.equals(other._cmd)) {
			return false;
		}
		if (_sender == null) {
			if (other._sender != null) {
				return false;
			}
		} else if (!_sender.equals(other._sender)) {
			return false;
		}
		return true;
	}
	
}
