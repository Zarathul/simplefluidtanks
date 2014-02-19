package simplefluidtanks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bouncycastle.util.CollectionStore;

import simplefluidtanks.BasicAStar.Node;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import net.minecraft.launchwrapper.LogWrapper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class ValveBlockEntity extends TileEntity implements IFluidHandler
{
	private FluidTank internalTank;
	private Multimap<Integer, BlockCoords> tankPriorities;
	private byte tankFacingSides;
	
	private HashMap<BlockCoords, Integer> tankToPriorityMappings;
	private HashSet<BlockCoords> tanks;
	
	private BasicAStar aStar = new BasicAStar();
	
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

	public int getCapacity()
	{
		return internalTank.getCapacity();
	}
	
	public int getFluidAmount()
	{
		return internalTank.getFluidAmount();
	}
	
	public FluidStack getFluid()
	{
		return internalTank.getFluid();
	}
	
	public int getTankFacingSides()
	{
		return tankFacingSides;
	}
	
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
	
	public boolean isFacingTank(int side)
	{
		if (side >= Byte.MIN_VALUE && side <= Byte.MAX_VALUE)
		{
			byte flags = (byte)(int)Direction.sidesToBitFlagsMappings.get(side);
			
			return (tankFacingSides & flags) == flags;
		}
		
		return false;
	}
	
	public boolean hasTanks()
	{
		return tankPriorities.size() > 1;
	}
	
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
				tankEntity.setValve(xCoord, yCoord, zCoord);
				tankEntities.add(tankEntity);
			}
		}
		
		// Update the textures for all connected tanks. This needs to be done after setting the valve. Otherwise the connected textures can't be properly calculated.
		for (TankBlockEntity t : tankEntities)
		{
			t.updateTextures();
		}
		
		internalTank.setCapacity((tankEntities.size() + 1) * SimpleFluidTanks.bucketsPerTank * FluidContainerRegistry.BUCKET_VOLUME);
		
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
	}

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
	
	private void distributeFluidToTanks()
	{
		// returned amount is mb(milli buckets)
		int amountToDistribute = internalTank.getFluidAmount();
		
		if (amountToDistribute == 0 || amountToDistribute == internalTank.getCapacity())
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
			int[] priorities = Ints.toArray(tankPriorities.keySet());
			Arrays.sort(priorities);
			
			Collection<BlockCoords> tanksToFill = null;
			
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
				
				amountToDistribute -= Math.ceil((double)capacity * (double)fillPercentage / 100d);
				
				if (amountToDistribute <= 0)
				{
					break;
				}
			}
		}
	}
	
	private void generateTankList()
	{
		tanks = new HashSet<BlockCoords>();
		
		BlockCoords startCoords = new BlockCoords(xCoord, yCoord, zCoord);
		ArrayList<BlockCoords> lastFoundTanks = new ArrayList<BlockCoords>();
		Set<BlockCoords> newFoundTanks = new HashSet<BlockCoords>();
		Collection<BlockCoords> adjacentTanks;
		EnumSet<BlockSearchMode> searchAll = EnumSet.of(BlockSearchMode.Above, BlockSearchMode.Below, BlockSearchMode.SameLevel);
		
		lastFoundTanks.add(startCoords);
		
		do
		{
			for (BlockCoords tank : lastFoundTanks)
			{
				adjacentTanks = findAdjacentTanks(tank, searchAll);
				
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
	
	private void computeFillPriorities()
	{
		tankToPriorityMappings = new HashMap<BlockCoords, Integer>();
		
		BlockCoords startTank = new BlockCoords(xCoord, yCoord, zCoord);
		
		HashSet<BlockCoords> newTanks =  new HashSet<BlockCoords>();
		HashMap<BlockCoords, Integer> tanksToPrioritize = new HashMap<BlockCoords, Integer>();
		ArrayList<BlockCoords> tanksOnSameHeight;
		ArrayList<BlockCoords> closestLowestTanks;
		ArrayList<BlockCoords> currentTanks =  new ArrayList<BlockCoords>();
		currentTanks.add(startTank);
		
		int priority = 0;
		int adjustedPriority;
		
		// TODO: stop same height branches if one of its tanks has close lower tanks
		
		do
		{
			for (BlockCoords currentTank : currentTanks)
			{
				closestLowestTanks = getClosestLowestTanks(currentTank);
				
				if (closestLowestTanks.get(0) == currentTank)
				{
					if (tanksToPrioritize.containsKey(currentTank))
					{
						continue;
					}
					
					tanksOnSameHeight = getTanksOnSameHeight(currentTank);
					
					for (BlockCoords sameHeightTank : tanksOnSameHeight)
					{
						adjustedPriority = (tankToPriorityMappings.containsKey(sameHeightTank)) ? Math.max(priority, tankToPriorityMappings.get(sameHeightTank)) : priority;
						
						tanksToPrioritize.put(sameHeightTank, adjustedPriority);
						newTanks.addAll(getAdjacentTanks(sameHeightTank, BlockSearchMode.Above));
					}
				}
				else
				{
					for (BlockCoords lowTank : closestLowestTanks)
					{
						tanksToPrioritize.put(lowTank, priority);
						newTanks.addAll(getAdjacentTanks(lowTank, BlockSearchMode.Above));
					}
				}
			}
			
			for (Entry<BlockCoords, Integer> entry : tanksToPrioritize.entrySet())
			{
				setTankPriority(entry.getKey(), entry.getValue());
			}
			
			tanksToPrioritize.clear();
			
			priority++;
			
			currentTanks.clear();
			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());
		
		// TODO: remove
		outputPriorities();
	}
	
	private void outputPriorities()
	{
		int[] priorities = Ints.toArray(tankPriorities.keySet());
		int currentPriority;
		Arrays.sort(priorities);
		
		Collection<BlockCoords> tanksToFill = null;
		
		for (int i = 0; i < priorities.length; i++)
		{
			currentPriority = priorities[i];
			tanksToFill = tankPriorities.get(currentPriority);
			
			System.out.println(currentPriority);
			
			for (BlockCoords tank : tanksToFill)
			{
				System.out.println("- " + tank);
			}
		}
		
		System.out.println(sameHTimeCalls + "x getTanksOnSameHeight(): " + sameHTime / 1000d + "s");
	}
	
	private long sameHTime = 0;
	private int sameHTimeCalls = 0;
	
	private ArrayList<BlockCoords> getTanksOnSameHeight(BlockCoords startTank)
	{
		StopWatch timer = new StopWatch();
		timer.start();
		
		if (startTank == null)
		{
			return null;
		}
		
		EnumSet<BlockSearchMode> searchBelowOnly = EnumSet.of(BlockSearchMode.Below);
		EnumSet<BlockSearchMode> searchAboveAndBelow = EnumSet.of(BlockSearchMode.Above, BlockSearchMode.Below);
		EnumSet<BlockSearchMode> searchFlags;
		boolean correctHeight;
		
		ArrayList<BlockCoords> tanksInSegment;
		ArrayList<BlockCoords> adjacentTanks;
		HashSet<BlockCoords> visitedTanks = new HashSet<BlockCoords>();
		ArrayList<BlockCoords> foundTanks = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> newTanks = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> currentTanks =  new ArrayList<BlockCoords>();
		currentTanks.add(startTank);
		
		do
		{
			for (BlockCoords currentTank : currentTanks)
			{
				tanksInSegment = getTanksInSegment(currentTank);
				searchFlags = (currentTank.y < startTank.y) ? searchAboveAndBelow : searchBelowOnly;
				correctHeight = (currentTank.y == startTank.y);
				
				for (BlockCoords segmentTank : tanksInSegment)
				{
					if (correctHeight)
					{
						foundTanks.add(segmentTank);
					}
					
					adjacentTanks = getAdjacentTanks(segmentTank, searchFlags);
					
					for (BlockCoords adjacentTank : adjacentTanks)
					{
						if (!visitedTanks.contains(adjacentTank))
						{
							newTanks.add(adjacentTank);
						}
					}
					
					visitedTanks.add(segmentTank);
				}
			}
			
			currentTanks.clear();
			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());
		
		timer.stop();
		sameHTime += timer.getTime();
		sameHTimeCalls++;
		
		return foundTanks;
	}
	
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
	
	private ArrayList<BlockCoords> getClosestTanks(Collection<BlockCoords> passableBlocks, Collection<BlockCoords> sources, BlockCoords destination)
	{
		if (tanks == null || tanks.isEmpty() || sources == null || sources.isEmpty() || destination == null)
		{
			return null;
		}
		
		ArrayList<Integer> distances = new ArrayList<Integer>();
		Multimap<Integer, BlockCoords> distanceToTanksMappings = ArrayListMultimap.create();
		int distance;
		
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
	
	private ArrayList<BlockCoords> getAdjacentTanks(BlockCoords block)
	{
		return getOrFindAdjacentTanks(block, null, EnumSet.allOf(BlockSearchMode.class), true);
	}
	
	private ArrayList<BlockCoords> getAdjacentTanks(BlockCoords block, BlockSearchMode mode)
	{
		return getOrFindAdjacentTanks(block, mode, null, true);
	}
	
	private ArrayList<BlockCoords> getAdjacentTanks(BlockCoords block, EnumSet<BlockSearchMode> searchFlags)
	{
		return getOrFindAdjacentTanks(block, null, searchFlags, true);
	}
	
	private ArrayList<BlockCoords> findAdjacentTanks(BlockCoords block)
	{
		return getOrFindAdjacentTanks(block, null, EnumSet.allOf(BlockSearchMode.class), false);
	}
	
	private ArrayList<BlockCoords> findAdjacentTanks(BlockCoords block, BlockSearchMode mode)
	{
		return getOrFindAdjacentTanks(block, mode, null, false);
	}
	
	private ArrayList<BlockCoords> findAdjacentTanks(BlockCoords block, EnumSet<BlockSearchMode> searchFlags)
	{
		return getOrFindAdjacentTanks(block, null, searchFlags, false);
	}
	
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
	
	private boolean hasPriority(BlockCoords tank)
	{
		return tankPriorities.containsValue(tank);
	}
}
