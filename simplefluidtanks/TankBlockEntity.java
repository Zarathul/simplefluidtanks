package simplefluidtanks;

import com.google.common.primitives.Ints;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;

public class TankBlockEntity extends TileEntity
{
	private int fillPercentage;
	private int[] valveCoords;
	private boolean isPartOfTank;
	
	public TankBlockEntity()
	{
		fillPercentage = 0;
		valveCoords = new int[] { 0, 0, 0 };
		isPartOfTank = false;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		
		fillPercentage = tag.getByte("FillPercentage");
		valveCoords = tag.getIntArray("ValveCoords");
		isPartOfTank = tag.getBoolean("IsPartOfTank");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		
		tag.setByte("FillPercentage", (byte)fillPercentage);
		tag.setIntArray("ValveCoords", valveCoords);
		tag.setBoolean("IsPartOfTank", isPartOfTank);
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
	
	public boolean isPartOfTank()
	{
		// TODO: remove after testing
		return true;
//		return isPartOfTank && valveCoords != null && valveCoords.length == 3;
	}
	
	public int[] getValveCoords()
	{
		return valveCoords;
	}
	
	public ValveBlockEntity getValve()
	{
		if (isPartOfTank())
		{
			TileEntity entity = this.worldObj.getBlockTileEntity(valveCoords[0], valveCoords[1], valveCoords[2]);
			
			if (entity != null && entity instanceof ValveBlockEntity)
			{
				return (ValveBlockEntity)entity;
			}
		}
		
		return null;
	}
	
	public boolean setValve(int x, int y, int z)
	{
		if (!isPartOfTank())
		{
			TileEntity entity = this.worldObj.getBlockTileEntity(x, y, z);
			
			if (entity != null && entity instanceof ValveBlockEntity)
			{
				valveCoords = new int[] { x, y, z };
				isPartOfTank = true;
				this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				
				return true;
			}
		}
		
		return false;
	}
	
	public int getFillPercentage()
	{
		return fillPercentage;
	}
	
	public boolean setFillPercentage(int percentage)
	{
		if (percentage >= 0 && percentage <= 100)
		{
			fillPercentage = percentage;
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			
			return true;
		}
		
		return false;
	}
	
	public boolean isSameValve(int ... coords)
	{
		// TODO: remove after testing
		return true;
		
//		if (coords == null || coords.length != 3)
//		{
//			return false;
//		}
//		
//		return (coords[0] == valveCoords[0] && coords[1] == valveCoords[1] && coords[2] == valveCoords[2]);
	}
}
