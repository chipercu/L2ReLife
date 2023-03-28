package npc.model.KetraVarkaWar;

import l2open.common.ThreadPoolManager;
import l2open.extensions.scripts.Functions;
import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.model.*;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.serverpackets.NpcInfo;
import l2open.gameserver.templates.L2NpcTemplate;
import l2open.util.Location;
import l2open.util.Rnd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class KetraBaseInstance extends L2NpcInstance {
    private static final String titleKetra = "Лагерь Кетра";
    private static final String titleVakra = "Лагерь Варка";
    private ScheduledFuture<?> _attackerSpawn;
    private ScheduledFuture<?> _guardSpawn;
    private static final int[] guards = {36661, 36662, 36663, 36664, 36665};

    private static final int officerId = 36660;
    private static final int recruitId = 36659;
    private static final int footmanId = 36658;

    private L2NpcInstance _general;
    private L2NpcInstance _magus;
    private L2NpcInstance _priest;
    private L2NpcInstance _guard;
    private L2NpcInstance _scout;

    private L2NpcInstance _officer1;
    private L2NpcInstance _recruit1_1;
    private L2NpcInstance _recruit1_2;
    private L2NpcInstance _footman1_1;
    private L2NpcInstance _footman1_2;

    private L2NpcInstance _officer2;
    private L2NpcInstance _recruit2_1;
    private L2NpcInstance _recruit2_2;
    private L2NpcInstance _footman2_1;
    private L2NpcInstance _footman2_2;

    private L2NpcInstance _officer3;
    private L2NpcInstance _recruit3_1;
    private L2NpcInstance _recruit3_2;
    private L2NpcInstance _footman3_1;
    private L2NpcInstance _footman3_2;

    private L2NpcInstance _officer4;
    private L2NpcInstance _recruit4_1;
    private L2NpcInstance _recruit4_2;
    private L2NpcInstance _footman4_1;
    private L2NpcInstance _footman4_2;

    private L2NpcInstance _officer5;
    private L2NpcInstance _recruit5_1;
    private L2NpcInstance _recruit5_2;
    private L2NpcInstance _footman5_1;
    private L2NpcInstance _footman5_2;

    public KetraBaseInstance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
       // this.setAI(new BaseAI(this));
        if (!this.is_block_move()){
            this.p_block_move(true, null);
        }
    }

    @Override
    public void onDecay() {

        if (_guardSpawn != null){
            _guardSpawn.cancel(true);
        }
        if (_attackerSpawn != null){
            _attackerSpawn.cancel(true);
        }
        super.onDecay();
    }

    @Override
    public void onSpawn() {

        if (this.getReflectionId() != 0) {
            _guardSpawn = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SpawnGuard(this, guards), 2000, 5000);

            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _officer1 = Functions.spawn(getLoc(), officerId, 0, getReflectionId());}
            }, 60000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _recruit1_1 = Functions.spawn(getLoc(), recruitId, 0, getReflectionId());}
            }, 60000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _recruit1_2 = Functions.spawn(getLoc(), recruitId, 0, getReflectionId());}
            }, 60000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _footman1_1 = Functions.spawn(getLoc(), footmanId, 0, getReflectionId());}
            }, 60000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _footman1_2 = Functions.spawn(getLoc(), footmanId, 0, getReflectionId());}
            }, 60000);

            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _officer2 = Functions.spawn(getLoc(), officerId, 0, getReflectionId());}
            }, 70000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _recruit2_1 = Functions.spawn(getLoc(), recruitId, 0, getReflectionId());}
            }, 70000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _recruit2_2 = Functions.spawn(getLoc(), recruitId, 0, getReflectionId());}
            }, 70000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _footman2_1 = Functions.spawn(getLoc(), footmanId, 0, getReflectionId());}
            }, 70000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _footman2_2 = Functions.spawn(getLoc(), footmanId, 0, getReflectionId());}
            }, 70000);

            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _officer3 = Functions.spawn(getLoc(), officerId, 0, getReflectionId());}
            }, 80000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _recruit3_1 = Functions.spawn(getLoc(), recruitId, 0, getReflectionId());}
            }, 80000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _recruit3_2 = Functions.spawn(getLoc(), recruitId, 0, getReflectionId());}
            }, 80000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _footman3_1 = Functions.spawn(getLoc(), footmanId, 0, getReflectionId());}
            }, 80000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _footman3_2 = Functions.spawn(getLoc(), footmanId, 0, getReflectionId());}
            }, 80000);

            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _officer4 = Functions.spawn(getLoc(), officerId, 0, getReflectionId());}
            }, 90000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _recruit4_1 = Functions.spawn(getLoc(), recruitId, 0, getReflectionId());}
            }, 90000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _recruit4_2 = Functions.spawn(getLoc(), recruitId, 0, getReflectionId());}
            }, 90000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _footman4_1 = Functions.spawn(getLoc(), footmanId, 0, getReflectionId());}
            }, 90000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _footman4_2 = Functions.spawn(getLoc(), footmanId, 0, getReflectionId());}
            }, 90000);

            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _officer5 = Functions.spawn(getLoc(), officerId, 0, getReflectionId());}
            }, 100000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _recruit5_1 = Functions.spawn(getLoc(), recruitId, 0, getReflectionId());}
            }, 100000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _recruit5_2 = Functions.spawn(getLoc(), recruitId, 0, getReflectionId());}
            }, 100000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _footman5_1 = Functions.spawn(getLoc(), footmanId, 0, getReflectionId());}
            }, 100000);
            ThreadPoolManager.getInstance().schedule(new l2open.common.RunnableImpl() {
                @Override
                public void runImpl() { _footman5_2 = Functions.spawn(getLoc(), footmanId, 0, getReflectionId());}
            }, 100000);

            _attackerSpawn = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SpawnAttacker(this, officerId), 120000, 10000);
        }

        super.onSpawn();
    }

    @Override
    public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean isDot, double i2, boolean sendMesseg, boolean bow, boolean crit, boolean tp) {
        super.reduceCurrentHp(2000, attacker, skill, awake, standUp, directHp, canReflect, isDot, i2, sendMesseg, bow, crit, tp);
        this.setTitle((int)this.getCurrentHpPercents() + " - Hit Points");

        if (this.getCurrentHp() < 4000){
            _guardSpawn.cancel(true);
            _attackerSpawn.cancel(true);
            //_attackerSpawn.cancel(true);
        }
    }

    @Override
    public boolean isAutoAttackable(L2Character attacker)
    {
        if(attacker == null){
            return false;
        }
        L2Player player = attacker.getPlayer();
        if(player == null){
            return false;
        }
        return player.getTeam() == 1;
    }
    @Override
    public boolean isInvul() {
        return false;
    }

    @Override
    public boolean isAttackable(L2Character attacker)
    {
        return isAutoAttackable(attacker);
    }


    public class SpawnGuard extends l2open.common.RunnableImpl {
        Location loc;
        L2NpcInstance actor;
        int[] mobsID;

        public SpawnGuard(L2NpcInstance actor, int[] mobsID) {
            this.actor = actor;
            this.loc = actor.getLoc();
            this.mobsID = mobsID;
        }

        private L2NpcInstance spawn(L2NpcInstance npc, int id) {
            if (npc == null || npc.isDead() || npc.isDecayed()) {
                loc = new Location(actor.getX() + Rnd.get(-200, 200), actor.getY() + Rnd.get(-200, 200), actor.getZ());
                npc = Functions.spawn(loc, id, 0, actor.getReflectionId());
                npc.setTitle(titleKetra);
                for (L2Player player : L2World.getAroundPlayers(npc)) {
                    if (player == null) {
                        continue;
                    }
                    player.sendPacket(new NpcInfo(npc, player));
                }
            }
            return npc;
        }

        @Override
        public void runImpl() throws Exception {
            _general = spawn(_general, mobsID[0]);
            _magus = spawn(_magus, mobsID[1]);
            _priest = spawn(_priest, mobsID[2]);
            _guard = spawn(_guard, mobsID[3]);
            _scout = spawn(_scout, mobsID[4]);
        }
    }


    public class SpawnAttacker extends l2open.common.RunnableImpl {
        Location loc;
        L2NpcInstance actor;
        int mobsID;

        public SpawnAttacker(KetraBaseInstance actor, int mobsID) {
            this.actor = actor;
            this.loc = actor.getLoc();
            this.mobsID = mobsID;
        }

        private L2NpcInstance spawn(L2NpcInstance npc, int id) {
            if (npc == null || npc.isDead() || npc.isDecayed()) {
                loc = new Location(actor.getX() + Rnd.get(-200, 200), actor.getY() + Rnd.get(-200, 200), actor.getZ());
                npc = Functions.spawn(loc, id, 0, actor.getReflectionId());
                npc.setTitle(titleKetra);

                for (L2Player player : L2World.getAroundPlayers(npc)) {
                    if (player == null) {
                        continue;
                    }
                    player.sendPacket(new NpcInfo(npc, player));
                }
            }
            return npc;
        }


        @Override
        public void runImpl() throws Exception {

            _officer1 = spawn(_officer1, officerId);
            _recruit1_1 = spawn(_recruit1_1, recruitId);
            _recruit1_2 = spawn(_recruit1_2, recruitId);
            _footman1_1 = spawn(_footman1_1, footmanId);
            _footman1_2 = spawn(_footman1_2, footmanId);

            _officer2 = spawn(_officer2, officerId);
            _recruit2_1 = spawn(_recruit2_1, recruitId);
            _recruit2_2 = spawn(_recruit2_2, recruitId);
            _footman2_1 = spawn(_footman2_1, footmanId);
            _footman2_2 = spawn(_footman2_2, footmanId);

            _officer3 = spawn(_officer3, officerId);
            _recruit3_1 = spawn(_recruit3_1, recruitId);
            _recruit3_2 = spawn(_recruit3_2, recruitId);
            _footman3_1 = spawn(_footman3_1, footmanId);
            _footman3_2 = spawn(_footman3_2, footmanId);

            _officer4 = spawn(_officer4, officerId);
            _recruit4_1 = spawn(_recruit4_1, recruitId);
            _recruit4_2 = spawn(_recruit4_2, recruitId);
            _footman4_1 = spawn(_footman4_1, footmanId);
            _footman4_2 = spawn(_footman4_2, footmanId);

            _officer5 = spawn(_officer5, officerId);
            _recruit5_1 = spawn(_recruit5_1, recruitId);
            _recruit5_2 = spawn(_recruit5_2, recruitId);
            _footman5_1 = spawn(_footman5_1, footmanId);
            _footman5_2 = spawn(_footman5_2, footmanId);

        }
    }
}
