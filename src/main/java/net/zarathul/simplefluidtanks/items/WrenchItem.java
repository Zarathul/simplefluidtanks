package net.zarathul.simplefluidtanks.items;

import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.registration.Registry;

/**
 * A simple wrench.
 */
public class WrenchItem extends Item implements IToolWrench, IToolHammer
{
	public WrenchItem()
	{
		super();

		setMaxStackSize(1);

		setCreativeTab(SimpleFluidTanks.creativeTab);
		setUnlocalizedName(Registry.WRENCH_ITEM_NAME);
	}

	@Override
	public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos)
	{
		return true;
	}

	@Override
	public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos)
	{
		// Do nothing.
		return;
	}

	@Override
	public boolean canWrench(EntityPlayer player, BlockPos pos)
	{
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, BlockPos pos)
	{
		// Do nothing.
		return;
	}

	@Override
	public boolean canWrench(EntityPlayer player, Entity entity)
	{
		// Don't wrench entities.
		return false;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, Entity entity)
	{
		// Do nothing.
		return;
	}
}