package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.world.World;
import net.zarathul.simplefluidtanks.Registry;

public class LegacyValveBlock extends ValveBlock
{
	public LegacyValveBlock()
	{
		super();
		setBlockName("legacy_" + Registry.VALVEBLOCK_NAME);
		setCreativeTab(null);
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z)
	{
		return false;
	}
}
