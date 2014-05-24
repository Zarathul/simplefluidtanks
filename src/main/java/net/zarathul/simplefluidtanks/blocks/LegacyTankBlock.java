package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.world.World;
import net.zarathul.simplefluidtanks.Registry;

public class LegacyTankBlock extends TankBlock
{
	public LegacyTankBlock()
	{
		super();
		setBlockName("legacy_" + Registry.TANKBLOCK_NAME);
		setCreativeTab(null);
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z)
	{
		return false;
	}
}
