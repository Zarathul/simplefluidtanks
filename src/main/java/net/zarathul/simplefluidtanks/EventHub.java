package net.zarathul.simplefluidtanks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.items.WrenchItem;
import net.zarathul.simplefluidtanks.theoneprobe.TheOneProbeCompat;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Hosts Forge event handlers on both the server and client side.
 */
public final class EventHub
{
	private static final String TANKBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + SimpleFluidTanks.TANKBLOCK_ENTITY_NAME;
	private static final String VALVEBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + SimpleFluidTanks.VALVEBLOCK_ENTITY_NAME;

	@SubscribeEvent
	public static void OnTileEntityRegistration(RegistryEvent.Register<TileEntityType<?>> event)
	{
		SimpleFluidTanks.tankEntity = TileEntityType.Builder.create(TankBlockEntity::new, SimpleFluidTanks.tankBlock).build(null);
		SimpleFluidTanks.tankEntity.setRegistryName(TANKBLOCK_ENTITY_KEY);

		SimpleFluidTanks.valveEntity = TileEntityType.Builder.create(ValveBlockEntity::new, SimpleFluidTanks.valveBlock).build(null);
		SimpleFluidTanks.valveEntity.setRegistryName(VALVEBLOCK_ENTITY_KEY);

		event.getRegistry().registerAll(
				SimpleFluidTanks.tankEntity,
				SimpleFluidTanks.valveEntity
		);
	}

	@SubscribeEvent
	public static void OnBlockRegistration(RegistryEvent.Register<Block> event)
	{
		SimpleFluidTanks.tankBlock = new TankBlock();
		SimpleFluidTanks.valveBlock = new ValveBlock();

		event.getRegistry().registerAll(
				SimpleFluidTanks.tankBlock,
				SimpleFluidTanks.valveBlock
		);
	}

	@SubscribeEvent
	public static void OnItemRegistration(RegistryEvent.Register<Item> event)
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
	public static void OnPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		PlayerEntity player = event.getEntityPlayer();
		if (player == null) return;

		ItemStack heldItemStack = event.getItemStack();
		if (heldItemStack.isEmpty()) return;

		if (Utils.isWrenchItem(heldItemStack.getItem()) && player.isSneaking())
		{
			TileEntity tileEntity = player.getEntityWorld().getChunkAt(event.getPos()).getTileEntity(event.getPos(), Chunk.CreateEntityType.CHECK);

			if (tileEntity instanceof TankBlockEntity || tileEntity instanceof  ValveBlockEntity)
			{
				event.setUseBlock(Event.Result.ALLOW);
				event.setUseItem(Event.Result.DENY);
			}
		}
	}

	@SubscribeEvent
	public static void onInteropSetup(InterModEnqueueEvent event)
	{
		if (ModList.get().isLoaded("theoneprobe"))
		{
			SimpleFluidTanks.log.debug("Sending compatibility request to TheOneProbe.");
			InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> new TheOneProbeCompat());
		}
		else
		{
			SimpleFluidTanks.log.debug("TheOneProbe not found. Skipping compatibility request.");
		}
	}
}