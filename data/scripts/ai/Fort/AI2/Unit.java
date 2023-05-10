package ai.Fort.AI2;

import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.util.Location;

public interface Unit {
    void move(FormationType formationType, Location ref, Location direction);

    void setMaster(L2Player player);

    void attack(L2Character target, boolean dontMove);

    void castSkill(L2Character target, boolean dontMove);

}
