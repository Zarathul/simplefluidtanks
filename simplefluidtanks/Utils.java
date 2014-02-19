package simplefluidtanks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import com.google.common.collect.Multimap;

import paulscode.sound.Vector3D;

import net.minecraft.launchwrapper.LogWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;


public final class Utils
{
	public static final <T extends TileEntity> T getTileEntityAt(IBlockAccess access, Class<T> entityType, int ... coords)
	{
		return getTileEntityAt(access, entityType, true, coords);
	}
	
	public static final <T extends TileEntity> T getTileEntityAt(IBlockAccess access, Class<T> entityType, BlockCoords coords)
	{
		return getTileEntityAt(access, entityType, true, coords.x, coords.y, coords.z);
	}
	
	public static final <T extends TileEntity> T getTileEntityAt(IBlockAccess access, Class<T> entityType, boolean suppressLogEntry, int ... coords)
	{
		if (access != null && entityType != null && coords != null && coords.length == 3)
		{
			TileEntity entity = access.getBlockTileEntity(coords[0], coords[1], coords[2]);
			
			if (entity == null || entity.getClass() != entityType)
			{
				if (!suppressLogEntry)
				{
					LogWrapper.severe("[%s] %s missing at x:%d / y:%d / z:%d.", SimpleFluidTanks.MOD_ID, entityType.getSimpleName(), coords[0], coords[1], coords[2]);
				}
				
				return null;
			}
			
			return (T)entity;
		}
		
		return null;
	}
}
