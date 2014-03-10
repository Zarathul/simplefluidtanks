package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.Direction;
import net.zarathul.simplefluidtanks.items.ValveItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Custom renderer for {@link ValveItem}.
 */
@SideOnly(Side.CLIENT)
public class ValveItemRenderer extends BaseItemRenderer
{
	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		super.renderItem(type, item, data);
		
		IIcon icon = SimpleFluidTanks.valveBlock.getIcon(Direction.XPOS, 0);
		IIcon iconIo = SimpleFluidTanks.valveBlock.getIcon(0, 0);
		IIcon iconTank = SimpleFluidTanks.valveBlock.getIcon(Direction.YPOS, 0);
		
		TessellationManager.startDrawingQuads();
		TessellationManager.renderPositiveXFace(0 + 1, 0, 0, 1, 1, icon, 1);
		TessellationManager.renderNegativeXFace(0, 0, 0, 1, 1, (type == ItemRenderType.EQUIPPED_FIRST_PERSON) ? iconIo : icon, 1);
		TessellationManager.renderPositiveYFace(0, 0 + 1, 0, 1, 1, iconTank, 1);
		TessellationManager.renderNegativeYFace(0, 0, 0, 1, 1, icon, 1);
		TessellationManager.renderPositiveZFace(0, 0, 0 + 1, 1, 1, (type != ItemRenderType.EQUIPPED_FIRST_PERSON) ? iconIo : icon, 1);
		TessellationManager.renderNegativeZFace(0, 0, 0, 1, 1, icon, 1);
		TessellationManager.draw();
	}
}
