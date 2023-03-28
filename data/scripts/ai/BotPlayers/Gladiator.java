package ai.BotPlayers;

import l2open.common.ThreadPoolManager;
import l2open.gameserver.model.*;
import l2open.util.GArray;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Gladiator extends BotPlayerAI {
    private Integer [] restrictedSkills = {
            1159, // Curse Death Link
            56, //Power Shot
            1456, //Wind Vortex Slug


    };

//    private static final List<Integer> rangeAttackSkills = new ArrayList<Integer>(){{
//        add(6);
//        //add(7);
//        //add(994);
//    }};
//    private static final List<Integer> meleeAttackSkills = new ArrayList<Integer>(){{
//        add(1);
//        add(9);
//
//    }};
//    private static final List<Integer> buffSkills = new ArrayList<Integer>(){{
//        add(78);
//       // add(8);
//
//    }};
//    private static final List<Integer> healSkills = new ArrayList<Integer>(){{add(121); }};
//    private static final List<Integer> deDuffSkills = new ArrayList<Integer>(){{add(0); }};
//    private static final List<Integer> ultimateSkills = new ArrayList<Integer>(){{add(287); }};

    public Gladiator(L2Player actor) {
        super(actor);
        spawnLoc = getActor().getLoc();
        targetList = new ArrayList<L2Character>();
        _restrictedSkill = fillRestrictedSkill(restrictedSkills);
        allSkills = skills();

        _attackTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AttackTask(), _botTaskDelay, _botTaskDelay);
        //_updateTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new UpdateTask(getActor(), getActor().getName()), _updateTaskDelay, _updateTaskDelay);
        //_selfBuffTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SelfBuffTask(), _buffTaskDelay, _buffTaskDelay);

    }

    @Override
    public void updateParty(L2Player actor) {
        super.updateParty(actor);
    }

    private GArray<L2Skill> skills(){
        if (getActor().isMageClass()){
            return getActor().getSkillsByType(L2Skill.SkillType.MDAM);
        }else {
            return getActor().getSkillsByType(L2Skill.SkillType.PDAM);
        }
    }



    private ArrayList<L2Skill> fillRestrictedSkill(Integer [] skill_id){
        ArrayList<L2Skill> restrictedSkill = new ArrayList<>();
        for (int id:  skill_id){
            L2Skill knownSkill = getActor().getKnownSkill(id);
            if (knownSkill != null){
                restrictedSkill.add(knownSkill);
            }
        }
        return restrictedSkill;
    }


    private ArrayList<L2Skill> fillSkillList(List<Integer> s){
//        if (!getActor().isOnline()){
//            _updateTask.cancel(true);
//        }

        ArrayList<L2Skill> skillList = new ArrayList<>();
        for (Integer i: s){
            L2Skill knownSkill = getActor().getKnownSkill(i);
            if (knownSkill != null){
                skillList.add(knownSkill);
            }
        }
        return skillList;
    }





}