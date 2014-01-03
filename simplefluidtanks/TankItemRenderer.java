package simplefluidtanks;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;

public class TankItemRenderer implements IItemRenderer
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
//		Tessellator tessellator = Tessellator.instance;
//		tessellator.startDrawingQuads();

		// adjust rendering space to match what caller expects
		boolean mustundotranslate = false;
		
		switch (type)
		{
			case EQUIPPED:
			case EQUIPPED_FIRST_PERSON:
			{
				break; // caller expects us to render over [0,0,0] to [1,1,1], no translation necessary
			}
			case ENTITY:
			case INVENTORY:
			{
				// translate our coordinates so that [0,0,0] to [1,1,1] translates to the [-0.5, -0.5, -0.5] to [0.5, 0.5, 0.5] expected by the caller.
//				GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
//				mustundotranslate = true; // must undo the translation when we're finished rendering
				break;
			}
			default:
				break; // never here
	    }
		
		Icon icon = Block.blocksList[SimpleFluidTanks.tankBlockId].getIcon(0, 0);
		TessellationManager.setBaseCoords(0, 0, 0);
		TessellationManager.renderCube(0, 0, 0, 1, 1, 1, icon);
/*
	    // xpos face blue
//	    Icon icon = item.getItem().getIconFromDamage(5);
	    tessellator.setNormal(1.0F, 0.0F, 0.0F);
	    tessellator.addVertexWithUV(1.0, 0.0, 0.0, (double)icon.getMaxU(), (double)icon.getMaxV());
	    tessellator.addVertexWithUV(1.0, 1.0, 0.0, (double)icon.getMaxU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(1.0, 1.0, 1.0, (double)icon.getMinU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(1.0, 0.0, 1.0, (double)icon.getMinU(), (double)icon.getMaxV());

	    // xneg face purple
//	    icon = item.getItem().getIconFromDamage(4);
	    tessellator.setNormal(-1.0F, 0.0F, 0.0F);
	    tessellator.addVertexWithUV(0.0, 0.0, 1.0, (double)icon.getMaxU(), (double)icon.getMaxV());
	    tessellator.addVertexWithUV(0.0, 1.0, 1.0, (double)icon.getMaxU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(0.0, 1.0, 0.0, (double)icon.getMinU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(0.0, 0.0, 0.0, (double)icon.getMinU(), (double)icon.getMaxV());

	    // zneg face white
//	    icon = item.getItem().getIconFromDamage(2);
	    tessellator.setNormal(0.0F, 0.0F, -1.0F);
	    tessellator.addVertexWithUV(0.0, 0.0, 0.0, (double)icon.getMaxU(), (double)icon.getMaxV());
	    tessellator.addVertexWithUV(0.0, 1.0, 0.0, (double)icon.getMaxU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(1.0, 1.0, 0.0, (double)icon.getMinU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(1.0, 0.0, 0.0, (double)icon.getMinU(), (double)icon.getMaxV());

	    // zpos face green
//	    icon = item.getItem().getIconFromDamage(3);
	    tessellator.setNormal(0.0F, 0.0F, -1.0F);
	    tessellator.addVertexWithUV(1.0, 0.0, 1.0, (double)icon.getMaxU(), (double)icon.getMaxV());
	    tessellator.addVertexWithUV(1.0, 1.0, 1.0, (double)icon.getMaxU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(0.0, 1.0, 1.0, (double)icon.getMinU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(0.0, 0.0, 1.0, (double)icon.getMinU(), (double)icon.getMaxV());

	    // ypos face red
//	    icon = item.getItem().getIconFromDamage(1);
	    tessellator.setNormal(0.0F, 1.0F, 0.0F);
	    tessellator.addVertexWithUV(1.0, 1.0, 1.0, (double)icon.getMaxU(), (double)icon.getMaxV());
	    tessellator.addVertexWithUV(1.0, 1.0, 0.0, (double)icon.getMaxU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(0.0, 1.0, 0.0, (double)icon.getMinU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(0.0, 1.0, 1.0, (double)icon.getMinU(), (double)icon.getMaxV());

	    // yneg face yellow
//	    icon = item.getItem().getIconFromDamage(0);
	    tessellator.setNormal(0.0F, -1.0F, 0.0F);
	    tessellator.addVertexWithUV(0.0, 0.0, 1.0, (double)icon.getMaxU(), (double)icon.getMaxV());
	    tessellator.addVertexWithUV(0.0, 0.0, 0.0, (double)icon.getMaxU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(1.0, 0.0, 0.0, (double)icon.getMinU(), (double)icon.getMinV());
	    tessellator.addVertexWithUV(1.0, 0.0, 1.0, (double)icon.getMinU(), (double)icon.getMaxV());
*/
	    if (mustundotranslate) GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
}
