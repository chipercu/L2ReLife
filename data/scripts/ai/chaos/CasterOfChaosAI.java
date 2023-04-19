package ai.chaos;

import l2open.common.RunnableImpl;
import l2open.common.ThreadPoolManager;
import l2open.extensions.scripts.Functions;
import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.ai.Fighter;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Object;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.L2World;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.tables.SkillTable;
import l2open.util.GArray;
import l2open.util.Rnd;

import java.util.concurrent.ScheduledFuture;

public class CasterOfChaosAI extends Fighter {

    private long _lastAttackTime = 0;
    private static final long NextAttack = 15 * 1000; // 120 сек
    private L2NpcInstance monster;
    private boolean missionStatus = false;
    private ScheduledFuture<?> scheduledFuture;
    private ScheduledFuture<?> moveTaskToMonster;

    public CasterOfChaosAI(L2Character actor) {
        super(actor);


    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

//    @Override
//    protected boolean thinkActive() {
//        L2NpcInstance actor = getActor();
//        if (actor == null)
//            return true;
////todo: сделать ожидание перед кастом
//        Functions.npcSay(getActor(), "qwer");
//
//
//        if (_lastAttackTime + (NextAttack + Rnd.get(5000, 10000)) < System.currentTimeMillis()) {
//            if (missionStatus) {
//                Functions.npcSay(getActor(), "Пока неудачники, вам всем наступит каюк");
//                Functions.npcSay(getActor(), "ЫхЫхЫХЫХЫХ");
//                getActor().abortAttack(true, false);
//                getActor().abortCast(true);
//                getActor().doDie(getActor());
//                missionStatus = false;
//            }
//            if (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) {
//                if (monster == null) {
//                    final GArray<L2NpcInstance> aroundNpc = L2World.getAroundNpc(actor, 2000, 300);
//                    if (!aroundNpc.isEmpty()) {
//                        monster = aroundNpc.get(0);
//                        Functions.npcSay(actor, "Цель " + monster.getName());
//                    }
//                }
//            }
//            if (monster != null) {
//                actor.setTarget(monster);
//                actor.moveToLocation(monster.getLoc(), 200, true);
//                _lastAttackTime = System.currentTimeMillis();
//            }
//        }
//
//        if (monster!= null && actor.getRealDistance3D(monster) < 300 && !missionStatus){
//            final L2Skill skill = SkillTable.getInstance().getInfo(5910, 1);
////            actor.getAI().Cast(SkillTable.getInstance().getInfo(5910, 1), monster, true, true);
//            getActor().getAI().Cast(skill, monster, false, false);
//            missionStatus = true;
//            Functions.npcSay(actor, "Миссия выполнена");
//        }
//        return true;
//    }


    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();

        ThreadPoolManager.getInstance().scheduleAI(new MonsterFinder(), 2000);

        moveTaskToMonster = ThreadPoolManager.getInstance().scheduleAtFixedRate(new MoveToMonster(), 5000, 5000);

        scheduledFuture = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
            if (monster!= null && getActor().getRealDistance3D(monster) < 300 && !missionStatus){
                getActor().getAI().clearTasks();
                final L2Skill skill = SkillTable.getInstance().getInfo(5910, 1);

                if (!checkMonster()){
                    getActor().doCast(skill, monster, true);
                    if (!monster.isChaos()){
                        monster.setChaos();
                        missionStatus = true;
                    }
                    Functions.npcSay(getActor(), "Миссия выполнена");
                    if (monster.isChaos()){
                        scheduledFuture.cancel(true);
                        if (scheduledFuture.isCancelled()){
                            Functions.npcSay(getActor(), "задача остановлена");
                        }
                    }
                }else {
                    ThreadPoolManager.getInstance().scheduleAI(new MonsterFinder(), 2000);
                    if (moveTaskToMonster == null){
                        moveTaskToMonster = ThreadPoolManager.getInstance().scheduleAtFixedRate(new MoveToMonster(), 5000, 5000);
                    }
                }

            }
        }, 10000, 3000);
        Functions.npcSay(getActor(), "AI Start");
    }

    private boolean checkMonster(){
        return monster.isChaos() || monster.isDead() || !monster.getAggroList().isEmpty();
    }

    @Override
    protected boolean maybeMoveToHome() {
        return false;
    }

    @Override
    public boolean isNotReturnHome() {
        return true;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    @Override
    protected void MY_DYING(L2Character killer) {
        if (moveTaskToMonster != null){
            moveTaskToMonster.cancel(true);
            moveTaskToMonster = null;
        }
        if (scheduledFuture != null){
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }

        super.MY_DYING(killer);
    }

    class MoveToMonster extends RunnableImpl{

        @Override
        public void runImpl() throws Exception {
            if (checkMonster()){
                ThreadPoolManager.getInstance().scheduleAI(new MonsterFinder(), 2000);
                return;
            }
            if (monster != null && getActor().getRealDistance3D(monster) > 300) {
                getActor().setTarget(monster);
                getActor().moveToLocation(monster.getLoc(), 200, true);
            }
            if (getActor().getRealDistance3D(monster) < 300){
                moveTaskToMonster.cancel(true);
                moveTaskToMonster = null;
            }
        }
    }


    class MonsterFinder extends RunnableImpl {

        @Override
        public void runImpl() throws Exception {
            if (monster == null){

                monster = getTarget();
                while (checkMonster()){
                    monster = getTarget();
                }
                Functions.npcSay(getTarget(), "target - " + monster.getName());
            }
        }
        private L2NpcInstance getTarget(){
            return L2World.getAroundNpc(getActor(), 2000, 300).stream()
                    .filter(L2Object::isMonster)
                    .filter(o -> !o.isAlikeDead())
                    .filter(o -> !o.isChaos())
                    .filter(o -> o.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
                    .findFirst().orElse(null);
//            final GArray<L2NpcInstance> aroundNpc = L2World.getAroundNpc(getActor(), 2000, 300);
//            return aroundNpc.get(Rnd.get(aroundNpc.size()));
        }


    }



}