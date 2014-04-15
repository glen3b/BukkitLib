package me.pagekite.glen3b.library.bukkit.menu.inventory;

import java.util.ArrayList;
import java.util.List;

import me.pagekite.glen3b.library.bukkit.GBukkitLibraryPlugin;
import me.pagekite.glen3b.library.bukkit.Utilities;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/**
 * Creates an inventory that acts as a menu.
 * 
 * @author nisovin (<a
 *         href="http://forums.bukkit.org/threads/icon-menu.108342/">original
 *         code</a>)
 * @author Glen Husman
 */
public class InventoryMenu implements Listener {

	protected String name;
	protected int size;

	protected String[] optionNames;
	protected ItemStack[] optionIcons;

	protected List<OptionClickEvent.Handler> _eventHandlers = new ArrayList<OptionClickEvent.Handler>();
	
	private GBukkitLibraryPlugin _plugin;
	
	/**
	 * Creates an inventory menu.
	 * @param name The color formatted name of the menu. This value is assumed to be unique when determining if a clicked inventory was this inventory.
	 * @param size The size of the inventory, which must be a multiple of 9.
	 */
	public InventoryMenu(String name, int size) {
		Validate.notNull(name, "The name must not be null.");
		Validate.isTrue(size > 0 && size % 9 == 0, "The size of the inventory must be a multiple of 9. Size: ", size);
		
		this.name = name;
		this.size = size;
		this.optionNames = new String[size];
		this.optionIcons = new ItemStack[size];
		getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
	}
	
	/**
	 * Removes the icon at the specified position.
	 * @param position The zero-based index of the item.
	 */
	public void deleteOption(int position){
		Validate.isTrue(position >= 0 && position < getSize(), "The position is not within the bounds of the menu. Position: ", position);
		
		optionNames[position] = null;
		optionIcons[position] = null;
	}

	/**
	 * Destroys this inventory menu instance.
	 */
	public void destroy() {
		HandlerList.unregisterAll(this);
		optionNames = null;
		optionIcons = null;
		_eventHandlers.clear();
		_eventHandlers = null;
	}
	
	protected GBukkitLibraryPlugin getPlugin(){
		if(_plugin == null || !_plugin.isEnabled()){
			_plugin = (GBukkitLibraryPlugin)Bukkit.getServer().getPluginManager().getPlugin("GBukkitLib");
		}
		
		return _plugin;
		
	}
	
	/**
	 * Gets the number of available option slots in this {@code InventoryMenu} instance.
	 * @return The number of available option slots in this inventory menu instance. If there is an illegal internal state, this method will return {@code -1}.
	 */
	public int getSize(){
		if(optionNames == null || optionIcons == null || optionNames.length != optionIcons.length){
			// Illegal internal state
			return -1;
		}
		
		return optionNames.length;
	}
	
	/**
	 * Event handler for inventory clicks.
	 * @param event The event.
	 */
	@EventHandler(priority = EventPriority.HIGH)
	protected void onInventoryClick(InventoryClickEvent event) {
		if((event.getView().getBottomInventory() != null && event.getView().getBottomInventory().getTitle().equals(name)) || (event.getView().getTopInventory() != null && event.getView().getTopInventory().getTitle().equals(name))){
			// Stop dupes
			event.setCancelled(true);
			event.setResult(Result.DENY);
		}
		
		if (event.getInventory().getTitle().equals(name) && event.getWhoClicked() instanceof Player) {
			event.setCancelled(true);
			int slot = event.getRawSlot();
			if (slot >= 0 && slot < getSize() && optionIcons[slot] != null) {
				OptionClickEvent e = new OptionClickEvent(
						(Player) event.getWhoClicked(), slot, optionNames[slot]);
				for(OptionClickEvent.Handler handlr :_eventHandlers){
					handlr.onOptionClick(e);
				}
				if (e.willClose()) {
					final Player p = (Player) event.getWhoClicked();
					Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),
							new Runnable() {
								public void run() {
									p.closeInventory();
								}
							}, 1);
				}
				if (e.willDestroy()) {
					destroy();
				}
			}
		}
	}

	/**
	 * Opens this inventory menu for the specified player.
	 * @param player The player for which to show the inventory.
	 * @return The resulting inventory view.
	 */
	public InventoryView open(Player player) {
		Validate.notNull(player, "The player must not be null.");
		
		Inventory inventory = Bukkit.createInventory(player, size, name);
		for (int i = 0; i < optionIcons.length; i++) {
			if (optionIcons[i] != null) {
				inventory.setItem(i, optionIcons[i]);
			}
		}
		return player.openInventory(inventory);
	}

	/**
	 * Register an event handler to be invoked when an option is clicked.
	 * @param handler An event handler that will be invoked upon option selection.
	 */
	public void registerOptionClickHandler(OptionClickEvent.Handler handler){
		Validate.notNull(handler, "The handler must not be null.");
		
		_eventHandlers.add(handler);
	}

	/**
	 * Sets the option at the specified position to the specified item.
	 * The item will have the specified name and lore.
	 * @param position The zero-based index of the item.
	 * @param icon The item itself to use.
	 * @param name The color-formatted name of the item.
	 * @param info The color formatted lore of the item.
	 */
	public void setOption(int position, ItemStack icon, String name,
			String... info) {
		Validate.isTrue(position >= 0 && position < getSize(), "The position is not within the bounds of the menu. Position: ", position);
		Validate.notNull(icon, "The icon is null.");
		Validate.notNull(name, "The item name is null.");
		Validate.noNullElements(info, "Item information contains null values.");
		
		optionNames[position] = name;
		optionIcons[position] = Utilities.Items.setItemNameAndLore(icon, name, info);
	}

}
