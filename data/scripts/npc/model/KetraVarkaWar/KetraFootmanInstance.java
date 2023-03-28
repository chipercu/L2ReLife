package npc.model.KetraVarkaWar;

import l2open.common.ThreadPoolManager;
import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.instances.L2MonsterInstance;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.templates.L2NpcTemplate;
import l2open.util.GArray;
import l2open.util.Rnd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;

public class KetraFootmanInstance extends L2MonsterInstance {

    private L2Character character;
    private ScheduledFuture<?> _actionTask;
    private static final Integer[] mobsId =  {36650, 36651, 36652, 36653, 36654, 36655, 36656, 36657};
    private static final ArrayList<Integer> varkaMobs = new ArrayList<Integer>(Arrays.asList(mobsId));


    public KetraFootmanInstance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
        //this.setAI(new KetraAttackerAI(this));
    }



    @Override
    public boolean isInvul() {
        return false;
    }

    @Override
    public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean isDot, double i2, boolean sendMesseg, boolean bow, boolean crit, boolean tp) {
        super.reduceCurrentHp(2000, attacker, skill, awake, standUp, directHp, canReflect, isDot, i2, sendMesseg, bow, crit, tp);
    }


    @Override
    public boolean isAutoAttackable(L2Character attacker)
    {
        if(attacker == null){
            return false;
        }
        L2Player player = attacker.getPlayer();
        if(player == null){
            return false;
        }
        return player.getTeam() == 1;
        //return "FuzzY".equalsIgnoreCase(player.getName());
    }

    @Override
    public boolean isAttackable(L2Character attacker)
    {
        return isAutoAttackable(attacker);
    }

//    @Override
//    public void onSpawn() {
//        _actionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ActionTask(this),500, 500);
//        super.onSpawn();
//    }

    @Override
    public void onDecay() {
        if (_actionTask != null){
            _actionTask.cancel(true);
        }
        character = null;
        super.onDecay();
    }

    public class ActionTask extends l2open.common.RunnableImpl{
        L2NpcInstance actor;

        public ActionTask(L2NpcInstance actor) {
            this.actor = actor;
        }

        private void attackAction(L2Character target, L2NpcInstance actor){
            if (target != null) {
                target.addDamageHate(actor, 0, Rnd.get(1, 100)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
            }
            actor.setRunning();
            actor.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
            actor.getAI().setAttackTarget(target); // На всякий случай, не обязательно делать
            actor.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null); // Переводим в состояние атаки
            actor.getAI().addTaskAttack(target); // Добавляем отложенное задание атаки, сработает в самом конце движения
        }

        @Override
        public void runImpl() throws Exception {
            final GArray<L2Character> aroundCharacters = actor.getAroundCharacters(1000, 200);

            if (character == null && aroundCharacters.get(0) != null){
                for (L2Character cha: aroundCharacters){
                    if (cha.isPlayer() && cha.getTeam() == 1){
                        character = cha;
                        attackAction(cha, actor);
                        break;
                    }else if (varkaMobs.contains(cha.getNpcId())){
                        character = cha;
                        attackAction(cha, actor);
                        break;
                    }
                }
            }else if (character.isDead()){
                character = null;
            }
        }
    }

}
