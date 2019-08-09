package net.zarathul.simplefluidtanks.items;

/*
import appeng.api.implementations.items.IAEWrench;
import blusunrize.immersiveengineering.api.tool.ITool;
import cofh.api.item.IToolHammer;
 */
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;

/**
 * A simple wrench.
 */
/*
@Optional.InterfaceList({
	@Optional.Interface(iface = "appeng.api.implementations.items.IAEWrench", modid = "appliedenergistics2"),
	@Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "cofhapi"),
	@Optional.Interface(iface = "blusunrize.immersiveengineering.api.tool.ITool", modid = "ImmersiveEngineering")
})
*/
public class WrenchItem extends Item //implements IAEWrench, IToolHammer, ITool
{
	public WrenchItem() {
		super(
			new Item.Properties()
				.maxStackSize(1)
				.group(SimpleFluidTanks.creativeTab)
		);

		setRegistryName(SimpleFluidTanks.WRENCH_ITEM_NAME);
		//setUnlocalizedName(SimpleFluidTanks.WRENCH_ITEM_NAME);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player)
	{
		return true;
	}

	/*
	// AE2
	@Override
	@Optional.Method(modid = "appliedenergistics2")
	public boolean canWrench(ItemStack wrench, EntityPlayer player, BlockPos pos)
	{
		return true;
	}

	// IE
	@Override
	@Optional.Method(modid = "ImmersiveEngineering")
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	// COFH
	@Override
	@Optional.Method(modid = "cofhapi")
	public boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos)
	{
		return true;
	}

	@Override
	@Optional.Method(modid = "cofhapi")
	public boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity)
	{
		return true;
	}

	@Override
	@Optional.Method(modid = "cofhapi")
	public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos)
	{
	}

	@Override
	@Optional.Method(modid = "cofhapi")
	public void toolUsed(ItemStack item, EntityLivingBase user, Entity entity)
	{
	}

	 */
}
