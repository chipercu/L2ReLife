package ai.Fort.AI2;

import l2open.gameserver.ai.L2PlayerAI;
import l2open.gameserver.model.L2ObjectsStorage;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.UnitLoc;
import l2open.gameserver.serverpackets.Say2;
import l2open.util.Location;
import l2open.util.geometry.Vector.Point2D;
import l2open.util.geometry.Vector.PointNSWE2P;
import l2open.util.geometry.Vector.Side;
import l2open.util.geometry.Vector.Vector2P;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class CommonAI extends L2PlayerAI {


    
    protected ScheduledFuture<?> aiTask;
    protected StateType stateType;
    
    public CommonAI(L2Player actor) {
        super(actor);
    }

    public void say(String text) {
        Say2 cs = new Say2(getActor().getObjectId(), 16, getActor().getName(), text);
        for (L2Player player : L2ObjectsStorage.getPlayers()) {
            player.sendPacket(cs);
        }
    }

    protected boolean maybeCancelAI(){
        if (getActor() == null || getActor().isDead() || getActor().isDeleting()){
            if (aiTask != null){
                aiTask.cancel(true);
                return true;
            }
        }
        return false;
    }

    protected List<UnitLoc> getUnitLocs(){
        return getActor().getUnitLocation();
    }

    protected Location findNewLoc(Location ref, Side FBside, Side LRside, int FBdist, int LRdist){
        Vector2P vector = getVector(FBdist, LRdist, ref, FBside, LRside);
        return new Location(vector.getX(), vector.getY(), getActor().getZ());
    }

    protected Vector2P getVector(int FBdist, int LRdist, Location ref, Side FBside, Side LRside){
        Point2D actorPoint = new Point2D(getActor().getX(), getActor().getY());
        Point2D refPoint = new Point2D(ref.x, ref.y);
        PointNSWE2P p1 = new PointNSWE2P(actorPoint, refPoint, FBdist, FBside);
        PointNSWE2P p2 = new PointNSWE2P(actorPoint, refPoint, LRdist, LRside);
        return new Vector2P(actorPoint, p1.getPoint2D(), p2.getPoint2D());
    }

    protected Location findNewLoc2(Location ref, Location direction, Side FBside, Side LRside, int FBdist, int LRdist){
        Vector2P vector = getVector2(FBdist, LRdist, ref, direction, FBside, LRside);
        return new Location(vector.getX(), vector.getY(), direction.z);
    }

    protected Vector2P getVector2(int FBdist, int LRdist, Location ref, Location direction, Side FBside, Side LRside){
        Point2D refPoint = new Point2D(ref.x, ref.y);
        Point2D dirPoint = new Point2D(direction.x, direction.y);
        PointNSWE2P p1 = new PointNSWE2P(refPoint, dirPoint, FBdist, FBside);
        PointNSWE2P p2 = new PointNSWE2P(refPoint, dirPoint, LRdist, LRside);
        return new Vector2P(refPoint, p1.getPoint2D(), p2.getPoint2D());
    }

}
