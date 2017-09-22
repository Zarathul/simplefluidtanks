package net.zarathul.simplefluidtanks.items;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.Utils;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

/**
 * {@link ValveBlock} in item form.
 */
public class ValveItem extends ItemBlock
{
	private static final String toolTipKey = "item." + SimpleFluidTanks.VALVE_ITEM_NAME + ".toolTip";
	private static final String toolTipDetailsKey = "item." + SimpleFluidTanks.VALVE_ITEM_NAME + ".toolTipDetails";

	public ValveItem(Block block)
	{
		super(block);
		this.setMaxStackSize(64);
		this.setCreativeTab(SimpleFluidTanks.creativeTab);
		this.setRegistryName(SimpleFluidTanks.VALVE_ITEM_NAME);
		this.setUnlocalizedName(SimpleFluidTanks.VALVE_ITEM_NAME);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			tooltip.addAll(Utils.multiLineTranslateToLocal(toolTipDetailsKey));
		}
		else
		{
			tooltip.add(I18n.translateToLocal(toolTipKey));
		}
	}
}
