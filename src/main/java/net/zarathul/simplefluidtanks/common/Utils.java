package net.zarathul.simplefluidtanks.common;

import java.util.ArrayList;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.rendering.TankModelFactory;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * General utility class.
 */
public final class Utils
{
	public static final ItemStack FILLED_BOTTLE = new ItemStack(Items.potionitem);

	/**
	 * Chances that other mods register fluid containers while the game is already running are low. So we cache the container data.
	 */
	private static FluidContainerData[] cachedFluidContainerData = null;

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

			while (StatCollector.canTranslate(currentKey))
			{
				lines.add(StatCollector.translateToLocalFormatted(currentKey, args));
				currentKey = key + ++x;
			}
		}

		return lines;
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

			return (containerFluid == null || containerFluid.amount == 0);
		}

		return false;
	}

	/**
	 * Wrapper method for FluidContainerRegistry.getContainerCapacity that overrides the default bottle volume if configured.
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

		// override default bottle volume
		if (Config.overrideBottleVolume > 0 && (container.isItemEqual(FluidContainerRegistry.EMPTY_BOTTLE) || container.isItemEqual(FILLED_BOTTLE))) return Config.overrideBottleVolume;

		return FluidContainerRegistry.getContainerCapacity(fluid, container);
	}

	/**
	 * Wrapper method for FluidContainerRegistry.fillFluidContainer that overrides the default bottle volume if configured.
	 * 
	 * @param fluid
	 * FluidStack containing the type and amount of fluid to fill.
	 * @param container
	 * ItemStack representing the empty container.
	 * @return
	 * Filled container if successful, otherwise null.
	 */
	public static ItemStack fillFluidContainer(FluidStack fluid, ItemStack container)
	{
		if (container == null || fluid == null) return null;

		// override default bottle volume
		if (Config.overrideBottleVolume > 0 && container.isItemEqual(FluidContainerRegistry.EMPTY_BOTTLE))
		{
			if (cachedFluidContainerData == null)
			{
				// cache container data if we haven't done it already
				cachedFluidContainerData = FluidContainerRegistry.getRegisteredFluidContainerData();
			}

			for (FluidContainerData data : cachedFluidContainerData)
			{
				if (container.isItemEqual(data.emptyContainer) && fluid.isFluidEqual(data.fluid) && fluid.amount >= Config.overrideBottleVolume)
				{
					return data.filledContainer.copy();
				}
			}
		}

		return FluidContainerRegistry.fillFluidContainer(fluid, container);
	}

	/**
	 * Wrapper method for FluidContainerRegistry.getFluidForFilledItem that overrides the default bottle volume.
	 * 
	 * @param filledContainer
	 * The fluid container.
	 * @return
	 * FluidStack representing stored fluid.
	 */
	public static FluidStack getFluidForFilledItem(ItemStack filledContainer)
	{
		if (filledContainer == null) return null;

		FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(filledContainer);

		// override default bottle volume
		if (fluid != null && Config.overrideBottleVolume > 0 && filledContainer.isItemEqual(FILLED_BOTTLE))
		{
			fluid.amount = Config.overrideBottleVolume;
		}

		return fluid;
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
		int level = (int)Math.round((fillPercentage / 100.0d) * TankModelFactory.FLUID_LEVELS);
		
		// Make sure that even for small amounts the fluid is rendered at the first level.
		return (fillPercentage > 0) ? Math.max(1, level) : 0;
	}
}
