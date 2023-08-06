package ai.Fort.AI2;

import l2open.common.RunnableImpl;
import l2open.common.ThreadPoolManager;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.UnitLoc;
import l2open.gameserver.model.base.UnitType;
import l2open.gameserver.skills.DocumentSkill;
import l2open.gameserver.tables.SkillTable;
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
        Location loc = Location.coordsRandomize(ref, 100, 200);
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
                if (getMaster() != null && getMaster().getUnitsCount() > 0){
                    int middle = getMaster().getUnitsCount() / 2;
                    if (formationPosition > middle){
                        int dist = (formationPosition - middle) * 25;
                        loc = findNewLoc2(ref, direction, Side.BACK, Side.RIGHT, 50, dist);
                    }else {
                        int dist = ((middle + 1) - formationPosition) * -25;
                        loc = findNewLoc2(ref, direction, Side.BACK, Side.LEFT, 50, dist);
                    }
                }
                break;
            }
            case FRONT_FALANG:{
                if (getMaster() != null && getMaster().getUnitsCount() > 0){
                    int middle = getMaster().getUnitsCount() / 2;
                    if (formationPosition > middle){
                        int dist = (formationPosition - middle) * 25;
                        loc = findNewLoc2(ref, direction, Side.FRONT, Side.RIGHT, 50, dist);
                    }else {
                        int dist = ((middle + 1) - formationPosition) * -25;
                        loc = findNewLoc2(ref, direction, Side.FRONT, Side.LEFT, 50, dist);
                    }
                }
                break;
            }
        }
        getActor().moveToLocation(loc, 0, true);
    }

    private class AiTask extends RunnableImpl {

        @Override
        public void runImpl() throws Exception {
            if (maybeCancelAI()){
                return;
            }
            switch (stateType){
                case ATTACK:{

                    break;
                }
            }


        }

    }

    @Override
    protected void MY_DYING(L2Character killer) {
        getActor().getFormationLoc().setFree().setUnit(null);
        super.MY_DYING(killer);
    }

    public L2Player getMaster() {
        return master;
    }

    public void setMaster(L2Player master) {
        this.master = master;
    }

    @Override
    public void attack(L2Character target, boolean dontMove) {
        Attack(target, true, dontMove);
    }

    @Override
    public void castSkill(L2Character target, boolean dontMove) {
        L2Skill skill = SkillTable.getInstance().getInfo(101, 1); //todo реализовать выбор скилов
        Cast(skill, target, true, dontMove);
    }

    public UnitType getType() {
        return type;
    }

    public void setType(UnitType type) {
        this.type = type;
    }
}
