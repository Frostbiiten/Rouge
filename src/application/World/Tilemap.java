package application.World;

import java.util.Hashtable;

import javafx.scene.image.Image;
public class Tilemap
{
	private int tileSize;
	private Image imgTiles;

	public static final int TOPLEFT = 0, TOP = 1, TOPRIGHT = 2;
	public static final int LEFT = 3, CENTER = 4, RIGHT = 5;
	public static final int BOTTOMLEFT = 6, BOTTOM = 7, BOTTOMRIGHT = 8;

	// Surrounding dependent mapping
	private int floors[][];
	private int walls[][];

	public Tilemap(String name, int tileSize)
	{
		this.tileSize = tileSize;

		String basepath = String.format("assets/tilemaps/%s/", name);
		imgTiles = new Image("file:" + basepath + "Tilemap.png");
		
		floors = new int[3][3];
		for (int x = 0; x < floors.length; x++)
		{
			for (int y = 0; y < floors[x].length; y++)
			{
				floors[x][y] = y * (int)(imgTiles.getWidth() / tileSize) + x;
			}
		}

		walls = new int[3][5];
		for (int x = 0; x < walls.length; x++)
		{
			for (int y = 0; y < walls[x].length; y++)
			{
				walls[x][y] = y * (int)(imgTiles.getWidth() / tileSize) + x + 3;
			}
		}
	}

	public int getTileSize()
	{
		return tileSize;
	}
	public Image getTilesImg()
	{
		return imgTiles;
	}
	public int[] getFloorPosition(int id)
	{
		int y = id / (int)(imgTiles.getWidth() / tileSize);
		int x = id - y * (int)(imgTiles.getWidth() / tileSize);
		return new int[]{x, y};
	}
	public int[] getWallPosition(int id)
	{
		int y = id / (int)(imgTiles.getWidth() / tileSize);
		int x = id - y * (int)(imgTiles.getWidth() / tileSize) - 3;
		return new int[]{x, y};
	}
	public int getFloorSlice(int x, int y)
	{
		return floors[x][y];
	}
	public int getWallSlice(int x, int y)
	{
		return walls[x][y];
	}

	public void getIdFromKernel(boolean[][] kernel)
	{
		for (int y = 0; y < kernel.length; y++)
		{
			for (int x = 0; x < kernel.length; x++)
			{
				int offset = 1;
				int id = 0;

				// Top
				if (kernel[1][0])
				{
					id += offset;
				}
				offset = offset <<= 1;

				// Right
				if (kernel[2][1])
				{
					id += offset;
				}
				offset = offset <<= 1;

				// Down
				if (kernel[1][2])
				{
					id += offset;
				}
				offset = offset <<= 1;

				// Left
				if (kernel[0][1])
				{
					id += offset;
				}
				offset = offset <<= 1;

				// Top right
				if (kernel[0][2])
				{
					id += offset;
				}
				offset = offset <<= 1;

				// Bottom right
				if (kernel[2][2])
				{
					id += offset;
				}
				offset = offset <<= 1;

				// Bottom left
				if (kernel[0][2])
				{
					id += offset;
				}
				offset = offset <<= 1;

				// Top left
				if (kernel[0][0])
				{
					id += offset;
				}
			}
		}
	}
}
