package npc.model.Military;

import l2open.config.ConfigValue;
import l2open.database.mysql;
import l2open.extensions.network.MMOConnection;
import l2open.gameserver.instancemanager.PlayerManager;
import l2open.gameserver.model.*;
import l2open.gameserver.network.L2GameClient;
import l2open.gameserver.serverpackets.SkillList;
import l2open.gameserver.tables.SkillTable;
import l2open.util.GArray;
import l2open.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MilitaryManager {

    public static void createUnit(int _classId, byte _sex, String name) {
        Unit unit = (Unit) L2Player.create(_classId, _sex, "Military", name, (byte) Rnd.get(0, 2), (byte) Rnd.get(0, 2), (byte) Rnd.get(0, 2), 0, 78);
        if (unit != null) {
            unit.setVar("unit_type", "Sage_Unit");
            giveAllSkills(unit);
        }
    }

    public static void deleteUnit(int objId) throws Exception {
        if (objId == -1)
            return;
        L2Player old_player = L2ObjectsStorage.getPlayer(objId);
        if (old_player != null)
            old_player.logout(false, false, true, true);
        PlayerManager.deleteCharByObjId(objId);
    }

    public static Unit spawnUnit(L2Object spawner) {
        GArray<HashMap<String, Object>> list = mysql.getAll("SELECT `obj_id`, `value`, (SELECT `account_name` FROM `characters` WHERE `characters`.`obj_Id` = `character_variables`.`obj_id` LIMIT 1) AS `account_name` FROM `character_variables` WHERE name LIKE 'unit_type'");
        List<Unit> unitList = new ArrayList<>();
        final List<HashMap<String, Object>> units = list.stream().filter(e -> e.get("unit_type").equals("Sage_Unit")).collect(Collectors.toList());
        units.forEach(e -> {
            L2GameClient client = new L2GameClient(new MMOConnection<L2GameClient>(null), true);
            client.setCharSelection((Integer) e.get("obj_id"));
            L2Player p = client.loadCharFromDisk(0);
//                    if (p == null || p.isDead()) {
//                        continue;
//                    }
            if (p != null) {
                client.setLoginName(e.get("account_name") == null ? "Sage_Unit_" + p.getName() : (String) e.get("account_name"));
                p.setLoc(spawner.getLoc());
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
                unitList.add((Unit) p);
            }
        });

        return unitList.isEmpty() ? null : unitList.get(0);
    }


    public static void giveAllSkills(L2Player unit) {
        if (unit == null) {
            return;
        }
        int unLearnable = 0;
        GArray<L2SkillLearn> skills = unit.getAvailableSkills(unit.getClassId());
        while (skills.size() > unLearnable) {
            unLearnable = 0;
            for (L2SkillLearn s : skills) {
                L2Skill sk = SkillTable.getInstance().getInfo(s.id, s.skillLevel);
                if (sk == null || !sk.getCanLearn(unit.getClassId())) {
                    unLearnable++;
                    continue;
                }
                unit.addSkill(sk, true);
                s.deleteSkills(unit);
            }
            skills = unit.getAvailableSkills(unit.getClassId());
        }
        unit.updateStats();
        unit.sendUserInfo(true);
        unit.sendPacket(new SkillList(unit));
    }


    public static void command(Unit unit) {


    }


}
