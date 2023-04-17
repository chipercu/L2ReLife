package ai.chaos;

import l2open.extensions.scripts.Functions;
import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.ai.Fighter;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.L2World;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.tables.SkillTable;
import l2open.util.GArray;
import l2open.util.Location;
import l2open.util.Rnd;

public class CasterOfChaosAI extends Fighter {

    private long _lastAttackTime = 0;
    private static final long NextAttack = 15 * 1000; // 120 сек
    private L2NpcInstance monster;
    private boolean missionStatus = false;

    public CasterOfChaosAI(L2Character actor) {
        super(actor);
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    protected boolean thinkActive() {
        L2NpcInstance actor = getActor();
        if (actor == null)
            return true;
//todo: сделать ожидание перед кастом


        if (_lastAttackTime + (NextAttack + Rnd.get(5000, 10000)) < System.currentTimeMillis()) {
            if (missionStatus) {
                Functions.npcSay(getActor(), "Пока неудачники, вам всем наступит каюк");
                Functions.npcSay(getActor(), "ЫхЫхЫХЫХЫХ");
                getActor().abortAttack(true, false);
                getActor().abortCast(true);
                getActor().doDie(getActor());
                missionStatus = false;
            }
            if (getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) {
                if (monster == null) {
                    final GArray<L2NpcInstance> aroundNpc = L2World.getAroundNpc(actor, 2000, 300);
                    if (!aroundNpc.isEmpty()) {
                        monster = aroundNpc.get(0);
                        Functions.npcSay(actor, "Цель " + monster.getName());
                    }
                }
            }
            if (monster != null) {
                actor.setTarget(monster);
                actor.moveToLocation(monster.getLoc(), 200, true);
                _lastAttackTime = System.currentTimeMillis();
            }
        }

        if (monster!= null && actor.getRealDistance3D(monster) < 300 && !missionStatus){
            final L2Skill skill = SkillTable.getInstance().getInfo(5910, 1);
//            actor.getAI().Cast(SkillTable.getInstance().getInfo(5910, 1), monster, true, true);
            getActor().getAI().Cast(skill, monster, false, false);
            missionStatus = true;
            Functions.npcSay(actor, "Миссия выполнена");
        }
        return true;
    }


    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        Functions.npcSay(getActor(), "Spawn - mission status:" + missionStatus );
        getActor().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
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
}