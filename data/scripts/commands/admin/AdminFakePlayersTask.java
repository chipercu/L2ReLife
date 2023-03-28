package scripts.commands.admin;

import l2open.common.RunnableImpl;
import l2open.common.ThreadPoolManager;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.ai.DefaultAI;
import l2open.gameserver.handler.AdminCommandHandler;
import l2open.gameserver.handler.IAdminCommandHandler;
import l2open.gameserver.model.*;
import l2open.gameserver.serverpackets.MagicSkillUse;
import l2open.gameserver.serverpackets.Say2;
import l2open.gameserver.skills.Formulas;
import l2open.util.GCSArray;
import l2open.util.Location;
import l2open.util.Rnd;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;

public class AdminFakePlayersTask implements IAdminCommandHandler, ScriptFile {

    public static enum TaskType {
        MOVE,
        ATTACK,
        CAST,
        BUFF,
        SELFBUFF,
    }

    public static class Task {
        private int id;
        private L2Player character;
        private TaskType type;
        private L2Skill skill;
        private L2Character targe;
        private Location loc;
        private boolean delay;
        private boolean pathfind;
        private boolean checkTarget = true;
        private int weight = 1000;

        public Task(int id, L2Player character, TaskType type, L2Skill skill, L2Character task_targe, int weight) {
            this.id = id;
            this.character = character;
            this.type = type;
            this.skill = skill;
            this.targe = task_targe;
            this.weight = weight;
        }

    }

    private static class TaskComparator implements Comparator<Task> {
        public int compare(Task o1, Task o2) {
            if (o1 == null || o2 == null)
                return 0;
            return o2.weight - o1.weight;
        }
    }

    protected static List<L2Character> targetList = new ArrayList<L2Character>();
    private static final TaskComparator task_comparator = new TaskComparator();
    protected static Map<Integer, Task> _task_list = new HashMap<Integer, Task>();

    protected ConcurrentSkipListSet<Task> _task_list2 = new ConcurrentSkipListSet<Task>(task_comparator);

    public static ScheduledFuture<?> _executeTask;
    public static ScheduledFuture<?> _updateTask;
    public static int task_delay = 250;


    protected static GCSArray<L2Skill> _disableSkill = new GCSArray<L2Skill>();
    public static long randomWalk = System.currentTimeMillis();
    public static final long randomWalkDelay = 5000;
    public static Location startLoc;
    public static int point = 0;
    public static boolean townWalk = false;
    public static List<Location> newLoc = new ArrayList<Location>() {{
        add(new Location(81096, 149400, -3464));
        add(new Location(81080, 147784, -3464));
        add(new Location(82808, 147816, -3464));
        add(new Location(82824, 149368, -3472));
    }};
//            new Location( 83208, 150520 ,-3512),
//            new Location( 79688, 150712 ,-3512),
//            new Location( 80424, 149240 ,-3504),
//            new Location( 80456, 147896 ,-3504)


    public static Integer[] selfBuffs = {
            78, 297
    };


    public long disableSkill(L2Skill skill, L2Character character) {
        _disableSkill.add(skill);
        long reuseDelay = Formulas.calcSkillReuseDelay(character, skill);
        ThreadPoolManager.getInstance().schedule(() -> _disableSkill.remove(skill), reuseDelay < 50 ? 2000L : reuseDelay);
        return reuseDelay;
    }

    private static enum Commands {
        admin_start_task,
        admin_stop_task,
        admin_task_status
    }

    public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar) {
        Commands command = (Commands) comm;

        if (!activeChar.getPlayerAccess().CanEditNPC) return false;

        switch (command) {
            case admin_start_task: {
                startLoc = activeChar.getLoc();
//
//                Task initMoveTask = new Task(9999, activeChar, TaskType.MOVE, null, null, 3000);
//                _task_list.add(initMoveTask);
//                Task initAttackTask = new Task(9999, activeChar, TaskType.ATTACK, null, null, 3000);
//                _task_list.add(initAttackTask);

                Collections.shuffle(newLoc);

                _updateTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new UpdateTask(activeChar), task_delay, task_delay);
                _executeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ExecuteTask(activeChar), task_delay, task_delay);
//                _executeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new TestTask(activeChar), 1000, 1000);

                activeChar.sendPacket(new Say2(0, 0, "DEBUG", "task started"));
                activeChar.setTitle("AI STARTED", false);
                break;
            }
            case admin_stop_task: {
                if (_updateTask != null) {
                    _updateTask.cancel(true);
                    _updateTask = null;
                }
                if (_executeTask != null) {
                    _executeTask.cancel(true);
                    _executeTask = null;
                }
                targetList.clear();
                _task_list.clear();
                _disableSkill.clear();

                activeChar.setTitle("AI STOPPED", false);
                activeChar.sendPacket(new Say2(0, 0, "DEBUG", "task stopped"));
                break;
            }
            case admin_task_status: {
                if (_executeTask != null) {
                    activeChar.sendPacket(new Say2(0, 0, "DEBUG", "task is active"));
                } else {
                    activeChar.sendPacket(new Say2(0, 0, "DEBUG", "task is stopped"));
                }

                break;
            }

        }


        return true;
    }

    public Task newMoveTask(int id, L2Player player, TaskType type, L2Skill skill, L2Character target, int w) {
        Task task = new Task(id, player, TaskType.MOVE, null, target, w);
        task.loc = task.targe.getLoc();
        return task;
    }


    public class TestTask extends RunnableImpl {
        L2Player player;

        public TestTask(L2Player player) {
            this.player = player;
        }

        @Override
        public void runImpl() throws Exception {
            if (player.isSkillDisabled(Long.valueOf(selfBuffs[0]))) {
                player.sendPacket(new Say2(0, 0, "TEST", "skill disable"));
            } else {
                player.sendPacket(new Say2(0, 0, "TEST", "skill enable"));
            }
        }
    }

    public class UpdateTask extends RunnableImpl {
        private L2Player player;

        public UpdateTask(L2Player player) {
            this.player = player;
        }

        @Override
        public void runImpl() throws Exception {
           // player.sendPacket(new Say2(0, 0, "debug", "" + _task_list.size()));
            L2Character target = (L2Character) player.getTarget();
            for (int i : selfBuffs) {
                L2Skill knownSkill = player.getKnownSkill(i);
                if (knownSkill != null) {
                    if (player.getEffectList().getEffectBySkillId(knownSkill.getId()) == null) {
                        Task task = new Task(knownSkill.getId(), player, TaskType.SELFBUFF, knownSkill, player, 3000);
                        if (!_task_list.containsKey(task.id)) {
                            _task_list.put(task.id, task);
                            //player.sendPacket(new Say2(0, 0, "SELFBUFF", "добавлена задача buff: " + knownSkill.getName()));
                        }
                    }
                }
            }

//            if (target != null) {
//                if (player.getRealDistance3D(target) > 60 && !target.isDead()) {
//                    Task task = newMoveTask(999999, player, TaskType.MOVE, null, target, 1000);
//                    if (!_task_list.containsKey(task.id)) {
//                        _task_list.put(task.id, task);
//                       // player.sendPacket(new Say2(0, 0, "MOVE", "добавлена задача move: " + target.getName()));
//                    }
//                }
//            }


            if (target != null && target.isMonster() && !target.isDead()) {
                int[] i = {1, 345};
                L2Skill knownSkill = player.getKnownSkill(i[1]);
                if (knownSkill != null) {
                    Task task = new Task(888888, player, TaskType.CAST, knownSkill, target, 1000);
                    if (!_task_list.containsKey(task.id)) {
                        _task_list.put(task.id, task);
                        player.sendPacket(new Say2(0, 0, "CAST", "добавлена задача cast: " + knownSkill.getName()));
                    }
                }
            }
        }
    }


    public class ExecuteTask extends RunnableImpl {
        private L2Player player;

        public ExecuteTask(L2Player player) {
            this.player = player;
        }

        @Override
        public void runImpl() throws Exception {

            if (_task_list.isEmpty()) {
                return;
            }
            if (player.isCastingNow()) {
                return;
            }
            if (player.isDead()) {
                return;
            }

            //Collection<Task> collect = new ArrayList<>(_task_list.values());
            Task currentTask = _task_list.values().stream().findFirst().orElse(null);

//            Collection<Task> values = _task_list.values();
//            Task currentTask = null;
//            if (curr != null) {
//                currentTask = new Task(curr.id, curr.character, curr.type, curr.skill, curr.targe, curr.weight);
//                _task_list.remove(curr);
//            }
            if (currentTask != null) {
                switch (currentTask.type) {
                    case BUFF: {
                        if (!_disableSkill.contains(currentTask.skill)) {
                            player.getAI().Cast(currentTask.skill, currentTask.targe, false, true);
                            disableSkill(currentTask.skill, currentTask.targe);
                            _task_list.remove(currentTask.id);
                            player.sendPacket(new Say2(0, 0, "BUFF", "выполнена задача" + currentTask.id));
    //                            if (player.isMoving && point > 0) {
    //                                point--;
    //                            }

    //                            if (movetask.isPresent()) {
    //                                movetask.get().delay = true;
    //                                if (point == 0) {
    //                                    point = newLoc.size();
    //                                }
    //                                point--;
    //
    //
    //                                ThreadPoolManager.getInstance().schedule(() -> movetask.get().delay = false, currentTask.skill.getCoolTime());
    //                            }
                        } else {
                            player.sendPacket(new Say2(0, 0, "BUFF", "задача " + currentTask.id + " отложена"));
                        }
                        break;
                    }
                    case ATTACK: {
                        if (currentTask.targe != null) {
                            player.getAI().Attack(currentTask.targe, false, false);
                            currentTask.targe = null;
                        }
                        break;
                    }
                    case CAST: {
                        if (currentTask.targe.isDead()) {
                            _task_list.remove(currentTask.id);
                            player.sendPacket(new Say2(0, 0, "CAST", "задача удалена cast: " + currentTask.skill.getName()));
                        }
                        if (currentTask.skill != null && !player.isSkillDisabled((long) currentTask.skill.getId()) && player.getRealDistance3D(currentTask.targe) < currentTask.skill.getCastRange()) {
                            player.getAI().Cast(currentTask.skill, currentTask.targe, false, false);
                           // player.sendPacket(new MagicSkillUse(player, currentTask.targe, currentTask.skill.getId(), currentTask.skill.getLevel(), currentTask.skill.getHitTime(), currentTask.skill.getReuseDelay()));
                            long l = Formulas.calcSkillReuseDelay(player, currentTask.skill);
                            player.disableSkill(currentTask.skill.getId(), currentTask.skill.getLevel(), l);
                            _task_list.remove(currentTask.id);
                            player.sendPacket(new Say2(0, 0, "CAST", "задача выполнена cast: " + currentTask.skill.getName()));
                        }
                        break;
                    }
                    case MOVE: {
                        if (townWalk) {
                            if (!player.isMoving && !currentTask.delay) {
                                player.moveToLocation(newLoc.get(point), 10, true);
                                point++;
                                if (point >= newLoc.size()) {
                                    point = 0;
                                }
                                currentTask.loc = newLoc.get(point);
                            }
                        } else {
                            if (!player.isMoving && currentTask.loc != null) {
                                player.moveToLocation(currentTask.loc, 30, true);
                                _task_list.remove(currentTask.id);
                            }

                        }

                        break;
                    }
                    case SELFBUFF: {
                        if (currentTask.skill != null && !player.isSkillDisabled((long) currentTask.skill.getId())) {
                            player.abortAttack(true , true);
                            player.getAI().Cast(currentTask.skill, currentTask.targe, false, true);
                            long l = Formulas.calcSkillReuseDelay(player, currentTask.skill);
                            player.disableSkill(currentTask.skill.getId(), currentTask.skill.getLevel(), l);
                            _task_list.remove(currentTask.id);
    //                                if (movetask.isPresent() && townWalk) {
    //                                    movetask.get().delay = true;
    //                                    if (point == 0) {
    //                                        point = newLoc.size();
    //                                    }
    //                                    point--;
    //
    //                                    ThreadPoolManager.getInstance().schedule(() -> movetask.get().delay = false, currentTask.skill.getCoolTime());
    //                                }
                           // player.sendPacket(new Say2(0, 0, "SKILL", "disable " + (int) l));
                            //player.sendPacket(new Say2(0, 0, "SELFBUFF", "задача выполнена buff: " + currentTask.skill.getName()));
                        }
                    }
                }
            }

        }

    }


    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    public void onLoad() {
        AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
    }

    public void onReload() {
    }

    public void onShutdown() {
    }
}