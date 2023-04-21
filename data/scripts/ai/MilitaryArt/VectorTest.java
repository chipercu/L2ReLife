package ai.MilitaryArt;

import npc.model.Military.GeoUtil.Point;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VectorTest {

    class Line{
        List<Point> points = new ArrayList<>();
        public Line(Point... p) {
            points.addAll(Arrays.asList(p));
        }
        @Override
        public String toString() {
            StringBuilder res = new StringBuilder("Line{\n");
            for (int i = 0; i < points.size(); i++) {
                res.append("point").append(i + 1 ).append("{ x:").append(points.get(i).x).append(" y:").append(points.get(i).y).append("}\n");
            }
            res.append("}");
            return res.toString();
        }
    }
//    class Point{
//        public int x;
//        public int y;
//        public Point(int x, int y) {
//            this.x = x;
//            this.y = y;
//        }
//        @Override
//        public String toString() {
//            return "Point{" +
//                    "x=" + x +
//                    ", y=" + y +
//                    '}';
//        }
//    }
    @Test
    public void test(){
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
        Line line = new Line(p1, p2, p3, p5, p6);
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

    class Square{
        private final Point a;
        private final Point b;
        private final Point c;
        private final Point d;
        public Square(Point a, Point b, Point c, Point d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        @Override
        public String toString() {
            return "Square{" +
                    "a=" + a.x + ":" + a.y +
                    ", b=" + b.x + ":" + b.y +
                    ", c=" + c.x + ":" + c.y +
                    ", d=" + d.x + ":" + d.y +
                    '}';
        }
    }




}