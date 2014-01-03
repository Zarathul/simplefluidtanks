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
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
    	int fillAmount = tank.fill(resource, doFill);
    	
    	if (fillAmount > 0 && doFill)
    	{
    		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    		distributeFluidToTanks();
    	}
    	
        return fillAmount;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        if (resource == null || !resource.isFluidEqual(tank.getFluid()))
        {
            return null;
        }
        
        FluidStack drainedFluid = tank.drain(resource.amount, doDrain);
        
        if (drainedFluid.amount > 0 && doDrain)
        {
        	worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        	distributeFluidToTanks();
        }
        
        return drainedFluid;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
    	FluidStack drainedFluid = tank.drain(maxDrain, doDrain);
        
    	if (drainedFluid.amount > 0 && doDrain)
        {
        	worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        	distributeFluidToTanks();
        }
    	
    	return drainedFluid;
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
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
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
	
	public ArrayListMultimap<Integer, int[]> getTanks()
	{
		return tanks;
	}
	
	public void setTanks(ArrayListMultimap<Integer, int[]> t)
	{
		tanks = t;
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
		int buckets = tank.getFluidAmount();
		
		if (buckets > 0)
		{
			int[] priorities = Ints.toArray(tanks.keySet());
			Arrays.sort(priorities);
			ArrayUtils.reverse(priorities);
			
			List<int[]> tanksToFill = null;
			
			for (int i = 0; i < priorities.length; i++)
			{
				tanksToFill = tanks.get(priorities[i]);
				
				int capacity = tanksToFill.size() * SimpleFluidTanks.bucketsPerTank * 1000;
				int fillPercentage = (int)((float)buckets / capacity * 100f);
				
				for (int[] tankCoords : tanksToFill)
				{
					TankBlockEntity tankEntity = (TankBlockEntity)worldObj.getBlockTileEntity(tankCoords[0], tankCoords[1], tankCoords[2]);
					
					if (tankEntity != null)
					{
						tankEntity.setFillPercentage(fillPercentage);
					}
				}
				
				buckets -= capacity;
				
				if (buckets <= 0)
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
		
		System.out.println();
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
		
		System.out.printf("Prio: %d - %d/%d/%d", priority, coords[0], coords[1], coords[2]);
		System.out.println();
		
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
		if (world.getBlockId(x, y, z) == SimpleFluidTanks.tankBlockId)
		{
			TankBlockEntity tankEntity = (TankBlockEntity)world.getBlockTileEntity(x, y, z);
			
			return !tankEntity.isPartOfTank();
		}
		
		return false;
	}
}
