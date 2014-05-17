package me.pagekite.glen3b.library.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import me.pagekite.glen3b.library.bukkit.reflection.ReflectionUtilities;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * <h3>ParticleEffects Library v1.4</h3><br/>
 *
 * This library was created by DarkBladee12 based on content related to particles of microgeek (names and packet values). It allows you to display all known Minecraft particle effects on a CraftBukkit server.
 *<p>
 * You are welcome to use it, modify it and redistribute it under the following conditions:
 * <ol>
 * <li> Don't claim this class as your own
 * <li> Don't remove this text
 * </ol>
 *
 * (Would be nice if you provide credit to me)
 * </p>
 * 
 * <p>
 * Note that the enum values of this class do <em>not</em> include icon crack, block dust, or block crack particles, as they are treated specially.
 *
 * @author <a href="http://forums.bukkit.org/members/darkbladee12.90749545/">DarkBladee12</a>
 * @author <a href="http://forums.bukkit.org/members/microgeek.90705652/">microgeek</a>
 * @author Glen Husman (doucmentation fixes, overload improvements)
 */
public enum ParticleEffect {
	/**
	 * Appears as a huge explosion.
	 * Displayed naturally by TNT and creepers.
	 */
	HUGE_EXPLOSION("hugeexplosion"),
	/**
	 * Appears as a smaller explosion than {@link #HUGE_EXPLOSION}.
	 * Displayed naturally by TNT and creepers.
	 */
	LARGE_EXPLODE("largeexplode"),
	/**
	 * Appears as little white sparkling stars.
	 * Displayed naturally by fireworks.
	 */
	FIREWORKS_SPARK("fireworksSpark"),
	/**
	 * Appears as bubbles.
	 * Displayed natrually in water.
	 */
	BUBBLE("bubble"),
	/**
	 * <i>Information about this particle is not known.</i>
	 */
	SUSPEND("suspend"),
	/**
	 * Appears as little gray dots.
	 * Displayed naturally in and near the void, as well as in water.
	 */
	DEPTH_SUSPEND("depthSuspend"),
	/**
	 * Appears as ittle gray dots.
	 * Displayed naturally as an ambient effect by Mycelium.
	 */
	TOWN_AURA("townaura"),
	/**
	 * Appears as light brown crosses.
	 * Displayed naturally by critical hits.
	 */
	CRIT("crit"),
	/**
	 * Appears as cyan stars
	 * Displayed naturally by hits with an enchanted weapon.
	 */
	MAGIC_CRIT("magicCrit"),
	/**
	 * Appears as little black/gray clouds.
	 * Displayed naturally by torches, primed TNT and end portals.
	 */
	SMOKE("smoke"),
	/**
	 * Appears as colored swirls.
	 * Displayed naturally by potion effects.
	 */
	MOB_SPELL("mobSpell"),
	/**
	 * Appears as transparent colored swirls.
	 * Displayed naturally by beacon-induced effects.
	 */
	MOB_SPELL_AMBIENT("mobSpellAmbient"),
	/**
	 * Appears as colored swirls.
	 * Displayed naturally by splash potions.
	 */
	SPELL("spell"),
	/**
	 * Appears as colored crosses.
	 * Displayed naturally by instant splash potions (ones which apply instantly, such as {@linkplain org.bukkit.potion.PotionEffectType#HARM harming} and {@linkplain org.bukkit.potion.PotionEffectType#HEAL healing}).
	 */
	INSTANT_SPELL("instantSpell"),
	/**
	 * Appears as colored crosses.
	 * Displayed naturally by witches.
	 */
	WITCH_MAGIC("witchMagic"),
	/**
	 * Appears as colored notes.
	 * Displayed naturally by note blocks (when they play a note).
	 */
	NOTE("note"),
	/**
	 * Appears as little purple clouds.
	 * Displayed naturally by nether portals, endermen, ender pearls, eyes of ender and ender chests.
	 */
	PORTAL("portal"),
	/**
	 * Appears as white letters.
	 * Displayed naturally by enchantment tables that are near bookshelves.
	 */
	ENCHANTMENT_TABLE("enchantmenttable"),
	/**
	 * Appears as white clouds.
	 * Displayed naturally along with explosions.
	 */
	EXPLODE("explode"),
	/**
	 * Appears as little flames.
	 * Displayed naturally by torches, furnaces, magma cubes and monster spawners.
	 */
	FLAME("flame"),
	/**
	 * Appears as little orange blobs.
	 * Displayed naturally by lava.
	 */
	LAVA("lava"),
	/**
	 * Appears as gray transparent squares.
	 */
	FOOTSTEP("footstep"),
	/**
	 * Appears as blue drops.
	 * Displayed naturally by water, rain and shaking wolves.
	 */
	SPLASH("splash"),
	/**
	 * Appears as blue droplets.
	 * Displayed naturally on water when fishing.
	 */
	WAKE("wake"),
	/**
	 * Appears as black and gray clouds.
	 * Displayed naturally by fire, minecarts with furnaces and blazes.
	 */
	LARGE_SMOKE("largesmoke"),
	/**
	 * Appears as large white clouds.
	 * Displayed naturally upon on death of a living entity.
	 */
	CLOUD("cloud"),
	/**
	 * Appears as little colored clouds.
	 * Displayed naturally by active redstone wires and redstone torches.
	 */
	RED_DUST("reddust"),
	/**
	 * Appears as little white fragments.
	 * Displayed naturally by cracking snowballs and eggs.
	 */
	SNOWBALL_POOF("snowballpoof"),
	/**
	 * Appears as blue drips.
	 * Displayed by blocks below a water source.
	 */
	DRIP_WATER("dripWater"),
	/**
	 * Appears as reddish orange drips.
	 * Displayed naturally by blocks below a lava source.
	 */
	DRIP_LAVA("dripLava"),
	/**
	 * Appears as white clouds.
	 */
	SNOW_SHOVEL("snowshovel"),
	/**
	 * Appears as little green fragments.
	 * Displayed naturally by slimes.
	 */
	SLIME("slime"),
	/**
	 * Appears as red hearts.
	 * Displayed natrually when breeding animals.
	 */
	HEART("heart"),
	/**
	 * Appears as dark gray cracked hearts.
	 * Displayed naturally when a player attacks a villager in a village.
	 */
	ANGRY_VILLAGER("angryVillager"),
	/**
	 * Appears as green stars.
	 * Displayed by bone meal and when trading with a villager.
	 */
	HAPPY_VILLAGER("happyVillager");

	private static final Map<String, ParticleEffect> NAME_MAP = new HashMap<String, ParticleEffect>();
	private static final double MAX_RANGE = 24; // More than 16 to give some breathing room
	private static Constructor<?> packetPlayOutWorldParticles;
	private static Field playerConnection;
	private static Method sendPacket;
	private final String name;

	static {
		for (ParticleEffect p : values()){
			NAME_MAP.put(p.name, p);
		}
		try {
			packetPlayOutWorldParticles = ReflectionUtilities.getConstructor(ReflectionUtilities.Minecraft.getType("PacketPlayOutWorldParticles"), String.class, float.class, float.class, float.class, float.class, float.class,
					float.class, float.class, int.class);
			playerConnection = ReflectionUtilities.getField(ReflectionUtilities.Minecraft.getType("EntityPlayer"), "playerConnection");
			sendPacket = playerConnection.getType().getDeclaredMethod("sendPacket", ReflectionUtilities.Minecraft.getType("Packet"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param name Name of this particle effect
	 */
	private ParticleEffect(String name) {
		this.name = name;
	}

	/**
	 * @return The protocol name of this particle effect.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets a particle effect by minecraft protocol name.
	 * @param name The name of the particle effect.
	 * @return The particle effect if found, or {@code null} if it was not found.
	 */
	public static ParticleEffect fromName(String name) {
		Validate.notEmpty(name, "A particle effect name must be specified.");

		for (Entry<String, ParticleEffect> e : NAME_MAP.entrySet()){
			if (e.getKey().equalsIgnoreCase(name)) return e.getValue();
		}

		return null;
	}

	/**
	 * Instantiates a new {@code PacketPlayOutWorldParticles} object using reflection.
	 *
	 * @param center Center location of the effect.
	 * @param offsetX Maximum distance particles can fly away from the center on the X-axis.
	 * @param offsetY Maximum distance particles can fly away from the center on the Y-axis.
	 * @param offsetZ Maximum distance particles can fly away from the center on the Z-axis.
	 * @param speed Display speed of the particles. A sort of data value.
	 * @param amount The number of particles to display.
	 * @return The packet object.
	 * @throws RuntimeException If an error occurs instantiating the packet.
	 */
	private static Object instantiatePacket(String name, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		Validate.isTrue(amount >= 1, "At least one packet must be instantiated.");

		try {
			return packetPlayOutWorldParticles.newInstance(name, (float) center.getX(), (float) center.getY(), (float) center.getZ(), offsetX, offsetY, offsetZ, speed, amount);
		} catch (Exception e) {
			throw new RuntimeException("Instantiation of a particle packet failed.", e);
		}
	}

	/**
	 * Instantiates a new {@code PacketPlayOutWorldParticles} object using reflection especially for the "iconcrack" effect
	 *
	 * @see #instantiatePacket
	 */
	private static Object instantiateIconCrackPacket(int id, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		return instantiatePacket("iconcrack_" + id, center, offsetX, offsetY, offsetZ, speed, amount);
	}

	/**
	 * Instantiates a new {@code PacketPlayOutWorldParticles} object using reflection especially for the "blockcrack" effect
	 *
	 * @see #instantiatePacket
	 */
	private static Object instantiateBlockCrackPacket(int id, byte data, Location center, float offsetX, float offsetY, float offsetZ, int amount) {
		return instantiatePacket("blockcrack_" + id + "_" + data, center, offsetX, offsetY, offsetZ, 0, amount);
	}

	/**
	 * Instantiates a new {@code PacketPlayOutWorldParticles} object using reflection especially for the "blockdust" effect
	 *
	 * @see #instantiatePacket
	 */
	private static Object instantiateBlockDustPacket(int id, byte data, Location center, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		return instantiatePacket("blockdust_" + id + "_" + data, center, offsetX, offsetY, offsetZ, speed, amount);
	}

	/**
	 * Using reflection, injects a packet into the players connection so that the client receives it.
	 *
	 * @param p Receiver of the packet. <i>Must</i> be an instance of {@code CraftPlayer}.
	 * @param packet Packet that is sent.
	 */
	private static void sendPacket(Player p, Object packet) {
		Validate.notNull(packet, "The packet must not be null.");

		try {
			sendPacket.invoke(playerConnection.get(ReflectionUtilities.CraftBukkit.getNMSHandle(p)), packet);
		} catch (Exception e) {
			throw new RuntimeException("Failed to send a packet to the player '" + p.getName() + "' reflectively.", e);
		}
	}

	/**
	 * Sends a packet through reflection to a collection of players.
	 * @see #sendPacket
	 */
	private static void sendPacket(Iterable<Player> players, Object packet) {
		for (Player p : players){
			if(p == null){
				throw new IllegalArgumentException("No null players may be specified as packet recipients.");
			}

			sendPacket(p, packet);
		}
	}

	/**
	 * Displays a particle effect which is only visible for players within a certain range of the centerpoint.
	 *
	 * @param center The center location of the particle effect.
	 * @param range The range which binds all players that will receive the packet.
	 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
	 * @param speed "Speed" of the particles, a data value of sorts.
	 * @param amount The number of particles to display.
	 * @param players Receivers of the particle effect.
	 */
	public void display(Location center, double range, Vector offset, float speed, int amount) {
		Validate.notNull(center, "The center location must not be null.");
		Validate.notNull(center.getWorld(), "The center location must not be null.");
		Validate.notNull(offset, "The offset values must not be null.");

		// Really no point to enforcing, just don't use huge values
		if (range > MAX_RANGE){
			// Enforced server-side to promote efficiency
			// throw new IllegalArgumentException("The range of particle recipients cannot exceed the maximum value of " + MAX_RANGE +", a limitation of the client.");
			Bukkit.getLogger().log(Level.WARNING, "A particle is being displayed with the range set to " + range + ", higher than the recommended maximum of " + MAX_RANGE + ", which will potentially result in inefficiency.");
		}

		sendPacket(Utilities.Entities.getEntitiesInRange(center, range, Player.class), instantiatePacket(name, center, (float)offset.getX(), (float)offset.getY(), (float)offset.getZ(), speed, amount));
	}

	/**
	 * Displays a particle effect which is visible for all players who can hypothetically see the particle if the appropriate packet is sent to their client.
	 * The range is very generous, however larger values can be specified in the linked overload.
	 *
	 * @param center The center location of the particle effect.
	 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
	 * @param speed "Speed" of the particles, a data value of sorts.
	 * @param amount The number of particles to display.
	 * @see #display(Location, double, Vector, float, int)
	 */
	public void display(Location center, Vector offset, float speed, int amount) {
		display(center, MAX_RANGE, offset, speed, amount);
	}

	/**
	 * Displays an item break (icon crack) particle effect which is only visible for players within a certain range of the centerpoint.
	 * @param center The center location of the particle effect.
	 * @param item The item type for which this effect applies.
	 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
	 * @param speed "Speed" of the particles, a data value of sorts.
	 * @param amount The number of particles to display.
	 * @param range The range which binds all players that will receive the packet.
	 */
	@SuppressWarnings("deprecation")
	public static void displayIconCrack(Location center, double range, Material item, Vector offset, float speed, int amount) {
		Validate.notNull(center, "The center location must not be null.");
		Validate.notNull(center.getWorld(), "The center location must not be null.");
		Validate.notNull(offset, "The offset values must not be null.");
		Validate.isTrue(item != null && !item.isBlock(), "The specified material is not a valid item.");

		// Really no point to enforcing, just don't use huge values
		if (range > MAX_RANGE){
			// Enforced server-side to promote efficiency
			// throw new IllegalArgumentException("The range of particle recipients cannot exceed the maximum value of " + MAX_RANGE +", a limitation of the client.");
			Bukkit.getLogger().log(Level.WARNING, "A particle is being displayed with the range set to " + range + ", higher than the recommended maximum of " + MAX_RANGE + ", which will potentially result in inefficiency.");
		}

		sendPacket(Utilities.Entities.getEntitiesInRange(center, range, Player.class), instantiateIconCrackPacket(item.getId(), center, (float)offset.getX(), (float)offset.getY(), (float)offset.getZ(), speed, amount));
	}

	/**
	 * Displays an item break (icon crack) particle effect which is visible for all players who can hypothetically see the particle if the appropriate packet is sent to their client.
	 * The range is very generous, however larger values can be specified in the linked overload.
	 * @param center The center location of the particle effect.
	 * @param item The item type for which this effect applies.
	 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
	 * @param speed "Speed" of the particles, a data value of sorts.
	 * @param amount The number of particles to display.
	 * @see #displayIconCrack(Location, double, Material, Vector, float, int)
	 */
	public static void displayIconCrack(Location center, Material item, Vector offset, float speed, int amount) {
		displayIconCrack(center, MAX_RANGE, item, offset, speed, amount);
	}

	/**
	 * Displays a block break (block crack) particle effect which is only visible for players within a certain range of the centerpoint.
	 * @param center The center location of the particle effect.
	 * @param data The material data (which includes type) of the represented block. This value may not be {@code null}.
	 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
	 * @param speed "Speed" of the particles, a data value of sorts.
	 * @param amount The number of particles to display.
	 * @param range The range which binds all players that will receive the packet.
	 */
	@SuppressWarnings("deprecation")
	public static void displayBlockCrack(Location center, double range, MaterialData data, Vector offset, int amount) {
		Validate.notNull(center, "The center location must not be null.");
		Validate.notNull(center.getWorld(), "The center location must not be null.");
		Validate.notNull(offset, "The offset values must not be null.");
		Validate.isTrue(data != null && data.getItemType().isBlock(), "The specified material is not a valid block.");

		// Really no point to enforcing, just don't use huge values
		if (range > MAX_RANGE){
			// Enforced server-side to promote efficiency
			// throw new IllegalArgumentException("The range of particle recipients cannot exceed the maximum value of " + MAX_RANGE +", a limitation of the client.");
			Bukkit.getLogger().log(Level.WARNING, "A particle is being displayed with the range set to " + range + ", higher than the recommended maximum of " + MAX_RANGE + ", which will potentially result in inefficiency.");
		}
		sendPacket(Utilities.Entities.getEntitiesInRange(center, range, Player.class), instantiateBlockCrackPacket(data.getItemTypeId(), data.getData(), center, (float)offset.getX(), (float)offset.getY(), (float)offset.getZ(), amount));
	}

	/**
	 * Displays a block break (block crack) particle effect which is visible for all players who can hypothetically see the particle if the appropriate packet is sent to their client.
	 * The range is very generous, however larger values can be specified in the linked overload.
	 * @param center The center location of the particle effect.
	 * @param data The material data (which includes type) of the represented block. This value may not be {@code null}.
	 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
	 * @param speed "Speed" of the particles, a data value of sorts.
	 * @param amount The number of particles to display.
	 * @see #displayBlockCrack(Location, double, MaterialData, Vector, int)
	 */
	public static void displayBlockCrack(Location center, MaterialData data, Vector offset, int amount) {
		displayBlockCrack(center, MAX_RANGE, data, offset, amount);
	}

	/**
	 * Displays a block dust particle effect which is only visible for players within a certain range of the centerpoint.
	 * @param center The center location of the particle effect.
	 * @param data The material data (which includes type) of the represented block. This value may not be {@code null}.
	 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
	 * @param speed "Speed" of the particles, a data value of sorts.
	 * @param amount The number of particles to display.
	 * @param range The range which binds all players that will receive the packet.
	 */
	@SuppressWarnings("deprecation")
	public static void displayBlockDust(Location center, double range, MaterialData data, Vector offset, float speed, int amount) {
		Validate.notNull(center, "The center location must not be null.");
		Validate.notNull(center.getWorld(), "The center location must not be null.");
		Validate.notNull(offset, "The offset values must not be null.");
		Validate.isTrue(data != null && data.getItemType().isBlock(), "The specified material is not a valid block.");

		// Really no point to enforcing, just don't use huge values
		if (range > MAX_RANGE){
			// Enforced server-side to promote efficiency
			// throw new IllegalArgumentException("The range of particle recipients cannot exceed the maximum value of " + MAX_RANGE +", a limitation of the client.");
			Bukkit.getLogger().log(Level.WARNING, "A particle is being displayed with the range set to " + range + ", higher than the recommended maximum of " + MAX_RANGE + ", which will potentially result in inefficiency.");
		}
		sendPacket(Utilities.Entities.getEntitiesInRange(center, range, Player.class), instantiateBlockDustPacket(data.getItemTypeId(), data.getData(), center, (float)offset.getX(), (float)offset.getY(), (float)offset.getZ(), speed, amount));
	}

	/**
	 * Displays a block dust particle effect which is visible for all players who can hypothetically see the particle if the appropriate packet is sent to their client.
	 * The range is very generous, however larger values can be specified in the linked overload.
	 * @param center The center location of the particle effect.
	 * @param data The material data (which includes type) of the represented block. This value may not be {@code null}.
	 * @param offset A vector representing the maximum distance particles can fly away from the center location on each axis (independently).
	 * @param speed "Speed" of the particles, a data value of sorts.
	 * @param amount The number of particles to display.
	 * @see #displayBlockDust(Location, double, MaterialData, Vector, float, int)
	 */
	public static void displayBlockDust(Location center, MaterialData data, Vector offset, float speed, int amount) {
		displayBlockDust(center, MAX_RANGE, data, offset, speed, amount);
	}
}