package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Custom implementation of the {@link IFluidTank} interface.
 * I'd much prefer to use the default implementation {@link net.minecraftforge.fluids.capability.templates.FluidTank},
 * but it's still buggy in 1.14.4, just different bugs.
 */
public class FluidTank implements IFluidTank, IFluidHandler
{
	@Nonnull
	private FluidStack fluid;
	private int capacity;
	private Consumer<FluidChange> fluidChangeHandler;

	/**
	 * Creates a new instance with the supplied capacity.
	 *
	 * @param capacity
	 * The capacity of the tank.
	 * @param fluidChangeHandler
	 * A callback that is triggered whenever the fluid in the tank changes.
	 */
	public FluidTank(int capacity, Consumer<FluidChange> fluidChangeHandler)
	{
		this.capacity = capacity;
		this.fluidChangeHandler = fluidChangeHandler;
	}

	// IFluidTank

	@Nonnull
	@Override
	public FluidStack getFluid()
	{
		return fluid;
	}

	@Override
	public int getFluidAmount()
	{
		return fluid.getAmount();
	}

	@Override
	public int getCapacity()
	{
		return capacity;
	}

	@Override
	public boolean isFluidValid(FluidStack stack)
	{
		return true;
	}

	// IFluidHandler

	@Override
	public int getTanks()
	{
		return 1;
	}

	@Nonnull
	@Override
	public FluidStack getFluidInTank(int tank)
	{
		return fluid;
	}

	@Override
	public int getTankCapacity(int tank)
	{
		return capacity;
	}

	@Override
	public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
	{
		return true;
	}

	// IFluidHandler + IFluidTank

	@Override
	public int fill(FluidStack resource, FluidAction action)
	{
		if (resource.isEmpty()) return 0;

		if (fluid.isEmpty())
		{
			if (action.simulate())
			{
				return Math.min(capacity, resource.getAmount());
			}

			setFluid(resource);
			fluidChanged(FluidChange.TYPE);
			// FIXME: fluid event
			//if (containerTile != null) FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, containerTile.getWorld(), containerTile.getPos(), this, fluid.amount));

			return fluid.getAmount();
		}

		if (!fluid.isFluidEqual(resource)) return 0;

		int fillAmount = Math.min(capacity - fluid.getAmount(), resource.getAmount());

		if (action.execute() && fillAmount > 0)
		{
			fluid.grow(fillAmount);
			fluidChanged(FluidChange.AMOUNT);
			// FIXME: fluid event
			//if (containerTile != null) FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, containerTile.getWorld(), containerTile.getPos(), this, fillAmount));
		}

		return fillAmount;
	}

	@Nonnull
	@Override
	public FluidStack drain(int maxDrain, FluidAction action)
	{
		if (fluid.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;

		if (fluid.getAmount() < maxDrain)
		{
			maxDrain = fluid.getAmount();
		}

		FluidStack drainedFluid = new FluidStack(fluid, maxDrain);

		if (action.execute())
		{
			fluid.shrink(maxDrain);
			fluidChanged(FluidChange.AMOUNT);
			// FIXME: fluid event
			//if (containerTile != null) FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(fluid, containerTile.getWorld(), containerTile.getPos(), this, drainAmount));
		}

		return drainedFluid;
	}

	@Nonnull
	@Override
	public FluidStack drain(FluidStack resource, FluidAction action)
	{
		if (fluid.isEmpty() || !fluid.isFluidEqual(resource)) return FluidStack.EMPTY;

		return drain(resource.getAmount(), action);
	}

	public void readFromNBT(CompoundNBT nbt)
	{
		capacity = nbt.getInt("Capacity");
		fluid = FluidStack.loadFluidStackFromNBT(nbt);
	}

	public void writeToNBT(CompoundNBT nbt)
	{
		nbt.putInt("Capacity", capacity);
		fluid.writeToNBT(nbt);
	}

	// Other stuff

	/**
	 * Sets the type and amount of fluid contained in the tank.
	 *
	 * @param fluid
	 * The fluid that should be contained in the tank.<br>
	 * Note that the amount will be limited to the tanks capacity.
	 */
	public void setFluid(FluidStack fluid)
	{
		this.fluid = fluid.copy();

		if (!this.fluid.isEmpty())
		{
			// limit the stored fluid to the tanks capacity
			this.fluid.setAmount(Math.min(this.fluid.getAmount(), capacity));
		}
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
		this.capacity = Math.max(0, capacity);

		// limit the fluid amount to the new capacity
		if (!fluid.isEmpty())
		{
			fluid.setAmount(Math.min(this.capacity, fluid.getAmount()));
		}
	}

	private void fluidChanged(FluidChange change)
	{
		if (fluidChangeHandler != null) fluidChangeHandler.accept(change);
	}

	public enum FluidChange
	{
		TYPE,		// Only going from empty to some fluid is considered a type change, but not going from a fluid to empty.
		AMOUNT;

		public boolean type() { return this == TYPE; }
		public boolean amount() { return this == AMOUNT; }
	}
//
//	/**
//	 * Gets the remaining capacity of the tank.
//	 *
//	 * @return The remaining amount of fluid the tank can take in until it is full.
//	 */
//	public int getRemainingCapacity()
//	{
//		return getCapacity() - getFluidAmount();
//	}
//
//	/**
//	 * Checks if the tank is full.
//	 *
//	 * @return <code>true</code> if the tank is full, otherwise <code>false</code>.
//	 */
//	public boolean isFull()
//	{
//		return getRemainingCapacity() == 0;
//	}
//
//	// region IFluidTank
//
//	@Nullable
//	@Override
//	public FluidStack getFluid()
//	{
//		return fluid;
//	}
//
//	@Override
//	public int getFluidAmount()
//	{
//		return (fluid != null) ? fluid.amount : 0;
//	}
//
//	@Override
//	public int getCapacity() { return capacity; }
//
//	@Override
//	public FluidTankInfo getInfo() { return new FluidTankInfo(this); }
//
//	@Override
//	public int fill(FluidStack fillFluid, boolean doFill)
//	{
//		if (fillFluid == null || fillFluid.amount <= 0) return 0;
//
//		if (fluid == null)
//		{
//			if (!doFill)
//			{
//				return Math.min(capacity, fillFluid.amount);
//			}
//
//			fluid = new FluidStack(fillFluid, Math.min(capacity, fillFluid.amount));
//			if (containerTile != null) FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, containerTile.getWorld(), containerTile.getPos(), this, fluid.amount));
//
//			return fluid.amount;
//		}
//
//		if (!fluid.isFluidEqual(fillFluid))
//		{
//			return 0;
//		}
//
//		int fillAmount = Math.min(capacity - fluid.amount, fillFluid.amount);
//
//		if (doFill && fillAmount > 0)
//		{
//			fluid.amount += fillAmount;
//			if (containerTile != null) FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, containerTile.getWorld(), containerTile.getPos(), this, fillAmount));
//		}
//
//		return fillAmount;
//	}
//
//	@Nullable
//	@Override
//	public FluidStack drain(int drainAmount, boolean doDrain)
//	{
//		if (fluid == null || drainAmount <= 0) return null;
//
//		if (fluid.amount < drainAmount)
//		{
//			drainAmount = fluid.amount;
//		}
//
//		FluidStack drainedFluid = new FluidStack(fluid, drainAmount);
//
//		if (doDrain)
//		{
//			fluid.amount -= drainAmount;
//
//			if (fluid.amount == 0)
//			{
//				fluid = null;
//			}
//
//			if (containerTile != null) FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(fluid, containerTile.getWorld(), containerTile.getPos(), this, drainAmount));
//		}
//
//		return drainedFluid;
//	}
//
//	// endregion
//
//	// region IFluidTankProperties
//
//	@Nullable
//	@Override
//	public FluidStack getContents() { return (fluid == null) ? null : fluid.copy(); }
//
//	/*
//	These values have nothing to do with the current contents of the tank. They are just filters
//	for what could potentially go into the tank. We want to be able to store any fluid, so true
//	everywhere it is.
//	 */
//
//	@Override
//	public boolean canFill() { return true;	}
//
//	@Override
//	public boolean canDrain() { return true; }
//
//	@Override
//	public boolean canFillFluidType(FluidStack fluidStack) { return true; }
//
//	@Override
//	public boolean canDrainFluidType(FluidStack fluidStack) { return true; }
//
//	// endregion
//
//	// region IFluidHandler
//
//	@Override
//	public IFluidTankProperties[] getTankProperties() { return tankProperties; }
//
//	@Nullable
//	@Override
//	public FluidStack drain(FluidStack fluidToDrain, boolean doDrain)
//	{
//		if (fluidToDrain == null || fluid == null || !fluidToDrain.isFluidEqual(fluid)) return null;
//
//		return drain(fluidToDrain.amount, doDrain);
//	}
//
//	// endregion
}
