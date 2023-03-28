package scripts.commands.voiced;

import l2open.extensions.scripts.Functions;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.handler.IVoicedCommandHandler;
import l2open.gameserver.handler.VoicedCommandHandler;
import l2open.gameserver.model.L2Player;


public class TargetStatusBuff extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private String[] _commandList = new String[] { "TargetStatusBuff" };

	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public boolean useVoicedCommand(String command, L2Player activeChar, String args)
	{
		command = command.intern();
		if(command.equalsIgnoreCase("TargetStatusBuff"))
		{
			activeChar.sendMessage("izi");
		}
		return false;
	}

	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}