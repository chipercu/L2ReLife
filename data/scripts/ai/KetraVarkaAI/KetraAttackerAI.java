package ai.KetraVarkaAI;

import l2open.common.ThreadPoolManager;
import l2open.config.ConfigValue;
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

public class KetraAttackerAI extends Fighter {
    private static final Integer[] mobsId = {36650, 36651, 36652, 36653, 36654, 36655, 36656, 36657};
    private static final int enemyNpcID1 = 36650;
    private static final int enemyNpcID2 = 36651;
    private static final int enemyNpcID3 = 36652;
    private static final int enemyNpcID4 = 36653;
    private static final int enemyNpcID5 = 36654;
    private static final int enemyNpcID6 = 36655;
    private static final int enemyNpcID7 = 36656;
    private static final int enemyNpcID8 = 36657;
    private static final int enemyTeam = 1;
    private L2Character character = null;
    private ScheduledFuture<?> _actionTask;
    private ArrayList<Location> path;
    private long _timer = 0;

    private ArrayList<Location> locations = new ArrayList<Location>(){{
        add(0, new Location( 127192, -41400 ,-3584));
        add(1, new Location( 125944, -41016 ,-3744));
        add(2, new Location( 125192, -41544 ,-3632));
        add(3, new Location( 124216, -42616 ,-3408));
        add(4, new Location( 123544, -43800 ,-3296));
        add(5, new Location( 123112, -45224 ,-3104));
        add(6, new Location( 122344, -45928 ,-2944));
        add(7, new Location( 120536, -46024 ,-2864));
        add(8, new Location( 118968, -46360 ,-2704));
        add(9, new Location( 118008, -46472 ,-2544));
        add(10, new Location( 116920, -46440 ,-2664));
        add(11, new Location( 115768, -46440 ,-2624));
        add(12, new Location( 114984, -46488 ,-2576));
        add(13, new Location( 114312, -45528 ,-2640));
        add(14, new Location( 113560, -44568 ,-2768));
        add(15, new Location( 112632, -44072 ,-2704));
        add(16, new Location( 111560, -43928 ,-2624));
        add(17, new Location( 110584, -44648 ,-2520));
        add(18, new Location( 109608, -45720 ,-2288));
        add(19, new Location( 108632, -46520 ,-2144));
        add(20, new Location( 108200, -47400 ,-2112));
        add(21, new Location( 108216, -48728 ,-2144));
        add(22, new Location( 108376, -50120 ,-2288));
        add(23, new Location( 108488, -51352 ,-2416));
        add(24, new Location( 108504, -52360 ,-2464));
        add(25, new Location( 108200, -53336 ,-2480));
    }};
    private ArrayList<Location> locations1 = new ArrayList<Location>(){{
        add( 0,new Location( 126536, -41016 ,-3680));
        add(1 ,new Location( 125656, -41144 ,-3712));
        add( 2,new Location( 124712, -41976 ,-3512));
        add( 3,new Location( 124008, -42888 ,-3360));
        add( 4,new Location( 123320, -44360 ,-3264));
        add( 5,new Location( 123016, -45608 ,-3040));
        add( 6,new Location( 123928, -46792 ,-2832));
        add( 7,new Location( 124504, -47800 ,-2752));
        add( 8,new Location( 124760, -49304 ,-2576));
        add( 9,new Location( 124424, -50616 ,-2496));
        add( 10,new Location( 123400, -52312 ,-2480));
        add( 11,new Location( 122472, -53448 ,-2448));
        add( 12,new Location( 121512, -54280 ,-2400));
        add( 13,new Location( 119976, -53784 ,-2336));
        add( 14,new Location( 118840, -53336 ,-2416));
        add( 15,new Location( 117848, -52344 ,-2512));
        add( 16,new Location( 116712, -51608 ,-2592));
        add( 17,new Location( 116088, -50952 ,-2624));
        add( 18,new Location( 115864, -49800 ,-2624));
        add( 19,new Location( 115272, -48696 ,-2624));
        add( 20,new Location( 114984, -47560 ,-2592));
        add( 21,new Location( 114712, -46360 ,-2560));
        add( 22,new Location( 114040, -45160 ,-2688));
        add( 23,new Location( 113144, -44184 ,-2768));
        add( 24,new Location( 111928, -43976 ,-2688));
        add( 25,new Location( 111272, -44024 ,-2592));
        add( 26,new Location( 110520, -44760 ,-2496));
        add( 27,new Location( 109640, -45640 ,-2296));
        add( 28,new Location( 108680, -46472 ,-2160));
        add( 29,new Location( 108152, -47448 ,-2080));
        add( 30,new Location( 108056, -48888 ,-2144));
        add( 31,new Location( 108392, -50360 ,-2320));
        add( 32,new Location( 108408, -51560 ,-2432));
        add( 33,new Location( 108200, -53240 ,-2480));
    }};
    private ArrayList<Location> locations2 = new ArrayList<Location>(){{
        add( 0,new Location( 126536, -41016 ,-3680));
        add(1 ,new Location( 125656, -41144 ,-3712));
        add( 2,new Location( 124712, -41976 ,-3512));
        add( 3,new Location( 124008, -42888 ,-3360));
        add( 4,new Location( 123320, -44360 ,-3264));
        add( 5,new Location( 123016, -45608 ,-3040));
        add( 6,new Location( 123928, -46792 ,-2832));
        add( 7,new Location( 124504, -47800 ,-2752));
        add( 8,new Location( 124760, -49304 ,-2576));
        add( 9,new Location( 124424, -50616 ,-2496));
        add( 10,new Location( 123400, -52312 ,-2480));
        add( 11,new Location( 122472, -53448 ,-2448));
        add( 12,new Location( 121512, -54280 ,-2400));
        add(13, new Location( 121304, -54504 ,-2384));
        add(14, new Location( 120408, -54952 ,-2304));
        add(15, new Location( 119176, -55544 ,-2272));
        add(16, new Location( 118008, -55864 ,-2320));
        add(17, new Location( 117080, -55912 ,-2376));
        add(18, new Location( 115928, -55896 ,-2560));
        add(19, new Location( 115032, -55832 ,-2688));
        add(20, new Location( 114200, -55720 ,-2736));
        add(21, new Location( 113160, -55448 ,-2800));
        add(22, new Location( 112024, -55096 ,-2832));
        add(23, new Location( 110440, -54616 ,-2880));
        add(24, new Location( 109160, -54136 ,-2728));
        add(25, new Location( 108408, -53608 ,-2528));
        add(26, new Location( 108040, -53560 ,-2480));
    }};

    public KetraAttackerAI(L2Character actor) {
        super(actor);
    }

    public static void debuger(L2NpcInstance actor, String txt){
        Functions.npcSay(actor, txt);
    }
    @Override
    public void checkAggression(L2Character target) {
        if (target == null || target.getTeam() != enemyTeam){
            return;
        }
        super.checkAggression(target);
    }
    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    protected boolean maybeMoveToHome()
    {
        return false;
    }

    @Override
    public boolean isNotReturnHome()
    {
        return true;
    }

    @Override
    protected boolean randomWalk()
    {
        return false;
    }



    @Override
    protected void onEvtSpawn() {
        setMaxPursueRange(Integer.MAX_VALUE);
        //path = locations;
        int i = Rnd.get(1, 3);
        if (i == 1) {
            path = locations;
        } else if (i == 2) {
            path = locations1;
        } else if (i == 3) {
            path = locations2;
        }
        getActor().setRunning();
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
            if (actor.isDead() || actor.isDecayed()){
                return;
            }
            final GArray<L2Character> aroundCharacters = actor.getAroundCharacters(900, 300);
            //debuger(actor, character != null ? character.getName() : "null");
            if (character != null) {
                if (character.isNpc() && character.isDead()) {
                        character = null;
                        stopAITask();
                        clearTasks();
                        clientStopMoving();
                        return;
                 //   }
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
                                if (!_char.isDead()){
                                    character = _char;
                                    actor.setTarget(_char);
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
                                actor.setTarget(player);
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
                if (character == null && System.currentTimeMillis() - _timer >= 1000){
                    if (!path.isEmpty()) {
                        addTaskMove(Location.coordsRandomize(path.get(0), 25, 50), true);
                        if (actor.getDistance(path.get(0)) <= 100) {
                            if (path.size() > 1){
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
