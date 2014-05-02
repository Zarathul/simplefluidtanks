package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

/**
 * The material for {@link TankBlock}s.
 */
public class TankMaterial extends Material
{
	public static final Material tankMaterial = new TankMaterial();

	public TankMaterial()
	{
		super(MapColor.airColor);
	}

	@Override
	public boolean isLiquid()
	{
		return false;
	}

	@Override
	public boolean isSolid()
	{
		return true;
	}

	@Override
	public boolean getCanBlockGrass()
	{
		return true;
	}

	@Override
	public boolean blocksMovement()
	{
		return true;
	}

	@Override
	public boolean getCanBurn()
	{
		return false;
	}

	@Override
	public boolean isReplaceable()
	{
		return false;
	}

	@Override
	public boolean isOpaque()
	{
		return false;
	}

	@Override
	public boolean isToolNotRequired()
	{
		return false;
	}

	@Override
	public int getMaterialMobility()
	{
		return 2;
	}
}