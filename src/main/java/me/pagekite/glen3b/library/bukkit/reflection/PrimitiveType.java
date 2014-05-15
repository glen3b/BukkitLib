package me.pagekite.glen3b.library.bukkit.reflection;

import java.util.Map;

import com.google.common.base.Defaults;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;

/**
 * Represents all Java primitives. This class does not convert to and from primitives and wrappers.
 * {@link ReflectionUtilities#getPrimitiveForWrapper(Class) getPrimitiveForWrapper} converts wrapper types to primitive types, and {@link ReflectionUtilities#getWrapperForPrimitive(Class) getWrapperForPrimitive} converts primitive types to wrapper types.
 * @author Glen Husman
 */
public enum PrimitiveType {

	/**
	 * Represents a boolean.
	 * @see Boolean
	 */
	BOOLEAN(boolean.class),
	/**
	 * Represents a signed byte.
	 * @see Byte
	 */
	BYTE(byte.class),
	/**
	 * Represents a character.
	 * @see Character
	 */
	CHARACTER(char.class),
	/**
	 * Represents a double-precision floating-point value.
	 * @see Double
	 */
	DOUBLE(double.class),
	/**
	 * Represents a single-precision floating-point value.
	 * @see Float
	 */
	FLOAT(float.class),
	/**
	 * Represents a signed integer.
	 * @see Integer
	 */
	INTEGER(int.class),
	/**
	 * Represents a long integer (signed).
	 * @see Long
	 */
	LONG(long.class),
	/**
	 * Represents a short integer (signed).
	 * @see Short
	 */
	SHORT(short.class),
	/**
	 * Represents void, which has no value.
	 * Note that by this class, {@code null} is considered to be the only instance of this type.
	 * @see Void
	 */
	VOID(void.class);
	
	private Object _default;
	private Object _wrapperDefault;
	private Class<?> _primitiveClass;
	private Class<?> _wrapperClass;
	
	static BiMap<Class<?>, Class<?>> PRIMITIVE_TYPE_MAP;
	private static Map<Class<?>, PrimitiveType> _primitiveTypeToEnumValueMap;
	private static Map<Class<?>, PrimitiveType> _wrapperTypeToEnumValueMap;
	
	/**
	 * Gets the {@link Class} object representing the wrapper class for this {@code PrimitiveType}.
	 * @return The wrapped type.
	 */
	public Class<?> getWrapper(){
		return _wrapperClass;
	}
	
	/**
	 * Gets the {@link Class} object representing the primitive keyword (the non-wrapped class) for this {@code PrimitiveType}.
	 * @return The unwrapped type.
	 */
	public Class<?> getPrimitive(){
		return _primitiveClass;
	}
	
	/**
	 * Returns the default value for this primitive.
	 * @return The default value for this primitive as an instance of the primitive class.
	 */
	public Object getDefaultValue(){
		return getDefaultValue(false);
	}
	
	/**
	 * Returns the default value for this primitive.
	 * @param useWrapper Whether to express the returned value as an instance of the wrapper class ({@code true}) or of the primitive class ({@code false}).
	 * @return The default value for this primitive.
	 */
	public Object getDefaultValue(boolean useWrapper){
		return useWrapper ? _wrapperDefault : _default;
	}
	
	/**
	 * @param clazz The PRIMITIVEs Class object.
	 */
	private PrimitiveType(Class<?> clazz){
		_primitiveClass = clazz;
		_wrapperClass = Primitives.wrap(clazz);
		_default = Defaults.defaultValue(_primitiveClass);
		_wrapperDefault = _wrapperClass.cast(_default);
	}
	
	static{
		ImmutableBiMap.Builder<Class<?>, Class<?>> primitiveToWrapperBuilder = ImmutableBiMap.builder();
		ImmutableMap.Builder<Class<?>, PrimitiveType> primitiveTypeToEnumBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<Class<?>, PrimitiveType> wrapperTypeToEnumBuilder = ImmutableMap.builder();
		for(PrimitiveType t : values()){
			primitiveToWrapperBuilder.put(t.getPrimitive(), t.getWrapper());
			primitiveTypeToEnumBuilder.put(t.getPrimitive(), t);
			wrapperTypeToEnumBuilder.put(t.getWrapper(), t);
		}
		PRIMITIVE_TYPE_MAP = primitiveToWrapperBuilder.build();
		_primitiveTypeToEnumValueMap = primitiveTypeToEnumBuilder.build();
		_wrapperTypeToEnumValueMap = wrapperTypeToEnumBuilder.build();
	}
	
	/**
	 * Determines if the specified type represents a primitive wrapper type.
	 * @param type The type to check.
	 * @return {@code true} if and only if type represents a supported primitive wrapper type.
	 */
	public static boolean isWrapper(Class<?> type){
		return type != null && PRIMITIVE_TYPE_MAP.containsValue(type);
	}
	
	/**
	 * Determines if the specified type represents a primitive type.
	 * @param type The type to check.
	 * @return {@code true} if and only if type represents a supported primitive non-wrapper type.
	 */
	public static boolean isPrimitive(Class<?> type){
		return type == null || PRIMITIVE_TYPE_MAP.containsKey(type);
	}
	
	/**
	 * Gets a {@code PrimitiveType} instance that is represented by the specified class.
	 * @param clazz The primitive type (which may be a wrapper or primitive class) for which to retrieve an enum value.
	 * @return The enum value representing the specified class, or {@code null} if not found.
	 */
	public static PrimitiveType getPrimitiveType(Class<?> clazz){
		if(clazz == null){
			return PrimitiveType.VOID;
		}
		
		if(_primitiveTypeToEnumValueMap.containsKey(clazz)){
			return _primitiveTypeToEnumValueMap.get(clazz);
		}
		
		if(_wrapperTypeToEnumValueMap.containsKey(clazz)){
			return _wrapperTypeToEnumValueMap.get(clazz);
		}
		
		return null;
	}
}
