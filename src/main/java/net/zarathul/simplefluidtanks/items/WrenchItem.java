package net.zarathul.simplefluidtanks.items;

import appeng.api.implementations.items.IAEWrench;
import blusunrize.immersiveengineering.api.tool.ITool;
import cofh.api.item.IToolHammer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;

/**
 * A simple wrench.
 */
public class WrenchItem extends Item implements IAEWrench, IToolHammer, ITool
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

	// AE2
	@Override
	public boolean canWrench(ItemStack wrench, EntityPlayer player, BlockPos pos)
	{
		return true;
	}

	// IE
	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	// COFH
	@Override
	public boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos)
	{
		return true;
	}

	@Override
	public boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity)
	{
		return true;
	}

	@Override
	public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos)
	{
	}

	@Override
	public void toolUsed(ItemStack item, EntityLivingBase user, Entity entity)
	{
	}
}