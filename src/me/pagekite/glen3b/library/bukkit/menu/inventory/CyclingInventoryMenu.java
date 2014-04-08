package me.pagekite.glen3b.library.bukkit.menu.inventory;

import me.pagekite.glen3b.library.bukkit.Utilities;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Creates an inventory that acts as a menu. The items in this inventory will
 * "cycle" through various choices.
 * 
 * @author nisovin (<a
 *         href="http://forums.bukkit.org/threads/icon-menu.108342/">original
 *         code</a>)
 * @author Glen Husman
 */
public class CyclingInventoryMenu extends InventoryMenu {

	/**
	 * Runnable that cycles through items at a specified index.
	 * 
	 * @author Glen Husman
	 */
	protected class ItemCycler extends BukkitRunnable {

		/**
		 * The inventory that is viewed and actually contains the cycled items.
		 */
		protected Inventory _viewed;

		/**
		 * Item index to cycle through. In the jagged array, this is the first
		 * index.
		 */
		protected int _itemIndex;

		/**
		 * Cycle number. Modulo is used by the class (to avoid runtime changes)
		 */
		protected int _cycleNumber;

		private int _taskID = -1;

		/**
		 * Creates an item cycler.
		 * 
		 * @param itemIndex
		 *            The index of the item array in the jagged array.
		 * @param created
		 *            The inventory which is being viewed.
		 */
		public ItemCycler(int itemIndex, Inventory created) {
			Validate.notNull(created, "Inventories can't be null!");
			Validate.isTrue(itemIndex >= 0 && itemIndex < optionIcons.length,
					"Index not within the bounds of the array.");

			_itemIndex = itemIndex;
			_viewed = created;
		}

		/**
		 * Cancels the task.
		 */
		public void cancel() {
			if (_taskID == -1) {
				return;
			}

			getPlugin().getServer().getScheduler().cancelTask(_taskID);
			_taskID = -1;
		}

		/**
		 * Gets the task ID.
		 * 
		 * @return The current scheduled task ID, or -1 if unscheduled.
		 */
		public int getTaskID() {
			return _taskID;
		}

		/**
		 * Runs the task.
		 */
		@Override
		public void run() {
			_viewed.setItem(_itemIndex, optionIcons[_itemIndex][++_cycleNumber
					% optionIcons[_itemIndex].length]);

			if (_viewed.getViewers().size() < 1) {
				// Nobody's watching us
				cancel();
			}
		}

		/**
		 * Schedule the task.
		 * 
		 * @param interval
		 *            The interval, in server ticks.
		 */
		public void schedule(long interval) {
			_taskID = getPlugin()
					.getServer()
					.getScheduler()
					.scheduleSyncRepeatingTask(getPlugin(), this, interval,
							interval);
		}

	}
	protected Long[] cycleDelays;

	protected ItemStack[][] optionIcons;

	/**
	 * Creates an inventory menu.
	 * 
	 * @param name
	 *            The color formatted name of the menu. This value is assumed to
	 *            be unique when determining if a clicked inventory was this
	 *            inventory.
	 * @param size
	 *            The size of the inventory, which must be a multiple of 9.
	 */
	public CyclingInventoryMenu(String name, int size) {
		super(name, size);

		this.optionIcons = new ItemStack[size][];
		this.cycleDelays = new Long[size];
	}

	/**
	 * Removes the icon at the specified position.
	 * 
	 * @param position
	 *            The zero-based index of the item.
	 */
	public void deleteOption(int position) {
		super.deleteOption(position);
		cycleDelays[position] = -1L;
	}

	/**
	 * Destroys this inventory menu instance.
	 */
	public void destroy() {
		super.destroy();
		cycleDelays = null;
	}

	/**
	 * Gets the number of available option slots in this {@code InventoryMenu}
	 * instance.
	 * 
	 * @return The number of available option slots in this inventory menu
	 *         instance. If there is an illegal internal state, this method will
	 *         return {@code -1}.
	 */
	public int getSize() {
		if (optionNames == null || optionIcons == null || cycleDelays == null
				|| optionNames.length != optionIcons.length) {
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
			if (slot >= 0 && slot < getSize() && optionIcons[slot] != null && optionIcons[slot].length > 0) {
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
	 * 
	 * @param player
	 *            The player for which to show the inventory.
	 * @return The resulting inventory view.
	 */
	public InventoryView open(Player player) {
		Validate.notNull(player, "The player must not be null.");

		Inventory inventory = Bukkit.createInventory(player, size, name);
		for (int i = 0; i < optionIcons.length; i++) {
			if (optionIcons[i] != null && optionIcons[i].length > 0) {
				inventory.setItem(i, optionIcons[i][0]);
				if (optionIcons[i].length > 1) {
					new ItemCycler(i, inventory).schedule(cycleDelays[i]);
				}
			}
		}
		return player.openInventory(inventory);
	}

	/**
	 * Sets the option at the specified position to the specified item. The item
	 * will have the specified name and lore.
	 * 
	 * @param position
	 *            The zero-based index of the item.
	 * @param icon
	 *            The item itself to use.
	 * @param name
	 *            The color-formatted name of the item.
	 * @param info
	 *            The color-formatted lore of the item.
	 */
	public void setOption(int position, ItemStack icon, String name,
			String... info) {
		setOption(position, new ItemStack[] { Utilities.setItemNameAndLore(
				icon, name, info) }, name, 0L);
	}

	/**
	 * Sets the option at the specified position to the specified item. The
	 * items will have no {@code ItemMeta} modified.
	 * 
	 * @param position
	 *            The zero-based index of the item.
	 * @param icon
	 *            The item itself to use.
	 * @param cycleDelay
	 *            The time between cycling through items, in server ticks.
	 */
	public void setOption(int position, ItemStack[] icons, long cycleDelay) {
		setOption(position, icons, null, cycleDelay);
	}
	
	/**
	 * Sets the option at the specified position to the specified item. The
	 * items will have their display name modified.
	 * 
	 * @param position
	 *            The zero-based index of the item.
	 * @param icons
	 *            The items to use.
	 * @param name
	 *            The color-formatted name of the item. This name will override
	 *            all provided names. If it is null, the name will remain
	 *            unchanged.
	 * @param cycleDelay
	 *            The time between cycling through items, in server ticks.
	 */
	public void setOption(int position, ItemStack[] icons, String name,
			long cycleDelay) {
		Validate.isTrue(
				position >= 0 && position < getSize(),
				"The position is not within the bounds of the menu. Position: ",
				position);
		Validate.notEmpty(icons, "The icons array is null or empty.");
		Validate.noNullElements(icons, "Some icons icons are null.");
		Validate.isTrue(cycleDelay > 0 || icons.length == 1,
				"The cycle delay must be at least one tick.");

		if (name != null) {
			for (ItemStack stack : icons) {
				ItemMeta data = stack.getItemMeta();
				data.setDisplayName(name);
				stack.setItemMeta(data);
			}
		}

		optionNames[position] = name;
		optionIcons[position] = icons;
		cycleDelays[position] = cycleDelay;
	}

}
