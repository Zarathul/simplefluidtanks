package net.zarathul.simplefluidtanks;

import net.minecraft.creativetab.CreativeTabs;
import net.zarathul.simplefluidtanks.blocks.FakeFluidBlock;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = SimpleFluidTanks.MOD_ID, name = "SimpleFluidTanks", version = SimpleFluidTanks.VERSION)
public class SimpleFluidTanks
{
	@Instance(value = SimpleFluidTanks.MOD_ID)
	public static SimpleFluidTanks instance;

	@SidedProxy(clientSide = "net.zarathul.simplefluidtanks.ClientProxy", serverSide = "net.zarathul.simplefluidtanks.ServerProxy")
	public static CommonProxy proxy;

	// blocks
	public static TankBlock tankBlock;
	public static ValveBlock valveBlock;
	public static FakeFluidBlock fakeFluidBlock;

	public static CreativeTabs creativeTab;

	// constants
	public static final String MOD_ID = "simplefluidtanks";
	public static final String VERSION = "@VERSION@";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit(event);
	}
}