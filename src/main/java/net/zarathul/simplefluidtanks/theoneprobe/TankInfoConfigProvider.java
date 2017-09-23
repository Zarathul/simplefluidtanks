package net.zarathul.simplefluidtanks.theoneprobe;

import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class TankInfoConfigProvider implements IProbeConfigProvider
{
	@Override
	public void getProbeConfig(IProbeConfig config, EntityPlayer player, World world, Entity entity, IProbeHitEntityData data)
	{
		// Do nothing, there are no entities in this mod.
	}

	@Override
	public void getProbeConfig(IProbeConfig config, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
	{
		// Don't show fluid information because it is only synced to the client if the type of fluid changes.
		config.setTankMode(0);
	}
}
