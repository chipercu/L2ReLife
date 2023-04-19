package events.ExpForSkills;

import l2open.config.ConfigValue;
import l2open.extensions.multilang.CustomMessage;
import l2open.extensions.scripts.Functions;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.Announcements;
import l2open.gameserver.cache.Msg;
import l2open.gameserver.model.L2Object;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.L2Spawn;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.serverpackets.SkillList;
import l2open.gameserver.tables.SkillTable;
import l2open.gameserver.tables.SkillTreeTable;
import l2open.util.Files;
import l2open.util.GArray;
import l2open.util.Util;

import java.util.Collection;

// Эвент ExpForSkills
public class ExpForSkills extends Functions implements ScriptFile
{
    private static final String event_name = "ExpForSkills";
    private static final int EMPTY_ORB_PRICE = 50000; // 50.000 adena at x1 servers
    private static final int FULL_ORB_PRICE = 50000; // 50.000 adena at x1 servers
    private static final int SKILL_PRICE = 50000; // 50.000 adena at x1 servers

    private static final int COFFER_ID = 8659;
    private static final int EVENT_MANAGER_ID = 36669;
    private static GArray<L2Spawn> _spawns = new GArray<L2Spawn>();

    private static boolean _active = false;

    /**
     * Спавнит эвент менеджеров
     */
    private void spawnEventManagers()
    {
        final int[][] EVENT_MANAGERS = {
                { -14823, 123567, -3143, 8192 }, // Gludio
                { -83159, 150914, -3155, 49152 }, // Gludin
                { 18600, 145971, -3095, 40960 }, // Dion
                { 82158, 148609, -3493, 60 }, // Giran
                { 110992, 218753, -3568, 0 }, // Hiene
                { 116339, 75424, -2738, 0 }, // Hunter Village
                { 81140, 55218, -1551, 32768 }, // Oren
                { 147148, 27401, -2231, 2300 }, // Aden
                { 43532, -46807, -823, 31471 }, // Rune
                { 87765, -141947, -1367, 6500 }, // Schuttgart
                { 147154, -55527, -2807, 61300 } // Goddard
        };

        SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
    }

    /**
     * Удаляет спавн эвент менеджеров
     */
    private void unSpawnEventManagers()
    {
        deSpawnNPCs(_spawns);
    }

    /**
     * Читает статус эвента из базы.
     * @return
     */
    private static boolean isActive()
    {
        return IsActive(event_name);
    }

    /**
     * Запускает эвент
     */
    public void startEvent()
    {
        L2Player player = (L2Player) getSelf();
        if(!player.getPlayerAccess().IsEventGm)
            return;

        if(SetActive(event_name, true))
        {
            spawnEventManagers();
            _log.info("Event: " + event_name + " started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events."+event_name+".AnnounceEventStarted", null);
        }
        else
            player.sendMessage("Event '"+ event_name +"' already started.");

        _active = true;
        show(Files.read("data/html/admin/events.htm", player), player);
    }

    /**
     * Останавливает эвент
     */
    public void stopEvent()
    {
        L2Player player = (L2Player) getSelf();
        if(!player.getPlayerAccess().IsEventGm)
            return;
        if(SetActive(event_name, false))
        {
            unSpawnEventManagers();
            _log.info("Event: " + event_name + " stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events."+event_name+".AnnounceEventStoped", null);
        }
        else
            player.sendMessage("Event '"+event_name+"' not started.");

        _active = false;
        show(Files.read("data/html/admin/events.htm", player), player);
    }

    /**
     * Продает 1 сундук игроку
     */
    public void buycoffer(String[] var)
    {
        L2Player player = (L2Player) getSelf();

        if(!player.isQuestContinuationPossible(true))
            return;

        if(!L2NpcInstance.canBypassCheck(player, player.getLastNpc()))
            return;

        int coffer_count = 1;
        try
        {
            coffer_count = Integer.valueOf(var[0]);
        }
        catch(Exception E)
        {}

        long need_adena = (long) (EMPTY_ORB_PRICE * ConfigValue.CofferOfShadowsPriceRate * coffer_count);
        if(player.getAdena() < need_adena)
        {
            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }

        player.reduceAdena(need_adena, true);
        Functions.addItem(player, COFFER_ID, coffer_count);
    }

    /**
     * Продает 1 сундук игроку
     */
    public void getSkill(String[] var)
    {
        L2Player player = (L2Player) getSelf();

        if(!player.isQuestContinuationPossible(true))
            return;

        if(!L2NpcInstance.canBypassCheck(player, player.getLastNpc()))
            return;

        int skill_ID = 0;
        try
        {
            skill_ID = Integer.parseInt(var[0]);
        }
        catch(Exception E)
        {}

        addSkill(player, skill_ID);
    }



    private void addSkill(L2Player activeChar, int skill_id)
    {

        final L2Skill knownSkill = activeChar.getKnownSkill(skill_id);

        if (activeChar.getAdena() < EMPTY_ORB_PRICE){
            activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }

        if (knownSkill != null){
            final short skillLevel = knownSkill.getLevel();
            if (skillLevel > 4) {
                activeChar.sendMessage("Это умение улучшено до максимум.");
                return;
            }
            if (activeChar.getAdena() < SKILL_PRICE * skillLevel){
                activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }
            L2Skill updateSkill = SkillTable.getInstance().getInfo(skill_id, skillLevel + 1);
            activeChar.sendMessage("Вы успешно повысили умение " + updateSkill.getName() + " до "+updateSkill.getLevel() +" уровня");
            activeChar.reduceAdena(EMPTY_ORB_PRICE * (skillLevel + 1), true);
            activeChar.addSkill(updateSkill, true);
            //            activeChar.sendMessage("You gave the skill " + updateSkill.getName() + " to " + player.getName() + ".");
        }else {
            if (activeChar.getAdena() < SKILL_PRICE){
                activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }
            L2Skill updateSkill = SkillTable.getInstance().getInfo(skill_id, 1);
            activeChar.sendMessage("Вы получили умение " + updateSkill.getName() + " -  "+updateSkill.getLevel() +" уровня");
            activeChar.reduceAdena(EMPTY_ORB_PRICE, true);
            activeChar.addSkill(updateSkill, true);
        }
        activeChar.sendPacket(new SkillList(activeChar));
        activeChar.updateStats();
        activeChar.sendUserInfo(true);
    }

    /**
     * Добавляет в диалоги эвент менеджеров строчку с байпасом для покупки сундука
     */
    private static int[] buycoffer_counts = { 1, 5, 10, 50 }; //TODO в конфиг

    public String DialogAppend_36669(Integer val)
    {
        if(val != 0)
            return "";

        String price;
        String append = "";
        StringBuilder s = new StringBuilder();
        s.append("<button ALIGN=LEFT action=\"bypass -h scripts_events."+event_name+"."+event_name+":buycoffer " + 5 +"\" value=\"Заполнить сферу боевым опытом\" width=250 height=31 back=\"New_Buttons.Btn_Back\" fore=\"New_Buttons.Btn_Font\">");

        s.append("<table>");
        s.append("<tr>");
        s.append("<td>");
        s.append("<button ALIGN=LEFT action=\"bypass -h scripts_events."+event_name+"."+event_name+":getSkill " + 26078 +"\" value=\"получить умение MP\" width=64 height=64 back=\"relife.bust_mp\" fore=\"relife.bust_mp\">");
        s.append("</td>");
        s.append("<td>");
        s.append("<button ALIGN=LEFT action=\"bypass -h scripts_events."+event_name+"."+event_name+":getSkill " + 26079 +"\" value=\"получить умение HP\" width=64 height=64 back=\"relife.bust_hp\" fore=\"relife.bust_hp\">");
        s.append("</td>");
        s.append("</tr>");
        s.append("<tr>");
        s.append("<td>");
        s.append("<button ALIGN=LEFT action=\"bypass -h scripts_events."+event_name+"."+event_name+":getSkill " + 26080 +"\" value=\"получить умение CP\" width=64 height=64 back=\"relife.bust_cp\" fore=\"relife.bust_cp\">");
        s.append("</td>");
        s.append("<td>");
        s.append("<button ALIGN=LEFT action=\"bypass -h scripts_events."+event_name+"."+event_name+":getSkill " + 26081 +"\" value=\"получить умение Скорость Физ Атаки\" width=64 height=64 back=\"relife.speed_attack\" fore=\"relife.speed_attack\">");
        s.append("</td>");
        s.append("</tr>");
        s.append("<tr>");
        s.append("<td>");
        s.append("<button ALIGN=LEFT action=\"bypass -h scripts_events."+event_name+"."+event_name+":getSkill " + 26082 +"\" value=\"получить умение Скорость Маг Атаки\" width=64 height=64 back=\"relife.speed_cast\" fore=\"relife.speed_cast\">");
        s.append("</td>");
        s.append("<td>");
        s.append("<button ALIGN=LEFT action=\"bypass -h scripts_events."+event_name+"."+event_name+":getSkill " + 26083 +"\" value=\"получить умение Скорость Бега\" width=64 height=64 back=\"relife.speed_move\" fore=\"relife.speed_move\">");
        s.append("</td>");
        s.append("</tr>");
        s.append("<tr>");
        s.append("<td>");
        s.append("<button ALIGN=LEFT action=\"bypass -h scripts_events."+event_name+"."+event_name+":getSkill " + 26084 +"\" value=\"получить умение Точность\" width=64 height=64 back=\"relife.accurancy\" fore=\"relife.accurancy\">");
        s.append("</td>");
        s.append("<td>");
        s.append("<button ALIGN=LEFT action=\"bypass -h scripts_events."+event_name+"."+event_name+":getSkill " + 26085 +"\" value=\"получить умение Уклонение\" width=64 height=64 back=\"relife.evassion\" fore=\"relife.evassion\">");
        s.append("</td>");
        s.append("</tr>");
        s.append("</table>");
        return s.toString();
    }

    public void onLoad()
    {
        if(isActive())
        {
            _active = true;
            spawnEventManagers();
            _log.info("Loaded Event: "+event_name+" [state: activated]");
        }
        else
            _log.info("Loaded Event: "+event_name +" [state: deactivated]");
    }

    public void onReload()
    {
        unSpawnEventManagers();
    }

    public void onShutdown()
    {
        unSpawnEventManagers();
    }

    public static void OnPlayerEnter(L2Player player)
    {
        if(_active)
            Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events."+event_name+".AnnounceEventStarted", null);
    }
}
