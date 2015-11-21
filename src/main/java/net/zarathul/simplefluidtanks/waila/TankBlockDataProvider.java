package net.zarathul.simplefluidtanks.waila;

import java.util.List;

import mcp.mobius.waila.api.ITaggedList.ITipList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataAccessorServer;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;

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
	public NBTTagCompound getNBTData(TileEntity te, NBTTagCompound tag, IWailaDataAccessorServer accessor)
	{
		return null;
	}

	@Override
	public ITipList getWailaHead(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}

	@Override
	public ITipList getWailaBody(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
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

			if (config.getConfig(Registry.WAILA_TANK_CAPACITY_KEY))
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
		}

		return currenttip;
	}

	@Override
	public ITipList getWailaTail(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}
}