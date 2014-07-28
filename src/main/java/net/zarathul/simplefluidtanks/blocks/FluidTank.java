package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

/**
 * Custom implementation of the {@link IFluidTank} interface.
 */
public class FluidTank implements IFluidTank
{
	/**
	 * The contained fluid.
	 */
	protected FluidStack fluid;

	/**
	 * The tanks capacity.
	 */
	protected int capacity;

	/**
	 * Creates a new instance with the supplied capacity.
	 * 
	 * @param capacity
	 * The capacity of the tank.
	 */
	public FluidTank(int capacity)
	{
		this(null, capacity);
	}

	/**
	 * Creates a new instance with the supplied capacity and fluid.
	 * 
	 * @param stack
	 * The fluid the tank should be initially filled with.
	 * @param capacity
	 * The capacity of the tank.
	 */
	public FluidTank(FluidStack stack, int capacity)
	{
		setCapacity(capacity);
		setFluid(stack);
	}

	/**
	 * Creates a new instance with the supplied capacity, fluid type and amount.
	 * 
	 * @param fluid
	 * The type of fluid initially contained in the tank.
	 * @param amount
	 * The initial amount of the contained fluid.
	 * @param capacity
	 * The capacity of the tank.
	 */
	public FluidTank(Fluid fluid, int amount, int capacity)
	{
		this(new FluidStack(fluid, amount), capacity);
	}

	public FluidTank readFromNBT(NBTTagCompound nbt)
	{
		setCapacity(nbt.getInteger("Capacity"));

		FluidStack loadedFluid = (nbt.hasKey("Empty")) ? null : FluidStack.loadFluidStackFromNBT(nbt);
		setFluid(loadedFluid);

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

		return nbt;
	}

	/**
	 * Sets the type and amount of fluid contained in the tank.
	 * 
	 * @param fluid
	 * The fluid that should be contained in the tank.<br>
	 * Note that the amount will be limited to the tanks capacity.
	 */
	public void setFluid(FluidStack fluid)
	{
		if (fluid != null)
		{
			// limit the stored fluid to the tanks capacity
			fluid.amount = Math.min(fluid.amount, capacity);
		}

		this.fluid = fluid;
	}

	/**
	 * Sets the capacity of the tank.
	 * 
	 * @param capacity
	 * The capacity the tank should have.<br>
	 * Values smaller than 0 are ignored.
	 */
	public void setCapacity(int capacity)
	{
		// negative capacity makes no sense
		this.capacity = (capacity >= 0) ? capacity : 0;

		// limit the fluid amount to the new capacity
		if (fluid != null)
		{
			fluid.amount = Math.min(this.capacity, fluid.amount);
		}
	}

	/**
	 * Gets the remaining capacity of the tank.
	 * 
	 * @return The remaining amount of fluid the tank can take in until it is full.
	 */
	public int getRemainingCapacity()
	{
		return getCapacity() - getFluidAmount();
	}

	/**
	 * Checks if the tank is full.
	 * 
	 * @return <code>true</code> if the tank is full, otherwise <code>false</code>.
	 */
	public boolean isFull()
	{
		return getRemainingCapacity() == 0;
	}

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

		FluidStack drainedFluid = new FluidStack(fluid, drainAmount);

		if (doDrain)
		{
			fluid.amount -= drainAmount;

			if (fluid.amount == 0)
			{
				fluid = null;
			}
		}

		return drainedFluid;
	}
}
