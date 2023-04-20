package ai.MilitaryArt;

import l2open.extensions.scripts.Functions;
import l2open.gameserver.ai.Fighter;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.instances.L2NpcInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ai.MilitaryArt.BattleOrderType.*;

public class Squad extends Fighter {


    private BattleOrderType battleOrderType;
    private List<L2NpcInstance> soldiers = new ArrayList<>();
    private final Map<Integer, Integer> soldiersIDAndCount = new HashMap<>();
    private MilitaryRank rank;

    public Squad(L2Character actor) {
        super(actor);
        this.battleOrderType = RANDOM;
        spawnSoldiers();


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

    @Override
    protected void MY_DYING(L2Character killer) {
        super.MY_DYING(killer);
    }

    private void spawnSoldiers(){

        for (Map.Entry<Integer, Integer> entry: this.soldiersIDAndCount.entrySet()){
            for (int i = 0; i < entry.getValue(); i++){
                this.soldiers.add(Functions.spawn(getActor().getLoc(), entry.getKey()));
            }
        }
    }


}