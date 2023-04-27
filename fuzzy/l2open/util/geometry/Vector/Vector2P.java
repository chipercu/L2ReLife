package l2open.util.geometry.Vector;

public class Vector2P {

    private double x;
    private double y;
    private final Point2D origin;
    private final Point2D point;
    private final Point2D point_O2P;

    public Vector2P(Point2D origin, Point2D p1, Point2D p2) {
        this.origin = origin;
        this.point_O2P = calcO(p1, p2);
        this.point = calc();
        this.x = point.x;
        this.y = point.y;
    }

    private Point2D calc() {
        double c = 2;
        double x = point_O2P.x * c - origin.x;
        double y = point_O2P.y * c - origin.y;
        return new Point2D(x, y);
    }


    public Point2D calcO(Point2D p1, Point2D p2){
        double xo = (p1.x + p2.x) / 2;
        double yo = (p1.y + p2.y) / 2;
        return new Point2D(xo, yo);
    }


    public Point2D getPoint_O2P() {
        return this.point_O2P;
    }

    public Point2D getPoint() {
        return this.point;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Vector( " + point + " )";
    }
}
