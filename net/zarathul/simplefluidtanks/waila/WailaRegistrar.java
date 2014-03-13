package net.zarathul.simplefluidtanks.waila;

import cpw.mods.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.util.StatCollector;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.LocalizationHelper;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Handles registration with Waila
 */
public final class WailaRegistrar
{
	private static final String WAILA_TANK_COUNT = "tankCount";
	private static final String WAILA_TOTAL_CAPACITY = "totalCapacity";
	private static final String WAILA_TANK_CAPACITY = "tankCapacity";
	private static final String WAILA_TANK_LINKED = "linkStatus";
	private static final String WAILA_CAPACITY_IN_MILLIBUCKETS = "capacityInMb";
	private static final String WAILA_FLUID_NAME = "fluidName";
	
	public static final String WAILA_TANK_COUNT_KEY = SimpleFluidTanks.MOD_ID + WAILA_TANK_COUNT;
	public static final String WAILA_TOTAL_CAPACITY_KEY = SimpleFluidTanks.MOD_ID + WAILA_TOTAL_CAPACITY;
	public static final String WAILA_TANK_CAPACITY_KEY = SimpleFluidTanks.MOD_ID + WAILA_TANK_CAPACITY;
	public static final String WAILA_TANK_LINKED_KEY = SimpleFluidTanks.MOD_ID + WAILA_TANK_LINKED;
	public static final String WAILA_CAPACITY_IN_MILLIBUCKETS_KEY = SimpleFluidTanks.MOD_ID + WAILA_CAPACITY_IN_MILLIBUCKETS;
	public static final String WAILA_FLUID_NAME_KEY = SimpleFluidTanks.MOD_ID + WAILA_FLUID_NAME;
	
	private static final String WAILA = "waila.";
	private static final String WAILA_TANK_COUNT_LOCA = WAILA + WAILA_TANK_COUNT;
	private static final String WAILA_TOTAL_CAPACITY_LOCA = WAILA + WAILA_TOTAL_CAPACITY;
	private static final String WAILA_TANK_CAPACITY_LOCA = WAILA + WAILA_TANK_CAPACITY;
	private static final String WAILA_TANK_LINKED_LOCA = WAILA + WAILA_TANK_LINKED;
	private static final String WAILA_CAPACITY_IN_MILLIBUCKETS_LOCA = WAILA + WAILA_CAPACITY_IN_MILLIBUCKETS;
	private static final String WAILA_FLUID_NAME_LOCA = WAILA + WAILA_FLUID_NAME;
	
	public static final String WAILA_TOOLTIP = WAILA + "toolTip.";
	public static final String WAILA_TOOLTIP_CAPACITY = WAILA_TOOLTIP + "capacity";
	public static final String WAILA_TOOLTIP_ISLINKED = WAILA_TOOLTIP + "isLinked";
	public static final String WAILA_TOOLTIP_TANKS = WAILA_TOOLTIP + "tanks";
	public static final String WAILA_TOOLTIP_FLUID = WAILA_TOOLTIP + "fluid";
	public static final String WAILA_TOOLTIP_FLUID_EMPTY = WAILA_TOOLTIP + "fluidEmpty";
	
	/**
	 * Registers config options and tooltip providers for Waila. (Only called by Waila, don't call this method directly).
	 * @param registrar
	 * The registration interface provided by Waila.
	 */
	public static final void registerCallback(IWailaRegistrar registrar)
	{
		registrar.addConfig("Simple Fluid Tanks", WAILA_TANK_COUNT_KEY, StatCollector.translateToLocal(WAILA_TANK_COUNT_LOCA));
		registrar.addConfig("Simple Fluid Tanks", WAILA_TOTAL_CAPACITY_KEY, StatCollector.translateToLocal(WAILA_TOTAL_CAPACITY_LOCA));
		registrar.addConfig("Simple Fluid Tanks", WAILA_TANK_CAPACITY_KEY, StatCollector.translateToLocal(WAILA_TANK_CAPACITY_LOCA));
		registrar.addConfig("Simple Fluid Tanks", WAILA_TANK_LINKED_KEY, StatCollector.translateToLocal(WAILA_TANK_LINKED_LOCA));
		registrar.addConfig("Simple Fluid Tanks", WAILA_CAPACITY_IN_MILLIBUCKETS_KEY, StatCollector.translateToLocal(WAILA_CAPACITY_IN_MILLIBUCKETS_LOCA));
		registrar.addConfig("Simple Fluid Tanks", WAILA_FLUID_NAME_KEY, StatCollector.translateToLocal(WAILA_FLUID_NAME_LOCA));
		
		registrar.registerBodyProvider(ValveBlockDataProvider.instance, ValveBlockEntity.class);
		registrar.registerBodyProvider(TankBlockDataProvider.instance, TankBlockEntity.class);
	}
}
