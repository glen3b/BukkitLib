package me.pagekite.glen3b.library.bukkit;

import java.awt.Color;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

/**
 * Represents the palette that {@link ChatColor} uses to represent colors. Allows attempted matching of {@link Color}s to {@link ChatColor}s.
 * @author codename_B
 * @author Glen Husman
 */
public final class ChatColorPalette {
	// Internal mechanisms
	private ChatColorPalette() {}

	private static double getDistance(Color c1, Color c2) {
		double rmean = (c1.getRed() + c2.getRed()) / 2.0;
		double r = c1.getRed() - c2.getRed();
		double g = c1.getGreen() - c2.getGreen();
		int b = c1.getBlue() - c2.getBlue();
		double weightR = 2 + rmean / 256.0;
		double weightG = 4.0;
		double weightB = 2 + (255 - rmean) / 256.0;
		return weightR * r * r + weightG * g * g + weightB * b * b;
	}

	private static final Color[] colors = {
		// foreground
		new Color(0, 0, 0), new Color(0, 0, 170), new Color(0, 170, 0),
		new Color(0, 170, 170), new Color(170, 0, 0), new Color(170, 0, 170),
		new Color(255, 170, 0), new Color(170, 170, 170), new Color(85, 85, 85),
		new Color(85, 85, 255), new Color(85, 255, 85), new Color(85, 255, 255),
		new Color(255, 85, 85), new Color(255, 85, 255), new Color(255, 255, 85),
		new Color(255, 255, 255),
	};
	
	static{
		ChatColor[] allValues = ChatColor.values();
		if(allValues.length - 6 /* There are 6 format codes */ != colors.length || !allValues[0].equals(ChatColor.BLACK /* Assure order is the same */)){
			System.err.println("The ChatColorPalette version installed does not match the Bukkit API ChatColor enumeration. ChatColorPalette is terminating execution, and API users may fail to function properly. Please contact glen3b and inform him of this error.");
			CHAT_COLORS_TO_COLORS = null;
		}else{
			// Prepare the immutable map
			ImmutableBiMap.Builder<ChatColor, Color> mapBuilder = ImmutableBiMap.<ChatColor, Color>builder();
			for(int i = 0; i < colors.length; i++){
				mapBuilder.put(allValues[i], colors[i]);
			}
			CHAT_COLORS_TO_COLORS = mapBuilder.build();
		}
		
	}
	
	/**
	 * An immutable map which maps {@link ChatColor}s to RGB colors, as known by this palette instance.
	 * If this value is {@code null}, it indicates an unsupported change in the {@code ChatColor} enumeration order.
	 * <p>
	 * This map does not contain keys for {@code ChatColor}s which are for formatting only.
	 * </p>
	 * <p>
	 * Attempts to modify this map will throw an {@code UnsupportedOperationException}.
	 * </p>
	 * <p>
	 * Due to the nature of the Google Collections API, it is possible to retrieve the inverse of this map, which maps colors to chat colors.
	 * This is an intended API feature, and can be used to map colors to chat colors <em>exactly</em>. Unless this is the indended result,
	 * it is recommended that this class be used for color matching, as it implements fuzzy matches.
	 * </p>
	 */
	public static final BiMap<ChatColor, Color> CHAT_COLORS_TO_COLORS;
	
	/**
	 * The maximum tolerated difference in component levels by {@link ChatColorPalette#areIdentical(Color, Color) areIdentical}.
	 */
	public static final int FUZZY_MATCH_LEVEL = 5;

	/**
	 * A fuzzy matching function to grab colors that are very close together in RGB values
	 * to allow for some slight distortion. Does not account for the alpha channel.
	 *
	 * @param c1 The first color.
	 * @param c2 The second color.
	 * @return {@code true} if and only if the specified colors are a fuzzy match.
	 */
	public static boolean areIdentical(Color c1, Color c2) {
		Validate.notNull(c1);
		Validate.notNull(c2);
		
		return Math.abs(c1.getRed()-c2.getRed()) <= FUZZY_MATCH_LEVEL &&
				Math.abs(c1.getGreen()-c2.getGreen()) <= FUZZY_MATCH_LEVEL &&
				Math.abs(c1.getBlue()-c2.getBlue()) <= FUZZY_MATCH_LEVEL;

	}

	/**
	 * Get the closest known matching {@code ChatColor} to the given color.
	 *
	 * @param r The red component of the color.
	 * @param b The blue component of the color.
	 * @param g The green component of the color.
	 * @return The closest {@code ChatColor} in the palette.
	 * @see #matchColor(Color)
	 */
	public static ChatColor matchColor(int r, int g, int b) {
		return matchColor(new Color(r, g, b));
	}

	/**
	 * The minimum value required in the alpha component of a color for {@link ChatColorPalette#matchColor(Color) matchColor} to consider it non equal to {@link ChatColor#BLACK}.
	 */
	public static final int MINIMUM_ALPHA = 128;
	
	/**
	 * Get the closest matching {@code ChatColor} in this palette to the given
	 * color. If the alpha value of the color is less than {@linkplain ChatColorPalette#MINIMUM_ALPHA the minimum alpha value},
	 * this method will return {@link ChatColor#BLACK}.
	 *
	 * @param color The {@code Color} to match.
	 * @return The closest {@code ChatColor} in the palette.
	 */
	public static ChatColor matchColor(Color color) {
		Validate.notNull(color);
		
		if (color.getAlpha() < MINIMUM_ALPHA) return ChatColor.BLACK;

		int index = 0;
		double best = -1;

		for(int i = 0; i < colors.length; i++) {
			if(areIdentical(colors[i], color)) {
				return ChatColor.values()[i];
			}
		}

		for (int i = 0; i < colors.length; i++) {
			double distance = getDistance(color, colors[i]);
			if (distance < best || best == -1) {
				best = distance;
				index = i;
			}
		}

		// Minecraft has 15 colors
		return ChatColor.values()[index];
	}
}
