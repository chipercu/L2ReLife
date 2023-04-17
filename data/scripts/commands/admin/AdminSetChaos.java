package commands.admin;

import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.handler.AdminCommandHandler;
import l2open.gameserver.handler.IAdminCommandHandler;
import l2open.gameserver.model.L2Object;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.skills.AbnormalVisualEffect;

public class AdminSetChaos implements IAdminCommandHandler, ScriptFile {

    private static enum Commands
    {
        admin_set_chaos
    }

    @Override
    public void onLoad() {
        AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
    }

    @Override
    public void onReload() {

    }

    @Override
    public void onShutdown() {

    }

    @Override
    public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar) {
        Commands command = (Commands) comm;
        AbnormalVisualEffect speed_down = AbnormalVisualEffect.ave_speed_down;
        AbnormalVisualEffect red_pulse = AbnormalVisualEffect.ave_air_battle_root;
        AbnormalVisualEffect frozen = AbnormalVisualEffect.ave_frozen_pillar;
        AbnormalVisualEffect stun = AbnormalVisualEffect.ave_ghost_stun;
        AbnormalVisualEffect big_body = AbnormalVisualEffect.ave_big_body;
        AbnormalVisualEffect grizli = AbnormalVisualEffect.ave_vp_keep;

        switch (command){

            case admin_set_chaos:{
                final L2Object target = activeChar.getTarget();
                if (target != null){
                    if (target.isPlayer()){
                        L2Player player = (L2Player) target;
                        player.startAbnormalEffect(grizli);
                        activeChar.sendMessage("addEffect" + player.getAbnormalEffect());
                    } else if (target.isNpc()) {
                        L2NpcInstance npc = (L2NpcInstance) target;
                        npc.startAbnormalEffect(big_body);
                        npc.startAbnormalEffect(grizli);
                        activeChar.sendMessage("addEffect" + npc.getAbnormalEffect());
                    }


                }




            }
        }



        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }
}
