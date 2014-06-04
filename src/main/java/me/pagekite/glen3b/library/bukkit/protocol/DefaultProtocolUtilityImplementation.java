package me.pagekite.glen3b.library.bukkit.protocol;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import me.pagekite.glen3b.library.bukkit.reflection.InternalPackage.SubPackage;
import me.pagekite.glen3b.library.bukkit.reflection.ReflectionUtilities;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Represents a fully independent, directly reflective implementation of protocol utilities.
 * Not for direct use by client code.
 * @author Glen Husman
 */
public class DefaultProtocolUtilityImplementation implements ProtocolUtilities {

	private Constructor<?> _nbtTagListConstructor;
	private Method _nbtCompound_set;
	private Method _nmsItemStackToCraftbukkitItemStack;
	@Override
	public void init(Plugin plugin) {
		try {
			_nbtTagListConstructor = ReflectionUtilities.getConstructor(ReflectionUtilities.Minecraft.getType("NBTTagList"));
			_nbtCompound_set = ReflectionUtilities.Minecraft.getType("NBTTagCompound").getDeclaredMethod("set", String.class, ReflectionUtilities.Minecraft.getType("NBTBase"));
			_nmsItemStackToCraftbukkitItemStack = SubPackage.INVENTORY.getClass("CraftItemStack").getDeclaredMethod("asCraftMirror", 
					ReflectionUtilities.Minecraft.getType("ItemStack"));
			
		} catch (Exception e) {
			_nbtTagListConstructor = null;
			_nbtCompound_set = null;
			_nmsItemStackToCraftbukkitItemStack = null;
			e.printStackTrace();
		}
	}

	@Override
	public ItemStack assureCraftItemStack(ItemStack stack) throws Exception {
		Validate.notNull(stack);
		
		Object nmsItemStack = ReflectionUtilities.CraftBukkit.getNMSHandle(stack); // Calls appropriate CraftItemStack methods

		// Next, actually build the stack
		return (ItemStack) _nmsItemStackToCraftbukkitItemStack.invoke(
				null, nmsItemStack);
	}
	
	Object setGlowing(Object nmsStack, boolean glowing) throws Exception {
		Object nmsStackDataTag = ReflectionUtilities.getValue(nmsStack, "tag");
		if(nmsStackDataTag == null){
			nmsStackDataTag = ReflectionUtilities.createInstance(ReflectionUtilities.Minecraft.getType("NBTTagCompound"));
		}
		Object enchList = glowing ? _nbtTagListConstructor.newInstance() : null;
		_nbtCompound_set.invoke(nmsStackDataTag, "ench", enchList);
		ReflectionUtilities.setValue(nmsStack, "tag", nmsStackDataTag);
		return nmsStack;
		
	}

	@Override
	public ProtocolOperationReturn<ItemStack> setGlowing(ItemStack stack, boolean glowing) {
		if(_nbtTagListConstructor == null || _nbtCompound_set == null || _nmsItemStackToCraftbukkitItemStack == null){
			return new ProtocolOperationReturn<ItemStack>(ProtocolOperationResult.FAILURE);
		}
		
		if(stack == null){
			return new ProtocolOperationReturn<ItemStack>(ProtocolOperationResult.FAILURE_INCORRECT_ARGUMENT_TYPE);
		}
		
		if(stack.getItemMeta().hasEnchants()){
			return new ProtocolOperationReturn<ItemStack>(ProtocolOperationResult.FAILURE);
		}
		
		try{
			return new ProtocolOperationReturn<ItemStack>(ProtocolOperationResult.SUCCESS, (ItemStack)_nmsItemStackToCraftbukkitItemStack.invoke(null, setGlowing(ReflectionUtilities.CraftBukkit.getNMSHandle(stack), glowing)));
		}catch(Exception except){
			return new ProtocolOperationReturn<ItemStack>(ProtocolOperationResult.FAILURE, except);
		}
		
		
	}

	@Override
	public void cleanup(Plugin plugin) {
		_nbtTagListConstructor = null;
		_nbtCompound_set = null;
		_nmsItemStackToCraftbukkitItemStack = null;
	}

}
