package net.zarathul.simplefluidtanks.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
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
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
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

			if (config.getConfig(Registry.WAILA_TANK_CAPACITY_KEY))
			{
				int fillPercentage = tankEntity.getFillPercentage();
				int amount = (int)((fillPercentage / 100.0d) * Config.bucketsPerTank * 1000);
				
				if (config.getConfig(Registry.WAILA_CAPACITY_IN_MILLIBUCKETS_KEY))
				{
					currenttip.add(StatCollector.translateToLocalFormatted(
							Registry.WAILA_TOOLTIP_TANK_CAPACITY,
							amount,
							Config.bucketsPerTank * 1000,
							"mB",
							fillPercentage));
				}
				else
				{
					currenttip.add(StatCollector.translateToLocalFormatted(
							Registry.WAILA_TOOLTIP_TANK_CAPACITY,
							amount / 1000,
							Config.bucketsPerTank,
							"B",
							fillPercentage));
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