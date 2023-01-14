package application.World;

import javafx.scene.image.Image;
public class Tilemap
{
	private int tileSize;
	private Image imgTiles;
	
	public static final int TOPLEFT = 0, TOP = 1, TOPRIGHT = 2;
	public static final int LEFT = 3, CENTER = 4, RIGHT = 5;
	public static final int BOTTOMLEFT = 6, BOTTOM = 7, BOTTOMRIGHT = 8;

	// 9-slice images and map tile type identifiers
	private int floors[], floorID;
	private int walls[], wallID;

	public Tilemap(String name, int tileSize, int wallID, int floorID)
	{
		this.tileSize = tileSize;

		String basepath = String.format("assets/tilemaps/%s/", name);
		imgTiles = new Image("file:" + basepath + "Tiles.png");
		
		// Arrays to store 9-slice indexes

		floors = new int[9];
		this.floorID = wallID;
		
		walls = new int[9];
		this.wallID = wallID;
	}

	public int getTileSize()
	{
		return tileSize;
	}
	public Image getTilesImg()
	{
		return imgTiles;
	}
	public int[] getFloorArray()
	{
		return floors;
	}
	public int[] getWallsArray()
	{
		return walls;
	}

	public void setSlice(int[] arr, int slice, int x, int y)
	{
		setSlice(arr, slice, y * ((int)imgTiles.getWidth() / tileSize) + x);
	}
	public void setSlice(int[] arr, int slice, int value)
	{
		arr[slice] = value;
	}
}
