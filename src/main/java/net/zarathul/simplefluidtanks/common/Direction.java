package net.zarathul.simplefluidtanks.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;

/**
 * Provides constants and helper methods to convert vanilla directions into {@link ForgeDirection}s and vice versa.
 */
public class Direction
{
	/**
	 * Maps bitflags to vanilla minecraft direction values.
	 */
	public static final ImmutableList<Integer> sidesToBitFlagsMappings = ImmutableList.of(
		1, 	// 0 YNEG
		2, 	// 1 YPOS
		4, 	// 2 ZNEG
		8, 	// 3 ZPOS
		16,	// 4 XNEG
		32	// 5 XPOS
	);
}
