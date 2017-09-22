package net.zarathul.simplefluidtanks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.items.WrenchItem;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

@Mod(modid = SimpleFluidTanks.MOD_ID, name = "SimpleFluidTanks", version = SimpleFluidTanks.VERSION,
     updateJSON = "https://raw.githubusercontent.com/Zarathul/mcmodversions/master/simplefluidtanks.json",
     guiFactory = "net.zarathul.simplefluidtanks.configuration.ConfigGuiFactory")
public class SimpleFluidTanks
{
	@Instance(value = SimpleFluidTanks.MOD_ID)
	public static SimpleFluidTanks instance;

	@SidedProxy(clientSide = "net.zarathul.simplefluidtanks.ClientProxy", serverSide = "net.zarathul.simplefluidtanks.ServerProxy")
	public static CommonProxy proxy;

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

	// items
	public static TankItem tankItem;
	public static ValveItem valveItem;
	public static WrenchItem wrenchItem;

	// creative tabs
	public static CreativeTabs creativeTab;

	// event hubs
	public static CommonEventHub commonEventHub;
	public static ClientEventHub clientEventHub;
	
	// logger
	public static Logger log;
	
	// constants
	public static final String MOD_ID = "simplefluidtanks";
	public static final String MOD_READABLE_NAME = "Simple Fluid Tanks";
	public static final String MOD_TAB_NAME = "Simple Mods";
	public static final String VERSION = "@VERSION@";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit(event);
	}

	/**
	 * Adds a creative mode tab.
	 */
	@SideOnly(Side.CLIENT)
	public static final void addCreativeTab()
	{
		// Check if a a "Simple Mods" tab already exists, otherwise make one.
		SimpleFluidTanks.creativeTab = Arrays.stream(CreativeTabs.CREATIVE_TAB_ARRAY)
			.filter(tab -> tab.getTabLabel().equals(SimpleFluidTanks.MOD_TAB_NAME))
			.findFirst()
			.orElseGet(() ->
				new CreativeTabs(SimpleFluidTanks.MOD_TAB_NAME)
				{
					private ItemStack iconStack;

					@Override
					public String getTranslatedTabLabel()
					{
						return this.getTabLabel();
					}

					@Override
					public ItemStack getTabIconItem()
					{
						if (iconStack == null) iconStack = new ItemStack(SimpleFluidTanks.valveItem);

						return iconStack;
					}
				}
			);
	}
}