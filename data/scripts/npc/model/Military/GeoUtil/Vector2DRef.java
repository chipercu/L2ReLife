package npc.model.Military.GeoUtil;

import l2open.gameserver.model.L2Object;

public class Vector2DRef {

    public static Point getFrontPoint(L2Object actor, L2Object reference, double dist){
        Point A = new Point(actor.getX(), actor.getY()); //точка объекта
        Point ref = new Point(reference.getX(), reference.getY()); //точка относительно которой вычисляем
        double angle = calculateAngleFrom(A, ref);
        double radian1 = Math.toRadians(angle - 90);
        double radian2 = Math.toRadians(angle + 90);
        int c_x = (int) (A.x - Math.sin(radian1) * dist);
        int c_y = (int) (A.y + Math.cos(radian1) * dist);
        return new Point(c_x + (int) (Math.cos(radian2)), c_y + (int) (Math.sin(radian2))); //точка на одной линии с точкой объекта и точкой ref
    }
    public static Point getBackPoint(L2Object actor, L2Object reference, double dist){
        Point A = new Point(actor.getX(), actor.getY()); //точка объекта
        Point ref = new Point(reference.getX(), reference.getY()); //точка относительно которой вычисляем
        double angle = calculateAngleFrom(A, ref);
        double radian1 = Math.toRadians(angle + 90);
        double radian2 = Math.toRadians(angle - 90);
        int c_x = (int) (A.x - Math.sin(radian1) * dist);
        int c_y = (int) (A.y + Math.cos(radian1) * dist);
        return new Point(c_x + (int) (Math.cos(radian2)), c_y + (int) (Math.sin(radian2))); //точка на одной линии с точкой объекта и точкой ref
    }
    public static Point getFrontLeftPoint(L2Object actor, L2Object reference, double dist, int leftDist){
        Point A = new Point(actor.getX(), actor.getY()); //точка объекта
        Point ref = new Point(reference.getX(), reference.getY()); //точка относительно которой вычисляем
        double angle = calculateAngleFrom(A, ref);
        double radian1 = Math.toRadians(angle - 90);
        double radian2 = Math.toRadians(angle + 90);
        int c_x = (int) (A.x - Math.sin(radian1) * dist);
        int c_y = (int) (A.y + Math.cos(radian1) * dist);
        return new Point(c_x + (int) (Math.cos(radian1) * leftDist), c_y + (int) (Math.sin(radian1) * leftDist));// точка с лева
    }
    public static Point getFrontRightPoint(L2Object actor, L2Object reference, double dist, int rightDist){
        Point A = new Point(actor.getX(), actor.getY()); //точка объекта
        Point ref = new Point(reference.getX(), reference.getY()); //точка относительно которой вычисляем
        double angle = calculateAngleFrom(A, ref);
        double radian1 = Math.toRadians(angle - 90);
        double radian2 = Math.toRadians(angle + 90);
        int c_x = (int) (A.x - Math.sin(radian1) * dist);
        int c_y = (int) (A.y + Math.cos(radian1) * dist);
        return new Point(c_x + (int) (Math.cos(radian2) * rightDist), c_y + (int) (Math.sin(radian2) * rightDist));// точка с права
    }
    public static Point getBackLeftPoint(L2Object actor, L2Object reference, double dist, int leftDist){
        Point A = new Point(actor.getX(), actor.getY()); //точка объекта
        Point ref = new Point(reference.getX(), reference.getY()); //точка относительно которой вычисляем
        double angle = calculateAngleFrom(A, ref);
        double radian1 = Math.toRadians(angle + 90);
        double radian2 = Math.toRadians(angle - 90);
        int c_x = (int) (A.x - Math.sin(radian1) * dist);
        int c_y = (int) (A.y + Math.cos(radian1) * dist);
        return new Point(c_x + (int) (Math.cos(radian1) * leftDist), c_y + (int) (Math.sin(radian1) * leftDist));// точка с лева
    }
    public static Point getBackRightPoint(L2Object actor, L2Object reference, double dist, int rightDist){
        Point A = new Point(actor.getX(), actor.getY()); //точка объекта
        Point ref = new Point(reference.getX(), reference.getY()); //точка относительно которой вычисляем
        double angle = calculateAngleFrom(A, ref);
        double radian1 = Math.toRadians(angle + 90);
        double radian2 = Math.toRadians(angle - 90);
        int c_x = (int) (A.x - Math.sin(radian1) * dist);
        int c_y = (int) (A.y + Math.cos(radian1) * dist);
        return new Point(c_x + (int) (Math.cos(radian2) * rightDist), c_y + (int) (Math.sin(radian2) * rightDist));// точка с права
    }

    public static void test(){
        Point A = new Point(-85, -41); //точка объекта
        Point ref = new Point(70, -87); //точка относительно которой вычисляем

        //todo: вынести потом в параметры метода
        double distance = 40; //на какой дистанции от точки объекта будут находится точки
        int w = 20; //расстояние между найденных точек
        boolean direction = false;  //true- ref будет считаться что она позади объекта, false - впереди

        double angle = calculateAngleFrom(A, ref);
        System.out.println(angle);

        double radian1 = Math.toRadians(angle - 90);
        double radian2 = Math.toRadians(angle + 90);
        if (direction){
            radian1 = Math.toRadians(angle + 90);
            radian2 = Math.toRadians(angle - 90);
        }
        int c_x = (int) (A.x - Math.sin(radian1) * distance);
        int c_y = (int) (A.y + Math.cos(radian1) * distance);

        Point p1 = new Point(c_x + (int) (Math.cos(radian2)), c_y + (int) (Math.sin(radian2))); //точка на одной линии с точкой объекта и точкой ref
        Point p2 = new Point(c_x + (int) (Math.cos(radian1) * w), c_y + (int) (Math.sin(radian1) * w));// точка с лева
        Point p3 = new Point(c_x + (int) (Math.cos(radian2) * w), c_y + (int) (Math.sin(radian2) * w));// точка с права
        Point p5 = new Point(c_x + (int) (Math.cos(radian1) * (w*2)), c_y + (int) (Math.sin(radian1) * (w*2))); //и так далее...
        Point p6 = new Point(c_x + (int) (Math.cos(radian2) * (w*2)), c_y + (int) (Math.sin(radian2) * (w*2)));
        PointsOnLine line = new PointsOnLine(p1, p2, p3, p5, p6);
        System.out.println(line);

    }
    public static double calculateAngleFrom(Point a, Point b) {
        double angleTarget = Math.toDegrees(Math.atan2(b.y - a.y, b.x - a.x));
        if(angleTarget < 0)
            angleTarget = 360 + angleTarget;
        return angleTarget;
    }




    public final double getDistance(Point a, Point b) {
        return Math.sqrt(getXYDeltaSq(a, b));
    }
    public Point getFrontPoint(Point a, Point ref, int dist){
        int posX = a.x;
        int posY = a.y;
        int signx = posX < ref.x ? -1 : 1;
        int signy = posY < ref.y ? -1 : 1;
        posX += Math.round(signx * dist);
        posY += Math.round(signy * dist);
        return new Point(posX, posY);
    }
    public final long getXYDeltaSq(Point a, Point b) {
        long dx = b.x - a.x;
        long dy = b.y - a.y;
        return dx * dx + dy * dy;
    }



}
