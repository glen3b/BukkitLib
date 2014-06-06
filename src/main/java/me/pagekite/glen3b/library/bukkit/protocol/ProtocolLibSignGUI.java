package me.pagekite.glen3b.library.bukkit.protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.pagekite.glen3b.library.ResultReceived;
import me.pagekite.glen3b.library.bukkit.menu.sign.AbstractSignGUIManager;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

/**
 * The default implementation of the sign GUI manager. Not intended for direct use by client code.
 * @author <a href="http://forums.bukkit.org/members/nisovin.2980/">nisovian</a>
 */
public class ProtocolLibSignGUI extends AbstractSignGUIManager {


    protected ProtocolManager protocolManager;
    protected PacketAdapter packetListener;
    protected Map<UUID, ResultReceived<Player, String[]>> listeners;
    protected Map<UUID, Vector> signLocations;

    
    
    public ProtocolLibSignGUI(Plugin plugin) {
        protocolManager = ProtocolLibrary.getProtocolManager();        
        listeners = new ConcurrentHashMap<UUID, ResultReceived<Player, String[]>>();
        signLocations = new ConcurrentHashMap<UUID, Vector>();
        

        protocolManager.addPacketListener(
        packetListener =  new PacketAdapter(plugin, PacketType.Play.Client.UPDATE_SIGN) 
        {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                final Player player = event.getPlayer();
                
                Vector v = signLocations.remove(player.getUniqueId());
                if (v == null) return;        
                List<Integer> list = event.getPacket().getIntegers().getValues();
                if (list.get(0) != v.getBlockX()) return;
                if (list.get(1) != v.getBlockY()) return;
                if (list.get(2) != v.getBlockZ()) return;
                
                final String[] lines = event.getPacket().getStringArrays().getValues().get(0);
                final ResultReceived<Player, String[]> response = listeners.remove(event.getPlayer().getUniqueId());
                if (response != null) {
                    event.setCancelled(true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            response.onReceive(player, lines);
                        }
                    });
                }
            }
        });
    }
    
    @Override
    public void open(Player player, String[] paramdefaultText, ResultReceived<Player, String[]> response) {
    	Validate.notNull(player, "The player must not be null.");
    	Validate.isTrue(paramdefaultText == null || paramdefaultText.length <= 4, "No more than 4 default lines may be specified.");
        
        int x = 0, y = 0, z = 0;
        String[] defaultText = paramdefaultText == null ? null : new String[4];
        List<PacketContainer> packets = new ArrayList<PacketContainer>(defaultText == null ? 1 : 4);
        
        if (defaultText != null) {
        	
        	for(int i = 0; i < paramdefaultText.length; i++){
            	defaultText[i] = paramdefaultText[i] == null ? "" : paramdefaultText[i];
            }
        	
        	for(int i = paramdefaultText.length; i < defaultText.length; i++){
        		defaultText[i] = "";
        	}
        	
            x = player.getLocation().getBlockX();
            z = player.getLocation().getBlockZ();
            
            PacketContainer packet53 = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
            packet53.getIntegers().write(0, x).write(1, y).write(2, z);
            packet53.getBlocks().write(0, org.bukkit.Material.SIGN_POST);
            packets.add(packet53);
            
            PacketContainer packet130 = protocolManager.createPacket(PacketType.Play.Server.UPDATE_SIGN);
            packet130.getIntegers().write(0, x).write(1,y).write(2, z);
            packet130.getStringArrays().write(0, defaultText);
            packets.add(packet130);
        }
        
        PacketContainer packet133 = protocolManager.createPacket(PacketType.Play.Server.OPEN_SIGN_ENTITY);
        packet133.getIntegers().write(0, x).write(2, z);
        packets.add(packet133);
        
        if (defaultText != null) {
            PacketContainer packet53 = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
            packet53.getIntegers().write(0, x).write(1, 0).write(2, z);
            packet53.getBlocks().write(0, org.bukkit.Material.BEDROCK);
            packets.add(packet53);
        }
        
        try {
            for (PacketContainer packet : packets) {
                protocolManager.sendServerPacket(player, packet);
            }
            signLocations.put(player.getUniqueId(), new Vector(x, y, z));
            listeners.put(player.getUniqueId(), response);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("An error occurred while sending the sign edit packets.", e);
        }       
        
    }
    
    @Override
    public void destroy() {
        protocolManager.removePacketListener(packetListener);
        listeners.clear();
        signLocations.clear();
    }
	
}
