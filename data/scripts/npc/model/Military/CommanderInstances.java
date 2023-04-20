package npc.model.Military;

import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.templates.L2NpcTemplate;

public class CommanderInstances extends L2NpcInstance {

    private L2NpcInstance commander;

    public CommanderInstances(int objectId, L2NpcTemplate template) {
        super(objectId, template);
    }


    public L2NpcInstance getCommander() {
        return commander;
    }

    public void setCommander(L2NpcInstance commander) {
        this.commander = commander;
    }


}
