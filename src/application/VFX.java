package application;

import javafx.scene.image.Image;

public class VFX
{
	private static AnimatedSprite[] hitEffects;
	private static Vector2[] hitEffectLocations;
	private static int lastSpawnedHitIndex;

	private static AnimatedSprite[] dustEffects;
	private static Vector2[] dustEffectLocations;
	private static int lastSpawnedDustIndex;
	
	public static void init()
	{
		// Create and initialize hit effect arrays
		hitEffects = new AnimatedSprite[15];
		hitEffectLocations = new Vector2[15];
		for (int i = 0; i < hitEffects.length; i++)
		{
			AnimatedSprite sprite = new AnimatedSprite(new Image("file:assets/objects/impact0.png"), 15, 6, 1, false);
			hitEffects[i] = sprite;
			sprite.getNode().setFitWidth(40);
			sprite.getNode().setPreserveRatio(true);
			hitEffectLocations[i] = new Vector2();
			GameManager.addAnimatedSprite(hitEffects[i]);
		}
		lastSpawnedHitIndex = 0;

		dustEffects = new AnimatedSprite[20];
		dustEffectLocations = new Vector2[20];
		for (int i = 0; i < dustEffects.length; i++)
		{
			AnimatedSprite sprite = new AnimatedSprite(new Image("file:assets/dust.png"), 15, 6, 1, false);
			dustEffects[i] = sprite;
			sprite.getNode().setFitWidth(40 + Math.random() * 9);
			sprite.getNode().setPreserveRatio(true);
			dustEffectLocations[i] = new Vector2();
			GameManager.addAnimatedSprite(dustEffects[i]);
		}
		lastSpawnedDustIndex = 0;
	}
	
	// Spawn hit impact at location with rotation
	public static void spawnHitImpact(int x, int y, double angle)
	{
		AnimatedSprite currentHitImpact = hitEffects[lastSpawnedHitIndex];
		
		// Store position for when camera moves
		hitEffectLocations[lastSpawnedHitIndex].x = x;
		hitEffectLocations[lastSpawnedHitIndex].y = y;

		// Set initial position
		currentHitImpact.getNode().setX((x - Camera.getX()) * AppProps.SCALE);
		currentHitImpact.getNode().setY((y - Camera.getY()) * AppProps.SCALE);
		currentHitImpact.play();

		// Rotate according to angle
		currentHitImpact.getNode().setRotate(angle);
		
		// Loop between the first index and final index
		lastSpawnedHitIndex++;
		if (lastSpawnedHitIndex == hitEffects.length)
		{
			lastSpawnedHitIndex = 0;
		}
	}

	// Spawn dust at location
	public static void spawnDust(int x, int y)
	{
		AnimatedSprite currentDust = dustEffects[lastSpawnedDustIndex];
		
		// Store position for when camera moves
		dustEffectLocations[lastSpawnedDustIndex].x = x;
		dustEffectLocations[lastSpawnedDustIndex].y = y;

		// Set initial position
		currentDust.getNode().setX((x - Camera.getX()) * AppProps.SCALE);
		currentDust.getNode().setY((y - Camera.getY()) * AppProps.SCALE);
		currentDust.getNode().setOpacity(1);
		
		// Set random rotation
		currentDust.getNode().setRotate(Math.random() * 6 - 3);

		// Play dust animation
		currentDust.play();
		
		// Loop between the first index and final index
		lastSpawnedDustIndex++;
		if (lastSpawnedDustIndex == dustEffects.length)
		{
			lastSpawnedDustIndex = 0;
		}
	}

	public static void update()
	{
		// Update position of each effect
		for (int i = 0; i < hitEffects.length; i++)
		{
			if (hitEffects[i].isPlaying())
			{
				hitEffects[i].getNode().setX((hitEffectLocations[i].x - Camera.getX()) * AppProps.SCALE);
				hitEffects[i].getNode().setY((hitEffectLocations[i].y - Camera.getY()) * AppProps.SCALE);
			}
			else
			{
				// Move off screen
				hitEffects[i].getNode().setX(-9999);
			}
		}

		for (int i = 0; i < dustEffects.length; i++)
		{
			AnimatedSprite currentDust = dustEffects[i];

			double currentOpacity = currentDust.getNode().getOpacity();
			dustEffectLocations[i].y -= 1 * currentOpacity;
			currentDust.getNode().setOpacity(currentOpacity * 0.9);

			if (currentDust.getNode().getOpacity() == 0)
			{
				hitEffects[i].getNode().setX(-9999);
			}

			if (currentDust.getNode().getRotate() > 0)
			{
				currentDust.getNode().setRotate(currentDust.getNode().getRotate() + currentOpacity);
			}
			else
			{
				currentDust.getNode().setRotate(currentDust.getNode().getRotate() - currentOpacity);
			}

			dustEffects[i].getNode().setX((dustEffectLocations[i].x - Camera.getX()) * AppProps.SCALE);
			dustEffects[i].getNode().setY((dustEffectLocations[i].y - Camera.getY()) * AppProps.SCALE);
		}
	}
}
