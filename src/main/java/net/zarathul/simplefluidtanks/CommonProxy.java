package net.zarathul.simplefluidtanks;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;

public class CommonProxy
{
	public void preInit(FMLPreInitializationEvent event)
	{
		SimpleFluidTanks.log = event.getModLog();
		
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
