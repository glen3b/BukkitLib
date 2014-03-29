package me.pagekite.glen3b.library.bukkit.menu.inventory;

import java.util.ArrayList;
import java.util.List;

import me.pagekite.glen3b.library.bukkit.GBukkitLibraryPlugin;
import me.pagekite.glen3b.library.bukkit.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
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

	private String name;
	private int size;

	private String[] optionNames;
	private ItemStack[] optionIcons;

	private List<OptionClickEvent.Handler> _eventHandlers = new ArrayList<OptionClickEvent.Handler>();
	
	/**
	 * Register an event handler to be invoked when an option is clicked.
	 * @param handler An event handler that will be invoked upon option selection.
	 */
	public void registerOptionClickHandler(OptionClickEvent.Handler handler){
		if(handler == null){
			throw new IllegalArgumentException("The handler must not be null.");
		}
		
		_eventHandlers.add(handler);
	}
	
	/**
	 * Creates an inventory menu.
	 * @param name The color formatted name of the menu. This value is assumed to be unique when determining if a clicked inventory was this inventory.
	 * @param size The size of the inventory, which must be a multiple of 9.
	 */
	public InventoryMenu(String name, int size) {
		if(name == null){
			throw new IllegalArgumentException("The name must not be null.");
		}
		
		if(size <= 0 || size % 9 != 0){
			throw new IllegalArgumentException("The size of the inventory must be a multiple of 9.");
		}
		
		this.name = name;
		this.size = size;
		this.optionNames = new String[size];
		this.optionIcons = new ItemStack[size];
		getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
	}

	private GBukkitLibraryPlugin _plugin;
	
	private GBukkitLibraryPlugin getPlugin(){
		if(_plugin == null || !_plugin.isEnabled()){
			_plugin = (GBukkitLibraryPlugin)Bukkit.getServer().getPluginManager().getPlugin("GBukkitLib");
		}
		
		return _plugin;
		
	}
	
	/**
	 * Removes the icon at the specified position.
	 * @param position The zero-based index of the item.
	 */
	public void deleteOption(int position){
		if(position < 0){
			throw new IllegalArgumentException("The position must be a positive index.");
		}
		optionNames[position] = null;
		optionIcons[position] = null;
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
		if(position < 0){
			throw new IllegalArgumentException("The position must be a positive index.");
		}
		if(icon == null || info == null || name == null){
			throw new IllegalArgumentException("All item information must not be null.");
		}
		
		optionNames[position] = name;
		optionIcons[position] = Utilities.setItemNameAndLore(icon, name, info);
	}

	/**
	 * Opens this inventory menu for the specified player.
	 * @param player The player for which to show the inventory.
	 */
	public void open(Player player) {
		if(player == null){
			throw new IllegalArgumentException("The player must not be null.");
		}
		
		Inventory inventory = Bukkit.createInventory(player, size, name);
		for (int i = 0; i < optionIcons.length; i++) {
			if (optionIcons[i] != null) {
				inventory.setItem(i, optionIcons[i]);
			}
		}
		player.openInventory(inventory);
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

	@EventHandler(priority = EventPriority.MONITOR)
	void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getTitle().equals(name)) {
			event.setCancelled(true);
			int slot = event.getRawSlot();
			if (slot >= 0 && slot < size && optionNames[slot] != null) {
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

}
