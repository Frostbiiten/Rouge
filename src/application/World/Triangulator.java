package application.World;

import java.util.ArrayList;

import application.Vector2;

public class Triangulator
{
	// For floating point operations, valeus can be very precise
	public static final double LENIENCY = 0.0001;

	public Triangulator()
	{
		// no constructor code
	}
	
	public ArrayList<Edge> triangulate(ArrayList<Vector2> points)
	{
		// Get bounds
		Vector2 min = new Vector2(Double.MAX_VALUE, Double.MAX_VALUE);
		Vector2 max = new Vector2(-Double.MAX_VALUE, -Double.MAX_VALUE);
		
		for (int i = 0; i < points.size(); i++)
		{
			Vector2 point = points.get(i);
			min.x = Math.min(min.x, point.x);
			min.y = Math.min(min.y, point.y);
			max.x = Math.max(max.x, point.x);
			max.y = Math.max(max.y, point.y);
		}
		
		// Difference between min and max
		Vector2 diff = Vector2.Subtract(max, min);
		Vector2 mid = Vector2.Divide(Vector2.Add(min, max), 2);

		ArrayList<Triangle> tris = new ArrayList<Triangle>();

		// Create supertriangle
		Vector2 p0 = new Vector2(min.x - 1, min.y - 1);
		Vector2 p1 = new Vector2(mid.x - 1, max.y + Math.max(diff.x, diff.y) * 2);
		Vector2 p2 = new Vector2(mid.x + Math.max(diff.x, diff.y) * 2, min.y - 1);

		tris.add(new Triangle(p0, p1, p2));
	
		for (int i = 0; i < points.size(); i++)
		{
			Vector2 currentPoint = points.get(i);
			ArrayList<Edge> uniqueEdges = new ArrayList<Edge>();

			for (int j = 0; j < tris.size(); j++)
			{
				if (tris.get(j).inCircumcircle(currentPoint))
				{
					uniqueEdges.addAll(tris.get(j).getEdges());
					
					// Remove and fix index
					tris.remove(j);
					j--;
				}
			}
			
			ArrayList<Edge> badEdges = new ArrayList<Edge>();

			// Remove nonunique
			for (int a = 0; a < uniqueEdges.size(); a++)
			{
				for (int b = a + 1; b < uniqueEdges.size(); b++)
				{
					Edge edgeA = uniqueEdges.get(a);
					Edge edgeB = uniqueEdges.get(b);
					
					// Check if edges are the same (too similar)
					if (closeTo(edgeA, edgeB))
					{
						badEdges.add(edgeA);
						badEdges.add(edgeB);
					}
				}
			}
			
			// Remove all shared edges
			uniqueEdges.removeAll(badEdges);
			
			// Create new triangle!
			for (int j = 0; j < uniqueEdges.size(); j++)
			{
				Edge e = uniqueEdges.get(j);
				tris.add(new Triangle(e.getA(), e.getB(), currentPoint));
			}
		}

		// Remove original points
		for (int i = 0; i < tris.size(); i++)
		{
			Triangle tri = tris.get(i);

			// remove original supertriangle points
			if (closeTo(tri.getA(), p0) || closeTo(tri.getB(), p0) || closeTo(tri.getC(), p0) ||
				closeTo(tri.getA(), p1) || closeTo(tri.getB(), p1) || closeTo(tri.getC(), p1) ||
				closeTo(tri.getA(), p2) || closeTo(tri.getB(), p2) || closeTo(tri.getC(), p2))
			{
				tris.remove(tri);
				i--;
			}
		}
		
		ArrayList<Edge> finalEdges = new ArrayList<Edge>();
		for (int i = 0; i < tris.size(); i++)
		{
			finalEdges.addAll(tris.get(i).getEdges());
		}
		
		return finalEdges;
	}

	// Methods to check if two values are close to each other minding floating point precision

	public static boolean closeTo(double a, double b)
	{
		return Math.abs(a - b) < LENIENCY;
	}

	public static boolean closeTo(Vector2 a, Vector2 b)
	{
		return closeTo(a.x, b.x) && closeTo(a.y, b.y);
	}

	public static boolean closeTo(Edge a, Edge b)
	{
		return (
			(closeTo(a.getA(), b.getA()) && closeTo(a.getB(), b.getB())) ||
			(closeTo(a.getA(), b.getB()) && closeTo(a.getB(), b.getA())));
	}
}
