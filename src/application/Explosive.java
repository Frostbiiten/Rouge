package application;

import javafx.scene.image.Image;

public class Explosive extends Prop
{
	public Explosive(Vector2 position)
	{
		super(position, new Image("file:assets/objects/explosive.png"), 1);
	}

	@Override
	public void hit(Vector2 direction)
	{
		// Explode
		GameManager.removeProp(this);
		Explosion explosion = new Explosion(position.x, position.y - 40, Explosion.LARGE, 20, false, false);
		GameManager.addExplosion(explosion);
		Camera.shakeCamera(20, 0.95, 2);

		for (int i = 0; i < 7; i++)
		{
			VFX.spawnDust(position.x + Math.random() * 40 - 20, position.y + Math.random() * 40 - 20);
		}
	}
}
