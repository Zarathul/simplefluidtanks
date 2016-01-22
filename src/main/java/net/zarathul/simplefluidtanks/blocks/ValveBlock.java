package net.zarathul.simplefluidtanks.blocks;

import java.util.Random;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Represents a valve in the mods multiblock structure.
 */
public class ValveBlock extends WrenchableBlock
{
	public static final PropertyInteger DOWN = PropertyInteger.create("down", 0, 2);
	public static final PropertyInteger UP = PropertyInteger.create("up", 0, 2);
	public static final PropertyInteger NORTH = PropertyInteger.create("north", 0, 2);
	public static final PropertyInteger SOUTH = PropertyInteger.create("south", 0, 2);
	public static final PropertyInteger WEST = PropertyInteger.create("west", 0, 2);
	public static final PropertyInteger EAST = PropertyInteger.create("east", 0, 2);
	
	private static final int GRATE_TEXTURE_ID = 0;
	private static final int IO_TEXTURE_ID = 1;
	private static final int TANK_TEXTURE_ID = 2;

	public ValveBlock()
	{
		super(TankMaterial.tankMaterial);

		setUnlocalizedName(Registry.VALVE_BLOCK_NAME);
		setCreativeTab(SimpleFluidTanks.creativeTab);
		setHardness(Config.valveBlockHardness);
		setResistance(Config.valveBlockResistance);
		setStepSound(soundTypeMetal);
		setHarvestLevel("pickaxe", 2);
		
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(UP, GRATE_TEXTURE_ID)
				.withProperty(NORTH, IO_TEXTURE_ID));
	}

	@Override
	public int getRenderType()
	{
		return 3;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
	{
		return new ValveBlockEntity();
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, DOWN, UP, NORTH, SOUTH, WEST, EAST);
	}
	
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return 0;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, pos);
		
		if (valveEntity != null)
		{
			if (valveEntity.hasTanks())
			{
				state = state.withProperty(DOWN, (valveEntity.isFacingTank(EnumFacing.DOWN)) ? GRATE_TEXTURE_ID : IO_TEXTURE_ID)
						.withProperty(UP, (valveEntity.isFacingTank(EnumFacing.UP)) ? GRATE_TEXTURE_ID : IO_TEXTURE_ID)
						.withProperty(NORTH, (valveEntity.isFacingTank(EnumFacing.NORTH)) ? GRATE_TEXTURE_ID : IO_TEXTURE_ID)
						.withProperty(SOUTH, (valveEntity.isFacingTank(EnumFacing.SOUTH)) ? GRATE_TEXTURE_ID : IO_TEXTURE_ID)
						.withProperty(WEST, (valveEntity.isFacingTank(EnumFacing.WEST)) ? GRATE_TEXTURE_ID : IO_TEXTURE_ID)
						.withProperty(EAST, (valveEntity.isFacingTank(EnumFacing.EAST)) ? GRATE_TEXTURE_ID : IO_TEXTURE_ID);
			}
			else
			{
				EnumFacing facing = valveEntity.getFacing();
				
				state = state.withProperty(DOWN, TANK_TEXTURE_ID)
						.withProperty(UP, GRATE_TEXTURE_ID)
						.withProperty(NORTH, (facing == EnumFacing.NORTH) ? IO_TEXTURE_ID : TANK_TEXTURE_ID)
						.withProperty(SOUTH, (facing == EnumFacing.SOUTH) ? IO_TEXTURE_ID : TANK_TEXTURE_ID)
						.withProperty(WEST, (facing == EnumFacing.WEST) ? IO_TEXTURE_ID : TANK_TEXTURE_ID)
						.withProperty(EAST, (facing == EnumFacing.EAST) ? IO_TEXTURE_ID : TANK_TEXTURE_ID);
			}
		}
		
		return state;
	}

	@Override
	public boolean requiresUpdates()
	{
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack items)
	{
		super.onBlockPlacedBy(world, pos, state, placer, items);
		
		if (!world.isRemote)
		{
			EnumFacing facing = placer.getHorizontalFacing().getOpposite();
			
			ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, pos);
			
			if (valveEntity != null)
			{
				valveEntity.setFacing(facing);
				world.markChunkDirty(pos, valveEntity);
				world.markBlockForUpdate(pos);
			}
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		if (!world.isRemote)
		{
			ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, pos);

			if (valveEntity != null)
			{
				valveEntity.formMultiblock();
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			ItemStack equippedItemStack = player.getCurrentEquippedItem();

			if (equippedItemStack != null)
			{
				// react to fluid containers
				if (equippedItemStack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(equippedItemStack))
				{
					handleContainerClick(world, pos, player, equippedItemStack);
					
					return true;
				}
			}
		}
		
		return super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random)
	{
		return 1;
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, BlockPos pos)
	{
		ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, pos);

		if (valveEntity != null)
		{
			float capacity = valveEntity.getCapacity();
			float fluidAmount = valveEntity.getFluidAmount();
			int signalStrength = (capacity != 0) ? ((int) Math.floor((fluidAmount / capacity) * 14.0f)) + ((fluidAmount > 0) ? 1 : 0) : 0;

			return signalStrength;
		}

		return 0;
	}

	@Override
	protected void handleToolWrenchClick(World world, BlockPos pos, EntityPlayer player, ItemStack equippedItemStack)
	{
		// on sneak use: disband the multiblock | on use: rebuild the multiblock

		ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, pos);

		if (player.isSneaking())
		{
			if (valveEntity != null)
			{
				valveEntity.disbandMultiblock();
			}

			world.setBlockToAir(pos);
			// last two parameters are metadata and fortune
			dropBlockAsItem(world, pos, this.getDefaultState(), 0);
		}
		else if (valveEntity != null)
		{
			// rebuild the tank
			valveEntity.formMultiblock();
		}
	}

	/**
	 * Handles fluid containers used on the {@link ValveBlock}.
	 * 
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link ValveBlock}s coordinates.
	 * @param player
	 * The player using the item.
	 * @param equippedItemStack
	 * The item(stack) used on the {@link ValveBlock}.
	 */
	private void handleContainerClick(World world, BlockPos pos, EntityPlayer player, ItemStack equippedItemStack)
	{
		ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, pos);

		if (valveEntity != null)
		{
			if (FluidContainerRegistry.isEmptyContainer(equippedItemStack) ||
				Utils.isEmptyComplexContainer(equippedItemStack) ||
				(equippedItemStack.getItem() instanceof IFluidContainerItem && player.isSneaking()))
			{
				fillContainerFromTank(world, pos, player, equippedItemStack, valveEntity);
			}
			else
			{
				drainContainerIntoTank(world, pos, player, equippedItemStack, valveEntity);
			}
		}
	}

	/**
	 * Fills an empty container with the liquid contained in the multiblock tank.
	 * 
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link ValveBlock}s coordinates.
	 * @param player
	 * The player holding the container.
	 * @param equippedItemStack
	 * The container {@link ItemStack}.
	 * @param valveEntity
	 * The affected {@link ValveBlock}s {@link TileEntity} ({@link ValveBlockEntity}).
	 */
	private void fillContainerFromTank(World world, BlockPos pos, EntityPlayer player, ItemStack equippedItemStack, ValveBlockEntity valveEntity)
	{
		if (valveEntity.getFluid() == null) return;

		if (equippedItemStack.getItem() instanceof IFluidContainerItem)
		{
			// handle IFluidContainerItem items

			IFluidContainerItem containerItem = (IFluidContainerItem) equippedItemStack.getItem();
			int fillFluidAmount = containerItem.fill(equippedItemStack, valveEntity.getFluid(), true);
			valveEntity.drain(null, fillFluidAmount, true);
		}
		else
		{
			// handle drain/fill by exchange items

			ItemStack filledContainer = Utils.fillFluidContainer(valveEntity.getFluid(), equippedItemStack);

			if (filledContainer != null)
			{
				int containerCapacity = Utils.getFluidContainerCapacity(valveEntity.getFluid(), equippedItemStack);

				if (containerCapacity > 0)
				{
					FluidStack drainedFluid = valveEntity.drain(null, containerCapacity, true);
					if (drainedFluid != null && drainedFluid.amount == containerCapacity)
					{
						if (--equippedItemStack.stackSize <= 0)
						{
							player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
						}

						// add filled container to player inventory or drop it to the ground if the inventory is full or we're dealing with a fake player

						if (player instanceof FakePlayer || !player.inventory.addItemStackToInventory(filledContainer))
						{
							world.spawnEntityInWorld(new EntityItem(world, player.posX + 0.5D, player.posY + 1.5D, player.posZ + 0.5D, filledContainer));
						}
						else if (player instanceof EntityPlayerMP)
						{
							((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
						}
					}
				}
			}
		}
	}

	/**
	 * Drains the contents of a container into the multiblock tank.
	 * 
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link ValveBlock}s coordinates.
	 * @param player
	 * The player holding the container.
	 * @param equippedItemStack
	 * The container {@link ItemStack}.
	 * @param valveEntity
	 * The affected {@link ValveBlock}s {@link TileEntity} ({@link ValveBlockEntity}).
	 */
	private void drainContainerIntoTank(World world, BlockPos pos, EntityPlayer player, ItemStack equippedItemStack, ValveBlockEntity valveEntity)
	{
		if (valveEntity.isFull()) return;

		if (equippedItemStack.getItem() instanceof IFluidContainerItem)
		{
			// handle IFluidContainerItem items

			IFluidContainerItem containerItem = (IFluidContainerItem) equippedItemStack.getItem();
			FluidStack containerFluid = containerItem.getFluid(equippedItemStack);
			FluidStack tankFluid = valveEntity.getFluid();

			if (tankFluid == null || tankFluid.isFluidEqual(containerFluid))
			{
				int drainAmount = Math.min(valveEntity.getRemainingCapacity(), containerFluid.amount);
				// drain the fluid from the container first because the amount per drain could be limited
				FluidStack drainFluid = containerItem.drain(equippedItemStack, drainAmount, true);
				valveEntity.fill(null, drainFluid, true);
			}
		}
		else
		{
			// handle drain/fill by exchange items

			FluidStack containerFluid = Utils.getFluidForFilledItem(equippedItemStack);

			if (valveEntity.fill(null, containerFluid, true) > 0 && !player.capabilities.isCreativeMode) // don't consume the container contents in creative mode
			{
				ItemStack emptyContainer = FluidContainerRegistry.drainFluidContainer(equippedItemStack);

				if (--equippedItemStack.stackSize <= 0)
				{
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}

				// add emptied container to player inventory or drop it to the ground if the inventory is full or we're dealing with a fake player

				if (player instanceof FakePlayer || !player.inventory.addItemStackToInventory(emptyContainer))
				{
					world.spawnEntityInWorld(new EntityItem(world, player.posX + 0.5D, player.posY + 1.5D, player.posZ + 0.5D, emptyContainer));
				}
				else if (player instanceof EntityPlayerMP)
				{
					((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
				}
			}
		}
	}
}
