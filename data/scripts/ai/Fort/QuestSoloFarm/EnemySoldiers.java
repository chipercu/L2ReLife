package ai.Fort.QuestSoloFarm;

import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.ai.Fighter;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2World;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.util.Rnd;

public class EnemySoldiers extends Fighter
{
	private long _lastAttackTime = 0;
	private static final long NextAttack = 10 * 1000; // 10 сек
	private L2NpcInstance monster;


	private static final int Tank_Capitan = 36625;
	private static final int Guard = 36626;
	private static final int Archer = 36627;
	private static final int Mage = 36628;
	private static final int Healer = 36629;
	private static final int[] TARGET = { Tank_Capitan, Guard, Archer, Mage, Healer};


	public EnemySoldiers(L2NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive() {
		L2NpcInstance actor = getActor();
		if(actor == null)
			return true;

		if(_lastAttackTime + (NextAttack + Rnd.get(5000, 10000)) < System.currentTimeMillis()) {
			if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) {
				if(monster == null) {
					for (L2NpcInstance npc : L2World.getAroundNpc(actor, 1000, 200))
						if (npc.getNpcId() == TARGET[Rnd.get(TARGET.length)]) {
							npc.addDamageHate(actor, 0, 100);
							monster = npc;
						}
				}
				if(monster != null) {
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, monster);
					_lastAttackTime = System.currentTimeMillis();
				}
			}
		}
		return true;
	}

	@Override
	protected void MY_DYING(L2Character killer) {
		monster = null;
		_lastAttackTime = 0;
		super.MY_DYING(killer);
	}


}