package npc.model.Military;

import l2open.gameserver.model.L2Player;
import l2open.gameserver.templates.L2PlayerTemplate;

public class Soldier extends L2Player {

    private L2Player commander;

    public Soldier(int objectId, L2PlayerTemplate template, int bot) {
        super(objectId, template, bot);
    }


    public L2Player getCommander() {
        return commander;
    }

    public void setCommander(L2Player commander) {
        this.commander = commander;
    }






}
