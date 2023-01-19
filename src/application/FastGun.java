package application;

import javafx.scene.image.Image;

public class FastGun extends Gun 
{
	public FastGun(boolean playerOwned)
	{
		// Call base class constructor
		super (30, 30, 5, 3, 0.05, playerOwned, new Image("file:assets/guns/fast.png"));
		autoFire = true;
		name = "Rapidfire";
	}

	@Override
	public boolean fire(double xPos, double yPos, double xDir, double yDir)
	{
		// Can't fire if cooling down
		if (coolingDown || reloading)
		{
			return false;
		}

		// Check for remaining ammo
		if (ammo == 0)
		{
			if (magazineCount > 0)
			{
				reload();
			}

			return false;
		}
		else
		{
			ammo--;
			
			// Only shake camera if it is shot by player
			if (playerOwned)
			{
				Camera.shakeCamera(15, 0.2, 1);
			}
			
			// Create sprite for projectile and spawn it into the world 
			AnimatedSprite sprite = new AnimatedSprite(new Image("file:assets/objects/bulletfast.png"), 15, 8, 1, true);
			Projectile newProjectile = new NormalProjectile(xPos, yPos, xDir, yDir, playerOwned, sprite);
			newProjectile.setXPos(newProjectile.getXPos() + Math.random() * 8 - 4);
			newProjectile.setYPos(newProjectile.getYPos() + Math.random() * 8 - 4);
			newProjectile.setRadius(2.5);
			newProjectile.scaleVelocity(1.3);
			GameManager.spawnProjectile(newProjectile);

			// Start cooldown timer
			triggerCooldown();
			
			// Confirm shot
			return true;
		}
	}
}
