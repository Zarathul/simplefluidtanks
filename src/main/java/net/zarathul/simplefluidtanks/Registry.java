package net.zarathul.simplefluidtanks;

import net.minecraft.item.Item;
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
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Provides helper methods to register blocks and custom renderers.
 */
public final class Registry
{
	private static final String TANKBLOCK_NAME = "tankBlock";
	private static final String TANKBLOCK_KEY = SimpleFluidTanks.MOD_ID + TANKBLOCK_NAME;
	
	private static final String TANKBLOCK_ENTITY_NAME = "tankBlockEntity";
	private static final String TANKBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + TANKBLOCK_ENTITY_NAME;
	
	private static final String TANKITEM_NAME = "tankItem";
	
	private static final String VALVEBLOCK_NAME = "valveBlock";
	private static final String VALVEBLOCK_KEY = SimpleFluidTanks.MOD_ID + VALVEBLOCK_NAME;
	
	private static final String VALVEBLOCK_ENTITY_NAME = "valveBlockEntity";
	private static final String VALVEBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + VALVEBLOCK_ENTITY_NAME;
	
	private static final String VALVEITEM_NAME = "valveItem";
	
	/**
	 * Gets the {@link TankBlock}s unlocalized name.
	 * @return
	 * The {@link TankBlock}s unlocalized name.
	 */
	public static String getTankBlockName()
	{
		return TANKBLOCK_NAME;
	}
	
	/**
	 * Gets the {@link TankItem}s unlocalized name.
	 * @return
	 * The {@link TankItem}s unlocalized name.
	 */
	public static String getTankItemName()
	{
		return TANKITEM_NAME;
	}
	
	/**
	 * Gets the {@link ValveBlock}s unlocalized name.
	 * @return
	 * The {@link ValveBlock}s unlocalized name.
	 */
	public static String getValveBlockName()
	{
		return VALVEBLOCK_NAME;
	}
	
	/**
	 * Gets the {@link ValveItem}s unlocalized name.
	 * @return
	 * The {@link ValveItem}s unlocalized name.
	 */
	public static String getValveItemName()
	{
		return VALVEITEM_NAME;
	}
	
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
	public static void registerCustomRenderers()
	{
		SimpleFluidTanks.tankBlockRenderer = new TankBlockRenderer();
		SimpleFluidTanks.tankItemRenderer = new TankItemRenderer();
		SimpleFluidTanks.valveItemRenderer = new ValveItemRenderer();
		
		ClientRegistry.bindTileEntitySpecialRenderer(TankBlockEntity.class, SimpleFluidTanks.tankBlockRenderer);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(SimpleFluidTanks.tankBlock), SimpleFluidTanks.tankItemRenderer);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(SimpleFluidTanks.valveBlock), SimpleFluidTanks.valveItemRenderer);
	}
}
