package net.zarathul.simplefluidtanks.common;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.EnumFacing;

/**
 * Provides constants and helper methods dealing with vanilla directions.
 */
public class Direction
{
	/**
	 * Maps vanilla minecraft direction values to bitflags.
	 */
	public static final ImmutableMap<EnumFacing, Integer> sidesToBitFlagsMappings = new ImmutableMap.Builder()
			.put(EnumFacing.DOWN, 1)
			.put(EnumFacing.UP, 2)
			.put(EnumFacing.NORTH, 4)
			.put(EnumFacing.SOUTH, 8)
			.put(EnumFacing.WEST, 16)
			.put(EnumFacing.EAST, 32)
			.build();
}
