package me.pagekite.glen3b.gbukkitlib;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * A service allowing plugins to register configuration files to automatically save.
 * @author Glen Husman
 */
public final class AutoSaverScheduler {

	private Plugin _plugin;
	
	/**
	 * Internal constructor for service registration.
	 * @param plugin The plugin to use to register autosave tasks.
	 */
	AutoSaverScheduler(Plugin plugin){
		_plugin = plugin;
	}
	
	private final class AutosavedConfig implements Runnable{
		public AutosavedConfig(File file, FileConfiguration cfg){
			_file = file;
			_config = cfg;
		}
		
		public File _file;
		public FileConfiguration _config;
		@Override
		public void run() {
			try {
				_config.save(_file);
				_plugin.getLogger().log(Level.FINE, "Autosaving configuration to " + _file.getName() + " in folder " + _file.getParent());
			} catch (IOException e) {
				_plugin.getLogger().log(Level.WARNING, "Autosaving configuration to " + _file.getName() + " in folder " + _file.getParent() + " FAILED!", e);
			}
		}
	}
	
	/**
	 * Gets the configuration path for the file name fileName within a plugin's data directory.
	 * @param plugin The plugin who's configuration file's path should be retrieved.
	 * @return A @{link File} instance which represents fileName in the plugin.
	 */
	public File getConfigurationPath(Plugin plugin, String fileName){
		if(plugin == null){
			throw new IllegalArgumentException("The plugin must not be null.");
		}
		
		if(fileName == null){
			throw new IllegalArgumentException("The file name must not be null.");
		}
		
		return new File(plugin.getDataFolder(), fileName);
	}
	
	/**
	 * Gets the configuration path for the file name "config.yml" within a plugin's data directory.
	 * @param plugin The plugin who's configuration file's path should be retrieved.
	 * @return A @{link File} instance which represents config.yml in the plugin. The name config.yml is hardcoded.
	 */
	public File getConfigurationPath(Plugin plugin){
		return getConfigurationPath(plugin, "config.yml");
	}
	
	/**
	 * Register a configuration file to automatically save.
	 * @param path The path of the configuration to save.
	 * @param config The configuration to save to the specified file.
	 * @param saveInterval The interval, in server ticks, between autosaves.
	 * @see {@link getConfigurationPath}
	 */
	public void registerAutosave(File path, FileConfiguration config, long saveInterval){
		if(path == null){
			throw new IllegalArgumentException("The configuration path cannot be null.");
		}
		if(config == null){
			throw new IllegalArgumentException("The configuration object cannot be null.");
		}
		if(saveInterval <= 0){
			throw new IllegalArgumentException("The save interval must be at least one tick.");
		}
		
		//Schedule the task
		_plugin.getServer().getScheduler().runTaskTimer(_plugin, new AutosavedConfig(path, config), saveInterval, saveInterval);
	}
	
}
