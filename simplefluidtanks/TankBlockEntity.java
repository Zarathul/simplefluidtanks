package simplefluidtanks;

import java.util.HashMap;

import com.google.common.primitives.Ints;

import net.minecraft.launchwrapper.LogWrapper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;

public class TankBlockEntity extends TileEntity
{
	private int fillPercentage;
	private boolean isPartOfTank;
	private int[] valveCoords;
	private int[] textureIds;
	private boolean[] connections;
	
	public TankBlockEntity()
	{
		fillPercentage = 0;
		isPartOfTank = false;
		valveCoords = new int[] { 0, 0, 0 };
		textureIds = new int[] { 0, 0, 0, 0, 0, 0 };
		connections = new boolean[6];
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		
		fillPercentage = tag.getByte("FillPercentage");
		isPartOfTank = tag.getBoolean("isPartOfTank");
		valveCoords = tag.getIntArray("ValveCoords");
		textureIds = tag.getIntArray("TextureIds");
		connections = new boolean[6];
		connections[ConnectedTexturesHelper.XPOS] = tag.getBoolean("X+");
		connections[ConnectedTexturesHelper.XNEG] = tag.getBoolean("X-");
		connections[ConnectedTexturesHelper.YPOS] = tag.getBoolean("Y+");
		connections[ConnectedTexturesHelper.YNEG] = tag.getBoolean("Y-");
		connections[ConnectedTexturesHelper.ZPOS] = tag.getBoolean("Z+");
		connections[ConnectedTexturesHelper.ZNEG] = tag.getBoolean("Z-");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		
		tag.setByte("FillPercentage", (byte)fillPercentage);
		tag.setBoolean("isPartOfTank", isPartOfTank);
		tag.setIntArray("ValveCoords", valveCoords);
		tag.setIntArray("TextureIds", textureIds);
		tag.setBoolean("X+", connections[ConnectedTexturesHelper.XPOS]);
		tag.setBoolean("X-", connections[ConnectedTexturesHelper.XNEG]);
		tag.setBoolean("Y+", connections[ConnectedTexturesHelper.YPOS]);
		tag.setBoolean("Y-", connections[ConnectedTexturesHelper.YNEG]);
		tag.setBoolean("Z+", connections[ConnectedTexturesHelper.ZPOS]);
		tag.setBoolean("Z-", connections[ConnectedTexturesHelper.ZNEG]);
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
		return isPartOfTank && valveCoords != null && valveCoords.length == 3;
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
				
				return true;
			}
		}
		
		return false;
	}
	
	public void updateTextures()
	{
		textureIds = determineTextureIds();
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	private int[] determineTextureIds()
	{
		connections = determineConnections();
		int[] ids = new int[6];
		ids[ConnectedTexturesHelper.XPOS] = ConnectedTexturesHelper.getPositiveXTexture(connections);
		ids[ConnectedTexturesHelper.XNEG] = ConnectedTexturesHelper.getNegativeXTexture(connections);
		ids[ConnectedTexturesHelper.YPOS] = ConnectedTexturesHelper.getPositiveYTexture(connections);
		ids[ConnectedTexturesHelper.YNEG] = ConnectedTexturesHelper.getNegativeYTexture(connections);
		ids[ConnectedTexturesHelper.ZPOS] = ConnectedTexturesHelper.getPositiveZTexture(connections);
		ids[ConnectedTexturesHelper.ZNEG] = ConnectedTexturesHelper.getNegativeZTexture(connections);
		
		return ids;
	}

	public int getFillPercentage()
	{
		return fillPercentage;
	}
	
	public boolean[] getConnections()
	{
		return connections;
	}
	
	public int getTexture(int side)
	{
		if (side < 0 || side > 5)
		{
			return -1;
		}
		
		if (textureIds[side] >= 0)
		{
			return textureIds[side];
		}
		
		textureIds = determineTextureIds();
		
		return textureIds[side];
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
		if (coords == null || coords.length != 3)
		{
			return false;
		}
		
		return (coords[0] == valveCoords[0] && coords[1] == valveCoords[1] && coords[2] == valveCoords[2]);
	}
	
	private boolean[] determineConnections()
	{
		boolean[] connections = new boolean[6];
		connections[ConnectedTexturesHelper.XPOS] = shouldConnectTo(xCoord + 1, yCoord, zCoord);	// X+
		connections[ConnectedTexturesHelper.XNEG] = shouldConnectTo(xCoord - 1, yCoord, zCoord);	// X-
		connections[ConnectedTexturesHelper.YPOS] = shouldConnectTo(xCoord, yCoord + 1, zCoord);	// Y+
		connections[ConnectedTexturesHelper.YNEG] = shouldConnectTo(xCoord, yCoord - 1, zCoord);	// Y-
		connections[ConnectedTexturesHelper.ZPOS] = shouldConnectTo(xCoord, yCoord, zCoord + 1);	// Z+
		connections[ConnectedTexturesHelper.ZNEG] = shouldConnectTo(xCoord, yCoord, zCoord - 1);	// Z-

		return connections;
	}
	
	private boolean shouldConnectTo(int x, int y, int z)
	{
		// only check adjacent blocks
		if (x < xCoord - 1 || x > xCoord + 1 || y < yCoord - 1 || y > yCoord + 1 || z < zCoord - 1 || z > zCoord + 1)
		{
			return false;
		}
		
		int neighborBlockId = worldObj.getBlockId(x, y, z);
		
		if (neighborBlockId == SimpleFluidTanks.tankBlockId)
		{
			TileEntity neighborEntity = worldObj.getBlockTileEntity(x, y, z);
			
			if (neighborEntity == null || !(neighborEntity instanceof TankBlockEntity))
			{
				LogWrapper.log.severe("Possible map corruption detected. TankBlockEntity missing at x:%d / y:%d / z:%d. Expect severe rendering and tank logic issues.", x, y, z);
				return false;
			}
			
			TankBlockEntity connectionCandidate = (TankBlockEntity)neighborEntity;
			
			return (connectionCandidate.isSameValve(valveCoords));
		}
		
		return false;
	}

	public void reset()
	{
		fillPercentage = 0;
		isPartOfTank = false;
		valveCoords = new int[] { 0, 0, 0 };
		textureIds = new int[] { 0, 0, 0, 0, 0, 0 };
		connections = new boolean[6];
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
}
