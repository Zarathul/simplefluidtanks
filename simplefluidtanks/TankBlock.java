package simplefluidtanks;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TankBlock extends BlockContainer
{
	public TankBlock(int blockId)
	{
		super(blockId, TankMaterial.tankMaterial);
		
		setUnlocalizedName(SimpleFluidTanks.REGISTRY_TANKBLOCK_NAME);
		setCreativeTab(SimpleFluidTanks.creativeTab);
		setHardness(2.5f);
		setStepSound(soundGlassFootstep);
	}

	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	@SideOnly(Side.CLIENT)
	public Icon[] getIcons()
	{
		return icons;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		return icons[0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		icons = new Icon[]
		{
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_closed"),				//  0
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_open"),					//  1

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_bottom"),			//  2
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_left_right"),			//  3

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_right"),				//  4
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_bottom_right"),			//  5
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_bottom_left"),			//  6
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_left"),				//  7

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_left_right_top"),		//  8
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_bottom_right"),		//  9
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_left_right_bottom"),		// 10
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_bottom_left"),		// 11
				
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top"),					// 12
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_bottom"),				// 13
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_left"),					// 14
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_right")					// 15
		};
	}
	// TODO: remove debug code
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		if (!world.isRemote)
		{
			ItemStack equippedItemStack = player.getCurrentEquippedItem();
			
			if (equippedItemStack != null && equippedItemStack.itemID == Item.stick.itemID)
			{
				TankBlockEntity tank = (TankBlockEntity)world.getBlockTileEntity(x, y, z);
				ValveBlockEntity valve = tank.getValve();
				int[] coords = new int[] { x, y, z };
				
				for (Map.Entry<Float, int[]> entry : valve.tanks.entries())
				{
					if (Arrays.equals(coords, entry.getValue()))
					{
						player.addChatMessage(String.format("%d/%d/%d - %f", x, y, z, entry.getKey()));
						break;
					}
				}
			}
		}
		
		return super.onBlockActivated(world, x, y, z, player, par6, par7, par8, par9);
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int par5)
	{
		resetTanks(world, x, y, z);
	}

	@Override
	public int quantityDropped(Random par1Random)
	{
		return 1;
	}

	@Override
	public float getExplosionResistance(Entity par1Entity)
	{
		return 1000f;
	}

	@Override
	public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ)
	{
		return 1000f;
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
	public boolean canRenderInPass(int pass)
	{
		return (pass == 1);
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
	
	private void resetTanks(World world, int x, int y, int z)
	{
		if (!world.isRemote)
		{
			TankBlockEntity tankEntity = (TankBlockEntity)world.getBlockTileEntity(x, y, z);
			ValveBlockEntity valveEntity = tankEntity.getValve();
			
			if (valveEntity != null)
			{
				valveEntity.resetTanks();
			}
		}
	}
}
