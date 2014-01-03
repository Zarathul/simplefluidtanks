
package simplefluidtanks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

public class FluidTank implements IFluidTank
{
    protected FluidStack fluid;
    protected int capacity;

    public FluidTank(int capacity)
    {
        this(null, capacity);
    }

    public FluidTank(FluidStack stack, int capacity)
    {
        setCapacity(capacity);
        setFluid(stack);
    }

    public FluidTank(Fluid fluid, int amount, int capacity)
    {
        this(new FluidStack(fluid, amount), capacity);
    }

    public FluidTank readFromNBT(NBTTagCompound nbt)
    {
        setCapacity(nbt.getInteger("Capacity"));
        
        if (!nbt.hasKey("Empty"))
        {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
            setFluid(fluid);
        }
        
//        System.out.println();
//        System.out.printf("Tank (read): %d/%d (%s)", fluid.amount, capacity, FluidRegistry.getFluidName(fluid.fluidID));
//        System.out.println();
        
        return this;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
    	nbt.setInteger("Capacity", capacity);
        
    	if (fluid != null)
        {
            fluid.writeToNBT(nbt);
        }
        else
        {
            nbt.setString("Empty", "");
        }
    	
//    	System.out.println();
//    	System.out.printf("Tank (write): %d/%d (%s)", fluid.amount, capacity, FluidRegistry.getFluidName(fluid.fluidID));
//    	System.out.println();
        
    	return nbt;
    }

    public void setFluid(FluidStack fluid)
    {
        // limit the stored fluid to the specified capacity
        if (fluid != null)
    	{
        	fluid.amount = Math.min(fluid.amount, capacity);
    	}
        
        this.fluid = fluid;
    }

    public void setCapacity(int capacity)
    {
    	// negative capacity makes no sense
        this.capacity = (capacity >= 0) ? capacity : 0;
    }

    /* IFluidTank */
    @Override
    public FluidStack getFluid()
    {
        return fluid;
    }

    @Override
    public int getFluidAmount()
    {
        return (fluid != null) ? fluid.amount : 0;
    }

    @Override
    public int getCapacity()
    {
        return capacity;
    }

    @Override
    public FluidTankInfo getInfo()
    {
        return new FluidTankInfo(this);
    }

    @Override
    public int fill(FluidStack fillFluid, boolean doFill)
    {
        if (fillFluid == null)
        {
            return 0;
        }

        if (fluid == null)
        {
        	if (!doFill)
        	{
        		return Math.min(capacity, fillFluid.amount);
        	}
        	
            fluid = new FluidStack(fillFluid, Math.min(capacity, fillFluid.amount));

            return fluid.amount;
        }

        if (!fluid.isFluidEqual(fillFluid))
        {
            return 0;
        }
        
        int fillAmount = Math.min(capacity - fluid.amount, fillFluid.amount);

        if (doFill && fillAmount > 0)
        {
            fluid.amount += fillAmount;
        }

        return fillAmount;
    }

    @Override
    public FluidStack drain(int drainAmount, boolean doDrain)
    {
        if (fluid == null)
        {
            return null;
        }

        if (fluid.amount < drainAmount)
        {
        	drainAmount = fluid.amount;
        }

        FluidStack stack = new FluidStack(fluid, drainAmount);
        
        if (doDrain)
        {
            fluid.amount -= drainAmount;
            
            if (fluid.amount == 0)
            {
                fluid = null;
            }
        }
        
        return stack;
    }
}
