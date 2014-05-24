package net.zarathul.simplefluidtanks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.zarathul.simplefluidtanks.blocks.FakeFluidBlock;
import net.zarathul.simplefluidtanks.blocks.LegacyTankBlock;
import net.zarathul.simplefluidtanks.blocks.LegacyValveBlock;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.items.LegacyTankItem;
import net.zarathul.simplefluidtanks.items.LegacyValveItem;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.items.WrenchItem;
import net.zarathul.simplefluidtanks.rendering.TankBlockRenderer;
import net.zarathul.simplefluidtanks.rendering.TankItemRenderer;
import net.zarathul.simplefluidtanks.rendering.ValveItemRenderer;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;
import cpw.mods.fml.client.registry.RenderingRegistry;
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

	public static final String WRENCH_ITEM_NAME = "wrench";

	private static final String TANKBLOCK_ENTITY_NAME = "tankBlockEntity";
	private static final String TANKBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + TANKBLOCK_ENTITY_NAME;

	private static final String VALVEBLOCK_ENTITY_NAME = "valveBlockEntity";
	private static final String VALVEBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + VALVEBLOCK_ENTITY_NAME;

	// Legacy registration keys, used for backwards compatibility. TODO:remove in next version
	private static final String LEGACY_TANKBLOCK_KEY = SimpleFluidTanks.MOD_ID + TANKBLOCK_NAME;
	private static final String LEGACY_VALVEBLOCK_KEY = SimpleFluidTanks.MOD_ID + VALVEBLOCK_NAME;
	private static final String LEGACY_TANKBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + TANKBLOCK_ENTITY_NAME;
	private static final String LEGACY_VALVEBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + VALVEBLOCK_ENTITY_NAME;

	/**
	 * Creates and registers all blocks added by the mod.
	 */
	public static void registerBlocks()
	{
		// TankBlock
		SimpleFluidTanks.tankBlock = new TankBlock();
		GameRegistry.registerBlock(SimpleFluidTanks.tankBlock, TankItem.class, TANKBLOCK_NAME, SimpleFluidTanks.MOD_ID);
		SimpleFluidTanks.fakeFluidBlock = new FakeFluidBlock();

		// ValveBlock
		SimpleFluidTanks.valveBlock = new ValveBlock();
		GameRegistry.registerBlock(SimpleFluidTanks.valveBlock, ValveItem.class, VALVEBLOCK_NAME, SimpleFluidTanks.MOD_ID);

		// TileEntities
		GameRegistry.registerTileEntityWithAlternatives(TankBlockEntity.class, TANKBLOCK_ENTITY_KEY, LEGACY_TANKBLOCK_ENTITY_KEY);
		GameRegistry.registerTileEntityWithAlternatives(ValveBlockEntity.class, VALVEBLOCK_ENTITY_KEY, LEGACY_VALVEBLOCK_ENTITY_KEY);

		// Legacy stuff TODO:remove in next version
		SimpleFluidTanks.legacyTankBlock = new LegacyTankBlock();
		GameRegistry.registerBlock(SimpleFluidTanks.legacyTankBlock, LegacyTankItem.class, LEGACY_TANKBLOCK_KEY);
		
		SimpleFluidTanks.legacyValveBlock = new LegacyValveBlock();
		GameRegistry.registerBlock(SimpleFluidTanks.legacyValveBlock, LegacyValveItem.class, LEGACY_VALVEBLOCK_KEY);
	}

	/**
	 * Creates and registers all items added by the mod.
	 */
	public static void registerItems()
	{
		SimpleFluidTanks.wrenchItem = new WrenchItem();
		GameRegistry.registerItem(SimpleFluidTanks.wrenchItem, WRENCH_ITEM_NAME, SimpleFluidTanks.MOD_ID);
	}

	/**
	 * Creates and registers the mods custom renderers.
	 */
	@SideOnly(Side.CLIENT)
	public static void registerCustomRenderers()
	{
		RenderingRegistry.registerBlockHandler(new TankBlockRenderer());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(SimpleFluidTanks.tankBlock), new TankItemRenderer());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(SimpleFluidTanks.valveBlock), new ValveItemRenderer());
		// Legacy stuff TODO:remove in next version
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(SimpleFluidTanks.legacyTankBlock), new TankItemRenderer());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(SimpleFluidTanks.legacyValveBlock), new ValveItemRenderer());
	}

	/**
	 * Registers with Waila, if installed.
	 */
	public static final void registerWithWaila()
	{
		FMLInterModComms.sendMessage("Waila", "register", "net.zarathul.simplefluidtanks.waila.Registry.register");
	}

	/**
	 * Adds a tab in creative mode for the mod.
	 */
	@SideOnly(Side.CLIENT)
	public static final void addCreativeTab()
	{
		SimpleFluidTanks.creativeTab = new CreativeTabs("Simple Fluid Tanks")
		{
			@Override
			public String getTranslatedTabLabel()
			{
				return this.getTabLabel();
			}

			@Override
			public Item getTabIconItem()
			{
				return Item.getItemFromBlock(SimpleFluidTanks.tankBlock);
			}
		};
	}
}
