package net.zarathul.simplefluidtanks.common;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;


/**
 * General utility class.
 */
public final class Utils
{
	/**
	 * Gets the {@link TileEntity} at the specified coordinates, casted to the specified type.
	 * @param access
	 * A {@link IBlockAccess} implementation. Usually the world.
	 * @param entityType
	 * The type the {@link TileEntity} should be casted to.
	 * @param coords
	 * The coordinates of the {@link TileEntity}.
	 * @return
	 * The casted {@link TileEntity} or <code>null</code> if no {@link TileEntity} was found or the casting failed.
	 */
	public static final <T extends TileEntity> T getTileEntityAt(IBlockAccess access, Class<T> entityType, BlockCoords coords)
	{
		return getTileEntityAt(access, entityType, coords.x, coords.y, coords.z);
	}
	
	/**
	 * Gets the {@link TileEntity} at the specified coordinates, casted to the specified type.
	 * @param access
	 * A {@link IBlockAccess} implementation. Usually the world.
	 * @param entityType
	 * The type the {@link TileEntity} should be casted to.
	 * @param coords
	 * The coordinates of the {@link TileEntity}.
	 * @return
	 * The casted {@link TileEntity} or <code>null</code> if no {@link TileEntity} was found or the casting failed.
	 */
	public static final <T extends TileEntity> T getTileEntityAt(IBlockAccess access, Class<T> entityType, int ... coords)
	{
		if (access != null && entityType != null && coords != null && coords.length == 3)
		{
			TileEntity entity = access.getBlockTileEntity(coords[0], coords[1], coords[2]);
			
			if (entity != null && entity.getClass() == entityType)
			{
				return (T)entity;
			}
		}
		
		return null;
	}
}
