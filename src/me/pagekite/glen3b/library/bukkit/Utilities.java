/*
   This file is part of GBukkitLib.

    GBukkitLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GBukkitLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with GBukkitLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.pagekite.glen3b.library.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import me.pagekite.glen3b.library.bukkit.teleport.QueuedTeleport;
import me.pagekite.glen3b.library.bukkit.teleport.TeleportationManager;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * A static class housing common Bukkit methods and constants.
 * <p>
 * <b>This class will be refactored into child classes in a future update.</b> When this happens, all method overloads will reroute to the refactored versions, and will lose documentation and become deprecated.
 * The documentation will be a link to the new version, which will contain the true method documentation.
 * </p>
 * 
 * @author Glen Husman
 */
public final class Utilities {

	/**
	 * The target number of server ticks per second.
	 * One server tick, on a server which is running at perfect target framerate, is 0.05 seconds.
	 */
	public static final long TICKS_PER_SECOND = 20L;
	
	/**
	 * The target number of server ticks per minute.
	 */
	public static final long TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;
	
	/**
	 * Gets a list of the {@link UUID}s of all currently online players.
	 * <p>
	 * This will be a list of Mojang-provided UUIDs unless all of the following are not true:
	 * <ol>
	 * <li>The server is in offline mode.</li>
	 * <li>The server does not have a properly configured proxy which supports IP and UUID forwarding when used in conjunction with this Bukkit implementation.</li>
	 * </ol>
	 * 
	 * If not all of these are true, the UUID will be calculated based on a hash of the username.
	 * </p>
	 * 
	 * If all of the above are true, the UUIDs returned by this method should be equivalent to those that would be returned from the <a href="https://github.com/Mojang/AccountsClient">Mojang account client utility</a>.
	 * 
	 * @return A mutable list of all of the unique identifiers of all of the players currently online on the {@code Bukkit} server.
	 * @see Server#getOnlinePlayers()
	 * @see Player#getUniqueId()
	 */
	public static List<UUID> getOnlinePlayerIDs(){
		ArrayList<UUID> players = new ArrayList<UUID>();
		
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			players.add(player.getUniqueId());
		}
		
		return players;
	}
	
	/**
	 * Gets a list of the names of all currently online players. Keep in mind that names are no longer safe as persistent cross-session unique identifiers.
	 * @return A mutable list of all of the usernames of all of the players currently online on the {@code Bukkit} server.
	 * @see Server#getOnlinePlayers()
	 * @see Player
	 */
	public static List<String> getOnlinePlayerNames(){
		ArrayList<String> players = new ArrayList<String>();
		
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			players.add(player.getName());
		}
		
		return players;
	}
	
	/**
	 * Run the specified tasks after the completion of the specified teleport. This method is intended to wrap calls to {@link TeleportationManager} methods which may return a {@code null} {@link QueuedTeleport}. If the method returns {@code null} and that value is passed into this method, the tasks will run instantly after the teleport, as was intended, without an additional {@code null} check in client code.
	 * @param <T> The type of the destination of the teleport.
	 * @param teleport The teleport to scedule tasks for. If this is {@code null} or cancelled, the tasks will be run instantly.
	 * @param tasks The tasks to run.
	 * @return Whether the tasks were queued. The return value will be {@code false} if they ran instantly during the execution of this method and {@code true} if they were queued for execution and consequently have not yet run.
	 * @see TeleportationManager#teleportPlayer(Player player, Location targetLoc)
	 */
	public static <T> boolean runAfterTeleport(QueuedTeleport<T> teleport, Runnable... tasks){
		Validate.noNullElements(tasks, "There must not be any null tasks.");
		
		if(teleport == null || teleport.isCancelled()){
			// Instant execution
			for(Runnable task : tasks){
				task.run();
			}
			
			return false;
		}else{
			// Queue execution
			for(Runnable task : tasks){
				teleport.registerOnTeleport(task);
			}
			
			return true;
		}
	}
	
	/**
	 * Attempt to parse {@code str} as an integer.
	 * @param str The string to attempt to parse.
	 * @return A {@code boolean} indicating if the parsing of the string was successful.
	 * @deprecated Use {@link Utilities#parseInt(String, int)} instead, it does not require a double parse to obtain the value.
	 */
	@Deprecated
	public static boolean isInt(String str){
		if(str == null || str.trim().isEmpty()){
			return false;
		}
		
		try{
			Integer.parseInt(str);
		}catch(Throwable thr){
			return false;
		}
		
		return true;
    }
	
	/**
	 * Attempt to parse {@code str} as an integer, returning a default value if it is not possible.
	 * @param str The string to attempt to parse.
	 * @param defaultVal The value to return if {@code str} cannot be parsed.
	 * @return {@code str} as an integer if it is a valid, parseable integer; {@code defaultVal} otherwise.
	 */
	public static int parseInt(String str, int defaultVal){
		if(str == null || str.trim().isEmpty()){
			return defaultVal;
		}
		
		try{
			return Integer.parseInt(str);
		}catch(Throwable thr){
			return defaultVal;
		}
    }
	
	/**
	 * Schedules a task to execute on the main server thread under the brand of the GBukkitLib plugin after one tick.
	 * @param task The task to execute on the main server thread after one server tick. It must not be {@code null}.
	 * @return The ID of the scheduled task.
	 * @deprecated Please use {@link Utilities#scheduleTickTask(Plugin, Runnable)} whilst passing in your own plugin instance.
	 * @see Utilities#scheduleTickTask(Plugin, Runnable, boolean)
	 */
	@Deprecated
	public static int scheduleTickTask(Runnable task){
		return scheduleTickTask(null, task);
	}
	
	/**
	 * Schedules a task to execute after one tick.
	 * @param host The plugin under which to schedule this task. If this parameter is {@code null}, the GBukkitLib plugin instance as retrieved by the {@code PluginManager} will be used for scheduling. <b>Using this method with a {@code null} plugin argument is deprecated, and this functionality will be removed in a future release.</b>
	 * @param task The task to execute on the main server thread after one server tick. It must not be {@code null}.
	 * @param Whether to run this task asynchronously. If this is true, the task will be executed on a separate thread from the main server thread. Asynchronous tasks should <b>never</b> access any Bukkit API other than the scheduler, which can be used to schedule a synchronous task. Synchronous tasks block the main server thread, but have the liberty of full Bukkit API access.
	 * @return The scheduled task as returned by the bukkit scheduler.
	 * @see org.bukkit.scheduler.BukkitScheduler#runTaskLater(Plugin plugin, Runnable task, long delay)
	 * @see org.bukkit.scheduler.BukkitScheduler#runTaskLaterAsynchronously(Plugin, Runnable, long)
	 */
	public static BukkitTask scheduleTickTask(Plugin host, Runnable task, boolean async){
		Validate.notNull(task, "The task must not be null.");
		
		Plugin hostPl = host == null ? Bukkit.getServer().getPluginManager().getPlugin("GBukkitLib") : host;
		
		return async ? Bukkit.getScheduler().runTaskLater(hostPl, task, 1L) : Bukkit.getScheduler().runTaskLaterAsynchronously(hostPl, task, 1L);
	}
	
	/**
	 * Schedules a task to execute on the main server thread after one tick.
	 * @param host The plugin under which to schedule this task. If this parameter is {@code null}, the GBukkitLib plugin instance as retrieved by the {@code PluginManager} will be used for scheduling. <b>Using this method with a {@code null} plugin argument is deprecated, and this functionality will be removed in a future release.</b>
	 * @param task The task to execute on the main server thread after one server tick. It must not be {@code null}.
	 * @return The ID of the scheduled task.
	 * @see Utilities#scheduleTickTask(Plugin, Runnable, boolean)
	 */
	public static int scheduleTickTask(Plugin host, Runnable task){
		return scheduleTickTask(host, task, false).getTaskId();
	}
	
	/**
	 * Sets the display name of the specified item. <b>It also removes any existing lore.</b>
	 * The item that is passed in should not be assumed to be unmodified after the operation.
	 * @param item The item to modify the data of.
	 * @param name The new display name of the item.
	 * @return The modified item.
	 */
	public static ItemStack setItemName(ItemStack item, String name) {
		return setItemNameAndLore(item, name, null);
	}
	
	/**
	 * Sets the display name of the specified item. It also removes any lore.
	 * The item that is passed in should not be assumed to be unmodified after the operation.
	 * @param item The item to modify the data of.
	 * @param name The new display name of the item.
	 * @return The modified item.
	 * @deprecated Use {@link Utilities#setItemName(ItemStack, String)}, it has a less misleading name.
	 */
	@Deprecated
	public static ItemStack setItemNameAndLore(ItemStack item, String name) {
		return setItemNameAndLore(item, name, new String[0]);
	}
	
	// TODO: Is this safe? Is there a better way? I want to avoid creating too many instances.
	private static List<String> emptyList = Collections.unmodifiableList(new ArrayList<String>(0));
	
	/**
	 * Sets the display name and lore of the specified item.
	 * The item that is passed in should not be assumed to be unmodified after the operation.
	 * @param item The item to modify the data of.
	 * @param name The new display name of the item.
	 * @param lore The new lore of the item. If this parameter is null, the lore will be set to an empty, unmodifiable list. Otherwise, it will be converted to an {@link ArrayList} and then set as the item lore.
	 * @return The modified item.
	 */
	public static ItemStack setItemNameAndLore(ItemStack item, String name,
			String[] lore) {
		Validate.notNull(item, "The item is null.");
		Validate.notEmpty(name, "The name is null.");
		// Lore array may be null, we just assume the user wants no lore
		// Validate.notNull(lore, "The lore array is null.");
		Validate.noNullElements(lore, "The lore array contains null elements.");
		
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(lore == null ? emptyList : Arrays.asList(lore));
		item.setItemMeta(im);
		return item;
	}
	
	private Utilities(){
		//No instance should be created
	}
	
}
