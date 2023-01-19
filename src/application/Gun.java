package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Animation.Status;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.util.Duration;

public abstract class Gun
{
	protected Image imgGun;
	protected int ammo;
	protected double reloadTime;
	protected int magazineSize;
	protected int magazineCount;
	protected boolean playerOwned;
	protected double cooldownTime;
	protected boolean coolingDown;
	protected Timeline cooldownTimeline;
	protected boolean reloading;
	protected Timeline reloadTimeline;
	protected boolean infiniteMags;
	protected boolean autoFire;
	protected String name;

	// Constructor of parent absent gun class
	Gun (int ammo, int magazineSize, int magazineCount, double reloadTime, double cooldownTime, boolean playerOwned, Image image)
	{
		this.ammo = ammo;
		this.magazineSize = magazineSize;
		this.magazineCount = magazineCount;
		this.reloadTime = reloadTime;
		this.cooldownTime = cooldownTime;
		this.playerOwned = playerOwned;

		// Enemies have unlimited ammo
		infiniteMags = !playerOwned;

		// Autofire is by default false
		autoFire = false;

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

		Gun thisGun = this;
		reloadTimeline = new Timeline(new KeyFrame(Duration.seconds(reloadTime), new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (playerOwned && GameManager.getPlayer().getWeapon() != thisGun)
				{
					return;
				}

				thisGun.ammo = magazineSize;
				
				// Only remove magazine if its not unlimited/player owned
				if (!infiniteMags && playerOwned)
				{
					thisGun.magazineCount--;
				}

				reloading = false;

				if (playerOwned)
				{
					UI.updateWeapon();
				}
			}
		}));

		imgGun = image;
		name = "Weapon";
	}
	
	// Reloading timer
	protected void reload()
	{
		if (magazineCount > 0)
		{
			reloading = true;
			
			if (playerOwned)
			{
				UI.updateWeapon();
			}

			reloadTimeline.play();
		}
	}

	// Cancel reload
	protected void cancelReload()
	{
		reloading = false;
		if (reloadTimeline.getStatus() == Status.RUNNING)
		{
			reloadTimeline.stop();
		}
	}

	// Method to be called after every shot to cool down 
	protected void triggerCooldown()
	{
		coolingDown = true;
		cooldownTimeline.play();
	}

	public abstract boolean fire(double xPos, double yPos, double xDir, double yDir);

	// Accessor methods
	public Image getImage()
	{
		return imgGun;
	}
	public int getAmmo()
	{
		return ammo;
	}
	public int getMagazines()
	{
		return magazineCount;
	}
	public String getName()
	{
		return name;
	}
	public boolean getReloading()
	{
		return reloading;
	}
	public boolean autofireEnabled()
	{
		return autoFire;
	}
}
