package me.pagekite.glen3b.library.bukkit;

import me.pagekite.glen3b.library.bukkit.protocol.ProtocolOperationResult;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
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

	private static final Enchantment GLOW_ENCHANT_INDICATOR = Enchantment.LURE;
	private static final int GLOW_ENCHANT_LEVEL = 31762;


	public ItemStack assureCraftItemStack(ItemStack stack){
		if(!MinecraftReflection.isCraftItemStack(stack)){
			return MinecraftReflection.getBukkitItemStack(stack);
		}
		return null;
	}


	/**
	 * Set the glowing status of an item stack.
	 *
	 * @param stack the item stack to modify
	 * @param glowing true to make the item glow, false to stop it glowing
	 */
	public ProtocolOperationResult setGlowing(ItemStack stack, boolean glowing) {

		try{
			ItemMeta m = stack.getItemMeta();
			if(glowing){	
				if(m.hasEnchants()){
					return ProtocolOperationResult.FAILURE;
				}
				m.addEnchant(GLOW_ENCHANT_INDICATOR, GLOW_ENCHANT_LEVEL, true);
			}else{
				if(stack.containsEnchantment(GLOW_ENCHANT_INDICATOR) && stack.getEnchantmentLevel(GLOW_ENCHANT_INDICATOR) == GLOW_ENCHANT_LEVEL){
					m.removeEnchant(GLOW_ENCHANT_INDICATOR);
				}else{
					return ProtocolOperationResult.NOT_NEEDED;
				}
			}
			stack.setItemMeta(m);
			return ProtocolOperationResult.SUCCESS_QUEUED;
		}catch(Exception ex){
			ex.printStackTrace();
			return ProtocolOperationResult.FAILURE;
		}
	}

	private void addGlow(ItemStack[] stacks) {
		for (ItemStack stack : stacks) {
			if (stack != null && stack.hasItemMeta()) {
				if(stack.containsEnchantment(GLOW_ENCHANT_INDICATOR) && stack.getEnchantmentLevel(GLOW_ENCHANT_INDICATOR) == GLOW_ENCHANT_LEVEL){
					// If our custom enchant exists and is set to the appropriate value, overwrite enchantment glow so it will render as glow w/o enchants
					NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(stack);
					compound.put(NbtFactory.ofList("ench"));
					NbtFactory.setItemTag(stack, compound);
				}
			}
		}
	}
}
