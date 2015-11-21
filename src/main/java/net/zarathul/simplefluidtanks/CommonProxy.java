package net.zarathul.simplefluidtanks;

import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy
{
	public void preInit(FMLPreInitializationEvent event)
	{
		SimpleFluidTanks.commonEventHub = new CommonEventHub();
		FMLCommonHandler.instance().bus().register(SimpleFluidTanks.commonEventHub);
		
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
