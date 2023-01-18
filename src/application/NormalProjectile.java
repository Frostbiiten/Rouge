package application;

public class NormalProjectile extends Projectile
{
	private static final double speed = 20;

	// Construct projectile with specified position and velocity, specifying if it was player-shot
	public NormalProjectile (double xPosition, double yPosition, double xDir, double yDir, boolean playerOwned)
	{
		super(xPosition, yPosition, xDir * speed, yDir * speed, playerOwned, 20);
	}

	@Override
	public void update()
	{
		// Move projectile based on velocity and update mask
		xPos += xVel;
		yPos += yVel;
		updateMask();
	}

	@Override
	public void collide()
	{
		// There is not much special behaviour for the default gun (no reflect, etc)
		//Camera.shakeCamera(xVel, xPos, radius);
	}
}
