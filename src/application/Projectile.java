package application;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

public abstract class Projectile
{
	// Visual
	protected AnimatedSprite sprite;

	// Movement
	protected double xPos, yPos;
	protected double xVel, yVel;
	
	// Collision
	protected Rectangle mask;
	protected boolean playerOwned;
	protected double radius;

	// There are 3 main parts in a projectile's lifetime
	// The beginning, when the projectile is first shot (constructor)
	// The midlife, when the projectile is travelling through the air
	// The endlife, when the projectile collides with an object/wall

	// Constructor is called when first shot
	Projectile (double xPos, double yPos, double xVel, double yVel, boolean playerOwned, double radius, AnimatedSprite sprite)
	{
		this.xPos = xPos;
		this.yPos = yPos;
		this.xVel = xVel;
		this.yVel = yVel;
		this.playerOwned = playerOwned;
		this.radius = radius;
		this.mask = new Rectangle(xPos - radius, yPos - radius, radius * 1.8, radius * 1.8);
	}

	// Constructor is called when first shot
	Projectile (double xPos, double yPos, double xVel, double yVel, boolean playerOwned, double radius)
	{
		this.xPos = xPos;
		this.yPos = yPos;
		this.xVel = xVel;
		this.yVel = yVel;
		this.playerOwned = playerOwned;
		this.radius = radius;
		this.mask = new Rectangle(xPos - radius, yPos - radius, radius * 1.8, radius * 1.8);

		// Create sprite and play
		sprite = new AnimatedSprite(new Image("file:assets/objects/bullet0.png"), 15, 8, 1, true);
		sprite.play();
	}

	// Moved
	public abstract void update();

	// Destroyed
	public abstract void collide();

	// Method for updating the mask after moving, etc.
	public void updateMask()
	{
		double adjustedWidth = radius * 1.8;
		double adjustedHeight = radius * 1.8;
		mask.setX(xPos - adjustedWidth / 2);
		mask.setY(yPos - adjustedHeight / 2);
		mask.setWidth(adjustedWidth * 2);
		mask.setHeight(adjustedHeight * 2);
	}

	// Scale velocity by a scalar
	public void scaleVelocity(double velocityScale)
	{
		xVel *= velocityScale;
		yVel *= velocityScale;
	}

	// Setters
	public void setRadius(double radius)
	{
		this.radius = radius;
	}
	public void setXPos(double x)
	{
		xPos = x;
	}
	public void setYPos(double y)
	{
		yPos = y;
	}
	
	// Getters
	public Bounds getMask()
	{
		return mask.getBoundsInParent();
	}
	public double getXPos()
	{
		return xPos;
	}
	public double getYPos()
	{
		return yPos;
	}
	public double getXVel()
	{
		return xVel;
	}
	public double getYVel()
	{
		return yVel;
	}
	public ImageView getNode()
	{
		return sprite.getNode();
	}
	public boolean getPlayerOwned()
	{
		return playerOwned;
	}
}
