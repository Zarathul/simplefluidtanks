package simplefluidtanks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

public final class TessellationManager
{
	// The max block width, height and depth is 1f, so we divide it by 16f to get 16 "subblocks" in every dimension 
	private static final float pixel = 1f / 16f;
	private static final Tessellator tr = Tessellator.instance;
	
	private static double xBaseCoord;
	private static double yBaseCoord;
	private static double zBaseCoord;
	
	private TessellationManager()
	{
	}
	
	public static void setBaseCoords(double x, double y, double z)
	{
		xBaseCoord = x;
		yBaseCoord = y;
		zBaseCoord = z;
	}
	
	public static void renderCube(int xOffset, int yOffset, int zOffset, int width, int height, int depth, ResourceLocation texture)
	{
		renderCube(xOffset, yOffset, zOffset, width, height, depth, texture, false);
	}
	
	public static void renderCube(int xOffset, int yOffset, int zOffset, int width, int height, int depth, ResourceLocation texture, boolean renderInside)
	{
		renderCube(xOffset, yOffset, zOffset, width, height, depth, texture, new UVTextureMapping(), renderInside);
	}
	
	public static void renderCube(int xOffset, int yOffset, int zOffset, int width, int height, int depth, ResourceLocation texture, UVTextureMapping textureMapping, boolean renderInside)
	{
		renderPositiveZFace(0, 0, 0, 16, 16, texture, textureMapping);
		renderNegativeZFace(0, 0, 0, 16, 16, texture, textureMapping);
		renderPositiveXFace(0, 0, 0, 16, 16, texture, textureMapping);
		renderNegativeXFace(0, 0, 0, 16, 16, texture, textureMapping);
		renderPositiveYFace(0, 0, 0, 16, 16, texture, textureMapping);
		renderNegativeYFace(0, 0, 0, 16, 16, texture, textureMapping);
		
		if (renderInside)
		{
			renderNegativeZFace(0, 0, 16, 16, 16, texture, textureMapping);
			renderPositiveZFace(0, 0, -16, 16, 16, texture, textureMapping);
			renderNegativeXFace(16, 0, 0, 16, 16, texture, textureMapping);
			renderPositiveXFace(-16, 0, 0, 16, 16, texture, textureMapping);
			renderNegativeYFace(0, 16, 0, 16, 16, texture, textureMapping);
			renderPositiveYFace(0, -16, 0, 16, 16, texture, textureMapping);
		}
	}
	
	public static void renderPositiveXFace(int xOffset, int yOffset, int zOffset, int height, int depth, ResourceLocation texture)
	{
		renderPositiveXFace(xOffset, yOffset, zOffset, height, depth, texture, new UVTextureMapping());
	}
	
	public static void renderPositiveXFace(int xOffset, int yOffset, int zOffset, int height, int depth, ResourceLocation texture, UVTextureMapping textureMapping)
	{
		double x = xBaseCoord + (xOffset + 16) * pixel;

		// bottom right
		double zBr = zBaseCoord + zOffset * pixel;
		double yBr = yBaseCoord + yOffset * pixel;

		// top right
		double zTr = zBaseCoord + zOffset * pixel;
		double yTr = yBaseCoord + (yOffset + height) * pixel;

		// top left
		double zTl = zBaseCoord + (zOffset + depth) * pixel;
		double yTl = yBaseCoord + (yOffset + height) * pixel;
		
		// bottom left
		double zBl = zBaseCoord + (zOffset + depth) * pixel;
		double yBl = yBaseCoord + yOffset * pixel;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		
		tr.startDrawingQuads();
		
		// bottom right
		tr.addVertexWithUV(x, yBr, zBr, textureMapping.positiveX.bottomRight.u, textureMapping.positiveX.bottomRight.v);
		// top right
		tr.addVertexWithUV(x, yTr, zTr, textureMapping.positiveX.topRight.u, textureMapping.positiveX.topRight.v);
		// top left
		tr.addVertexWithUV(x, yTl, zTl, textureMapping.positiveX.topLeft.u, textureMapping.positiveX.topLeft.v);
		// bottom left
		tr.addVertexWithUV(x, yBl, zBl, textureMapping.positiveX.bottomLeft.u, textureMapping.positiveX.bottomLeft.v);
		
		tr.draw();
	}
	
	public static void renderNegativeXFace(int xOffset, int yOffset, int zOffset, int height, int depth, ResourceLocation texture)
	{
		renderNegativeXFace(xOffset, yOffset, zOffset, height, depth, texture, new UVTextureMapping());
	}
	
	public static void renderNegativeXFace(int xOffset, int yOffset, int zOffset, int height, int depth, ResourceLocation texture, UVTextureMapping textureMapping)
	{
		double x = xBaseCoord + xOffset * pixel;
		
		// bottom left
		double zBl = zBaseCoord + zOffset * pixel;
		double yBl = yBaseCoord + yOffset * pixel;

		// bottom right
		double zBr = zBaseCoord + (zOffset + depth) * pixel;
		double yBr = yBaseCoord + yOffset * pixel;

		// top right
		double zTr = zBaseCoord + (zOffset + depth) * pixel;
		double yTr = yBaseCoord + (yOffset + height) * pixel;

		// top left
		double zTl = zBaseCoord + zOffset * pixel;
		double yTl = yBaseCoord + (yOffset + height) * pixel;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		
		tr.startDrawingQuads();
		
		// bottom left
		tr.addVertexWithUV(x, yBl, zBl, textureMapping.negativeX.bottomLeft.u, textureMapping.negativeX.bottomLeft.v);
		// bottom right
		tr.addVertexWithUV(x, yBr, zBr, textureMapping.negativeX.bottomRight.u, textureMapping.negativeX.bottomRight.v);
		// top right
		tr.addVertexWithUV(x, yTr, zTr, textureMapping.negativeX.topRight.u, textureMapping.negativeX.topRight.v);
		// top left
		tr.addVertexWithUV(x, yTl, zTl, textureMapping.negativeX.topLeft.u, textureMapping.negativeX.topLeft.v);
		
		tr.draw();
	}
	
	public static void renderPositiveYFace(int xOffset, int yOffset, int zOffset, int width, int depth, ResourceLocation texture)
	{
		renderPositiveYFace(xOffset, yOffset, zOffset, width, depth, texture, new UVTextureMapping());
	}
	
	public static void renderPositiveYFace(int xOffset, int yOffset, int zOffset, int width, int depth, ResourceLocation texture, UVTextureMapping textureMapping)
	{
		double y = yBaseCoord + (yOffset + 16) * pixel;

		// top left
		double xTl = xBaseCoord + xOffset * pixel;
		double zTl = zBaseCoord + zOffset * pixel;
		
		// bottom left
		double xBl = xBaseCoord + xOffset * pixel;
		double zBl = zBaseCoord + (zOffset + depth) * pixel;

		// bottom right
		double xBr = xBaseCoord + (xOffset + width) * pixel;
		double zBr = zBaseCoord + (zOffset + depth) * pixel;

		// top right
		double xTr = xBaseCoord + (xOffset + width) * pixel;
		double zTr = zBaseCoord + zOffset * pixel;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		
		tr.startDrawingQuads();
		
		// top left
		tr.addVertexWithUV(xTl, y, zTl, textureMapping.positiveY.topLeft.u, textureMapping.positiveY.topLeft.v);
		// bottom left
		tr.addVertexWithUV(xBl, y, zBl, textureMapping.positiveY.bottomLeft.u, textureMapping.positiveY.bottomLeft.v);
		// bottom right
		tr.addVertexWithUV(xBr, y, zBr, textureMapping.positiveY.bottomRight.u, textureMapping.positiveY.bottomRight.v);
		// top right
		tr.addVertexWithUV(xTr, y, zTr, textureMapping.positiveY.topRight.u, textureMapping.positiveY.topRight.v);
		
		tr.draw();
	}
	
	public static void renderNegativeYFace(int xOffset, int yOffset, int zOffset, int width, int depth, ResourceLocation texture)
	{
		renderNegativeYFace(xOffset, yOffset, zOffset, width, depth, texture, new UVTextureMapping());
	}
	
	public static void renderNegativeYFace(int xOffset, int yOffset, int zOffset, int width, int depth, ResourceLocation texture, UVTextureMapping textureMapping)
	{
		double y = yBaseCoord + yOffset * pixel;
		
		// bottom left
		double xBl = xBaseCoord + xOffset * pixel;
		double zBl = zBaseCoord + zOffset * pixel;

		// bottom right
		double xBr = xBaseCoord + (xOffset + width) * pixel;
		double zBr = zBaseCoord + zOffset * pixel;

		// top right
		double xTr = xBaseCoord + (xOffset + width) * pixel;
		double zTr = zBaseCoord + (zOffset + depth) * pixel;

		// top left
		double xTl = xBaseCoord + xOffset * pixel;
		double zTl = zBaseCoord + (zOffset + depth) * pixel;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		
		tr.startDrawingQuads();
		
		// bottom left
		tr.addVertexWithUV(xBl, y, zBl, textureMapping.negativeY.bottomLeft.u, textureMapping.negativeY.bottomLeft.v);
		// bottom right
		tr.addVertexWithUV(xBr, y, zBr, textureMapping.negativeY.bottomRight.u, textureMapping.negativeY.bottomRight.v);
		// top right
		tr.addVertexWithUV(xTr, y, zTr, textureMapping.negativeY.topRight.u, textureMapping.negativeY.topRight.v);
		// top left
		tr.addVertexWithUV(xTl, y, zTl, textureMapping.negativeY.topLeft.u, textureMapping.negativeY.topLeft.v);
		
		tr.draw();
	}
	
	public static void renderPositiveZFace(int xOffset, int yOffset, int zOffset, int width, int height, ResourceLocation texture)
	{
		renderPositiveZFace(xOffset, yOffset, zOffset, width, height, texture, new UVTextureMapping());
	}
	
	public static void renderPositiveZFace(int xOffset, int yOffset, int zOffset, int width, int height, ResourceLocation texture, UVTextureMapping textureMapping)
	{
		double z = zBaseCoord + (zOffset + 16) * pixel;
		
		// bottom left
		double xBl = xBaseCoord + xOffset * pixel;
		double yBl = yBaseCoord + yOffset * pixel;

		// bottom right
		double xBr = xBaseCoord + (xOffset + width) * pixel;
		double yBr = yBaseCoord + yOffset * pixel;

		// top right
		double xTr = xBaseCoord + (xOffset + width) * pixel;
		double yTr = yBaseCoord + (yOffset + height) * pixel;

		// top left
		double xTl = xBaseCoord + xOffset * pixel;
		double yTl = yBaseCoord + (yOffset + height) * pixel;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		
		tr.startDrawingQuads();
		
		// bottom left
		tr.addVertexWithUV(xBl, yBl, z, textureMapping.positiveZ.bottomLeft.u, textureMapping.positiveZ.bottomLeft.v);
		// bottom right
		tr.addVertexWithUV(xBr, yBr, z, textureMapping.positiveZ.bottomRight.u, textureMapping.positiveZ.bottomRight.v);
		// top right
		tr.addVertexWithUV(xTr, yTr, z, textureMapping.positiveZ.topRight.u, textureMapping.positiveZ.topRight.v);
		// top left
		tr.addVertexWithUV(xTl, yTl, z, textureMapping.positiveZ.topLeft.u, textureMapping.positiveZ.topLeft.v);
		
		tr.draw();
	}
	
	public static void renderNegativeZFace(int xOffset, int yOffset, int zOffset, int width, int height, ResourceLocation texture)
	{
		renderNegativeZFace(xOffset, yOffset, zOffset, width, height, texture, new UVTextureMapping());
	}
	
	public static void renderNegativeZFace(int xOffset, int yOffset, int zOffset, int width, int height, ResourceLocation texture, UVTextureMapping textureMapping)
	{
		double z = zBaseCoord + zOffset * pixel;

		// bottom right
		double xBr = xBaseCoord + xOffset * pixel;
		double yBr = yBaseCoord + yOffset * pixel;

		// top right
		double xTr = xBaseCoord + xOffset * pixel;
		double yTr = yBaseCoord + (yOffset + height) * pixel;

		// top left
		double xTl = xBaseCoord + (xOffset + width) * pixel;
		double yTl = yBaseCoord + (yOffset + height) * pixel;
		
		// bottom left
		double xBl = xBaseCoord + (xOffset + width) * pixel;
		double yBl = yBaseCoord + yOffset * pixel;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		
		tr.startDrawingQuads();
		
		// bottom right
		tr.addVertexWithUV(xBr, yBr, z, textureMapping.negativeZ.bottomRight.u, textureMapping.negativeZ.bottomRight.v);
		// top right
		tr.addVertexWithUV(xTr, yTr, z, textureMapping.negativeZ.topRight.u, textureMapping.negativeZ.topRight.v);
		// top left
		tr.addVertexWithUV(xTl, yTl, z, textureMapping.negativeZ.topLeft.u, textureMapping.negativeZ.topLeft.v);
		// bottom left
		tr.addVertexWithUV(xBl, yBl, z, textureMapping.negativeZ.bottomLeft.u, textureMapping.negativeZ.bottomLeft.v);
		
		tr.draw();
	}
}
