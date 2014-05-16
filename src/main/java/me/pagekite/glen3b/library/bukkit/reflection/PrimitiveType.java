package me.pagekite.glen3b.library.bukkit.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

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
	BOOLEAN(boolean.class, Boolean.class),
	/**
	 * Represents a signed byte.
	 * @see Byte
	 */
	BYTE(byte.class, Byte.class),
	/**
	 * Represents a character.
	 * @see Character
	 */
	CHARACTER(char.class, Character.class),
	/**
	 * Represents a double-precision floating-point value.
	 * @see Double
	 */
	DOUBLE(double.class, Double.class),
	/**
	 * Represents a single-precision floating-point value.
	 * @see Float
	 */
	FLOAT(float.class, Float.class),
	/**
	 * Represents a signed integer.
	 * @see Integer
	 */
	INTEGER(int.class, Integer.class),
	/**
	 * Represents a long integer (signed).
	 * @see Long
	 */
	LONG(long.class, Long.class),
	/**
	 * Represents a short integer (signed).
	 * @see Short
	 */
	SHORT(short.class, Short.class),
	/**
	 * Represents void, which has no value.
	 * Note that by this class, {@code null} is considered to be the only instance of this type.
	 * @see Void
	 */
	VOID(void.class, Void.class);
	
	/**
	 * Represents default values for primitive types as defined by the <i>Java Language Specification</i>.
	 */
	public static final class DefaultValues{
		private DefaultValues(){}
		
		/**
		 * Represents default primitive values as instances of the primitive types.
		 */
		public static final class Primitives{
			private Primitives(){}
			
			/**
			 * Represents the default boolean value.
			 */
			public static final boolean BOOLEAN_DEFAULT = false;
			
			/**
			 * Represents the default byte value.
			 */
			public static final byte BYTE_DEFAULT = (byte)0;
			
			/**
			 * Represents the default character value.
			 */
			public static final char CHARACTER_DEFAULT = '\0';
			
			/**
			 * Represents the default double value.
			 */
			public static final double DOUBLE_DEFAULT = 0.0;
			
			/**
			 * Represents the default float value.
			 */
			public static final float FLOAT_DEFAULT = 0.0F;
			
			/**
			 * Represents the default integer value.
			 */
			public static final int INTEGER_DEFAULT = 0;
			
			/**
			 * Represents the default long value.
			 */
			public static final long LONG_DEFAULT = 0L;
			
			/**
			 * Represents the default short value.
			 */
			public static final short SHORT_DEFAULT = (short)0;
		}
		
		/**
		 * Represents default primitive values as instances of the wrapper types.
		 */
		public static final class Wrappers{
			private Wrappers(){}
			
			/**
			 * Represents the default {@link Boolean} value.
			 */
			public static final Boolean BOOLEAN_DEFAULT = new Boolean(Primitives.BOOLEAN_DEFAULT);
			
			/**
			 * Represents the default {@link Byte} value.
			 */
			public static final Byte BYTE_DEFAULT = new Byte(Primitives.BYTE_DEFAULT);
			
			/**
			 * Represents the default {@link Character} value.
			 */
			public static final Character CHARACTER_DEFAULT = new Character(Primitives.CHARACTER_DEFAULT);
			
			/**
			 * Represents the default {@link Double} value.
			 */
			public static final Double DOUBLE_DEFAULT = new Double(Primitives.DOUBLE_DEFAULT);
			
			/**
			 * Represents the default {@link Float} value.
			 */
			public static final Float FLOAT_DEFAULT = new Float(Primitives.FLOAT_DEFAULT);
			
			/**
			 * Represents the default {@link Integer} value.
			 */
			public static final Integer INTEGER_DEFAULT = new Integer(Primitives.INTEGER_DEFAULT);
			
			/**
			 * Represents the default {@link Long} value.
			 */
			public static final Long LONG_DEFAULT = new Long(Primitives.LONG_DEFAULT);
			
			/**
			 * Represents the default {@link Short} value.
			 */
			public static final Short SHORT_DEFAULT = new Short(Primitives.SHORT_DEFAULT);
			
			/**
			 * Represents the default (and only) {@link Void} value, {@code null}.
			 */
			public static final Void VOID_DEFAULT = null;
		}
	}
	
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
	 * Returns the default value for this primitive. <b>This method should only be used if the {@code PrimitiveType} is not known at compile time.</b> If it is, the values in {@link DefaultValues} should be used in preference to this method.
	 * @return The default value for this primitive as an instance of the wrapper class (as the returned value is an {@code Object}).
	 */
	public Object getDefaultValue(){
		return _wrapperDefault;
	}
	
	/**
	 * Returns the default value for this primitive.
	 * @param useWrapper Whether to express the returned value as an instance of the wrapper class ({@code true}) or of the primitive class ({@code false}).
	 * @return The default value for this primitive.
	 * @deprecated This method effectively ignores the parameter as the language runtime automatically boxes values to be of the wrapper type when returned as an {@code Object}. {@link #getDefaultValue()} is preferred.
	 */
	@Deprecated
	public Object getDefaultValue(boolean useWrapper){
		return useWrapper ? _wrapperDefault : _default;
	}
	
	/**
	 * @param primitive The primitive type.
	 * @param ref The reference/wrapper type.
	 */
	private PrimitiveType(Class<?> primitive, Class<?> ref){
		_primitiveClass = primitive;
		_wrapperClass = ref;
		// Type defaults are NOT assigned here, they are assigned in static initializer
		_default = null;
		_wrapperDefault = null;
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
		
		// This is really a hack
		// Basically it gets all the default values which are declared as constants in the Primitives and Wrappers classes
		// And it assigns the appropriate values in the enum type
		for(Field field : DefaultValues.Primitives.class.getDeclaredFields()){
			int mods = field.getModifiers();
			if(Modifier.isPublic(mods) && Modifier.isStatic(mods) && Modifier.isFinal(mods)){
				// Assume this field represents a "default" constant
				PrimitiveType type = _primitiveTypeToEnumValueMap.get(field.getType());
				if(type == null){
					continue;
				}
				
				try {
					type._default = field.get(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		for(Field field : DefaultValues.Wrappers.class.getDeclaredFields()){
			int mods = field.getModifiers();
			if(Modifier.isPublic(mods) && Modifier.isStatic(mods) && Modifier.isFinal(mods)){
				// Assume this field represents a "default" constant
				PrimitiveType type = _wrapperTypeToEnumValueMap.get(field.getType());
				if(type == null){
					continue;
				}
				
				try {
					type._wrapperDefault = field.get(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
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
		
		if(_primitiveTypeToEnumValueMap.containsKey(clazz)){
			return _primitiveTypeToEnumValueMap.get(clazz);
		}
		
		if(_wrapperTypeToEnumValueMap.containsKey(clazz)){
			return _wrapperTypeToEnumValueMap.get(clazz);
		}
		
		return null;
	}
}
