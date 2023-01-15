package application;

import application.World.Map;

public class Player
{
	private double health;
	
	// Player movement
	private Vector2 position;
	private Vector2 velocity;
	private Vector2 dimensions;

	private final double movementSpeed = 1;
	
	// Constructor
	public Player()
	{
		health = 100;
		position = new Vector2();
		velocity = new Vector2();

		dimensions = new Vector2(20, 20);
	}
	
	//
	public void update(double timeMultiplier)
	{
		// Set velocity based on input
		velocity = Vector2.Multiply(InputManager.getDirectionalInput(), movementSpeed);
		
		// Move player
		position.x += velocity.x * timeMultiplier;
		position.y += velocity.y * timeMultiplier;
		
		Map map = GameManager.getMap();
		
		// ** needed to 'scale' coordinates to tile
		int x = (int)(position.x / map.getTilemap().getTileSize());
		int y = (int)(position.y / map.getTilemap().getTileSize());

		// Check if inside wall on each side
		boolean leftWall = map.getFloorTile(x - 1, y);
		boolean rightWall = map.getFloorTile(x + 1, y);
		boolean topWall = map.getFloorTile(x, y - 1);
		boolean bottomWall = map.getFloorTile(x, y - 1);
		
		// TODO: respond to collisions
	}
	
	public Vector2 getPosition()
	{
		return position;
	}
}
