package npc.model.KetraVarkaWar;

import l2open.common.ThreadPoolManager;
import l2open.extensions.scripts.Functions;
import l2open.gameserver.ai.CtrlIntention;
import l2open.gameserver.model.*;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.serverpackets.ExShowScreenMessage;
import l2open.gameserver.serverpackets.NpcInfo;
import l2open.gameserver.templates.L2NpcTemplate;
import l2open.util.Location;
import l2open.util.Rnd;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class IntermediarBaseInstance extends L2NpcInstance {
    private static final String titleKetra = "Лагерь Кетра";
    private static final String titleVakra = "Лагерь Варка";
    private static final int _mobRespawnDelay = 10;
    private L2NpcInstance _general;
    private L2NpcInstance _magus;
    private L2NpcInstance _priest;
    private L2NpcInstance _guard;
    private L2NpcInstance _scout;

    private List<L2NpcInstance> guards = new ArrayList<L2NpcInstance>();

    public Faction faction;
    private ScheduledFuture<?> _guardSpawn;

    public static enum Faction {
        KETRA,
        VARKA
    }

    private static final int[] varkaMobs = {
            36653,
            36654,
            36655,
            36656,
            36657
    };
    private static final int[] ketraMobs = {
            36661,
            36662,
            36663,
            36664,
            36665
    };

    private int damagePoint = 20;

    public IntermediarBaseInstance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
        //this.setAI(new BaseAI(this));
        if (!this.is_block_move()) {
            this.p_block_move(true, null);
        }
        faction = Faction.VARKA;
    }

    public IntermediarBaseInstance getInstances() {
        return this;
    }

    @Override
    public boolean isInvul() {
        return false;
    }

    private static void attack(L2Character attacker, L2NpcInstance guard) {
        if (attacker != null) {
            attacker.addDamageHate(guard, 0, Rnd.get(1, 100)); // Это нужно, чтобы гвард не перестал атаковать цель после первых ударов
            guard.setRunning();
            guard.setAttackTimeout(20000 + System.currentTimeMillis()); // Это нужно, чтобы не сработал таймаут
            guard.getAI().setAttackTarget(attacker); // На всякий случай, не обязательно делать
            guard.getAI().changeIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null); // Переводим в состояние атаки
            guard.getAI().addTaskAttack(attacker); // Добавляем отложенное задание атаки, сработает в самом конце движения
        }
    }

    public Faction getFaction() {
        return this.faction;
    }

    @Override
    public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean isDot, double i2, boolean sendMesseg, boolean bow, boolean crit, boolean tp) {
        damagePoint--;
        attacker.sendMessage("Point = " + damagePoint);

        if (damagePoint <= 0) {
            if (faction == Faction.VARKA) {
                this.setTitle(titleKetra);
                faction = Faction.KETRA;
                if (_guardSpawn != null) {
                    _guardSpawn.cancel(true);
                }
                _general = null;
                _magus = null;
                _priest = null;
                _guard = null;
                _scout = null;

                _guardSpawn = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SpawnTask(this, ketraMobs), 3000, 10000);
                for (L2Player player : L2World.getAroundPlayers(this)) {
                    if (player == null) {
                        continue;
                    }
                    player.sendPacket(new NpcInfo(this, player));
                    player.sendPacket(new ExShowScreenMessage("База 1 была захвачена лагерем Кетра", 3000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
                    player.sendMessage("База 1 была захвачена лагерем Кетра");
                }
            } else if (faction == Faction.KETRA) {
                this.setTitle(titleVakra);
                faction = Faction.VARKA;
                if (_guardSpawn != null) {
                    _guardSpawn.cancel(true);
                }
                _general = null;
                _magus = null;
                _priest = null;
                _guard = null;
                _scout = null;

                _guardSpawn = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SpawnTask(this, varkaMobs), 3000, 10000);
                for (L2Player player : L2World.getAroundPlayers(this)) {
                    if (player == null) {
                        continue;
                    }
                    player.sendPacket(new NpcInfo(this, player));
                    player.sendPacket(new ExShowScreenMessage("База была захвачена лагерем Варка", 3000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, 1, -1, true));
                    player.sendMessage("База была захвачена лагерем Варка");
                }
            }
            for (L2NpcInstance npc : guards) {
                npc.deleteMe();
            }
            damagePoint = 20;
        }
    }

    @Override
    public void onSpawn() {
        _guardSpawn = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SpawnTask(this, varkaMobs), 2000, 10000);
        super.onSpawn();
    }

    @Override
    public boolean isAutoAttackable(L2Character attacker) {
        if (attacker == null) {
            return false;
        }
        L2Player player = attacker.getPlayer();
        if (player == null) {
            return false;
        }
        if (this.getTitle().equalsIgnoreCase(titleKetra) && player.getTeam() == 1) {
            return true;
        } else if (this.getTitle().equalsIgnoreCase(titleVakra) && player.getTeam() == 2) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isAttackable(L2Character attacker) {
        return isAutoAttackable(attacker);
    }

    public class SpawnTask extends l2open.common.RunnableImpl {
        Location loc;
        L2NpcInstance actor;
        int[] mobsID;

        public SpawnTask(L2NpcInstance actor, int[] mobsID) {
            this.actor = actor;
            this.loc = actor.getLoc();
            this.mobsID = mobsID;
        }

        private L2NpcInstance spawn(L2NpcInstance npc, int id) {
            if (npc == null || npc.isDead() || npc.isDecayed()) {
                loc = new Location(actor.getX() + Rnd.get(-200, 200), actor.getY() + Rnd.get(-200, 200), actor.getZ());
                npc = Functions.spawn(loc, id, 0, actor.getReflectionId());
                npc.boss = actor;
                npc.setTitle(actor.getObjectId() + "");

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
}
