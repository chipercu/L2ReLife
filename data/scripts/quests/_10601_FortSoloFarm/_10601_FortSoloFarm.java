package quests._10601_FortSoloFarm;

import javolution.util.FastMap;
import l2open.common.RunnableImpl;
import l2open.common.ThreadPoolManager;
import l2open.extensions.scripts.Functions;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.cache.Msg;
import l2open.gameserver.instancemanager.InstancedZoneManager;
import l2open.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.Reflection;
import l2open.gameserver.model.instances.L2MinionInstance;
import l2open.gameserver.model.instances.L2MonsterInstance;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.items.L2ItemInstance;
import l2open.gameserver.model.quest.Quest;
import l2open.gameserver.model.quest.QuestState;
import l2open.gameserver.serverpackets.ExSendUIEvent;
import l2open.gameserver.serverpackets.MagicSkillUse;
import l2open.gameserver.serverpackets.SystemMessage;
import l2open.gameserver.tables.ReflectionTable;
import l2open.gameserver.tables.SkillTable;
import l2open.gameserver.xml.ItemTemplates;
import l2open.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

public class _10601_FortSoloFarm extends Quest implements ScriptFile
{
	private final static int DungeonLeaderMark = 9797;
	private final static int RewardMarksCount = 1000; // цифра с потолка
	private final static int KnightsEpaulette = 9912;

	private static final int Tank_Capitan = 36625;
	private static final int Guard = 36626;
	private static final int Archer = 36627;
	private static final int Mage = 36628;
	private static final int Healer = 36629;

	private static final int ENEMY = 36630;
	private static final int ENEMY_WARRIOR = 36632;
	private static final int ENEMY_OVERLORD = 36634;

	private static final int ENEMY_SHAMAN = 36633;

	private static final int ENEMY_CHAMPION = 36635;
	private static final int ENEMY_ARCHER = 36631;

	private static final int [] monsterId = {ENEMY, ENEMY_WARRIOR ,ENEMY_OVERLORD};
	ArrayList<Integer> enemyList = new ArrayList<>(Arrays.asList(ENEMY, ENEMY_WARRIOR ,ENEMY_OVERLORD, ENEMY_ARCHER, ENEMY_CHAMPION));
	//	private static final int[] enemyList = {ENEMY, ENEMY_WARRIOR ,ENEMY_OVERLORD, ENEMY_ARCHER, ENEMY_CHAMPION};

	private L2NpcInstance capitan;
	private L2NpcInstance shaman;
	private L2NpcInstance champion;

	public boolean isSpawnGuard() {
		return spawnGuard;
	}

	public void setSpawnGuard(boolean spawnGuard) {
		this.spawnGuard = spawnGuard;
	}

	private boolean spawnGuard = true;
	private static final Location[] spawnGuardLoc = {
			new Location( 125976, 125640 ,-2584),
			new Location( 126024, 125656 ,-2584),
			new Location( 126072, 125640 ,-2584),
			new Location( 126120, 125656 ,-2584)
			};
	private static final Location[] magePosition = {
			new Location( 125944, 126136 ,-2624),
			new Location( 125832, 126120 ,-2624),
			new Location( 125816, 126216 ,-2632),
			new Location( 125944, 126232 ,-2640)
	};
	private static final Location[] healerPosition = {
			new Location( 125784, 126008 ,-2608),
			new Location( 125960, 126024 ,-2608)
	};
	private static final Location[] archerPosition = {
			new Location( 125768, 126120 ,-2608),
			new Location( 125656, 126088 ,-2576),
			new Location( 125704, 126008 ,-2608),
			new Location( 126040, 126168 ,-2624),
			new Location( 126120, 126072 ,-2592),
			new Location( 126024, 126040 ,-2608)
	};
	private static final Location[] guardPosition = {
			new Location( 125704, 126296 ,-2608),
			new Location( 125816, 126312 ,-2640),
			new Location( 125928, 126328 ,-2640),
			new Location( 126040, 126360 ,-2632)
	};
	ScheduledFuture<?> _guardTask = null;
	private ScheduledFuture<?> _monsterTask = null;
	private ScheduledFuture<?> _championAttack = null;
	private int monsterKillCounter = 0;
	private int monsterSpawnCount = 0;


	public static void set_player(L2Player _player) {
		_10601_FortSoloFarm._player = _player;
	}

	private static Location spawnCapitanLoc = new Location(126120, 125944, -2592);
	private static Location spawnEnemyLoc = new Location(125208, 128408, -3072);
	private static Location moveEnemyLoc = new Location( 125816, 126312 ,-2640);
	private static Location championSpawnLoc = new Location( 124600, 128648 ,-3168);
	private static Location shamanSpawnLoc = new Location( 125368, 128728 ,-2976);
	private List<L2NpcInstance> enemyTarget = new ArrayList<>();
	private static L2Player _player;


	private static FastMap<Integer, Integer> _instances = new FastMap<Integer, Integer>();

	public _10601_FortSoloFarm()
	{
		super(false);

		addStartNpc(35666, 35698, 35735, 35767, 35804, 35835, 35867, 35904, 35936, 35974, 36011, 36043, 36081, 36118, 36149, 36181, 36219, 36257, 36294, 36326, 36364);
		addQuestItem(DungeonLeaderMark, KnightsEpaulette);
		addTalkId(Tank_Capitan);
		addAttackId(Tank_Capitan, Guard);
		addKillId(Tank_Capitan, Guard, ENEMY, ENEMY_WARRIOR ,ENEMY_OVERLORD, ENEMY_SHAMAN, ENEMY_ARCHER, ENEMY_CHAMPION);
	}

	private void makeBuff(L2NpcInstance npc, L2Player player, int skillId, int level)
	{
		GArray<L2Character> target = new GArray<L2Character>();
		target.add(player);
		npc.broadcastSkill(new MagicSkillUse(npc, player, skillId, level, 0, 0));
		npc.callSkill(SkillTable.getInstance().getInfo(skillId, level), target, true);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		L2Player player = st.getPlayer();
		String htmltext = event;
		if(event.equalsIgnoreCase("Enter")) {
			enterInstance(player, st);
			st.setState(STARTED);
			st.setCond(1);
			set_player(player);
		} else if(event.equalsIgnoreCase("Support")) {
			if(st.getInt("spells") < 5)
				htmltext = "32509-06.htm";
			else
				htmltext = "32509-04.htm";
			return htmltext;
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		int id = st.getState();
		L2Player player = st.getPlayer();
		if (st.getState() == 3 && npcId == Tank_Capitan){
			htmltext = "Успешно отбили атаку на форт";
		}

		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		L2Player player = st.getPlayer();
		int npcId = npc.getNpcId();
		int refId = player.getReflection().getId();
		if (npcId == ENEMY || npcId == ENEMY_WARRIOR || npcId == ENEMY_OVERLORD){
				monsterKillCounter++;
				if (monsterKillCounter > 20 && st.getCond() == 2){
					st.setCond(3);
					spawnGuard = true;
					//monsterKillCounter = 0;
					dropItem(capitan, 14, 1);
					Functions.npcSayToPlayer(capitan, player, player.getName() + "Возми мой лук ! Убей незаметно их шамана что бы остоновить атаку!");
				}else if (monsterKillCounter > 10 && st.getCond() == 1){
					st.setCond(2);
					spawnGuard = true;
				}
		}else if (npcId == ENEMY_SHAMAN){
			st.setCond(4);
			spawnGuard = true;
			_championAttack = ThreadPoolManager.getInstance().scheduleAI(new ChampionAttack(), 5000);
			if (!_monsterTask.isCancelled() || _monsterTask != null){
				_monsterTask.cancel(true);
				player.sendMessage("monster task is = " + _monsterTask.isCancelled());
				_monsterTask = null;
				player.sendMessage("monster task is = " + _monsterTask);
			}

		}else if (npcId == ENEMY_CHAMPION){
			st.setCond(5);
			monsterKillCounter = 0;
			//spawnGuard = true;
			spawnGuard = false;
			if (!_guardTask.isCancelled() || _guardTask != null){
				_guardTask.cancel(true);
				_guardTask = null;
			}
			st.getPlayer().sendPacket(new SystemMessage("Атака на форт была успешно отбита!!"));
			for (L2NpcInstance guard: enemyTarget){
				guard.getAI().addTaskMove(spawnGuardLoc[Rnd.get(spawnGuardLoc.length)], true);
			}
			st.setState(COMPLETED);
			st.getPlayer().getReflection().startCollapseTimer(90000);
			st.exitCurrentQuest(true);
			monsterKillCounter = 0;
			monsterSpawnCount = 0;


		}
		return null;
	}

	@Override
	public String onAttack(L2NpcInstance npc, QuestState st)
	{
		L2Player player = st.getPlayer();
		int npcId = npc.getNpcId();
		if (npcId == Guard || npcId == Tank_Capitan){
			Functions.npcSayToPlayer(capitan, player, player.getName() + " !!Не убивай свойх !! о то получишь люлей!");
		}
		return null;
	}

	private void enterInstance(L2Player player, QuestState qs) {

		int instancedZoneId = 1004;
		InstancedZoneManager ilm = InstancedZoneManager.getInstance();
		FastMap<Integer, InstancedZone> ils = ilm.getById(instancedZoneId);
		if(ils == null) {
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}
		InstancedZone il = ils.get(0);

		assert il != null;

		if(player.isInParty()) {
			player.sendPacket(Msg.A_PARTY_CANNOT_BE_FORMED_IN_THIS_AREA);
			return;
		} else if(player.isCursedWeaponEquipped()) {
			player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
			return;
		}

		Integer old = _instances.get(player.getObjectId());
		if(old != null) {
			Reflection old_r = ReflectionTable.getInstance().get(old);
			if(old_r != null) {
				player.setReflection(old_r);
				player.teleToLocation(il.getTeleportCoords());
				player.setVar("backCoords", old_r.getReturnLoc().toXYZString());
				return;
			}
		}
		Reflection r = new Reflection(il.getName());
		r.setInstancedZoneId(instancedZoneId);
		for(InstancedZone i : ils.values()) {
			if(r.getReturnLoc() == null)
				r.setReturnLoc(i.getReturnCoords());
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
			r.FillSpawns(i.getSpawnsInfo());
		}
		int timelimit = il.getTimelimit();

		player.setReflection(r);
		player.teleToLocation(il.getTeleportCoords());
		player.setVar("backCoords", r.getReturnLoc().toXYZString());
		player.sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(timelimit));
		player.sendPacket(new ExSendUIEvent(player, false, false, 60, 0, "До первой волны атаки"));
		r.setNotCollapseWithoutPlayers(true);
		r.startCollapseTimer(timelimit * 60 * 1000L);
		_instances.put(player.getObjectId(), r.getId());
		capitan = addSpawnToInstance(Tank_Capitan, spawnCapitanLoc, 0, player.getReflectionId());
		shaman = addSpawnToInstance(ENEMY_SHAMAN, shamanSpawnLoc, 0, player.getReflectionId());
		champion = addSpawnToInstance(ENEMY_CHAMPION, championSpawnLoc, 0, player.getReflectionId());
		capitan.setCurrentHpMp(capitan.getMaxHp(), capitan.getMaxMp());
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				Functions.npcSayToPlayer(capitan, player, player.getName() + " !! Готовся к бою! Разведчики доложили что враг уже на подходе!");
			}
		}, 5000);
		_guardTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AddGuards(qs, true), 10000, 5000);
		_monsterTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new EnemyAttack(monsterId, qs), 60000, 15000);
	}

	private void dropItem(L2NpcInstance npc, int itemId, int count) {
		L2ItemInstance item = ItemTemplates.getInstance().createItem(itemId);
		item.setCount(count);
		item.dropMe(npc, npc.getLoc());
	}
	private class ChampionAttack extends RunnableImpl{
		private void attack(){
			L2NpcInstance attaker = champion;
			L2NpcInstance hated = capitan;


			attaker.setRunning();
			attaker.getAI().addTaskMove(spawnEnemyLoc, true);

			if (hated != null) {
				hated.addDamageHate(attaker, 0, Rnd.get(1, 100)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
			}

			attaker.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
			attaker.getAI().setAttackTarget(hated); // На всякий случай, не обязательно делать
			attaker.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null); // Переводим в состояние атаки
			attaker.getAI().addTaskAttack(hated); // Добавляем отложенное задание атаки, сработает в самом конце движения
			ThreadPoolManager.getInstance().schedule(() -> {
				attaker.getAI().addTaskAttack(hated);
			}, 10000);

			L2MonsterInstance master = (L2MonsterInstance) champion;
			final List<L2MinionInstance> spawnedMinions = master.getMinionList().getSpawnedMinions();
			for(L2MinionInstance minion: spawnedMinions){
				minion.setRunning();
				minion.getAI().addTaskMove(spawnEnemyLoc, true);
				if (hated != null) {
					hated.addDamageHate(minion, 0, Rnd.get(1, 100)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
				}
				ThreadPoolManager.getInstance().schedule(() -> {
					attaker.getAI().addTaskAttack(hated);
					Functions.npcSay(attaker, " Атакуем!!!"); //TODO переделать или убрать
				}, 10000);
				minion.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
				minion.getAI().setAttackTarget(hated); // На всякий случай, не обязательно делать
				minion.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null); // Переводим в состояние атаки
				minion.getAI().addTaskAttack(hated); // Добавляем отложенное задание атаки, сработает в самом конце движения
			}
		}
		@Override
		public void runImpl() throws Exception {
			attack();
		}
	}

	private class EnemyAttack extends RunnableImpl{
		int[] id;
		QuestState qs;

		public EnemyAttack(int [] id,QuestState qs) {

			this.id = id;
			this.qs = qs;
		}
		private void spawnMonster(){
			L2NpcInstance guard = addSpawnToInstance(id[Rnd.get(monsterId.length)], spawnEnemyLoc, 150, qs.getPlayer().getReflection().getId() );
			guard.setRunning();
			guard.getAI().addTaskMove(moveEnemyLoc, true);
			L2NpcInstance hated = enemyTarget.get(Rnd.get(enemyTarget.size()));
			if (hated != null) {
				hated.addDamageHate(guard, 0, Rnd.get(1, 100)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
			}
			guard.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
			guard.getAI().setAttackTarget(hated); // На всякий случай, не обязательно делать
			guard.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null); // Переводим в состояние атаки
			guard.getAI().addTaskAttack(hated); // Добавляем отложенное задание атаки, сработает в самом конце движения
		}

		@Override
		public void runImpl() throws Exception {
			if (monsterSpawnCount < 50) {
				for (int i = 0; i < Rnd.get(2, 4); i++) {
					spawnMonster();
					monsterSpawnCount++;
				}
			}
		}
	}

	private class AddGuards extends RunnableImpl {
		private boolean doSpawn;
		public boolean isDoSpawn() {
			return doSpawn;
		}
		public void setDoSpawn(boolean doSpawn) {
			this.doSpawn = doSpawn;
		}

		private final QuestState qs;
		public AddGuards(QuestState qs, boolean doSpawn) {
			this.qs = qs;
			this.doSpawn = doSpawn;
		}
		private void guardPrepare(L2NpcInstance guard, Location[] moveLoc, int ord){
			guard.setCurrentHpMp(guard.getMaxHp(), guard.getMaxMp());
			guard.setRunning();
			guard.getAI().addTaskMove(moveLoc[ord], true);
			guard.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
				guard.getAI().setAttackTarget(guard.getRandomHated()); // На всякий случай, не обязательно делать
				guard.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, guard.getRandomHated(), null); // Переводим в состояние атаки
				guard.getAI().addTaskAttack(guard.getRandomHated()); // Добавляем отложенное задание атаки, сработает в самом конце движения
		}


		private void spawnGuard(int id, Location [] spawnLoc, Location[] moveLoc){
			for (int i = 0; i < spawnLoc.length; i++) {
				L2NpcInstance guard = addSpawnToInstance(id, spawnLoc[i], 0, qs.getPlayer().getReflection().getId());
				guardPrepare(guard, moveLoc, i);
				enemyTarget.add(guard);
			}
		}
		private void spawnHealer(int id, Location [] spawnLoc, Location[] moveLoc){
			for (int i = 0; i < moveLoc.length; i++) {
				L2NpcInstance guard = addSpawnToInstance(id, spawnLoc[i], 0, qs.getPlayer().getReflection().getId());
				guardPrepare(guard, moveLoc, i);
				enemyTarget.add(guard);
			}
		}

		@Override
		public void runImpl() {
			int cond = qs.getCond();
			if (cond == 1){
				if (monsterKillCounter == 0 && isDoSpawn()){
					//spawnGuard = true;
					setDoSpawn(false);
					Functions.npcSay(capitan, "Стражники! По местам!!!");
					spawnGuard(Guard, spawnGuardLoc, guardPosition);
				}
			}else if (cond == 2) {
				if (monsterKillCounter >= 10 && !isDoSpawn()){
					setDoSpawn(true);
					Functions.npcSay(capitan, "Лучники! На позиции!");
					spawnGuard(Archer, spawnGuardLoc, archerPosition);
				}
			} else if (cond == 3 ) {
				if (monsterKillCounter >= 20 && isDoSpawn()) {
					setDoSpawn(false);
					Functions.npcSay(capitan, "Лекари нужна ваша помошь!!");
					spawnHealer(Healer, spawnGuardLoc, healerPosition);

				}
			}else if (cond == 4){
				if (monsterKillCounter >= 20 && !isDoSpawn()) {
					setDoSpawn(true);
					Functions.npcSay(capitan, "Маги на позиции!! Враги наступают");
					spawnGuard(Mage, spawnGuardLoc, magePosition);

				}
			}else if (cond == 5){
				setDoSpawn(false);
				if (!_monsterTask.isCancelled() || _monsterTask != null){
					_monsterTask.cancel(true);
					_monsterTask = null;
				}
				if (!_guardTask.isCancelled() || _guardTask != null){
					_guardTask.cancel(true);
					_guardTask = null;
				}
				qs.getPlayer().sendPacket(new SystemMessage("Атака на форт была успешно отбита!!"));
				for (L2NpcInstance guard: enemyTarget){
					guard.getAI().addTaskMove(spawnGuardLoc[Rnd.get(spawnGuardLoc.length)], true);
				}
				qs.setState(COMPLETED);
				qs.getPlayer().getReflection().startCollapseTimer(60000);
			}
			//_player.sendMessage("questCond = " + cond);

			for (L2NpcInstance npc : enemyTarget){
				final GArray<L2Character> aroundCharacters = npc.getAroundCharacters(500, 300);
				for (L2Character cha: aroundCharacters){
					if (enemyList.contains((Integer) cha.getNpcId())){
						npc.getAI().addTaskAttack(cha);

						npc.setTitle(cha.getName()); //TODO дебаг

						return;
					}
				}
			}
		}
	}


	public void onLoad()
	{
		ScriptFile._log.info("Loaded Quest: 10601: Fort Solo Farm");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}