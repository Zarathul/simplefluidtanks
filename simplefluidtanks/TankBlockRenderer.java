package simplefluidtanks;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

@SideOnly(Side.CLIENT)
public class TankBlockRenderer implements ISimpleBlockRenderingHandler
{
//	private static final String MODEL_RESOURCE_NAME = "/assets/simplefluidtanks/models/fluidtank.tcn";
//	private static final ResourceLocation MODE_TEXTURE_LOCATION = new ResourceLocation("/assets/simplefluidtanks/models/textures/fluidtank.png");
	
	private IModelCustom model;
	private int renderId;
	
	public TankBlockRenderer()
	{
//		model = AdvancedModelLoader.loadModel(MODEL_RESOURCE_NAME);
		renderId = SimpleFluidTanks.TANKBLOCK_RENDERER_ID;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		int brightness = block.getMixedBrightnessForBlock(world, x, y, z);
		Tessellator.instance.setBrightness(brightness);
		EasyTess.renderCube(x, y, z, 16, 16, 16, 0, 0, block.getIcon(0, 0), 0);
//		EasyTess.renderCube(x, y, z, 16, 16, 16, 0, 0, block.getIcon(0, 0), 0);
//		Tessellator t = Tessellator.instance;
//		t.draw();
//		t.startDrawingQuads();
//		t.draw();
//		t.startDrawingQuads();
//		model.renderAll();
//		Minecraft.getMinecraft().renderEngine.bindTexture(MODE_TEXTURE_LOCATION);
//		GL11.glPushMatrix();
//		GL11.glTranslatef((float)x + 0.5f, (float)y + 1.5f, (float)z + 0.5f);
//		GL11.glRotatef(180, 0f, 0f, 1f);
//		GL11.glPopMatrix();

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory()
	{
		return false;
	}

	@Override
	public int getRenderId()
	{
		return this.renderId; 
	}
}
