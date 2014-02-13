package simplefluidtanks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class FluidFlowNode
{
	public ArrayList<BlockCoords> tanks;
	
	public FluidFlowNode parentSegment;
	public Multimap<Integer, FluidFlowNode> segmentsAbove;
	public Multimap<Integer, FluidFlowNode> segmentsBelow;
	
	public FluidFlowNode()
	{
		tanks = new ArrayList<BlockCoords>();
		segmentsAbove = ArrayListMultimap.create();
		segmentsBelow = ArrayListMultimap.create();
	}
	
	public FluidFlowNode addSegmentAbove(int group, Collection<BlockCoords> tanks)
	{
		return addSegment(group, tanks, true);
	}
	public FluidFlowNode addSegmentBelow(int group, Collection<BlockCoords> tanks)
	{
		return addSegment(group, tanks, false);
	}
	
	public boolean isSource()
	{
		return parentSegment == null;
	}
	
	public boolean isDeadEnd()
	{
		return segmentsBelow.size() == 0;
	}
	
	private FluidFlowNode addSegment(int group, Collection<BlockCoords> tanks, boolean above)
	{
		if (tanks != null && !tanks.isEmpty())
		{
			FluidFlowNode newSegment = new FluidFlowNode();
			newSegment.tanks.addAll(tanks);
			newSegment.parentSegment = this;
			
			if (above)
			{
				segmentsAbove.put(group, newSegment);
			}
			else
			{
				segmentsBelow.put(group, newSegment);
			}
			
			return newSegment;
		}
		
		return null;
	}
}
