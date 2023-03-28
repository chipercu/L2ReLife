package commands.admin;

import l2open.database.DatabaseUtils;
import l2open.database.FiltredPreparedStatement;
import l2open.database.L2DatabaseFactory;
import l2open.database.ThreadConnection;
import l2open.gameserver.instancemanager.*;
import l2open.gameserver.model.*;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.templates.L2NpcTemplate;
import l2open.util.Util;
import services.VoteManager;
import l2open.config.*;
import l2open.extensions.multilang.CustomMessage;
import l2open.extensions.scripts.ScriptFile;
import l2open.extensions.scripts.Scripts;
import l2open.extensions.scripts.Scripts.ScriptClassAndMethod;
import l2open.gameserver.TradeController;
import l2open.gameserver.cache.*;
import l2open.gameserver.handler.AdminCommandHandler;
import l2open.gameserver.handler.IAdminCommandHandler;
import l2open.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2open.gameserver.model.quest.Quest;
import l2open.gameserver.model.quest.QuestState;
import l2open.gameserver.serverpackets.NpcHtmlMessage;
import l2open.gameserver.tables.DoorTable;
import l2open.gameserver.tables.FishTable;
import l2open.gameserver.tables.GmListTable;
import l2open.gameserver.tables.NpcTable;
import l2open.gameserver.tables.PetDataTable;
import l2open.gameserver.tables.ReflectionTable;
import l2open.gameserver.tables.SkillTable;
import l2open.gameserver.tables.SpawnTable;
import l2open.gameserver.tables.StaticObjectsTable;
import l2open.gameserver.tables.TerritoryTable;
import l2open.util.Files;
import l2open.util.Strings;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminReload implements IAdminCommandHandler, ScriptFile {
    private static enum Commands {
        admin_reload,
        admin_reload_fstring,
        admin_reload_multisell,
        admin_reload_gmaccess,
        admin_reload_htm,
        admin_reload_qs,
        admin_reload_qs_help,
        admin_reload_loc,
        admin_reload_skills,
        admin_reload_npc,
        admin_reload_npc2,
        admin_reload_spawn,
        admin_reload_fish,
        admin_reload_abuse,
        admin_reload_translit,
        admin_reload_shops,
        admin_reload_static,
        admin_reload_doors,
        admin_reload_pkt_logger,
        admin_reload_pets,
        admin_reload_locale,
        admin_reload_instances,
        admin_reload_nobles,
        admin_reload_vote,
        admin_reload_locali,
        admin_reload_config,
        admin_reload_image
    }

    public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar) {
        Commands command = (Commands) comm;

        if (!activeChar.getPlayerAccess().CanReload)
            return false;

        switch (command) {
            case admin_reload:
                break;
            case admin_reload_image:
                ImagesChache.getInstance().load();
                break;
            case admin_reload_fstring:
                FStringCache.reload();
                break;
            case admin_reload_multisell: {
                try {
                    L2Multisell.getInstance().reload();
                } catch (Exception e) {
                    return false;
                }
                for (ScriptClassAndMethod handler : Scripts.onReloadMultiSell)
                    activeChar.callScripts(handler.scriptClass, handler.method);
                activeChar.sendMessage("Multisell list reloaded!");
                break;
            }
            case admin_reload_gmaccess: {
                try {
                    ConfigSystem.loadGMAccess();
                    for (L2Player player : L2ObjectsStorage.getPlayers())
                        if (!ConfigValue.EverybodyHasAdminRights)
                            player.setPlayerAccess(ConfigSystem.gmlist.get(player.getObjectId()));
                        else {
                            if (ConfigSystem.gmlist.containsKey(player.getObjectId()))
                                player.setPlayerAccess(ConfigSystem.gmlist.get(player.getObjectId()));
                            else
                                player.setPlayerAccess(ConfigSystem.gmlist.get(new Integer(0)));
                        }
                } catch (Exception e) {
                    return false;
                }
                activeChar.sendMessage("GMAccess reloaded!");
                break;
            }
            case admin_reload_htm: {
                Files.cacheClean();
                Files.loadPtsHtml("./data/html-ru.zip");
                Files.loadPtsHtml("./data/html-en.zip");
                activeChar.sendMessage("HTML cache clearned.");
                break;
            }
            case admin_reload_qs: {
                if (fullString.endsWith("all"))
                    for (L2Player p : L2ObjectsStorage.getPlayers())
                        reloadQuestStates(p);
                else {
                    L2Object t = activeChar.getTarget();

                    if (t != null && t.isPlayer()) {
                        L2Player p = (L2Player) t;
                        reloadQuestStates(p);
                    } else
                        reloadQuestStates(activeChar);
                }
                break;
            }
            case admin_reload_qs_help: {
                activeChar.sendMessage("");
                activeChar.sendMessage("Quest Help:");
                activeChar.sendMessage("reload_qs_help - This Message.");
                activeChar.sendMessage("reload_qs <selected target> - reload all quest states for target.");
                activeChar.sendMessage("reload_qs <no target or target is not player> - reload quests for self.");
                activeChar.sendMessage("reload_qs all - reload quests for all players in world.");
                activeChar.sendMessage("");
                break;
            }
            case admin_reload_loc: {
                TerritoryTable.getInstance().reloadData();
                ZoneManager.getInstance().reload();
                GmListTable.broadcastMessageToGMs("Locations and zones reloaded.");
                break;
            }
            case admin_reload_skills: {
                SkillTable.getInstance().reload();
                GmListTable.broadcastMessageToGMs("Skill table reloaded by " + activeChar.getName() + ".");
                _log.info("Skill table reloaded by " + activeChar.getName() + ".");
                break;
            }
            case admin_reload_npc: {
                NpcTable.getInstance().reloadAllNpc();
                GmListTable.broadcastMessageToGMs("Npc table reloaded.");
                break;
            }
            case admin_reload_npc2: {

                if (wordList.length < 2)
                    return false;
                String param = wordList[1];
                if (param.equalsIgnoreCase("all")) {
                    activeChar.sendMessage("Scripts reload starting...");
                    if (Scripts.getInstance().reload()){
                        activeChar.sendMessage("Scripts reloaded with errors. Loaded " + Scripts.getInstance().getClasses().size() + " classes.");
                    } else{
                        activeChar.sendMessage("Scripts successfully reloaded. Loaded " + Scripts.getInstance().getClasses().size() + " classes.");
                    }
                } else if (Scripts.getInstance().reloadClass(param)){
                    activeChar.sendMessage("Scripts reloaded with errors. Loaded " + Scripts.getInstance().getClasses().size() + " classes.");
                } else{
                    activeChar.sendMessage("Scripts successfully reloaded. Loaded " + Scripts.getInstance().getClasses().size() + " classes.");
                }
                NpcTable.getInstance().reloadAllNpc();
                GmListTable.broadcastMessageToGMs("Npc table reloaded.");

                L2Object obj = activeChar.getTarget();
                int targetId = 1;
                if (obj != null && obj.isNpc()) {
                    L2NpcInstance target = (L2NpcInstance) obj;
                    targetId = target.getNpcId();
                    L2Spawn spawn = target.getSpawn();
                    if (spawn != null)
                        spawn.stopRespawn();
                    if (ConfigValue.saveAdminDeSpawn){
                        deleteSpawn(spawn);
                        target.deleteMe();
                    }
                } else{
                    activeChar.sendPacket(Msg.INVALID_TARGET);
                }

                spawnMonster(activeChar, String.valueOf(targetId), 30, 1);



                break;
            }
            case admin_reload_spawn: {
                SpawnTable.getInstance().reloadAll();
                GmListTable.broadcastMessageToGMs("All npc respawned.");
                break;
            }
            case admin_reload_fish: {
                FishTable.getInstance().reload();
                GmListTable.broadcastMessageToGMs("Fish table reloaded.");
                break;
            }
            case admin_reload_abuse: {
                ConfigSystem.abuseLoad();
                GmListTable.broadcastMessageToGMs("Abuse reloaded.");
                break;
            }
            case admin_reload_translit: {
                Strings.reload();
                GmListTable.broadcastMessageToGMs("Translit reloaded.");
                break;
            }
            case admin_reload_shops: {
                TradeController.reload();
                GmListTable.broadcastMessageToGMs("Shops reloaded.");
                break;
            }
            case admin_reload_static: {
                StaticObjectsTable.getInstance().reloadStaticObjects();
                GmListTable.broadcastMessageToGMs("Static objects table reloaded.");
                break;
            }
            case admin_reload_doors: {
                DoorTable.getInstance().respawn();
                GmListTable.broadcastMessageToGMs("Door table reloaded.");
                break;
            }
            case admin_reload_pkt_logger: {
                try {
                    //Config.reloadPacketLoggerConfig();
                    activeChar.sendMessage("Packet Logger setting reloaded");
                } catch (Exception e) {
                    activeChar.sendMessage("Failed reload Packet Logger setting. Check stdout for error!");
                }
                break;
            }
            case admin_reload_pets: {
                PetDataTable.reload();
                GmListTable.broadcastMessageToGMs("PetDataTable reloaded");
                break;
            }
            case admin_reload_locale: {
                CustomMessage.reload();
                GmListTable.broadcastMessageToGMs("Localization reloaded");
                break;
            }
            case admin_reload_instances: {
                InstancedZoneManager.getInstance().reload();
                DimensionalRiftManager.getInstance().reload();
                GmListTable.broadcastMessageToGMs("Instanced zones reloaded");

                Reflection r = ReflectionTable.SOD_REFLECTION_ID == 0 ? null : ReflectionTable.getInstance().get(ReflectionTable.SOD_REFLECTION_ID);
                if (r != null)
                    r.collapse();
                ServerVariables.unset("SoD_id");
                break;
            }
            case admin_reload_nobles: {
                OlympiadDatabase.loadNobles();
                OlympiadDatabase.loadNoblesRank();
                break;
            }
            case admin_reload_vote: {
                VoteManager.load();
                break;
            }
            case admin_reload_locali: {
                CustomMessage.reload();
                break;
            }
            case admin_reload_config: {
                try {
                    ConfigSystem.reload();
                } catch (Exception e) {
                    e.printStackTrace();
                    activeChar.sendMessage("Config reloaded Error!!!");
                }
                GmListTable.broadcastMessageToGMs("Config reloaded");
                break;
            }
        }
        activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/reload.htm"));
        return true;
    }

    private void reloadQuestStates(L2Player p) {
        for (QuestState qs : p.getAllQuestsStates())
            p.delQuestState(qs.getQuest().getName());
        Quest.playerEnter(p);
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
    private void spawnMonster(L2Player activeChar, String monsterId, int respawnTime, int mobCount) {
        L2Object target = activeChar.getTarget();
        if (target == null)
            target = activeChar;

        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher regexp = pattern.matcher(monsterId);
        L2NpcTemplate template;
        if (regexp.matches()) {
            // First parameter was an ID number
            int monsterTemplate = Integer.parseInt(monsterId);
            template = NpcTable.getTemplate(monsterTemplate);
            if (Util.contains_int(ConfigValue.NotSpawnMob, monsterTemplate)) {
                activeChar.sendMessage("Этого Моба/НПС/РБ нельзя спаунить через Админку...");
                return;
            }
        } else {
            // First parameter wasn't just numbers so go by name not ID
            monsterId = monsterId.replace('_', ' ');
            template = NpcTable.getTemplateByName(monsterId);
        }

        if (template == null) {
            activeChar.sendMessage("Incorrect monster template.");
            return;
        }

        try {
            L2Spawn spawn = new L2Spawn(template);
            spawn.setLoc(target.getLoc());
            spawn.setLocation(0);
            spawn.setAmount(mobCount);
            spawn.setHeading(activeChar.getHeading());
            spawn.setRespawnDelay(respawnTime);
            spawn.setReflection(activeChar.getReflection().getId());

            if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
                activeChar.sendMessage("Raid Boss " + template.name + " already spawned.");
            else {
                spawn.init();
                if (respawnTime == 0)
                    spawn.stopRespawn();
                activeChar.sendMessage("Created " + template.name + " on " + target.getObjectId() + ".");
            }
            if (ConfigValue.saveAdminSpawn)
                addNewSpawn(spawn, activeChar.getName());
        } catch (Exception e) {
            activeChar.sendMessage("Target is not ingame.");
        }
    }

    public void addNewSpawn(L2Spawn spawn, String adminName) {
        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        try {
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
        } catch (Exception e1) {
            _log.warning("spawn couldnt be stored in db:" + e1);
        } finally {
            DatabaseUtils.closeDatabaseCS(con, statement);
        }
    }

    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    public void onLoad() {
        AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
    }

    public void onReload() {
    }

    public void onShutdown() {
    }
}