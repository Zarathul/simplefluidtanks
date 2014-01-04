package simplefluidtanks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import scala.tools.nsc.doc.base.comment.OrderedList;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.primitives.Ints;

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

public class ValveBlockEntity extends TileEntity implements IFluidHandler
{
	protected FluidTank tank;
	private ArrayListMultimap<Integer, int[]> tanks;
	
	public ValveBlockEntity()
	{
		super();
		tank = new FluidTank(0);
		tanks = ArrayListMultimap.create();
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
        
        String tagcontent = (tag != null) ? tag.toString() : "null";
        String side = (worldObj != null) ? (!worldObj.isRemote) ? "true" : "false" : "world not loaded";
        System.out.println("ValveBlockEntity NBTread: " + tagcontent + ". (Server: " + side + ")");
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
        
        String tagcontent = (tag != null) ? tag.toString() : "null";
        String side = (worldObj != null) ? (!worldObj.isRemote) ? "true" : "false" : "world not loaded";
        System.out.println("ValveBlockEntity NBTwrite: " + tagcontent + ". (Server: " + side + ")");
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
            FluidStack drainedFluid = (drainFluid != null) ? tank.drain(drainFluid.amount, doDrain) :
            						  (drainAmount >= 0) ? tank.drain(drainAmount, doDrain) :
            						  null;
            
            if (doDrain && drainedFluid != null && drainedFluid.amount > 0)
            {
        		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
            	worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            	distributeFluidToTanks();
            }
            
            return drainedFluid;
    	}
    	
    	return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[] { tank.getInfo() };
    }
	
	@Override
	public Packet getDescriptionPacket()
	{
		System.out.println("ValveBlockEntity desc packet requested. (Server: " + !worldObj.isRemote + ")");
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, -1, tag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		System.out.println("ValveBlockEntity packet132 received. (Server: " + !worldObj.isRemote + ")");
		readFromNBT(packet.data);
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
	
	public void findTanks()
	{
		generateTankList(worldObj, xCoord, yCoord, zCoord, tanks);
		
		ArrayList<TankBlockEntity> tankEntities = new ArrayList<TankBlockEntity>(tanks.size());
		
		for (Map.Entry<Integer, int[]> entry : tanks.entries())
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
		for (Map.Entry<Integer, int[]> entry : tanks.entries())
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
			tanks = (ArrayListMultimap<Integer, int[]>)objStream.readObject();
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
			int[] priorities = Ints.toArray(tanks.keySet());
			Arrays.sort(priorities);
			ArrayUtils.reverse(priorities);
			
			List<int[]> tanksToFill = null;
			
			for (int i = 0; i < priorities.length; i++)
			{
				tanksToFill = tanks.get(priorities[i]);
				
				int capacity = tanksToFill.size() * SimpleFluidTanks.bucketsPerTank * 1000;
				int fillPercentage = (int)((float)amountToDistribute / capacity * 100f);
				
				for (int[] tankCoords : tanksToFill)
				{
					TankBlockEntity tankEntity = (TankBlockEntity)worldObj.getBlockTileEntity(tankCoords[0], tankCoords[1], tankCoords[2]);
					
					if (tankEntity != null)
					{
						tankEntity.setFillPercentage(fillPercentage);
					}
				}
				
				amountToDistribute -= capacity;
				
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
	
	private void generateTankList(World world, int x, int y, int z, ArrayListMultimap<Integer, int[]> tanks)
	{
		int xOffset = 0;
		int yOffset = 0;
		int zOffset = 0;
		
		// find a tank block directly connected to the valve 
		
		if (isValidTank(world, x, y, z + 1))
		{
			zOffset = 1;
		}
		else if (isValidTank(world, x, y, z - 1))
		{
			zOffset = -1;
		}
		else if (isValidTank(world, x + 1, y, z))
		{
			xOffset = 1;
		}
		else if (isValidTank(world, x - 1, y, z))
		{
			xOffset = -1;
		}
		else if (isValidTank(world, x, y + 1, z))
		{
			yOffset = 1;
		}
		else if (isValidTank(world, x, y - 1, z))
		{
			yOffset = -1;
		}
		else
		{
			return;
		}
		
		// find all other tank blocks connected to the first one - note that the flood find algo will not search vertically because it would mess up priority calculations
		ArrayListMultimap<Integer, int[]> foundTanks = floodFindTanks(world, x + xOffset, y + yOffset, z + zOffset, tanks, world.getActualHeight() - 1);
		
		do
		{
			ArrayListMultimap<Integer, int[]> newTanks = ArrayListMultimap.create();
			
			for (Map.Entry<Integer, int[]> entry : foundTanks.entries())
			{
				int priority = entry.getKey();
				int[] coords = entry.getValue();
				
				newTanks.putAll(floodFindTanks(world, coords[0], coords[1] - 1, coords[2], tanks, priority + 1));
				newTanks.putAll(floodFindTanks(world, coords[0], coords[1] + 1, coords[2], tanks, priority - world.getActualHeight()));
			}
			
			foundTanks = newTanks;
		}
		while (foundTanks.size() > 0);
	}
	
	private ArrayListMultimap<Integer, int[]> floodFindTanks(World world, int x, int y, int z, ArrayListMultimap<Integer, int[]> tanks, int priority)
	{
		if (!isValidTank(world, x, y, z))
		{
			return ArrayListMultimap.create();
		}
		
		int[] coords = new int[] { x, y, z };
		
		// containsValue() seems to check only reference equality, so we have to resort to this or add a superfluous value class
		for (int[] alreadyFoundCoords : tanks.values())
		{
			if (Arrays.equals(alreadyFoundCoords, coords))
			{
				return ArrayListMultimap.create();
			}
		}
		
		tanks.put(priority, coords);
		
		ArrayListMultimap<Integer, int[]> newTanks = ArrayListMultimap.create();
		newTanks.put(priority, coords);
		 
		newTanks.putAll(floodFindTanks(world, x + 1, y, z, tanks, priority));
		newTanks.putAll(floodFindTanks(world, x - 1, y, z, tanks, priority));
		newTanks.putAll(floodFindTanks(world, x, y, z + 1, tanks, priority));
		newTanks.putAll(floodFindTanks(world, x, y, z - 1, tanks, priority));
		
		return newTanks;
	}
	
	private boolean isValidTank(World world, int x, int y, int z)
	{
		if (world.getBlockId(x, y, z) == SimpleFluidTanks.tankBlock.blockID)
		{
			TankBlockEntity tankEntity = (TankBlockEntity)world.getBlockTileEntity(x, y, z);
			
			return !tankEntity.isPartOfTank();
		}
		
		return false;
	}
}
