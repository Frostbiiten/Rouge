package application.World;

import java.util.ArrayList;

import javafx.scene.image.PixelReader;

// A chunk is simply a matrix representing the possible tiles
public class Chunk
{
	public class ChunkMatch
	{
		// ID of matched chunk
		private int id;

		// The relative x/y shift of the other chunk
		private int xShift;
		private int yShift;

		public ChunkMatch (int id, int xShift, int yShift)
		{
			this.id = id;
			this.xShift = xShift;
			this.yShift = yShift;
		}

		public int getID()
		{
			return id;
		}

		public int getXShift()
		{
			return xShift;
		}

		public int getYShift()
		{
			return yShift;
		}
	}

	private int[][] tiles;
	private ArrayList<ChunkMatch> matches;

	public Chunk(ArrayList<Integer> colors, PixelReader reader, int readerX, int readerY, int width, int height, boolean xFlip, boolean yFlip)
	{
		// Create 2d array
		tiles = new int[width][height];
		
		// Set up tiles
		for (int localX = 0; localX < width; localX++)
		{
			for (int localY = 0; localY < height; localY++)
			{
				// Get the real coordinates on the image itself
				int realX, realY;
				if (xFlip)
				{
					realX = (width - localX - 1) + readerX;
				}
				else
				{
					realX = localX + readerX;
				}
				if (yFlip)
				{
					realY = (height - localY - 1) + readerY;
				}
				else
				{
					realY = localY + readerY;
				}
				
				// Get the color at the coordinate
				int color = reader.getArgb(realX, realY);

				// Use binary search to get color index
				int searchStart = 0;
				int searchEnd = colors.size() - 1;
				int mid = 0, searchIndex = -1;
				
				while (searchStart <= searchEnd)
				{
					mid = (searchStart + searchEnd) / 2;
					if (colors.get(mid) == color)
					{
						// Color is mid (found)
						searchIndex = mid;
						break;
					}
					else if (color > colors.get(mid))
					{
						// Color is more than mid
						searchStart = mid + 1;
					}
					else
					{
						// Only other case is color must be less than mid
						searchEnd = mid - 1;
					}
				}
				
				// Set pixel to index
				tiles[localX][localY] = searchIndex;
			}
		}

		// Create match array
		matches = new ArrayList<ChunkMatch>();
	}

	// Get the id at a position within the tiles
	public int getID(int x, int y)
	{
		return tiles[x][y];
	}
	public void addMatch(int other, int x, int y)
	{
		matches.add(new ChunkMatch(other, x, y));
	}
	public ArrayList<ChunkMatch> getMatches()
	{
		return matches;
	}
}