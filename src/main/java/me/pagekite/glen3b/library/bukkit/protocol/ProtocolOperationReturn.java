package me.pagekite.glen3b.library.bukkit.protocol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents the return of an operation using protocol utilities.
 * @author Glen Husman
 * @param <R> The type of the return value.
 */
public final class ProtocolOperationReturn<R> {

	private ProtocolOperationResult _result;
	private R _retVal;
	private Throwable _errCause;
	
	/**
	 * Creates a protocol operation return value.
	 * @param result The result of the operation.
	 * @param cause The cause of the result.
	 */
	public ProtocolOperationReturn(ProtocolOperationResult result, Throwable cause){
		this(result);
		_errCause = cause;
	}
	
	/**
	 * Creates a protocol operation return value.
	 * @param result The result of the operation.
	 */
	public ProtocolOperationReturn(ProtocolOperationResult result){
		Validate.notNull(result, "The protocol operation result must not be null.");
		
		_result = result;
	}
	
	/**
	 * Creates a protocol operation return value.
	 * @param result The result of the operation.
	 * @param value The return value of the operation.
	 */
	public ProtocolOperationReturn(ProtocolOperationResult result, R value){
		this(result);
		_retVal = value;
	}
	
	/**
	 * @return The result of this protocol operation.
	 */
	@Nonnull public ProtocolOperationResult getResult(){
		return _result;
	}
	
	/**
	 * @return The return value of the executed protocol operation.
	 */
	@Nullable public R getReturn(){
		return _retVal;
	}
	
	/**
	 * @return The cause of the failure of the executed protocol operation, or {@code null} if unknown or not applicable.
	 */
	@Nullable public Throwable getErrorCause(){
		return _errCause;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other){
		return new EqualsBuilder().appendSuper(super.equals(other))
				.append(getResult(), other == null ? null : ((ProtocolOperationReturn<R>)other).getResult()).
				append(getReturn(), other == null ? null : ((ProtocolOperationReturn<R>)other).getReturn()).
				append(getErrorCause(), other == null ? null : ((ProtocolOperationReturn<R>)other).getErrorCause()).
				isEquals();
	}
	
	@Override
	public int hashCode(){
		return new HashCodeBuilder(37, 17).appendSuper(super.hashCode())
				.append(getResult()).
				append(getReturn()).
				append(getErrorCause()).
				toHashCode();
	}
}
