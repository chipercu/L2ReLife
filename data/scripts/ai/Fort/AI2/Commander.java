package ai.Fort.AI2;

import l2open.util.Location;

public interface Commander {
    void addUnit(Unit observer);
    void removeUnit(Unit observer);

    void notifyMove(FormationType formationType, Location ref, Location direction);



}
