package me.pagekite.glen3b.library.bukkit.protocol;

import me.pagekite.glen3b.library.bukkit.Utilities;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Utilities involving modifications to packets sent to and from the client.
 * This internally-used interfaces implementations use a packet-processing
 * library to perform certain functions hard or impossible with the Bukkit API.
 * Instances of this class are expected to be {@code null} if and only if that
 * spracket processor is not available. This interface specifies implementations
 * of all protocol utility methods in the {@link Utilities} class.
 * 
 * <p>
 * This is an internally used type which should not be instantiated,
 * implemented, or called directly by client code. Wrappers in the
 * {@code Utilities} class call this type for you. If you need support for a
 * protocol library not included wiht GBukkitLib, please file an issue ticket or
 * a pull request so we can take care of implementing it.
 * </p>
 * 
 * @author Glen Husman
 */
public interface ProtocolUtilities {

	/**
	 * Initialize protocol utilities.
	 * 
	 * @param plugin
	 *            The plugin for which events will be subscribed, and will
	 *            handle packet reception.
	 */
	public abstract void init(Plugin plugin);

	/**
	 * Assure that the specified {@link ItemStack} is a {@code CraftItemStack}.
	 * 
	 * @param stack
	 *            The {@link ItemStack}.
	 * @return {@code stack} represented as a {@code CraftItemStack}.
	 * @throws Exception In the case of an error. 
	 */
	public abstract ItemStack assureCraftItemStack(ItemStack stack) throws Exception;

	/**
	 * Make a {@code CraftItemStack} "glow."
	 * 
	 * @param stack
	 *            The {@code CraftItemStack} to make glow.
	 * @param glowing
	 *            Whether it should glow.
	 * @return The result of the protocol operation.
	 */
	public abstract ProtocolOperationResult setGlowing(ItemStack stack,
			boolean glowing);

	/**
	 * Perform disable-time cleanup of events and such.
	 * 
	 * @param plugin
	 *            The plugin to use for housekeeping.
	 */
	public abstract void cleanup(Plugin plugin);

}