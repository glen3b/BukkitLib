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

import javax.annotation.Nullable;

import me.pagekite.glen3b.library.bukkit.protocol.ProtocolOperationResult;
import me.pagekite.glen3b.library.bukkit.teleport.QueuedTeleport;
import me.pagekite.glen3b.library.bukkit.teleport.TeleportationManager;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

/**
 * A static class housing common methods, constants, and utility functions.
 * 
 * @author Glen Husman
 */
public final class Utilities {

	/**
	 * Internally used class. Registered internally to allow for certain utility methods to function.
	 * @author Glen Husman
	 */
	static final class EventRegistrar implements Listener{
		// TODO Use it!
	}

	private static ProtocolUtilities _protocolLib;
	
	/**
	 * Initializes the utilities class with event registrations and such. Internal method, not meant to be called by user code.
	 * @param hostPlugin The GBukkitLib plugin instance.
	 * @param protocolLib ProtocolLib utility wrapper, or {@code null} if it does not exist.
	 */
	static void initialize(GBukkitLibraryPlugin hostPlugin, @Nullable ProtocolUtilities protocolLib){
		if(protocolLib != null){
			protocolLib.init(hostPlugin);
		}
		
		_protocolLib = protocolLib;
	}
	
	/**
	 * Assure that a value is between two other values.
	 * @param value The value to clamp.
	 * @param minimum The minimum return value.
	 * @param maximum The maximum return value.
	 * @return {@code value} if it is between {@code minimum} and {@code maximum}, {@code minimum} if {@code value < minimum}, or {@code maximum} of {@code value > maximum}.
	 */
	public static float clamp(float value, float minimum, float maximum) {
		return Math.max(Math.min(value, maximum), minimum);
	}
	
	/**
	 * Assure that a value is between two other values.
	 * @param value The value to clamp.
	 * @param minimum The minimum return value.
	 * @param maximum The maximum return value.
	 * @return {@code value} if it is between {@code minimum} and {@code maximum}, {@code minimum} if {@code value < minimum}, or {@code maximum} of {@code value > maximum}.
	 */
	public static double clamp(double value, double minimum, double maximum) {
		return Math.max(Math.min(value, maximum), minimum);
	}
	
	/**
	 * Assure that a value is between two other values.
	 * @param value The value to clamp.
	 * @param minimum The minimum return value.
	 * @param maximum The maximum return value.
	 * @return {@code value} if it is between {@code minimum} and {@code maximum}, {@code minimum} if {@code value < minimum}, or {@code maximum} of {@code value > maximum}.
	 */
	public static long clamp(long value, long minimum, long maximum) {
		return Math.max(Math.min(value, maximum), minimum);
	}
	
	/**
	 * Assure that a value is between two other values.
	 * @param value The value to clamp.
	 * @param minimum The minimum return value.
	 * @param maximum The maximum return value.
	 * @return {@code value} if it is between {@code minimum} and {@code maximum}, {@code minimum} if {@code value < minimum}, or {@code maximum} of {@code value > maximum}.
	 */
	public static int clamp(int value, int minimum, int maximum) {
		return Math.max(Math.min(value, maximum), minimum);
	}


	/**
	 * @deprecated Use {@link Constants#TICKS_PER_SECOND}
	 */
	@Deprecated
	public static final long TICKS_PER_SECOND = Constants.TICKS_PER_SECOND;

	/**
	 * @deprecated Use {@link Constants#TICKS_PER_MINUTE}
	 */
	@Deprecated
	public static final long TICKS_PER_MINUTE = Constants.TICKS_PER_MINUTE;

	/**
	 * Utility methods involving entities.
	 * @author Glen Husman
	 */
	public static final class Entities {
		private Entities(){
			//No instance should be created
		}

		/**
		 * Gets the entity with the specified UUID of the specified type. This method will search the specified world and will return the first entity with the specified UUID.
		 * @param <T> The type of the entity.
		 * @param clazz The type of the entity.
		 * @param id The persistent universally unique identifier of the entity.
		 * @param world The world to search.
		 * @return The first known entity with the specified identifier, or {@code null} if not found.
		 */
		@SuppressWarnings("unchecked")
		public static <T extends Entity> T getEntity(UUID id, World world, Class<T> clazz){
			Validate.notNull(id, "The ID cannot be null.");
			Validate.notNull(world, "The world cannot be null.");
			Validate.notNull(clazz, "The entity class must not be null.");

			if(Player.class.isAssignableFrom(clazz)){
				for(Player e : world.getPlayers()){
					if(e != null && e.isValid() && e.getUniqueId().equals(id)){
						return (T)e;
					}
				}
			}else{
				for(T e : world.getEntitiesByClass(clazz)){
					if(e != null && e.isValid() && e.getUniqueId().equals(id)){
						return e;
					}
				}
			}

			return null;
		}

		/**
		 * Gets the entity with the specified UUID of the specified type. This method will search all worlds registered with the Bukkit API, and will return the first entity with the specified UUID.
		 * @param <T> The type of the entity.
		 * @param id The persistent universally unique identifier of the entity.
		 * @return The first known entity with the specified identifier, or {@code null} if not found.
		 */
		public static <T extends Entity> T getEntity(UUID id, Class<T> clazz){
			for(World world : Bukkit.getServer().getWorlds()){
				T entity = getEntity(id, world, clazz);
				if(entity != null){
					return entity;
				}
			}

			return null;
		}

		/**
		 * Gets the entity with the specified UUID. This method will search the specified world and will return the first entity with the specified UUID.
		 * @param id The persistent universally unique identifier of the entity.
		 * @param world The world to search.
		 * @return The first known entity with the specified identifier, or {@code null} if not found.
		 */
		public static Entity getEntity(UUID id, World world){
			Validate.notNull(id, "The ID cannot be null.");
			Validate.notNull(world, "The world cannot be null.");

			for(Entity e : world.getEntities()){
				if(e != null && e.isValid() && e.getUniqueId().equals(id)){
					return e;
				}
			}

			return null;
		}

		/**
		 * Gets the entity with the specified UUID. This method will search all worlds registered with the Bukkit API, and will return the first entity with the specified UUID.
		 * @param id The persistent universally unique identifier of the entity.
		 * @return The first known entity with the specified identifier, or {@code null} if not found.
		 */
		public static Entity getEntity(UUID id){
			for(World world : Bukkit.getServer().getWorlds()){
				Entity entity = getEntity(id, world);
				if(entity != null){
					return entity;
				}
			}

			return null;
		}
	}

	/**
	 * Utility methods involving players.
	 * @author Glen Husman
	 */
	public static final class Players {
		private Players(){
			//No instance should be created
		}

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
		 * Gets a list of the names of the specified players. Keep in mind that names are no longer safe as persistent cross-session unique identifiers.
		 * @param players The {@code Player}s for which to retrieve the names.
		 * @return A mutable list of all of the usernames of the {@code Player} objects passed in.
		 * @see Player#getName()
		 */
		public static List<String> getPlayerNames(Player... players){
			Validate.noNullElements(players, "There must not be null players!");
			
			ArrayList<String> playerNames = new ArrayList<String>();

			for(Player player : players){
				playerNames.add(player.getName());
			}

			return playerNames;
		}

		/**
		 * Gets a list of the names of all currently online players. Keep in mind that names are no longer safe as persistent cross-session unique identifiers.
		 * @return A mutable list of all of the usernames of all of the players currently online on the {@code Bukkit} server.
		 * @see Server#getOnlinePlayers()
		 * @see Player#getName()
		 */
		public static List<String> getOnlinePlayerNames(){
			return getPlayerNames(Bukkit.getOnlinePlayers());
		}
	}

	/**
	 * Utility methods involving execution scheduling.
	 * @author Glen Husman
	 */
	public static final class Scheduler {
		private Scheduler(){
			//No instance should be created
		}

		/**
		 * Schedules a task to execute every second, starting one second after the call to this method.
		 * @param host The plugin under which to schedule this task. This parameter may not be {@code null}.
		 * @param task The task to execute on the main server thread after one server tick. It must not be {@code null}.
		 * @param Whether to run this task asynchronously. If this is true, the task will be executed on a separate thread from the main server thread. Asynchronous tasks should <b>never</b> access any Bukkit API other than the scheduler, which can be used to schedule a synchronous task. Synchronous tasks block the main server thread, but have the liberty of full Bukkit API access.
		 * @return The scheduled task as returned by the bukkit scheduler.
		 * @see org.bukkit.scheduler.BukkitScheduler#runTaskTimer(Plugin plugin, Runnable task, long delay)
		 * @see org.bukkit.scheduler.BukkitScheduler#runTaskTimerAsynchronously(Plugin, Runnable, long)
		 */
		public static BukkitTask scheduleOneSecondTimer(Plugin host, Runnable task, boolean async){
			Validate.notNull(task, "The task must not be null.");
			Validate.isTrue(host != null && host.isEnabled(), "The host must be a non-null, enabled plugin.");

			return async ? Bukkit.getScheduler().runTaskTimer(host, task, Constants.TICKS_PER_SECOND, Constants.TICKS_PER_SECOND) : Bukkit.getScheduler().runTaskTimerAsynchronously(host, task, Constants.TICKS_PER_SECOND, Constants.TICKS_PER_SECOND);
		}

		/**
		 * Schedules a task to execute after one tick.
		 * @param host The plugin under which to schedule this task. This parameter may not be {@code null}.
		 * @param task The task to execute after one server tick. It must not be {@code null}.
		 * @param Whether to run this task asynchronously. If this is true, the task will be executed on a separate thread from the main server thread. Asynchronous tasks should <b>never</b> access any Bukkit API other than the scheduler, which can be used to schedule a synchronous task. Synchronous tasks block the main server thread, but have the liberty of full Bukkit API access.
		 * @return The scheduled task as returned by the bukkit scheduler.
		 * @see org.bukkit.scheduler.BukkitScheduler#runTaskLater(Plugin plugin, Runnable task, long delay)
		 * @see org.bukkit.scheduler.BukkitScheduler#runTaskLaterAsynchronously(Plugin, Runnable, long)
		 */
		public static BukkitTask scheduleTickTask(Plugin host, Runnable task, boolean async){
			Validate.notNull(task, "The task must not be null.");
			Validate.isTrue(host != null && host.isEnabled(), "The host must be a non-null, enabled plugin.");

			return async ? Bukkit.getScheduler().runTaskLater(host, task, 1L) : Bukkit.getScheduler().runTaskLaterAsynchronously(host, task, 1L);
		}

		/**
		 * Schedules a task to execute on the main server thread after one tick.
		 * @param host The plugin under which to schedule this task. This parameter may not be {@code null}.
		 * @param task The task to execute on the main server thread after one server tick. It must not be {@code null}.
		 * @return The ID of the scheduled task.
		 * @see Utilities#scheduleTickTask(Plugin, Runnable, boolean)
		 */
		public static int scheduleTickTask(Plugin host, Runnable task){
			return scheduleTickTask(host, task, false).getTaskId();
		}

		/**
		 * Run the specified tasks after the completion of the specified teleport. This method is intended to wrap calls to {@link TeleportationManager} methods which may return a {@code null} {@link QueuedTeleport}. If the method returns {@code null} and that value is passed into this method, the tasks will run instantly after the teleport, as was intended, without an additional {@code null} check in client code.
		 * @param <T> The type of the destination of the teleport.
		 * @param teleport The teleport to scedule tasks for. If this is {@code null} or cancelled, the tasks will be run instantly.
		 * @param tasks The tasks to run.
		 * @return Whether the tasks were queued. The return value will be {@code false} if they ran instantly during the execution of this method and {@code true} if they were queued for execution and consequently have not yet run.
		 * @see TeleportationManager#teleportPlayer(Player player, Location targetLoc)
		 */
		public static <T> boolean runAfterTeleport(@Nullable QueuedTeleport<T> teleport, Runnable... tasks){
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

	}

	/**
	 * @deprecated Use {@link Scheduler#runAfterTeleport(QueuedTeleport, Runnable...)}
	 */
	@Deprecated
	public static <T> boolean runAfterTeleport(QueuedTeleport<T> teleport, Runnable... tasks){
		return Scheduler.runAfterTeleport(teleport, tasks);
	}

	/**
	 * Utility methods involving arguments, argument parsing, and value parsing.
	 * @author Glen Husman
	 */
	public static final class Arguments{

		private Arguments(){
			//No instance should be created
		}

		/**
		 * Attempt to parse {@code str} as a double=precision real number, returning a default value if it is not possible.
		 * @param str The string to attempt to parse.
		 * @param defaultVal The value to return if {@code str} cannot be parsed.
		 * @return {@code str} as a {@code double} if it is a valid, parseable floating point number; {@code defaultVal} otherwise.
		 */
		public static double parseDouble(String str, double defaultVal){
			if(str == null || str.trim().isEmpty()){
				return defaultVal;
			}

			try{
				return Double.parseDouble(str);
			}catch(NumberFormatException thr){
				return defaultVal;
			}
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
			}catch(NumberFormatException thr){
				return defaultVal;
			}
		}
	}

	/**
	 * @deprecated Use {@link Arguments#parseInt(String, int)}
	 */
	@Deprecated
	public static int parseInt(String str, int defaultVal){
		return Arguments.parseInt(str, defaultVal);
	}

	/**
	 * Schedules a task to execute after one tick.
	 * @param host The plugin under which to schedule this task. If this parameter is {@code null}, the GBukkitLib plugin instance as retrieved by the {@code PluginManager} will be used for scheduling. <b>Using this method with a {@code null} plugin argument is deprecated, and this functionality will be removed in a future release.</b>
	 * @param task The task to execute on the main server thread after one server tick. It must not be {@code null}.
	 * @param Whether to run this task asynchronously. If this is true, the task will be executed on a separate thread from the main server thread. Asynchronous tasks should <b>never</b> access any Bukkit API other than the scheduler, which can be used to schedule a synchronous task. Synchronous tasks block the main server thread, but have the liberty of full Bukkit API access.
	 * @return The scheduled task as returned by the bukkit scheduler.
	 * @see org.bukkit.scheduler.BukkitScheduler#runTaskLater(Plugin plugin, Runnable task, long delay)
	 * @see org.bukkit.scheduler.BukkitScheduler#runTaskLaterAsynchronously(Plugin, Runnable, long)
	 * @deprecated Use {@link Scheduler#scheduleTickTask(Plugin, Runnable, boolean)}. The new method disallows {@code null} plugin arguments.
	 */
	@Deprecated
	public static BukkitTask scheduleTickTask(@Nullable Plugin host, Runnable task, boolean async){
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
	 * @deprecated Use {@link Scheduler#scheduleTickTask(Plugin, Runnable)}. The new method disallows {@code null} plugin arguments.
	 */
	@Deprecated
	public static int scheduleTickTask(@Nullable Plugin host, Runnable task){
		return scheduleTickTask(host, task, false).getTaskId();
	}

	/**
	 * Utility methods involving items.
	 * @author Glen Husman
	 */
	public static final class Items{
		private Items(){}

		/**
		 * Utility methods involving potions and potion effects.
		 * @author Glen Husman
		 */
		public static final class Potions{
			private Potions(){}

			private static final List<PotionEffectType> _negativeEffects = Arrays.asList(
					PotionEffectType.BLINDNESS,
					PotionEffectType.CONFUSION,
					PotionEffectType.HARM,
					PotionEffectType.HUNGER,
					PotionEffectType.POISON,
					PotionEffectType.SLOW,
					PotionEffectType.SLOW_DIGGING,
					PotionEffectType.WEAKNESS,
					PotionEffectType.WITHER
					);
			
			/**
			 * Determines if a potion effect is a negative effect.
			 * @param effect The effect to check.
			 * @return Whether the effect is a negative or "bad" effect.
			 */
			public static boolean isNegative(PotionEffectType effect){
				Validate.notNull(effect, "The potion effect is null.");

				return _negativeEffects.contains(effect);
			}
		}

		/**
		 * <p>
		 * Removes glow from an {@link ItemStack}. This is accomplished by sending packets to the client
		 * that contain an enchantments NBT list that is empty. A custom NBT tag is used to accomplish this.
		 * </p>
		 * <p>
		 * For this operation to succeed, ProtocolLib is required on the server.
		 * A lack of this plugin will be indicated with a return value of
		 * {@link ProtocolOperationResult#PROCOTOLLIB_NOT_AVAILABLE}.
		 * </p>
		 * <p>
		 * If this operation succeeds and no unexpected errors occur, the return value will be {@link ProtocolOperationResult#SUCCESS_QUEUED}. The reason for this is that this method merely removes an NBT tag. Protocol operations will display the item as not glowing <b>when the appropriate packets are sent</b>. Therefore, the rendering of the (lack of the) glow (if it was previously present) is not instant, and will occur in the future, hence the indication of queued behavior.
		 * </p>
		 * @param stack The {@link ItemStack} to render using normal vanilla enchantment rendering mechanics.
		 * @return A non-null indicator of the success of this operation.
		 */
		public static ProtocolOperationResult removeItemGlow(ItemStack stack){
			Validate.notNull(stack, "The item to modify must not be null.");
			
			if(_protocolLib == null){
				return ProtocolOperationResult.PROCOTOLLIB_NOT_AVAILABLE;
			}
			
			return _protocolLib.setGlowing(stack, false);
			
		}
		
		/**
		 * <p>
		 * Adds glow to an {@link ItemStack}. This is accomplished by sending packets to the client
		 * that contain an enchantments NBT list that is empty. A custom NBT tag is used to accomplish this.
		 * </p>
		 * <p>
		 * For this operation to succeed, ProtocolLib is required on the server.
		 * A lack of this plugin will be indicated with a return value of
		 * {@link ProtocolOperationResult#PROCOTOLLIB_NOT_AVAILABLE}.
		 * </p>
		 * <p>
		 * If this operation succeeds and no unexpected errors occur, the return value will be {@link ProtocolOperationResult#SUCCESS_QUEUED}. The reason for this is that this method merely sets an NBT tag. Protocol operations will display the item as glowing <b>when the appropriate packets are sent</b>. Therefore, the rendering of the glow is not instant, and will occur in the future, hence the indication of queued behavior.
		 * </p>
		 * @param stack The {@link ItemStack} to render as having enchantments without actually having any.
		 * @return A non-null indicator of the success of this operation.
		 */
		public static ProtocolOperationResult addItemGlow(ItemStack stack){
			Validate.notNull(stack, "The item to modify must not be null.");
			
			if(_protocolLib == null){
				return ProtocolOperationResult.PROCOTOLLIB_NOT_AVAILABLE;
			}
			
			return _protocolLib.setGlowing(stack, true);
			
		}
		
		/**
		 * Adds or removes the appropriate properties to make the specified {@code ItemStack} "glow" without having enchantments.
		 * @param stack The {@link ItemStack} to render as having enchantments without actually having any.
		 * @param isGlowing Whether to make the {@code ItemStack} artificially glow.
		 * @return A non-null indicator of the success of this operation.
		 * @see Items#addItemGlow(ItemStack)
		 * @see Items#removeItemGlow(ItemStack)
		 */
		public static ProtocolOperationResult setItemGlowing(ItemStack stack, boolean isGlowing){
			if(isGlowing){
				return addItemGlow(stack);
			}else{
				return removeItemGlow(stack);
			}
		}
		
		/**
		 * Sets the display name of the specified item. <b>It also removes any existing lore.</b>
		 * The item that is passed will be unmodified after the operation.
		 * @param item The item to modify the data of.
		 * @param name The new display name of the item.
		 * @return The modified item.
		 */
		public static ItemStack setItemName(ItemStack item, String name) {
			return setItemNameAndLore(item, name, null);
		}

		/**
		 * Enchants the specified item, ignoring restrictions on level.
		 * The item that is passed will be unmodified after the operation.
		 * @param item The item to modify the data of.
		 * @param enchant The enchantment to apply.
		 * @param level The level at which to apply the enchant.
		 * @return The modified item.
		 */
		public static ItemStack enchant(ItemStack item, Enchantment enchant, int level){
			Validate.notNull(item, "The item is null.");
			Validate.notNull(enchant, "The color is null.");
			Validate.isTrue(level >= 0, "The enchantment level is invalid.");

			ItemMeta im = item.getItemMeta();
			im.addEnchant(enchant, level, true);
			ItemStack nItem = item.clone();
			nItem.setItemMeta(im);
			return nItem;
		}

		/**
		 * Sets the color of leather armor. The item that is passed will be unmodified after the operation.
		 * @param item The item to modify the data of. If it does not have the appropriate metadata for the operation, this will throw an exception.
		 * @param color The new color of the item.
		 * @return The modified item.
		 */
		public static ItemStack setArmorColor(ItemStack item, Color color) {
			Validate.notNull(item, "The item is null.");
			Validate.notNull(color, "The color is null.");
			Validate.isTrue(item.getType() == Material.LEATHER_HELMET || item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.LEATHER_BOOTS, "The item does not have leather armor meta information.");

			LeatherArmorMeta im = (LeatherArmorMeta) (item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(Material.LEATHER_CHESTPLATE));
			im.setColor(color);
			ItemStack nItem = item.clone();
			nItem.setItemMeta(im);
			return nItem;
		}

		/**
		 * Sets the display name and lore of the specified item. The item that is passed will be unmodified after the operation.
		 * @param item The item to modify the data of.
		 * @param name The new display name of the item.
		 * @param lore The new lore of the item. If this parameter is {@code null}, the lore will be set to an empty, unmodifiable list. Otherwise, it will be converted to an {@link ArrayList} and then set as the item lore.
		 * @return The modified item.
		 */
		public static ItemStack setItemNameAndLore(ItemStack item, String name,
				@Nullable String[] lore) {
			Validate.notNull(item, "The item is null.");
			Validate.notEmpty(name, "The name is null.");
			// Lore array may be null, we just assume the user wants no lore
			// Validate.notNull(lore, "The lore array is null.");
			if(lore != null){
				Validate.noNullElements(lore, "The lore array contains null elements.");
			}

			ItemMeta im = item.getItemMeta();
			im.setDisplayName(name);
			im.setLore(lore == null ? emptyList : Arrays.asList(lore));
			ItemStack nItem = item.clone();
			nItem.setItemMeta(im);
			return nItem;
		}
	}

	/**
	 * @deprecated Use {@link Items#setItemName(ItemStack, String)}, it has a less misleading name.
	 */
	@Deprecated
	public static ItemStack setItemName(ItemStack item, String name) {
		return Items.setItemName(item, name);
	}

	/**
	 * Sets the display name of the specified item. It also removes any lore.
	 * @param item The item to modify the data of.
	 * @param name The new display name of the item.
	 * @return The modified item.
	 * @deprecated Use {@link Items#setItemName(ItemStack, String)}, it has a less misleading name.
	 */
	@Deprecated
	public static ItemStack setItemNameAndLore(ItemStack item, String name) {
		return setItemNameAndLore(item, name, new String[0]);
	}

	// TODO: Is this safe? Is there a better way? It's immutable, is that bad?
	private static List<String> emptyList = Collections.emptyList();

	/**
	 * @deprecated Use {@link Items#setItemNameAndLore(ItemStack, String, String[])}.
	 */
	@Deprecated
	public static ItemStack setItemNameAndLore(ItemStack item, String name,
			String[] lore) {
		return Items.setItemNameAndLore(item, name, lore);
	}

	private Utilities(){
		//No instance should be created
	}

}
