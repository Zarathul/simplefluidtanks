package net.zarathul.simplefluidtanks.items;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A simple BuildCraft compatible wrench.
 */
public class WrenchItem extends Item implements IToolWrench
{
	public WrenchItem(int id)
	{
		super(id);

		setFull3D();
		setMaxStackSize(1);

		setCreativeTab(SimpleFluidTanks.creativeTab);
		setUnlocalizedName(SimpleFluidTanks.REGISTRY_WRENCHITEM_NAME);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		itemIcon = iconRegister.registerIcon(SimpleFluidTanks.REGISTRY_WRENCHITEM_KEY);
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World world, int x, int y, int z)
	{
		return true;
	}

	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z)
	{
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z)
	{
		player.swingItem();
	}
}
