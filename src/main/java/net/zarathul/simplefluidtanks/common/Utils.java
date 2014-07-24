package net.zarathul.simplefluidtanks.common;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

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

			while (StatCollector.canTranslate(currentKey))
			{
				lines.add(StatCollector.translateToLocalFormatted(currentKey, args));
				currentKey = key + ++x;
			}
		}

		return lines;
	}

	/**
	 * Gets the capacity for a registered fluid container.
	 * 
	 * @param fluid
	 * The fluid the container can hold.
	 * @param container
	 * The container.
	 * @return
	 * The containers capacity or 0 if the container could not be found.
	 */
	public static final int getFluidContainerCapacity(FluidStack fluid, ItemStack container)
	{
		if (fluid == null || container == null) return 0;

		Item containerItem = container.getItem();

		FluidContainerData[] containerData = FluidContainerRegistry.getRegisteredFluidContainerData();

		if (containerData != null)
		{
			for (FluidContainerData data : containerData)
			{
				if (((data.emptyContainer != null && data.emptyContainer.getItem() == containerItem) || (data.filledContainer != null && data.filledContainer.getItem() == containerItem)) && fluid.isFluidEqual(data.fluid))
				{
					return data.fluid.amount;
				}
			}
		}

		return 0;
	}

	/**
	 * Gets the empty container for a filled one.
	 * 
	 * @param filledContainer
	 * The filled container.
	 * @return
	 * The empty container or null if no empty container could be found.
	 */
	public static final ItemStack getEmptyFluidContainer(ItemStack filledContainer)
	{
		if (filledContainer == null) return null;

		FluidStack containerFluid = FluidContainerRegistry.getFluidForFilledItem(filledContainer);

		Item containerItem = filledContainer.getItem();

		FluidContainerData[] containerData = FluidContainerRegistry.getRegisteredFluidContainerData();

		if (containerData != null)
		{
			for (FluidContainerData data : containerData)
			{
				if ((data.filledContainer != null && data.filledContainer.getItem() == containerItem) && containerFluid.isFluidEqual(data.fluid))
				{
					return data.emptyContainer.copy();
				}
			}
		}

		return null;
	}

	/**
	 * Checks if an item is a container that implements {@code IFluidContainerItem} and is empty.
	 * 
	 * @param item
	 * The container to check.
	 * @return
	 * {@code true} if the container is empty and implements {@code IFluidContainerItem}, otherwise {@code false}.
	 */
	public static final boolean isEmptyComplexContainer(ItemStack item)
	{
		if (item == null) return false;

		if (item.getItem() instanceof IFluidContainerItem)
		{
			IFluidContainerItem container = (IFluidContainerItem) item.getItem();
			FluidStack containerFluid = container.getFluid(item);

			return (containerFluid == null || (containerFluid != null && containerFluid.amount == 0));
		}

		return false;
	}
}
