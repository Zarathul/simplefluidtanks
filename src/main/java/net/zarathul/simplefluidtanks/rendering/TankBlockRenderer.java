package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.Direction;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Custom renderer for {@link TankBlock}s.
 */
@SideOnly(Side.CLIENT)
public class TankBlockRenderer extends TileEntitySpecialRenderer
{
	public static final double flickerOffset = 0.001;
	
	/**
	 * Default constructor.
	 */
	public TankBlockRenderer()
	{
		super();
	}

	@Override
	public void renderTileEntityAt(TileEntity entity, double x, double y, double z, float f)
	{
		TankBlockEntity tankEntity = Utils.getTileEntityAt(entity.getWorldObj(), TankBlockEntity.class, entity.xCoord, entity.yCoord, entity.zCoord);
		
		if (tankEntity == null)
		{
			return;
		}
		
		IIcon[] icons = SimpleFluidTanks.tankBlock.getIcons();
		
		TessellationManager.setBaseCoords(x, y, z);
		
		// force the texture atlas that contains blocks
		bindTexture(TextureMap.locationBlocksTexture);
		
		if (!tankEntity.isPartOfTank())
		{
			renderUnlinkedTank(tankEntity, icons[0]);
		}
		else
		{
			boolean[] connections = tankEntity.getConnections();
			int fillPercentage = tankEntity.getFillPercentage();
			double fluidHeight = 16.0 / 100 * fillPercentage;
			double verticalTextureOffset = 16.0 / 100 * (100 - fillPercentage);
			IIcon fluidIcon = getFluidTexture(tankEntity);
			
			renderFluid(tankEntity, connections, fluidIcon, fluidHeight, verticalTextureOffset);
			renderLinkedTank(tankEntity, connections, icons);
		}
	}
	
	/**
	 * Renders a slightly shrinked {@link TankBlock} without connected textures.
	 * @param entity
	 * The {@link TileEntity} data for the {@link TankBlock} to render.
	 * @param icon
	 * The texture to use.
	 */
	private void renderUnlinkedTank(TankBlockEntity entity, IIcon icon)
	{
		TessellationManager.startDrawingQuads();
		TessellationManager.renderCube(1, 1, 1, 14, 14, 14, icon, true, TessellationManager.pixel);
		TessellationManager.draw();
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
	private void renderLinkedTank(TankBlockEntity entity, boolean[] connections, IIcon[] icons)
	{
		TessellationManager.startDrawingQuads();
		
		if (!connections[Direction.XPOS])
		{
			TessellationManager.renderPositiveXFace(16, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.XPOS)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.XPOS))
			{
				TessellationManager.renderNegativeXFace(16 - flickerOffset, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.XNEG)]);
			}
		}
		
		if (!connections[Direction.XNEG])
		{
			TessellationManager.renderNegativeXFace(0, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.XNEG)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.XNEG))
			{
				TessellationManager.renderPositiveXFace(0 + flickerOffset, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.XPOS)]);
			}
		}
		
		if (!connections[Direction.ZPOS])
		{
			TessellationManager.renderPositiveZFace(0, 0, 16, 16, 16, icons[entity.getTextureIndex(Direction.ZPOS)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.ZPOS))
			{
				TessellationManager.renderNegativeZFace(0, 0, 16 - flickerOffset, 16, 16, icons[entity.getTextureIndex(Direction.ZNEG)]);
			}
		}
		
		if (!connections[Direction.ZNEG])
		{
			TessellationManager.renderNegativeZFace(0, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.ZNEG)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.ZNEG))
			{
				TessellationManager.renderPositiveZFace(0, 0, 0 + flickerOffset, 16, 16, icons[entity.getTextureIndex(Direction.ZPOS)]);
			}
		}
		
		if (!connections[Direction.YPOS])
		{
			TessellationManager.renderPositiveYFace(0, 16, 0, 16, 16, icons[entity.getTextureIndex(Direction.YPOS)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.YPOS))
			{
				TessellationManager.renderNegativeYFace(0, 16 - flickerOffset, 0, 16, 16, icons[entity.getTextureIndex(Direction.YNEG)]);
			}
		}
		
		if (!connections[Direction.YNEG])
		{
			TessellationManager.renderNegativeYFace(0, 0, 0, 16, 16, icons[entity.getTextureIndex(Direction.YNEG)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.YNEG))
			{
				TessellationManager.renderPositiveYFace(0, 0 + flickerOffset, 0, 16, 16, icons[entity.getTextureIndex(Direction.YPOS)]);
			}
		}
		
		TessellationManager.draw();
	}
	
	/**
	 * Renders the fluid in the {@link TankBlock}.
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity}.
	 * @param connections
	 * The {@link TankBlock}s connected textures information.
	 * @param fluidIcon
	 * The fluids texture.
	 * @param fluidHeight
	 * The height of the fluid in the {@link TankBlock}.
	 * @param verticalTextureOffset
	 * The vertical offset for the fluids texture.
	 */
	private void renderFluid(TankBlockEntity entity, boolean[] connections, IIcon fluidIcon, double fluidHeight, double verticalTextureOffset)
	{
		if (fluidHeight > 0 && fluidIcon != null)
		{
	        TessellationManager.startDrawingQuads();
			
			IBlockAccess world = entity.getWorldObj();
			
	        if (!connections[Direction.XPOS])
			{
	        	TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord + 1, entity.yCoord, entity.zCoord));
				TessellationManager.renderPositiveXFace(16 - flickerOffset, 0, 0, fluidHeight, 16, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			if (!connections[Direction.XNEG])
			{
				TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord - 1, entity.yCoord, entity.zCoord));
				TessellationManager.renderNegativeXFace(0 + flickerOffset, 0, 0, fluidHeight, 16, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			if (!connections[Direction.ZPOS])
			{
				TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord + 1));
				TessellationManager.renderPositiveZFace(0, 0, 16 - flickerOffset, 16, fluidHeight, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			if (!connections[Direction.ZNEG])
			{
				TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord, entity.zCoord - 1));
				TessellationManager.renderNegativeZFace(0, 0, 0 + flickerOffset, 16, fluidHeight, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			TankBlockEntity tankAbove = Utils.getTileEntityAt(world, TankBlockEntity.class, entity.xCoord, entity.yCoord + 1, entity.zCoord);
			
			if (!connections[Direction.YPOS] || tankAbove == null || tankAbove.getFillPercentage() == 0)
			{
				TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord + 1, entity.zCoord));
				TessellationManager.renderPositiveYFace(0, fluidHeight - flickerOffset, 0, 16, 16, fluidIcon);
			}
			
			if (!connections[Direction.YNEG])
			{
				TessellationManager.setBrightness(SimpleFluidTanks.tankBlock.getMixedBrightnessForBlock(world, entity.xCoord, entity.yCoord - 1, entity.zCoord));
				TessellationManager.renderNegativeYFace(0, 0 + flickerOffset, 0, 16, 16, fluidIcon);
			}
			
			TessellationManager.draw();
		}
	}
	
	/**
	 * Checks if inside of a {@link TankBlock} should be rendered on the specified side.
	 * @param x
	 * The x-coordinate of the {@link TankBlock}.
	 * @param y
	 * The y-coordinate of the {@link TankBlock}.
	 * @param z
	 * The z-coordinate of the {@link TankBlock}.
	 * @param side
	 * The side to check.
	 * @return
	 * <code>true</code> if the inside should be rendered otherwise <code>false</code>.
	 */
	private boolean shouldRenderInside(int x, int y, int z, int side)
	{
		World world = Minecraft.getMinecraft().theWorld;
		
		switch (side)
		{
			case Direction.XPOS:
				return world.isAirBlock(x + 1, y, z);
			case Direction.XNEG:
				return world.isAirBlock(x - 1, y, z);
			case Direction.YPOS:
				return world.isAirBlock(x, y + 1, z);
			case Direction.YNEG:
				return world.isAirBlock(x, y - 1, z);
			case Direction.ZPOS:
				return world.isAirBlock(x, y, z + 1);
			case Direction.ZNEG:
				return world.isAirBlock(x, y, z - 1);
			default:
				return false;
		}
	}
	
	/**
	 * Gets the texture of the fluid inside the multiblock tank structure.
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity} to get the texture for.
	 * @return
	 * The fluids texture or <code>null</code> if the {@link TankBlock} is not linked to a {@link ValveBlock} or the multiblock tank is empty.
	 */
	private IIcon getFluidTexture(TankBlockEntity entity)
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
