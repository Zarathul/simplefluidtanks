package simplefluidtanks;

import java.util.HashMap;

public final class ConnectedTexturesHelper
{
	public static final int XPOS = 3;
	public static final int XNEG = 1;
	public static final int YPOS = 5;
	public static final int YNEG = 4;
	public static final int ZPOS = 0;
	public static final int ZNEG = 2;
	
	
	public static int getPositiveXTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[YPOS] && connections[YNEG] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 1;
		}
		else if (connections[YPOS] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 8;
		}
		else if (connections[ZPOS] && connections[YPOS] && connections[YNEG])
		{
			textureIndex = 11;
		}
		else if (connections[YNEG] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 10;
		}
		else if (connections[ZNEG] && connections[YNEG] && connections[YPOS])
		{
			textureIndex = 9;
		}
		else if (connections[YPOS] && connections[YNEG])
		{
			textureIndex = 2;
		}
		else if (connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 3;
		}
		else if (connections[YPOS] && connections[ZPOS])
		{
			textureIndex = 7;
		}
		else if (connections[ZPOS] && connections[YNEG])
		{
			textureIndex = 6;
		}
		else if (connections[YNEG] && connections[ZNEG])
		{
			textureIndex = 5;
		}
		else if (connections[ZNEG] && connections[YPOS])
		{
			textureIndex = 4;
		}
		else if (connections[YPOS])
		{
			textureIndex = 12;
		}
		else if (connections[YNEG])
		{
			textureIndex = 13;
		}
		else if (connections[ZPOS])
		{
			textureIndex = 14;
		}
		else if (connections[ZNEG])
		{
			textureIndex = 15;
		}
		
		return textureIndex;
	}
	
	public static int getNegativeXTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[YPOS] && connections[YNEG] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 1;
		}
		else if (connections[YPOS] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 8;
		}
		else if (connections[ZPOS] && connections[YPOS] && connections[YNEG])
		{
			textureIndex = 9;
		}
		else if (connections[YNEG] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 10;
		}
		else if (connections[ZNEG] && connections[YNEG] && connections[YPOS])
		{
			textureIndex = 11;
		}
		else if (connections[YPOS] && connections[YNEG])
		{
			textureIndex = 2;
		}
		else if (connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 3;
		}
		else if (connections[YPOS] && connections[ZPOS])
		{
			textureIndex = 4;
		}
		else if (connections[ZPOS] && connections[YNEG])
		{
			textureIndex = 5;
		}
		else if (connections[YNEG] && connections[ZNEG])
		{
			textureIndex = 6;
		}
		else if (connections[ZNEG] && connections[YPOS])
		{
			textureIndex = 7;
		}
		else if (connections[YPOS])
		{
			textureIndex = 12;
		}
		else if (connections[YNEG])
		{
			textureIndex = 13;
		}
		else if (connections[ZPOS])
		{
			textureIndex = 15;
		}
		else if (connections[ZNEG])
		{
			textureIndex = 14;
		}
		
		return textureIndex;
	}
	
	public static int getPositiveZTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[YPOS] && connections[YNEG] && connections[XPOS] && connections[XNEG])
		{
			textureIndex = 1;
		}
		else if (connections[YPOS] && connections[XPOS] && connections[XNEG])
		{
			textureIndex = 8;
		}
		else if (connections[XPOS] && connections[YPOS] && connections[YNEG])
		{
			textureIndex = 9;
		}
		else if (connections[YNEG] && connections[XPOS] && connections[XNEG])
		{
			textureIndex = 10;
		}
		else if (connections[XNEG] && connections[YNEG] && connections[YPOS])
		{
			textureIndex = 11;
		}
		else if (connections[YPOS] && connections[YNEG])
		{
			textureIndex = 2;
		}
		else if (connections[XPOS] && connections[XNEG])
		{
			textureIndex = 3;
		}
		else if (connections[YPOS] && connections[XPOS])
		{
			textureIndex = 4;
		}
		else if (connections[XPOS] && connections[YNEG])
		{
			textureIndex = 5;
		}
		else if (connections[YNEG] && connections[XNEG])
		{
			textureIndex = 6;
		}
		else if (connections[XNEG] && connections[YPOS])
		{
			textureIndex = 7;
		}
		else if (connections[YPOS])
		{
			textureIndex = 12;
		}
		else if (connections[YNEG])
		{
			textureIndex = 13;
		}
		else if (connections[XPOS])
		{
			textureIndex = 15;
		}
		else if (connections[XNEG])
		{
			textureIndex = 14;
		}
		
		return textureIndex;
	}
	
	public static int getNegativeZTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[YPOS] && connections[YNEG] && connections[XPOS] && connections[XNEG])
		{
			textureIndex = 1;
		}
		else if (connections[YPOS] && connections[XPOS] && connections[XNEG])
		{
			textureIndex = 8;
		}
		else if (connections[XPOS] && connections[YPOS] && connections[YNEG])
		{
			textureIndex = 11;
		}
		else if (connections[YNEG] && connections[XPOS] && connections[XNEG])
		{
			textureIndex = 10;
		}
		else if (connections[XNEG] && connections[YNEG] && connections[YPOS])
		{
			textureIndex = 9;
		}
		else if (connections[YPOS] && connections[YNEG])
		{
			textureIndex = 2;
		}
		else if (connections[XPOS] && connections[XNEG])
		{
			textureIndex = 3;
		}
		else if (connections[YPOS] && connections[XPOS])
		{
			textureIndex = 7;
		}
		else if (connections[XPOS] && connections[YNEG])
		{
			textureIndex = 6;
		}
		else if (connections[YNEG] && connections[XNEG])
		{
			textureIndex = 5;
		}
		else if (connections[XNEG] && connections[YPOS])
		{
			textureIndex = 4;
		}
		else if (connections[YPOS])
		{
			textureIndex = 12;
		}
		else if (connections[YNEG])
		{
			textureIndex = 13;
		}
		else if (connections[XPOS])
		{
			textureIndex = 14;
		}
		else if (connections[XNEG])
		{
			textureIndex = 15;
		}
		
		return textureIndex;
	}
	
	public static int getPositiveYTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[XPOS] && connections[XNEG] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 1;
		}
		else if (connections[XPOS] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 11;
		}
		else if (connections[ZPOS] && connections[XPOS] && connections[XNEG])
		{
			textureIndex = 8;
		}
		else if (connections[XNEG] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 9;
		}
		else if (connections[ZNEG] && connections[XNEG] && connections[XPOS])
		{
			textureIndex = 10;
		}
		else if (connections[XPOS] && connections[XNEG])
		{
			textureIndex = 3;
		}
		else if (connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 2;
		}
		else if (connections[XPOS] && connections[ZPOS])
		{
			textureIndex = 7;
		}
		else if (connections[ZPOS] && connections[XNEG])
		{
			textureIndex = 4;
		}
		else if (connections[XNEG] && connections[ZNEG])
		{
			textureIndex = 5;
		}
		else if (connections[ZNEG] && connections[XPOS])
		{
			textureIndex = 6;
		}
		else if (connections[XPOS])
		{
			textureIndex = 14;
		}
		else if (connections[XNEG])
		{
			textureIndex = 15;
		}
		else if (connections[ZPOS])
		{
			textureIndex = 12;
		}
		else if (connections[ZNEG])
		{
			textureIndex = 13;
		}
		
		return textureIndex;
	}
	
	public static int getNegativeYTexture(boolean[] connections)
	{
		int textureIndex = 0;
		
		if (connections[XPOS] && connections[XNEG] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 1;
		}
		else if (connections[XPOS] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 11;
		}
		else if (connections[ZPOS] && connections[XPOS] && connections[XNEG])
		{
			textureIndex = 10;
		}
		else if (connections[XNEG] && connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 9;
		}
		else if (connections[ZNEG] && connections[XNEG] && connections[XPOS])
		{
			textureIndex = 8;
		}
		else if (connections[XPOS] && connections[XNEG])
		{
			textureIndex = 3;
		}
		else if (connections[ZPOS] && connections[ZNEG])
		{
			textureIndex = 2;
		}
		else if (connections[XPOS] && connections[ZPOS])
		{
			textureIndex = 6;
		}
		else if (connections[ZPOS] && connections[XNEG])
		{
			textureIndex = 5;
		}
		else if (connections[XNEG] && connections[ZNEG])
		{
			textureIndex = 4;
		}
		else if (connections[ZNEG] && connections[XPOS])
		{
			textureIndex = 7;
		}
		else if (connections[XPOS])
		{
			textureIndex = 14;
		}
		else if (connections[XNEG])
		{
			textureIndex = 15;
		}
		else if (connections[ZPOS])
		{
			textureIndex = 13;
		}
		else if (connections[ZNEG])
		{
			textureIndex = 12;
		}
		
		return textureIndex;
	}
}
