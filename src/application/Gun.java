package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public abstract class Gun
{
	protected int ammo;
	protected double reloadTime;
	protected int magazineSize;
	protected int magazineCount;
	protected boolean playerOwned;
	protected double cooldownTime;
	protected boolean coolingDown;
	protected Timeline cooldownTimeline;

	// Constructor of parent absent gun class
	Gun (int ammo, int magazineSize, int magazineCount, double reloadTime, double cooldownTime, boolean playerOwned)
	{
		this.ammo = ammo;
		this.magazineSize = magazineSize;
		this.magazineCount = magazineCount;
		this.reloadTime = reloadTime;
		this.cooldownTime = cooldownTime;
		this.playerOwned = playerOwned;

		// Cooldown mechanics
		coolingDown = false;
		cooldownTimeline = new Timeline(new KeyFrame(Duration.seconds(cooldownTime), new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				coolingDown = false;
			}
		}));
	}
	
	void reload()
	{
		if (magazineCount > 0)
		{
			ammo = magazineSize;
			magazineCount--;
		}
	}

	// Method to be called after every shot to cool down 
	void triggerCooldown()
	{
		coolingDown = true;
		cooldownTimeline.play();
	}

	abstract void fire(double xPos, double yPos, double xDir, double yDir);
}
