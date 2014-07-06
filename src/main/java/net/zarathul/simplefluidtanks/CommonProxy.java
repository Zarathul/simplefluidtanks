package net.zarathul.simplefluidtanks;

import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy
{
	public void preInit(FMLPreInitializationEvent event)
	{
		Config.load(event.getSuggestedConfigurationFile());
		Registry.registerBlocks();
		Registry.registerItems();
	}

	public void init(FMLInitializationEvent event)
	{
		Registry.registerRecipes();
	}

	public void postInit(FMLPostInitializationEvent event)
	{
	}
}
