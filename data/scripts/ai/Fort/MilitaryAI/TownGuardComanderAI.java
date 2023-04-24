package scripts.ai.Fort.MilitaryAI;

import ai.Fort.MilitaryAI.TankCommanderAI;
import l2open.common.RunnableImpl;
import l2open.common.ThreadPoolManager;
import l2open.config.ConfigValue;
import l2open.database.mysql;
import l2open.extensions.network.MMOConnection;
import l2open.gameserver.ai.L2PlayerAI;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Object;
import l2open.gameserver.model.L2ObjectsStorage;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.base.UnitType;
import l2open.gameserver.model.entity.residence.Fortress;
import l2open.gameserver.network.L2GameClient;
import l2open.gameserver.serverpackets.Say2;
import l2open.util.GArray;
import l2open.util.Location;
import npc.model.Military.GeoUtil.Point;
import npc.model.Military.GeoUtil.Vector2DRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static l2open.gameserver.model.base.UnitType.knight;


public class TownGuardComanderAI extends L2PlayerAI {
    private List<L2Player> units = new ArrayList<>();

    private L2Object objectReference;
    private L2Player commander;
    private ScheduledFuture<?> ai;
    private ScheduledFuture<?> moveAi;

    private int currentPatrolPoint = 0;
    private Location[] patrolPoints = {
            new Location(82712, 147896, -3495),
            new Location(81128, 147896, -3495),
            new Location(81144, 149336, -3495),
            new Location(82728, 149304, -3495),
    };

    private Fortress fortress;
    private List<LocForUnit> unitLocation = new ArrayList<>();
    private LocForUnit loc1;
    private LocForUnit loc2;
    private LocForUnit loc3;
    private LocForUnit loc4;
    private Location refLocation;


    public TownGuardComanderAI(L2Player actor) {
        super(actor);
        spawnUnit();
//        moveAi = ThreadPoolManager.getInstance().scheduleAtFixedRate(new MoveTask(), 500, 500);

    }

    public class MoveTask extends RunnableImpl {


        @Override
        public void runImpl() throws Exception {
            say("move");

            if (refLocation != null && refLocation.distance(getActor().getLoc()) > 100) {
                toPosition(getActor().getLoc(), FormationType.PATROL, false);
                unitsMove();
            }
            if (getActor().isMoving || getActor().isMovementDisabled()) {
                return;
            }

            if (!getActor().isInCombat()) {
                final Location point = patrolPoints[currentPatrolPoint];
                refLocation = point;
                move(point);
//                toPosition(getActor().getLoc(), FormationType.PATROL, false);
//                unitsMove();

                currentPatrolPoint++;
                if (currentPatrolPoint >= patrolPoints.length) {
                    currentPatrolPoint = 0;
                }
            }


        }
    }


    enum FormationType {
        INSTUCTAJ,
        DEFENCE,
        TRAINING,
        PATROL,
        RANDOM
    }

    @Override
    public void unitChatListener(L2Player player, int _chatType, String command) {
        final L2Player actor = getActor();
        if (command != null && !command.isEmpty()) {
            switch (command) {
                case "startMove": {
                    say("start_patrol");
                    commander = player;
                    toPosition(getActor().getLoc(), FormationType.PATROL, false);
                    unitsMove();
                    moveAi = ThreadPoolManager.getInstance().scheduleAtFixedRate(new MoveTask(), 1000, 1000);

                    break;
                }
                case "stopMove": {
                    if (moveAi != null) {
                        say("stop_patrol");
                        getActor().stopMove();
                        unitsStopMove();
                        moveAi.cancel(true);
                        moveAi = null;
                    }
                }
            }
        }
    }

    private void clearUnitsLoc() {
        unitLocation.clear();
    }

    private void move(Location location) {
//        clearUnitsLoc();
        getActor().moveToLocation(location, 0, true);
    }

    private Location getUnitPositionByType(UnitType type) {
        for (LocForUnit loc : unitLocation) {
            if (loc.getLocForType() == type && !loc.isOccupied()) {
                loc.setOccupied(true);
                return loc.loc;
            }
        }
        return Location.coordsRandomize(getActor().getLoc(), 200, 300);
    }

    private void unitsMove() {
        for (L2Player unit : units) {
            if (unit.isDead() || unit.isMovementDisabled()) {
                return;
            }
//            unit.moveToLocation(getUnitPositionByType(unit.getUnitType()), 0, false);
//            units.get(0).moveToLocation(loc1.loc.x, loc1.loc.y, loc1.loc.z, 0, true);
//            units.get(1).moveToLocation(loc2.loc.x, loc2.loc.y, loc2.loc.z, 0, true);
//            units.get(2).moveToLocation(loc3.loc.x, loc3.loc.y, loc3.loc.z, 0, true);
//            units.get(3).moveToLocation(loc4.loc.x, loc4.loc.y, loc4.loc.z, 0, true);
            if (units.get(0).getDistance(getActor()) > 150) {
                units.get(0).moveToLocation(loc1.loc, 1, true);
            }
            if (units.get(0).getDistance(getActor()) > 150) {
                units.get(1).moveToLocation(loc2.loc, 1, true);
            }
            if (units.get(0).getDistance(getActor()) > 150) {
                units.get(2).moveToLocation(loc3.loc, 1, true);
            }
            if (units.get(0).getDistance(getActor()) > 150) {
                units.get(3).moveToLocation(loc4.loc, 1, true);
            }

        }
    }

    private void unitsStopMove() {
        for (L2Player unit : units) {
            unit.stopMove();
        }
    }

    private void toPosition(Location refLocation, FormationType type, boolean running) {
        if (running) {
            getActor().setRunning();
            units.forEach(L2Character::setRunning);
        } else {
            getActor().setWalking();
            units.forEach(L2Character::setWalking);
        }
        if (type == FormationType.PATROL) {
            findPatrolLoc(refLocation, refLocation);
        } else if (type == FormationType.INSTUCTAJ) {
            findInstructLoc(getActor(), refLocation);
        } else if (type == FormationType.RANDOM) {

        }
    }


    static class LocForUnit {
        private Location loc;
        private UnitType locForType;
        private boolean isOccupied;

        public LocForUnit(Location loc, UnitType locForType) {
            this.loc = loc;
            this.locForType = locForType;
            this.isOccupied = false;
        }

        public void setOccupied(boolean occupied) {
            isOccupied = occupied;
        }

        public Location getLoc() {
            return loc;
        }

        public UnitType getLocForType() {
            return locForType;
        }

        public boolean isOccupied() {
            return isOccupied;
        }
    }

    private void findPatrolLoc(Location loc, Location ref) {
        clearUnitsLoc();
//        unitLocation.add(new LocForUnit(calcPoint(loc, ref, 50, 50, true, false), knight));
//        unitLocation.add(new LocForUnit(calcPoint(loc, ref, 50, 50, false, false), knight));
//        unitLocation.add(new LocForUnit(calcPoint(loc, ref, 100, 50, true, false), knight));
//        unitLocation.add(new LocForUnit(calcPoint(loc, ref, 100, 50, false, false), knight));
//        loc1 = new LocForUnit(calcPoint(loc, ref, 100, 50, true, false), knight);
//        loc2 = new LocForUnit(calcPoint(loc, ref, 100, 50, false, false), knight);
//        loc3 = new LocForUnit(calcPoint(loc, ref, 200, 50, true, false), knight);
//        loc4 = new LocForUnit(calcPoint(loc, ref, 200, 50, false, false), knight);

        loc1 = new LocForUnit(getFrontLeftPoint(getActor(), ref, 70, 40, false), knight);
        loc2 = new LocForUnit(getFrontRightPoint(getActor(), ref, 70, 40, false), knight);
        loc3 = new LocForUnit(getFrontLeftPoint(getActor(), ref, 150, 40, false), knight);
        loc4 = new LocForUnit(getFrontRightPoint(getActor(), ref, 150, 40, false), knight);

    }

    private void findInstructLoc(L2Object actor, Location ref) {
        int unitDist = 150;
        int unitWidth = 50;
        clearUnitsLoc();
        unitLocation.add(new LocForUnit(getFrontLeftPoint(actor, ref, unitDist, unitWidth, false), knight));
        unitLocation.add(new LocForUnit(getFrontRightPoint(actor, ref, unitDist, unitWidth, false), knight));
        unitLocation.add(new LocForUnit(getFrontLeftPoint(actor, ref, unitDist * 2, unitWidth, false), knight));
        unitLocation.add(new LocForUnit(getFrontRightPoint(actor, ref, unitDist * 2, unitWidth, false), knight));
    }


    public List<L2Player> getUnits() {
        return units;
    }

    public void say(String text) {
        Say2 cs = new Say2(getActor().getObjectId(), 16, getActor().getName(), text);
        for (L2Player player : L2ObjectsStorage.getPlayers()) {
            player.sendPacket(cs);
        }
    }


    public Location getFrontLeftPoint(L2Object actor, Location locRef, double dist, int leftDist, boolean front) {
        Point A = new Point(actor.getX(), actor.getY()); //точка объекта
        Point ref = new Point(locRef.x, locRef.y); //точка относительно которой вычисляем
        double angle = calculateAngleFrom(A, ref);
        double radian1 = Math.toRadians(angle - 90);
        double radian2 = Math.toRadians(angle + 90);
        if (!front) {
            radian1 = Math.toRadians(angle + 90);
            radian2 = Math.toRadians(angle - 90);
        }
        int c_x = (int) (A.x - Math.sin(radian1) * dist);
        int c_y = (int) (A.y + Math.cos(radian1) * dist);
//        return new Point(c_x + (int) (Math.cos(radian1) * leftDist), c_y + (int) (Math.sin(radian1) * leftDist));// точка с лева

        return new Location(c_x + (int) (Math.cos(radian1) * leftDist), c_y + (int) (Math.sin(radian1) * leftDist), actor.getZ());
    }

    public Location getFrontRightPoint(L2Object actor, Location locRef, double dist, int rightDist, boolean front) {
        Point A = new Point(actor.getX(), actor.getY()); //точка объекта
        Point ref = new Point(locRef.x, locRef.y); //точка относительно которой вычисляем
        double angle = calculateAngleFrom(A, ref);
        double radian1 = Math.toRadians(angle - 90);
        double radian2 = Math.toRadians(angle + 90);
        if (!front) {
            radian1 = Math.toRadians(angle + 90);
            radian2 = Math.toRadians(angle - 90);
        }
        int c_x = (int) (A.x - Math.sin(radian1) * dist);
        int c_y = (int) (A.y + Math.cos(radian1) * dist);
//        return new Point(c_x + (int) (Math.cos(radian2) * rightDist), c_y + (int) (Math.sin(radian2) * rightDist));// точка с права
        return new Location(c_x + (int) (Math.cos(radian2) * rightDist), c_y + (int) (Math.sin(radian2) * rightDist), actor.getZ());
    }

    public double calculateAngleFrom(Point a, Point b) {
        double angleTarget = Math.toDegrees(Math.atan2(b.y - a.y, b.x - a.x));
        if (angleTarget < 0)
            angleTarget = 360 + angleTarget;
        return angleTarget;
    }


    public double getDistance(Point a, Point b) {
        return Math.sqrt(getXYDeltaSq(a, b));
    }

    public Point getCenterPoint(Point a, Point ref, int dist) {
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

    private void spawnUnit() {

        GArray<HashMap<String, Object>> list = mysql.getAll("SELECT `obj_id`, `value`, (SELECT `account_name` FROM `characters` WHERE `characters`.`obj_Id` = `character_variables`.`obj_id` LIMIT 1) AS `account_name` FROM `character_variables` WHERE name LIKE 'unit_type'");

        for (HashMap<String, Object> e : list) {
            if (getActor().getObjectId() == (Integer) e.get("obj_id")) {
                continue;
            }
            L2GameClient client = new L2GameClient(new MMOConnection<>(null), true);
            client.setCharSelection((Integer) e.get("obj_id"));
            L2Player p = client.loadCharFromDisk(0);

            if (p == null || p.getVar("unit_type") == null || !p.getVar("unit_type").equals("knight")) {
                continue;
            }
            p.setUnitType(knight);

            client.setLoginName(e.get("account_name") == null ? "OfflineTrader_" + p.getName() : (String) e.get("account_name"));
            p.setLoc(Location.coordsRandomize(getActor().getLoc(), 100, 200));
            client.OnOfflineTrade();
            p.spawnMe();
            p.updateTerritories();
            p.setOnlineStatus(true);
            p.setInvisible(false);
            p.setConnected(true);
            p.setNameColor(Integer.decode("0x" + ConfigValue.OfflineTradeNameColor));
            p.broadcastUserInfo(true);
            this.units.add(p);
        }
        say("spawned " + this.units.size());
    }


}
