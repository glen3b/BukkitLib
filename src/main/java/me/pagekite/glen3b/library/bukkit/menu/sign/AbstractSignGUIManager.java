package me.pagekite.glen3b.library.bukkit.menu.sign;

import me.pagekite.glen3b.library.ResultReceived;

import org.bukkit.entity.Player;

public abstract class AbstractSignGUIManager implements SignGUI {

	@Override
	public void open(Player player, ResultReceived<Player, String[]> response) {
		open(player, null, response);
	}
	
	public abstract void destroy();

}
