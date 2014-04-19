package me.pagekite.glen3b.library.bukkit;

import java.lang.reflect.Field;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

/**
 * Utilities involving reflection.
 * @author Glen Husman
 * @author <a href="https://forums.bukkit.org/members/microgeek.90705652/">microgeek</a>
 */
public final class ReflectionUtilities {

	/**
	 * Sets the value of a field on an {@link Object} instance via reflection.
	 * @param instance The instance of the class upon which to set the field.
	 * @param fieldName The name of the {@link Field} to modify.
	 * @param value The value of which the field will be set to.
	 * @throws SecurityException If a {@link SecurityManager} blocks this operation.
	 * @throws NoSuchFieldException If the specified field does not exist.
	 * @throws IllegalAccessException If the field is not accessible for write, such as if it is final.
	 * @throws IllegalArgumentException If an argument is invalid.
	 * @see Class#getDeclaredField
	 * @see Field#set
	 * @author <a href="https://forums.bukkit.org/members/microgeek.90705652/">microgeek</a>
	 */
	public static void setValue(Object instance, String fieldName, Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Validate.notNull(instance, "The object instance must not be null.");
		Validate.notEmpty(fieldName, "The field name must be defined.");

		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(instance, value);
	}

	/**
	 * Gets the component of the package name of NMS and OBC classes which represents the minecraft server version.
	 * Determines this value by using the fully qualified class and package name of the CraftBukkit implementation class of {@link Server}.
	 * @return The version component of the NMS package running on this server.
	 * @author Glen Husman
	 */
	public static String getPackageVersionString() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}

	/**
	 * Gets the value of a field on an {@link Object} instance via reflection.
	 * @param instance The instance of the class which the field will be retrieved from.
	 * @param fieldName The name of the {@link Field} to retrieve.
	 * @return The value of the field.
	 * @throws SecurityException If a {@link SecurityManager} blocks this operation.
	 * @throws NoSuchFieldException If the specified field does not exist.
	 * @throws IllegalAccessException If the field is not accessible for write, such as if it is final.
	 * @throws IllegalArgumentException If an argument is invalid.
	 * @see Class#getDeclaredField
	 * @see Field#get
	 * @author <a href="https://forums.bukkit.org/members/microgeek.90705652/">microgeek</a>
	 */
	public static Object getValue(Object instance, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(instance);
	}

}
