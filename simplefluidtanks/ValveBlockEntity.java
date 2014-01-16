package simplefluidtanks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
	protected FluidTank tank;
	private ArrayListMultimap<Float, int[]> tanks;
	private byte inputSides;
	
	public ValveBlockEntity()
	{
		super();
		tank = new FluidTank(0);
		tanks = ArrayListMultimap.create();
		inputSides = -1;
	}

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        tank.readFromNBT(tag);
        
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
        
        inputSides = tag.getByte("InputSides");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tank.writeToNBT(tag);
        
        try
        {
			tag.setByteArray("Tanks", tanksAsByteArray());
		}
        catch (IOException e)
        {
			e.printStackTrace();
		}
        
        tag.setByte("InputSides", inputSides);
    }

    @Override
    public int fill(ForgeDirection from, FluidStack drainFluid, boolean doFill)
    {
    	if (!worldObj.isRemote)
    	{
        	int fillAmount = tank.fill(drainFluid, doFill);
        	
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
            FluidStack drainedFluid = (drainFluid != null && drainFluid.isFluidEqual(tank.getFluid())) ? tank.drain(drainFluid.amount, doDrain) :
            						  (drainAmount >= 0) ? tank.drain(drainAmount, doDrain) :
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
    	if (fluid != null && tank.getRemainingCapacity() > 0)
    	{
        	FluidStack tankFluid = tank.getFluid();
        	
        	return (tankFluid == null || tankFluid.fluidID == fluid.getID());
    	}
    	
    	return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
    	return (fluid != null && tank.getFluidAmount() > 0 && fluid.getID() == tank.getFluid().fluidID);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[] { tank.getInfo() };
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
		return tank.getCapacity();
	}
	
	public int getFluidAmount()
	{
		return tank.getFluidAmount();
	}
	
	public FluidStack getFluid()
	{
		return tank.getFluid();
	}
	
	public int getInputSides()
	{
		return inputSides;
	}
	
	public void updateInputSides()
	{
		int sides = 0;
		
		if (isLinkedTank(worldObj, xCoord, yCoord, zCoord + 1))
		{
			sides = sides | ConnectedTexturesHelper.sidesToBitFlagsMappings[ConnectedTexturesHelper.ZPOS];
		}
		
		if (isLinkedTank(worldObj, xCoord, yCoord, zCoord - 1))
		{
			sides = sides | ConnectedTexturesHelper.sidesToBitFlagsMappings[ConnectedTexturesHelper.ZNEG];
		}
		
		if (isLinkedTank(worldObj, xCoord + 1, yCoord, zCoord))
		{
			sides = sides | ConnectedTexturesHelper.sidesToBitFlagsMappings[ConnectedTexturesHelper.XPOS];
		}
		
		if (isLinkedTank(worldObj, xCoord - 1, yCoord, zCoord))
		{
			sides = sides | ConnectedTexturesHelper.sidesToBitFlagsMappings[ConnectedTexturesHelper.XNEG];
		}
		
		if (isLinkedTank(worldObj, xCoord, yCoord + 1, zCoord))
		{
			sides = sides | ConnectedTexturesHelper.sidesToBitFlagsMappings[ConnectedTexturesHelper.YPOS];
		}
		
		if (isLinkedTank(worldObj, xCoord, yCoord - 1, zCoord))
		{
			sides = sides | ConnectedTexturesHelper.sidesToBitFlagsMappings[ConnectedTexturesHelper.YNEG];
		}
		
		inputSides = (byte)sides;
	}
	
	public boolean isInputSide(int side)
	{
		if (side >= Byte.MIN_VALUE && side <= Byte.MAX_VALUE)
		{
			byte flags = (byte)ConnectedTexturesHelper.sidesToBitFlagsMappings[side];
			
			return (inputSides & flags) == flags;
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
		
		for (Map.Entry<Float, int[]> entry : tanks.entries())
		{
			int[] coords = entry.getValue();
			TankBlockEntity tankEntity = (TankBlockEntity)worldObj.getBlockTileEntity(coords[0], coords[1], coords[2]);
			tankEntity.setValve(xCoord, yCoord, zCoord);
			tankEntities.add(tankEntity);
		}
		
		for (TankBlockEntity t : tankEntities)
		{
			t.updateTextures();
		}
		
		tank.setCapacity(SimpleFluidTanks.bucketsPerTank * FluidContainerRegistry.BUCKET_VOLUME * tankEntities.size());
		
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
		tank.setCapacity(0);
		tank.setFluid(null);
		
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
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
		int amountToDistribute = tank.getFluidAmount();
		
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
		// fill priority starts at 0 at the same height as the valve, 
		// going up one block decreases priority by the worlds maximum height (256 at the moment), 
		// going down one block increases priority by a value between 0 and 1 depending on the distance between the block and the valve on the x or z axis, whichever is longer
		
		ArrayListMultimap<Float, int[]> foundTanks = floodFindTanks(world, x, y - 1, z, tanks, 1);		// Y+
		foundTanks.putAll(floodFindTanks(worldObj, x + 1, y, z, tanks, 0));								// X+
		foundTanks.putAll(floodFindTanks(worldObj, x - 1, y, z, tanks, 0));								// X-
		foundTanks.putAll(floodFindTanks(worldObj, x, y, z + 1, tanks, 0));								// Z+
		foundTanks.putAll(floodFindTanks(worldObj, x, y, z - 1, tanks, 0));								// Z-
		foundTanks.putAll(floodFindTanks(worldObj, x, y + 1, z, tanks, -world.getActualHeight()));		// Y+
		
		do
		{
			ArrayListMultimap<Float, int[]> newTanks = ArrayListMultimap.create();
			
			for (Map.Entry<Float, int[]> entry : foundTanks.entries())
			{
				float priority = entry.getKey();
				int[] coords = entry.getValue();
				float priorityYPos = priority - ((coords[1] + 1 <= y) ? 1 : world.getActualHeight());
				float priorityYNeg = priority + 1 - normalizePriorityOffset((Math.max(Math.abs(x - coords[0]), Math.abs(z - coords[2]))));
				
				newTanks.putAll(floodFindTanks(world, coords[0], coords[1] + 1, coords[2], tanks, priorityYPos));		// Y+
				newTanks.putAll(floodFindTanks(world, coords[0], coords[1] - 1, coords[2], tanks, priorityYNeg));		// Y-
			}
			
			foundTanks = newTanks;
		}
		while (foundTanks.size() > 0);
	}
	
	private ArrayListMultimap<Float, int[]> floodFindTanks(World world, int x, int y, int z, ArrayListMultimap<Float, int[]> tanks, float priority)
	{
		if (!isUnlinkedTank(world, x, y, z))
		{
			return ArrayListMultimap.create();
		}
		
		int[] coords = new int[] { x, y, z };
		
		// check if the tank at the current location has already been found (note: ArrayListMultimap.containsValue() seems to check only reference equality)
		for (int[] alreadyFoundCoords : tanks.values())
		{
			if (Arrays.equals(alreadyFoundCoords, coords))
			{
				return ArrayListMultimap.create();
			}
		}
		
		tanks.put(priority, coords);
		
		ArrayListMultimap<Float, int[]> newTanks = ArrayListMultimap.create();
		newTanks.put(priority, coords);
		 
		newTanks.putAll(floodFindTanks(world, x + 1, y, z, tanks, priority));
		newTanks.putAll(floodFindTanks(world, x - 1, y, z, tanks, priority));
		newTanks.putAll(floodFindTanks(world, x, y, z + 1, tanks, priority));
		newTanks.putAll(floodFindTanks(world, x, y, z - 1, tanks, priority));
		
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
