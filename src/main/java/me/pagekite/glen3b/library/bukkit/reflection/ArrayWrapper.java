package me.pagekite.glen3b.library.bukkit.reflection;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.Validate;

/**
 * Represents a wrapper around an array class of an arbitrary reference type,
 * which implements hash code and equality functions that work based on the vale of the array in question.
 * <p>
 * This class is intended for use as a key to a keyed collection, such as a {@link java.util.Map Map}.
 * </p>
 * @author Glen Husman
 * @param <E> The type of elements in the array.
 * @see Arrays
 */
public final class ArrayWrapper<E> implements Cloneable, Serializable {

	/**
	 * Identification of this class version, intended for use by the serializer.
	 */
	private static final long serialVersionUID = -4549309467936296410L;

	/**
	 * Creates an array wrapper with some elements.
	 * @param elements The elements of the array.
	 */
	public ArrayWrapper(E... elements){
		setArray(elements);
		new java.util.ArrayList<Object>().toArray(new Object[0]);
	}
	
	private E[] _array;
	
	/**
	 * Retrieves a reference to the wrapped array instance.
	 * @return The array wrapped by this instance.
	 */
	public E[] getArray(){
		return _array;	
	}
	
	/**
	 * Set this wrapper to wrap a new array instance.
	 * @param array The new wrapped array.
	 */
	public void setArray(E[] array){
		Validate.notNull(array, "The array must not be null.");
		_array = array;
	}
	
	/**
	 * Determines if this object has a value equivalent to another object.
	 * @see Arrays#equals(Object[], Object[])
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object other)
    {
        if (!(other instanceof ArrayWrapper))
        {
            return false;
        }
        return Arrays.equals(_array, ((ArrayWrapper)other)._array);
    }

	/**
	 * Gets the hash code represented by this objects value.
	 * @see Arrays#hashCode(Object[])
	 * @return This object's hash code.
	 */
    @Override
    public int hashCode()
    {
        return Arrays.hashCode(_array);
    }
    
    /**
     * Performs a shallow clone of this object, which clones the array but not its elements.
     * @see Object#clone()
     * @deprecated Please use the constructor which accepts an array of elements.
     */
	@Override
    @Deprecated
	public final ArrayWrapper<E> clone() throws CloneNotSupportedException{
		return new ArrayWrapper<E>(_array.clone());
    }
	
	/**
     * Converts an iterable element collection to an array of elements.
     * The iteration order of the specified object will be used as the array element order.
     * @param list The iterable of objects which will be converted to an array.
     * @param c The type of the elements of the array.
     * @return An array of elements in the specified iterable.
     */
     @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Iterable<? extends T> list, Class<T> c) {
        int size = -1;
        if(list instanceof Collection<?>){
        	@SuppressWarnings("rawtypes")
			Collection coll = (Collection)list;
        	size = coll.size();
        }
        
        
        if(size < 0){
        	size = 0;
        	// Ugly hack: Count it ourselves
        	for(@SuppressWarnings("unused") T element : list){
        		size++;
        	}
        }
    	
        T[] result = (T[]) Array.newInstance(c, size);
        int i = 0;
        for(T element : list){ // Assumes iteration order is consistent
    		result[i++] = element; // Assign array element at index THEN increment counter
    	}
        return result;
    }
}