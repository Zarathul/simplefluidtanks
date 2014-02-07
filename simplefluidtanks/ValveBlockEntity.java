package simplefluidtanks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class ValveBlockEntity extends TileEntity implements IFluidHandler
{
	protected FluidTank internalTank;
	private ArrayList<int[]> tanks;
	private byte tankFacingSides;
	
	public ValveBlockEntity()
	{
		super();
		internalTank = new FluidTank(0);
		tanks = new ArrayList<int[]>();
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
		
		if (isLinkedTank(xCoord, yCoord, zCoord + 1))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.ZPOS];
		}
		
		if (isLinkedTank(xCoord, yCoord, zCoord - 1))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.ZNEG];
		}
		
		if (isLinkedTank(xCoord + 1, yCoord, zCoord))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.XPOS];
		}
		
		if (isLinkedTank(xCoord - 1, yCoord, zCoord))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.XNEG];
		}
		
		if (isLinkedTank(xCoord, yCoord + 1, zCoord))
		{
			sides = sides | Direction.sidesToBitFlagsMappings[Direction.YPOS];
		}
		
		if (isLinkedTank(xCoord, yCoord - 1, zCoord))
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
		generateTankList();
		
		ArrayList<TankBlockEntity> tankEntities = new ArrayList<TankBlockEntity>(tanks.size());
		
		// set the valve for all connected tanks
		for (int[] tankCoords : tanks)
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tankCoords);
			
			if (tankEntity != null)
			{
				tankEntity.setValve(xCoord, yCoord, zCoord);
				tankEntities.add(tankEntity);
			}
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
		for (int[] tankCoords : tanks)
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tankCoords);
			
			if (tankEntity != null)
			{
				tankEntity.reset();
			}
		}
		
		tanks.clear();
		tankFacingSides = 0;
		internalTank.setCapacity(0);
		internalTank.setFluid(null);
		
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
			tanks = (ArrayList<int[]>)objStream.readObject();
		}
		finally
		{
			objStream.close();
		}
	}
	
	private void distributeFluidToTanks()
	{
		int amountToDistribute = internalTank.getFluidAmount();
		
		if (amountToDistribute == 0 || amountToDistribute == internalTank.getCapacity())
		{
			int percentage = (amountToDistribute == 0) ? 0 : 100;
			
			// empty all tanks
			for (int[] tankCoords : tanks)
			{
				TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, tankCoords);
				
				if (tankEntity != null)
				{
					tankEntity.setFillPercentage(percentage);
				}
			}
		}
		else
		{
			
		}
	}
	
	private void generateTankList()
	{
		int[] startCoords = new int[] { xCoord, yCoord, zCoord };
		ArrayList<int[]> lastFoundTanks = new ArrayList<int[]>();
		ArrayList<int[]> newFoundTanks = new ArrayList<int[]>();
		int[][] newCoords = new int[6][];
		
		lastFoundTanks.add(startCoords);
		
		do
		{
			for (int[] tankCoords : lastFoundTanks)
			{
				newCoords[Direction.XPOS] = new int[] { tankCoords[0] + 1, tankCoords[1]    , tankCoords[2]     };
				newCoords[Direction.XNEG] = new int[] { tankCoords[0] - 1, tankCoords[1]    , tankCoords[2]     };
				newCoords[Direction.YPOS] = new int[] { tankCoords[0]    , tankCoords[1] + 1, tankCoords[2]     };
				newCoords[Direction.YNEG] = new int[] { tankCoords[0]    , tankCoords[1] - 1, tankCoords[2]     };
				newCoords[Direction.ZPOS] = new int[] { tankCoords[0]    , tankCoords[1]    , tankCoords[2] + 1 };
				newCoords[Direction.ZNEG] = new int[] { tankCoords[0]    , tankCoords[1]    , tankCoords[2] - 1 };
				
				for (int[] coords : newCoords)
				{
					if (worldObj.getBlockId(coords[0], coords[1], coords[2]) == SimpleFluidTanks.tankBlock.blockID && !isLinkedTank(coords))
					{
						tanks.add(coords);
						newFoundTanks.add(coords);
					}
				}
			}
			
			lastFoundTanks.clear();
			lastFoundTanks.addAll(newFoundTanks);
			newFoundTanks.clear();
		}
		while (lastFoundTanks.size() > 0);
	}
	
	private boolean isUnlinkedTank(int ... coords)
	{
		if (coords == null || coords.length < 3)
		{
			return false;
		}
		
		if (worldObj.getBlockId(coords[0], coords[1], coords[2]) == SimpleFluidTanks.tankBlock.blockID)
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, coords);
			
			if (tankEntity != null)
			{
				return !tankEntity.isPartOfTank();
			}
		}
		
		return false;
	}
	
	private boolean isLinkedTank(int ... coords)
	{
		if (coords == null || coords.length < 3)
		{
			return false;
		}
		
		for (int[] linkedTankCoords : tanks)
		{
			if (Arrays.equals(coords, linkedTankCoords))
			{
				return true;
			}
		}
		
		return false;
	}
}
