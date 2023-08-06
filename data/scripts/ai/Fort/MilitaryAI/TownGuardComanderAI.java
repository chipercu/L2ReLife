package scripts.ai.Fort.MilitaryAI;
//package ai.Fort.MilitaryAI;

import l2open.common.RunnableImpl;
import l2open.common.ThreadPoolManager;
import l2open.config.ConfigValue;
import l2open.database.mysql;
import l2open.extensions.network.MMOConnection;
import l2open.gameserver.ai.L2PlayerAI;
import l2open.gameserver.model.L2Object;
import l2open.gameserver.model.L2ObjectsStorage;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.base.UnitType;
import l2open.gameserver.model.entity.residence.Fortress;
import l2open.gameserver.network.L2GameClient;
import l2open.gameserver.serverpackets.Say2;
import l2open.util.GArray;
import l2open.util.Location;
import l2open.util.geometry.Vector.Point2D;
import l2open.util.geometry.Vector.PointNSWE2P;
import l2open.util.geometry.Vector.Side;
import l2open.util.geometry.Vector.Vector2P;
import npc.model.Military.GeoUtil.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static l2open.gameserver.model.base.UnitType.KNIGHT;


public class TownGuardComanderAI extends L2PlayerAI {
    private List<L2Player> units = new ArrayList<>();

    private L2Object objectReference;
    private L2Player commander;
    private ScheduledFuture<?> ai;
    private ScheduledFuture<?> moveAi;

    private int currentPatrolPoint = 0;
    private Location[] patrolPoints = {
            new Location(81528, 144632, -3559),
            new Location(82440, 144568, -3559),
            new Location(83864, 144552, -3430),
            new Location(83880, 146056, -3430),
            new Location(83896, 147688, -3431),
            new Location(83048, 147704, -3495),
            new Location(81528, 147704, -3495),
            new Location(81544, 146056, -3559)
    };

    private Fortress fortress;
    private List<LocForUnit> unitLocation = new ArrayList<>();
    private LocForUnit loc1;
    private LocForUnit loc2;
    private LocForUnit loc3;
    private LocForUnit loc4;

    private L2Player unit1;
    private L2Player unit2;
    private L2Player unit3;
    private L2Player unit4;
    private boolean unitsIsInPosition = false;


    private Location refLocation;


    public TownGuardComanderAI(L2Player actor) {
        super(actor);
        spawnUnit();
//        moveAi = ThreadPoolManager.getInstance().scheduleAtFixedRate(new MoveTask(), 500, 500);

    }


    public class MoveTask extends RunnableImpl {

        private boolean isInRange(int range){
           return  !unit1.isInRange(loc1.loc, range)
                    || !unit2.isInRange(loc2.loc, range)
                    || !unit3.isInRange(loc3.loc, range)
                    || !unit4.isInRange(loc4.loc, range);
        }
        private boolean isInMove(){
            return unit1.isMoving || unit2.isMoving || unit3.isMoving || unit4.isMoving;
        }

        @Override
        public void runImpl() throws Exception {

            if (getActor() == null){
                moveAi.cancel(true);
            }

            if (!getActor().isInCombat()) {
//                refLocation = patrolPoints[currentPatrolPoint];
                if (refLocation != null) {
                    findPosition(getActor(), refLocation, FormationType.PATROL);
//                    unitsMove();
                }

                int range = 70;
                if (isInRange(range)) {
                    ThreadPoolManager.getInstance().schedule(() -> {
                        if (isInRange(range)) {
                            getActor().stopMove();
                        }
                    }, 1000);
//                    getActor().stopMove();
                    unitsMove(false);
                    if (!isInMove()){
                        say("reform");
                        findPosition(getActor(), getActor().getLoc(), FormationType.RANDOM);
                        unitsMove(true);
                    }
                    return;
                }


                if (getActor().isInRange(refLocation, 20) && !unitsIsInPosition) {

                    currentPatrolPoint++;
                    say(String.valueOf(currentPatrolPoint));
                    if (currentPatrolPoint >= patrolPoints.length) {
                        currentPatrolPoint = 0;
                    }
                    unitsIsInPosition = true;
                }
                if (getActor().getDistance(patrolPoints[currentPatrolPoint]) > 150) {
                    unitsIsInPosition = false;
                }
                refLocation = patrolPoints[currentPatrolPoint];
                move(patrolPoints[currentPatrolPoint]);
                unitsMove(false);
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
                    refLocation = patrolPoints[currentPatrolPoint];
                    findPosition(getActor(), refLocation, FormationType.PATROL);
//                    toPosition(getActor(), getActor().getLoc() , FormationType.PATROL, false);
                    unitsMove(false);
                    moveAi = ThreadPoolManager.getInstance().scheduleAtFixedRate(new MoveTask(), 1000, 500);

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

    private void unitsMove(boolean running) {
        if (running) {
            getActor().setRunning();
            unit1.setRunning();
            unit2.setRunning();
            unit3.setRunning();
            unit4.setRunning();
        } else {
            getActor().setWalking();
            unit1.setWalking();
            unit2.setWalking();
            unit3.setWalking();
            unit4.setWalking();
        }
        units.get(0).moveToLocation(loc1.loc.x, loc1.loc.y, loc1.loc.z, 0, true);
        units.get(1).moveToLocation(loc2.loc.x, loc2.loc.y, loc2.loc.z, 0, true);
        units.get(2).moveToLocation(loc3.loc.x, loc3.loc.y, loc3.loc.z, 0, true);
        units.get(3).moveToLocation(loc4.loc.x, loc4.loc.y, loc4.loc.z, 0, true);
//            if (units.get(0).getDistance(getActor()) > 70) {
//                units.get(0).moveToLocation(loc1.loc, 0, true);
//            }
//            if (units.get(0).getDistance(getActor()) > 70) {
//                units.get(1).moveToLocation(loc2.loc, 0, true);
//            }
//            if (units.get(0).getDistance(getActor()) > 100) {
//                units.get(2).moveToLocation(loc3.loc, 0, true);
//            }
//            if (units.get(0).getDistance(getActor()) > 100) {
//                units.get(3).moveToLocation(loc4.loc, 0, true);
//            }

//        }
    }

    private void unitsStopMove() {
        for (L2Player unit : units) {
            unit.stopMove();
        }
    }

    private void findPosition(L2Object actor, Location refLocation, FormationType type) {

        if (type == FormationType.PATROL) {
            findPatrolLoc(actor, refLocation);
        } else if (type == FormationType.INSTUCTAJ) {
            findInstructLoc(getActor(), refLocation);
        } else if (type == FormationType.RANDOM) {
            findRandomLoc(actor, refLocation);
        }
    }

    private void findRandomLoc(L2Object actor, Location refLocation) {
        loc1 = new LocForUnit(Location.coordsRandomize(refLocation, 50, 150), knight);
        loc2 = new LocForUnit(Location.coordsRandomize(refLocation, 50, 150), knight);
        loc3 = new LocForUnit(Location.coordsRandomize(refLocation, 50, 150), knight);
        loc4 = new LocForUnit(Location.coordsRandomize(refLocation, 50, 150), knight);
    }


    static class LocForUnit {
        private Location loc;
        private UnitType locForType;
        private boolean isOccupied;

        public LocForUnit(Vector2P vector, int height, UnitType locForType) {
            this.loc = new Location((int) vector.getX(), (int) vector.getY(), height);
            this.locForType = locForType;
            this.isOccupied = false;
        }

        public LocForUnit(Location coordsRandomize, UnitType knight) {
            this.loc =coordsRandomize;
            this.locForType = knight;
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

    private void findPatrolLoc(L2Object actor, Location ref) {
        clearUnitsLoc();
        int unitDist = 50;
        int unitWidth = 30;
        Point2D actorPoint = new Point2D(actor.getX(), actor.getY());
        Point2D refPoint = new Point2D(ref.x, ref.y);

        //расчет NSWE координат
        PointNSWE2P backPoint1 = new PointNSWE2P(actorPoint, refPoint, unitDist, Side.BACK);
        PointNSWE2P backPoint2 = new PointNSWE2P(actorPoint, refPoint, unitDist + unitWidth, Side.BACK);
        PointNSWE2P left = new PointNSWE2P(actorPoint, refPoint, unitWidth, Side.LEFT);
        PointNSWE2P right = new PointNSWE2P(actorPoint, refPoint, unitWidth, Side.RIGHT);

        //создание 3д точки
        loc1 = new LocForUnit(new Vector2P(actorPoint, backPoint1.getPoint2D(), left.getPoint2D()), actor.getZ(), KNIGHT);
        loc2 = new LocForUnit(new Vector2P(actorPoint, backPoint1.getPoint2D(), right.getPoint2D()), actor.getZ(), KNIGHT);
        loc3 = new LocForUnit(new Vector2P(actorPoint, backPoint2.getPoint2D(), left.getPoint2D()), actor.getZ(), KNIGHT);
        loc4 = new LocForUnit(new Vector2P(actorPoint, backPoint2.getPoint2D(), right.getPoint2D()), actor.getZ(), KNIGHT);
    }

    private void findInstructLoc(L2Object actor, Location ref) {
        int unitDist = 70;
        int unitWidth = 40;
        clearUnitsLoc();
        Point2D actorPoint = new Point2D(actor.getX(), actor.getY());
        Point2D refPoint = new Point2D(ref.x, ref.y);

        //расчет NSWE координат
        PointNSWE2P backPoint1 = new PointNSWE2P(actorPoint, refPoint, unitDist, Side.BACK);
        PointNSWE2P backPoint2 = new PointNSWE2P(actorPoint, refPoint, unitDist + unitWidth, Side.BACK);
        PointNSWE2P left = new PointNSWE2P(actorPoint, refPoint, unitWidth, Side.LEFT);
        PointNSWE2P right = new PointNSWE2P(actorPoint, refPoint, unitWidth, Side.RIGHT);

        //расчет векторов
        Vector2P v1 = new Vector2P(actorPoint, backPoint1.getPoint2D(), left.getPoint2D());
        Vector2P v2 = new Vector2P(actorPoint, backPoint1.getPoint2D(), right.getPoint2D());
        Vector2P v3 = new Vector2P(actorPoint, backPoint2.getPoint2D(), left.getPoint2D());
        Vector2P v4 = new Vector2P(actorPoint, backPoint2.getPoint2D(), right.getPoint2D());

        //создание 3д точки
        Location location1 = new Location((int) v1.getX(), (int) v1.getY(), actor.getZ());
        Location location2 = new Location((int) v2.getX(), (int) v2.getY(), actor.getZ());
        Location location3 = new Location((int) v3.getX(), (int) v3.getY(), actor.getZ());
        Location location4 = new Location((int) v4.getX(), (int) v4.getY(), actor.getZ());
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

    public double calculateAngleFrom(Point a, Point b) {
        double angleTarget = Math.toDegrees(Math.atan2(b.y - a.y, b.x - a.x));
        if (angleTarget < 0)
            angleTarget = 360 + angleTarget;
        return angleTarget;
    }


    public double getDistance(Point a, Point b) {
        return Math.sqrt(getXYDeltaSq(a, b));
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
            p.setUnitType(KNIGHT);

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
            if (unit1 == null) {
                unit1 = p;
            } else if (unit2 == null) {
                unit2 = p;
            } else if (unit3 == null) {
                unit3 = p;
            } else if (unit4 == null) {
                unit4 = p;
            }
            this.units.add(p);
        }
        say("spawned " + this.units.size());
    }


}
