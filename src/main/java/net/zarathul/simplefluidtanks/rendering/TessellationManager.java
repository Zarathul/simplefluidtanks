package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

/**
 * Wraps minecrafts {@link TessellationManager} to make it a tiny bit more user friendly.
 */
public final class TessellationManager
{
	/**
	 * The max block width, height and depth is 1f, so we divide it by 16f to get 16 "subblocks" in every dimension
	 */
	public static final double pixel = 1d / 16d;
	
	/**
	 * Reference to the {@link Tessellator} instance.
	 */
	private static final Tessellator tr = Tessellator.instance;
	
	/**
	 * The base x-coordinate all methods are working on.
	 */
	private static double xBaseCoord = 0.0;
	
	/**
	 * The base y-coordinate all methods are working on.
	 */
	private static double yBaseCoord = 0.0;
	
	/**
	 * The base z-coordinate all methods are working on.
	 */
	private static double zBaseCoord = 0.0;
	
	/**
	 * Default constructor.
	 */
	private TessellationManager()
	{
	}
	
	/**
	 * Set the base coordinates.
	 * @param coords
	 * The new base coordinates. 
	 */
	public static void setBaseCoords(double ... coords)
	{
		if (coords != null && coords.length >= 3)
		{
			xBaseCoord = coords[0];
			yBaseCoord = coords[1];
			zBaseCoord = coords[2];
		}
	}
	
	/**
	 * Renders a cube without the inside and the offsets scaled by <code>TessellationManager.pixel</code>.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The cubes width.
	 * @param height
	 * The cubes height.
	 * @param depth
	 * The cubes depth.
	 * @param icon
	 * The cubes texture.
	 */
	public static void renderCube(double xOffset, double yOffset, double zOffset, double width, double height, double depth, IIcon icon)
	{
		renderCube(xOffset, yOffset, zOffset, width, height, depth, icon, false, pixel);
	}
	
	/**
	 * Renders a cube.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The cubes width.
	 * @param height
	 * The cubes height.
	 * @param depth
	 * The cubes depth.
	 * @param icon
	 * The cubes texture.
	 * @param renderInside
	 * <code>true</code> if the inside should be rendered, otherwise <code>false</code>.
	 * @param scale
	 * The factor that is used to scale the offsets.
	 */
	public static void renderCube(double xOffset, double yOffset, double zOffset, double width, double height, double depth, IIcon icon, boolean renderInside, double scale)
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
	
	/**
	 * Renders the face on the positive x side with offsets scaled by <code>TessellationManager.pixel</code>
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param height
	 * The faces height.
	 * @param depth
	 * The faces depth.
	 * @param icon
	 * The faces texture.
	 */
	public static void renderPositiveXFace(double xOffset, double yOffset, double zOffset, double height, double depth, IIcon icon)
	{
		renderPositiveXFace(xOffset, yOffset, zOffset, height, depth, 0, 0, 0, 0, icon, pixel);
	}
	
	/**
	 * Renders the face on the positive x side.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param height
	 * The faces height.
	 * @param depth
	 * The faces depth.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderPositiveXFace(double xOffset, double yOffset, double zOffset, double height, double depth, IIcon icon, double scale)
	{
		renderPositiveXFace(xOffset, yOffset, zOffset, height, depth, 0, 0, 0, 0, icon, scale);
	}
	
	/**
	 * Renders the face on the positive x side with optional texture offsets.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param height
	 * The faces height.
	 * @param depth
	 * The faces depth.
	 * @param uOffset
	 * The texture offset on the u-axis.
	 * @param vOffset
	 * The texture offset on the v-axis.
	 * @param uMaxOffset
	 * The max texture offset on the u-axis.
	 * @param vMaxOffset
	 * The max texture offset on the v-axis.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderPositiveXFace(double xOffset, double yOffset, double zOffset, double height, double depth, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, IIcon icon, double scale)
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
	
	/**
	 * Renders the face on the negative x side with offsets scaled by <code>TessellationManager.pixel</code>
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param height
	 * The faces height.
	 * @param depth
	 * The faces depth.
	 * @param icon
	 * The faces texture.
	 */
	public static void renderNegativeXFace(double xOffset, double yOffset, double zOffset, double height, double depth, IIcon icon)
	{
		renderNegativeXFace(xOffset, yOffset, zOffset, height, depth, 0, 0, 0, 0, icon, pixel);
	}
	
	/**
	 * Renders the face on the negative x side.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param height
	 * The faces height.
	 * @param depth
	 * The faces depth.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderNegativeXFace(double xOffset, double yOffset, double zOffset, double height, double depth, IIcon icon, double scale)
	{
		renderNegativeXFace(xOffset, yOffset, zOffset, height, depth, 0, 0, 0, 0, icon, scale);
	}
	
	/**
	 * Renders the face on the negative x side with optional texture offsets.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param height
	 * The faces height.
	 * @param depth
	 * The faces depth.
	 * @param uOffset
	 * The texture offset on the u-axis.
	 * @param vOffset
	 * The texture offset on the v-axis.
	 * @param uMaxOffset
	 * The max texture offset on the u-axis.
	 * @param vMaxOffset
	 * The max texture offset on the v-axis.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderNegativeXFace(double xOffset, double yOffset, double zOffset, double height, double depth, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, IIcon icon, double scale)
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
	
	/**
	 * Renders the face on the positive y side with offsets scaled by <code>TessellationManager.pixel</code>
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param depth
	 * The faces depth.
	 * @param icon
	 * The faces texture.
	 */
	public static void renderPositiveYFace(double xOffset, double yOffset, double zOffset, double width, double depth, IIcon icon)
	{
		renderPositiveYFace(xOffset, yOffset, zOffset, width, depth, 0, 0, 0, 0, icon, pixel);
	}
	
	/**
	 * Renders the face on the positive y side.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param depth
	 * The faces depth.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderPositiveYFace(double xOffset, double yOffset, double zOffset, double width, double depth, IIcon icon, double scale)
	{
		renderPositiveYFace(xOffset, yOffset, zOffset, width, depth, 0, 0, 0, 0, icon, scale);
	}
	
	/**
	 * Renders the face on the positive y side with optional texture offsets.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param depth
	 * The faces depth.
	 * @param uOffset
	 * The texture offset on the u-axis.
	 * @param vOffset
	 * The texture offset on the v-axis.
	 * @param uMaxOffset
	 * The max texture offset on the u-axis.
	 * @param vMaxOffset
	 * The max texture offset on the v-axis.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderPositiveYFace(double xOffset, double yOffset, double zOffset, double width, double depth, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, IIcon icon, double scale)
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
	
	/**
	 * Renders the face on the negative y side with offsets scaled by <code>TessellationManager.pixel</code>
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param depth
	 * The faces depth.
	 * @param icon
	 * The faces texture.
	 */
	public static void renderNegativeYFace(double xOffset, double yOffset, double zOffset, double width, double depth, IIcon icon)
	{
		renderNegativeYFace(xOffset, yOffset, zOffset, width, depth, 0, 0, 0, 0, icon, pixel);
	}
	
	/**
	 * Renders the face on the negative y side.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param depth
	 * The faces depth.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderNegativeYFace(double xOffset, double yOffset, double zOffset, double width, double depth, IIcon icon, double scale)
	{
		renderNegativeYFace(xOffset, yOffset, zOffset, width, depth, 0, 0, 0, 0, icon, scale);
	}
	
	/**
	 * Renders the face on the negative y side with optional texture offsets.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param depth
	 * The faces depth.
	 * @param uOffset
	 * The texture offset on the u-axis.
	 * @param vOffset
	 * The texture offset on the v-axis.
	 * @param uMaxOffset
	 * The max texture offset on the u-axis.
	 * @param vMaxOffset
	 * The max texture offset on the v-axis.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderNegativeYFace(double xOffset, double yOffset, double zOffset, double width, double depth, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, IIcon icon, double scale)
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
	
	/**
	 * Renders the face on the positive z side with offsets scaled by <code>TessellationManager.pixel</code>
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param height
	 * The faces height.
	 * @param icon
	 * The faces texture.
	 */
	public static void renderPositiveZFace(double xOffset, double yOffset, double zOffset, double width, double height, IIcon icon)
	{
		renderPositiveZFace(xOffset, yOffset, zOffset, width, height, 0, 0, 0, 0, icon, pixel);
	}
	
	/**
	 * Renders the face on the positive z side.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param height
	 * The faces height.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderPositiveZFace(double xOffset, double yOffset, double zOffset, double width, double height, IIcon icon, double scale)
	{
		renderPositiveZFace(xOffset, yOffset, zOffset, width, height, 0, 0, 0, 0, icon, scale);
	}
	
	/**
	 * Renders the face on the positive z side with optional texture offsets.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param height
	 * The faces height.
	 * @param uOffset
	 * The texture offset on the u-axis.
	 * @param vOffset
	 * The texture offset on the v-axis.
	 * @param uMaxOffset
	 * The max texture offset on the u-axis.
	 * @param vMaxOffset
	 * The max texture offset on the v-axis.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderPositiveZFace(double xOffset, double yOffset, double zOffset, double width, double height, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, IIcon icon, double scale)
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
	
	/**
	 * Renders the face on the negative z side with offsets scaled by <code>TessellationManager.pixel</code>
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param height
	 * The faces height.
	 * @param icon
	 * The faces texture.
	 */
	public static void renderNegativeZFace(double xOffset, double yOffset, double zOffset, double width, double height, IIcon icon)
	{
		renderNegativeZFace(xOffset, yOffset, zOffset, width, height, 0, 0, 0, 0, icon, pixel);
	}
	
	/**
	 * Renders the face on the negative z side.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param height
	 * The faces height.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderNegativeZFace(double xOffset, double yOffset, double zOffset, double width, double height, IIcon icon, double scale)
	{
		renderNegativeZFace(xOffset, yOffset, zOffset, width, height, 0, 0, 0, 0, icon, scale);
	}
	
	/**
	 * Renders the face on the negative z side with optional texture offsets.
	 * @param xOffset
	 * The offset on the x-axis.
	 * @param yOffset
	 * The offset on the y-axis.
	 * @param zOffset
	 * The offset on the z-axis.
	 * @param width
	 * The faces width.
	 * @param height
	 * The faces height.
	 * @param uOffset
	 * The texture offset on the u-axis.
	 * @param vOffset
	 * The texture offset on the v-axis.
	 * @param uMaxOffset
	 * The max texture offset on the u-axis.
	 * @param vMaxOffset
	 * The max texture offset on the v-axis.
	 * @param icon
	 * The faces texture.
	 * @param scale
	 * The value to scale the offsets with.
	 */
	public static void renderNegativeZFace(double xOffset, double yOffset, double zOffset, double width, double height, double uOffset, double vOffset, double uMaxOffset, double vMaxOffset, IIcon icon, double scale)
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
