package net.zarathul.simplefluidtanks.rendering;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
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
	public static int renderPass = 0;
	
	private static final double flickerOffset = 0.0005d;
	private static final float yPosLightFactor = 1.0f;
	private static final float yNegLightFactor = 0.5f;
	private static final float zLightFactor = 0.8f;
	private static final float xLightFactor = 0.6f;

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if (modelId == id) {
			TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, x, y, z);
	
			if (tankEntity == null || !(tankEntity instanceof TankBlockEntity)) return false;
	
			TankBlock tank = (TankBlock) block;
			IIcon[] frameIcons = tank.getIcons();
	
			int colorMultiplier = SimpleFluidTanks.tankBlock.colorMultiplier(world, x, y, z);
			float red = (colorMultiplier >> 16 & 255) / 255.0F;
			float green = (colorMultiplier >> 8 & 255) / 255.0F;
			float blue = (colorMultiplier & 255) / 255.0F;
	
	        //Liquid
	        if (renderPass == 0)
	        {
	
				if (tankEntity.isPartOfTank())
				{
					final int fillPercentage = tankEntity.getFillPercentage();
					final boolean[] connections = tankEntity.getConnections();
		
					if (fillPercentage > 0)
					{
						Fluid fluid = tankEntity.getFluid();
						IIcon fluidIcon = (fluid != null) ? fluid.getStillIcon() : null;
		
						if (fluidIcon != null)
						{
							renderFluid(renderer, connections, fluidIcon, fillPercentage, x, y, z, red, green, blue);
							return true;
						}
					}
				}
				return false;
	        } 
	        //Block
	        else
	        {
				if (tankEntity.isPartOfTank())
				{
					final boolean[] connections = tankEntity.getConnections();
					renderFrame(tank, renderer, tankEntity, connections, frameIcons, red, green, blue);
				}
				else
				{
					renderUnlinkedTank(tank, renderer, tankEntity, frameIcons[0], red, green, blue);
				}
				return true;
	        }
		}
		return false;
	}

	@Override
	public int getRenderId()
	{
		return id;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
//		TessellationManager.setBaseCoords(-0.5f, -0.5f, -0.5f);
//		TessellationManager.startDrawingQuads();
//		TessellationManager.renderCube(0, 0, 0, 16, 16, 16, block.getIcon(0, 0));
//		TessellationManager.draw();
//		TessellationManager.resetBaseCoords();
		renderStandardInvBlock( renderer, block, block.getIcon(0, 0));
	}
	
    public static void renderStandardInvBlock (RenderBlocks renderblocks, Block block, IIcon icon)
    {
        Tessellator tessellator = Tessellator.instance;
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1F, 0.0F);
        renderblocks.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderblocks.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1F);
        renderblocks.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderblocks.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1F, 0.0F, 0.0F);
        renderblocks.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderblocks.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }
	

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}

	/**
	 * Renders a {@link TankBlock} without connected textures.
	 * 
	 * @param block
	 * The {@link TankBlock} to render.
	 * @param renderer
	 * The renderer.
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity}.
	 * @param icon
	 * The texture to use.
	 * @param red
	 * The red channel color multiplier.
	 * @param green
	 * The green channel color multiplier.
	 * @param blue
	 * The blue channel color multiplier.
	 */
	private void renderUnlinkedTank(TankBlock block, RenderBlocks renderer, TankBlockEntity entity, IIcon icon, float red, float green, float blue)
	{
        Tessellator tessellator = Tessellator.instance;
		IBlockAccess world = entity.getWorldObj();

		TankBlockEntity BlockYNEG = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord, entity.yCoord - 1, entity.zCoord);
		TankBlockEntity BlockYPOS = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord, entity.yCoord + 1, entity.zCoord);
		TankBlockEntity BlockZNEG = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord, entity.yCoord, entity.zCoord - 1);
		TankBlockEntity BlockZPOS = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord, entity.yCoord, entity.zCoord + 1);
		TankBlockEntity BlockXNEG = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord - 1, entity.yCoord, entity.zCoord);
		TankBlockEntity BlockXPOS = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord + 1, entity.yCoord, entity.zCoord);

		if (BlockYNEG == null || BlockYNEG.isPartOfTank())
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(yNegLightFactor * red, yNegLightFactor * green, yNegLightFactor * blue);
			renderer.renderFaceYNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			tessellator.setColorOpaque_F(yPosLightFactor * red, yPosLightFactor * green, yPosLightFactor * blue);
			renderer.renderFaceYPos(block, entity.xCoord, entity.yCoord - 1 + flickerOffset, entity.zCoord, icon);
		}

		if (BlockYPOS == null || BlockYPOS.isPartOfTank())
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(yPosLightFactor * red, yPosLightFactor * green, yPosLightFactor * blue);
			renderer.renderFaceYPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			tessellator.setColorOpaque_F(yNegLightFactor * red, yNegLightFactor * green, yNegLightFactor * blue);
			renderer.renderFaceYNeg(block, entity.xCoord, entity.yCoord + 1 - flickerOffset, entity.zCoord, icon);
		}

		if (BlockZNEG == null || BlockZNEG.isPartOfTank())
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
			renderer.renderFaceZNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			renderer.renderFaceZPos(block, entity.xCoord, entity.yCoord, entity.zCoord - 1 + flickerOffset, icon);
		}

		if (BlockZPOS == null || BlockZPOS.isPartOfTank())
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
			renderer.renderFaceZPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			renderer.renderFaceZNeg(block, entity.xCoord, entity.yCoord, entity.zCoord + 1 - flickerOffset, icon);
		}

		if (BlockXNEG == null || BlockXNEG.isPartOfTank())
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
			renderer.renderFaceXNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			renderer.renderFaceXPos(block, entity.xCoord - 1 + flickerOffset, entity.yCoord, entity.zCoord, icon);
		}

		if (BlockXPOS == null || BlockXPOS.isPartOfTank())
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
			renderer.renderFaceXPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			renderer.renderFaceXNeg(block, entity.xCoord + 1 - flickerOffset, entity.yCoord, entity.zCoord, icon);
		}
	}

	/**
	 * Renders the {@link TankBlock} with connected textures.
	 * 
	 * @param block
	 * The {@link TankBlock} to render.
	 * @param renderer
	 * The renderer.
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity}.
	 * @param connections
	 * The {@link TankBlock}s connected textures information.
	 * @param icons
	 * The {@link TankBlock}s textures.
	 * @param red
	 * The red channel color multiplier.
	 * @param green
	 * The green channel color multiplier.
	 * @param blue
	 * The blue channel color multiplier.
	 */
	private void renderFrame(TankBlock block, RenderBlocks renderer, TankBlockEntity entity, boolean[] connections, IIcon[] icons, float red, float green, float blue)
	{
        Tessellator tessellator = Tessellator.instance;
		IBlockAccess world = entity.getWorldObj();

		if (!connections[Direction.YNEG])
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(yNegLightFactor * red, yNegLightFactor * green, yNegLightFactor * blue);
			renderer.uvRotateBottom = 3;
			renderer.renderFaceYNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.YPOS)]);
			renderer.uvRotateBottom = 0;
			tessellator.setColorOpaque_F(yPosLightFactor * red, yPosLightFactor * green, yPosLightFactor * blue);
			renderer.uvRotateTop = 3;
			renderer.renderFaceYPos(block, entity.xCoord, entity.yCoord - 1 + flickerOffset, entity.zCoord, icons[entity.getTextureIndex(Direction.YPOS)]);
			renderer.uvRotateTop = 0;
		}

		if (!connections[Direction.YPOS])
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(yPosLightFactor * red, yPosLightFactor * green, yPosLightFactor * blue);
			renderer.uvRotateTop = 3;
			renderer.renderFaceYPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.YPOS)]);
			renderer.uvRotateTop = 0;
			tessellator.setColorOpaque_F(yNegLightFactor * red, yNegLightFactor * green, yNegLightFactor * blue);
			renderer.uvRotateBottom = 3;
			renderer.renderFaceYNeg(block, entity.xCoord, entity.yCoord + 1 - flickerOffset, entity.zCoord, icons[entity.getTextureIndex(Direction.YPOS)]);
			renderer.uvRotateBottom = 0;
		}

		if (!connections[Direction.ZNEG])
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
			renderer.renderFaceZNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.ZNEG)]);
			renderer.renderFaceZPos(block, entity.xCoord, entity.yCoord, entity.zCoord - 1 + flickerOffset, icons[entity.getTextureIndex(Direction.ZPOS)]);
		}

		if (!connections[Direction.ZPOS])
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
			renderer.renderFaceZPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.ZPOS)]);
			renderer.renderFaceZNeg(block, entity.xCoord, entity.yCoord, entity.zCoord + 1 - flickerOffset, icons[entity.getTextureIndex(Direction.ZNEG)]);
		}

		if (!connections[Direction.XNEG])
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
			renderer.renderFaceXNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.XNEG)]);
			renderer.renderFaceXPos(block, entity.xCoord - 1 + flickerOffset, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.XPOS)]);
		}

		if (!connections[Direction.XPOS])
		{
			tessellator.setBrightness(block.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			tessellator.setColorOpaque_F(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
			renderer.renderFaceXPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.XPOS)]);
			renderer.renderFaceXNeg(block, entity.xCoord + 1 - flickerOffset, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.XNEG)]);
		}
	}

	/**
	 * Renders the fluid in the {@link TankBlock}.
	 * 
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
	 * @param red
	 * The red channel color multiplier.
	 * @param green
	 * The green channel color multiplier.
	 * @param blue
	 * The blue channel color multiplier.
	 */
	private void renderFluid(RenderBlocks renderer, boolean[] connections, IIcon fluidIcon, int fillPercentage, int x, int y, int z, float red, float green, float blue)
	{
		double[] renderBounds = new double[]
		{
			(connections[Direction.XNEG]) ? 0.0 : flickerOffset,
			(connections[Direction.YNEG]) ? 0.0 : flickerOffset,
			(connections[Direction.ZNEG]) ? 0.0 : flickerOffset,
			(connections[Direction.XPOS]) ? 1.0 : 1.0 - flickerOffset,
			(connections[Direction.YPOS] || fillPercentage < 100) ? 1.0 : 1.0 - flickerOffset,
			(connections[Direction.ZPOS]) ? 1.0 : 1.0 - flickerOffset
		};

		renderer.setRenderBounds(renderBounds[0], renderBounds[1], renderBounds[2], renderBounds[3], (renderBounds[4] / 100.0) * fillPercentage, renderBounds[5]);
		renderer.setOverrideBlockTexture(fluidIcon);
		renderer.renderStandardBlockWithColorMultiplier(SimpleFluidTanks.fakeFluidBlock, x, y, z, red, green, blue);
		renderer.clearOverrideBlockTexture();
		renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
	}
}
