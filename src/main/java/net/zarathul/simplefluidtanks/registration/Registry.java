package net.zarathul.simplefluidtanks.registration;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.LogWrapper;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.ConnectorBlock;
import net.zarathul.simplefluidtanks.blocks.FakeFluidBlock;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.configuration.Recipe;
import net.zarathul.simplefluidtanks.items.ConnectorItem;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.items.WrenchItem;
import net.zarathul.simplefluidtanks.rendering.TankBlockRenderer;
import net.zarathul.simplefluidtanks.rendering.ValveItemRenderer;
import net.zarathul.simplefluidtanks.tileentities.ConnectorBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Provides helper methods to register blocks, items, custom renderers etc.
 */
public final class Registry
{
	public static final String TANKBLOCK_NAME = "tankBlock";
	public static final String TANKITEM_NAME = "tankItem";

	public static final String CONNECTORBLOCK_NAME = "connectorBlock";
	public static final String CONNECTORITEM_NAME = "connectorItem";

	public static final String VALVEBLOCK_NAME = "valveBlock";
	public static final String VALVEITEM_NAME = "valveItem";

	public static final String WRENCH_ITEM_NAME = "wrench";

	private static final String TANKBLOCK_ENTITY_NAME = "tankBlockEntity";
	private static final String TANKBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + TANKBLOCK_ENTITY_NAME;

	private static final String CONNECTORBLOCK_ENTITY_NAME = "connectorBlockEntity";
	private static final String CONNECTORBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + CONNECTORBLOCK_ENTITY_NAME;

	private static final String VALVEBLOCK_ENTITY_NAME = "valveBlockEntity";
	private static final String VALVEBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + VALVEBLOCK_ENTITY_NAME;

	/**
	 * Creates and registers all blocks added by the mod.
	 */
	public static void registerBlocks()
	{
		// TankBlock
		SimpleFluidTanks.tankBlock = new TankBlock();
		GameRegistry.registerBlock(SimpleFluidTanks.tankBlock, TankItem.class, TANKBLOCK_NAME);
		SimpleFluidTanks.fakeFluidBlock = new FakeFluidBlock();

		// ConnectorBlock
		SimpleFluidTanks.connectorBlock = new ConnectorBlock();
		GameRegistry.registerBlock(SimpleFluidTanks.connectorBlock, ConnectorItem.class, CONNECTORBLOCK_NAME);

		// ValveBlock
		SimpleFluidTanks.valveBlock = new ValveBlock();
		GameRegistry.registerBlock(SimpleFluidTanks.valveBlock, ValveItem.class, VALVEBLOCK_NAME);

		// TileEntities
		GameRegistry.registerTileEntity(TankBlockEntity.class, TANKBLOCK_ENTITY_KEY);
		GameRegistry.registerTileEntity(ConnectorBlockEntity.class, CONNECTORBLOCK_ENTITY_KEY);
		GameRegistry.registerTileEntity(ValveBlockEntity.class, VALVEBLOCK_ENTITY_KEY);
	}

	/**
	 * Creates and registers all items added by the mod.
	 */
	public static void registerItems()
	{
		if (Config.wrenchEnabled)
		{
			SimpleFluidTanks.wrenchItem = new WrenchItem();
			GameRegistry.registerItem(SimpleFluidTanks.wrenchItem, WRENCH_ITEM_NAME, SimpleFluidTanks.MOD_ID);
		}
	}

	/**
	 * Creates and registers the mods custom renderers.
	 */
	@SideOnly(Side.CLIENT)
	public static void registerCustomRenderers()
	{
		RenderingRegistry.registerBlockHandler(new TankBlockRenderer());
		//MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(SimpleFluidTanks.valveBlock), new ValveItemRenderer());
	}

	/**
	 * Registers with Waila, if installed.
	 */
	public static final void registerWithWaila()
	{
		FMLInterModComms.sendMessage("Waila", "register", "net.zarathul.simplefluidtanks.waila.Registry.register");
	}

	/**
	 * Registers the mods recipes.
	 */
	public static final void registerRecipes()
	{
		ItemStack tankBlockRecipeResult = new ItemStack(SimpleFluidTanks.tankBlock);
		ItemStack connectorBlockRecipeResult = new ItemStack(SimpleFluidTanks.connectorBlock);
		ItemStack valveBlockRecipeResult = new ItemStack(SimpleFluidTanks.valveBlock);

		registerRecipeWithAlternative(tankBlockRecipeResult, Config.tankBlockRecipe, Config.defaultTankBlockRecipe);
		registerRecipeWithAlternative(connectorBlockRecipeResult, Config.connectorBlockRecipe, Config.defaultConnectorBlockRecipe);
		registerRecipeWithAlternative(valveBlockRecipeResult, Config.valveBlockRecipe, Config.defaultValveBlockRecipe);

		if (Config.wrenchEnabled)
		{
			ItemStack wrenchRecipeResult = new ItemStack(SimpleFluidTanks.wrenchItem);
			registerRecipeWithAlternative(wrenchRecipeResult, Config.wrenchRecipe, Config.defaultWrenchRecipe);
		}
	}

	/**
	 * Tries to register the recipe for the specified ItemStack. If registration fails
	 * the default recipe is used instead.
	 * 
	 * @param result
	 * The ItemStack crafted using the specified recipe.
	 * @param recipe
	 * The recipe.
	 * @param defaultRecipe
	 * The fall back recipe.
	 */
	private static final void registerRecipeWithAlternative(ItemStack result, Recipe recipe, Recipe defaultRecipe)
	{
		if (!registerRecipe(result, recipe))
		{
			LogWrapper.severe("[%s] Failed to register recipe for: %s. Check your config file.", SimpleFluidTanks.MOD_ID, result.getUnlocalizedName());

			if (!registerRecipe(result, defaultRecipe))
			{
				LogWrapper.severe("[%s] Failed to register default recipe for: %s.", SimpleFluidTanks.MOD_ID, result.getUnlocalizedName());
			}
		}
	}

	/**
	 * Tries to register the recipe for the specified ItemStack.
	 * 
	 * @param result
	 * The ItemStack crafted using the specified recipe.
	 * @param recipe
	 * The recipe.
	 * @returns
	 * <code>true</code> if the registration succeeded, otherwise <code>false</code>.
	 */
	private static final boolean registerRecipe(ItemStack result, Recipe recipe)
	{
		Object[] registrationArgs;

		try
		{
			registrationArgs = recipe.getRegistrationArgs();

			if (registrationArgs != null && recipe.yield > 0)
			{
				result.stackSize = recipe.yield;

				if (recipe.isShapeless)
				{
					GameRegistry.addRecipe(new ShapelessOreRecipe(result, registrationArgs));
				}
				else
				{
					GameRegistry.addRecipe(new ShapedOreRecipe(result, false, registrationArgs));
				}

				return true;
			}
		}
		catch (Exception e)
		{
		}

		return false;
	}

	/**
	 * Adds a tab in creative mode for the mod.
	 */
	@SideOnly(Side.CLIENT)
	public static final void addCreativeTab()
	{
		SimpleFluidTanks.creativeTab = new CreativeTabs(SimpleFluidTanks.MOD_READABLE_NAME)
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
