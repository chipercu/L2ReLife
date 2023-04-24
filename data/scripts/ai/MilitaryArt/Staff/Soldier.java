package scripts.ai.MilitaryArt.Staff;

import ai.MilitaryArt.BattleOrderType;
import ai.MilitaryArt.MilitaryRank;
import l2open.common.RunnableImpl;
import l2open.common.ThreadPoolManager;
import l2open.config.ConfigValue;
import l2open.database.mysql;
import l2open.extensions.network.MMOConnection;
import l2open.extensions.scripts.Functions;
import l2open.gameserver.ai.L2PlayerAI;
import l2open.gameserver.clientpackets.Say2C;
import l2open.gameserver.model.*;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.items.L2ItemInstance;
import l2open.gameserver.network.L2GameClient;
import l2open.gameserver.serverpackets.Say2;
import l2open.gameserver.tables.SkillTable;
import l2open.gameserver.xml.ItemTemplates;
import l2open.util.GArray;
import l2open.util.Location;
import l2open.util.Rnd;
import npc.model.Military.GeoUtil.Point;
import npc.model.Military.GeoUtil.Vector2DRef;
import npc.model.Military.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class Soldier extends L2PlayerAI {


    private MilitaryRank rank;

    private static final int unitDist = 200;
    private static final int unitWidth = 100;

    private List<L2Player> units = new ArrayList<>();

    private List<Location> unitPosition = new ArrayList<>();
    private L2Object objectReference;
    private L2Player commander;
    private BattleOrderType battleOrderType;
    private ScheduledFuture<?> ai;


    public Soldier(L2Character actor) {
        super((L2Player) actor);
        unitSay("initAI");




    }

    @Override
    public void unitChatListener(L2Player player, int _chatType, String command) {

        final L2Player actor = getActor();
        if (command != null && !command.isEmpty()){
            switch (command){
                case "startAI" : {
                    ai = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AI(), 1000, 1000);
                    break;
                }
                case "stopAI" : {
                    if (ai != null){
                        ai.cancel(true);
                        ai = null;
                        units.clear();
                        unitSay("stopAI");
                    }
                    break;
                }
                case "clear": {
                        for (L2Player unit: units){
                            unit.deleteMe();
                        }
                        units.clear();
                        unitSay(units.size() + "");
                    break;
                }
                case "spawn" : {
                    spawnUnit();
                    break;
                }
                case "gotomy": {


                    Location partyFollowLoc = Location.coordsRandomize(player.getLoc(), 75, 125);
                    //getActor().getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, master, 100);
                    unitSay("follow");
                    actor.moveToLocation(partyFollowLoc, 50, false);
                    objectReference = player.getTarget();

//                    getActor().followToCharacter_v2(partyFollowLoc, player, 100, false);
                    break;
                }
                case "falang": {

                    final List<Location> falangLoc = findFalangLoc(actor, player.getTarget(), false);

                    for (int i = 0; i < units.size(); i++) {
                        final L2Player p = units.get(i);
//                        ThreadPoolManager.getInstance().schedule(() -> {
//                            L2Skill skill = SkillTable.getInstance().getInfo(88, 3);
//                            p.getAI().Cast(skill, p, false, true);
//                        }, 5000);
                        p.startAttackStanceTask();
                        p.moveToLocation(falangLoc.get(i), 0, false);
                        p.setHeading(player.getHeading());
                    }

                    for (Location loc: falangLoc){
                        L2ItemInstance item = ItemTemplates.getInstance().createItem(57);
                        item.setCount(1);
                        item.dropMe(actor, loc);
                    }


//                    if (!getUnits().isEmpty()){
//                        final List<L2Player> activeUnits = getActiveUnits();
//                        for (L2Player unit: activeUnits){
//                            for (int i = 0; i < activeUnits.size(); i++) {
//                                unit.moveToLocation(getUnitPosition().get(i), 0, false);
//                            }
//                        }
//                    }
                    break;
                }

            }


        }

        super.unitChatListener(player, _chatType, command);
    }

    private List<Location> findFalangLoc(L2Player actor,L2Object ref, boolean front){
        List<Location> new_loc = new ArrayList<>();
        int unitDist = 200;
        int unitWidth = 50;
        Point p1 = Vector2DRef.getCenterPoint(actor, ref, unitDist, front);
        Point p2left = Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth, front);
        Point p3right = Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth, front);
        Point p4left = Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth * 2, front);
        Point p5right = Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth * 2, front);
        new_loc.add(new Location(p1.x, p1.y, actor.getZ()));
        new_loc.add(new Location(p2left.x, p2left.y, actor.getZ()));
        new_loc.add(new Location(p3right.x, p3right.y, actor.getZ()));
        new_loc.add(new Location(p4left.x, p4left.y, actor.getZ()));
        new_loc.add(new Location(p5right.x, p5right.y, actor.getZ()));
        return new_loc;
    }


    private List<L2Player> getActiveUnits() {
        return this.units.stream()
                .filter(u -> !u.isDead())
                .filter(u -> !u.isMovementDisabled())
                .filter(u -> !u.isSleeping())
                .collect(Collectors.toList());
    }


    private void findUnitPoints(L2Object ref, boolean front) {

        L2Player actor = getActor();

        int activeUnits = getActiveUnits().size();
        if (activeUnits == 0) {
            return;
        }
        List<Location> new_loc = new ArrayList<>();
        if (activeUnits == 1) {
            Point frontPoint = Vector2DRef.getCenterPoint(actor, ref, unitDist, front);
            new_loc.add(new Location(frontPoint.x, frontPoint.y, actor.getZ()));
        } else if (activeUnits == 2) {
            Point p1 = Vector2DRef.getCenterPoint(actor, ref, unitDist, front);
            Point p2 = Rnd.nextBoolean() ? Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth, front) : Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth, front);
            new_loc.add(new Location(p1.x, p1.y, actor.getZ()));
            new_loc.add(new Location(p2.x, p2.y, actor.getZ()));
        } else if (activeUnits == 3) {
            Point p1 = Vector2DRef.getCenterPoint(actor, ref, unitDist, front);
            Point p2left = Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth, front);
            Point p3right = Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth, front);
            new_loc.add(new Location(p1.x, p1.y, actor.getZ()));
            new_loc.add(new Location(p2left.x, p2left.y, actor.getZ()));
            new_loc.add(new Location(p3right.x, p3right.y, actor.getZ()));
        }else if (activeUnits == 4){
            Point p1 = Vector2DRef.getCenterPoint(actor, ref, unitDist, front);
            Point p2left = Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth, front);
            Point p3right = Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth, front);
            Point p4 = Rnd.nextBoolean() ? Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth*2, front) : Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth*2, front);
            new_loc.add(new Location(p1.x, p1.y, actor.getZ()));
            new_loc.add(new Location(p2left.x, p2left.y, actor.getZ()));
            new_loc.add(new Location(p3right.x, p3right.y, actor.getZ()));
            new_loc.add(new Location(p4.x, p4.y, actor.getZ()));
        } else if (activeUnits == 5) {
            Point p1 = Vector2DRef.getCenterPoint(actor, ref, unitDist, front);
            Point p2left = Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth, front);
            Point p3right = Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth, front);
            Point p4left = Vector2DRef.getFrontLeftPoint(actor, ref, unitDist, unitWidth*2, front);
            Point p5right = Vector2DRef.getFrontRightPoint(actor, ref, unitDist, unitWidth*2, front);
            new_loc.add(new Location(p1.x, p1.y, actor.getZ()));
            new_loc.add(new Location(p2left.x, p2left.y, actor.getZ()));
            new_loc.add(new Location(p3right.x, p3right.y, actor.getZ()));
            new_loc.add(new Location(p4left.x, p4left.y, actor.getZ()));
            new_loc.add(new Location(p5right.x, p5right.y, actor.getZ()));
        }
        if (!new_loc.isEmpty()) {
            getUnitPosition().clear();
            getUnitPosition().addAll(new_loc);
        }
    }

    public List<Location> getUnitPosition() {
        return this.unitPosition;
    }
    private int getUnitCount() {
        return units.size();
    }

    public List<L2Player> getUnits() {
        return units;
    }

    public void setUnits(List<L2Player> units) {
        this.units = units;
    }

    public void setUnitPosition(List<Location> unitPosition) {
        this.unitPosition = unitPosition;
    }

    public BattleOrderType getBattleOrderType() {
        return battleOrderType;
    }

    public void setBattleOrderType(BattleOrderType battleOrderType) {
        this.battleOrderType = battleOrderType;
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

    public void unitSay(String text){
        Say2 cs = new Say2(getActor().getObjectId(), 16, getActor().getName(), text);
        for(L2Player player : L2ObjectsStorage.getPlayers()){
            player.sendPacket(cs);
        }

    }
   class AI extends RunnableImpl {

        public AI(){}

        @Override
        public void runImpl() throws Exception {

            if (getUnits().isEmpty()){
//                unitSay("Spawn My Units");
                spawnUnit();
            }
//            findUnitPoints(objectReference, true);
        }
    }


    private void spawnUnit(){
        GArray<HashMap<String, Object>> list = mysql.getAll("SELECT `obj_id`, `value`, (SELECT `account_name` FROM `characters` WHERE `characters`.`obj_Id` = `character_variables`.`obj_id` LIMIT 1) AS `account_name` FROM `character_variables` WHERE name LIKE 'bot1'");

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
            //p.restoreEffects();
            //p.restoreDisableSkills();
            p.broadcastUserInfo(true);
            this.units.add(p);
            i++;
            if (i > 5){
                break;
            }
        }
        unitSay("spawned " + this.units.size());
    }



    @Override
    protected void MY_DYING(L2Character killer) {
        super.MY_DYING(killer);
    }
}
