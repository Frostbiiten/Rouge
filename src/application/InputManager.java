package application;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public class InputManager
{
	// Booleans to store if each direction is being pressed
	private static boolean up, down, left, right;
	
	// Vector representation of input direction
	private static Vector2 directionalInput;
	
	// Mouse position relative to top left of scene
	private static Vector2 mousePos;
	private static boolean leftPressed;
	private static boolean rightPressed;
	
	public static void init(Scene scene)
	{
		directionalInput = new Vector2(0, 0);
		mousePos = new Vector2(0, 0);
		
		scene.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.W)
			{
				up = true;
				updateDirectionalInput();
			}
			else if (e.getCode() == KeyCode.A)
			{
				left = true;
				updateDirectionalInput();
			}
			else if (e.getCode() == KeyCode.S)
			{
				down = true;
				updateDirectionalInput();
			}
			else if (e.getCode() == KeyCode.D)
			{
				right = true;
				updateDirectionalInput();
			}
			else if (e.getCode() == KeyCode.L)
			{
				if (GameManager.getPlayer().getDead())
				{
					UI.gameOverResponse(false);
				}
			}
			else if (e.getCode() == KeyCode.R)
			{
				if (GameManager.getPlayer().getDead())
				{
					UI.gameOverResponse(true);
				}
			}
		});

		scene.setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.W)
			{
				up = false;
				updateDirectionalInput();
			}
			else if (e.getCode() == KeyCode.A)
			{
				left = false;
				updateDirectionalInput();
			}
			else if (e.getCode() == KeyCode.S)
			{
				down = false;
				updateDirectionalInput();
			}
			else if (e.getCode() == KeyCode.D)
			{
				right = false;
				updateDirectionalInput();
			}
		});
		
		// Update mouse position when dragged or simply moved
		scene.setOnMouseMoved(e -> 
		{
			mousePos.x = e.getX();
			mousePos.y = e.getY();
			UI.updateCrosshairPos(mousePos.x, mousePos.y);
		});

		scene.setOnMouseDragged(e -> 
		{
			mousePos.x = e.getX();
			mousePos.y = e.getY();
			UI.updateCrosshairPos(mousePos.x, mousePos.y);
		});

		// Trigger click in player class when pressed recieved
		scene.setOnMousePressed(e ->
		{
			MouseButton btn = e.getButton();
			if (btn == MouseButton.PRIMARY)
			{
				GameManager.getPlayer().click(true);
				leftPressed = true;
			}
			else if (btn == MouseButton.SECONDARY)
			{
				GameManager.getPlayer().click(false);
				rightPressed = true;
			}
		});

		scene.setOnMouseReleased(e ->
		{
			MouseButton btn = e.getButton();
			if (btn == MouseButton.PRIMARY)
			{
				leftPressed = false;
			}
			else if (btn == MouseButton.SECONDARY)
			{
				rightPressed = false;
			}
		});

		scene.setOnScroll(e ->
		{
			GameManager.getPlayer().scroll(e.getDeltaY());
		});
	}

	private static void updateDirectionalInput()
	{
		directionalInput.x = 0;
		if (right)
		{
			directionalInput.x++;
		}
		if (left)
		{
			directionalInput.x--;
		}

		directionalInput.y = 0;
		if (up)
		{
			directionalInput.y--;
		}
		if (down)
		{
			directionalInput.y++;
		}
	}
	public static Vector2 getDirectionalInput()
	{
		return directionalInput;
	}
	public static Vector2 getMousePos()
	{
		return mousePos;
	}
	public static Vector2 getMouseWorldPos()
	{
		Vector2 camPosNoShake = Camera.getPosNoShake();
		return new Vector2((mousePos.x / AppProps.SCALE) + camPosNoShake.x, (mousePos.y / AppProps.SCALE) + camPosNoShake.y);
	}

	// Methods to check if left or right mouse button is pressed
	public static boolean leftMousePressed()
	{
		return leftPressed;
	}
	public static boolean rightMousePressed()
	{
		return rightPressed;
	}
}