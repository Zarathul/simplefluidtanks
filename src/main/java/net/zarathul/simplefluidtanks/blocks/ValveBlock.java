package net.zarathul.simplefluidtanks.blocks;

import java.util.Random;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.Direction;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Represents a valve in the mods multiblock structure.
 */
public class ValveBlock extends WrenchableBlock
{
	public ValveBlock()
	{
		super(TankMaterial.tankMaterial);

		setBlockName(Registry.VALVEBLOCK_NAME);
		setCreativeTab(SimpleFluidTanks.creativeTab);
		setHardness(Config.valveBlockHardness);
		setResistance(Config.valveBlockResistance);
		setStepSound(soundTypeMetal);
		setHarvestLevel("pickaxe", 2);
	}

	@SideOnly(Side.CLIENT)
	private IIcon icon;
	@SideOnly(Side.CLIENT)
	private IIcon iconTank;
	@SideOnly(Side.CLIENT)
	private IIcon iconIo;

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		return (side == meta) ? iconIo : (side == Direction.YPOS) ? iconTank : icon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		ValveBlockEntity valveEntity = Utils.getTileEntityAt(blockAccess, ValveBlockEntity.class, x, y, z);

		if (valveEntity != null && valveEntity.hasTanks())
		{
			if (valveEntity.isFacingTank(side))
			{
				return iconTank;
			}

			return iconIo;
		}

		return getIcon(side, blockAccess.getBlockMetadata(x, y, z));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		icon = iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":valve");
		iconTank = iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":valve_tank");
		iconIo = iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":valve_io");
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
	{
		return new ValveBlockEntity();
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack items)
	{
		super.onBlockPlacedBy(world, x, y, z, player, items);

		if (!world.isRemote)
		{
			int l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			int direction;

			switch (l)
			{
				case 1:
					direction = Direction.XPOS;
					break;

				case 2:
					direction = Direction.ZPOS;
					break;

				case 3:
					direction = Direction.XNEG;
					break;

				default:
					direction = Direction.ZNEG;
					break;
			}

			world.setBlockMetadataWithNotify(x, y, z, direction, 2);
		}
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		super.onBlockAdded(world, x, y, z);

		if (!world.isRemote)
		{
			ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, x, y, z);

			if (valveEntity != null)
			{
				valveEntity.formMultiblock();
			}
		}
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int par5)
	{
		if (!world.isRemote)
		{
			ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, x, y, z);

			if (valveEntity != null)
			{
				valveEntity.disbandMultiblock();
			}
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
				// react to fluid containers
				if (equippedItemStack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(equippedItemStack))
				{
					handleContainerClick(world, x, y, z, player, equippedItemStack);

					return true;
				}
			}
		}

		return super.onBlockActivated(world, x, y, z, player, par6, par7, par8, par9);
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
		ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, x, y, z);

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
	protected void handleToolWrenchClick(World world, int x, int y, int z, EntityPlayer player, ItemStack equippedItemStack)
	{
		// on sneak use: disband the multiblock | on use: rebuild the multiblock

		ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, x, y, z);

		if (player.isSneaking())
		{
			if (valveEntity != null)
			{
				valveEntity.disbandMultiblock();
			}

			world.setBlockToAir(x, y, z);
			// last two parameters are metadata and fortune
			dropBlockAsItem(world, x, y, z, 0, 0);
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
	 * @param x
	 * The {@link ValveBlock}s x-coordinate.
	 * @param y
	 * The {@link ValveBlock}s y-coordinate.
	 * @param z
	 * The {@link ValveBlock}s z-coordinate.
	 * @param player
	 * The player using the item.
	 * @param equippedItemStack
	 * The item(stack) used on the {@link ValveBlock}.
	 */
	private void handleContainerClick(World world, int x, int y, int z, EntityPlayer player, ItemStack equippedItemStack)
	{
		ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, x, y, z);

		if (valveEntity != null)
		{
			if (FluidContainerRegistry.isEmptyContainer(equippedItemStack) ||
				Utils.isEmptyComplexContainer(equippedItemStack) ||
				(equippedItemStack.getItem() instanceof IFluidContainerItem && player.isSneaking()))
			{
				fillContainerFromTank(world, x, y, z, player, equippedItemStack, valveEntity);
			}
			else
			{
				drainContainerIntoTank(world, x, y, z, player, equippedItemStack, valveEntity);
			}
		}
	}

	/**
	 * Fills an empty container with the liquid contained in the multiblock tank.
	 * 
	 * @param world
	 * The world.
	 * @param x
	 * The {@link ValveBlock}s x-coordinate.
	 * @param y
	 * The {@link ValveBlock}s y-coordinate.
	 * @param z
	 * The {@link ValveBlock}s z-coordinate.
	 * @param player
	 * The player holding the container.
	 * @param equippedItemStack
	 * The container {@link ItemStack}.
	 * @param valveEntity
	 * The affected {@link ValveBlock}s {@link TileEntity} ({@link ValveBlockEntity}).
	 */
	private void fillContainerFromTank(World world, int x, int y, int z, EntityPlayer player, ItemStack equippedItemStack, ValveBlockEntity valveEntity)
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

						// add filled container to player inventory or drop it to the ground if the inventory is full

						if (!player.inventory.addItemStackToInventory(filledContainer))
						{
							world.spawnEntityInWorld(new EntityItem(world, x + 0.5D, y + 1.5D, z + 0.5D, filledContainer));
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
	 * @param x
	 * The {@link ValveBlock}s x-coordinate.
	 * @param y
	 * The {@link ValveBlock}s y-coordinate.
	 * @param z
	 * The {@link ValveBlock}s z-coordinate.
	 * @param player
	 * The player holding the container.
	 * @param equippedItemStack
	 * The container {@link ItemStack}.
	 * @param valveEntity
	 * The affected {@link ValveBlock}s {@link TileEntity} ({@link ValveBlockEntity}).
	 */
	private void drainContainerIntoTank(World world, int x, int y, int z, EntityPlayer player, ItemStack equippedItemStack, ValveBlockEntity valveEntity)
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
				ItemStack emptyContainer = Utils.getEmptyFluidContainer(equippedItemStack);

				if (--equippedItemStack.stackSize <= 0)
				{
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}

				// add emptied container to player inventory or drop it to the ground if the inventory is full

				if (!player.inventory.addItemStackToInventory(emptyContainer))
				{
					world.spawnEntityInWorld(new EntityItem(world, x + 0.5D, y + 1.5D, z + 0.5D, emptyContainer));
				}
				else if (player instanceof EntityPlayerMP)
				{
					((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
				}
			}
		}
	}
}
