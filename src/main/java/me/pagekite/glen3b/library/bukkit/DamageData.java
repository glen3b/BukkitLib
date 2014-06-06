package me.pagekite.glen3b.library.bukkit;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.google.common.base.Preconditions;

/**
 * Represents information about dealt damage.
 * @author Glen Husman
 */
public final class DamageData implements Cloneable {

	private Object _source;
	private double _amount;
	private DamageCause _cause;
	long _time = System.currentTimeMillis();
	
	/**
	 * Internal method provided to get the raw source object.
	 */
	Object getRawSource(){
		return _source;
	}
	
	/**
	 * Gets the time at which the damage occurred.
	 * @return The time at which the damage occurred, in milliseconds.
	 * @see System#currentTimeMillis()
	 */
	public long getTime(){
		return _time;
	}

	/**
	 * Get the source of the damage done as an entity.
	 * @return The source of damage, or {@code null} if it is not an entity that dealt the damage.
	 */
	public Entity getEntitySource(){
		if(_source == null || !(_source instanceof Entity)){
			return null;
		}
		return (Entity)_source;
	}
	
	/**
	 * Get the source of the damage done as a nlock.
	 * @return The source of damage, or {@code null} if it is not a block that dealt the damage.
	 */
	public Block getBlockSource(){
		if(_source == null || !(_source instanceof Block)){
			return null;
		}
		return (Block)_source;
	}
	
	/**
	 * Set the source of damage done to a new value.
	 * @param newSource The new source of damage.
	 */
	public void setSource(Block newSource){
		_source = newSource;
	}
	
	/**
	 * Set the source of damage done to a new value.
	 * @param newSource The new source of damage.
	 */
	public void setSource(Entity newSource){
		_source = newSource;
	}

	/**
	 * @return The final amount of damage dealt by this event.
	 */
	public double getDamageAmount() {
		return _amount;
	}

	/**
	 * Sets the final amount of damage dealt by this source, as known by the store. This value may be decreased if health is regenerated.
	 * @param amount The new amount.
	 */
	public void setDamageAmount(double amount) {
		if(amount < 0){
			throw new IllegalArgumentException("The damage amount must be positive.");
		}
		
		this._amount = amount;
	}

	/**
	 * @return The cause of damage.
	 */
	public DamageCause getCause() {
		return _cause;
	}

	/**
	 * @param cause The new damage cause.
	 */
	public void setCause(DamageCause cause) {
		this._cause = Preconditions.checkNotNull(cause, "The damage cause must not be null");
	}
	
	/**
	 * Performs a shallow clone of this damage information object.
	 * Note that since entities and blocks cannot be cloned, their instances refer to the same instances as this class.
	 * @see Object#clone()
	 */
	@Override
	public DamageData clone() throws CloneNotSupportedException{
		// We can only really do a shallow clone
		return (DamageData) super.clone();	
	}

	@Override
	public String toString() {
		return "DamageData [Source=" + _source + ", Amount=" + _amount
				+ ", Cause=" + _cause + ", Time=" + _time + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(_amount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((_cause == null) ? 0 : _cause.hashCode());
		result = prime * result + ((_source == null) ? 0 : _source.hashCode());
		result = prime * result + (int) (_time ^ (_time >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DamageData)) {
			return false;
		}
		DamageData other = (DamageData) obj;
		if (Double.doubleToLongBits(_amount) != Double
				.doubleToLongBits(other._amount)) {
			return false;
		}
		if (_cause != other._cause) {
			return false;
		}
		if (_source == null) {
			if (other._source != null) {
				return false;
			}
		} else if (!_source.equals(other._source)) {
			return false;
		}
		if (_time != other._time) {
			return false;
		}
		return true;
	}
	
	
	
}
