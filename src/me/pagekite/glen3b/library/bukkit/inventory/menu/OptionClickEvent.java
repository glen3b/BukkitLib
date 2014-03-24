package me.pagekite.glen3b.library.bukkit.inventory.menu;

import org.bukkit.entity.Player;

/**
 * The event raised by the inventory when an option in the inventory menu is clicked.
 * @author Glen Husman
 */
public class OptionClickEvent {
	private int position;
	private String name;
	private boolean close;
	private boolean destroy;
	private Player player;
	
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
	
	/**
	 * Creates an instance of this event.
	 * @param who The player who clicked on the inventory.
	 * @param position The zero-based position within the inventory which was clicked.
	 * @param name The name of the clicked item.
	 */
	public OptionClickEvent(Player who, int position, String name) {
		this.player = who;
		this.position = position;
		this.name = name;
		this.close = true;
		this.destroy = false;
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
	 * Gets the name of the item.
	 * @return The name of the clicked item.
	 */
	public String getName() {
		return name;
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
}
