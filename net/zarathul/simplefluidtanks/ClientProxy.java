package net.zarathul.simplefluidtanks;

import net.minecraftforge.client.MinecraftForgeClient;
import net.zarathul.simplefluidtanks.rendering.TankBlockRenderer;
import net.zarathul.simplefluidtanks.rendering.TankItemRenderer;
import net.zarathul.simplefluidtanks.rendering.ValveItemRenderer;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.waila.WailaRegistrar;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
		
		// create and register custom renderers
		SimpleFluidTanks.tankBlockRenderer = new TankBlockRenderer();
		SimpleFluidTanks.tankItemRenderer = new TankItemRenderer();
		SimpleFluidTanks.valveItemRenderer = new ValveItemRenderer();
		
		RenderingRegistry.registerBlockHandler(new TankBlockRenderer());
		MinecraftForgeClient.registerItemRenderer(SimpleFluidTanks.tankBlock.blockID, SimpleFluidTanks.tankItemRenderer);
		MinecraftForgeClient.registerItemRenderer(SimpleFluidTanks.valveBlock.blockID, SimpleFluidTanks.valveItemRenderer);
	}
	
	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);
		
		// Try to register with Waila
		FMLInterModComms.sendMessage("Waila", "register", "net.zarathul.simplefluidtanks.waila.WailaRegistrar.registerCallback");
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
	}
}
