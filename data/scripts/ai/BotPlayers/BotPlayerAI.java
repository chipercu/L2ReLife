package ai.BotPlayers;

import l2open.common.RunnableImpl;
import l2open.common.ThreadPoolManager;
import l2open.config.ConfigValue;
import l2open.extensions.scripts.Functions;
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
import l2open.gameserver.skills.effects.EffectTemplate;
import l2open.gameserver.tables.SkillTable;
import l2open.gameserver.tables.SkillTreeTable;
import l2open.gameserver.templates.L2Item;
import l2open.util.GArray;
import l2open.util.GCSArray;
import l2open.util.Location;
import l2open.util.Rnd;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class BotPlayerAI extends L2PlayerAI {


    protected List<L2Skill> _rangeAttackSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _meleeAttackSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _buffSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _partyBuffSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _healSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _deDuffSkillList = new ArrayList<L2Skill>();
    protected List<L2Skill> _ultimateSkillList = new ArrayList<L2Skill>();

    protected Collection<L2Skill> allSkills;


    protected GCSArray<L2Skill> _disableSkill = new GCSArray<L2Skill>();
    protected List<L2Skill> _restrictedSkill;
    protected boolean isCatingNow = false;
    protected long coolTimeDelay = System.currentTimeMillis();
    private boolean enableSpiritShots = false;

    protected ScheduledFuture<?> _updateTask;
    protected ScheduledFuture<?> _attackTask;
    protected ScheduledFuture<?> _selfBuffTask;
    protected ScheduledFuture<?> _partyBuffTask;
    protected ScheduledFuture<?> _ultimateTask;


    long delay = System.currentTimeMillis();
    long enchantDelay = System.currentTimeMillis();

    private Location partyFollowLoc;
    private boolean follow = false;

    protected int _radiusNewTargetToList = 2500;

    protected BotAiTalk aiTalk;
    protected List<L2Character> targetList;
    protected Location spawnLoc;

    protected Transaction tradeTransaction;
    protected static final int _botTaskDelay = 250;
    protected static final int _updateTaskDelay = 500;
    protected static final int _buffTaskDelay = 1000;
    protected L2Character target;
    protected L2Player master;
    protected L2ItemInstance activeWeaponInstance;
    L2Player transactionOtherPlayer;


    protected static final int[] soulshot = {1464,   //Soulshot: C-grade
            1465,   //Soulshot: B-grade
            1466,   //Soulshot: A-grade
            1467,   //Soulshot: S-grade
    };
    protected static final int[] spiritshot = {3949,   //spiritshot: C-grade
            3950,   //spiritshot: B-grade
            3951,   //spiritshot: A-grade
            3952,   //spiritshot: S-grade
    };


    public BotPlayerAI(L2Character actor) {
        super((L2Player) actor);
        activeWeaponInstance = getActor().getActiveWeaponInstance();
        aiTalk = new BotAiTalk(getActor());
        getActor().resetSkillsReuse();
        getActor().getSkillReuseTimeStamps().clear();
//        if (getActor().isMageClass()) {
//            for (Buff buffId : BuffScheme.buffSchemes.get(1).getBuffIds()) {
//                buff(buffId.getId(), buffId.getLevel(), (L2Playable) actor);
//            }
//        } else {
//            for (Buff buffId : BuffScheme.buffSchemes.get(2).getBuffIds()) {
//                buff(buffId.getId(), buffId.getLevel(), (L2Playable) actor);
//            }
//        }
        fillAllSkillAvailable();
    }


    public void disableSkill(L2Skill skill) {
        _disableSkill.add(skill);
        long reuseDelay = skill.getReuseDelay();
        if (skill.getFlyType() == FlyToLocation.FlyType.CHARGE){
            reuseDelay = 2000L;
        }

        ThreadPoolManager.getInstance().schedule(() -> _disableSkill.remove(skill), getActor().isMageClass() ?  reuseDelay : skill.getReuseDelay() * 2);
    }

    public boolean skillIsDisable(L2Skill skill) {
        return _disableSkill.contains(skill);
    }

    protected L2Skill getTopSkill(Collection<L2Skill> skills) {

        Optional<L2Skill> first = Optional.empty();
        L2Skill skill = null;
        //Optional<L2Skill> first = skills.stream().sorted((o1, o2) -> (int) (o1.getPower() - o2.getPower())).reduce((o1, o2) -> o2);
        if (getActor() != null) {
             //first =
            List<L2Skill> collect = skills.stream()
                    .filter(o -> !o.isToggle() || !o.isPassive())
                    .filter(o -> o.getWeaponDependancy(getActor()))
                    .filter(o -> o.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
                    .filter(o -> !_restrictedSkill.contains(o))
                    .filter(o -> o.getNumCharges() <= getActor().getIncreasedForce())
                    .filter(o -> !skillIsDisable(o))
                    .sorted((o1, o2) -> (int) (o1.getPower() - o2.getPower()))
                    .collect(Collectors.toList());



            L2Skill rushSkill = null;

            for (L2Skill s: collect){
                if (s.getFlyType() == FlyToLocation.FlyType.CHARGE && getActor().getRealDistance3D(target) > 200 ){
                    rushSkill = s;
                    break;
                }
            }
            if (!collect.isEmpty()){
                if (collect.contains(rushSkill)){
                    skill = rushSkill;
                }else {
                    skill = collect.get(0);
                }
            }
        }
        return skill;
    }

    protected void fillAllSkillAvailable() {
        if (getActor() != null) {
            Collection<L2Skill> allSkills = getActor().getAllSkills();
            for (L2Skill skill : allSkills) {
                if (!skill.isPassive() && !skill.isToggle()) {
                    if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF) {
                        if (skill.getReuseDelay() > 3 * 60 * 1000) {
                            _ultimateSkillList.add(skill);
                        } else {
                            _buffSkillList.add(skill);
                        }
                    } else if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY) {
                        _partyBuffSkillList.add(skill);
                    } else if (//skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE &&
                            skill.getSkillType() != L2Skill.SkillType.HEAL) {
                        if (skill.getCastRange() > 300) {
                            _rangeAttackSkillList.add(skill);
                        } else {
                            _meleeAttackSkillList.add(skill);
                        }
                    }
                }
            }
        }
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
        Optional<L2Character> first= Optional.empty();
        if (getActor() != null && target == null && !targetList.isEmpty()){
            first = targetList.stream()
                    .filter(t -> !t.isDead())
                    .filter(t -> !t.getClass().getSimpleName().equalsIgnoreCase("TreasureChestInstance"))
                    .min((o1, o2) -> (int) (o1.getRealDistance3D(getActor()) - o2.getRealDistance3D(getActor())));
        }
        return first.orElse(null);
    }

//    public L2Character getNextTarget() {
//
//        L2Character new_target = null;
//        if (!targetList.isEmpty() && target == null && getActor() != null) {
//            //getActor().setTarget(newTarget);
//            double dist = 2500;
//
//            List<L2Character> new_targetList = targetList;
//
//            for (L2Character character : new_targetList) {
//                if (character != null) {
//                    if (character.isDead()){
//                        continue;
//                    }
//                    if (!character.isDead() || !GeoEngine.canSeeTarget(getActor(), target, false)) {
//                        double c_dist = getActor().getRealDistance3D(character);
//                        if (c_dist < dist) {
//                            dist = c_dist;
//                            new_target = character;
//                        }
//                    }
//                }
//            }
//        }
//        if (new_target != null) {
//            getActor().setTarget(new_target);
//        }
//        return new_target;
//    }

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


    private void attack() {
        if (getActor() != null && target != null && !target.isDead()) {
            autoSoulshot(getActor());
            double dist = getActor().getRealDistance3D(target);
            if(!getActor().isCastingNow()){

                L2Skill topSkill = getTopSkill(allSkills);

                if (topSkill != null) {
                    if (target.isMonster() || target.isNpc() || target.isRaid() || target.isBoss()) {
                        if (!getActor().isMageClass()){
                            if (dist > 200){

                                if (topSkill.getFlyType() == FlyToLocation.FlyType.CHARGE || topSkill.getCastRange() > dist){
                                    setLog("rushhhh");
                                    getActor().getAI().Cast(topSkill, target, false, false);
                                    //Attack(target, false, false);

                                }
                            }else {
                                getActor().getAI().Cast(topSkill, target, false, false);
                                //Attack(target, false, false);
                            }
                            //Attack(target, false, false);
                        }else {
                            getActor().getAI().Cast(topSkill, target, false, false);
                        }

                        disableSkill(topSkill);
                        setLog(topSkill.getName());

//                    getActor().getAI().Cast(topSkill, target, false, false);
//                    disableSkill(topSkill);
//                    setLog(topSkill.getName());
                    } else if (target.isPlayer()) {
                        if (!getActor().getDuel().isFinished()) {
                        }
                    }
                }else {
//                    if (GeoEngine.canSeeTarget(getActor(), target, false)){
//                        setLog("see");
//                    }else {
//                        setLog("dont see");
//                    }

                    if (GeoEngine.canMoveToCoord(getActor().getX(), getActor().getY(), getActor().getZ(), target.getX(), target.getY(), target.getZ(), getActor().getGeoIndex())){
                        setLog("move true");
                    }else {
                        setLog("move false");
                    }
//                    if (GeoEngine.MoveCheck()) {
//                        setLog("move true");
//                    } else {
//                        setLog("move false");
//                    }


                    if (dist > 40){
                        //setLog("move");
                        getActor().moveToLocation(GeoEngine.moveCheck(getActor().getX(), getActor().getY(), getActor().getZ(), target.getX(), target.getY(), getActor().getGeoIndex()), 20, true);
                    }else {
                        getActor().getAI().Attack(target, false, false);
                    }

                }
            }else if (dist > 200){
                setLog(String.valueOf(dist));
                getActor().moveToLocation(target.getLoc(), 20, true);
            }



        }
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

    public void autoSoulshot(L2Player bot) {
        L2Item.Grade crystalType;
        int soul = 0;
        int spirit = 0;
        if (bot.getActiveWeaponInstance() != null) {
            crystalType = bot.getActiveWeaponInstance().getCrystalType();
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

        if (bot.getInventory().getItemByItemId(soul) != null) {
            IItemHandler handler = ItemHandler.getInstance().getItemHandler(soul);
            handler.useItem(bot, bot.getInventory().getItemByItemId(soul), false);
        }
        if (bot.getInventory().getItemByItemId(spirit) != null) {
            IItemHandler handler = ItemHandler.getInstance().getItemHandler(spirit);
            handler.useItem(bot, bot.getInventory().getItemByItemId(spirit), false);
            enableSpiritShots = true;
        } else {
            enableSpiritShots = false;
        }
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


                if (!getActor().isInCombat() && enchantDelay < System.currentTimeMillis() - (Rnd.get(1000, 5000) + 10000)) {
                    getActor().getActiveWeaponInstance();
                    enchantDelay = System.currentTimeMillis();
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
                if (target == null){
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


        } else {
            stopAiTask();
        }
    }

    public void stopAiTask() {
        if (getActor() != null) {
            setLog("stop AI");
        }
        if (_attackTask != null) {
            _attackTask.cancel(true);
        }
        if (_selfBuffTask != null) {
            _selfBuffTask.cancel(true);
        }
        if (_partyBuffTask != null) {
            _partyBuffTask.cancel(true);
        }
        if (_ultimateTask != null) {
            _ultimateTask.cancel(true);
        }
        if (_updateTask != null) {
            _updateTask.cancel(true);
        }
    }


    public class SelfBuffTask extends RunnableImpl {

        public SelfBuffTask() {
        }

        @Override
        public void runImpl() throws Exception {
            //List<L2Skill> _buffList = new ArrayList<L2Skill>();
            if (getActor() != null) {
                for (L2Skill skill : _buffSkillList) {
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


    @Override
    public void botSay(L2Player player, int _chatType, String text) {
        String s = aiTalk.sayInReturn(text, true, player, _chatType);
        int delay = s.length() * 100 + 1000;

        ThreadPoolManager.getInstance().schedule(() -> player.sendPacket(new Say2(getActor().getObjectId(), _chatType, "->" + getActor().getName(), s)), delay);
    }
}



