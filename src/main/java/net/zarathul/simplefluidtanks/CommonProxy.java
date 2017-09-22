package net.zarathul.simplefluidtanks;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.zarathul.simplefluidtanks.configuration.Config;

public class CommonProxy
{
	public void preInit(FMLPreInitializationEvent event)
	{
		SimpleFluidTanks.log = event.getModLog();

		SimpleFluidTanks.commonEventHub = new CommonEventHub();
		MinecraftForge.EVENT_BUS.register(SimpleFluidTanks.commonEventHub);
		
		Config.load(event.getSuggestedConfigurationFile());
	}

	public void init(FMLInitializationEvent event)
	{
		FMLInterModComms.sendFunctionMessage(
			"theoneprobe",
			"getTheOneProbe",
			"net.zarathul.simplefluidtanks.theoneprobe.TheOneProbeCompat$GetTheOneProbe");
	}

	public void postInit(FMLPostInitializationEvent event)
	{
	}
}
