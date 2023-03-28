package quests._10600_FortPartyFarm;

import javolution.util.FastMap;
import l2open.common.ThreadPoolManager;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.cache.Msg;
import l2open.gameserver.clientpackets.Say2C;
import l2open.gameserver.instancemanager.FortressManager;
import l2open.gameserver.instancemanager.InstancedZoneManager;
import l2open.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import l2open.gameserver.model.*;
import l2open.gameserver.model.entity.residence.Fortress;
import l2open.gameserver.model.instances.L2DoorInstance;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.quest.Quest;
import l2open.gameserver.model.quest.QuestState;
import l2open.gameserver.serverpackets.ExSendUIEvent;
import l2open.gameserver.serverpackets.NpcSay;
import l2open.gameserver.serverpackets.SystemMessage;
import l2open.gameserver.tables.ReflectionTable;
import l2open.util.GArray;
import l2open.util.Location;
import l2open.util.Rnd;
import npc.model.L2SepulcherMonsterInstance;

import java.util.List;

public class _10600_FortPartyFarm extends Quest implements ScriptFile
{
	private final static int DungeonLeaderMark = 9797;
	private final static int RewardMarksCount = 1000; // цифра с потолка
	private final static int KnightsEpaulette = 9912;

	private static final FastMap<Integer, Prison> _prisons = new FastMap<Integer, Prison>().setShared(true);

	private static final int Guard = 35082;
	private static int guard_count = 0;
	private static final int Acher = 35720;
	private static final int Mage = 35717;
	private static final int Healer = 35718;

	private static final int Ministr = 35722;
	private static final int Mage_Capitan = 35716;
	private static final int Tank_Capitan = 35713;
	private static final int DD_Capitan = 35721;
	private static final int Archer_Capitan = 35719;
	private static final int Commander = 35771;
	private static int capitan_kill_count = 0;

	private static final int door1 = 24220006;
	private static final int door2 = 24220007;
	private static final int door3 = 24220008;


	private static final int BrandTheExile = 25589;
	private static final int CommanderKoenig = 25592;
	private static final int GergTheHunter = 25593;

	private static final int[] type1 = new int[] {Healer, Acher, Mage};

	public CheckStatus LAST_CHECK_STATUS = CheckStatus.GRACIA_FINAL;




	public _10600_FortPartyFarm()
	{
		super(false);

		// Detention Camp Wardens
		addStartNpc(35666, 35698, 35735, 35767, 35804, 35835, 35867, 35904, 35936, 35974, 36011, 36043, 36081, 36118, 36149, 36181, 36219, 36257, 36294, 36326, 36364);
		addQuestItem(DungeonLeaderMark, KnightsEpaulette);
		addKillId(Guard, Acher, Mage, Healer, Ministr, Mage_Capitan, Tank_Capitan, DD_Capitan, Archer_Capitan, Commander);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		if(event.equalsIgnoreCase("gludio_fort_a_campkeeper_q0511_03.htm") || event.equalsIgnoreCase("gludio_fort_a_campkeeper_q0511_06.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("exit"))
		{
			st.exitCurrentQuest(true);
			return null;
		}
		else if(event.equalsIgnoreCase("enter")){
			st.setCond(1);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
			return enterPrison(st.getPlayer());
		}


//			if(st.getState() == CREATED || !check(st.getPlayer()))
//				return "gludio_fort_a_campkeeper_q0511_01a.htm";
//			else
//				return enterPrison(st.getPlayer());
		return event;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		if(!check(st.getPlayer()))
			return "gludio_fort_a_campkeeper_q0511_01a.htm";
		if(st.getState() == CREATED)
			return "gludio_fort_a_campkeeper_q0511_01.htm";
		if(st.getQuestItemsCount(DungeonLeaderMark) > 0)
		{
			st.giveItems(KnightsEpaulette, st.getQuestItemsCount(DungeonLeaderMark));
			st.takeItems(DungeonLeaderMark, -1);
			st.playSound(SOUND_FINISH);
			return "gludio_fort_a_campkeeper_q0511_09.htm";
		}
		return "gludio_fort_a_campkeeper_q0511_10.htm";
	}
	private void addAgrro (L2NpcInstance npc) {
		for(L2Character cha : L2World.getAroundCharacters(npc, 1200, 300)) {
			if (cha.isPlayer()) {
				cha.addDamageHate(npc, 0, Rnd.get(50, 150)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
				npc.setRunning();
				npc.getAI().setAttackTarget(cha); // На всякий случай, не обязательно делать
				npc.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, cha, null); // Переводим в состояние атаки
				npc.getAI().addTaskAttack(cha); // Добавляем отложенное задание атаки, сработает в самом конце движения
			}
		}
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		L2NpcInstance capitan;
		for(Prison prison : _prisons.values())

			if(prison.getReflectionId() == npc.getReflection().getId())
			{
				switch(npc.getNpcId())
				{
					case Guard:
						guard_count++;
						//npc.broadcastPacket(new NpcSay(npc, Say2C.NPC_ALL, "count : " + guard_count));
						if (guard_count > 3){
							ReflectionTable.getInstance().get(prison.getReflectionId()).openDoor(door2);
							addSpawnToInstance(Ministr, new Location(154300, 145300, -12611), 0 , prison.getReflectionId());
							for (int i = 154200; i < 154550; i += 50) {
								L2NpcInstance guard = addSpawnToInstance(type1[Rnd.get(type1.length)], new Location(i, 145176,-12611), 0 , prison.getReflectionId());
							}
						}
						break;
					case Ministr:
						ReflectionTable.getInstance().get(prison.getReflectionId()).openDoor(door3);
						L2NpcInstance archer_capitan = addSpawnToInstance(Archer_Capitan, new Location(153304, 142088, -12763), 0 , prison.getReflectionId());
						L2NpcInstance dd_capitan =addSpawnToInstance(DD_Capitan, new Location(153700, 142088, -12763), 0 , prison.getReflectionId());
//						String fort_name = st.getPlayer().get
//						archer_capitan.setTitle(prison.getFortId());
						break;
					case Mage_Capitan:
					case Tank_Capitan:
						for (int i = 0; i < Rnd.get(7, 11); i++) {
							L2NpcInstance guard = addSpawnToInstance(type1[Rnd.get(type1.length)], new Location(npc.getX() + Rnd.get(-500, 500), npc.getY() + Rnd.get(-500, 500), npc.getZ()), 0 , prison.getReflectionId() );
							//guards.add(guard);
							addAgrro(guard);
						}
						capitan_kill_count++;
						if (capitan_kill_count == 4){
							L2NpcInstance commander = addSpawnToInstance(Commander, new Location(153576, 142072, -12763), 0 , prison.getReflectionId());
							addAgrro(commander);
						}
						break;
					case Archer_Capitan:
						for (int i = 0; i < Rnd.get(8, 13); i++) {
							L2NpcInstance guard = addSpawnToInstance(type1[Rnd.get(type1.length)], new Location(npc.getX() + Rnd.get(-500, 500), npc.getY() + Rnd.get(-500, 500), npc.getZ()), 0 , prison.getReflectionId() );
							//guards.add(guard);
							addAgrro(guard);
						}
						capitan = addSpawnToInstance(Tank_Capitan, new Location(npc.getX() + Rnd.get(-500, 500), npc.getY() + Rnd.get(-500, 500), npc.getZ()), 0 , prison.getReflectionId());
						addAgrro(capitan);
						capitan_kill_count++;
						break;
					case DD_Capitan:
						for (int i = 0; i < Rnd.get(9, 12); i++) {
							L2NpcInstance guard = addSpawnToInstance(type1[Rnd.get(type1.length)], new Location(npc.getX() + Rnd.get(-500, 500), npc.getY() + Rnd.get(-500, 500), npc.getZ()), 0 , prison.getReflectionId() );
							//guards.add(guard);
							addAgrro(guard);
						}
						capitan = addSpawnToInstance(Mage_Capitan, new Location(npc.getX() + Rnd.get(-500, 500), npc.getY() + Rnd.get(-500, 500), npc.getZ()), 0 , prison.getReflectionId());
						addAgrro(capitan);
						capitan_kill_count++;
						break;
					case Commander:
						L2Party party = st.getPlayer().getParty();
						if(party != null)
							for(L2Player member : party.getPartyMembers()) {
								QuestState qs = member.getQuestState(getClass());
								if(qs != null && qs.isStarted())
								{
									qs.giveItems(KnightsEpaulette, 5000);
									qs.playSound(SOUND_ITEMGET);
									qs.getPlayer().sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(2));
								}
							}
						else
						{
							st.giveItems(DungeonLeaderMark, RewardMarksCount);
							st.playSound(SOUND_ITEMGET);
							st.getPlayer().sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(2));
						}
//						for (L2NpcInstance guard: guards){
//							guard.deleteMe();
//						}
						Reflection r = ReflectionTable.getInstance().get(prison.getReflectionId());
						if(r != null)
							r.startCollapseTimer(120000); // Всех боссов убили, запускаем коллапс через 5 минут
						break;
				}
				break;
			}

		return null;
	}

	private void spawnGuards(L2NpcInstance npc, Prison prison) {
		for (int i = 0; i < Rnd.get(8, 12); i++) {
			L2NpcInstance guard = addSpawnToInstance(type1[Rnd.get(type1.length)], new Location(npc.getX() + Rnd.get(-500, 500), npc.getY() + Rnd.get(-500, 500), npc.getZ()), 0 , prison.getReflectionId() );
			//guards.add(guard);
			addAgrro(guard);
		}
	}

	private boolean check(L2Player player)
	{
		Fortress fort = FortressManager.getInstance().getFortressByObject(player);
		if(fort == null)
			return false;
		L2Clan clan = player.getClan();
		if(clan == null)
			return false;
		if(clan.getClanId() != fort.getOwnerId())
			return false;
		return true;
	}

	private String enterPrison(L2Player player)
	{
		Fortress fort = FortressManager.getInstance().getFortressByIndex(player.getFortress().getId());
		if(fort == null
				//|| fort.getOwner() != player.getClan()
		)
			return "gludio_fort_a_campkeeper_q0511_01a.htm";

		// Крепость должна быть независимой
//		if(fort.getFortState() != 1)
//			return "gludio_fort_a_campkeeper_q0511_13.htm";

		InstancedZoneManager ilm = InstancedZoneManager.getInstance();
		FastMap<Integer, InstancedZone> ils = ilm.getById(1003);

		if(ils == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return null;
		}

		InstancedZone il = ils.get(0);

		assert il != null;

		String name = il.getName();
		int timelimit = il.getTimelimit();
		int min_level = il.getMinLevel();
		int max_level = il.getMaxLevel();
		int minParty = il.getMinParty();
		int maxParty = il.getMaxParty();
		GArray<L2DoorInstance> dorr = il.getDoors();

		if(minParty > 1 && !player.isInParty())
		{
			player.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
			return null;
		}

		if(player.isInParty())
		{
			if(player.getParty().isInReflection())
			{
				player.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(player));
				return null;
			}

			for(L2Player member : player.getParty().getPartyMembers())
				if(ilm.getTimeToNextEnterInstance(name, member) > 0)
				{
					player.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(member));
					return null;
				}

			if(!player.getParty().isLeader(player))
			{
				player.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_TRY_TO_ENTER);
				return null;
			}

			if(player.getParty().getMemberCount() > maxParty)
			{
				player.sendPacket(Msg.YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT);
				return null;
			}

			for(L2Player member : player.getParty().getPartyMembers())
			{
				if(member.getLevel() < min_level || member.getLevel() > max_level)
				{
					SystemMessage sm = new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member);
					member.sendPacket(sm);
					player.sendPacket(sm);
					return null;
				}
				if(member.getClan() != player.getClan())
					return "gludio_fort_a_campkeeper_q0511_01a.htm";
				if(!player.isInRange(member, 500))
				{
					member.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
					player.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
					return null;
				}
			}
		}

		Prison prison = null;
		if(!_prisons.isEmpty())
		{
			prison = _prisons.get(fort.getId());
			if(prison != null && prison.isLocked())
			{
				// TODO правильное сообщение
				player.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(player));
				return null;
			}
		}

		prison = new Prison(fort.getId(), ils);
		_prisons.put(prison.getFortId(), prison);

		Reflection r = ReflectionTable.getInstance().get(prison.getReflectionId());
		r.setReturnLoc(player.getLoc());
		for (L2DoorInstance dor: dorr){
			dor.closeMe();
			ReflectionTable.getInstance().get(r.getId()).closeDoor(dor.getDoorId());
		}

		for(L2Player member : player.getParty().getPartyMembers())
		{
			if(member != player)
				newQuestState(member, STARTED);
			member.setReflection(r);
			member.teleToLocation(il.getTeleportCoords());
			member.setVar("backCoords", r.getReturnLoc().toXYZString());
			member.setVarInst(name, String.valueOf(System.currentTimeMillis()));
			//member.sendPacket(new ExSendUIEvent(member, false, true, 0, timelimit * 60 * 1000, ""));
		}

		player.getParty().setReflection(r);
		r.setParty(player.getParty());
		r.startCollapseTimer(timelimit * 60 * 1000L);
		player.getParty().broadcastToPartyMembers(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(timelimit));
		for (L2DoorInstance dor: dorr){
			ReflectionTable.getInstance().get(r.getId()).closeDoor(dor.getDoorId());
		}
		for (int i = 145900; i < 146100; i += 50) {
			prison.initSpawnNew(Guard, 153544, i, -12611, true);
		}
		return null;
	}

	private class Prison {
		private int _fortId;
		private int _reflectionId;
		private long _lastEnter;

		private class PrisonSpawnTask extends l2open.common.RunnableImpl {
			int _npcId;
			public PrisonSpawnTask(int npcId) {
				_npcId = npcId;
			}

			public void runImpl()
			{
				addSpawnToInstance(_npcId, new Location(53304, 245992, -6576, 25958), 0, _reflectionId);
			}
		}
		private class PrisonSpawnTaskNew extends l2open.common.RunnableImpl {
			int _npcId;
			int _x, _y, _z;


			public PrisonSpawnTaskNew(int npcId, int x, int y, int z) {
				_npcId = npcId;
				_x = x;
				_y = y;
				_z = z;
			}

			public void runImpl()
			{
				addSpawnToInstance(_npcId, new Location(_x, _y, _z, 16383), 0, _reflectionId);
			}
		}

		public Prison(int id, FastMap<Integer, InstancedZone> ils) {
			try
			{
				Reflection r = new Reflection(ils.get(0).getName());
				r.setInstancedZoneId(1003);
				for(InstancedZone i : ils.values())
				{
					if(r.getTeleportLoc() == null)
						r.setTeleportLoc(i.getTeleportCoords());
					if(i.getDoors() != null)
						for(L2DoorInstance d : i.getDoors())
						{
							L2DoorInstance door = d.clone();
							r.addDoor(door);
							door.setReflection(r);
							door.spawnMe();
						}
				}

				_reflectionId = r.getId();
				_fortId = id;
				_lastEnter = System.currentTimeMillis();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		public void initSpawn(int npcId, boolean first)
		{
			ThreadPoolManager.getInstance().schedule(new PrisonSpawnTask(npcId), first ? 60000 : 180000);
		}
		public void initSpawnNew(int npcId, int x, int y, int z, boolean first)
		{
			ThreadPoolManager.getInstance().schedule(new PrisonSpawnTaskNew(npcId, x, y ,z), first ? 0 : 30000);
		}

		public int getReflectionId()
		{
			return _reflectionId;
		}

		public int getFortId()
		{
			return _fortId;
		}

		public boolean isLocked()
		{
			return System.currentTimeMillis() - _lastEnter < 30 * 60 * 1000L;
		}
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
}