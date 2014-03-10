package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.items.TankItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Custom renderer for {@link TankItem}.
 */
@SideOnly(Side.CLIENT)
public class TankItemRenderer extends BaseItemRenderer
{
	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		super.renderItem(type, item, data);
		
		IIcon icon = SimpleFluidTanks.tankBlock.getIcon(0, 0);
		
		TessellationManager.startDrawingQuads();
		TessellationManager.renderCube(0, 0, 0, 1, 1, 1, icon, false, 1);
		TessellationManager.draw();
	}
}
