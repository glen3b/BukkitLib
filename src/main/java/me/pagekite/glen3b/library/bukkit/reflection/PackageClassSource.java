package me.pagekite.glen3b.library.bukkit.reflection;

import java.util.Collection;

/** 
 * Represents a {@link Package} which can load and cache classes in an alternate way.
 * @author Glen Husman
 */
public interface PackageClassSource {

	/**
	 * Gets the {@link Class} instance representing the class with the specified name within this package. This method is expected to cache loaded classes.
	 * @param className The case-sensitive name of the class.
	 * @return The class within this package with the specified name.
	 * @exception ClassNotFoundException Thrown when the class with the specified name is not found.
	 */
	public Class<?> getClass(String className) throws ClassNotFoundException;
	
	/**
	 * Gets an unmodifiable set containing all currently cached loaded classes.
	 * @return A set of loaded classes in this package.
	 */
	public Collection<Class<?>> getCachedClasses();
	
	/**
	 * Gets the package that is represented.
	 * @return The package represented by this instance.
	 */
	public Package getPackage();
	
}
