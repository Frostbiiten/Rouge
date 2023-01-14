package application.World;

import java.util.ArrayList;

import application.Vector2;

public class Triangle
{
	private Vector2 a, b, c;
	
	public Triangle(Vector2 a, Vector2 b, Vector2 c)
	{
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public boolean inCircumcircle(Vector2 point)
	{
		double ab = Vector2.SqrMagnitude(a);
		double cd = Vector2.SqrMagnitude(b);
		double ef = Vector2.SqrMagnitude(c);

		double circumX = (ab * (c.y - b.y) + cd * (a.y - c.y) + ef * (b.y - a.y)) / (a.x * (c.y - b.y) + b.x * (a.y - c.y) + c.x * (b.y - a.y));
		double circumY = (ab * (c.x - b.x) + cd * (a.x - c.x) + ef * (b.x - a.x)) / (a.y * (c.x - b.x) + b.y * (a.x - c.x) + c.y * (b.x - a.x));

		Vector2 circum = new Vector2(circumX / 2, circumY / 2);
		double circumRadius = Vector2.SqrMagnitude(Vector2.Subtract(a,  circum));
		double dist = Vector2.SqrMagnitude(Vector2.Subtract(point, circum));
		return dist <= circumRadius;
	}


	
	public ArrayList<Edge> getEdges()
	{
		ArrayList<Edge> edges = new ArrayList<Edge>();
		edges.add(new Edge(a, b));
		edges.add(new Edge (b, c));
		edges.add(new Edge(c, a));
		return edges;
	}

	public Vector2 getA()
	{
		return a;
	}

	public Vector2 getB()
	{
		return b;
	}

	public Vector2 getC()
	{
		return b;
	}
	
	public boolean onVertex(Vector2 point)
	{
		return (Math.abs(a.x - point.x) < Triangulator.LENIENCY && Math.abs(a.y - point.y) < Triangulator.LENIENCY);
	}
}
