package net.zarathul.simplefluidtanks;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.items.WrenchItem;
import net.zarathul.simplefluidtanks.rendering.BakedTankFluidModel;
import net.zarathul.simplefluidtanks.rendering.BakedTankModel;
import net.zarathul.simplefluidtanks.theoneprobe.TheOneProbeCompat;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

import java.util.ArrayList;
import java.util.Map;

/**
 * Hosts Forge event handlers on both the server and client side.
 */
public final class EventHub
{
	// Common

	private static final String TANKBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + SimpleFluidTanks.TANKBLOCK_ENTITY_NAME;
	private static final String VALVEBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + SimpleFluidTanks.VALVEBLOCK_ENTITY_NAME;

	@SubscribeEvent
	public static void OnTileEntityRegistration(RegistryEvent.Register<TileEntityType<?>> event)
	{
		// TODO: check is the null really is a problem
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

		TileEntity tile = player.getEntityWorld().getChunkAt(event.getPos()).getTileEntity(event.getPos(), Chunk.CreateEntityType.CHECK);
		boolean tileIsTankComponent = tile instanceof TankBlockEntity || tile instanceof  ValveBlockEntity;
		ItemStack heldItemStack = event.getItemStack();

		if (heldItemStack.isEmpty()) return;

		if (tileIsTankComponent && Utils.isWrenchItem(heldItemStack.getItem()) && player.isSneaking())
		{
			event.setUseBlock(Event.Result.ALLOW);
			event.setUseItem(Event.Result.DENY);
		}
	}

	@SubscribeEvent
	public static void onInteropSetup(InterModEnqueueEvent event)
	{
		if (ModList.get().isLoaded("theoneprobe"))
		{
			SimpleFluidTanks.log.info("Sending compatibility request to TheOneProbe.");
			InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> new TheOneProbeCompat());
		}
		else
		{
			SimpleFluidTanks.log.info("TheOneProbe not found. Skipping compatibility request.");
		}
	}

	// Client

	@SubscribeEvent
	public static void onModelBakeEvent(ModelBakeEvent event)
	{
		// generate fluid models for all registered fluids for 16 levels each

		Fluid fluid;
		IBakedModel[] bakedFluidModels;

		for (Map.Entry<ResourceLocation, Fluid> entry : ForgeRegistries.FLUIDS.getEntries())
		{
			fluid = entry.getValue();
			// Why the EMPTY fluid and the flowing variants are in the registry now is beyond me.
			if (fluid == Fluids.EMPTY || fluid == Fluids.FLOWING_WATER || fluid == Fluids.FLOWING_LAVA) continue;

			bakedFluidModels = new IBakedModel[BakedTankModel.FLUID_LEVELS];

			for (int x = 0; x < BakedTankModel.FLUID_LEVELS; x++)
			{
				bakedFluidModels[x] = new BakedTankFluidModel(fluid, x + 1);
			}

			BakedTankModel.FLUID_MODELS.put(entry.getKey(), bakedFluidModels);
		}

		// get ModelResourceLocations of all tank block variants from the registry except "inventory"

		Map<ResourceLocation, IBakedModel> registry = event.getModelRegistry();
		ArrayList<ResourceLocation> modelLocations = Lists.newArrayList();

		// TODO: remove the toLowerCase() later when everything is changed to be lc by default
		// as of 1.11.2 (maybe earlier) all resource names must be all lower case
		String modelPath = SimpleFluidTanks.TANK_BLOCK_NAME.toLowerCase();

		for (ResourceLocation modelLoc : registry.keySet())
		{
			if (modelLoc instanceof ModelResourceLocation
				&& modelLoc.getNamespace().equals(SimpleFluidTanks.MOD_ID)
				&& modelLoc.getPath().equals(modelPath))
			{
				if (((ModelResourceLocation)modelLoc).getVariant().equals("inventory")) modelLocations.add(modelLoc);
			}
		}

		// replace the registered tank block variants with BakedTankModels

		IBakedModel registeredModel;
		IBakedModel replacementModel;

		for (ResourceLocation loc : modelLocations)
		{
			registeredModel = event.getModelRegistry().get(loc);
			replacementModel = new BakedTankModel(registeredModel);
			event.getModelRegistry().put(loc, replacementModel);
		}
	}
}