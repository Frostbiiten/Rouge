package application;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class InputManager
{
	// Booleans to store if each direction is being pressed
	private static boolean up, down, left, right;
	
	// Vector representation of input direction
	private static Vector2 directionalInput;
	
	// Mouse position relative to top left of scene
	private static Vector2 mousePos;
	
	public static void Init(Scene scene)
	{
		directionalInput = new Vector2(0, 0);
		mousePos = new Vector2(0, 0);
		
		// TODO: make generic version of this (maybe array of keycode-bool pairs to update or something)
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
		
		scene.setOnMouseMoved(e -> 
		{
			mousePos.x = e.getX();
			mousePos.y = e.getY();
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
}