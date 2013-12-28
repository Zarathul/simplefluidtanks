package simplefluidtanks;

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
	
	public ValveBlockEntity()
	{
		super();
		this.tank = new FluidTank(SimpleFluidTanks.bucketsPerTank * FluidContainerRegistry.BUCKET_VOLUME);
	}

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        tank.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tank.writeToNBT(tag);
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
}
