package application.World;

import java.util.ArrayList;
import java.util.Random;

import application.Camera;
import application.GameManager;
import application.Vector2;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Map
{
	private int width, height;
	private int[][] tiles;
	private Tilemap tilemap;

	// Map generation
	private ArrayList<Rectangle> rooms;
	private ArrayList<Edge> hallways;
	private ArrayList<Edge> graph;

	private Rectangle startRoom;
	private Rectangle endRoom;

	public Map(int width, int height, int roomCount, Tilemap tilemap)
	{
		// Initialize width, height and tilemap
		this.width = width;
		this.height = height;
		this.tilemap = tilemap;
		
		int maxWidth = 20, minWidth = 12, maxHeight = 20, minHeight = 12;

		// Random room generation
		Random rand = new Random();
		rooms = new ArrayList<Rectangle>();
		ArrayList<Vector2> roomPositions = new ArrayList<Vector2>();

		Rectangle roomBounds = new Rectangle(0, 0, width, height);

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

			Rectangle rect = new Rectangle(rand.nextInt(width), rand.nextInt(height), rand.nextInt(maxWidth - minWidth) + minWidth, rand.nextInt(maxHeight - minHeight) + minHeight);

			// Skip if rectangle is outside of bounds
			if (rect.getX() + rect.getWidth() > width - 1 || rect.getY() + rect.getHeight() > height - 1)
			{
				continue;
			}

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

			if (valid)
			{
				// Reset attempts as room is valid
				attempts = 0;

				// Add to list of rooms along with position
				rooms.add(rect);
				roomPositions.add(new Vector2(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2));
			}
		}

		// Triangulate graph using bowyer watson algorithm
		Triangulator triangulator = new Triangulator();
		graph = triangulator.triangulate(roomPositions);
		hallways = new ArrayList<Edge>(graph);

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
	
		// Array to store if hallway waws created at [x][y] or not (default false)
		boolean[][] floorTiles = new boolean[width][height];

		// Use a* algorithm to find paths
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

			// Add initial point to queue with 0 cost in queue, with previous as self
			queue.add(new int[]{(int)beginVec.x, (int)beginVec.y, 0});

			visited[(int)beginVec.x][(int)beginVec.y] = 0;
			previousNodes[(int)beginVec.x][(int)beginVec.y] = new int[]{(int)beginVec.x, (int)beginVec.y};

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

				for (int offsetIndex = 0; offsetIndex < neighborOffsets.length; offsetIndex++)
				{
					int[] currentOffset = neighborOffsets[offsetIndex];
					int newSpotX = nodeX + currentOffset[0];
					int newSpotY = nodeY + currentOffset[1];

					// Make sure new spot is in the map's range
					if (newSpotX >= 0 && newSpotX < width && newSpotY >= 0 && newSpotY < height
						&& visited[newSpotX][newSpotY] != 1) // Make sure it's either in queue or visited
					{
						// Get new cost of neighbor from this node
						int newNeighborCost = currentGCost + 1;

						// Use manhattan distance to find heuristics for neighbor
						int gCost = Math.abs(newSpotX - (int)beginVec.x) + Math.abs(newSpotY - (int)beginVec.y); // Distance from begin

						// Check if the distance is more optimal and the node is not already in queue
						if (newNeighborCost < gCost || visited[newSpotX][newSpotY] != 0)
						{
							int hCost = Math.abs(newSpotX - (int)endVec.x) + Math.abs(newSpotY - (int)endVec.y); // Distance from end

							// Alter heuristic based on map
							int fCost = (gCost + hCost) * 10;
							if (!floorTiles[newSpotX][newSpotY]) // Prefer to be in hall
							{
								fCost += 5;
							}

							/*
							for (int room = 0; room < rooms.size(); room++)
							{
								if (rooms.get(room).getBoundsInParent().contains(endVec.x, endVec.y))
								{
									continue;
								}

								if (rooms.get(room).getBoundsInParent().contains(newSpotX, newSpotY))
								{
									fCost += 1;
								}
							}
							*/

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

		// Shrink rooms for padding/spacing purposes
		for (int i = 0; i < rooms.size(); i++)
		{
			Rectangle currentRoom = rooms.get(i);
			currentRoom.setWidth(currentRoom.getWidth() - 4);
			currentRoom.setX(currentRoom.getX() + 2);
			currentRoom.setHeight(currentRoom.getHeight() - 4);
			currentRoom.setY(currentRoom.getY() + 2);

			for (int x = (int)currentRoom.getX(); x < (int)currentRoom.getX() + currentRoom.getWidth(); x++)
			{
				for (int y = (int)currentRoom.getY(); y < (int)currentRoom.getY() + currentRoom.getHeight(); y++)
				{
					floorTiles[x][y] = true;
				}
			}
		}

		// Get graph "components" using flood fill algorithm

		// 'visited' array to store which component a tile belongs to
		int component[][] = new int[width][height];
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				component[x][y] = -1;
			}
		}

		// Arraylist of components. [0] = component num, [1] = component size
		ArrayList<int[]> components = new ArrayList<int[]>();

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

						// Add adjacent nodes
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

		// Use insertion sort to sort components by size
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

		// Get largest component (arraylist is sorted from least to greatestb)
		int[] largestComponent = components.get(components.size() - 1);

		// If id is not largestComponent, then set it to empty, leaving only the largest component
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

		// Print
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
			System.out.println();
		}
		System.out.println();

		// To check if room hasn't been removed, just check if the center tile is true in the floorTiles array
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

		// Sort rooms by distance from center
		Vector2 mapCenter = new Vector2(width / 2, height / 2);
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
		Vector2 startRoomPos = new Vector2(startRoom.getX() + startRoom.getWidth() / 2, startRoom.getY() + startRoom.getHeight() / 2);

		// Sort again to get furthest from start room to assign to end room
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

		endRoom = rooms.get(rooms.size() - 1);

		// Create tiles 2d array
		tiles = new int[width][height];

		// Basic
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

    public void draw(Vector2 cameraPos, WritableImage renderImg)
	{
		// Get pixel reader and writer to draw from tileset to screen
		PixelReader reader = tilemap.getTilesImg().getPixelReader();
		PixelWriter writer = renderImg.getPixelWriter();
		int tileSize = tilemap.getTileSize();

		Canvas c = GameManager.getCanvas();
		GraphicsContext ct = c.getGraphicsContext2D();
		ct.clearRect(0, 0, c.getWidth(), c.getHeight());

		ct.setFill(Color.RED);
		for (int i = 0; i < rooms.size(); i++)
		{
			Rectangle current = rooms.get(i);
			ct.fillRect(current.getX() - cameraPos.x, current.getY() - cameraPos.y, current.getWidth(), current.getHeight());
		}

		ct.setLineWidth(3);
		ct.setStroke(Color.BLACK);
		for (int i = 0; i < graph.size(); i++)
		{
			Edge current = graph.get(i);
			Vector2 a = Vector2.Subtract(current.getA(), Camera.getPos());
			Vector2 b = Vector2.Subtract(current.getB(), Camera.getPos());
			ct.strokeLine(a.x, a.y, b.x, b.y);
		}

		ct.setStroke(Color.CYAN);
		for (int i = 0; i < hallways.size(); i++)
		{
			Edge current = hallways.get(i);
			Vector2 a = Vector2.Subtract(current.getA(), Camera.getPos());
			Vector2 b = Vector2.Subtract(current.getB(), Camera.getPos());
			ct.strokeLine(a.x, a.y, b.x, b.y);
		}

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
