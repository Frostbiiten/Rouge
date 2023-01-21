package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class Pickup
{
	// Visual
	private ImageView pickupView;

	// Interaction
	private Timeline pickupEventTimeline;
	private Rectangle mask;
	private int timeElapsed;

	// Constructor
	public Pickup(double x, double y)
	{
		double randItemChance = Math.random();
		KeyFrame eventKeyframe;
		Image item = null;
		
		// Spawn health most of the time (64%)
		if (randItemChance < 0.64)
		{
			// Set event keyframe to increase health by 1
			eventKeyframe = new KeyFrame(Duration.millis(1), new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					GameManager.getPlayer().addHealth(1);
				}
			});

			// Set hp image
			item = new Image("file:assets/pickups/hp.png");
		}
		else
		{
			double randGun = Math.random();

			if (randGun < 0.5)
			{
				// Most common (magazine)
				// Set magazine image
				item = new Image("file:assets/pickups/magazine.png");

				// Set keyframe to refill current weapon
				eventKeyframe = new KeyFrame(Duration.millis(1), new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						GameManager.getPlayer().getWeapon().refill();
						UI.updateWeapon();
					}
				});
			}
			else if (randGun < 0.8)
			{
				// Next common (rapidfire)

				// Set gun image
				item = new Image("file:assets/guns/fast.png");
				eventKeyframe = new KeyFrame(Duration.millis(1), new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						GameManager.getPlayer().addWeapon(new FastGun(true));
					}
				});
			}
			else
			{
				// Next common (explosive)
				// Set gun image
				item = new Image("file:assets/guns/rocket.png");
				eventKeyframe = new KeyFrame(Duration.millis(1), new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						GameManager.getPlayer().addWeapon(new RocketGun(true));
					}
				});
			}
		}

		// Create timeline with set keyframe
		pickupEventTimeline = new Timeline(eventKeyframe);

		// Create imageview with assigned image
		pickupView = new ImageView(item);
		
		// Limit height to 40
		pickupView.setPreserveRatio(true);
		pickupView.setFitHeight(30);
		
		// Create mask with speicified position and 20x20 dimensions
		mask = new Rectangle(x, y, 20, 20);

		// Start at 0 for elapsed time
		timeElapsed = 0;
	}

	// Method to update position of imageview on screen
	public void updateScreenPos()
	{
		// Use mask's location as position
		double screenX = (mask.getX() - Camera.getX()) * AppProps.SCALE;
		double screenY = (mask.getY() - Camera.getY()) * AppProps.SCALE;
		
		// Set position on screen
		pickupView.setX(screenX);
		pickupView.setY(screenY + Math.sin(timeElapsed / 50.0) * 10);

		// Increment time
		timeElapsed++;
	}

	// Method to pick up the item
	public void pickup()
	{
		pickupEventTimeline.play();
	}

	// Method to get the imageview
    public ImageView getNode()
	{
		return pickupView;
    }

	// Method to get mask
    public Rectangle getMask()
	{
		return mask;
    }
}