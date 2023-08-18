package l2open.util.geometry.Vector;

public class PointNSWE2P{

    private double x;
    private double y;
    private Side side;
    private Point2D point2D;

    public PointNSWE2P(Point2D origin, Point2D ref, double dist, Side side) {
        this.side = side;
        this.point2D = getNSWEPoint(origin, ref, dist, side);
        this.x = point2D.x;
        this.y = point2D.y;
    }

    public static Point2D getNSWEPoint(Point2D origin, Point2D ref, double dist, Side side){
        double radian;
        if (side == Side.RIGHT){
            radian = Math.toRadians(calculateAngleFrom(origin, ref) + 90);
            return new Point2D((int) (origin.x  + (Math.cos(radian)) * dist), (int) (origin.y + (Math.sin(radian)) * dist));
        }else if (side == Side.LEFT){
            radian = Math.toRadians(calculateAngleFrom(origin, ref) - 90);
            return new Point2D((int) (origin.x + (Math.cos(radian)) * dist), (int) (origin.y + (Math.sin(radian)) * dist));
        } else if (side == Side.BACK) {
            radian = Math.toRadians(calculateAngleFrom(origin, ref) + 90);
            return new Point2D((int) (origin.x - Math.sin(radian) * dist), (int) (origin.y + Math.cos(radian) * dist));
        } else if (side == Side.FRONT) {
            radian = Math.toRadians(calculateAngleFrom(origin, ref) - 90);
            return new Point2D((int) (origin.x - Math.sin(radian) * dist), (int) (origin.y + Math.cos(radian) * dist));
        }
        return origin;
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

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public Point2D getPoint2D() {
        return point2D;
    }

    public void setPoint2D(Point2D point2D) {
        this.point2D = point2D;
    }

    public static double calculateAngleFrom(Point2D p1, Point2D p2) {
//        double angleTarget = Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
        double angleTarget = Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
        if(angleTarget < 0)
            angleTarget = 360 + angleTarget;
        return angleTarget;
    }

    @Override
    public String toString() {
        return this.side + "-Point[" +
                "x: " + this.x +
                ", y: " + this.y +
                ']';
    }
}
