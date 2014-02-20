package simplefluidtanks;

import java.util.EnumSet;


/**
 * Describes which blocks should be included in a search.
 */
public enum BlockSearchMode
{
	/**
	 * Include blocks in Y+ direction.
	 */
	Above,
	
	/**
	 * Include blocks in Y- direction.
	 */
	Below,
	
	/**
	 * Include blocks in X+,X-,Z+,Z- directions.
	 */
	SameLevel;
	
	public static final EnumSet<BlockSearchMode> All = EnumSet.allOf(BlockSearchMode.class);
	public static final EnumSet<BlockSearchMode> SameLevelAndAbove = EnumSet.of(BlockSearchMode.Above, BlockSearchMode.SameLevel);
	public static final EnumSet<BlockSearchMode> SameLevelAndBelow = EnumSet.of(BlockSearchMode.Below, BlockSearchMode.SameLevel);
	public static final EnumSet<BlockSearchMode> AboveAndBelow = EnumSet.of(BlockSearchMode.Above, BlockSearchMode.Below);
	public static final EnumSet<BlockSearchMode> AboveOnly = EnumSet.of(BlockSearchMode.Above);
	public static final EnumSet<BlockSearchMode> BelowOnly = EnumSet.of(BlockSearchMode.Below);
}
