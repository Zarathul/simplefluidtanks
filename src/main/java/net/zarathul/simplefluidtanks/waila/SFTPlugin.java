package net.zarathul.simplefluidtanks.waila;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.util.text.translation.I18n;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Hosts the registry callback for Waila.
 */
@WailaPlugin
public final class SFTPlugin implements IWailaPlugin
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
	public static final String WAILA_TOOLTIP_TANK_CAPACITY = WAILA_TOOLTIP + "tankCapacity";
	public static final String WAILA_TOOLTIP_VALVE_CAPACITY = WAILA_TOOLTIP + "valveCapacity";
	public static final String WAILA_TOOLTIP_ISLINKED = WAILA_TOOLTIP + "isLinked";
	public static final String WAILA_TOOLTIP_TANKS = WAILA_TOOLTIP + "tanks";
	public static final String WAILA_TOOLTIP_FLUID = WAILA_TOOLTIP + "fluid";
	public static final String WAILA_TOOLTIP_FLUID_EMPTY = WAILA_TOOLTIP + "fluidEmpty";
	public static final String WAILA_TOOLTIP_YES = WAILA_TOOLTIP + "yes";
	public static final String WAILA_TOOLTIP_NO = WAILA_TOOLTIP + "no";

	/**
	 * Registers config options and tooltip providers for Waila. (Only called by Waila, don't call this method directly).
	 * 
	 * @param registrar
	 * The registration interface provided by Waila.
	 */
	@Override
	public void register(IWailaRegistrar registrar)
	{
		registrar.addConfig(SimpleFluidTanks.MOD_READABLE_NAME, WAILA_TANK_COUNT_KEY, I18n.translateToLocal(WAILA_TANK_COUNT_LOCA));
		registrar.addConfig(SimpleFluidTanks.MOD_READABLE_NAME, WAILA_TOTAL_CAPACITY_KEY, I18n.translateToLocal(WAILA_TOTAL_CAPACITY_LOCA));
		registrar.addConfig(SimpleFluidTanks.MOD_READABLE_NAME, WAILA_TANK_CAPACITY_KEY, I18n.translateToLocal(WAILA_TANK_CAPACITY_LOCA));
		registrar.addConfig(SimpleFluidTanks.MOD_READABLE_NAME, WAILA_TANK_LINKED_KEY, I18n.translateToLocal(WAILA_TANK_LINKED_LOCA));
		registrar.addConfig(SimpleFluidTanks.MOD_READABLE_NAME, WAILA_CAPACITY_IN_MILLIBUCKETS_KEY, I18n.translateToLocal(WAILA_CAPACITY_IN_MILLIBUCKETS_LOCA));
		registrar.addConfig(SimpleFluidTanks.MOD_READABLE_NAME, WAILA_FLUID_NAME_KEY, I18n.translateToLocal(WAILA_FLUID_NAME_LOCA));

		registrar.registerBodyProvider(ValveBlockDataProvider.instance, ValveBlockEntity.class);
		registrar.registerBodyProvider(TankBlockDataProvider.instance, TankBlockEntity.class);
	}
}
