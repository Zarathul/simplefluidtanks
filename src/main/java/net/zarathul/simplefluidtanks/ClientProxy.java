package net.zarathul.simplefluidtanks;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.zarathul.simplefluidtanks.registration.Registry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		Registry.addCreativeTab();
		
		super.preInit(event);
		
		SimpleFluidTanks.clientEventHub = new ClientEventHub();
		MinecraftForge.EVENT_BUS.register(SimpleFluidTanks.clientEventHub);

		Registry.registerItemModels();
	}

	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
	}
}
