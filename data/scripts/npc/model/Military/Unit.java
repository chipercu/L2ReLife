package npc.model.Military;

import ai.MilitaryArt.BattleOrderType;
import ai.MilitaryArt.MilitaryRank;
import l2open.gameserver.model.L2Object;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.templates.L2PlayerTemplate;
import l2open.util.Location;
import l2open.util.Rnd;
import npc.model.Military.GeoUtil.Point;
import npc.model.Military.GeoUtil.Vector2DRef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static npc.model.Military.Commands.*;

public class Unit extends L2Player {

    private MilitaryRank rank;

    private static final int unitDist = 200;
    private static final int unitWidth = 100;

    private List<Unit> units = new ArrayList<>();

    private List<Location> unitPosition = new ArrayList<>();
    private L2Object objectReference;

    private L2Player commander;

    public Unit(int objectId, L2PlayerTemplate template, int bot) {
        super(objectId, template, bot);


        switch (rank) {
            case OFFICER: {
                command(CHARGE);
            }
        }
    }

    private void findUnitPoints(L2Object ref) {
        int activeUnits = getActiveUnits().size();
        if (activeUnits == 0) {
            return;
        }
        List<Location> new_loc = new ArrayList<>();
        if (activeUnits == 1) {
            Point frontPoint = Vector2DRef.getFrontPoint(this, ref, unitDist);
            new_loc.add(new Location(frontPoint.x, frontPoint.y, this.getZ()));
        } else if (activeUnits == 2) {
            Point p1 = Vector2DRef.getFrontPoint(this, ref, unitDist);
            Point p2 = Rnd.nextBoolean() ? Vector2DRef.getFrontLeftPoint(this, ref, unitDist, unitWidth) : Vector2DRef.getFrontRightPoint(this, ref, unitDist, unitWidth);
            new_loc.add(new Location(p1.x, p1.y, this.getZ()));
            new_loc.add(new Location(p2.x, p2.y, this.getZ()));
        } else if (activeUnits == 3) {
            Point p1 = Vector2DRef.getFrontPoint(this, ref, unitDist);
            Point p2left = Vector2DRef.getFrontLeftPoint(this, ref, unitDist, unitWidth);
            Point p3right = Vector2DRef.getFrontRightPoint(this, ref, unitDist, unitWidth);
            new_loc.add(new Location(p1.x, p1.y, this.getZ()));
            new_loc.add(new Location(p2left.x, p2left.y, this.getZ()));
            new_loc.add(new Location(p3right.x, p3right.y, this.getZ()));
        }else if (activeUnits == 4){
            Point p1 = Vector2DRef.getFrontPoint(this, ref, unitDist);
            Point p2left = Vector2DRef.getFrontLeftPoint(this, ref, unitDist, unitWidth);
            Point p3right = Vector2DRef.getFrontRightPoint(this, ref, unitDist, unitWidth);
            Point p4 = Rnd.nextBoolean() ? Vector2DRef.getFrontLeftPoint(this, ref, unitDist, unitWidth*2) : Vector2DRef.getFrontRightPoint(this, ref, unitDist, unitWidth*2);
            new_loc.add(new Location(p1.x, p1.y, this.getZ()));
            new_loc.add(new Location(p2left.x, p2left.y, this.getZ()));
            new_loc.add(new Location(p3right.x, p3right.y, this.getZ()));
            new_loc.add(new Location(p4.x, p4.y, this.getZ()));
        } else if (activeUnits == 5) {
            Point p1 = Vector2DRef.getFrontPoint(this, ref, unitDist);
            Point p2left = Vector2DRef.getFrontLeftPoint(this, ref, unitDist, unitWidth);
            Point p3right = Vector2DRef.getFrontRightPoint(this, ref, unitDist, unitWidth);
            Point p4left = Vector2DRef.getFrontLeftPoint(this, ref, unitDist, unitWidth*2);
            Point p5right = Vector2DRef.getFrontRightPoint(this, ref, unitDist, unitWidth*2);
            new_loc.add(new Location(p1.x, p1.y, this.getZ()));
            new_loc.add(new Location(p2left.x, p2left.y, this.getZ()));
            new_loc.add(new Location(p3right.x, p3right.y, this.getZ()));
            new_loc.add(new Location(p4left.x, p4left.y, this.getZ()));
            new_loc.add(new Location(p5right.x, p5right.y, this.getZ()));
        }
        if (!new_loc.isEmpty()) {
            getUnitPosition().clear();
            getUnitPosition().addAll(new_loc);
        }
    }

    public List<Location> getUnitPosition() {
        return this.unitPosition;
    }

    public void command(Commands command) {
        if (units.isEmpty()) {
            return;
        }
        List<Unit> unitList = getActiveUnits();

        switch (command) {
            case CHARGE: {
//                if (unitList.size() != getUnitPosition().size()){
//                    findUnitPoints(getObjectReference());
//                }
//                for (int i = 0; i < unitList.size(); i++) {
//                    unitList.get(i).moveToLocation(getUnitPosition().get(i), 0, true);
//                }
            }
            case FALANG: {
                findUnitPoints(getObjectReference());
                if (unitList.size() != getUnitPosition().size()){
                    findUnitPoints(getObjectReference());
                }
                for (int i = 0; i < unitList.size(); i++) {
                    unitList.get(i).moveToLocation(getUnitPosition().get(i), 0, true);
                }
            }
        }


    }

    private List<Unit> getActiveUnits() {
        return this.units.stream()
                .filter(u -> !u.isDead())
                .filter(u -> !u.isMovementDisabled())
                .filter(u -> !u.isSleeping())
                .collect(Collectors.toList());
    }
//    private Location getFrontPoint(){
//        L2Player actor = this;
//
//        int posX = actor.getX();
//        int posY = actor.getY();
//        int posZ = actor.getZ();
//
//        int signx = posX < attacker.getX() ? -1 : 1;
//        int signy = posY < attacker.getY() ? -1 : 1;
//
//        int range = 200;
//
//        posX += Math.round(signx * range);
//        posY += Math.round(signy * range);
//        posZ = GeoEngine.getHeight(posX, posY, posZ, actor.getReflection().getGeoIndex());
//
//        if(GeoEngine.canMoveToCoord(attacker.getX(), attacker.getY(), attacker.getZ(), posX, posY, posZ, actor.getReflection().getGeoIndex())){
//            addTaskMove(posX, posY, posZ, false);
//        }
//    }


    private int getUnitCount() {
        return units.size();
    }

    private List<Location> getUnitPosition(BattleOrderType type) {
        int unitCounts = getActiveUnits().size();
        List<Location> locationList = new ArrayList<>();

        switch (type) {
            case FALANG: {
                Location loc = this.getLoc();


            }

        }

        return locationList;

    }

    public L2Object getObjectReference() {
        return objectReference;
    }

    public void setObjectReference(L2Object objectReference) {
        this.objectReference = objectReference;
    }

    public MilitaryRank getRank() {
        return rank;
    }

    public void setRank(MilitaryRank rank) {
        this.rank = rank;
    }

    public L2Player getCommander() {
        return commander;
    }

    public void setCommander(L2Player commander) {
        this.commander = commander;
    }
}
