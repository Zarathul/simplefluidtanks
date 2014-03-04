package net.zarathul.simplefluidtanks.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.LocalizationHelper;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * {@link ValveBlock} in item form.
 */
public class ValveItem extends ItemBlock
{
	private static final String toolTipKey = "item." + SimpleFluidTanks.REGISTRY_VALVEITEM_NAME + ".toolTip";
	private static final String toolTipDetailsKey = "item." + SimpleFluidTanks.REGISTRY_VALVEITEM_NAME + ".toolTipDetails";
	
	public ValveItem(Block block)
	{
		super(block);
		this.setMaxStackSize(64);
		this.setCreativeTab(SimpleFluidTanks.creativeTab);
		this.setUnlocalizedName(SimpleFluidTanks.REGISTRY_VALVEITEM_NAME);
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
			list.addAll(LocalizationHelper.multiLineTranslateToLocal(toolTipDetailsKey));
		}
		else
		{
			list.add(StatCollector.translateToLocal(toolTipKey));
		}
	}
}
