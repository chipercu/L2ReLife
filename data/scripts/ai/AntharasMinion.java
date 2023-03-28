package ai;

import bosses.AntharasManager;
import l2open.gameserver.ai.CtrlEvent;
import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.ai.Fighter;
import l2open.gameserver.geodata.GeoEngine;
import l2open.gameserver.model.*;
import l2open.gameserver.model.instances.*;
import l2open.gameserver.model.instances.L2NpcInstance.AggroInfo;
import l2open.gameserver.tables.SkillTable;
import l2open.util.*;

import java.util.*;
import java.util.concurrent.*;

public class AntharasMinion extends Fighter
{
	private boolean attack = false;

	public AntharasMinion(L2NpcInstance actor)
	{
		super(actor);
		actor.p_block_debuff.getAndSet(true);
	}

	@Override
	protected void onEvtSpawn()
	{
		_lastActive = System.currentTimeMillis()+10000;
		super.onEvtSpawn();
		for(L2Player p : AntharasManager.getPlayersInside())
			if(!p.isDead())
			{
				notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 5000);
				if(!attack)
				{
					changeIntention(CtrlIntention.AI_INTENTION_ATTACK, p, null);
					attack = true;
				}
			}
	}

	@Override
	protected void MY_DYING(L2Character killer)
	{
		getActor().doCast(SkillTable.getInstance().getInfo(5097, 1), getActor(), true);
		super.MY_DYING(killer);
	}

	/******************* Временная затычка *******************/
	private long _lastActive = 0;
	private long _time = 5000;

	public static List<AggroInfo> getHateList()
	{
		L2BossInstance _antharas = AntharasManager._antharas;

		if(_antharas == null || _antharas.isDead())
			return Collections.emptyList();
		GArray<AggroInfo> aggroList = _antharas.getAggroList();

		List<AggroInfo> activeList = new ArrayList<AggroInfo>();
		List<AggroInfo> passiveList = new ArrayList<AggroInfo>();

		for(AggroInfo ai : aggroList)
			if(ai.hate >= 0)
			{
				L2Playable cha = ai.attacker;
				if(cha != null)
					if(cha.isStunned() || cha.isSleeping() || cha.isParalyzed() || cha.isAfraid() || cha.isBlocked())
						passiveList.add(ai);
					else
						activeList.add(ai);
			}

		if(!activeList.isEmpty())
		{
			Collections.sort(activeList, new Comparator<AggroInfo>()
			{
				@Override
				public int compare(AggroInfo o1, AggroInfo o2)
				{
					return o2.hate - o1.hate;
				}
			});

			return activeList;
		}
		else if(!passiveList.isEmpty())
		{
			Collections.sort(passiveList, new Comparator<AggroInfo>()
			{
				@Override
				public int compare(AggroInfo o1, AggroInfo o2)
				{
					return o2.hate - o1.hate;
				}
			});

			return passiveList;
		}

		return Collections.emptyList();
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return true;
		else if(_lastActive + _time > System.currentTimeMillis())
			return false;
		else if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
		{
			_lastActive = System.currentTimeMillis();

			try
			{				
				List<AggroInfo> _hate_list = getHateList();

				int size = 10; // берем первую десятку
				size = _hate_list.size() > size ? size : _hate_list.size();
				L2Player player = null;
				if(_hate_list.size() > 0)
					player = _hate_list.get(Rnd.get(size)).attacker.getPlayer();

				boolean zatuchka=true;
				if(!playerAtack(player))
				{
					for(AggroInfo agro_info : _hate_list)
						if(agro_info != null && agro_info.attacker != null && agro_info.attacker.getPlayer() != null && playerAtack(agro_info.attacker.getPlayer()))
						{
							zatuchka=false;
							break;
						}
				}
				else
					zatuchka=false;
				if(zatuchka)
					for(L2Player p : AntharasManager.getPlayersInside())
						if(playerAtack(p))
							break;
			}
			catch(Exception e)
			{
				//actor.deleteMe();
				e.printStackTrace();
			}
		}
		return true;
	}

	private boolean playerAtack(L2Player player)
	{
		L2NpcInstance actor = getActor();
		if(player != null && !player.isDead() && GeoEngine.canAttacTarget(actor, player, false))
		{
			_time = 5000;
			player.addDamageHate(actor, 0, 1000); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
			actor.setRunning(); // Включаем бег...
			actor.setAttackTimeout(Integer.MAX_VALUE + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
			actor.getAI().setAttackTarget(player); // На всякий случай, не обязательно делать
			actor.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, player, null); // Переводим в состояние атаки
			actor.getAI().addTaskAttack(player); // Добавляем отложенное задание атаки, сработает в самом конце движения
			return true;
		}
		return false;
	}
	/******************* ******************** *******************/
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}