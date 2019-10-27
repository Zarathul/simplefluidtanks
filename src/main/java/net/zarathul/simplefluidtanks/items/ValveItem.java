package net.zarathul.simplefluidtanks.items;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.Utils;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;

/**
 * {@link ValveBlock} in item form.
 */
public class ValveItem extends BlockItem
{
	private static final String toolTipKey = "item." + SimpleFluidTanks.MOD_ID + "." + SimpleFluidTanks.VALVE_ITEM_NAME + ".tooltip";
	private static final String toolTipDetailsKey = "item." + SimpleFluidTanks.MOD_ID + "." + SimpleFluidTanks.VALVE_ITEM_NAME + ".tooltip_details";

	public ValveItem(Block block)
	{
		super(block, new Item.Properties()
				.maxStackSize(64)
				.group(SimpleFluidTanks.creativeTab));

		this.setRegistryName(SimpleFluidTanks.VALVE_ITEM_NAME);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		long windowHandle = Minecraft.getInstance().mainWindow.getHandle();
		int leftShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT);
		int rightShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);

		// This does not work for some reason.
		//KeyBinding SneakKey = Minecraft.getInstance().gameSettings.keyBindSneak;
		//if (SneakKey.isKeyDown())

		if (leftShiftState == GLFW.GLFW_PRESS || rightShiftState == GLFW.GLFW_PRESS)
		{
			tooltip.addAll(Utils.multiLineTranslateToLocal(toolTipDetailsKey, 1));
		}
		else
		{
			tooltip.add(new TranslationTextComponent(toolTipKey));
		}
	}
}
