package me.pagekite.glen3b.gbukkitlib;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

public final class GBukkitLibraryPlugin extends JavaPlugin {

	@Override
	public void onDisable(){
		this.getServer().getServicesManager().unregisterAll(this);
	}
	
	@Override
	public void onEnable(){
		this.getServer().getServicesManager().register(TeleportationManager.class, new GBukkitTPManager(), this, ServicePriority.Normal);
	}
	
	private final class GBukkitTPManager implements TeleportationManager {
		private GBukkitTPManager(){
			
		}
		
		private final class ScheduledDecrementRunner implements Runnable, Listener, QueuedTeleport{
			
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

				player.sendMessage(String.format("%sTeleporting in %d second%s, do not move.", ChatColor.YELLOW.toString(), initialDelay, initialDelay == 1 ? "" : "s"));
			}

			public void cleanup(boolean notifyPlayer){
				_ownTask.cancel();
				_isValid = false;
				
				if(notifyPlayer && Bukkit.getPlayer(_playerName) != null){
					Bukkit.getPlayer(_playerName).sendMessage(ChatColor.DARK_RED + "Pending teleportation cancelled.");
				}
				
				HandlerList.unregisterAll(this);
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
			
			public boolean isValid(){
				return _isValid;
			}
			
			@Override
			public void run() {
				_isValid = _isValid && Bukkit.getPlayer(_playerName) != null;
				
				if(!_isValid){
					cleanup(false);
					return;
				}
				
				_remDelay--;
				
				Player affected = Bukkit.getPlayer(_playerName);
				
				if(_remDelay <= 0){
					//Teleport to location
					affected.teleport(_target);
					affected.sendMessage(ChatColor.GOLD + "Teleporting...");
					cleanup(false);
				}else{
					affected.sendMessage(String.format("%sTeleporting in %d second%s.", ChatColor.DARK_PURPLE.toString(), _remDelay, _remDelay == 1 ? "" : "s"));
				}
			}

			@Override
			public void cancel() {
				if(isValid()){
					cleanup(false);
				}
			}

			@Override
			public boolean isCancelled() {
				return !isValid();
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
		}
		
		private HashMap<String, ScheduledDecrementRunner> _teleportsQueued = new HashMap<String, ScheduledDecrementRunner>();
		
		public void teleportPlayer(Player player, Location targetLoc, int teleportDelay){
			if(teleportDelay < 0){
				throw new IllegalArgumentException("Teleport delay must not be negative.");
			}
			
			if(player == null){
				throw new IllegalArgumentException("The player cannot be null.");
			}
			
			if(targetLoc == null){
				throw new IllegalArgumentException("The target location cannot be null.");
			}
			
			//Cleanup existing teleport, if any, in the queue
			if(getTeleport(player) != null){
				getTeleport(player).cancel();
			}
			
			//Check for no teleport delay
			if(teleportDelay == 0 || player.hasPermission("gbukkitlib.tpdelay.bypass")){
				//TODO: Send message to player indicating teleportation
			  	player.teleport(targetLoc);
			  	return;
			}
			
			//Queue new teleportation
			_teleportsQueued.put(player.getName().toLowerCase().trim(), new ScheduledDecrementRunner(player, teleportDelay, targetLoc));
		}
		
		public void teleportPlayer(Player player, Location targetLoc){
			teleportPlayer(player, targetLoc, /* TODO: Get delay from configuration */ 3);
		}

		@Override
		public QueuedTeleport getTeleport(Player teleport) {
			if(teleport == null){
				throw new IllegalArgumentException("The player cannot be null.");
			}
			
			return _teleportsQueued.containsKey(teleport.getName().toLowerCase().trim()) ? _teleportsQueued.get(teleport.getName().toLowerCase().trim()) : null;
		}
	}
	
}
