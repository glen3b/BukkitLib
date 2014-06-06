package me.pagekite.glen3b.library.bukkit;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

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
	 * Get the source of the damage done as an instance of {@link Entity}.
	 * <p>
	 * If the damager related to this data is an instance of {@link Projectile},
	 * this method will return the <em>shooter of</em> that projectile, assuming it is an instance of {@code Entity}.
	 * To get the projectile instance itself, use {@link #getSourceAsProjectile()}.
	 * @return The source of damage, or {@code null} if it is not an entity that dealt the damage.
	 */
	public Entity getSourceAsEntity(){
		if(_source == null || !(_source instanceof Entity /* One check because Projectile extends Entity */)){
			return null;
		}
		Projectile proj = getSourceAsProjectile();
		ProjectileSource shooter = proj == null ? null : proj.getShooter();
		if(shooter instanceof Entity){
			return (Entity)shooter;
		}else{
			return (Entity)_source;
		}
	}
	
	/**
	 * Get the source of the damage done as an instance of {@link Block}.
	 * <p>
	 * If the damager related to this data is an instance of {@link Projectile},
	 * this method will return the <em>block represented by the shooter of</em> that projectile, assuming it is an instance of {@code BlockProjectileSource}.
	 * To get the projectile instance itself, use {@link #getSourceAsProjectile()}.
	 * @return The source of damage, or {@code null} if it is not a block that dealt the damage.
	 */
	public Block getSourceAsBlock(){
		if(_source == null || (!(_source instanceof Block) && !(_source instanceof Projectile))){
			return null;
		}
		Projectile proj = getSourceAsProjectile();
		ProjectileSource shooter = proj == null ? null : proj.getShooter();
		if(shooter instanceof BlockProjectileSource){
			return ((BlockProjectileSource)shooter).getBlock();
		}else{
			return (Block)_source;
		}
	}
	
	/**
	 * Get the source of the damage done as an instance of {@link Projectile}.
	 * @return The source of damage, or {@code null} if it is not a projectile that dealt the damage.
	 */
	public Projectile getSourceAsProjectile(){
		if(_source == null || !(_source instanceof Projectile)){
			return null;
		}
		return (Projectile)_source;
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
	 * Sets the final amount of damage dealt by this source, as known by the store.
	 * <p>
	 * This value is intended to be decreased by using this setter if health lost by this damage event is regenerated.
	 * <p>
	 * This setter method will round incoming values to the nearest 100th decimal place.
	 * @param amount The new amount.
	 * @see Math#rint(double)
	 */
	public void setDamageAmount(double amount) {
		if(amount < 0){
			throw new IllegalArgumentException("The damage amount must be positive.");
		}
		
		this._amount = Math.rint(amount * 100.0) / 100.0; // Round to hundredth decimal place
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
