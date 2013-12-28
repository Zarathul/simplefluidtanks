package simplefluidtanks;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

@SideOnly(Side.CLIENT)
public class TankBlockRenderer extends TileEntitySpecialRenderer
{
	private final ResourceLocation[] textureLocations;
	
	public TankBlockRenderer()
	{
		super();
		
		textureLocations = new ResourceLocation[]
		{
			new ResourceLocation("simplefluidtanks", "/textures/blocks/brick.png"),
			new ResourceLocation("simplefluidtanks", "/textures/blocks/glass.png"),
			new ResourceLocation("/assets/simplefluidtanks/models/textures/fluidtank_glass.png"),
			new ResourceLocation("/assets/simplefluidtanks/models/textures/fluidtank_frame.png")
		};
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
	{
		Block block = tileEntity.getBlockType();
		
		if (block != null && block instanceof TankBlock)
		{
			Tessellator tsr = Tessellator.instance;
			int brightness = block.getMixedBrightnessForBlock(tileEntity.worldObj, (int)x, (int)y, (int)z);
			tsr.setBrightness(brightness);
//			tsr.startDrawingQuads();
			
			
//			this.bindTexture(textureLocations[0]);
			
			TessellationManager.setBaseCoords(x, y, z);
			TessellationManager.renderCube(0, 0, 0, 16, 16, 16, textureLocations[1], true);
//			TessellationManager.renderPositiveZFace(0, 0, 0, 16, 16, textureLocations[1]);
//			TessellationManager.renderNegativeZFace(0, 0, 16, 16, 16, textureLocations[1]);
//
//			TessellationManager.renderNegativeZFace(0, 0, 0, 16, 16, textureLocations[1]);
//			TessellationManager.renderPositiveZFace(0, 0, -16, 16, 16, textureLocations[1]);
//			
//			TessellationManager.renderPositiveXFace(0, 0, 0, 16, 16, textureLocations[1]);
//			TessellationManager.renderNegativeXFace(16, 0, 0, 16, 16, textureLocations[1]);
//			
//			TessellationManager.renderNegativeXFace(0, 0, 0, 16, 16, textureLocations[1]);
//			TessellationManager.renderPositiveXFace(-16, 0, 0, 16, 16, textureLocations[1]);
//			
//			TessellationManager.renderPositiveYFace(0, 0, 0, 16, 16, textureLocations[1]);
//			TessellationManager.renderNegativeYFace(0, 16, 0, 16, 16, textureLocations[1]);
//			
//			TessellationManager.renderNegativeYFace(0, 0, 0, 16, 16, textureLocations[1]);
//			TessellationManager.renderPositiveYFace(0, -16, 0, 16, 16, textureLocations[1]);
			
//			ArrayList<double[]> vertices = new ArrayList<double[]>(8);
//			// back side
//			vertices.add(new double[] { x, y, z });
//			vertices.add(new double[] { x + 16 * pixel, y, z });
//			vertices.add(new double[] { x + 16 * pixel, y + 16 * pixel, z });
//			vertices.add(new double[] { x, y + 16 * pixel, z });
//			// front side
//			vertices.add(new double[] { x, y, z + 16 * pixel });
//			vertices.add(new double[] { x + 16 * pixel, y, z + 16 * pixel });
//			vertices.add(new double[] { x + 16 * pixel, y + 16 * pixel, z + 16 * pixel });
//			vertices.add(new double[] { x, y + 16 * pixel, z + 16 * pixel });
			
			
//			// positive z face
//			// - bottom left
//			tsr.addVertexWithUV(x, y, z + 16 * pixel, 0, 1);
//			// - bottom right
//			tsr.addVertexWithUV(x + 16 * pixel, y, z + 16 * pixel, 1, 1);
//			// - top right
//			tsr.addVertexWithUV(x + 16 * pixel, y + 16 * pixel, z + 16 * pixel, 1, 0);
//			// - top left
//			tsr.addVertexWithUV(x, y + 16 * pixel, z + 16 * pixel, 0, 0);
			
//			tsr.draw();
//			tsr.startDrawingQuads();
//			
//			this.bindTexture(textureLocations[1]);
			
//			// negative z face
//			// - bottom right
//			tsr.addVertexWithUV(x, y, z, 1, 1);
//			// - top right
//			tsr.addVertexWithUV(x, y + 16 * pixel, z, 1, 0);
//			// - top left
//			tsr.addVertexWithUV(x + 16 * pixel, y + 16 * pixel, z, 0, 0);
//			// - bottom left
//			tsr.addVertexWithUV(x + 16 * pixel, y, z, 0, 1);
			
//			tsr.draw();
		}
	}
	
	private static double pixel = 1F / 16F;
}
