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


    public static void question(String text) {
        for (L2Player player : L2ObjectsStorage.getPlayers())
            if (player != null && player.getLevel() >= 76 && player.getVar("jailed") == null)
                player.scriptRequest(text, "Vote:addResult", new Object[0]);
    }



    public boolean useAdminCommand(Enum comm, final String[] wordList, String fullString, final L2Player activeChar) {
        Commands command = (Commands) comm;

        if (activeChar.getPlayerAccess().Menu) {
            switch (command) {
                case admin_party:
                    Functions.callScripts("scripts.services.PartyMaker.PartyMaker", "showCreateDialog", new Object[] {activeChar.getObjectId()});
                    break;
                case admin_party_all:
                    Functions.callScripts("scripts.services.PartyMaker.PartyMaker", "showGroups", new Object[] {activeChar.getObjectId()});
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