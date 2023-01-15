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
		
		floors = new int[3][7];
		for (int x = 0; x < floors.length; x++)
		{
			for (int y = 0; y < floors[x].length; y++)
			{
				floors[x][y] = y * (int)(imgTiles.getWidth() / tileSize) + x;
			}
		}

		walls = new int[3][7];
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
	public boolean isFloor(int id)
	{
		return getFloorPosition(id)[0] < 3;
	}
	public boolean isBackWall(int id)
	{
		// Range of back wall tiles
		return id >= 3 && id <= 5;
	}
}
