package me.pagekite.glen3b.library.bukkit.menu.sign;

import org.bukkit.entity.Player;

/**
 * Represents a listener for when the player successfully edits a fake sign created by the sign GUI manager.
 * These listeners are invoked on the main server thread.
 * @author <a href="http://forums.bukkit.org/members/nisovin.2980/">nisovian</a>
 */
public interface SignSubmitListener {
	
	/**
	 * Invoked when the editing of a certain sign GUI is completed.
	 * @param player The player who edited the sign.
	 * @param lines The lines of the sign sent by the player from the sign interface.
	 */
	public void onEditComplete(Player player, String[] lines);
}
