package application;

import java.util.ArrayList;

import application.World.Map;
import javafx.geometry.Bounds;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;

public class Player
{
	
	// Player movement
	private Vector2 position;
	private Vector2 velocity;
	private Vector2 facing;
	private Vector2 radius;

	private final double movementSpeed = 2;
	
	// Combat
	private int hp;
	
	// Guns
	private ArrayList<Gun> guns;
	private int currentGun;
	private Rectangle mask;
	
	// Constructor
	public Player()
	{
		// Default 4 hp
		hp = 4;

		// Initialize movement variables and dimensions
		position = new Vector2();
		velocity = new Vector2();
		radius = new Vector2(8, 8);
		facing = new Vector2();
		
		// Create player mask
		mask = new Rectangle();
		updateMask();

		// Initialize guns and grant player default weapon
		guns = new ArrayList<Gun>();
		guns.add(new DefaultGun(true));
		currentGun = 0;
	}
	
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
			if (wallDist <= 0)
			{
				velocity.y = -wallDist;
			}
		}

		// Set dithering enabled if view is obstructed by wall
		Vector2 ditherCheckPos = map.getTilePosition(position.x, position.y + radius.y);
		map.setDithering(!map.getFloorTile((int)ditherCheckPos.x, (int)ditherCheckPos.y + 2) || bottomWall);

		// Move player and update bounds
		position.x += velocity.x;
		position.y += velocity.y;
		updateMask();

		// Move camera
		Vector2 camPos = new Vector2(position.x - AppProps.BASE_WIDTH / 2, position.y - AppProps.BASE_HEIGHT / 2);
		Camera.setPos(Vector2.Lerp(Camera.getPos(), camPos, 0.4));

		// Update direction facing based on mouse position
		facing = Vector2.Normalize(Vector2.Subtract(InputManager.getMouseWorldPos(), position));
	}
	public void updateMask()
	{
		double adjustedWidth = radius.x * 1.8;
		double adjustedHeight = radius.y * 1.8;

		mask.setX(position.x - adjustedWidth / 2);
		mask.setY(position.y - adjustedHeight / 2);
		mask.setWidth(adjustedWidth);
		mask.setHeight(adjustedHeight);
	}
	public void draw(WritableImage img)
	{
		// Get pixel writer to draw player
		PixelWriter writer = img.getPixelWriter();

		// Draw pixels to fill player bounds
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

		int realX = (int)(position.x + facing.x * 20 - Camera.getPos().x);
		int realY = (int)(position.y + facing.y * 20 - Camera.getPos().y);
		if (realX >= 0 && realX < img.getWidth() && realY >= 0 && realY < img.getHeight())
		{
			writer.setArgb(realX, realY, 0xFFFF0000);
		}
	}
	
	// Run whenever the player clicks
	public void click()
	{
		if (currentGun >= 0 && currentGun < guns.size())
		{
			guns.get(currentGun).fire(position.x, position.y, facing.x, facing.y);
		}
	}
	
	// Game mechanic methods
	public void damage()
	{
		// Decrement health and update hp counter
		hp--;
		UI.updateHealth(hp);

		if (hp == 0)
		{
			System.out.print("Die player");
		}
	}

	// Accessor methods
	public Vector2 getPosition()
	{
		return position;
	}
	public Bounds getMask()
	{
		return mask.getBoundsInParent();
	}
	
	// Mutator methods
	public void setPosition(double x, double y)
	{
		position.x = x;
		position.y = y;
	}
}
