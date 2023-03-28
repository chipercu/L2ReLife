package commands.admin;

import l2open.config.ConfigValue;
import l2open.database.DatabaseUtils;
import l2open.database.FiltredPreparedStatement;
import l2open.database.L2DatabaseFactory;
import l2open.database.ThreadConnection;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.cache.Msg;
import l2open.gameserver.handler.AdminCommandHandler;
import l2open.gameserver.handler.IAdminCommandHandler;
import l2open.gameserver.instancemanager.RaidBossSpawnManager;
import l2open.gameserver.model.L2Object;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Spawn;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.tables.NpcTable;
import l2open.gameserver.templates.L2NpcTemplate;
import l2open.util.Location;
import l2open.util.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminNpcReload implements IAdminCommandHandler, ScriptFile {

    private static enum Commands {
        admin_npc_reload,
        admin_npc_delete
    }


    @Override
    public boolean useAdminCommand(Enum anEnum, String[] strings, String s, L2Player l2Player) {
        Commands command = (Commands) anEnum;

        String npc_id;

        final Location loc = l2Player.getLoc();

        if (!l2Player.getPlayerAccess().CanBanChat) {
            return false;
        }

        switch (command) {
            case admin_npc_reload:
                L2Object obj = l2Player.getTarget();
                if (obj != null && obj.isNpc()) {
                    L2NpcInstance target = (L2NpcInstance) obj;
                    npc_id = target.getNpcId() + "";
                    L2Spawn spawn = target.getSpawn();
                    if (spawn != null)
                        spawn.stopRespawn();
                    if (ConfigValue.saveAdminDeSpawn)
                        deleteSpawn(spawn);
                    target.deleteMe();
                    spawnMonster(l2Player, npc_id, 10, 1, loc);
                    l2Player.sendMessage("NPC - " + npc_id + " is reloaded");
                } else{
                    l2Player.sendPacket(Msg.INVALID_TARGET);
                }
                break;
            case admin_npc_delete:
                for (L2NpcInstance npc: l2Player.getAroundNpc(300, 100)){
                    npc.deleteMe();
                }
        }
        return true;
    }


    public void deleteSpawn(L2Spawn spawn) {
        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        try {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM `spawnlist` WHERE `npc_templateid`=? AND `locx`=? AND `locy`=? AND `locz`=? AND `loc_id`=?");
            statement.setInt(1, spawn.getNpcId());
            statement.setInt(2, spawn.getLocx());
            statement.setInt(3, spawn.getLocy());
            statement.setInt(4, spawn.getLocz());
            statement.setInt(5, spawn.getLocation());
            statement.execute();
        } catch (Exception e) {
            _log.warning("spawn couldnt be deleted in db:" + e);
        } finally {
            DatabaseUtils.closeDatabaseCS(con, statement);
        }
    }

    private void spawnMonster(L2Player activeChar, String monsterId, int respawnTime, int mobCount, Location loc)
    {
        L2Object target = activeChar.getTarget();
        if(target == null)
            target = activeChar;

        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher regexp = pattern.matcher(monsterId);
        L2NpcTemplate template;
        if(regexp.matches())
        {
            // First parameter was an ID number
            int monsterTemplate = Integer.parseInt(monsterId);
            template = NpcTable.getTemplate(monsterTemplate);
            if(Util.contains_int(ConfigValue.NotSpawnMob, monsterTemplate))
            {
                activeChar.sendMessage("Этого Моба/НПС/РБ нельзя спаунить через Админку...");
                return;
            }
        }
        else
        {
            // First parameter wasn't just numbers so go by name not ID
            monsterId = monsterId.replace('_', ' ');
            template = NpcTable.getTemplateByName(monsterId);
        }

        if(template == null)
        {
            activeChar.sendMessage("Incorrect monster template.");
            return;
        }

        try
        {
            L2Spawn spawn = new L2Spawn(template);
            spawn.setLoc(loc);
            spawn.setLocation(0);
            spawn.setAmount(mobCount);
            spawn.setHeading(activeChar.getHeading());
            spawn.setRespawnDelay(respawnTime);
            spawn.setReflection(activeChar.getReflection().getId());

            if(RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
                activeChar.sendMessage("Raid Boss " + template.name + " already spawned.");
            else
            {
                spawn.init();
                if(respawnTime == 0)
                    spawn.stopRespawn();
                activeChar.sendMessage("Created " + template.name + " on " + target.getObjectId() + ".");
            }
            if(ConfigValue.saveAdminSpawn)
                addNewSpawn(spawn, activeChar.getName());
        }
        catch(Exception e)
        {
            activeChar.sendMessage("Target is not ingame.");
        }
    }

    public void addNewSpawn(L2Spawn spawn, String adminName)
    {
        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO `spawnlist` (location,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id,aiParam) values(?,?,?,?,?,?,?,?,?,-1)");
            statement.setString(1, "AdminSpawn: " + adminName);
            statement.setInt(2, spawn.getAmount());
            statement.setInt(3, spawn.getNpcId());
            statement.setInt(4, spawn.getLocx());
            statement.setInt(5, spawn.getLocy());
            statement.setInt(6, spawn.getLocz());
            statement.setInt(7, spawn.getHeading());
            statement.setInt(8, spawn.getRespawnDelay());
            statement.setInt(9, spawn.getLocation());
            statement.execute();
        }
        catch(Exception e1)
        {
            _log.warning("spawn couldnt be stored in db:" + e1);
        }
        finally
        {
            DatabaseUtils.closeDatabaseCS(con, statement);
        }
    }


    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    @Override
    public void onLoad() {
        AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
    }

    @Override
    public void onReload() {

    }

    @Override
    public void onShutdown() {

    }

}
