package ai.dragonvalley;

import l2open.config.ConfigValue;
import l2open.gameserver.ai.CtrlEvent;
import l2open.gameserver.ai.Fighter;
import l2open.gameserver.geodata.GeoEngine;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Spawn;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.tables.NpcTable;
import l2open.util.Location;
import l2open.util.Rnd;

/**
 * It is made in 2 minutes :D
 * @author Drizzy
 * @date 30.11.2013
 */
public class DragonValleyFarm extends Fighter
{
	public DragonValleyFarm(L2NpcInstance actor)
	{
		super(actor);
	}

	private static final int [] mobs = {36620, 36621, 36622, 36623};

	/**
	@Override
	public void MY_DYING(L2Character last_attacker)
	{
		L2NpcInstance actor = getActor();
		if(actor.param1 == 0)
		{
			if(Rnd.chance(15))
			{
				myself.CreateOnePrivateEx(1022855,"bloody_berserker",0,0,actor.getX() + 30,actor.getY() + 10,actor.getZ(),0,1,0,0);
				myself.CreateOnePrivateEx(1022855,"bloody_berserker",0,0,actor.getX() + 30,actor.getY() - 10,actor.getZ(),0,1,0,0);
				myself.CreateOnePrivateEx(1022855,"bloody_berserker",0,0,actor.getX() + 30,actor.getY() + 30,actor.getZ(),0,1,0,0);
				myself.CreateOnePrivateEx(1022855,"bloody_berserker",0,0,actor.getX() + 30,actor.getY() - 30,actor.getZ(),0,1,0,0);
				myself.CreateOnePrivateEx(1022855,"bloody_berserker",0,0,actor.getX() + 30,actor.getY() - 50,actor.getZ(),0,1,0,0);
			}
		}
		super.MY_DYING(last_attacker);
	}
	**/
	@Override
	protected void MY_DYING(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(actor != null && actor.param1 == 0)
			if(Rnd.chance(10)) {
				try {
				Location pos = GeoEngine.findPointToStay(actor.getX() + 30, actor.getY() + Rnd.get(-50, 30), actor.getZ(), 0, 0, actor.getReflection().getGeoIndex());
				L2Spawn sp = new L2Spawn(NpcTable.getTemplate(mobs[Rnd.get(0,3)]));
				sp.setLoc(pos);
				L2NpcInstance npc = sp.doSpawn(true, false, 1, 0, 0, null);
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Rnd.get(1, 100));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		super.MY_DYING(killer);
	}
}