package me.pagekite.glen3b.library.bukkit.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.pagekite.glen3b.library.bukkit.Utilities;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Utilities involving reflection.
 * 
 * @author Glen Husman
 * @author <a
 *         href="https://forums.bukkit.org/members/microgeek.90705652/">microgeek
 *         </a>
 */
public final class ReflectionUtilities {

	// Reflective instance cache
	private static Map<Class<?>, Method> _getHandleMethods;

	/**
	 * Reset the reflective caches stored internally by this class. This should
	 * generally only be used if reflective operations that are expected to
	 * succeed do not without a call to this method.
	 */
	public static void resetCache() {
		_getHandleMethods = Collections
				.synchronizedMap(new HashMap<Class<?>, Method>());
	}

	static {
		resetCache();
	}

	/**
	 * Sets the value of a field on an {@link Object} instance via reflection.
	 * 
	 * @param instance
	 *            The instance of the class upon which to set the field.
	 * @param fieldName
	 *            The name of the {@link Field} to modify.
	 * @param value
	 *            The value of which the field will be set to.
	 * @throws SecurityException
	 *             If a {@link SecurityManager} blocks this operation.
	 * @throws NoSuchFieldException
	 *             If the specified field does not exist.
	 * @throws IllegalAccessException
	 *             If the field is not accessible for write, such as if it is
	 *             final.
	 * @throws IllegalArgumentException
	 *             If an argument is invalid.
	 * @see Class#getDeclaredField
	 * @see Field#set
	 * @author <a
	 *         href="https://forums.bukkit.org/members/microgeek.90705652/">microgeek
	 *         </a>
	 */
	public static void setValue(Object instance, String fieldName, Object value)
			throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Validate.notNull(instance, "The object instance must not be null.");
		Validate.notEmpty(fieldName, "The field name must be defined.");

		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(instance, value);
	}

	/**
	 * Invoke a method on an object. This will iterate through all methods of
	 * the object to determine the appropriate overload.
	 * <p>
	 * If a specific overload is desired, the following may be used instead to
	 * avoid the overhead of this method.
	 * 
	 * <pre>
	 * 	Method method = object.getClass().getMethod(method, argumentTypes);
	 * 	method.setAccessible(true);
	 * 	method.invoke(object, varArgs);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * Variable arguments methods must be invoked with a parameter of an array,
	 * not as a varargs invocation. That array must also be cast to
	 * {@code Object} so the varargs invocation of this method does not
	 * interpret that array as the argument array passed to this method.
	 * </p>
	 * 
	 * @param object
	 *            The object upon which to invoke the method.
	 * @param method
	 *            The name of the method to invoke.
	 * @param args
	 *            The arguments to pass to the method. If this array is
	 *            {@code null}, it will be treated as an empty array.
	 * @return The result of the invoked method.
	 * @author Glen Husman
	 * @see Class#getMethod(String, Class...)
	 * @see Method#setAccessible(boolean)
	 * @see Method#invoke(Object, Object...)
	 */
	public static Object invokeMethod(Object object, String method,
			Object... args) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Validate.notNull(object, "The object instance must not be null.");
		Validate.notEmpty(method, "The method name must be defined.");

		if (args == null) {
			// Sanity check
			args = new Object[] {};
		}

		try {
			Class<?>[] params = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				params[i] = args[i] == null ? Void.TYPE : args[i].getClass();
			}
			Method m = object.getClass().getMethod(method, params);
			m.setAccessible(true);
			return m.invoke(object, args);
		} catch (NoSuchMethodException er) {
			// TODO: Recursively search for method based on superclass of
			// parameters, or use implementation below?
		}

		List<Method> possibleMethods = Lists.newArrayListWithExpectedSize(2); // Assume
		// 2
		// method
		// overloads
		// as
		// a
		// default,
		// but
		// the
		// list
		// will
		// resize

		for (Method m : object.getClass().getMethods()) {
			if (!m.getName().equals(method)) {
				continue;
			}

			if (m.isVarArgs()
					&& m.getGenericParameterTypes().length <= args.length) {
				possibleMethods.add(m);
			} else if (m.getGenericParameterTypes().length == args.length) {
				Type[] argTypes = m.getGenericParameterTypes();
				boolean isAcceptableOverload = true;
				for (int i = 0; i < args.length; i++) {
					if (argTypes[i] instanceof Class) {
						isAcceptableOverload = ClassUtils.isAssignable(
								(Class<?>) argTypes[i],
								args[i] == null ? Void.TYPE : args[i]
										.getClass());
					} else {
						// TODO: Support more types
						isAcceptableOverload = false;
					}

					if (!isAcceptableOverload) {
						break;
					}
				}

				if (isAcceptableOverload) {
					possibleMethods.add(m);
				}
			}
		}

		for (Method m : possibleMethods) {
			m.setAccessible(true);
			try {
				return m.invoke(object, args);
			} catch (IllegalArgumentException err) {
				// Assume bad method overload, continue
			}
		}

		// No method found that would properly invoke
		throw new NoSuchMethodException(
				"Could not find an appropriate method overload with the specified name. Methods found by loose match: "
						+ Arrays.toString(new Function<List<Method>, String[]>() {

							@Override
							public String[] apply(List<Method> paramF) {
								// Convert method list to method signature
								// String[]
								String[] value = new String[paramF.size()];
								for (int i = 0; i < paramF.size(); i++) {
									value[i] = paramF.get(i).toGenericString();
								}
								return value;
							}

						}.apply(possibleMethods)));

		// Method handleMethod = object.getClass().getMethod(method);
		// handleMethod.setAccessible(true);
		// return handleMethod.invoke(object, args);
	}

	private static Method _bukkitAPIItemStackToNMSStack;
	private static Method _nmsItemStackToCraftbukkitItemStack;
	private static Class<?> _nmsItemStackClass;

	/**
	 * Returns a {@code CraftItemStack} instance representing the specified
	 * instance. If reflective NBT operations are to be performed on this stack,
	 * it is recommended to wrap the constructor in this call. If the
	 * {@code ItemStack} instance is already a {@code CraftItemStack}, that
	 * instance will be returned.
	 * <p>
	 * The return of this method will be {@code null} if any of the following
	 * are true:
	 * <ul>
	 * <li>Reflective utilities are not compatible with this server.</li>
	 * <li>An unexpected error occurs. Due to this behavior, no exceptions
	 * except argument invalidity exceptions will be thrown from this method.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param instance
	 *            The {@link ItemStack} instance to convert.
	 * @return A {@code CraftItemStack} instance, which can hold NBT and NMS
	 *         data.
	 */
	public static ItemStack getCraftStack(ItemStack instance) {
		Validate.notNull(instance, "The ItemStack is null.");

		try {
			if (Utilities.getProtocolUtilityInstance() != null) {
				return Utilities.getProtocolUtilityInstance().assureCraftItemStack(instance);

			} else {
				if (_bukkitAPIItemStackToNMSStack == null) {
					_bukkitAPIItemStackToNMSStack = Class.forName(
							"org.bukkit.craftbukkit."
									+ getPackageVersionString()
									+ ".inventory.CraftItemStack", true,
									Bukkit.getServer().getClass().getClassLoader())
									.getMethod("asNMSCopy", ItemStack.class); // Reasonably
					// hacky
					// :)
				}

				Object nmsItemStack = _bukkitAPIItemStackToNMSStack.invoke(
						null, instance);
				if (_nmsItemStackClass == null) {
					_nmsItemStackClass = nmsItemStack.getClass();
				}

				if (_nmsItemStackToCraftbukkitItemStack == null) {
					_nmsItemStackToCraftbukkitItemStack = Class.forName(
							"org.bukkit.craftbukkit."
									+ getPackageVersionString()
									+ ".inventory.CraftItemStack", true,
									Bukkit.getServer().getClass().getClassLoader())
									.getMethod("asCraftMirror", _nmsItemStackClass);
				}

				// Next, actually build the stack
				return (ItemStack) _nmsItemStackToCraftbukkitItemStack.invoke(
						null, nmsItemStack);
			}
		} catch (Exception except) {
			// Unexpected error
			// For the purposes of reflection, we don't want client code to have
			// to deal with a reflection error
			// The client just needs to know the item can't be converted
			except.printStackTrace();

		}
		return null;
	}

	/**
	 * Gets the NMS handle for the specified bukkit object (NOT the CraftBukkit
	 * class). Should be compatible across versions as it uses reflection. The
	 * {@link Method} instance is cached internally, so repeated calls to this
	 * method with the same parameter type should be relatively efficient.
	 * 
	 * <p>
	 * Certain classes have special behaviors, and are documented below.
	 * <table>
	 * <tr>
	 * <td><b>Class Name</b></td>
	 * <td><b>Output</b></td>
	 * </tr>
	 * <tr>
	 * <td>{@code ItemStack}</td>
	 * <td>A copy of the {@code ItemStack} as a {@code net.minecraft.server.ItemStack}, as retrieved by static methods in the {@code CraftItemStack} implementation class.
	 * </tr>
	 * <tr>
	 * <td>Any CraftBukkit type (with a {@code getHandle} method)</td>
	 * <td>The return value of that type's {@code getHandle} method on that instance.</td>
	 * </tr>
	 * </table>
	 * </p>
	 * 
	 * @param entity
	 *            The bukkit object instance for which to retrieve the handle
	 *            using the {@code getHandle()} method.
	 * @return The NMS handle for the specified CraftBukkit object.
	 * @author Glen Husman
	 * @throws SecurityException
	 *             If a {@link SecurityManager} blocks this operation.
	 * @throws NoSuchMethodException
	 *             If the object does not have an NMS representation retrievable
	 *             via a CraftBukkit method with the signature of
	 *             {@code getHandle()}.
	 * @throws InvocationTargetException
	 *             If the handle retriever method throws an exception.
	 * @throws IllegalArgumentException
	 *             If an argument is invalid.
	 * @throws IllegalAccessException
	 *             If the method cannot be successfully accessed.
	 */
	public static Object getNMSHandle(Object entity)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Validate.notNull(entity, "The object instance must not be null.");

		if (_getHandleMethods.containsKey(entity.getClass())) {
			try {
				Method hMethod = _getHandleMethods.get(entity.getClass());

				return hMethod.invoke(entity);
			} catch (NullPointerException npe) {
				throw (NoSuchMethodException) new NoSuchMethodException(
						"The specified object does not have a getHandle method.")
				.initCause(npe);
			}
		}

		try {
			Method handleMethod = entity.getClass().getMethod("getHandle");
			handleMethod.setAccessible(true);
			_getHandleMethods.put(entity.getClass(), handleMethod);
			return handleMethod.invoke(entity);
		} catch (NoSuchMethodException err) {
			if(entity instanceof ItemStack){
				// A CraftItemStack was retrieved
				// We can assume that the item passed in was a bukkit ItemStack
				// And that craftStack instanceof CraftItemStack
				// Therefore, we should convert to NMS stack and return
				// Using CraftItemStack.asNMSCopy(ItemStack)
				if (_bukkitAPIItemStackToNMSStack == null) {
					try {
						_bukkitAPIItemStackToNMSStack = Class.forName(
								"org.bukkit.craftbukkit."
										+ getPackageVersionString()
										+ ".inventory.CraftItemStack", true,
										Bukkit.getServer().getClass().getClassLoader())
										.getMethod("asNMSCopy", ItemStack.class);
						// Reasonably hacky :)
					} catch (ClassNotFoundException e) {
						// Unexpected reflective error
						// The class should exist
						// However, it should be assumed that this
						// plugin cannot convert NMS stacks, so according
						// to us the method doesn't exist
						// Therefore, we should rethrow the exception
						throw (NoSuchMethodException) new NoSuchMethodException(
								"The specified object does not have a getHandle method.")
						.initCause(e);
					}
				}
				return _bukkitAPIItemStackToNMSStack.invoke(null, entity);
			}
			_getHandleMethods.put(entity.getClass(), null);
			throw (NoSuchMethodException) new NoSuchMethodException(
					"The specified object does not have a getHandle method.")
			.initCause(err);
		}
	}

	/**
	 * Get an array of {@code Method} objects that have the specified name. If a
	 * method object is found, {@code setAccessible(true} will be called upon
	 * it.
	 * 
	 * @param declaredClass
	 *            The {@code Class} which will be searched for methods.
	 * @param name
	 *            The name of the method to retrieve.
	 * @return A non-null array of {@code Method}s which are the member
	 *         overloads of the specified method.
	 */
	public static Method[] getMethodsByName(Class<?> declaredClass, String name) {
		Validate.notNull(declaredClass,
				"The class which contains the methods must not be null.");
		Validate.notEmpty(name, "You must provide a method name.");

		List<Method> retval = Lists.newArrayListWithCapacity(1);

		for (Method m : declaredClass.getMethods()) {
			if (m.getName().trim().equalsIgnoreCase(name.trim())) {
				m.setAccessible(true);
				retval.add(m);
			}
		}

		return retval.toArray(new Method[0]);
	}

	/**
	 * Gets the component of the package name of NMS and OBC classes which
	 * represents the minecraft server version. Determines this value by using
	 * the fully qualified class and package name of the CraftBukkit
	 * implementation class of {@link Server}.
	 * 
	 * @return The version component of the NMS package running on this server.
	 * @author Glen Husman
	 */
	public static String getPackageVersionString() {
		return Bukkit.getServer().getClass().getPackage().getName()
				.replace(ClassUtils.PACKAGE_SEPARATOR, ",").split(",")[3];
	}

	/**
	 * Gets the value of a field on an {@link Object} instance via reflection.
	 * 
	 * @param instance
	 *            The instance of the class which the field will be retrieved
	 *            from.
	 * @param fieldName
	 *            The name of the {@link Field} to retrieve.
	 * @return The value of the field.
	 * @throws SecurityException
	 *             If a {@link SecurityManager} blocks this operation.
	 * @throws NoSuchFieldException
	 *             If the specified field does not exist.
	 * @throws IllegalAccessException
	 *             If the field is not accessible for write, such as if it is
	 *             final.
	 * @throws IllegalArgumentException
	 *             If an argument is invalid.
	 * @see Class#getDeclaredField
	 * @see Field#get
	 * @author <a
	 *         href="https://forums.bukkit.org/members/microgeek.90705652/">microgeek
	 *         </a>
	 */
	public static Object getValue(Object instance, String fieldName)
			throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(instance);
	}

}
