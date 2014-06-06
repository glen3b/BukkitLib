package me.pagekite.glen3b.library.bukkit.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import me.pagekite.glen3b.library.bukkit.Utilities;
import me.pagekite.glen3b.library.bukkit.reflection.InternalPackage.SubPackage;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
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
 * @see Class
 * @see Method
 * @see Field
 * @see Constructor
 */
public final class ReflectionUtilities {

	/**
	 * Reset the reflective caches stored internally by this class. This should
	 * generally only be used if reflective operations that are expected to
	 * succeed do not without a call to this method.
	 */
	static void resetCache() {
		CraftBukkit._getHandleMethods = Collections
				.synchronizedMap(new HashMap<Class<?>, Method>());
		_fieldCache = Collections
				.synchronizedMap(new HashMap<Class<?>, Map<String, Field>>());

		for(InternalPackage pkg : InternalPackage.values()){
			pkg.loadedClasses.clear();
		}

		for(SubPackage pkg : SubPackage.values()){
			pkg.loadedClasses.clear();
		}

		_obcPkgVerStr = null;
	}

	/**
	 * Creates an instance of the specified class.
	 * @param clazz The {@code Class} for which to create an instance.
	 * @param args The arguments to pass to the constructor.
	 * @return An instance of the specified type created by the closest matching constructor.
	 * @throws SecurityException If a {@link SecurityManager} blocks this operation.
	 * @throws NoSuchMethodException If a constructor with the specified criteria does not exist.
	 * @throws InvocationTargetException If an exception occurs during the invocation of the constructor.
	 * @throws IllegalArgumentException If the incorrect number or type of arguments were specified.
	 * @throws IllegalAccessException If the constructor cannot be accessed. Due to the call to {@link java.lang.reflect.Constructor#setAccessible(boolean) setAccessible}, this exception should never be thrown.
	 * @throws InstantiationException If the specified type cannot be created, such as if it is an abstract class.
	 * @see Constructor#newInstance(Object...)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createInstance(Class<T> clazz, Object... args) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Validate.notNull(clazz, "The reflected type must be defined.");
		if(args == null){
			// Interpret as empty
			args = new Object[0];
		}

		Class<?>[] argTypes = new Class<?>[args.length];

		for(int i = 0; i < argTypes.length; i++){
			argTypes[i] = args[i] == null ? Object.class : args[i].getClass();
		}

		return (T) getConstructor(clazz, argTypes).newInstance(args);
	}

	/**
	 * Gets a constructor, which may be inaccessible to public viewers due to access modifiers, for the specified class.
	 * @param clazz The {@code Class} for which to obtain the constructor.
	 * @param cArgs The types of the arguments to pass to the constructor.
	 * @return The constructor instance with the specified criteria.
	 * @throws SecurityException If a {@link SecurityManager} blocks this operation.
	 * @throws NoSuchMethodException If a constructor with the specified criteria does not exist.
	 */
	public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... cArgs) throws NoSuchMethodException, SecurityException{
		Validate.notNull(clazz, "The reflected type must be defined.");
		if(cArgs == null){
			// Interpret as empty
			cArgs = new Class<?>[0];
		}

		Constructor<?> ctor = clazz.getDeclaredConstructor(cArgs);
		ctor.setAccessible(true);
		return ctor;
	}

	/**
	 * Assumes caller synchronizes on the fieldCache properly.
	 * @param clazz The class.
	 * @param field The field to get.
	 * @return The field value.
	 * @throws NoSuchFieldException If the field doesn't exist.
	 */
	private static Field getCachedFieldInstance(Class<?> clazz, String field) throws NoSuchFieldException{
		if(!_fieldCache.containsKey(clazz)){
			_fieldCache.put(clazz, new HashMap<String, Field>());
		}
		Map<String, Field> declaredFields = _fieldCache.get(clazz); // returns a REFERENCE
		if(declaredFields.containsKey(field)){
			Field val = declaredFields.get(field);
			if(val == null){
				throw new NoSuchFieldException(clazz.getCanonicalName() + " does not declare a reflectively accessible field by the name of '" + field + "'.");
			}
			return val;
		}

		Field value = null;
		Throwable cause = null;

		// Caching is my excuse to use an extremely expensive search operation
		// Hopefully barely any of this will have to be called
		try{
			value = clazz.getField(field);
		}catch(NoSuchFieldException err){
			try{
				value = clazz.getDeclaredField(field);
			}catch(NoSuchFieldException innerErr){
				try{
					cause = innerErr.initCause(err);
				}catch(IllegalStateException except){
					cause = innerErr;
				}
				// value should still be null
				for(Class<?> superclass = clazz; superclass != null; superclass = superclass.getSuperclass()){
					for(Field f : superclass.getDeclaredFields()){
						if(f.getName().equals(field)){
							value = f;
							break;
						}
					}
					if(value != null){
						// We found it!
						break;
					}
				}
			}
		}

		declaredFields.put(field, value);

		if(value == null){
			throw (NoSuchFieldException) new NoSuchFieldException(clazz.getCanonicalName() + " does not declare a reflectively accessible field by the name of '" + field + "'.").initCause(cause);
		}else{
			// Very important!
			value.setAccessible(true);
		}

		return value;
	}

	private static Map<Class<?>, Map<String, Field>> _fieldCache;

	static {
		resetCache();
	}

	/**
	 * Reflection involving Minecraft (NMS) classes and code.
	 * @author Glen Husman
	 */
	public static final class Minecraft{

		private Minecraft(){}

		/**
		 * Gets the NMS class with the specified name.
		 * @param name The name of the class in the {@code net.minecraft.server} package.
		 * @return The class instance.
		 * @throws ClassNotFoundException If the class with the specified name is not found.
		 * @see InternalPackage#getClass(String)
		 */
		public static Class<?> getType(String name) throws ClassNotFoundException{
			return InternalPackage.MINECRAFT_SERVER.getClass(name);
		}
	}

	/**
	 * Reflection involving CraftBukkit (OBC) classes and code.
	 * @author Glen Husman
	 */
	public static final class CraftBukkit{

		private static Method _bukkitAPIItemStackToNMSStack;

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
		 * <li>An unexpected error occurs.</li>
		 * </ul>
		 * Due to this behavior, no exceptions except argument invalidity
		 * exceptions will be thrown from this method.
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
				// Default util implementation ensures that this will work
				return Utilities.getProtocolUtilityInstance().assureCraftItemStack(instance);

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
		 * Sets the number of ticks that an item entity has lived. This call uses reflection to access NMS, as the exposed API methods do not allow changing of this value.
		 * @param entity The item entity for which to set the age.
		 * @param age The new age, in ticks, of the item.
		 * @throws InvocationTargetException If an error occurs while reflectively obtaining the NMS handle.
		 * @throws IllegalAccessException If a security error occurs while reflectively obtaining the NMS handle.
		 * @throws NoSuchMethodException If an error occurs while reflectively obtaining the NMS handle.
		 * @throws IllegalArgumentException If an illegal argument is specified to this method.
		 * @throws SecurityException If a security error occurs while reflectively obtaining the NMS handle.
		 * @throws NoSuchFieldException If an error occurs while reflectively setting the age of the NMS item entity such that the appropriate field is not found.
		 */
		public static void setItemAge(Item entity, int age) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException{
			Validate.isTrue(entity != null && entity.isValid() && !entity.isDead() && entity.getWorld() != null, "The specified object does not represent a valid item entity.");
			Object nmsItem = getNMSHandle(entity);
			setValue(nmsItem, "age", age);
		}
		
		private CraftBukkit(){}

		/**
		 * Gets the OBC class with the specified name.
		 * @param name The name of the class in the specified subpackage.
		 * @param src The subpackage containing the class.
		 * @return The class instance.
		 * @throws ClassNotFoundException If the class with the specified name is not found.
		 * @see SubPackage#getClass(String)
		 */
		public static Class<?> getType(SubPackage src, String name) throws ClassNotFoundException{
			return src == null ? getType(name) : src.getClass(name);
		}

		private static boolean _hasCachedCraftPlayer = false;
		private static Class<? extends Player> _cachedCraftPlayerType = null;
		private static Method _nmsPlayer_playerConnection_sendPacketMethod = null;

		/**
		 * Sends an NMS packet instance to a specific player.
		 * @param player The player who will receive the packet.
		 * @param packet The NMS packet object.
		 * @exception IllegalArgumentException If player is {@code null}.
		 * @exception IllegalArgumentException If packet is {@code null}.
		 * @exception IllegalArgumentException If packet is not an instance of the NMS packet type.
		 */
		public static void sendPacket(Player player, Object packet){
			Validate.notNull(player, "The player must not be null.");
			Validate.notNull(packet, "The packet instance must not be null.");
			Class<?> packetType = null;
			try {
				packetType = Minecraft.getType("Packet");
			} catch (Exception e) {
				throw new RuntimeException("An error occurred during the process of reflectively sending the specified packet to the specified player.", e);
			}
			
			Validate.isTrue(packetType.isInstance(packet), "The packet instance must be assignable to the NMS packet type.");
			
			try {
				// EntityPlayer instance
				Object nmsPlayer = getNMSHandle(player);
				// PlayerConnection for this player
				Object networkingHandle = getValue(nmsPlayer, "playerConnection");
				if(_nmsPlayer_playerConnection_sendPacketMethod == null){
					_nmsPlayer_playerConnection_sendPacketMethod = networkingHandle.getClass().getDeclaredMethod("sendPacket", packetType);
					_nmsPlayer_playerConnection_sendPacketMethod.setAccessible(true);
				}
				// Send the packet to the player in question
				_nmsPlayer_playerConnection_sendPacketMethod.invoke(networkingHandle, packet);
			} catch (Exception e) {
				throw new RuntimeException("An error occurred during the process of reflectively sending the specified packet to the specified player.", e);
			}
		}
		
		/**
		 * Gets the {@link Class} instance representing the {@code org.bukkit.craftbukkit.entity.CraftPlayer} implementation class.
		 * @return The cached {@code org.bukkit.craftbukkit.entity.CraftPlayer} {@link Class} instance, or {@code null} if it could not be found.
		 */
		public static synchronized Class<? extends org.bukkit.entity.Player> getCraftPlayerType(){
			/*
			 * We cache the Class instance separately due to the asSubclass call.
			 * The SubPackages will cache it, but they do not cache the Bukkit interfaces explicitly.
			 */
			
			if(!_hasCachedCraftPlayer){
				Class<?> cachedPl = null;
				try{
					cachedPl = getType(SubPackage.ENTITY, "CraftPlayer");
				}catch(ClassNotFoundException except){
					cachedPl = null;
					except.printStackTrace();
				}

				_cachedCraftPlayerType = cachedPl == null ? null : cachedPl.asSubclass(org.bukkit.entity.Player.class);

				_hasCachedCraftPlayer = true;
			}

			return _cachedCraftPlayerType;
		}

		/**
		 * Gets the OBC class with the specified name.
		 * @param name The name of the class in the {@code prg.bukkit.craftbukkit} package.
		 * @return The class instance.
		 * @throws ClassNotFoundException If the class with the specified name is not found.
		 * @see InternalPackage#getClass(String)
		 */
		public static Class<?> getType(String name) throws ClassNotFoundException{
			return InternalPackage.CRAFTBUKKIT.getClass(name);
		}

		private static Map<Class<?>, Method> _getHandleMethods;

		/**
		 * Gets the NMS handle for the specified CraftBukkit object (this method does
		 * NOT return the CraftBukkit class). Should be compatible across versions as
		 * it uses reflection. The {@link Method} instance is cached internally,
		 * so repeated calls to this method with the same parameter type should be relatively efficient.
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
		 * <td>{@code Item}</td>
		 * <td>The underlying {@code EntityItem} instance, as known by the specialized {@code item} field in the {@code CraftItem} entity implementation class.
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
			
			if(entity instanceof Item){
				// IMPORTANT: Do this BEFORE attempting the getHandle call but AFTER reading caches
				// Use the specialized "item" field instance
				try {
					return getValue(entity, "item");
				} catch (NoSuchFieldException e) {
					// Ignore exception, let the instance fall through to the getHandle fallback check
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
							_bukkitAPIItemStackToNMSStack = ReflectionUtilities.CraftBukkit.getType(SubPackage.INVENTORY, "CraftItemStack")
									.getMethod("asNMSCopy", ItemStack.class);
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
	}

	/**
	 * Get the {@code Class} instance representing the primitive type of the specified wrapper class.
	 * @param wrapper The wrapper type for which to find the primitive type.
	 * @return The primitive type for the specified wrapper.
	 * @see PrimitiveType
	 * @exception ClassNotFoundException Thrown if a matching primitive class cannot be found for the specified wrapper type.
	 */
	public static Class<?> getPrimitiveForWrapper(Class<?> wrapper) throws ClassNotFoundException{
		if(PrimitiveType.PRIMITIVE_TYPE_MAP.inverse().containsKey(wrapper)){
			return PrimitiveType.PRIMITIVE_TYPE_MAP.inverse().get(wrapper);
		}

		throw new ClassNotFoundException("The specified type (" + wrapper.getCanonicalName() + ") is not a supported primitive wrapper type.");
	}

	/**
	 * Get the {@code Class} instance representing the wrapper type of the specified primitive keyword.
	 * @param primitive The primitive type for which to find the wrapper type.
	 * @return The wrapper type for the specified primitive.
	 * @see PrimitiveType
	 * @exception ClassNotFoundException Thrown if a matching wrapper class cannot be found for the specified primitive type.
	 */
	public static Class<?> getWrapperForPrimitive(Class<?> primitive) throws ClassNotFoundException{
		if(PrimitiveType.PRIMITIVE_TYPE_MAP.containsKey(primitive)){
			return PrimitiveType.PRIMITIVE_TYPE_MAP.get(primitive);
		}

		throw new ClassNotFoundException("The specified type (" + primitive.getCanonicalName() + ") is not a supported primitive type.");
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
	 */
	public static void setValue(Object instance, String fieldName, Object value)
			throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Validate.notNull(instance, "The specified object must not be null.");
		Validate.notEmpty(fieldName, "The field name must be specified.");

		if(instance.getClass().isArray() && fieldName.equals("length")){
			// Throw custom error on attempting to reflect length fields
			throw new IllegalAccessException("It is impossible to reflectively set the 'length' field of an array class.");
		}

		Field field = getCachedFieldInstance(instance.getClass(), fieldName);
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
	 */
	public static synchronized String getPackageVersionString() {
		if(_obcPkgVerStr == null){
			_obcPkgVerStr = Bukkit.getServer().getClass().getPackage().getName()
					.split(Pattern.quote(ClassUtils.PACKAGE_SEPARATOR))[3];
		}
		return _obcPkgVerStr;
	}

	private static String _obcPkgVerStr = null;

	/**
	 * Gets a {@link Field} declared by a subtype of {@link Object} instance via reflection.
	 * 
	 * @param clazz
	 *            The type containing the field.
	 * @param fieldName
	 *            The name of the {@link Field} to retrieve.
	 * @return The object representing the reflectively obtained field.
	 * @throws SecurityException
	 *             If a {@link SecurityManager} blocks this operation.
	 * @throws NoSuchFieldException
	 *             If the specified field does not exist.
	 * @see Class#getDeclaredField
	 * @see Field
	 */
	public static Field getField(Class<?> clazz, String fieldName)
			throws NoSuchFieldException, SecurityException {
		Validate.notNull(clazz, "The specified class must not be null.");
		Validate.notEmpty(fieldName, "The field name must be specified.");


		return getCachedFieldInstance(clazz, fieldName);
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
	 */
	public static Object getValue(Object instance, String fieldName)
			throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Validate.notNull(instance, "The specified object must not be null.");
		Validate.notEmpty(fieldName, "The field name must be specified.");

		if(instance.getClass().isArray() && fieldName.equals("length")){
			// Reflect length fields
			return Array.getLength(instance);
		}

		Field field = getCachedFieldInstance(instance.getClass(), fieldName);
		return field.get(instance);
	}

}
