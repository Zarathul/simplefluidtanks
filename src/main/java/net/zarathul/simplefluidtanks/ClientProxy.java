package net.zarathul.simplefluidtanks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.zarathul.simplefluidtanks.rendering.TankBlockRenderer;
import net.zarathul.simplefluidtanks.rendering.TankItemRenderer;
import net.zarathul.simplefluidtanks.rendering.ValveItemRenderer;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		addCreativeTab();
		
		super.preInit(event);
		
		Registry.registerCustomRenderers();
		
		FMLInterModComms.sendMessage("Waila", "register", "net.zarathul.simplefluidtanks.Registry.registerWithWaila");
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
	
	private void addCreativeTab()
	{
		SimpleFluidTanks.creativeTab = new CreativeTabs("Simple Fluid Tanks")
		{
			@Override
			@SideOnly(Side.CLIENT)
			public String getTranslatedTabLabel()
			{
				return this.getTabLabel();
			}

			@Override
			@SideOnly(Side.CLIENT)
			public Item getTabIconItem()
			{
				return Item.getItemFromBlock(SimpleFluidTanks.tankBlock);
			}
		};
	}
}
