package net.zarathul.simplefluidtanks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;
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
		Config.load(event.getSuggestedConfigurationFile());
		Registry.registerBlocks();
	}
	
	public void init(FMLInitializationEvent event)
	{
		Recipes.registerRecipes();
	}
	
	public void postInit(FMLPostInitializationEvent event)
	{
	}
}
