package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.util.EnumFacing;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.common.Direction;

/**
 * Provides helper methods to get texture indexes for connected {@link TankBlock}s.
 */
public final class ConnectedTexturesHelper
{
	/**
	 * Get the icon index for the positive x side of a {@link TankBlock} with the specified connections.
	 * 
	 * @param connections
	 * The sides the {@link TankBlock} is connected to other blocks of it's kind.
	 * @return The icon index.
	 */
	public static int getPositiveXTexture(boolean[] connections)
	{
		int textureIndex = 0;

		if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 1;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 8;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 11;
		}
		else if (connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 10;
		}
		else if (connections[EnumFacing.NORTH.getIndex()] && connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 9;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 2;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 3;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.SOUTH.getIndex()])
		{
			textureIndex = 7;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 6;
		}
		else if (connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 5;
		}
		else if (connections[EnumFacing.NORTH.getIndex()] && connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 4;
		}
		else if (connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 12;
		}
		else if (connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 13;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()])
		{
			textureIndex = 14;
		}
		else if (connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 15;
		}

		return textureIndex;
	}

	/**
	 * Get the icon index for the negative x side of a {@link TankBlock} with the specified connections.
	 * 
	 * @param connections
	 * The sides the {@link TankBlock} is connected to other blocks of it's kind.
	 * @return The icon index.
	 */
	public static int getNegativeXTexture(boolean[] connections)
	{
		int textureIndex = 0;

		if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 1;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 8;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 9;
		}
		else if (connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 10;
		}
		else if (connections[EnumFacing.NORTH.getIndex()] && connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 11;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 2;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 3;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.SOUTH.getIndex()])
		{
			textureIndex = 4;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 5;
		}
		else if (connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 6;
		}
		else if (connections[EnumFacing.NORTH.getIndex()] && connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 7;
		}
		else if (connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 12;
		}
		else if (connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 13;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()])
		{
			textureIndex = 15;
		}
		else if (connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 14;
		}

		return textureIndex;
	}

	/**
	 * Get the icon index for the positive z side of a {@link TankBlock} with the specified connections.
	 * 
	 * @param connections
	 * The sides the {@link TankBlock} is connected to other blocks of it's kind.
	 * @return The icon index.
	 */
	public static int getPositiveZTexture(boolean[] connections)
	{
		int textureIndex = 0;

		if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 1;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 8;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 9;
		}
		else if (connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 10;
		}
		else if (connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 11;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 2;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 3;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.EAST.getIndex()])
		{
			textureIndex = 4;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 5;
		}
		else if (connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 6;
		}
		else if (connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 7;
		}
		else if (connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 12;
		}
		else if (connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 13;
		}
		else if (connections[EnumFacing.EAST.getIndex()])
		{
			textureIndex = 15;
		}
		else if (connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 14;
		}

		return textureIndex;
	}

	/**
	 * Get the icon index for the negative z side of a {@link TankBlock} with the specified connections.
	 * 
	 * @param connections
	 * The sides the {@link TankBlock} is connected to other blocks of it's kind.
	 * @return The icon index.
	 */
	public static int getNegativeZTexture(boolean[] connections)
	{
		int textureIndex = 0;

		if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 1;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 8;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 11;
		}
		else if (connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 10;
		}
		else if (connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 9;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 2;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 3;
		}
		else if (connections[EnumFacing.UP.getIndex()] && connections[EnumFacing.EAST.getIndex()])
		{
			textureIndex = 7;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 6;
		}
		else if (connections[EnumFacing.DOWN.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 5;
		}
		else if (connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 4;
		}
		else if (connections[EnumFacing.UP.getIndex()])
		{
			textureIndex = 12;
		}
		else if (connections[EnumFacing.DOWN.getIndex()])
		{
			textureIndex = 13;
		}
		else if (connections[EnumFacing.EAST.getIndex()])
		{
			textureIndex = 14;
		}
		else if (connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 15;
		}

		return textureIndex;
	}

	/**
	 * Get the icon index for the positive y side of a {@link TankBlock} with the specified connections.
	 * 
	 * @param connections
	 * The sides the {@link TankBlock} is connected to other blocks of it's kind.
	 * @return The icon index.
	 */
	public static int getPositiveYTexture(boolean[] connections)
	{
		int textureIndex = 0;

		if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 1;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 11;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 8;
		}
		else if (connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 9;
		}
		else if (connections[EnumFacing.NORTH.getIndex()] && connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.EAST.getIndex()])
		{
			textureIndex = 10;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 3;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 2;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.SOUTH.getIndex()])
		{
			textureIndex = 7;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 4;
		}
		else if (connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 5;
		}
		else if (connections[EnumFacing.NORTH.getIndex()] && connections[EnumFacing.EAST.getIndex()])
		{
			textureIndex = 6;
		}
		else if (connections[EnumFacing.EAST.getIndex()])
		{
			textureIndex = 14;
		}
		else if (connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 15;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()])
		{
			textureIndex = 12;
		}
		else if (connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 13;
		}

		return textureIndex;
	}

	/**
	 * Get the icon index for the negative y side of a {@link TankBlock} with the specified connections.
	 * 
	 * @param connections
	 * The sides the {@link TankBlock} is connected to other blocks of it's kind.
	 * @return The icon index.
	 */
	public static int getNegativeYTexture(boolean[] connections)
	{
		int textureIndex = 0;

		if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 1;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 11;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 10;
		}
		else if (connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 9;
		}
		else if (connections[EnumFacing.NORTH.getIndex()] && connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.EAST.getIndex()])
		{
			textureIndex = 8;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 3;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 2;
		}
		else if (connections[EnumFacing.EAST.getIndex()] && connections[EnumFacing.SOUTH.getIndex()])
		{
			textureIndex = 6;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()] && connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 5;
		}
		else if (connections[EnumFacing.WEST.getIndex()] && connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 4;
		}
		else if (connections[EnumFacing.NORTH.getIndex()] && connections[EnumFacing.EAST.getIndex()])
		{
			textureIndex = 7;
		}
		else if (connections[EnumFacing.EAST.getIndex()])
		{
			textureIndex = 14;
		}
		else if (connections[EnumFacing.WEST.getIndex()])
		{
			textureIndex = 15;
		}
		else if (connections[EnumFacing.SOUTH.getIndex()])
		{
			textureIndex = 13;
		}
		else if (connections[EnumFacing.NORTH.getIndex()])
		{
			textureIndex = 12;
		}

		return textureIndex;
	}
}
