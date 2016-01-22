package net.zarathul.simplefluidtanks;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Hosts Forge and FML event handlers on both the server and client side.
 */
public final class CommonEventHub
{
	private final HashSet<BlockPos> ignoreBlockBreakEvent;
	
	public CommonEventHub()
	{
		ignoreBlockBreakEvent = new HashSet<BlockPos>();
	}
	
	public void ignoreBlockBreak(BlockPos pos)
	{
		if (pos != null)
		{
			ignoreBlockBreakEvent.add(pos);
		}
	}
	
	@SubscribeEvent
	public void OnBlockBreak(BreakEvent event)
	{
		if (!event.world.isRemote)
		{
			Block block = event.state.getBlock();
			
			if (block instanceof TankBlock)
			{
				// ignore the event if the tanks coordinates are on the ignore list
				if (ignoreBlockBreakEvent.contains(event.pos))
				{
					ignoreBlockBreakEvent.remove(event.pos);

					return;
				}

				// get the valve the tank is connected to and disband the multiblock
				ValveBlockEntity valveEntity = Utils.getValve(event.world, event.pos);

				if (valveEntity != null)
				{
					valveEntity.disbandMultiblock();
				}
			}
			else if (block instanceof ValveBlock)
			{
				// disband the multiblock if the valve is mined/destroyed
				ValveBlockEntity valveEntity = Utils.getTileEntityAt(event.world, ValveBlockEntity.class, event.pos);

				if (valveEntity != null)
				{
					valveEntity.disbandMultiblock();
				}
			}
		}
	}
}
