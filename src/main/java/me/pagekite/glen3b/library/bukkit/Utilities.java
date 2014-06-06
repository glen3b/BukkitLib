/*
   This file is part of GBukkitCore.

    GBukkitCore is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GBukkitCore is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with GBukkitCore.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.pagekite.glen3b.library.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.pagekite.glen3b.library.bukkit.command.CommandSenderType;
import me.pagekite.glen3b.library.bukkit.protocol.ProtocolOperationResult;
import me.pagekite.glen3b.library.bukkit.protocol.ProtocolOperationReturn;
import me.pagekite.glen3b.library.bukkit.protocol.ProtocolUtilities;
import me.pagekite.glen3b.library.bukkit.reflection.ReflectionUtilities;
import me.pagekite.glen3b.library.bukkit.teleport.QueuedTeleport;
import me.pagekite.glen3b.library.bukkit.teleport.TeleportationManager;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A static class housing common methods, constants, and utility functions.
 * 
 * @author Glen Husman
 */
public final class Utilities {

	// used for thread safety in initialize and cleanup
	private static Object initializationSynclock = new Object();

	/**
	 * Internal reference to ProtocolLib utils.
	 */
	private static ProtocolUtilities _protocolLib;

	/**
	 * <b>This method should not be called in standard programming. It is only visible for internal use.</b>
	 * @return The protocol operation utility instance, or {@code null} if it does not exist.
	 */
	public static ProtocolUtilities getProtocolUtilityInstance(){
		return _protocolLib;
	}

	private static UtilityEventListener _eventListener;

	/**
	 * <b>Internally used event listener class. Not for consumption by API users. This class does not have a stable API.</b>
	 */
	public static final class UtilityEventListener implements Listener{

		private UtilityEventListener(Plugin pl){
			_host = pl;
		}

		private Plugin _host;

		// Kick event determiners

		private Map<UUID, String> _kickedPlayers = Maps.newHashMap();

		public Map<UUID, String> getKickedPlayers(){
			return _kickedPlayers;
		}

		private final class KickRunner implements Runnable{
			private UUID _id;

			public KickRunner(Player pl){
				_id = pl.getUniqueId();
			}

			@Override
			public void run() {
				_kickedPlayers.remove(_id);
			}
		}

		@EventHandler(priority = EventPriority.MONITOR)
		public void onKick(final PlayerKickEvent event){
			_kickedPlayers.put(event.getPlayer().getUniqueId(), event.getReason() == null ? "" : event.getReason());
			Bukkit.getScheduler().runTask(_host, new KickRunner(event.getPlayer()));
		}

		// End kick event determiners


		// Wolf spawn assurance
		private Set<Location> _wolfSpawnLocs = Sets.newHashSet();

		public Set<Location> getWolfSpawnLocSet(){
			return _wolfSpawnLocs;
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onEntitySpawn(final CreatureSpawnEvent event){
			if(event.getEntityType() == EntityType.WOLF){
				// Avoid a double iteration (a "contains" and "remove" call)
				Iterator<Location> iter = getWolfSpawnLocSet().iterator();

				while(iter.hasNext()){
					if(iter.next().equals(event.getLocation())){
						event.setCancelled(false);
						iter.remove();
						return;
					}
				}
			}
		}

		//Killassists
		
		private Map<UUID, Deque<DamageData>> _mobsToDamageInformation = Maps.newHashMapWithExpectedSize(50);
		
		public Deque<DamageData> getDeque(UUID mob){
			Validate.notNull(mob);
			
			if(!_mobsToDamageInformation.containsKey(mob)){
				_mobsToDamageInformation.put(mob, new ArrayDeque<DamageData>(5));
			}
			
			return _mobsToDamageInformation.get(mob);
		}
		
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void onHealthRegain(final EntityRegainHealthEvent event){
			double regainAmountTicker = event.getAmount();
			Deque<DamageData> damagers = getDeque(event.getEntity().getUniqueId());
			while(regainAmountTicker > 0 && damagers.size() > 0){
				DamageData leastRecentDamage = damagers.getLast();
				if(leastRecentDamage.getDamageAmount() > regainAmountTicker){
					leastRecentDamage.setDamageAmount(leastRecentDamage.getDamageAmount() - regainAmountTicker);
					regainAmountTicker = 0;
				}else if(leastRecentDamage.getDamageAmount() == regainAmountTicker){
					damagers.removeLast();
					regainAmountTicker = 0;
				}else{
					// Damage amount of source is less than regain amount ticker
					regainAmountTicker -= damagers.removeLast().getDamageAmount();
				}
			}
			
		}

		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void onHealthLoss(final EntityDamageEvent event){
			Deque<DamageData> damagers = getDeque(event.getEntity().getUniqueId());
			DamageData info = new DamageData();
			info.setCause(event.getCause());
			info.setDamageAmount(event.getDamage());
			if(event instanceof EntityDamageByEntityEvent){
				info.setSource(((EntityDamageByEntityEvent)event).getDamager());
			}else if(event instanceof EntityDamageByBlockEvent){
				info.setSource(((EntityDamageByBlockEvent)event).getDamager());
			}
			damagers.addFirst(info);
		}
	}

	/**
	 * Initializes the utilities class with event registrations and such. Internal method, not meant to be called by user code.
	 * @param hostPlugin The GBukkitCore plugin instance.
	 */
	static void initialize(Plugin hostPlugin){
		synchronized(initializationSynclock){
			Preconditions.checkState(_protocolLib == null, "Utilities has not been cleaned up since last initialization! Call cleanup(Plugin) to clean up internal fields before you reinitialize it.");
			Preconditions.checkState(_eventListener == null, "Utilities has not been cleaned up since last initialization! Call cleanup(Plugin) to clean up internal fields before you reinitialize it.");

			RegisteredServiceProvider<ProtocolUtilities> pLib = Bukkit.getServicesManager().getRegistration(ProtocolUtilities.class);

			if(pLib != null && pLib.getProvider() != null){
				_protocolLib = pLib.getProvider();
				_protocolLib.init(hostPlugin);
			}else{
				_protocolLib = null;
			}

			_eventListener = new UtilityEventListener(hostPlugin);
			Bukkit.getPluginManager().registerEvents(_eventListener, hostPlugin);

			Utilities.Effects.resetCache();
		}
	}

	/**
	 * Clean up the mess.
	 * @param hostPlugin The plugin creating the mess.
	 */
	static void cleanup(GBukkitCorePlugin hostPlugin){
		synchronized(initializationSynclock){
			if(_protocolLib != null){
				_protocolLib.cleanup(hostPlugin);
			}

			_protocolLib = null;

			if(_eventListener != null){
				HandlerList.unregisterAll(_eventListener);
			}

			_eventListener = null;
		}
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
	 * Utility predicates that apply to Bukkit.
	 * @author Glen Husman
	 */
	public static final class Predicates{
		private Predicates(){
			// No instance
		}

		private static final class InstanceofCommandSenderTypePredicate implements Predicate<Object>{

			public InstanceofCommandSenderTypePredicate(CommandSenderType type){
				_type = type == null ? CommandSenderType.ALL : type;
			}

			private CommandSenderType _type;

			@Override
			public boolean apply(Object sender) {
				return sender instanceof CommandSender && _type.isInstance(sender.getClass());
			}

		}	

		private static final class HasPermissionPredicate implements Predicate<Permissible>{

			public HasPermissionPredicate(String permNode){
				_usePermInstance = false;
				_node = permNode;
			}

			boolean _usePermInstance;

			public HasPermissionPredicate(Permission node){
				_usePermInstance = true;
				_perm = node;
			}
			private Permission _perm;
			private String _node;

			@Override
			public boolean apply(Permissible sender) {
				return _usePermInstance ? _perm == null || sender.hasPermission(_perm) : _node == null || sender.hasPermission(_node);
			}

		}

		/**
		 * Returns a predicate that will return {@code true} if and only if the {@link CommandSender} in question is of the specified {@link CommandSenderType}. If {@code type} is null, {@link CommandSenderType#ALL ALL} is the assumed default value.
		 * @param type The type of the command sender.
		 * @return A new predicate instance that will return {@code true} when the conditions above are satisfied.
		 */
		public static Predicate<? super CommandSender> isOfSenderType(CommandSenderType type){
			return new InstanceofCommandSenderTypePredicate(type);
		}

		/**
		 * Gets a predicate that returns {@code true} if {@code node} is {@code null} <b>or</b> the {@link CommandSender} in question has the permission node {@code node}.
		 * @param node The permission node to check.
		 * @return A predicate that will return {@code true} when the conditions specified above are satisfied.
		 */
		public static Predicate<Permissible> hasPermission(String node){
			return new HasPermissionPredicate(node);
		}

		/**
		 * Gets a predicate that returns {@code true} if {@code node} is {@code null} <b>or</b> the {@link CommandSender} in question has the permission node {@code node}.
		 * @param node The permission node to check.
		 * @return A predicate that will return {@code true} when the conditions specified above are satisfied.
		 */
		public static Predicate<Permissible> hasPermission(Permission node){
			return new HasPermissionPredicate(node);
		}
	}

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

		/**
		 * Gets a set of entities within a certain range.
		 * <p>
		 * <i>Implementation Note:</i> This method uses {@link Location#distanceSquared(Location)} for distance comparison to reduce the computational expense of this method.
		 * This method will also only iterate through entities in the world specified by the center location.
		 * @param center The centerpoint of the circular range.
		 * @param range The radius of the circle representing the range. Negative values are treated as if they were positive.
		 * @param clazz The type of the entities being found. This may be {@code Entity.class} for all entities.
		 * @return A non-null list of all entities within the specified range from the specified location.
		 */
		public static <T extends Entity> List<T> getEntitiesInRange(Location center, double range, Class<T> clazz) {
			Validate.notNull(center, "The center location must be defined.");
			Validate.notNull(center.getWorld(), "The center location must be defined.");

			List<T> entities = new ArrayList<T>();

			if(range == 0){
				// No point in searching
				return entities;
			}

			double squared = range * range;
			for (T entity : center.getWorld().getEntitiesByClass(clazz)){
				if (entity.getLocation().distanceSquared(center) <= squared){
					entities.add(entity);
				}
			}
			return entities;
		}
		
		/**
		 * Gets the list of recent damagers of the entity with the specified ID, as tracked by the utility event listener.
		 * <p>
		 * The sum of the damage amounts of the values in the list should equal the current health of the entity.
		 * <p>
		 * The list of damage information values is copied, such that modifications to the returned list will not affect the internal data store.
		 * <p>
		 * The returned list is sorted such that the first element will represent the most recent damage source and the last element will represent the last damage source that has still played a part in entity damage.
		 * <p>
		 * Due to the returned list being cloned, this reference is not necessarily valid for multiple server ticks.
		 * @param entityId The UUID of the entity for which to retrieve damager data.
		 * @return A mutable list of damage information about the specified entity.
		 */
		public static List<DamageData> getDamagers(UUID entityId){
			Validate.notNull(entityId, "The entity for which information is being retrieved must not be null.");
			Deque<DamageData> info = _eventListener.getDeque(entityId);
			if(info.size() == 0){
				return Lists.newArrayListWithExpectedSize(0);
			}
			List<DamageData> returnedList = Lists.newArrayListWithExpectedSize(info.size());
			for(DamageData data : info){
				// The iterator, according to docs, iterates from head to tail
				// This is consistent with our docs, which puts the most recent damage cause first
				try {
					returnedList.add(data.clone());
				} catch (CloneNotSupportedException e) {
					throw new RuntimeException("Damager information for this entity could not be cloned from the internal data store.", e);
				}
			}
			return returnedList;
		}
		
		/**
		 * Gets the list of recent damagers of the specified entity, as tracked by the utility event listener.
		 * @param entity The entity for which to retrieve damager data.
		 * @return A list of damage information about the specified entity.
		 * @see #getDamagers(UUID)
		 */
		public static List<DamageData> getDamagers(Entity entity){
			Validate.notNull(entity, "The entity for which information is being retrieved must not be null.");
			return getDamagers(entity.getUniqueId());
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
		 * Determines if the quit event corresponds to a player being kicked.
		 * @param event The event representing the kicking of the player.
		 * @return The kick reason, or {@code null} if and only if the event does not correspond to a known, non-cancelled kick.
		 */
		public static String isKick(PlayerQuitEvent event){
			return _eventListener.getKickedPlayers().get(Preconditions.checkNotNull(event, "The specified event is null!").getPlayer().getUniqueId());
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
		 * @param async Whether to run this task asynchronously. If this is true, the task will be executed on a separate thread from the main server thread. Asynchronous tasks should <b>never</b> access any Bukkit API other than the scheduler, which can be used to schedule a synchronous task. Synchronous tasks block the main server thread, but have the liberty of full Bukkit API access.
		 * @return The scheduled task as returned by the bukkit scheduler.
		 * @see org.bukkit.scheduler.BukkitScheduler#runTaskTimer(Plugin, Runnable, long, long)
		 * @see org.bukkit.scheduler.BukkitScheduler#runTaskTimerAsynchronously(Plugin, Runnable, long, long)
		 */
		public static BukkitTask scheduleOneSecondTimer(Plugin host, Runnable task, boolean async){
			Validate.notNull(task, "The task must not be null.");
			Validate.isTrue(host != null && host.isEnabled(), "The host must be a non-null, enabled plugin.");

			return async ? Bukkit.getScheduler().runTaskTimer(host, task, Constants.TICKS_PER_SECOND, Constants.TICKS_PER_SECOND) : Bukkit.getScheduler().runTaskTimerAsynchronously(host, task, Constants.TICKS_PER_SECOND, Constants.TICKS_PER_SECOND);
		}

		/**
		 * Run the specified tasks after the completion of the specified teleport.
		 * <p>
		 * This method is intended to wrap calls to {@link TeleportationManager} methods which may return a {@code null} {@link QueuedTeleport}. If the method returns {@code null} and that value is passed into this method, the tasks will run instantly after the teleport, as was intended, without an additional {@code null} check in client code.
		 * @param <T> The type of the destination of the teleport.
		 * @param teleport The teleport to scedule tasks for. If this is {@code null} or cancelled, the tasks will be run instantly.
		 * @param tasks The tasks to run.
		 * @return Whether the tasks were queued. The return value will be {@code false} if they ran instantly during the execution of this method and {@code true} if they were queued for execution and consequently have not yet run.
		 * @see TeleportationManager#teleportPlayer(Player player, Location targetLoc)
		 * @see QueuedTeleport#registerOnTeleport(Runnable)
		 */
		public static <T> boolean runAfterTeleport(@Nullable QueuedTeleport<T> teleport, @Nonnull Runnable... tasks){
			Validate.notEmpty(tasks, "There must be tasks to execute.");

			if(teleport == null || teleport.isCancelled()){
				// Instant execution
				for(Runnable task : tasks){
					Validate.notNull(task, "There must not be any null tasks.");

					task.run();
				}

				return false;
			}else{
				// Queue execution
				for(Runnable task : tasks){
					Validate.notNull(task, "There must not be any null tasks.");

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
		 * Attempt to parse {@code str} as a double-precision real number, returning a default value if it is not possible.
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
	 * Methods to play effects within the Minecraft world.
	 */
	public static final class Effects {

		/**
		 * <h3>ParticleEffects Library v1.4</h3><br/>
		 *
		 * This library was created by DarkBladee12 based on content related to particles of microgeek (names and packet values). It allows you to display all known Minecraft particle effects on a CraftBukkit server.
		 *<p>
		 * You are welcome to use it, modify it and redistribute it under the following conditions:
		 * <ol>
		 * <li> Don't claim this class as your own
		 * <li> Don't remove this text
		 * </ol>
		 *
		 * (Would be nice if you provide credit to me)
		 * </p>
		 * 
		 * <p>
		 * Note that the enum values of this class do <em>not</em> include icon crack, block dust, or block crack particles, as they are treated specially.
		 *
		 * @author <a href="http://forums.bukkit.org/members/darkbladee12.90749545/">DarkBladee12</a>
		 * @author <a href="http://forums.bukkit.org/members/microgeek.90705652/">microgeek</a>
		 * @author Glen Husman (doucmentation fixes, overload improvements)
		 */
		public static enum Particle {
			/**
			 * Appears as a huge explosion.
			 * Displayed naturally by TNT and creepers.
			 */
			HUGE_EXPLOSION("hugeexplosion"),
			/**
			 * Appears as a smaller explosion than {@link #HUGE_EXPLOSION}.
			 * Displayed naturally by TNT and creepers.
			 */
			LARGE_EXPLODE("largeexplode"),
			/**
			 * Appears as little white sparkling stars.
			 * Displayed naturally by fireworks.
			 */
			FIREWORKS_SPARK("fireworksSpark"),
			/**
			 * Appears as bubbles.
			 * Displayed natrually in water.
			 */
			BUBBLE("bubble"),
			/**
			 * <i>Information about this particle is not known.</i>
			 */
			SUSPEND("suspend"),
			/**
			 * Appears as little gray dots.
			 * Displayed naturally in and near the void, as well as in water.
			 */
			DEPTH_SUSPEND("depthSuspend"),
			/**
			 * Appears as ittle gray dots.
			 * Displayed naturally as an ambient effect by Mycelium.
			 */
			TOWN_AURA("townaura"),
			/**
			 * Appears as light brown crosses.
			 * Displayed naturally by critical hits.
			 */
			CRIT("crit"),
			/**
			 * Appears as cyan stars
			 * Displayed naturally by hits with an enchanted weapon.
			 */
			MAGIC_CRIT("magicCrit"),
			/**
			 * Appears as little black/gray clouds.
			 * Displayed naturally by torches, primed TNT and end portals.
			 */
			SMOKE("smoke"),
			/**
			 * Appears as colored swirls.
			 * Displayed naturally by potion effects.
			 */
			MOB_SPELL("mobSpell"),
			/**
			 * Appears as transparent colored swirls.
			 * Displayed naturally by beacon-induced effects.
			 */
			MOB_SPELL_AMBIENT("mobSpellAmbient"),
			/**
			 * Appears as colored swirls.
			 * Displayed naturally by splash potions.
			 */
			SPELL("spell"),
			/**
			 * Appears as colored crosses.
			 * Displayed naturally by instant splash potions (ones which apply instantly, such as {@linkplain org.bukkit.potion.PotionEffectType#HARM harming} and {@linkplain org.bukkit.potion.PotionEffectType#HEAL healing}).
			 */
			INSTANT_SPELL("instantSpell"),
			/**
			 * Appears as colored crosses.
			 * Displayed naturally by witches.
			 */
			WITCH_MAGIC("witchMagic"),
			/**
			 * Appears as colored notes.
			 * Displayed naturally by note blocks (when they play a note).
			 */
			NOTE("note"),
			/**
			 * Appears as little purple clouds.
			 * Displayed naturally by nether portals, endermen, ender pearls, eyes of ender and ender chests.
			 */
			PORTAL("portal"),
			/**
			 * Appears as white letters.
			 * Displayed naturally by enchantment tables that are near bookshelves.
			 */
			ENCHANTMENT_TABLE("enchantmenttable"),
			/**
			 * Appears as white clouds.
			 * Displayed naturally along with explosions.
			 */
			EXPLODE("explode"),
			/**
			 * Appears as little flames.
			 * Displayed naturally by torches, furnaces, magma cubes and monster spawners.
			 */
			FLAME("flame"),
			/**
			 * Appears as little orange blobs.
			 * Displayed naturally by lava.
			 */
			LAVA("lava"),
			/**
			 * Appears as gray transparent squares.
			 */
			FOOTSTEP("footstep"),
			/**
			 * Appears as blue drops.
			 * Displayed naturally by water, rain and shaking wolves.
			 */
			SPLASH("splash"),
			/**
			 * Appears as blue droplets.
			 * Displayed naturally on water when fishing.
			 */
			WAKE("wake"),
			/**
			 * Appears as black and gray clouds.
			 * Displayed naturally by fire, minecarts with furnaces and blazes.
			 */
			LARGE_SMOKE("largesmoke"),
			/**
			 * Appears as large white clouds.
			 * Displayed naturally upon on death of a living entity.
			 */
			CLOUD("cloud"),
			/**
			 * Appears as little colored clouds.
			 * Displayed naturally by active redstone wires and redstone torches.
			 */
			RED_DUST("reddust"),
			/**
			 * Appears as little white fragments.
			 * Displayed naturally by cracking snowballs and eggs.
			 */
			SNOWBALL_POOF("snowballpoof"),
			/**
			 * Appears as blue drips.
			 * Displayed by blocks below a water source.
			 */
			DRIP_WATER("dripWater"),
			/**
			 * Appears as reddish orange drips.
			 * Displayed naturally by blocks below a lava source.
			 */
			DRIP_LAVA("dripLava"),
			/**
			 * Appears as white clouds.
			 */
			SNOW_SHOVEL("snowshovel"),
			/**
			 * Appears as little green fragments.
			 * Displayed naturally by slimes.
			 */
			SLIME("slime"),
			/**
			 * Appears as red hearts.
			 * Displayed natrually when breeding animals.
			 */
			HEART("heart"),
			/**
			 * Appears as dark gray cracked hearts.
			 * Displayed naturally when a player attacks a villager in a village.
			 */
			ANGRY_VILLAGER("angryVillager"),
			/**
			 * Appears as green stars.
			 * Displayed by bone meal and when trading with a villager.
			 */
			HAPPY_VILLAGER("happyVillager");

			private static final Map<String, Particle> NAME_MAP = new TreeMap<String, Particle>(String.CASE_INSENSITIVE_ORDER);
			private static final double MAX_RANGE = 32; // More than 16 to give some breathing room
			private static Constructor<?> packetPlayOutWorldParticles;
			private static Field playerConnection;
			private static Method sendPacket;
			private final String name;

			private static void loadReflectionObjects(){
				try {
					packetPlayOutWorldParticles = ReflectionUtilities.getConstructor(ReflectionUtilities.Minecraft.getType("PacketPlayOutWorldParticles"), String.class, float.class, float.class, float.class, float.class, float.class,
							float.class, float.class, int.class);
					playerConnection = ReflectionUtilities.getField(ReflectionUtilities.Minecraft.getType("EntityPlayer"), "playerConnection");
					sendPacket = playerConnection.getType().getDeclaredMethod("sendPacket", ReflectionUtilities.Minecraft.getType("Packet"));
					sendPacket.setAccessible(true);
				}catch (Exception e) {
					throw new IllegalStateException("Reflective object initialization failed.", e);
				}
			}

			private static void flushReflectionCache(){
				packetPlayOutWorldParticles = null;
				playerConnection = null;
				sendPacket = null;
			}

			private static boolean isReflectionInitialized(){
				return packetPlayOutWorldParticles != null && playerConnection != null && sendPacket != null;
			}

			static {
				for (Particle p : values()){
					NAME_MAP.put(p.name, p);
				}
				try{
					loadReflectionObjects();
				}catch(Throwable ex){
					Bukkit.getLogger().log(Level.WARNING, "Failed to load reflection required for particle effects.", ex);
				}
			}

			/**
			 * @param name Name of this particle effect
			 */
			private Particle(String name) {
				this.name = name;
			}

			/**
			 * @return The protocol name of this particle effect.
			 */
			public String getName() {
				return this.name;
			}

			/**
			 * Gets a particle effect by minecraft protocol name.
			 * The name parameter is case-insensitive.
			 * @param name The name of the particle effect.
			 * @return The particle effect if found, or {@code null} if it was not found.
			 * @exception IllegalArgumentException If {@code name} is {@code null}.
			 * @exception IllegalArgumentException If a particle effect with the specified name cannot be found.
			 */
			public static Particle fromName(String name) {
				Validate.notEmpty(name, "A particle effect name must be specified.");

				Particle returnVal = NAME_MAP.get(name.trim()); // We use a TreeMap with case insensitive comparison, so efficient lookups can be used while maintaining case safety

				if(returnVal == null){
					throw new IllegalArgumentException("A particle effect with the specified name could not be found.");
				}

				return returnVal;
			}

			/**
			 * Instantiates a new {@code PacketPlayOutWorldParticles} object using reflection.
			 *
			 * @param center Center location of the effect.
			 * @param offsetX Maximum distance particles can fly away from the center on the X-axis.
			 * @param offsetY Maximum distance particles can fly away from the center on the Y-axis.
			 * @param offsetZ Maximum distance particles can fly away from the center on the Z-axis.
			 * @param speed Display speed of the particles. A sort of data value.
			 * @param amount The number of particles to display.
			 * @return The packet object.
			 * @throws RuntimeException If an error occurs instantiating the packet.
			 */
			private static Object instantiatePacket(String name, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
				Validate.isTrue(amount >= 1, "At least one packet must be instantiated.");

				if(!isReflectionInitialized()){
					loadReflectionObjects();
				}

				try {
					return packetPlayOutWorldParticles.newInstance(name, (float) center.getX(), (float) center.getY(), (float) center.getZ(), offsetX, offsetY, offsetZ, speed, amount);
				} catch (Exception e) {
					throw new RuntimeException("Instantiation of a particle packet failed.", e);
				}
			}

			/**
			 * Instantiates a new {@code PacketPlayOutWorldParticles} object using reflection especially for the "iconcrack" effect
			 *
			 * @see #instantiatePacket
			 */
			private static Object instantiateIconCrackPacket(int id, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
				return instantiatePacket("iconcrack_" + id, center, offsetX, offsetY, offsetZ, speed, amount);
			}

			/**
			 * Instantiates a new {@code PacketPlayOutWorldParticles} object using reflection especially for the "blockcrack" effect
			 *
			 * @see #instantiatePacket
			 */
			private static Object instantiateBlockCrackPacket(int id, byte data, Location center, float offsetX, float offsetY, float offsetZ, int amount) {
				return instantiatePacket("blockcrack_" + id + "_" + data, center, offsetX, offsetY, offsetZ, 0, amount);
			}

			/**
			 * Instantiates a new {@code PacketPlayOutWorldParticles} object using reflection especially for the "blockdust" effect
			 *
			 * @see #instantiatePacket
			 */
			private static Object instantiateBlockDustPacket(int id, byte data, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
				return instantiatePacket("blockdust_" + id + "_" + data, center, offsetX, offsetY, offsetZ, speed, amount);
			}

			/**
			 * Using reflection, injects a packet into the players connection so that the client receives it.
			 *
			 * @param p Receiver of the packet. <i>Must</i> be an instance of {@code CraftPlayer}.
			 * @param packet Packet that is sent.
			 */
			private static void sendPacket(Player p, Object packet) {
				Validate.notNull(packet, "The packet must not be null.");

				if(!isReflectionInitialized()){
					loadReflectionObjects();
				}

				try {
					sendPacket.invoke(playerConnection.get(ReflectionUtilities.CraftBukkit.getNMSHandle(p)), packet);
				} catch (Exception e) {
					throw new RuntimeException("Failed to send a packet to the player '" + p.getName() + "' reflectively.", e);
				}
			}

			/**
			 * Sends a packet through reflection to a collection of players.
			 * @see #sendPacket(Player, Object)
			 */
			private static void sendPacket(Iterable<Player> players, Object packet) {
				for (Player p : players){
					if(p == null){
						throw new IllegalArgumentException("No null players may be specified as packet recipients.");
					}

					sendPacket(p, packet);
				}
			}

			/**
			 * Displays a particle effect which is only visible for players within a certain range of the centerpoint.
			 *
			 * @param center The center location of the particle effect.
			 * @param range The range which binds all players that will receive the packet.
			 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
			 * @param speed "Speed" of the particles, a data value of sorts.
			 * @param amount The number of particles to display.
			 */
			public void display(Location center, double range, Vector offset, float speed, int amount) {
				Validate.notNull(center, "The center location must not be null.");
				Validate.notNull(center.getWorld(), "The center location must not be null.");
				Validate.notNull(offset, "The offset values must not be null.");

				// Really no point to enforcing, just don't use huge values
				if (range > MAX_RANGE){
					// Enforced server-side to promote efficiency
					// throw new IllegalArgumentException("The range of particle recipients cannot exceed the maximum value of " + MAX_RANGE +", a limitation of the client.");
					Bukkit.getLogger().log(Level.WARNING, "A particle is being displayed with the range set to " + range + ", higher than the recommended maximum of " + MAX_RANGE + ", which will potentially result in inefficiency.");
				}

				sendPacket(Utilities.Entities.getEntitiesInRange(center, range, Player.class), instantiatePacket(name, center, (float)offset.getX(), (float)offset.getY(), (float)offset.getZ(), speed, amount));
			}

			/**
			 * Displays a particle effect which is visible for all players who can hypothetically see the particle if the appropriate packet is sent to their client.
			 * The range is very generous, however larger values can be specified in the linked overload.
			 *
			 * @param center The center location of the particle effect.
			 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
			 * @param speed "Speed" of the particles, a data value of sorts.
			 * @param amount The number of particles to display.
			 * @see #display(Location, double, Vector, float, int)
			 */
			public void display(Location center, Vector offset, float speed, int amount) {
				display(center, MAX_RANGE, offset, speed, amount);
			}

			/**
			 * Displays an item break (icon crack) particle effect which is only visible for players within a certain range of the centerpoint.
			 * @param center The center location of the particle effect.
			 * @param item The item type for which this effect applies.
			 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
			 * @param speed "Speed" of the particles, a data value of sorts.
			 * @param amount The number of particles to display.
			 * @param range The range which binds all players that will receive the packet.
			 */
			@SuppressWarnings("deprecation")
			public static void displayIconCrack(Location center, double range, Material item, Vector offset, float speed, int amount) {
				Validate.notNull(center, "The center location must not be null.");
				Validate.notNull(center.getWorld(), "The center location must not be null.");
				Validate.notNull(offset, "The offset values must not be null.");
				Validate.isTrue(item != null && !item.isBlock(), "The specified material is not a valid item.");

				// Really no point to enforcing, just don't use huge values
				if (range > MAX_RANGE){
					// Enforced server-side to promote efficiency
					// throw new IllegalArgumentException("The range of particle recipients cannot exceed the maximum value of " + MAX_RANGE +", a limitation of the client.");
					Bukkit.getLogger().log(Level.WARNING, "A particle is being displayed with the range set to " + range + ", higher than the recommended maximum of " + MAX_RANGE + ", which will potentially result in inefficiency.");
				}

				sendPacket(Utilities.Entities.getEntitiesInRange(center, range, Player.class), instantiateIconCrackPacket(item.getId(), center, (float)offset.getX(), (float)offset.getY(), (float)offset.getZ(), speed, amount));
			}

			/**
			 * Displays an item break (icon crack) particle effect which is visible for all players who can hypothetically see the particle if the appropriate packet is sent to their client.
			 * The range is very generous, however larger values can be specified in the linked overload.
			 * @param center The center location of the particle effect.
			 * @param item The item type for which this effect applies.
			 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
			 * @param speed "Speed" of the particles, a data value of sorts.
			 * @param amount The number of particles to display.
			 * @see #displayIconCrack(Location, double, Material, Vector, float, int)
			 */
			public static void displayIconCrack(Location center, Material item, Vector offset, float speed, int amount) {
				displayIconCrack(center, MAX_RANGE, item, offset, speed, amount);
			}

			/**
			 * Displays a block break (block crack) particle effect which is only visible for players within a certain range of the centerpoint.
			 * @param center The center location of the particle effect.
			 * @param data The material data (which includes type) of the represented block. This value may not be {@code null}.
			 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
			 * @param amount The number of particles to display.
			 * @param range The range which binds all players that will receive the packet.
			 */
			@SuppressWarnings("deprecation")
			public static void displayBlockCrack(Location center, double range, MaterialData data, Vector offset, int amount) {
				Validate.notNull(center, "The center location must not be null.");
				Validate.notNull(center.getWorld(), "The center location must not be null.");
				Validate.notNull(offset, "The offset values must not be null.");
				Validate.isTrue(data != null && data.getItemType().isBlock(), "The specified material is not a valid block.");

				// Really no point to enforcing, just don't use huge values
				if (range > MAX_RANGE){
					// Enforced server-side to promote efficiency
					// throw new IllegalArgumentException("The range of particle recipients cannot exceed the maximum value of " + MAX_RANGE +", a limitation of the client.");
					Bukkit.getLogger().log(Level.WARNING, "A particle is being displayed with the range set to " + range + ", higher than the recommended maximum of " + MAX_RANGE + ", which will potentially result in inefficiency.");
				}
				sendPacket(Utilities.Entities.getEntitiesInRange(center, range, Player.class), instantiateBlockCrackPacket(data.getItemTypeId(), data.getData(), center, (float)offset.getX(), (float)offset.getY(), (float)offset.getZ(), amount));
			}

			/**
			 * Displays a block break (block crack) particle effect which is visible for all players who can hypothetically see the particle if the appropriate packet is sent to their client.
			 * The range is very generous, however larger values can be specified in the linked overload.
			 * @param center The center location of the particle effect.
			 * @param data The material data (which includes type) of the represented block. This value may not be {@code null}.
			 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
			 * @param amount The number of particles to display.
			 * @see #displayBlockCrack(Location, double, MaterialData, Vector, int)
			 */
			public static void displayBlockCrack(Location center, MaterialData data, Vector offset, int amount) {
				displayBlockCrack(center, MAX_RANGE, data, offset, amount);
			}

			/**
			 * Displays a block dust particle effect which is only visible for players within a certain range of the centerpoint.
			 * @param center The center location of the particle effect.
			 * @param data The material data (which includes type) of the represented block. This value may not be {@code null}.
			 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
			 * @param speed "Speed" of the particles, a data value of sorts.
			 * @param amount The number of particles to display.
			 * @param range The range which binds all players that will receive the packet.
			 */
			@SuppressWarnings("deprecation")
			public static void displayBlockDust(Location center, double range, MaterialData data, Vector offset, float speed, int amount) {
				Validate.notNull(center, "The center location must not be null.");
				Validate.notNull(center.getWorld(), "The center location must not be null.");
				Validate.notNull(offset, "The offset values must not be null.");
				Validate.isTrue(data != null && data.getItemType().isBlock(), "The specified material is not a valid block.");

				// Really no point to enforcing, just don't use huge values
				if (range > MAX_RANGE){
					// Enforced server-side to promote efficiency
					// throw new IllegalArgumentException("The range of particle recipients cannot exceed the maximum value of " + MAX_RANGE +", a limitation of the client.");
					Bukkit.getLogger().log(Level.WARNING, "A particle is being displayed with the range set to " + range + ", higher than the recommended maximum of " + MAX_RANGE + ", which will potentially result in inefficiency.");
				}
				sendPacket(Utilities.Entities.getEntitiesInRange(center, range, Player.class), instantiateBlockDustPacket(data.getItemTypeId(), data.getData(), center, (float)offset.getX(), (float)offset.getY(), (float)offset.getZ(), speed, amount));
			}

			/**
			 * Displays a block dust particle effect which is visible for all players who can hypothetically see the particle if the appropriate packet is sent to their client.
			 * The range is very generous, however larger values can be specified in the linked overload.
			 * @param center The center location of the particle effect.
			 * @param data The material data (which includes type) of the represented block. This value may not be {@code null}.
			 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
			 * @param speed "Speed" of the particles, a data value of sorts.
			 * @param amount The number of particles to display.
			 * @see #displayBlockDust(Location, double, MaterialData, Vector, float, int)
			 */
			public static void displayBlockDust(Location center, MaterialData data, Vector offset, float speed, int amount) {
				displayBlockDust(center, MAX_RANGE, data, offset, speed, amount);
			}
		}

		private Effects(){}

		/**
		 * Reset internal cache.
		 */
		static void resetCache(){
			Particle.flushReflectionCache();
		}

		/**
		 * Play heart particles at the given location.
		 * <p>
		 * <i>Implementation note:</i> This method accomplishes the desired behavior by spawning a wolf, playing the {@linkplain EntityEffect#WOLF_HEARTS wolf heart} effect, and removing the wolf.
		 * Event handlers at the highest priority level are registered that will uncancel the spawn of this wolf.
		 * </p>
		 * @param location The location at which to play the heart effect.
		 */
		public static void playHeartEffect(@Nonnull Location location){
			Validate.notNull(location, "The location of the effect must not be null.");
			Validate.notNull(location.getWorld(), "The location must not have a null world.");

			_eventListener.getWolfSpawnLocSet().add(location);
			Wolf o = location.getWorld().spawn(location, Wolf.class);
			o.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) Constants.TICKS_PER_MINUTE, 0));
			o.playEffect(EntityEffect.WOLF_HEARTS);
			o.remove();
		}

		/**
		 * <p>
		 * This method provides a version independent way to instantly explode {@code FireworkEffect}s at a given location.
		 * It uses {@link org.bukkit.entity.Entity#setTicksLived(int) setTicksLived} to accomplish this.
		 * </p>
		 * @param location The location at which to display the firework effects.
		 * @param effects The firework effects to render.
		 */
		public static void playFirework(Location location, FireworkEffect... effects) {
			Validate.notNull(location, "The location of the effect must not be null.");
			Validate.notNull(location.getWorld(), "The location must not have a null world.");
			Validate.noNullElements(effects, "Null firework effects are not allowed.");

			// Bukkity load (CraftFirework)
			World world = location.getWorld();
			Firework fw = world.spawn(location, Firework.class);

			/*
			 * Now we mess with the metadata, allowing nice clean spawning of a pretty firework (look, pretty lights!)
			 */
			// metadata load
			FireworkMeta data = fw.getFireworkMeta();
			// clear existing
			data.clearEffects();
			// power of one
			data.setPower(1);
			// add the effects
			data.addEffects(effects);
			// set the meta
			fw.setFireworkMeta(data);

			// Set the "ticks flown" to a high value - game will remove everything after playing the effect
			fw.setTicksLived(123);
		}

	}

	/**
	 * Utility methods involving items.
	 * @author Glen Husman
	 */
	public static final class Items{
		private Items(){}

		/**
		 * Utility methods involving potions and potion effects.
		 * This class assumes that if an effect is not negative, it must be positive, and that all effects are either positive or negative.
		 * @author Glen Husman
		 */
		public static final class Potions{
			private Potions(){}

			private static final ImmutableSet<PotionEffectType> _negativeEffects;
			private static final ImmutableSet<PotionEffectType> _positiveEffects;

			static{
				// Build immutable sets
				_negativeEffects = ImmutableSet.of(
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

				ImmutableSet.Builder<PotionEffectType> positiveBuilder = ImmutableSet.builder();
				for(PotionEffectType type : PotionEffectType.values()){
					if(!_negativeEffects.contains(type)){
						positiveBuilder.add(type);
					}
				}
				_positiveEffects = positiveBuilder.build();
			}

			/**
			 * Determines if a potion effect is a positive effect.
			 * @param effect The effect to check.
			 * @return Whether the effect is a positive or "good" effect. If {@code effect} is {@code null}, the return value will be {@code false}.
			 */
			public static boolean isPositive(@Nullable PotionEffectType effect){
				if(effect == null){
					return false;
				}

				return _positiveEffects.contains(effect);
			}

			/**
			 * Gets all positive potion effects known to this plugin.
			 * @return An immutable set of all known positive potion effects.
			 */
			public static Set<PotionEffectType> getPositiveEffects(){
				return _positiveEffects;
			}

			/**
			 * Gets all negative potion effects known to this plugin.
			 * @return An immutable set of all known negative potion effects.
			 */
			public static Set<PotionEffectType> getNegativeEffects(){
				return _negativeEffects;
			}

			/**
			 * Determines if a potion effect is a negative effect.
			 * @param effect The effect to check.
			 * @return Whether the effect is a negative or "bad" effect. If {@code effect} is {@code null}, the return value will be {@code false}.
			 */
			public static boolean isNegative(@Nullable PotionEffectType effect){
				if(effect == null){
					return false;
				}

				return _negativeEffects.contains(effect);
			}
		}

		/**
		 * <p>
		 * Adds or removes glow to an {@link ItemStack}. This is accomplished by sending packets to the client
		 * that contain an enchantments NBT list that is empty. As with any packet or NMS modifying operation, glitches may occur.
		 * Found bugs should be filed on the GBukkitCore project.
		 * </p>
		 * <p>
		 * For this operation to succeed, a protocol library is required on the server.
		 * A lack of one will be indicated with a return value of
		 * {@link ProtocolOperationResult#LIBRARY_NOT_AVAILABLE}.
		 * However, the default reflective library is supported, therefore this method <i>should</i> always be capable of working.
		 * </p>
		 * <p>
		 * <b>Implementation Note - ProtocolLib:</b> A "magic enchant" stored on the server as a constant "flag" is used to accomplish the no-enchant glow effect.
		 * Packets which display enchantments are intercepted and rewritten before the client receives them.
		 * If this operation succeeds and no unexpected errors occur, the return value will contain {@link ProtocolOperationResult#SUCCESS_QUEUED}.
		 * The reason for this is that this method merely sets an enchantment which will be parsed by packet interceptors. Protocol operations will display the item as glowing <i>when the appropriate packets are sent</i>.
		 * Therefore, the rendering of the glow is not instant, and will occur in the future, hence the indication of queued behavior.
		 * </p>
		 * <p><b>Implementation Note - Raw Reflection:</b> The item enchantment tag is set directly, rendering it <i>unsafe for Bukkit API use</i> after a call to this method.
		 * This implementation will return a value containing {@link ProtocolOperationResult#SUCCESS} upon operation success because the NBT tags are set immediately.
		 * However, for it to be effective, the returned {@code ItemStack} encapsulated in the {@link ProtocolOperationReturn} <b>must</b> be used.</p>
		 * <p>
		 * If the {@code ItemStack} is not a {@code CraftItemStack}, the code is not guaranteed to function properly.
		 * <br/>
		 * This method will return a value containing {@link ProtocolOperationResult#FAILURE} if the {@code ItemStack} already has enchantments, as conflicts would be ultimately inevitable. In addition, if it has enchantments, it already glows.
		 * </p>
		 * @param stack The {@link ItemStack} to render as having no enchantments but having the effect.
		 * @param isGlowing Whether to make the {@code ItemStack} artificially glow.
		 * @return A non-null indicator of the success of this operation, as known by the server.
		 */
		public static ProtocolOperationReturn<ItemStack> setItemGlowing(ItemStack stack, boolean isGlowing){
			Validate.notNull(stack, "The item to modify must not be null.");

			if(_protocolLib == null){
				return new ProtocolOperationReturn<ItemStack>(ProtocolOperationResult.LIBRARY_NOT_AVAILABLE);
			}

			return _protocolLib.setGlowing(stack, isGlowing);
		}

		/* BEGIN COLORING REGION */

		private static boolean isColorable(ItemStack stack){
			return stack.getData() instanceof Colorable || stack.getType() == Material.STAINED_CLAY
					|| stack.getType() == Material.STAINED_GLASS || stack.getType() == Material.STAINED_GLASS_PANE || stack.getType() == Material.CARPET
					|| stack.getType() == Material.FIREWORK;
		}

		@SuppressWarnings("deprecation")
		private static void color(ItemStack stack, Color color){
			if(!isColorable(stack)){
				return;
			}

			if(stack.getType() != Material.FIREWORK){
				if(stack.getData() instanceof Colorable){
					MaterialData data = stack.getData();
					((Colorable)data).setColor(DyeColor.getByFireworkColor(color));
					stack.setData(data);
				}else{
					stack.setData(stack.getType().getNewData(DyeColor.getByFireworkColor(color).getWoolData())); // Needed until Bukkit adds proper support
				}
			}else{
				// Set firework color
				// This implementation overwrites other colors
				FireworkEffectMeta meta = ((FireworkEffectMeta)stack.getItemMeta());
				FireworkEffect eff = meta.getEffect();
				eff.getColors().clear();
				eff.getColors().add(color);
				meta.setEffect(eff);
				stack.setItemMeta(meta);
			}
		}

		@SuppressWarnings("deprecation")
		private static void color(ItemStack stack, DyeColor color){
			if(!isColorable(stack)){
				return;
			}

			if(stack.getType() != Material.FIREWORK){
				if(stack.getData() instanceof Colorable){
					MaterialData data = stack.getData();
					((Colorable)data).setColor(color);
					stack.setData(data);
				}else{
					stack.setData(stack.getType().getNewData(color.getWoolData())); // Needed until Bukkit adds proper support
				}
			}else{
				// Set firework color
				// This implementation overwrites other colors
				FireworkEffectMeta meta = ((FireworkEffectMeta)stack.getItemMeta());
				FireworkEffect eff = meta.getEffect();
				eff.getColors().clear();
				eff.getColors().add(color.getFireworkColor());
				meta.setEffect(eff);
				stack.setItemMeta(meta);
			}
		}

		/**
		 * Sets the color of the specified item.
		 * The item that is passed will be unmodified after the operation.
		 * @param item The item to modify the data of.
		 * @param color The new color of the item.
		 * @return The modified item.
		 */
		public static ItemStack setColor(ItemStack item, DyeColor color) {
			Validate.isTrue(item != null && isColorable(item), "The specified item is not colorable.");
			Validate.notNull(color, "The specified color is null.");

			ItemStack nItem = item.clone();
			color(nItem, color);
			return nItem;
		}

		/**
		 * Sets the color of the specified item.
		 * The item that is passed will be unmodified after the operation.
		 * @param item The item to modify the data of.
		 * @param color The new color of the item. This is assumed to be a color representing a firework's color (not a standard color).
		 * @return The modified item.
		 * @see DyeColor#getByFireworkColor(Color)
		 */
		public static ItemStack setColor(ItemStack item, Color color) {
			Validate.isTrue(item != null && isColorable(item), "The specified item is not colorable.");
			Validate.notNull(color, "The specified color is null.");

			ItemStack nItem = item.clone();
			color(nItem, color);
			return nItem;
		}

		/**
		 * Sets the color of the specified block.
		 * The block that is passed will be modified by reference after the operation.
		 * @param block The block to modify the data of.
		 * @param color The new color of the item.
		 */
		@SuppressWarnings("deprecation")
		public static void setColor(Block block, DyeColor color) {
			Validate.isTrue(block != null && ((block.getState() != null && block.getState().getData() instanceof Colorable)
					|| (block.getType() == Material.STAINED_CLAY
					|| block.getType() == Material.STAINED_GLASS || block.getType() == Material.STAINED_GLASS_PANE || block.getType() == Material.CARPET)), "The specified block is not colorable.");
			Validate.notNull(color, "The specified color is null.");

			if(block.getState().getData() instanceof Colorable){
				MaterialData data = block.getState().getData();
				((Colorable)data).setColor(color);
				block.getState().setData(data);
			}else{
				block.setData(color.getWoolData()); // Needed until Bukkit adds proper support
			}
		}

		/**
		 * Sets the color of the specified block.
		 * The block that is passed will be modified by reference after the operation.
		 * @param block The block to modify the data of.
		 * @param color The new color of the item. This is assumed to be a standard color (not a firework color).
		 * @see DyeColor#getByColor(Color)
		 */
		public static void setColor(Block block, Color color) {
			Validate.notNull(color, "The specified color is null.");

			setColor(block, DyeColor.getByColor(color));
		}

		/* END COLORING REGION */

		/**
		 * Sets the display name of the specified item. <b>It also removes any existing lore.</b>
		 * The item that is passed will be unmodified after the operation.
		 * @param item The item to modify the data of.
		 * @param name The new display name of the item.
		 * @return The modified item.
		 */
		public static ItemStack setItemName(ItemStack item, String name) {
			return setItemNameAndLore(item, name, (String[])null);
		}

		/**
		 * Enchants the specified item, ignoring restrictions on level.
		 * The item that is passed will be unmodified after the operation.
		 * @param item The item to modify the data of.
		 * @param enchant The enchantment to apply.
		 * @param level The one-based level at which to apply the enchant. A value of 1 indicates an enchantment level of 1 (exactly what is displayed to the user).
		 * @return The modified item.
		 */
		public static ItemStack enchant(ItemStack item, Enchantment enchant, int level){
			Validate.notNull(item, "The item is null.");
			Validate.notNull(enchant, "The enchantment is null.");
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
				@Nullable String... lore) {
			Validate.notNull(item, "The item is null.");
			Validate.notEmpty(name, "The name is null.");
			// Lore array may be null, we just assume the user wants no lore
			// Validate.notNull(lore, "The lore array is null.");
			if(lore != null){
				Validate.noNullElements(lore, "The lore array contains null elements.");
			}

			ItemMeta im = item.getItemMeta();
			im.setDisplayName(name);
			im.setLore(lore == null || lore.length == 0 ? Collections.<String>emptyList() : Arrays.asList(lore));
			ItemStack nItem = item.clone();
			nItem.setItemMeta(im);
			return nItem;
		}
	}

	private Utilities(){
		//No instance should be created
	}

}
