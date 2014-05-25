package me.pagekite.glen3b.library.bungeecord;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import me.pagekite.glen3b.library.bukkit.Constants;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * This allows the sender to easily transport players between various servers on a BungeeCord network.
 * @author Glen Husman
 */
public final class ServerTransportManager implements PluginMessageListener {

	private Plugin _plugin;

	private Map<String, List<ResultReceived<String, String[]>>> _playerListReceivers = Collections.synchronizedMap(new HashMap<String, List<ResultReceived<String, String[]>>>());

	/**
	 * Internal constructor. <b>Should not be called except by the GBukkitLib plugin instance.</b> This type is registered as a service.
	 * @param plugin The GBukkitLib plugin instance.
	 */
	public ServerTransportManager(Plugin plugin){
		_plugin = plugin;

		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
		plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
	}

	/**
	 * Send the specified player to the specified other server.
	 * @param player The player to send to another server.
	 * @param serverName The name of the target server on the BungeeCord network.
	 */
	public void sendPlayer(Player player, String serverName){
		Validate.notNull(player, "Player must not be null.");
		Validate.notEmpty(serverName, "The server name must not be empty.");

		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(serverName);
		player.sendPluginMessage(_plugin, "BungeeCord", out.toByteArray());
	}

	/**
	 * Gets the array of players currently online on a server.
	 * @param serverName The name of the server.
	 * @param resultHandler The function to invoke upon receiving the result. The source parameter is the server name, the result is the player list.
	 * @see ResultReceived
	 */
	public void getPlayers(String serverName, ResultReceived<String, String[]> resultHandler){
		Validate.notNull(resultHandler, "The result handler must not be null.");
		Validate.notEmpty(serverName, "The server name must not be empty.");

		if(!_playerListReceivers.containsKey(serverName.toLowerCase().trim())){
			_playerListReceivers.put(serverName.toLowerCase().trim(), new ArrayList<ResultReceived<String, String[]>>(1));
		}

		_playerListReceivers.get(serverName.toLowerCase().trim()).add(resultHandler);

		final ByteArrayDataOutput out = ByteStreams.newDataOutput();

		out.writeUTF("PlayerList");
		out.writeUTF(serverName);

		schedulePlayerTask(new SendPluginMessageToPlayer(out));
	}

	private final class SendPluginMessageToPlayer implements PlayerTaskHandler{

		private ByteArrayDataOutput _out;
		
		public SendPluginMessageToPlayer(ByteArrayDataOutput out){
			_out = out;
		}
		
		@Override
		public void runTask(Player player) {
			player.sendPluginMessage(_plugin, "BungeeCord", _out.toByteArray());
		}
		
	}
	
	/**
	 * Interface allowing a task to be run upon a player signing on.
	 * @author Glen Husman
	 *
	 */
	public static interface PlayerTaskHandler{

		/**
		 * Run the task.
		 * @param player The player to use.
		 */
		public void runTask(Player player);
	}

	/**
	 * Used for sending messages to Bungee via random players.
	 */
	private static final Random _randomProvider = new Random();

	/**
	 * Schedule a player task if necessary, or run it immediately if possible.
	 * @param task The task to run.
	 * @return {@code true} if the task ran immediately, {@code false} otherwise.
	 */
	private boolean schedulePlayerTask(PlayerTaskHandler task){
		Validate.notNull(task);
		
		int onlineCount = Bukkit.getServer().getOnlinePlayers().length;

		if(onlineCount >= 1){
			// We have a player
			task.runTask(Bukkit.getServer().getOnlinePlayers()[_randomProvider.nextInt(onlineCount)]);
			return true;
		}else{
			// Wait until sign-on
			new PlayerSignonWaiter(task).runTaskTimer(_plugin, Constants.TICKS_PER_SECOND, Constants.TICKS_PER_SECOND / 2);
			return false;
		}
	}
	
	/**
	 * Used to wait for a player to sign on before sending data. Intended use: Wait for player signon to send/receive BungeeCord task/message.
	 * @author Glen Husman
	 */
	private static final class PlayerSignonWaiter extends BukkitRunnable{

		private PlayerTaskHandler _task;

		public PlayerSignonWaiter(PlayerTaskHandler task){
			_task = task;
		}

		@Override
		public void run() {
			int onlineCount = Bukkit.getServer().getOnlinePlayers().length;

			if(onlineCount >= 1){
				_task.runTask(Bukkit.getServer().getOnlinePlayers()[_randomProvider.nextInt(onlineCount)]);
				cancel();
			}
		}

	}


	// JavaDoc should be copied from interface
	@Override
	public void onPluginMessageReceived(String channel, Player receiver, byte[] message) {
		if (!channel.equals("BungeeCord")) {
			return;
		}

		DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

		try {
			String subchannel = in.readUTF();
			if (subchannel.equals("PlayerList")) {
				synchronized(_playerListReceivers){
					String serverName = in.readUTF(); 
					List<ResultReceived<String, String[]>> handlers = _playerListReceivers.get(serverName.toLowerCase().trim());
					String[] playerList = in.readUTF().split(", ");
					Iterator<ResultReceived<String, String[]>> handlerIterator = handlers.iterator();
					while(handlerIterator.hasNext()){
						ResultReceived<String, String[]> handler = handlerIterator.next();
						handler.onReceive(serverName, playerList);
						handlerIterator.remove();
					}
				}
			}
		} catch (IOException e) {
			// There was an issue in creating the subchannel string
			// TODO: Handle it better
			Bukkit.getLogger().log(Level.SEVERE, "Error reading subchannel response information from BungeeCord.", e);
		}
	}

}
