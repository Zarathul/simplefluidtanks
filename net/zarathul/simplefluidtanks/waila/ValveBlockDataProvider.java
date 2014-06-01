package net.zarathul.simplefluidtanks.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

public final class ValveBlockDataProvider implements IWailaDataProvider
{
	public static final ValveBlockDataProvider instance = new ValveBlockDataProvider();
	
	private ValveBlockDataProvider()
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
		
		if (entity != null && entity instanceof ValveBlockEntity)
		{
			ValveBlockEntity valveEntity = (ValveBlockEntity)entity;
			
			if (config.getConfig(WailaRegistrar.WAILA_TANK_COUNT_KEY))
			{
				currenttip.add(StatCollector.translateToLocalFormatted(WailaRegistrar.WAILA_TOOLTIP_TANKS, valveEntity.getLinkedTankCount()));
			}

			if (config.getConfig(WailaRegistrar.WAILA_TOTAL_CAPACITY_KEY))
			{
				int totalFillPercentage = MathHelper.clamp_int(valveEntity.getFluidAmount() * (100 / valveEntity.getCapacity()), 0, 100);
				
				if (config.getConfig(WailaRegistrar.WAILA_CAPACITY_IN_MILLIBUCKETS_KEY))
				{
					currenttip.add(StatCollector.translateToLocalFormatted(WailaRegistrar.WAILA_TOOLTIP_VALVE_CAPACITY, valveEntity.getFluidAmount(), valveEntity.getCapacity(), "mB", totalFillPercentage));
				}
				else
				{
					currenttip.add(StatCollector.translateToLocalFormatted(WailaRegistrar.WAILA_TOOLTIP_VALVE_CAPACITY, valveEntity.getFluidAmount() / 1000, valveEntity.getCapacity() / 1000, "B", totalFillPercentage));
				}
			}
			
			if (config.getConfig(WailaRegistrar.WAILA_FLUID_NAME_KEY))
			{
				String fluidName = StatCollector.translateToLocal(WailaRegistrar.WAILA_TOOLTIP_FLUID_EMPTY);
				FluidStack fluidStack = valveEntity.getFluid();
				
				if (fluidStack != null)
				{
					Fluid fluid = fluidStack.getFluid();
					
					if (fluid != null)
					{
						fluidName = fluid.getLocalizedName();
					}
				}
				
				currenttip.add(StatCollector.translateToLocalFormatted(WailaRegistrar.WAILA_TOOLTIP_FLUID, fluidName));
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
