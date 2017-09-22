package net.zarathul.simplefluidtanks.theoneprobe;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
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
 */
public class TankInfoProvider implements IProbeInfoProvider
{
	// I18N keys
	private static final String TANK_INFO = "tankInfo.";
	private static final String TANK_CAPACITY = TANK_INFO + "tankCapacity";
	private static final String VALVE_CAPACITY = TANK_INFO + "valveCapacity";
	private static final String IS_LINKED = TANK_INFO + "isLinked";
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
			probeInfo.text(I18n.translateToLocalFormatted(TANK_CAPACITY,Config.bucketsPerTank, "B"));
		}
		else if (block == SimpleFluidTanks.valveBlock)
		{
			ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, data.getPos());

			int amount = valveEntity.getFluidAmount();
			int capacity = valveEntity.getCapacity();
			int totalFillPercentage = (capacity > 0) ? MathHelper.clamp((int) ((long) amount * 100 / capacity), 0, 100) : 0;

			probeInfo.text(I18n.translateToLocalFormatted(
				VALVE_CAPACITY,
				amount / Fluid.BUCKET_VOLUME,
				capacity / Fluid.BUCKET_VOLUME,
				"B", totalFillPercentage));

			if (mode == ProbeMode.EXTENDED)
			{
				probeInfo.text(I18n.translateToLocalFormatted(TANKS, valveEntity.getLinkedTankCount()));
			}

		}
	}
}
