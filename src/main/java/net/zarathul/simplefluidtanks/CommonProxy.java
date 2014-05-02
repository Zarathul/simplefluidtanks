package net.zarathul.simplefluidtanks;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy
{
	public void preInit(FMLPreInitializationEvent event)
	{
		Config.load(event.getSuggestedConfigurationFile());
		Registry.registerBlocks();
	}
	
	public void init(FMLInitializationEvent event)
	{
		Recipes.register();
	}
	
	public void postInit(FMLPostInitializationEvent event)
	{
	}
}
