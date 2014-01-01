package simplefluidtanks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;

public final class TessellationManager
{
	// The max block width, height and depth is 1f, so we divide it by 16f to get 16 "subblocks" in every dimension 
	private static final double pixel = 1d / 16d;
	// Bandaid fix for the flickering issues with adjacent blocks
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
	
	public static void renderCube(double xOffset, double yOffset, double zOffset, double width, double height, double depth, Icon icon)
	{
		renderCube(xOffset, yOffset, zOffset, width, height, depth, icon, false);
	}
	
	public static void renderCube(double xOffset, double yOffset, double zOffset, double width, double height, double depth, Icon icon, boolean renderInside)
	{
		renderPositiveXFace(xOffset + width, yOffset, zOffset, height, depth, icon);
		renderNegativeXFace(xOffset, yOffset, zOffset, height, depth, icon);
		renderPositiveYFace(xOffset, yOffset + height, zOffset, width, depth, icon);
		renderNegativeYFace(xOffset, yOffset, zOffset, width, depth, icon);
		renderPositiveZFace(xOffset, yOffset, zOffset + depth, width, height, icon);
		renderNegativeZFace(xOffset, yOffset, zOffset, width, height, icon);
		
		if (renderInside)
		{
			// positive x back side
			renderNegativeXFace(xOffset + width, yOffset, zOffset, height, depth, icon);
			// negative x back side
			renderPositiveXFace(xOffset, yOffset, zOffset, height, depth, icon);
			// positive y back side
			renderNegativeYFace(xOffset, yOffset + height, zOffset, width, depth, icon);
			// negative y back side
			renderPositiveYFace(xOffset, yOffset, zOffset, width, depth, icon);
			// positive z back side
			renderNegativeZFace(xOffset, yOffset, zOffset + depth, width, height, icon);
			// negative back side
			renderPositiveZFace(xOffset, yOffset, zOffset, width, height, icon);
		}
	}
	
	public static void renderPositiveXFace(double xOffset, double yOffset, double zOffset, double height, double depth, Icon icon)
	{
		tr.setNormal(1f, 0f, 0f);
		
		double x = xBaseCoord + xOffset * pixel;

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
		
		// bottom right
		tr.addVertexWithUV(x, yBr, zBr, icon.getMaxU(), icon.getMaxV());
		// top right
		tr.addVertexWithUV(x, yTr, zTr, icon.getMaxU(), icon.getMinV());
		// top left
		tr.addVertexWithUV(x, yTl, zTl, icon.getMinU(), icon.getMinV());
		// bottom left
		tr.addVertexWithUV(x, yBl, zBl, icon.getMinU(), icon.getMaxV());
	}
	
	public static void renderNegativeXFace(double xOffset, double yOffset, double zOffset, double height, double depth, Icon icon)
	{
		tr.setNormal(-1f, 0f, 0f);

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
		
		// bottom left
		tr.addVertexWithUV(x, yBl, zBl, icon.getMinU(), icon.getMaxV());
		// bottom right
		tr.addVertexWithUV(x, yBr, zBr, icon.getMaxU(), icon.getMaxV());
		// top right
		tr.addVertexWithUV(x, yTr, zTr, icon.getMaxU(), icon.getMinV());
		// top left
		tr.addVertexWithUV(x, yTl, zTl, icon.getMinU(), icon.getMinV());
	}
	
	public static void renderPositiveYFace(double xOffset, double yOffset, double zOffset, double width, double depth, Icon icon)
	{
		tr.setNormal(0f, 1f, 0f);
		
		double y = yBaseCoord + yOffset * pixel;

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
		
		// bottom right
		tr.addVertexWithUV(xBr, y, zBr, icon.getMaxU(), icon.getMaxV());
		// top right
		tr.addVertexWithUV(xTr, y, zTr, icon.getMaxU(), icon.getMinV());
		// top left
		tr.addVertexWithUV(xTl, y, zTl, icon.getMinU(), icon.getMinV());
		// bottom left
		tr.addVertexWithUV(xBl, y, zBl, icon.getMinU(), icon.getMaxV());
	}
	
	public static void renderNegativeYFace(double xOffset, double yOffset, double zOffset, double width, double depth, Icon icon)
	{
		tr.setNormal(0f, -1f, 0f);
		
		double y = yBaseCoord + yOffset * pixel;
		
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
		
		// top right
		tr.addVertexWithUV(xTr, y, zTr, icon.getMaxU(), icon.getMinV());
		// top left
		tr.addVertexWithUV(xTl, y, zTl, icon.getMinU(), icon.getMinV());
		// bottom left
		tr.addVertexWithUV(xBl, y, zBl, icon.getMinU(), icon.getMaxV());
		// bottom right
		tr.addVertexWithUV(xBr, y, zBr, icon.getMaxU(), icon.getMaxV());
	}
	
	public static void renderPositiveZFace(double xOffset, double yOffset, double zOffset, double width, double height, Icon icon)
	{
		tr.setNormal(0f, 0f, 1f);
		
		double z = zBaseCoord + zOffset * pixel;
		
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
		
		// bottom left
		tr.addVertexWithUV(xBl, yBl, z, icon.getMinU(), icon.getMaxV());
		// bottom right
		tr.addVertexWithUV(xBr, yBr, z, icon.getMaxU(), icon.getMaxV());
		// top right
		tr.addVertexWithUV(xTr, yTr, z, icon.getMaxU(), icon.getMinV());
		// top left
		tr.addVertexWithUV(xTl, yTl, z, icon.getMinU(), icon.getMinV());
	}
	
	public static void renderNegativeZFace(double xOffset, double yOffset, double zOffset, double width, double height, Icon icon)
	{
		tr.setNormal(0f, 0f, -1f);
		
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
		
		// bottom right
		tr.addVertexWithUV(xBr, yBr, z, icon.getMaxU(), icon.getMaxV());
		// top right
		tr.addVertexWithUV(xTr, yTr, z, icon.getMaxU(), icon.getMinV());
		// top left
		tr.addVertexWithUV(xTl, yTl, z, icon.getMinU(), icon.getMinV());
		// bottom left
		tr.addVertexWithUV(xBl, yBl, z, icon.getMinU(), icon.getMaxV());
	}
}
