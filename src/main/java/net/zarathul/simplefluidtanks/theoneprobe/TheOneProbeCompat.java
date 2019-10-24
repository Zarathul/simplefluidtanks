package net.zarathul.simplefluidtanks.theoneprobe;

import mcjty.theoneprobe.api.ITheOneProbe;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Hosts the callback for TheOneProbe.
 */
public final class TheOneProbeCompat
{
	public static class GetTheOneProbe implements Function<ITheOneProbe, Void>
	{
		@Nullable
		@Override
		public Void apply(ITheOneProbe theOneProbe)
		{
			theOneProbe.registerProvider(new TankInfoProvider());
			theOneProbe.registerProbeConfigProvider(new TankInfoConfigProvider());
			SimpleFluidTanks.log.info("TheOneProbe compatibility enabled.");

			return null;
		}
	}
}
