package ai.Fort.MilitaryAI;

import l2open.database.mysql;
import l2open.gameserver.model.L2Player;
import l2open.util.GArray;

import java.util.HashMap;

public abstract class MilitaryDefaultAI {

    protected abstract void move();
    protected abstract void cast();
    protected abstract void attack();

}
