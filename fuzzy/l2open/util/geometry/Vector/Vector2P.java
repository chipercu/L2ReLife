package l2open.util.geometry.Vector;

public class Vector2P {

    private int x;
    private int y;
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
        int c = 2;
        int x = point_O2P.x * c - origin.x;
        int y = point_O2P.y * c - origin.y;
        return new Point2D(x, y);
    }


    public Point2D calcO(Point2D p1, Point2D p2){
        int xo = (p1.x + p2.x) / 2;
        int yo = (p1.y + p2.y) / 2;
        return new Point2D(xo, yo);
    }


    public Point2D getPoint_O2P() {
        return this.point_O2P;
    }

    public Point2D getPoint() {
        return this.point;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Vector( " + point + " )";
    }
}
