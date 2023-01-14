package application.World;

import java.util.ArrayList;

import application.Vector2;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Map
{
	private int width, height;
	private int[][] tiles;
	private Partition root;
	private Tilemap tilemap;

	public Map(int width, int height, int roomCount, Tilemap tilemap)
	{
		// Initialize width, height and tilemap
		this.width = width;
		this.height = height;
		this.tilemap = tilemap;

		// Create tiles 2d array
		tiles = new int[width][height];

		// Create root partition and begin to create children
		root = new Partition(0, 0, width, height, 0);

		// Create partitions
		for (int i = 0; i < roomCount; i++)
		{
			root.addPartition();
		}
	
		// Sort by size using insertion sort
		ArrayList<Partition> partitions = root.getPartitions();
		for (int end = 1; end < partitions.size(); end++)
		{
			Partition item = partitions.get(end);
			int i = end;
			
			int currentArea = item.getWidth() * item.getHeight();
			int previousArea = partitions.get(i - 1).getWidth() * partitions.get(i - 1).getHeight();
			while (i > 0 && currentArea < previousArea)
			{
				partitions.set(i, partitions.get(i - 1));
				i--;
			}
			partitions.set(i, item);
		}

		// TODO: group rooms by sizes, specialize them (boss (?), etc)

		// Fill rooms
		for (int i = 0; i < partitions.size(); i++)
		{
			Partition currentPartition = partitions.get(i);

			// Get bounds of partition using position and dimensions
			int xBegin = currentPartition.getXPos();
			int xEnd = currentPartition.getXPos() + currentPartition.getWidth();

			int yBegin = currentPartition.getYPos();
			int yEnd = currentPartition.getYPos() + currentPartition.getHeight();

			for (int x = xBegin; x < xEnd; x++)
			{
				for (int y = yBegin; y < yEnd; y++)
				{
					boolean left = x == xBegin;
					boolean right = x == xEnd - 1;
					boolean top = y == yBegin;
					boolean bottom = y == yEnd - 1;

					// Get tile id based on where the tile is
					int tileID;

					// Do corner cases first
					if (left && top)
					{
						tileID = tilemap.getWallsArray()[Tilemap.TOPLEFT];
					}
					else if (left && bottom)
					{
						tileID = tilemap.getWallsArray()[Tilemap.BOTTOMLEFT];
					}
					else if (right && top)
					{
						tileID = tilemap.getWallsArray()[Tilemap.TOPRIGHT];
					}
					else if (right && bottom)
					{
						tileID = tilemap.getWallsArray()[Tilemap.BOTTOMRIGHT];
					}
					else if (left)
					{
						tileID = tilemap.getWallsArray()[Tilemap.LEFT];
					}
					else if (top)
					{
						tileID = tilemap.getWallsArray()[Tilemap.TOP];
					}
					else if (right)
					{
						tileID = tilemap.getWallsArray()[Tilemap.RIGHT];
					}
					else if (bottom)
					{
						tileID = tilemap.getWallsArray()[Tilemap.BOTTOM];
					}
					else
					{
						boolean floorLeft = x == xBegin + 1;
						boolean floorRight = x == xEnd - 2;
						boolean floorTop = y == yBegin + 1;
						boolean floorBottom = y == yEnd - 2;

						// Check cases for INSIDE room

						if (floorLeft && floorTop)
						{
							tileID = tilemap.getFloorArray()[Tilemap.TOPLEFT];
						}
						else if (floorLeft && floorBottom)
						{
							tileID = tilemap.getFloorArray()[Tilemap.BOTTOMLEFT];
						}
						else if (floorRight && floorTop)
						{
							tileID = tilemap.getFloorArray()[Tilemap.TOPRIGHT];
						}
						else if (floorRight && floorBottom)
						{
							tileID = tilemap.getFloorArray()[Tilemap.BOTTOMRIGHT];
						}
						else if (floorLeft)
						{
							tileID = tilemap.getFloorArray()[Tilemap.LEFT];
						}
						else if (floorTop)
						{
							tileID = tilemap.getFloorArray()[Tilemap.TOP];
						}
						else if (floorRight)
						{
							tileID = tilemap.getFloorArray()[Tilemap.RIGHT];
						}
						else if (floorBottom)
						{
							tileID = tilemap.getFloorArray()[Tilemap.BOTTOM];
						}
						else
						{
							// Center (normal)
							tileID = tilemap.getFloorArray()[Tilemap.CENTER];
						}
					}

					tiles[x][y] = tileID;
					System.out.print(String.format("%2s ", tileID));
				}
				System.out.println();
			}
		}
	}

    public void draw(Vector2 cameraPos, WritableImage renderImg)
	{
		// Get pixel reader and writer to draw from tileset to screen
		PixelReader reader = tilemap.getTilesImg().getPixelReader();
		PixelWriter writer = renderImg.getPixelWriter();
		int tileSize = tilemap.getTileSize();

		root.drawBounds(cameraPos, renderImg);

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				// Destination x and y coordinate of tile to be drawn
				int destX = x * tileSize - (int)cameraPos.x;
				int destY = y * tileSize - (int)cameraPos.y;

				int tileID = tiles[x][y];
				if (tileID == 0)
				{
					continue;
				}

				// Source x and y coordinate of tile to be drawn from tilemap
				int srcY = tileID / (int)(tilemap.getTilesImg().getWidth() / tileSize);
				int srcX = tileID - (srcY * (int)(tilemap.getTilesImg().getWidth() / tileSize));

				srcX *= tileSize;
				srcY *= tileSize;

				
				// Make sure destination coordinate is onscreen
				if (destX > 0 && destX < renderImg.getWidth() && destY > 0 && destY < renderImg.getHeight())
				{
					try
					{
						writer.setPixels(destX, destY, tileSize, tileSize, reader, srcX, srcY);
					}
					catch (Exception e)
					{

					}
				}
			}
		}
    }
}
