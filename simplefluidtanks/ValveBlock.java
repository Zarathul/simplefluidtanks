package simplefluidtanks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidContainerItem;

public class ValveBlock extends BlockContainer
{
	public ValveBlock(int blockId)
	{
		super(blockId, TankMaterial.tankMaterial);
		
		this.setUnlocalizedName(SimpleFluidTanks.REGISTRY_VALVEBLOCK_NAME);
		this.setCreativeTab(SimpleFluidTanks.creativeTab);
		this.setHardness(1.5f);
		this.setStepSound(soundMetalFootstep);
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
		System.out.println("onAdded");
	}

	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int par5, EntityPlayer player)
	{
		super.onBlockHarvested(world, x, y, z, par5, player);
		System.out.println("onHarvested");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		if (!world.isRemote)
		{
			ItemStack equippedItemStack = player.getCurrentEquippedItem();
			
			if (equippedItemStack != null)
			{
				if (equippedItemStack.itemID == Item.stick.itemID)
				{
					handleStickClick(world, x, y, z, player);
				}
				else if (FluidContainerRegistry.isContainer(equippedItemStack))
				{
					handleContainerClick(world, x, y, z, player, equippedItemStack);
				}
			}
		}
		
		return true;
	}
	
	private void handleStickClick(World world, int x, int y, int z, EntityPlayer player)
	{
		TileEntity blockEntity = world.getBlockTileEntity(x, y, z);

		if (blockEntity != null && blockEntity instanceof ValveBlockEntity)
		{
			ValveBlockEntity tankEntity = (ValveBlockEntity) blockEntity;
			String containerStatus = tankEntity.getFluidAmount() + "/" + tankEntity.getCapacity() + " (" + ((tankEntity.getFluidAmount() > 0) ? tankEntity.getFluid().getFluid().getLocalizedName() : "empty") + ")";
			// TODO: remove debug chat message
			player.sendChatToPlayer(new ChatMessageComponent().addText(containerStatus));
		}
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

			String containerStatus = tankEntity.getFluidAmount() + "/" + tankEntity.getCapacity() + " (" + ((tankEntity.getFluidAmount() > 0) ? tankEntity.getFluid().getFluid().getLocalizedName() : "empty") + ")";
			// TODO: remove debug chat message
			player.sendChatToPlayer(new ChatMessageComponent().addText(containerStatus));
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
}
