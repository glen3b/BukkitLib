package me.pagekite.glen3b.library.bungeecord;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * This allows the sender to easily transport players between various servers on a BungeeCord network.
 * @author Glen Husman
 */
public final class ServerTransportManager {

	/**
	 * Internal constructor.
	 * @param plugin The GBukkitLib plugin instance.
	 */
	public ServerTransportManager(Plugin plugin){
		_plugin = plugin;

		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
	}
	
	private Plugin _plugin;
	
	/**
	 * Send the specified player to the specified other server.
	 * @param player The player to send to another server.
	 * @param serverName The name of the target server on the BungeeCord network.
	 */
	public void sendPlayer(Player player, String serverName){
		if(player == null)
			throw new IllegalArgumentException("Player must not be null.");
		
		if(serverName == null)
			throw new IllegalArgumentException("Server name must not be null.");
		
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(serverName);
		player.sendPluginMessage(_plugin, "BungeeCord", out.toByteArray());
	}
	
}
