package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public abstract class Enemy
{
	// Behaviour
	protected Timeline actionTimeline;
	protected Vector2 position;
	protected int hp;

	// Visual
	protected double spriteScale;
	protected AnimatedSprite idleSprites, actionSprites, currentSprite; 
	protected ImageView imgView;
	protected ImageView gunView;

	// Collision
	protected Rectangle mask;
	protected Vector2 radius;
	protected Rectangle room;

	Enemy(String name, int idleXSprites, int actionXSprites, Vector2 position, double actionInterval, Vector2 radius, double spriteScale, int hp, Rectangle room)
	{
		// Load sprites
		Image idleSheet = new Image("file:assets/enemies/" + name + "/idle.png");
		Image runSheet = new Image("file:assets/enemies/" + name + "/action.png");

		this.spriteScale = spriteScale;
		idleSprites = new AnimatedSprite(idleSheet, 12, idleXSprites, 1, true);
		actionSprites = new AnimatedSprite(runSheet, 12, actionXSprites, 1, true);

		currentSprite = idleSprites;
		imgView = currentSprite.getNode();

		// Scale sprites
		idleSprites.getNode().setFitHeight(spriteScale * idleSprites.getFrameHeight());
		idleSprites.getNode().setPreserveRatio(true);

		actionSprites.getNode().setFitHeight(spriteScale * actionSprites.getFrameHeight());
		actionSprites.getNode().setPreserveRatio(true);

		// Play sprites
		idleSprites.play();
		actionSprites.play();

		// Initialize position
		this.position = position;
		
		// Initialize hp
		this.hp = hp;

		actionTimeline = new Timeline(new KeyFrame(Duration.seconds(actionInterval), new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				// Anywhere between half and double speed
				actionTimeline.setRate(Math.random() * 1.5 + 0.5);
				actionUpdate();
			}
		}));
		actionTimeline.setCycleCount(Timeline.INDEFINITE);
		actionTimeline.play();
		
		// Create mask and set dimensions
		this.radius = radius;
		mask = new Rectangle();
		mask.setWidth(radius.x * 2);
		mask.setHeight(radius.y * 2);
		updateMask();

		// Add sprites above bg, below fg
		GameManager.getRoot().getChildren().add(GameManager.getBgDepth() + 1, idleSprites.getNode());
		GameManager.getRoot().getChildren().add(GameManager.getBgDepth() + 1, actionSprites.getNode());

		// Offscreen by default
		actionSprites.getNode().setX(-9999);
		actionSprites.getNode().setY(-9999);

		// Set room with padding
		this.room = new Rectangle(room.getX() + 10, room.getY() + 10, room.getWidth() - 20, room.getHeight() - 20);
	}

	abstract void update();
	abstract void actionUpdate();

	protected void die()
	{
		// When dead, remove all sprites from the pane
		GameManager.getRoot().getChildren().remove(idleSprites.getNode());
		GameManager.getRoot().getChildren().remove(actionSprites.getNode());
		GameManager.getRoot().getChildren().remove(gunView);

		// Cause a deathexplosion that does no damage
		Explosion deathExplosion = new Explosion(position.x, position.y, Explosion.SMALL, 0, true, true);
		GameManager.addExplosion(deathExplosion);

		// Remove the enemy from the arraylist to no longer keep track of it
		GameManager.removeEnemy(this);
	}
	protected void damage()
	{
		// Add 5 points for every damage hit dealt
		GameManager.addPoints(hp * 5);

		// Deduct health and check if enemy should die
		hp--;
		if (hp < 0)
		{
			die();
		}
	}
	protected void updateMask()
	{
		// Update position of mask
		// Dimensions are constant, so they can be left
		mask.setX(position.x - radius.x);
		mask.setY(position.y - radius.y);
	}
	protected void updateScreenPos()
	{
		double x = (position.x - Camera.getX()) * AppProps.SCALE - currentSprite.getFrameWidth() / 2 * spriteScale;
		double y = (position.y - Camera.getY()) * AppProps.SCALE - currentSprite.getFrameHeight() / 2 * spriteScale;
		imgView.setX(x);
		imgView.setY(y);
	}

	// Mutator methods
	public void setX(double x)
	{
		position.x = x;
	}

	public void setY(double y)
	{
		position.y = y;
	}

	public double getX()
	{
		return position.x;
	}

	public double getY()
	{
		return position.y;
	}
	
	public Rectangle getMask()
	{
		return mask;
	}
}
