package application;

public class NormalProjectile extends Projectile
{
	private static final double playerShotSpeed = 0.2;
	private static final double enemyShotSpeed = 3.5;
	private static final double playerShotRadius = 4;
	private static final double enemyShotRadius = 8;

	// Construct projectile with specified position and velocity, specifying if it was player-shot
	public NormalProjectile (double xPosition, double yPosition, double xDir, double yDir, boolean playerOwned)
	{
		super(xPosition, yPosition, xDir, yDir, playerOwned, 5);

		if (playerOwned)
		{
			sprite.getNode().setPreserveRatio(true);
			sprite.getNode().setFitHeight(20);
			sprite.getNode().setRotate(Math.toDegrees(Math.atan2(yDir, xDir)));
		}

		// Set shot speed according to the shooter
		if (playerOwned)
		{
			xVel *= playerShotSpeed;
			yVel *= playerShotSpeed;
			radius = playerShotRadius;
		}
		else
		{
			xVel *= enemyShotSpeed;
			yVel *= enemyShotSpeed;
			radius = enemyShotRadius;
		}
	
	}

	@Override
	public void update()
	{
		// Move projectile based on velocity and update mask
		xPos += xVel;
		yPos += yVel;
		updateMask();
		sprite.getNode().setX((xPos - radius - Camera.getX()) * AppProps.SCALE);
		sprite.getNode().setY((yPos - radius - Camera.getY()) * AppProps.SCALE);
	}

	@Override
	public void collide()
	{
		// There is not much special behaviour for the default gun (no reflect, etc)
		VFX.spawnHitImpact((int)xPos, (int)yPos, 0);
		Camera.shakeCamera(0.5, 0.3, 1);
	}
}
