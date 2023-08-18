package commands.admin;

import l2open.extensions.scripts.Functions;
import l2open.extensions.scripts.ScriptFile;
import l2open.extensions.scripts.Scripts;
import l2open.gameserver.GameServer;
import l2open.gameserver.handler.AdminCommandHandler;
import l2open.gameserver.handler.IAdminCommandHandler;
import l2open.gameserver.model.L2ObjectsStorage;
import l2open.gameserver.model.L2Player;

public class AdminTestCommands implements IAdminCommandHandler, ScriptFile {
    private static enum Commands {
        admin_party,
        admin_party_all
    }

    public boolean useAdminCommand(Enum comm, final String[] wordList, String fullString, final L2Player activeChar) {
        Commands command = (Commands) comm;

        if (activeChar.getPlayerAccess().Menu) {
            switch (command) {
                case admin_party:
                    scripts.services.PartyMaker.PartyMaker.getInstance().showGroups(activeChar);
                    break;
            }
            return true;
        }

        return false;
    }





    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    public void onLoad() {
        AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
    }

    public void onReload() {
    }

    public void onShutdown() {
    }
}