package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.zarathul.simplefluidtanks.common.Direction;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A fake block that can't be placed and is only used for rendering fluids in tanks.
 */
public class FakeFluidBlock extends Block
{
	public FakeFluidBlock()
	{
		super(Blocks.water.getMaterial());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
	{
		// Note: "pos" represents the coordinates of the adjacent block, not the FakeFluidBlock 
		// (e.g. side = EnumFacing.UP means "pos" contains the coordinates of the block above).
		
		BlockPos tankCoords = pos.offset(side.getOpposite());

		TankBlockEntity tankEntity = Utils.getTileEntityAt(blockAccess, TankBlockEntity.class, tankCoords);

		if (tankEntity == null)
		{
			return true;
		}

		if (side == EnumFacing.UP)
		{
			if (!tankEntity.isConnected(EnumFacing.UP))
			{
				return true;
			}

			TankBlockEntity tankAbove = Utils.getTileEntityAt(blockAccess, TankBlockEntity.class, pos);

			return (tankAbove == null || tankAbove.isEmpty());
		}

		return !tankEntity.isConnected(side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getMixedBrightnessForBlock(IBlockAccess blockAccess, BlockPos pos)
	{
		int mixedBrightness = super.getMixedBrightnessForBlock(blockAccess, pos);
		int blockLight = (mixedBrightness >> 4) & 15;
		int skyLight = (mixedBrightness >> 20);

		TankBlockEntity tankEntity = Utils.getTileEntityAt(blockAccess, TankBlockEntity.class, pos);

		if (tankEntity != null)
		{
			Fluid fluid = tankEntity.getFluid();

			if (fluid != null)
			{
				int luminostiy = fluid.getLuminosity();

				if (luminostiy > blockLight)
				{
					// replace the block light with the fluids luminosity
					return skyLight << 20 | luminostiy << 4;
				}
			}
		}

		return mixedBrightness;
	}
}
