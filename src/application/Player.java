package application;

import java.util.ArrayList;

import application.World.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Player
{
	// Player movement
	private final double movementSpeed = 2;
	private Vector2 position;
	private Vector2 velocity;
	private Vector2 facing;
	private Vector2 radius;

	// Rolling
	private final double rollSpeed = 4;
	private Timeline rollTimeline;
	private Vector2 rollDirection;
	private boolean rolling;
	private int rollStage;
	
	// Combat
	private int hp;
	private Rectangle mask;
	private int damageCooldownClock;
	
	// Guns
	private ArrayList<Gun> guns;
	private int currentGun;
	private ImageView gunView;
	private Vector2 currentGunPos;
	private Vector2 recoilOffset;

	// Dust
	private int dustSpawnCounter;
	private final int dustSpawnInterval = 20;
	
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
		currentGunPos = new Vector2();
		recoilOffset = new Vector2();
		
		// Create player mask
		mask = new Rectangle();
		updateMask();

		// Initialize guns and grant player default weapon
		guns = new ArrayList<Gun>();
		guns.add(new DefaultGun(true));
		guns.add(new FastGun(true));
		guns.add(new RocketGun(true));
		currentGun = 0;

		// Create imageview for gun and add to pane
		gunView = new ImageView(guns.get(currentGun).getImage());

		// Dust fields
		dustSpawnCounter = 0;

		// Set up timeline for rolling
		rollTimeline = new Timeline(new KeyFrame(Duration.seconds(0.1), new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				rolling = rollStage > 3;
				rollStage--;
				if (rollStage == 0)
				{
					rollTimeline.stop();
				}
			}
		}));
		rollTimeline.setCycleCount(Timeline.INDEFINITE);

		// Default roll fields
		rollStage = 0;
		rollDirection = new Vector2();
		rolling = false;
	}
	
	public void update()
	{
		// Countdown damage invunerability timer
		if (damageCooldownClock > 0)
		{
			damageCooldownClock--;
		}

		// Set velocity based on input/rolling
		if (rolling)
		{
			velocity = Vector2.Multiply(rollDirection, rollSpeed);
		}
		else
		{
			if (InputManager.getDirectionalInput().x != 0 || InputManager.getDirectionalInput().y != 0)
			{
				velocity = Vector2.Multiply(Vector2.Normalize(InputManager.getDirectionalInput()), movementSpeed);
			}
			else
			{
				velocity.x = 0;
				velocity.y = 0;
			}
		}
		
		Map map = GameManager.getMap();
		int tileSize = map.getTilemap().getTileSize();
		
		// ** needed to 'scale' coordinates to tile
		Vector2 leftTilePos = map.getTilePosition(position.x - radius.x, position.y - radius.y);
		Vector2 rightTilePos = map.getTilePosition(position.x + radius.x, position.y - radius.y);
		Vector2 topTilePos = map.getTilePosition(position.x - radius.x, position.y - radius.y);
		Vector2 bottomTilePos = map.getTilePosition(position.x - radius.x, position.y + radius.y);

		// Check if inside wall on each side
		boolean leftWall = !(map.getFloorTile((int)leftTilePos.x - 1, (int)leftTilePos.y) || map.getFloorTile((int)leftTilePos.x - 1, (int)leftTilePos.y + 1));
		boolean rightWall = !(map.getFloorTile((int)leftTilePos.x + 1, (int)leftTilePos.y) || map.getFloorTile((int)leftTilePos.x + 1, (int)leftTilePos.y + 1));
		boolean topWall = !map.getFloorTile((int)topTilePos.x, (int)topTilePos.y - 1);
		boolean bottomWall = !map.getFloorTile((int)bottomTilePos.x, (int)bottomTilePos.y + 1);

		// Interpolate recoil offset back to 0
		recoilOffset = Vector2.Lerp(recoilOffset, Vector2.ZERO, 0.5);

		// Get target gun position with all variables that affect its position considered
		Vector2 targetGunPos = new Vector2(
			(int)(position.x - Camera.getX() - (gunView.getImage().getWidth() / 2 / AppProps.SCALE) + (radius.x * 1.5 * facing.x) + recoilOffset.x) * AppProps.SCALE,
			(int)(position.y - Camera.getY() - (gunView.getImage().getHeight() / 2 / AppProps.SCALE) + (radius.y * 1.5 * facing.y) + recoilOffset.y) * AppProps.SCALE
		);

		// Smoothly move gun towawrds target location
		currentGunPos = Vector2.Lerp(currentGunPos, targetGunPos, 0.5);

		gunView.setX(currentGunPos.x);
		gunView.setY(currentGunPos.y);

		if (InputManager.leftMousePressed())
		{
			if (guns.get(currentGun).autofireEnabled())
			{
				click(true);
			}
		}
		
		// Rotate weapon according direction being faced
		if (facing.x > 0)
		{
			gunView.setScaleX(1);
			gunView.setRotate(Math.toDegrees(Math.atan2(facing.y, facing.x)));
		}
		else
		{
			gunView.setScaleX(-1);
			gunView.setRotate(180 + Math.toDegrees(Math.atan2(facing.y, facing.x)));
		}

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

		dustSpawnCounter++;
		if(dustSpawnCounter >= dustSpawnInterval && (Math.abs(velocity.x) > 0.5 || Math.abs(velocity.y) > 0.5))
		{
			VFX.spawnDust((int)(position.x - velocity.x), (int)(position.y - velocity.y));
			dustSpawnCounter = 0;
		}

		// Move player and update bounds
		position.x += velocity.x;
		position.y += velocity.y;
		updateMask();

		// Move camera
		Vector2 camPos = new Vector2(
			position.x - AppProps.BASE_WIDTH / 2 + (InputManager.getMousePos().x - AppProps.REAL_WIDTH / 2) * 0.1,
			position.y - AppProps.BASE_HEIGHT / 2 + (InputManager.getMousePos().y - AppProps.REAL_HEIGHT / 2) * 0.12);
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
	public void click(boolean left)
	{
		if (left)
		{
			if (currentGun >= 0 && currentGun < guns.size())
			{
				if (guns.get(currentGun).fire(position.x, position.y, facing.x, facing.y))
				{
					recoilOffset.x -= facing.x * 50;
					recoilOffset.y -= facing.y * 50;
				}
			}

			UI.updateWeapon();
		}
		else
		{
			// Roll if not already rolling, cooldown has expired and input is being received
			if (rollStage == 0 && (InputManager.getDirectionalInput().x != 0 || InputManager.getDirectionalInput().y != 0))
			{
				rollDirection = Vector2.Normalize(InputManager.getDirectionalInput());
				roll();
			}
		}
	}

	// Run whenever the player scrolls
	public void scroll(double delta)
	{
		guns.get(currentGun).cancelReload();

		// Scroll through different weapons, use modulo to limit range
		currentGun += delta;
		currentGun = Math.floorMod(currentGun, guns.size());
		UI.updateWeapon();
		gunView.setImage(guns.get(currentGun).getImage());
	}
	
	// Game mechanic methods
	public void damage()
	{
		Camera.shakeCamera(4, 0.7, 1);

		// Don't take damage during cooldown or roll
		if (damageCooldownClock > 0 || rolling)
		{
			return;
		}

		// Decrement health and update hp counter
		hp--;
		UI.updateHealth(hp);
		damageCooldownClock = 60;

		if (hp == 0)
		{
			System.out.print("Die player");
		}
	}
	public void updateGun()
	{
		gunView.setImage(guns.get(currentGun).getImage());
		UI.updateWeapon();
	}
	public void roll()
	{
		rollStage = 5;
		rolling = true;
		rollTimeline.play();
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
    public Gun getWeapon()
	{
		return guns.get(currentGun);
    }
	public ImageView getGunNode()
	{
		return gunView;
	}
	public Vector2 getFacingDir()
	{
		return facing;
	}

	// Mutator methods
	public void setPosition(double x, double y)
	{
		position.x = x;
		position.y = y;
	}
}
