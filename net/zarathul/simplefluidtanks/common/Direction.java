package net.zarathul.simplefluidtanks.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.ForgeDirection;

import com.google.common.collect.ImmutableList;

/**
 * Provides constants and helper methods to convert vanilla directions into {@link ForgeDirection}s and vice versa.
 */
public class Direction
{
	/**
	 * The vanilla minecraft value for the direction along the negative y axis.
	 */
	public static final int YNEG = 0;
	
	/**
	 * The vanilla minecraft value for the direction along the positive y axis.
	 */
	public static final int YPOS = 1;
	
	/**
	 * The vanilla minecraft value for the direction along the negative z axis.
	 */
	public static final int ZNEG = 2;
	
	/**
	 * The vanilla minecraft value for the direction along the positive z axis.
	 */
	public static final int ZPOS = 3;
	
	/**
	 * The vanilla minecraft value for the direction along the negative x axis.
	 */
	public static final int XNEG = 4;
	
	/**
	 * The vanilla minecraft value for the direction along the positive x axis.
	 */
	public static final int XPOS = 5;
	
	/**
	 * Maps bitflags to vanilla minecraft direction values.
	 */
	public static final ImmutableList<Integer> sidesToBitFlagsMappings = ImmutableList.of
	(
		1, 	// 0	YNEG
		2, 	// 1	YPOS
		4, 	// 2	ZNEG
		8, 	// 3	ZPOS
		16,	// 4	XNEG
		32	// 5	XPOS
	);
	
	/**
	 * Maps each vanilla minecraft side to its opposite side.
	 */
	public static final Map<Integer, Integer> vanillaSideOpposites = Collections.unmodifiableMap
	(
		new HashMap<Integer, Integer>()
		{{
			put(YNEG, YPOS);
			put(YPOS, YNEG);
			put(ZNEG, ZPOS);
			put(ZPOS, ZNEG);
			put(XNEG, XPOS);
			put(XPOS, XNEG);
		}}
	);
	
	/**
	 * Maps directional offsets to their respective vanilla sides.
	 */
	public static final Map<Integer, DirectionalOffset> vanillaSideOffsets = Collections.unmodifiableMap
	(
		new HashMap<Integer, DirectionalOffset>()
		{{
			put(YNEG, DirectionalOffset.YNEG);
			put(YPOS, DirectionalOffset.YPOS);
			put(ZNEG, DirectionalOffset.ZNEG);
			put(ZPOS, DirectionalOffset.ZPOS);
			put(XNEG, DirectionalOffset.XNEG);
			put(XPOS, DirectionalOffset.XPOS);
		}}
	);
	
	/**
	 * Maps vanilla minecraft direction values to Forge directions.
	 */
	public static final Map<Integer, ForgeDirection> vanillaToForgeMapping = Collections.unmodifiableMap
	(
		new HashMap<Integer, ForgeDirection>()
		{{
			put(YNEG, ForgeDirection.DOWN);
			put(YPOS, ForgeDirection.UP);
			put(ZNEG, ForgeDirection.NORTH);
			put(ZPOS, ForgeDirection.SOUTH);
			put(XNEG, ForgeDirection.WEST);
			put(XPOS, ForgeDirection.EAST);
		}}
	);
	
	/**
	 * Maps Forge directions to vanilla minecraft direction values.
	 */
	public static final Map<ForgeDirection, Integer> forgeToVanillaMapping = Collections.unmodifiableMap
	(
		new HashMap<ForgeDirection, Integer>()
		{{
			put(ForgeDirection.UP, YPOS);
			put(ForgeDirection.DOWN, YNEG);
			put(ForgeDirection.EAST, XPOS);
			put(ForgeDirection.WEST, XNEG);
			put(ForgeDirection.SOUTH, ZPOS);
			put(ForgeDirection.NORTH, ZNEG);
		}}
	);
	
	/**
	 * Converts a {@link ForgeDirection} into a vanilla minecraft direction value.
	 * @param direction
	 * The direction to convert.
	 * @return
	 * <code>0-5</code> or <code>-1</code> for ForgeDirection.UNKNOWN
	 */
	public static int fromForge(ForgeDirection direction)
	{
		if (direction == ForgeDirection.UNKNOWN)
		{
			return -1;
		}
		
		return forgeToVanillaMapping.get(direction);
	}
	
	/**
	 * Converts a vanilla minecraft direction value into a {@link ForgeDirection}.
	 * @param side
	 * The vanilla minecraft direction value to convert.
	 * @return
	 * The corresponding {@link ForgeDirection}.
	 */
	public static ForgeDirection toForge(int side)
	{
		if (side >= 0 && side <= 5)
		{
			return vanillaToForgeMapping.get(side);
		}
		
		return ForgeDirection.UNKNOWN;
	}
	
	/**
	 * Defines offsets for different directions.
	 */
	public enum DirectionalOffset
	{
		YNEG(0, -1, 0),
		YPOS(0, 1, 0),
		ZNEG(0, 0, -1),
		ZPOS(0, 0, 1),
		XNEG(-1, 0, 0),
		XPOS(1, 0, 0);
		
		/**
		 * The offset for the x-axis.
		 */
		public final int x;
		/**
		 * The offset for the y-axis.
		 */
		public final int y;
		/**
		 * The offset for the z-axis.
		 */
		public final int z;
		
		private DirectionalOffset(int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}
