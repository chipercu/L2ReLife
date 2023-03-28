package ai.BotPlayers;

import l2open.common.RunnableImpl;
import l2open.common.ThreadPoolManager;
import l2open.config.ConfigValue;
import l2open.extensions.scripts.Functions;
import l2open.gameserver.ai.DefaultAI;
import l2open.gameserver.ai.L2PlayerAI;
import l2open.gameserver.geodata.GeoEngine;
import l2open.gameserver.handler.IItemHandler;
import l2open.gameserver.handler.ItemHandler;
import l2open.gameserver.model.*;
import l2open.gameserver.model.base.Transaction;
import l2open.gameserver.model.entity.Duel;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.items.L2ItemInstance;
import l2open.gameserver.serverpackets.*;
import l2open.gameserver.skills.Env;
import l2open.gameserver.skills.Formulas;
import l2open.gameserver.skills.effects.EffectTemplate;
import l2open.gameserver.tables.SkillTable;
import l2open.gameserver.tables.SkillTreeTable;
import l2open.gameserver.templates.L2Item;
import l2open.gameserver.templates.L2Weapon;
import l2open.util.GArray;
import l2open.util.GCSArray;
import l2open.util.Location;
import l2open.util.Rnd;
import l2open.util.reference.HardReference;
import l2open.util.reference.HardReferences;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class FakePlayerAI extends L2PlayerAI {

    protected List<L2Skill> _rangeAttackSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _rangeAOEAttackSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _meleeAttackSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _meleeAOEAttackSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _selfBuffSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _partyBuffSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _selfHealSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _healSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _rangeDebuffSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _meleeDebuffSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _ultimateSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _pvpBuffSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _rushSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _energySkillList = new ArrayList<L2Skill>();


    protected GCSArray<L2Skill> _disableSkill = new GCSArray<L2Skill>();
    protected List<L2Skill> _restrictedSkill;

    protected ScheduledFuture<?> _attackTask;
    protected ScheduledFuture<?> castTask;




    long delay = System.currentTimeMillis();

    private Location partyFollowLoc;
    private boolean follow = false;

    protected static final int _radiusNewTargetToList = 2500;

    protected BotAiTalk aiTalk;
    protected List<L2Character> targetList;
    protected Location spawnLoc;

    protected Transaction tradeTransaction;
    protected static final int _botTaskDelay = 500;

    protected L2Character target;
    protected L2Player master;
    protected L2ItemInstance activeWeaponInstance;
    protected L2Player transactionOtherPlayer;


    protected static final int[] soulshot = {
            1464,   //Soulshot: C-grade
            1465,   //Soulshot: B-grade
            1466,   //Soulshot: A-grade
            1467,   //Soulshot: S-grade
    };
    protected static final int[] spiritshot = {
            3949,   //spiritshot: C-grade
            3950,   //spiritshot: B-grade
            3951,   //spiritshot: A-grade
            3952,   //spiritshot: S-grade
    };


    public FakePlayerAI(L2Character actor) {
        super((L2Player) actor);
        activeWeaponInstance = getActor().getActiveWeaponInstance();
        aiTalk = new BotAiTalk(getActor());
        getActor().resetSkillsReuse();
        getActor().getSkillReuseTimeStamps().clear();
        autoSoulshot();
//        if (getActor().isMageClass()) {
//            for (Buff buffId : BuffScheme.buffSchemes.get(1).getBuffIds()) {
//                buff(buffId.getId(), buffId.getLevel(), (L2Playable) actor);
//            }
//        } else {
//            for (Buff buffId : BuffScheme.buffSchemes.get(2).getBuffIds()) {
//                buff(buffId.getId(), buffId.getLevel(), (L2Playable) actor);
//            }
//        }
    }


    public long disableSkill(L2Skill skill) {
        _disableSkill.add(skill);
        long reuseDelay = Formulas.calcSkillReuseDelay(getActor(), skill);
        ThreadPoolManager.getInstance().schedule(() -> _disableSkill.remove(skill), reuseDelay < 50? 2000L : reuseDelay);
        return reuseDelay;
    }

    public boolean skillIsDisable(L2Skill skill) {
        return _disableSkill.contains(skill);
    }

    protected L2Skill getTopSkill(Collection<L2Skill> skills) {
        return skills.stream()
                .filter(o -> o.getWeaponDependancy(getActor()))
                .filter(o -> !_restrictedSkill.contains(o))
                .filter(o -> o.getNumCharges() <= getActor().getIncreasedForce())
                .filter(o -> !skillIsDisable(o))
                .sorted((o1, o2) -> (int) (o2.getPower() - o1.getPower()))
                .findFirst().orElse(null);
    }


    public void setLog(String text) {
        L2Character actor = getActor();
        if (actor != null) {
            //_log.info(text);
            Say2 cs = new Say2(0, 0, "DEBUG", text);
            for (L2Player player : L2ObjectsStorage.getPlayers())
                if (player.isGM()) player.sendPacket(cs);
        }
    }

    private L2Character getNextTarget() {
        Optional<L2Character> first = Optional.empty();
        if (getActor() != null && target == null && !targetList.isEmpty()) {
            first = targetList.stream()
                    .filter(t -> !t.isDead())
                    .filter(t -> !t.getClass().getSimpleName().equalsIgnoreCase("TreasureChestInstance"))
                    .min((o1, o2) -> (int) (o1.getRealDistance3D(getActor()) - o2.getRealDistance3D(getActor())));
        }
        return first.orElse(null);
    }

    private void bowPlayers() {

        int posX = getActor().getX();
        int posY = getActor().getY();
        int posZ = getActor().getZ();

        int signx = posX < target.getX() ? -1 : 1;
        int signy = posY < target.getY() ? -1 : 1;

        int range = 500;

        posX += Math.round(signx * range);
        posY += Math.round(signy * range);
        posZ = GeoEngine.getHeight(posX, posY, posZ, getActor().getReflection().getGeoIndex());

        if (GeoEngine.canMoveToCoord(target.getX(), target.getY(), target.getZ(), posX, posY, posZ, getActor().getReflection().getGeoIndex())) {
            getActor().moveToLocation(posX, posY, posZ, 0, true);
        }
    }

    private boolean useSkill(List<L2Skill> skillList, int dist){
        L2Skill skill = getTopSkill(skillList);
        if (skill != null){
            if (target.getEffectList().getEffectBySkillId(skill.getId()) != null){
                return false;
            }
            if (skill.getCastRange() > dist){
                setLog(skill.getName());
                getActor().getAI().Cast(skill, target, false, false);
                disableSkill(skill);
                return true;
            }else {
                getActor().followToCharacter(target, 40, true, true);
                //getActor().getAI().Attack(target, false, false);
                return false;
            }
        }
        return false;
    }


    private void attack() {
        if (getActor() != null && target != null && !target.isDead()) {
            int dist = (int) getActor().getRealDistance3D(target);
            if (!getActor().isCastingNow()) {

                    if (target.isMonster() || target.isNpc() || target.isRaid() || target.isBoss()) {
                        if (getActor().isMageClass()) {

                        } else {
                            long targetAroundCount = target.getAroundNpc(300, 150).stream()
                                    .filter(L2Object::isMonster)
                                    .filter(t -> !t.isDead())
                                    .count();
                            if (dist > 250 ) {
                                if (targetAroundCount > 1 && getTopSkill(_rangeAOEAttackSkillList) != null){
                                    useSkill(_rangeAOEAttackSkillList, dist);
                                }else {
                                    if (Rnd.get(100) < 70){
                                        useSkill(_rangeAttackSkillList, dist);
                                    }else if (Rnd.get(100) < 70){
                                        useSkill(_rushSkillList, dist);
                                    }else {
                                        useSkill(_rangeDebuffSkillList, dist);
                                    }
                                }
                            }else if (dist < 100){
                                if (targetAroundCount > 1 && getTopSkill(_meleeAOEAttackSkillList) != null){
                                    useSkill(_meleeAOEAttackSkillList, dist);
                                }else {
                                    if (Rnd.get(100) < 70){
                                        useSkill(_meleeAttackSkillList, dist);
                                    }else {
                                        useSkill(_meleeDebuffSkillList, dist);
                                    }
                                }
                            }else {
                                getActor().moveToLocation(target.getLoc(), 40, true);
                            }
                            setLog(String.valueOf(targetAroundCount));
                        }

                    } else {

                    }
//                } else if (dist > 200) {
//                    setLog(String.valueOf(dist));
//                    getActor().moveToLocation(target.getLoc(), 20, true);
//                }
            }
        }
    }
    private static class TaskComparator implements Comparator<Task> {
        public int compare(Task o1, Task o2) {
            if (o1 == null || o2 == null)
                return 0;
            return o2.weight - o1.weight;
        }
    }

    private static final int TaskDefaultWeight = 1000;
    private static final TaskComparator task_comparator = new TaskComparator();
    protected ConcurrentSkipListSet<Task> _task_list = new ConcurrentSkipListSet<Task>(task_comparator);

    public static enum TaskType {
        MOVE,
        ATTACK,
        CAST,
        BUFF
    }

    public static class Task {
        public TaskType type;
        public L2Skill skill;
        public HardReference<? extends L2Character> task_target = HardReferences.emptyRef();
        public Location loc;
        public boolean pathfind;
        public boolean checkTarget = true;
        public int weight = TaskDefaultWeight;
    }


    protected static void buff(int id, int level, L2Playable playable) {
        if (id < 20) return;

        if (playable.isPlayer() && (playable.getLevel() < ConfigValue.BufferMinLevel || playable.getLevel() > ConfigValue.BufferMaxLevel)) {
            playable.sendMessage("Баффер доступен для игроков с уровней не ниже " + ConfigValue.BufferMinLevel + " и не выше " + ConfigValue.BufferMaxLevel + ".");
            return;
        }

        final double hp = playable.getCurrentHp();
        final double mp = playable.getCurrentMp();
        final double cp = playable.getCurrentCp();
        int buff_level = level > 0 ? level : SkillTable.getInstance().getBaseLevel(id);
        L2Skill skill = SkillTable.getInstance().getInfo(id, buff_level > 100 ? 1 : buff_level);
        if (buff_level > 100) {
            buff_level = SkillTreeTable.convertEnchantLevel(SkillTable.getInstance().getBaseLevel(id), buff_level, skill.getEnchantLevelCount());
            skill = SkillTable.getInstance().getInfo(id, buff_level);
        }

        if (!skill.checkSkillAbnormal(playable) && !skill.isBlockedByChar(playable, skill)) {
            for (EffectTemplate et : skill.getEffectTemplates()) {
                int result;
                Env env = new Env(playable, playable, skill);
                L2Effect effect = et.getEffect(env);
                if (effect != null && effect.getCount() == 1 && effect.getTemplate()._instantly && !effect.getSkill().isToggle()) {
                    // Эффекты однократного действия не шедулятся, а применяются немедленно
                    // Как правило это побочные эффекты для скиллов моментального действия
                    effect.onStart();
                    effect.onActionTime();
                    effect.onExit();
                } else if (effect != null && !effect.getEffected().p_block_buff.get()) {
                    if (ConfigValue.BufferTime > 0) effect.setPeriod(ConfigValue.BufferTime * 60000);
                    if ((result = playable.getEffectList().addEffect(effect)) > 0) {
                        if ((result & 2) == 2) playable.setCurrentHp(hp, false);
                        if ((result & 4) == 4) playable.setCurrentMp(mp);
                        if ((result & 8) == 8) playable.setCurrentCp(cp);
                    }
                }
            }
        }
        //skill.getEffects(playable, playable, false, false, ConfigValue.BBS_BUFFER_ALT_TIME * 60000, 0, false);
    }

    public void autoSoulshot() {
        int soul = 0;
        int spirit = 0;
        if (getActor().getActiveWeaponInstance() != null) {
            L2Item.Grade crystalType = getActor().getActiveWeaponInstance().getCrystalType();
            switch (crystalType) {
                case NONE:
                    break;
                case D:
                    break;
                case C:
                    soul = soulshot[0];
                    spirit = spiritshot[0];
                    break;
                case B:
                    soul = soulshot[1];
                    spirit = spiritshot[1];
                    break;
                case A:
                    soul = soulshot[2];
                    spirit = spiritshot[2];
                    break;
                case S:
                case S80:
                case S84:
                    soul = soulshot[3];
                    spirit = spiritshot[3];
                    break;
            }
        }

        getActor().addAutoSoulShot(soul);
        getActor().addAutoSoulShot(spirit);
        getActor().AutoShot();

    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    protected void ATTACKED(L2Character attacker, int damage, L2Skill skill) {
        if (attacker != null && !attacker.isDead()) {
            if (!targetList.contains(attacker)) {
                targetList.add(attacker);
                if (attacker.isMonster()) {
                    Functions.npcSay((L2NpcInstance) attacker, "add");
                }

            }
        }
        super.ATTACKED(attacker, damage, skill);
    }

    public class AttackTask extends RunnableImpl {

        public AttackTask() {
        }

        public void runImpl() {
            if (getActor() != null) {
                if (target != null) {
                    if (target.isDead()) {
                        targetList.remove(target);
                        target = getNextTarget();
                        getActor().setTarget(target);
                    } else {
                        attack();
                    }
                }
                if (getActor().getCurrentMp() < 100) {
                    getActor().setCurrentMp(getActor().getMaxMp());
                }
                updateTask();
            }
        }
    }

    public void updateParty(L2Player actor) {
        if (actor != null && actor.isInParty()) {
            for (L2Player player : actor.getParty().getPartyMembers()) {
                player.sendPacket(new PartySpelled(actor, true));
            }
        }
    }

    protected void updateTask() {
        //setLog("update");

        if (getActor() != null) {

            if (getActor().isSitting()) {
                getActor().setAI(new L2PlayerAI(getActor()));
                stopAiTask();
            }

            if (getActor().isInParty()) {
                updateParty(getActor());
                L2Party party = getActor().getParty();
                if (master == null || !party.containsMember(master)) {
                    master = party.getPartyLeader();
                    //getActor().setTitle("Master:" + master.getName());
                }
                if (master.getTarget() != null) {
                    L2Character masterTarget = (L2Character) master.getTarget();
                    if (!targetList.contains(masterTarget)) {
                        if (masterTarget.isMonster() && !masterTarget.isDead()) {
                            targetList.add(masterTarget);
                            //Functions.npcSay((L2NpcInstance) masterTarget, "masterTarget_add");

                        }
                    }
                }
                if (master.getReflection().getId() != getActor().getReflection().getId()) {
                    getActor().setLoc(master.getLoc());
                    getActor().setReflection(master.getReflection().getId());
                    getActor().spawnMe();
                }

                if (targetList.isEmpty() && getActor().getRealDistance3D(master) > 150) {
                    //getActor().isFollow = true;
                    partyFollowLoc = Location.coordsRandomize(master.getLoc(), 75, 125);
                    //getActor().getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, master, 100);
                    getActor().followToCharacter_v2(partyFollowLoc, master, 100, false);
                    //

                }

                if (!getActor().isInCombat() && delay < System.currentTimeMillis() - (Rnd.get(1000, 5000) + 10000)) {
                    getActor().moveToLocation(Location.coordsRandomize(master.getLoc(), 50, 200), 0, true);
                    follow = false;
                    delay = System.currentTimeMillis();
                }

            } else {
                master = null;
                GArray<L2Character> aroundCharacters = getActor().getAroundCharacters(_radiusNewTargetToList, 200);
                if (!aroundCharacters.isEmpty()) {
                    for (L2Character c : aroundCharacters) {
                        if (c.isMonster() && !c.isDead()) {
                            if (!c.isInCombat()) {
                                targetList.add(c);
                            }
                        }


                    }
                }
                if (target == null) {
                    target = getNextTarget();
                    getActor().setTarget(target);
                }
                if (!getActor().isInCombat() && targetList.isEmpty()) {
                    // setLog("moveToSpawnLoc");
                    getActor().moveToLocation(spawnLoc, 20, true);
                }
            }

            if (getActor().getActiveWeaponInstance() == null) {
                int[] disarmSkills = {485, 775, 794, 877, 5260};
                if (!getActor().getEffectList().containEffectFromSkills(disarmSkills)) {
                    getActor().getInventory().equipItem(activeWeaponInstance, true);
                }

            }
            if (getActor().isInTransaction()) {
                final Transaction transaction = getActor().getTransaction();
                transactionOtherPlayer = transaction.getOtherPlayer(getActor());

                if (getActor().getTransaction().isTypeOf(Transaction.TransactionType.PARTY)) {
                    L2Party party = transactionOtherPlayer.getParty();
                    getActor().joinParty(party);
                    getActor().setTitle(transactionOtherPlayer.getName(), false);
                    targetList.clear();
                    transaction.cancel();
                    transactionOtherPlayer = null;
                } else if (getActor().getTransaction().isTypeOf(Transaction.TransactionType.TRADE_REQUEST)) {
                    tradeTransaction = new Transaction(Transaction.TransactionType.TRADE, transactionOtherPlayer, getActor());
                    transactionOtherPlayer.sendPacket(new SystemMessage(SystemMessage.YOU_BEGIN_TRADING_WITH_C1).addString(getActor().getName()), new TradeStart(transactionOtherPlayer, getActor()));
                } else if (getActor().getTransaction().isTypeOf(Transaction.TransactionType.TRADE)) {
                    if (tradeTransaction.isConfirmed(transactionOtherPlayer)) {
                        tradeTransaction.confirm(getActor());
                        tradeTransaction.tradeItems();
                        transaction.cancel();
                        transactionOtherPlayer = null;
                    }
                } else if (getActor().getTransaction().isTypeOf(Transaction.TransactionType.CLAN)) {
                    transaction.cancel();
                } else if (getActor().getTransaction().isTypeOf(Transaction.TransactionType.FRIEND)) {
                    transaction.cancel();
                } else if (getActor().getTransaction().isTypeOf(Transaction.TransactionType.DUEL)) {
                    new Duel(transactionOtherPlayer, getActor(), false);
                    targetList.add(transactionOtherPlayer);
                    transaction.cancel();
                    transactionOtherPlayer = null;
                }

            }

            useSelf();

        } else {
            stopAiTask();
        }
    }

    private void useSelf() {
        L2Skill self = getSkill(_selfBuffSkillList);
        if (self != null) {

            if (getActor().getEffectList().getEffectBySkillId(self.getId()) == null) {
                getActor().getAI().Cast(self, getActor(), false, true);
                //disableSkill(self);
                setLog(self.getName() + " : " + (int) disableSkill(self));
                if (getActor().isInParty()) {
                    updateParty(getActor());
                }
            }
        }
    }

    private L2Skill getSkill(List<L2Skill> skillList) {
        Optional<L2Skill> skill = skillList.stream()
                .filter(o -> o.getWeaponDependancy(getActor()))
                .filter(o -> !_restrictedSkill.contains(o))
                .filter(o -> o.getNumCharges() <= getActor().getIncreasedForce())
                .filter(o -> getActor().getEffectList().getEffectBySkillId(o.getId()) == null)
                .filter(o -> !skillIsDisable(o))
                .findFirst();
        return skill.orElse(null);
    }

    public void stopAiTask() {
        if (getActor() != null) {
            setLog("stop AI");
        }
        if (_attackTask != null) {
            _attackTask.cancel(true);
        }
    }

    public class SelfBuffTask extends RunnableImpl {

        public SelfBuffTask() {
        }

        @Override
        public void runImpl() throws Exception {
            //List<L2Skill> _buffList = new ArrayList<L2Skill>();
            if (getActor() != null) {
                for (L2Skill skill : _selfBuffSkillList) {
                    L2Skill self = getActor().getKnownSkill(skill.getId());
                    if (getActor().getEffectList().getEffectBySkillId(self.getId()) == null) {
                        getActor().getAI().Cast(self, getActor(), false, true);
                        if (self.isToggle()) {
                            getActor().altUseSkill(self, getActor());
                        }
                        updateParty(getActor());
                    }
                }
            }


            //TODO: cleance для биша и камов
//            for (L2Effect e : bot.getEffectList().getAllEffects()) {
//                if (e.isOffensive() && e.getSkill().isCancelable() && e.getSkill().getId() != 2530 && e.getSkill().getId() != 5660 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 4215 && !_buffList.contains(e.getSkill()))
//                    _buffList.add(e.getSkill());
//            }
//            if (_buffList.size() > 0 && skillIsCD(Skills.SoulCleance)) {
//                L2Skill knownSkill = bot.getKnownSkill(Skills.SoulCleance.id);
//                bot.altUseSkill(knownSkill, bot);
//            }
        }

    }

   // public class


    @Override
    public void botSay(L2Player player, int _chatType, String text) {
        String s = aiTalk.sayInReturn(text, true, player, _chatType);
        int delay = s.length() * 100 + 1000;

        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                player.sendPacket(new Say2(getActor().getObjectId(), _chatType, "->" + getActor().getName(), s));

            }
        }, delay);
    }
}



