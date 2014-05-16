package me.pagekite.glen3b.library.bukkit.reflection;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import com.google.common.collect.Maps;

/**
 * Represents an internal minecraft package.
 * @author Glen Husman
 */
public enum InternalPackage implements PackageClassSource{

	/**
	 * Represents the {@code net.minecraft.server} package, also known as NMS. This package contains vanilla minecraft server implementation classes.
	 */
	NET_MINECRAFT_SERVER(Package.getPackage("net.minecraft.server." + ReflectionUtilities.getPackageVersionString())),
	/**
	 * Represents the {@code org.bukkit.craftbukkit} package, also known as OBC. This package contains CraftBukkit implementation classes of Bukkit interfaces and abstract classes.
	 */
	ORG_BUKKIT_CRAFTBUKKIT(Bukkit.getServer().getClass().getPackage());
	
	private Package _package;
	
	private InternalPackage(Package pkg){
		_package = pkg;
	}
	
	/**
	 * Get the {@link Package} represented by this instance.
	 * @return The internally represented minecraft package.
	 */
	public Package getPackage(){
		return _package;
	}
	
	@Override
	public String toString(){
		return _package.getName();
	}
	
	@Override
	public Class<?> getClass(String className) throws ClassNotFoundException {
		Validate.notEmpty(className, "The class name must be specified.");
		
		String cName = className.trim();
		String fqcName = getPackage().getName() + ClassUtils.PACKAGE_SEPARATOR + cName;
		
		Class<?> retVal = null;
		Exception errCause = null;
		
		if(loadedClasses.containsKey(cName)){
			retVal = loadedClasses.get(cName);
			
			if(retVal == null){
				errCause = new NullPointerException("The cached Class instance representing " + fqcName + " is null.");
			}
		}else{
			try{
				retVal = Class.forName(fqcName);
			}catch(ClassNotFoundException except){
				// Will rethrow later
				errCause = except;
				retVal = null;
			}

			loadedClasses.put(cName, retVal);
		}
		
		if(retVal == null){
			throw new ClassNotFoundException(fqcName + " does not exist.", errCause);
		}
		
		return retVal;
	}

	Map<String, Class<?>> loadedClasses = Maps.newHashMap(); // Package-private to allow for resetCache to work
	private Collection<Class<?>> _cachedClassView = Collections.unmodifiableCollection(loadedClasses.values());
	
	@Override
	public Collection<Class<?>> getCachedClasses() {
		return _cachedClassView;
	}
	
	/**
	 * Represents a sub-package of {@link InternalPackage#ORG_BUKKIT_CRAFTBUKKIT org.bukkit.craftbukkit} which further divides that package into categories.
	 * @author Glen Husman
	 */
	public static enum SubPackage implements PackageClassSource{

		/**
		 * Represents the subpackage containing block-related classes.
		 */
		BLOCK("block"),
		/**
		 * Represents the subpackage containing classes related to reading and writing chunks to and from disk.
		 */
		CHUNK_INPUT_OUTPUT("chunkio"),
		/**
		 * Represents the subpackage containing classes related to commands.
		 */
		COMMAND("command"),
		/**
		 * Represents the subpackage containing classes related to conversation management.
		 */
		CONVERSATIONS("conversations"),
		/**
		 * Represents the subpackage containing enchantment classes.
		 */
		ENCHANTMENTS("enchantments"),
		/**
		 * Represents the subpackage containing entity-related classes.
		 */
		ENTITY("entity"),
		/**
		 * Represents the subpackage containing event-related classes.
		 */
		EVENT("event"),
		/**
		 * Represents the subpackage containing classes related to world generation.
		 */
		WORLD_GENERATOR("generator"),
		/**
		 * Represents the subpackage containing classes related to the generation of command help menus.
		 */
		HELP("help"),
		/**
		 * Represents the subpackage containing inventory-related classes.
		 */
		INVENTORY("inventory"),
		/**
		 * Represents the subpackage containing classes related to in-game maps.
		 */
		MAP("map"),
		/**
		 * Represents the subpackage containing classes related to Bukkit metadata.
		 */
		METADATA("metadata"),
		/**
		 * Represents the subpackage containing classes related to potions.
		 */
		POTION("potion"),
		/**
		 * Represents the subpackage containing classes related to projectiles.
		 */
		PROJECTILES("projectiles"),
		/**
		 * Represents the subpackage containing classes related to the Bukkit scheduler.
		 */
		SCHEDULER("scheduler"),
		/**
		 * Represents the subpackage containing classes related to scoreboards.
		 */
		SCOREBOARD("scoreboard"),
		/**
		 * Represents the subpackage containing classes which are used to automatically notify server administrators about Bukkit updates.
		 */
		UPDATER("updater"),
		/**
		 * Represents the subpackage containing utility classes written by and included with CraftBukkit.
		 */
		UTILITY("util");
		
		private Package _package;
		private String _name;
		
		/**
		 * Get the {@link Package} represented by this instance.
		 * @return The internally represented minecraft package.
		 */
		public Package getPackage(){
			return _package;
		}
		
		private SubPackage(String name){
			_name = name;
			_package = Package.getPackage(InternalPackage.ORG_BUKKIT_CRAFTBUKKIT + (name == null ? "" : "." + name));
		}
		
		/**
		 * Gets the name of this subpackage.
		 * @return The name of this subpackage (which is {@code null} for {@link SubPackage#MAIN MAIN}).
		 */
		public String getSubpackageName(){
			return _name;
		}
		
		private static Map<String, SubPackage> BY_NAME;
		
		static {
			SubPackage[] vals = values();
			
			BY_NAME = Maps.newHashMapWithExpectedSize(vals.length);
			
	        for (SubPackage subpkg : vals) {
	            
	            BY_NAME.put(subpkg.getSubpackageName(), subpkg);
	        }
	    }
		
		/**
		 * Gets the subpackage with the specified name. The name is trimmed by this method.
		 * @param name The name of the subpackage.
		 * @return The subpackage with the specified name, or {@code null} if not found.
		 */
		public static SubPackage getBySubpackageName(String name){
			return BY_NAME.get(name == null ? null : name.toLowerCase().trim());
		}
		
		@Override
		public String toString(){
			return _package.getName();
		}
		
		@Override
		public Class<?> getClass(String className) throws ClassNotFoundException {
			Validate.notEmpty(className, "The class name must be specified.");
			
			String cName = className.trim();
			String fqcName = getPackage().getName() + ClassUtils.PACKAGE_SEPARATOR + cName;
			
			Class<?> retVal = null;
			Exception errCause = null;
			
			if(loadedClasses.containsKey(cName)){
				retVal = loadedClasses.get(cName);
				
				if(retVal == null){
					errCause = new NullPointerException("The cached Class instance representing " + fqcName + " is null.");
				}
			}else{
				try{
					retVal = Class.forName(fqcName);
				}catch(ClassNotFoundException except){
					// Will rethrow later
					errCause = except;
					retVal = null;
				}

				loadedClasses.put(cName, retVal);
			}
			
			if(retVal == null){
				throw new ClassNotFoundException(fqcName + " does not exist.", errCause);
			}
			
			return retVal;
		}

		Map<String, Class<?>> loadedClasses = Maps.newHashMap(); // Package-private to allow for resetCache to work
		private Collection<Class<?>> _cachedClassView = Collections.unmodifiableCollection(loadedClasses.values());
		
		@Override
		public Collection<Class<?>> getCachedClasses() {
			return _cachedClassView;
		}
	}
}
