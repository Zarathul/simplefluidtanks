package net.zarathul.simplefluidtanks.common;

import java.util.ArrayList;
import java.util.Collection;

import net.zarathul.simplefluidtanks.common.Direction.DirectionalOffset;

/**
 * Represents the coordinates of a block in the world.
 */
public class BlockCoords
{
	/**
	 * The x-coordinate.
	 */
	public final int x;

	/**
	 * The y-coordinate.
	 */
	public final int y;

	/**
	 * The z-coordinate.
	 */
	public final int z;

	/**
	 * Creates a new instance with the specified coordinates.
	 * 
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
	 * Copies and offsets all {@link BlockCoords} instances in the specified collection by the specified offsets.
	 * 
	 * @param blocks
	 * The coordinates to offset.
	 * @param offsets
	 * The values by which the coordinates should be offset.
	 * @return An {@link ArrayList} containing the offset copies of the {@link BlockCoords}.
	 */
	public static ArrayList<BlockCoords> offsetBy(Collection<BlockCoords> blocks, int... offsets)
	{
		if (blocks == null || offsets == null || offsets.length < 3) throw new IllegalArgumentException();

		ArrayList<BlockCoords> offsetBlocks = new ArrayList<BlockCoords>(blocks.size());

		for (BlockCoords block : blocks)
		{
			offsetBlocks.add(new BlockCoords(block.x + offsets[0], block.y + offsets[1], block.z + offsets[2]));
		}

		return offsetBlocks;
	}

	/**
	 * Creates a {@link BlockCoords} instance from the specified coordinates, offset in the specified direction by the specified amount.
	 * 
	 * @param direction
	 * The direction the offset is relative to.
	 * @param amount
	 * The amount by which the coordinates should be offset.
	 * @param coords
	 * The coordinates.
	 * @return The offset {@link BlockCoords} instance.
	 */
	public static BlockCoords offset(int direction, int amount, int... coords)
	{
		if (coords == null || coords.length < 3 || !Direction.vanillaSideOffsets.containsKey(direction)) throw new IllegalArgumentException();

		DirectionalOffset offset = Direction.vanillaSideOffsets.get(direction);

		return new BlockCoords(coords[0] + offset.x * amount, coords[1] + offset.y * amount, coords[2] + offset.z * amount);
	}

	/**
	 * Creates a copy of the current instance offset by 1, in the specified direction.
	 * 
	 * @param direction
	 * The direction the offset is relative to.
	 * @return The offset copy of the current instance.
	 */
	public BlockCoords offset(int direction)
	{
		return offset(direction, 1);
	}

	/**
	 * Creates a copy of the current instance offset by the specified amount, in the specified direction.
	 * 
	 * @param direction
	 * The direction the offset is relative to.
	 * @param amount
	 * The amount by which the coordinates should be offset.
	 * @return The offset copy of the current instance.
	 */
	public BlockCoords offset(int direction, int amount)
	{
		return offset(direction, amount, x, y, z);
	}

	/**
	 * Creates a copy of the current instance offset by the specified values.
	 * 
	 * @param offsets
	 * The values by which the coordinates should be offset.
	 * @return The offset copy of the current instance.
	 */
	public BlockCoords offsetBy(int... offsets)
	{
		if (offsets == null || offsets.length < 3) throw new IllegalArgumentException();

		return new BlockCoords(x + offsets[0], y + offsets[1], z + offsets[2]);
	}

	/**
	 * Gets the distance from the current instance to the specified coordinates.
	 * 
	 * @param block
	 * The coordinates to get the distance to.
	 * @return The distance from the current instance to <code>block</code>.<br>
	 * or<br>
	 * <ode>-1</code> if <code>block</code> was <code>null</code>.
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
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof BlockCoords)) return false;
		BlockCoords other = (BlockCoords) obj;
		if (x != other.x) return false;
		if (y != other.y) return false;
		if (z != other.z) return false;

		return true;
	}

	@Override
	public String toString()
	{
		return "[x=" + x + ", y=" + y + ", z=" + z + "]";
	}
}
