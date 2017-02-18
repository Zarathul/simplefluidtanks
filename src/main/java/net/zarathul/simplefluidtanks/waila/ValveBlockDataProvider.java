package net.zarathul.simplefluidtanks.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Provides Waila with tooltip information for {@link ValveBlock}s.
 */
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

		if (entity != null && entity instanceof ValveBlockEntity)
		{
			ValveBlockEntity valveEntity = (ValveBlockEntity) entity;

			if (config.getConfig(Registry.WAILA_TANK_COUNT_KEY))
			{
				currenttip.add(I18n.translateToLocalFormatted(Registry.WAILA_TOOLTIP_TANKS, valveEntity.getLinkedTankCount()));
			}

			if (config.getConfig(Registry.WAILA_TOTAL_CAPACITY_KEY))
			{
				int amount = valveEntity.getFluidAmount();
				int capacity = valveEntity.getCapacity();
				int totalFillPercentage = (capacity > 0) ? MathHelper.clamp((int) ((long) amount * 100 / capacity), 0, 100) : 0;

				if (config.getConfig(Registry.WAILA_CAPACITY_IN_MILLIBUCKETS_KEY))
				{
					currenttip.add(I18n.translateToLocalFormatted(
							Registry.WAILA_TOOLTIP_VALVE_CAPACITY,
							amount,
							capacity,
							"mB",
							totalFillPercentage));
				}
				else
				{
					currenttip.add(I18n.translateToLocalFormatted(
							Registry.WAILA_TOOLTIP_VALVE_CAPACITY,
							amount / 1000,
							capacity / 1000,
							"B",
							totalFillPercentage));
				}
			}

			if (config.getConfig(Registry.WAILA_FLUID_NAME_KEY))
			{
				String fluidName = valveEntity.getLocalizedFluidName();

				if (fluidName == null)
				{
					fluidName = I18n.translateToLocal(Registry.WAILA_TOOLTIP_FLUID_EMPTY);
				}

				currenttip.add(I18n.translateToLocalFormatted(Registry.WAILA_TOOLTIP_FLUID, fluidName));
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