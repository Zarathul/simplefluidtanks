package net.zarathul.simplefluidtanks.blocks;

import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * A base class for blocks that have custom behavior when a wrench is used on them.
 */
public abstract class WrenchableBlock extends BlockContainer
{

	protected WrenchableBlock(Material material)
	{
		super(material);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			ItemStack equippedItemStack = player.getCurrentEquippedItem();

			if (equippedItemStack != null)
			{
				Item item = equippedItemStack.getItem();

				if (item instanceof IToolWrench || item instanceof IToolHammer)	// react to Wrenches (Buildcraft + Thermal Expansion)
				{
					handleToolWrenchClick(world, pos, player, equippedItemStack);

					return true;
				}
			}

			return false;
		}

		return true;
	}

	/**
	 * Handles clicks with wrenches on the {@link BlockContainer}.
	 * 
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link ValveBlock}s coordinates.
	 * @param player
	 * The player using the item.
	 * @param equippedItemStack
	 * The item(stack) used on the {@link ValveBlock}.
	 */
	protected abstract void handleToolWrenchClick(World world, BlockPos pos, EntityPlayer player, ItemStack equippedItemStack);
}
