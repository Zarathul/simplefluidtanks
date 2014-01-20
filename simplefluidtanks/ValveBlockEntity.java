package simplefluidtanks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.primitives.Floats;

public class ValveBlockEntity extends TileEntity implements IFluidHandler
{
	protected FluidTank internalTank;
	// TODO: make tanks field private again
	public ArrayListMultimap<Float, int[]> tanks;
	private byte tankFacingSides;
	
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
    	if (!isFacingTank(Direction.fromForge(from)) && fluid != null && internalTank.getRemainingCapacity() > 0)
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
		
		if (isLinkedTank(worldObj, xCoord, yCoord, zCoord + 1))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.ZPOS];
		}
		
		if (isLinkedTank(worldObj, xCoord, yCoord, zCoord - 1))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.ZNEG];
		}
		
		if (isLinkedTank(worldObj, xCoord + 1, yCoord, zCoord))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.XPOS];
		}
		
		if (isLinkedTank(worldObj, xCoord - 1, yCoord, zCoord))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.XNEG];
		}
		
		if (isLinkedTank(worldObj, xCoord, yCoord + 1, zCoord))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.YPOS];
		}
		
		if (isLinkedTank(worldObj, xCoord, yCoord - 1, zCoord))
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
		generateTankList(worldObj, xCoord, yCoord, zCoord, tanks);
		
		ArrayList<TankBlockEntity> tankEntities = new ArrayList<TankBlockEntity>(tanks.size());
		
		// set the valve for all connected tanks
		for (Map.Entry<Float, int[]> entry : tanks.entries())
		{
			int[] coords = entry.getValue();
			TankBlockEntity tankEntity = (TankBlockEntity)worldObj.getBlockTileEntity(coords[0], coords[1], coords[2]);
			tankEntity.setValve(xCoord, yCoord, zCoord);
			tankEntities.add(tankEntity);
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
		for (Map.Entry<Float, int[]> entry : tanks.entries())
		{
			int[] coords = entry.getValue();
			TankBlockEntity tankEntity = (TankBlockEntity)worldObj.getBlockTileEntity(coords[0], coords[1], coords[2]);
			tankEntity.reset();
		}
		
		tanks.clear();
		internalTank.setCapacity(0);
		internalTank.setFluid(null);
		
		updateTankFacingSides();
		
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
			tanks = (ArrayListMultimap<Float, int[]>)objStream.readObject();
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
		
		if (amountToDistribute > 0)
		{
			float[] priorities = Floats.toArray(tanks.keySet());
			Arrays.sort(priorities);
			ArrayUtils.reverse(priorities);
			
			List<int[]> tanksToFill = null;
			
			for (int i = 0; i < priorities.length; i++)
			{
				tanksToFill = tanks.get(priorities[i]);
				
				int capacity = tanksToFill.size() * SimpleFluidTanks.bucketsPerTank * 1000;
				double fillPercentage = Math.min((double)amountToDistribute / (double)capacity * 100d, 100d);
				
				for (int[] tankCoords : tanksToFill)
				{
					TankBlockEntity tankEntity = (TankBlockEntity)worldObj.getBlockTileEntity(tankCoords[0], tankCoords[1], tankCoords[2]);
					
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
		else
		{
			for (int[] tankCoords : tanks.values())
			{
				TankBlockEntity tankEntity = (TankBlockEntity)worldObj.getBlockTileEntity(tankCoords[0], tankCoords[1], tankCoords[2]);
				
				if (tankEntity != null)
				{
					tankEntity.setFillPercentage(0);
				}
			}
		}
	}
	
	private void generateTankList(World world, int x, int y, int z, ArrayListMultimap<Float, int[]> tanks)
	{
		/*		
		int[][] startCoords = new int[][]
		{
			{ x    , y - 1, z     },	// Y-
			{ x + 1, y    , z     },	// X+
			{ x - 1, y    , z     },	// X-
			{ x    , y    , z + 1 },	// Z+
			{ x    , y    , z - 1 },	// Z-
			{ x    , y + 1, z     }		// Y+
		};
		
		ArrayList<FluidDistributionInfo> test = new ArrayList<FluidDistributionInfo>();
		int[] currentCoords;
		
		for (int i = 0; i <= 6; i++)
		{
			currentCoords = startCoords[i];
			
			while (isUnlinkedTank(world, currentCoords[0], currentCoords[1], currentCoords[2]))
			{
				
				currentCoords[1] -= 1;
			}
		}
		*/
		// fill priority starts at 0 at the same height as the valve, 
		// going up one block decreases priority by the worlds maximum height (256 at the moment), 
		// going down one block increases priority by a value between 0 and 1 depending on the distance between the block and the valve on the x or z axis, whichever is longer
		
		HashMap<int[], Integer> distancesToValve = new HashMap<int[], Integer>();
		ArrayListMultimap<Float, int[]> foundTanks = floodFindTanks(world, x, y - 1, z, tanks, 1, distancesToValve, 1);			// Y-
		foundTanks.putAll(floodFindTanks(worldObj, x + 1, y, z, tanks, 0, distancesToValve, 1));								// X+
		foundTanks.putAll(floodFindTanks(worldObj, x - 1, y, z, tanks, 0, distancesToValve, 1));								// X-
		foundTanks.putAll(floodFindTanks(worldObj, x, y, z + 1, tanks, 0, distancesToValve, 1));								// Z+
		foundTanks.putAll(floodFindTanks(worldObj, x, y, z - 1, tanks, 0, distancesToValve, 1));								// Z-
		foundTanks.putAll(floodFindTanks(worldObj, x, y + 1, z, tanks, -world.getActualHeight(), distancesToValve, 1));			// Y+
		
		do
		{
			ArrayListMultimap<Float, int[]> newTanks = ArrayListMultimap.create();
			
			for (Map.Entry<Float, int[]> entry : foundTanks.entries())
			{
				float priority = entry.getKey();
				int[] coords = entry.getValue();
				int distance = distancesToValve.get(coords);
				float priorityYNeg = priority + 1 - normalizePriorityOffset(distance);
				
				newTanks.putAll(floodFindTanks(world, coords[0], coords[1] - 1, coords[2], tanks, priorityYNeg, distancesToValve, distance + 1));		// Y-
			}
			
			for (Map.Entry<Float, int[]> entry : foundTanks.entries())
			{
				float priority = entry.getKey();
				int[] coords = entry.getValue();
				int distance = distancesToValve.get(coords);
				float priorityYPos = priority - world.getActualHeight();
				
				newTanks.putAll(floodFindTanks(world, coords[0], coords[1] + 1, coords[2], tanks, priorityYPos, distancesToValve, distance + 1));		// Y+
			}
			
			foundTanks = newTanks;
		}
		while (foundTanks.size() > 0);
	}
	
	private ArrayListMultimap<Float, int[]> floodFindTanks(World world, int x, int y, int z, ArrayListMultimap<Float, int[]> tanks, float priority, HashMap<int[], Integer> distances, int distance)
	{
		if (!isUnlinkedTank(world, x, y, z))
		{
			return ArrayListMultimap.create();
		}
		
		int[] coords = new int[] { x, y, z };
		
		// check if the tank at the current location has already been found (note: ArrayListMultimap.containsValue() seems to check only reference equality)
		for (Map.Entry<Float, int[]> alreadyFoundTank : tanks.entries())
		{
			if (Arrays.equals(alreadyFoundTank.getValue(), coords))
			{
				System.out.println(String.format("%d/%d/%d : %f - %f", x, y, z, priority, alreadyFoundTank.getKey()));
				return ArrayListMultimap.create();
			}
		}
		
//		for (int[] alreadyFoundCoords : tanks.values())
//		{
//			if (Arrays.equals(alreadyFoundCoords, coords))
//			{
//				return ArrayListMultimap.create();
//			}
//		}
		
		tanks.put(priority, coords);
		distances.put(coords, distance);
		
		int newDistance = distance + 1;
		ArrayListMultimap<Float, int[]> newTanks = ArrayListMultimap.create();
		newTanks.put(priority, coords);
		
		newTanks.putAll(floodFindTanks(world, x + 1, y, z, tanks, priority, distances, newDistance));
		newTanks.putAll(floodFindTanks(world, x - 1, y, z, tanks, priority, distances, newDistance));
		newTanks.putAll(floodFindTanks(world, x, y, z + 1, tanks, priority, distances, newDistance));
		newTanks.putAll(floodFindTanks(world, x, y, z - 1, tanks, priority, distances, newDistance));
		
		return newTanks;
	}
	
	private boolean isUnlinkedTank(World world, int x, int y, int z)
	{
		if (world.getBlockId(x, y, z) == SimpleFluidTanks.tankBlock.blockID)
		{
			TankBlockEntity tankEntity = (TankBlockEntity)world.getBlockTileEntity(x, y, z);
			
			return !tankEntity.isPartOfTank();
		}
		
		return false;
	}
	
	private boolean isLinkedTank(World world, int x, int y, int z)
	{
		int[] coords = new int[] { x, y, z };
		
		for (int[] linkedTankCoords : tanks.values())
		{
			if (ArrayUtils.isEquals(coords, linkedTankCoords))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private float normalizePriorityOffset(float value)
	{
		float normalized = Math.abs(value);
		
		while (normalized >= 1)
		{
			normalized /= 10;
		}
		
		return normalized;
	}
}
