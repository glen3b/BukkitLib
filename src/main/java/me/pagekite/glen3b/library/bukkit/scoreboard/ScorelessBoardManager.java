package me.pagekite.glen3b.library.bukkit.scoreboard;

import java.util.Collection;
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
	 * A class containing entries intended for use as scoreboard spacers.
	 * There are multiple values because scoreboard entries must be unique.
	 * @deprecated This class will be replaced with a more vertasile system of dealing with duplicate values, rendering it pointless as 16 character spaced strings could be used.
	 */
	@Deprecated
	public static final class Spacers{
		private Spacers(){}
		/**
		 * A string representing 16 spaces. Intended for use as the first spacer.
		 */
		public static final String FIRST = "                ";
		/**
		 * A string representing a reset color code and 14 spaces. Intended for use as the second spacer.
		 */
		public static final String SECOND = ChatColor.RESET.toString() + "              ";
		/**
		 * A string representing two reset color codes and 12 spaces. Intended for use as the third spacer.
		 */
		public static final String THIRD = ChatColor.RESET.toString() + ChatColor.RESET.toString() + "            ";
		/**
		 * A string representing three reset color codes and 10 spaces. Intended for use as the fourth spacer.
		 */
		public static final String FOURTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "          ";
		/**
		 * A string representing four reset color codes and 8 spaces. Intended for use as the fifth spacer.
		 */
		public static final String FIFTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "        ";
		/**
		 * A string representing five reset color codes and 6 spaces. Intended for use as the sixth spacer.
		 */
		public static final String SIXTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "      ";

		/**
		 * A string representing 6 reset color codes and four spaces. Intended for use as the seventh spacer.
		 */
		public static final String SEVENTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "    ";
		/**
		 * A string representing 7 reset color codes and two spaces. Intended for use as the eighth spacer.
		 */
		public static final String EIGHTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "  ";

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
		
		/**
		 * Create a scoreboard entry for the given spacer.
		 * @param spacer The spacer string.
		 * @return A scoreboard entry representing that spacer.
		 */
		public static final ScoreboardEntry createEntry(String spacer){
			return new ScoreboardEntry(spacer);
		}

	}

	/**
	 * Resets the scoreboard of the specified player, causing it to be recreated upon the next retrieval of that player's scoreboard.
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
	 * @return The scoreboard this manager owns for the specified player, or {@code null} if the player cannot be assigned a scoreboard.
	 */
	public final Scoreboard getScoreboard(Player player){
		Validate.notNull(player, "The player must not be null!");
		
		if(_trackedPlayerBoards.containsKey(player.getUniqueId())){
			return _trackedPlayerBoards.get(player.getUniqueId());
		}
		
		final ScoreboardInformation newBoard = prepareScoreboard(player);
		
		if(newBoard == null){
			_trackedPlayerBoards.put(player.getUniqueId(), null);
			return null;
		}
		
		final Scoreboard boardInstance = Bukkit.getScoreboardManager().getNewScoreboard();
		final Objective dummyObjective = boardInstance.registerNewObjective(SIDEBOARD_OBJECTIVE_NAME, "dummy");
		dummyObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		dummyObjective.setDisplayName(newBoard.getTitle().toString());
		
		List<Runnable> runnableCollection = Lists.newArrayListWithExpectedSize(2);
		runnableCollection.add(new Runnable(){ // This one cycles the title

			@Override
			public void run() {
				dummyObjective.setDisplayName(newBoard.getTitle().tick());
			}
			
		});
		
		final HashMap<ScoreboardEntry, TextCycler> entriesToCyclers = Maps.newHashMapWithExpectedSize(newBoard.getEntries().size());
		final HashMap<ScoreboardEntry, Integer> entriesToScores = Maps.newHashMapWithExpectedSize(newBoard.getEntries().size());
		
		for(int i = newBoard.getEntries().size() - 1; i >= 0; i--){
			entriesToCyclers.put(newBoard.getEntries().get(i), newBoard.getEntries().get(i).createCycler());
			entriesToScores.put(newBoard.getEntries().get(i), newBoard.getEntries().size() - i);
		}
		
		runnableCollection.add(new Runnable(){ // This one cycles each individual entry

			@Override
			public void run() {
				for(ScoreboardEntry e : newBoard.getEntries()){
					String oldVal = entriesToCyclers.get(e).toString();
					String newVal = entriesToCyclers.get(e).tick();
					if(oldVal != newVal /* Yes, this is a reference check. TextCycler should return the same reference for these situations. */){
						boardInstance.resetScores(oldVal);
						dummyObjective.getScore(newVal).setScore(entriesToScores.get(e));
					}
				}
			}
			
		});
		
		schedule(runnableCollection);
		
		_trackedPlayerBoards.put(player.getUniqueId(), boardInstance);
		
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
