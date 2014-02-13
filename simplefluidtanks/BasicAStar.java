package simplefluidtanks;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class BasicAStar
{
	public class Node implements Comparable<Node>
	{
		public int currentCost;
		public int estimatedTotalCost;
		public BlockCoords block;
		public Node parent;
		
		public Node()
		{
			this.currentCost = 0;
			this.estimatedTotalCost = 0;
			this.block = null;
			this.parent = null;
		}
		
		public Node(Node other)
		{
			currentCost = other.currentCost;
			estimatedTotalCost = other.estimatedTotalCost;
			block = other.block;
			parent = other.parent;
		}
		
		public boolean hasParent()
		{
			return parent != null;
		}
		
		private Node first()
		{
			Node currentNode = this;
			
			while (currentNode.parent != null)
			{
				currentNode = currentNode.parent;
			}
			
			return currentNode;
		}

		@Override
		public int compareTo(Node other)
		{
			return Integer.compare(estimatedTotalCost, other.estimatedTotalCost);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((block == null) ? 0 : block.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			
			if (obj == null)
				return false;
			
			if (!(obj instanceof Node))
				return false;
			
			Node other = (Node) obj;
			
			if (block == null)
			{
				if (other.block != null)
					return false;
			}
			else if (!block.equals(other.block))
				return false;
			
			return true;
		}
	}
	
	private PriorityQueue<Node> unvisitedNodes;
	private HashSet<BlockCoords> visitedBlocks;
	private HashMap<BlockCoords, Integer> minCosts;
	private HashSet<BlockCoords> passableBlocks;
	
	public BasicAStar()
	{
		unvisitedNodes = new PriorityQueue<Node>();
		visitedBlocks = new HashSet<BlockCoords>();
		minCosts = new HashMap<BlockCoords, Integer>();
	}
	
	public BasicAStar(Collection<BlockCoords> passableBlocks)
	{
		this();
		this.passableBlocks = new HashSet<BlockCoords>(passableBlocks);
	}

	public Node getShortestPath(BlockCoords start, BlockCoords goal)
	{
		if (start == null || goal == null)
		{
			return null;
		}
		
		try
		{
			visitedBlocks.clear();
			unvisitedNodes.clear();
			minCosts.clear();
			
			Node currentNode = new Node();
			currentNode.block = start;
			computeCosts(currentNode, start, goal);
			
			unvisitedNodes.offer(currentNode);
			
			while (!currentNode.block.equals(goal))
			{
				currentNode = unvisitedNodes.poll();
				
				visitedBlocks.add(currentNode.block);
				
				getAdjacentNodes(currentNode, goal);
			}
			
			return currentNode;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public void setPassableBlocks(Collection<BlockCoords> passableBlocks)
	{
		this.passableBlocks = new HashSet<BlockCoords>(passableBlocks);
	}
	
	private void computeCosts(Node node, BlockCoords from, BlockCoords goal)
	{
		int currentCost = (node.hasParent()) ? node.parent.currentCost + getMovementCost(node.parent.block, from) : 0;
		int estimatedRemainingCost = getEstimatedRemainingCost(from, goal);
		
		node.currentCost = currentCost;
		node.estimatedTotalCost = currentCost + estimatedRemainingCost;
	}
	
	private int getEstimatedRemainingCost(BlockCoords from, BlockCoords goal)
	{
		// Manhattan Distance
		int dx = Math.abs(from.x - goal.x);
		int dy = Math.abs(from.y - goal.y);
		
		return dx + dy;
	}
	
	private int getMovementCost(BlockCoords from, BlockCoords to)
	{
		if (from.equals(to))
		{
			return 0;
		}
		else if (passableBlocks.contains(to))
		{
			return 1;
		}
		else
		{
			return Integer.MAX_VALUE;
		}
	}
	
	private void getAdjacentNodes(Node node, BlockCoords goal)
	{
		BlockCoords[] neighborBlocks = new BlockCoords[]
		{
			node.block.cloneWithOffset(1),			// X+
			node.block.cloneWithOffset(-1),			// X-
			node.block.cloneWithOffset(0, 0, 1),	// Z+
			node.block.cloneWithOffset(0, 0, -1),	// Z-
		};
		
		for (BlockCoords neighborBlock : neighborBlocks)
		{
			if (!passableBlocks.contains(neighborBlock) || visitedBlocks.contains(neighborBlock))
			{
				continue;
			}
			
			Node newNode = new Node();
			newNode.block = neighborBlock;
			newNode.parent = node;
			
			computeCosts(newNode, newNode.block, goal);
			
			Integer minCost = minCosts.get(newNode.block);
			
			if (minCost != null && newNode.currentCost < minCost)
			{
				unvisitedNodes.remove(newNode);
				minCost = null;
			}
			
			if (minCost == null)
			{
				unvisitedNodes.offer(newNode);
				minCosts.put(newNode.block, newNode.currentCost);
			}
		}
	}
}
