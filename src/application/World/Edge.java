package application.World;

import application.Vector2;

public class Edge
{
	private Vector2 a, b;
	
	public Edge(Vector2 a, Vector2 b)
	{
		this.a = a;
		this.b = b;
	}
	
	public Vector2 getA()
	{
		return a;
	}

	public Vector2 getB()
	{
		return b;
	}
}
