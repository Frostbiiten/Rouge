package application;

import javafx.scene.image.Image;

public class RocketProjectile extends Projectile
{
	private static final double playerShotSpeed = 1.2;
	private static final double enemyShotSpeed = 1.0;

	// Construct projectile with specified position and velocity, specifying if it was player-shot
	public RocketProjectile (double xPosition, double yPosition, double xDir, double yDir, boolean playerOwned)
	{
		super(xPosition, yPosition, xDir, yDir, playerOwned, 5);

		radius = 4;

		// Set shot speed according to the shooter
		if (playerOwned)
		{
			xVel *= playerShotSpeed;
			yVel *= playerShotSpeed;

			// Create sprite
			sprite = new AnimatedSprite(new Image("file:assets/objects/bulletlarge.png"), 40, 9, 1, true);
		}
		else
		{
			xVel *= enemyShotSpeed;
			yVel *= enemyShotSpeed;

			// Create sprite
			sprite = new AnimatedSprite(new Image("file:assets/objects/bulletlarge.png"), 40, 9, 1, true);
		}

		sprite.getNode().setRotate(Math.toDegrees(Math.atan2(yDir, xDir)));
		sprite.getNode().setPreserveRatio(true);
		sprite.getNode().setFitHeight(50);

		xPos += xVel * 2;
		yPos += yVel * 2;

		// Play sprite animation
		sprite.play();
	}

	// Constructor with sprite specified
	public RocketProjectile (double xPosition, double yPosition, double xDir, double yDir, boolean playerOwned, AnimatedSprite sprite)
	{
		super(xPosition, yPosition, xDir, yDir, playerOwned, 5);

		// Set sprite
		this.sprite = sprite;

		if (playerOwned)
		{
			sprite.getNode().setPreserveRatio(true);
			sprite.getNode().setFitHeight(20);
			sprite.getNode().setRotate(Math.toDegrees(Math.atan2(yDir, xDir)));
		}

		radius = 4;

		// Set shot speed according to the shooter
		if (playerOwned)
		{
			xVel *= playerShotSpeed;
			yVel *= playerShotSpeed;
		}
		else
		{
			xVel *= enemyShotSpeed;
			yVel *= enemyShotSpeed;
		}

		sprite.getNode().setFitHeight(50);
		
		xPos += xVel * 20;
		yPos += yVel * 20;

		// Play sprite animation
		sprite.play();
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
		scaleVelocity(1.02);
	}

	@Override
	public void collide()
	{
		// There is not much special behaviour for the default gun (no reflect, etc)
		VFX.spawnHitImpact((int)xPos, (int)yPos, 180 + Math.toDegrees(Math.atan2(xVel, -yVel)));
		GameManager.addExplosion(new Explosion(xPos - xVel * 1.5, yPos - yVel * 1.5, Explosion.MEDIUM, 50, 10, playerOwned));
		Camera.shakeCamera(10, 0.8, 1);
	}
}

