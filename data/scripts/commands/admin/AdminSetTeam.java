package scripts.commands.admin;

import l2open.config.ConfigValue;
import l2open.database.DatabaseUtils;
import l2open.database.FiltredPreparedStatement;
import l2open.database.L2DatabaseFactory;
import l2open.database.ThreadConnection;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.cache.Msg;
import l2open.gameserver.handler.AdminCommandHandler;
import l2open.gameserver.handler.IAdminCommandHandler;
import l2open.gameserver.instancemanager.RaidBossSpawnManager;
import l2open.gameserver.model.L2Object;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Spawn;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.tables.NpcTable;
import l2open.gameserver.templates.L2NpcTemplate;
import l2open.util.Location;
import l2open.util.Util;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminSetTeam implements IAdminCommandHandler, ScriptFile {

    private static enum Commands {
        admin_set_team1,
        admin_set_team2,
        admin_set_team0

    }


    @Override
    public boolean useAdminCommand(Enum anEnum, String[] strings, String s, L2Player l2Player) {
        Commands command = (Commands) anEnum;
        StringTokenizer st;

        if (!l2Player.getPlayerAccess().CanBanChat) {
            return false;
        }

        switch (command) {
            case admin_set_team0:
                l2Player.setTeam(0, false);
            case admin_set_team1 :
                l2Player.setTeam(1, false);
                break;
            case admin_set_team2:
                l2Player.setTeam(2, false);
        }
        return true;
    }


    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    @Override
    public void onLoad() {
        AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
    }

    @Override
    public void onReload() {

    }

    @Override
    public void onShutdown() {

    }

}
