package me.pagekite.glen3b.library.bukkit.protocol;

import me.pagekite.glen3b.library.bukkit.menu.sign.SignGUI;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

/**
 * This internal class uses <a href="">ProtocolLib</a> to perform certain functions hard or
 * impossible with the Bukkit API.
 * 
 * <p>
 * <b>This is an internally used class which should not be instantiated or called directly by client code.</b>
 * Wrappers in the {@code Utilities} class call this type for you.
 * </p>
 * 
 * @author Glen Husman
 */
public final class ProtocolLibUtilImplementation implements ProtocolUtilities {

	ProtocolLibSignGUI _signManager;
	
	/* (non-Javadoc)
	 * @see me.pagekite.glen3b.library.bukkit.protocol.ProtocolUtilities#init(org.bukkit.plugin.Plugin)
	 */
	@Override
	public void init(Plugin plugin) {
		// Prevents ghost step sounds and particles (testing needs to be done)
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(PacketAdapter
						.params(plugin, PacketType.Play.Server.SET_SLOT,
								PacketType.Play.Server.WINDOW_ITEMS)
								.serverSide().listenerPriority(ListenerPriority.HIGH)) {

					@Override
					public void onPacketSending(PacketEvent event) {
						if (event.getPacketType().equals(PacketType.Play.Server.SET_SLOT)) {
							addGlow(new ItemStack[] { event.getPacket().getItemModifier().read(0) });
						} else {
							addGlow(event.getPacket().getItemArrayModifier().read(0));
						}
					}
				});
		
		_signManager = new ProtocolLibSignGUI(plugin);
		
		Bukkit.getServicesManager().register(SignGUI.class, _signManager, plugin, ServicePriority.Highest);
	}

	private static final Enchantment GLOW_ENCHANT_INDICATOR = Enchantment.LURE;
	private static final int GLOW_ENCHANT_LEVEL = 31762;


	/* (non-Javadoc)
	 * @see me.pagekite.glen3b.library.bukkit.protocol.ProtocolUtilities#assureCraftItemStack(org.bukkit.inventory.ItemStack)
	 */
	@Override
	public ItemStack assureCraftItemStack(ItemStack stack){
		if(!MinecraftReflection.isCraftItemStack(stack)){
			return MinecraftReflection.getBukkitItemStack(stack);
		}
		return stack;
	}

	/* (non-Javadoc)
	 * @see me.pagekite.glen3b.library.bukkit.protocol.ProtocolUtilities#setGlowing(org.bukkit.inventory.ItemStack, boolean)
	 */
	@Override
	public ProtocolOperationResult setGlowing(ItemStack stack, boolean glowing) {

		if(!MinecraftReflection.isCraftItemStack(stack)){
			return ProtocolOperationResult.FAILURE_INCORRECT_ARGUMENT_TYPE;
		}
		
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

	@Override
	public void cleanup(Plugin plugin) {
		// This class is expected to be responsible for all GBukkitLib protocol listeners
		ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
		_signManager.destroy();
	}
}
