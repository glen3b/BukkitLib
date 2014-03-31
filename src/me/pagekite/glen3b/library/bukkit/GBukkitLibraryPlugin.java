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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import me.pagekite.glen3b.library.bukkit.datastore.AutoSaverScheduler;
import me.pagekite.glen3b.library.bukkit.datastore.Message;
import me.pagekite.glen3b.library.bukkit.datastore.MessageProvider;
import me.pagekite.glen3b.library.bukkit.datastore.SerializableLocation;
import me.pagekite.glen3b.library.bukkit.teleport.QueuedTeleport;
import me.pagekite.glen3b.library.bukkit.teleport.TeleportationManager;
import me.pagekite.glen3b.library.bungeecord.ServerTransportManager;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * The plugin class for GBukkitLibrary. Contains many implementation classes of registered services.
 * @author Glen Husman
 */
public final class GBukkitLibraryPlugin extends JavaPlugin {

	private final class DefaultMessageProvider implements MessageProvider{

		@Override
		public Set<String> getProvidedMessages() {
			return getConfig().getConfigurationSection("messages").getKeys(false);
		}

		@Override
		public Message getMessage(String messageId) {
			if(getConfig().getConfigurationSection("messages").getString(messageId) == null){
				return null;
			}
			
			return new Message(messageId, getConfig().getConfigurationSection("messages").getString(messageId));
		}

		@Override
		public void setMessage(String key, String value)
				throws IllegalStateException {
			throw new IllegalStateException("This is a read-only message provider.");
		}

		@Override
		public void saveMessages() {
			throw new IllegalStateException("This is a read-only message provider.");
		}

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public void setMessage(Message value) throws IllegalStateException {
			Validate.notNull(value, "The value cannot be null.");
			
			setMessage(value.getKey(), value.getUnformattedValue());
		}
		
	}
	
	@Override
	public void onDisable(){
		this.getServer().getServicesManager().unregisterAll(this);
	}
	
	@Override
	public void onEnable(){
		this.getServer().getServicesManager().register(AutoSaverScheduler.class, new AutoSaverScheduler(this), this, ServicePriority.Normal);
		this.getServer().getServicesManager().register(TeleportationManager.class, new GBukkitTPManager(), this, ServicePriority.Normal);
		//XXX: Should the configuration-provided server-owner registered messages be at the highest priority, or the lowest priority?
		this.getServer().getServicesManager().register(MessageProvider.class, new DefaultMessageProvider(), this, ServicePriority.Highest);
		this.getServer().getServicesManager().register(ServerTransportManager.class, new ServerTransportManager(this), this, ServicePriority.High);
		ConfigurationSerialization.registerClass(SerializableLocation.class);
		saveDefaultConfig();
	}
	
	/**
	 * Default implementation class for teleportation management.
	 * @author Glen Husman
	 */
	public final class GBukkitTPManager implements TeleportationManager {
		private GBukkitTPManager(){
			
		}
		
		final class ScheduledDecrementRunner implements Runnable, Listener, QueuedTeleport{
			
			private int _remDelay;
			private String _playerName;
			private BukkitTask _ownTask;
			private Location _target;
			private boolean _isValid = true;
			
			private ScheduledDecrementRunner(final Player player, final int initialDelay, final Location target){
				_remDelay = initialDelay;
				_playerName = player.getName().toLowerCase().trim();
				_ownTask = Bukkit.getServer().getScheduler().runTaskTimer(GBukkitLibraryPlugin.this, this, 20L, 20L);
				Bukkit.getServer().getPluginManager().registerEvents(this, GBukkitLibraryPlugin.this);
				_target = target;

				player.sendMessage(Message.get("teleportBegin").replace("%time%", Integer.toString(initialDelay)).replace("%units%", initialDelay == 1 ? "second" : "seconds"));
			}

			public void cleanup(boolean notifyPlayer){
				_ownTask.cancel();
				_isValid = false;
				
				if(notifyPlayer && Bukkit.getPlayer(_playerName) != null){
					Bukkit.getPlayer(_playerName).sendMessage(Message.get("teleportCancelled"));
					for(Runnable r : _onTPCancel){
						r.run();
					}
				}
				
				HandlerList.unregisterAll(this);
				_onTP.clear();
				_onTPCancel.clear();
			}
			
			@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
			public void onPlayerDamage(EntityDamageEvent event){
				if(!_isValid){
					return;
				}
				
				if(event.getEntity() instanceof Player && ((Player)event.getEntity()).getName().trim().equalsIgnoreCase(_playerName)){
					cleanup(true);
				}
			}
			
			private List<Runnable> _onTP = new ArrayList<Runnable>();
			private List<Runnable> _onTPCancel = new ArrayList<Runnable>();
			
			@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
			public void onPlayerMove(PlayerMoveEvent event){
				if(!_isValid){
					return;
				}
				
				if(event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY() == event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ()){
					//The player did not actually move across a block, ignore it
					return;
				}
				
				if(event.getPlayer().getName().trim().equalsIgnoreCase(_playerName)){
					//Bad boy, our player moved
					cleanup(true);
				}
			}
			
			@Override
			public void run() {
				_isValid = !isCancelled() && Bukkit.getPlayer(_playerName) != null;
				
				if(!_isValid){
					cleanup(false);
					return;
				}
				
				_remDelay--;
				
				Player affected = Bukkit.getPlayer(_playerName);
				
				if(_remDelay <= 0){
					//Teleport to location
					affected.sendMessage(Message.get("teleporting"));
					affected.teleport(_target);
					
					for(Runnable r : _onTP){
						r.run();
					}
					
					cleanup(false);
				}else if(getConfig().getBoolean("showIncrementalMessages")){
					affected.sendMessage(Message.get("teleportProgress").replace("%time%", Integer.toString(_remDelay)).replace("%units%", _remDelay == 1 ? "second" : "seconds"));
				}
			}

			@Override
			public void cancel() {
				if(!isCancelled()){
					cleanup(false);
				}
			}

			@Override
			public boolean isCancelled() {
				return !_isValid;
			}

			@Override
			public Location getTo() {
				return _target;
			}

			@Override
			public int getRemainingDelay() {
				return _remDelay < 0 ? 0 : _remDelay;
			}

			@Override
			public Player getEntity() {
				if(isCancelled()){
					throw new IllegalStateException("This method cannot be called on a cancelled queued teleport.");
				}
				
				return Bukkit.getPlayer(_playerName);
			}

			@Override
			public void registerOnTeleport(Runnable delegate) {
				Validate.notNull(delegate, "The method to call must not be null.");
				
				_onTP.add(delegate);
			}

			@Override
			public void registerOnTeleportCancel(Runnable delegate) {
				Validate.notNull(delegate, "The method to call must not be null.");
				
				_onTPCancel.add(delegate);
			}
		}
		
		private HashMap<String, ScheduledDecrementRunner> _teleportsQueued = new HashMap<String, ScheduledDecrementRunner>();
		
		public QueuedTeleport teleportPlayer(Player player, Location targetLoc, int teleportDelay){
			Validate.isTrue(teleportDelay >= 0, "Teleport delay must not be negative. Value: ", teleportDelay);
			Validate.notNull(player, "The player must not be null.");
			Validate.notNull(targetLoc, "The target location must not be null.");
			
			//Cleanup existing teleport, if any, in the queue
			if(getTeleport(player) != null){
				getTeleport(player).cancel();
			}
			
			//Check for no teleport delay
			if(teleportDelay == 0 || player.hasPermission("gbukkitlib.tpdelay.bypass")){
				player.sendMessage(Message.get("teleporting"));
			  	player.teleport(targetLoc);
			  	return null;
			}
			
			//Queue new teleportation
			ScheduledDecrementRunner runner = new ScheduledDecrementRunner(player, teleportDelay, targetLoc);
			_teleportsQueued.put(player.getName().toLowerCase().trim(), runner);
			
			return runner;
		}
		
		public QueuedTeleport teleportPlayer(Player player, Location targetLoc){
			return teleportPlayer(player, targetLoc, getConfig().getInt("teleportDelay"));
		}

		@Override
		public QueuedTeleport getTeleport(Player teleport) {
			Validate.notNull(teleport, "The player is null.");
			
			return _teleportsQueued.containsKey(teleport.getName().toLowerCase().trim()) ? _teleportsQueued.get(teleport.getName().toLowerCase().trim()) : null;
		}
	}
	
}
