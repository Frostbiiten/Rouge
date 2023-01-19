package application;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;
import javafx.event.*;

public class AnimatedSprite
{
	// Source image
	private Image spriteSheet;
	
	// Visual fields
	private ImageView spriteView;

	// Timing fields
	private Timeline animationTimeline;
	private int currentFrame;
	private double rate;
	
	// The number of sprites horizontally/vertically in the image
	private int xFrames, yFrames;
	
	// The width/height of each sprite in the image
	private int frameWidth, frameHeight;

	// Constructor for given number of sprites horizontally/vertically
	public AnimatedSprite(Image spriteSheet, double fps, int xSpriteCount, int ySpriteCount, boolean loop)
	{
		this(spriteSheet, fps, loop);

		// Set tile dimension properties
		this.xFrames = xSpriteCount;
		this.yFrames = ySpriteCount;
		this.frameWidth = (int)(spriteSheet.getWidth() / xSpriteCount);
		this.frameHeight = (int)(spriteSheet.getHeight() / ySpriteCount);
	}

	// Constructor for given width/height
	public AnimatedSprite(Image spriteSheet, double fps, boolean loop, int spriteWidth, int spriteHeight)
	{
		this(spriteSheet, fps, loop);

		// Set tile dimension properties
		this.frameWidth = spriteWidth;
		this.frameHeight = spriteHeight;
		xFrames = (int)(spriteSheet.getWidth() / spriteWidth);
		yFrames = (int)(spriteSheet.getHeight() / spriteHeight);
	}

	// Private constructor with common code between them, called by other two constructors
	private AnimatedSprite(Image spriteSheet, double fps, boolean loop)
	{
		// Set spritesheet
		this.spriteSheet = spriteSheet;
		
		// Start from first frame
		currentFrame = 0;

		// Create timeline to cycle through frames
		animationTimeline = new Timeline(new KeyFrame(Duration.seconds(1 / fps), new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				redraw();
				currentFrame++;

				if (currentFrame == xFrames * yFrames)
				{
					// Reset to beginning frame if loop enabled
					if (loop)
					{
						currentFrame = 0;
					}
					else
					{
						currentFrame--;
						animationTimeline.stop();
					}
				}
			}
		}));
		animationTimeline.setCycleCount(Timeline.INDEFINITE);
		
		spriteView = new ImageView();
	}

	// Play animation from start frame
	public void play()
	{
		currentFrame = 0;
		animationTimeline.play();
	}

	public void redraw()
	{
		WritableImage target = new WritableImage(frameWidth, frameHeight);
		draw(0, 0, target);
		spriteView.setImage(target);
	}

	public ImageView getNode()
	{
		return spriteView;
	}

	public int getFrameWidth()
	{
		return frameWidth;
	}

	public int getFrameHeight()
	{
		return frameHeight;
	}

	public boolean isPlaying()
	{
		return animationTimeline.getStatus() == Animation.Status.RUNNING;
	}

	public void draw(int xPos, int yPos, WritableImage target)
	{

		// Get the coordinates of the draw region with 1 unit = 1 sprite at first
		int srcY = currentFrame / xFrames;
		int srcX = currentFrame - srcY * xFrames;

		// Multiply by sprite dimensions to get the coordinates in pixels
		srcY *= frameHeight;
		srcX *= frameWidth;
		
		int destX = xPos;
		int destY = yPos;
		int drawWidth = frameWidth;
		int drawHeight = frameHeight;
		
		// Determine if sprite should be even be drawn depending on screen bounds
		boolean xDraw = destX > -frameWidth && destX < target.getWidth();
		boolean yDraw = destY > -frameHeight && destY < target.getHeight();
	
		if (xDraw && yDraw)
		{
			// Adjust x drawing variables if on the edge to draw correctly
			if (destX <= 0 && destX > -frameWidth)
			{
				drawWidth = frameWidth + destX;
				srcX -= destX;
				destX = 0;
			}
			else if (destX + frameWidth >= target.getWidth() && destX < target.getWidth())
			{
				drawWidth = (int)target.getWidth() - destX;
				destX = (int)target.getWidth() - drawWidth;
			}

			// Adjust y drawing variables if on the edge to draw correctly
			if (destY <= 0 && destY > -frameWidth)
			{
				drawHeight = frameHeight + destY;
				srcY -= destY;
				destY = 0;
			}
			else if (destY + frameWidth >= target.getHeight() && destY < target.getWidth())
			{
				drawHeight = (int)target.getHeight() - destY;
				destY = (int)target.getHeight() - drawHeight;
			}

			// Write pixels
			target.getPixelWriter().setPixels(destX, destY, drawWidth, drawHeight, spriteSheet.getPixelReader(), srcX, srcY);
		}
	}
}
