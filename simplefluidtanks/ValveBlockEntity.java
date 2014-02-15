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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.CollectionStore;

import simplefluidtanks.BasicAStar.Node;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
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
	private Multimap<Integer, BlockCoords> tanks;
	private FluidTank internalTank;
	private byte tankFacingSides;
	
	private BasicAStar aStar = new BasicAStar();
	
	public ValveBlockEntity()
	{
		super();
		internalTank = new FluidTank(0);
		tanks = ArrayListMultimap.create();
		tankFacingSides = -1;
	}

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        internalTank.readFromNBT(tag);
        
        try
        {
			tanksFromByteArray(tag.getByteArray("Tanks"));
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
			tag.setByteArray("Tanks", tanksAsByteArray());
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
    	if (!worldObj.isRemote)
    	{
        	int fillAmount = internalTank.fill(drainFluid, doFill);
        	
        	if (doFill && fillAmount > 0)
        	{
        		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
        		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            	// triggers onNeighborTileChange on neighboring blocks, this is needed for comparators to work
        		worldObj.func_96440_m(xCoord, yCoord, zCoord, SimpleFluidTanks.tankBlock.blockID);
        		distributeFluidToTanks();
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
    
    protected FluidStack drain(ForgeDirection from, FluidStack drainFluid, int drainAmount, boolean doDrain)
    {
    	if (!worldObj.isRemote)
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
            	worldObj.func_96440_m(xCoord, yCoord, zCoord, SimpleFluidTanks.tankBlock.blockID);
            }
            
            return drainedFluid;
    	}
    	
    	return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
    	if (!isFacingTank(Direction.fromForge(from)) && fluid != null && !internalTank.isFull())
    	{
        	FluidStack tankFluid = internalTank.getFluid();
        	
        	return (tankFluid == null || tankFluid.fluidID == fluid.getID());
    	}
    	
    	return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
    	return (!isFacingTank(Direction.fromForge(from)) && fluid != null && internalTank.getFluidAmount() > 0 && fluid.getID() == internalTank.getFluid().fluidID);
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
		
		if (isLinkedTank(coords.cloneWithOffset(0, 0, 1)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.ZPOS];
		}
		
		if (isLinkedTank(coords.cloneWithOffset(0, 0, -1)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.ZNEG];
		}
		
		if (isLinkedTank(coords.cloneWithOffset(1, 0, 0)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.XPOS];
		}
		
		if (isLinkedTank(coords.cloneWithOffset(-1, 0, 0)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.XNEG];
		}
		
		if (isLinkedTank(coords.cloneWithOffset(0, 1, 0)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.YPOS];
		}
		
		if (isLinkedTank(coords.cloneWithOffset(0, -1, 0)))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.YNEG];
		}
		
		tankFacingSides = (byte)sides;
	}
	
	public boolean isFacingTank(int side)
	{
		if (side >= Byte.MIN_VALUE && side <= Byte.MAX_VALUE)
		{
			byte flags = (byte)Direction.sidesToBitFlagsMappings[side];
			
			return (tankFacingSides & flags) == flags;
		}
		
		return false;
	}
	
	public boolean hasTanks()
	{
		return tanks.size() > 0;
	}
	
	public void findTanks()
	{
		generateTankList();
		
		ArrayList<TankBlockEntity> tankEntities = new ArrayList<TankBlockEntity>(tanks.size());
		
		// set the valve for all connected tanks
		for (BlockCoords tankCoords : tanks.values())
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
		
		internalTank.setCapacity(SimpleFluidTanks.bucketsPerTank * FluidContainerRegistry.BUCKET_VOLUME * tankEntities.size());
		
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
	}

	public void resetTanks()
	{
		for (BlockCoords tankCoords : tanks.values())
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tankCoords);
			
			if (tankEntity != null)
			{
				tankEntity.reset();
			}
		}
		
		tanks.clear();
		tankFacingSides = 0;
		internalTank.setCapacity(0);
		internalTank.setFluid(null);
		
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
			objStream.writeObject(tanks);
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
			tanks = (ArrayListMultimap<Integer, BlockCoords>)objStream.readObject();
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
			
			for (BlockCoords tankCoords : tanks.values())
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
			int[] priorities = Ints.toArray(tanks.keySet());
			Arrays.sort(priorities);
			
			Collection<BlockCoords> tanksToFill = null;
			
			for (int i = 0; i < priorities.length; i++)
			{
				tanksToFill = tanks.get(priorities[i]);
				
				int capacity = tanksToFill.size() * SimpleFluidTanks.bucketsPerTank * 1000;
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
		BlockCoords startCoords = new BlockCoords(xCoord, yCoord, zCoord);
		ArrayList<BlockCoords> startingTanks = getAdjacentTanks(startCoords);
		
		// TODO: generate tank list
		
		for (BlockCoords startingTank : startingTanks)
		{
			
		}
	}
	
	private Collection<BlockCoords> getHighestPriorityTanks(BlockCoords startTank)
	{
		HashSet<BlockCoords> visitedTanks = new HashSet<BlockCoords>();
		ArrayList<BlockCoords> tanksInSegment;
		ArrayList<BlockCoords> adjacentTanks;
		ArrayList<BlockCoords> highPrioTanks = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> tanksBelow = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> tanksWithTanksBelow = new ArrayList<BlockCoords>();
		ArrayList<BlockCoords> newTanks = new ArrayList<BlockCoords>();
		newTanks.add(startTank);
		
		do
		{
			for (BlockCoords tank : newTanks)
			{
				if (visitedTanks.contains(tank))
				{
					continue;
				}
				
				tanksInSegment = getTanksInSegment(tank);
				
				for (BlockCoords segmentTank : tanksInSegment)
				{
					adjacentTanks = getAdjacentTanks(segmentTank, BlockSearchMode.Below);
					
					if (!adjacentTanks.isEmpty())
					{
						tanksWithTanksBelow.add(segmentTank);
					}
				}
				
				if (!tanksWithTanksBelow.isEmpty())
				{
					for (BlockCoords closeTank : getClosestTanks(tanksInSegment, tanksWithTanksBelow, tank))
					{
						tanksBelow.add(closeTank.cloneWithOffset(0, -1));
					}
					
					tanksWithTanksBelow.clear();
				}
				else
				{
					highPrioTanks.add(tank);
				}
				
				visitedTanks.addAll(tanksInSegment);
			}
			
			newTanks.addAll(tanksBelow);
			tanksBelow.clear();
		}
		while (!newTanks.isEmpty());
		
		return highPrioTanks;
	}
	
	private Collection<BlockCoords> getClosestTanks(Collection<BlockCoords> passableBlocks, Collection<BlockCoords> tanks, BlockCoords from)
	{
		if (passableBlocks == null || passableBlocks.isEmpty() || tanks == null || tanks.isEmpty() || from == null)
		{
			return new ArrayList<BlockCoords>();
		}
		
		ArrayList<Integer> distances = new ArrayList<Integer>();
		Multimap<Integer, BlockCoords> distanceToTanksMappings = ArrayListMultimap.create();
		int distance;
		
		aStar.setPassableBlocks(passableBlocks);
		
		for (BlockCoords tank : tanks)
		{
			if (from.equals(tank))
			{
				distance = 0;
			}
			else
			{
				distance = aStar.getShortestPath(from, tank).currentCost;
			}
			
			distances.add(distance);
			distanceToTanksMappings.put(distance, tank);
		}
		
		Collections.sort(distances);
		
		return distanceToTanksMappings.get(distances.get(0));
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
		ArrayList<BlockCoords> adjacentTanks;
		
		lastFoundTanks.add(firstTank);
		
		do
		{
			for (BlockCoords tank : lastFoundTanks)
			{
				adjacentTanks = getAdjacentTanks(tank, BlockSearchMode.SameLevel);
				
				if (adjacentTanks != null)
				{
					for (BlockCoords adjacentTank : adjacentTanks)
					{
						if (tanksInSegment.add(adjacentTank))
						{
							newFoundTanks.add(adjacentTank);
						}
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
		return getAdjacentTanks(block, null, EnumSet.allOf(BlockSearchMode.class));
	}
	
	private ArrayList<BlockCoords> getAdjacentTanks(BlockCoords block, BlockSearchMode mode)
	{
		return getAdjacentTanks(block, mode, null);
	}
	
	private ArrayList<BlockCoords> getAdjacentTanks(BlockCoords block, BlockSearchMode mode, EnumSet<BlockSearchMode> searchFlags)
	{
		if (block == null || (mode == null && searchFlags == null))
		{
			return null;
		}
		
		ArrayList<BlockCoords> tanks = new ArrayList<BlockCoords>();
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
		
		for (BlockCoords adjacentBlock : adjacentBlocks)
		{
			if (isUnlinkedTank(adjacentBlock))
			{
				tanks.add(adjacentBlock);
			}
		}
		
		return tanks;
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
		
		return false;
	}
	
	private boolean isLinkedTank(BlockCoords block)
	{
		if (block == null)
		{
			return false;
		}
		
		return tanks.containsValue(block);
	}
}
