package me.pagekite.glen3b.library.bukkit.scoreboard;

import me.pagekite.glen3b.library.bukkit.TextCycler;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

/**
 * Represents an entry in a "scoreless" scoreboard.
 * This class can also manipulate text cyclers.
 */
public final class ScoreboardEntry{

	private String _value;
	private String _prefix;

	/**
	 * Creates a scoreboard entry with the specified textual value.
	 * @param value The textual value.
	 */
	public ScoreboardEntry(String value){
		this((String)null, value);
	}

	/**
	 * Creates a scoreboard entry with the specified textual value and color code prefix.
	 * @param prefix The color prefix for the value.
	 * @param value The textual value.
	 */
	public ScoreboardEntry(ChatColor prefix, String value){
		this(new ChatColor[]{prefix}, value);
	}

	/**
	 * Creates a scoreboard entry with the specified textual value and mutliple color code prefix.
	 * @param prefix The color prefixes for the value, in order.
	 * @param value The textual value.
	 */
	public ScoreboardEntry(ChatColor[] prefix, String value){
		Validate.notNull(value, "A text value must be specified.");

		StringBuilder prefixBuilder = new StringBuilder(prefix == null ? 0 : prefix.length * 2);
		if(prefix != null){
			for(int i = 0; i < prefix.length; i++){
				Validate.notNull(prefix[i], "Null prefixes are not allowed.");
				prefixBuilder.append(prefix[i]);
			}
		}

		_value = value;
		_prefix = prefixBuilder.toString();
	}

	/**
	 * Creates a scoreboard entry with the specified textual value and prefix.
	 * @param prefix The textual value prefix, such as a color.
	 * @param value The textual value.
	 */
	public ScoreboardEntry(String prefix, String value){
		Validate.notNull(value, "A text value must be specified.");

		_value = value;
		_prefix = prefix == null ? "": prefix;
	}

	/**
	 * Get the text of the scoreboard entry.
	 * @return The untrimmed text of the scoreboard entry, excluding the prefix.
	 */
	public String getValue(){
		return _value;
	}

	/**
	 * Get the prefix of the scoreboard entry.
	 * @return The untrimmed prefix of the scoreboard entry, which may be blank if there is no prefix.
	 */
	public String getPrefix(){
		return _prefix;
	}

	/**
	 * Creates and returns a new text cycler, which cycles through this scoreboard manager entry text value.
	 * @return A new {@link TextCycler} instance, which cycles through scoreboard entry text.
	 */
	public TextCycler createCycler(){
		return new TextCycler(getPrefix(), getValue(), 15);
	}

	@Override
	public String toString(){
		return getPrefix() + getValue();
	}

	@Override
	public int hashCode() {
		final int prime = 3119;
		int result = 83;
		result = prime * result
				+ ((_prefix == null) ? 0 : _prefix.hashCode());
		result = prime * result
				+ ((_value == null) ? 0 : _value.hashCode());
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
		if (!(obj instanceof ScoreboardEntry)) {
			return false;
		}
		ScoreboardEntry other = (ScoreboardEntry) obj;
		if (_prefix == null) {
			if (other._prefix != null) {
				return false;
			}
		} else if (!_prefix.equals(other._prefix)) {
			return false;
		}
		if (_value == null) {
			if (other._value != null) {
				return false;
			}
		} else if (!_value.equals(other._value)) {
			return false;
		}
		return true;
	}

}