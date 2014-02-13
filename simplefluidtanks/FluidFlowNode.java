package simplefluidtanks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class FluidFlowNode
{
	public ArrayList<BlockCoords> tanks;
	
	public FluidFlowNode parentSegment;
	public Multimap<Integer, FluidFlowNode> segmentsAbove;
	public Multimap<Integer, FluidFlowNode> segmentsBelow;
	
	private Map<FluidFlowNode, Integer> segmentToGroupMappings;  
	
	public FluidFlowNode()
	{
		tanks = new ArrayList<BlockCoords>();
		segmentsAbove = LinkedListMultimap.create();
		segmentsBelow = LinkedListMultimap.create();
		segmentToGroupMappings = new HashMap<FluidFlowNode, Integer>();
	}
	
	public FluidFlowNode addSegmentAbove(Collection<BlockCoords> tanks)
	{
		return addSegment(0, tanks, true);
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
	
	public FluidFlowNode getFirstDeadEnd()
	{
		FluidFlowNode deadEnd;
		FluidFlowNode current = this;
		
		do
		{
			deadEnd = current;
			current = Utils.getFirstValueInMultiMap(deadEnd.segmentsBelow);
		}
		while (current != null);
		
		return deadEnd;
	}
	
	public int getGroup()
	{
		if (!this.isSource())
		{
			Integer group = parentSegment.segmentToGroupMappings.get(this);
			
			return (group != null) ? group.intValue() : -1;
		}
		
		return -1;
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
			
			segmentToGroupMappings.put(newSegment, group);
			
			return newSegment;
		}
		
		return null;
	}
}
