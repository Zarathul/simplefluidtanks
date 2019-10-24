package net.zarathul.simplefluidtanks.tileentities;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.rendering.BakedTankModel;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Holds {@link TileEntity} data for {@link TankBlock}s,
 */
public class TankBlockEntity extends TileEntity
{
	/**
	 * The fill level of the tank.
	 */
	private int fillLevel;

	/**
	 * Indicates if the {@link TankBlock} is part of a multiblock tank aka. connected to a {@link ValveBlock}.
	 */
	private boolean isPartOfTank;

	/**
	 * The coordinates of the {@link ValveBlock} the {@link TankBlock} is connected to.
	 */
	private BlockPos valveCoords;

	/**
	 * Contains information on which side there are other {@link TankBlock}s that belong to the same multiblock structure.
	 */
	private boolean[] connections;

	/**
	 * Contains additional information to render the tank model correctly.
	 */
	//private TankData modelData;

	/**
	 * Default constructor.
	 */
	public TankBlockEntity()
	{
		super(SimpleFluidTanks.tankEntity);

		fillLevel = 0;
		isPartOfTank = false;
		valveCoords = null;
		connections = new boolean[6];
		//modelData = new TankData(new ResourceLocation("minecraft", "empty"), 0, false);
	}

	/*
	@Override
	public void requestModelDataUpdate()
	{
		modelData.fluidName = getFluid().getRegistryName();
		modelData.fluidLevel = fillLevel;

		boolean tankAboveIsEmpty = true;
		boolean sameValve = false;
		TankBlockEntity tankAbove = Utils.getTileEntityAt(world, TankBlockEntity.class, pos.up());

		if (tankAbove != null)
		{
			tankAboveIsEmpty = tankAbove.isEmpty();
			ValveBlockEntity valve = getValve();
			ValveBlockEntity valveAbove = tankAbove.getValve();
			sameValve = valveAbove != null && valve == valveAbove;
		}

		// Only cull the fluids top face if the tank above is not empty and both tanks
		// are part of the same multiblock (share the same valve).
		modelData.cullFluidTop = !tankAboveIsEmpty && sameValve;

		super.requestModelDataUpdate();
	}

	@Nonnull
	@Override
	public IModelData getModelData()
	{
		return modelData;
	}

	 */

	@Override
	public void read(CompoundNBT tag)
	{
		super.read(tag);

		fillLevel = tag.getByte("FillLevel");
		isPartOfTank = tag.getBoolean("isPartOfTank");

		if (isPartOfTank)
		{
			int[] valveCoordsArray = tag.getIntArray("ValveCoords");
			valveCoords = new BlockPos(valveCoordsArray[0], valveCoordsArray[1], valveCoordsArray[2]);
		}

		connections = new boolean[6];
		connections[Direction.DOWN.getIndex()] = tag.getBoolean("Y-");
		connections[Direction.UP.getIndex()] = tag.getBoolean("Y+");
		connections[Direction.NORTH.getIndex()] = tag.getBoolean("Z-");
		connections[Direction.SOUTH.getIndex()] = tag.getBoolean("Z+");
		connections[Direction.WEST.getIndex()] = tag.getBoolean("X-");
		connections[Direction.EAST.getIndex()] = tag.getBoolean("X+");
	}

	@Nonnull
	@Override
	public CompoundNBT write(CompoundNBT tag)
	{
		super.write(tag);

		tag.putByte("FillLevel", (byte) fillLevel);
		tag.putBoolean("isPartOfTank", isPartOfTank);

		if (valveCoords != null)
		{
			int[] valveCoordsArray = new int[] { valveCoords.getX(), valveCoords.getY(), valveCoords.getZ() };
			tag.putIntArray("ValveCoords", valveCoordsArray);
		}

		tag.putBoolean("Y-", connections[Direction.DOWN.getIndex()]);
		tag.putBoolean("Y+", connections[Direction.UP.getIndex()]);
		tag.putBoolean("Z-", connections[Direction.NORTH.getIndex()]);
		tag.putBoolean("Z+", connections[Direction.SOUTH.getIndex()]);
		tag.putBoolean("X-", connections[Direction.WEST.getIndex()]);
		tag.putBoolean("X+", connections[Direction.EAST.getIndex()]);
		
		return tag;
	}

	@Nonnull
	@Override
	public CompoundNBT getUpdateTag()
	{
		CompoundNBT tag = super.getUpdateTag();
		write(tag);
		return tag;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(pos, -1, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
	{
		int oldFillLevel = fillLevel;
		boolean wasPartOfTank = isPartOfTank;
		boolean[] oldConnections = Arrays.copyOf(connections, connections.length);
		
		read(packet.getNbtCompound());
		
		if (wasPartOfTank != isPartOfTank
			|| oldFillLevel != fillLevel
			|| !Arrays.equals(oldConnections, connections))
		{
			Utils.syncBlockAndRerender(world, pos);
		}
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
		return fillLevel == 0;
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
			return Utils.getTileEntityAt(world, ValveBlockEntity.class, valveCoords);
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

		ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, pos);

		if (valveEntity != null)
		{
			valveCoords = pos;
			isPartOfTank = true;

			return true;
		}

		return false;
	}

	/**
	 * Gets the {@link TankBlock}s current fill level.
	 * 
	 * @return The {@link TankBlock}s filling level in percent.
	 */
	public int getFillLevel()
	{
		return fillLevel;
	}

	/**
	 * Sets the {@link TankBlock}s current fill level.
	 * 
	 * @param level
	 * The {@link TankBlock}s fill level.
	 * @param forceBlockUpdate
	 * Specifies if a block update should be forced.
	 * @return <code>true</code> if the fill level has changed, otherwise <code>false</code>.
	 */
	public boolean setFillLevel(int level, boolean forceBlockUpdate)
	{
		level = MathHelper.clamp(level, 0, BakedTankModel.FLUID_LEVELS);

		boolean levelChanged = (level != fillLevel);

		fillLevel = level;

		if (levelChanged || forceBlockUpdate)
		{
			Utils.syncBlockAndRerender(world, pos);
			markDirty();
		}

		return levelChanged;
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
	public boolean isConnected(Direction side)
	{
		if (side == null) return false;
		
		return connections[side.getIndex()];
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
	public void updateConnections()
	{
		connections[Direction.EAST.getIndex()] = shouldConnectTo(pos.east());		// X+
		connections[Direction.WEST.getIndex()] = shouldConnectTo(pos.west());		// X-
		connections[Direction.UP.getIndex()] = shouldConnectTo(pos.up());			// Y+
		connections[Direction.DOWN.getIndex()] = shouldConnectTo(pos.down());		// Y-
		connections[Direction.SOUTH.getIndex()] = shouldConnectTo(pos.south());	// Z+
		connections[Direction.NORTH.getIndex()] = shouldConnectTo(pos.north());	// Z-
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

		TankBlockEntity connectionCandidate = Utils.getTileEntityAt(world, TankBlockEntity.class, checkPos);

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
		fillLevel = 0;
		valveCoords = null;
		Arrays.fill(connections, false);

		if (!suppressBlockUpdates)
		{
			Utils.syncBlockAndRerender(world, pos);
			markDirty();
		}
	}
}
