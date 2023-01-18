package application;

import javafx.scene.image.Image;

public class VFX
{
	private static AnimatedSprite[] hitEffects;
	private static Vector2[] hitEffectLocations;
	private static int lastSpawnedHitIndex;
	
	public static void init()
	{
		// Create and initialize hit effect arrays
		hitEffects = new AnimatedSprite[15];
		hitEffectLocations = new Vector2[15];
		for (int i = 0; i < hitEffects.length; i++)
		{
			AnimatedSprite sprite = new AnimatedSprite(new Image("file:assets/objects/impact0.png"), 15, 6, 1, true);
			hitEffects[i] = sprite;
			sprite.getNode().setFitWidth(30);
			sprite.getNode().setPreserveRatio(true);
			hitEffectLocations[i] = new Vector2();
			GameManager.addAnimatedSprite(hitEffects[i]);
		}
		lastSpawnedHitIndex = 0;
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
	}
}
