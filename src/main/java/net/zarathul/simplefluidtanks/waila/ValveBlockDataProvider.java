package net.zarathul.simplefluidtanks.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.zarathul.simplefluidtanks.Registry;
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
			
			currenttip.clear();
			
			if (config.getConfig(Registry.WAILA_TANK_COUNT_KEY))
			{
				currenttip.add(StatCollector.translateToLocalFormatted(Registry.WAILA_TOOLTIP_TANKS, valveEntity.getLinkedTankCount()));
			}

			if (config.getConfig(Registry.WAILA_TOTAL_CAPACITY_KEY))
			{
				if (config.getConfig(Registry.WAILA_CAPACITY_IN_MILLIBUCKETS_KEY))
				{
					currenttip.add(StatCollector.translateToLocalFormatted(Registry.WAILA_TOOLTIP_CAPACITY, valveEntity.getFluidAmount(), "/", valveEntity.getCapacity(), " mB"));
				}
				else
				{
					currenttip.add(StatCollector.translateToLocalFormatted(Registry.WAILA_TOOLTIP_CAPACITY, valveEntity.getFluidAmount() / 1000, "/", valveEntity.getCapacity() / 1000, " B" ));
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
