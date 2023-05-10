package ai.Fort.AI2;

import l2open.common.ThreadPoolManager;
import l2open.config.ConfigValue;
import l2open.database.mysql;
import l2open.extensions.network.MMOConnection;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.network.L2GameClient;
import l2open.util.GArray;
import l2open.util.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static l2open.gameserver.model.base.UnitType.KNIGHT;

public class CommanderAI extends CommonAI implements Commander {

    private final List<L2Player> units = new ArrayList<>();
    private final List<Unit> unitList = new ArrayList<>();



    public CommanderAI(L2Player actor) {
        super(actor);
        if (getActor() != null) {
            stateType = StateType.STAY;
            spawnUnits();
            setMaster(actor);
            actor.setUnitsCount(unitList.size());
            aiTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AiTask(), 250, 250);
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
                case "start_patrol": {
                    stateType = StateType.MOVE_PATROL;
                    say("start_patrol");
                    break;
                }
                case "two_column": {
                    notifyMove(FormationType.TWO_COLUMN, getActor().getLoc(), player.getLoc());
                    break;
                }
                case "column": {
                    notifyMove(FormationType.COLUMN, getActor().getLoc(), player.getLoc());
                    break;
                }
                case "back_falang": {
                    notifyMove(FormationType.BACK_FALANG, getActor().getLoc(), player.getLoc());
                    break;
                }
                case "front_falang": {
                    notifyMove(FormationType.FRONT_FALANG, getActor().getLoc(), player.getLoc());
                    break;
                }
                case "stopMove": {
                    stateType = StateType.STAY;
                    say("stop_patrol");
                    break;
                }
            }
        }
    }

    @Override
    public void addUnit(Unit observer) {
        this.unitList.add(observer);
    }

    @Override
    public void removeUnit(Unit observer) {
        this.unitList.remove(observer);
    }

    @Override
    public void notifyMove(FormationType formationType, Location ref, Location direction) {
        this.unitList.forEach(u -> u.move(formationType, ref, direction));
    }

    @Override
    public void notifyAttack(L2Character target) {
        this.unitList.forEach(unit -> unit.attack(target, false));
    }

    @Override
    public void notifyCastSkill(L2Character target) {
        this.unitList.forEach(unit -> unit.castSkill(target, false));
    }

    @Override
    public void setMaster(L2Player master) {
        this.unitList.forEach(unit -> unit.setMaster(master));
    }


    private class AiTask implements Runnable {
        @Override
        public void run() {
            if (maybeCancelAI()) {
                return;
            }
            switch (stateType) {
                case MOVE_PATROL: {
                    notifyMove(FormationType.TWO_COLUMN, getActor().getLoc(), getActor().getLoc());
                    break;
                }
                case STAY: {
                    notifyMove(FormationType.FRONT_FALANG, getActor().getLoc(), getActor().getLoc());
                    break;
                }
                case MOVE_TO_BATTLE_POINT: {
                    notifyMove(FormationType.COLUMN, getActor().getLoc(), getActor().getLoc());
                    break;
                }
                case ATTACK: {
                    L2Character target = (L2Character) getActor().getTarget();
                    if (target != null) {
                        notifyAttack(target);
                    }
                }
            }


        }
    }
}
