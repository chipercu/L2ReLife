package ai.Fort.AI2;

import l2open.common.RunnableImpl;
import l2open.common.ThreadPoolManager;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.UnitLoc;
import l2open.gameserver.model.base.UnitType;

public class UnitAI extends CommonAI {
    private L2Player master;
    private UnitType type;

    public UnitAI(L2Player actor) {
        super(actor);
        if (getActor() != null){
            aiTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AiTask(), 250, 250);
        }
    }




    private class AiTask extends RunnableImpl {

        @Override
        public void runImpl() throws Exception {
            if (maybeCancelAI()){
                return;
            }
            UnitLoc formationLoc = getActor().getFormationLoc();
            if (formationLoc != null && !getActor().isInRange(formationLoc.getLoc(), 25)){
                getActor().moveToLocation(formationLoc.getLoc(), 0, true);
            }
        }

    }

    @Override
    protected void MY_DYING(L2Character killer) {
        getActor().getFormationLoc().setFree().setUnit(null);
        super.MY_DYING(killer);
    }


    //    public UnitLoc getFormationLoc() {
//        return formationLoc;
//    }
//
//    public void setFormationLoc(UnitLoc formationLoc) {
//        this.formationLoc = formationLoc;
//    }

    public L2Player getMaster() {
        return master;
    }

    public void setMaster(L2Player master) {
        this.master = master;
    }

    public UnitType getType() {
        return type;
    }

    public void setType(UnitType type) {
        this.type = type;
    }
}
