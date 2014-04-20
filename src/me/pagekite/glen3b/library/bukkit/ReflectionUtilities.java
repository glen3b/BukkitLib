package me.pagekite.glen3b.library.bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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
	 * Invoke a method on an object. This will iterate through all methods of the object to determine the appropriate overload.
	 * <p>If a specific overload is desired, the following may be used instead to avoid the overhead of this method.
	 * <pre>
	 * {@code
	 * Method method = object.getClass().getMethod(method, argumentTypes);
	 * method.setAccessible(true);
	 * method.invoke(object, varArgs);
	 * }
	 * </pre>
	 * </p>
	 * 
	 * <p>
	 * Variable arguments methods must be invoked with a parameter of an array, not as a varargs invocation.
	 * That array must also be cast to {@code Object} so the varargs invocation of this method does not interpret that array as the argument array passed to this method.
	 * </p>
	 * @param object The object upon which to invoke the method.
	 * @param method The name of the method to invoke.
	 * @param args The arguments to pass to the method. If this array is {@code null}, it will be treated as an empty array.
	 * @return The result of the invoked method.
	 * @author Glen Husman
	 * @see Class#getMethod(String, Class...)
	 * @see Method#setAccessible(boolean)
	 * @see Method#invoke(Object, Object...)
	 */
	public static Object invokeMethod(Object object, String method, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Validate.notNull(object, "The object instance must not be null.");
		Validate.notEmpty(method, "The method name must be defined.");
		
		if(args == null){
			// Sanity check
			args = new Object[]{};
		}
		
		try{
			Class<?>[] params = new Class[args.length];
			for(int i = 0; i < args.length; i++){
				params[i] = args[i] == null ? Void.TYPE : args[i].getClass();
			}
			Method m = object.getClass().getMethod(method, params);
			m.setAccessible(true);
			return m.invoke(object, args);
		}catch(NoSuchMethodException er){
			// TODO: Recursively search for method based on superclass of parameters, or use implementation below?
		}
		
		List<Method> possibleMethods = Lists.newArrayListWithExpectedSize(2); // Assume 2 method overloads as a default, but the list will resize

		for(Method m : object.getClass().getMethods()){
			if(!m.getName().equals(method)){
				continue;
			}
			
			if(m.isVarArgs() && m.getGenericParameterTypes().length <= args.length){
				possibleMethods.add(m);
			}else if(m.getGenericParameterTypes().length == args.length){
				Type[] argTypes = m.getGenericParameterTypes();
				boolean isAcceptableOverload = true;
				for(int i = 0; i < args.length; i++){
					if(argTypes[i] instanceof Class){
						isAcceptableOverload = ClassUtils.isAssignable((Class<?>)argTypes[i], args[i] == null ? Void.TYPE : args[i].getClass());
					}else{
						// TODO: Support more types
						isAcceptableOverload = false;
					}

					if(!isAcceptableOverload){
						break;
					}
				}

				if(isAcceptableOverload){
					possibleMethods.add(m);
				}
			}
		}

		for (Method m : possibleMethods){
			m.setAccessible(true);
			try{
				return m.invoke(object, args);
			}catch(IllegalArgumentException err){
				// Assume bad method overload, continue
			}
		}

		// No method found that would properly invoke
		throw new NoSuchMethodException("Could not find an appropriate method overload with the specified name. Methods found by loose match: " +
				Arrays.toString(new Function<List<Method>, String[]>(){

					@Override
					public String[] apply(List<Method> paramF) {
						// Convert method list to method signature String[]
						String[] value = new String[paramF.size()];
						for(int i = 0; i < paramF.size(); i++){
							value[i] = paramF.get(i).toGenericString();
						}
						return value;
					}

				}.apply(possibleMethods)));

		//Method handleMethod = object.getClass().getMethod(method);
		//handleMethod.setAccessible(true);
		//return handleMethod.invoke(object, args);
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
		Validate.notNull(entity, "The object instance must not be null.");

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
		return Bukkit.getServer().getClass().getPackage().getName().replace(ClassUtils.PACKAGE_SEPARATOR, ",").split(",")[3];
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
