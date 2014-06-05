package me.pagekite.glen3b.library.bukkit.scoreboard;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Represents an abstract scoreboard manager, which implements a skeletal framework for scoreboards.
 * The entry calculation is still delegated to subclasses.
 * <p>
 * This class does automatically assign the player a generated scoreboard upon joining, and destroys the board upon player leaving.
 * Therefore, statistical changes for dynamic values are guaranteed to appear.
 * The event listener registration for this to happen does <b>not</b> automatically happen in the constructor.
 * Therefore, event registration must be done by the calling plugin by calling the {@link #registerEvents()} method.
 * @author Glen Husman
 */
public abstract class AbstractBoardManager extends ScorelessBoardManager implements Listener {

	protected Plugin _hostPlugin;
	private long _schedulerDelay;

	/**
	 * Creates an abstract scoreboard manager.
	 * @param host The host plugin instance.
	 * @param schedulerDelay The delay, in ticks, between cycling of texts.
	 */
	public AbstractBoardManager(Plugin host, long schedulerDelay){
		Validate.notNull(host, "The host plugin is null!");
		Validate.isTrue(schedulerDelay > 0, "The scheduler delay must be positive.");

		_hostPlugin = host;
		_schedulerDelay = schedulerDelay;

	}

	/**
	 * Registers this scoreboard manager to handle events required to automatically display scoreboards.
	 */
	public final void registerEvents(){
		Bukkit.getPluginManager().registerEvents(this, _hostPlugin);
	}

	/**
	 * Resets the scoreboard of the specified player, causing it to be recreated upon the next retrieval of that player's scoreboard.
	 * Also sets the player to view the main server scoreboard, guaranteeing that updates to be made will be seen.
	 * @param player The player for whom to reset the scoreboard.
	 */
	@Override
	public void resetScoreboard(Player player){
		super.resetScoreboard(player);

		// Reset view to the main scoreboard
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	/**
	 * Gets the title of the scoreboard for the specified player.
	 * @param player The player who will be displayed the title.
	 * @return A string representing the title of the specified player's scoreboard.
	 */
	@Nonnull
	protected abstract String getTitle(Player player);

	/**
	 * Gets the prefix to the title of the scoreboard for the specified player.
	 * The default implementation of this method always returns {@code null}.
	 * @param player The player who will be displayed the title.
	 * @return A string representing the title prefix of the specified player's scoreboard, or {@code null} if there is no prefix.
	 */
	@Nullable
	protected String getTitlePrefix(Player player){
		return null;
	}

	@Override
	protected void schedule(Collection<? extends Runnable> tasks) {
		for(Runnable task : tasks){
			Bukkit.getScheduler().scheduleSyncRepeatingTask(_hostPlugin, task, _schedulerDelay, _schedulerDelay);
		}
	}

	/**
	 * Gets an ordered list of the sidebar entries for the specified player.
	 * @param player The player who will own the sidebar entries.
	 * @return A list of sidebar scoreboard entries for the specified player.
	 */
	protected abstract List<ScoreboardEntry> getSidebarEntries(Player player);

	@Override
	protected ScoreboardInformation prepareScoreboard(Player player) {
		return new ScoreboardInformation(getTitlePrefix(player), getTitle(player), getSidebarEntries(player));
	}

	// EVENT HANDLERS

	/**
	 * If the event is registered, displays the scoreboard retrieved for the joining player to the joining player.
	 * @param event The event being processed.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event){
		Scoreboard display = getScoreboard(event.getPlayer());
		if(display != null){
			event.getPlayer().setScoreboard(display);
		}
	}

	/**
	 * If the event is registered, destroys the cached scoreboard for the leaving player.
	 * @param event The event being processed.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLeaveGame(PlayerQuitEvent event){
		resetScoreboard(event.getPlayer());
	}

	/**
	 * If the event is registered, destroys the cached scoreboard for the leaving player.
	 * @param event The event being processed.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLeaveGame(PlayerKickEvent event){
		resetScoreboard(event.getPlayer());
	}

}
