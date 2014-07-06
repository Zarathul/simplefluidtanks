package net.zarathul.simplefluidtanks;

import net.zarathul.simplefluidtanks.registration.Registry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		FMLCommonHandler.instance().bus().register(new EventHub());

		Registry.addCreativeTab();

		super.preInit(event);

		Registry.registerCustomRenderers();
	}

	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);

		Registry.registerWithWaila();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
	}
}
