package application;

import javafx.scene.image.Image;

public class Barrel extends Prop
{
	private int hits;
	private boolean isBarrel;

	public Barrel(Vector2 position, boolean explosive)
	{
		super(position, new Image("file:assets/objects/barrel.png"), 1);

		// Randomize barrel chance
		isBarrel = Math.random() < 0.5;

		// Replace image if not barrel
		if (!isBarrel)
		{
			propView.setImage(new Image("file:assets/objects/crate.png"));
		}
		
		// Default number of hits is 0
		hits = 0;

		if (explosive)
		{
			// TODO: implement explosive barrel
			//barrelImg = new Image("file:assets/objects/barrel.png");
		}
	}

	@Override
	public void hit(Vector2 direction)
	{
		if (direction != null)
		{
			position.x += direction.x / 100;
			position.y += direction.y / 100;
		}

		hits++;
		
		for (int i = 0; i < 3; i++)
		{
			AnimatedSprite dust = VFX.spawnDust((int)(position.x + Math.random() * 10 - 5), (int)(position.y + Math.random() * 10));
		}

		// Check how many times it has been hit and set sprite accordingly
		if (isBarrel)
		{
			if (Math.random() < 0.5)
			{
				propView.setImage(new Image("file:assets/objects/barrel_broken.png"));
			}
			else
			{
				propView.setImage(new Image("file:assets/objects/barrel_broken_2.png"));
			}
		}
		else
		{
			if (Math.random() < 0.5)
			{
				propView.setImage(new Image("file:assets/objects/crate_broken.png"));
			}
			else
			{
				propView.setImage(new Image("file:assets/objects/crate_broken_2.png"));
			}
		}
		
		if (hits > 1)
		{
			// Remove object from pane
			GameManager.removeProp(this);
		}
	}

}
