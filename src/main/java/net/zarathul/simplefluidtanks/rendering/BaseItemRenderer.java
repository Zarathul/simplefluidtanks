package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Base class for custom item renderers.
 */
@SideOnly(Side.CLIENT)
public abstract class BaseItemRenderer implements IItemRenderer
{
	protected final double[] defaultOrigin = new double[] { -0.5, -0.5, -0.5 };
	protected final double[] equippedOrigin = new double[] { 0, 0, 0 };
	
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
		// ENTITY and INVENTORY expect [-0.5, -0.5, -0.5] to [0.5, 0.5, 0.5] coordinate range
		// EQUIPPED and EQUIPPED_FIRST_PERSON expect [0,0,0] to [1,1,1].
		TessellationManager.setBaseCoords((type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) ? equippedOrigin : defaultOrigin);
	}
}
