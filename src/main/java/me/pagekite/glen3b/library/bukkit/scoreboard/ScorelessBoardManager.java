package me.pagekite.glen3b.library.bukkit.scoreboard;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import me.pagekite.glen3b.library.bukkit.TextCycler;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * Represents a sidebar scoreboard manager which does <i>not</i> utilize Minecraft's score feature except for ordering.
 * A different scoreboard wrapper is used for each player.
 * Note that this class does <em>not</em> set any players to have its scoreboard, and implementers must do this.
 * It is suggested that this be done via {@link org.bukkit.entity.Player#setScoreboard(Scoreboard)} in {@link PlayerJoinEvent}.
 * <p>
 * <b>Implementers of this class are expected to:</b>
 * <ul>
 * <li>Create scoreboards using the factory methods provided by this class.
 * <li>Display the scoreboards created by in some manner to players.
 * <li>Override the appropriate abstract methods, including those required for scheduling and scoreboard entry generation.
 * </ul>
 * </p>
 * <p>
 * This class does <b>not</b>, by default, dispose of scoreboards at any point. The ticker will keep running even for players who are not online.
 * </p>
 * @author Glen Husman
 */
public abstract class ScorelessBoardManager {

	/**
	 * Represents a scoreboard manager entry.
	 */
	public static final class Entry{

		private String _value;
		private String _prefix;

		/**
		 * Creates a scoreboard entry with the specified textual value.
		 * @param value The textual value.
		 */
		public Entry(String value){
			this(null, value);
		}

		/**
		 * Creates a scoreboard entry with the specified textual value and prefix.
		 * @param prefix The textual value prefix, such as a color.
		 * @param value The textual value.
		 */
		public Entry(String prefix, String value){
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
			return _value;
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
			if (!(obj instanceof Entry)) {
				return false;
			}
			Entry other = (Entry) obj;
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

	/**
	 * A class containing entries intended for use as scoreboard spacers.
	 * There are multiple values because scoreboard entries must be unique.
	 */
	public static final class Spacers{
		private Spacers(){}
		/**
		 * A string representing 15 spaces. Intended for use as the first spacer.
		 */
		public static final String FIRST = "               ";
		/**
		 * A string representing a reset color code and 13 spaces. Intended for use as the second spacer.
		 */
		public static final String SECOND = ChatColor.RESET.toString() + "             ";
		/**
		 * A string representing two reset color codes and 11 spaces. Intended for use as the third spacer.
		 */
		public static final String THIRD = ChatColor.RESET.toString() + ChatColor.RESET.toString() + "           ";
		/**
		 * A string representing three reset color codes and 9 spaces. Intended for use as the fourth spacer.
		 */
		public static final String FOURTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "         ";
		/**
		 * A string representing four reset color codes and 7 spaces. Intended for use as the fifth spacer.
		 */
		public static final String FIFTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "       ";
		/**
		 * A string representing five reset color codes and 5 spaces. Intended for use as the sixth spacer.
		 */
		public static final String SIXTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "     ";

		/**
		 * A string representing 6 reset color codes and three spaces. Intended for use as the seventh spacer.
		 */
		public static final String SEVENTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "   ";
		/**
		 * A string representing 7 reset color codes and one space. Intended for use as the eighth spacer.
		 */
		public static final String EIGHTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + " ";

		/**
		 * Gets the spacer with the specified number.
		 * @param number The number representing the occurence index of the spacer, base one (a value of {@code 1} will return the first spacer).
		 * @return A spacer value, or {@code null} if not found for that number.
		 */
		public static final String getSpacer(int number){
			switch(number){
			case 1:
				return FIRST;
			case 2:
				return SECOND;
			case 3:
				return THIRD;
			case 4:
				return FOURTH;
			case 5:
				return FIFTH;
			case 6:
				return SIXTH;
			case 7:
				return SEVENTH;
			case 8:
				return EIGHTH;
			}

			return null;
		}

	}

	/**
	 * Resets the scoreboard of the specified player, causing it to be recreated upon next get.
	 * A call to this method does not, without subclass implementation, guarantee that the player will immediately see any change in scoreboard.
	 * @param player The player for whom to reset the scoreboard.
	 */
	public void resetScoreboard(Player player){
		Validate.notNull(player, "The player must not be null.");
		
		_trackedPlayerBoards.remove(player.getUniqueId());
	}
	
	/**
	 * Gets a scoreboard for the specified player.
	 * This method will return cached scoreboards if they exist, and create new ones if need be.
	 * Note that this method will only display proper information after the first invocation of the {@code Runnable}s passed to {@link #schedule(Collection)}.
	 * @param player The player who will own the scoreboard.
	 * @return The scoreboard this manager owns for the specified player.
	 */
	public Scoreboard getScoreboard(Player player){
		Validate.notNull(player, "The player must not be null!");
		
		if(_trackedPlayerBoards.get(player.getUniqueId()) != null){
			return _trackedPlayerBoards.get(player.getUniqueId());
		}
		
		final ScoreboardInformation newBoard = prepareScoreboard(player);
		final Scoreboard boardInstance = Bukkit.getScoreboardManager().getNewScoreboard();
		final Objective dummyObjective = boardInstance.registerNewObjective(SIDEBOARD_OBJECTIVE_NAME, "dummy");
		dummyObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		List<Runnable> runnableCollection = Lists.newArrayListWithExpectedSize(2);
		runnableCollection.add(new Runnable(){ // This one cycles the title

			@Override
			public void run() {
				dummyObjective.setDisplayName(newBoard.getTitle().tick());
			}
			
		});
		
		final HashMap<Entry, TextCycler> entriesToCyclers = Maps.newHashMapWithExpectedSize(newBoard.getEntries().size());
		final HashMap<Entry, Integer> entriesToScores = Maps.newHashMapWithExpectedSize(newBoard.getEntries().size());
		
		for(int i = newBoard.getEntries().size(); i > 0; i--){
			entriesToCyclers.put(newBoard.getEntries().get(i - 1), newBoard.getEntries().get(i - 1).createCycler());
			entriesToScores.put(newBoard.getEntries().get(i - 1), i);
		}
		
		runnableCollection.add(new Runnable(){ // This one cycles each individual entry

			@Override
			public void run() {
				for(Entry e : newBoard.getEntries()){
					boardInstance.resetScores(entriesToCyclers.get(e).toString());
					dummyObjective.getScore(entriesToCyclers.get(e).tick()).setScore(entriesToScores.get(e));
				}
			}
			
		});
		
		schedule(runnableCollection);
		
		return boardInstance;
	}
	
	/**
	 * The mao of player UUIDs to prepared scoreboards.
	 */
	protected HashMap<UUID, Scoreboard> _trackedPlayerBoards = new HashMap<UUID, Scoreboard>();
	/**
	 * The name of the sideboard objective which is displayed to the player.
	 */
	protected static final String SIDEBOARD_OBJECTIVE_NAME = "scoreboard";

	/**
	 * Represents information about a soon-to-be-created scoreboard.
	 */
	protected static final class ScoreboardInformation{
		/**
		 * Creates a scoreboard information instance.
		 * @param title The title of the scoreboard.
		 * @param entries The entries to include in the scoreboard, in order from top to bottom as they should appear.
		 */
		public ScoreboardInformation(String title, Entry... entries){
			this(null, title, entries);
		}

		/**
		 * Creates a scoreboard information instance.
		 * @param prefix The prefix of the title of the scoreboard.
		 * @param title The title of the scoreboard.
		 * @param entries The entries to include in the scoreboard, in order from top to bottom as they should appear.
		 */
		public ScoreboardInformation(String prefix, String title, Entry... entries){
			this(prefix, title, Lists.newArrayList(entries));
		}

		/**
		 * Creates a scoreboard information instance.
		 * @param title The title of the scoreboard.
		 * @param entries The entries to include in the scoreboard, in order from top to bottom as they should appear.
		 */
		public ScoreboardInformation(String title, List<Entry> entries){
			this(null, title, entries);
		}

		/**
		 * Creates a scoreboard information instance.
		 * @param prefix The prefix of the title of the scoreboard.
		 * @param title The title of the scoreboard.
		 * @param entries The entries to include in the scoreboard, in order from top to bottom as they should appear.
		 */
		public ScoreboardInformation(String prefix, String title, List<Entry> entries){
			Validate.notNull(title, "The scoreboard title must not be null.");
			Validate.noNullElements(entries, "Null scoreboard entries are not allowed. Consider using the Spacers class.");
			
			_entries = Collections.unmodifiableList(entries);
			_title = new TextCycler(prefix, title, 15);
		}
		
		private List<Entry> _entries;
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
		public List<Entry> getEntries(){
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

	/**
	 * Schedules multiple tasks to run under a parent plugin. All tasks will be for scoreboard text cycling.
	 * This method may use any interval for the desired UI appearance, but the tasks must be repeating and executed on the main thread.
	 * @param tasks The tasks to schedule.
	 */
	protected abstract void schedule(Collection<? extends Runnable> tasks);

	/**
	 * Prepares a scoreboard for the specified player. Only called when a cached scoreboard is not available. To force a call to this method, {@link #resetScoreboard(Player)} may be called.
	 * @param player The player who will own the scoreboard.
	 * @return A set of scoreboard entries that represents the player scoreboard. The iteration order of the collection containted in the returned object must represent the order of scoreboard entries, from top to bottom, to display to the player.
	 */
	protected abstract ScoreboardInformation prepareScoreboard(Player player);
}
