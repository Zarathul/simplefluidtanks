package net.zarathul.simplefluidtanks.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;

/**
 * A simple wrench.
 */
public class WrenchItem extends Item
{
	public WrenchItem() {
		super(new Item.Properties()
					  .maxStackSize(1)
					  .group(SimpleFluidTanks.creativeTab));

		setRegistryName(SimpleFluidTanks.WRENCH_ITEM_NAME);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player)
	{
		return true;
	}
}
