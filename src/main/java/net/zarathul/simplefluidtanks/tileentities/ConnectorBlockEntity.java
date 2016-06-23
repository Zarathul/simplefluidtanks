package net.zarathul.simplefluidtanks.tileentities;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class ConnectorBlockEntity extends TankBlockEntity  implements IFluidHandler
{


	// ============== IFluidHandler
	
		@Override
		public int fill(ForgeDirection from, FluidStack fillFluid, boolean doFill)
		{
			if (!worldObj.isRemote && isPartOfTank()) {
				ValveBlockEntity valveEntity = getValve();
				if (valveEntity != null) {
					return valveEntity.fill(from, fillFluid, doFill);
				}
			}
			return 0;
		}

		@Override
		public FluidStack drain(ForgeDirection from, FluidStack drainFluid, boolean doDrain)
		{
			if (isPartOfTank())
			{
				ValveBlockEntity valveEntity = getValve();
				if (valveEntity != null) {
					return valveEntity.drain(from, drainFluid, doDrain);
				}
			}
			return null;
		}

		@Override
		public FluidStack drain(ForgeDirection from, int drainAmount, boolean doDrain)
		{
			if (isPartOfTank())
			{
				ValveBlockEntity valveEntity = getValve();
				if (valveEntity != null) {
					return valveEntity.drain(from, drainAmount, doDrain);
				}
			}
			return null;
		}

		@Override
		public boolean canFill(ForgeDirection from, Fluid fluid)
		{
			if (isPartOfTank()) {
				ValveBlockEntity valveEntity = getValve();
				if (valveEntity != null) {
					return valveEntity.canFill(from, fluid);
				}
			}
			return false;
		}

		@Override
		public boolean canDrain(ForgeDirection from, Fluid fluid)
		{
			if (isPartOfTank()) {
				ValveBlockEntity valveEntity = getValve();
				if (valveEntity != null) {
					return valveEntity.canDrain(from, fluid);
				}
			}
			return false;
		}

		@Override
		public FluidTankInfo[] getTankInfo(ForgeDirection from)
		{
			if (isPartOfTank()) {
				ValveBlockEntity valveEntity = getValve();
				if (valveEntity != null) {
					return valveEntity.getTankInfo(from);
				}
			}
			return null;
		}
	
}
