package simplefluidtanks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

public final class TessellationManager
{
	// The max block width, height and depth is 1f, so we divide it by 16f to get 16 "subblocks" in every dimension 
	private static final float pixel = 1f / 16f;
	// Bandaid fix for the flickering issues with adjacent blocks
	private static final double flickerOffset = 0.0001;
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
		renderPositiveXFace(xOffset + width, yOffset, zOffset, height, depth, texture, textureMapping.positiveX);
		renderNegativeXFace(xOffset, yOffset, zOffset, height, depth, texture, textureMapping.negativeX);
		renderPositiveYFace(xOffset, yOffset + height, zOffset, width, depth, texture, textureMapping.positiveY);
		renderNegativeYFace(xOffset, yOffset, zOffset, width, depth, texture, textureMapping.negativeY);
		renderPositiveZFace(xOffset, yOffset, zOffset + depth, width, height, texture, textureMapping.positiveZ);
		renderNegativeZFace(xOffset, yOffset, zOffset, width, height, texture, textureMapping.negativeZ);
		
		if (renderInside)
		{
			// positive x back side
			renderNegativeXFace(xOffset + width, yOffset, zOffset, height, depth, texture, textureMapping.negativeX);
			// negative x back side
			renderPositiveXFace(xOffset, yOffset, zOffset, height, depth, texture, textureMapping.positiveX);
			// positive y back side
			renderNegativeYFace(xOffset, yOffset + height, zOffset, width, depth, texture, textureMapping.negativeY);
			// negative y back side
			renderPositiveYFace(xOffset, yOffset, zOffset, width, depth, texture, textureMapping.positiveY);
			// positive z back side
			renderNegativeZFace(xOffset, yOffset, zOffset + depth, width, height, texture, textureMapping.negativeZ);
			// negative back side
			renderPositiveZFace(xOffset, yOffset, zOffset, width, height, texture, textureMapping.positiveZ);
		}
	}
	
	public static void renderPositiveXFace(int xOffset, int yOffset, int zOffset, int height, int depth, ResourceLocation texture)
	{
		renderPositiveXFace(xOffset, yOffset, zOffset, height, depth, texture, new UVTextureMapping().positiveX);
	}
	
	public static void renderPositiveXFace(int xOffset, int yOffset, int zOffset, int height, int depth, ResourceLocation texture, Face mapping)
	{
		double x = xBaseCoord + xOffset * pixel + flickerOffset;

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
		tr.addVertexWithUV(x, yBr, zBr, mapping.bottomRight.u, mapping.bottomRight.v);
		// top right
		tr.addVertexWithUV(x, yTr, zTr, mapping.topRight.u, mapping.topRight.v);
		// top left
		tr.addVertexWithUV(x, yTl, zTl, mapping.topLeft.u, mapping.topLeft.v);
		// bottom left
		tr.addVertexWithUV(x, yBl, zBl, mapping.bottomLeft.u, mapping.bottomLeft.v);
		
		tr.draw();
	}
	
	public static void renderNegativeXFace(int xOffset, int yOffset, int zOffset, int height, int depth, ResourceLocation texture)
	{
		renderNegativeXFace(xOffset, yOffset, zOffset, height, depth, texture, new UVTextureMapping().negativeX);
	}
	
	public static void renderNegativeXFace(int xOffset, int yOffset, int zOffset, int height, int depth, ResourceLocation texture, Face mapping)
	{
		double x = xBaseCoord + xOffset * pixel - flickerOffset;
		
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
		tr.addVertexWithUV(x, yBl, zBl, mapping.bottomLeft.u, mapping.bottomLeft.v);
		// bottom right
		tr.addVertexWithUV(x, yBr, zBr, mapping.bottomRight.u, mapping.bottomRight.v);
		// top right
		tr.addVertexWithUV(x, yTr, zTr, mapping.topRight.u, mapping.topRight.v);
		// top left
		tr.addVertexWithUV(x, yTl, zTl, mapping.topLeft.u, mapping.topLeft.v);
		
		tr.draw();
	}
	
	public static void renderPositiveYFace(int xOffset, int yOffset, int zOffset, int width, int depth, ResourceLocation texture)
	{
		renderPositiveYFace(xOffset, yOffset, zOffset, width, depth, texture, new UVTextureMapping().positiveY);
	}
	
	public static void renderPositiveYFace(int xOffset, int yOffset, int zOffset, int width, int depth, ResourceLocation texture, Face mapping)
	{
		double y = yBaseCoord + yOffset * pixel + flickerOffset;

		// bottom right
		double xBr = xBaseCoord + xOffset * pixel;
		double zBr = zBaseCoord + zOffset * pixel;

		// top right
		double xTr = xBaseCoord + xOffset * pixel;
		double zTr = zBaseCoord + (zOffset + depth) * pixel;
		
		// top left
		double xTl = xBaseCoord + (xOffset + width) * pixel;
		double zTl = zBaseCoord + (zOffset + depth) * pixel;
		
		// bottom left
		double xBl = xBaseCoord + (xOffset + width) * pixel;
		double zBl = zBaseCoord + zOffset * pixel;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		
		tr.startDrawingQuads();
		
		// bottom right
		tr.addVertexWithUV(xBr, y, zBr, mapping.bottomRight.u, mapping.bottomRight.v);
		// top right
		tr.addVertexWithUV(xTr, y, zTr, mapping.topRight.u, mapping.topRight.v);
		// top left
		tr.addVertexWithUV(xTl, y, zTl, mapping.topLeft.u, mapping.topLeft.v);
		// bottom left
		tr.addVertexWithUV(xBl, y, zBl, mapping.bottomLeft.u, mapping.bottomLeft.v);
		
		tr.draw();
	}
	
	public static void renderNegativeYFace(int xOffset, int yOffset, int zOffset, int width, int depth, ResourceLocation texture)
	{
		renderNegativeYFace(xOffset, yOffset, zOffset, width, depth, texture, new UVTextureMapping().negativeY);
	}
	
	public static void renderNegativeYFace(int xOffset, int yOffset, int zOffset, int width, int depth, ResourceLocation texture, Face mapping)
	{
		double y = yBaseCoord + yOffset * pixel - flickerOffset;
		
		// top right
		double xTr = xBaseCoord + xOffset * pixel;
		double zTr = zBaseCoord + zOffset * pixel;

		// top left
		double xTl = xBaseCoord + (xOffset + width) * pixel;
		double zTl = zBaseCoord + zOffset * pixel;
		
		// bottom left
		double xBl = xBaseCoord + (xOffset + width) * pixel;
		double zBl = zBaseCoord + (zOffset + depth) * pixel;

		// bottom right
		double xBr = xBaseCoord + xOffset * pixel;
		double zBr = zBaseCoord + (zOffset + depth) * pixel;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		
		tr.startDrawingQuads();
		
		// top right
		tr.addVertexWithUV(xTr, y, zTr, mapping.topRight.u, mapping.topRight.v);
		// top left
		tr.addVertexWithUV(xTl, y, zTl, mapping.topLeft.u, mapping.topLeft.v);
		// bottom left
		tr.addVertexWithUV(xBl, y, zBl, mapping.bottomLeft.u, mapping.bottomLeft.v);
		// bottom right
		tr.addVertexWithUV(xBr, y, zBr, mapping.bottomRight.u, mapping.bottomRight.v);
		
		tr.draw();
	}
	
	public static void renderPositiveZFace(int xOffset, int yOffset, int zOffset, int width, int height, ResourceLocation texture)
	{
		renderPositiveZFace(xOffset, yOffset, zOffset, width, height, texture, new UVTextureMapping().positiveZ);
	}
	
	public static void renderPositiveZFace(int xOffset, int yOffset, int zOffset, int width, int height, ResourceLocation texture, Face mapping)
	{
		double z = zBaseCoord + zOffset * pixel + flickerOffset;
		
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
		tr.addVertexWithUV(xBl, yBl, z, mapping.bottomLeft.u, mapping.bottomLeft.v);
		// bottom right
		tr.addVertexWithUV(xBr, yBr, z, mapping.bottomRight.u, mapping.bottomRight.v);
		// top right
		tr.addVertexWithUV(xTr, yTr, z, mapping.topRight.u, mapping.topRight.v);
		// top left
		tr.addVertexWithUV(xTl, yTl, z, mapping.topLeft.u, mapping.topLeft.v);
		
		tr.draw();
	}
	
	public static void renderNegativeZFace(int xOffset, int yOffset, int zOffset, int width, int height, ResourceLocation texture)
	{
		renderNegativeZFace(xOffset, yOffset, zOffset, width, height, texture, new UVTextureMapping().negativeZ);
	}
	
	public static void renderNegativeZFace(int xOffset, int yOffset, int zOffset, int width, int height, ResourceLocation texture, Face mapping)
	{
		double z = zBaseCoord + zOffset * pixel - flickerOffset;

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
		tr.addVertexWithUV(xBr, yBr, z, mapping.bottomRight.u, mapping.bottomRight.v);
		// top right
		tr.addVertexWithUV(xTr, yTr, z, mapping.topRight.u, mapping.topRight.v);
		// top left
		tr.addVertexWithUV(xTl, yTl, z, mapping.topLeft.u, mapping.topLeft.v);
		// bottom left
		tr.addVertexWithUV(xBl, yBl, z, mapping.bottomLeft.u, mapping.bottomLeft.v);
		
		tr.draw();
	}
}
