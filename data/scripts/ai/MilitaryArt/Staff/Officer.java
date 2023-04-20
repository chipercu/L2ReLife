package ai.MilitaryArt.Staff;

import ai.MilitaryArt.BattleOrderType;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.instances.L2NpcInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ai.MilitaryArt.MilitaryRank.OFFICER;
import static ai.MilitaryArt.MilitaryRank.SOLDIER;

public class Officer extends Common {


    private BattleOrderType battleOrderType;
    private List<L2NpcInstance> soldiers = new ArrayList<>();
    private final Map<Integer, Integer> soldiersIDAndCount = new HashMap<>();


    public Officer(L2Character actor) {
        super(actor);
        setRank(OFFICER);


    }



    public List<L2NpcInstance> getSoldiers() {
        return soldiers;
    }

    public void setSoldiers(List<L2NpcInstance> soldiers) {
        this.soldiers = soldiers;
    }


    @Override
    public boolean isGlobalAI() {
        return true;
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
