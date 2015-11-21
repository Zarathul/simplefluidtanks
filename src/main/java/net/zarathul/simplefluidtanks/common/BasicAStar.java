package net.zarathul.simplefluidtanks.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/**
 * Very basic unoptimized implementation of the A-Star algorithm, that only works on a plane (meaning it does not search down- or upwards). It is currently used to find the closest block coordinates
 * in a {@link Collection} to the given block coordinates.<br>
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
		 * The {@link BlockPos} representing the position of this node in the world.
		 */
		public BlockPos block;
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
		 * 
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
		 * Indicates if this {@link Node} has a parent or not.
		 * 
		 * @return <code>true</code> if this {@link Node} has a parent, otherwise <code>false</code>.
		 */
		public boolean hasParent()
		{
			return parent != null;
		}

		/**
		 * Finds the first {@link Node} in the hierarchy.
		 * 
		 * @return The first {@link Node} in the hierarchy.
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
			if (this == obj) return true;
			if (obj == null) return false;
			if (!(obj instanceof Node)) return false;

			Node other = (Node) obj;

			if (block == null)
			{
				if (other.block != null) return false;
			}
			else if (!block.equals(other.block)) return false;

			return true;
		}
	}

	/**
	 * Holds the {@link Node}s that have not been visited by the algorithm yet.
	 */
	private final PriorityQueue<Node> unvisitedNodes;

	/**
	 * Holds the coordinates the algorithm has already visited.
	 */
	private final HashSet<BlockPos> visitedBlocks;

	/**
	 * Holds the current smallest known cost to get to a {@link Node}, for every visited {@link Node}.
	 */
	private final HashMap<BlockPos, Integer> minCosts;

	/**
	 * A {@link Set} of {@link BlockPos} the algorithm can move through.
	 */
	private HashSet<BlockPos> passableBlocks;

	/**
	 * Default constructor.
	 */
	public BasicAStar()
	{
		unvisitedNodes = new PriorityQueue<Node>();
		visitedBlocks = new HashSet<BlockPos>();
		minCosts = new HashMap<BlockPos, Integer>();
	}

	/**
	 * Creates a new instance of the algorithm that can perform searches within the specified bounds.
	 * 
	 * @param passableBlocks
	 * A {@link Collection} of {@link BlockPos} the algorithm can move through.
	 */
	public BasicAStar(Collection<BlockPos> passableBlocks)
	{
		this();
		this.passableBlocks = new HashSet<BlockPos>(passableBlocks);
	}

	/**
	 * Tries to find the shortest path from one {@link BlockPos} to another.
	 * 
	 * @param start
	 * The coordinates of the starting {@link Node}.
	 * @param goal
	 * The coordinates of the goal.
	 * @return <code>null</code> if one of the arguments was <code>null</code> or if no way could be found from the start to the goal.<br>
	 * Otherwise returns a {@link Node} representing the reached goal. It holds the total accumulated cost to reach the goal and it can be used to step through and reconstruct the shortest path.
	 */
	public Node getShortestPath(BlockPos start, BlockPos goal)
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
	 * Sets the {@link BlockPos} the algorithm can move through.
	 * 
	 * @param passableBlocks
	 * A {@link Set} of {@link BlockPos} the algorithm can move through.
	 */
	public void setPassableBlocks(Collection<BlockPos> passableBlocks)
	{
		this.passableBlocks = new HashSet<BlockPos>(passableBlocks);
	}

	/**
	 * Computes the cost that has been accumulated so far and the estimated remaining cost for a {@link Node}.
	 * 
	 * @param node
	 * The {@link Node} for which the costs should be computed.
	 * @param goal
	 * The coordinates of the goal.
	 */
	private void computeCosts(Node node, BlockPos goal)
	{
		int currentCost = (node.hasParent()) ? node.parent.currentCost + getMovementCost(node.parent.block, node.block) : 0;
		int estimatedRemainingCost = getEstimatedRemainingCost(node.block, goal);

		node.currentCost = currentCost;
		node.estimatedTotalCost = currentCost + estimatedRemainingCost;
	}

	/**
	 * The heuristic algorithm (Manhattan distance) used to guess the remaining cost for a {@link Node}.
	 * 
	 * @param start
	 * The coordinates of the starting {@link Node}.
	 * @param goal
	 * The coordinates of the goal.
	 * @return The guessed cost to get from <code>start</code> to <code>goal</code>.
	 */
	private int getEstimatedRemainingCost(BlockPos start, BlockPos goal)
	{
		// Manhattan Distance
		int dx = Math.abs(start.getX() - goal.getX());
		int dy = Math.abs(start.getY() - goal.getY());

		return dx + dy;
	}

	/**
	 * The algorithm used to determine the cost to move from a {@link Node} to one of its adjacent {@link Node}s.
	 * 
	 * @param from
	 * The coordinates of the {@link Node} to move from.
	 * @param to
	 * The coordinates of the {@link Node} to move to.
	 * @return
	 */
	private int getMovementCost(BlockPos from, BlockPos to)
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
	 * 
	 * @param node
	 * The {@link Node} to get the adjacent {@link Node}s for.
	 * @param goal
	 * The coordinates of the goal.
	 */
	private void getAdjacentNodes(Node node, BlockPos goal)
	{
		BlockPos[] neighborBlocks = new BlockPos[]
		{
			node.block.offset(EnumFacing.EAST),
			node.block.offset(EnumFacing.WEST),
			node.block.offset(EnumFacing.SOUTH),
			node.block.offset(EnumFacing.NORTH)
		};

		for (BlockPos neighborBlock : neighborBlocks)
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
