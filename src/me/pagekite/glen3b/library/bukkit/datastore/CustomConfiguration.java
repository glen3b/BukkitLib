package me.pagekite.glen3b.library.bukkit.datastore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Represents a custom config file.
 * @author Glen Husman
 */
public class CustomConfiguration {

	private Plugin _backend;
	private String _fileName;
	
	private File _file;
	
	private FileConfiguration _cfg;
	
	/**
	 * Gets the configuration instance.
	 * @return An object representing the custom configuration file.
	 */
    public FileConfiguration getConfig() {
        if (_cfg == null) {
            reloadConfig();
        }
        return _cfg;
    }
	
	/**
	 * Reloads the configuration file.
	 */
    public void reloadConfig() {
        if (_file == null) {
        	_file = new File(_backend.getDataFolder(), _fileName);
        }
        _cfg = YamlConfiguration.loadConfiguration(_file);
     
        // Look for defaults in the jar
        InputStream defConfigStream = _backend.getResource("customConfig.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            _cfg.setDefaults(defConfig);
        }
    }
	
    /**
     * Saves the configuration file to disc.
     */
    public void save(){
    	if (_cfg == null || _file == null) {
            return;
        }
        try {
            getConfig().save(_file);
        } catch (IOException ex) {
            _backend.getLogger().log(Level.SEVERE, "Could not save config to " + _fileName, ex);
        }
    }
    
    /**
     * Saves the default configuration to disc.
     * @return Whether the configuration was saved. This method returns {@code true} if the file was saved (because the file did not previously exist), and {@code false} if the file was already present.
     */
    public boolean saveDefaultConfig() {
        if (_file == null) {
            _file = new File(_backend.getDataFolder(), _fileName);
        }
        if (!_file.exists()) {            
             _backend.saveResource(_fileName, true);
             
             return true;
         }
        
        return false;
    }
    
    /**
     * Gets the path of the configuration.
     * @return The path of the configuration file in the data directory of the plugin.
     */
    public File getPath(){
    	if (_file == null) {
            _file = new File(_backend.getDataFolder(), _fileName);
        }
    	
    	return _file;
    }
    
	/**
	 * Creates a new custom configuration instance.
	 * @param backingStore The plugin which contains this configuration.
	 * @param name The filename of the configuration file.
	 */
	public CustomConfiguration(Plugin backingStore, String name){
		Validate.notEmpty(name, "The file name must not be null or empty.");
		Validate.notNull(backingStore, "The plugin is null.");
		
		_backend = backingStore;
		_fileName = name;
	}
	
}
