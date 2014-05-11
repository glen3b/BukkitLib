package me.pagekite.glen3b.library.bukkit.command;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.ImmutableList;

/**
 * Represents a command that can be processed by the {@link PlayerCommandPreprocessEvent}, which will be registered at {@linkplain EventPriority#HIGHEST highest} priority.
 * @author Glen Husman
 */
public final class PreprocessableCommand implements Listener {

	private List<String> _aliases;
	
	/**
	 * Gets an immutable list of all known command aliases.
	 * @return The immutable list of command aliases.
	 */
	public List<String> getAliases(){
		return _aliases;
	}
	
	private PreprocessedCommandHandler _executor;
	
	/**
	 * Gets the currently registered command handler for this command.
	 * @return The (potentially {@code null}) handler of this command.
	 */
	public PreprocessedCommandHandler getExecutor(){
		return _executor;
	}
	
	/**
	 * Sets the command handler for this command, which will be invoked upon command execution.
	 * @param executor The (potentially {@code null}) handler of this command.
	 */
	public void setExecutor(@Nullable PreprocessedCommandHandler executor){
		_executor = executor;
	}
	
	/**
	 * Creates a preprocessor-handled command. This does <i>not</i>register the event handlers.
	 * @param aliases The aliases of the command, not including the preceding '/' character.
	 */
	public PreprocessableCommand(String... aliases){
		Validate.notEmpty(aliases, "At least one alias is required."); // Should also do the null check
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		for(int i = 0; i < aliases.length; i++){
			if(aliases[i] == null || StringUtils.isWhitespace(aliases[i])){
				throw new IllegalArgumentException("Empty aliases are not allowed.");
			}
			builder.add(aliases[i].toLowerCase().trim());
		}
		_aliases = builder.build();
	}
	
	/**
	 * Registers this command handler to process events.
	 * @param plugin The host plugin.
	 */
	public void registerEvents(@Nonnull Plugin plugin){
		Validate.notNull(plugin, "The host plugin must not be null.");
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	/**
	 * Checks for this command as the preprocessed command.
	 * @param event The command preprocessing event.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event){
		if(getExecutor() == null){
			return;
		}
		
		String[] cmdAndArgs = event.getMessage().split(" ", 2);
		cmdAndArgs[0] = cmdAndArgs[0].toLowerCase().trim();
		for(String alias : _aliases){
			if(alias.equals(cmdAndArgs[0])){
				event.setCancelled(getExecutor().onCommand(event.getPlayer(), this, alias, cmdAndArgs.length == 1 ? new String[0] : cmdAndArgs[1].split(" ")));
				break;
			}
		}
	}
	
}
