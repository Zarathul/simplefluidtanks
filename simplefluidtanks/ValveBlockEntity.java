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

import org.bouncycastle.util.CollectionStore;

import simplefluidtanks.BasicAStar.Node;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
	private HashSet<BlockCoords> tanks;
	private ArrayList<FluidFlowNode> fluidFlowBranches;
	
	private FluidTank internalTank;
	private byte tankFacingSides;
	
	private BasicAStar aStar = new BasicAStar();
	
	public ValveBlockEntity()
	{
		super();
		internalTank = new FluidTank(0);
		tanks = new HashSet<BlockCoords>();
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
		for (BlockCoords tankCoords : tanks)
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
		for (BlockCoords tankCoords : tanks)
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
			tanks = (HashSet<BlockCoords>)objStream.readObject();
		}
		finally
		{
			objStream.close();
		}
	}
	
	private void distributeFluidToTanks()
	{
		int amountToDistribute = internalTank.getFluidAmount();
		
		if (amountToDistribute == 0 || amountToDistribute == internalTank.getCapacity())
		{
			int percentage = (amountToDistribute == 0) ? 0 : 100;
			
			for (BlockCoords tankCoords : tanks)
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
			ArrayList<FluidFlowNode> currentSegments = new ArrayList<FluidFlowNode>();
			ArrayList<BlockCoords> tanksToFill = new ArrayList<BlockCoords>();
			
			int amountPerBranch = amountToDistribute / fluidFlowBranches.size();
			int amountLeft;
			int group;
			
			// TODO : gogo fix it future me
			for (FluidFlowNode node : fluidFlowBranches)
			{
				amountLeft = amountPerBranch;
				node.getFirstDeadEnd();
			}
		}
		
		/*
		int segmentCapacity = tanksToFill.size() * SimpleFluidTanks.bucketsPerTank * 1000;
		int fillPercentage = (amountPerBranch >= segmentCapacity) ? 100 : (int)(Math.min((double)amountPerBranch / (double)segmentCapacity * 100d, 100d));
		
		// update the tanks
		for (BlockCoords tank : tanksToFill)
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tank);
			
			if (tankEntity != null)
			{
				tankEntity.setFillPercentage(fillPercentage);
			}
		}
		*/
	}
	
	private void generateTankList()
	{
		BlockCoords startCoords = new BlockCoords(xCoord, yCoord, zCoord);
		FluidFlowNode fluidFlowRoot = new FluidFlowNode();
		
		ArrayList<BlockCoords> startingTanks = getAdjacentTanks(startCoords);
		ArrayList<FluidFlowNode> fluidFlowBranches = new ArrayList<FluidFlowNode>(startingTanks.size());
		
		FluidFlowNode currentBranch;
		
		for (BlockCoords startingTank : startingTanks)
		{
			currentBranch = getFluidFlowBranch(startingTank);
			
			if (currentBranch != null)
			{
				fluidFlowBranches.add(currentBranch);
			}
		}
	}
	
	private FluidFlowNode getFluidFlowBranch(BlockCoords startTank)
	{
		if (startTank == null)
		{
			return null;
		}
		
		FluidFlowNode branchRoot = new FluidFlowNode();
		branchRoot.tanks = getTanksInSegment(startTank);
		
		tanks.addAll(branchRoot.tanks);
		
		Stack<FluidFlowNode> segmentsBelow = new Stack<FluidFlowNode>();
		segmentsBelow.push(branchRoot);
		
		Stack<FluidFlowNode> segmentsAbove = new Stack<FluidFlowNode>();
		
		FluidFlowNode currentSegment;
		
		while(!segmentsBelow.isEmpty() || !segmentsAbove.isEmpty())
		{
			// Get the next segment. Segments that where found below others are handled first.
			currentSegment = (!segmentsBelow.isEmpty()) ? segmentsBelow.pop() : segmentsAbove.pop();
			
			// get segments below
			Collection<FluidFlowNode> newSegments = getSegmentsBelow(currentSegment);
			
			for (FluidFlowNode node : newSegments)
			{
				segmentsBelow.push(node);
			}
			
			// get segments above
			newSegments = getSegmentsAbove(currentSegment);
			
			for (FluidFlowNode node : newSegments)
			{
				segmentsAbove.push(node);
			}
		}
		
		return branchRoot;
	}
	
	private Collection<FluidFlowNode> getSegmentsAbove(FluidFlowNode startSegment)
	{
		return getSegments(startSegment, true);
	}

	private Collection<FluidFlowNode> getSegmentsBelow(FluidFlowNode startSegment)
	{
		return getSegments(startSegment, false);
	}
	
	private Collection<FluidFlowNode> getSegments(FluidFlowNode startSegment, boolean above)
	{
		ArrayList<FluidFlowNode> newSegments = new ArrayList<FluidFlowNode>();
		ArrayList<BlockCoords> newTanks = new ArrayList<BlockCoords>();
		BlockSearchMode mode = (above) ? BlockSearchMode.Above : BlockSearchMode.Below;
		
		for (BlockCoords tank : startSegment.tanks)
		{
			 newTanks.addAll(getAdjacentTanks(tank, mode));
		}
		
		int group = 0;
		
		for (BlockCoords tank : newTanks)
		{
			if (tanks.contains(tank))
			{
				continue;
			}
			
			ArrayList<BlockCoords> newSegmentTanks = getTanksInSegment(tank);
			tanks.addAll(newSegmentTanks);
			FluidFlowNode newSegment;
			
			if (above)
			{
				newSegment = startSegment.addSegmentAbove(newSegmentTanks);
			}
			else
			{
				aStar.setPassableBlocks(startSegment.tanks);
				BlockCoords firstTank = startSegment.tanks.get(0);
				
				// group = shortest distance from the current segments first tank to any tank of the new segment that's directly connected to it
				for (BlockCoords newTank : newSegmentTanks)
				{
					BlockCoords aboveNewTank = newTank.cloneWithOffset(0, 1);
					
					if (tanks.contains(aboveNewTank))
					{
						if (firstTank.equals(aboveNewTank))
						{
							group = 0;
							break;
						}
						else
						{
							Node path = aStar.getShortestPath(firstTank, aboveNewTank);
							group = Math.min(group, path.currentCost);
//							LogWrapper.info("%s -> %s", firstTank, aboveNewTank);
//							LogWrapper.info("cost: %d", path.currentCost);
						}
					}
				}
				
				newSegment = startSegment.addSegmentBelow(group, newSegmentTanks);
			}
			
			newSegments.add(newSegment);
		}
		
		return newSegments;
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
		
		return tanks.contains(block);
	}
}
