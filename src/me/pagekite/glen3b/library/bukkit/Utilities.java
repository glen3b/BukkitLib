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

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A static class housing common methods and constants.
 * @author Glen Husman
 */
public final class Utilities {

	/**
	 * Determine if str is an integer.
	 * @param str The string to validate.
	 * @return If str is a valid, parseable integer.
	 */
	public static boolean isInt(String str){
		try{
			Integer.parseInt(str);
		}catch(Throwable thr){
			return false;
		}
    	return true;
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
	
	/**
	 * Gets a list of the names of all online players.
	 * @return A list of all of the minecraft usernames currently online on the {@code Bukkit} server.
	 * @see Bukkit#getServer()
	 * @see Server#getOnlinePlayers()
	 */
	public static List<String> getOnlinePlayerNames(){
		ArrayList<String> players = new ArrayList<String>();
		
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			players.add(player.getName());
		}
		
		return players;
	}
	
	/**
	 * The target number of server ticks per second.
	 */
	public static final long TICKS_PER_SECOND = 20L;
	
	/**
	 * The target number of server ticks per minute.
	 */
	public static final long TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;
	
	private Utilities(){
		//No instance should be created
	}
	
}
