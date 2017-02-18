package net.zarathul.simplefluidtanks.registration;

import java.util.Arrays;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.configuration.Recipe;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.items.WrenchItem;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Provides helper methods to register blocks, items, custom renderers etc.
 */
public final class Registry
{
	public static final String TANK_BLOCK_NAME = "tankBlock";
	public static final String TANK_ITEM_NAME = "tankItem";

	public static final String VALVE_BLOCK_NAME = "valveBlock";
	public static final String VALVE_ITEM_NAME = "valveItem";

	public static final String WRENCH_ITEM_NAME = "wrench";

	private static final String TANKBLOCK_ENTITY_NAME = "tankBlockEntity";
	private static final String TANKBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + TANKBLOCK_ENTITY_NAME;

	private static final String VALVEBLOCK_ENTITY_NAME = "valveBlockEntity";
	private static final String VALVEBLOCK_ENTITY_KEY = SimpleFluidTanks.MOD_ID + ":" + VALVEBLOCK_ENTITY_NAME;
	
	private static final String TANKITEM_MODEL_RESLOC = SimpleFluidTanks.MOD_ID + ":" + TANK_BLOCK_NAME;
	private static final String VALVEITEM_MODEL_RESLOC = SimpleFluidTanks.MOD_ID + ":" + VALVE_BLOCK_NAME;
	private static final String WRENCHITEM_MODEL_RESLOC = SimpleFluidTanks.MOD_ID + ":" + WRENCH_ITEM_NAME;

	/**
	 * Creates and registers all blocks added by the mod.
	 */
	public static void registerBlocks()
	{
		// TankBlock
		SimpleFluidTanks.tankBlock = new TankBlock();
		GameRegistry.register(SimpleFluidTanks.tankBlock);

		// ValveBlock
		SimpleFluidTanks.valveBlock = new ValveBlock();
		GameRegistry.register(SimpleFluidTanks.valveBlock);

		// TileEntities
		GameRegistry.registerTileEntity(TankBlockEntity.class, TANKBLOCK_ENTITY_KEY);
		GameRegistry.registerTileEntity(ValveBlockEntity.class, VALVEBLOCK_ENTITY_KEY);
	}

	/**
	 * Creates and registers all items added by the mod.
	 */
	public static void registerItems()
	{
		SimpleFluidTanks.tankItem = new TankItem(SimpleFluidTanks.tankBlock);
		GameRegistry.register(SimpleFluidTanks.tankItem);
		
		SimpleFluidTanks.valveItem = new ValveItem(SimpleFluidTanks.valveBlock);
		GameRegistry.register(SimpleFluidTanks.valveItem);
		
		SimpleFluidTanks.wrenchItem = new WrenchItem();
		GameRegistry.register(SimpleFluidTanks.wrenchItem);
	}
	
	/**
	 * Registers item models. Must be called after registerItems().
	 */
	public static void registerItemModels()
	{
		ModelLoader.setCustomModelResourceLocation(SimpleFluidTanks.tankItem, 0, new ModelResourceLocation(TANKITEM_MODEL_RESLOC, "inventory"));
		ModelLoader.setCustomModelResourceLocation(SimpleFluidTanks.valveItem, 0, new ModelResourceLocation(VALVEITEM_MODEL_RESLOC, "inventory"));
		ModelLoader.setCustomModelResourceLocation(SimpleFluidTanks.wrenchItem, 0, new ModelResourceLocation(WRENCHITEM_MODEL_RESLOC, "inventory"));
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
		ItemStack valveBlockRecipeResult = new ItemStack(SimpleFluidTanks.valveBlock);

		registerRecipeWithAlternative(tankBlockRecipeResult, Config.tankBlockRecipe, Config.defaultTankBlockRecipe);
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
			SimpleFluidTanks.log.warn(String.format("Failed to register recipe for '%s'. Check your config file.", result.getItem().getRegistryName()));

			if (!registerRecipe(result, defaultRecipe))
			{
				SimpleFluidTanks.log.error(String.format("Failed to register default recipe for '%s'. This should never happen.", result.getItem().getRegistryName()));
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
				result.setCount(recipe.yield);

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
					@Override
					public String getTranslatedTabLabel()
					{
						return this.getTabLabel();
					}
					
					@Override
					public ItemStack getTabIconItem()
					{
						return new ItemStack(Item.getItemFromBlock(SimpleFluidTanks.valveBlock));
					}
				}
			);
	}
}
