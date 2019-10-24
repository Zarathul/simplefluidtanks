package net.zarathul.simplefluidtanks;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.configuration.gui.ConfigGuiFactory;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.items.WrenchItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

@Mod(SimpleFluidTanks.MOD_ID)
public class SimpleFluidTanks
{
	// block, item and tileentity names
	public static final String TANK_BLOCK_NAME = "tank";
	public static final String VALVE_BLOCK_NAME = "valve";

	public static final String TANK_ITEM_NAME = "tank";
	public static final String VALVE_ITEM_NAME = "valve";
	public static final String WRENCH_ITEM_NAME = "wrench";

	public static final String TANKBLOCK_ENTITY_NAME = "tank";
	public static final String VALVEBLOCK_ENTITY_NAME = "valve";

	// blocks
	public static TankBlock tankBlock;
	public static ValveBlock valveBlock;
	public static final Material tankMaterial = new Material(MaterialColor.AIR, false, true, true, false, false, false, false, PushReaction.BLOCK);

	// tileEntities
	public static TileEntityType<?> tankEntity;
	public static TileEntityType<?> valveEntity;

	// items
	public static TankItem tankItem;
	public static ValveItem valveItem;
	public static WrenchItem wrenchItem;

	// creative tabs
	public static ItemGroup creativeTab = MakeCreativeTab();

	// constants
	public static final String MOD_ID = "simplefluidtanks";
	public static final String MOD_READABLE_NAME = "Simple Fluid Tanks";

	// logger
	public static final Logger log = LogManager.getLogger(MOD_ID);

	public SimpleFluidTanks()
	{
		// Setup configs
		ModLoadingContext Mlc = ModLoadingContext.get();
		Mlc.registerConfig(ModConfig.Type.COMMON, Config.CommonConfigSpec, MOD_ID + "-common.toml");
		Config.load(Config.CommonConfigSpec, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "-common.toml"));

		// Setup config UI
		DistExecutor.callWhenOn(Dist.CLIENT, () ->
				() -> {
					ConfigGuiFactory.setConfigHolder("net.zarathul.simplefluidtanks.configuration.Config");
					Mlc.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> ConfigGuiFactory::getConfigGui);
					return null;
				}
		);

		// Setup event listeners
		IEventBus SetupEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		SetupEventBus.register(EventHub.class);
		MinecraftForge.EVENT_BUS.register(EventHub.class);
	}

	static ItemGroup MakeCreativeTab()
	{
		// Checks if a "Simple Mods" tab already exists, otherwise makes one.
		return Arrays.stream(ItemGroup.GROUPS)
			.filter(tab -> tab.getPath().equals(SimpleFluidTanks.MOD_ID))
			.findFirst()
			.orElseGet(() ->
				new ItemGroup(SimpleFluidTanks.MOD_ID)
				{
					@OnlyIn(Dist.CLIENT)
					private ItemStack iconStack;

					@Override
					@OnlyIn(Dist.CLIENT)
					public ItemStack createIcon()
					{
						if (iconStack == null) iconStack = new ItemStack(valveItem);

						return iconStack;
					}
				}
			);
	}
}