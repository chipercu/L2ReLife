package scripts.commands.admin;

import ai.chaos.CasterOfChaosAI;
import l2open.config.ConfigValue;
import l2open.database.mysql;
import l2open.extensions.network.MMOConnection;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.cache.Msg;
import l2open.gameserver.handler.AdminCommandHandler;
import l2open.gameserver.handler.IAdminCommandHandler;
import l2open.gameserver.instancemanager.PlayerManager;
import l2open.gameserver.instancemanager.QuestManager;
import l2open.gameserver.model.*;
import l2open.gameserver.model.base.ClassId;
import l2open.gameserver.model.base.Race;
import l2open.gameserver.model.entity.olympiad.Olympiad;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.items.L2ItemInstance;
import l2open.gameserver.model.items.MailParcelController;
import l2open.gameserver.model.quest.Quest;
import l2open.gameserver.model.quest.QuestState;
import l2open.gameserver.network.L2GameClient;
import l2open.gameserver.serverpackets.Say2;
import l2open.gameserver.serverpackets.SkillList;
import l2open.gameserver.tables.SkillTable;
import l2open.gameserver.tables.player.PlayerData;
import l2open.gameserver.templates.L2Item;
import l2open.gameserver.templates.L2PlayerTemplate;
import l2open.gameserver.xml.ItemTemplates;
import l2open.util.GArray;
import l2open.util.Location;
import l2open.util.Rnd;
import ai.BotPlayers.BerserkerAI;
import ai.BotPlayers.Gladiator;
import ai.BotPlayers.GladiatorAI;
import ai.PlayerTest;
import npc.model.Military.GeoUtil.Point;
import npc.model.Military.GeoUtil.Vector2DRef;
import npc.model.Military.Unit;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AdminFakePlayers implements IAdminCommandHandler, ScriptFile {

    private static enum Commands {
        admin_fake_player,
        admin_fake_delete,
        admin_create_fake,
        admin_set_bot,
        admin_get_skills,
        admin_set_ai,
        admin_autoshot,
        admin_testvector,
        admin_unit_test
    }

    public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar) {
        Commands command = (Commands) comm;

        if (!activeChar.getPlayerAccess().CanEditNPC) return false;

        switch (command) {

            case admin_fake_player: {
                new Thread(new l2open.common.RunnableImpl() {
                    @Override
                    public void runImpl() {
                        GArray<HashMap<String, Object>> list = mysql.getAll("SELECT `obj_id`, `value`, (SELECT `account_name` FROM `characters` WHERE `characters`.`obj_Id` = `character_variables`.`obj_id` LIMIT 1) AS `account_name` FROM `character_variables` WHERE name LIKE 'bot1'");
                        for (HashMap<String, Object> e : list) {
                            L2GameClient client = new L2GameClient(new MMOConnection<L2GameClient>(null), true);
                            client.setCharSelection((Integer) e.get("obj_id"));
                            L2Player p = client.loadCharFromDisk(0);
                            if (p == null || p.isDead())
                                continue;
                            client.setLoginName(e.get("account_name") == null ? "OfflineTrader_" + p.getName() : (String) e.get("account_name"));
                            p.setLoc(activeChar.getLoc());
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
//                            setAIfromClass(p);


                            if (p.getClan() != null && p.getClan().getClanMember(p.getObjectId()) != null)
                                p.getClan().getClanMember(p.getObjectId()).setPlayerInstance(p, false);
                            _log.info("Restored bot: " + p.getName());
                        }

                    }
                }).start();


//                        L2GameClient client = new L2GameClient(new MMOConnection<L2GameClient>(null), true);
//                        //client.setCharSelection(268530865);
//                        L2Player p = client.loadCharFromDisk(0);
//                        client.setLoginName("BOT");
//                        p.setLoc(activeChar.getLoc());
//                        client.OnOfflineTrade();
//                        //p.restoreBonus();
//                        p.spawnMe();
//                        p.updateTerritories();
//                        p.setOnlineStatus(true);
//                        //p.setOfflineMode(true);
//                        p.setConnected(true);
//                        p.setNameColor(Integer.decode("0x" + ConfigValue.OfflineTradeNameColor));
//                        //p.restoreEffects();
//                        //p.restoreDisableSkills();
//                        p.broadcastUserInfo(true);
//                        p.setTitle("BOT");
//                        p.setInvisible(false);
//
//                        //p.setAI(new PlayerTest(p));
//                        if (p.getClan() != null && p.getClan().getClanMember(p.getObjectId()) != null)
//                            p.getClan().getClanMember(p.getObjectId()).setPlayerInstance(p, false);
//
//                        _log.info("Restored " + p.getName() + " in world");

                break;
            }
            case admin_unit_test:{
//                MilitaryManager.createUnit(5, (byte) 1, "Unit_Tank");
//                final L2Player player = L2Player.create(5, (byte) 1, "Military", "Unit_Tank", (byte) Rnd.get(0, 2), (byte) Rnd.get(0, 2), (byte) Rnd.get(0, 2), 0, 78);
//                if (player != null) {
//                    player.setVar("unit_type", "Sage_Unit");
//                    giveAllSkills(player);
//                }
                spawnUnit(activeChar);
//                if (unit != null) {
//                    giveAllSkills(unit);
//                }
//                if (unit != null) {
//                    unit.setCommander(activeChar);
//                }
                break;
            }
            case admin_fake_delete: {
                L2Player bot;
                if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer()) {
                    bot = activeChar.getTarget().getPlayer();
                    bot.deleteMe();

                }
                break;
            }
            case admin_get_skills: {
                //Optional<L2Skill> first =
                activeChar.getAllSkills().stream()
                        .filter(o -> !o.isToggle())
                        .filter(o -> !o.isPassive())
                        .sorted((o1, o2) -> (int) (o1.getPower() - o2.getPower()))
                        .forEach(o -> {
                                    if (activeChar.isGM()) {
                                        FileWriter file = null;
                                        StringBuilder sb = new StringBuilder();
                                        try {
                                            file = new FileWriter("skills-" + activeChar.getClassId().name() + ".txt", true);
                                            sb.append("add(")
                                                    .append(o.getId())
                                                    .append("); //")
                                                    .append(o.getName())
                                                    .append("\n");
                                            file.write(sb.toString());
                                            file.flush();
                                            file.close();

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } finally {
                                            try {
                                                assert file != null;
                                                file.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }


                                    //activeChar.sendPacket(new Say2(0, 0, "DEBUG", "----------------------------"));
                                    activeChar.sendPacket(new Say2(0, 0, "DEBUG",
                                            o.getName()
                                                    + ": id " + o.getId()
                                                    + "| power: " + o.getPower()));
                                    //activeChar.sendPacket(new Say2(0, 0, "DEBUG", "----------------------------"));
                                }
                        );
                break;
            }
            case admin_create_fake: {
                int _classId = Rnd.get(88, 118);
                int _sex = Rnd.get(0, 1);


                for (int i = 0; i < 10; i++) {
//                    int _classId = Rnd.get(88, 118);
//                    int _sex = Rnd.get(0, 1);
                    L2Player newChar = L2Player.create(_classId, (byte) _sex, "BOT", "Bot" + i, (byte) Rnd.get(0, 2), (byte) Rnd.get(0, 2), (byte) Rnd.get(0, 2), 0, Rnd.get(20, 85));
                    newChar.setVar("bot", "bot");
                    adminGiveAllSkills(newChar);
                }

//                initNewChar(client, newChar);
//                newChar.setLevel(Rnd.get(20, 84));

                break;
            }
            case admin_set_bot: {
                if (activeChar.getTarget()!= null){
                    final L2Player target = (L2Player) activeChar.getTarget();
                    target.setVar("unit_type", "town_patrol_commander");
                    target.setVar("bot1", "bot1");
                }
                break;
            }
            case admin_set_ai: {
                if (activeChar.getTarget().isPlayer()) {
                    L2Player player = activeChar.getTarget().getPlayer();
                    switch (player.getClassId()) {
                        case gladiator:
                        case duelist:
                            player.setAI(new GladiatorAI(player));
                            break;
                        case berserker:
                        case doombringer:
                            player.setAI(new BerserkerAI(player));
                            break;
                    }

                    player.setAI(new scripts.ai.Fort.MilitaryAI.TownGuardComanderAI(player));

                } else if (activeChar.getTarget().isNpc()) {
                    L2NpcInstance npc = (L2NpcInstance) activeChar.getTarget();
                    if (npc.getNpcId() == 36671) {
//                        npc.setAI(new Fighter(npc));
                        npc.setAI(new CasterOfChaosAI(npc));
                        activeChar.sendMessage("AI reset");
                    }
                }

                break;
            }
            case admin_autoshot: {
                int soul = 0;
                int spirit = 0;
                if (activeChar.getActiveWeaponInstance() != null) {
                    L2Item.Grade crystalType = activeChar.getActiveWeaponInstance().getCrystalType();
                    switch (crystalType) {
                        case NONE:
                            break;
                        case D:
                            break;
                        case C:
                            soul = 1464;   //Soulshot: C-grade
                            spirit = 3949;   //spiritshot: C-grade
                            break;
                        case B:
                            soul = 1465;   //Soulshot: B-grade
                            spirit = 3950;   //spiritshot: B-grade
                            break;
                        case A:
                            soul = 1466;   //Soulshot: A-grade
                            spirit = 3951;   //spiritshot: A-grade
                            break;
                        case S:
                        case S80:
                        case S84:
                            soul = 1467;   //Soulshot: S-grade
                            spirit = 3952;   //spiritshot: S-grade
                            break;
                    }
                }
                activeChar.addAutoSoulShot(soul);
                activeChar.addAutoSoulShot(spirit);
                activeChar.AutoShot();
            }
        }
        return true;
    }

    private void spawnUnit(L2Player activeChar){

        GArray<HashMap<String, Object>> list = mysql.getAll("SELECT `obj_id`, `value`, (SELECT `account_name` FROM `characters` WHERE `characters`.`obj_Id` = `character_variables`.`obj_id` LIMIT 1) AS `account_name` FROM `character_variables` WHERE name LIKE 'unit_type'");

        for (HashMap<String, Object> e : list) {
            L2GameClient client = new L2GameClient(new MMOConnection<>(null), true);
            client.setCharSelection((Integer) e.get("obj_id"));
            L2Player p = client.loadCharFromDisk(0);

            if (p == null || p.getVar("unit_type") == null || !p.getVar("unit_type").equals("town_patrol_commander")){
                continue;
            }
            client.setLoginName(e.get("account_name") == null ? "OfflineTrader_" + p.getName() : (String) e.get("account_name"));
            p.setLoc(Location.coordsRandomize(activeChar.getLoc(), 50, 100));
            client.OnOfflineTrade();
            p.spawnMe();
            p.updateTerritories();
            p.setOnlineStatus(true);
            p.setInvisible(false);
            p.setConnected(true);
            p.setNameColor(Integer.decode("0x" + ConfigValue.OfflineTradeNameColor));
            p.broadcastUserInfo(true);
        }
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

    private void setAIfromClass(L2Player p) {
        ClassId classId = p.getClassId();
        if (classId == ClassId.maleSoldier) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.femaleSoldier) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.trooper) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.warder) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.berserker) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.maleSoulbreaker) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.femaleSoulbreaker) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.arbalester) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.doombringer) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.maleSoulhound) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.femaleSoulhound) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.trickster) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.inspector) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.judicator) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.duelist) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.dreadnought) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.phoenixKnight) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.hellKnight) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.sagittarius) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.adventurer) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.archmage) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.soultaker) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.arcanaLord) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.cardinal) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.hierophant) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.evaTemplar) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.swordMuse) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.windRider) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.moonlightSentinel) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.mysticMuse) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.elementalMaster) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.evaSaint) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.shillienTemplar) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.spectralDancer) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.ghostHunter) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.ghostSentinel) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.stormScreamer) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.spectralMaster) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.shillienSaint) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.titan) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.grandKhauatari) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.dominator) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.doomcryer) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.fortuneSeeker) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.maestro) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.fighter) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.warrior) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.gladiator) { //TODO:
            p.setAI(new Gladiator(p));
            //p.setAI(new Fighter(p));
        } else if (classId == ClassId.warlord) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.knight) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.paladin) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.darkAvenger) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.rogue) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.treasureHunter) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.hawkeye) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.mage) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.wizard) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.sorceror) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.necromancer) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.warlock) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.cleric) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.bishop) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.prophet) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.elvenFighter) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.elvenKnight) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.templeKnight) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.swordSinger) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.elvenScout) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.plainsWalker) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.silverRanger) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.elvenMage) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.elvenWizard) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.spellsinger) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.elementalSummoner) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.oracle) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.elder) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.darkFighter) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.palusKnight) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.shillienKnight) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.bladedancer) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.assassin) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.abyssWalker) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.phantomRanger) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.darkMage) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.darkWizard) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.spellhowler) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.phantomSummoner) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.shillienOracle) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.shillienElder) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.orcFighter) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.orcRaider) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.destroyer) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.orcMonk) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.tyrant) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.orcMage) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.orcShaman) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.overlord) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.warcryer) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.dwarvenFighter) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.scavenger) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.bountyHunter) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.artisan) {
            p.setAI(new PlayerTest(p));
        } else if (classId == ClassId.warsmith) {
            p.setAI(new PlayerTest(p));
        }
    }

    private void initNewChar(L2Player newChar) {
        L2PlayerTemplate template = newChar.getTemplate();

        PlayerData.getInstance().restoreCharSubClasses(newChar);

        if (ConfigValue.StartingAdena > 0) newChar.addAdena(ConfigValue.StartingAdena);

        if (ConfigValue.GiveStartPremium) {
//            if(CharNameTable.getInstance().accountCharNumber(getClient().getLoginName()) <= ConfigValue.GiveStartPremiumCharCount)
//            {
//                if(ConfigValue.StartPremiumType == 0)
//                {
//                    try
//                    {
//                        mysql.setEx(L2DatabaseFactory.getInstanceLogin(), "UPDATE `accounts` SET `bonus`=?,`bonus_expire`=UNIX_TIMESTAMP()+" + (int) ConfigValue.StartPremiumRate[1] + "*24*60*60 WHERE `login`=?", ConfigValue.StartPremiumRate[0], newChar.getAccountName());
//                        newChar.setVar("PremiumStart", "-1");
//                    }
//                    catch(SQLException e)
//                    {
//                        e.printStackTrace();
//                    }
//                }
//                else if(ConfigValue.StartPremiumType == 1)
//                {
//                    try
//                    {
//                        mysql.setEx(L2DatabaseFactory.getInstanceLogin(), "REPLACE INTO bonus(obj_id, account, bonus_name, bonus_value, bonus_expire_time) VALUES ("+newChar.getObjectId()+",'"+newChar.getAccountName()+"','RATE_ALL',"+ConfigValue.StartPremiumRate[0]+",UNIX_TIMESTAMP()+" + (int) ConfigValue.StartPremiumRate[1] + "*24*60*60)");
//                        newChar.setVar("PremiumStart", "-1");
//                    }
//                    catch(SQLException e)
//                    {
//                        e.printStackTrace();
//                    }
//                }
//
//
//            }
        }

        if (ConfigValue.StartingItem.length > 0 && ConfigValue.StartingItem[0] > 0) {
            ItemTemplates itemTable = null;
            L2ItemInstance item = null;
            for (int i = 0; i < ConfigValue.StartingItem.length; i++) {
                itemTable = ItemTemplates.getInstance();
                item = itemTable.createItem(ConfigValue.StartingItem[i]);
                item.setCount(ConfigValue.StartingItemCount.length > i ? ConfigValue.StartingItemCount[i] : ConfigValue.StartingItemCount[0]);
                item.setEnchantLevel(ConfigValue.StartingItemEnchant.length > i ? ConfigValue.StartingItemEnchant[i] : ConfigValue.StartingItemEnchant[0]);
                newChar.getInventory().addItem(item);
            }
        }

        newChar.setXYZInvisible(template.spawnLoc);

        if (ConfigValue.CharTitle) newChar.setTitle(ConfigValue.CharAddTitle);
        else newChar.setTitle("");

        ItemTemplates itemTable = ItemTemplates.getInstance();
        {
            for (L2Item i : template.getItems()) {
                L2ItemInstance item = itemTable.createItem(i.getItemId());
                newChar.getInventory().addItem(item);

                if (item.getItemId() == 5588) // tutorial book
                    newChar.registerShortCut(new L2ShortCut(11, 0, L2ShortCut.TYPE_ITEM, item.getObjectId(), -1));

                if (item.isEquipable() && (newChar.getActiveWeaponItem() == null || item.getItem().getType2() != L2Item.TYPE2_WEAPON))
                    newChar.getInventory().equipItem(item, false);
            }

            // Scroll of Escape: Kamael Village
            L2ItemInstance item = itemTable.createItem(9716);
            item.setCount(10);
            newChar.getInventory().addItem(item);

            // Adventurer's Scroll of Escape
            item = itemTable.createItem(10650);
            item.setCount(5);
            newChar.getInventory().addItem(item);
        }

        for (L2SkillLearn skill : newChar.getAvailableSkills(newChar.getClassId()))
            newChar.addSkill(SkillTable.getInstance().getInfo(skill.id, skill.skillLevel), true);

        if (newChar.getSkillLevel(1001) > 0) // Soul Cry
            newChar.registerShortCut(new L2ShortCut(1, 0, L2ShortCut.TYPE_SKILL, 1001, 1));
        if (newChar.getSkillLevel(1177) > 0) // Wind Strike
            newChar.registerShortCut(new L2ShortCut(1, 0, L2ShortCut.TYPE_SKILL, 1177, 1));
        if (newChar.getSkillLevel(1216) > 0) // Self Heal
            newChar.registerShortCut(new L2ShortCut(2, 0, L2ShortCut.TYPE_SKILL, 1216, 1));

        // add attack, take, sit shortcut
        newChar.registerShortCut(new L2ShortCut(0, 0, L2ShortCut.TYPE_ACTION, 2, -1));
        newChar.registerShortCut(new L2ShortCut(3, 0, L2ShortCut.TYPE_ACTION, 5, -1));
        newChar.registerShortCut(new L2ShortCut(10, 0, L2ShortCut.TYPE_ACTION, 0, -1));
        // fly transform
        newChar.registerShortCut(new L2ShortCut(0, 10, L2ShortCut.TYPE_SKILL, 911, 1));
        newChar.registerShortCut(new L2ShortCut(3, 10, L2ShortCut.TYPE_SKILL, 884, 1));
        newChar.registerShortCut(new L2ShortCut(4, 10, L2ShortCut.TYPE_SKILL, 885, 1));
        // air ship
        newChar.registerShortCut(new L2ShortCut(0, 11, L2ShortCut.TYPE_ACTION, 70, 0));

        //startTutorialQuest(newChar);

        newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
        newChar.setCurrentCp(0); // retail
        newChar.getRecommendation().setRecomTimeLeft(3600);

        if (ConfigValue.MailOnEnterGame) {
            MailParcelController.Letter mail = new MailParcelController.Letter();
            mail.senderId = 1;
            mail.senderName = ConfigValue.MailOnEnterGameSenderName;
            mail.receiverId = newChar.getObjectId();
            mail.receiverName = newChar.getName();
            mail.topic = ConfigValue.MailOnEnterGameTopic;
            mail.body = ConfigValue.MailOnEnterGameBody;
            mail.price = 0;
            mail.unread = 1;
            mail.system = 0;
            mail.hideSender = 2;
            mail.validtime = 720 * 3600 + (int) (System.currentTimeMillis() / 1000L);

            MailParcelController.getInstance().sendLetter(mail);
            //newChar.setVar("MyBirthdayReceiveYear", String.valueOf(now.get(Calendar.YEAR)), -1);
        }

        if (ConfigValue.CharacterCreate350q) {
            Quest q = QuestManager.getQuest("_350_EnhanceYourWeapon");
            QuestState qs = q.newQuestState(newChar, Quest.STARTED);
            qs.setCond(1);
        }
        if (ConfigValue.CharacterCreateNoble) {
            Quest q = QuestManager.getQuest("_234_FatesWhisper");
            QuestState qs = newChar.getQuestState(q.getName());
            if (qs != null) qs.exitCurrentQuest(true);
            q.newQuestState(newChar, Quest.COMPLETED);

            if (newChar.getRace() == Race.kamael) {
                q = QuestManager.getQuest("_236_SeedsOfChaos");
                qs = newChar.getQuestState(q.getName());
                if (qs != null) qs.exitCurrentQuest(true);
                q.newQuestState(newChar, Quest.COMPLETED);
            } else {
                q = QuestManager.getQuest("_235_MimirsElixir");
                qs = newChar.getQuestState(q.getName());
                if (qs != null) qs.exitCurrentQuest(true);
                q.newQuestState(newChar, Quest.COMPLETED);
            }

            Olympiad.addNoble(newChar);
            newChar.setNoble(true);
            newChar.updatePledgeClass();
            newChar.updateNobleSkills();
        } else if (ConfigValue.CharacterSetSkillNoble)
            newChar.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_NOBLESSE_BLESSING, 1));


        //newChar.setOnlineStatus(false);

        PlayerManager.saveCharToDisk(newChar);
        newChar.deleteMe(); // release the world of this character and it's inventory

        //client.setCharSelection(CharacterSelectionInfo.loadCharacterSelectInfo(client.getLoginName()));
    }

    private void adminGiveAllSkills(L2Player activeChar) {
        L2Player player = null;
        if (activeChar != null && ((L2Object) activeChar).isPlayer()) {
            player = (L2Player) activeChar;
        } else {
            activeChar.sendPacket(Msg.INVALID_TARGET);
            return;
        }
        int unLearnable = 0;
        int skillCounter = 0;
        GArray<L2SkillLearn> skills = player.getAvailableSkills(player.getClassId());
        while (skills.size() > unLearnable) {
            unLearnable = 0;
            for (L2SkillLearn s : skills) {
                L2Skill sk = SkillTable.getInstance().getInfo(s.id, s.skillLevel);
                if (sk == null || !sk.getCanLearn(player.getClassId())) {
                    unLearnable++;
                    continue;
                }
                if (player.getSkillLevel(sk.getId()) == -1) skillCounter++;
                player.addSkill(sk, true);
                s.deleteSkills(player);
            }
            skills = player.getAvailableSkills(player.getClassId());
        }

        player.updateStats();
        player.sendUserInfo(true);
        player.sendMessage("Admin gave you " + skillCounter + " skills.");
        player.sendPacket(new SkillList(player));
        activeChar.sendMessage("You gave " + skillCounter + " skills to " + player.getName());
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