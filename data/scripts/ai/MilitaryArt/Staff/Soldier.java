package ai.MilitaryArt.Staff;

import ai.MilitaryArt.BattleOrderType;
import l2open.gameserver.ai.L2PlayerAI;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.instances.L2NpcInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Soldier extends L2PlayerAI {


    private BattleOrderType battleOrderType;
    private List<L2NpcInstance> soldiers = new ArrayList<>();
    private final Map<Integer, Integer> soldiersIDAndCount = new HashMap<>();


    public Soldier(L2Character actor) {
        super((L2Player) actor);



    }



    public List<L2NpcInstance> getSoldiers() {
        return soldiers;
    }

    public void setSoldiers(List<L2NpcInstance> soldiers) {
        this.soldiers = soldiers;
    }

    @Override
    protected void MY_DYING(L2Character killer) {
        super.MY_DYING(killer);
    }
}
