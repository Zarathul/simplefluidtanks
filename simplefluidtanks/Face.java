package simplefluidtanks;

import net.minecraftforge.client.model.obj.TextureCoordinate;

public class Face
{
	public final TextureCoordinate topLeft = new TextureCoordinate(0, 0);
	public final TextureCoordinate topRight = new TextureCoordinate(1, 0);
	public final TextureCoordinate bottomRight = new TextureCoordinate(1, 1);
	public final TextureCoordinate bottomLeft = new TextureCoordinate(0, 1);
	
	public void mirrorU()
	{
		// flip top left and bottom left texture coordinates
		float oldTopLeftU = topLeft.u;
		float oldTopleftV = topLeft.v;
		
		topLeft.u = bottomLeft.u;
		topLeft.v = bottomLeft.v;
		
		bottomLeft.u = oldTopLeftU;
		bottomLeft.v = oldTopleftV;
		
		// flip top right and bottom right texture coordinates
		float oldTopRightU = topRight.u;
		float oldTopRightV = topRight.v;
		
		topRight.u = bottomRight.u;
		topRight.v = bottomRight.v;
		
		bottomRight.u = oldTopRightU;
		bottomRight.v = oldTopRightV;
		
	}
	
	public void mirrorV()
	{
		// flip top left and top right texture coordinates
		float oldTopLeftU = topLeft.u;
		float oldTopleftV = topLeft.v;
		
		topLeft.u = topRight.u;
		topLeft.v = topRight.v;
		
		topRight.u = oldTopLeftU;
		topRight.v = oldTopleftV;
		
		// flip bottom left and bottom right texture coordinates
		float oldBottomLeftU = bottomLeft.u;
		float oldBottomLeftV = bottomLeft.v;
		
		bottomLeft.u = bottomRight.u;
		bottomLeft.v = bottomRight.v;
		
		bottomRight.u = oldBottomLeftU;
		bottomRight.v = oldBottomLeftV;
	}
	
	public void rotateRight()
	{
		float oldTopLeftU = topLeft.u;
		float oldTopleftV = topLeft.v;
		float oldTopRightU = topRight.u;
		float oldTopRightV = topRight.v;
		float oldBottomRightU = bottomRight.u;
		float oldBottomRightV = bottomRight.v;
		float oldBottomLeftU = bottomLeft.u;
		float oldBottomLeftV = bottomLeft.v;
		
		topLeft.u = oldBottomLeftU;
		topLeft.v = oldBottomLeftV;
		
		topRight.u = oldTopLeftU;
		topRight.v = oldTopleftV;
		
		bottomRight.u = oldTopRightU;
		bottomRight.v = oldTopRightV;
		
		bottomLeft.u = oldBottomRightU;
		bottomLeft.v = oldBottomRightV;
	}
}
