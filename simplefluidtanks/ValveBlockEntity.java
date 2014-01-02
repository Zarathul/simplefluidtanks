package simplefluidtanks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

import com.google.common.collect.ArrayListMultimap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class ValveBlockEntity extends TileEntity implements IFluidHandler
{
	protected FluidTank tank;
	private ArrayListMultimap<Integer, int[]> tanks;
	
	public ValveBlockEntity()
	{
		super();
		this.tank = new FluidTank(SimpleFluidTanks.bucketsPerTank * FluidContainerRegistry.BUCKET_VOLUME);
		this.tanks = ArrayListMultimap.create();
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
    		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
        	this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
        
        return drainedFluid;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
    	FluidStack drainedFluid = tank.drain(maxDrain, doDrain);
        
    	if (drainedFluid.amount > 0 && doDrain)
        {
        	this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
		
	}
}
