package simplefluidtanks;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Very basic unoptimized implementation of the A-Star algorithm, that only works on a plane (meaning it does not search down- or upwards).
 * It is currently used to find the closest {@link BlockCoords} in a {@link Collection} to given {@link BlockCoords}.<br>
 * (References used http://theory.stanford.edu/~amitp/GameProgramming/)
 */
public class BasicAStar
{
	/**
	 * Represents a node in the {@link BasicAStar} implementation.
	 */
	public class Node implements Comparable<Node>
	{
		/**
		 * The cost that has been accumulated so far.
		 */
		public int currentCost;
		/**
		 * The estimated remaining cost to reach the goal.
		 */
		public int estimatedTotalCost;
		/**
		 * The {@link BlockCoords} representing the position of this node in the world.
		 */
		public BlockCoords block;
		/**
		 * The previous node.
		 */
		public Node parent;
		
		/**
		 * Default constructor.
		 */
		public Node()
		{
			this.currentCost = 0;
			this.estimatedTotalCost = 0;
			this.block = null;
			this.parent = null;
		}
		
		/**
		 * Creates a copy of the the given {@link Node}.
		 * @param other
		 * The {@link Node} to copy.
		 */
		public Node(Node other)
		{
			currentCost = other.currentCost;
			estimatedTotalCost = other.estimatedTotalCost;
			block = other.block;
			parent = other.parent;
		}
		
		
		/**
		 * @return
		 * <code>true</code> if this {@link Node} has a parent.
		 */
		public boolean hasParent()
		{
			return parent != null;
		}
		
		
		/**
		 * @return
		 * The first {@link Node} in the hierarchy.
		 */
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
	
	/**
	 * Holds the {@link Node}s that have not been visited by the algorithm yet.
	 */
	private PriorityQueue<Node> unvisitedNodes;
	
	/**
	 * Holds the coordinates the algorithm has already visited.
	 */
	private HashSet<BlockCoords> visitedBlocks;
	
	/**
	 * Holds the current smallest known cost to get to a {@link Node}, for every visited {@link Node}.
	 */
	private HashMap<BlockCoords, Integer> minCosts;
	
	/**
	 * A {@link Set} of {@link BlockCoords} the algorithm can move through.
	 */
	private HashSet<BlockCoords> passableBlocks;
	
	/**
	 * Default constructor.
	 */
	public BasicAStar()
	{
		unvisitedNodes = new PriorityQueue<Node>();
		visitedBlocks = new HashSet<BlockCoords>();
		minCosts = new HashMap<BlockCoords, Integer>();
	}
	
	/**
	 * @param passableBlocks
	 * A {@link Set} of {@link BlockCoords} the algorithm can move through.
	 */
	public BasicAStar(Collection<BlockCoords> passableBlocks)
	{
		this();
		this.passableBlocks = new HashSet<BlockCoords>(passableBlocks);
	}

	/**
	 * Tries to find the shortest path from one {@link BlockCoords} to another.
	 * @param start
	 * The coordinates of the starting {@link Node}.
	 * @param goal
	 * The coordinates of the goal.
	 * @return
	 * <code>null</code> if one of the arguments was <code>null</code> or if no way could be found from the start to the goal.<br>
	 * Otherwise returns a {@link Node} representing the reached goal. It holds the total accumulated cost to reach the goal and it can be used to step through and reconstruct the shortest path.
	 */
	public Node getShortestPath(BlockCoords start, BlockCoords goal)
	{
		if (start == null || goal == null)
		{
			return null;
		}
		
		visitedBlocks.clear();
		unvisitedNodes.clear();
		minCosts.clear();
		
		Node currentNode = new Node();
		currentNode.block = start;
		computeCosts(currentNode, goal);
		
		unvisitedNodes.offer(currentNode);
		
		while (!currentNode.block.equals(goal) && !unvisitedNodes.isEmpty())
		{
			currentNode = unvisitedNodes.poll();
			
			visitedBlocks.add(currentNode.block);
			
			getAdjacentNodes(currentNode, goal);
		}
		
		return (currentNode.block.equals(goal)) ? currentNode : null;
	}
	
	/**
	 * Sets the {@link BlockCoords} the algorithm can move through.
	 * @param passableBlocks
	 * A {@link Set} of {@link BlockCoords} the algorithm can move through.
	 */
	public void setPassableBlocks(Collection<BlockCoords> passableBlocks)
	{
		this.passableBlocks = new HashSet<BlockCoords>(passableBlocks);
	}
	
	/**
	 * Computes the cost that has been accumulated so far and the estimated remaining cost for a {@link Node}.
	 * @param node
	 * The {@link Node} for which the costs should be computed.
	 * @param goal
	 * The coordinates of the goal.
	 */
	private void computeCosts(Node node, BlockCoords goal)
	{
		int currentCost = (node.hasParent()) ? node.parent.currentCost + getMovementCost(node.parent.block, node.block) : 0;
		int estimatedRemainingCost = getEstimatedRemainingCost(node.block, goal);
		
		node.currentCost = currentCost;
		node.estimatedTotalCost = currentCost + estimatedRemainingCost;
	}
	
	/**
	 * The heuristic algorithm (Manhattan distance) used to guess the remaining cost for a {@link Node}.
	 * @param start
	 * The coordinates of the starting {@link Node}.
	 * @param goal
	 * The coordinates of the goal.
	 * @return
	 * The guessed cost to get from <code>start</code> to <code>goal</code>.
	 */
	private int getEstimatedRemainingCost(BlockCoords start, BlockCoords goal)
	{
		// Manhattan Distance
		int dx = Math.abs(start.x - goal.x);
		int dy = Math.abs(start.y - goal.y);
		
		return dx + dy;
	}
	
	/**
	 * The algorithm uses to get the cost to move from a {@link Node} to one of its adjacent {@link Node}s. 
	 * @param from
	 * The coordinates of the {@link Node} to move from.
	 * @param to
	 * The coordinates of the {@link Node} to move to.
	 * @return
	 */
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
	
	/**
	 * Gets the adjacent {@link Node}s for the given {@link Node} with the given goal and adds them to a {@link PriorityQueue} containing unvisited {@link Node}s.
	 * @param node
	 * The {@link Node} to get the adjacent {@link Node}s for.
	 * @param goal
	 * The coordinates of the goal.
	 */
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
			
			computeCosts(newNode, goal);
			
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
