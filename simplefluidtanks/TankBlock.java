package simplefluidtanks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class TankBlock extends BlockContainer
{
	public TankBlock(int blockId)
	{
		super(blockId, Material.iron);
		
		this.setUnlocalizedName(SimpleFluidTanks.REGISTRY_TANKBLOCK_NAME);
		this.setCreativeTab(SimpleFluidTanks.creativeTab);
		this.setHardness(2.0f);
		this.setStepSound(soundGlassFootstep);
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
//		icon = iconRegister.registerIcon(SimpleFluidTanks.TEXTURE_LOCATION + ":" + SimpleFluidTanks.TEXTURE_TANKBLOCK);
	}
	
	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TankBlockEntity();
	}
}
