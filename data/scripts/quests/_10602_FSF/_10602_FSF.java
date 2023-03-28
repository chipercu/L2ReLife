package quests._10602_FSF;

import javolution.util.FastMap;
import l2open.common.ThreadPoolManager;
import l2open.config.ConfigValue;
import l2open.extensions.scripts.Functions;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.cache.Msg;
import l2open.gameserver.idfactory.IdFactory;
import l2open.gameserver.instancemanager.InstancedZoneManager;
import l2open.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Effect;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.Reflection;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.items.L2ItemInstance;
import l2open.gameserver.model.quest.Quest;
import l2open.gameserver.model.quest.QuestState;
import l2open.gameserver.serverpackets.ExSendUIEvent;
import l2open.gameserver.serverpackets.MagicSkillUse;
import l2open.gameserver.serverpackets.SystemMessage;
import l2open.gameserver.tables.NpcTable;
import l2open.gameserver.tables.ReflectionTable;
import l2open.gameserver.tables.SkillTable;
import l2open.gameserver.templates.L2NpcTemplate;
import l2open.gameserver.xml.ItemTemplates;
import l2open.util.Files;
import l2open.util.GArray;
import l2open.util.Location;
import l2open.util.Rnd;

import java.util.concurrent.ScheduledFuture;

public class _10602_FSF extends Quest implements ScriptFile {

	private static ScheduledFuture<?> showThread;
	private final static int DungeonLeaderMark = 9797;
	private final static int RewardMarksCount = 1000; // цифра с потолка
	private final static int KnightsEpaulette = 9912;

	private static final int Guard = 35082;
	private static final int Archer = 35720;
	private static final int Mage = 35717;
	private static final int Healer = 35718;

	private static final int Ministr = 35722;
	private static final int Mage_Capitan = 35716;
	private static final int Tank_Capitan = 35713;
	private static final int DD_Capitan = 35721;
	private static final int Archer_Capitan = 35719;
	private static final int Commander = 35771;
	private static int guard_count = 4;
	private static int archer_count = 4;
	private static int mage_count = 4;
	private static int healer_count = 2;
	private static int guard_count_max = 4;
	private static int archer_count_max = 4;
	private int mage_count_max = 4;
	private int healer_count_max = 2;

	public  L2Player get_player() {
		return _player;
	}

	public void set_player(L2Player _player) {
		this._player = _player;
	}

	private L2Player _player;


//	private L2NpcInstance Guard = new L2NpcInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(35082));
//	private L2NpcInstance Acher = new L2NpcInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(35720));
//	private L2NpcInstance Mage = new L2NpcInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(35717));
//	private L2NpcInstance Healer = new L2NpcInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(35718));
	public  L2NpcInstance target;

	public  L2NpcInstance getTarget() {
		return target;
	}
	public void setTarget(L2NpcInstance target) {
		this.target = target;
	}

	private static final int targetId = 36624;
	private static Location target_loc = new Location(125816, 126536, -2680);

	public int getGuard_count() {
		return guard_count;
	}
	public void setGuard_count(int guard_count) {
		this.guard_count = guard_count;
	}

	public int getArcher_count() {
		return archer_count;
	}
	public void setArcher_count(int archer_count) {
		this.archer_count = archer_count;
	}

	public int getMage_count() {
		return mage_count;
	}
	public void setMage_count(int mage_count) {
		this.mage_count = mage_count;
	}

	public int getHealer_count() {
		return healer_count;
	}
	public  void setHealer_count(int healer_count) {
		this.healer_count = healer_count;
	}





	private static FastMap<Integer, Integer> _instances = new FastMap<Integer, Integer>();

	public _10602_FSF()
	{
		super(false);

		addStartNpc(35666, 35698, 35735, 35767, 35804, 35835, 35867, 35904, 35936, 35974, 36011, 36043, 36081, 36118, 36149, 36181, 36219, 36257, 36294, 36326, 36364);
		addQuestItem(DungeonLeaderMark, KnightsEpaulette);
		addTalkId(targetId);
		addAttackId(targetId, Guard, Archer, Mage, Healer);
//		addKillId(LATANA);
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
			enterInstance(player);
			set_player(player);
			return null;
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
		if(npcId == targetId) {
			return talkPanel();
		}
//		else if(npcId == KOSUPPORTER)
//		{
//			if(cond == 1 || cond == 2)
//				htmltext = "32502-01.htm";
//			else
//				htmltext = "32502-05.htm";
//		}
//		else if(npcId == KOIO)
//		{
//			if(st.getQuestItemsCount(SPEAR) > 0 && st.getQuestItemsCount(STAGE1) == 0)
//				htmltext = "32509-01.htm";
//			if(st.getQuestItemsCount(ENCHSPEAR) > 0 && st.getQuestItemsCount(STAGE2) == 0)
//				htmltext = "32509-01.htm";
//			if(st.getQuestItemsCount(SPEAR) == 0 && st.getQuestItemsCount(STAGE1) > 0)
//				htmltext = "32509-07.htm";
//			if(st.getQuestItemsCount(ENCHSPEAR) == 0 && st.getQuestItemsCount(STAGE2) > 0)
//				htmltext = "32509-07.htm";
//			if(st.getQuestItemsCount(SPEAR) == 0 && st.getQuestItemsCount(ENCHSPEAR) == 0)
//				htmltext = "32509-07.htm";
//			if(st.getQuestItemsCount(STAGE1) == 0 && st.getQuestItemsCount(STAGE2) == 0)
//				htmltext = "32509-01.htm";
//			if(st.getQuestItemsCount(SPEAR) > 0 && st.getQuestItemsCount(STAGE1) > 0) {
//				st.takeItems(SPEAR, 1);
//				st.takeItems(STAGE1, 1);
//				player.getEffectList().stopEffect(8239);
//				st.giveItems(ENCHSPEAR, 1);
//				htmltext = "32509-02.htm";
//			}
//			if(st.getQuestItemsCount(ENCHSPEAR) > 0 && st.getQuestItemsCount(STAGE2) > 0)
//			{
//				st.takeItems(ENCHSPEAR, 1);
//				st.takeItems(STAGE2, 1);
//				st.giveItems(LASTSPEAR, 1);
//				player.getEffectList().stopEffect(8239);
//				htmltext = "32509-03.htm";
//			}
//			if(st.getQuestItemsCount(LASTSPEAR) > 0)
//				htmltext = "32509-03.htm";
//		}
//		else if(npcId == KOSUPPORTER2)
//		{
//			if(cond == 4)
//			{
//				st.giveItems(ScrollOfEscape, 1);
//				st.giveItems(PSHIRT, 1);
//				st.addExpAndSp(28000000, 2850000);
//				st.set("cond", "5");
//				st.setState(COMPLETED);
//				st.playSound(SOUND_FINISH);
//				st.exitCurrentQuest(false);
//				player.setVitality(20000);
//				player.getReflection().startCollapseTimer(60000);
//				htmltext = "32512-01.htm";
//			}
//			else if(id == COMPLETED)
//				htmltext = "32512-03.htm";
//		}
		return talkPanel();
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		L2Player player = st.getPlayer();
		int npcId = npc.getNpcId();
		int refId = player.getReflection().getId();

//		switch(npcId)
//		{
//			case VSWARRIOR1:
//			case VSWARRIOR2:
//				if(st.getInt("stage") == 1)
//					st.set("stage", "2");
//				break;
//			case VSCOMMAO1:
//			case VSCOMMAO2:
//				if(st.getInt("stage") == 2)
//					st.set("stage", "3");
//				if(st.getQuestItemsCount(SPEAR) > 0 && st.getQuestItemsCount(STAGE1) == 0)
//					st.giveItems(STAGE1, 1);
//				break;
//			case VSGMAG1:
//			case VSGMAG2:
//				if(st.getInt("stage") == 3)
//					st.set("stage", "4");
//				if(st.getQuestItemsCount(ENCHSPEAR) > 0 && st.getQuestItemsCount(STAGE2) == 0)
//					st.giveItems(STAGE2, 1);
//				break;
//			case VSHGAPG1:
//			case VSHGAPG2:
//				if(st.getInt("stage") == 4)
//					st.set("stage", "5");
//				break;
//			case LATANA:
//				st.setCond(4);
//				st.playSound(SOUND_MIDDLE);
//				addSpawnToInstance(KOSUPPORTER2, npc.getLoc(), 0, refId);
//				break;
//		}
//
//		if(contains(Pailaka3rd, npcId))
//		{
//			if(Rnd.get(100) < 30)
//				dropItem(npc, PAILAKA3DROP[Rnd.get(PAILAKA3DROP.length)], 1);
//		}
//
//		if(contains(Antelopes, npcId))
//			dropItem(npc, ANTELOPDROP[Rnd.get(ANTELOPDROP.length)], Rnd.get(1, 10));

		return null;
	}

	@Override
	public String onAttack(L2NpcInstance npc, QuestState st)
	{
		L2Player player = st.getPlayer();
		int npcId = npc.getNpcId();
		switch(npcId)
		{
//			case Guard:
//			case Archer:
//			case Mage:
//			case Healer:
//				ThreadPoolManager.getInstance().execute( new ShowPanelTask(player));
//				break;
			case targetId:
				npc.setTitle(get_player().getName());
				showPanel(get_player());
				ThreadPoolManager.getInstance().execute( new ShowPanelTask(player));
				break;
		}
		return null;
	}

	private void enterInstance(L2Player player) {

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
		player.sendPacket(new ExSendUIEvent(player, false, false, 180, 0, "До первой волны атаки"));
		r.setNotCollapseWithoutPlayers(true);
		r.startCollapseTimer(timelimit * 60 * 1000L);
		_instances.put(player.getObjectId(), r.getId());
		target = addSpawnToInstance(targetId, target_loc, 0, player.getReflectionId());
		showPanel(player);

		showThread = ThreadPoolManager.getInstance().scheduleAtFixedDelay(new ShowPanelTask(player), 1000, 5000);

	}

	@Override
	public void onAbort(QuestState qs) {
		exitToInstance();
		super.onAbort(qs);
	}

	public String talkPanel(){
		StringBuilder dialog = new StringBuilder("<html><body>");
		dialog.append("Выстовите охрану пока враг еще не напал");
		dialog.append("<table border=0  width=105%>");

		dialog.append("<tr>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"up\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:moveUp").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"down\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:moveDown").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"left\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:moveLeft").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"right\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:moveRight").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("</tr>")
				.append("<tr>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"guard\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:spawnGuard").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"archer\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:spawnArcher").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"mage\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:spawnMage").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"healer\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:spawnHealer").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("</tr>")
				.append("<tr>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"exit\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:exitToInstance").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("</tr>");


		dialog.append("</table></body></html>");
		return dialog.toString();
	}

	public void showPanel(L2Player player){
		StringBuilder dialog = new StringBuilder("<html><body>");

		dialog.append("Выстовите охрану пока враг еще не напал");
		dialog.append("<table border=0  width=105%>");

		dialog.append("<tr>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"up\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:moveUp").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"down\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:moveDown").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"left\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:moveLeft").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"right\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:moveRight").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("</tr>")
//				.append("<tr>")
//				.append("<td width=32 align=center valign=top>").append("<button value=\"left\" action=\"bypass -h scripts_quests._10601_FortSoloFarm._10601_FortSoloFarm:moveGuards left").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
//				.append("<td width=32 align=center valign=top>").append("<button value=\"right\" action=\"bypass -h scripts_quests._10601_FortSoloFarm._10601_FortSoloFarm:moveGuards right").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
//				.append("<td width=32 align=center valign=top>").append("<button value=\"up\" action=\"bypass -h scripts_quests._10601_FortSoloFarm._10601_FortSoloFarm:moveGuards up").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
//				.append("<td width=32 align=center valign=top>").append("<button value=\"down\" action=\"bypass -h scripts_quests._10601_FortSoloFarm._10601_FortSoloFarm:moveGuards down").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
//				.append("</tr>")
				.append("<tr>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"guard\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:spawnGuard").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"archer\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:spawnArcher").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"mage\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:spawnMage").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"healer\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:spawnHealer").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("</tr>")
				.append("<tr>")
				.append("<td width=32 align=center valign=top>").append("<button value=\"exit\" action=\"bypass -h scripts_quests._10602_FSF._10602_FSF:exitToInstance").append("\" width=32 height=32 back=\"icon.skill0997\" fore=\"icon.skill0997\"/>").append("</td>")
				.append("</tr>");


		dialog.append("</table></body></html>");

		Functions.show(dialog.toString(), get_player(), null);

	}
	public void moveGuards(String direction){
		if (target != null)
		if (direction.equals("left")){
			target.teleToLocation(target.getX() + 50, target.getY(), target.getZ(), target.getReflection().getId());
		}else if (direction.equals("right")){
			target.teleToLocation(target.getX() - 50, target.getY(), target.getZ(), target.getReflection().getId());
		}else if (direction.equals("up")){
			target.teleToLocation(target.getX(), target.getY() + 50, target.getZ(), target.getReflection().getId());
		}else if (direction.equals("down")){
			target.teleToLocation(target.getX(), target.getY() - 50, target.getZ(), target.getReflection().getId());
		}
		showPanel(get_player());

	}




	public void moveUp(){
		if (getTarget() != null){
		getTarget().teleToLocation(getTarget().getX() + 50, getTarget().getY(), getTarget().getZ(), getTarget().getReflection().getId());
		}
		//showPanel(get_player());
	}
	public void moveDown(){
		if (getTarget() != null){
			getTarget().teleToLocation(getTarget().getX() - 50, getTarget().getY(), getTarget().getZ(), getTarget().getReflection().getId());
		}
		//showPanel(get_player());
	}
	public void moveLeft(){
		if (getTarget() != null){
			getTarget().teleToLocation(getTarget().getX(), getTarget().getY() + 50, getTarget().getZ(), getTarget().getReflection().getId());
		}
		//showPanel(get_player());
	}
	public void moveRight(){
		if (getTarget() != null){
			getTarget().teleToLocation(getTarget().getX(), getTarget().getY() - 50, getTarget().getZ(), getTarget().getReflection().getId());
		}
		//showPanel(get_player());
	}

	public void spawnGuard(){
		if (getGuard_count() > 0) {
			addSpawnToInstance(Guard, getTarget().getLoc(), 0, getTarget().getReflection().getId());
			setGuard_count(getGuard_count() - 1);
		}
		showPanel(get_player());
	}
	public void spawnArcher(){
		if (getArcher_count() > 0) {
			L2NpcInstance npc = addSpawnToInstance(Archer, getTarget().getLoc(), 0, getTarget().getReflection().getId());
			npc.setTitle(get_player().getFortress().getName());
			setArcher_count(getArcher_count() - 1);
		}
		showPanel(get_player());
	}
	public void spawnMage(){
		if (getMage_count() > 0) {
			addSpawnToInstance(Mage, getTarget().getLoc(), 0, getTarget().getReflection().getId());
			setMage_count(getMage_count() - 1);
		}
		showPanel(get_player());
	}
	public void spawnHealer(){
		if (getHealer_count() > 0){
			addSpawnToInstance(Healer, getTarget().getLoc(), 0 , getTarget().getReflection().getId());
			setHealer_count(getHealer_count() - 1);
		}
		showPanel(get_player());
	}


	public void exitToInstance(){
		Reflection r = ReflectionTable.getInstance().get(getTarget().getReflectionId());
		if(r != null) {
			r.startCollapseTimer(1000);
			showThread.cancel(true);
		}
	}



	private void dropItem(L2NpcInstance npc, int itemId, int count) {
		L2ItemInstance item = ItemTemplates.getInstance().createItem(itemId);
		item.setCount(count);
		item.dropMe(npc, npc.getLoc());
	}

	private class ShowPanelTask extends l2open.common.RunnableImpl {
		L2Player _player;
		public ShowPanelTask(L2Player player) {
			_player = player;
		}
		public void runImpl()
		{
			showPanel(_player);
		}
	}

	public void onLoad()
	{
		ScriptFile._log.info("Loaded Quest: 10602: Fort Solo Farm");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}