package me.pagekite.glen3b.library.bukkit.scoreboard;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import me.pagekite.glen3b.library.bukkit.TextCycler;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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
	 * Represents a 16 character spacer string which can be used as a scoreboard entry.
	 * Duplicate values will be dealt with in the new system.
	 */
	public static final String SPACER_ENTRY = "                ";
	
	/**
	 * A class containing entries intended for use as scoreboard spacers.
	 * There are multiple values because scoreboard entries must be unique.
	 * @deprecated This class will be replaced with a more vertasile system of dealing with duplicate values, rendering it pointless as {@link ScorelessBoardManager#SPACER_ENTRY} can be used.
	 */
	@Deprecated
	public static final class Spacers{
		private Spacers(){}
		/**
		 * A string representing 16 spaces. Intended for use as the first spacer.
		 * @deprecated Use {@link ScorelessBoardManager#SPACER_ENTRY}.
		 */
		@Deprecated
		public static final String FIRST = "                ";
		/**
		 * A string representing a reset color code and 14 spaces. Intended for use as the second spacer.
		 * @deprecated Will not be used, being replaced with a more vertasile system of dealing with duplicate values.
		 */
		@Deprecated
		public static final String SECOND = ChatColor.RESET.toString() + "              ";
		/**
		 * A string representing two reset color codes and 12 spaces. Intended for use as the third spacer.
		 * @deprecated Will not be used, being replaced with a more vertasile system of dealing with duplicate values.
		 */
		@Deprecated
		public static final String THIRD = ChatColor.RESET.toString() + ChatColor.RESET.toString() + "            ";
		/**
		 * A string representing three reset color codes and 10 spaces. Intended for use as the fourth spacer.
		 * @deprecated Will not be used, being replaced with a more vertasile system of dealing with duplicate values.
		 */
		@Deprecated
		public static final String FOURTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "          ";
		/**
		 * A string representing four reset color codes and 8 spaces. Intended for use as the fifth spacer.
		 * @deprecated Will not be used, being replaced with a more vertasile system of dealing with duplicate values.
		 */
		@Deprecated
		public static final String FIFTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "        ";
		/**
		 * A string representing five reset color codes and 6 spaces. Intended for use as the sixth spacer.
		 * @deprecated Will not be used, being replaced with a more vertasile system of dealing with duplicate values.
		 */
		@Deprecated
		public static final String SIXTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "      ";

		/**
		 * A string representing 6 reset color codes and four spaces. Intended for use as the seventh spacer.
		 * @deprecated Will not be used, being replaced with a more vertasile system of dealing with duplicate values.
		 */
		@Deprecated
		public static final String SEVENTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "    ";
		/**
		 * A string representing 7 reset color codes and two spaces. Intended for use as the eighth spacer.
		 * @deprecated Will not be used, being replaced with a more vertasile system of dealing with duplicate values.
		 */
		@Deprecated
		public static final String EIGHTH = ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + ChatColor.RESET.toString() + "  ";

		/**
		 * Gets the spacer with the specified number.
		 * @param number The number representing the occurence index of the spacer, base one (a value of {@code 1} will return the first spacer).
		 * @return A spacer value, or {@code null} if not found for that number.
		 * @deprecated Uses deprecated spacer fields, and is obselete due to the upcoming vertasile system of dealing with duplicate values.
		 */
		@Deprecated
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
		 * @deprecated Will not be needed, use {@link ScoreboardEntry#ScoreboardEntry(String)} with {@link ScorelessBoardManager#SPACER_ENTRY}.
		 */
		@Deprecated
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
	
	private final class TitleCycler extends BukkitRunnable{
		public UUID playerID;
		public ScoreboardInformation scoreInfo;
		
		public void dispose(){
			cancel();
			playerID = null;
			scoreInfo = null;
		}
		
		@Override
		public void run() {
			if(_trackedPlayerBoards == null || playerID == null || scoreInfo == null){
				dispose();
				return;
			}
			
			Scoreboard board = _trackedPlayerBoards.get(playerID);
			
			if(board == null){
				dispose();
				return;
			}
			
			Objective objective = board.getObjective(SIDEBOARD_OBJECTIVE_NAME);
			
			if(objective == null){
				dispose();
				return;
			}
			
			objective.setDisplayName(scoreInfo.getTitle().tick());
		}
	}
	
	/**
	 * Gets a scoreboard for the specified player.
	 * This method will return cached scoreboards if they exist, and create new ones if need be.
	 * Note that this method will only display proper information after the first invocation of the {@code Runnable}s passed to {@link #schedule(Collection)}.
	 * @param player The player who will own the scoreboard.
	 * @return The scoreboard this manager owns for the specified player, or {@code null} if the player cannot be assigned a scoreboard.
	 */
	public final Scoreboard getScoreboard(final Player player){
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
		
		List<BukkitRunnable> runnableCollection = Lists.newArrayListWithExpectedSize(2);
		TitleCycler title = new TitleCycler();
		title.playerID = player.getUniqueId();
		title.scoreInfo = newBoard;
		runnableCollection.add(title);
		
		final Map<ScoreboardEntry, TextCycler> entriesToCyclers = Maps.newHashMapWithExpectedSize(newBoard.getEntries().size());
		final Map<ScoreboardEntry, Integer> entriesToScores = Maps.newHashMapWithExpectedSize(newBoard.getEntries().size());
		final Map<String, Team> prefixesToTeams = Maps.newHashMap();
		
		
		for(int i = newBoard.getEntries().size() - 1; i >= 0; i--){
			entriesToCyclers.put(newBoard.getEntries().get(i), newBoard.getEntries().get(i).createCycler());
			entriesToScores.put(newBoard.getEntries().get(i), newBoard.getEntries().size() - i);
			
			if(!prefixesToTeams.containsKey(newBoard.getEntries().get(i).getPrefix())){
				String teamName = StringUtils.trimToEmpty(newBoard.getEntries().get(i).getPrefix().replace(ChatColor.COLOR_CHAR, '&').replace(' ', '-'));
				if(teamName.length() > 16){
					teamName = teamName.substring(0, 16);
				}
				Team value = boardInstance.registerNewTeam(teamName);
				value.setPrefix(newBoard.getEntries().get(i).getPrefix());
				prefixesToTeams.put(newBoard.getEntries().get(i).getPrefix(), value);
			}
		}
		
		runnableCollection.add(new BukkitRunnable(){ // This one cycles each individual entry

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				for(ScoreboardEntry e : newBoard.getEntries()){
					TextCycler cycler = entriesToCyclers.get(e);
					String oldVal = cycler.toString();
					String newVal = cycler.tick();
					if(oldVal != newVal /* Yes, this is a reference check. TextCycler should return the same reference for these situations. */){
						prefixesToTeams.get(e.getPrefix()).removePlayer(Bukkit.getOfflinePlayer(oldVal)); // Workaround for lack of API - getOfflinePlayer should not be used
						prefixesToTeams.get(e.getPrefix()).addPlayer(Bukkit.getOfflinePlayer(newVal)); // Workaround for lack of API - getOfflinePlayer should not be used
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
	 * The map of player UUIDs to prepared scoreboards that are owned by them.
	 */
	protected final Map<UUID, Scoreboard> _trackedPlayerBoards = Maps.newHashMap();
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
