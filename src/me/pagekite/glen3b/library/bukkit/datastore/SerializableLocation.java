package me.pagekite.glen3b.library.bukkit.datastore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * Represents a YAML-serializable location class.
 * @author Glen Husman
 */
public final class SerializableLocation implements Serializable, ConfigurationSerializable {
	private static final long serialVersionUID = 4330155630564355916L;
	private double x,y,z;
    private String world;
    
    /**
     * Creates a SerializableLocation from a location.
     * @param loc The location to serialize.
     */
    public SerializableLocation(Location loc) {
        x=loc.getX();
        y=loc.getY();
        z=loc.getZ();
        world=loc.getWorld().getName();
    }
    
    /**
     * Gets the deserialized location.
     * @return The Location instance, or null if the serialized form was invalid.
     */
    public Location getLocation() {
        World w = Bukkit.getWorld(world);
        if(w==null)
            return null;
        Location toRet = new Location(w,x,y,z);
        return toRet;
    }
    
    /**
     * Deserialization constructor.
     * @see ConfigurationSerializable
     * @param serialized The serialized form of the object.
     */
  	public SerializableLocation(Map<String, Object> serialized){
  		x = (Double)serialized.get("x");
  		y = (Double)serialized.get("y");
  		z = (Double)serialized.get("z");
  		world = (String)serialized.get("world");
  	}
    
  	/**
  	 * Serializes the object.
  	 * @see ConfigurationSerializable
  	 */
	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> objects = new HashMap<String, Object>();
		objects.put("x", x);
		objects.put("y", y);
		objects.put("z", z);
		objects.put("world", world);
		return objects;
	}
}
