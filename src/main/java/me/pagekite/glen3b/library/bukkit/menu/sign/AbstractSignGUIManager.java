package me.pagekite.glen3b.library.bukkit.menu.sign;

import org.bukkit.entity.Player;

public abstract class AbstractSignGUIManager implements SignGUI {

	@Override
	public void open(Player player, SignSubmitListener response) {
		open(player, null, response);
	}
	
	public abstract void destroy();

}
