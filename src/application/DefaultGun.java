package application;

import javafx.scene.image.Image;

public class DefaultGun extends Gun 
{
	public DefaultGun(boolean playerOwned)
	{
		// Call base class constructor
		super (10, 10, 1, 2, 0.2, playerOwned, new Image("file:assets/guns/default.png"));
		infiniteMags = true;
		name = "Default";
	}

	@Override
	// Because this is the default gun, the ammo should never run out
	// So just reset magazine on reload without depleting anything else
	protected void reload()
	{
		reloading = true;
		UI.updateWeapon();
		reloadTimeline.play();
		magazineCount = 1;
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

			// Play sound effect
			AudioManager.playShoot();
			
			// Create new projectile and spawn into world
			Projectile newProjectile = new NormalProjectile(xPos, yPos, xDir, yDir, playerOwned);
			GameManager.spawnProjectile(newProjectile);

			// Start cooldown timer
			triggerCooldown();
			
			// Confirm shot
			return true;
		}
	}
}
