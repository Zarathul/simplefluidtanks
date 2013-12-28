package simplefluidtanks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = SimpleFluidTanks.MOD_ID, name = "SimpleFluidTanks", version = "0.0.1")
@NetworkMod(serverSideRequired = true, clientSideRequired = true)
public class SimpleFluidTanks
{
	@Instance(value = "SimpleFluidTanks")
	public static SimpleFluidTanks instance;

	@SidedProxy(clientSide = "simplefluidtanks.ClientProxy", serverSide = "simplefluidtanks.ServerProxy")
	public static CommonProxy proxy;
	
	public static Block tankBlock;
	public static Block valveBlock;
	
	public static int tankBlockId;
	public static int valveBlockId;
	
	public static int bucketsPerTank;
	
	public static CreativeTabs creativeTab;
	
	@SideOnly(Side.CLIENT)
	public static TankBlockRenderer tankBlockRenderer;
	
	// constants
	public static final String MOD_ID = "SimpleFluidTanks";
	
	// - texture
	public static final String TEXTURE_LOCATION = "simplefluidtanks";
	public static final String TEXTURE_TANKBLOCK = "brick";
	
	// - registry
	public static final String REGISTRY_TANKBLOCK_NAME = "tankBlock";
	public static final String REGISTRY_TANKBLOCK_KEY = MOD_ID + REGISTRY_TANKBLOCK_NAME;
	public static final String REGISTRY_TANKBLOCK_READABLE_NAME = "Fluid Tank";
	
	public static final String REGISTRY_TANKBLOCK_ENTITY_NAME = "tankBlockEntity";
	public static final String REGISTRY_TANKBLOCK_ENTITY_KEY = MOD_ID + REGISTRY_TANKBLOCK_ENTITY_NAME;
	
	public static final String REGISTRY_VALVEBLOCK_NAME = "valveBlock";
	public static final String REGISTRY_VALVEBLOCK_KEY = MOD_ID + REGISTRY_VALVEBLOCK_NAME;
	public static final String REGISTRY_VALVEBLOCK_READABLE_NAME = "Fluid Tank Valve";
	
	public static final String REGISTRY_VALVEBLOCK_ENTITY_NAME = "valveBlockEntity";
	public static final String REGISTRY_VALVEBLOCK_ENTITY_KEY = MOD_ID + REGISTRY_VALVEBLOCK_ENTITY_NAME;
	
	// - config settings
	public static final int CONFIG_DEFAULT_TANKBLOCK_ID = 2526;
	public static final int CONFIG_DEFAULT_VALVEBLOCK_ID = 2525;
	public static final int CONFIG_DEFAULT_BUCKETS_PER_TANK = 16;
	public static final String CONFIG_CATEGORY = "simple fluid tanks";
	public static final String CONFIG_TANKBLOCK_ID_KEY = "TankblockId";
	public static final String CONFIG_TANKBLOCK_ID_COMMENT = "The tanks block id.";
	public static final String CONFIG_VALVEBLOCK_ID_KEY = "ValveblockId";
	public static final String CONFIG_VALVEBLOCK_ID_COMMENT = "The valves block id.";
	public static final String CONFIG_BUCKETS_PER_TANK_KEY = "BucketsPerTank";
	public static final String CONFIG_BUCKETS_PER_TANK_COMMENT = "The amount of liquid that can be stored in one tank, measured in buckets.";

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

	public static String getModId()
	{
		return SimpleFluidTanks.MOD_ID;
	}
}