package npc.model;

import javafx.scene.media.MediaPlayer;
import l2open.extensions.scripts.Functions;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.templates.L2NpcTemplate;
import l2open.util.NpcUtils;

public final class L2FortGuardCapitan10601Instance extends L2NpcInstance {
	public static final int Guard = 35082;
	public static final int Archer = 35720;
	public static final int Mage = 35717;
	public static final int Healer = 35718;

	public L2FortGuardCapitan10601Instance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker.isMonster() || attacker.isPlayer() && attacker.getKarma() > 0;

	}

	@Override
	public void onBypassFeedback(L2Player player, String command) {
		if(!canBypassCheck(player, this))
			return;
		

		if(command.startsWith("spawn_guard")){
			//spawnToInstance(Guard, this.getLoc(), 0, this.getReflectionId(), true);
			L2NpcInstance npcInstance = NpcUtils.spawnSingle(Guard, this.getLoc(), this.getReflectionId(), 3600000);
			npcInstance.p_block_move(true, null);
			player.sendMessage(npcInstance.getName() + " был призван");
		}
		super.onBypassFeedback(player, command);
	}




	@Override
	public String getHtmlPath(int npcId, int val)

	{
		return "data/html/fortress/quest10601-1.htm";
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}
}