package net.zarathul.simplefluidtanks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.items.WrenchItem;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Hosts Forge event handlers on both the server and client side.
 */
public final class CommonEventHub
{
	private static final String TANKBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + SimpleFluidTanks.TANKBLOCK_ENTITY_NAME;
	private static final String VALVEBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + SimpleFluidTanks.VALVEBLOCK_ENTITY_NAME;

	public CommonEventHub()
	{
	}

	@SubscribeEvent
	public void OnBlockRegistration(RegistryEvent.Register<Block> event)
	{
		SimpleFluidTanks.tankBlock = new TankBlock();
		SimpleFluidTanks.valveBlock = new ValveBlock();

		GameRegistry.registerTileEntity(TankBlockEntity.class, TANKBLOCK_ENTITY_KEY);
		GameRegistry.registerTileEntity(ValveBlockEntity.class, VALVEBLOCK_ENTITY_KEY);

		event.getRegistry().registerAll(
			SimpleFluidTanks.tankBlock,
			SimpleFluidTanks.valveBlock
		);
	}

	@SubscribeEvent
	public void OnItemRegistration(RegistryEvent.Register<Item> event)
	{
		SimpleFluidTanks.tankItem = new TankItem(SimpleFluidTanks.tankBlock);
		SimpleFluidTanks.valveItem = new ValveItem(SimpleFluidTanks.valveBlock);
		SimpleFluidTanks.wrenchItem = new WrenchItem();

		event.getRegistry().registerAll(
			SimpleFluidTanks.tankItem,
			SimpleFluidTanks.valveItem,
			SimpleFluidTanks.wrenchItem
		);
	}

	@SubscribeEvent
	public void OnPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		if (player == null) return;

		TileEntity tile = player.getEntityWorld().getChunkFromBlockCoords(event.getPos()).getTileEntity(event.getPos(), Chunk.EnumCreateEntityType.CHECK);
		boolean tileIsTankComponent = tile instanceof TankBlockEntity || tile instanceof  ValveBlockEntity;
		ItemStack heldItemStack = event.getItemStack();

		if (heldItemStack.isEmpty()) return;

		if (tileIsTankComponent && Utils.isWrenchItem(heldItemStack.getItem()) && player.isSneaking())
		{
			event.setUseBlock(Event.Result.ALLOW);
			event.setUseItem(Event.Result.DENY);
		}
	}
}
