package ai.Fort.AI2;

import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.util.Location;

public interface Commander {
    void addUnit(Unit observer);
    void removeUnit(Unit observer);

    void notifyMove(FormationType formationType, Location ref, Location direction);

    void notifyAttack(L2Character target);

    void notifyCastSkill(L2Character target);

    void setMaster(L2Player master);



}
