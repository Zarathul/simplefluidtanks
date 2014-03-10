package net.zarathul.simplefluidtanks.common;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents the coordinates of a block in the world.
 */
public class BlockCoords
{
	/**
	 * The x-coordinate.
	 */
	public int x;
	
	/**
	 * The y-coordinate.
	 */
	public int y;
	
	/**
	 * The z-coordinate.
	 */
	public int z;
	
	/**
	 * Default constructor
	 */
	public BlockCoords()
	{
	}
	
	/**
	 * Creates a new instance with the supplied coordinates.
	 * @param x
	 * The x-coordinate.
	 * @param y
	 * The y-coordinate.
	 * @param z
	 * The z-coordinate.
	 */
	public BlockCoords(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Creates a copy of the given {@link BlockCoords}.
	 * @param coords
	 * The {@link BlockCoords} to copy.
	 */
	public BlockCoords(BlockCoords coords)
	{
		if (coords != null)
		{
			x = coords.x;
			y = coords.y;
			z = coords.z;
		}
	}
	
	/**
	 * Copies and offsets each supplied {@link BlockCoords} instance by the supplied offsets.
	 * @param blocks
	 * The coordinates to offset.
	 * @param offsets
	 * The values by which the coordinates should be offset.
	 * @return
	 * An {@link ArrayList} containing the offset copies of the supplied {@link BlockCoords}.
	 */
	public static ArrayList<BlockCoords> cloneWithOffset(Collection<BlockCoords> blocks, int ... offsets)
	{
		if (blocks == null)
		{
			return null;
		}
		
		ArrayList<BlockCoords> offsetBlocks = new ArrayList<BlockCoords>(blocks.size());
		
		for (BlockCoords block : blocks)
		{
			offsetBlocks.add(block.cloneWithOffset(offsets));
		}
		
		return offsetBlocks;
	}
	
	/**
	 * Offsets the coordinates of the current instance by the supplied values.
	 * @param offsets
	 * The values by which the coordinates should be offset.
	 * @return
	 * The current instance after the changes.
	 */
	public BlockCoords offset(int ... offsets)
	{
		if (offsets == null || offsets.length < 1 || offsets.length > 3)
		{
			return this;
		}
		
		x += offsets[0];
		y += (offsets.length > 1) ? offsets[1] : 0;
		z += (offsets.length > 2) ? offsets[2] : 0;
		
		return this;
	}
	
	/**
	 * Creates a copy of the current instance offset by the supplied values.
	 * @param offsets
	 * The values by which the coordinates should be offset.
	 * @return
	 * The offset copy of the current instance.
	 */
	public BlockCoords cloneWithOffset(int ... offsets)
	{
		BlockCoords newCoords = new BlockCoords(this);
		
		return newCoords.offset(offsets);
	}
	
	/**
	 * Gets the distance from the current instance to the supplied coordinates.
	 * @param block
	 * The coordinates to get the distance to.
	 * @return
	 * The distance from the current instance to <code>block</code>.<br> or<br><ode>-1</code> if <code>block</code> was <code>null</code>. 
	 */
	public int getDistanceTo(BlockCoords block)
	{
		if (block == null)
		{
			return -1;
		}
		
		int distance = Math.abs(x - block.x) + Math.abs(y - block.y) + Math.abs(z - block.z);
		
		return distance;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		
		return result;
	}
	
	public boolean equals(int x, int y, int z)
	{
		return (this.x == x && this.y == y && this.z == z);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BlockCoords))
			return false;
		BlockCoords other = (BlockCoords) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		
		return true;
	}

	@Override
	public String toString()
	{
		return "[x=" + x + ", y=" + y + ", z=" + z + "]";
	}
}
