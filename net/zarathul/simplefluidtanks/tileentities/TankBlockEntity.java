package net.zarathul.simplefluidtanks.tileentities;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.BlockCoords;
import net.zarathul.simplefluidtanks.common.Direction;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.rendering.ConnectedTexturesHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Holds {@link TileEntity} data for {@link TankBlock}s,
 */
public class TankBlockEntity extends TileEntity
{
	/**
	 * The filling level of the tank in percent.
	 */
	private int fillPercentage;
	
	/**
	 * Indicates if the {@link TankBlock} is part of a multiblock tank aka. connected to a {@link ValveBlock}.
	 */
	private boolean isPartOfTank;
	
	/**
	 * The coordinates of the {@link ValveBlock} the {@link TankBlock} is connected to.
	 */
	private BlockCoords valveCoords;
	
	/**
	 * The ids of the textures to use when rendering.
	 */
	private int[] textureIds;
	private boolean[] connections;
	
	/**
	 * Default constructor.
	 */
	public TankBlockEntity()
	{
		fillPercentage = 0;
		isPartOfTank = false;
		valveCoords = null;
		textureIds = new int[] { 0, 0, 0, 0, 0, 0 };
		connections = new boolean[6];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		int renderDistance = Minecraft.getMinecraft().gameSettings.renderDistance;
		
		return Math.max(400, Math.pow(64, (3 - renderDistance)));
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		
		fillPercentage = tag.getByte("FillPercentage");
		isPartOfTank = tag.getBoolean("isPartOfTank");
		
		if (isPartOfTank)
		{
			int[] valveCoordsArray = tag.getIntArray("ValveCoords");
			valveCoords = new BlockCoords(valveCoordsArray[0], valveCoordsArray[1], valveCoordsArray[2]);
		}
		
		textureIds = tag.getIntArray("TextureIds");
		connections = new boolean[6];
		connections[Direction.XPOS] = tag.getBoolean("X+");
		connections[Direction.XNEG] = tag.getBoolean("X-");
		connections[Direction.YPOS] = tag.getBoolean("Y+");
		connections[Direction.YNEG] = tag.getBoolean("Y-");
		connections[Direction.ZPOS] = tag.getBoolean("Z+");
		connections[Direction.ZNEG] = tag.getBoolean("Z-");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		
		tag.setByte("FillPercentage", (byte)fillPercentage);
		tag.setBoolean("isPartOfTank", isPartOfTank);
		
		if (valveCoords != null)
		{
			int[] valveCoordsArray = new int[] { valveCoords.x, valveCoords.y, valveCoords.z };
			tag.setIntArray("ValveCoords", valveCoordsArray);
		}
		
		tag.setIntArray("TextureIds", textureIds);
		tag.setBoolean("X+", connections[Direction.XPOS]);
		tag.setBoolean("X-", connections[Direction.XNEG]);
		tag.setBoolean("Y+", connections[Direction.YPOS]);
		tag.setBoolean("Y-", connections[Direction.YNEG]);
		tag.setBoolean("Z+", connections[Direction.ZPOS]);
		tag.setBoolean("Z-", connections[Direction.ZNEG]);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		readFromNBT(packet.data);
	}
	
	/**
	 * Checks if the {@link TankBlock} is part of a multiblock tank.
	 * @return
	 * <code>true</code> if the {@link TankBlock} is part of a multiblock tank, otherwise false.
	 */
	public boolean isPartOfTank()
	{
		return isPartOfTank && valveCoords != null;
	}
	
	/**
	 * Gets the {@link ValveBlock}s {@link TileEntity} the {@link TankBlock} is linked to.
	 * @return
	 * The valves {@link TileEntity}<br>
	 * or<br>
	 * <code>null</code> if the {@link TankBlock} is not linked to a {@link ValveBlock}.
	 */
	public ValveBlockEntity getValve()
	{
		if (isPartOfTank())
		{
			return Utils.getTileEntityAt(worldObj, ValveBlockEntity.class, valveCoords);
		}
		
		return null;
	}
	
	/**
	 * Links the {@link TankBlock} to a {@link ValveBlock}.
	 * @param coords
	 * The coordinates of the {@link ValveBlock}.
	 * @return
	 * <code>true</code> if linking succeeded, otherwise <code>false</code>.
	 */
	public boolean setValve(BlockCoords coords)
	{
		if (isPartOfTank() || coords == null)
		{
			return false;
		}
		
		ValveBlockEntity valveEntity = Utils.getTileEntityAt(worldObj, ValveBlockEntity.class, coords);
		
		if (valveEntity != null)
		{
			valveCoords = coords;
			isPartOfTank = true;
			worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Updates the {@link TankBlock}s textures.
	 */
	public void updateTextures()
	{
		determineTextureIds();
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	/**
	 * Gets the {@link TankBlock}s current filling level in percent.
	 * @return
	 * The {@link TankBlock}s filling level in percent.
	 */
	public int getFillPercentage()
	{
		return fillPercentage;
	}
	
	/**
	 * Gets the {@link TankBlock}s current filling level.
	 * @param percentage
	 * The percentage the {@link TankBlock}s filling level should be set to.
	 * @return
	 * <code>true</code> if the filling level was updated, otherwise <code>false</code>.
	 */
	public boolean setFillPercentage(int percentage)
	{
		if (percentage < 0 || percentage > 100 || percentage == fillPercentage)
		{
			return false;
		}
		
		fillPercentage = percentage;
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		
		return true;
	}
	
	/**
	 * Gets info on which side of the {@link TankBlock} is connected to another {@link TankBlock} of the same multiblock structure.
	 * @return
	 * A boolean array whose elements are <code>true</code> for sides with a {@link TankBlock} of the same multiblock structure.<br>
	 * Vanilla side values are used as indexes.
	 * @see Direction
	 */
	public boolean[] getConnections()
	{
		// I'd rather return an read-only collection here. For performance reasons I'll leave it as is for now (the array is used for rendering and a list would introduce unnecessary overhead).
		return connections;
	}
	
	/**
	 * Gets the texture for the specified side of the {@link TankBlock}.
	 * @param side
	 * The side to get the texture for.
	 * @return
	 * The texture id or <code>-1</code> if the <code>side</code> argument was invalid.
	 * @see Direction
	 */
	public int getTexture(int side)
	{
		if (side < 0 || side > 5)
		{
			return -1;
		}
		
		return textureIds[side];
	}
	
	/**
	 * Checks if the {@link TankBlock} is connected to a {@link ValveBlock} at the specified coordinates.
	 * @param coords
	 * The {@link ValveBlock}s coordinates.
	 * @return
	 * <code>true</code> if the {@link TankBlock} is connected to a {@link ValveBlock} at the specified coordinates, otherwise <code>false</code>.
	 */
	public boolean hasValveAt(BlockCoords coords)
	{
		if (!isPartOfTank() || coords == null)
		{
			return false;
		}
		
		return coords.equals(valveCoords);
	}
	
	/**
	 * Gets the texture ids for the different sides of the {@link TankBlock}.
	 */
	private void determineTextureIds()
	{
		determineConnections();
		
		textureIds[Direction.XPOS] = ConnectedTexturesHelper.getPositiveXTexture(connections);
		textureIds[Direction.XNEG] = ConnectedTexturesHelper.getNegativeXTexture(connections);
		textureIds[Direction.YPOS] = ConnectedTexturesHelper.getPositiveYTexture(connections);
		textureIds[Direction.YNEG] = ConnectedTexturesHelper.getNegativeYTexture(connections);
		textureIds[Direction.ZPOS] = ConnectedTexturesHelper.getPositiveZTexture(connections);
		textureIds[Direction.ZNEG] = ConnectedTexturesHelper.getNegativeZTexture(connections);
	}
	
	/**
	 * Builds an array that holds information on which side of the {@link TankBlock} is connected to another {@link TankBlock} of the same multiblock structure.
	 */
	private void determineConnections()
	{
		connections[Direction.XPOS] = shouldConnectTo(xCoord + 1, yCoord, zCoord);	// X+
		connections[Direction.XNEG] = shouldConnectTo(xCoord - 1, yCoord, zCoord);	// X-
		connections[Direction.YPOS] = shouldConnectTo(xCoord, yCoord + 1, zCoord);	// Y+
		connections[Direction.YNEG] = shouldConnectTo(xCoord, yCoord - 1, zCoord);	// Y-
		connections[Direction.ZPOS] = shouldConnectTo(xCoord, yCoord, zCoord + 1);	// Z+
		connections[Direction.ZNEG] = shouldConnectTo(xCoord, yCoord, zCoord - 1);	// Z-
	}
	
	/**
	 * Checks if the {@link TankBlock}s textures should connect to a {@link TankBlock} at the specified coordinates.
	 * @param x
	 * The x-coordinate of the connection candidate.
	 * @param y
	 * The y-coordinate of the connection candidate.
	 * @param z
	 * The z-coordinate of the connection candidate.
	 * @return
	 * <code>true</code> if the textures should connect, otherwise <code>false</code>.
	 */
	private boolean shouldConnectTo(int x, int y, int z)
	{
		// only check adjacent blocks
		if (x < xCoord - 1 || x > xCoord + 1 || y < yCoord - 1 || y > yCoord + 1 || z < zCoord - 1 || z > zCoord + 1)
		{
			return false;
		}
		
		if (worldObj.getBlockId(x, y, z) == SimpleFluidTanks.tankBlock.blockID)
		{
			TankBlockEntity connectionCandidate = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, x, y, z);
			
			if (connectionCandidate != null)
			{
				return (connectionCandidate.hasValveAt(valveCoords));
			}
		}
		
		return false;
	}

	/**
	 * Resets aka. disconnects the {@link TankBlock} from a multiblock tank.
	 */
	public void reset()
	{
		isPartOfTank = false;
		fillPercentage = 0;
		valveCoords = null;
		Arrays.fill(textureIds, 0);
		Arrays.fill(connections, false);
		
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
}
