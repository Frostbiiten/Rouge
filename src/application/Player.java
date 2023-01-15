package application;

import application.World.Map;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Player
{
	private double health;
	
	// Player movement
	private Vector2 position;
	private Vector2 velocity;
	private Vector2 radius;

	private final double movementSpeed = 2;
	
	// Constructor
	public Player()
	{
		health = 100;
		position = new Vector2();
		velocity = new Vector2();

		radius = new Vector2(8, 8);
	}
	
	//
	public void update()
	{
		// Set velocity based on input
		velocity = Vector2.Multiply(InputManager.getDirectionalInput(), movementSpeed);
		
		
		Map map = GameManager.getMap();
		int tileSize = map.getTilemap().getTileSize();
		
		// ** needed to 'scale' coordinates to tile
		Vector2 leftTilePos = map.getTilePosition(position.x - radius.x, position.y);
		Vector2 rightTilePos = map.getTilePosition(position.x + radius.x, position.y);
		Vector2 topTilePos = map.getTilePosition(position.x, position.y - radius.y);
		Vector2 bottomTilePos = map.getTilePosition(position.x, position.y + radius.y);

		// Check if inside wall on each side
		boolean leftWall = !map.getFloorTile((int)leftTilePos.x - 1, (int)leftTilePos.y);
		boolean rightWall = !map.getFloorTile((int)rightTilePos.x + 1, (int)rightTilePos.y);
		boolean topWall = !map.getFloorTile((int)topTilePos.x, (int)topTilePos.y - 1);
		boolean bottomWall = !map.getFloorTile((int)bottomTilePos.x, (int)bottomTilePos.y + 1);

		if (leftWall)
		{
			double wallDist = (position.x - radius.x + velocity.x) - (leftTilePos.x * tileSize);
			if (wallDist <= 0)
			{
				velocity.x = -wallDist;
			}
		}

		if (rightWall)
		{
			// Subtract tileSize because right wall is on the other side of the tile
			double wallDist = tileSize - ((position.x + radius.x + velocity.x) - (rightTilePos.x * tileSize));
			if (wallDist <= 0)
			{
				velocity.x = -wallDist;
			}
		}

		if (topWall)
		{
			double wallDist = ((position.y - radius.y + velocity.y) - (topTilePos.y * tileSize));
			if (wallDist <= 0)
			{
				velocity.y = -wallDist;
			}
		}

		if (bottomWall)
		{
			double wallDist = tileSize - ((position.y + radius.y + velocity.y) - (bottomTilePos.y * tileSize));
			System.out.println(wallDist);
			if (wallDist <= 0)
			{
				velocity.y = -wallDist;
			}
		}

		map.setDithering(bottomWall);

		// Move player
		position.x += velocity.x;
		position.y += velocity.y;
		

		// Move camera
		Vector2 camPos = new Vector2(position.x - AppProps.BASE_WIDTH / 2, position.y - AppProps.BASE_HEIGHT / 2);
		Camera.setPos(Vector2.Lerp(Camera.getPos(), camPos, 0.4));
	}

	public void draw(WritableImage img)
	{
		PixelWriter writer = img.getPixelWriter();

		for (int localX = -(int)radius.x; localX < radius.x; localX++)
		{
			for (int localY = -(int)radius.y; localY < radius.y; localY++)
			{
				int realX = (int)(position.x + localX - Camera.getPos().x);
				int realY = (int)(position.y + localY - Camera.getPos().y);

				// Make sure in range
				if (realX >= 0 && realX < img.getWidth() && realY >= 0 && realY < img.getHeight())
				{
					writer.setArgb(realX, realY, 0xFFFF0000);
				}
			}
		}
	}
	
	public Vector2 getPosition()
	{
		return position;
	}

	public void setPosition(double x, double y)
	{
		position.x = x;
		position.y = y;
	}
}
