package application;

public class Vector2 
{
    enum Direction
    {
        UP,
        DOWN,
        LEFT,
        RIGHT;

        Vector2 getVector()
        {
            switch (this)
            {
                case UP:
                    return Vector2.UP;
                case DOWN:
                    return Vector2.DOWN;
                case LEFT:
                    return Vector2.LEFT;
                case RIGHT:
                    return Vector2.RIGHT;
            }

            return null;
        }

		boolean isHorizontal()
		{
			return this == Direction.LEFT || this == Direction.RIGHT;
		}
    }

	// FIELDS
	public double x, y;

	// CONSTRUCTORS
	public Vector2()
	{
		x = 0;
		y = 0;
	}
	
	public Vector2(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	// Copy constructor
	public Vector2(Vector2 vector)
	{
		x = vector.x;
		y = vector.y;
	}

	// Static constants
	public static final Vector2 UP = new Vector2(0, -1);
	public static final Vector2 DOWN = new Vector2(0, 1);
	public static final Vector2 RIGHT = new Vector2(1, 0);
	public static final Vector2 LEFT = new Vector2(-1, 0);
	public static final Vector2 ZERO = new Vector2(0, 0);

	// METHODS
	// Arithmetic
	public static Vector2 Add(Vector2 a, Vector2 b)
	{
		return new Vector2(a.x + b.x, a.y + b.y);
	}

	public static Vector2 Subtract(Vector2 a, Vector2 b)
	{
		return new Vector2(a.x - b.x, a.y - b.y);
	}

	public static Vector2 Multiply(Vector2 a, Vector2 b)
	{
		return new Vector2(a.x * b.x, a.y * b.y);
	}

	public static Vector2 Multiply(Vector2 a, double b)
	{
		return new Vector2(a.x * b, a.y * b);
	}

	public static Vector2 Divide(Vector2 a, Vector2 b)
	{
		return new Vector2(a.x / b.x, a.y / b.y);
	}

	public static Vector2 Divide(Vector2 a, double b)
	{
		return new Vector2(a.x / b, a.y / b);
	}

	// Utility functions
	public static Vector2 Lerp(Vector2 a, Vector2 b, double time)
	{
		return Add(a, Multiply(Subtract(b, a), time));
	}
	
	public static double SqrMagnitude(Vector2 vector)
	{
		return vector.x * vector.x + vector.y * vector.y;
	}

	public static double Magnitude(Vector2 vector)
	{
		return Math.sqrt(SqrMagnitude(vector));
	}

	public static double SqrDistance(Vector2 a, Vector2 b)
	{
		double xDelta = a.x - b.x;
		double yDelta = a.y - b.y;
		return xDelta * xDelta + yDelta * yDelta;
	}
	
	public static double Distance(Vector2 a, Vector2 b)
	{
		return Math.sqrt(SqrDistance(a, b));
	}
	
	public static double DotProduct(Vector2 a, Vector2 b)
	{
		return a.x * b.x + a.y * b.y;
	}

	public static Vector2 Normalize(Vector2 vector)
	{
		return Vector2.Divide(vector, Vector2.Magnitude(vector));
	}

	public static Vector2 Perpendicular(Vector2 vector)
	{
		return new Vector2(-vector.y, vector.x);
	}
	
	public String toString() 
	{
		return String.format("%f, %f", x, y);
	}
}
