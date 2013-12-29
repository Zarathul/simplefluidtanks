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
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_closed.png"),
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_top_open.png"),
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_topBottom_open.png"),
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_topright_open.png"),
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_lefttopright_open.png"),
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_all_open.png"),
			new ResourceLocation("simplefluidtanks", "/textures/blocks/tank_only_corners.png"),
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
		
		Tessellator tsr = Tessellator.instance;
		int brightness = block.getMixedBrightnessForBlock(tileEntity.worldObj, (int)x, (int)y, (int)z);
		tsr.setBrightness(brightness);
		
		TessellationManager.setBaseCoords(x, y, z);
		
		if (!entity.isPartOfTank())
		{
			renderSolid(entity);
		}
		else
		{
			HashMap<String, Boolean> connections = getConnections(entity);
			
			renderPositiveXFace(entity, connections);
		}
	}
	
	private void renderSolid(TankBlockEntity entity)
	{
		TessellationManager.renderCube(2, 0, 2, 12, 14, 12, textureLocations[0], true);
	}
	
	private void renderPositiveXFace(TankBlockEntity entity, HashMap<String, Boolean> connections)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections.get("X+"))
		{
			int[] textureInfo = getConnectedTexture(connections);
			
			Face mapping = new Face();
			int transformations = textureInfo[1]; 
			
			if (transformations - 4 >= 0)
			{
				mapping.rotateRight();
				transformations -= 4;
			}
			if (transformations - 2 >= 0)
			{
				mapping.mirrorV();
				transformations -= 2;
			}
			if (transformations - 1 >= 0)
			{
				mapping.mirrorU();
			}
			
			int z = (connections.get("Z-")) ? 0 : 2;
			int depth = (connections.get("Z+")) ? 16 - z : 14 - z;
			int height = (connections.get("Y+")) ? 16 : 14;
			
//			TessellationManager.renderPositiveXFace(2 + 12, 0, z, height, depth, textureLocations[textureInfo[0]], mapping);
			TessellationManager.renderPositiveXFace(16, 0, 0, 16, 16, textureLocations[textureInfo[0]], mapping);
		}
	}
	
	private int[] getConnectedTexture(HashMap<String, Boolean> connections)
	{
		// first int is the texture index, second a bitmask : 1=mirrorU, 2=mirrorV, 4=rotateRight
		int[] textureInfo = new int[] { 0, 0 };
		
		if (connections.get("Y+") && connections.get("Y-") && connections.get("Z+") && connections.get("Z-"))
		{
			textureInfo[0] = ((connections.get("Z-Y+") && connections.get("Z+Y+") && connections.get("Z+Y-") && connections.get("Z-Y-"))) ? 5 : 6;
			textureInfo[1] = 0;
		}
		else if (connections.get("Y+") && connections.get("Z+") && connections.get("Z-"))
		{
			textureInfo[0] = 4;
			textureInfo[1] = 0;
		}
		else if (connections.get("Z+") && connections.get("Y+") && connections.get("Y-"))
		{
			textureInfo[0] = 4;
			textureInfo[1] = 6;
		}
		else if (connections.get("Y-") && connections.get("Z+") && connections.get("Z-"))
		{
			textureInfo[0] = 4;
			textureInfo[1] = 1;
		}
		else if (connections.get("Z-") && connections.get("Y-") && connections.get("Y+"))
		{
			textureInfo[0] = 4;
			textureInfo[1] = 4;
		}
		else if (connections.get("Y+") && connections.get("Y-"))
		{
			textureInfo[0] = 2;
			textureInfo[1] = 0;
		}
		else if (connections.get("Z+") && connections.get("Z-"))
		{
			textureInfo[0] = 2;
			textureInfo[1] = 4;
		}
		else if (connections.get("Y+") && connections.get("Z+"))
		{
			textureInfo[0] = 3;
			textureInfo[1] = 2;
		}
		else if (connections.get("Z+") && connections.get("Y-"))
		{
			textureInfo[0] = 3;
			textureInfo[1] = 3;
		}
		else if (connections.get("Y-") && connections.get("Z-"))
		{
			textureInfo[0] = 3;
			textureInfo[1] = 4;
		}
		else if (connections.get("Z-") && connections.get("Y+"))
		{
			textureInfo[0] = 3;
			textureInfo[1] = 0;
		}
		else if (connections.get("Y+"))
		{
			textureInfo[0] = 1;
			textureInfo[1] = 0;
		}
		else if (connections.get("Y-"))
		{
			textureInfo[0] = 1;
			textureInfo[1] = 1;
		}
		else if (connections.get("Z+"))
		{
			textureInfo[0] = 1;
			textureInfo[1] = 6;
		}
		else if (connections.get("Z-"))
		{
			textureInfo[0] = 1;
			textureInfo[1] = 4;
		}
		
		return textureInfo;
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
		connections.put("Z+Y-", shouldConnectTo(entity, entity.xCoord, entity.yCoord - 1, entity.zCoord + 1));
		connections.put("Z-Y-", shouldConnectTo(entity, entity.xCoord, entity.yCoord - 1, entity.zCoord - 1));

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
