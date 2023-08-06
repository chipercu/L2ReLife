package ai.Fort.FortManager;

import l2open.extensions.scripts.Functions;
import l2open.extensions.scripts.Script;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.instancemanager.FortressManager;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.entity.residence.Fortress;

import java.util.*;

public class GuardManager implements ScriptFile {

    public static final Map<Integer, FortressGuards> fortressGuards = new HashMap<>();


    @Override
    public void onLoad() {
        initFortressGuards();
    }

    public void addSquad(Fortress fortress){
        if (fortressGuards.get(fortress.getId()) == null){
            fortressGuards.put(fortress, new FortressGuards(fortress));
        }else {
            fortressGuards.get(fortress.getId()).squadList.add(new Squad(fortress, FortSquadType.WIZARDS));
            registerSqudToDB();
        }

    }

    public void loadFortressGuards(){
        Collection<Fortress> fortresses = FortressManager.getInstance().getFortresses().values();

        for (Fortress fort: fortresses){
            fortressGuards.
        }

    }

    public void initFortressGuards(){
        Collection<Fortress> fortresses = FortressManager.getInstance().getFortresses().values();

        for (Fortress fort: fortresses){
            FortressGuards guards = fortressGuards.get(fort.getId());
            for (Squad squad : guards.squadList) {
                squad.initSquad();
            }

        }
    }






    @Override
    public void onReload() {

    }

    @Override
    public void onShutdown() {

    }

    static class Squad{
        private Fortress fortress;
        private L2Player commander;
        private FortSquadType squadType;
        private List<L2Player> units;

        public Squad(Fortress fortress, FortSquadType squadType) {
            this.fortress = fortress;
            this.squadType = squadType;
        }

        private void initSquad(){
            switch (squadType){

                case DEFENDERS:{
                    spawnDefenders();
                    break;
                }
                case ARCHERS:{
                    spawnArchers();
                    break;
                }
                case WIZARDS:{
                    spawnWizards();
                    break;
                }
                case WARRIORS:{
                    spawnWarriors();
                    break;
                }
            }
        }

        private void spawnWarriors() {

        }

        private void spawnWizards() {

        }

        private void spawnArchers() {

        }

        private void spawnDefenders() {

        }

        public FortSquadType getSquadType() {
            return squadType;
        }
    }

    static class FortressGuards{

        private List<Squad> squadList = new ArrayList<>();
        private Fortress fortress;

        public FortressGuards(Fortress fortress) {
            this.fortress = fortress;
            initGuards(fortress);
        }


        private Squad getSquadFromDB(int fortressId){

            return new Squad(this.fortress,FortSquadType.WIZARDS);
        }



        private void initGuards(Fortress fortress){



            squadList.add(getSquadFromDB(fortress.getId()));
        }



    }

}
