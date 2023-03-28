package ai;

import l2open.extensions.scripts.Functions;
import l2open.gameserver.ai.Fighter;
import l2open.gameserver.instancemanager.HandysBlockCheckerManager;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.entity.BlockCheckerEngine;
import l2open.gameserver.model.instances.L2BlockInstance;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.items.L2ItemInstance;
import l2open.gameserver.serverpackets.ExCubeGameChangePoints;
import l2open.gameserver.serverpackets.ExCubeGameExtendedChangePoints;
import l2open.gameserver.tables.SkillTable;
import l2open.util.Location;
import l2open.util.Rnd;

public class HandysBlock extends Fighter
{
	public HandysBlock(L2Character actor)
	{
		super(actor);
		actor.setIsInvul(true);
	}

	@Override
	protected void onEvtSeeSpell(L2Skill skill, L2Character caster)
	{
		L2BlockInstance actor = (L2BlockInstance) getActor();
		if(actor == null || skill == null || caster == null || caster.getPlayer() == null)
			return;

		L2Player player = caster.getPlayer();
		int arena = player.getBlockCheckerArena();
		if(arena == -1 || arena > 3)
			return;

		if(skill.getId() == 5852 || skill.getId() == 5853)
		{
			if(GetAbnormalLevel(actor,Skill_GetAbnormalType(SkillTable.getInstance().getInfo(2616,1))) > 0)
				return;
			else if(GetAbnormalLevel(actor,Skill_GetAbnormalType(SkillTable.getInstance().getInfo(2617,1))) > 0)
			{
				switch(Rnd.get(2))
				{
					case 0:
						AddUseSkillDesire(caster,SkillTable.getInstance().getInfo(5855,1),100000000);
						break;
					case 1:
						AddUseSkillDesire(caster,SkillTable.getInstance().getInfo(5854,1),100000000);
						break;
				}
			}
			HandysBlockCheckerManager.ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(arena);

			if(holder.getPlayerTeam(player) == 0 && !actor.isRed())
			{
				actor.changeColor();
				increaseTeamPointsAndSend(player, holder.getEvent());
			}
			else if(holder.getPlayerTeam(player) == 1 && actor.isRed())
			{
				actor.changeColor();
				increaseTeamPointsAndSend(player, holder.getEvent());
			}
			else
				return;

			if(Rnd.get(100) < 20)
			{
				if(Rnd.get(2) > 0)
					dropItem(actor, 13787, holder.getEvent(), player);
				else
					dropItem(actor, 13788, holder.getEvent(), player);
			}
		}
		super.onEvtSeeSpell(skill, caster);
	}

	private void increaseTeamPointsAndSend(L2Player player, BlockCheckerEngine eng)
	{
		int team = eng.getHolder().getPlayerTeam(player);
		eng.increasePlayerPoints(player, team);

		int timeLeft = (int) ((eng.getStarterTime() - System.currentTimeMillis()) / 1000);
		boolean isRed = eng.getHolder().getRedPlayers().contains(player);

		ExCubeGameChangePoints changePoints = new ExCubeGameChangePoints(timeLeft, eng.getBluePoints(), eng.getRedPoints());
		ExCubeGameExtendedChangePoints secretPoints = new ExCubeGameExtendedChangePoints(timeLeft, eng.getBluePoints(), eng.getRedPoints(), isRed, player, eng.getPlayerPoints(player, isRed));

		eng.getHolder().broadCastPacketToTeam(changePoints);
		eng.getHolder().broadCastPacketToTeam(secretPoints);
	}

	private void dropItem(L2NpcInstance block, int id, BlockCheckerEngine eng, L2Player player)
	{
		L2ItemInstance drop = Functions.createItem(id);
		drop.dropToTheGround(block, Location.findPointToStay(block, 50));
		eng.addNewDrop(drop);
	}

	@Override
	protected void thinkAttack()
	{}

	@Override
	protected void onIntentionAttack(L2Character target)
	{}

	@Override
	protected void ATTACKED(L2Character attacker, int damage, L2Skill skill)
	{}

	@Override
	protected void onEvtAggression(L2Character attacker, int aggro)
	{}

	@Override
	protected void onEvtClanAttacked(L2Character attacked_member, L2Character attacker, int damage)
	{}
}