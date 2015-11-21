package net.zarathul.simplefluidtanks;

import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.zarathul.simplefluidtanks.configuration.Config;

/**
 * Hosts Forge and FML event handlers on the client side.
 */
public final class ClientEventHub
{
	@SubscribeEvent
	public void OnConfigChanged(OnConfigChangedEvent event)
	{
		if (SimpleFluidTanks.MOD_ID.equals(event.modID) && !event.isWorldRunning)
		{
			Config.sync();
		}
	}
}
