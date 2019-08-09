package net.zarathul.simplefluidtanks;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
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
	public static final String TANK_BLOCK_NAME = "tankBlock";
	public static final String VALVE_BLOCK_NAME = "valveBlock";

	public static final String TANK_ITEM_NAME = "tankItem";
	public static final String VALVE_ITEM_NAME = "valveItem";
	public static final String WRENCH_ITEM_NAME = "wrench";

	public static final String TANKBLOCK_ENTITY_NAME = "tankBlockEntity";
	public static final String VALVEBLOCK_ENTITY_NAME = "valveBlockEntity";

	// blocks
	public static TankBlock tankBlock;
	public static ValveBlock valveBlock;

	// tileEntities
	public static TileEntityType<?> tankEntity;
	public static TileEntityType<?> valveEntity;

	// items
	public static TankItem tankItem;
	public static ValveItem valveItem;
	public static WrenchItem wrenchItem;

	// creative tabs
	public static ItemGroup creativeTab;

	// constants
	public static final String MOD_ID = "simplefluidtanks";
	public static final String MOD_READABLE_NAME = "Simple Fluid Tanks";
	public static final String MOD_TAB_NAME = "Simple Mods";
	public static final String VERSION = "@VERSION@";

	// logger
	public static final Logger log = LogManager.getLogger(MOD_ID);

	SimpleFluidTanks()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::CommonInit);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::ClientInit);
	}

	private void CommonInit(final FMLCommonSetupEvent event)
	{
		MinecraftForge.EVENT_BUS.register(CommonEventHub.class);

		//Config.load(event.getSuggestedConfigurationFile());
	}

	private void ClientInit(final FMLClientSetupEvent event)
	{
		// Check if a a "Simple Mods" creative tab already exists, otherwise make one.
		creativeTab = Arrays.stream(ItemGroup.GROUPS)
				.filter(tab -> tab.getTabLabel().equals(SimpleFluidTanks.MOD_TAB_NAME))
				.findFirst()
				.orElseGet(() ->
					new ItemGroup(SimpleFluidTanks.MOD_TAB_NAME)
					{
						private ItemStack iconStack;

						@OnlyIn(Dist.CLIENT)
						public ItemStack createIcon()
						{
							if (iconStack == null) iconStack = new ItemStack(valveItem);

							return iconStack;
						}
					}
				);

		MinecraftForge.EVENT_BUS.register(ClientEventHub.class);
	}
}