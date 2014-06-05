package me.pagekite.glen3b.library.bukkit.scoreboard;

import java.util.Collections;
import java.util.List;

import me.pagekite.glen3b.library.bukkit.TextCycler;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Lists;

/**
 * Represents information about a soon-to-be-created scoreboard.
 * This type is intended for returning information that 
 */
public final class ScoreboardInformation{
	/**
	 * Creates a scoreboard information instance.
	 * @param title The title of the scoreboard.
	 * @param entries The entries to include in the scoreboard, in order from top to bottom as they should appear.
	 */
	public ScoreboardInformation(String title, ScoreboardEntry... entries){
		this(null, title, entries);
	}

	/**
	 * Creates a scoreboard information instance.
	 * @param prefix The prefix of the title of the scoreboard.
	 * @param title The title of the scoreboard.
	 * @param entries The entries to include in the scoreboard, in order from top to bottom as they should appear.
	 */
	public ScoreboardInformation(String prefix, String title, ScoreboardEntry... entries){
		this(prefix, title, Lists.newArrayList(entries));
	}

	/**
	 * Creates a scoreboard information instance.
	 * @param title The title of the scoreboard.
	 * @param entries The entries to include in the scoreboard, in order from top to bottom as they should appear.
	 */
	public ScoreboardInformation(String title, List<ScoreboardEntry> entries){
		this(null, title, entries);
	}

	/**
	 * Creates a scoreboard information instance.
	 * @param prefix The prefix of the title of the scoreboard.
	 * @param title The title of the scoreboard.
	 * @param entries The entries to include in the scoreboard, in order from top to bottom as they should appear.
	 */
	public ScoreboardInformation(String prefix, String title, List<ScoreboardEntry> entries){
		Validate.notNull(title, "The scoreboard title must not be null.");
		Validate.noNullElements(entries, "Null scoreboard entries are not allowed. Consider using the Spacers class.");
		
		_entries = Collections.unmodifiableList(entries);
		_title = new TextCycler(prefix, title, 32);
	}
	
	private List<ScoreboardEntry> _entries;
	private TextCycler _title;
	
	/**
	 * Gets the single cycler reference held by this class which will cycle through the title.
	 * @return A {@code TextCycler} that cycles through the scoreboard title.
	 */
	public TextCycler getTitle(){
		return _title;
	}
	
	/**
	 * Gets an unmodifiable collection of existing scoreboard entries. This collection is guaranteed to iterate in the order (from top to bottom) that the entries should appear in to the player.
	 * @return An unmodifiable collection of scoreboard entries.
	 */
	public List<ScoreboardEntry> getEntries(){
		return _entries;	
	}

	@Override
	public int hashCode() {
		final int prime = 997;
		int result = 31;
		result = prime * result
				+ ((_entries == null) ? 0 : _entries.hashCode());
		result = prime * result
				+ ((_title == null) ? 0 : _title.hashCode());
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
		if (!(obj instanceof ScoreboardInformation)) {
			return false;
		}
		ScoreboardInformation other = (ScoreboardInformation) obj;
		if (_entries == null) {
			if (other._entries != null) {
				return false;
			}
		} else if (!_entries.equals(other._entries)) {
			return false;
		}
		if (_title == null) {
			if (other._title != null) {
				return false;
			}
		} else if (!_title.equals(other._title)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ScoreboardInformation [title=" + getTitle()
				+ ", entries=" + getEntries() + "]";
	}
}
