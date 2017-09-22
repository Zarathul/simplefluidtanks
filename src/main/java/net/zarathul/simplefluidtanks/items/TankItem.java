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
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

/**
 * {@link TankBlock} in item form.
 */
public class TankItem extends ItemBlock
{
	private static final String toolTipKey = "item." + SimpleFluidTanks.TANK_ITEM_NAME + ".toolTip";
	private static final String toolTipDetailsKey = "item." + SimpleFluidTanks.TANK_ITEM_NAME + ".toolTipDetails";

	public TankItem(Block block)
	{
		super(block);
		
		setMaxStackSize(64);
		setCreativeTab(SimpleFluidTanks.creativeTab);
		setRegistryName(SimpleFluidTanks.TANK_ITEM_NAME);
		setUnlocalizedName(SimpleFluidTanks.TANK_ITEM_NAME);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			tooltip.addAll(Utils.multiLineTranslateToLocal(toolTipDetailsKey, Config.bucketsPerTank));
		}
		else
		{
			tooltip.add(I18n.translateToLocal(toolTipKey));
		}
	}
}