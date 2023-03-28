package ai.KetraVarkaAI;

import l2open.common.ThreadPoolManager;
import l2open.extensions.scripts.Functions;
import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.ai.Fighter;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.util.GArray;
import l2open.util.Location;
import l2open.util.Rnd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;

public class VarkaAttackerAI extends Fighter {
    private static final Integer[] mobsId = {36658, 36659, 36660, 36661, 36662, 36663, 36664, 36665};
    private static final int enemyNpcID1 = 36658;
    private static final int enemyNpcID2 = 36659;
    private static final int enemyNpcID3 = 36660;
    private static final int enemyNpcID4 = 36661;
    private static final int enemyNpcID5 = 36662;
    private static final int enemyNpcID6 = 36663;
    private static final int enemyNpcID7 = 36664;
    private static final int enemyNpcID8 = 36665;
    private static final int enemyTeam = 2;
    private L2Character character = null;
    private long _timer = 0;
    private ScheduledFuture<?> _actionTask;

    private ArrayList<Location> path;

    private ArrayList<Location> locations = new ArrayList<Location>() {{
        add(0, new Location(108664, -53784, -2592));
        add(1, new Location(109720, -54376, -2808));
        add(2, new Location(110808, -54584, -2880));
        add(3, new Location(112168, -55032, -2832));
        add(4, new Location(113336, -55416, -2784));
        add(5, new Location(114904, -55832, -2704));
        add(6, new Location(116216, -55896, -2504));
        add(7, new Location(117704, -55896, -2336));
        add(8, new Location(119160, -55176, -2288));
        add(9, new Location(121000, -54312, -2368));
        add(10, new Location(122056, -53480, -2448));
        add(11, new Location(123176, -52248, -2464));
        add(12, new Location(124168, -50952, -2480));
        add(13, new Location(124744, -49448, -2560));
        add(14, new Location(124520, -48008, -2736));
        add(15, new Location(123656, -46616, -2880));
        add(16, new Location(123000, -45720, -3024));
        add(17, new Location(123336, -44408, -3264));
        add(18, new Location(123880, -43208, -3344));
        add(19, new Location(124504, -42280, -3456));
        add(20, new Location(125368, -41416, -3656));
        add(21, new Location(126056, -40824, -3760));
        add(22, new Location(126888, -41256, -3632));
        add(23, new Location(127528, -41688, -3536));
    }};
    private ArrayList<Location> locations1 = new ArrayList<Location>() {{
        add(0, new Location(108216, -52696, -2464));
        add(1, new Location(108248, -51384, -2416));
        add(2, new Location(108264, -49480, -2224));
        add(3, new Location(108008, -47912, -2080));
        add(4, new Location(108616, -46504, -2144));
        add(5, new Location(109832, -45448, -2336));
        add(6, new Location(111064, -44152, -2592));
        add(7, new Location(112104, -43864, -2704));
        add(8, new Location(113496, -44344, -2768));
        add(9, new Location(114632, -46120, -2576));
        add(10, new Location(115976, -46472, -2640));
        add(11, new Location(117368, -46408, -2680));
        add(12, new Location(118904, -46392, -2688));
        add(13, new Location(120584, -46104, -2864));
        add(14, new Location(121800, -45928, -2896));
        add(15, new Location(122968, -45688, -3040));
        add(16, new Location(123304, -44424, -3248));
        add(17, new Location(123800, -43320, -3328));
        add(18, new Location(124584, -42184, -3488));
        add(19, new Location(125528, -41240, -3688));
        add(20, new Location(126136, -40824, -3744));
        add(21, new Location(126824, -41160, -3648));
        add(22, new Location(127544, -41576, -3536));

    }};

    public VarkaAttackerAI(L2Character actor) {
        super(actor);
    }

    public static void debuger(L2NpcInstance actor, String txt) {
        Functions.npcSay(actor, txt);
    }

    @Override
    public void checkAggression(L2Character target) {
        if (target == null || target.getTeam() != enemyTeam) {
            return;
        }
        if (target.isNpc() && !Arrays.asList(mobsId).contains(target.getNpcId())) {
            return;
        }
        if (character != null) {
            return;
        }
        super.checkAggression(target);
    }

    @Override
    protected boolean maybeMoveToHome() {
        return false;
    }

    @Override
    public boolean isNotReturnHome() {
        return true;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    @Override
    protected void onEvtSpawn() {
        setMaxPursueRange(Integer.MAX_VALUE);
        final L2NpcInstance actor = getActor();
        int i = Rnd.get(1, 2);
        if (i == 1) {
            path = locations;
        } else if (i == 2) {
            path = locations1;
        }
        actor.setRunning();
        _actionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ActionTask(getActor()), 1000, 1000);
        super.onEvtSpawn();
    }

    public class ActionTask extends l2open.common.RunnableImpl {

        L2NpcInstance actor;


        public ActionTask(L2NpcInstance actor) {
            this.actor = actor;
        }

        @Override
        public void runImpl() throws Exception {
            if (actor.isDead() || actor.isDecayed()) {
                return;
            }
            final GArray<L2Character> aroundCharacters = actor.getAroundCharacters(900, 300);
            //debuger(actor, character != null ? character.getName() : "null");
            if (character != null) {
                if (character.isNpc() && character.isDead()) {
//                    int npcId = character.getNpcId();
//                    if (npcId == 36649 || npcId == 36648 || npcId == 36647) {
//                        character = null;
//                        stopAITask();
//                        clearTasks();
//                        clientStopMoving();
//                        return;
//                    }
                    character = null;
                    stopAITask();
                    clearTasks();
                    clientStopMoving();
                    return;
                } else if (character.isPlayer()) {
                    L2Player player = (L2Player) character;
                    if (player.getTeam() != enemyTeam || player.isInvisible() || player.isDead() || actor.getDistance(player) > 1200) {
                        character = null;
                        stopAITask();
                        clearTasks();
                        clientStopMoving();
                        //Functions.npcSayToPlayer(getActor(), player, "stopAI");
                        return;
                    }
                }
                character.addDamageHate(actor, 0, Rnd.get(50, 100)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
                actor.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
                actor.getAI().setAttackTarget(character); // На всякий случай, не обязательно делать
                actor.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, character, null); // Переводим в состояние атаки
            } else {
                if (aroundCharacters.size() > 0) {
                    //L2Character _char = aroundCharacters.get(Rnd.get(aroundCharacters.size()));
                    for (L2Character _char : aroundCharacters) {
                        if (_char.isNpc()) {
                            int npcId = _char.getNpcId();
                            if (npcId == enemyNpcID1 || npcId == enemyNpcID2 || npcId == enemyNpcID3
                                    || npcId == enemyNpcID4 || npcId == enemyNpcID5 || npcId == enemyNpcID6
                                    || npcId == enemyNpcID7 || npcId == enemyNpcID8) {
                                if (!_char.isDead()) {
                                    character = _char;
                                    getActor().setTarget(_char);
                                    startAITask();
                                    clearTasks();
                                    clientStopMoving();
                                    character.addDamageHate(actor, 0, Rnd.get(50, 100)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
                                    actor.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
                                    actor.getAI().setAttackTarget(character); // На всякий случай, не обязательно делать
                                    actor.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, character, null); // Переводим в состояние атаки
                                    break;
                                }
                            }
                        } else if (_char.isPlayer()) {
                            L2Player player = (L2Player) _char;
                            if (player.getTeam() == enemyTeam && !player.isInvisible() && !player.isDead()) {
                                character = player;
                                getActor().setTarget(player);
                                //Functions.npcSayToPlayer(getActor(), player, getActor().getTarget().getName());
                                startAITask();
                                clearTasks();
                                clientStopMoving();
                                character.addDamageHate(actor, 0, Rnd.get(50, 100)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
                                actor.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
                                actor.getAI().setAttackTarget(character); // На всякий случай, не обязательно делать
                                actor.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, character, null); // Переводим в состояние атаки
                                break;
                            }
                        }

                    }
                }
                if (character == null && System.currentTimeMillis() - _timer >= 1000) {
                    if (!path.isEmpty()) {
                        addTaskMove(Location.coordsRandomize(path.get(0), 25, 50), true);
                        if (actor.getDistance(path.get(0)) < 100) {
                            if (path.size() > 1) {
                                path.remove(0);
                            }
                        }
                        //debuger(actor, locations.size() + " size");
                        doTask();
                    }
                    _timer = System.currentTimeMillis();
                }
            }
        }
    }


}
