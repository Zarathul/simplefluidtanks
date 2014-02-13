package simplefluidtanks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class BlockCoords implements Serializable // Comparable<BlockCoords>
{
	public int x;
	public int y;
	public int z;
	
//	private static final BlockCoords origin = new BlockCoords(0, 0, 0);
	
	public BlockCoords()
	{
	}
	
	public BlockCoords(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public BlockCoords(BlockCoords coords)
	{
		if (coords != null)
		{
			x = coords.x;
			y = coords.y;
			z = coords.z;
		}
	}
	
	public static ArrayList<BlockCoords> cloneWithOffset(Collection<BlockCoords> blocks, int ... offsets)
	{
		if (blocks == null)
		{
			return null;
		}
		
		ArrayList<BlockCoords> offsetBlocks = new ArrayList<BlockCoords>();
		
		for (BlockCoords block : blocks)
		{
			offsetBlocks.add(block.cloneWithOffset(offsets));
		}
		
		return offsetBlocks;
	}
	
	public void offset(int ... offsets)
	{
		if (offsets == null || offsets.length < 1 || offsets.length > 3)
		{
			return;
		}
		
		x += offsets[0];
		y += (offsets.length > 1) ? offsets[1] : 0;
		z += (offsets.length > 2) ? offsets[2] : 0;
	}
	
	public BlockCoords cloneWithOffset(int ... offsets)
	{
		BlockCoords newCoords = new BlockCoords(this);
		newCoords.offset(offsets);
		
		return newCoords;
	}
	
	public int getDistanceTo(BlockCoords block)
	{
		if (block == null)
		{
			return -1;
		}
		
		int distance = Math.abs(x - block.x) + Math.abs(y - block.y) + Math.abs(z - block.z);
		
		return distance;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BlockCoords))
			return false;
		BlockCoords other = (BlockCoords) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		
		return true;
	}

	@Override
	public String toString()
	{
		return "[x=" + x + ", y=" + y + ", z=" + z + "]";
	}
	
	
//
//	@Override
//	public int compareTo(BlockCoords other)
//	{
//		int distanceThis = getDistanceTo(origin);
//		int distanceOther = other.getDistanceTo(origin);
//		
//		int result = Integer.compare(distanceThis, distanceOther);
//		
//		return result;
//	}
}
