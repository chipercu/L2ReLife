package npc.model.Military;

import ai.MilitaryArt.MilitaryRank;
import l2open.gameserver.model.L2Player;
import l2open.util.Rnd;

public class MilitaryManager {

    public static Unit newMilitaryUnit(int _classId, byte _sex, MilitaryRank rank, L2Player commander){
        Unit unit = (Unit) L2Player.create(_classId, (byte) _sex, "BOT", "Bot", (byte) Rnd.get(0, 2), (byte) Rnd.get(0, 2), (byte) Rnd.get(0, 2), 0, Rnd.get(20, 85));

        if (rank != null){
            unit.setRank(rank);
        }
        if (commander != null){
            unit.setCommander(commander);
        }




        return unit;
    }

    public static void command(Unit unit){



    }











}
