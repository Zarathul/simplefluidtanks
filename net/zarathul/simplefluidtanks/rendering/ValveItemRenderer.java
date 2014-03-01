package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.Direction;
import net.zarathul.simplefluidtanks.items.ValveItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Custom renderer for {@link ValveItem}.
 */
@SideOnly(Side.CLIENT)
public class ValveItemRenderer implements IItemRenderer
{
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		switch (type)
		{
			case ENTITY:
			case EQUIPPED:
			case EQUIPPED_FIRST_PERSON:
			case INVENTORY:
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		switch (type)
		{
			case ENTITY:
			{
		        return (helper == ItemRendererHelper.ENTITY_BOBBING ||
		                helper == ItemRendererHelper.ENTITY_ROTATION ||
		                helper == ItemRendererHelper.BLOCK_3D);
			}
			case EQUIPPED:
			{
		        return (helper == ItemRendererHelper.BLOCK_3D ||
		                helper == ItemRendererHelper.EQUIPPED_BLOCK);
			}
			case EQUIPPED_FIRST_PERSON:
			{
				return (helper == ItemRendererHelper.EQUIPPED_BLOCK);
			}
			case INVENTORY:
			{
				return (helper == ItemRendererHelper.INVENTORY_BLOCK);
			}
			default:
			{
				return false;
			}
		}
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();

		// ENTITY and INVENTORY expect [-0.5, -0.5, -0.5] to [0.5, 0.5, 0.5] coordinate range, EQUIPPED and EQUIPPED_FIRST_PERSON expect [0,0,0] to [1,1,1].
		double[] baseCoords = (type == ItemRenderType.ENTITY || type == ItemRenderType.INVENTORY) ? new double[] { -0.5, -0.5, -0.5 } : new double[]{ 0, 0, 0 };
		Icon icon = SimpleFluidTanks.valveBlock.getIcon(Direction.XPOS, 0);
		Icon iconIo = SimpleFluidTanks.valveBlock.getIcon(0, 0);
		Icon iconTank = SimpleFluidTanks.valveBlock.getIcon(Direction.YPOS, 0);
		
		TessellationManager.setBaseCoords(baseCoords);
		TessellationManager.renderPositiveXFace(0 + 1, 0, 0, 1, 1, icon, 1);
		TessellationManager.renderNegativeXFace(0, 0, 0, 1, 1, (type == ItemRenderType.EQUIPPED_FIRST_PERSON) ? iconIo : icon, 1);
		TessellationManager.renderPositiveYFace(0, 0 + 1, 0, 1, 1, iconTank, 1);
		TessellationManager.renderNegativeYFace(0, 0, 0, 1, 1, icon, 1);
		TessellationManager.renderPositiveZFace(0, 0, 0 + 1, 1, 1, (type != ItemRenderType.EQUIPPED_FIRST_PERSON) ? iconIo : icon, 1);
		TessellationManager.renderNegativeZFace(0, 0, 0, 1, 1, icon, 1);
		tessellator.draw();
	}
}
