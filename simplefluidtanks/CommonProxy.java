package simplefluidtanks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CommonProxy
{
	public void preInit(FMLPreInitializationEvent event)
	{
		// create creative tab
		SimpleFluidTanks.creativeTab = new CreativeTabs("Simple Fluid Tanks")
		{
			@Override
			@SideOnly(Side.CLIENT)
			public String getTranslatedTabLabel()
			{
				return this.getTabLabel();
			}

			@Override
			@SideOnly(Side.CLIENT)
			public int getTabIconItemIndex()
			{
				return SimpleFluidTanks.tankBlock.blockID;
			}
		};
		
		// load config
		Config.load(event);
	}
	
	public void init(FMLInitializationEvent event)
	{
		// create and register TankBlock
		SimpleFluidTanks.tankBlock = new TankBlock(SimpleFluidTanks.tankBlockId);
		GameRegistry.registerBlock(SimpleFluidTanks.tankBlock, TankItem.class, SimpleFluidTanks.REGISTRY_TANKBLOCK_KEY);
		
		// create and register ValveBlock
		SimpleFluidTanks.valveBlock = new ValveBlock(SimpleFluidTanks.valveBlockId);
		GameRegistry.registerBlock(SimpleFluidTanks.valveBlock, ValveItem.class, SimpleFluidTanks.REGISTRY_VALVEBLOCK_KEY);
		
		// register TileEntities
		GameRegistry.registerTileEntity(ValveBlockEntity.class, SimpleFluidTanks.REGISTRY_VALVEBLOCK_ENTITY_KEY);
		GameRegistry.registerTileEntity(TankBlockEntity.class, SimpleFluidTanks.REGISTRY_TANKBLOCK_ENTITY_KEY);
		
		// set harvest levels for blocks
		MinecraftForge.setBlockHarvestLevel(SimpleFluidTanks.tankBlock, "pickaxe", 2);
		MinecraftForge.setBlockHarvestLevel(SimpleFluidTanks.valveBlock, "pickaxe", 2);
	}
	
	public void postInit(FMLPostInitializationEvent event)
	{
		// register Recipes
		Recipes.registerRecipes();
	}
}
