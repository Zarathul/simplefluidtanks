package net.zarathul.simplefluidtanks;

import net.zarathul.simplefluidtanks.registration.Registry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		SimpleFluidTanks.clientEventHub = new ClientEventHub();
		FMLCommonHandler.instance().bus().register(SimpleFluidTanks.clientEventHub);

		Registry.addCreativeTab();

		super.preInit(event);
	}

	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);

		Registry.registerItemModels();
		Registry.registerWithWaila();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
	}
}
