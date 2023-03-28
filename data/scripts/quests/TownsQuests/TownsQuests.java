package quests.TownsQuests;

import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.model.L2ObjectsStorage;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.quest.Quest;
import l2open.gameserver.serverpackets.RadarControl;

public class TownsQuests extends Quest implements ScriptFile {

    protected static final int GLUDIO_CAPITAN = 30332;
    protected static final int GLUDIO_STAMP = 13823;
    protected static final int HUNTER_CAPITAN = 30707;
    protected static final int HUNTER_STAMP = 13823;
    private int REWARD;

    public TownsQuests() {
        super(PARTY_NONE);
    }


    public static L2NpcInstance getNpcByID(int id){
        return L2ObjectsStorage.getByNpcId(id);
    }

    public static void addRadarToNpc(L2Player player, L2NpcInstance npc){
        player.sendPacket(new RadarControl(2, 1, npc.getLoc()));// Убираем флажок на карте и стрелку на компасе
        player.sendPacket(new RadarControl(0, 2, npc.getLoc()));// Ставим флажок на карте и стрелку на компасе
        //player.radar.addMarker(npc.getX(), npc.getY(), npc.getZ());
    }

    @Override
    public void onLoad() {ScriptFile._log.info("Loaded Quest: " + getClass().getName());}

    @Override
    public void onReload() {

    }

    @Override
    public void onShutdown() {
    }


}
