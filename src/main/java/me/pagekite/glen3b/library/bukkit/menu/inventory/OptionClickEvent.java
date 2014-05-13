package me.pagekite.glen3b.library.bukkit.menu.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The event raised by the inventory when an option in the inventory menu is clicked.
 * @author Glen Husman
 */
public class OptionClickEvent {
	/**
	 * A handler of an option click event.
	 * @author Glen Husman
	 */
	public static interface Handler{
		/**
		 * The method called when an option in the inventory is clicked.
		 * @param event The event representing this click action.
		 */
		public void onOptionClick(OptionClickEvent event);
	}
	private int position;
	private String name;
	private boolean close;
	private boolean destroy;
	private ItemStack clicked;
	
	private Player player;
	
	/**
	 * Creates an instance of this event.
	 * @param who The player who clicked on the inventory.
	 * @param position The zero-based position within the inventory which was clicked.
	 * @param stack The clicked item.
	 */
	public OptionClickEvent(Player who, int position, ItemStack stack) {
		this.player = who;
		this.position = position;
		this.name = stack.hasItemMeta() && stack.getItemMeta().hasDisplayName() ? stack.getItemMeta().getDisplayName() : /* TODO Better way? */ stack.getType().toString();
		this.clicked = stack;
		this.close = true;
		this.destroy = false;
	}

	/**
	 * Gets the name of the item. It may be {@code null}.
	 * @return The name of the clicked item.
	 * @deprecated This name is no longer set by the creator of the {@link InventoryMenu}. {@link OptionClickEvent#getItem() getItem()} is preferred.
	 */
	@Deprecated
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the item that was clicked.
	 * @return A reference to the {@link ItemStack} clicked by the player.
	 */
	public ItemStack getItem(){
		return clicked;
	}
	
	/**
	 * Get the player who clicked on the inventory.
	 * @return The player who clicked on the inventory and triggered this event.
	 */
	public Player getPlayer(){
		return player;
	}

	/**
	 * Gets the position of the item within the inventory menu.
	 * @return The zero-based position within the inventory which was clicked.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Sets a boolean indicating if the inventory will be closed.
	 * @param close Whether the inventory will be closed as a result of this click.
	 */
	public void setWillClose(boolean close) {
		this.close = close;
	}
	
	/**
	 * Sets a boolean indicating if the inventory will be destroyed.
	 * @param destroy Whether the inventory menu instance will be destroyed as a result of this click.
	 */
	public void setWillDestroy(boolean destroy) {
		this.destroy = destroy;
	}

	/**
	 * Gets a boolean indicating if the inventory will be closed.
	 * @return Whether the inventory will be closed as a result of this click.
	 */
	public boolean willClose() {
		return close;
	}

	/**
	 * Gets a boolean indicating if the inventory will be destroyed.
	 * @return Whether the inventory menu instance will be destroyed as a result of this click.
	 */
	public boolean willDestroy() {
		return destroy;
	}
}
