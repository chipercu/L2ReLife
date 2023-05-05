package ai.Fort.AI2;

import l2open.gameserver.model.L2Player;
import l2open.util.Location;

public interface Unit {
    void move(FormationType formationType, Location ref, Location direction);
}
