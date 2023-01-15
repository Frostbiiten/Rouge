package application;

public class Util
{
    public static double lerp(double a, double b, double t)
    {
        return a + (b - a) * t;
    }
}
