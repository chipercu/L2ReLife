package ai.KetraVarkaAI;

import l2open.common.ThreadPoolManager;
import l2open.extensions.scripts.Functions;
import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.ai.Fighter;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.util.GArray;
import l2open.util.Rnd;

import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;

public class KetraGuardAI extends Fighter {
    private static final Integer[] enemyNpcID = {36650, 36651, 36652, 36653, 36654, 36655, 36656, 36657};
    private static final int enemyNpcID1 = 36650;
    private static final int enemyNpcID2 = 36651;
    private static final int enemyNpcID3 = 36652;
    private static final int enemyNpcID4 = 36653;
    private static final int enemyNpcID5 = 36654;
    private static final int enemyNpcID6 = 36655;
    private static final int enemyNpcID7 = 36656;
    private static final int enemyNpcID8 = 36657;
    private static final int enemyTeam = 1;
    private L2Character character = null;
    private ScheduledFuture<?> _actionTask;
    private long _timer = 0;

    public KetraGuardAI(L2Character actor) {
        super(actor);
    }

    public static void debuger(L2NpcInstance actor, String txt) {
        Functions.npcSay(actor, txt);
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    @Override
    public void checkAggression(L2Character target) {
        if (target == null || target.getTeam() != enemyTeam){
            return;
        }
        if (target.isNpc() && !Arrays.asList(enemyNpcID).contains(target.getNpcId())){
            return;
        }
        if (character != null){
            return;
        }
        super.checkAggression(target);
    }

    @Override
    protected void onEvtSpawn() {
        getActor().startRandomAnimation();
        getActor().setRunning();
        _actionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ActionTask(getActor()), 1000, 1000);
        super.onEvtSpawn();
    }
    public class ActionTask extends l2open.common.RunnableImpl {

        L2NpcInstance actor;



        public ActionTask(L2NpcInstance actor) {
            this.actor = actor;
        }

        @Override
        public void runImpl() throws Exception {
            if (actor.isDead() || actor.isDecayed()){
                return;
            }
            final GArray<L2Character> aroundCharacters = actor.getAroundCharacters(900, 300);
            //debuger(actor, character != null ? character.getName() : "null");
            if (character != null) {
                if (character.isNpc() && character.isDead()) {
//                    int npcId = character.getNpcId();
//                    if (npcId == 36649 || npcId == 36648 || npcId == 36647) {
//                        character = null;
//                        stopAITask();
//                        clearTasks();
//                        clientStopMoving();
//                        return;
//                    }
                    character = null;
                    stopAITask();
                    clearTasks();
                    clientStopMoving();
                    return;
                } else if (character.isPlayer()) {
                    L2Player player = (L2Player) character;
                    if (player.getTeam() != enemyTeam || player.isInvisible() || player.isDead() || actor.getDistance(player) > 1200) {
                        character = null;
                        stopAITask();
                        clearTasks();
                        clientStopMoving();
                       // Functions.npcSayToPlayer(getActor(), player, "stopAI");
                        return;
                    }
                }
                character.addDamageHate(actor, 0, Rnd.get(50, 100)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
                actor.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
                actor.getAI().setAttackTarget(character); // На всякий случай, не обязательно делать
                actor.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, character, null); // Переводим в состояние атаки
                // actor.getAI().addTaskAttack(character); // Добавляем отложенное задание атаки, сработает в самом конце движения
                //doTask();

            } else {
                if (aroundCharacters.size() > 0) {
                    //L2Character _char = aroundCharacters.get(Rnd.get(aroundCharacters.size()));
                    for (L2Character _char : aroundCharacters) {
                        if (_char.isNpc()) {
                            int npcId = _char.getNpcId();
                            if (npcId == enemyNpcID1 || npcId == enemyNpcID2 || npcId == enemyNpcID3
                                    || npcId == enemyNpcID4 || npcId == enemyNpcID5 || npcId == enemyNpcID6
                                    || npcId == enemyNpcID7 || npcId == enemyNpcID8) {
                                if (!_char.isDead()){
                                    character = _char;
                                    getActor().setTarget(_char);
                                    startAITask();
                                    clearTasks();
                                    clientStopMoving();
                                    character.addDamageHate(actor, 0, Rnd.get(50, 100)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
                                    actor.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
                                    actor.getAI().setAttackTarget(character); // На всякий случай, не обязательно делать
                                    actor.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, character, null); // Переводим в состояние атаки
                                    // actor.getAI().addTaskAttack(character); // Добавляем отложенное задание атаки, сработает в самом конце движения
                                    //doTask();

                                    break;
                                }
                            }
                        } else if (_char.isPlayer()) {
                            L2Player player = (L2Player) _char;
                            if (player.getTeam() == enemyTeam && !player.isInvisible() && !player.isDead()) {
                                character = player;
                                getActor().setTarget(player);
                               // Functions.npcSayToPlayer(getActor(), player, getActor().getTarget().getName());
                                startAITask();
                                clearTasks();
                                clientStopMoving();
                                character.addDamageHate(actor, 0, Rnd.get(50, 100)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
                                actor.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
                                actor.getAI().setAttackTarget(character); // На всякий случай, не обязательно делать
                                actor.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, character, null); // Переводим в состояние атаки
                                // actor.getAI().addTaskAttack(character); // Добавляем отложенное задание атаки, сработает в самом конце движения
                                //doTask();

                                break;
                            }
                        }

                    }
                }
                if (character == null && System.currentTimeMillis() - _timer >= 3000){
                    returnHome(true);
                    _timer = System.currentTimeMillis();
                }
            }
        }
    }
}
