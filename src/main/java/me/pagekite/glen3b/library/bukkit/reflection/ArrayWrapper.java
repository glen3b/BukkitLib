package me.pagekite.glen3b.library.bukkit.reflection;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * Represents a wrapper around an array class of an arbitrary reference type,
 * which implements hash code and equality functions that work based on the vale of the array in question.
 * <p>
 * This class is intended for use as a key to a keyed collection, such as a {@link Map}.
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
}