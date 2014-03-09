package net.zarathul.simplefluidtanks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		addCreativeTab();
		
		super.preInit(event);
		
		Registry.registerCustomRenderers();
		Registry.registerWithWaila();
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
	
	/**
	 * Adds a tab in creative mode for the mod.
	 */
	private void addCreativeTab()
	{
		SimpleFluidTanks.creativeTab = new CreativeTabs("Simple Fluid Tanks")
		{
			@Override
			public String getTranslatedTabLabel()
			{
				return this.getTabLabel();
			}

			@Override
			public Item getTabIconItem()
			{
				return Item.getItemFromBlock(SimpleFluidTanks.tankBlock);
			}
		};
	}
}
