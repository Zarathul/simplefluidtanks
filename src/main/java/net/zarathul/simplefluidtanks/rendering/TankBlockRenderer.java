package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
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

	private static final double flickerOffset = 0.0009765625d;
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

		IIcon[] frameIcons = SimpleFluidTanks.tankBlock.getIcons();

		TessellationManager.setBaseCoords(x, y, z);

		if (tankEntity.isPartOfTank())
		{
			int fillPercentage = tankEntity.getFillPercentage();
			boolean[] connections = tankEntity.getConnections();

			if (fillPercentage > 0)
			{
				Fluid fluid = tankEntity.getFluid();
				IIcon fluidIcon = (fluid != null) ? fluid.getStillIcon() : null;

				if (fluidIcon != null)
				{
					renderFluid(renderer, connections, fluidIcon, fillPercentage, x, y, z);
				}
			}

			renderFrame(block, renderer, tankEntity, connections, frameIcons);
		}
		else
		{
			renderUnlinkedTank(block, renderer, tankEntity, frameIcons[0]);
		}

		return true;
	}

	@Override
	public int getRenderId()
	{
		return id;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		TessellationManager.setBaseCoords(-0.5f, -0.5f, -0.5f);
		TessellationManager.startDrawingQuads();
		TessellationManager.renderCube(0, 0, 0, 16, 16, 16, block.getIcon(0, 0));
		TessellationManager.draw();
		TessellationManager.resetBaseCoords();
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}

	/**
	 * Renders a {@link TankBlock} without connected textures.
	 * 
	 * @param icon
	 * The texture to use.
	 */
	private void renderUnlinkedTank(Block block, RenderBlocks renderer, TankBlockEntity entity, IIcon icon)
	{
		IBlockAccess world = entity.getWorldObj();
		int colorMultiplier = SimpleFluidTanks.tankBlock.colorMultiplier(world, entity.xCoord, entity.yCoord, entity.zCoord);
		float red = (colorMultiplier >> 16 & 255) / 255.0F;
		float green = (colorMultiplier >> 8 & 255) / 255.0F;
		float blue = (colorMultiplier & 255) / 255.0F;

		TankBlockEntity BlockYNEG = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord, entity.yCoord - 1, entity.zCoord);
		TankBlockEntity BlockYPOS = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord, entity.yCoord + 1, entity.zCoord);
		TankBlockEntity BlockZNEG = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord, entity.yCoord, entity.zCoord - 1);
		TankBlockEntity BlockZPOS = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord, entity.yCoord, entity.zCoord + 1);
		TankBlockEntity BlockXNEG = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord - 1, entity.yCoord, entity.zCoord);
		TankBlockEntity BlockXPOS = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord + 1, entity.yCoord, entity.zCoord);

		if (BlockYNEG == null || BlockYNEG.isPartOfTank())
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(yNegLightFactor * red, yNegLightFactor * green, yNegLightFactor * blue);
			renderer.renderFaceYNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			TessellationManager.setColorOpaque(yPosLightFactor * red, yPosLightFactor * green, yPosLightFactor * blue);
			renderer.renderFaceYPos(block, entity.xCoord, entity.yCoord - 1, entity.zCoord, icon);
		}

		if (BlockYPOS == null || BlockYPOS.isPartOfTank())
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(yPosLightFactor * red, yPosLightFactor * green, yPosLightFactor * blue);
			renderer.renderFaceYPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			TessellationManager.setColorOpaque(yNegLightFactor * red, yNegLightFactor * green, yNegLightFactor * blue);
			renderer.renderFaceYNeg(block, entity.xCoord, entity.yCoord + 1, entity.zCoord, icon);
		}

		if (BlockZNEG == null || BlockZNEG.isPartOfTank())
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
			renderer.renderFaceZNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			renderer.renderFaceZPos(block, entity.xCoord, entity.yCoord, entity.zCoord - 1, icon);
		}

		if (BlockZPOS == null || BlockZPOS.isPartOfTank())
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
			renderer.renderFaceZPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			renderer.renderFaceZNeg(block, entity.xCoord, entity.yCoord, entity.zCoord + 1, icon);
		}

		if (BlockXNEG == null || BlockXNEG.isPartOfTank())
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
			renderer.renderFaceXNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			renderer.renderFaceXPos(block, entity.xCoord - 1, entity.yCoord, entity.zCoord, icon);
		}

		if (BlockXPOS == null || BlockXPOS.isPartOfTank())
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
			renderer.renderFaceXPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icon);
			renderer.renderFaceXNeg(block, entity.xCoord + 1, entity.yCoord, entity.zCoord, icon);
		}
	}

	/**
	 * Renders the {@link TankBlock} with connected textures.
	 * 
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity}.
	 * @param connections
	 * The {@link TankBlock}s connected textures information.
	 * @param icons
	 * The {@link TankBlock}s textures.
	 */
	private void renderFrame(Block block, RenderBlocks renderer, TankBlockEntity entity, boolean[] connections, IIcon[] icons)
	{
		IBlockAccess world = entity.getWorldObj();
		int colorMultiplier = SimpleFluidTanks.tankBlock.colorMultiplier(world, entity.xCoord, entity.yCoord, entity.zCoord);
		float red = (colorMultiplier >> 16 & 255) / 255.0F;
		float green = (colorMultiplier >> 8 & 255) / 255.0F;
		float blue = (colorMultiplier & 255) / 255.0F;

		if (!connections[Direction.YNEG])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(yNegLightFactor * red, yNegLightFactor * green, yNegLightFactor * blue);
			renderer.uvRotateBottom = 3;
			renderer.renderFaceYNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.YPOS)]);
			renderer.uvRotateBottom = 0;
			TessellationManager.setColorOpaque(yPosLightFactor * red, yPosLightFactor * green, yPosLightFactor * blue);
			renderer.uvRotateTop = 3;
			renderer.renderFaceYPos(block, entity.xCoord, entity.yCoord - 1, entity.zCoord, icons[entity.getTextureIndex(Direction.YPOS)]);
			renderer.uvRotateTop = 0;
		}

		if (!connections[Direction.YPOS])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(yPosLightFactor * red, yPosLightFactor * green, yPosLightFactor * blue);
			renderer.uvRotateTop = 3;
			renderer.renderFaceYPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.YPOS)]);
			renderer.uvRotateTop = 0;
			TessellationManager.setColorOpaque(yNegLightFactor * red, yNegLightFactor * green, yNegLightFactor * blue);
			renderer.uvRotateBottom = 3;
			renderer.renderFaceYNeg(block, entity.xCoord, entity.yCoord + 1, entity.zCoord, icons[entity.getTextureIndex(Direction.YPOS)]);
			renderer.uvRotateBottom = 0;
		}

		if (!connections[Direction.ZNEG])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
			renderer.renderFaceZNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.ZNEG)]);
			renderer.renderFaceZPos(block, entity.xCoord, entity.yCoord, entity.zCoord - 1, icons[entity.getTextureIndex(Direction.ZPOS)]);
		}

		if (!connections[Direction.ZPOS])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(zLightFactor * red, zLightFactor * green, zLightFactor * blue);
			renderer.renderFaceZPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.ZPOS)]);
			renderer.renderFaceZNeg(block, entity.xCoord, entity.yCoord, entity.zCoord + 1, icons[entity.getTextureIndex(Direction.ZNEG)]);
		}

		if (!connections[Direction.XNEG])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
			renderer.renderFaceXNeg(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.XNEG)]);
			renderer.renderFaceXPos(block, entity.xCoord - 1, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.XPOS)]);
		}

		if (!connections[Direction.XPOS])
		{
			TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord));
			TessellationManager.setColorOpaque(xLightFactor * red, xLightFactor * green, xLightFactor * blue);
			renderer.renderFaceXPos(block, entity.xCoord, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.XPOS)]);
			renderer.renderFaceXNeg(block, entity.xCoord + 1, entity.yCoord, entity.zCoord, icons[entity.getTextureIndex(Direction.XNEG)]);
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
	 */
	private void renderFluid(RenderBlocks renderer, boolean[] connections, IIcon fluidIcon, int fillPercentage, int x, int y, int z)
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
		renderer.renderStandardBlock(SimpleFluidTanks.fakeFluidBlock, x, y, z);
		renderer.clearOverrideBlockTexture();
		renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
	}
}
