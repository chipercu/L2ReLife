package npc.model.Military.GeoUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PointsOnLine {
    List<Point> points = new ArrayList<>();

    public PointsOnLine(Point... p) {
        points.addAll(Arrays.asList(p));
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public void addPoint(Point point){
        this.points.add(point);
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
