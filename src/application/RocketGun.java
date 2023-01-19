package application;

import javafx.scene.image.Image;

public class RocketGun extends Gun 
{
	public RocketGun(boolean playerOwned)
	{
		// Call base class constructor
		super (5, 5, 5, 4, 0.9, playerOwned, new Image("file:assets/guns/rocketlauncher.png"));
		name = "Explosive";
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
				Camera.shakeCamera(17, 0.4, 1);
			}
			
			// Create new projectile and spawn into world
			Projectile newProjectile = new RocketProjectile(xPos, yPos, xDir, yDir, playerOwned);
			GameManager.spawnProjectile(newProjectile);

			// Start cooldown timer
			triggerCooldown();
			
			// Confirm shot
			return true;
		}
	}
}
