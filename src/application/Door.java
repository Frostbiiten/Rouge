package application;

import javafx.scene.image.ImageView;

public class Door
{
	private AnimatedSprite sprite;
	private Vector2 position;

	Door(Vector2 position)
	{
		this.position = position;
	}

	public void open()
	{
		sprite.play();
	}
	
	public ImageView getNode()
	{
		return sprite.getNode();
	}
}
