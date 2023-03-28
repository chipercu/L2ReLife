package ai.Fort.QuestSoloFarm;

import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.ai.Fighter;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.L2World;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.tables.SkillTable;
import l2open.util.Rnd;

public class Mage extends Fighter
{
	private long _lastAttackTime = 0;
	private static final long NextAttack = 10 * 1000; // 10 сек
	private L2NpcInstance monster;

	private static final int ENEMY = 36630;
	private static final int ENEMY_WARRIOR = 36632;
	private static final int ENEMY_OVERLORD = 36634;

	private static final int ENEMY_SHAMAN = 36633;

	private static final int ENEMY_CHAMPION = 36635;
	private static final int ENEMY_ARCHER = 36631;

	private static final int [] TARGET = {ENEMY, ENEMY_WARRIOR ,ENEMY_OVERLORD, ENEMY_ARCHER, ENEMY_CHAMPION};

	private final L2Skill vampRage = SkillTable.getInstance().getInfo(6727, 1);
	private final L2Skill holyResist = SkillTable.getInstance().getInfo(6729, 1);
	private final L2Skill blessBlood = SkillTable.getInstance().getInfo(6725, 1);
	private final L2Skill recharge = SkillTable.getInstance().getInfo(6728, 1);
	private final L2Skill heal = SkillTable.getInstance().getInfo(6724, 1);

	public Mage(L2NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean randomWalk() {return false;}

	@Override
	public void checkAggression(L2Character target) {
		L2NpcInstance actor = getActor();
		if(target.isPlayer())
			return;
		super.checkAggression(target);
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