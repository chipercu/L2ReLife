package ai.Fort.AI2;

import l2open.common.ThreadPoolManager;
import l2open.config.ConfigValue;
import l2open.database.mysql;
import l2open.extensions.network.MMOConnection;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.UnitLoc;
import l2open.gameserver.network.L2GameClient;
import l2open.util.GArray;
import l2open.util.Location;
import l2open.util.geometry.Vector.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static l2open.gameserver.model.base.UnitType.*;

import static l2open.util.geometry.Vector.Side.*;

public class CommanderAI extends CommonAI {

    private final List<L2Player> units = new ArrayList<>();

    public CommanderAI(L2Player actor) {
        super(actor);
        if (getActor() != null){
            aiTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AiTask(), 250, 250);
            spawnUnits();
            changeLocFromUnitsLoc(getActor().getLoc(), FormationType.FRONT_FALANG);
            for (L2Player unit: units){
                UnitLoc unitLoc = getUnitLocs().stream()
                        .filter(loc -> loc.getUnitType() == unit.getUnitType())
                        .filter(loc -> !loc.isOccupied())
                        .min((o1, o2) -> o2.getPriority() - o1.getPriority())
                        .get();
                unit.setFormationLoc(unitLoc);
            }
        }
    }

    private void spawnUnits() {
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
            this.units.add(p);

        }
//        say("spawned " + this.units.size());

    }

    @Override
    public void unitChatListener(L2Player player, int _chatType, String command) {
        final L2Player actor = getActor();
        if (command != null && !command.isEmpty()) {
            switch (command) {
                case "startMove": {
                    say("start_patrol");
                    break;
                }
                case "stopMove": {
                    if (aiTask != null) {
                        say("stop_patrol");
                        getActor().stopMove();
                        for (L2Player unit: units){
                            unit.stopMove();
                        }
                        aiTask.cancel(true);
                        aiTask = null;
                    }
                }
            }
        }
    }

    private void changeLocFromUnitsLoc(Location ref, FormationType formationType){
        int dist = 50;
        switch (formationType){
            case BATTLE_BACK_FALANG:{
                battle_falang(ref, dist, BACK);
                getUnitLocs().get(6).setLoc(findNewLoc(ref, BACK, RIGHT, dist*2, dist));
                getUnitLocs().get(7).setLoc(findNewLoc(ref, BACK, LEFT, dist*2, dist));
                break;
            }
            case BATTLE_FRONT_FALANG:{
                battle_falang(ref, dist, FRONT);
                getUnitLocs().get(6).setLoc(findNewLoc(ref, BACK, RIGHT, dist, dist));
                getUnitLocs().get(7).setLoc(findNewLoc(ref, BACK, LEFT, dist, dist));
                break;
            }
            case COLUMN:{
                getUnitLocs().get(0).setLoc(findNewLoc(ref, BACK, RIGHT, dist*2, 0));
                getUnitLocs().get(1).setLoc(findNewLoc(ref, BACK, RIGHT, dist*3, 0));
                getUnitLocs().get(2).setLoc(findNewLoc(ref, BACK, RIGHT, dist*4, 0));
                getUnitLocs().get(3).setLoc(findNewLoc(ref, BACK, RIGHT, dist*5, 0));
                getUnitLocs().get(4).setLoc(findNewLoc(ref, BACK, RIGHT, dist*6, 0));
                getUnitLocs().get(5).setLoc(findNewLoc(ref, BACK, RIGHT, dist*7, 0));
                getUnitLocs().get(6).setLoc(findNewLoc(ref, BACK, RIGHT, dist*8, 0));
                getUnitLocs().get(7).setLoc(findNewLoc(ref, BACK, RIGHT, dist*9, 0));
                break;
            }
            case TWO_COLUMN:{
                getUnitLocs().get(0).setLoc(findNewLoc(ref, BACK, RIGHT, dist, 25));
                getUnitLocs().get(1).setLoc(findNewLoc(ref, BACK, LEFT, dist, 25));
                getUnitLocs().get(2).setLoc(findNewLoc(ref, BACK, RIGHT, dist*2, 25));
                getUnitLocs().get(3).setLoc(findNewLoc(ref, BACK, LEFT, dist*2, 25));
                getUnitLocs().get(4).setLoc(findNewLoc(ref, BACK, RIGHT, dist*3, 25));
                getUnitLocs().get(5).setLoc(findNewLoc(ref, BACK, LEFT, dist*3, 25));
                getUnitLocs().get(6).setLoc(findNewLoc(ref, BACK, RIGHT, dist*4, 25));
                getUnitLocs().get(7).setLoc(findNewLoc(ref, BACK, LEFT, dist*4, 25));
                break;
            }
            case FRONT_FALANG:{
                dist += 25;
                if (getUnitLocs().isEmpty()){
                    getUnitLocs().add(new UnitLoc(KNIGHT, 1, findNewLoc(ref, FRONT, RIGHT, dist, 175)));
                    getUnitLocs().add(new UnitLoc(KNIGHT, 2, findNewLoc(ref, FRONT, RIGHT, dist, 125)));
                    getUnitLocs().add(new UnitLoc(KNIGHT, 3, findNewLoc(ref, FRONT, RIGHT, dist, 75)));
                    getUnitLocs().add(new UnitLoc(KNIGHT, 4, findNewLoc(ref, FRONT, RIGHT, dist, 25)));
                    getUnitLocs().add(new UnitLoc(KNIGHT, 5, findNewLoc(ref, FRONT, LEFT, dist, 25)));
                    getUnitLocs().add(new UnitLoc(KNIGHT, 6, findNewLoc(ref, FRONT, LEFT, dist, 75)));
                    getUnitLocs().add(new UnitLoc(HEALER, 7, findNewLoc(ref, FRONT, LEFT, dist, 125)));
                    getUnitLocs().add(new UnitLoc(SUPORT, 8, findNewLoc(ref, FRONT, LEFT, dist, 175)));
                }else {
                    falang(ref, dist, FRONT);
                }
                break;
            }
            case BACK_FALANG:{
                falang(ref, dist, BACK);
                break;
            }
        }
    }

    private void battle_falang(Location ref, int dist, Side side) {
        getUnitLocs().get(0).setLoc(findNewLoc(ref, side, LEFT, dist, 125));
        getUnitLocs().get(1).setLoc(findNewLoc(ref, side, LEFT, dist, 75));
        getUnitLocs().get(2).setLoc(findNewLoc(ref, side, LEFT, dist, 25));
        getUnitLocs().get(3).setLoc(findNewLoc(ref, side, RIGHT, dist, 25));
        getUnitLocs().get(4).setLoc(findNewLoc(ref, side, RIGHT, dist, 75));
        getUnitLocs().get(5).setLoc(findNewLoc(ref, side, RIGHT, dist, 125));
    }

    private void falang(Location ref, int dist, Side side) {
        getUnitLocs().get(0).setLoc(findNewLoc(ref, side, RIGHT, dist, 175));
        getUnitLocs().get(1).setLoc(findNewLoc(ref, side, RIGHT, dist, 125));
        getUnitLocs().get(2).setLoc(findNewLoc(ref, side, RIGHT, dist, 75));
        getUnitLocs().get(3).setLoc(findNewLoc(ref, side, RIGHT, dist, 25));
        getUnitLocs().get(4).setLoc(findNewLoc(ref, side, LEFT, dist, 25));
        getUnitLocs().get(5).setLoc(findNewLoc(ref, side, LEFT, dist, 75));
        getUnitLocs().get(6).setLoc(findNewLoc(ref, side, LEFT, dist, 125));
        getUnitLocs().get(7).setLoc(findNewLoc(ref, side, LEFT, dist, 175));
    }


    private class AiTask implements Runnable {
        @Override
        public void run() {
            if (maybeCancelAI()){
                return;
            }

        }
    }
}
