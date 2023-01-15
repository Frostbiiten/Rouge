package application.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import application.GameManager;
import application.Vector2;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import application.Util;

public class Map
{
	private int width, height;
	private boolean[][] floorTiles;

	// Map generation
	private ArrayList<Rectangle> rooms;
	private ArrayList<Vector2> roomPositions;
	private Rectangle startRoom;
	private Rectangle endRoom;

	// Drawing
	private int[][][] tiles;
	private Tilemap tilemap;
	private boolean ditheringEnabled;
	private double ditherShrink = 1;

	// Constructor
	public Map(int mapWidth, int mapHeight, int roomCount, Tilemap tilemap)
	{
		// Initialize width, height and tilemap
		width = mapWidth;
		height = mapHeight;
		this.tilemap = tilemap;

		// Generate rooms and get hallway edges
		generateRooms(roomCount);
		ArrayList<Edge> hallways = getHallEdges();

		// Create 2d array to store whether a tile is floor or not.
		floorTiles = pathfindRooms(hallways);

		// Array to store the component the tile at [x][y] belongs to
		int[][] component = new int[width][height];

		// Get components 'islands'/isolated room groups
		ArrayList<int[]> components = getComponents(floorTiles, component);

		// Clean up map
		pruneMap(floorTiles, component, components);

		// Chose various important rooms
		chooseRooms();

		// Upscale to deal with some imperfections
		floorTiles = upscale(floorTiles);

		// Generate tiles to be displayed on screen
		generateVisualTiles(floorTiles);

		// Default dither disabled
		ditheringEnabled = false;
	}

	// INTERNAL METHODS:
	// Method to generate tiles to be shown onscreen from tile boolean array
	private void generateVisualTiles(boolean[][] floorTiles)
	{
		// Initialize tiles array to store actual visual tiles
		// By default their height is 1
		tiles = new int[width][height][1];

		// Base tiles
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				// Get matrix of surrounding tiles
				boolean[][] tileKernel = new boolean[3][3];

				for (int localX = 0; localX < tileKernel.length; localX++)
				{
					for (int localY = 0; localY < tileKernel[localX].length; localY++)
					{
						int realX = x - localX - 1;
						int realY = y - localY - 1;
						
						// Make sure tile is in range before attempting read
						if (realX >= 0 && realX < width && realY >= 0 && realY < height)
						{
							tileKernel[localX][localY] = floorTiles[realX][realY];
						}
					}
				}

				// Check surroundings
				boolean isFloor = floorTiles[x][y];
				boolean aboveIsFloor = false, belowIsFloor = false;
				boolean leftIsFloor = false, rightIsFloor = false;

				if (y > 0)
				{
					aboveIsFloor = floorTiles[x][y - 1];
				}
				if (x > 0)
				{
					leftIsFloor = floorTiles[x - 1][y];
				}
				if (y < height - 1)
				{
					belowIsFloor = floorTiles[x][y + 1];
				}
				if (x < width - 1)
				{
					rightIsFloor = floorTiles[x + 1][y];
				}

				if (isFloor)
				{
					if (tiles[x][y][0] != 0)
					{
						continue;
					}

					// Start from center tile
					int[] floorMatrixPosition = new int[]{1, 1};

					// Offset based on surroundings
					if (!leftIsFloor)
					{
						floorMatrixPosition[0]--;
					}
					else
					{
						if (!rightIsFloor)
						{
							floorMatrixPosition[0]++;
						}
					}


					if (!aboveIsFloor)
					{
						floorMatrixPosition[1]--;

						if (y > 0)
						{
							int[] wallMatrixPosition = new int[]{1, 2};
							tiles[x][y - 1][0] = tilemap.getWallSlice(wallMatrixPosition[0], wallMatrixPosition[1]);
						}
					}
					else
					{
						if (!belowIsFloor)
						{
							floorMatrixPosition[1]++;
						}
					}

					if (y < height - 1 && !belowIsFloor)
					{
						int[] wallMatrixPosition = new int[]{1, 0};
						tiles[x][y + 1][0] = tilemap.getWallSlice(wallMatrixPosition[0], wallMatrixPosition[1]);
					}

					if (floorMatrixPosition[0] != 1)
					{
						// Start from center tile
						int[] wallMatrixPosition = new int[]{floorMatrixPosition[0], floorMatrixPosition[1]};

						if (!aboveIsFloor || !belowIsFloor)
						{
							wallMatrixPosition[1] = 1;
						}

						if (!rightIsFloor)
						{
							if (x < width - 1)
							{
								tiles[x + 1][y][0] = tilemap.getWallSlice(2 - wallMatrixPosition[0], wallMatrixPosition[1]);
							}
						}
						
						if (!leftIsFloor)
						{
							if (x > 0)
							{
								tiles[x - 1][y][0] = tilemap.getWallSlice(2 - wallMatrixPosition[0], wallMatrixPosition[1]);
							}
						}
					}

					tiles[x][y][0] = tilemap.getFloorSlice(floorMatrixPosition[0], floorMatrixPosition[1]);

					// Corner tiles

					// Top left
					if (!leftIsFloor && !aboveIsFloor && rightIsFloor && belowIsFloor)
					{
						if (x > 0 && y > 0)
						{
							tiles[x - 1][y - 1][0] = tilemap.getWallSlice(2, 1);

							if (y > 1)
							{
								tiles[x - 1][y - 1][0] = tilemap.getWallSlice(0, 5);
							}
						}
					}

					// Top right
					if (!rightIsFloor && !aboveIsFloor && leftIsFloor && belowIsFloor)
					{
						if (x < width - 1 && y > 0)
						{
							tiles[x + 1][y - 1][0] = tilemap.getWallSlice(0, 1);

							if (y > 1)
							{
								tiles[x + 1][y - 1][0] = tilemap.getWallSlice(1, 5);
							}
						}
					}

					// Bottom right
					if (!rightIsFloor && aboveIsFloor && leftIsFloor && !belowIsFloor)
					{
						if (x < width - 1 && y < height - 1)
						{
							tiles[x + 1][y + 1][0] = tilemap.getWallSlice(1, 6);
						}
					}

					// Bottom left
					if (!leftIsFloor && aboveIsFloor && rightIsFloor && !belowIsFloor)
					{
						if (x > 0 && y < height - 1)
						{
							tiles[x - 1][y + 1][0] = tilemap.getWallSlice(0, 6);
						}
					}
				}
				else
				{
					if (tiles[x][y][0] != 0)
					{
						continue;
					}

					// Start from base tile
					int[] wallMatrixPosition = new int[]{1, 1};
					tiles[x][y][0] = tilemap.getWallSlice(wallMatrixPosition[0], wallMatrixPosition[1]);
				}
			}
		}

		//  Horzontal sweeps to fix left/right walls
		for (int y = 1; y < height - 1; y++)
		{
			for (int x = 1; x < width - 1; x++)
			{
				// Check if it is a floor tile. Only run if it isn't
				if (!tilemap.isFloor(tiles[x][y][0]))
				{
					int[] wallMatrixPosition = tilemap.getWallPosition(tiles[x][y][0]);
					if (wallMatrixPosition[0] == 1 && wallMatrixPosition[1] == 4)
					{
						// Check if they are floor tiles or wall tiles
						boolean leftwall = tilemap.getFloorPosition(tiles[x - 1][y][0])[0] > 2;
						boolean rightwall = tilemap.getFloorPosition(tiles[x + 1][y][0])[0] > 2;

						// Make sure the booleans are referring to a specific tile
						leftwall = leftwall &&  tilemap.getWallPosition(tiles[x - 1][y][0])[1] == 4;
						rightwall = rightwall &&  tilemap.getWallPosition(tiles[x + 1][y][0])[1] == 4;

						if (leftwall != rightwall)
						{
							if (leftwall)
							{
								wallMatrixPosition[0]++;
							}

							if (rightwall)
							{
								wallMatrixPosition[0]--;
							}
						}

						tiles[x][y][0] = tilemap.getWallSlice(wallMatrixPosition[0], wallMatrixPosition[1]);
					}
				}
			}
		}

		// Fix inner corner tiles
		for (int y = 1; y < height - 1; y++)
		{
			for (int x = 1; x < width - 1; x++)
			{
				// Create 3x3 kernel for processing corners
				boolean[][] floorKernel = new boolean[3][3];
				int floorTileCount = 0, cornerX = -1, cornerY = -1;
				for (int localX = 0; localX < 3; localX++)
				{
					for (int localY = 0; localY < 3; localY++)
					{
						boolean val = floorKernel[localX][localY] = floorTiles[x + localX - 1][y + localY - 1];
						if (val)
						{
							floorTileCount++;
						}
						else
						{
							cornerX = localX;
							cornerY = localY;
						}
					}
				}

				if (floorTileCount == 8)
				{
					// Top left corner
					if (cornerX == 0 && cornerY == 0)
					{
						tiles[x][y][0] = tilemap.getFloorSlice(2, 5);
						tiles[x - 1][y - 1][0] = tilemap.getWallSlice(2, 2);
					}

					// Top right corner
					if (cornerX == 2 && cornerY == 0)
					{
						tiles[x][y][0] = tilemap.getFloorSlice(0, 5);
						tiles[x + 1][y - 1][0] = tilemap.getWallSlice(0, 2);
					}

					// Bottom right corner
					if (cornerX == 2 && cornerY == 2)
					{
						tiles[x][y][0] = tilemap.getFloorSlice(0, 3);
						tiles[x + 1][y + 1][0] = tilemap.getWallSlice(0, 0);
					}

					// Bottom left corner
					if (cornerX == 0 && cornerY == 2)
					{
						tiles[x][y][0] = tilemap.getFloorSlice(2, 3);
						tiles[x - 1][y + 1][0] = tilemap.getWallSlice(2, 0);
					}
				}
			}
		}

		// Extrude walls vertically
		for (int y = 1; y < height - 1; y++)
		{
			for (int x = 1; x < width - 1; x++)
			{
				// Only walls are extruded
				if (!tilemap.isFloor(tiles[x][y][0]))
				{
					int[] wallMatrixPos = tilemap.getWallPosition(tiles[x][y][0]);
					boolean isFront = wallMatrixPos[1] == 2;

					if (isFront)
					{
						wallMatrixPos[1] = 4;
						tiles[x][y][0] = tilemap.getWallSlice(wallMatrixPos[0], wallMatrixPos[1]);
					}

					for (int height = 1; height < 3; height++)
					{
						if (isFront)
						{
							wallMatrixPos[1]--;
						}

						tiles[x][y] = Arrays.copyOf(tiles[x][y], tiles[x][y].length + 1);
						tiles[x][y][tiles[x][y].length - 1] = tilemap.getWallSlice(wallMatrixPos[0], wallMatrixPos[1]);
					}

					/*
					int[] wallMatrixPos = tilemap.getWallPosition(tiles[x][y][0]);

					if (wallMatrixPos[1] == 4)
					{
						for (int height = 1; height < 2; height++)
						{
							wallMatrixPos[1]--;
							tiles[x][y] = Arrays.copyOf(tiles[x][y], tiles[x][y].length + 1);

							// Check if straight horizontal tile should be forced depending on surroundings
							if (wallMatrixPos[1] == 2 && height == 2 && (floorTiles[x + 1][y + 1] != floorTiles[x - 1][y + 1]))
							{
								wallMatrixPos[0] = 1;
							}

							tiles[x][y][tiles[x][y].length - 1] = tilemap.getWallSlice(wallMatrixPos[0], wallMatrixPos[1]);
						}
					}
					else
					{
						for (int height = 1; height < 2; height++)
						{
							tiles[x][y] = Arrays.copyOf(tiles[x][y], tiles[x][y].length + 1);
							tiles[x][y][tiles[x][y].length - 1] = tilemap.getWallSlice(wallMatrixPos[0], wallMatrixPos[1]);
						}
					}
					*/
				}
			}
		}
	}

	// Method to scale map by a 2 to remove elements with 1 width/height (like thin halls) and other various imperfections
	private boolean[][] upscale(boolean[][] floorTiles)
	{
		// Double dimensions
		width *= 2;
		height *= 2;

		// Use temporary array to store upscaled map
		boolean[][] upscaledTiles = new boolean[width][height];

		// Iterate on smaller array, copy to 2x2 kernel in larger array
		for (int x = 0; x < width / 2; x++)
		{
			for (int y = 0; y < height / 2; y++)
			{
				upscaledTiles[x * 2][y * 2] = floorTiles[x][y];
				upscaledTiles[x * 2 + 1][y * 2] = floorTiles[x][y];
				upscaledTiles[x * 2][y * 2 + 1] = floorTiles[x][y];
				upscaledTiles[x * 2 + 1][y * 2 + 1] = floorTiles[x][y];
			}
		}

		// Upscale rooms
		for (int i = 0; i < rooms.size(); i++)
		{
			Rectangle current = rooms.get(i);

			// Scaling is done about the origin at (0, 0), where all points are positive, so no fancy math is needed,
			// they can just be scaled directly as shown

			current.setWidth(current.getWidth() * 2);
			current.setHeight(current.getHeight() * 2);
			current.setX(current.getX() * 2);
			current.setY(current.getY() * 2);
		}

		// Set original to scaled
		return upscaledTiles;
	}

	// Method to print the generated floor tiles map for debugging purposes
	private void debugPrintMap(boolean[][] floorTiles)
	{
		// Print row by row (x in inner loop)
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if(floorTiles[x][y])
				{
					System.out.print("▓");
				}
				else
				{
					System.out.print("░");
				}
			}

			// End line after each row
			System.out.println();
		}

		// Print line to pad
		System.out.println();
	}

	// Method to choose start and end rooms, along with others
	private void chooseRooms()
	{
		// Get center of map
		Vector2 mapCenter = new Vector2(width / 2, height / 2);

		// Sort rooms by distance from center using insertion sort
		for (int end = 1; end < rooms.size(); end++)
		{
			Rectangle rect = rooms.get(end);
			int index = end;

			// Get distance of current rect from center (use square distance because it is faster to compute)
			double sqrDistance = Vector2.SqrDistance(mapCenter, new Vector2(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2));

			// Sort using distance from center
			while (index > 0 && sqrDistance < Vector2.SqrDistance(mapCenter,
				new Vector2(rooms.get(index - 1).getX() + rooms.get(index - 1).getWidth() / 2, rooms.get(index - 1).getY() + rooms.get(index - 1).getWidth() / 2)))
			{
				rooms.set(index, rooms.get(index - 1));
				index--;
			}

			rooms.set(index, rect);
		}

		// Chose furthest from center for start room
		startRoom = rooms.get(rooms.size() - 1);

		// Get start room position
		Vector2 startRoomPos = new Vector2(startRoom.getX() + startRoom.getWidth() / 2, startRoom.getY() + startRoom.getHeight() / 2);

		// Sort again but based on distance from start room to assign furthest room to end room
		for (int end = 1; end < rooms.size(); end++)
		{
			Rectangle rect = rooms.get(end);
			int index = end;

			// Get distance of current rect from center (use square distance because it is faster to compute)
			double sqrDistance = Vector2.SqrDistance(startRoomPos, new Vector2(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2));

			// Sort using distance from center
			while (index > 0 && sqrDistance < Vector2.SqrDistance(startRoomPos,
				new Vector2(rooms.get(index - 1).getX() + rooms.get(index - 1).getWidth() / 2, rooms.get(index - 1).getY() + rooms.get(index - 1).getWidth() / 2)))
			{
				rooms.set(index, rooms.get(index - 1));
				index--;
			}

			rooms.set(index, rect);
		}

		// Assign end room to furthest
		endRoom = rooms.get(rooms.size() - 1);
	}

	// Method to process map to ensure it is suitable for latter steps
	private void pruneMap(boolean[][] floorTiles, int[][] component, ArrayList<int[]> components)
	{
		// Get components and store the group of rooms with most area/largest component. Use end index because least to greatest sorting
		int[] largestComponent = components.get(components.size() - 1);

		// Prune tiles: If any tile is not largestComponent, make it a wall tile
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				if (component[x][y] != largestComponent[0])
				{
					floorTiles[x][y] = false;
				}
			}
		}

		// Prune rooms: Remove the rectangle objects that have previously been pruned
		for (int room = 0; room < rooms.size(); room++)
		{
			Rectangle current = rooms.get(room);
			
			// Check if center of rectangle is inside a room
			if (!floorTiles[(int)(current.getX() + current.getWidth() / 2)][(int)(current.getY() + current.getHeight() / 2)])
			{
				rooms.remove(current);
				room--;
			}
		}
	}

	// Method to get the individual "islands"/room groups from the graph
	private ArrayList<int[]> getComponents(boolean[][] floorTiles, int[][] component)
	{
		// Use a 'visited' array to store which component the tile at [x][y] belongs to. Default is -1 (none)
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				component[x][y] = -1;
			}
		}

		// Arraylist of components. [0] = component num, [1] = component size
		ArrayList<int[]> components = new ArrayList<int[]>();

		// Get graph "components" using flood fill algorithm
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				// Check if room is open
				if (floorTiles[x][y] && component[x][y] == -1)
				{
					int[] currentComponent = new int[]{components.size(), 0};
					components.add(currentComponent);
					
					ArrayList<int[]> queue = new ArrayList<int[]>();

					// Begin floodfill
					queue.add(new int[]{x, y});
					component[x][y] = currentComponent[0];

					while(!queue.isEmpty())
					{
						// Get front node and 'pop' queue
						int[] current = queue.get(0);
						queue.remove(0);
						int nodeX = current[0], nodeY = current[1];

						// Add this cell to size of current component
						currentComponent[1]++;

						// Add adjacent nodes to queue, make sure they are not already occupied
						if (nodeX > 0)
						{
							if (component[nodeX - 1][nodeY] == -1 && floorTiles[nodeX - 1][nodeY])
							{
								component[nodeX - 1][nodeY] = currentComponent[0];
								queue.add(new int[]{nodeX - 1, nodeY});
							}
						}
						if (nodeX < width - 1)
						{
							if (component[nodeX + 1][nodeY] == -1 && floorTiles[nodeX + 1][nodeY])
							{
								component[nodeX + 1][nodeY] = currentComponent[0];
								queue.add(new int[]{nodeX + 1, nodeY});
							}
						}

						if (nodeY > 0)
						{
							if (component[nodeX][nodeY - 1] == -1 && floorTiles[nodeX][nodeY - 1])
							{
								component[nodeX][nodeY - 1] = currentComponent[0];
								queue.add(new int[]{nodeX, nodeY - 1});
							}
						}
						if (nodeY < height - 1)
						{
							if (component[nodeX][nodeY + 1] == -1 && floorTiles[nodeX][nodeY + 1])
							{
								component[nodeX][nodeY + 1] = currentComponent[0];
								queue.add(new int[]{nodeX, nodeY + 1});
							}
						}
					}
				}
			}
		}

		// Use insertion sort to sort components by size, least to greatest
		for (int end = 1; end < components.size(); end++)
		{
			int[] val = components.get(end);
			int index = end;

			// Compare sizes obtained from floodFill
			while (index > 0 && val[1] < components.get(index - 1)[1])
			{
				components.set(index, components.get(index - 1));
				index--;
			}

			components.set(index, val);
		}
	
		return components;
	}

	// Method to pathfind rooms and generate a 2d tile bool array representing if a tile is floor or not
	private boolean[][] pathfindRooms(ArrayList<Edge> hallways)
	{
		// Array to store if hallway was created at [x][y] or not (default false)
		boolean[][] floorTiles = new boolean[width][height];

		// First fill halls
		for (int i = 0; i < hallways.size(); i++)
		{
			Edge current = hallways.get(i);

			Vector2 beginVec = current.getA();
			Vector2 endVec = current.getB();

			// Array to store the state of grid spot
			// -1 : unvisited
			// 0 : in queue
			// 1 : visited
			int[][] visited = new int[width][height];
			
			// Array to store the cost of a cell
			// [0] = node x
			// [1] = node y
			// [2] = cost
			int[][] costArr = new int[width][height];

			// Array to store the previous cell from traversal
			int[][][] previousNodes = new int[width][height][2];

			// Set default values
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					visited[x][y] = -1;
					costArr[x][y] = Integer.MAX_VALUE;
					previousNodes[x][y] = new int[]{-1, -1};
				}
			}

			// Pathfind from the beginning of an edge to the end
			ArrayList<int[]> queue = new ArrayList<int[]>();

			// 1. Add initial point to queue
			queue.add(new int[]{(int)beginVec.x, (int)beginVec.y, 0});

			// 1. Set visited flag to in queue (0)
			visited[(int)beginVec.x][(int)beginVec.y] = 0;

			// 3. Set previous node for navigation to self (because this is the starting node)
			previousNodes[(int)beginVec.x][(int)beginVec.y] = new int[]{(int)beginVec.x, (int)beginVec.y};

			// Run until queue is empty
			while(queue.size() != 0)
			{
				// Sort queue using insertion sort
				for (int end = 1; end < queue.size(); end++)
				{
					int[] val = queue.get(end);
					int index = end;

					while (index > 0)
					{
						int[] previous = queue.get(index - 1);
						if (costArr[val[0]][val[1]] < costArr[previous[0]][previous[1]])
						{
							queue.set(index, queue.get(index - 1));
							index--;
						}
						else
						{
							break;
						}
					}

					queue.set(index, val);
				}

				// Get 'node' at the front of the queue and pop
				int[] front = queue.get(0);
				int nodeX = front[0], nodeY = front[1];
				queue.remove(0);

				// Set visited value to closed
				visited[nodeX][nodeY] = 1;

				// Check if end has been reached
				if (nodeX == (int)endVec.x && nodeY == (int)endVec.y)
				{
					// Trace path back for hall array
					int traceX = nodeX, traceY = nodeY;
					floorTiles[traceX][traceY] = true;

					// First node's previous will be itself, so trace until then
					while(previousNodes[traceX][traceY][0] != traceX || previousNodes[traceX][traceY][1] != traceY)
					{
						traceX = previousNodes[traceX][traceY][0];
						traceY = previousNodes[traceX][traceY][1];
						floorTiles[traceX][traceY] = true;
					}

					floorTiles[traceX][traceY] = true;
					break;
				}

				// Offsets of neighbors on grid
				int[][] neighborOffsets = new int[][]
				{
					{+1, 0}, // right
					{-1, 0}, // left
					{0, +1}, // bottom
					{0, -1}, // right
				};
				
				// Get g cost for current node
				int currentGCost = Math.abs(nodeX - (int)beginVec.x) + Math.abs(nodeY - (int)beginVec.y);

				// Iterate through each 'neighbor' node on each side
				for (int offsetIndex = 0; offsetIndex < neighborOffsets.length; offsetIndex++)
				{
					int[] currentOffset = neighborOffsets[offsetIndex];
					int newSpotX = nodeX + currentOffset[0];
					int newSpotY = nodeY + currentOffset[1];

					// Make sure new spot is in the map's bounds
					if (newSpotX >= 0 && newSpotX < width && newSpotY >= 0 && newSpotY < height
						&& visited[newSpotX][newSpotY] != 1) // Make sure it's either in queue or unvisited
					{
						// Get new cost of neighbor from this node
						int newNeighborCost = currentGCost + 1;

						// Use *manhattan distance* to find heuristics for neighbor (euclidean distance would yeild diagonals)
						int gCost = Math.abs(newSpotX - (int)beginVec.x) + Math.abs(newSpotY - (int)beginVec.y); // Distance from start point

						// Check if the distance is more optimal and the node is not already in queue
						if (newNeighborCost < gCost || visited[newSpotX][newSpotY] != 0)
						{
							int hCost = Math.abs(newSpotX - (int)endVec.x) + Math.abs(newSpotY - (int)endVec.y); // Distance from end point

							// Alter heuristic based on map
							int fCost = (gCost + hCost) * 10;
							if (!floorTiles[newSpotX][newSpotY]) // Favor merging halls (penalize if not in hall)
							{
								fCost += 5;
							}

							// Set cost and previous node for neighbor
							costArr[newSpotX][newSpotY] = fCost;
							previousNodes[newSpotX][newSpotY] = new int[]{nodeX, nodeY};

							// Add to queue if not already there
							if (visited[newSpotX][newSpotY] != 0)
							{
								visited[newSpotX][newSpotY] = 0;
								queue.add(new int[]{newSpotX, newSpotY});
							}
						}
					}
				}
			}
		}
		
		// Slightly shrink rooms for padding/spacing purposes
		for (int i = 0; i < rooms.size(); i++)
		{
			Rectangle currentRoom = rooms.get(i);
			currentRoom.setWidth(currentRoom.getWidth() - 2);
			currentRoom.setX(currentRoom.getX() + 1);
			currentRoom.setHeight(currentRoom.getHeight() - 2);
			currentRoom.setY(currentRoom.getY() + 1);

			for (int x = (int)currentRoom.getX(); x < (int)currentRoom.getX() + currentRoom.getWidth(); x++)
			{
				for (int y = (int)currentRoom.getY(); y < (int)currentRoom.getY() + currentRoom.getHeight(); y++)
				{
					floorTiles[x][y] = true;
				}
			}
		}

		return floorTiles;
	}

	// Generate room rectangles and their corresponding centers, stored in roompositions array 
	private void generateRooms(int roomCount)
	{
		rooms = new ArrayList<Rectangle>();
		roomPositions = new ArrayList<Vector2>();

		int maxWidth = 8, minWidth = 6, maxHeight = 8, minHeight = 6;

		Random rand = new Random();
		int attempts = 0;

		while (rooms.size() < roomCount)
		{
			// Restart room generation if there have been too many failed attempts
			attempts++;
			if (attempts > 100)
			{
				attempts = 0;
				rooms.clear();
			}

			// Generate rectangle with random position and dimensions
			Rectangle rect = new Rectangle(rand.nextInt(width), rand.nextInt(height), rand.nextInt(maxWidth - minWidth) + minWidth, rand.nextInt(maxHeight - minHeight) + minHeight);

			// Discard if out of bounds
			if (rect.getX() + rect.getWidth() > width - 1 || rect.getY() + rect.getHeight() > height - 1)
			{
				continue;
			}

			// Check if rectangle overlaps pre-existing rooms
			boolean valid = true;
			for (int i = 0; i < rooms.size(); i++)
			{
				// Make sure rectangle doesn't overlap another/go out of range
				if (rooms.get(i).intersects(rect.getBoundsInParent()))
				{
					valid = false;
					break;
				}
			}

			// Discard if invalid, add to lists if valid
			if (valid)
			{
				// Reset attempts as room is valid
				attempts = 0;

				// Add to list of rooms along with position
				rooms.add(rect);
				roomPositions.add(new Vector2(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2));
			}
		}
	}

	// Generate a list of edges that connect the previously generated rooms
	private ArrayList<Edge> getHallEdges()
	{
		// Triangulate graph using bowyer watson algorithm
		Triangulator triangulator = new Triangulator();
		ArrayList<Edge> graph = triangulator.triangulate(roomPositions);
		ArrayList<Edge> hallways = new ArrayList<Edge>(graph);

		// Generate MST using prim's algorithm
		ArrayList<Edge> mst = generateMST(hallways);
		
		// Remove edges not in mst from graph, except for 15%
		for (int i = 0; i < graph.size(); i++)
		{
			Edge current = graph.get(i);

			// Check if edge exists in mst
			boolean inMST = false;
			for (int j = 0; j < mst.size(); j++)
			{
				if (Triangulator.closeTo(current, mst.get(j)))
				{
					inMST = true;
					break;
				}
			}

			// 15% chance to keep edges not in mst
			boolean keeping = true;
			if (!inMST)
			{
				keeping = Math.random() < 0.15;
			}

			if (!keeping)
			{
				hallways.remove(current);
				i--;
			}
		}

		return hallways;
	}

	// Compute the minimum spanning tree
	private ArrayList<Edge> generateMST(ArrayList<Edge> graph)
	{
		// points to visit
		ArrayList<Vector2> unvisited = new ArrayList<Vector2>();
		ArrayList<Vector2> visited = new ArrayList<Vector2>();

		// Add all vertices to unvisited
		for (int i = 0; i < graph.size(); i++)
		{
			// Check if point is already in graph
			boolean containsA = false, containsB = false;

			for (int j = 0; j < unvisited.size(); j++)
			{
				if (!containsA && Triangulator.closeTo(unvisited.get(j), graph.get(i).getA()))
				{
					containsA = true;
				}

				if (!containsB && Triangulator.closeTo(unvisited.get(j), graph.get(i).getB()))
				{
					containsA = true;
				}
			}

			if (!containsA)
			{
				unvisited.add(graph.get(i).getA());
			}
			if (!containsB)
			{
				unvisited.add(graph.get(i).getB());
			}
		}

		// Add arbitratry point to begin at
		visited.add(graph.get(0).getA());

		// Final mst
		ArrayList<Edge> mst = new ArrayList<Edge>();

		while (unvisited.size() > 0)
		{
			// If running != true by the end, the traversal must be complete
			boolean running = false;

			Edge shortestEdge = null; // Java complains about uninitialized without this
			double shortestEdgeLength = Double.POSITIVE_INFINITY;

			// Iterate through edges to find connections
			for (int i = 0; i < graph.size(); i++)
			{
				Edge current = graph.get(i);

				// Check how many vertices have already been visited in edge
				int visitedVerts = 0;
				for (int j = 0; j < visited.size(); j++)
				{
					// Check if both vertices are already visited
					if (Triangulator.closeTo(visited.get(j), current.getA()))
					{
						visitedVerts++;
					}

					if (Triangulator.closeTo(visited.get(j), current.getB()))
					{
						visitedVerts++;
					}
				}

				// If 0 verts have been visited, it hasn't been reached by traversal yet
				// If 2 verts have been visited, the entire edge has already been accounted for
				if (visitedVerts == 1)
				{
					double length = Vector2.Distance(current.getA(), current.getB());
					if (length < shortestEdgeLength)
					{
						shortestEdgeLength = length;
						shortestEdge = current;
						running = true;
					}
				}
			}

			if (running)
			{
				mst.add(shortestEdge);

				// Remove points from this edge as it has just been visited
				for (int i = 0; i < unvisited.size(); i++)
				{
					Vector2 current = unvisited.get(i);

					if (Triangulator.closeTo(current, shortestEdge.getA()) || Triangulator.closeTo(current, shortestEdge.getB()))
					{
						// Remove and fix index for iteration
						unvisited.remove(current);
						i--;
					}
				}

				// Add edge mst and its vertices to visited
				visited.add(shortestEdge.getA());
				visited.add(shortestEdge.getB());
				mst.add(shortestEdge);
			}
			else
			{
				break;
			}
		}

		return mst;
	}

	// PUBLIC METHODS:

	// Method to get the tilemap being ussed
	public Tilemap getTilemap()
	{
		return tilemap;
	}

	public Vector2 getTilePosition(double x, double y)
	{
		int tileX = (int)(x / getTilemap().getTileSize());
		int tileY = (int)(y / getTilemap().getTileSize());
		return new Vector2(tileX, tileY);
	}

	// Method to get if the tile at [x][y] is a floor tile or not
	public boolean getFloorTile(int x, int y)
	{
		if (x < 0 || x >= width || y < 0 || y >= height)
		{
			return false;
		}
		
		return floorTiles[x][y];
	}

	// Method to get the start room in the map
	public Rectangle getStartRoom()
	{
		return startRoom;
	}
	
	// Method to get the end room in the map
	public Rectangle getEndRoom()
	{
		return endRoom;
	}


	// Method to draw the map's tiles onto the screen
    public void draw(Vector2 cameraPos, WritableImage renderImg)
	{
		if (ditheringEnabled)
		{
			ditherShrink = Util.lerp(ditherShrink, 1, 0.1);
		}
		else
		{
			ditherShrink = Util.lerp(ditherShrink, 0, 0.01);
		}

		// Get pixel reader and writer to draw from tileset to screen
		PixelReader reader = tilemap.getTilesImg().getPixelReader();
		PixelWriter writer = renderImg.getPixelWriter();
		int tileSize = tilemap.getTileSize();

		// TODO: replace with player position ...
		double ditherX = GameManager.getPlayer().getPosition().x - cameraPos.x;
		double ditherY = GameManager.getPlayer().getPosition().y - cameraPos.y;

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				// Destination x and y coordinate of tile to be drawn
				int destX = x * tileSize - (int)cameraPos.x;
				int destY = y * tileSize - (int)cameraPos.y;

				for (int height = 0; height < tiles[x][y].length; height++)
				{
					int tileID = tiles[x][y][height];

					// Source x and y coordinate of tile to be drawn from tilemap
					int srcY = tileID / (int)(tilemap.getTilesImg().getWidth() / tileSize);
					int srcX = tileID - (srcY * (int)(tilemap.getTilesImg().getWidth() / tileSize));

					srcX *= tileSize;
					srcY *= tileSize;
					
					// Make sure destination coordinate is onscreen
					if (destX >= 0 && destX + tileSize < renderImg.getWidth() && destY >= 0 && destY + tileSize < renderImg.getHeight())
					{
						// Never dither 'base' of tilemap or back tiles, quick draw tiles outisde of dither range
						if (!tilemap.isBackWall(tileID) || getDitherIntensity(ditherX, ditherY, destX, destY) > 86)
						{
							writer.setPixels(destX, destY, tileSize, tileSize, reader, srcX, srcY);
						}
						else
						{
							for (int localX = 0; localX < tileSize; localX++)
							{
								for (int localY = 0; localY < tileSize; localY++)
								{
									double ditherIntensity =  getDitherIntensity(ditherX, ditherY, destX + localX, destY + localY);
									int ditherStep = (int)Math.ceil(Math.pow(0.02 * ditherIntensity, -1));

									if (localX % ditherStep == 0 && localY % ditherStep == 0)
									{
										writer.setArgb(destX + localX, destY + localY, reader.getArgb(srcX + localX, srcY + localY));
									}
								}
							}
						}
					}

					destY -= tileSize;
				}
			}
		}
    }

	public void setDithering(boolean dither)
	{
		ditheringEnabled = dither;
	}

	private double getDitherIntensity(double srcX, double srcY, double destX, double destY)
	{
		return Math.sqrt(Math.pow(srcX - destX, 2) + Math.pow(srcY - destY, 2)) * ditherShrink;
	}
}
