package application;

import application.World.Map;
import javafx.scene.image.ImageView;

public class NormalEnemy extends Enemy
{
	// Movement
	private final double runSpeed = 1;
	private Vector2 movementVector;
	private boolean running;

	// Combat
	private Gun gun;
	private Vector2 recoilOffset;
	private Vector2 currentGunPos;

	NormalEnemy(String name, Vector2 position)
	{
		super(name, 6, 6, position, 3.5, new Vector2(6, 8), 3, 3);
		running = false;

		// Choose random gun type
		int randID = (int)(Math.random() * 3);
		if (randID == 0)
		{
			gun = new DefaultGun(false);
		}
		else if (randID == 1)
		{
			gun = new FastGun(false);
		}
		else if (randID == 2)
		{
			gun = new RocketGun(false);
		}

		// Add 2 above to be on top of the enemy's actual sprites
		gunView = new ImageView(gun.getImage());
		GameManager.getRoot().getChildren().add(GameManager.getBgDepth() + 3, gunView);
		
		// Initialize vectors to default
		movementVector = new Vector2();
		recoilOffset = new Vector2();
		currentGunPos = new Vector2();
	}

	@Override
	void update()
	{
		// Move and update mask
		if (running)
		{
			position.x += movementVector.x;
			position.y += movementVector.y;
			updateMask();
		}

		Map map = GameManager.getMap();
		Vector2 tilePos = map.getTilePosition(position.x, position.y);
		if (!map.getFloorTile((int)tilePos.x - 1, (int)tilePos.y))
		{
			movementVector.x = 0;
		}

		if (!map.getFloorTile((int)tilePos.x + 1, (int)tilePos.y))
		{
			movementVector.x = 0;
		}

		if (!map.getFloorTile((int)tilePos.x, (int)tilePos.y - 1))
		{
			movementVector.y = 0;
		}

		if (!map.getFloorTile((int)tilePos.x, (int)tilePos.y + 1))
		{
			movementVector.y = 0;
		}

		Vector2 playerDirection = Vector2.Normalize(Vector2.Subtract(GameManager.getPlayer().getPosition(), position));

		// Interpolate recoil offset back to 0
		recoilOffset = Vector2.Lerp(recoilOffset, Vector2.ZERO, 0.5);

		Vector2 targetGunPos = new Vector2(
			(int)(position.x - Camera.getX() + recoilOffset.x + playerDirection.x * 10) * AppProps.SCALE - gunView.getImage().getWidth() / 2,
			(int)(position.y - Camera.getY() + recoilOffset.y + playerDirection.y * 10) * AppProps.SCALE - gunView.getImage().getHeight() / 2
		);

		// Smoothly move gun towawrds target location
		currentGunPos = Vector2.Lerp(currentGunPos, targetGunPos, 0.5);
		gunView.setX(currentGunPos.x);
		gunView.setY(currentGunPos.y);

		// Rotate weapon according direction being faced
		if (playerDirection.x > 0)
		{
			gunView.setScaleX(1);
			gunView.setRotate(Math.toDegrees(Math.atan2(playerDirection.y, playerDirection.x)));
		}
		else
		{
			gunView.setScaleX(-1);
			gunView.setRotate(180 + Math.toDegrees(Math.atan2(playerDirection.y, playerDirection.x)));
		}

		// Shoot at player
		if (!gun.getReloading())
		{
			if (gun.fire(position.x, position.y, playerDirection.x, playerDirection.y))
			{
				recoilOffset.x -= playerDirection.x * 50;
				recoilOffset.y -= playerDirection.y * 50;
			}
		}

		// Update position of screen
		updateScreenPos();
	}

	@Override
	void actionUpdate()
	{
		running = !running;

		if (running)
		{
			idleSprites.getNode().setX(-9999);

			// Set sprite for action
			currentSprite = actionSprites;
			
			// Get delta
			movementVector.x = GameManager.getPlayer().getPosition().x - position.x;
			movementVector.y = GameManager.getPlayer().getPosition().y - position.y;
			
			// If too close to player, move in less significant vector instead
			if (Math.abs(movementVector.x) < 20 && Math.abs(movementVector.y) < 20)
			{
				movementVector = Vector2.Multiply(Vector2.Normalize(movementVector), runSpeed);
				// Choose to move in axis with lesser delta
				if (Math.abs(movementVector.x) < Math.abs(movementVector.y))
				{
					movementVector.y *= 0.1;
				}
				else
				{
					movementVector.x *= 0.1;
				}
			}
			else
			{
				movementVector = Vector2.Multiply(Vector2.Normalize(movementVector), runSpeed);
				// Choose to move in axis with greater delta
				if (Math.abs(movementVector.x) > Math.abs(movementVector.y))
				{
					movementVector.y *= 0.1;
				}
				else
				{
					movementVector.x *= 0.1;
				}
			}
		}
		else
		{
			actionSprites.getNode().setX(-9999);
			currentSprite = idleSprites;
		}

		// Set sprite ImageView depending on current one
		imgView = currentSprite.getNode();
	}
}
