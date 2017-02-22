package net.zarathul.simplefluidtanks.common;

import java.util.ArrayList;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.rendering.BakedTankModel;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * General utility class.
 */
public final class Utils
{
	/**
	 * Gets the {@link TileEntity} at the specified coordinates, cast to the specified type.
	 * 
	 * @param access
	 * An {@link IBlockAccess} implementation. Usually the world.
	 * @param entityType
	 * The type the {@link TileEntity} should be cast to.
	 * @param coords
	 * The coordinates of the {@link TileEntity}.
	 * @return The cast {@link TileEntity} or <code>null</code> if no {@link TileEntity} was found or the types didn't match.
	 */
	public static final <T extends TileEntity> T getTileEntityAt(IBlockAccess access, Class<T> entityType, BlockPos coords)
	{
		if (access != null && entityType != null && coords != null)
		{
			TileEntity entity = access.getTileEntity(coords);

			if (entity != null && entity.getClass() == entityType)
			{
				return (T) entity;
			}
		}

		return null;
	}
	
	/**
	 * Triggers synchronization of TileEntity data with the client. Also causes
	 * rerender of the block but does not not trigger a block update.
	 * 
	 * @param world
	 * The world.
	 * @param pos
	 * The location of the block.
	 */
	public static final void syncBlockAndRerender(World world, BlockPos pos)
	{
		if (world == null || pos == null) return;
		
		IBlockState state = world.getBlockState(pos);
		
		world.markAndNotifyBlock(pos, null, state, state, 2);
	}

	/**
	 * Gets the {@link ValveBlock}s {@link TileEntity} for a linked {@link TankBlock}.
	 * 
	 * @return The valves {@link ValveBlockEntity}<br>
	 * or<br>
	 * <code>null</code> if no linked {@link ValveBlock} was found.
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link TankBlock}s coordinates.
	 */
	public static ValveBlockEntity getValve(IBlockAccess world, BlockPos pos)
	{
		if (world != null && pos != null)
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, pos);

			if (tankEntity != null)
			{
				ValveBlockEntity valveEntity = tankEntity.getValve();

				return valveEntity;
			}
		}

		return null;
	}

	/**
	 * A predicate that returns <code>true</code> if passed string is neither <code>null</code> nor empty.
	 */
	private static final Predicate<String> stringNotNullOrEmpty = new Predicate<String>()
	{
		@Override
		public boolean apply(String item)
		{
			return !Strings.isNullOrEmpty(item);
		}
	};

	/**
	 * Checks a list of strings for <code>null</code> and empty elements.
	 * 
	 * @param items
	 * The list of strings to check.
	 * @return
	 * <code>true</code> if the list neither contains <code>null</code> elements nor empty strings, otherwise <code>false</code>.
	 */
	public static final boolean notNullorEmpty(Iterable<String> items)
	{
		return Iterables.all(items, stringNotNullOrEmpty);
	}

	/**
	 * Gets the localized formatted strings for the specified key and formatting arguments.
	 * 
	 * @param key
	 * The base key without an index (e.g. "myKey" gets "myKey0", "myKey1" ... etc.).
	 * @param args
	 * Formatting arguments.
	 * @return
	 */
	public static final ArrayList<String> multiLineTranslateToLocal(String key, Object... args)
	{
		ArrayList<String> lines = new ArrayList<String>();

		if (key != null)
		{
			int x = 0;
			String currentKey = key + x;

			while (I18n.canTranslate(currentKey))
			{
				lines.add(I18n.translateToLocalFormatted(currentKey, args));
				currentKey = key + ++x;
			}
		}

		return lines;
	}
	
	/**
	 * Calculates the fluid level for the specified fill percentage.
	 * 
	 * @param percentage
	 * The fill percentage.
	 * @return
	 * A value between 0 and {@code TankModelFactory.FLUID_LEVELS}.
	 */
	public static int getFluidLevel(int fillPercentage)
	{
		int level = (int)Math.round((fillPercentage / 100.0d) * BakedTankModel.FLUID_LEVELS);
		
		// Make sure that even for small amounts the fluid is rendered at the first level.
		return (fillPercentage > 0) ? Math.max(1, level) : 0;
	}
	
	/**
	 * Calculates the comparator redstone signal strength based on the quotient of the specified values.
	 * 
	 * @param numerator
	 * The numerator.
	 * @param denominator
	 * The denominator.
	 * @return
	 * A value between 0 and 15.
	 */
	public static int getComparatorLevel(float numerator, float denominator)
	{
		int level = (denominator != 0) ? ((int) Math.floor((numerator / denominator) * 14.0f)) + ((numerator > 0) ? 1 : 0) : 0;
		
		return level;
	}
}
