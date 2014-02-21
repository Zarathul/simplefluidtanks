package simplefluidtanks;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * {@link TankBlock} in item form.
 */
public class TankItem extends ItemBlock
{
	private static final String toolTipAddonKey = "item." + SimpleFluidTanks.REGISTRY_TANKITEM_NAME + ".toolTip";
	private static final String toolTipAddonDetails1Key = "item." + SimpleFluidTanks.REGISTRY_TANKITEM_NAME + ".toolTipDetails1";
	private static final String toolTipAddonDetails2Key = "item." + SimpleFluidTanks.REGISTRY_TANKITEM_NAME + ".toolTipDetails2";
	private static final String toolTipAddonDetails3Key = "item." + SimpleFluidTanks.REGISTRY_TANKITEM_NAME + ".toolTipDetails3";
	
	public TankItem(int id)
	{
		super(id);
		this.setMaxStackSize(64);
		this.setCreativeTab(SimpleFluidTanks.creativeTab);
		this.setUnlocalizedName(SimpleFluidTanks.REGISTRY_TANKITEM_NAME);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber()
	{
		return 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack items, EntityPlayer player, List list, boolean advancedItemTooltipsEnabled)
	{
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			list.add(StatCollector.translateToLocal(toolTipAddonDetails1Key));
			list.add(StatCollector.translateToLocal(toolTipAddonDetails2Key));
			list.add(StatCollector.translateToLocalFormatted(toolTipAddonDetails3Key, SimpleFluidTanks.bucketsPerTank));
		}
		else
		{
			list.add(StatCollector.translateToLocal(toolTipAddonKey));
		}
	}
}