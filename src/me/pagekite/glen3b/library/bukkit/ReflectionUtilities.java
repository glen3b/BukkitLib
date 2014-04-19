package me.pagekite.glen3b.library.bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Server;

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
	 * Gets the NMS handle for the specified bukkit object (NOT the CraftBukkit class). Should be compatible across versions as it uses reflection.
	 * @param entity The bukkit object instance for which to retrieve the handle using the {@code getHandle()} method.
	 * @return The NMS handle for the specified CraftBukkit object.
	 * @author Glen Husman
	 * @throws SecurityException If a {@link SecurityManager} blocks this operation.
	 * @throws NoSuchMethodException If the object does not have an NMS representation retrievable via a CraftBukkit method with the signature of {@code getHandle()}.
	 * @throws InvocationTargetException If the handle retriever method throws an exception.
	 * @throws IllegalArgumentException If an argument is invalid.
	 * @throws IllegalAccessException If the method cannot be successfully accessed.
	 */
	public static Object getNMSHandle(Object entity) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Validate.notNull(entity, "The entity must not be null.");
		
		Method handleMethod = entity.getClass().getMethod("getHandle");
		handleMethod.setAccessible(true);
		return handleMethod.invoke(entity);
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
