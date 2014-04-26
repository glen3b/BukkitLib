package me.pagekite.glen3b.library.bukkit;

import me.pagekite.glen3b.library.bukkit.protocol.ProtocolOperationResult;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

/**
 * Utilities involving modifications to packets sent to and from the client.
 * This internal class uses ProtocolLib to perform certain functions hard or
 * impossible with the Bukkit API. Instances of this class are expected to be
 * {@code null} if and only if ProtocolLib is not available. This class contains
 * implementations of many protocol utility methods in the {@link Utilities}
 * class.
 * 
 * This class is not in the {@link me.pagekite.glen3b.library.bukkit.protocol} package because it is internally used, and restrictions with access modifiers are not possible in that package.
 * 
 * @author Glen Husman
 */
final class ProtocolUtilities {

	/**
	 * Initialize protocol utilities.
	 * 
	 * @param plugin
	 *            The plugin for which events will be subscribed, and will
	 *            handle packet reception.
	 */
	public void init(Plugin plugin) {
		// Prevents ghost step sounds and particles (testing needs to be done)
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(PacketAdapter
						.params(plugin, PacketType.Play.Server.SET_SLOT,
								PacketType.Play.Server.WINDOW_ITEMS)
								.serverSide().listenerPriority(ListenerPriority.HIGH)) {

					@Override
					public void onPacketSending(PacketEvent event) {
						// TODO: Can this be done more efficiently?
						PacketContainer cloned = event.getPacket().deepClone();
						
						if (event.getPacketType().equals(PacketType.Play.Server.SET_SLOT)) {
							ItemStack[] toMod = new ItemStack[] { cloned.getItemModifier().read(0) };
							addGlow(toMod);
							cloned.getItemModifier().write(0, toMod[0]);
						} else {
							ItemStack[] toMod = cloned.getItemArrayModifier().read(0);
							addGlow(toMod);
							cloned.getItemArrayModifier().write(0, toMod);
						}
						event.setPacket(cloned);
					}
				});
	}

	private static final String NBT_INDICATOR_KEY = "GBukkitLib-ItemGlow";
	private static final byte NBT_INDICATOR_GLOW_VALUE = Byte.MAX_VALUE;


	/**
	 * Set the glowing status of an item stack.
	 *
	 * @param stack the item stack to modify
	 * @param glowing true to make the item glow, false to stop it glowing
	 */
	public ProtocolOperationResult setGlowing(ItemStack stack, boolean glowing) {

		try{
			NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(stack);
			if(glowing){	
				if(compound.containsKey(NBT_INDICATOR_KEY) && compound.getByte(NBT_INDICATOR_KEY) == NBT_INDICATOR_GLOW_VALUE){
					return ProtocolOperationResult.NOT_NEEDED;
				}
				compound.put(NbtFactory.of(NBT_INDICATOR_KEY, NBT_INDICATOR_GLOW_VALUE));
			}else{
				if(compound.containsKey(NBT_INDICATOR_KEY)){
					compound.remove(NBT_INDICATOR_KEY);
				}else{
					return ProtocolOperationResult.NOT_NEEDED;
				}
			}
			NbtFactory.setItemTag(stack, compound);
			return ProtocolOperationResult.SUCCESS_QUEUED;
		}catch(Exception ex){
			ex.printStackTrace();
			return ProtocolOperationResult.FAILURE;
		}
	}

	private void addGlow(ItemStack[] stacks) {
		for (ItemStack stack : stacks) {
			if (stack != null) {
				NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(stack);
				if(compound.containsKey(NBT_INDICATOR_KEY) && compound.getByte(NBT_INDICATOR_KEY) == NBT_INDICATOR_GLOW_VALUE){
					// If our custom NBT key exists and is set to the appropriate value, overwrite enchantment glow so it will render as glow w/o enchants
					compound.put(NbtFactory.ofList("ench"));
					NbtFactory.setItemTag(stack, compound);
				}
			}
		}
	}
}
