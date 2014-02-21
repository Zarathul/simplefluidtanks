package simplefluidtanks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;

/**
 * Holds {@link TileEntity} data for {@link ValveBlock}s,
 */
public class ValveBlockEntity extends TileEntity implements IFluidHandler
{
	/**
	 * The {@link FluidTank} that actually holds all the fluid in the multiblock tank.
	 */
	private FluidTank internalTank;
	
	/**
	 * The fill priorities of all connected {@link TankBlock}s.
	 */
	private Multimap<Integer, BlockCoords> tankPriorities;
	
	/**
	 * A bitmask storing which sides of the {@link ValveBlock} face connected {@link TankBlock}s.
	 * @see Direction
	 */
	private byte tankFacingSides;
	
	
	/**
	 * A temporary mapping of connected {@link TankBlock}s to their priorities.<br>
	 * <b>Caution:</b> This will be empty after reloading the {@link ValveBlock} from nbt data.
	 */
	private HashMap<BlockCoords, Integer> tankToPriorityMappings;
	
	/**
	 * A temporary set of all connected {@link TankBlock}s.<br>
	 * <b>Caution:</b> This will be empty after reloading the {@link ValveBlock} from nbt data.
	 */
	private HashSet<BlockCoords> tanks;
	
	/**
	 * Holds a {@link BasicAStar} instance while the tank finding algorithms are running. 
	 */
	private BasicAStar aStar;
	
	public ValveBlockEntity()
	{
		super();
		internalTank = new FluidTank(0);
		tankPriorities = ArrayListMultimap.create();
		tankFacingSides = -1;
	}

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        internalTank.readFromNBT(tag);
        
        try
        {
			tanksFromByteArray(tag.getByteArray("TankPriorities"));
		}
        catch (ClassNotFoundException e)
        {
			e.printStackTrace();
		}
        catch (IOException e)
        {
			e.printStackTrace();
		}
        
        tankFacingSides = tag.getByte("TankFacingSides");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        internalTank.writeToNBT(tag);
        
        try
        {
			tag.setByteArray("TankPriorities", tanksAsByteArray());
		}
        catch (IOException e)
        {
			e.printStackTrace();
		}
        
        tag.setByte("TankFacingSides", tankFacingSides);
    }

    @Override
    public int fill(ForgeDirection from, FluidStack drainFluid, boolean doFill)
    {
    	if (!worldObj.isRemote && hasTanks())
    	{
        	int fillAmount = internalTank.fill(drainFluid, doFill);
        	
        	if (doFill && fillAmount > 0)
        	{
        		distributeFluidToTanks();
        		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
        		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            	// triggers onNeighborTileChange on neighboring blocks, this is needed for comparators to work
        		worldObj.func_96440_m(xCoord, yCoord, zCoord, SimpleFluidTanks.valveBlock.blockID);
        	}
        	
            return fillAmount;
    	}
    	
    	return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack drainFluid, boolean doDrain)
    {
    	return drain(from, drainFluid, -1, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int drainAmount, boolean doDrain)
    {
    	return drain(from, null, drainAmount, doDrain);
    }
    
    /**
     * Drains fluid from the multiblock tank.
     * @param from
     * The direction the fluid is drained from.
     * @param drainFluid
     * The fluid type and amount to drain.
     * @param drainAmount
     * The amount of fluid to drain.
     * @param doDrain
     * Indicates whether the draining should be simulated or not.
     * @return
     * The type and amount of fluid drained.
     */
    private FluidStack drain(ForgeDirection from, FluidStack drainFluid, int drainAmount, boolean doDrain)
    {
    	if (!worldObj.isRemote && hasTanks())
    	{
            FluidStack drainedFluid = (drainFluid != null && drainFluid.isFluidEqual(internalTank.getFluid())) ? internalTank.drain(drainFluid.amount, doDrain) :
            						  (drainAmount >= 0) ? internalTank.drain(drainAmount, doDrain) :
            						  null;
            
            if (doDrain && drainedFluid != null && drainedFluid.amount > 0)
            {
            	distributeFluidToTanks();
        		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
            	worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            	// triggers onNeighborTileChange on neighboring blocks, this is needed for comparators to work
            	worldObj.func_96440_m(xCoord, yCoord, zCoord, SimpleFluidTanks.valveBlock.blockID);
            }
            
            return drainedFluid;
    	}
    	
    	return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
    	if (hasTanks() && !isFacingTank(Direction.fromForge(from)) && fluid != null && !internalTank.isFull())
    	{
        	FluidStack tankFluid = internalTank.getFluid();
        	
        	return (tankFluid == null || tankFluid.isFluidEqual(new FluidStack(fluid, 0)));
    	}
    	
    	return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
    	if (hasTanks() && !isFacingTank(Direction.fromForge(from)) && fluid != null && internalTank.getFluidAmount() > 0)
    	{
        	FluidStack tankFluid = internalTank.getFluid();
        	
        	return (tankFluid != null && tankFluid.isFluidEqual(new FluidStack(fluid, 0)));
    	}
    	
    	return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[] { internalTank.getInfo() };
    }
	
	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, -1, tag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		readFromNBT(packet.data);
		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
	}

	/**
	 * Gets the multiblock tanks capacity.
	 * @return
	 * The capacity in millibuckets.
	 */
	public int getCapacity()
	{
		return internalTank.getCapacity();
	}
	
	/**
	 * Gets the amount of fluid in the multiblock tank.
	 * @return
	 * The amount of fluid in millibuckets.
	 */
	public int getFluidAmount()
	{
		return internalTank.getFluidAmount();
	}
	
	/**
	 * Gets the fluid in the multiblock tank.
	 * @return
	 * A {@link FluidStack} representing the fluid in the multiblock tank.
	 */
	public FluidStack getFluid()
	{
		return internalTank.getFluid();
	}
	
	/**
	 * Gets the bitmask storing which sides of the {@link ValveBlock} face connected {@link TankBlock}s.
	 * @return
	 * A bitmask.
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
		
		BlockCoords coords = new BlockCoords(xCoord, yCoord, zCoord);
		
		if (isInTankList(coords.cloneWithOffset(0, 0, 1)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(Direction.ZPOS);
		}
		
		if (isInTankList(coords.cloneWithOffset(0, 0, -1)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(Direction.ZNEG);
		}
		
		if (isInTankList(coords.cloneWithOffset(1, 0, 0)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(Direction.XPOS);
		}
		
		if (isInTankList(coords.cloneWithOffset(-1, 0, 0)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(Direction.XNEG);
		}
		
		if (isInTankList(coords.cloneWithOffset(0, 1, 0)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(Direction.YPOS);
		}
		
		if (isInTankList(coords.cloneWithOffset(0, -1, 0)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings.get(Direction.YNEG);
		}
		
		tankFacingSides = (byte)sides;
	}
	
	/**
	 * Checks if the specified side of the {@link ValveBlock} is facing a connected {@link TankBlock}.
	 * @param side
	 * The side to check.
	 * @return
	 * <code>true</code> if the {@link ValveBlock} is facing a connected {@link TankBlock} on the specified side, otherwise <code>false</code>.
	 * @see Direction
	 */
	public boolean isFacingTank(int side)
	{
		if (side >= Byte.MIN_VALUE && side <= Byte.MAX_VALUE)
		{
			byte flags = (byte)(int)Direction.sidesToBitFlagsMappings.get(side);
			
			return (tankFacingSides & flags) == flags;
		}
		
		return false;
	}
	
	/**
	 * Checks if the {@link ValveBlock} has connected {@link TankBlock}s.
	 * @return
	 * <code>true</code> if the {@link ValveBlock} has connected {@link TankBlock}s, otherwise <code>false</code>.
	 */
	public boolean hasTanks()
	{
		// the ValveBlock also counts as a tank in the multiblock structure, that is why it is " > 1" instead of " > 0"
		return tankPriorities.size() > 1;
	}
	
	/**
	 * Finds all {@link TankBlock}s connected to the {@link ValveBlock} and computes their filling priorities.
	 */
	public void findTanks()
	{
		generateTankList();
		computeFillPriorities();
		
		ArrayList<TankBlockEntity> tankEntities = new ArrayList<TankBlockEntity>(tankPriorities.size());
		
		// set the valve for all connected tanks
		for (BlockCoords tankCoords : tankPriorities.values())
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tankCoords);
			
			if (tankEntity != null)
			{
				tankEntity.setValve(new BlockCoords(xCoord, yCoord, zCoord));
				tankEntities.add(tankEntity);
			}
		}
		
		// Update the textures for all connected tanks. This needs to be done after setting the valve. Otherwise the connected textures can't be properly calculated.
		for (TankBlockEntity t : tankEntities)
		{
			t.updateTextures();
		}
		
		// calculate and set the internal tanks capacity, note the " + 1" is needed because the ValveBlock itself is considered a tank with storage capacity
		internalTank.setCapacity((tankEntities.size() + 1) * SimpleFluidTanks.bucketsPerTank * FluidContainerRegistry.BUCKET_VOLUME);
		
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
	}

	/**
	 * Resets aka. disconnects all connected {@link TankBlock}s and resets the {@link ValveBlock} itself (capacity etc.).
	 */
	public void resetTanks()
	{
		for (BlockCoords tankCoords : tankPriorities.values())
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tankCoords);
			
			if (tankEntity != null)
			{
				tankEntity.reset();
			}
		}
		
		tankPriorities.clear();
		tankFacingSides = 0;
		internalTank.setCapacity(0);
		internalTank.setFluid(null);
		
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    	// triggers onNeighborTileChange on neighboring blocks, this is needed for comparators to work
    	worldObj.func_96440_m(xCoord, yCoord, zCoord, SimpleFluidTanks.valveBlock.blockID);
	}
	
	/**
	 * Serializes the <code>tankPriorities</code> Multimap into a byte array.
	 * @return
	 * The {@link TankBlock} priorities in serialized form.
	 * @throws IOException
	 * Occurs if the serialization fails.
	 */
	private byte[] tanksAsByteArray() throws IOException
	{
		byte[] data = null;
		ByteArrayOutputStream byteStream = null;
		ObjectOutputStream objStream = null;
		
		try
		{
			byteStream = new ByteArrayOutputStream();
			objStream = new ObjectOutputStream(byteStream);
			objStream.writeObject(tankPriorities);
			data = byteStream.toByteArray();
		}
		finally
		{
			objStream.close();
		}
		
		return ((data != null) ? data : new byte[0]);
	}
	
	/**
	 * Deserializes the <code>tankPriorities</code> Multimap from a byte array.
	 * @param data
	 * The {@link TankBlock} priorities in serialized form.
	 * @throws IOException
	 * Occurs if the deserialization fails.
	 * @throws ClassNotFoundException
	 * Occurs if the deserialization fails.
	 */
	private void tanksFromByteArray(byte[] data) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream byteStream = null;
		ObjectInputStream objStream = null;
		
		try
		{
			byteStream = new ByteArrayInputStream(data);
			objStream = new ObjectInputStream(byteStream);
			tankPriorities = (ArrayListMultimap<Integer, BlockCoords>)objStream.readObject();
		}
		finally
		{
			objStream.close();
		}
	}
	
	/**
	 * Distributes the fluid currently held by the multiblock tank over the connected {@link TankBlock}s.
	 */
	private void distributeFluidToTanks()
	{
		// returned amount is mb(milli buckets)
		int amountToDistribute = internalTank.getFluidAmount();
		
		if (amountToDistribute == 0 || amountToDistribute == internalTank.getCapacity()) // there is nothing to distribute or the internal tank is full (no fill percentage calculations needed)
		{
			int percentage = (amountToDistribute == 0) ? 0 : 100;
			
			for (BlockCoords tankCoords : tankPriorities.values())
			{
				TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tankCoords);
				
				if (tankEntity != null)
				{
					tankEntity.setFillPercentage(percentage);
				}
			}
		}
		else
		{
			// get the fill priorities and sort them low to high
			int[] priorities = Ints.toArray(tankPriorities.keySet());
			Arrays.sort(priorities);
			
			Collection<BlockCoords> tanksToFill = null;
			
			// for each priority get all the TankBlocks and fill them evenly
			for (int i = 0; i < priorities.length; i++)
			{
				tanksToFill = tankPriorities.get(priorities[i]);
				
				int capacity = tanksToFill.size() * SimpleFluidTanks.bucketsPerTank * FluidContainerRegistry.BUCKET_VOLUME;
				double fillPercentage = Math.min((double)amountToDistribute / (double)capacity * 100d, 100d);
				
				for (BlockCoords tank : tanksToFill)
				{
					TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tank);
					
					if (tankEntity != null)
					{
						tankEntity.setFillPercentage((int)fillPercentage);
					}
				}
				
				amountToDistribute -= Math.ceil(capacity * fillPercentage / 100d);
				
				if (amountToDistribute <= 0)
				{
					break;
				}
			}
		}
	}
	
	/**
	 * Generates a list of the coordinates of all {@link TankBlock}s connected to the {@link ValveBlock}. This list is temporary and contains no priority information.
	 */
	private void generateTankList()
	{
		tanks = new HashSet<BlockCoords>();
		
		BlockCoords startCoords = new BlockCoords(xCoord, yCoord, zCoord);
		ArrayList<BlockCoords> lastFoundTanks = new ArrayList<BlockCoords>();
		Set<BlockCoords> newFoundTanks = new HashSet<BlockCoords>();
		Collection<BlockCoords> adjacentTanks;
		
		lastFoundTanks.add(startCoords);
		
		// simple flood find algorithm: get all adjacent blocks to the current one -> repeat for all blocks found until there is nothing left to find
		do
		{
			for (BlockCoords tank : lastFoundTanks)
			{
				adjacentTanks = findAdjacentTanks(tank);
				
				for (BlockCoords adjacentTank : adjacentTanks)
				{
					if (!tanks.contains(adjacentTank))
					{
						newFoundTanks.add(adjacentTank);
					}
				}
				
				tanks.addAll(adjacentTanks);
			}
			
			lastFoundTanks.clear();
			lastFoundTanks.addAll(newFoundTanks);
			newFoundTanks.clear();
		}
		while (lastFoundTanks.size() > 0);
	}
	
	/**
	 * Computes the fill priorities for all connected {@link TankBlock}s.
	 */
	private void computeFillPriorities()
	{
		aStar = new BasicAStar();
		tankToPriorityMappings = new HashMap<BlockCoords, Integer>();
		
		BlockCoords startTank = new BlockCoords(xCoord, yCoord, zCoord);
		BlockCoords sourceTank;
		
		HashSet<BlockCoords> newTanks =  new HashSet<BlockCoords>();
		HashSet<BlockCoords> handledTanks =  new HashSet<BlockCoords>();
		HashMap<BlockCoords, Integer> tanksToPrioritize = new HashMap<BlockCoords, Integer>();
		ArrayList<BlockCoords> tanksWithoutLowerTanks = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> currentTanks =  new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> tanksOnSameHeight;
		ArrayList<BlockCoords> lowerTanks;
		
		currentTanks.add(startTank);
		
		int priority = 0;
		int adjustedPriority;
		
		/* Prioritization uses two separate algorithms:
		 * - find the closest, lowest tanks that are connected to the current one (algorithm 1)
		 * - if the closest, lowest tank is the same as the current tank, find all tanks on the same height level that are reachable 
		 *   without ever going over the current tanks height level and set them all to the current priority if the current priority
		 *   is a higher value (= actual lower priority) than the priority the tanks already have (algorithm 2)
		 * - otherwise just set the closest, lowest tanks priority to the current priority value
		 * - in both cases add the tanks directly above the found tanks to the list of tanks that are processed in the next iteration
		 * The order of execution is important. Tanks found by the algorithm 1 have to be prioritized before tanks found by algorithm 2.
		 */
		
		do
		{
			for (BlockCoords currentTank : currentTanks)
			{
				lowerTanks = getClosestLowestTanks(currentTank);
				
				// handle tanks with lower tanks first, store the rest for later processing
				if (lowerTanks.get(0) == currentTank)	
				{
					tanksWithoutLowerTanks.add(currentTank);
				}
				else
				{
					handledTanks.add(currentTank);
					
					for (BlockCoords lowerTank : lowerTanks)
					{
						tanksToPrioritize.put(lowerTank, priority);
						newTanks.addAll(getAdjacentTanks(lowerTank, BlockSearchMode.Above));
					}
				}
			}
			
			// find connected tanks on the same height without stepping over the height level of the initial tank 
			for (BlockCoords tank : tanksWithoutLowerTanks)
			{
				if (!tanksToPrioritize.containsKey(tank))
				{
					tanksOnSameHeight = getTanksOnSameHeight(tank);
					
					if (Collections.disjoint(tanksOnSameHeight, handledTanks))
					{
						for (BlockCoords sameHeightTank : tanksOnSameHeight)
						{
							adjustedPriority = (tankToPriorityMappings.containsKey(sameHeightTank)) ? Math.max(priority, tankToPriorityMappings.get(sameHeightTank)) : priority;
							
							tanksToPrioritize.put(sameHeightTank, adjustedPriority);
							newTanks.addAll(getAdjacentTanks(sameHeightTank, BlockSearchMode.Above));
						}
					}
				}
			}
			
			for (Entry<BlockCoords, Integer> entry : tanksToPrioritize.entrySet())
			{
				setTankPriority(entry.getKey(), entry.getValue());
			}
			
			priority++;
			
			tanksWithoutLowerTanks.clear();
			handledTanks.clear();
			tanksToPrioritize.clear();
			currentTanks.clear();
			
			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());
		
		aStar = null;
	}
	
	/**
	 * Gets a list of all tanks on the same height level that are reachable from one another without ever stepping 
	 * over the initial tanks height level.
	 * @param startTank
	 * The tanks to start from.
	 * @return
	 * An {@link ArrayList} of found tanks.
	 */
	private ArrayList<BlockCoords> getTanksOnSameHeight(BlockCoords startTank)
	{
		if (startTank == null)
		{
			return null;
		}
		
		EnumSet<BlockSearchMode> searchFlags;
		
		ArrayList<BlockCoords> adjacentTanks;
		HashSet<BlockCoords> visitedTanks = new HashSet<BlockCoords>();
		ArrayList<BlockCoords> foundTanks = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> newTanks = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> lastFoundTanks = new ArrayList<BlockCoords>();
		lastFoundTanks.add(startTank);
		
		/*
		 * Algorithm 2:
		 * - uses basically the same flood find algorithm generateTankList() uses, only modification is the height level limit
		 */
		
		do
		{
			for (BlockCoords currentTank : lastFoundTanks)
			{
				if (currentTank.y == startTank.y)
				{
					foundTanks.add(currentTank);
				}
				
				searchFlags = (currentTank.y < startTank.y) ? BlockSearchMode.All : BlockSearchMode.SameLevelAndBelow;
				adjacentTanks = getAdjacentTanks(currentTank, searchFlags);
				
				for (BlockCoords adjacentTank : adjacentTanks)
				{
					if (!visitedTanks.contains(adjacentTank))
					{
						newTanks.add(adjacentTank);
					}
				}
				
				visitedTanks.add(currentTank);
			}
			
			lastFoundTanks.clear();
			lastFoundTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!lastFoundTanks.isEmpty());
		
		return foundTanks;
	}
	
	/**
	 * Sets or updates the priority for the {@link TankBlock} at the specified coordinates.
	 * @param tank
	 * The {@link TankBlock} coordinates.
	 * @param priority
	 * The {@link TankBlock}s priority.
	 */
	private void setTankPriority(BlockCoords tank, int priority)
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
	 * @param startTank
	 * The {@link TankBlock}s coordinates to start from.
	 * @return
	 * An {@link ArrayList} of found tanks.
	 */
	private ArrayList<BlockCoords> getClosestLowestTanks(BlockCoords startTank)
	{
		if (startTank == null)
		{
			return null;
		}
		
		ArrayList<BlockCoords> tanksInSegment;
		ArrayList<BlockCoords> adjacentTanks;
		ArrayList<BlockCoords> closestTanksWithTanksBelow;
		ArrayList<BlockCoords> tanksWithTanksBelow = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> newTanks = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> foundTanks = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> currentTanks = new ArrayList<BlockCoords>();
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
			for (BlockCoords currentTank : currentTanks)
			{
				tanksInSegment = getTanksInSegment(currentTank);
				
				for (BlockCoords segmentTank : tanksInSegment)
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
					closestTanksWithTanksBelow = (tanksWithTanksBelow.size() > 1) ? getClosestTanks(tanksInSegment, tanksWithTanksBelow, currentTank) : tanksWithTanksBelow;
					
					for (BlockCoords closestTank : closestTanksWithTanksBelow)
					{
						newTanks.add(closestTank.cloneWithOffset(0, -1));
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
	 * Gets the {@link BlockCoords} from the specified {@link Collection} that are closest to the specified destination.
	 * @param passableBlocks
	 * The {@link BlockCoords} the search algorithm is allowed to move through.
	 * @param sources
	 * The coordinates of the source blocks whose distances to the destination is measured.
	 * @param destination
	 * The coordinates of the destination block.
	 * @return
	 * An {@link ArrayList} of the found {@link BlockCoords}.
	 */
	private ArrayList<BlockCoords> getClosestTanks(Collection<BlockCoords> passableBlocks, Collection<BlockCoords> sources, BlockCoords destination)
	{
		if (tanks == null || tanks.isEmpty() || sources == null || sources.isEmpty() || destination == null)
		{
			return null;
		}
		
		ArrayList<Integer> distances = new ArrayList<Integer>();
		Multimap<Integer, BlockCoords> distanceToTanksMappings = ArrayListMultimap.create();
		int distance;
		
		// simply use my crappy A-Star implementation to measure the distances
		aStar.setPassableBlocks(tanks);
		
		for (BlockCoords source : sources)
		{
			distance = (source.equals(destination)) ? 0 : aStar.getShortestPath(source, destination).currentCost;
			distances.add(distance);
			distanceToTanksMappings.put(distance, source);
		}
		
		Collections.sort(distances);
		
		return new ArrayList<BlockCoords>(distanceToTanksMappings.get(distances.get(0)));
	}
	
	/**
	 * Gets all tanks in the same segment as the specified one. Segment means all tanks 
	 * connected without going up or down.
	 * @param firstTank
	 * The coordinates of the tank to start from.
	 * @return
	 * An {@link ArrayList} of the found {@link BlockCoords}.
	 */
	private ArrayList<BlockCoords> getTanksInSegment(BlockCoords firstTank)
	{
		if (firstTank == null)
		{
			return null;
		}
		
		LinkedHashSet<BlockCoords> tanksInSegment = new LinkedHashSet<BlockCoords>();
		tanksInSegment.add(firstTank);
		
		ArrayList<BlockCoords> lastFoundTanks = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> newFoundTanks = new ArrayList<BlockCoords>();
		Collection<BlockCoords> adjacentTanks;
		
		lastFoundTanks.add(firstTank);
		
		do
		{
			for (BlockCoords tank : lastFoundTanks)
			{
				adjacentTanks = getAdjacentTanks(tank, BlockSearchMode.SameLevel);
				
				for (BlockCoords adjacentTank : adjacentTanks)
				{
					if (tanksInSegment.add(adjacentTank))
					{
						newFoundTanks.add(adjacentTank);
					}
				}
			}
			
			lastFoundTanks.clear();
			lastFoundTanks.addAll(newFoundTanks);
			newFoundTanks.clear();
		}
		while (!lastFoundTanks.isEmpty());
		
		return new ArrayList<BlockCoords>(tanksInSegment);
	}
	
	/**
	 * Gets all adjacent tanks to the specified one (uses temporary tank list).
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @return
	 * An {@link ArrayList} of the found {@link BlockCoords}.
	 */
	private ArrayList<BlockCoords> getAdjacentTanks(BlockCoords block)
	{
		return getOrFindAdjacentTanks(block, null, BlockSearchMode.All, true);
	}
	
	/**
	 * Gets adjacent tanks to the specified one (uses temporary tank list).
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param mode
	 * Specifies which adjacent tanks should be included.
	 * @return
	 * An {@link ArrayList} of the found {@link BlockCoords}.
	 */
	private ArrayList<BlockCoords> getAdjacentTanks(BlockCoords block, BlockSearchMode mode)
	{
		return getOrFindAdjacentTanks(block, mode, null, true);
	}
	
	/**
	 * Gets adjacent tanks to the specified one (uses temporary tank list).
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param searchFlags
	 * Specifies which adjacent tanks should be included.
	 * @return
	 * An {@link ArrayList} of the found {@link BlockCoords}.
	 */
	private ArrayList<BlockCoords> getAdjacentTanks(BlockCoords block, EnumSet<BlockSearchMode> searchFlags)
	{
		return getOrFindAdjacentTanks(block, null, searchFlags, true);
	}
	
	/**
	 * Gets all adjacent tanks to the specified one (uses {@link TileEntity} data to determine which blocks are valid tanks).
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @return
	 * An {@link ArrayList} of the found {@link BlockCoords}.
	 */
	private ArrayList<BlockCoords> findAdjacentTanks(BlockCoords block)
	{
		return getOrFindAdjacentTanks(block, null, BlockSearchMode.All, false);
	}
	
	/**
	 * Gets all adjacent tanks to the specified one (uses {@link TileEntity} data to determine which blocks are valid tanks).
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param mode
	 * Specifies which adjacent tanks should be included.
	 * @return
	 * An {@link ArrayList} of the found {@link BlockCoords}.
	 */
	private ArrayList<BlockCoords> findAdjacentTanks(BlockCoords block, BlockSearchMode mode)
	{
		return getOrFindAdjacentTanks(block, mode, null, false);
	}
	
	/**
	 * Gets all adjacent tanks to the specified one (uses {@link TileEntity} data to determine which blocks are valid tanks).
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param searchFlags
	 * Specifies which adjacent tanks should be included.
	 * @return
	 * An {@link ArrayList} of the found {@link BlockCoords}.
	 */
	private ArrayList<BlockCoords> findAdjacentTanks(BlockCoords block, EnumSet<BlockSearchMode> searchFlags)
	{
		return getOrFindAdjacentTanks(block, null, searchFlags, false);
	}
	
	/**
	 * Gets all adjacent tanks to the specified one.
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
	private ArrayList<BlockCoords> getOrFindAdjacentTanks(BlockCoords block, BlockSearchMode mode, EnumSet<BlockSearchMode> searchFlags, boolean useTankList)
	{
		if (block == null || (mode == null && searchFlags == null))
		{
			return null;
		}
		
		ArrayList<BlockCoords> foundTanks = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> adjacentBlocks = new ArrayList<BlockCoords>();
		
		if (mode == BlockSearchMode.SameLevel || (searchFlags != null && searchFlags.contains(BlockSearchMode.SameLevel)))
		{
			adjacentBlocks.add(new BlockCoords(block.x + 1, block.y, block.z));	// X+
			adjacentBlocks.add(new BlockCoords(block.x - 1, block.y, block.z));	// X-
			adjacentBlocks.add(new BlockCoords(block.x, block.y, block.z + 1));	// Z+
			adjacentBlocks.add(new BlockCoords(block.x, block.y, block.z - 1));	// Z-
		}
		
		if (mode == BlockSearchMode.Above || (searchFlags != null && searchFlags.contains(BlockSearchMode.Above)))
		{
			adjacentBlocks.add(new BlockCoords(block.x, block.y + 1, block.z));	// Y+
		}
		
		if (mode == BlockSearchMode.Below || (searchFlags != null && searchFlags.contains(BlockSearchMode.Below)))
		{
			adjacentBlocks.add(new BlockCoords(block.x, block.y - 1, block.z));	// Y-
		}
		
		if (useTankList)
		{
			// use the tank cache to check if we found valid tanks
			for (BlockCoords adjacentBlock : adjacentBlocks)
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
			for (BlockCoords adjacentBlock : adjacentBlocks)
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
	 * @param block
	 * The coordinates of the block to check.
	 * @return
	 * <code>true</code> if the specified block is a valid {@link TankBlock}, otherwise <code>false</code>.
	 */
	private boolean isUnlinkedTank(BlockCoords block)
	{
		if (block == null)
		{
			return false;
		}
		
		if (worldObj.getBlockId(block.x, block.y, block.z) == SimpleFluidTanks.tankBlock.blockID)
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, block);
			
			if (tankEntity != null)
			{
				return !tankEntity.isPartOfTank();
			}
		}
		else if (block.equals(xCoord, yCoord, zCoord) && tankPriorities.isEmpty())
		{
			// this valve is also considered a unlinked tank as long as it has no associated tanks  
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the specified coordinates are contained in the temporary tank list.
	 * @param block
	 * The coordinates of the block to check.
	 * @return
	 * <code>true</code> if the specified coordinates were found, otherwise <code>false</code>.
	 */
	private boolean isInTankList(BlockCoords block)
	{
		if (block == null)
		{
			return false;
		}
		
		if (tanks != null)
		{
			return tanks.contains(block);
		}
		else if (tankPriorities != null)
		{
			return tankPriorities.values().contains(block);
		}
		
		return false;
	}
	
	/**
	 * Checks if the tank at the specified coordinates has a priority associated with it.
	 * @param tank
	 * The coordinates of the block to check.
	 * @return
	 * <code>true</code> if the tank has a priority, otherwise <code>false</code>.
	 */
	private boolean hasPriority(BlockCoords tank)
	{
		return tankPriorities.containsValue(tank);
	}
}
