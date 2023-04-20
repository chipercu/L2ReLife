package ai.MilitaryArt.Staff;

import ai.MilitaryArt.MilitaryRank;
import l2open.gameserver.ai.DefaultAI;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.instances.L2NpcInstance;

public class Common extends DefaultAI {

    private MilitaryRank rank;
    private L2NpcInstance commander;

    public Common(L2Character actor) {
        super(actor);
    }




    public MilitaryRank getRank() {
        return rank;
    }

    public void setRank(MilitaryRank rank) {
        this.rank = rank;
    }

    public L2NpcInstance getCommander() {
        return commander;
    }

    public void setCommander(L2NpcInstance commander) {
        this.commander = commander;
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
