package net.zarathul.simplefluidtanks.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ImmutableList;

/**
 * Provides constants and helper methods to convert vanilla directions into {@link ForgeDirection}s and vice versa.
 */
public class Direction
{
	public static final int XPOS = 5;
	public static final int XNEG = 4;
	public static final int YPOS = 1;
	public static final int YNEG = 0;
	public static final int ZPOS = 3;
	public static final int ZNEG = 2;
	
	public static final ImmutableList<Integer> sidesToBitFlagsMappings = ImmutableList.of
	(
		1, 	// 0	YNEG
		2, 	// 1	YPOS
		4, 	// 2	ZNEG
		8, 	// 3	ZPOS
		16,	// 4	XNEG
		32	// 5	XPOS
	);
	
	public static final Map<Integer, ForgeDirection> vanillaToForgeMapping = Collections.unmodifiableMap
	(
		new HashMap<Integer, ForgeDirection>()
		{{
			put(YPOS, ForgeDirection.UP);
			put(YNEG, ForgeDirection.DOWN);
			put(XPOS, ForgeDirection.EAST);
			put(XNEG, ForgeDirection.WEST);
			put(ZPOS, ForgeDirection.SOUTH);
			put(ZNEG, ForgeDirection.NORTH);
		}}
	);
	
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
	
	public static int fromForge(ForgeDirection direction)
	{
		if (direction == ForgeDirection.UNKNOWN)
		{
			return -1;
		}
		
		return forgeToVanillaMapping.get(direction);
	}
	
	public static ForgeDirection toForge(int side)
	{
		if (side >= 0 && side <= 5)
		{
			return vanillaToForgeMapping.get(side);
		}
		
		return ForgeDirection.UNKNOWN;
	}
}
