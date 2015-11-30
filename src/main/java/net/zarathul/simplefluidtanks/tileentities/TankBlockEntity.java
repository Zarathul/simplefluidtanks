package net.zarathul.simplefluidtanks.tileentities;

import java.util.Arrays;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
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
	 * Indicates if the {@link TankBlock} is part of a multiblock tank aka. connected to a {@link ValveBlock}.
	 */
	private boolean isPartOfTank;

	/**
	 * The coordinates of the {@link ValveBlock} the {@link TankBlock} is connected to.
	 */
	private BlockPos valveCoords;

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
		isPartOfTank = tag.getBoolean("isPartOfTank");

		if (isPartOfTank)
		{
			int[] valveCoordsArray = tag.getIntArray("ValveCoords");
			valveCoords = new BlockPos(valveCoordsArray[0], valveCoordsArray[1], valveCoordsArray[2]);
		}

		textureIds = tag.getIntArray("TextureIds");
		connections = new boolean[6];
		connections[EnumFacing.DOWN.getIndex()] = tag.getBoolean("Y-");
		connections[EnumFacing.UP.getIndex()] = tag.getBoolean("Y+");
		connections[EnumFacing.NORTH.getIndex()] = tag.getBoolean("Z-");
		connections[EnumFacing.SOUTH.getIndex()] = tag.getBoolean("Z+");
		connections[EnumFacing.WEST.getIndex()] = tag.getBoolean("X-");
		connections[EnumFacing.EAST.getIndex()] = tag.getBoolean("X+");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);

		tag.setByte("FillPercentage", (byte) fillPercentage);
		tag.setBoolean("isPartOfTank", isPartOfTank);

		if (valveCoords != null)
		{
			int[] valveCoordsArray = new int[] { valveCoords.getX(), valveCoords.getY(), valveCoords.getZ() };
			tag.setIntArray("ValveCoords", valveCoordsArray);
		}

		tag.setIntArray("TextureIds", textureIds);
		tag.setBoolean("Y-", connections[EnumFacing.DOWN.getIndex()]);
		tag.setBoolean("Y+", connections[EnumFacing.UP.getIndex()]);
		tag.setBoolean("Z-", connections[EnumFacing.NORTH.getIndex()]);
		tag.setBoolean("Z+", connections[EnumFacing.SOUTH.getIndex()]);
		tag.setBoolean("X-", connections[EnumFacing.WEST.getIndex()]);
		tag.setBoolean("X+", connections[EnumFacing.EAST.getIndex()]);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);

		return new S35PacketUpdateTileEntity(pos, -1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		readFromNBT(packet.getNbtCompound());
		worldObj.markBlockForUpdate(pos);
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
	 * @param pos
	 * The coordinates of the {@link ValveBlock}.
	 * @return <code>true</code> if linking succeeded, otherwise <code>false</code>.
	 */
	public boolean setValve(BlockPos pos)
	{
		if (isPartOfTank() || pos == null)
		{
			return false;
		}

		ValveBlockEntity valveEntity = Utils.getTileEntityAt(worldObj, ValveBlockEntity.class, pos);

		if (valveEntity != null)
		{
			valveCoords = pos;
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

		textureIds[EnumFacing.EAST.getIndex()] = ConnectedTexturesHelper.getPositiveXTexture(connections);
		textureIds[EnumFacing.WEST.getIndex()] = ConnectedTexturesHelper.getNegativeXTexture(connections);
		textureIds[EnumFacing.UP.getIndex()] = ConnectedTexturesHelper.getPositiveYTexture(connections);
		textureIds[EnumFacing.DOWN.getIndex()] = ConnectedTexturesHelper.getNegativeYTexture(connections);
		textureIds[EnumFacing.SOUTH.getIndex()] = ConnectedTexturesHelper.getPositiveZTexture(connections);
		textureIds[EnumFacing.NORTH.getIndex()] = ConnectedTexturesHelper.getNegativeZTexture(connections);
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
			worldObj.markBlockForUpdate(pos);
			worldObj.markChunkDirty(pos, this);
		}

		return percentageChanged;
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
	 * Determines if the {@link TankBlock} is connected to another {@link TankBlock} of the same multiblock structure on the specified side.
	 * 
	 * @param side
	 * The side to check.
	 * @return <code>true</code> the the specified side is connected, otherwise <code>false</code>.
	 */
	public boolean isConnected(EnumFacing side)
	{
		if (side == null) return false;
		
		return connections[side.getIndex()];
	}

	/**
	 * Gets the texture index for the specified side of the {@link TankBlock}.
	 * 
	 * @param side
	 * The side to get the index for.
	 * @return The texture index.
	 */
	public int getTextureIndex(EnumFacing side)
	{
		return textureIds[side.getIndex()];
	}

	/**
	 * Checks if the {@link TankBlock} is connected to a {@link ValveBlock} at the specified coordinates.
	 * 
	 * @param pos
	 * The {@link ValveBlock}s coordinates.
	 * @return <code>true</code> if the {@link TankBlock} is connected to a {@link ValveBlock} at the specified coordinates, otherwise <code>false</code>.
	 */
	public boolean hasValveAt(BlockPos pos)
	{
		if (!isPartOfTank() || pos == null)
		{
			return false;
		}

		return pos.equals(valveCoords);
	}

	/**
	 * Builds an array that holds information on which side of the {@link TankBlock} is connected to another {@link TankBlock} of the same multiblock structure.
	 */
	private void updateConnections()
	{
		connections[EnumFacing.EAST.getIndex()] = shouldConnectTo(pos.east());		// X+
		connections[EnumFacing.WEST.getIndex()] = shouldConnectTo(pos.west());		// X-
		connections[EnumFacing.UP.getIndex()] = shouldConnectTo(pos.up());			// Y+
		connections[EnumFacing.DOWN.getIndex()] = shouldConnectTo(pos.down());		// Y-
		connections[EnumFacing.SOUTH.getIndex()] = shouldConnectTo(pos.south());	// Z+
		connections[EnumFacing.NORTH.getIndex()] = shouldConnectTo(pos.north());	// Z-
	}

	/**
	 * Checks if the {@link TankBlock}s textures should connect to a {@link TankBlock} at the specified coordinates.
	 * 
	 * @param checkPos
	 * The coordinates of the connection candidate.
	 * @return <code>true</code> if the textures should connect, otherwise <code>false</code>.
	 */
	private boolean shouldConnectTo(BlockPos checkPos)
	{
		// only check adjacent blocks
		if (checkPos.getX() < pos.getX() - 1 || checkPos.getX() > pos.getX() + 1 ||
			checkPos.getY() < pos.getY() - 1 || checkPos.getY() > pos.getY() + 1 ||
			checkPos.getZ() < pos.getZ() - 1 || checkPos.getZ() > pos.getZ() + 1)
		{
			return false;
		}

		TankBlockEntity connectionCandidate = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, checkPos);

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
			worldObj.markBlockForUpdate(pos);
			worldObj.markChunkDirty(pos, this);
		}
	}
}
