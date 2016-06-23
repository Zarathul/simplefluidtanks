package net.zarathul.simplefluidtanks.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Provides Waila with tooltip information for {@link TankBlock}s.
 */
public final class TankBlockDataProvider implements IWailaDataProvider
{
	public static final TankBlockDataProvider instance = new TankBlockDataProvider();

	private TankBlockDataProvider()
	{
	}

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		TileEntity entity = accessor.getTileEntity();

		if (entity != null && entity instanceof TankBlockEntity)
		{
			TankBlockEntity tankEntity = (TankBlockEntity) entity;

			if (config.getConfig(Registry.WAILA_TANK_LINKED_KEY))
			{
				String readableFlag = StatCollector.translateToLocal((tankEntity.isPartOfTank()) ? Registry.WAILA_TOOLTIP_YES : Registry.WAILA_TOOLTIP_NO);
				currenttip.add(StatCollector.translateToLocalFormatted(Registry.WAILA_TOOLTIP_ISLINKED, readableFlag));
			}

			if (config.getConfig(Registry.WAILA_TANK_TOTAL_CAPACITY_KEY) && tankEntity.isPartOfTank())
			{
				ValveBlockEntity valveEntity = tankEntity.getValve();
				if (valveEntity != null) 
				{
					int amount = valveEntity.getFluidAmount();
					int capacity = valveEntity.getCapacity();
					int totalFillPercentage = (capacity > 0) ? MathHelper.clamp_int((int) ((long) amount * 100 / capacity), 0, 100) : 0;
	
					if (config.getConfig(Registry.WAILA_CAPACITY_IN_MILLIBUCKETS_KEY))
					{
						currenttip.add(StatCollector.translateToLocalFormatted(Registry.WAILA_TOOLTIP_VALVE_CAPACITY, amount, capacity, "mB", totalFillPercentage));
					}
					else
					{
						currenttip.add(StatCollector.translateToLocalFormatted(Registry.WAILA_TOOLTIP_VALVE_CAPACITY, amount / 1000, capacity / 1000, "B", totalFillPercentage));
					}
				}
			} else if (config.getConfig(Registry.WAILA_TANK_CAPACITY_KEY))
			{
				if (config.getConfig(Registry.WAILA_CAPACITY_IN_MILLIBUCKETS_KEY))
				{
					currenttip.add(StatCollector.translateToLocalFormatted(Registry.WAILA_TOOLTIP_TANK_CAPACITY, Config.bucketsPerTank * 1000, "mB", tankEntity.getFillPercentage()));
				}
				else
				{
					currenttip.add(StatCollector.translateToLocalFormatted(Registry.WAILA_TOOLTIP_TANK_CAPACITY, Config.bucketsPerTank, "B", tankEntity.getFillPercentage()));
				}
			}

			if (config.getConfig(Registry.WAILA_FLUID_NAME_KEY) && tankEntity.isPartOfTank())
			{
				ValveBlockEntity valveEntity = tankEntity.getValve();
				if (valveEntity != null) 
				{
					String fluidName = valveEntity.getLocalizedFluidName();
	
					if (fluidName == null)
					{
						fluidName = StatCollector.translateToLocal(Registry.WAILA_TOOLTIP_FLUID_EMPTY);
					}
					currenttip.add(StatCollector.translateToLocalFormatted(Registry.WAILA_TOOLTIP_FLUID, fluidName));
				}
			}
		
		}

		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}
}
