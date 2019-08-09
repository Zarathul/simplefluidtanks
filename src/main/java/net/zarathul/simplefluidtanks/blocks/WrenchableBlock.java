package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.zarathul.simplefluidtanks.common.Utils;

/**
 * A base class for blocks that have custom behavior when a wrench is used on them.
 */
public abstract class WrenchableBlock extends Block
{
	protected WrenchableBlock(Block.Properties props)
	{
		super(props);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
	{
		ItemStack heldItemStack = player.getHeldItem(hand);

		if (!heldItemStack.isEmpty() && Utils.isWrenchItem(heldItemStack.getItem()))
		{
			if (!world.isRemote)
			{
				handleToolWrenchClick(world, pos, player, heldItemStack);
			}

			return true;
		}

		return super.onBlockActivated(state, world, pos, player, hand, hit);
	}

	/**
	 * Handles clicks with wrenches on the block.
	 * 
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link ValveBlock}s coordinates.
	 * @param player
	 * The player using the item.
	 * @param equippedItemStack
	 * The item(stack) used on the block.
	 */
	protected abstract void handleToolWrenchClick(World world, BlockPos pos, PlayerEntity player, ItemStack equippedItemStack);
}
