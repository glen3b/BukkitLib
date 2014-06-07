package me.pagekite.glen3b.library.bukkit.protocol;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.bigteddy98.packetapi.Packet;
import me.bigteddy98.packetapi.PacketWrapper;
import me.bigteddy98.packetapi.api.PacketHandler;
import me.bigteddy98.packetapi.api.PacketListener;
import me.bigteddy98.packetapi.api.PacketRecieveEvent;
import me.bigteddy98.packetapi.api.PacketType;
import me.pagekite.glen3b.library.ResultReceived;
import me.pagekite.glen3b.library.bukkit.menu.sign.AbstractSignGUIManager;
import me.pagekite.glen3b.library.bukkit.reflection.ReflectionUtilities;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/**
 * Implementation class, not for use by client plugins.
 * @author Glen Husman
 */
public final class PacketAPISignGUI extends AbstractSignGUIManager
implements PacketListener {

	protected Map<UUID, ResultReceived<Player, String[]>> listeners;
	protected Map<UUID, Vector> signLocations;
	private Plugin _plugin;
	protected Field _packetPlayInUpdateSign_linesField;
	
	public PacketAPISignGUI(Plugin plugin){
	        listeners = new ConcurrentHashMap<UUID, ResultReceived<Player, String[]>>();
	        signLocations = new ConcurrentHashMap<UUID, Vector>();
	        _plugin = plugin;
	}
	
	@PacketHandler(listenType = PacketType.PacketPlayInUpdateSign)
	public void onPacketReceive(final PacketRecieveEvent event){
		// This very well be an async call, I don't know because it isn't documented!
		// Let's assume it is, and be VEERY careful (by running it on the main thread)

		Bukkit.getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable(){

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				final Player player = Bukkit.getPlayer(event.getRecieverName()); // Ugly hack #1, but it's not the worst. This is the reason we're on the main thread.

				Vector v = signLocations.remove(player.getUniqueId());
				if (v == null) return;
				
				Integer x = 0, y = -1, z = 0;
				String[] lines = new String[4];
				
				try{
					// I try to avoid obfuscated names as much as possible, but I may not have much of a choice
					// 3 ints, 1 String[]: I can get the String[] safely, but the ints become ambiguous
					// A fallback exists here which is even worse: Relying on JRE-specific implementation details
					if(_packetPlayInUpdateSign_linesField == null){
						for(Field field : event.getPacket().getNMSPacket().getClass().getDeclaredFields() /* Get the private PacketPlayInUpdateSign fields */){
							if(field.getType() == String[].class){
								// Found it!
								field.setAccessible(true);
								_packetPlayInUpdateSign_linesField = field;
								break;
							}
						}
					}
					lines = (String[])_packetPlayInUpdateSign_linesField.get(event.getPacket().getNMSPacket());
					
					// Obfuscated names in a little-changing class are A LOT better than relying on JRE-specific implementation details that are not in the Java API spec
					x = (Integer)ReflectionUtilities.getValue(event.getPacket().getNMSPacket(), "a");
					y = (Integer)ReflectionUtilities.getValue(event.getPacket().getNMSPacket(), "b");
					z = (Integer)ReflectionUtilities.getValue(event.getPacket().getNMSPacket(), "c");
				}catch(Exception except){
					except.printStackTrace();
					// ASSUMES lines HAS BEEN ASSIGNED SAFELY
					// Time to use non-API-documented implementation details (sort of like using CB reflection hax, but for the JRE and 10x worse)
					Field[] declaredInstanceFields = event.getPacket().getNMSPacket().getClass().getDeclaredFields(); // Get the private fields, (WARN: Implementation-specific catch following) in declaration order (not in API, but in implementation)
					int fieldCt = 0; // X,Y,Z field assignment increments this (no assigned values = 0, X assigned = 1, X+Y assigned = 2, X+Y+Z assigned = 3
					
					try{
					for(int i = 0; i < declaredInstanceFields.length; i++){
						if(fieldCt > 2){
							break;
						}
						Field f = declaredInstanceFields[i];
						if(f.getType() != int.class){
							continue;
						}
						f.setAccessible(true);
						switch(fieldCt++){
						case 0:
							x = f.getInt(event.getPacket().getNMSPacket());
							break;
						case 1:
							y = f.getInt(event.getPacket().getNMSPacket());
							break;
						case 2:
							z = f.getInt(event.getPacket().getNMSPacket());
							break;
						}
					}
					}catch(Exception nexcept){
						if(nexcept.getCause() == null){
							try{
								nexcept.initCause(except);
							}catch(IllegalStateException bleh){
								// Ignore
							}
						}
						throw new RuntimeException("Error occurred during the obtaining of coordinate values from an incoming packet", nexcept);
					}
				}
				
				if (x != v.getBlockX()) return;
				if (y != v.getBlockY()) return;
				if (z != v.getBlockZ()) return;

				final ResultReceived<Player, String[]> response = listeners.remove(player.getUniqueId());
				if (response != null) {
					event.setCancelled(true);
					// No need to wrap in a sync scheduler call as we already wrapped the whole handler in that
					response.onReceive(player, lines);
				}
			}

		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void open(Player player, String[] paramdefaultText, ResultReceived<Player, String[]> response) {
		Validate.notNull(player, "The player must not be null.");
		Validate.isTrue(paramdefaultText == null || paramdefaultText.length <= 4, "No more than 4 default lines may be specified.");

		String[] defaultText = paramdefaultText == null ? null : new String[4];

		Location signLoc = new Location(player.getWorld(), player.getLocation().getX(), 0, player.getLocation().getZ());

		if (defaultText != null) {

			for(int i = 0; i < paramdefaultText.length; i++){
				defaultText[i] = paramdefaultText[i] == null ? "" : paramdefaultText[i];
			}

			for(int i = paramdefaultText.length; i < defaultText.length; i++){
				defaultText[i] = "";
			}


			player.sendBlockChange(signLoc, Material.SIGN_POST, (byte) 0);
			player.sendSignChange(signLoc, defaultText);
		}

		PacketWrapper signGUIOpen = Packet.PacketPlayOutOpenSignEditor(signLoc.getBlockX(), signLoc.getBlockY(), signLoc.getBlockZ());
		signGUIOpen.send(player);

		if (defaultText != null) {
			player.sendBlockChange(signLoc, Material.BEDROCK, (byte) 0);
		}

		signLocations.put(player.getUniqueId(), signLoc.toVector());
		listeners.put(player.getUniqueId(), response);    

	}

	@Override
	public void destroy() {
		// PacketAPI isn't a big fan of letting us clean up, I'll let it do it
	}

}
