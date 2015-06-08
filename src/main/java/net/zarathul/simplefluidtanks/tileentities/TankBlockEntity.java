package net.zarathul.simplefluidtanks.tileentities;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.BlockCoords;
import net.zarathul.simplefluidtanks.common.Direction;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.rendering.ConnectedTexturesHelper;

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
	 * The filling level of the tank in percent.
	 */
	private int fluidLightLevel;

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

	/**
	 * Contains information on which side there are other {@link TankBlock}s that belong to the same multiblock structure.
	 */
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
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);

		fillPercentage = tag.getByte("FillPercentage");
		fluidLightLevel = tag.getByte("FluidLightLevel");
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

		tag.setByte("FillPercentage", (byte) fillPercentage);
		tag.setByte("FluidLightLevel", (byte) fluidLightLevel);
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

		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		readFromNBT(packet.func_148857_g());
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	/**
	 * Checks if the {@link TankBlock} is part of a multiblock tank.
	 * 
	 * @return <code>true</code> if the {@link TankBlock} is part of a multiblock tank, otherwise false.
	 */
	public boolean isPartOfTank()
	{
		return isPartOfTank && valveCoords != null;
	}

	/**
	 * Checks if the {@link TankBlock} is empty.
	 * 
	 * @return <code>true</code> if the {@link TankBlock} is empty, otherwise false.
	 */
	public boolean isEmpty()
	{
		return fillPercentage == 0;
	}

	/**
	 * Gets the {@link ValveBlock}s {@link TileEntity} the {@link TankBlock} is linked to.
	 * 
	 * @return The valves {@link TileEntity}<br>
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
	 * 
	 * @param coords
	 * The coordinates of the {@link ValveBlock}.
	 * @return <code>true</code> if linking succeeded, otherwise <code>false</code>.
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

			return true;
		}

		return false;
	}

	/**
	 * Updates the texture ids for the different sides of the {@link TankBlock}.
	 */
	public void updateTextures()
	{
		updateConnections();

		textureIds[Direction.XPOS] = ConnectedTexturesHelper.getPositiveXTexture(connections);
		textureIds[Direction.XNEG] = ConnectedTexturesHelper.getNegativeXTexture(connections);
		textureIds[Direction.YPOS] = ConnectedTexturesHelper.getPositiveYTexture(connections);
		textureIds[Direction.YNEG] = ConnectedTexturesHelper.getNegativeYTexture(connections);
		textureIds[Direction.ZPOS] = ConnectedTexturesHelper.getPositiveZTexture(connections);
		textureIds[Direction.ZNEG] = ConnectedTexturesHelper.getNegativeZTexture(connections);
	}

	/**
	 * Gets the {@link TankBlock}s current filling level in percent.
	 * 
	 * @return The {@link TankBlock}s filling level in percent.
	 */
	public int getFillPercentage()
	{
		return fillPercentage;
	}

	/**
	 * Sets the {@link TankBlock}s current filling level in percent.
	 * 
	 * @param percentage
	 * The percentage the {@link TankBlock}s filling level should be set to.
	 * @param forceBlockUpdate
	 * Specifies if a block update should be forced.
	 * @return <code>true</code> if the filling level was updated, otherwise <code>false</code>.
	 */
	public boolean setFillPercentage(int percentage, boolean forceBlockUpdate)
	{
		percentage = MathHelper.clamp_int(percentage, 0, 100);

		boolean percentageChanged = (percentage != fillPercentage);

		fillPercentage = percentage;

		if (percentageChanged || forceBlockUpdate)
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		}

		return percentageChanged;
	}

	/**
	 * Gets the {@link TankBlock}s current fluid light level.
	 * 
	 * @return The {@link TankBlock}s fluid light level.
	 */
	public int getFluidLightLevel()
	{
		return fluidLightLevel;
	}

	/**
	 * Sets the {@link TankBlock}s current fluid light level.
	 * 
	 * @param fluidLight
	 * The amount of light the fluid in the {@link TankBlock}s emits.
	 * @param forceBlockUpdate
	 * Specifies if a block update should be forced.
	 * @return <code>true</code> if the fluid light level was changed, otherwise <code>false</code>.
	 */
	public boolean setFluidLightLevel(int fluidLight, boolean forceBlockUpdate)
	{
		fluidLight = MathHelper.clamp_int(fluidLight, 0, 15);

		boolean fluidLightChanged = (fluidLight != fluidLightLevel);

		fluidLightLevel = fluidLight;

		if (fluidLightChanged || forceBlockUpdate)
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		}

		return fluidLightChanged;
	}

	/**
	 * Gets the {@link Fluid} inside the multiblock tank structure.
	 * 
	 * @return The fluid or <code>null</code> if the {@link TankBlock} is not linked to a {@link ValveBlock} or the multiblock tank is empty.
	 */
	public Fluid getFluid()
	{
		ValveBlockEntity valve = getValve();

		if (valve != null)
		{
			FluidStack fluidStack = valve.getFluid();

			if (fluidStack != null)
			{
				return fluidStack.getFluid();
			}
		}

		return null;
	}

	/**
	 * Gets info on which side of the {@link TankBlock} is connected to another {@link TankBlock} of the same multiblock structure.
	 * 
	 * @return A boolean array whose elements are <code>true</code> for sides with a {@link TankBlock} of the same multiblock structure.<br>
	 * Vanilla side values are used as indexes.
	 * @see Direction
	 */
	public boolean[] getConnections()
	{
		// I'd rather return an read-only collection here. For performance reasons I'll leave it as is for now (the array is used for rendering and a list would introduce unnecessary overhead).
		return connections;
	}

	/**
	 * Gets the texture index for the specified side of the {@link TankBlock}.
	 * 
	 * @param side
	 * The side to get the index for.
	 * @return The texture index or <code>-1</code> if the <code>side</code> argument was invalid.
	 * @see Direction
	 */
	public int getTextureIndex(int side)
	{
		if (side < 0 || side > 5)
		{
			return -1;
		}

		return textureIds[side];
	}

	/**
	 * Checks if the {@link TankBlock} is connected to a {@link ValveBlock} at the specified coordinates.
	 * 
	 * @param coords
	 * The {@link ValveBlock}s coordinates.
	 * @return <code>true</code> if the {@link TankBlock} is connected to a {@link ValveBlock} at the specified coordinates, otherwise <code>false</code>.
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
	 * Builds an array that holds information on which side of the {@link TankBlock} is connected to another {@link TankBlock} of the same multiblock structure.
	 */
	private void updateConnections()
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
	 * 
	 * @param x
	 * The x-coordinate of the connection candidate.
	 * @param y
	 * The y-coordinate of the connection candidate.
	 * @param z
	 * The z-coordinate of the connection candidate.
	 * @return <code>true</code> if the textures should connect, otherwise <code>false</code>.
	 */
	private boolean shouldConnectTo(int x, int y, int z)
	{
		// only check adjacent blocks
		if (x < xCoord - 1 || x > xCoord + 1 || y < yCoord - 1 || y > yCoord + 1 || z < zCoord - 1 || z > zCoord + 1)
		{
			return false;
		}

		// Connect to our valve
		if (valveCoords != null && valveCoords.equals(x, y, z))
		{
			return true;
		}

		// Connect to any tank that have the same valve as us.
		TankBlockEntity connectionCandidate = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, x, y, z);
		if (connectionCandidate != null)
		{
			return (connectionCandidate.hasValveAt(valveCoords));
		} 

		return false;
	}

	/**
	 * Disconnects the {@link TankBlock} from a multiblock tank.
	 * 
	 * @param suppressBlockUpdates
	 * Specifies if block updates should be suppressed.
	 */
	public void disconnect(boolean suppressBlockUpdates)
	{
		isPartOfTank = false;
		fillPercentage = 0;
		valveCoords = null;
		Arrays.fill(textureIds, 0);
		Arrays.fill(connections, false);

		if (!suppressBlockUpdates)
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		}
	}
}
