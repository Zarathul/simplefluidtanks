package net.zarathul.simplefluidtanks.common;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

/**
 * General utility class.
 */
public final class Utils
{
	/**
	 * Gets the {@link TileEntity} at the specified coordinates, casted to the specified type.
	 * 
	 * @param access
	 * An {@link IBlockAccess} implementation. Usually the world.
	 * @param entityType
	 * The type the {@link TileEntity} should be casted to.
	 * @param coords
	 * The coordinates of the {@link TileEntity}.
	 * @return The casted {@link TileEntity} or <code>null</code> if no {@link TileEntity} was found or the casting failed.
	 */
	public static final <T extends TileEntity> T getTileEntityAt(IBlockAccess access, Class<T> entityType, BlockCoords coords)
	{
		return getTileEntityAt(access, entityType, coords.x, coords.y, coords.z);
	}

	/**
	 * Gets the {@link TileEntity} at the specified coordinates, casted to the specified type.
	 * 
	 * @param access
	 * An {@link IBlockAccess} implementation. Usually the world.
	 * @param entityType
	 * The type the {@link TileEntity} should be casted to.
	 * @param coords
	 * The coordinates of the {@link TileEntity}.
	 * @return The casted {@link TileEntity} or <code>null</code> if no {@link TileEntity} was found or the casting failed.
	 */
	public static final <T extends TileEntity> T getTileEntityAt(IBlockAccess access, Class<T> entityType, int... coords)
	{
		if (access != null && entityType != null && coords != null && coords.length == 3)
		{
			TileEntity entity = access.getTileEntity(coords[0], coords[1], coords[2]);

			if (entity != null && entity.getClass() == entityType)
			{
				return (T) entity;
			}
		}

		return null;
	}

	/**
	 * Check if the block at the specified location is of the specified type.
	 * 
	 * @param access
	 * An {@link IBlockAccess} implementation. Usually the world.
	 * @param type
	 * The type the block should be checked against.
	 * @param coords
	 * The blocks coordinates.
	 * @return <code>true</code> if the block at the specified coordinates is of the specified type, otherwise <code>false</code>.
	 */
	public static final <T extends Block> boolean isBlockType(IBlockAccess access, Class<T> type, BlockCoords coords)
	{
		return isBlockType(access, type, coords.x, coords.y, coords.z);
	}

	/**
	 * Check if the block at the specified location is of the specified type.
	 * 
	 * @param access
	 * An {@link IBlockAccess} implementation. Usually the world.
	 * @param type
	 * The type the block should be checked against.
	 * @param x
	 * The blocks x-coordinate.
	 * @param y
	 * The blocks y-coordinate.
	 * @param z
	 * The blocks z-coordinate.
	 * @return <code>true</code> if the block at the specified coordinates is of the specified type, otherwise <code>false</code>.
	 */
	public static final <T extends Block> boolean isBlockType(IBlockAccess access, Class<T> type, int x, int y, int z)
	{
		Block blockToCheck = access.getBlock(x, y, z);

		return ((blockToCheck != null) && (type.isInstance(blockToCheck)));
	}
}
