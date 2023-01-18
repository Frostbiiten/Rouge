package application;

public class DefaultGun extends Gun 
{
	public DefaultGun(boolean playerOwned)
	{
		// Call base class constructor
		super (999, 999, 1, 0, 0.2, playerOwned);
	}

	@Override
	// Because this is the default gun, the ammo should never run out
	// So just reset magazine on reload without depleting anything else
	void reload()
	{
		ammo = magazineSize;
	}

	@Override
	void fire(double xPos, double yPos, double xDir, double yDir)
	{
		// Can't fire if cooling down
		if (coolingDown)
		{
			return;
		}

		// Check for remaining ammo
		if (ammo == 0)
		{
			// No shot ...
		}
		else
		{
			ammo--;
			
			// Only shake camera if it is shot by player
			if (playerOwned)
			{
				Camera.shakeCamera(1, 0.3, 1);
			}
			
			// Create new projectile and spawn into world
			Projectile newProjectile = new NormalProjectile(xPos, yPos, xDir, yDir, playerOwned);
			GameManager.spawnProjectile(newProjectile);

			// Start cooldown timer
			triggerCooldown();
		}
	}
}
