package me.pagekite.glen3b.library.bukkit.protocol;

import me.pagekite.glen3b.library.bukkit.Utilities;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

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
 * <p>
 * <b>This is an internally used class which should not be instantiated or called directly by client code.</b>
 * Wrappers in the {@code Utilities} class call this type for you.
 * </p>
 * 
 * @author Glen Husman
 */
public final class ProtocolUtilities {

	/**
	 * Initialize protocol utilities.
	 * 
	 * @param plugin
	 *            The plugin for which events will be subscribed, and will
	 *            handle packet reception.
	 */
	public void init(Plugin plugin) {

	}

	private static final Enchantment GLOW_ENCHANT_INDICATOR = Enchantment.LURE;
	private static final int GLOW_ENCHANT_LEVEL = 31762;


	public ItemStack assureCraftItemStack(ItemStack stack){
		if(!MinecraftReflection.isCraftItemStack(stack)){
			return MinecraftReflection.getBukkitItemStack(stack);
		}
		return null;
	}

	public ProtocolOperationResult setGlowing(ItemStack stack, boolean glowing) {

		try{
			ItemMeta m = stack.getItemMeta();
			if(glowing){	
				if(m.hasEnchants()){
					return ProtocolOperationResult.FAILURE;
				}
					if (stack != null && stack.hasItemMeta()) {
						if(stack.containsEnchantment(GLOW_ENCHANT_INDICATOR) && stack.getEnchantmentLevel(GLOW_ENCHANT_INDICATOR) == GLOW_ENCHANT_LEVEL){
							// If our custom enchant exists and is set to the appropriate value, overwrite enchantment glow so it will render as glow w/o enchants
							NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(stack);
							compound.put(NbtFactory.ofList("ench"));
							NbtFactory.setItemTag(stack, compound);
						}
					
				}
			}else{
					if (stack != null && stack.hasItemMeta()) {
							// If our custom enchant exists and is set to the appropriate value, overwrite enchantment glow so it will render as glow w/o enchants
							NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(stack);
							compound.remove("ench");
							NbtFactory.setItemTag(stack, compound);
				}
			}
			stack.setItemMeta(m);
			return ProtocolOperationResult.SUCCESS;
		}catch(Exception ex){
			ex.printStackTrace();
			return ProtocolOperationResult.FAILURE;
		}
	}

	public void addGlow(ItemStack[] stacks) {
		
	}
}
