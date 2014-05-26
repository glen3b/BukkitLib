package me.pagekite.glen3b.library.bukkit.protocol;

/**
 * Represents the result of an operation involving or relating to protocol
 * modification. This enum represents specific cases that are documented in
 * further detail with each method that returns this type.
 * 
 * @author Glen Husman
 */
public enum ProtocolOperationResult {
	/**
	 * The protocol operation failed because an appropriate library was not
	 * available to handle the necessary operations required for the operation's completion.
	 */
	LIBRARY_NOT_AVAILABLE(false),
	/**
	 * Represents a generic protocol operation failure.
	 */
	FAILURE(false),
	/**
	 * Represents a protocol operation failure due to an argument not being of
	 * the correct reflective type. This value may be returned when a
	 * CraftBukkit implementation class is expected, but a Bukkit API type is
	 * given. This value should only be returned if an argument is of the
	 * incorrect runtime type. The closest possible type should be enforced at
	 * compile time.
	 */
	FAILURE_INCORRECT_ARGUMENT_TYPE(false),
	/**
	 * Represents a successful operation that has not yet taken place, but
	 * should unless an unexpected error occurs.
	 */
	SUCCESS_QUEUED(true),
	/**
	 * Represents a successful operation that has taken place and the intended
	 * result was the actual result.
	 */
	SUCCESS(true),
	/**
	 * Represents that a protocol operation cannot happen without conflicting
	 * with existing data. This value will only be returned if the intended
	 * result is already present without packet modification. Calling {@link
	 * #succeeded()} on this value will return {@code true}.
	 */
	NOT_NEEDED(true);

	private boolean _success;

	private ProtocolOperationResult(boolean success) {
		_success = success;
	}

	/**
	 * Determines whether the protocol operation ended with having the intended
	 * affect.
	 * 
	 * @return Whether the change was applied successfully.
	 */
	public boolean succeeded() {
		return _success;
	}
}
