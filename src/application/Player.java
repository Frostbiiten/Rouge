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

	// Misc
	private ArrayList<Rectangle> completedRooms;
	private Rectangle currentRoom;
	private Rectangle activeRoom;
	
	// Points and level completion
	private boolean levelCompleted;
	private boolean dead;
	
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

		// Initialize gun and grant player default weapon
		guns = new ArrayList<Gun>();
		guns.add(new DefaultGun(true));
		currentGun = 0;

		// Create imageview for gun and add to pane
		gunView = new ImageView(guns.get(currentGun).getImage());

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

		// Set up completed rooms arraylist to keep track of what rooms have been completed
		completedRooms = new ArrayList<Rectangle>();

		// Start with level not completed yet
		levelCompleted = false;
		dead = false;
	}
	
	public void update()
	{
		if (dead)
		{
			return;
		}

		if (levelCompleted)
		{
			// Get the x position of the room's center
			double roomCenterX = GameManager.getMap().getEndRoom().getX() + GameManager.getMap().getEndRoom().getWidth() / 2;
			double roomCenterY = GameManager.getMap().getEndRoom().getY() + GameManager.getMap().getEndRoom().getHeight() / 2;

			// Move player to center of room
			position.x = Util.lerp(position.x, roomCenterX, 0.05);
			position.y = Util.lerp(position.y, roomCenterY, 0.05);

			// When the player gets close enough to the center, send them to the next level
			if (Math.abs(roomCenterX - position.x) < 5 && Math.abs(roomCenterY - position.y) < 5)
			{
				GameManager.play();
			}
		}

		// Countdown damage invunerability timer
		if (damageCooldownClock > 0)
		{
			damageCooldownClock--;
		}

		Map map = GameManager.getMap();
		int tileSize = map.getTilemap().getTileSize();

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

		// Get current room
		currentRoom = map.getRoom(position.x, position.y);
		if (currentRoom != null)
		{
			UI.addMinimapRoom(currentRoom);

			if (currentRoom == GameManager.getMap().getEndRoom())
			{
				levelCompleted = true;
			}

			if (currentRoom != activeRoom && !completedRooms.contains(currentRoom))
			{
				activeRoom = currentRoom;
				GameManager.startRoom(activeRoom);
			}
		}
		
		Vector2 minTilePos = map.getTilePosition(position.x - radius.y + 1, position.y - radius.x + 1);
		Vector2 midTilePos = map.getTilePosition(position.x, position.y);
		Vector2 maxTilePos = map.getTilePosition(position.x + radius.y - 1, position.y + radius.x - 1);

		// Check if inside wall on each side
		boolean leftWall = !(
			map.getFloorTile((int)midTilePos.x - 1, (int)minTilePos.y) &&
			map.getFloorTile((int)midTilePos.x - 1, (int)midTilePos.y) &&
			map.getFloorTile((int)midTilePos.x - 1, (int)maxTilePos.y)
		);

		boolean rightWall = !(
			map.getFloorTile((int)midTilePos.x + 1, (int)minTilePos.y) &&
			map.getFloorTile((int)midTilePos.x + 1, (int)midTilePos.y) &&
			map.getFloorTile((int)midTilePos.x + 1, (int)maxTilePos.y)
		);

		boolean topWall = !(
			map.getFloorTile((int)minTilePos.x, (int)midTilePos.y - 1) &&
			map.getFloorTile((int)midTilePos.x, (int)midTilePos.y - 1) &&
			map.getFloorTile((int)maxTilePos.x, (int)midTilePos.y - 1)
		);

		boolean bottomWall = !(
			map.getFloorTile((int)minTilePos.x, (int)midTilePos.y + 1) &&
			map.getFloorTile((int)midTilePos.x, (int)midTilePos.y + 1) &&
			map.getFloorTile((int)maxTilePos.x, (int)midTilePos.y + 1)
		);


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
			double wallDist = (position.x + radius.x + velocity.x) - midTilePos.x * tileSize - tileSize;
			if (wallDist < 0)
			{
				velocity.x -= wallDist;
			}
		}

		if (rightWall)
		{
			double wallDist = tileSize - ((position.x + radius.x + velocity.x) - midTilePos.x * tileSize);
			if (wallDist < 0)
			{
				velocity.x += wallDist;
			}
		}

		if (topWall)
		{

			double wallDist = (position.y + radius.y + velocity.y) - midTilePos.y * tileSize - tileSize;
			if (wallDist < 0)
			{
				velocity.y -= wallDist;
			}
		}

		if (bottomWall)
		{
			double wallDist = tileSize - ((position.y + radius.y + velocity.y) - midTilePos.y * tileSize);
			if (wallDist < 0)
			{
				velocity.y += wallDist;
			}
		}

		// Move player
		position.x += velocity.x;
		position.y += velocity.y;

		// Keep within room if necessary
		if (activeRoom != null)
		{
			double oldX = position.x;
			double oldY = position.y;

			position.x = Math.max(activeRoom.getX() - radius.x, position.x);
			position.x = Math.min(activeRoom.getX() + activeRoom.getWidth() + radius.x, position.x);
			position.y = Math.max(activeRoom.getY() - radius.y, position.y);
			position.y = Math.min(activeRoom.getY() + activeRoom.getHeight() + radius.y, position.y);

			if (oldX != position.x || oldY != position.y)
			{
				UI.setLabelInfo("CLEAR THE ROOM BEFORE CONTINUING!", 300);
			}
		}

		// Update bounds
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
		// Return if already dead
		if (dead)
		{
			return;
		}

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
		// Return if already dead
		if (dead)
		{
			return;
		}

		// Cancel reload when weapon changed
		guns.get(currentGun).cancelReload();

		// Truncate delta to either +1 or -1 (no variable scrolling)
		if (delta > 0)
		{
			delta = 1;
		}
		else
		{
			delta = -1;
		}

		// Scroll through different weapons, use modulo to limit range
		currentGun += delta;
		currentGun = Math.floorMod(currentGun, guns.size());
		gunView.setImage(guns.get(currentGun).getImage());
		UI.updateWeapon();
	}
	
	// Game mechanic methods
	public void damage()
	{
		// Don't take damage during cooldown or roll
		if (damageCooldownClock > 0 || rolling)
		{
			return;
		}

		// Decrement health, update hp counter, shake camera and add critical hit effect
		hp--;
		UI.updateHealth(hp);
		damageCooldownClock = 60;
		Camera.shakeCamera(4, 0.7, 1);
		VFX.spawnCritical(position.x, position.y);

		// Kill player if necessary
		if (hp == 0)
		{
			dead = true;

			for (int i = 0; i < 4; i++)
			{
				VFX.spawnCritical(position.x + Math.random() * 40 - 20, position.y + Math.random() * 40 - 20);
			}

			GameManager.gameOver();
			UI.gameOver();
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
    public void addHealth(int health)
	{
		// Clamp hp within range if player is below max health
		if (hp < 4)
		{
			hp += health;

			// Limit to 4 hp without override
			hp = (int)Math.min(hp, 4);
		}
		UI.updateHealth(hp);
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
    public ArrayList<Gun> getWeapons()
	{
		return guns;
    }
    public int getHp()
	{
		return hp;
    }
	public ImageView getGunNode()
	{
		return gunView;
	}
	public Vector2 getFacingDir()
	{
		return facing;
	}
    public int getWeaponIndex()
	{
		  return currentGun;
    }
	public Rectangle getCurrentRoom()
	{
		return currentRoom;
	}
	public boolean getDead()
	{
		return dead;
	}

	// Method to get last active room player current is in
	public Rectangle getActiveRoom()
	{
		return activeRoom;
	}
    public void clearRoom(Rectangle room)
	{
		completedRooms.add(room);
		activeRoom = null;
    }
	public void addWeapon(Gun gun)
	{
		// Iterate through each gun
		for (int i = 0; i < guns.size(); i++)
		{
			// Check if gun is already owned by player by comparing name
			if (guns.get(i).getName().equals(gun.getName()))
			{
				// Refill the gun if it is already owned
				guns.get(i).refill();
				UI.updateWeapon();
				return;
			}
		}

		// If here is reached, the player must not have owned the gun, so grant it
		guns.add(gun);
	}

	// Mutator methods
	public void setPosition(double x, double y)
	{
		position.x = x;
		position.y = y;
	}
	public void setHp(int hp)
	{
		this.hp = hp;
		UI.updateHealth(hp);
	}
    public void setWeapons(ArrayList<Gun> weapons)
	{
		guns = weapons;
    }
    public void setWeaponIndex(int index)
	{
		currentGun = index;
		gunView.setImage(guns.get(currentGun).getImage());
		UI.updateWeapon();
    }
}
