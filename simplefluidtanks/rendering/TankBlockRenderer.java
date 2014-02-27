package simplefluidtanks.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import simplefluidtanks.blocks.TankBlock;
import simplefluidtanks.blocks.ValveBlock;
import simplefluidtanks.common.Direction;
import simplefluidtanks.common.Utils;
import simplefluidtanks.tileentities.TankBlockEntity;
import simplefluidtanks.tileentities.ValveBlockEntity;
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
		
		TankBlock tank = (TankBlock)tankEntity.getBlockType();
		
		Icon[] icons = tank.getIcons();
		
		TessellationManager.setBaseCoords(x, y, z);
		Tessellator tsr = Tessellator.instance;
		
		int brightness = tank.getMixedBrightnessForBlock(tankEntity.worldObj, tankEntity.xCoord, tankEntity.yCoord, tankEntity.zCoord);
		tsr.setBrightness(brightness);
		
		// force the texture atlas that contains blocks
		bindTexture(TextureMap.locationBlocksTexture);
		
		tsr.startDrawingQuads();
		
		if (!tankEntity.isPartOfTank())
		{
			renderSolid(tankEntity, icons[0]);
		}
		else
		{
			boolean[] connections = tankEntity.getConnections();
			int fillPercentage = tankEntity.getFillPercentage();
			double fluidHeight = 16.0 / 100 * fillPercentage;
			double verticalTextureOffset = 16.0 / 100 * (100 - fillPercentage);
			Icon fluidIcon = getFluidTexture(tankEntity);
			
			renderPositiveXFace(tankEntity, connections, icons, fluidIcon, fluidHeight, verticalTextureOffset);
			renderNegativeXFace(tankEntity, connections, icons, fluidIcon, fluidHeight, verticalTextureOffset);
			renderPositiveZFace(tankEntity, connections, icons, fluidIcon, fluidHeight, verticalTextureOffset);
			renderNegativeZFace(tankEntity, connections, icons, fluidIcon, fluidHeight, verticalTextureOffset);
			renderPositiveYFace(tankEntity, connections, icons, fluidIcon, fluidHeight);
			renderNegativeYFace(tankEntity, connections, icons, fluidIcon, fillPercentage);
		}
		
		tsr.draw();
	}
	
	/**
	 * Renders a slightly shrinked {@link TankBlock} including the inside.
	 * @param entity
	 * The {@link TileEntity} data for the {@link TankBlock} to render.
	 * @param icon
	 * The texture to use.
	 */
	private void renderSolid(TankBlockEntity entity, Icon icon)
	{
		TessellationManager.renderCube(1, 1, 1, 14, 14, 14, icon, true, TessellationManager.pixel);
	}
	
	/**
	 * Renders the positive x side of the {@link TankBlock}.
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity}.
	 * @param connections
	 * The {@link TankBlock}s connected textures information.
	 * @param icons
	 * The {@link TankBlock}s textures.
	 * @param fluidIcon
	 * The fluids texture.
	 * @param fluidHeight
	 * The height of the fluid in the {@link TankBlock}.
	 * @param verticalTextureOffset
	 * The vertical offset for the fluids texture.
	 */
	private void renderPositiveXFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, double fluidHeight, double verticalTextureOffset)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections[Direction.XPOS])
		{
			if (fluidHeight > 0 && fluidIcon != null)
			{
				TessellationManager.renderPositiveXFace(16 - flickerOffset, 0, 0, fluidHeight, 16, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			TessellationManager.renderPositiveXFace(16, 0, 0, 16, 16, icons[entity.getTexture(Direction.XPOS)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.XPOS))
			{
				TessellationManager.renderNegativeXFace(16 - flickerOffset, 0, 0, 16, 16, icons[entity.getTexture(Direction.XNEG)]);
			}
		}
	}
	
	/**
	 * Renders the negative x side of the {@link TankBlock}.
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity}.
	 * @param connections
	 * The {@link TankBlock}s connected textures information.
	 * @param icons
	 * The {@link TankBlock}s textures.
	 * @param fluidIcon
	 * The fluids texture.
	 * @param fluidHeight
	 * The height of the fluid in the {@link TankBlock}.
	 * @param verticalTextureOffset
	 * The vertical offset for the fluids texture.
	 */
	private void renderNegativeXFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, double fluidHeight, double verticalTextureOffset)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections[Direction.XNEG])
		{
			if (fluidHeight > 0 && fluidIcon != null)
			{
				TessellationManager.renderNegativeXFace(0 + flickerOffset, 0, 0, fluidHeight, 16, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			TessellationManager.renderNegativeXFace(0, 0, 0, 16, 16, icons[entity.getTexture(Direction.XNEG)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.XNEG))
			{
				TessellationManager.renderPositiveXFace(0 + flickerOffset, 0, 0, 16, 16, icons[entity.getTexture(Direction.XPOS)]);
			}
		}
	}
	
	/**
	 * Renders the positive z side of the {@link TankBlock}.
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity}.
	 * @param connections
	 * The {@link TankBlock}s connected textures information.
	 * @param icons
	 * The {@link TankBlock}s textures.
	 * @param fluidIcon
	 * The fluids texture.
	 * @param fluidHeight
	 * The height of the fluid in the {@link TankBlock}.
	 * @param verticalTextureOffset
	 * The vertical offset for the fluids texture.
	 */
	private void renderPositiveZFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, double fluidHeight, double verticalTextureOffset)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections[Direction.ZPOS])
		{
			if (fluidHeight > 0 && fluidIcon != null)
			{
				TessellationManager.renderPositiveZFace(0, 0, 16 - flickerOffset, 16, fluidHeight, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			TessellationManager.renderPositiveZFace(0, 0, 16, 16, 16, icons[entity.getTexture(Direction.ZPOS)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.ZPOS))
			{
				TessellationManager.renderNegativeZFace(0, 0, 16 - flickerOffset, 16, 16, icons[entity.getTexture(Direction.ZNEG)]);
			}
		}
	}
	
	/**
	 * Renders the negative z side of the {@link TankBlock}.
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity}.
	 * @param connections
	 * The {@link TankBlock}s connected textures information.
	 * @param icons
	 * The {@link TankBlock}s textures.
	 * @param fluidIcon
	 * The fluids texture.
	 * @param fluidHeight
	 * The height of the fluid in the {@link TankBlock}.
	 * @param verticalTextureOffset
	 * The vertical offset for the fluids texture.
	 */
	private void renderNegativeZFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, double fluidHeight, double verticalTextureOffset)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections[Direction.ZNEG])
		{
			if (fluidHeight > 0 && fluidIcon != null)
			{
				TessellationManager.renderNegativeZFace(0, 0, 0 + flickerOffset, 16, fluidHeight, 0, verticalTextureOffset, 0, 0, fluidIcon, TessellationManager.pixel);
			}
			
			TessellationManager.renderNegativeZFace(0, 0, 0, 16, 16, icons[entity.getTexture(Direction.ZNEG)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.ZNEG))
			{
				TessellationManager.renderPositiveZFace(0, 0, 0 + flickerOffset, 16, 16, icons[entity.getTexture(Direction.ZPOS)]);
			}
		}
	}
	
	/**
	 * Renders the positive y side of the {@link TankBlock}.
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity}.
	 * @param connections
	 * The {@link TankBlock}s connected textures information.
	 * @param icons
	 * The {@link TankBlock}s textures.
	 * @param fluidIcon
	 * The fluids texture.
	 * @param fluidHeight
	 * The height of the fluid in the {@link TankBlock}.
	 */
	private void renderPositiveYFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, double fluidHeight)
	{
		if (fluidHeight > 0 && fluidIcon != null)
		{
			TessellationManager.renderPositiveYFace(0, fluidHeight - flickerOffset, 0, 16, 16, fluidIcon);
		}
		
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (!connections[Direction.YPOS])
		{
			TessellationManager.renderPositiveYFace(0, 16, 0, 16, 16, icons[entity.getTexture(Direction.YPOS)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.YPOS))
			{
				TessellationManager.renderNegativeYFace(0, 16 - flickerOffset, 0, 16, 16, icons[entity.getTexture(Direction.YNEG)]);
			}
		}
	}
	
	/**
	 * Renders the negative y side of the {@link TankBlock}.
	 * @param entity
	 * The {@link TankBlock}s {@link TileEntity}.
	 * @param connections
	 * The {@link TankBlock}s connected textures information.
	 * @param icons
	 * The {@link TankBlock}s textures.
	 * @param fluidIcon
	 * The fluids texture.
	 * @param fillPercentage
	 * The {@link TankBlock}s filling level.
	 */
	private void renderNegativeYFace(TankBlockEntity entity, boolean[] connections, Icon[] icons, Icon fluidIcon, int fillPercentage)
	{
		// only render this side if there isn't a tank block from the same tank in front of it 
		if (fillPercentage > 0 && fluidIcon != null && !connections[Direction.YNEG])
		{
			TessellationManager.renderNegativeYFace(0, 0 + flickerOffset, 0, 16, 16, fluidIcon);
		}
		
		if (!connections[Direction.YNEG])
		{
			TessellationManager.renderNegativeYFace(0, 0, 0, 16, 16, icons[entity.getTexture(Direction.YNEG)]);
			// inner face
			if (shouldRenderInside(entity.xCoord, entity.yCoord, entity.zCoord, Direction.YNEG))
			{
				TessellationManager.renderPositiveYFace(0, 0 + flickerOffset, 0, 16, 16, icons[entity.getTexture(Direction.YPOS)]);
			}
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
