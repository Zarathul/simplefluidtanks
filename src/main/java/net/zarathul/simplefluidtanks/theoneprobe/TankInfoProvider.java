package net.zarathul.simplefluidtanks.theoneprobe;

import mcjty.theoneprobe.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Provides TheOneProbe tooltip information for portals.
 */
public class TankInfoProvider implements IProbeInfoProvider
{
	// I18N keys
	private static final String TANK_INFO = IProbeInfo.STARTLOC + "tank_info.";
	private static final String CAPACITY = TANK_INFO + "capacity" + IProbeInfo.ENDLOC;
	private static final String IS_LINKED = TANK_INFO + "is_linked" + IProbeInfo.ENDLOC;
	private static final String AMOUNT = TANK_INFO + "amount" + IProbeInfo.ENDLOC;
	private static final String TANKS = TANK_INFO + "tanks" + IProbeInfo.ENDLOC;
	private static final String YES = TANK_INFO + "yes" + IProbeInfo.ENDLOC;
	private static final String NO = TANK_INFO + "no" + IProbeInfo.ENDLOC;

	@Override
	public String getID()
	{
		return SimpleFluidTanks.MOD_ID + ":TankInfoProvider";
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data)
	{
		Block block = blockState.getBlock();

		if (block == SimpleFluidTanks.tankBlock && mode == ProbeMode.EXTENDED)
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, data.getPos());

			probeInfo.text(String.format("%s %s", IS_LINKED, ((tankEntity.isPartOfTank()) ? YES : NO)));
			probeInfo.text(String.format("%s %d B", CAPACITY, Config.bucketsPerTank.get()));
		}
		else if (block == SimpleFluidTanks.valveBlock)
		{
			ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, data.getPos());

			if (!valveEntity.hasTanks()) return;

			int amount = valveEntity.getFluidAmount();
			int capacity = valveEntity.getCapacity();
			int totalFillPercentage = (capacity > 0) ? MathHelper.clamp((int) ((long) amount * 100 / capacity), 0, 100) : 0;

			amount /= FluidAttributes.BUCKET_VOLUME;
			capacity /= FluidAttributes.BUCKET_VOLUME;

			String suffix = "/ " + Utils.getMetricFormattedNumber(capacity, "%.1f %s%s", "%d %s", "B");

			if (mode == ProbeMode.EXTENDED) suffix += " (" + totalFillPercentage + "%)";

			probeInfo.progress(
					amount,
					capacity,
					probeInfo.defaultProgressStyle().numberFormat(NumberFormat.COMPACT)
							.filledColor(0xFF2222DD)
							.alternateFilledColor(0xFF2222DD)
							.suffix(suffix));

			if (amount > 0)
			{
				probeInfo.text(valveEntity.getFluidRegistryName().toString());
			}

			if (mode == ProbeMode.EXTENDED)
			{
				probeInfo.text(String.format("%s %d B", AMOUNT, amount));
				probeInfo.text(String.format("%s %d B", CAPACITY, capacity));
				probeInfo.text(String.format("%s %d", TANKS, valveEntity.getLinkedTankCount()));
			}
		}
	}
}
