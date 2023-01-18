package application;

public class DefaultGun extends Gun 
{
	public DefaultGun(boolean playerOwned)
	{
		// Call base class constructor
		super (999, 999, 1, 0, playerOwned);
	}

	@Override
	// Because this is the default gun, the ammo should never run out, so just reset magazine
	void reload()
	{
		ammo = magazineSize;
	}

	@Override
	void fire(double xPos, double yPos, double xDir, double yDir)
	{
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
			
			// Create new projectile and spawn
			Projectile newProjectile = new NormalProjectile(xPos, yPos, xDir, yDir, playerOwned);
			GameManager.spawnProjectile(newProjectile);
		}
	}
}
