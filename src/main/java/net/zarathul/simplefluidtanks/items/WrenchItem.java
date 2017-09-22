package net.zarathul.simplefluidtanks.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;

/**
 * A simple wrench.
 */
public class WrenchItem extends Item
{
	public WrenchItem()
	{
		super();

		setMaxStackSize(1);

		setCreativeTab(SimpleFluidTanks.creativeTab);
		setRegistryName(SimpleFluidTanks.WRENCH_ITEM_NAME);
		setUnlocalizedName(SimpleFluidTanks.WRENCH_ITEM_NAME);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		return true;
	}
}