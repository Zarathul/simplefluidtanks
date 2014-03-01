package net.zarathul.simplefluidtanks.rendering;

import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.common.Direction;


/**
 * Provides helper methods to get texture indexes for connected {@link TankBlock}s.
 */
public final class ConnectedTexturesHelper
{
	public static int getPositiveXTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[Direction.YPOS] && connections[Direction.YNEG] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 1;
		}
		else if (connections[Direction.YPOS] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 8;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.YPOS] && connections[Direction.YNEG])
		{
			textureIndex = 11;
		}
		else if (connections[Direction.YNEG] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 10;
		}
		else if (connections[Direction.ZNEG] && connections[Direction.YNEG] && connections[Direction.YPOS])
		{
			textureIndex = 9;
		}
		else if (connections[Direction.YPOS] && connections[Direction.YNEG])
		{
			textureIndex = 2;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 3;
		}
		else if (connections[Direction.YPOS] && connections[Direction.ZPOS])
		{
			textureIndex = 7;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.YNEG])
		{
			textureIndex = 6;
		}
		else if (connections[Direction.YNEG] && connections[Direction.ZNEG])
		{
			textureIndex = 5;
		}
		else if (connections[Direction.ZNEG] && connections[Direction.YPOS])
		{
			textureIndex = 4;
		}
		else if (connections[Direction.YPOS])
		{
			textureIndex = 12;
		}
		else if (connections[Direction.YNEG])
		{
			textureIndex = 13;
		}
		else if (connections[Direction.ZPOS])
		{
			textureIndex = 14;
		}
		else if (connections[Direction.ZNEG])
		{
			textureIndex = 15;
		}
		
		return textureIndex;
	}
	
	public static int getNegativeXTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[Direction.YPOS] && connections[Direction.YNEG] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 1;
		}
		else if (connections[Direction.YPOS] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 8;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.YPOS] && connections[Direction.YNEG])
		{
			textureIndex = 9;
		}
		else if (connections[Direction.YNEG] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 10;
		}
		else if (connections[Direction.ZNEG] && connections[Direction.YNEG] && connections[Direction.YPOS])
		{
			textureIndex = 11;
		}
		else if (connections[Direction.YPOS] && connections[Direction.YNEG])
		{
			textureIndex = 2;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 3;
		}
		else if (connections[Direction.YPOS] && connections[Direction.ZPOS])
		{
			textureIndex = 4;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.YNEG])
		{
			textureIndex = 5;
		}
		else if (connections[Direction.YNEG] && connections[Direction.ZNEG])
		{
			textureIndex = 6;
		}
		else if (connections[Direction.ZNEG] && connections[Direction.YPOS])
		{
			textureIndex = 7;
		}
		else if (connections[Direction.YPOS])
		{
			textureIndex = 12;
		}
		else if (connections[Direction.YNEG])
		{
			textureIndex = 13;
		}
		else if (connections[Direction.ZPOS])
		{
			textureIndex = 15;
		}
		else if (connections[Direction.ZNEG])
		{
			textureIndex = 14;
		}
		
		return textureIndex;
	}
	
	public static int getPositiveZTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[Direction.YPOS] && connections[Direction.YNEG] && connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 1;
		}
		else if (connections[Direction.YPOS] && connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 8;
		}
		else if (connections[Direction.XPOS] && connections[Direction.YPOS] && connections[Direction.YNEG])
		{
			textureIndex = 9;
		}
		else if (connections[Direction.YNEG] && connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 10;
		}
		else if (connections[Direction.XNEG] && connections[Direction.YNEG] && connections[Direction.YPOS])
		{
			textureIndex = 11;
		}
		else if (connections[Direction.YPOS] && connections[Direction.YNEG])
		{
			textureIndex = 2;
		}
		else if (connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 3;
		}
		else if (connections[Direction.YPOS] && connections[Direction.XPOS])
		{
			textureIndex = 4;
		}
		else if (connections[Direction.XPOS] && connections[Direction.YNEG])
		{
			textureIndex = 5;
		}
		else if (connections[Direction.YNEG] && connections[Direction.XNEG])
		{
			textureIndex = 6;
		}
		else if (connections[Direction.XNEG] && connections[Direction.YPOS])
		{
			textureIndex = 7;
		}
		else if (connections[Direction.YPOS])
		{
			textureIndex = 12;
		}
		else if (connections[Direction.YNEG])
		{
			textureIndex = 13;
		}
		else if (connections[Direction.XPOS])
		{
			textureIndex = 15;
		}
		else if (connections[Direction.XNEG])
		{
			textureIndex = 14;
		}
		
		return textureIndex;
	}
	
	public static int getNegativeZTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[Direction.YPOS] && connections[Direction.YNEG] && connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 1;
		}
		else if (connections[Direction.YPOS] && connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 8;
		}
		else if (connections[Direction.XPOS] && connections[Direction.YPOS] && connections[Direction.YNEG])
		{
			textureIndex = 11;
		}
		else if (connections[Direction.YNEG] && connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 10;
		}
		else if (connections[Direction.XNEG] && connections[Direction.YNEG] && connections[Direction.YPOS])
		{
			textureIndex = 9;
		}
		else if (connections[Direction.YPOS] && connections[Direction.YNEG])
		{
			textureIndex = 2;
		}
		else if (connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 3;
		}
		else if (connections[Direction.YPOS] && connections[Direction.XPOS])
		{
			textureIndex = 7;
		}
		else if (connections[Direction.XPOS] && connections[Direction.YNEG])
		{
			textureIndex = 6;
		}
		else if (connections[Direction.YNEG] && connections[Direction.XNEG])
		{
			textureIndex = 5;
		}
		else if (connections[Direction.XNEG] && connections[Direction.YPOS])
		{
			textureIndex = 4;
		}
		else if (connections[Direction.YPOS])
		{
			textureIndex = 12;
		}
		else if (connections[Direction.YNEG])
		{
			textureIndex = 13;
		}
		else if (connections[Direction.XPOS])
		{
			textureIndex = 14;
		}
		else if (connections[Direction.XNEG])
		{
			textureIndex = 15;
		}
		
		return textureIndex;
	}
	
	public static int getPositiveYTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[Direction.XPOS] && connections[Direction.XNEG] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 1;
		}
		else if (connections[Direction.XPOS] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 11;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 8;
		}
		else if (connections[Direction.XNEG] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 9;
		}
		else if (connections[Direction.ZNEG] && connections[Direction.XNEG] && connections[Direction.XPOS])
		{
			textureIndex = 10;
		}
		else if (connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 3;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 2;
		}
		else if (connections[Direction.XPOS] && connections[Direction.ZPOS])
		{
			textureIndex = 7;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.XNEG])
		{
			textureIndex = 4;
		}
		else if (connections[Direction.XNEG] && connections[Direction.ZNEG])
		{
			textureIndex = 5;
		}
		else if (connections[Direction.ZNEG] && connections[Direction.XPOS])
		{
			textureIndex = 6;
		}
		else if (connections[Direction.XPOS])
		{
			textureIndex = 14;
		}
		else if (connections[Direction.XNEG])
		{
			textureIndex = 15;
		}
		else if (connections[Direction.ZPOS])
		{
			textureIndex = 12;
		}
		else if (connections[Direction.ZNEG])
		{
			textureIndex = 13;
		}
		
		return textureIndex;
	}
	
	public static int getNegativeYTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[Direction.XPOS] && connections[Direction.XNEG] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 1;
		}
		else if (connections[Direction.XPOS] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 11;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 10;
		}
		else if (connections[Direction.XNEG] && connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 9;
		}
		else if (connections[Direction.ZNEG] && connections[Direction.XNEG] && connections[Direction.XPOS])
		{
			textureIndex = 8;
		}
		else if (connections[Direction.XPOS] && connections[Direction.XNEG])
		{
			textureIndex = 3;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.ZNEG])
		{
			textureIndex = 2;
		}
		else if (connections[Direction.XPOS] && connections[Direction.ZPOS])
		{
			textureIndex = 6;
		}
		else if (connections[Direction.ZPOS] && connections[Direction.XNEG])
		{
			textureIndex = 5;
		}
		else if (connections[Direction.XNEG] && connections[Direction.ZNEG])
		{
			textureIndex = 4;
		}
		else if (connections[Direction.ZNEG] && connections[Direction.XPOS])
		{
			textureIndex = 7;
		}
		else if (connections[Direction.XPOS])
		{
			textureIndex = 14;
		}
		else if (connections[Direction.XNEG])
		{
			textureIndex = 15;
		}
		else if (connections[Direction.ZPOS])
		{
			textureIndex = 13;
		}
		else if (connections[Direction.ZNEG])
		{
			textureIndex = 12;
		}
		
		return textureIndex;
	}
}
