package application.World;

import java.util.ArrayList;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

public class Map
{
	// List of rooms
	private ArrayList<Room> room;

	public Map(Image source, int width, int height, int chunkWidth, int chunkHeight)
	{
		PixelReader reader = source.getPixelReader();

		// Arraylist of unique colors (for different tiles)
		ArrayList<Integer> colors = new ArrayList<Integer>();

		// Use binary search and insertion to propogate arraylist with unique numbers
		for (int x = 0; x < source.getWidth(); x++)
		{
			for (int y = 0; y < source.getHeight(); y++)
			{
				// Get the color at the coordinate
				int color = reader.getArgb(x, y);

				// Use binary search to see if color exists in colors ArrayList
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
				
				// If color still wasn't found, insert it in the correct order
				if (searchIndex == -1)
				{
					if (colors.size() > 0)
					{
						if (color > colors.get(mid))
						{
							searchIndex = mid + 1;
						}
						else
						{
							searchIndex = mid;
						}
					}
					else
					{
						searchIndex = 0;
					}

					colors.add(searchIndex, color);
				}
			}
		}

		// Create array to store chunks (4 for different orientations from x flip and y flip)
		Chunk[] chunks = new Chunk[((int)source.getWidth() + 1 - chunkWidth) * ((int)source.getHeight() + 1 - chunkHeight) * 4];

		// Create chunks
		for (int y = 0; y <= source.getHeight() - chunkHeight; y++)
		{
			for (int x = 0; x <= source.getWidth() - chunkWidth; x++)
			{
				int baseIndex = (y * (int)(source.getWidth() - chunkWidth + 1) + x) * 4;
				chunks[baseIndex] = new Chunk(colors, reader, x, y, chunkWidth, chunkHeight, false, false);
				chunks[baseIndex + 1] = new Chunk(colors, reader, x, y, chunkWidth, chunkHeight, true, false);
				chunks[baseIndex + 2] = new Chunk(colors, reader, x, y, chunkWidth, chunkHeight, false, true);
				chunks[baseIndex + 3] = new Chunk(colors, reader, x, y, chunkWidth, chunkHeight, true, true);
			}
		}

		// Debug
		for (int i = 0; i < chunks.length; i++)
		{
			for (int y = 0; y < chunkHeight; y++)
			{
				for (int x = 0; x < chunkWidth; x++)
				{
					int b = chunks[i].getID(x, y);
					if (b == 0)
					{
						System.out.print("░");
					}
					else if (b == 1)
					{
						System.out.print("▒");
					}
					else if (b == 2)
					{
						System.out.print("▓");
					}
					else
					{
						System.out.print(" ");
					}
				}
				System.out.println();
			}
			System.out.println();
		}


		// I haven't added the actual 'base cases': i.e., the 3x3 tile is a 'neighbor' to the 3x3 tile right beside it with mutually exclusive bounds
		
		// Map to perform wave function collapse on
		// This will be default initialized to FALSE, therefore, I will that to represent the valid configuration.
		// True will be used to represent a definite incompatibility at the indicies of the trues
		boolean[][][] map = new boolean[width][height][colors.size()];

		// Queue of superposition 'tiles' to update
		ArrayList<Integer> queue = new ArrayList<Integer>();
		queue.add(height * width / 2); // begin at center of map

		// 'visited queue' - false by default
		boolean[] visited = new boolean[width * height];
		
		// If spot is NOT empty, it must be filled by a neighbor of all its neighboring nodes
		while (queue.size() > 0)
		{
			System.out.println(queue.size());

			// Take from queue, 'pop' and set visited
			int spot = queue.get(0);
			int yPos = spot / width;
			int xPos = spot - (yPos * width);
			queue.remove(0);

			// Keep track of possible chunks indices for current 'tile'
			ArrayList<Integer> possibleChunks = new ArrayList<Integer>();

			// Iterate through all all chunks and see if they are a possible configuration for the superposition
			for (int chunk = 0; chunk < chunks.length; chunk++)
			{
				boolean valid = true;
				
				for (int x = 0; x < chunkWidth; x++)
				{
					for (int y = 0; y < chunkHeight; y++)
					{
						int realX = xPos + x;
						int realY = yPos + y;

						if (realX >= 0 && realX < width && realY >= 0 && realY < height)
						{
							// Get the superposition
							boolean[] superposition = map[realX][realY];

							// See if the current spot in the configuration is possible (true means invalid)
							if (superposition[chunks[chunk].getID(x, y)] == true)
							{
								valid = false;
								break;
							}
						}
					}

					if (!valid)
					{
						break;
					}
				}

				if (valid)
				{
					possibleChunks.add(chunk);
				}
			}

			// Only count as visited if collapse occured
			if (possibleChunks.size() == 0)
			{
				visited[spot] = false;
				continue;
			}

			// Choose a possible random collapsed chunk to superimpose
			int randomState = (int)(Math.random() * possibleChunks.size());
			Chunk collapsedChunk = chunks[possibleChunks.get(randomState)];

			// completely collapse tile itself
			for (int x = 0; x < chunkWidth; x++)
			{
				for (int y = 0; y < chunkHeight; y++)
				{
					int realX = xPos + x;
					int realY = yPos + y;

					if (realX >= 0 && realX < width && realY >= 0 && realY < height)
					{
						// get collapsed state id
						int newID = collapsedChunk.getID(x, y);

						// Get superposition to collapse
						boolean[] superposition = map[xPos + x][yPos + y];

						for (int i = 0; i < superposition.length; i++)
						{
							if (i == newID)
							{
								superposition[i] = false;
							}
							else
							{
								superposition[i] = true;
							}
						}
					}
				}
			}

			// Add adjacents to queue (respectively top, bottom, right and left respectively)
			int[] adjacents = new int[]{-width, width, 1, -1};
			for (int i = 0; i < adjacents.length; i++)
			{
				int newSpot = spot + adjacents[i];
				// Add to queue if not visited
				if (newSpot >= 0 && newSpot < width * height && !visited[newSpot])
				{
					queue.add(queue.size(), newSpot);
					visited[newSpot] = true;
				}
			}
		}

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int color = 0; color < colors.size(); color++)
				{
					if (map[x][y][color] == false)
					{
						if (color == 0)
						{
							System.out.print("▓");
						}
						else if (color == 1)
						{
							System.out.print("░");
						}
						else if (color == 2)
						{
							System.out.print("▒");
						}
						else
						{
							System.out.print(" ");
						}
						break;
					}
				}
			}
			System.out.println();
		}
	}

	// TODO: use 9slice to generate tilemap (possible 2x scale)
}
