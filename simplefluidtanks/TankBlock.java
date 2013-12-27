package simplefluidtanks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class TankBlock extends Block
{
	public TankBlock(int blockId)
	{
		super(blockId, Material.iron);
		
		this.setUnlocalizedName(SimpleFluidTanks.REGISTRY_TANKBLOCK_NAME);
		this.setCreativeTab(SimpleFluidTanks.creativeTab);
		this.setHardness(2.0f);
		this.setStepSound(soundMetalFootstep);
	}
	
	@SideOnly(Side.CLIENT)
	private Icon icon;

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		return icon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		icon = iconRegister.registerIcon(SimpleFluidTanks.TEXTURE_LOCATION + ":" + SimpleFluidTanks.TEXTURE_TANKBLOCK);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass()
	{
		return 1;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return SimpleFluidTanks.TANKBLOCK_RENDERER_ID;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderAsNormalBlock()
	{
		return false;
	}
}
