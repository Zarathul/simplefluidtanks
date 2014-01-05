package simplefluidtanks;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Icon;

public final class TessellationManager
{
	// The max block width, height and depth is 1f, so we divide it by 16f to get 16 "subblocks" in every dimension 
	public static final double pixel = 1d / 16d;
	
	private static final Tessellator tr = Tessellator.instance;
	private static double xBaseCoord;
	private static double yBaseCoord;
	private static double zBaseCoord;
	
	private TessellationManager()
	{
	}
	
	public static void setBaseCoords(double ... coords)
	{
		if (coords != null && coords.length >= 3)
		{
			xBaseCoord = coords[0];
			yBaseCoord = coords[1];
			zBaseCoord = coords[2];
		}
	}
	
	public static void renderCube(double xOffset, double yOffset, double zOffset, double width, double height, double depth, Icon icon)
	{
		renderCube(xOffset, yOffset, zOffset, width, height, depth, icon, false, pixel);
	}
	
	public static void renderCube(double xOffset, double yOffset, double zOffset, double width, double height, double depth, Icon icon, boolean renderInside, double scale)
	{
		renderPositiveXFace(xOffset + width, yOffset, zOffset, height, depth, icon, scale);
		renderNegativeXFace(xOffset, yOffset, zOffset, height, depth, icon, scale);
		renderPositiveYFace(xOffset, yOffset + height, zOffset, width, depth, icon, scale);
		renderNegativeYFace(xOffset, yOffset, zOffset, width, depth, icon, scale);
		renderPositiveZFace(xOffset, yOffset, zOffset + depth, width, height, icon, scale);
		renderNegativeZFace(xOffset, yOffset, zOffset, width, height, icon, scale);
		
		if (renderInside)
		{
			// positive x back side
			renderNegativeXFace(xOffset + width, yOffset, zOffset, height, depth, icon, scale);
			// negative x back side
			renderPositiveXFace(xOffset, yOffset, zOffset, height, depth, icon, scale);
			// positive y back side
			renderNegativeYFace(xOffset, yOffset + height, zOffset, width, depth, icon, scale);
			// negative y back side
			renderPositiveYFace(xOffset, yOffset, zOffset, width, depth, icon, scale);
			// positive z back side
			renderNegativeZFace(xOffset, yOffset, zOffset + depth, width, height, icon, scale);
			// negative back side
			renderPositiveZFace(xOffset, yOffset, zOffset, width, height, icon, scale);
		}
	}
	
	public static void renderPositiveXFace(double xOffset, double yOffset, double zOffset, double height, double depth, Icon icon)
	{
		renderPositiveXFace(xOffset, yOffset, zOffset, height, depth, 0, 0, 0, 0, icon, pixel);
	}
	
	public static void renderPositiveXFace(double xOffset, double yOffset, double zOffset, double height, double depth, Icon icon, double scale)
	{
		renderPositiveXFace(xOffset, yOffset, zOffset, height, depth, 0, 0, 0, 0, icon, scale);
	}
	
	public static void renderPositiveXFace(double xOffset, double yOffset, double zOffset, double height, double depth, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, Icon icon, double scale)
	{
		tr.setNormal(1f, 0f, 0f);
		
		double x = xBaseCoord + xOffset * scale;

		// bottom right
		double zBr = zBaseCoord + zOffset * scale;
		double yBr = yBaseCoord + yOffset * scale;

		// top right
		double zTr = zBaseCoord + zOffset * scale;
		double yTr = yBaseCoord + (yOffset + height) * scale;

		// top left
		double zTl = zBaseCoord + (zOffset + depth) * scale;
		double yTl = yBaseCoord + (yOffset + height) * scale;
		
		// bottom left
		double zBl = zBaseCoord + (zOffset + depth) * scale;
		double yBl = yBaseCoord + yOffset * scale;
		
		double minU = (uOffset > 0 && uOffset < 16) ? icon.getMinU() + (icon.getInterpolatedU(uOffset) - icon.getMinU()) : icon.getMinU();
		double maxU = (uMaxOffset > 0 && uMaxOffset < 16) ? icon.getMaxU() - (icon.getMaxU() - icon.getInterpolatedU(uMaxOffset)) : icon.getMaxU();
		double minV = (vOffset > 0 && vOffset < 16) ? icon.getMinV() + (icon.getInterpolatedV(vOffset) - icon.getMinV()) : icon.getMinV();
		double maxV = (vMaxOffset > 0 && vMaxOffset < 16) ? icon.getMaxV() - (icon.getMaxV() - icon.getInterpolatedV(vMaxOffset)) : icon.getMaxV();
		
		// bottom right
		tr.addVertexWithUV(x, yBr, zBr, maxU, maxV);
		// top right
		tr.addVertexWithUV(x, yTr, zTr, maxU, minV);
		// top left
		tr.addVertexWithUV(x, yTl, zTl, minU, minV);
		// bottom left
		tr.addVertexWithUV(x, yBl, zBl, minU, maxV);
	}
	
	public static void renderNegativeXFace(double xOffset, double yOffset, double zOffset, double height, double depth, Icon icon)
	{
		renderNegativeXFace(xOffset, yOffset, zOffset, height, depth, 0, 0, 0, 0, icon, pixel);
	}
	
	public static void renderNegativeXFace(double xOffset, double yOffset, double zOffset, double height, double depth, Icon icon, double scale)
	{
		renderNegativeXFace(xOffset, yOffset, zOffset, height, depth, 0, 0, 0, 0, icon, scale);
	}
	
	public static void renderNegativeXFace(double xOffset, double yOffset, double zOffset, double height, double depth, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, Icon icon, double scale)
	{
		tr.setNormal(-1f, 0f, 0f);

		double x = xBaseCoord + xOffset * scale;
		
		// bottom left
		double zBl = zBaseCoord + zOffset * scale;
		double yBl = yBaseCoord + yOffset * scale;

		// bottom right
		double zBr = zBaseCoord + (zOffset + depth) * scale;
		double yBr = yBaseCoord + yOffset * scale;

		// top right
		double zTr = zBaseCoord + (zOffset + depth) * scale;
		double yTr = yBaseCoord + (yOffset + height) * scale;

		// top left
		double zTl = zBaseCoord + zOffset * scale;
		double yTl = yBaseCoord + (yOffset + height) * scale;
		
		double minU = (uOffset > 0 && uOffset < 16) ? icon.getMinU() + (icon.getInterpolatedU(uOffset) - icon.getMinU()) : icon.getMinU();
		double maxU = (uMaxOffset > 0 && uMaxOffset < 16) ? icon.getMaxU() - (icon.getMaxU() - icon.getInterpolatedU(uMaxOffset)) : icon.getMaxU();
		double minV = (vOffset > 0 && vOffset < 16) ? icon.getMinV() + (icon.getInterpolatedV(vOffset) - icon.getMinV()) : icon.getMinV();
		double maxV = (vMaxOffset > 0 && vMaxOffset < 16) ? icon.getMaxV() - (icon.getMaxV() - icon.getInterpolatedV(vMaxOffset)) : icon.getMaxV();
		
		// bottom left
		tr.addVertexWithUV(x, yBl, zBl, minU, maxV);
		// bottom right
		tr.addVertexWithUV(x, yBr, zBr, maxU, maxV);
		// top right
		tr.addVertexWithUV(x, yTr, zTr, maxU, minV);
		// top left
		tr.addVertexWithUV(x, yTl, zTl, minU, minV);
	}
	
	public static void renderPositiveYFace(double xOffset, double yOffset, double zOffset, double width, double depth, Icon icon)
	{
		renderPositiveYFace(xOffset, yOffset, zOffset, width, depth, 0, 0, 0, 0, icon, pixel);
	}
	
	public static void renderPositiveYFace(double xOffset, double yOffset, double zOffset, double width, double depth, Icon icon, double scale)
	{
		renderPositiveYFace(xOffset, yOffset, zOffset, width, depth, 0, 0, 0, 0, icon, scale);
	}
	
	public static void renderPositiveYFace(double xOffset, double yOffset, double zOffset, double width, double depth, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, Icon icon, double scale)
	{
		tr.setNormal(0f, 1f, 0f);
		
		double y = yBaseCoord + yOffset * scale;

		// bottom right
		double xBr = xBaseCoord + xOffset * scale;
		double zBr = zBaseCoord + zOffset * scale;

		// top right
		double xTr = xBaseCoord + xOffset * scale;
		double zTr = zBaseCoord + (zOffset + depth) * scale;
		
		// top left
		double xTl = xBaseCoord + (xOffset + width) * scale;
		double zTl = zBaseCoord + (zOffset + depth) * scale;
		
		// bottom left
		double xBl = xBaseCoord + (xOffset + width) * scale;
		double zBl = zBaseCoord + zOffset * scale;
		
		double minU = (uOffset > 0 && uOffset < 16) ? icon.getMinU() + (icon.getInterpolatedU(uOffset) - icon.getMinU()) : icon.getMinU();
		double maxU = (uMaxOffset > 0 && uMaxOffset < 16) ? icon.getMaxU() - (icon.getMaxU() - icon.getInterpolatedU(uMaxOffset)) : icon.getMaxU();
		double minV = (vOffset > 0 && vOffset < 16) ? icon.getMinV() + (icon.getInterpolatedV(vOffset) - icon.getMinV()) : icon.getMinV();
		double maxV = (vMaxOffset > 0 && vMaxOffset < 16) ? icon.getMaxV() - (icon.getMaxV() - icon.getInterpolatedV(vMaxOffset)) : icon.getMaxV();
		
		// bottom right
		tr.addVertexWithUV(xBr, y, zBr, maxU, maxV);
		// top right
		tr.addVertexWithUV(xTr, y, zTr, maxU, minV);
		// top left
		tr.addVertexWithUV(xTl, y, zTl, minU, minV);
		// bottom left
		tr.addVertexWithUV(xBl, y, zBl, minU, maxV);
	}
	
	public static void renderNegativeYFace(double xOffset, double yOffset, double zOffset, double width, double depth, Icon icon)
	{
		renderNegativeYFace(xOffset, yOffset, zOffset, width, depth, 0, 0, 0, 0, icon, pixel);
	}
	
	public static void renderNegativeYFace(double xOffset, double yOffset, double zOffset, double width, double depth, Icon icon, double scale)
	{
		renderNegativeYFace(xOffset, yOffset, zOffset, width, depth, 0, 0, 0, 0, icon, scale);
	}
	
	public static void renderNegativeYFace(double xOffset, double yOffset, double zOffset, double width, double depth, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, Icon icon, double scale)
	{
		tr.setNormal(0f, -1f, 0f);
		
		double y = yBaseCoord + yOffset * scale;
		
		// top right
		double xTr = xBaseCoord + xOffset * scale;
		double zTr = zBaseCoord + zOffset * scale;

		// top left
		double xTl = xBaseCoord + (xOffset + width) * scale;
		double zTl = zBaseCoord + zOffset * scale;
		
		// bottom left
		double xBl = xBaseCoord + (xOffset + width) * scale;
		double zBl = zBaseCoord + (zOffset + depth) * scale;

		// bottom right
		double xBr = xBaseCoord + xOffset * scale;
		double zBr = zBaseCoord + (zOffset + depth) * scale;
		
		double minU = (uOffset > 0 && uOffset < 16) ? icon.getMinU() + (icon.getInterpolatedU(uOffset) - icon.getMinU()) : icon.getMinU();
		double maxU = (uMaxOffset > 0 && uMaxOffset < 16) ? icon.getMaxU() - (icon.getMaxU() - icon.getInterpolatedU(uMaxOffset)) : icon.getMaxU();
		double minV = (vOffset > 0 && vOffset < 16) ? icon.getMinV() + (icon.getInterpolatedV(vOffset) - icon.getMinV()) : icon.getMinV();
		double maxV = (vMaxOffset > 0 && vMaxOffset < 16) ? icon.getMaxV() - (icon.getMaxV() - icon.getInterpolatedV(vMaxOffset)) : icon.getMaxV();
		
		// top right
		tr.addVertexWithUV(xTr, y, zTr, maxU, minV);
		// top left
		tr.addVertexWithUV(xTl, y, zTl, minU, minV);
		// bottom left
		tr.addVertexWithUV(xBl, y, zBl, minU, maxV);
		// bottom right
		tr.addVertexWithUV(xBr, y, zBr, maxU, maxV);
	}
	
	public static void renderPositiveZFace(double xOffset, double yOffset, double zOffset, double width, double height, Icon icon)
	{
		renderPositiveZFace(xOffset, yOffset, zOffset, width, height, 0, 0, 0, 0, icon, pixel);
	}
	
	public static void renderPositiveZFace(double xOffset, double yOffset, double zOffset, double width, double height, Icon icon, double scale)
	{
		renderPositiveZFace(xOffset, yOffset, zOffset, width, height, 0, 0, 0, 0, icon, scale);
	}
	
	public static void renderPositiveZFace(double xOffset, double yOffset, double zOffset, double width, double height, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, Icon icon, double scale)
	{
		tr.setNormal(0f, 0f, 1f);
		
		double z = zBaseCoord + zOffset * scale;
		
		// bottom left
		double xBl = xBaseCoord + xOffset * scale;
		double yBl = yBaseCoord + yOffset * scale;

		// bottom right
		double xBr = xBaseCoord + (xOffset + width) * scale;
		double yBr = yBaseCoord + yOffset * scale;

		// top right
		double xTr = xBaseCoord + (xOffset + width) * scale;
		double yTr = yBaseCoord + (yOffset + height) * scale;

		// top left
		double xTl = xBaseCoord + xOffset * scale;
		double yTl = yBaseCoord + (yOffset + height) * scale;
		
		double minU = (uOffset > 0 && uOffset < 16) ? icon.getMinU() + (icon.getInterpolatedU(uOffset) - icon.getMinU()) : icon.getMinU();
		double maxU = (uMaxOffset > 0 && uMaxOffset < 16) ? icon.getMaxU() - (icon.getMaxU() - icon.getInterpolatedU(uMaxOffset)) : icon.getMaxU();
		double minV = (vOffset > 0 && vOffset < 16) ? icon.getMinV() + (icon.getInterpolatedV(vOffset) - icon.getMinV()) : icon.getMinV();
		double maxV = (vMaxOffset > 0 && vMaxOffset < 16) ? icon.getMaxV() - (icon.getMaxV() - icon.getInterpolatedV(vMaxOffset)) : icon.getMaxV();
		
		// bottom left
		tr.addVertexWithUV(xBl, yBl, z, minU, maxV);
		// bottom right
		tr.addVertexWithUV(xBr, yBr, z, maxU, maxV);
		// top right
		tr.addVertexWithUV(xTr, yTr, z, maxU, minV);
		// top left
		tr.addVertexWithUV(xTl, yTl, z, minU, minV);
	}
	
	public static void renderNegativeZFace(double xOffset, double yOffset, double zOffset, double width, double height, Icon icon)
	{
		renderNegativeZFace(xOffset, yOffset, zOffset, width, height, 0, 0, 0, 0, icon, pixel);
	}
	
	public static void renderNegativeZFace(double xOffset, double yOffset, double zOffset, double width, double height, Icon icon, double scale)
	{
		renderNegativeZFace(xOffset, yOffset, zOffset, width, height, 0, 0, 0, 0, icon, scale);
	}
	
	public static void renderNegativeZFace(double xOffset, double yOffset, double zOffset, double width, double height, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, Icon icon, double scale)
	{
		tr.setNormal(0f, 0f, -1f);
		
		double z = zBaseCoord + zOffset * scale;

		// bottom right
		double xBr = xBaseCoord + xOffset * scale;
		double yBr = yBaseCoord + yOffset * scale;

		// top right
		double xTr = xBaseCoord + xOffset * scale;
		double yTr = yBaseCoord + (yOffset + height) * scale;

		// top left
		double xTl = xBaseCoord + (xOffset + width) * scale;
		double yTl = yBaseCoord + (yOffset + height) * scale;
		
		// bottom left
		double xBl = xBaseCoord + (xOffset + width) * scale;
		double yBl = yBaseCoord + yOffset * scale;
		
		double minU = (uOffset > 0 && uOffset < 16) ? icon.getMinU() + (icon.getInterpolatedU(uOffset) - icon.getMinU()) : icon.getMinU();
		double maxU = (uMaxOffset > 0 && uMaxOffset < 16) ? icon.getMaxU() - (icon.getMaxU() - icon.getInterpolatedU(uMaxOffset)) : icon.getMaxU();
		double minV = (vOffset > 0 && vOffset < 16) ? icon.getMinV() + (icon.getInterpolatedV(vOffset) - icon.getMinV()) : icon.getMinV();
		double maxV = (vMaxOffset > 0 && vMaxOffset < 16) ? icon.getMaxV() - (icon.getMaxV() - icon.getInterpolatedV(vMaxOffset)) : icon.getMaxV();
		
		// bottom right
		tr.addVertexWithUV(xBr, yBr, z, maxU, maxV);
		// top right
		tr.addVertexWithUV(xTr, yTr, z, maxU, minV);
		// top left
		tr.addVertexWithUV(xTl, yTl, z, minU, minV);
		// bottom left
		tr.addVertexWithUV(xBl, yBl, z, minU, maxV);
	}
}
