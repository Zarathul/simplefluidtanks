package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.zarathul.simplefluidtanks.common.BlockCoords;
import net.zarathul.simplefluidtanks.common.Direction;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		int shiftDir = Direction.vanillaSideOpposites.get(side);
		BlockCoords tankCoords = BlockCoords.offsetBy(shiftDir, 1, x, y, z);
		
		TankBlockEntity tankEntity = Utils.getTileEntityAt(blockAccess, TankBlockEntity.class, tankCoords);
		
		if (tankEntity == null)
		{
			return true;
		}
		
		boolean[] connections = tankEntity.getConnections();
		
		if (side == Direction.YPOS)
		{
			if (!connections[Direction.YPOS])
			{
				return true;
			}
			
			TankBlockEntity tankAbove = Utils.getTileEntityAt(blockAccess, TankBlockEntity.class, x, y, z);
			
			return (tankAbove == null || tankAbove.isEmpty());
		}
		
		return !connections[side];
	}
}
