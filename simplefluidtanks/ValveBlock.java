package simplefluidtanks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.io.ByteStreams;

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
		
		ArrayListMultimap<Integer, int[]> tanks = ArrayListMultimap.create();
		generateTankList(world, x, y, z, tanks);
		ValveBlockEntity entity = (ValveBlockEntity)world.getBlockTileEntity(x, y, z);
		entity.setTanks(tanks);
		
		ArrayList<TankBlockEntity> tankEntities = new ArrayList<TankBlockEntity>(tanks.size());
		
		for (Map.Entry<Integer, int[]> entry : tanks.entries())
		{
			int[] coords = entry.getValue();
			TankBlockEntity tankEntity = (TankBlockEntity)world.getBlockTileEntity(coords[0], coords[1], coords[2]);
			tankEntity.setValve(x, y, z);
			tankEntities.add(tankEntity);
		}
		
		for (TankBlockEntity t : tankEntities)
		{
			t.updateTextures();
		}
	}

	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int par5, EntityPlayer player)
	{
		super.onBlockHarvested(world, x, y, z, par5, player);
		
		ValveBlockEntity entity = (ValveBlockEntity)world.getBlockTileEntity(x, y, z);
		
		for (Map.Entry<Integer, int[]> entry : entity.getTanks().entries())
		{
			int[] coords = entry.getValue();
			TankBlockEntity tankEntity = (TankBlockEntity)world.getBlockTileEntity(coords[0], coords[1], coords[2]);
			tankEntity.reset();
		}
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
	
	private void generateTankList(World world, int x, int y, int z, ArrayListMultimap<Integer, int[]> tanks)
	{
		int xOffset = 0;
		int yOffset = 0;
		int zOffset = 0;
		
		// find a tank block directly connected to the valve 
		
		if (isValidTank(world, x, y, z + 1))
		{
			zOffset = 1;
		}
		else if (isValidTank(world, x, y, z - 1))
		{
			zOffset = -1;
		}
		else if (isValidTank(world, x + 1, y, z))
		{
			xOffset = 1;
		}
		else if (isValidTank(world, x - 1, y, z))
		{
			xOffset = -1;
		}
		else if (isValidTank(world, x, y + 1, z))
		{
			yOffset = 1;
		}
		else if (isValidTank(world, x, y - 1, z))
		{
			yOffset = -1;
		}
		else
		{
			return;
		}
		
		// find all other tank blocks connected to the first one - note that the flood find algo will not search vertically because it would mess up priority calculations
		ArrayListMultimap<Integer, int[]> foundTanks = floodFindTanks(world, x + xOffset, y + yOffset, z + zOffset, tanks, world.getActualHeight() - 1);
		
		do
		{
			ArrayListMultimap<Integer, int[]> newTanks = ArrayListMultimap.create();
			
			for (Map.Entry<Integer, int[]> entry : foundTanks.entries())
			{
				int priority = entry.getKey();
				int[] coords = entry.getValue();
				
				newTanks.putAll(floodFindTanks(world, coords[0], coords[1] - 1, coords[2], tanks, priority + 1));
				newTanks.putAll(floodFindTanks(world, coords[0], coords[1] + 1, coords[2], tanks, priority - world.getActualHeight()));
			}
			
			foundTanks = newTanks;
		}
		while (foundTanks.size() > 0);
		
		System.out.println();
	}
	
	private ArrayListMultimap<Integer, int[]> floodFindTanks(World world, int x, int y, int z, ArrayListMultimap<Integer, int[]> tanks, int priority)
	{
		if (!isValidTank(world, x, y, z))
		{
			return ArrayListMultimap.create();
		}
		
		int[] coords = new int[] { x, y, z };
		
		// containsValue() seems to check only reference equality, so we have to resort to this or add a superfluous value class
		for (int[] alreadyFoundCoords : tanks.values())
		{
			if (Arrays.equals(alreadyFoundCoords, coords))
			{
				return ArrayListMultimap.create();
			}
		}
		
		tanks.put(priority, coords);
		
		System.out.printf("Prio: %d - %d/%d/%d", priority, coords[0], coords[1], coords[2]);
		System.out.println();
		
		ArrayListMultimap<Integer, int[]> newTanks = ArrayListMultimap.create();
		newTanks.put(priority, coords);
		 
		newTanks.putAll(floodFindTanks(world, x + 1, y, z, tanks, priority));
		newTanks.putAll(floodFindTanks(world, x - 1, y, z, tanks, priority));
		newTanks.putAll(floodFindTanks(world, x, y, z + 1, tanks, priority));
		newTanks.putAll(floodFindTanks(world, x, y, z - 1, tanks, priority));
		
		return newTanks;
	}
	
	private boolean isValidTank(World world, int x, int y, int z)
	{
		if (world.getBlockId(x, y, z) == SimpleFluidTanks.tankBlockId)
		{
			TankBlockEntity tankEntity = (TankBlockEntity)world.getBlockTileEntity(x, y, z);
			
			return !tankEntity.isPartOfTank();
		}
		
		return false;
	}
}
