package application;

public abstract class Gun
{
	protected int ammo;
	protected double reloadTime;
	protected int magazineSize;
	protected int magazineCount;
	protected boolean playerOwned;

	// Constructor of parent absent gun class
	Gun (int ammo, int magazineSize, int magazineCount, double reloadTime, boolean playerOwned)
	{
		this.ammo = ammo;
		this.reloadTime = reloadTime;
		this.magazineSize = magazineSize;
		this.magazineCount = magazineCount;
		this.playerOwned = playerOwned;
	}
	
	void reload()
	{
		if (magazineCount > 0)
		{
			ammo = magazineSize;
			magazineCount--;
		}
	}
	abstract void fire(double xPos, double yPos, double xDir, double yDir);
}
