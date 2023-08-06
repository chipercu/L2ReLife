package fuzzy.GuardFactory;

import java.util.ArrayList;
import java.util.List;

public class TownGuardManager {

    private List<TownGuardFactory> guards = new ArrayList<>();

    public void initGuards(){
        TownGuardFactory knight = new Knight();
        knight.create();
        guards.add(knight);
    }


}
