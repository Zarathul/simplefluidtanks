package net.zarathul.simplefluidtanks.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;

/**
 * {@link TankBlock} in item form.
 */
public class TankItem extends ItemBlock
{
	private static final String toolTipKey = "item." + Registry.TANK_ITEM_NAME + ".toolTip";
	private static final String toolTipDetailsKey = "item." + Registry.TANK_ITEM_NAME + ".toolTipDetails";

	public TankItem(Block block)
	{
		super(block);
		
		setMaxStackSize(64);
		setCreativeTab(SimpleFluidTanks.creativeTab);
		setRegistryName(Registry.TANK_ITEM_NAME);
		setUnlocalizedName(Registry.TANK_ITEM_NAME);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack items, EntityPlayer player, List list, boolean advancedItemTooltipsEnabled)
	{
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			list.addAll(Utils.multiLineTranslateToLocal(toolTipDetailsKey, Config.bucketsPerTank));
		}
		else
		{
			list.add(I18n.translateToLocal(toolTipKey));
		}
	}
}