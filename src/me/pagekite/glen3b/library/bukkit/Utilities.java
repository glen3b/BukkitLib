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
import java.util.List;

import me.pagekite.glen3b.library.bukkit.teleport.QueuedTeleport;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

/**
 * A static class housing common methods and constants.
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
	 * Gets a list of the names of all online players.
	 * @return A list of all of the <b>usernames</b> of all of the players currently online on the {@code Bukkit} server.
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
	 * Schedules a task to execute on the main server thread after one tick.
	 * @param host The plugin under which to schedule this task. If this parameter is {@code null}, the GBukkitLib plugin instance as retrieved by the {@code PluginManager} will be used for scheduling. Using this method with a {@code null} plugin argument is deprecated.
	 * @param task The task to execute on the main server thread after one server tick. It must not be {@code null}.
	 * @return The ID of the scheduled task.
	 * @see org.bukkit.scheduler.BukkitScheduler#scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay)
	 */
	public static int schedule(Plugin host, Runnable task){
		Validate.notNull(task, "The task must not be null.");
		
		return Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(host == null ? Bukkit.getServer().getPluginManager().getPlugin("GBukkitLib") : host, task, 1L);
	}
	
	/**
	 * Sets the display name of the specified item. It also removes any lore.
	 * The item that is passed in should not be assumed to be unmodified after the operation.
	 * @param item The item to modify the data of.
	 * @param name The new display name of the item.
	 * @return The modified item.
	 */
	public static ItemStack setItemNameAndLore(ItemStack item, String name) {
		return setItemNameAndLore(item, name, new String[0]);
	}
	
	/**
	 * Sets the display name and lore of the specified item.
	 * The item that is passed in should not be assumed to be unmodified after the operation.
	 * @param item The item to modify the data of.
	 * @param name The new display name of the item.
	 * @param lore The new lore of the item.
	 * @return The modified item.
	 */
	public static ItemStack setItemNameAndLore(ItemStack item, String name,
			String[] lore) {
		Validate.notNull(item, "The item is null.");
		Validate.notEmpty(name, "The name is null.");
		Validate.notNull(lore, "The lore array is null.");
		Validate.noNullElements(lore, "The lore array contains null elements.");
		
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(lore == null ? new ArrayList<String>() : Arrays.asList(lore));
		item.setItemMeta(im);
		return item;
	}
	
	private Utilities(){
		//No instance should be created
	}
	
}
