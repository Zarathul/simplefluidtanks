package net.zarathul.simplefluidtanks.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.zarathul.simplefluidtanks.Registry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LegacyTankItem extends TankItem
{
	private final String itemName = "legacy_" + Registry.TANKITEM_NAME;
	private final String tooltipKey = "item." + itemName + ".toolTip";

	public LegacyTankItem(Block block)
	{
		super(block);
		setUnlocalizedName(itemName);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack items, EntityPlayer player, List list, boolean advancedItemTooltipsEnabled)
	{
		list.add(StatCollector.translateToLocal(tooltipKey));
	}
}
