package simplefluidtanks;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.launchwrapper.LogWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidRegistry.FluidRegisterEvent;
import net.minecraftforge.fluids.FluidStack;

@SideOnly(Side.CLIENT)
public class TankBlockRenderer extends TileEntitySpecialRenderer
{
	public TankBlockRenderer()
	{
		super();
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
	{
		// this should never happen, but hey it's minecraft
		if (tileEntity == null || !(tileEntity instanceof TankBlockEntity))
		{
			LogWrapper.log.severe("Possible map corruption detected. TankBlockEntity missing at x:%d / y:%d / z:%d. Expect severe rendering and tank logic issues.", x, y, z);
			return;
		}
		
		Block block = tileEntity.getBlockType();
		
		// this should also never happen
		if (block == null || !(block instanceof TankBlock))
		{
			LogWrapper.log.severe("Possible map corruption detected. TankBlock missing at x:%d / y:%d / z:%d. Expect severe rendering and tank logic issues.", x, y, z);
			return;
		}
		
		TankBlock tank = (TankBlock)block;
		TankBlockEntity entity = (TankBlockEntity)tileEntity;
		Icon[] icons = tank.getIcons();
		
		TessellationManager.setBaseCoords(x, y, z);
		Tessellator tsr = Tessellator.instance;
		int brightness = block.getMixedBrightnessForBlock(entity.worldObj, entity.xCoord, entity.yCoord, entity.zCoord);
		tsr.setBrightness(brightness);
		
		bindTexture(TextureMap.locationBlocksTexture);
		
		tsr.startDrawingQuads();
		
		if (!entity.isPartOfTank())
		{
			renderSolid(entity, icons[0]);
		}
		else
		{
			boolean[] connections = entity.getConnections();
			int fillPercentage = entity.getFillPercentage();
			double fluidHeight = 16.0 / 100 * fillPercentage;
			double verticalTextureOffset = 16.0 / 100 * (100 - fillPercentage);
			Icon fluidIcon = getFluidTexture(entity);
			
			renderPositiveXFace(entity, connections, icons, fluidIcon, fillPercentage, fluidHeight, verticalTextureOffset);
			renderNegativeXFace(entity, connections, icons, fluidIcon, fillPercentage, fluidHeight, verticalTextureOffset);
			renderPositiveZFace(entity, connections, icons, fluidIcon, fillPercentage, fluidHeight, verticalTextureOffset);
			renderNegativeZFace(entity, connections, icons, fluidIcon, fillPercentage, fluidHeight, verticalTextureOffset);
			renderPositiveYFace(entity, connections, icons, fluidIcon, fillPercentage, fluidHeight);
			renderNegativeYFace(entity, connections, icons, fluidIcon, fillPercentage);
		}
		
		tsr.draw();
	}
	
	private void renderSolid(TankBlockEntity entity, Icon icon)
	{
		TessellationManager.renderCube(1, 0, 1, 14, 14, 14, icon, true, TessellationManager.pixel);
	}
	
	private void renderPositiveXFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, int fillPercentage, double fluidHeight, double verticalTextureOffset)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections[ConnectedTexturesHelper.XPOS])
		{
			if (fillPercentage > 0 && fluidIcon != null)
			{
				TessellationManager.renderPositiveXFace(16, 0, 0, fluidHeight, 16, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			TessellationManager.renderPositiveXFace(16, 0, 0, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.XPOS)]);
			// inner face
			TessellationManager.renderNegativeXFace(16, 0, 0, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.XNEG)]);
		}
	}
	
	private void renderNegativeXFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, int fillPercentage, double fluidHeight, double verticalTextureOffset)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections[ConnectedTexturesHelper.XNEG])
		{
			if (fillPercentage > 0 && fluidIcon != null)
			{
				TessellationManager.renderNegativeXFace(0, 0, 0, fluidHeight, 16, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			TessellationManager.renderNegativeXFace(0, 0, 0, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.XNEG)]);
			// inner face
			TessellationManager.renderPositiveXFace(0, 0, 0, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.XPOS)]);
		}
	}
	
	private void renderPositiveZFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, int fillPercentage, double fluidHeight, double verticalTextureOffset)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections[ConnectedTexturesHelper.ZPOS])
		{
			if (fillPercentage > 0 && fluidIcon != null)
			{
				TessellationManager.renderPositiveZFace(0, 0, 16, 16, fluidHeight, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			TessellationManager.renderPositiveZFace(0, 0, 16, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.ZPOS)]);
			// inner face
			TessellationManager.renderNegativeZFace(0, 0, 16, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.ZNEG)]);
		}
	}
	
	private void renderNegativeZFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, int fillPercentage, double fluidHeight, double verticalTextureOffset)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections[ConnectedTexturesHelper.ZNEG])
		{
			if (fillPercentage > 0 && fluidIcon != null)
			{
				TessellationManager.renderNegativeZFace(0, 0, 0, 16, fluidHeight, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			TessellationManager.renderNegativeZFace(0, 0, 0, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.ZNEG)]);
			// inner face
			TessellationManager.renderPositiveZFace(0, 0, 0, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.ZPOS)]);
		}
	}
	
	private void renderPositiveYFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, int fillPercentage, double fluidHeight)
	{
		if (fillPercentage > 0 && fluidIcon != null)
		{
			TessellationManager.renderPositiveYFace(0, fluidHeight, 0, 16, 16, fluidIcon);
		}
		
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections[ConnectedTexturesHelper.YPOS])
		{
			TessellationManager.renderPositiveYFace(0, 16, 0, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.YPOS)]);
			// inner face
			TessellationManager.renderNegativeYFace(0, 16, 0, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.YNEG)]);
		}
	}
	
	private void renderNegativeYFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, int fillPercentage)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (fillPercentage > 0 && fluidIcon != null && !connections[ConnectedTexturesHelper.YNEG])
		{
			TessellationManager.renderNegativeYFace(0, 0, 0, 16, 16, fluidIcon);
		}
		
		if (!connections[ConnectedTexturesHelper.YNEG])
		{
			TessellationManager.renderNegativeYFace(0, 0, 0, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.YNEG)]);
			// inner face
			TessellationManager.renderPositiveYFace(0, 0, 0, 16, 16, icons[entity.getTexture(ConnectedTexturesHelper.YPOS)]);
		}
	}
	
	private Icon getFluidTexture(TankBlockEntity entity)
	{
		ValveBlockEntity valve = entity.getValve();
		
		if (valve != null)
		{
			FluidStack fluidStack = valve.getFluid();
			
			if (fluidStack != null)
			{
				return fluidStack.getFluid().getIcon();
			}
		}
		
		return null;
	}
}
