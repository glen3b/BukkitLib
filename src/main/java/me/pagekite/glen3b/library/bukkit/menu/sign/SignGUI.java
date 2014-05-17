package me.pagekite.glen3b.library.bukkit.menu.sign;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;

/**
 * Represents a manager of sign editor interfaces that can be used by plugins.
 * The instance of this interface can be retrieved via the {@linkplain org.bukkit.plugin.ServicesManager Bukkit service manager}.
 * @author <a href="http://forums.bukkit.org/members/nisovin.2980/">nisovian</a>
 */
public interface SignGUI {

	/**
	 * Opens a blank sign editor GUI for the specified player.
	 * @param player The player to whom the editor will be displayed.
	 * @param response The object which will handle the completion of sign editing.
	 */
	public void open(@Nonnull Player player, @Nullable SignSubmitListener response);
	
	/**
	 * Opens a sign editor GUI for the specified player with text already in it.
	 * @param player The player to whom the editor will be displayed.
	 * @param defaultText The text already in the editor GUI.
	 * @param response The object which will handle the completion of sign editing.
	 */
	public void open(@Nonnull Player player, @Nullable String[] defaultText, @Nullable SignSubmitListener response);
	
}
