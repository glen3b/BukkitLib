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
	 * Creates an inventory menu builder.
	 * @param name The name of the inventory.
	 * @param rows The number of rows in the inventory.
	 * @return The new factory instance.
	 */
	public static Builder create(String name, int rows){
		return new Builder(new CyclingInventoryMenu(name, rows * 9));
	}
	
	/**
	 * Creates an inventory menu the size of a chest (3 rows).
	 * @param name The name of the inventory.
	 * @return The new factory instance.
	 */
	public static Builder createChest(String name){
		return create(name, 3);
	}
	
	/**
	 * Creates an inventory menu the size of a large chest (6 rows).
	 * @param name The name of the inventory.
	 * @return The new factory instance.
	 */
	public static Builder createLargeChest(String name){
		return create(name, 6);
	}
	
	/**
	 * Creates an inventory menu with one row.
	 * @param name The name of the inventory.
	 * @return The new factory instance.
	 */
	public static Builder createRow(String name){
		return create(name, 1);
	}
	
	/**
	 * A class for the creation of cycling inventory menus.
	 * @author Glen Husman
	 */
	public static class Builder extends InventoryMenu.Builder {

		/**
		 * Creates an inventory menu factory.
		 * @param wrapped The inventory menu so far.
		 */
		public Builder(CyclingInventoryMenu wrapped){
			super(wrapped);
		}
		
		/**
		 * Builds the inventory menu.
		 * @return A reference to the inventory menu created by this factory.
		 */
		public CyclingInventoryMenu build(){
			return (CyclingInventoryMenu)_wrapped;
		}
		
		/**
		 * Registers an option selection handler.
		 * @param handler The option click handler.
		 * @return This instance.
		 */
		public Builder registerClickHandler(OptionClickEvent.Handler handler){
			_wrapped.registerOptionClickHandler(handler);
			
			return this;
		}
		
		/**
		 * Removes the icon at the specified position.
		 * @param position The zero-based index of the item.
		 * @return This instance.
		 */
		public Builder removeOption(int position){
			_wrapped.deleteOption(position);
			
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
		public Builder setOption(int position, ItemStack icon, String name,
				String... info){
			_wrapped.setOption(position, icon, name, info == null ? new String[0] : info);
			
			return this;
		}
		
		/**
		 * Sets the option at the specified position to the specified item.
		 * @param position The zero-based index of the item.
		 * @param icon The item itself to use.
		 * @return This instance.
		 */
		public Builder setOption(int position, ItemStack icon){
			_wrapped.setOption(position, icon);
			
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
		public Builder setOption(int position, ItemStack[] icons, long cycleDelay){
			((CyclingInventoryMenu)_wrapped).setOption(position, icons, cycleDelay);
			
			return this;
		}
		
	}
	
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
		 * Cycle number, raw. Modulo is used by the class (to avoid runtime changes in array size).
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
	 * @see #isSchedulerOptimizationEnabled()
	 * @see #setSchedulerOptimizationEnabled(boolean)
	 */
	private boolean _optimizeCyclerScheduling = true;

	/**
	 * Determines whether cycler slot scheduling optimization is enabled.
	 * Optimization is defined as <em>not</em> scheduling cycler tasks if the array size is one.
	 * The default value of this is {@code true}.
	 * @return A {@code boolean} representing the presence value of scheduler cycling.
	 * @see #setSchedulerOptimizationEnabled(boolean)
	 */
	public boolean isSchedulerOptimizationEnabled(){
		return _optimizeCyclerScheduling;
	}
	
	/**
	 * Sets the enabled status of scheduler optimization.
	 * The only reason this should ever be set to {@code false} is if an item element at a given slot does not cycle,
	 * but its value may change at runtime during inventory viewing (and the user will want to see this change).
	 * With the optimizer enabled, the change from a single cycle item to another single cycle item will not be viewed by the user.
	 * However, without the optimizer enabled, the user will see the change as soon as the cycler delay for that slot has elapsed.
	 * @param value The new status of cycler scheduling optimization.
	 * @see #isSchedulerOptimizationEnabled()
	 */
	public void setSchedulerOptimizationEnabled(boolean value){
		_optimizeCyclerScheduling = value;
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
		optionIcons = null;
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
		if (optionIcons == null || cycleDelays == null
				|| optionIcons.length != optionIcons.length) {
			// Illegal internal state
			return -1;
		}

		return optionIcons.length;
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
						(Player) event.getWhoClicked(), slot, event.getCurrentItem());
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
	 * Gets the items currently set for the option at the specified position.
	 * This method does not make any guarantees about the reference status of the returned object.
	 * 
	 * @param index
	 *            The zero-based index of the item.
	 */
	public ItemStack[] getOption(int index){
		Validate.isTrue(
				index >= 0 && index < getSize(),
				"The position is not within the bounds of the menu. Position: ",
				index);
		
		return optionIcons[index] == null ? new ItemStack[0] : optionIcons[index];
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
				if(cycleDelays[i] <= 0){
					// Invalid delay, invalid state
					if(!isSchedulerOptimizationEnabled() || optionIcons[i].length > 1){ // If the item SHOULD be cycled
						// Throw an illegal state exception
						throw new IllegalStateException("An invalid cycler delay was specified for a slot which should be cycled. Delay value: " + cycleDelays[i]);
					}
					// Otherwise no scheduling is needed and we can ignore safely
				}else{
					// Valid delay
					if (!isSchedulerOptimizationEnabled() || optionIcons[i].length > 1) {
						// Schedule if needed
						new ItemCycler(i, inventory).schedule(cycleDelays[i]);
					}
				}
			}
		}
		return player.openInventory(inventory);
	}

	/**
	 * Sets the option at the specified position to the specified item.
	 * 
	 * @param position
	 *            The zero-based index of the item.
	 * @param icon
	 *            The item itself to use.
	 */
	public void setOption(int position, ItemStack icon) {
		setOption(position, new ItemStack[] { icon }, 0L);
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
		setOption(position, new ItemStack[] { Utilities.Items.setItemNameAndLore(
				icon, name, info) }, name, 0L);
	}

	/**
	 * Sets the option at the specified position to the specified item. The
	 * items will have no {@code ItemMeta} modified.
	 * 
	 * @param position
	 *            The zero-based index of the item.
	 * @param icons
	 *            The items to use.
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
	 *            all provided names. If it is {@code null}, the names will remain
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

		optionIcons[position] = icons;
		cycleDelays[position] = cycleDelay;
	}

}
