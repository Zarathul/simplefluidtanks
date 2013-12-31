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
		
		Tessellator tsr = Tessellator.instance;
		int brightness = block.getMixedBrightnessForBlock(tileEntity.worldObj, (int)x, (int)y, (int)z);
		tsr.setBrightness(brightness);
		
		TessellationManager.setBaseCoords(x, y, z);
		
		tsr.startDrawingQuads();
		
		if (!entity.isPartOfTank())
		{
			renderSolid(entity, icons[0]);
		}
		else
		{
			HashMap<String, Boolean> connections = getConnections(entity);
			int fillPercentage = entity.getFillPercentage();
			Icon fluidIcon = FluidRegistry.LAVA.getStillIcon();
			
			renderPositiveXFace(entity, connections, icons, fluidIcon, fillPercentage);
			renderNegativeXFace(entity, connections, icons, fluidIcon, fillPercentage);
			renderPositiveZFace(entity, connections, icons, fluidIcon, fillPercentage);
			renderNegativeZFace(entity, connections, icons, fluidIcon, fillPercentage);
			renderPositiveYFace(entity, connections, icons, fluidIcon, fillPercentage);
			renderNegativeYFace(entity, connections, icons, fluidIcon, fillPercentage);
		}
		
		tsr.draw();
	}
	
	private void renderSolid(TankBlockEntity entity, Icon icon)
	{
		TessellationManager.renderCube(1, 0, 1, 14, 14, 14, icon, true);
	}
	
	private void renderPositiveXFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons, Icon fluidIcon, int fillPercentage)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("X+"))
		{
			int textureIndex = getConnectedTexture(connections, 3);
			TessellationManager.renderPositiveXFace(16, 0, 0, 16, 16, icons[textureIndex]);
			// inner face
			textureIndex = getConnectedTexture(connections, 1);
			TessellationManager.renderNegativeXFace(15.01, 0, 0, 16, 16, icons[textureIndex]);
			
			if (fillPercentage > 0)
			{
				double fluidZ = (connections.get("Z-")) ? 0.0 : 1.0;
				double fluidDepth = ((connections.get("Z+")) ? 16.0 : 15.0) - fluidZ;
				double fluidY = (connections.get("Y-")) ? 0.0 : 1.0;
				double fluidHeight = 16.0 / 100 * fillPercentage;
				fluidHeight -= fluidY;
				fluidHeight = ((!connections.get("Y+")) ? Math.min(fluidHeight, 15) : fluidHeight);
				
				TessellationManager.renderPositiveXFace(15, fluidY, fluidZ, fluidHeight, fluidDepth, fluidIcon);
			}
		}
	}
	
	private void renderNegativeXFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons, Icon fluidIcon, int fillPercentage)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("X-"))
		{
			int textureIndex = getConnectedTexture(connections, 1);
			TessellationManager.renderNegativeXFace(0, 0, 0, 16, 16, icons[textureIndex]);
			// inner face
			textureIndex = getConnectedTexture(connections, 3);
			TessellationManager.renderPositiveXFace(0.99d, 0, 0, 16, 16, icons[textureIndex]);
			
			if (fillPercentage > 0)
			{
				double fluidZ = (connections.get("Z-")) ? 0.0 : 1.0;
				double fluidDepth = ((connections.get("Z+")) ? 16.0 : 15.0) - fluidZ;
				double fluidY = (connections.get("Y-")) ? 0.0 : 1.0;
				double fluidHeight = 16.0 / 100 * fillPercentage;
				fluidHeight -= fluidY;
				fluidHeight = ((!connections.get("Y+")) ? Math.min(fluidHeight, 15) : fluidHeight);
				
				TessellationManager.renderNegativeXFace(1, fluidY, fluidZ, fluidHeight, fluidDepth, fluidIcon);
			}
		}
	}
	
	private void renderPositiveZFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons, Icon fluidIcon, int fillPercentage)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("Z+"))
		{
			int textureIndex = getConnectedTexture(connections, 0);
			TessellationManager.renderPositiveZFace(0, 0, 16, 16, 16, icons[textureIndex]);
			// inner face
			textureIndex = getConnectedTexture(connections, 2);
			TessellationManager.renderNegativeZFace(0, 0, 15.01d, 16, 16, icons[textureIndex]);
			
			if (fillPercentage > 0)
			{
				double fluidX = (connections.get("X-")) ? 0.0 : 1.0;
				double fluidWidth = ((connections.get("X+")) ? 16.0 : 15.0) - fluidX;
				double fluidY = (connections.get("Y-")) ? 0.0 : 1.0;
				double fluidHeight = 16.0 / 100 * fillPercentage;
				fluidHeight -= fluidY;
				fluidHeight = ((!connections.get("Y+")) ? Math.min(fluidHeight, 15) : fluidHeight);
				
				TessellationManager.renderPositiveZFace(fluidX, fluidY, 15, fluidWidth, fluidHeight, fluidIcon);
			}
		}
	}
	
	private void renderNegativeZFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons, Icon fluidIcon, int fillPercentage)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("Z-"))
		{
			int textureIndex = getConnectedTexture(connections, 2);
			TessellationManager.renderNegativeZFace(0, 0, 0, 16, 16, icons[textureIndex]);
			// inner face
			textureIndex = getConnectedTexture(connections, 0);
			TessellationManager.renderPositiveZFace(0, 0, 0.99d, 16, 16, icons[textureIndex]);
			
			if (fillPercentage > 0)
			{
				double fluidX = (connections.get("X-")) ? 0.0 : 1.0;
				double fluidWidth = ((connections.get("X+")) ? 16.0 : 15.0) - fluidX;
				double fluidY = (connections.get("Y-")) ? 0.0 : 1.0;
				double fluidHeight = 16.0 / 100 * fillPercentage;
				fluidHeight -= fluidY;
				fluidHeight = ((!connections.get("Y+")) ? Math.min(fluidHeight, 15) : fluidHeight);
				
				TessellationManager.renderNegativeZFace(fluidX, fluidY, 1, fluidWidth, fluidHeight, fluidIcon);
			}
		}
	}
	
	private void renderPositiveYFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons, Icon fluidIcon, int fillPercentage)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("Y+"))
		{
			int textureIndex = getConnectedTexture(connections, 5);
			TessellationManager.renderPositiveYFace(0, 16, 0, 16, 16, icons[textureIndex]);
			// inner face
			textureIndex = getConnectedTexture(connections, 4);
			TessellationManager.renderNegativeYFace(0, 15.01d, 0, 16, 16, icons[textureIndex]);
		}
		
		if (fillPercentage > 0 && !(fillPercentage >= 100 && connections.get("Y+")))
		{
			double fluidZ = (connections.get("Z-")) ? 0.0 : 1.0;
			double fluidDepth = ((connections.get("Z+")) ? 16.0 : 15.0) - fluidZ;
			double fluidX = (connections.get("X-")) ? 0.0 : 1.0;
			double fluidWidth = ((connections.get("X+")) ? 16.0 : 15.0) - fluidX;
			
			double fluidHeight = 16.0 / 100 * fillPercentage;
			double fluidY = ((!connections.get("Y+")) ? Math.min(fluidHeight, 15) : fluidHeight);
			
			TessellationManager.renderPositiveYFace(fluidX, fluidY, fluidZ, fluidWidth, fluidDepth, fluidIcon);
		}
	}
	
	private void renderNegativeYFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons, Icon fluidIcon, int fillPercentage)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("Y-"))
		{
			int textureIndex = getConnectedTexture(connections, 4);
			TessellationManager.renderNegativeYFace(0, 0, 0, 16, 16, icons[textureIndex]);
			// inner face
			textureIndex = getConnectedTexture(connections, 5);
			TessellationManager.renderPositiveYFace(0, 0.99d, 0, 16, 16, icons[textureIndex]);
		}
		
		if (!connections.get("Y-"))
		{
			double fluidZ = (connections.get("Z-")) ? 0.0 : 1.0;
			double fluidDepth = ((connections.get("Z+")) ? 16.0 : 15.0) - fluidZ;
			double fluidX = (connections.get("X-")) ? 0.0 : 1.0;
			double fluidWidth = ((connections.get("X+")) ? 16.0 : 15.0) - fluidX;
			
			TessellationManager.renderNegativeYFace(fluidX, 1.0, fluidZ, fluidWidth, fluidDepth, fluidIcon);
		}
		
	}
	
	// direction: 0 = Z+, 1 = X-, 2 = Z-, 3 = X+, 4 = Y-, 5 = Y+
	private int getConnectedTexture(HashMap<String, Boolean> connections, int direction)
	{
		int textureIndex = 0;
		
		switch (direction)
		{
			case 0:
				textureIndex = getPositiveZTexture(connections);
			break;
			
			case 1:
				textureIndex = getNegativeXTexture(connections);
			break;
			
			case 2:
				textureIndex = getNegativeZTexture(connections);
			break;
			
			case 3:
				textureIndex = getPositiveXTexture(connections);
			break;
			
			case 4:
				textureIndex = getNegativeYTexture(connections);
			break;
			case 5:
				textureIndex = getPositiveYTexture(connections);
			break;
		}
		
		return textureIndex;
	}
	
	private int getPositiveXTexture(HashMap<String, Boolean> connections)
	{
		int textureIndex = 0;
		
		if (connections.get("Y+") && connections.get("Y-") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 1;
		}
		else if (connections.get("Y+") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 8;
		}
		else if (connections.get("Z+") && connections.get("Y+") && connections.get("Y-"))
		{
			textureIndex = 11;
		}
		else if (connections.get("Y-") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 10;
		}
		else if (connections.get("Z-") && connections.get("Y-") && connections.get("Y+"))
		{
			textureIndex = 9;
		}
		else if (connections.get("Y+") && connections.get("Y-"))
		{
			textureIndex = 2;
		}
		else if (connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 3;
		}
		else if (connections.get("Y+") && connections.get("Z+"))
		{
			textureIndex = 7;
		}
		else if (connections.get("Z+") && connections.get("Y-"))
		{
			textureIndex = 6;
		}
		else if (connections.get("Y-") && connections.get("Z-"))
		{
			textureIndex = 5;
		}
		else if (connections.get("Z-") && connections.get("Y+"))
		{
			textureIndex = 4;
		}
		else if (connections.get("Y+"))
		{
			textureIndex = 12;
		}
		else if (connections.get("Y-"))
		{
			textureIndex = 13;
		}
		else if (connections.get("Z+"))
		{
			textureIndex = 14;
		}
		else if (connections.get("Z-"))
		{
			textureIndex = 15;
		}
		
		return textureIndex;
	}
	
	private int getNegativeXTexture(HashMap<String, Boolean> connections)
	{
		int textureIndex = 0;
		
		if (connections.get("Y+") && connections.get("Y-") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 1;
		}
		else if (connections.get("Y+") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 8;
		}
		else if (connections.get("Z+") && connections.get("Y+") && connections.get("Y-"))
		{
			textureIndex = 9;
		}
		else if (connections.get("Y-") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 10;
		}
		else if (connections.get("Z-") && connections.get("Y-") && connections.get("Y+"))
		{
			textureIndex = 11;
		}
		else if (connections.get("Y+") && connections.get("Y-"))
		{
			textureIndex = 2;
		}
		else if (connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 3;
		}
		else if (connections.get("Y+") && connections.get("Z+"))
		{
			textureIndex = 4;
		}
		else if (connections.get("Z+") && connections.get("Y-"))
		{
			textureIndex = 5;
		}
		else if (connections.get("Y-") && connections.get("Z-"))
		{
			textureIndex = 6;
		}
		else if (connections.get("Z-") && connections.get("Y+"))
		{
			textureIndex = 7;
		}
		else if (connections.get("Y+"))
		{
			textureIndex = 12;
		}
		else if (connections.get("Y-"))
		{
			textureIndex = 13;
		}
		else if (connections.get("Z+"))
		{
			textureIndex = 15;
		}
		else if (connections.get("Z-"))
		{
			textureIndex = 14;
		}
		
		return textureIndex;
	}
	
	private int getPositiveZTexture(HashMap<String, Boolean> connections)
	{
		int textureIndex = 0;
		
		if (connections.get("Y+") && connections.get("Y-") && connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 1;
		}
		else if (connections.get("Y+") && connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 8;
		}
		else if (connections.get("X+") && connections.get("Y+") && connections.get("Y-"))
		{
			textureIndex = 9;
		}
		else if (connections.get("Y-") && connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 10;
		}
		else if (connections.get("X-") && connections.get("Y-") && connections.get("Y+"))
		{
			textureIndex = 11;
		}
		else if (connections.get("Y+") && connections.get("Y-"))
		{
			textureIndex = 2;
		}
		else if (connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 3;
		}
		else if (connections.get("Y+") && connections.get("X+"))
		{
			textureIndex = 4;
		}
		else if (connections.get("X+") && connections.get("Y-"))
		{
			textureIndex = 5;
		}
		else if (connections.get("Y-") && connections.get("X-"))
		{
			textureIndex = 6;
		}
		else if (connections.get("X-") && connections.get("Y+"))
		{
			textureIndex = 7;
		}
		else if (connections.get("Y+"))
		{
			textureIndex = 12;
		}
		else if (connections.get("Y-"))
		{
			textureIndex = 13;
		}
		else if (connections.get("X+"))
		{
			textureIndex = 15;
		}
		else if (connections.get("X-"))
		{
			textureIndex = 14;
		}
		
		return textureIndex;
	}
	
	private int getNegativeZTexture(HashMap<String, Boolean> connections)
	{
		int textureIndex = 0;
		
		if (connections.get("Y+") && connections.get("Y-") && connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 1;
		}
		else if (connections.get("Y+") && connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 8;
		}
		else if (connections.get("X+") && connections.get("Y+") && connections.get("Y-"))
		{
			textureIndex = 11;
		}
		else if (connections.get("Y-") && connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 10;
		}
		else if (connections.get("X-") && connections.get("Y-") && connections.get("Y+"))
		{
			textureIndex = 9;
		}
		else if (connections.get("Y+") && connections.get("Y-"))
		{
			textureIndex = 2;
		}
		else if (connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 3;
		}
		else if (connections.get("Y+") && connections.get("X+"))
		{
			textureIndex = 7;
		}
		else if (connections.get("X+") && connections.get("Y-"))
		{
			textureIndex = 6;
		}
		else if (connections.get("Y-") && connections.get("X-"))
		{
			textureIndex = 5;
		}
		else if (connections.get("X-") && connections.get("Y+"))
		{
			textureIndex = 4;
		}
		else if (connections.get("Y+"))
		{
			textureIndex = 12;
		}
		else if (connections.get("Y-"))
		{
			textureIndex = 13;
		}
		else if (connections.get("X+"))
		{
			textureIndex = 14;
		}
		else if (connections.get("X-"))
		{
			textureIndex = 15;
		}
		
		return textureIndex;
	}
	
	private int getPositiveYTexture(HashMap<String, Boolean> connections)
	{
		int textureIndex = 0;
		
		if (connections.get("X+") && connections.get("X-") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 1;
		}
		else if (connections.get("X+") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 11;
		}
		else if (connections.get("Z+") && connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 8;
		}
		else if (connections.get("X-") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 9;
		}
		else if (connections.get("Z-") && connections.get("X-") && connections.get("X+"))
		{
			textureIndex = 10;
		}
		else if (connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 3;
		}
		else if (connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 2;
		}
		else if (connections.get("X+") && connections.get("Z+"))
		{
			textureIndex = 7;
		}
		else if (connections.get("Z+") && connections.get("X-"))
		{
			textureIndex = 4;
		}
		else if (connections.get("X-") && connections.get("Z-"))
		{
			textureIndex = 5;
		}
		else if (connections.get("Z-") && connections.get("X+"))
		{
			textureIndex = 6;
		}
		else if (connections.get("X+"))
		{
			textureIndex = 14;
		}
		else if (connections.get("X-"))
		{
			textureIndex = 15;
		}
		else if (connections.get("Z+"))
		{
			textureIndex = 12;
		}
		else if (connections.get("Z-"))
		{
			textureIndex = 13;
		}
		
		return textureIndex;
	}
	
	private int getNegativeYTexture(HashMap<String, Boolean> connections)
	{
		int textureIndex = 0;
		
		if (connections.get("X+") && connections.get("X-") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 1;
		}
		else if (connections.get("X+") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 11;
		}
		else if (connections.get("Z+") && connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 10;
		}
		else if (connections.get("X-") && connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 9;
		}
		else if (connections.get("Z-") && connections.get("X-") && connections.get("X+"))
		{
			textureIndex = 8;
		}
		else if (connections.get("X+") && connections.get("X-"))
		{
			textureIndex = 3;
		}
		else if (connections.get("Z+") && connections.get("Z-"))
		{
			textureIndex = 2;
		}
		else if (connections.get("X+") && connections.get("Z+"))
		{
			textureIndex = 6;
		}
		else if (connections.get("Z+") && connections.get("X-"))
		{
			textureIndex = 5;
		}
		else if (connections.get("X-") && connections.get("Z-"))
		{
			textureIndex = 4;
		}
		else if (connections.get("Z-") && connections.get("X+"))
		{
			textureIndex = 7;
		}
		else if (connections.get("X+"))
		{
			textureIndex = 14;
		}
		else if (connections.get("X-"))
		{
			textureIndex = 15;
		}
		else if (connections.get("Z+"))
		{
			textureIndex = 13;
		}
		else if (connections.get("Z-"))
		{
			textureIndex = 12;
		}
		
		return textureIndex;
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
	
	private HashMap<String, Boolean> getConnections(TankBlockEntity entity)
	{
		HashMap<String, Boolean> connections = new HashMap<String, Boolean>(10);
		connections.put("X+", shouldConnectTo(entity, entity.xCoord + 1, entity.yCoord, entity.zCoord));
		connections.put("X-", shouldConnectTo(entity, entity.xCoord - 1, entity.yCoord, entity.zCoord));
		connections.put("Y+", shouldConnectTo(entity, entity.xCoord, entity.yCoord + 1, entity.zCoord));
		connections.put("Y-", shouldConnectTo(entity, entity.xCoord, entity.yCoord - 1, entity.zCoord));
		connections.put("Z+", shouldConnectTo(entity, entity.xCoord, entity.yCoord, entity.zCoord + 1));
		connections.put("Z-", shouldConnectTo(entity, entity.xCoord, entity.yCoord, entity.zCoord - 1));

		connections.put("Z-Y+", shouldConnectTo(entity, entity.xCoord, entity.yCoord + 1, entity.zCoord - 1));
		connections.put("Z+Y+", shouldConnectTo(entity, entity.xCoord, entity.yCoord + 1, entity.zCoord + 1));
		connections.put("Z-Y-", shouldConnectTo(entity, entity.xCoord, entity.yCoord - 1, entity.zCoord - 1));
		connections.put("Z+Y-", shouldConnectTo(entity, entity.xCoord, entity.yCoord - 1, entity.zCoord + 1));
		
		connections.put("X-Y+", shouldConnectTo(entity, entity.xCoord - 1, entity.yCoord + 1, entity.zCoord));
		connections.put("X+Y+", shouldConnectTo(entity, entity.xCoord + 1, entity.yCoord + 1, entity.zCoord));
		connections.put("X-Y-", shouldConnectTo(entity, entity.xCoord - 1, entity.yCoord - 1, entity.zCoord));
		connections.put("X+Y-", shouldConnectTo(entity, entity.xCoord + 1, entity.yCoord - 1, entity.zCoord));
		
		connections.put("X-Z+", shouldConnectTo(entity, entity.xCoord - 1, entity.yCoord, entity.zCoord + 1));
		connections.put("X+Z+", shouldConnectTo(entity, entity.xCoord + 1, entity.yCoord, entity.zCoord + 1));
		connections.put("X-Z-", shouldConnectTo(entity, entity.xCoord - 1, entity.yCoord, entity.zCoord - 1));
		connections.put("X+Z-", shouldConnectTo(entity, entity.xCoord + 1, entity.yCoord, entity.zCoord - 1));

		return connections;
	}
	
	private boolean shouldConnectTo(TankBlockEntity entity, int x, int y, int z)
	{
		// only check adjacent blocks
		if (x < entity.xCoord - 1 || x > entity.xCoord + 1 || y < entity.yCoord - 1 || y > entity.yCoord + 1 || z < entity.zCoord - 1 || z > entity.zCoord + 1)
		{
			return false;
		}
		
		int neighborBlockId = entity.worldObj.getBlockId(x, y, z);
		
		if (neighborBlockId == SimpleFluidTanks.tankBlockId)
		{
			TileEntity neighborEntity = entity.worldObj.getBlockTileEntity(x, y, z);
			
			if (neighborEntity == null || !(neighborEntity instanceof TankBlockEntity))
			{
				LogWrapper.log.severe("Possible map corruption detected. TankBlockEntity missing at x:%d / y:%d / z:%d. Expect severe rendering and tank logic issues.", x, y, z);
				return false;
			}
			
			TankBlockEntity connectionCandidate = (TankBlockEntity)neighborEntity;
			
			return (connectionCandidate.isPartOfTank() && connectionCandidate.isSameValve(entity.getValveCoords()));
		}
		
		return false;
	}
}
