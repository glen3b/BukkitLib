package me.pagekite.glen3b.library.bukkit.protocol;

import static me.pagekite.glen3b.library.bukkit.protocol.ProtocolLibUtilImplementation.GLOW_ENCHANT_INDICATOR;
import static me.pagekite.glen3b.library.bukkit.protocol.ProtocolLibUtilImplementation.GLOW_ENCHANT_LEVEL;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

import me.bigteddy98.packetapi.PacketAPI;
import me.bigteddy98.packetapi.api.PacketHandler;
import me.bigteddy98.packetapi.api.PacketListener;
import me.bigteddy98.packetapi.api.PacketSendEvent;
import me.pagekite.glen3b.library.bukkit.reflection.ReflectionUtilities;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

/**
 * A wrapper for <em>some</em> protocol abilities in PacketAPI. Little wrapper classes exist in the library in question, so reflection is still required.
 * @author Glen Husman
 */
public class PacketAPIUtilityImplementation implements ProtocolUtilities, PacketListener {

	/**
	 * Field used to invoke methods not directly supported by this library, as we can use one reflection cache.
	 */
	private DefaultProtocolUtilityImplementation _purelyReflectiveImplementation;

	/**
	 * Method that uses the NMS enchantment manager to get the level of an enchantment on a stack.
	 * Returns 0 if enchantment doesn't exist.
	 */
	private Method _getEnchantmentLevel;

	/*
	 * Lazy initializers of reflective access to the two packet types we receive.
	 * I don't go by field name, but instead by field type.
	 * It feels like reimplementing ProtocolLib's fabulous "fuzzy reflection," but hackier.
	 */

	private void initializeWindowItemCache(){
		if(_windowItemsStackArray == null){
			try {
				for(Field possible : ReflectionUtilities.Minecraft.getType("PacketPlayOutWindowItems").getDeclaredFields()){
					if(possible.getType().isArray() && possible.getType().getComponentType().equals(ReflectionUtilities.Minecraft.getType("ItemStack"))){
						// Assume we found a match
						possible.setAccessible(true);
						_windowItemsStackArray = possible;
						break;
					}
				}
			} catch (Exception e) {
				Bukkit.getLogger().log(Level.SEVERE, "Error loading internal Minecraft packet class required for item glows.", e);
				return;
			}
		}
	}

	private Field _windowItemsStackArray;
	private Field _setSlotItemStack;

	private void initializeSetSlotCache(){
		if(_setSlotItemStack == null){
			try {
				for(Field possible : ReflectionUtilities.Minecraft.getType("PacketPlayOutSetSlot").getDeclaredFields()){
					if(possible.getType().equals(ReflectionUtilities.Minecraft.getType("ItemStack"))){
						// Assume we found a match
						possible.setAccessible(true);
						_setSlotItemStack = possible;
						break;
					}
				}
			} catch (Exception e) {
				Bukkit.getLogger().log(Level.SEVERE, "Error loading internal Minecraft packet class required for item glows.", e);
				return;
			}
		}
	}

	// End fuzzy match initializers

	@Override
	public void init(Plugin plugin) {
		_purelyReflectiveImplementation = Bukkit.getServicesManager().getRegistration(DefaultProtocolUtilityImplementation.class).getProvider();
		PacketAPI.getInstance().addListener(this);
		try {
			_getEnchantmentLevel = ReflectionUtilities.Minecraft.getType("EnchantmentManager").getDeclaredMethod("getEnchantmentLevel", int.class, ReflectionUtilities.Minecraft.getType("ItemStack"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation") // Enchantment ID retrieval
	@PacketHandler
	public void onSend(PacketSendEvent event) {
		/*
		 * According to the eclipse compiler, assuming T is a type and T1 extends, T, this is OK:
		 * T[] a = new T1[0];
		 * 
		 * According to my runtime tests, it also works, but throws an ArrayStoreException if you try to put a T that's not a T1 into the a array.
		 * 
		 * 
		 * 
		 * Therefore, an Object[] can represent a net.minecraft.server.ItemStack[] safely
		 */

		try{
			// Since we only mess with references to either array elements or items themselves, no setting is needed :D
			
			if (event.getPacket().getName().equals("PacketPlayOutWindowItems")) {
				initializeWindowItemCache();
				Object[] itemsToUpdate = (Object[]) _windowItemsStackArray.get(event.getPacket().getNMSPacket());
				
				for(Object nmsItem : itemsToUpdate){
					if(((Integer)_getEnchantmentLevel.invoke(null, GLOW_ENCHANT_INDICATOR.getId(), nmsItem)).intValue() == GLOW_ENCHANT_LEVEL){
						// Make the ItemStack in the sent packet GLOW
						_purelyReflectiveImplementation.setGlowing(nmsItem, true);
					}
				}
			}else if (event.getPacket().getName().equals("PacketPlayOutSetSlot")){
				initializeSetSlotCache();
				Object nmsItem = _setSlotItemStack.get(event.getPacket().getNMSPacket());
				if(((Integer)_getEnchantmentLevel.invoke(null, GLOW_ENCHANT_INDICATOR.getId(), nmsItem)).intValue() == GLOW_ENCHANT_LEVEL){
					// Make the ItemStack in the sent packet GLOW
					_purelyReflectiveImplementation.setGlowing(nmsItem, true);
				}
			}else{
				return;
			}
		}catch(Exception except){
			except.printStackTrace();
		}
	}

	@Override
	public ItemStack assureCraftItemStack(ItemStack stack) throws Exception {
		return _purelyReflectiveImplementation.assureCraftItemStack(stack);
	}

	@Override
	public ProtocolOperationReturn<ItemStack> setGlowing(ItemStack stack,
			boolean glowing) {
		initializeWindowItemCache();
		initializeSetSlotCache();

		try{
			ItemMeta m = stack.getItemMeta();
			if(glowing){	
				if(m.hasEnchants()){
					return new ProtocolOperationReturn<ItemStack>(ProtocolOperationResult.FAILURE);
				}
				m.addEnchant(GLOW_ENCHANT_INDICATOR, GLOW_ENCHANT_LEVEL, true);
			}else{
				if(stack.containsEnchantment(GLOW_ENCHANT_INDICATOR) && stack.getEnchantmentLevel(GLOW_ENCHANT_INDICATOR) == GLOW_ENCHANT_LEVEL){
					m.removeEnchant(GLOW_ENCHANT_INDICATOR);
				}else{
					return new ProtocolOperationReturn<ItemStack>(ProtocolOperationResult.NOT_NEEDED);
				}
			}
			stack.setItemMeta(m);
			return new ProtocolOperationReturn<ItemStack>(ProtocolOperationResult.SUCCESS_QUEUED, stack);
		}catch(Exception ex){
			ex.printStackTrace();
			return new ProtocolOperationReturn<ItemStack>(ProtocolOperationResult.FAILURE, ex);
		}
	}

	@Override
	public void cleanup(Plugin plugin) {
		// PacketAPI does not support unregistering listeners :/
		// I hope it takes care of that for us
	}

}
