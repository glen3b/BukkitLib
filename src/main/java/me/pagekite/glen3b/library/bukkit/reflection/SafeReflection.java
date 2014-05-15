package me.pagekite.glen3b.library.bukkit.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang.Validate;


/**
 * Provides wrapper utility methods intended to be able to do reflection without throwing exceptions.
 * Unless otherwise noted, methods in this class will, in the event of catching an exception, print the stack trace of that exception and return {@code null}.
 * @author Glen Husman
 */
public final class SafeReflection {

	/**
	 * Attempts to return the value of the specified static field.
	 * @param clazz The type for which to retrieve the field value.
	 * @param fieldName The name of the field.
	 * @return The field value (which may be {@code null}), or {@code null} if an error occurs.
	 */
	public static Object getFieldValue(Class<?> clazz, String fieldName){
		return getFieldValue(clazz, null, fieldName, true);
	}

	/**
	 * Attempts to return the value of the specified field.
	 * @param instance The instance of the specified object which contains the field value.
	 * @param fieldName The name of the field.
	 * @return The field value (which may be {@code null}), or {@code null} if an error occurs.
	 */
	public static Object getFieldValue(Object instance, String fieldName){
		if(instance == null){
			return null;
		}

		return getFieldValue(instance.getClass(), instance, fieldName, true);
	}

	/**
	 * Attempts to return the value of the specified field.
	 * @param clazz The type for which to retrieve the field value.
	 * @param instance The instance of the specified object which contains the field value.
	 * @param fieldName The name of the field.
	 * @return The field value (which may be {@code null}), or {@code null} if an error occurs.
	 */
	public static Object getFieldValue(Class<?> clazz, Object instance, String fieldName){
		return getFieldValue(clazz, instance, fieldName, true);
	}



	/**
	 * Attempts to return the value of the specified field.
	 * @param clazz The type for which to retrieve the field value.
	 * @param instance The instance of the specified object which contains the field value.
	 * @param fieldName The name of the field.
	 * @param setAccessible Whether the field is not visible (as per a <i>Java</i> access modifier).
	 * @return The field value (which may be {@code null}), or {@code null} if an error occurs.
	 */
	public static Object getFieldValue(Class<?> clazz, Object instance, String fieldName, boolean setAccessible){
		if(fieldName == null){
			return null;
		}

		if(clazz == null){
			return null;
		}

		try {
			Field f;
			try{
				f = clazz.getField(fieldName);
			}catch(NoSuchFieldException ex){
				f = clazz.getDeclaredField(fieldName);
			}
			if(setAccessible){
				f.setAccessible(true);
			}
			return f.get(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Attempts to set the value of the specified static.
	 * @param clazz The type for which to set the field value.
	 * @param fieldName The name of the field.
	 * @param val The new value of the field.
	 * @return {@code true} if and only if the operation succeeds without errors.
	 */
	public static boolean setFieldValue(Class<?> clazz, String fieldName, Object val){
		if(clazz == null){
			return false;
		}

		return setFieldValue(clazz, null, fieldName, true, val);
	}

	/**
	 * Attempts to set the value of the specified field.
	 * @param instance The instance of the specified object which contains the field value.
	 * @param fieldName The name of the field.
	 * @param val The new value of the field.
	 * @return {@code true} if and only if the operation succeeds without errors.
	 */
	public static boolean setFieldValue(Object instance, String fieldName, Object val){
		if(instance == null){
			return false;
		}

		return setFieldValue(instance.getClass(), instance, fieldName, true, val);
	}

	/**
	 * Attempts to set the value of the specified field.
	 * @param clazz The type for which to set the field value.
	 * @param instance The instance of the specified object which contains the field value.
	 * @param fieldName The name of the field.
	 * @param val The new value of the field.
	 * @return {@code true} if and only if the operation succeeds without errors.
	 */
	public static boolean setFieldValue(Class<?> clazz, Object instance, String fieldName, Object val){
		return setFieldValue(clazz, instance, fieldName, true, val);
	}

	/**
	 * Attempts to set the value of the specified field.
	 * @param clazz The type for which to set the field value.
	 * @param instance The instance of the specified object which contains the field value.
	 * @param fieldName The name of the field.
	 * @param setAccessible Whether the field is not visible (as per a <i>Java</i> access modifier).
	 * @param val The new value of the field.
	 * @return {@code true} if and only if the operation succeeds without errors.
	 */
	public static boolean setFieldValue(Class<?> clazz, Object instance, String fieldName, boolean setAccessible, Object val){
		if(fieldName == null){
			return false;
		}

		if(clazz == null){
			return false;
		}

		try {
			Field f;
			f = clazz.getDeclaredField(fieldName);

			if(setAccessible){
				f.setAccessible(true);
			}
			f.set(instance, val);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Attempts to get the NMS handle of the specified object reflectively.
	 * @param craftbukkitObject The object for which to retrieve the handle.
	 * @return The method returned by the {@code getHandle()} method of the specified object, or {@code null} if an error occurs.
	 * @see ReflectionUtilities.CraftBukkit#getNMSHandle(Object)
	 */
	public static Object getNMSHandle(Object craftbukkitObject){
		if(craftbukkitObject == null){
			return null;
		}

		try {
			return ReflectionUtilities.CraftBukkit.getNMSHandle(craftbukkitObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Gets the method with the specified parameter types.
	 * @param clazz The class containing the method.
	 * @param methodName The name of the method.
	 * @param parameters The method parameter types.
	 * @return The method with the details specified, or {@code null} if an error occurs.
	 * @see Class#getDeclaredMethod(String, Class...)
	 */
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] parameters){
		Validate.notNull(clazz, "The class must not be null.");

		try {
			Method val = clazz.getDeclaredMethod(methodName, parameters);
			val.setAccessible(true);
			return val;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
