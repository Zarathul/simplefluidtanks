package net.zarathul.simplefluidtanks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
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
}
