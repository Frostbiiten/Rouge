package application;

import java.util.ArrayList;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class Explosion
{
	// Defines explosion size
	public final static int SMALL = 0, MEDIUM = 1, LARGE = 2;

	// Main fields
	private double xPos, yPos;
	private Rectangle mask;
	private boolean playerSafe;

	// Rendering
	private AnimatedSprite sprite;
	private double spriteScale;
	
	public Explosion (double x, double y, int size, double radius, double damage, boolean playerSafe)
	{
		// Set x and y position
		xPos = x;
		yPos = y;

		// Set sprites according to specified size id
		if (size == SMALL)
		{
			sprite = new AnimatedSprite(new Image("file:assets/explosions/small.png"), 15, 9, 1, false);
			spriteScale = 1;
		}
		else if (size == MEDIUM)
		{
			sprite = new AnimatedSprite(new Image("file:assets/explosions/medium.png"), 15, 7, 1, false);
			spriteScale = 2;
		}
		else if (size == LARGE)
		{
			sprite = new AnimatedSprite(new Image("file:assets/explosions/large.png"), 15, 20, 1, false);
			
			// Scale sprite to make it larger
			spriteScale = 1.6;
		}

		sprite.getNode().setFitWidth(sprite.getFrameWidth() * spriteScale);
		sprite.getNode().setPreserveRatio(true);

		// Create collision mask (adjust for more leniency)
		double adjustedRadius = radius * 1.8;
		mask = new Rectangle(xPos - adjustedRadius, yPos - adjustedRadius, adjustedRadius * 2, adjustedRadius * 2);
		Bounds maskBounds = mask.getBoundsInParent();
		
		// Check if it should hit the player
		if (!playerSafe)
		{
			if (GameManager.playerCollision(this))
			{
				GameManager.getPlayer().damage();
			}
		}
		else
		{
			ArrayList<Enemy> enemies = GameManager.getEnemies();
			for (int i = 0; i < enemies.size(); i++)
			{
				if (enemies.get(i).getMask().intersects(maskBounds))
				{
					enemies.get(i).damage();
				}
			}

			ArrayList<Prop> props = GameManager.getProps();
			for (int i = 0; i < props.size(); i++)
			{
				Prop currentProp = props.get(i);
				if (currentProp.getMask().intersects(maskBounds))
				{
					props.get(i).hit(new Vector2(currentProp.getX() - x, currentProp.getY() - y));
				}
			}
		}
		
		// Start playing explosion animation
		sprite.play();
	}
	
	public void update()
	{
		// Update onscreen position relative to camera and scale
		sprite.getNode().setX((xPos - Camera.getX()) * AppProps.SCALE - sprite.getFrameWidth() / 2 * spriteScale);
		sprite.getNode().setY((yPos - Camera.getY()) * AppProps.SCALE - sprite.getFrameHeight() / 2 * spriteScale);
	}

	public AnimatedSprite getSprite()
	{
		return sprite;
	}

	public ImageView getNode()
	{
		return sprite.getNode();
	}

	public Rectangle getMask()
	{
		return mask;
	}
}
