package npc.model.KetraVarkaWar;

import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.instances.L2MonsterInstance;
import l2open.gameserver.templates.L2NpcTemplate;

public class KetraSoldierInstance extends L2MonsterInstance {

    public KetraSoldierInstance(int objectId, L2NpcTemplate template) {
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



}
