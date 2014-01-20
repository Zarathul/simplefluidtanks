package simplefluidtanks;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		int renderDistance = Minecraft.getMinecraft().gameSettings.renderDistance;
		
		return Math.max(400, Math.pow(64, (3 - renderDistance)));
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
		connections[Direction.XPOS] = tag.getBoolean("X+");
		connections[Direction.XNEG] = tag.getBoolean("X-");
		connections[Direction.YPOS] = tag.getBoolean("Y+");
		connections[Direction.YNEG] = tag.getBoolean("Y-");
		connections[Direction.ZPOS] = tag.getBoolean("Z+");
		connections[Direction.ZNEG] = tag.getBoolean("Z-");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		
		tag.setByte("FillPercentage", (byte)fillPercentage);
		tag.setBoolean("isPartOfTank", isPartOfTank);
		tag.setIntArray("ValveCoords", valveCoords);
		tag.setIntArray("TextureIds", textureIds);
		tag.setBoolean("X+", connections[Direction.XPOS]);
		tag.setBoolean("X-", connections[Direction.XNEG]);
		tag.setBoolean("Y+", connections[Direction.YPOS]);
		tag.setBoolean("Y-", connections[Direction.YNEG]);
		tag.setBoolean("Z+", connections[Direction.ZPOS]);
		tag.setBoolean("Z-", connections[Direction.ZNEG]);
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
		return isPartOfTank && valveCoords != null && valveCoords.length >= 3;
	}
	
	public ValveBlockEntity getValve()
	{
		if (isPartOfTank())
		{
			return Utils.getTileEntityAt(worldObj, ValveBlockEntity.class, valveCoords);
		}
		
		return null;
	}
	
	public boolean setValve(int ... coords)
	{
		if (isPartOfTank() || coords == null || coords.length < 3)
		{
			return false;
		}
		
		ValveBlockEntity valveEntity = Utils.getTileEntityAt(worldObj, ValveBlockEntity.class, coords);
		
		if (valveEntity != null)
		{
			valveCoords = coords;
			isPartOfTank = true;
			worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
			
			return true;
		}
		
		return false;
	}
	
	public void updateTextures()
	{
		determineTextureIds();
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public int getFillPercentage()
	{
		return fillPercentage;
	}
	
	public boolean[] getConnections()
	{
		// I'd rather return an read-only collection here. For performance reasons I'll leave it as is for now (the array is used for rendering and a list would introduce unnecessary overhead).
		return connections;
	}
	
	public int getTexture(int side)
	{
		if (side < 0 || side > 5)
		{
			return -1;
		}
		
		return textureIds[side];
	}
	
	public boolean setFillPercentage(int percentage)
	{
		if (percentage < 0 || percentage > 100 || percentage == fillPercentage)
		{
			return false;
		}
		
		fillPercentage = percentage;
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		
		return true;
	}
	
	public boolean hasValveAt(int ... coords)
	{
		if (!isPartOfTank() || coords == null || coords.length < 3)
		{
			return false;
		}
		
		return Arrays.equals(coords, valveCoords);
	}
	
	private void determineTextureIds()
	{
		determineConnections();
		
		textureIds[Direction.XPOS] = ConnectedTexturesHelper.getPositiveXTexture(connections);
		textureIds[Direction.XNEG] = ConnectedTexturesHelper.getNegativeXTexture(connections);
		textureIds[Direction.YPOS] = ConnectedTexturesHelper.getPositiveYTexture(connections);
		textureIds[Direction.YNEG] = ConnectedTexturesHelper.getNegativeYTexture(connections);
		textureIds[Direction.ZPOS] = ConnectedTexturesHelper.getPositiveZTexture(connections);
		textureIds[Direction.ZNEG] = ConnectedTexturesHelper.getNegativeZTexture(connections);
	}
	
	private void determineConnections()
	{
		connections[Direction.XPOS] = shouldConnectTo(xCoord + 1, yCoord, zCoord);	// X+
		connections[Direction.XNEG] = shouldConnectTo(xCoord - 1, yCoord, zCoord);	// X-
		connections[Direction.YPOS] = shouldConnectTo(xCoord, yCoord + 1, zCoord);	// Y+
		connections[Direction.YNEG] = shouldConnectTo(xCoord, yCoord - 1, zCoord);	// Y-
		connections[Direction.ZPOS] = shouldConnectTo(xCoord, yCoord, zCoord + 1);	// Z+
		connections[Direction.ZNEG] = shouldConnectTo(xCoord, yCoord, zCoord - 1);	// Z-
	}
	
	private boolean shouldConnectTo(int x, int y, int z)
	{
		// only check adjacent blocks
		if (x < xCoord - 1 || x > xCoord + 1 || y < yCoord - 1 || y > yCoord + 1 || z < zCoord - 1 || z > zCoord + 1)
		{
			return false;
		}
		
		if (worldObj.getBlockId(x, y, z) == SimpleFluidTanks.tankBlock.blockID)
		{
			TankBlockEntity connectionCandidate = Utils.getTileEntityAt(worldObj, TankBlockEntity.class, x, y, z);
			
			if (connectionCandidate != null)
			{
				return (connectionCandidate.hasValveAt(valveCoords));
			}
		}
		
		return false;
	}

	public void reset()
	{
		isPartOfTank = false;
		fillPercentage = 0;
		Arrays.fill(valveCoords, 0);
		Arrays.fill(textureIds, 0);
		Arrays.fill(connections, false);
		
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
}
