package events.KetraVarkaWar;

import gnu.trove.list.array.TIntArrayList;
import l2open.common.ThreadPoolManager;
import l2open.config.ConfigValue;
import l2open.extensions.listeners.L2ZoneEnterLeaveListener;
import l2open.extensions.multilang.CustomMessage;
import l2open.extensions.scripts.Functions;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.clientpackets.Say2C;
import l2open.gameserver.geodata.GeoEngine;
import l2open.gameserver.instancemanager.CastleManager;
import l2open.gameserver.instancemanager.ServerVariables;
import l2open.gameserver.instancemanager.ZoneManager;
import l2open.gameserver.model.*;
import l2open.gameserver.model.L2Zone.ZoneType;
import l2open.gameserver.model.base.Experience;
import l2open.gameserver.model.entity.olympiad.Olympiad;
import l2open.gameserver.model.entity.residence.Castle;
import l2open.gameserver.model.entity.siege.territory.TerritorySiege;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.items.L2ItemInstance;
import l2open.gameserver.model.items.LockType;
import l2open.gameserver.serverpackets.*;
import l2open.gameserver.skills.AbnormalVisualEffect;
import l2open.util.*;
import l2open.util.reference.HardReference;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public class KetraVarkaWar extends Functions implements ScriptFile {
    private static Logger _log = Logger.getLogger(KetraVarkaWar.class.getName());
    private static Reflection reflection = null;

    private static final String eventName = "War of Ketra vs Varka";
    private static final String titleKetra = "Лагерь Кетра";
    private static final String titleVakra = "Лагерь Варка";

    public class StartTask extends l2open.common.RunnableImpl {
        public void runImpl() {
            if (!_active) {
                startTimerTask();
                return;
            }

            if (isPvPEventStarted()) {
                _log.info("CtF not started: another event is already running");
                startTimerTask();
                return;
            }

            if (TerritorySiege.isInProgress()) {
                _log.info("CtF not started: TerritorySiege in progress");
                startTimerTask();
                return;
            }

            for (Castle c : CastleManager.getInstance().getCastles().values()) {
                if (c.getSiege() != null && c.getSiege().isInProgress()) {
                    _log.info("CtF not started: CastleSiege in progress");
                    startTimerTask();
                    return;
                }
            }

            if (false)
                start(new String[]{"1", "1"});
            else
                start(new String[]{"-1", "-1"});
        }
    }

    private static ScheduledFuture<?> _startTask;

    private static GCSArray<HardReference<L2Player>> varka_team = new GCSArray<HardReference<L2Player>>();
    private static GCSArray<HardReference<L2Player>> ketra_team = new GCSArray<HardReference<L2Player>>();

    private static final int ketraBaseID = 36667;
    private static final int varkaBaseID = 36668;
    private static final int iBaseID = 36666;

    private static L2NpcInstance ketraBase = null;
    private static L2NpcInstance varkaBase = null;
    private static L2NpcInstance _iBase1 = null;
    private static L2NpcInstance _iBase2 = null;
    private static L2NpcInstance _iBase3 = null;
    private static L2NpcInstance _iBase4 = null;
    private static L2NpcInstance _iBase5 = null;

    private static final Location ketraBaseLoc = new Location(127672, -41672, -3536);
    private static final Location varkaBaseLoc = new Location(108056, -53448, -2480);
    private static final Location[] intermediaryBaseLoc = {
            new Location(123064, -46088, -2976), new Location(114424, -46552, -2576),
            new Location(121624, -54504, -2400), new Location(113016, -55528, -2816),
            new Location(107944, -47096, -2048),
    };

    private static boolean _isRegistrationActive = false;
    private static int _status = 0;
    private static int _time_to_start;
    private static int _category;

    private static int _minLevel;
    private static int _maxLevel;
    private static final int _bonusItemID = 57;
    private static final double _bonusItemCount = 1000000L;

    private static int _autoContinue = 0;
    public static int[] KetraVarkaWarStart = {19, 30};
    private static final int[] minLevelForCategory = {20, 30, 40, 52, 62, 76};
    private static final int[] maxLevelForCategory = {29, 39, 51, 61, 75, 85};

    private static ScheduledFuture<?> _endTask;

    private static final L2Zone _zone = ZoneManager.getInstance().getZoneByIndex(ZoneType.battle_zone, 9004, true);
    ZoneListener _zoneListener = new ZoneListener();

    private static List<Long> time2 = new ArrayList<Long>();

    private void initTimer(boolean new_day) {
        time2.clear();

        //TODO: временный затык для запуска ивента на следующей минуте после перезагрузке скрипта
        LocalTime time = LocalTime.now();
        KetraVarkaWarStart[0] = time.getHour();
        KetraVarkaWarStart[1] = time.getMinute() + 1;

        if (KetraVarkaWarStart[0] == -1)
            return;
        long cur_time = System.currentTimeMillis();
        for (int i = 0; i < KetraVarkaWarStart.length; i += 2) {
            Calendar ci = Calendar.getInstance();
            if (new_day)
                ci.add(Calendar.HOUR_OF_DAY, 12);
            ci.set(Calendar.HOUR_OF_DAY, KetraVarkaWarStart[i]);
            ci.set(Calendar.MINUTE, KetraVarkaWarStart[i + 1]);
            ci.set(Calendar.SECOND, 00);

            long delay = ci.getTimeInMillis();
            if (delay - cur_time > 0)
                time2.add(delay);
            ci = null;
        }
        Collections.sort(time2);
        long delay = 0;
        while (time2.size() != 0 && (delay = time2.remove(0)) - cur_time <= 0) ;
        if (_startTask != null) {
            _startTask.cancel(true);
        }
        if (delay - cur_time > 0) {
            for (L2Player player : L2ObjectsStorage.getPlayers()) {
                if (player.isGM()) {
                    player.sendMessage("Load Event : " + eventName + " - " + (delay - cur_time) / 1000);
                }
            }
            _startTask = ThreadPoolManager.getInstance().schedule(new StartTask(), delay - cur_time);
        }


    }

    public void onLoad() {
        if (_active) {
            _zone.getListenerEngine().addMethodInvokedListener(_zoneListener);

            initTimer(false);

            _active = ServerVariables.getString("KetraVarkaWar", "on").equalsIgnoreCase("on");

            if (!_active)
                _log.info("Loaded Event: " + eventName + " not active.");
            else
                _log.info("Loaded Event: " + eventName + " active.");

            for (L2Player player : L2ObjectsStorage.getPlayers()) {
                if (player.isGM()) {
                    player.sendMessage("Load Event : " + eventName + " - " + _active);
                }
            }

        } else {
            for (L2Player player : L2ObjectsStorage.getPlayers()) {
                if (player.isGM()) {
                    player.sendMessage("Load Event : " + eventName + " - " + _active);
                }
            }
        }
    }

    public void onReload() {
        if (_zone != null)
            _zone.getListenerEngine().removeMethodInvokedListener(_zoneListener);
        if (_startTask != null)
            _startTask.cancel(true);
    }

    public void onShutdown() {
        onReload();
    }

    private static boolean _active = false;

    public static boolean isActive() {
        return _active;
    }

    public void activateEvent() {
        L2Player player = (L2Player) getSelf();
        if (!player.getPlayerAccess().IsEventGm)
            return;

        if (!isActive()) {
            if (_startTask == null)
                initTimer(false);
            ServerVariables.set(eventName, "on");
            _log.info("Event '" + eventName + "' activated.");
            //Announcements.getInstance().announceByCustomMessage("scripts.events.CtF.AnnounceEventStarted", null);
        } else
            player.sendMessage("Event '" + eventName + "' already active.");

        _active = true;

        show(Files.read("data/html/admin/events.htm", player), player);
    }

    public void deactivateEvent() {
        L2Player player = (L2Player) getSelf();
        if (!player.getPlayerAccess().IsEventGm)
            return;

        if (isActive()) {
            if (_startTask != null) {
                _startTask.cancel(true);
                _startTask = null;
            }
            ServerVariables.unset("KetraVarkaWar");
            _log.info("Event '" + eventName + "' deactivated.");
        } else
            player.sendMessage("Event '" + eventName + "' not active.");

        _active = false;

        show(Files.read("data/html/admin/events.htm", player), player);
    }

    public static boolean isRunned() {
        return _isRegistrationActive || _status > 0;
    }

    public String DialogAppend_31225(Integer val) {
        if (val == 0) {
            L2Player player = (L2Player) getSelf();
            return Files.read("data/scripts/events/KetraVarkaWar/31225.html", player);
        }
        return "";
    }

    // KetraBase
    public String DialogAppend_36647(Integer val) {
        if (_status < 2)
            return "";
        L2Player player = (L2Player) getSelf();
        if (player.getTeam() != 1)
            return "";
        if (val == 0)
            return Files.read("data/scripts/events/KetraVarkaWar/35423.html", player).replaceAll("n1", "" + Rnd.get(100, 999)).replaceAll("n2", "" + Rnd.get(100, 999));
        return "";
    }

    // VarkaBase
    public String DialogAppend_36648(Integer val) {
        if (_status < 2)
            return "";
        L2Player player = (L2Player) getSelf();
        if (player.getTeam() != 2)
            return "";
        if (val == 0)
            return Files.read("data/scripts/events/KetraVarkaWar/35426.html", player).replaceAll("n1", "" + Rnd.get(100, 999)).replaceAll("n2", "" + Rnd.get(100, 999));
        return "";
    }

    public void start(String[] var) {
        L2Player player = (L2Player) getSelf();
        if (var.length != 2) {
            if (player != null)
                player.sendMessage(new CustomMessage("common.Error", player));
            return;
        }

        int category;
        int autoContinue;
        try {
            category = Integer.parseInt(var[0]);
            autoContinue = Integer.parseInt(var[1]);
        } catch (Exception e) {
            if (player != null)
                player.sendMessage(new CustomMessage("common.Error", player));
            return;
        }

        _category = category;
        _autoContinue = autoContinue;

        _minLevel = 1;
        _maxLevel = 85;

        if (_endTask != null) {
            if (player != null)
                player.sendMessage(new CustomMessage("common.TryLater", player));
            return;
        }
        reflection = new Reflection("KetraVarkaWarInstances");
        reflection.setGeoIndex(GeoEngine.NextGeoIndex(23, 16, reflection.getId()));

        _status = 0;
        _isRegistrationActive = true;
        _time_to_start = 1;

        varka_team = new GCSArray<HardReference<L2Player>>();
        ketra_team = new GCSArray<HardReference<L2Player>>();

        if (ketraBase != null)
            ketraBase.deleteMe();
        if (varkaBase != null)
            varkaBase.deleteMe();

        ketraBase = spawn(ketraBaseLoc, ketraBaseID, 0, reflection.getId());
        varkaBase = spawn(varkaBaseLoc, varkaBaseID, 0, reflection.getId());
        ketraBase.setTitle(titleKetra);
        varkaBase.setTitle(titleVakra);
        _iBase1 = spawn(intermediaryBaseLoc[0], iBaseID, 2, reflection.getId());
        _iBase2 = spawn(intermediaryBaseLoc[1], iBaseID, 2, reflection.getId());
        _iBase3 = spawn(intermediaryBaseLoc[2], iBaseID, 2, reflection.getId());
        _iBase4 = spawn(intermediaryBaseLoc[3], iBaseID, 2, reflection.getId());
        _iBase5 = spawn(intermediaryBaseLoc[4], iBaseID, 2, reflection.getId());
        _iBase1.setTitle(titleVakra);
        _iBase2.setTitle(titleVakra);
        _iBase3.setTitle(titleVakra);
        _iBase4.setTitle(titleVakra);
        _iBase5.setTitle(titleVakra);

//        ketraBase.setAI(new BaseAI(ketraBase));
//        varkaBase.setAI(new BaseAI(varkaBase));

        _iBase1.decayMe();
        _iBase2.decayMe();
        _iBase3.decayMe();
        _iBase4.decayMe();
        _iBase5.decayMe();

        ketraBase.decayMe();
        varkaBase.decayMe();
        sayToAll(eventName + " - начнется через " + _time_to_start + " минуту");

        executeTask("events.KetraVarkaWar.KetraVarkaWar", "question", new Object[0], 10000);
        executeTask("events.KetraVarkaWar.KetraVarkaWar", "announce", new Object[0], 60000);
    }

    public static void sayToAll(String text) {
        for (L2Player player : L2ObjectsStorage.getPlayers()) {
            player.sendPacket(new Say2(0, Say2C.CRITICAL_ANNOUNCEMENT, "", text));
        }
    }

    public static void question() {
        for (L2Player player : L2ObjectsStorage.getPlayers()) {
            if (player != null && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().getId() <= 0 && !player.isInOlympiadMode() && !player.isDead() && !player.isInZone(ZoneType.epic) && !player.isFlying() && player.getVar("jailed") == null && player.getVarB("event_invite", true))
                player.scriptRequest("Do you want to participate in event 'War of Ketra and Varka'?", "events.KetraVarkaWar.KetraVarkaWar:addPlayer", new Object[0]);
        }
    }

    public static void announce() {
        if (varka_team.size() + ketra_team.size() < 1)
        //players_list1.isEmpty() || players_list2.isEmpty())
        {
            sayToAll(eventName + "Событие отложено из-за нехватки войск");
            _isRegistrationActive = false;
            _status = 0;
            executeTask("events.KetraVarkaWar.KetraVarkaWar", "autoContinue", new Object[0], 10000);
            if (!varka_team.isEmpty()) {
                for (HardReference<L2Player> ref : varka_team) {
                    L2Player p = ref.get();
                    if (p == null)
                        continue;
                    p.setEventReg(false);
                }
            }
            if (!ketra_team.isEmpty()) {
                for (HardReference<L2Player> ref : ketra_team) {
                    L2Player p = ref.get();
                    if (p == null)
                        continue;
                    p.setEventReg(false);
                }
            }
            varka_team.clear();
            ketra_team.clear();
            return;
        }

        if (_time_to_start > 1) {
            _time_to_start--;
            String[] param = {String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel)};
            sayToAll(eventName + " - начнется через " + _time_to_start + " минуту");
            executeTask("events.KetraVarkaWar.KetraVarkaWar", "announce", new Object[0], 60000);
        } else {
            _status = 1;
            _isRegistrationActive = false;
            sayToAll(eventName + "- Набор добровольцев закрыт, идет призыв на место битвы");
            executeTask("events.KetraVarkaWar.KetraVarkaWar", "prepare", new Object[0], 5000);
        }
    }

    public void un_reg() {
        L2Player player = (L2Player) getSelf();
        if (player == null)
            return;
        else if (!_isRegistrationActive) {
            player.sendMessage("Вы не можете снять регистрацию с ивента.");
            return;
        }
        removePlayer(player);
        player.sendMessage("Вы сняли регистрацию с CtF.");
    }

    public void addPlayer() {
        L2Player player = (L2Player) getSelf();
        if (player == null || !checkPlayer(player, true))
            return;

        int team, size1 = varka_team.size(), size2 = ketra_team.size();

        if (size1 > size2)
            team = 2;
        else if (size1 < size2)
            team = 1;
        else
            team = Rnd.get(1, 2);
            //team = 2;

        if (team == 1) {
            varka_team.add(player.getRef());
            player.setEventReg(true);
            player.sendMessage("Вы успешно зарегистрировались на ивент!");
            if (player.isGM()) {
                player.sendMessage("VAKRA");
            }
        } else if (team == 2) {
            ketra_team.add(player.getRef());
            player.setEventReg(true);
            player.sendMessage("Вы успешно зарегистрировались на ивент!");
            if (player.isGM()) {
                player.sendMessage("KETRA");
            }
        } else
            _log.info("WTF??? Command id 0 in CtF...");
    }

    public static boolean is_reg(L2Player player) {
        return varka_team.contains(player.getRef()) || ketra_team.contains(player.getRef());
    }

    public static boolean checkPlayer(L2Player player, boolean first) {
        if (first && !_isRegistrationActive) {
            player.sendMessage(new CustomMessage("scripts.events.Late", player));
            return false;
        } else if (first && (varka_team.contains(player.getRef()) || ketra_team.contains(player.getRef()))) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.Cancelled", player));
            return false;
        } else if (first && (player.isInEvent() != 0 || player.isEventReg())) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.OtherEvent", player).addString(player.getEventName(player.isInEvent())));
            return false;
        } else if (player.getLevel() < _minLevel || player.getLevel() > _maxLevel) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledLevel", player));
            return false;
        } else if (player.isMounted()) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.Cancelled", player));
            return false;
        } else if (player.getDuel() != null) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledDuel", player));
            return false;
        } else if (player.getTeam() != 0) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledOtherEvent", player));
            return false;
        } else if (player.getOlympiadGame() != null || player.isInZoneOlympiad() || first && Olympiad.isRegistered(player)) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledOlympiad", player));
            return false;
        } else if (player.isInParty() && player.getParty().isInDimensionalRift()) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledOtherEvent", player));
            return false;
        } else if (player.isTeleporting()) {
            player.sendMessage(new CustomMessage("scripts.events.CtF.CancelledTeleport", player));
            return false;
        } else if (player.isCursedWeaponEquipped()) {
            player.sendMessage("С проклятым оружием на ивент нельзя.");
            return false;
        } else if (player.isInOfflineMode() || player.inObserverMode() || player.isLogout())// Если игрок в observeMode, то удаляем его с ивента...не хуй было туда заходить)))
            return false;
        else if (player.isInStoreMode()) {
            player.sendMessage("Во время торговли на ивент нельзя.");
            return false;
        } else if (player.getVar("jailed") != null) {
            player.sendMessage("В тюрьме на ивент нельзя");
            return false;
        } else if (player.getReflection().getId() > 0) {
            player.sendMessage("Регистрация отменена, нельзя находится во временной зоне.");
            return false;
        }
        return true;
    }

    public static void prepare() {
        cleanPlayers();
        clearArena();
        ressurectPlayers();

        ketraBase.spawnMe();
        varkaBase.spawnMe();
        _iBase1.spawnMe();
        _iBase2.spawnMe();
        _iBase3.spawnMe();
        _iBase4.spawnMe();
        _iBase5.spawnMe();

        paralyzePlayers();

        executeTask("events.KetraVarkaWar.KetraVarkaWar", "teleportPlayersToColiseum", new Object[0], 4000);
        executeTask("events.KetraVarkaWar.KetraVarkaWar", "healPlayers", new Object[0], 6000);
        //executeTask("events.KetraVarkaWar.KetraVarkaWar", "go", new Object[0], 64000);
        executeTask("events.KetraVarkaWar.KetraVarkaWar", "go", new Object[0], 10000);

        sayToAll(eventName + " - в скором времени начнется битва");
        for (HardReference<L2Player> ref : varka_team) {
            L2Player player = ref.get();
            if (player == null) {
                continue;
            }
            player.sendPacket(new ExSendUIEvent(player, false, false, 65, 0, "Death Lock"));
        }
        for (HardReference<L2Player> ref : ketra_team) {
            L2Player player = ref.get();
            if (player == null) {
                continue;
            }
            player.sendPacket(new ExSendUIEvent(player, false, false, 65, 0, "Death Lock"));
        }

    }

    public static void go() {
        _status = 2;
        upParalyzePlayers();
        clearArena();
        sayToAll(eventName + " - В БОЙ!!!");
        _endTask = executeTask("events.KetraVarkaWar.KetraVarkaWar", "endOfTime", new Object[0], 30*60*1000);
    }

    public static void endOfTime() {
        endBattle(3); // ничья
    }

    public static void endBattle(int win) {
        if (_endTask != null) {
            _endTask.cancel(false);
            _endTask = null;
        }
        if (varkaBase != null) {
            varkaBase.deleteMe();
            varkaBase = null;
        }
        if (ketraBase != null) {
            ketraBase.deleteMe();
            ketraBase = null;
        }
        if (_iBase1 != null) {
            _iBase1.deleteMe();
            _iBase1 = null;
        }
        if (_iBase2 != null) {
            _iBase2.deleteMe();
            _iBase2 = null;
        }
        if (_iBase3 != null) {
            _iBase3.deleteMe();
            _iBase3 = null;
        }
        if (_iBase4 != null) {
            _iBase4.deleteMe();
            _iBase4 = null;
        }
        if (_iBase5 != null) {
            _iBase5.deleteMe();
            _iBase5 = null;
        }

        final List<L2NpcInstance> npcs = reflection.getNpcs();
        for (L2NpcInstance npc: npcs) {
            npc.deleteMe();
        }
        _status = 0;

        if (win == 1) {
            sayToAll(eventName + " - Победу одержал лагерь Варка");
            giveItems(10, varka_team);
            for (L2NpcInstance npc: reflection.getNpcs()){
                int npcId = npc.getNpcId();
                if (npcId == 36650 || npcId == 36651 || npcId == 36652 || npcId == 36653 || npcId == 36654 || npcId == 36655 || npcId == 36656 || npcId == 36657) {
                    npc.broadcastPacket2(new SocialAction(npc.getObjectId(), 3));
                } else if (npcId == 36658 || npcId == 36659 || npcId == 36660 || npcId == 36661 || npcId == 36662 || npcId == 36663 || npcId == 36664 || npcId == 36665) {
                    npc.reduceCurrentHp(npc.getMaxHp() + npc.getMaxCp() + 1, null, null, true, true, false, false, true, npc.getMaxHp() + npc.getMaxCp() + 1, true, false, false, false);
                }
            }
        } else if (win == 2) {
            sayToAll(eventName + " - Победу одержал лагерь Кетра");
            giveItems(10, ketra_team);

            for (L2NpcInstance npc: reflection.getNpcs()){
                int npcId = npc.getNpcId();
                if (npcId == 36658 || npcId == 36659 || npcId == 36660 || npcId == 36661 || npcId == 36662 || npcId == 36663 || npcId == 36664 || npcId == 36665) {
                    npc.broadcastPacket2(new SocialAction(npc.getObjectId(), 3));
                } else if (npcId == 36650 || npcId == 36651 || npcId == 36652 || npcId == 36653 || npcId == 36654 || npcId == 36655 || npcId == 36656 || npcId == 36657) {
                    npc.reduceCurrentHp(npc.getMaxHp() + npc.getMaxCp() + 1, null, null, true, true, false, false, true, npc.getMaxHp() + npc.getMaxCp() + 1, true, false, false, false);
                }
            }
        }

        sayToAll(eventName + " - Участники события будут отправлены обратно в течении 30 секунд");
        executeTask("events.KetraVarkaWar.KetraVarkaWar", "end", new Object[0], 30000);
        _isRegistrationActive = false;
    }

    public static void end() {
        executeTask("events.KetraVarkaWar.KetraVarkaWar", "ressurectPlayers", new Object[0], 1000);
        executeTask("events.KetraVarkaWar.KetraVarkaWar", "healPlayers", new Object[0], 2000);
        executeTask("events.KetraVarkaWar.KetraVarkaWar", "teleportPlayersToSavedCoords", new Object[0], 3000);
        //executeTask("events.KetraVarkaWar.KetraVarkaWar", "autoContinue", new Object[0], 10000);
        if (reflection != null) {
            reflection.startCollapseTimer(1);
            reflection = null;
        }
    }

    public void autoContinue() {
        if (reflection != null) {
            reflection.startCollapseTimer(1);
            reflection = null;
        }
        varka_team.clear();
        ketra_team.clear();

        if (_autoContinue > 0) {
            if (_autoContinue >= minLevelForCategory.length) {
                _autoContinue = 0;
                startTimerTask();
                return;
            }
            start(new String[]{"" + (_autoContinue + 1), "" + (_autoContinue + 1)});
        } else
            startTimerTask();
    }

    public void startTimerTask() {
        long delay = 0;
        long cur_time = System.currentTimeMillis();

        while (time2.size() != 0 && (delay = time2.remove(0)) - cur_time <= 0) ;
        if (_startTask != null)
            _startTask.cancel(true);
        if (delay - cur_time > 0)
            _startTask = ThreadPoolManager.getInstance().schedule(new StartTask(), delay - cur_time);
        else
            initTimer(true);
    }

    public static void giveItems(int rate, GCSArray<HardReference<L2Player>> list) {
        for (HardReference<L2Player> ref : list) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            if (player.getAttainment() != null)
                player.getAttainment().event_battle_end(0, true);
            addItem(player, _bonusItemID, (long) (_bonusItemCount * rate));
        }
    }
    private static void lockItems(L2Player player) {
        if (ConfigValue.CaptureTheFlagOlympiadItems || ConfigValue.CaptureTheFlagForbiddenItems.length > 0) {
            TIntArrayList items = new TIntArrayList();

            if (ConfigValue.CaptureTheFlagForbiddenItems.length > 0) {
                for (int i = 0; i < ConfigValue.CaptureTheFlagForbiddenItems.length; i++) {
                    items.add(ConfigValue.CaptureTheFlagForbiddenItems[i]);
                }
            }

            if (ConfigValue.CaptureTheFlagOlympiadItems) {
                for (L2ItemInstance item : player.getInventory().getItems()) {
                    if (!item.getOlympiadUse())
                        items.add(item.getItemId());
                }
            }

            player.getInventory().lockItems(LockType.INCLUDE, items.toArray());
        }
    }

    public static void teleportPlayerToColiseum(GCSArray<HardReference<L2Player>> players_list, Location locations) {
        for (HardReference<L2Player> ref : players_list) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            unRide(player);
            unSummonPet(player, true);
            player.setIsInEvent((byte) 17);
            lockItems(player);
            player.setIsInvul(false);
            player.setVar("backCoords", player.getLoc().toXYZString());
            player.setReflection(reflection);
            player.teleToLocation(locations.x + Rnd.get(-100, 100), locations.y + Rnd.get(-300, 300), locations.z);
            player.setTitle(player.getTeam() > 1 ? "Лагерь Кетра" : "Лагерь Варка");
        }
    }
    public static void teleportPlayersToColiseum() {
            teleportPlayerToColiseum(varka_team, varkaBaseLoc);
            teleportPlayerToColiseum(ketra_team, ketraBaseLoc);
    }

    public static void teleportPlayerToSavedCoords(GCSArray<HardReference<L2Player>> players_list) {
        for (HardReference<L2Player> ref : players_list) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            try {
                if (ConfigValue.CaptureTheFlagOlympiadItems || ConfigValue.CaptureTheFlagForbiddenItems.length > 0)
                    player.getInventory().unlock();
                player.setTitle(player.s_ai0);
                player.setTeam(0, true);
                player.setEventReg(false);
                player.setIsInEvent((byte) 0);
                player.getEffectList().stopAllEffects();
                if (player.getPet() != null) {
                    L2Summon summon = player.getPet();
                    summon.getEffectList().stopAllEffects();
                }
                player.setIsInvul(false);

                String back = player.getVar("backCoords");
                if (back != null) {
                    player.unsetVar("backCoords");
                    player.unsetVar("reflection");
                    player.teleToLocation(new Location(back), 0);
                }
            } catch (Exception e) {
                player.setTitle(player.s_ai0);
                player.setIsInvul(false);
                player.teleToLocation(147800, -55320, -2728, 0);
                player.unsetVar("backCoords");
                player.unsetVar("reflection");
                e.printStackTrace();
            }
        }
    }
    public static void teleportPlayersToSavedCoords() {
        teleportPlayerToSavedCoords(varka_team);
        teleportPlayerToSavedCoords(ketra_team);
    }

    public static void paralyzePlayer(GCSArray<HardReference<L2Player>> players_list) {
        for (HardReference<L2Player> ref : players_list) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            player.getEffectList().stopEffect(L2Skill.SKILL_MYSTIC_IMMUNITY);
            player.getEffectList().stopEffect(1540);
            player.getEffectList().stopEffect(1418);
            player.getEffectList().stopEffect(396);
            player.getEffectList().stopEffect(914);
            player.stopAbnormalEffect(AbnormalVisualEffect.ave_ultimate_defence);
            player.stopAbnormalEffect(AbnormalVisualEffect.ave_invincibility);
            player.setIsInvul(false);
            player.p_block_move(true, null);
            if (player.getPet() != null) {
                player.getPet().p_block_move(true, null);
            }
            if (player.getParty() != null)
                player.getParty().oustPartyMember(player);
        }
    }
    public static void paralyzePlayers() {
        paralyzePlayer(varka_team);
        paralyzePlayer(ketra_team);
    }

    public static void upParalyzePlayer(GCSArray<HardReference<L2Player>> players_list) {
        for (HardReference<L2Player> ref : players_list) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            player.p_block_move(false, null);
            if (player.getPet() != null) {
                player.getPet().p_block_move(false, null);
            }
        }
    }
    public static void upParalyzePlayers() {
        upParalyzePlayer(varka_team);
        upParalyzePlayer(ketra_team);
    }

    public static void ressurectPlayer(GCSArray<HardReference<L2Player>> players_list) {
        for (HardReference<L2Player> ref : players_list) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            if (player.isDead()) {
                player.restoreExp();
                player.setCurrentCp(player.getMaxCp());
                player.setCurrentHp(player.getMaxHp(), true);
                player.setCurrentMp(player.getMaxMp());
                player.broadcastPacket(new Revive(player));
            }
        }
    }
    public static void ressurectPlayers() {
        ressurectPlayer(varka_team);
        ressurectPlayer(ketra_team);
    }

    public static void healPlayer(GCSArray<HardReference<L2Player>> players_list) {
        for (HardReference<L2Player> ref : players_list) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
            player.setCurrentCp(player.getMaxCp());
        }
    }
    public static void healPlayers() {
    healPlayer(varka_team);
    healPlayer(ketra_team);
    }

    public static void cleanPlayer(GCSArray<HardReference<L2Player>> players_list) {
        for (HardReference<L2Player> ref : varka_team) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            if (!checkPlayer(player, false))
                removePlayer(player);
            else
                player.setTeam(1, true);
        }
        for (HardReference<L2Player> ref : ketra_team) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            if (!checkPlayer(player, false))
                removePlayer(player);
            else
                player.setTeam(2, true);
        }
    }
    public static void cleanPlayers() {
        for (HardReference<L2Player> ref : varka_team) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            if (!checkPlayer(player, false))
                removePlayer(player);
            else
                player.setTeam(1, true);
        }
        for (HardReference<L2Player> ref : ketra_team) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            if (!checkPlayer(player, false))
                removePlayer(player);
            else
                player.setTeam(2, true);
        }
    }

    public static void clearArena() {
        for (L2Object obj : _zone.getObjects())
            if (obj != null) {
                L2Player player = obj.getPlayer();
                if (player != null && !varka_team.contains(player.getRef()) && !ketra_team.contains(player.getRef()) && player.getReflection() == reflection) {
                    player.teleToLocation(147451, 46728, -3410);
                    player.setIsInvul(false);
                }
            }
    }

    public static void sayToParticipants(String text) {
        for (HardReference<L2Player> ref : varka_team) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            player.sendPacket(new ExShowScreenMessage(text, 3000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
            player.sendMessage(text);
        }
        for (HardReference<L2Player> ref : ketra_team) {
            L2Player player = ref.get();
            if (player == null)
                continue;
            player.sendPacket(new ExShowScreenMessage(text, 3000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
        }
    }

    public static void OnDie(L2Character self, L2Character killer) {
        if (_status > 1 && self != null && self.isPlayer() && self.getTeam() > 0 && (varka_team.contains(self.getRef()) || ketra_team.contains(self.getRef()))) {
            executeTask("events.KetraVarkaWar.KetraVarkaWar", "resurrectAtBase", new Object[]{(L2Player) self}, 1000);
        }
        if (self != null) {
            if (self.getNpcId() == ketraBaseID) {
                endBattle(1);
            } else if (self.getNpcId() == varkaBaseID) {
                endBattle(2);
            } else if (self.getNpcId() == iBaseID) {
                if (self.getTitle().equalsIgnoreCase(titleVakra)) {
                    sayToParticipants("База была захвачена лагерем Кетра");
                    self.setTitle(titleKetra);
                    self.spawnMe();
                } else if (self.getTitle().equalsIgnoreCase(titleKetra)) {
                    sayToParticipants("База была захвачена лагерем Варка");
                    self.setTitle(titleVakra);
                    self.spawnMe();
                }
            }
        }
    }

    public static void resurrectAtBase(L2Player player) {
        if (player.getTeam() <= 0)
            return;
        player.setIsInvul(false);
        if (player.isDead()) {
            player.setCurrentCp(player.getMaxCp());
            player.setCurrentHp(player.getMaxHp(), true);
            player.setCurrentMp(player.getMaxMp());
            player.broadcastPacket(new Revive(player));
        }
        Location pos;
        if (player.getTeam() == 2) {
            if (_iBase4.getTitle().equalsIgnoreCase(titleKetra) || _iBase5.getTitle().equalsIgnoreCase(titleKetra)) {
                pos = Rnd.chance(50) ?
                        new Location(_iBase4.getX() + Rnd.get(-200, 200), _iBase4.getY() + Rnd.get(-200, 200), _iBase4.getZ())
                        : new Location(_iBase5.getX() + Rnd.get(-200, 200), _iBase5.getY() + Rnd.get(-200, 200), _iBase5.getZ());
            } else if (_iBase2.getTitle().equalsIgnoreCase(titleKetra) || _iBase3.getTitle().equalsIgnoreCase(titleKetra)) {
                pos = Rnd.chance(50) ?
                        new Location(_iBase2.getX() + Rnd.get(-200, 200), _iBase2.getY() + Rnd.get(-200, 200), _iBase2.getZ())
                        : new Location(_iBase3.getX() + Rnd.get(-200, 200), _iBase3.getY() + Rnd.get(-200, 200), _iBase3.getZ());
            } else if (_iBase1.getTitle().equalsIgnoreCase(titleKetra)) {
                pos = new Location(_iBase1.getX() + Rnd.get(-200, 200), _iBase1.getY() + Rnd.get(-200, 200), _iBase1.getZ());
            } else {
                pos = ketraBaseLoc;
            }
        } else {
            if (_iBase2.getTitle().equalsIgnoreCase(titleVakra) || _iBase3.getTitle().equalsIgnoreCase(titleVakra)) {
                pos = Rnd.chance(50) ?
                        new Location(_iBase2.getX() + Rnd.get(-200, 200), _iBase2.getY() + Rnd.get(-200, 200), _iBase2.getZ())
                        : new Location(_iBase3.getX() + Rnd.get(-200, 200), _iBase3.getY() + Rnd.get(-200, 200), _iBase3.getZ());
            } else if (_iBase4.getTitle().equalsIgnoreCase(titleVakra) || _iBase5.getTitle().equalsIgnoreCase(titleVakra)) {
                pos = Rnd.chance(50) ?
                        new Location(_iBase4.getX() + Rnd.get(-200, 200), _iBase4.getY() + Rnd.get(-200, 200), _iBase4.getZ())
                        : new Location(_iBase5.getX() + Rnd.get(-200, 200), _iBase5.getY() + Rnd.get(-200, 200), _iBase5.getZ());
            } else {
                pos = varkaBaseLoc;
            }
        }

        player.teleToLocation(pos);
    }

    public static Location OnEscape(L2Player player) {
        if (player != null)
            player.setEventReg(false);
        if (_status > 1 && player != null && (varka_team.contains(player.getRef()) || ketra_team.contains(player.getRef())))
            removePlayer(player);
        return null;
    }

    public static void OnPlayerExit(L2Player player) {
        if (player != null)
            player.setEventReg(false);
        if (player != null && (varka_team.contains(player.getRef()) || ketra_team.contains(player.getRef()))) {
            // Вышел или вылетел во время регистрации
            if (_status == 0 && _isRegistrationActive && (varka_team.contains(player.getRef()) || ketra_team.contains(player.getRef()))) {
                removePlayer(player);
                return;
            }
            // Вышел или вылетел во время телепортации
            if (_status == 1 && (varka_team.contains(player.getRef()) || ketra_team.contains(player.getRef()))) {
                removePlayer(player);

                try {
                    player.setIsInvul(false);
                    String back = player.getVar("backCoords");
                    if (back != null) {
                        player.unsetVar("backCoords");
                        player.unsetVar("reflection");
                        player.teleToLocation(new Location(back), 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return;
            }

            // Вышел или вылетел во время эвента
            OnEscape(player);
        }
    }

    private class ZoneListener extends L2ZoneEnterLeaveListener {
        @Override
        public void objectEntered(L2Zone zone, L2Object object) {
            if (object == null)
                return;
            L2Player player = object.getPlayer();
            if (_status > 0 && player != null && !varka_team.contains(player.getRef()) && !ketra_team.contains(player.getRef()) && player.getReflection() == reflection)
                ThreadPoolManager.getInstance().schedule(new TeleportTask((L2Character) object, new Location(147451, 46728, -3410)), 3000);
        }

        @Override
        public void objectLeaved(L2Zone zone, L2Object object) {
            if (object == null)
                return;
            L2Player player = object.getPlayer();
            if (_status > 1 && player != null && player.getTeam() > 0 && (varka_team.contains(player.getRef()) || ketra_team.contains(player.getRef())) && player.getReflection() == reflection) {
                double angle = Util.convertHeadingToDegree(object.getHeading()); // угол в градусах
                double radian = Math.toRadians(angle - 90); // угол в радианах
                int x = (int) (object.getX() + 50 * Math.sin(radian));
                int y = (int) (object.getY() - 50 * Math.cos(radian));
                int z = object.getZ();
                ThreadPoolManager.getInstance().schedule(new TeleportTask((L2Character) object, new Location(x, y, z)), 3000);
            }
        }
    }

    public class TeleportTask extends l2open.common.RunnableImpl {
        Location loc;
        L2Character target;

        public TeleportTask(L2Character target, Location loc) {
            this.target = target;
            this.loc = loc;
            target.startStunning();
        }

        public void runImpl() {
            target.stopStunning();
            target.teleToLocation(loc);
        }
    }

    private static void removePlayer(L2Player player) {
        if (player != null) {
            varka_team.remove(player.getRef());
            ketra_team.remove(player.getRef());
            player.setTeam(0, true);
            player.setEventReg(false);
            player.setIsInEvent((byte) 0);
            player.getEffectList().stopAllEffects();
            if (player.getPet() != null) {
                L2Summon summon = player.getPet();
                summon.getEffectList().stopAllEffects();
            }
            player.setTitle(player.s_ai0);

            if (ConfigValue.CaptureTheFlagOlympiadItems || ConfigValue.CaptureTheFlagForbiddenItems.length > 0)
                player.getInventory().unlock();
        }
    }

}