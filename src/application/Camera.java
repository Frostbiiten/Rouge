package application;

public class Camera
{
	private static Vector2 cameraPos;
	
	public static void setPos(Vector2 pos)
	{
		cameraPos = new Vector2(pos);
	}

	public static void setPos(double x, double y)
	{
		cameraPos.x = x;
		cameraPos.x = y;
	}

	public static void move(double x, double y)
	{
		cameraPos.x += x;
		cameraPos.y += y;
	}

	public static Vector2 getPos()
	{
		return cameraPos;
	}
	
	private static Vector2 cameraTarget = new Vector2();
	public static void freeCam()
	{
		cameraTarget.x += InputManager.getDirectionalInput().x * 10;
		cameraTarget.y += InputManager.getDirectionalInput().y * 10;
		Camera.setPos(Vector2.Lerp(Camera.getPos(), cameraTarget, 0.1));
	}
}
