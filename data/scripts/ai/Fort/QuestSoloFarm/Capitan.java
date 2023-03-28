package ai.Fort.QuestSoloFarm;

import l2open.gameserver.ai.Fighter;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.tables.SkillTable;

public class Capitan extends Fighter
{
	private final L2Skill vampRage = SkillTable.getInstance().getInfo(6727, 1);
	private final L2Skill holyResist = SkillTable.getInstance().getInfo(6729, 1);
	private final L2Skill blessBlood = SkillTable.getInstance().getInfo(6725, 1);
	private final L2Skill recharge = SkillTable.getInstance().getInfo(6728, 1);
	private final L2Skill heal = SkillTable.getInstance().getInfo(6724, 1);

	public Capitan(L2NpcInstance actor)
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






}