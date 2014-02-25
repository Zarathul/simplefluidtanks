package simplefluidtanks;

import buildcraft.api.tools.IToolWrench;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;

/**
 * A base class for blocks that have custom behavior when a buildcraft compatible wrenches is used on them.
 */
public abstract class WrenchableBlock extends BlockContainer
{
	protected WrenchableBlock(int id, Material material)
	{
		super(id, material);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		if (!world.isRemote)
		{
			ItemStack equippedItemStack = player.getCurrentEquippedItem();
			
			if (equippedItemStack != null)
			{
				if (equippedItemStack.getItem() instanceof IToolWrench)	// react to Buildcraft Api ToolWrench
				{
					handleToolWrenchClick(world, x, y, z, player, equippedItemStack);
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Handles Buildcraft ToolWrechnes used on the {@link ValveBlock}.
	 * @param world
	 * The world.
	 * @param x
	 * The {@link ValveBlock}s x-coordinate.
	 * @param y
	 * The {@link ValveBlock}s y-coordinate.
	 * @param z
	 * The {@link ValveBlock}s z-coordinate.
	 * @param player
	 * The player using the item.
	 * @param equippedItemStack
	 * The item(stack) used on the {@link ValveBlock}.
	 */
	protected abstract void handleToolWrenchClick(World world, int x, int y, int z, EntityPlayer player, ItemStack equippedItemStack);
}
