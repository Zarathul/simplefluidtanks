package net.zarathul.simplefluidtanks.tileentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.zarathul.simplefluidtanks.blocks.FluidTank;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.BasicAStar;
import net.zarathul.simplefluidtanks.common.BlockSearchMode;
import net.zarathul.simplefluidtanks.common.Direction;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;

/**
 * Holds {@link TileEntity} data for {@link ValveBlock}s,
 */
public class ValveBlockEntity extends TileEntity implements IFluidHandler
{
	/**
	 * The {@link FluidTank} that actually holds all the fluid in the multiblock tank.
	 */
	private final FluidTank internalTank;

	/**
	 * Holds the number of {@link TankBlock}s that are linked to this {@link ValveBlock}. 
	 * (This is primarily used on the client side. This way the multimap containing the 
	 * tank information does not have to be synced to clients).
	 */
	private int linkedTankCount;

	/**
	 * The fill priorities of all connected {@link TankBlock}s.
	 */
	private Multimap<Integer, BlockPos> tankPriorities;

	/**
	 * A bitmask storing which sides of the {@link ValveBlock} face connected {@link TankBlock}s.
	 * 
	 * @see Direction
	 */
	private byte tankFacingSides;

	/**
	 * A temporary mapping of connected {@link TankBlock}s to their priorities.<br>
	 * <b>Caution:</b> This will be empty after reloading the {@link ValveBlock} from nbt data.
	 */
	private HashMap<BlockPos, Integer> tankToPriorityMappings;

	/**
	 * A temporary set of all connected {@link TankBlock}s.<br>
	 * <b>Caution:</b> This will be empty after reloading the {@link ValveBlock} from nbt data.
	 */
	private HashSet<BlockPos> tanks;

	/**
	 * A temporary set of all connected {@link TankBlock}s before disbanding the multiblock structure.<br>
	 * <b>Caution:</b> This will be empty after reloading the {@link ValveBlock} from nbt data.
	 */
	private HashSet<BlockPos> tanksBeforeDisband;

	/**
	 * Holds a {@link BasicAStar} instance while the tank finding algorithms are running.
	 */
	private BasicAStar aStar;
	
	/**
	 * The facing of the valve when it's not part of a multiblock.
	 */
	private EnumFacing facing;

	public ValveBlockEntity()
	{
		super();
		internalTank = new FluidTank(0);
		tankPriorities = ArrayListMultimap.create();
		tankFacingSides = -1;
		linkedTankCount = 0;
		facing = EnumFacing.NORTH;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);

		internalTank.readFromNBT(tag);
		readTankPrioritiesFromNBT(tag);
		linkedTankCount = (tag.hasKey("LinkedTankCount")) ? tag.getInteger("LinkedTankCount") : Math.max(tankPriorities.size() - 1, 0);

		tankFacingSides = tag.getByte("TankFacingSides");
		facing = EnumFacing.getFront(tag.getByte("Facing"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);

		internalTank.writeToNBT(tag);
		writeTankPrioritiesToNBT(tag);

		tag.setByte("TankFacingSides", tankFacingSides);
		tag.setByte("Facing", (byte)facing.getIndex());
		
		return tag;
	}

	@Override
	public NBTTagCompound getUpdateTag()
	{
		NBTTagCompound tag = super.getUpdateTag();
		tag.setByte("TankFacingSides", tankFacingSides);
		tag.setByte("Facing", (byte)facing.getIndex());
		tag.setInteger("LinkedTankCount", linkedTankCount);
		internalTank.writeToNBT(tag);
		
		return tag;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		return new SPacketUpdateTileEntity(pos, -1, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
	{
		readFromNBT(packet.getNbtCompound());
		Utils.markBlockForUpdate(worldObj, pos);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public int fill(EnumFacing from, FluidStack fillFluid, boolean doFill)
	{
		if (!worldObj.isRemote && hasTanks())
		{
			int fillAmount = internalTank.fill(fillFluid, doFill);

			if (doFill && fillAmount > 0)
			{
				distributeFluidToTanks();
				Utils.markBlockForUpdate(worldObj, pos);
				markDirty();
				FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fillFluid, worldObj, pos, internalTank, fillAmount));
			}

			return fillAmount;
		}

		return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack drainFluid, boolean doDrain)
	{
		return drain(from, drainFluid, -1, doDrain);
	}

	@Override
	public FluidStack drain(EnumFacing from, int drainAmount, boolean doDrain)
	{
		return drain(from, null, drainAmount, doDrain);
	}

	/**
	 * Drains fluid from the multiblock tank.
	 * 
	 * @param from
	 * The direction the fluid is drained from.
	 * @param drainFluid
	 * The fluid type and amount to drain.
	 * @param drainAmount
	 * The amount of fluid to drain.
	 * @param doDrain
	 * Indicates whether the draining should be simulated or not.
	 * @return The type and amount of fluid drained.
	 */
	private FluidStack drain(EnumFacing from, FluidStack drainFluid, int drainAmount, boolean doDrain)
	{
		if (!worldObj.isRemote && hasTanks())
		{
			FluidStack drainedFluid = (drainFluid != null && drainFluid.isFluidEqual(internalTank.getFluid())) ?
				internalTank.drain(drainFluid.amount, doDrain) :
				(drainAmount >= 0) ? internalTank.drain(drainAmount, doDrain) : null;

			if (doDrain && drainedFluid != null && drainedFluid.amount > 0)
			{
				distributeFluidToTanks();
				Utils.markBlockForUpdate(worldObj, pos);
				markDirty();
				FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(drainedFluid, worldObj, pos, internalTank, drainedFluid.amount));
			}

			return drainedFluid;
		}

		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		if (hasTanks() && !isFacingTank(from) && fluid != null && !internalTank.isFull())
		{
			FluidStack tankFluid = internalTank.getFluid();

			return (tankFluid == null || tankFluid.isFluidEqual(new FluidStack(fluid, 0)));
		}

		return false;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		if (hasTanks() && !isFacingTank(from) && fluid != null && internalTank.getFluidAmount() > 0)
		{
			FluidStack tankFluid = internalTank.getFluid();

			return (tankFluid != null && tankFluid.isFluidEqual(new FluidStack(fluid, 0)));
		}

		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		return new FluidTankInfo[] { internalTank.getInfo() };
	}

	/**
	 * Gets the facing of the valve.
	 * 
	 * @return The direction the valve is facing in.
	 */
	public EnumFacing getFacing()
	{
		return facing;
	}

	/**
	 * Sets the facing of the valve.
	 * 
	 * @param One of the {@link EnumFacing} values (values on the y axis are ignored).
	 */
	public void setFacing(EnumFacing facing)
	{
		if (facing.getAxis() != EnumFacing.Axis.Y)
		{
			this.facing = facing;
		}
	}

	/**
	 * Gets the multiblock tanks capacity.
	 * 
	 * @return The capacity in millibuckets.
	 */
	public int getCapacity()
	{
		return internalTank.getCapacity();
	}

	/**
	 * Gets the amount of fluid in the multiblock tank.
	 * 
	 * @return The amount of fluid in millibuckets.
	 */
	public int getFluidAmount()
	{
		return internalTank.getFluidAmount();
	}

	/**
	 * Gets the remaining capacity of the tank.
	 * 
	 * @return The remaining amount of fluid the tank can take in until it is full.
	 */
	public int getRemainingCapacity()
	{
		return internalTank.getRemainingCapacity();
	}

	/**
	 * Gets the luminosity of the fluid in the multiblock tank.
	 * 
	 * @return The luminosity of the fluid.
	 */
	public int getFluidLuminosity()
	{
		FluidStack storedFluid = this.internalTank.getFluid();

		if (storedFluid != null)
		{
			Fluid fluid = storedFluid.getFluid();

			if (fluid != null)
			{
				return fluid.getLuminosity();
			}
		}

		return 0;
	}

	/**
	 * Gets the localized name of the fluid in the multiblock tank.
	 * 
	 * @return The localized name of the fluid.
	 */
	public String getLocalizedFluidName()
	{
		FluidStack storedFluid = this.internalTank.getFluid();

		if (storedFluid != null)
		{
			Fluid fluid = storedFluid.getFluid();

			if (fluid != null)
			{
				return fluid.getLocalizedName(storedFluid);
			}
		}

		return null;
	}

	/**
	 * Gets the fluid in the multiblock tank.
	 * 
	 * @return A {@link FluidStack} representing the fluid in the multiblock tank.
	 */
	public FluidStack getFluid()
	{
		return internalTank.getFluid();
	}

	/**
	 * Checks if the tank is full.
	 * 
	 * @return <code>true</code> if the tank is full, otherwise <code>false</code>.
	 */
	public boolean isFull()
	{
		return internalTank.isFull();
	}

	/**
	 * Gets the bitmask storing which sides of the {@link ValveBlock} face connected {@link TankBlock}s.
	 * 
	 * @return A bitmask.
	 * @see Direction
	 */
	public int getTankFacingSides()
	{
		return tankFacingSides;
	}

	/**
	 * Updates the bitmask storing which sides of the {@link ValveBlock} face connected {@link TankBlock}s.
	 */
	public void updateTankFacingSides()
	{
		int sides = 0;

		if (isInTankList(pos.south()))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(EnumFacing.SOUTH);
		}

		if (isInTankList(pos.north()))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(EnumFacing.NORTH);
		}

		if (isInTankList(pos.east()))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(EnumFacing.EAST);
		}

		if (isInTankList(pos.west()))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(EnumFacing.WEST);
		}

		if (isInTankList(pos.up()))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(EnumFacing.UP);
		}

		if (isInTankList(pos.down()))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(EnumFacing.DOWN);
		}

		tankFacingSides = (byte) sides;
	}

	/**
	 * Checks if the specified side of the {@link ValveBlock} is facing a connected {@link TankBlock}.
	 * 
	 * @param side
	 * The side to check.
	 * @return <code>true</code> if the {@link ValveBlock} is facing a connected {@link TankBlock} on the specified side, otherwise <code>false</code>.
	 * @see Direction
	 */
	public boolean isFacingTank(EnumFacing side)
	{
		if (side != null)
		{
			byte flags = Direction.sidesToBitFlagsMappings.get(side).byteValue();

			return (tankFacingSides & flags) == flags;
		}

		return false;
	}

	/**
	 * Checks if the {@link ValveBlock} has connected {@link TankBlock}s.
	 * 
	 * @return <code>true</code> if the {@link ValveBlock} has connected {@link TankBlock}s, otherwise <code>false</code>.
	 */
	public boolean hasTanks()
	{
		return linkedTankCount > 0;
	}

	/**
	 * Gets the number of linked {@link TankBlock}s.
	 * 
	 * @return The number of linked {@link TankBlock}s.
	 */
	public int getLinkedTankCount()
	{
		return linkedTankCount;
	}

	/**
	 * Disconnects all connected {@link TankBlock}s and resets the {@link ValveBlock} itself (capacity etc.).
	 */
	public void disbandMultiblock()
	{
		disbandMultiblock(false);
	}

	/**
	 * Disconnects all connected {@link TankBlock}s and resets the {@link ValveBlock} itself (capacity etc.).
	 * 
	 * @param suppressBlockUpdates
	 * Specifies if block updates should be suppressed.
	 */
	public void disbandMultiblock(boolean suppressBlockUpdates)
	{
		for (BlockPos tankCoords : tankPriorities.values())
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tankCoords);

			if (tankEntity != null)
			{
				tankEntity.disconnect(suppressBlockUpdates);
			}
		}

		if (suppressBlockUpdates)
		{
			// This Set is used later to update TankBlocks that are no longer part of the multiblock structure.
			tanksBeforeDisband = new HashSet<BlockPos>();
			tanksBeforeDisband.addAll(tankPriorities.values());
		}

		FluidStack fluidInTank = internalTank.getFluid();
		FluidStack spilledFluid = (fluidInTank != null) ? fluidInTank.copy() : null;
		tankPriorities.clear();
		linkedTankCount = 0;
		tankFacingSides = 0;
		internalTank.setFluid(null);
		internalTank.setCapacity(0);

		if (!suppressBlockUpdates)
		{
			Utils.markBlockForUpdate(worldObj, pos);
			markDirty();

			if (spilledFluid != null)
			{
				FluidEvent.fireEvent(new FluidEvent.FluidSpilledEvent(spilledFluid, worldObj, pos));
			}
		}
	}

	/**
	 * Re-runs the tank searching and prioritization algorithms and redistributes the fluid. 
	 * This allows for additional {@link TankBlock}s to be added to the multiblock structure.
	 */
	public void formMultiblock()
	{
		// store the current fluid for reinsertion
		FluidStack fluid = internalTank.getFluid();
		int oldFluidAmount = (fluid != null) ? fluid.amount : 0;

		// find new tanks and update the valves textures

		// block updates are suppressed here because tanks are updated anyway when the fluid is distributed
		disbandMultiblock(true);
		findAndPrioritizeTanks();
		// tanks that are no longer part of the multiblock structure need to be updated to render correctly
		updateOrphanedTanks();
		updateTankFacingSides();

		// redistribute the fluid
		internalTank.setFluid(fluid);
		distributeFluidToTanks(true);
		// the ValveBlock also counts as a tank in the multiblock structure
		linkedTankCount = Math.max(tankPriorities.size() - 1, 0);

		Utils.markBlockForUpdate(worldObj, pos);
		markDirty();
		
		if (oldFluidAmount > this.internalTank.getCapacity() && fluid != null)
		{
			FluidStack spilledFluid = fluid.copy();
			spilledFluid.amount = oldFluidAmount - this.internalTank.getCapacity();
			FluidEvent.fireEvent(new FluidEvent.FluidSpilledEvent(spilledFluid, worldObj, pos));
		}
	}

	/**
	 * Finds all {@link TankBlock}s connected to the {@link ValveBlock} and computes their filling priorities.
	 */
	private void findAndPrioritizeTanks()
	{
		generateTankList();
		computeFillPriorities();

		ArrayList<TankBlockEntity> tankEntities = new ArrayList<TankBlockEntity>(tankPriorities.size());

		// set the valve for all connected tanks
		for (BlockPos tankCoords : tankPriorities.values())
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tankCoords);

			if (tankEntity != null)
			{
				tankEntity.setValve(pos);
				tankEntities.add(tankEntity);
			}
		}
		
		// This needs to be done after setting the valve. Otherwise the connected textures will be wrong.
		for (TankBlockEntity t : tankEntities)
		{
			t.updateConnections();
		}

		// calculate and set the internal tanks capacity, note the " + 1" is needed because the ValveBlock itself is considered a tank with storage capacity
		internalTank.setCapacity((tankEntities.size() + 1) * Config.bucketsPerTank * FluidContainerRegistry.BUCKET_VOLUME);
	}

	/**
	 * Marks TankBlocks that are no longer part of the multiblock structure for an update.
	 */
	private void updateOrphanedTanks()
	{
		Collection<BlockPos> tanksToUpdate = Collections2.filter(tanksBeforeDisband, Predicates.not(Predicates.in(tanks)));

		for (BlockPos tank : tanksToUpdate)
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tank);

			if (tankEntity != null)
			{
				Utils.markBlockForUpdate(worldObj, pos);
				worldObj.markChunkDirty(tank, tankEntity);
			}
		}

		tanksBeforeDisband.clear();
	}

	/**
	 * Distributes the fluid currently held by the multiblock tank over the connected {@link TankBlock}s.
	 */
	private void distributeFluidToTanks()
	{
		distributeFluidToTanks(false);
	}

	/**
	 * Distributes the fluid currently held by the multiblock tank over the connected {@link TankBlock}s.
	 * 
	 * @param forceBlockUpdates
	 * Specifies if block updates should be forced.
	 */
	private void distributeFluidToTanks(boolean forceBlockUpdates)
	{
		// returned amount is mb(milli buckets)
		int amountToDistribute = internalTank.getFluidAmount();

		if (amountToDistribute == 0 || amountToDistribute == internalTank.getCapacity())
		{
			// there is nothing to distribute or the internal tank is full (no fill percentage calculations needed)
			int percentage = (amountToDistribute == 0) ? 0 : 100;

			for (BlockPos tankCoords : tankPriorities.values())
			{
				TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tankCoords);

				if (tankEntity != null)
				{
					tankEntity.setFillPercentage(percentage, forceBlockUpdates);
				}
			}
		}
		else
		{
			// get the fill priorities and sort them low to high
			int[] priorities = Ints.toArray(tankPriorities.keySet());
			Arrays.sort(priorities);

			Collection<BlockPos> tanksToFill = null;

			// for each priority get all the TankBlocks and fill them evenly
			for (int i = 0; i < priorities.length; i++)
			{
				tanksToFill = tankPriorities.get(priorities[i]);

				int capacity = tanksToFill.size() * Config.bucketsPerTank * FluidContainerRegistry.BUCKET_VOLUME;
				int fillPercentage = MathHelper.clamp_int((int) Math.ceil((double) amountToDistribute / (double) capacity * 100d), 0, 100);

				for (BlockPos tank : tanksToFill)
				{
					TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tank);

					if (tankEntity != null)
					{
						tankEntity.setFillPercentage(fillPercentage, forceBlockUpdates);
					}
				}

				amountToDistribute -= Math.min(capacity, amountToDistribute);
			}
		}
	}

	/**
	 * Generates a list of the coordinates of all {@link TankBlock}s connected to the {@link ValveBlock}. This list is temporary and contains no priority information.
	 */
	private void generateTankList()
	{
		tanks = new HashSet<BlockPos>();

		ArrayList<BlockPos> currentTanks = new ArrayList<BlockPos>();
		ArrayList<BlockPos> newTanks = new ArrayList<BlockPos>();
		Collection<BlockPos> adjacentTanks;

		currentTanks.add(pos);

		// simple flood find algorithm: get all adjacent TankBlocks to the current one -> repeat for all blocks found until there is nothing left to find
		do
		{
			for (BlockPos tank : currentTanks)
			{
				adjacentTanks = findAdjacentTanks(tank);

				for (BlockPos adjacentTank : adjacentTanks)
				{
					if (tanks.add(adjacentTank))
					{
						newTanks.add(adjacentTank);
					}
				}
			}

			currentTanks.clear();
			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (currentTanks.size() > 0);
	}

	/**
	 * Computes the fill priorities for all connected {@link TankBlock}s.
	 */
	private void computeFillPriorities()
	{
		aStar = new BasicAStar();
		tankToPriorityMappings = new HashMap<BlockPos, Integer>();

		ArrayList<BlockPos> tanksWithoutLowerTanks = new ArrayList<BlockPos>();
		ArrayList<BlockPos> currentTanks = new ArrayList<BlockPos>();
		ArrayList<BlockPos> tanksOnSameHeight;
		ArrayList<BlockPos> lowerTanks;
		HashMap<BlockPos, Integer> tanksToPrioritize = new HashMap<BlockPos, Integer>();
		HashSet<BlockPos> newTanks = new HashSet<BlockPos>();
		HashSet<BlockPos> handledSourceTanks = new HashSet<BlockPos>();
		HashSet<BlockPos> handledSegmentTanks = new HashSet<BlockPos>();

		currentTanks.add(pos);

		int priority = 0;
		int adjustedPriority;

		/*
		 * Prioritization uses two separate algorithms:
		 * - find the closest, lowest tanks that are connected to the current one (algorithm 1)
		 * - if the closest, lowest tank is the same as the current tank, find all tanks on the same height level that are reachable 
		 *   without ever going over the current tanks height level and set them all to the current priority if the current priority
		 *   is a higher value (= actual lower priority) than the priority the tanks already have (algorithm 2)
		 * - otherwise just set the closest, lowest tanks priority to the current priority value
		 * - in both cases add the tanks directly above the found tanks to the list of tanks that are processed in the next iteration
		 *   
		 *   The order of execution is important. Tanks found by the algorithm 1 have to be prioritized before tanks found by algorithm 2.
		 */

		do
		{
			for (BlockPos currentTank : currentTanks)
			{
				if (handledSegmentTanks.contains(currentTank)) continue;

				lowerTanks = getClosestLowestTanks(currentTank);

				// handle tanks with lower tanks first, store the rest for later processing
				if (lowerTanks.get(0) == currentTank)
				{
					tanksWithoutLowerTanks.add(currentTank);
					handledSegmentTanks.addAll(lowerTanks);
				}
				else
				{
					handledSourceTanks.add(currentTank);

					for (BlockPos lowerTank : lowerTanks)
					{
						tanksToPrioritize.put(lowerTank, priority);
						newTanks.addAll(getAdjacentTanks(lowerTank, BlockSearchMode.Above));
					}
				}
			}

			// find connected tanks on the same height without stepping over the height level of the initial tank
			for (BlockPos tankWithoutLowerTanks : tanksWithoutLowerTanks)
			{
				if (!tanksToPrioritize.containsKey(tankWithoutLowerTanks))
				{
					tanksOnSameHeight = getTanksOnSameHeight(tankWithoutLowerTanks);

					if (Collections.disjoint(tanksOnSameHeight, handledSourceTanks))
					{
						for (BlockPos tankOnSameHeight : tanksOnSameHeight)
						{
							adjustedPriority = (tankToPriorityMappings.containsKey(tankOnSameHeight)) ?
									Math.max(priority, tankToPriorityMappings.get(tankOnSameHeight)) : priority;

							tanksToPrioritize.put(tankOnSameHeight, adjustedPriority);
							newTanks.addAll(getAdjacentTanks(tankOnSameHeight, BlockSearchMode.Above));
						}
					}
				}
			}

			for (Entry<BlockPos, Integer> entry : tanksToPrioritize.entrySet())
			{
				setTankPriority(entry.getKey(), entry.getValue());
			}

			priority++;

			tanksWithoutLowerTanks.clear();
			handledSourceTanks.clear();
			handledSegmentTanks.clear();
			tanksToPrioritize.clear();
			currentTanks.clear();

			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());

		aStar = null;
	}

	/**
	 * Gets a list of all tanks on the same height level that are reachable from one another without ever stepping over the initial tanks height level.
	 * 
	 * @param startTank
	 * The tanks to start from.
	 * @return An {@link ArrayList} of found tanks.
	 */
	private ArrayList<BlockPos> getTanksOnSameHeight(BlockPos startTank)
	{
		if (startTank == null)
		{
			return null;
		}

		ArrayList<BlockPos> adjacentTanks;
		ArrayList<BlockPos> foundTanks = new ArrayList<BlockPos>();
		ArrayList<BlockPos> currentTanks = new ArrayList<BlockPos>();
		EnumSet<BlockSearchMode> searchFlags;
		HashSet<BlockPos> handledTanks = new HashSet<BlockPos>();
		HashSet<BlockPos> newTanks = new HashSet<BlockPos>();

		currentTanks.add(startTank);

		/*
		 * Algorithm 2:
		 * - uses basically the same flood find algorithm generateTankList() uses, only modification is the height level limit
		 */

		do
		{
			for (BlockPos currentTank : currentTanks)
			{
				if (currentTank.getY() == startTank.getY())
				{
					foundTanks.add(currentTank);
				}

				searchFlags = (currentTank.getY() < startTank.getY()) ? BlockSearchMode.All : BlockSearchMode.SameLevelAndBelow;
				adjacentTanks = getAdjacentTanks(currentTank, searchFlags);

				for (BlockPos adjacentTank : adjacentTanks)
				{
					if (!handledTanks.contains(adjacentTank))
					{
						newTanks.add(adjacentTank);
					}
				}

				handledTanks.add(currentTank);
			}

			currentTanks.clear();
			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());

		return foundTanks;
	}

	/**
	 * Sets or updates the priority for the {@link TankBlock} at the specified coordinates.
	 * 
	 * @param tank
	 * The {@link TankBlock} coordinates.
	 * @param priority
	 * The {@link TankBlock}s priority.
	 */
	private void setTankPriority(BlockPos tank, int priority)
	{
		if (tank == null || priority < 0)
		{
			return;
		}

		Integer oldPriority = tankToPriorityMappings.put(tank, priority);

		if (oldPriority == null)
		{
			tankPriorities.put(priority, tank);
		}
		else
		{
			tankPriorities.remove(oldPriority, tank);
			tankPriorities.put(priority, tank);
		}
	}

	/**
	 * Gets the closes, lowest tanks reachable from the specified tank.
	 * 
	 * @param startTank
	 * The {@link TankBlock}s coordinates to start from.
	 * @return An {@link ArrayList} of found tanks.
	 */
	private ArrayList<BlockPos> getClosestLowestTanks(BlockPos startTank)
	{
		if (startTank == null)
		{
			return null;
		}

		ArrayList<BlockPos> tanksInSegment;
		ArrayList<BlockPos> adjacentTanks;
		ArrayList<BlockPos> closestTanksWithTanksBelow;
		ArrayList<BlockPos> tanksWithTanksBelow = new ArrayList<BlockPos>();
		ArrayList<BlockPos> newTanks = new ArrayList<BlockPos>();
		ArrayList<BlockPos> foundTanks = new ArrayList<BlockPos>();
		ArrayList<BlockPos> currentTanks = new ArrayList<BlockPos>();

		currentTanks.add(startTank);

		/*
		 * Algorithm 1:
		 * - find all the tanks connected to the current one without going up or down, this is called a segment
		 * - find all tanks in the segment that have tanks below them
		 * - pick the ones that are closest to the current tank
		 * - repeat until no tanks are found below the tanks in the current segment
		 * - the tanks in the segments of the last iterations are the ones we are looking for
		 */

		do
		{
			for (BlockPos currentTank : currentTanks)
			{
				tanksInSegment = getTanksInSegment(currentTank);

				for (BlockPos segmentTank : tanksInSegment)
				{
					adjacentTanks = getAdjacentTanks(segmentTank, BlockSearchMode.Below);

					if (!adjacentTanks.isEmpty() && !hasPriority(adjacentTanks.get(0)))
					{
						tanksWithTanksBelow.add(segmentTank);
					}
				}

				if (!tanksWithTanksBelow.isEmpty())
				{
					// if there is more than one way down, only consider the closest ones
					closestTanksWithTanksBelow = (tanksWithTanksBelow.size() > 1) ?
							getClosestTanks(tanksInSegment, tanksWithTanksBelow, currentTank) : tanksWithTanksBelow;

					for (BlockPos closestTank : closestTanksWithTanksBelow)
					{
						newTanks.add(closestTank.down());
					}
				}
				else
				{
					foundTanks.addAll(tanksInSegment);
				}

				tanksWithTanksBelow.clear();
			}

			currentTanks.clear();
			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());

		return foundTanks;
	}

	/**
	 * Gets the {@link BlockPos} from the specified {@link Collection} that are closest to the specified destination.
	 * Note: A-Star needs to be set up before calling this.
	 * 
	 * @param passableBlocks
	 * The {@link BlockPos} the search algorithm is allowed to move through.
	 * @param sources
	 * The coordinates of the source blocks whose distances to the destination is measured.
	 * @param destination
	 * The coordinates of the destination block.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> getClosestTanks(Collection<BlockPos> passableBlocks, Collection<BlockPos> sources, BlockPos destination)
	{
		if (tanks == null || tanks.isEmpty() || sources == null || sources.isEmpty() || destination == null)
		{
			return null;
		}

		ArrayList<Integer> distances = new ArrayList<Integer>();
		Multimap<Integer, BlockPos> distanceToTanksMappings = ArrayListMultimap.create();
		int distance;

		// simply use my crappy A-Star implementation to measure the distances
		aStar.setPassableBlocks(tanks);

		for (BlockPos source : sources)
		{
			distance = (source.equals(destination)) ? 0 : aStar.getShortestPath(source, destination).currentCost;
			distances.add(distance);
			distanceToTanksMappings.put(distance, source);
		}

		Collections.sort(distances);

		return new ArrayList<BlockPos>(distanceToTanksMappings.get(distances.get(0)));
	}

	/**
	 * Gets all tanks in the same segment as the specified one. Segment means all tanks connected without going up or down.
	 * 
	 * @param firstTank
	 * The coordinates of the tank to start from.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> getTanksInSegment(BlockPos firstTank)
	{
		if (firstTank == null)
		{
			return null;
		}

		LinkedHashSet<BlockPos> foundTanks = new LinkedHashSet<BlockPos>();
		foundTanks.add(firstTank);

		ArrayList<BlockPos> currentTanks = new ArrayList<BlockPos>();
		ArrayList<BlockPos> newTanks = new ArrayList<BlockPos>();
		Collection<BlockPos> adjacentTanks;

		currentTanks.add(firstTank);

		do
		{
			for (BlockPos currentTank : currentTanks)
			{
				adjacentTanks = getAdjacentTanks(currentTank, BlockSearchMode.SameLevel);

				for (BlockPos adjacentTank : adjacentTanks)
				{
					if (foundTanks.add(adjacentTank))
					{
						newTanks.add(adjacentTank);
					}
				}
			}

			currentTanks.clear();
			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());

		return new ArrayList<BlockPos>(foundTanks);
	}

	/**
	 * Gets all adjacent tanks to the specified one (uses temporary tank list).
	 * 
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> getAdjacentTanks(BlockPos block)
	{
		return getOrFindAdjacentTanks(block, null, BlockSearchMode.All, true);
	}

	/**
	 * Gets adjacent tanks to the specified one (uses temporary tank list).
	 * 
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param mode
	 * Specifies which adjacent tanks should be included.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> getAdjacentTanks(BlockPos block, BlockSearchMode mode)
	{
		return getOrFindAdjacentTanks(block, mode, null, true);
	}

	/**
	 * Gets adjacent tanks to the specified one (uses temporary tank list).
	 * 
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param searchFlags
	 * Specifies which adjacent tanks should be included.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> getAdjacentTanks(BlockPos block, EnumSet<BlockSearchMode> searchFlags)
	{
		return getOrFindAdjacentTanks(block, null, searchFlags, true);
	}

	/**
	 * Gets all adjacent tanks to the specified one (uses {@link TileEntity} data to determine which blocks are valid tanks).
	 * 
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> findAdjacentTanks(BlockPos block)
	{
		return getOrFindAdjacentTanks(block, null, BlockSearchMode.All, false);
	}

	/**
	 * Gets all adjacent tanks to the specified one (uses {@link TileEntity} data to determine which blocks are valid tanks).
	 * 
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param mode
	 * Specifies which adjacent tanks should be included.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> findAdjacentTanks(BlockPos block, BlockSearchMode mode)
	{
		return getOrFindAdjacentTanks(block, mode, null, false);
	}

	/**
	 * Gets all adjacent tanks to the specified one (uses {@link TileEntity} data to determine which blocks are valid tanks).
	 * 
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param searchFlags
	 * Specifies which adjacent tanks should be included.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> findAdjacentTanks(BlockPos block, EnumSet<BlockSearchMode> searchFlags)
	{
		return getOrFindAdjacentTanks(block, null, searchFlags, false);
	}

	/**
	 * Gets all adjacent tanks to the specified one.
	 * 
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param mode
	 * Specifies which adjacent tanks should be included.
	 * @param searchFlags
	 * Specifies which adjacent tanks should be included.
	 * @param useTankList
	 * Specifies if the temporary tank list or {@link TileEntity} should be used to determine which blocks are valid tanks.
	 * @return
	 */
	private ArrayList<BlockPos> getOrFindAdjacentTanks(BlockPos block, BlockSearchMode mode, EnumSet<BlockSearchMode> searchFlags, boolean useTankList)
	{
		if (block == null || (mode == null && searchFlags == null))
		{
			return null;
		}

		ArrayList<BlockPos> foundTanks = new ArrayList<BlockPos>();
		ArrayList<BlockPos> adjacentBlocks = new ArrayList<BlockPos>();

		if (mode == BlockSearchMode.SameLevel || (searchFlags != null && searchFlags.contains(BlockSearchMode.SameLevel)))
		{
			adjacentBlocks.add(block.east());	// X+
			adjacentBlocks.add(block.west());	// X-
			adjacentBlocks.add(block.south());	// Z+
			adjacentBlocks.add(block.north());	// Z-
		}

		if (mode == BlockSearchMode.Above || (searchFlags != null && searchFlags.contains(BlockSearchMode.Above)))
		{
			adjacentBlocks.add(block.up());	// Y+
		}

		if (mode == BlockSearchMode.Below || (searchFlags != null && searchFlags.contains(BlockSearchMode.Below)))
		{
			adjacentBlocks.add(block.down());	// Y-
		}

		if (useTankList)
		{
			// use the tank cache to check if we found valid tanks
			for (BlockPos adjacentBlock : adjacentBlocks)
			{
				if (isInTankList(adjacentBlock))
				{
					foundTanks.add(adjacentBlock);
				}
			}
		}
		else
		{
			// use the block types and tile entity data to check if we found valid tanks
			for (BlockPos adjacentBlock : adjacentBlocks)
			{
				if (isUnlinkedTank(adjacentBlock))
				{
					foundTanks.add(adjacentBlock);
				}
			}
		}

		return foundTanks;
	}

	/**
	 * Checks if the block at the specified location is a {@link TankBlock} that is not connected to a {@link ValveBlock}.
	 * 
	 * @param block
	 * The coordinates of the block to check.
	 * @return <code>true</code> if the specified block is a valid {@link TankBlock}, otherwise <code>false</code>.
	 */
	private boolean isUnlinkedTank(BlockPos block)
	{
		if (block == null)
		{
			return false;
		}
		
		IBlockState state = worldObj.getBlockState(block);
		
		if (state != null)
		{
			if (state.getBlock() instanceof TankBlock)
			{
				TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, block);

				if (tankEntity != null)
				{
					return !tankEntity.isPartOfTank();
				}
			}
			else if (block.equals(pos) && tankPriorities.isEmpty())
			{
				// this valve is also considered a unlinked tank as long as it has no associated tanks
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the specified coordinates are contained in the temporary tank list.
	 * 
	 * @param pos
	 * The coordinates of the block to check.
	 * @return <code>true</code> if the specified coordinates were found, otherwise <code>false</code>.
	 */
	private boolean isInTankList(BlockPos pos)
	{
		if (pos == null)
		{
			return false;
		}

		if (tanks != null)
		{
			return tanks.contains(pos);
		}
		else if (tankPriorities != null)
		{
			return tankPriorities.values().contains(pos);
		}

		return false;
	}

	/**
	 * Checks if the tank at the specified coordinates has a priority associated with it.
	 * 
	 * @param tank
	 * The coordinates of the block to check.
	 * @return <code>true</code> if the tank has a priority, otherwise <code>false</code>.
	 */
	private boolean hasPriority(BlockPos tank)
	{
		return tankPriorities.containsValue(tank);
	}

	/**
	 * Writes the tank priority map to the specified NBT tag.
	 * 
	 * @param tag
	 * The tag to write to.
	 * @param priorities
	 * The tank priority map.
	 */
	private void writeTankPrioritiesToNBT(NBTTagCompound tag)
	{
		if (tag == null)
		{
			return;
		}

		NBTTagCompound tankPrioritiesTag = new NBTTagCompound();
		BlockPos currentCoords;
		int[] serializableEntry;
		int i = 0;

		for (Entry<Integer, BlockPos> entry : tankPriorities.entries())
		{
			currentCoords = entry.getValue();
			serializableEntry = new int[] { entry.getKey(), currentCoords.getX(), currentCoords.getY(), currentCoords.getZ() };
			tankPrioritiesTag.setIntArray(Integer.toString(i), serializableEntry);
			i++;
		}

		tag.setTag("TankPriorities", tankPrioritiesTag);
	}

	/**
	 * Read the tank priority map from the specified NBT tag.
	 * 
	 * @param tag
	 * The tag to read from.
	 * @return The tank priority map.
	 */
	private void readTankPrioritiesFromNBT(NBTTagCompound tag)
	{
		tankPriorities = ArrayListMultimap.create();

		if (tag != null)
		{
			NBTTagCompound tankPrioritiesTag = tag.getCompoundTag("TankPriorities");
			String key;
			int i = 0;
			int[] serializedEntry;

			while (tankPrioritiesTag.hasKey(key = Integer.toString(i)))
			{
				serializedEntry = tankPrioritiesTag.getIntArray(key);
				tankPriorities.put(serializedEntry[0], new BlockPos(serializedEntry[1], serializedEntry[2], serializedEntry[3]));
				i++;
			}
		}
	}
}
