package me.pagekite.glen3b.library.bukkit.menu.inventory;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

/**
 * A class for the creation of cycling inventory menus.
 * @author Glen Husman
 */
public final class CyclingInventoryMenuFactory {

	/**
	 * The inventory menu as constructed so far.
	 */
	protected CyclingInventoryMenu _wrapped;
	
	/**
	 * Gets the size of the inventory.
	 * @return The size (in slots) of the underlying inventory menu.
	 */
	public int getSize(){
		return _wrapped.getSize();
	}
	
	/**
	 * Creates an inventory menu with one row.
	 * @param name The name of the inventory.
	 * @return The new factory instance.
	 */
	public static CyclingInventoryMenuFactory createRow(String name){
		return create(name, 1);
	}
	
	/**
	 * Creates an inventory menu.
	 * @param name The name of the inventory.
	 * @param rows The number of rows in the inventory.
	 * @return The new factory instance.
	 */
	public static CyclingInventoryMenuFactory create(String name, int rows){
		return new CyclingInventoryMenuFactory(new CyclingInventoryMenu(name, rows * 9));
	}
	
	/**
	 * Creates an inventory menu the size of a chest (3 rows).
	 * @param name The name of the inventory.
	 * @return The new factory instance.
	 */
	public static CyclingInventoryMenuFactory createChest(String name){
		return create(name, 3);
	}
	
	/**
	 * Creates an inventory menu the size of a large chest (6 rows).
	 * @param name The name of the inventory.
	 * @return The new factory instance.
	 */
	public static CyclingInventoryMenuFactory createLargeChest(String name){
		return create(name, 6);
	}
	
	/**
	 * Builds the inventory menu.
	 * @return A reference to the inventory menu created by this factory.
	 */
	public CyclingInventoryMenu build(){
		return _wrapped;
	}
	
	/**
	 * Registers an option selection handler.
	 * @param handler The option click handler.
	 * @return This instance.
	 */
	public CyclingInventoryMenuFactory registerClickHandler(OptionClickEvent.Handler handler){
		_wrapped.registerOptionClickHandler(handler);
		
		return this;
	}
	
	/**
	 * Removes the icon at the specified position.
	 * @param position The zero-based index of the item.
	 * @return This instance.
	 */
	public CyclingInventoryMenuFactory removeOption(int position){
		_wrapped.deleteOption(position);
		
		return this;
	}
	
	/**
	 * Sets the option at the specified position to the specified item.
	 * The item will have the name and lore as they were when the array was passed in.
	 * @param position The zero-based index of the item.
	 * @param icons The items to use.
	 * @param cycleDelay The delay between cycles of the item, in server ticks.
	 * @return This instance.
	 */
	public CyclingInventoryMenuFactory setOption(int position, ItemStack[] icons, long cycleDelay){
		_wrapped.setOption(position, icons, cycleDelay);
		
		return this;
	}
	
	/**
	 * Sets the option at the specified position to the specified item.
	 * The item will have the specified name and lore. All strings must be color-formatted by the caller.
	 * @param position The zero-based index of the item.
	 * @param icon The item itself to use.
	 * @param name The color-formatted name of the item.
	 * @param info The color-formatted lore of the item.
	 * @return This instance.
	 */
	public CyclingInventoryMenuFactory setOption(int position, ItemStack icon, String name,
			String... info){
		_wrapped.setOption(position, icon, name, info == null ? new String[0] : info);
		
		return this;
	}
	
	/**
	 * Creates an inventory menu factory.
	 * @param wrapped The inventory menu so far.
	 */
	public CyclingInventoryMenuFactory(CyclingInventoryMenu wrapped){
		Validate.notNull(wrapped, "The wrapped inventory menu must not be null.");
		Validate.isTrue(wrapped.getSize() > 0, "The wrapped inventory menu has an illegal internal state.");
		
		_wrapped = wrapped;
	}
	
}
