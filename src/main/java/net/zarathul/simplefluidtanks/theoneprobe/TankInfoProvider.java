package net.zarathul.simplefluidtanks.theoneprobe;
/*
import mcjty.theoneprobe.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Provides TheOneProbe tooltip information for portals.

public class TankInfoProvider implements IProbeInfoProvider
{
	// I18N keys
	private static final String TANK_INFO = "tankInfo.";
	private static final String CAPACITY = TANK_INFO + "capacity";
	private static final String IS_LINKED = TANK_INFO + "isLinked";
	private static final String AMOUNT = TANK_INFO + "amount";
	private static final String TANKS = TANK_INFO + "tanks";
	private static final String YES = TANK_INFO + "yes";
	private static final String NO = TANK_INFO + "no";

	@Override
	public String getID()
	{
		return SimpleFluidTanks.MOD_ID + ":TankInfoProvider";
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
	{
		Block block = blockState.getBlock();

		if (block == SimpleFluidTanks.tankBlock && mode == ProbeMode.EXTENDED)
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, data.getPos());

			String readableFlag = I18n.translateToLocal((tankEntity.isPartOfTank()) ? YES : NO);
			probeInfo.text(I18n.translateToLocalFormatted(IS_LINKED, readableFlag));
			probeInfo.text(I18n.translateToLocalFormatted(CAPACITY,Config.bucketsPerTank));
		}
		else if (block == SimpleFluidTanks.valveBlock)
		{
			ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, data.getPos());

			if (!valveEntity.hasTanks()) return;

			int amount = valveEntity.getFluidAmount();
			int capacity = valveEntity.getCapacity();
			int totalFillPercentage = (capacity > 0) ? MathHelper.clamp((int) ((long) amount * 100 / capacity), 0, 100) : 0;

			amount /= Fluid.BUCKET_VOLUME;
			capacity /= Fluid.BUCKET_VOLUME;

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
				probeInfo.text(valveEntity.getLocalizedFluidName());
			}

			if (mode == ProbeMode.EXTENDED)
			{
				probeInfo.text(I18n.translateToLocalFormatted(AMOUNT, amount));
				probeInfo.text(I18n.translateToLocalFormatted(CAPACITY, capacity));
				probeInfo.text(I18n.translateToLocalFormatted(TANKS, valveEntity.getLinkedTankCount()));
			}

		}
	}
}
*/