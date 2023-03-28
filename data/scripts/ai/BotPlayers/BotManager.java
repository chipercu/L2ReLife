package ai.BotPlayers;

import l2open.config.ConfigValue;
import l2open.database.mysql;
import l2open.extensions.network.MMOConnection;
import l2open.extensions.scripts.Functions;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.model.L2ObjectsStorage;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2World;
import l2open.gameserver.model.base.ClassId;
import l2open.gameserver.network.L2GameClient;
import l2open.util.GArray;
import ai.PlayerTest;

import java.util.Collection;
import java.util.HashMap;

public class BotManager extends Functions implements ScriptFile {

    public void loadAllFakePlayers(){
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
                    client.setLoginName((String) e.get("account_name") == null ? "OfflineTrader_" + p.getName() : (String) e.get("account_name"));
                    //p.setLoc(activeChar.getLoc());
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
                    setAIfromClass(p);

                    if (p.getClan() != null && p.getClan().getClanMember(p.getObjectId()) != null)
                        p.getClan().getClanMember(p.getObjectId()).setPlayerInstance(p, false);
                    _log.info("Restored bot: " + p.getName());
                }
            }
        }).start();
    }


    private void setAIfromClass(L2Player p){
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
        } else if (classId == ClassId.gladiator) {
            p.setAI(new Gladiator(p));
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



    @Override
    public void onLoad() {
//        Collection<L2Player> players = L2ObjectsStorage.getPlayers();
//        for (L2Player p: players){
//            if (p.getVar("bot1").equalsIgnoreCase("bot1")){
//                p.logout(false, false, true, true);
//            }
//        }
//        loadAllFakePlayers();
    }

    @Override
    public void onReload() {

    }

    @Override
    public void onShutdown() {

    }
}
