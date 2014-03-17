package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.common.Direction;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Custom renderer for {@link TankBlock}s.
 */
@SideOnly(Side.CLIENT)
public class TankBlockRenderer implements ISimpleBlockRenderingHandler
{
	public static final int id = RenderingRegistry.getNextAvailableRenderId();
	public static int pass = -1;
	
	private static final double flickerOffset = 0.001;
	private static final float yPosLightFactor = 1.0f;
	private static final float yNegLightFactor = 0.5f;
	private static final float zLightFactor = 0.8f;
	private static final float xLightFactor = 0.6f;

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, x, y, z);
		
		if (tankEntity == null)
		{
			return false;
		}
		
		Icon[] icons = SimpleFluidTanks.tankBlock.getIcons();
		
		TessellationManager.setBaseCoords(x, y, z);
		
		if (!tankEntity.isPartOfTank())
		{
			renderUnlinkedTank(renderer, tankEntity, icons[0]);
		}
		else
		{
			boolean[] connections = tankEntity.getConnections();
			
			int fillPercentage = tankEntity.getFillPercentage();
			
			if (pass == 0)
			{
				renderFrame(tankEntity, connections, icons);
			}
			else
			{
				if (fillPercentage > 0)
				{
					Fluid fluid = tankEntity.getFluid();
					Icon fluidIcon = (fluid != null) ? fluid.getStillIcon() : null;
					
					if (fluidIcon != null)
					{
						renderFluid(renderer, connections, fluidIcon, fillPercentage, x, y, z);
					}
				}
			}
		}
		
		return true;
	}

	@Override
	public int getRenderId()
	{
		return id;
	}

	@Override
	public boolean shouldRender3DInInventory()
	{
		return false;
	}
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
	}
	
	/**
	 * Renders a slightly shrinked {@link TankBlock} without connected textures.
	 * @param icon
	 * The texture to use.
	 */
	private void renderUnlinkedTank(RenderBlocks renderer, TankBlockEntity entity, Icon icon)
	{
		IBlockAccess world = entity.getWorldObj();
        int colorMultiplier = SimpleFluidTanks.tankBlock.colorMultiplier(world, entity.xCoord, entity.yCoord, entity.zCoord);
        float red = (float)(colorMultiplier >> 16 & 255) / 255.0F;
        float green = (float)(colorMultiplier >> 8 & 255) / 255.0F;
        float blue = (float)(colorMultiplier & 255) / 255.0F;
		
        // negative y 
		TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord - 1, entity.zCoord));
		TessellationManager.setColorOpaque(yNegLightFactor * red, yNegLightFactor * green, yNegLightFactor * blue);
		TessellationManager.renderNegativeYFace(1, 1, 1, 14, 14, icon);
		// back side
		TessellationManager.renderPositiveYFace(1, 1, 1, 14, 14, icon);
		
		// positive y
		TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord + 1, entity.zCoord));
		TessellationManager.setColorOpaque(yPosLightFactor * red, yPosLightFactor * green, yPosLightFactor * blue);
		TessellationManager.renderPositiveYFace(1, 15, 1, 14, 14, icon);
		// back side
		TessellationManager.renderNegativeYFace(1, 15, 1, 14, 14, icon);
		
		// negative z 
		TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord - 1));
		TessellationManager.setColorOpaque(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
		TessellationManager.renderNegativeZFace(1, 1, 1, 14, 14, icon);
		// back side
		TessellationManager.renderPositiveZFace(1, 1, 1, 14, 14, icon);
		
		// positive z 
		TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord + 1));
		TessellationManager.setColorOpaque(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
		TessellationManager.renderPositiveZFace(1, 1, 15, 14, 14, icon);
		// back side
		TessellationManager.renderNegativeZFace(1, 1, 15, 14, 14, icon);
		
		// negative x
		TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord - 1, entity.yCoord, entity.zCoord));
		TessellationManager.setColorOpaque(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
		TessellationManager.renderNegativeXFace(1, 1, 1, 14, 14, icon);
		// back side
		TessellationManager.renderPositiveXFace(1, 1, 1, 14, 14, icon);
		
		// positive x
		TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord + 1, entity.yCoord, entity.zCoord));
		TessellationManager.setColorOpaque(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
		TessellationManager.renderPositiveXFace(15, 1, 1, 14, 14, icon);
		// back side
		TessellationManager.renderNegativeXFace(15, 1, 1, 14, 14, icon);
	}
	
	/**
	 * Renders the {@link TankBlock} with connected textures.
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity}.
	 * @param connections
	 * The {@link TankBlock}s connected textures information.
	 * @param icons
	 * The {@link TankBlock}s textures.
	 */
	private void renderFrame(TankBlockEntity entity, boolean[] connections, Icon[] icons)
	{
		IBlockAccess world = entity.getWorldObj();
        int colorMultiplier = SimpleFluidTanks.tankBlock.colorMultiplier(world, entity.xCoord, entity.yCoord, entity.zCoord);
        float red = (float)(colorMultiplier >> 16 & 255) / 255.0F;
        float green = (float)(colorMultiplier >> 8 & 255) / 255.0F;
        float blue = (float)(colorMultiplier & 255) / 255.0F;
		
		if (!connections[Direction.YNEG])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord - 1, entity.zCoord));
			TessellationManager.setColorOpaque(yNegLightFactor * red, yNegLightFactor * green, yNegLightFactor * blue);
			TessellationManager.renderNegativeYFace(0, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.YNEG)]);
			TessellationManager.renderPositiveYFace(0, 0 + flickerOffset, 0, 16, 16, icons[entity.getTextureIndex(Direction.YPOS)]);
		}
		
		if (!connections[Direction.YPOS])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord + 1, entity.zCoord));
			TessellationManager.setColorOpaque(yPosLightFactor * red, yPosLightFactor * green, yPosLightFactor * blue);
			TessellationManager.renderPositiveYFace(0, 16, 0, 16, 16, icons[entity.getTextureIndex(Direction.YPOS)]);
			TessellationManager.renderNegativeYFace(0, 16 - flickerOffset, 0, 16, 16, icons[entity.getTextureIndex(Direction.YNEG)]);
		}
		
		if (!connections[Direction.ZNEG])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord - 1));
			TessellationManager.setColorOpaque(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
			TessellationManager.renderNegativeZFace(0, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.ZNEG)]);
			TessellationManager.renderPositiveZFace(0, 0, 0 + flickerOffset, 16, 16, icons[entity.getTextureIndex(Direction.ZPOS)]);
		}
		
		if (!connections[Direction.ZPOS])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord + 1));
			TessellationManager.setColorOpaque(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
			TessellationManager.renderPositiveZFace(0, 0, 16, 16, 16, icons[entity.getTextureIndex(Direction.ZPOS)]);
			TessellationManager.renderNegativeZFace(0, 0, 16 - flickerOffset, 16, 16, icons[entity.getTextureIndex(Direction.ZNEG)]);
		}
		
		if (!connections[Direction.XNEG])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord - 1, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
			TessellationManager.renderNegativeXFace(0, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.XNEG)]);
			TessellationManager.renderPositiveXFace(0 + flickerOffset, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.XPOS)]);
		}
		
		if (!connections[Direction.XPOS])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord + 1, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
			TessellationManager.renderPositiveXFace(16, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.XPOS)]);
			TessellationManager.renderNegativeXFace(16 - flickerOffset, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.XNEG)]);
		}
	}
	
	/**
	 * Renders the fluid in the {@link TankBlock}.
	 * @param renderer
	 * The renderer.
	 * @param connections
	 * The {@link TankBlock}s connected textures information.
	 * @param fluidIcon
	 * The fluids texture.
	 * @param fillPercentage
	 * The {@link TankBlock}s fill percentage.
	 * @param x
	 * The x-coordinate.
	 * @param y
	 * The y-coordinate.
	 * @param z
	 * The z-coordinate.
	 */
	private void renderFluid(RenderBlocks renderer, boolean[] connections, Icon fluidIcon, int fillPercentage, int x, int y, int z)
	{
		double[] renderBounds = new double[]
		{
			(connections[Direction.XNEG]) ? 0.0 : flickerOffset,
			(connections[Direction.YNEG]) ? 0.0 : flickerOffset,
			(connections[Direction.ZNEG]) ? 0.0 : flickerOffset,
			(connections[Direction.XPOS]) ? 1.0 : 1.0 - flickerOffset,
			(connections[Direction.YPOS]) ? 1.0 : 1.0 - flickerOffset,
			(connections[Direction.ZPOS]) ? 1.0 : 1.0 - flickerOffset
		};
		
		renderer.setRenderBounds(renderBounds[0], renderBounds[1], renderBounds[2], renderBounds[3], (renderBounds[4] / 100.0) * fillPercentage, renderBounds[5]);
        renderer.setOverrideBlockTexture(fluidIcon);
        renderer.renderStandardBlock(SimpleFluidTanks.tankBlock, (int)x, (int)y, (int)z);
        renderer.clearOverrideBlockTexture();
        renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
	}
}
