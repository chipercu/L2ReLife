package npc.model.Military;

import ai.MilitaryArt.BattleOrderType;
import ai.MilitaryArt.MilitaryRank;
import l2open.gameserver.geodata.GeoEngine;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.templates.L2PlayerTemplate;
import l2open.util.Location;
import l2open.util.Rnd;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static npc.model.Military.Commands.*;

public class Unit extends L2Player {

    private MilitaryRank rank;

    private List<Unit> units = new ArrayList<>();

    private List<Location> unitPosition = new ArrayList<>();

    private L2Player commander;

    public Unit(int objectId, L2PlayerTemplate template, int bot) {
        super(objectId, template, bot);

        switch (rank){
            case OFFICER: {
                command(CHARGE);
            }
        }

    }

    public void command(Commands command){
        if (units.isEmpty()){
            return;
        }
        List<Unit> unitList = units.stream()
                .filter(u -> !u.isDead())
                .filter(u -> !u.isMovementDisabled())
                .filter(u -> !u.isSleeping())
                .collect(Collectors.toList());

        switch (command){
            case CHARGE: {
                unitList.forEach(unit -> {
//                    unit.moveToLocation();

                });
            }
            case FALANG:{
                unitList.forEach(unit -> {


                });

            }
        }



    }
    private int getActiveUnits(){
       return (int) units.stream()
                .filter(unit -> !unit.isDead())
                .count();
    }
//    private Location getFrontPoint(){
//        L2Player actor = this;
//
//        int posX = actor.getX();
//        int posY = actor.getY();
//        int posZ = actor.getZ();
//
//        int signx = posX < attacker.getX() ? -1 : 1;
//        int signy = posY < attacker.getY() ? -1 : 1;
//
//        int range = 200;
//
//        posX += Math.round(signx * range);
//        posY += Math.round(signy * range);
//        posZ = GeoEngine.getHeight(posX, posY, posZ, actor.getReflection().getGeoIndex());
//
//        if(GeoEngine.canMoveToCoord(attacker.getX(), attacker.getY(), attacker.getZ(), posX, posY, posZ, actor.getReflection().getGeoIndex())){
//            addTaskMove(posX, posY, posZ, false);
//        }
//    }



    private int getUnitCount(){
        return units.size();
    }

    private List<Location> getUnitPosition(BattleOrderType type){
        int unitCounts = getActiveUnits();
        List<Location> locationList = new ArrayList<>();

        switch (type){
            case FALANG:{
                Location loc = this.getLoc();



            }

        }

        return locationList;

    }




    public MilitaryRank getRank() {
        return rank;
    }

    public void setRank(MilitaryRank rank) {
        this.rank = rank;
    }

    public L2Player getCommander() {
        return commander;
    }

    public void setCommander(L2Player commander) {
        this.commander = commander;
    }
}
