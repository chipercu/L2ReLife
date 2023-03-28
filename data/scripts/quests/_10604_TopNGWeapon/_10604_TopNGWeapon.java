package quests._10604_TopNGWeapon;

import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.model.L2ObjectsStorage;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.L2SkillLearn;
import l2open.gameserver.model.base.Experience;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.quest.Quest;
import l2open.gameserver.model.quest.QuestState;
import l2open.gameserver.serverpackets.RadarControl;
import l2open.gameserver.serverpackets.SkillList;
import l2open.gameserver.tables.SkillTable;
import l2open.util.Files;
import l2open.util.GArray;


public class _10604_TopNGWeapon extends Quest implements ScriptFile {

    private static final String HTML_ROOT = "data/scripts/quests/_10604_TopNGWeapon/";


    private static final int TalkingBlacksmith = 30283;
    private static final int TalkingMarius = 30405;

    private static final int TalkingGrandMaster = 30026;
    private static final int TalkingWeaponMerchant = 30001;
    private static final int TalkingArmorMerchant = 30002;
    private static final int TalkingJewelMerchant = 30003;
    private static final int TalkingGrocerMerchant = 30004;
    private static final int TalkingGuard = 30042;
    private static final int TalkingFighterSchoolGrandMaster = 30008;
    private static final int TalkingFizSchoolInstructor = 30028;

    private static final int Monster1 = 20101;
    private static final int Monster2 = 20103;

    private static final int LumpOfIron = 1751;
    private static final int SpiderSilk = 1363;


    private static final int MonsterItem = 1086;
    //reward
    private static final long EXP = 426284L;
    private static final long SP = 42284L;
    private static final int ADENA = 11500;




    private static final int SWORD_WEAPON_LOW = 1295;
    private static final int SWORD_WEAPON_TOP = 4219;
    private static final int BOW_WEAPON_LOW = 271;
    private static final int BOW_WEAPON_TOP = 273;
    private static final int DAGGER_WEAPON_LOW = 218;
    private static final int DAGGER_WEAPON_TOP = 4220;
    private static final int MAG_WEAPON_LOW = 9;
    private static final int MAG_WEAPON_TOP = 177;
    private static final int FIST_WEAPON_LOW = 255;
    private static final int FIST_WEAPON_TOP = 257;
    private static final int BLUNT_WEAPON_LOW = 154;
    private static final int BLUNT_WEAPON_TOP = 87;


    public _10604_TopNGWeapon()
    {
        super(PARTY_NONE);
        addStartNpc(TalkingBlacksmith);
        addTalkId(TalkingBlacksmith, TalkingMarius);
        addKillId(Monster1, Monster2);
        addQuestItem(LumpOfIron, SpiderSilk);
    }


    public static L2NpcInstance getNpcByID(int id){
        return L2ObjectsStorage.getByNpcId(id);
    }

    public static void addRadarToNpc(L2Player player, L2NpcInstance npc){
        player.sendPacket(new RadarControl(2, 1, npc.getLoc()));// Убираем флажок на карте и стрелку на компасе
        player.sendPacket(new RadarControl(0, 2, npc.getLoc()));// Ставим флажок на карте и стрелку на компасе
        //player.radar.addMarker(npc.getX(), npc.getY(), npc.getZ());
    }
    public static String fileRead( String htm, L2Player player, L2NpcInstance npc){
        String text = Files.read(HTML_ROOT + htm, player);
        text = text.replace("<?NPC?>", "<font color=\"0099FF\">" + npc.getName() + "</font>");
        text = text.replace("[button]", "<button action=\"bypass -h Quest _10603_TheBegginingOfTheWay addRadarToNpc " + player + " " + npc +"\" value=\"Понял!\" width=200 height=31 back \"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\">");
        return text;
    }

    @Override
    public String onKill(L2NpcInstance npc, QuestState qs) {
        int npcId = npc.getNpcId();
        if((npcId == Monster1) && qs.getCond() == 3) {
            if (qs.getQuestItemsCount(LumpOfIron) < 10) {
                qs.giveItems(LumpOfIron, 1);
                if (qs.getQuestItemsCount(LumpOfIron) == 10) {
                    qs.playSound(SOUND_MIDDLE);
                } else{
                    qs.playSound(SOUND_ITEMGET);
                }
            }
        }else if((npcId == Monster2) && qs.getCond() == 3) {
            if (qs.getQuestItemsCount(SpiderSilk) < 15) {
                qs.giveItems(SpiderSilk, 1);
                if (qs.getQuestItemsCount(SpiderSilk) == 15) {
                    qs.playSound(SOUND_MIDDLE);
                } else{
                    qs.playSound(SOUND_ITEMGET);
                }
            }
        }
        return null;
    }

    @Override
    public String onEvent(String event, QuestState st, L2NpcInstance npc)
    {
        L2Player player = st.getPlayer();
        String htmltext = "noquest";
        if(event.equalsIgnoreCase("Start")) {
            st.setState(STARTED);
            st.setCond(1);
            return nextNpc(player, st.getCond());
        }else if (event.equalsIgnoreCase("finish_quest")){
            st.takeItems(LumpOfIron, 10);
            st.takeItems(SpiderSilk, 15);
            if (player.getLevel() < 20){
                long addexp = Experience.LEVEL[15] - player.getExp();
                player.addExpAndSp(426284L, addexp / 5, false, false);
            }
            st.playSound(SOUND_FINISH);
            st.setState(COMPLETED);
            st.exitCurrentQuest(false);
            return nextNpc(player, st.getCond());
        }
        return htmltext;
    }

    public static String nextNpc(L2Player player, int cond){
        String htmltext = null;
        if (cond == 1) {
            htmltext = fileRead("talking_nextNPC_01.htm", player, getNpcByID(TalkingBlacksmith));
            addRadarToNpc(player, getNpcByID(TalkingGrandMaster));
        } else if (cond == 2) {
            htmltext = fileRead("talking_nextNPC_01.htm", player, getNpcByID(TalkingGrandMaster));
            addRadarToNpc(player, getNpcByID(TalkingGrandMaster));
        }
        return htmltext;
    }

    @Override
    public String onTalk(L2NpcInstance npc, QuestState st)
    {
        String htmltext = "noquest";
        int npcId = npc.getNpcId();
        int cond = st.getCond();
        L2Player player = st.getPlayer();
        if (npcId == TalkingBlacksmith) {
            if (st.getState() == CREATED) {
                htmltext = "_10604_blacksmith_01.htm";
            } else {
                htmltext = nextNpc(player, cond);
            }
        } else if (npcId == TalkingMarius) {
            if (cond == 1) {
                htmltext = "talking_marius_01.htm";
            } else if (cond == 6) {
                htmltext = "talking_grandmaster_02.htm";
            } else if (cond == 16 && st.getQuestItemsCount(MonsterItem) == 10){
                htmltext = "talking_grandmaster_03.htm";
            } else {
                htmltext = nextNpc(player, cond);
            }
        } else if (npcId == TalkingWeaponMerchant) {
            if (cond == 2) {
                if (player.isMageClass()) {
                    htmltext = "talking_weapon_merchant_02.htm";
                } else {
                    htmltext = "talking_weapon_merchant_01.htm";
                }
            } else {
                htmltext = nextNpc(player, cond);
            }
        } else if (npcId == TalkingArmorMerchant) {
            if (cond == 3) {
                htmltext = "talking_armor_merchant_01.htm";
            } else {
                htmltext = nextNpc(player, cond);
            }
        } else if (npcId == TalkingJewelMerchant) {
            if (cond == 4) {
                htmltext = "talking_jewel_merchant_01.htm";
            } else {
                htmltext = nextNpc(player, cond);
            }
        } else if (npcId == TalkingGrocerMerchant) {
            if (cond == 5) {
                htmltext = "talking_grocer_merchant_01.htm";
            } else {
                htmltext = nextNpc(player, cond);
            }
        } else if (npcId == TalkingGuard) {
            if (cond == 7) {
                if (player.isMageClass()) {
                    htmltext = "talking_guard_01.htm";
                } else {
                    htmltext = "talking_guard_02.htm";
                }
            } else if (cond == 8) {
                htmltext = nextNpc(player, cond);
            } else {
                htmltext = nextNpc(player, cond);
            }
        }else if (npcId == TalkingFighterSchoolGrandMaster){
            if (cond == 8){
                htmltext = "talking_fighter_school_grand_master_01.htm";
            }else if (cond == 14){
                htmltext = "talking_fighter_school_grand_master_02.htm";
            }else {
                htmltext = nextNpc(player, cond);
            }
        }else if (npcId == TalkingFizSchoolInstructor){
            if (cond == 9){
                htmltext = "talking_fiz_school_instructor_01.htm";
            }else if (cond == 11){
                giveSkill(player);
                htmltext = "talking_fiz_school_instructor_02.htm";
            }else if (cond == 13){
                htmltext = "talking_fiz_school_instructor_03.htm";
            }
            else {
                htmltext = nextNpc(player, cond);
            }
        }

        return htmltext;
    }

    private void giveSkill(L2Player player)
    {
        int count = 0;
        GArray<L2SkillLearn> skills = player.getAvailableSkills(player.getClassId());
        while(skills.size() > count)
        {
            count = 0;
            for(L2SkillLearn s : skills)
            {
                L2Skill sk = SkillTable.getInstance().getInfo(s.id, s.skillLevel);
                if(sk == null || !sk.getCanLearn(player.getClassId()))
                {
                    count++;
                    continue;
                }
                player.addSkill(sk, true);
            }
            skills = player.getAvailableSkills(player.getClassId());
        }

        player.sendPacket(new SkillList(player));
    }


    @Override
    public void onLoad() {ScriptFile._log.info("Loaded Quest: 10601: TheBegginingOfTheWay");}

    @Override
    public void onReload() {

    }

    @Override
    public void onShutdown() {

    }
}