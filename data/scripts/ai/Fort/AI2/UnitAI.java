package ai.Fort.AI2;

import l2open.common.RunnableImpl;
import l2open.common.ThreadPoolManager;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.UnitLoc;
import l2open.gameserver.model.base.UnitType;
import l2open.util.Location;
import l2open.util.geometry.Vector.Side;

public class UnitAI extends CommonAI implements Unit{
    private L2Player master;
    private UnitType type;
    private int formationPosition;

    public UnitAI(L2Player actor) {
        super(actor);
        if (getActor() != null){
            aiTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AiTask(), 250, 250);
        }
    }

    public int getFormationPosition() {
        return formationPosition;
    }

    public void setFormationPosition(int formationPosition) {
        this.formationPosition = formationPosition;
    }

    @Override
    public void move(FormationType formationType, Location ref, Location direction) {
        Location loc;
        switch (formationType){
            case TWO_COLUMN:{
                if (getFormationPosition()%2 == 0){
                    loc = findNewLoc2(ref, direction, Side.BACK, Side.LEFT, 50 * formationPosition, 25);
                }else {
                    loc = findNewLoc2(ref, direction, Side.BACK, Side.RIGHT, 50 * formationPosition, 25);
                }
                break;
            }
            case COLUMN: {
                loc = findNewLoc2(ref, direction, Side.BACK, Side.LEFT, 50 * formationPosition, 0);
                break;
            }
            case BACK_FALANG:{
                loc = findNewLoc2()



            }
            default: loc = Location.coordsRandomize(ref, 100, 200);
        }
        getActor().moveToLocation(loc, 0, true);






    }

//    @Override
//    public void move(Location location) {
//        if (getActor() != null && !getActor().isInRange(location, 25)){
//            getActor().moveToLocation(location, 0, true);
//        }
//
//    }


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
