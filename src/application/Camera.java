package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class Camera
{
	private static Vector2 cameraPos;
	
	// Camera shak
	private static Timeline shakeTimeline;
	private static Vector2 shakeOffset;
	private static double currentShakeMagnitude;
	private static boolean shaking;
	private static double currentShakeFalloff;
	
	public static void init()
	{
		// 1 Frame is ~16 milliseconds at 60 fps, so run shake update every frame
		KeyFrame shakeKeyframe = new KeyFrame(Duration.millis(16), new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				// Add shake offset (negate for shaking back and forth effect)
				shakeOffset.x = -shakeOffset.x;
				shakeOffset.y = -shakeOffset.y;
				
				// Reduce shake magnitude
				currentShakeMagnitude *= currentShakeFalloff;

				// Add some randomization, by a smaller amount compared to the total magnitude
				shakeOffset.x += Math.random() * currentShakeMagnitude / 2;
				shakeOffset.y += Math.random() * currentShakeMagnitude / 2;

				
				// Stop timeline if shake is too low
				if (currentShakeMagnitude < 0.1)
				{
					shakeTimeline.stop();
					shaking = false;
					return;
				}
				
				// Normalize and multiply by target magnitude of shake
				shakeOffset = Vector2.Normalize(shakeOffset);

				// Rare case that occurs when vector cannot be normalized due to 0 magnitude
				// Fallback to ceasing the shake in this case
				if (Double.isNaN(shakeOffset.x) || Double.isNaN(shakeOffset.y) || Double.isInfinite(shakeOffset.x) || Double.isInfinite(shakeOffset.y))
				{
					shakeOffset.x = 0;
					shakeOffset.y = 0;
					shakeTimeline.stop();
					shaking = false;
					return;
				}
				else
				{
					shakeOffset.x *= currentShakeMagnitude;
					shakeOffset.y *= currentShakeMagnitude;
				}
			}
		});

		shakeTimeline = new Timeline(shakeKeyframe);
		shakeTimeline.setCycleCount(Timeline.INDEFINITE);

		shakeOffset = new Vector2();
		cameraPos = new Vector2();
	}

	public static void setPos(Vector2 pos)
	{
		cameraPos = new Vector2(pos);
	}

	public static void setPos(double x, double y)
	{
		cameraPos.x = x;
		cameraPos.x = y;
	}

	public static Vector2 getPos()
	{
		return Vector2.Add(cameraPos, shakeOffset);
	}

	public static double getX()
	{
		return cameraPos.x + shakeOffset.x;
	}

	public static double getY()
	{
		return cameraPos.y + shakeOffset.y;
	}

	public static Vector2 getPosNoShake()
	{
		return cameraPos;
	}
	
	public static void shakeCamera(double magnitude, double fallOff, double rate)
	{
		// Don't interrupt larger shakes that are already occuring
		if (currentShakeMagnitude > magnitude)
		{
			currentShakeMagnitude += magnitude;
			currentShakeFalloff = (currentShakeFalloff + fallOff)/2;
			return;
		}

		currentShakeFalloff = fallOff;
		currentShakeMagnitude = magnitude;
		shaking = true;

		// Start and play timeline with only newly created keyframe
		shakeTimeline.jumpTo(Duration.millis(47));
		shakeTimeline.setRate(rate);
		shakeTimeline.playFromStart();
	}
}
