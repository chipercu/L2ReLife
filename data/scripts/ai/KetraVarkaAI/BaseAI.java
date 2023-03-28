package ai.KetraVarkaAI;

import l2open.gameserver.ai.DefaultAI;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.L2World;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.serverpackets.NpcInfo;

public class BaseAI extends DefaultAI {


    public BaseAI(L2Character actor) {
        super(actor);
    }



    @Override
    protected boolean randomAnimation() {
        return false;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    @Override
    protected void ATTACKED(L2Character attacker, int damage, L2Skill skill) {
        final L2NpcInstance actor = getActor();
        for(L2Player _cha : L2World.getAroundPlayers(actor)){
            _cha.sendPacket(new NpcInfo(actor, _cha));
        }
        attacker.sendMessage((int)getActor().getCurrentHp() + " HP");
//        if (Rnd.chance(5)){
//            attacker.reduceCurrentHp(attacker.getMaxHp() + attacker.getMaxCp() + 1, actor, null, true, true, false, false, true, attacker.getMaxHp() + attacker.getMaxCp() + 1, true, false, false, false);
//        }
    }
}
