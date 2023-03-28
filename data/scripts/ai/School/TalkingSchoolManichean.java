package ai.School;

import l2open.gameserver.ai.DefaultAI;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.quest.QuestState;
import l2open.gameserver.serverpackets.NpcSay;

/**
 * @author: FuzzY
 * АИ для маникенов в начальных школах для квеста начало пути.
 */

public class TalkingSchoolManichean extends DefaultAI
{
    public TalkingSchoolManichean(L2Character actor)
    {
        super(actor);
        actor.p_block_move(true, null);
    }
    @Override
    protected void ATTACKED(L2Character attacker, int damage, L2Skill skill)
    {
        if(IsNullCreature(attacker) == 0)
        {
            L2Player player = (L2Player) attacker;
            QuestState qs = player.getQuestState("_10603_TheBegginingOfTheWay");
            if (qs != null){
                int manicheanDamage = qs.getInt("ManicheanDamage");
                if (qs.getCond() == 10 && manicheanDamage < 250){
                    qs.set("ManicheanDamage" , manicheanDamage + damage);
                }else if (qs.getCond() == 12 && manicheanDamage < 250) {
                    if (!skill.isPassive()){
                        qs.set("ManicheanDamage" , (manicheanDamage + damage) * 3);
                    }
                    qs.set("ManicheanDamage" , manicheanDamage + damage);
                }else if ((qs.getCond() == 10 || qs.getCond() == 12) && manicheanDamage >= 250){
                    qs.setCond(qs.getCond() + 1);
                    qs.set("ManicheanDamage" , 0);
                }
            }
        }
        //super.ATTACKED(attacker, damage, skill);
    }


    @Override
    protected void onEvtSpawn()
    {
        super.onEvtSpawn();
    }

}
