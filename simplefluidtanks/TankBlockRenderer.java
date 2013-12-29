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
	private final ResourceLocation[] textureLocations;
	
	public TankBlockRenderer()
	{
		super();
		
		textureLocations = new ResourceLocation[]
		{
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_closed.png"),				//  0
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_open.png"),					//  1

			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_top_bottom.png"),			//  2
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_left_right.png"),			//  3

			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_top_right.png"),			//  4
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_bottom_right.png"),			//  5
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_bottom_left.png"),			//  6
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_top_left.png"),				//  7

			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_left_right_top.png"),		//  8
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_top_bottom_right.png"),		//  9
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_left_right_bottom.png"),	// 10
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_top_bottom_left.png"),		// 11
			
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_top.png"),					// 12
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_bottom.png"),				// 13
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_left.png"),					// 14
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_right.png"),				// 15
		};
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
			
			renderPositiveXFace(entity, connections, icons);
			renderNegativeXFace(entity, connections, icons);
			renderPositiveZFace(entity, connections, icons);
			renderNegativeZFace(entity, connections, icons);
			renderPositiveYFace(entity, connections, icons);
			renderNegativeYFace(entity, connections, icons);
		}
		
		tsr.draw();
	}
	
	private void renderSolid(TankBlockEntity entity, Icon icon)
	{
		TessellationManager.renderCube(2, 0, 2, 12, 14, 12, icon, true);
	}
	
	private void renderPositiveXFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("X+"))
		{
			int textureIndex = getConnectedTexture(connections, 3);
			TessellationManager.renderPositiveXFace(16, 0, 0, 16, 16, icons[textureIndex]);
			
			textureIndex = getConnectedTexture(connections, 1);
			TessellationManager.renderNegativeXFace(16, 0, 0, 16, 16, icons[textureIndex]);
		}
	}
	
	private void renderNegativeXFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("X-"))
		{
			int textureIndex = getConnectedTexture(connections, 1);
			TessellationManager.renderNegativeXFace(0, 0, 0, 16, 16, icons[textureIndex]);
			
			textureIndex = getConnectedTexture(connections, 3);
			TessellationManager.renderPositiveXFace(0, 0, 0, 16, 16, icons[textureIndex]);
		}
	}
	
	private void renderPositiveZFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("Z+"))
		{
			int textureIndex = getConnectedTexture(connections, 0);
			TessellationManager.renderPositiveZFace(0, 0, 16, 16, 16, icons[textureIndex]);
			
			textureIndex = getConnectedTexture(connections, 2);
			TessellationManager.renderNegativeZFace(0, 0, 16, 16, 16, icons[textureIndex]);		}
	}
	
	private void renderNegativeZFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("Z-"))
		{
			int textureIndex = getConnectedTexture(connections, 2);
			TessellationManager.renderNegativeZFace(0, 0, 0, 16, 16, icons[textureIndex]);
			
			textureIndex = getConnectedTexture(connections, 0);
			TessellationManager.renderPositiveZFace(0, 0, 0, 16, 16, icons[textureIndex]);
		}
	}
	
	private void renderPositiveYFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("Y+"))
		{
			int textureIndex = getConnectedTexture(connections, 5);
			TessellationManager.renderPositiveYFace(0, 16, 0, 16, 16, icons[textureIndex]);
			
			textureIndex = getConnectedTexture(connections, 4);
			TessellationManager.renderNegativeYFace(0, 16, 0, 16, 16, icons[textureIndex]);
		}
	}
	
	private void renderNegativeYFace(TankBlockEntity entity, HashMap<String, Boolean> connections, Icon[] icons)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("Y-"))
		{
			int textureIndex = getConnectedTexture(connections, 4);
			TessellationManager.renderNegativeYFace(0, 0, 0, 16, 16, icons[textureIndex]);
			
			textureIndex = getConnectedTexture(connections, 5);
			TessellationManager.renderPositiveYFace(0, 0, 0, 16, 16, icons[textureIndex]);
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
