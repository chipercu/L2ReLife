package ai.Fort.MilitaryAI;

import l2open.common.RunnableImpl;
import l2open.config.ConfigValue;
import l2open.database.mysql;
import l2open.extensions.network.MMOConnection;
import l2open.gameserver.ai.L2PlayerAI;
import l2open.gameserver.instancemanager.FortressManager;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Object;
import l2open.gameserver.model.L2ObjectsStorage;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.base.UnitType;
import l2open.gameserver.model.entity.residence.Fortress;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.network.L2GameClient;
import l2open.gameserver.serverpackets.Say2;
import l2open.util.GArray;
import l2open.util.Location;
import npc.model.Military.GeoUtil.Vector2DRef;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static l2open.gameserver.model.base.UnitType.*;


public class TankCommanderAI extends L2PlayerAI {
    private List<L2Player> units = new ArrayList<>();

    private L2Object objectReference;
    private L2Player commander;
    private ScheduledFuture<?> ai;
    private Fortress fortress;
    private List<LocForUnit> unitLocation;


    private UnitType[] patrolPos = {
            knight, knight, knight,
            healer, knight, buffer,
            knight, knight, knight
    };

    public TankCommanderAI(L2Player actor) {
        super(actor);

        fortress = FortressManager.getInstance().getFortressByName(getActor().getVar("fortressName"));
        if (fortress.getOwner() != null){
            commander = fortress.getOwner().getLeader().getPlayer();
            findPatrolLoc(getActor(), getActor());
        }


    }

    enum FormationType{
        INSTUCTAJ,
        DEFENCE,
        TRAINING,
        PATROL,
        RANDOM
    }

    private void clearUnitsLoc(){
        unitLocation.clear();
    }

    private void move(Location location){
        clearUnitsLoc();
        getActor().moveToLocation(location, 0, true);
    }
    private Location getUnitPositionByType(UnitType type){
        for (LocForUnit loc: unitLocation){
            if (loc.getLocForType() == type && !loc.isOccupied()){
                loc.setOccupied(true);
                return loc.loc;
            }
        }
        return Location.coordsRandomize(getActor().getLoc(), 200, 300);
    }

    private void unitsMove(){
        for (L2Player unit: units){
            if (unit.isDead() || unit.isMovementDisabled()){
                return;
            }
            unit.moveToLocation(getUnitPositionByType(unit.getUnitType()), 0, false);
        }
    }

    private void toPosition(FormationType type, boolean running){
        if (running){
            getActor().setRunning();
            units.forEach(L2Character::setRunning);
        }else {
            getActor().setWalking();
            units.forEach(L2Character::setWalking);
        }

        if (Objects.requireNonNull(type) == FormationType.DEFENCE) {
            findFalangLoc(getActor(), getActor(), true);
            unitsMove();
        } else if (type == FormationType.PATROL) {
            findPatrolLoc(getActor(), getActor());
            unitsMove();
        } else if (type == FormationType.INSTUCTAJ) {
            findInstructLoc(getActor(), getActor());
            unitsMove();
        } else if (type == FormationType.RANDOM) {

        }
    }


    static class LocForUnit{
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


    private void findFalangLoc(L2Object actor, L2Object ref, boolean front){
        int unitDist = 200;
        int unitWidth = 50;
        clearUnitsLoc();
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth, false), healer));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth, false), buffer));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth * 2, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth * 2, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth * 3, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth * 3, true), knight));
    }
    private void findPatrolLoc(L2Object actor, L2Object ref){
        int unitDist = 100;
        int unitWidth = 50;
        clearUnitsLoc();
        unitLocation.add(new LocForUnit(Vector2DRef.getCenterPoint(actor, ref, unitDist, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, 1, unitWidth, true), buffer));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontRightPoint(actor, ref, 1, unitWidth, true), healer));
        unitLocation.add(new LocForUnit(Vector2DRef.getCenterPoint(actor, ref, unitDist, false), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth, false), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth, false), knight));
    }
    private void findInstructLoc(L2Object actor, L2Object ref){
        int unitDist = 200;
        int unitWidth = 50;
        clearUnitsLoc();
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth * 4, true), healer));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth * 5, true), buffer));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth * 2, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth * 2, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth * 3, true), knight));
        unitLocation.add(new LocForUnit(Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth * 3, true), knight));
    }


    public List<L2Player> getUnits() {
        return units;
    }

    class AI extends RunnableImpl {

        public AI(){}

        @Override
        public void runImpl() throws Exception {

            if (getUnits().isEmpty()){
//                unitSay("Spawn My Units");
                spawnUnit(fortress);
            }
//            findUnitPoints(objectReference, true);
        }
    }

    public void say(String text){
        Say2 cs = new Say2(getActor().getObjectId(), 16, getActor().getName(), text);
        for(L2Player player : L2ObjectsStorage.getPlayers()){
           // if (getActor().getFortress().getId() == commander.getFortress().getId()){
                player.sendPacket(cs);
            //}
        }
    }

    private void spawnUnit(Fortress fortress){

        int fortressID = fortress.getId();

        GArray<HashMap<String, Object>> list = mysql.getAll("SELECT `obj_id`, `value`, (SELECT `account_name` FROM `characters` WHERE `characters`.`obj_Id` = `character_variables`.`obj_id` LIMIT 1) AS `account_name` FROM `character_variables` WHERE name LIKE 'fortressUnit'");

        int i = 0;
        for (HashMap<String, Object> e : list) {
            if (getActor().getObjectId() == (Integer) e.get("obj_id")){
                continue;
            }
            L2GameClient client = new L2GameClient(new MMOConnection<L2GameClient>(null), true);
            client.setCharSelection((Integer) e.get("obj_id"));
            L2Player p = client.loadCharFromDisk(0);
            if (p == null || p.isDead())
                continue;
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
            i++;
            if (i > 5){
                break;
            }
        }
        say("spawned " + this.units.size());
    }


}
