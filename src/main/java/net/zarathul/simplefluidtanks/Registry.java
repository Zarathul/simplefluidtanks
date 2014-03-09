package net.zarathul.simplefluidtanks;

import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.Item;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.MinecraftForgeClient;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.rendering.TankBlockRenderer;
import net.zarathul.simplefluidtanks.rendering.TankItemRenderer;
import net.zarathul.simplefluidtanks.rendering.ValveItemRenderer;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;
import net.zarathul.simplefluidtanks.waila.TankBlockDataProvider;
import net.zarathul.simplefluidtanks.waila.ValveBlockDataProvider;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Provides helper methods to register blocks and custom renderers.
 */
public final class Registry
{
	public static final String TANKBLOCK_NAME = "tankBlock";
	public static final String TANKITEM_NAME = "tankItem";
	
	public static final String VALVEBLOCK_NAME = "valveBlock";
	public static final String VALVEITEM_NAME = "valveItem";
	
	private static final String TANKBLOCK_KEY = SimpleFluidTanks.MOD_ID + TANKBLOCK_NAME;
	
	private static final String TANKBLOCK_ENTITY_NAME = "tankBlockEntity";
	private static final String TANKBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + TANKBLOCK_ENTITY_NAME;
	
	private static final String VALVEBLOCK_KEY = SimpleFluidTanks.MOD_ID + VALVEBLOCK_NAME;
	
	private static final String VALVEBLOCK_ENTITY_NAME = "valveBlockEntity";
	private static final String VALVEBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + VALVEBLOCK_ENTITY_NAME;
	
	// Waila 
	private static final String WAILA_TANK_COUNT = "tankCount";
	private static final String WAILA_TOTAL_CAPACITY = "totalCapacity";
	private static final String WAILA_TANK_CAPACITY = "tankCapacity";
	private static final String WAILA_TANK_LINKED = "linkStatus";
	private static final String WAILA_CAPACITY_IN_MILLIBUCKETS = "capacityInMb";
	
	public static final String WAILA_TANK_COUNT_KEY = SimpleFluidTanks.MOD_ID + WAILA_TANK_COUNT;
	public static final String WAILA_TOTAL_CAPACITY_KEY = SimpleFluidTanks.MOD_ID + WAILA_TOTAL_CAPACITY;
	public static final String WAILA_TANK_CAPACITY_KEY = SimpleFluidTanks.MOD_ID + WAILA_TANK_CAPACITY;
	public static final String WAILA_TANK_LINKED_KEY = SimpleFluidTanks.MOD_ID + WAILA_TANK_LINKED;
	public static final String WAILA_CAPACITY_IN_MILLIBUCKETS_KEY = SimpleFluidTanks.MOD_ID + WAILA_CAPACITY_IN_MILLIBUCKETS;
	
	private static final String WAILA = "waila.";
	private static final String WAILA_TANK_COUNT_LOCA = WAILA + WAILA_TANK_COUNT;
	private static final String WAILA_TOTAL_CAPACITY_LOCA = WAILA + WAILA_TOTAL_CAPACITY;
	private static final String WAILA_TANK_CAPACITY_LOCA = WAILA + WAILA_TANK_CAPACITY;
	private static final String WAILA_TANK_LINKED_LOCA = WAILA + WAILA_TANK_LINKED;
	private static final String WAILA_CAPACITY_IN_MILLIBUCKETS_LOCA = WAILA + WAILA_CAPACITY_IN_MILLIBUCKETS;
	
	public static final String WAILA_TOOLTIP = WAILA + "toolTip.";
	public static final String WAILA_TOOLTIP_CAPACITY = WAILA_TOOLTIP + "capacity";
	public static final String WAILA_TOOLTIP_ISLINKED = WAILA_TOOLTIP + "isLinked";
	public static final String WAILA_TOOLTIP_TANKS = WAILA_TOOLTIP + "tanks";
	
	/**
	 * Creates and registers all blocks added by the mod.
	 */
	public static void registerBlocks()
	{
		// TankBlock
		SimpleFluidTanks.tankBlock = new TankBlock();
		GameRegistry.registerBlock(SimpleFluidTanks.tankBlock, TankItem.class, TANKBLOCK_KEY);
		
		// ValveBlock
		SimpleFluidTanks.valveBlock = new ValveBlock();
		GameRegistry.registerBlock(SimpleFluidTanks.valveBlock, ValveItem.class, VALVEBLOCK_KEY);
		
		// TileEntities
		GameRegistry.registerTileEntity(ValveBlockEntity.class, VALVEBLOCK_ENTITY_KEY);
		GameRegistry.registerTileEntity(TankBlockEntity.class, TANKBLOCK_ENTITY_KEY);
	}
	
	/**
	 * Creates and registers the mods custom renderers.
	 */
	@SideOnly(Side.CLIENT)
	public static void registerCustomRenderers()
	{
		SimpleFluidTanks.tankBlockRenderer = new TankBlockRenderer();
		SimpleFluidTanks.tankItemRenderer = new TankItemRenderer();
		SimpleFluidTanks.valveItemRenderer = new ValveItemRenderer();
		
		ClientRegistry.bindTileEntitySpecialRenderer(TankBlockEntity.class, SimpleFluidTanks.tankBlockRenderer);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(SimpleFluidTanks.tankBlock), SimpleFluidTanks.tankItemRenderer);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(SimpleFluidTanks.valveBlock), SimpleFluidTanks.valveItemRenderer);
	}
	
	/**
	 * Registers with Waila, if installed.
	 */
	@SideOnly(Side.CLIENT)
	public static final void registerWithWaila()
	{
		FMLInterModComms.sendMessage("Waila", "register", "net.zarathul.simplefluidtanks.Registry.wailaCallback");
	}
	
	/**
	 * Registers config options and tooltip providers for Waila. (Only called by Waila, don't call this method directly).
	 * @param registrar
	 * The registration interface provided by Waila.
	 */
	public static final void wailaCallback(IWailaRegistrar registrar)
	{
		registrar.addConfig("Simple Fluid Tanks", WAILA_TANK_COUNT_KEY, StatCollector.translateToLocal(WAILA_TANK_COUNT_LOCA));
		registrar.addConfig("Simple Fluid Tanks", WAILA_TOTAL_CAPACITY_KEY, StatCollector.translateToLocal(WAILA_TOTAL_CAPACITY_LOCA));
		registrar.addConfig("Simple Fluid Tanks", WAILA_TANK_CAPACITY_KEY, StatCollector.translateToLocal(WAILA_TANK_CAPACITY_LOCA));
		registrar.addConfig("Simple Fluid Tanks", WAILA_TANK_LINKED_KEY, StatCollector.translateToLocal(WAILA_TANK_LINKED_LOCA));
		registrar.addConfig("Simple Fluid Tanks", WAILA_CAPACITY_IN_MILLIBUCKETS_KEY, StatCollector.translateToLocal(WAILA_CAPACITY_IN_MILLIBUCKETS_LOCA));
		
		registrar.registerBodyProvider(ValveBlockDataProvider.instance, ValveBlockEntity.class);
		registrar.registerBodyProvider(TankBlockDataProvider.instance, TankBlockEntity.class);
	}
}
