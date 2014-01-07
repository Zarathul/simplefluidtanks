package simplefluidtanks;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ValveBlock extends BlockContainer
{
	public ValveBlock(int blockId)
	{
		super(blockId, TankMaterial.tankMaterial);
		
		setUnlocalizedName(SimpleFluidTanks.REGISTRY_VALVEBLOCK_NAME);
		setCreativeTab(SimpleFluidTanks.creativeTab);
		setHardness(2.5f);
		setStepSound(soundMetalFootstep);
	}

	@SideOnly(Side.CLIENT)
	private Icon icon;
	@SideOnly(Side.CLIENT)
	private Icon iconIn;
	@SideOnly(Side.CLIENT)
	private Icon iconOut;

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		switch (side)
		{
			case ConnectedTexturesHelper.YPOS:
				return iconIn;
			case ConnectedTexturesHelper.XNEG:
				return iconOut;
			default:
				return icon;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		TileEntity entity = blockAccess.getBlockTileEntity(x, y, z);
		
		if (entity != null)
		{
			ValveBlockEntity valveEntity = (ValveBlockEntity)entity;
			
			if (valveEntity.hasTanks())
			{
				if (valveEntity.isInputSide(side))
				{
					return iconIn;
				}
				
				return iconOut;
			}
		}
		
		return getIcon(side, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		icon = iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":valve");
		iconIn = iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":valve_in");
		iconOut = iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":valve_out");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new ValveBlockEntity();
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		super.onBlockAdded(world, x, y, z);
		
		if (!world.isRemote)
		{
			ValveBlockEntity entity = (ValveBlockEntity)world.getBlockTileEntity(x, y, z);
			entity.findTanks();
			entity.updateInputSides();
		}
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int par5)
	{
		resetTanks(world, x, y, z);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		if (!world.isRemote)
		{
			ItemStack equippedItemStack = player.getCurrentEquippedItem();
			
			if (equippedItemStack != null)
			{
				if (FluidContainerRegistry.isContainer(equippedItemStack))
				{
					handleContainerClick(world, x, y, z, player, equippedItemStack);
				}
			}
		}
		
		return true;
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
	public int quantityDropped(int meta, int fortune, Random random)
	{
		return 1;
	}
	
	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int side)
	{
		TileEntity entity = world.getBlockTileEntity(x, y, z);
		
		if (entity != null && entity instanceof ValveBlockEntity)
		{
			ValveBlockEntity valve = (ValveBlockEntity)entity;
			float capacity = (float)valve.getCapacity();
			float fluidAmount = (float)valve.getFluidAmount();
			int signalStrength = ((int)Math.floor((fluidAmount / capacity)  * 14.0f)) + ((fluidAmount > 0) ? 1 : 0);
			
			return signalStrength;
		}
		
		return 0;
	}

	private void handleContainerClick(World world, int x, int y, int z, EntityPlayer player, ItemStack equippedItemStack)
	{
		TileEntity blockEntity = world.getBlockTileEntity(x, y, z);

		if (blockEntity != null	&& blockEntity instanceof ValveBlockEntity)
		{
			ValveBlockEntity tankEntity = (ValveBlockEntity) blockEntity;
			
			// only deal with buckets, all other fluid containers need to use pipes
			if (FluidContainerRegistry.isBucket(equippedItemStack))
			{
				if (FluidContainerRegistry.isEmptyContainer(equippedItemStack))
				{
					fillBucketFromTank(world, x, y, z, player, equippedItemStack, tankEntity);
				}
				else
				{
					drainBucketIntoTank(tankEntity, player, equippedItemStack);
				}
			}
		}
	}

	private void fillBucketFromTank(World world, int x, int y, int z, EntityPlayer player, ItemStack equippedItemStack, ValveBlockEntity tankEntity)
	{
		// fill empty bucket with liquid from the tank if it has stored enough
		if (tankEntity.getFluidAmount() >= FluidContainerRegistry.BUCKET_VOLUME)
		{
			FluidStack oneBucketOfFluid = new FluidStack(tankEntity.getFluid(), FluidContainerRegistry.BUCKET_VOLUME);
			ItemStack filledBucket = FluidContainerRegistry.fillFluidContainer(oneBucketOfFluid, FluidContainerRegistry.EMPTY_BUCKET);
			
			if (filledBucket != null && tankEntity.drain(null, oneBucketOfFluid, true).amount == FluidContainerRegistry.BUCKET_VOLUME)
			{
				// add filled bucket to player inventory or drop it to the ground if the inventory is full
	            if (!player.inventory.addItemStackToInventory(filledBucket))
	            {
	                world.spawnEntityInWorld(new EntityItem(world, (double)x + 0.5D, (double)y + 1.5D, (double)z + 0.5D, filledBucket));
	            }
	            else if (player instanceof EntityPlayerMP)
	            {
	                ((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
	            }
			}

            if (--equippedItemStack.stackSize <= 0)
            {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack)null);
            }
		}
	}
	
	private void drainBucketIntoTank(ValveBlockEntity tankEntity, EntityPlayer player, ItemStack equippedItemStack)
	{
		// fill the liquid from the bucket into the tank
		if ((tankEntity.getFluidAmount() == 0 || tankEntity.getFluid().isFluidEqual(equippedItemStack)) && tankEntity.getCapacity() - tankEntity.getFluidAmount() >= FluidContainerRegistry.BUCKET_VOLUME)
		{
			FluidStack fluidFromBucket = FluidContainerRegistry.getFluidForFilledItem(equippedItemStack);
			
			if (tankEntity.fill(null, fluidFromBucket, true) == FluidContainerRegistry.BUCKET_VOLUME)
			{
				// don't consume the filled bucket in creative mode 
                if (!player.capabilities.isCreativeMode)
                {
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(Item.bucketEmpty));
                }
			}
		}
	}
	
	private void resetTanks(World world, int x, int y, int z)
	{
		if (!world.isRemote)
		{
			ValveBlockEntity entity = (ValveBlockEntity)world.getBlockTileEntity(x, y, z);
			entity.resetTanks();
		}
	}
}
