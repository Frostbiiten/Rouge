package application;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

public abstract class Prop
{
	protected Vector2 position;
	protected ImageView propView;
	protected int imageScale;
	protected Rectangle mask;
	
	public Prop(Vector2 position, Image img, int imageScale)
	{
		// Set imagescale
		this.imageScale = imageScale;

		// Create imageview
		propView = new ImageView(img);
		propView.setPreserveRatio(true);
		propView.setFitWidth(img.getWidth() * imageScale * AppProps.SCALE);
		
		// Set position
		this.position = position;
		
		// Set mask
		mask = new Rectangle(position.x, position.y, img.getWidth(), img.getHeight());
	}
	
	public void update()
	{
	}

	public void updateScreenPos()
	{
		// Calculate position on screen (position is center of image)
		double screenX = (position.x - Camera.getX()) * AppProps.SCALE - propView.getImage().getWidth() / 2 * imageScale;
		double screenY = (position.y - Camera.getY()) * AppProps.SCALE - propView.getImage().getHeight() / 2 * imageScale;
		propView.setX(screenX);
		propView.setY(screenY);
	}
	
	public abstract void hit(Vector2 direction);

	// Method to get imageview of prop
	public ImageView getNode()
	{
		return propView;
	}

	// Method to get msak of prop
	public Rectangle getMask()
	{
		return mask;
	}

	// Method to get x position
	public double getX()
	{
		return position.x;
	}

	// Method to get y position
	public double getY()
	{
		return position.y;
	}
}