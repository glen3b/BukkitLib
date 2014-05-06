package me.pagekite.glen3b.library.bungeecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import me.pagekite.glen3b.library.bukkit.Constants;
import me.pagekite.glen3b.library.bukkit.datastore.Message;
import me.pagekite.glen3b.library.bukkit.teleport.QueuedTeleport;
import me.pagekite.glen3b.library.bukkit.teleport.ServerTeleportationManager;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * The default implementation of cross server teleportation on BungeeCord networks.
 * @author Glen Husman
 * @see ServerTeleportationManager
 * @see ServerTransportManager
 */
public final class DefaultServerTeleportationManager implements
		ServerTeleportationManager {

	final class ScheduledDecrementRunner implements Runnable, Listener, QueuedTeleport<String> {
		
		private int _remDelay;
		private UUID _playerId;
		private BukkitTask _ownTask;
		private String _target;
		private boolean _isValid = true;
		
		private List<Runnable> _onTP = new ArrayList<Runnable>();

		private List<Runnable> _onTPCancel = new ArrayList<Runnable>();
		
		private ScheduledDecrementRunner(final Player player, final int initialDelay, final String target){
			_remDelay = initialDelay;
			_playerId = player.getUniqueId();
			_ownTask = Bukkit.getServer().getScheduler().runTaskTimer(_instance, this, Constants.TICKS_PER_SECOND, Constants.TICKS_PER_SECOND);
			Bukkit.getServer().getPluginManager().registerEvents(this, _instance);
			_target = target;

			player.sendMessage(Message.get("teleportBegin").replace("%time%", Integer.toString(initialDelay)).replace("%units%", initialDelay == 1 ? "second" : "seconds"));
		}
		
		@Override
		public void cancel() {
			if(!isCancelled()){
				cleanup(false);
			}
		}
		public void cleanup(boolean notifyPlayer){
			_isValid = false;
			_ownTask.cancel();
			
			if(notifyPlayer && Bukkit.getPlayer(_playerId) != null){
				Bukkit.getPlayer(_playerId).sendMessage(Message.get("teleportCancelled"));
				for(Runnable r : _onTPCancel){
					r.run();
				}
			}
			
			HandlerList.unregisterAll(this);
			_onTP.clear();
			_onTPCancel.clear();
		}
		
		@Override
		public String getDestination() {
			return _target;
		}
		
		@Override
		public Player getEntity() {
			if(isCancelled()){
				throw new IllegalStateException("This method cannot be called on a cancelled queued teleport.");
			}
			
			return Bukkit.getPlayer(_playerId);
		}

		@Override
		public int getRemainingDelay() {
			return _remDelay < 0 ? 0 : _remDelay;
		}

		@Override
		public boolean isCancelled() {
			return !_isValid;
		}

		@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
		public void onPlayerDamage(EntityDamageEvent event){
			if(!_isValid){
				return;
			}
			
			if(event.getEntity() instanceof Player && event.getEntity().getUniqueId().equals(_playerId)){
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
			
			if(event.getPlayer().getUniqueId().equals(_playerId)){
				//Bad boy, our player moved
				cleanup(true);
			}
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

		@Override
		public void run() {
			_isValid = !isCancelled() && Bukkit.getPlayer(_playerId) != null;
			
			if(!_isValid){
				cleanup(false);
				return;
			}
			
			_remDelay--;
			
			Player affected = Bukkit.getPlayer(_playerId);
			
			if(_remDelay <= 0){
				//Teleport to server
				affected.sendMessage(Message.get("teleporting"));
				getServerTransporter().sendPlayer(affected, _target);
				
				for(Runnable r : _onTP){
					r.run();
				}
				
				cleanup(false);
			}else if(_instance.getConfig().getBoolean("showIncrementalMessages")){
				affected.sendMessage(Message.get("teleportProgress").replace("%time%", Integer.toString(_remDelay)).replace("%units%", _remDelay == 1 ? "second" : "seconds"));
			}
		}
	}
	
	private ServerTransportManager _transporter;
	
	private HashMap<String, ScheduledDecrementRunner> _teleportsQueued = new HashMap<String, ScheduledDecrementRunner>();
	
	private Plugin _instance;
	
	/**
	 * Creates a server transport manager. <br/><br/><b>This constructor should only be invoked by the GBukkitLib plugin instance.</b>
	 * @param instance The host plugin.
	 */
	public DefaultServerTeleportationManager(Plugin instance) {
		_instance = instance;
	}
	
	public ServerTransportManager getServerTransporter(){
		if(_transporter == null){
			_transporter = _instance.getServer().getServicesManager().getRegistration(ServerTransportManager.class).getProvider();
		}
		
		return _transporter;
	}

	@Override
	public QueuedTeleport<String> getTeleport(Player teleport) {
		Validate.notNull(teleport, "The player is null.");
		
		return _teleportsQueued.containsKey(teleport.getName().toLowerCase().trim()) ? _teleportsQueued.get(teleport.getName().toLowerCase().trim()) : null;
	}
	
	@Override
	public QueuedTeleport<String> teleportPlayer(Player player, String targetServer){
		return teleportPlayer(player, targetServer, _instance.getConfig().getInt("teleportDelay"));
	}
	
	@Override
	public QueuedTeleport<String> teleportPlayer(Player player, String targetServer, int teleportDelay){
		Validate.isTrue(teleportDelay >= 0, "Teleport delay must not be negative. Value: ", teleportDelay);
		Validate.notNull(player, "The player must not be null.");
		Validate.notEmpty(targetServer, "The target server must not be null.");
		
		//Cleanup existing teleport, if any, in the queue
		if(getTeleport(player) != null){
			getTeleport(player).cancel();
		}
		
		//Check for no teleport delay
		if(teleportDelay == 0 || player.hasPermission("gbukkitlib.tpdelay.bypass")){
			player.sendMessage(Message.get("teleporting"));
		  	getServerTransporter().sendPlayer(player, targetServer);
		  	return null;
		}
		
		//Queue new teleportation
		ScheduledDecrementRunner runner = new ScheduledDecrementRunner(player, teleportDelay, targetServer);
		_teleportsQueued.put(player.getName().toLowerCase().trim(), runner);
		
		return runner;
	}

}
