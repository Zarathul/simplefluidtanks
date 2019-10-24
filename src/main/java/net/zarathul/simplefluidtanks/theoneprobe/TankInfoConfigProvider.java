package net.zarathul.simplefluidtanks.theoneprobe;

import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class TankInfoConfigProvider implements IProbeConfigProvider
{
	@Override
	public void getProbeConfig(IProbeConfig config, PlayerEntity player, World world, Entity entity, IProbeHitEntityData data)
	{
		// Do nothing, there are no entities in this mod.
	}

	@Override
	public void getProbeConfig(IProbeConfig config, PlayerEntity player, World world, BlockState blockState, IProbeHitData data)
	{
		// Don't show fluid information because it is only synced to the client if the type of fluid changes.
		config.setTankMode(0);
	}
}
