package npc.model.KetraVarkaWar;

import l2open.extensions.scripts.Functions;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.instances.L2MonsterInstance;
import l2open.gameserver.templates.L2NpcTemplate;

public class VarkaAttackerInstance extends L2MonsterInstance {

    private static final String titleKetra = "Лагерь Кетра";
    private static final int recruitId = 36656;
    private static final int footmanId = 36657;

    public VarkaAttackerInstance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
       // this.setAI(new VarkaAttackerAI(this));
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
    public boolean isAutoAttackable(L2Character attacker) {
        if (attacker == null) {
            return false;
        }
        L2Player player = attacker.getPlayer();
        if (player == null) {
            return false;
        }
        return player.getTeam() == 2;
    }

    @Override
    public boolean isAttackable(L2Character attacker) {
        return isAutoAttackable(attacker);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
//        Functions.spawn(Location.coordsRandomize(getLoc(), 50, 100), recruitId,0, getReflectionId());
//        Functions.spawn(Location.coordsRandomize(getLoc(), 50, 100), recruitId,0, getReflectionId());
//        Functions.spawn(Location.coordsRandomize(getLoc(), 50, 100), footmanId,0, getReflectionId());
//        Functions.spawn(Location.coordsRandomize(getLoc(), 50, 100), footmanId,0, getReflectionId());

    }
}
