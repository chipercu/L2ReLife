package ai.BotPlayers;

import l2open.common.ThreadPoolManager;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BerserkerAI extends FakePlayerAI {

//    add(948); //Eye for Eye
//    add(483); //Sword Shield
//
//    add(1510); //Soul Cleanse
//    add(498); //Contagion

    private static final List<Integer> energySkillList  = new ArrayList<Integer>(){{

    }};
    private static final List<Integer> rushSkillList  = new ArrayList<Integer>(){{
        add(793); //Rush Impact
        add(493); //Storm Assault
        add(495); //Blade Rush
        add(494); //Shoulder Charge
        add(484); //Rush
    }};
    private static final List<Integer> pvpBuffSkillList  = new ArrayList<Integer>(){{
        add(917); //Final Secret
        add(499); //Courage
        add(1514); //Soul Barrier

    }};
    private static final List<Integer> ultimateSkillList  = new ArrayList<Integer>(){{

    }};
    private static final List<Integer> meleeDebuffSkillList  = new ArrayList<Integer>(){{
//        add(794); //Mass Disarm
//        add(485); //Disarm
        add(501); //Violent Temper
    }};
    private static final List<Integer> rangeDebuffSkillList  = new ArrayList<Integer>(){{
        add(501); //Violent Temper
    }};
    private static final List<Integer> healSkillList  = new ArrayList<Integer>(){{

    }};
    private static final List<Integer> selfHealSkillList  = new ArrayList<Integer>(){{
        add(833); //Body Reconstruction

    }};
    private static final List<Integer> partyBuffSkillList  = new ArrayList<Integer>(){{

    }};
    private static final List<Integer> selfBuffSkillList  = new ArrayList<Integer>(){{
        add(482); //Furious Soul
        add(834); //Blood Pact
        add(20006); //Soul Roar
    }};
    private static final List<Integer> restrictedSkills  = new ArrayList<Integer>(){{

    }};
    private static final List<Integer> rangeAttackSkillList  = new ArrayList<Integer>(){{

    }};
    private static final List<Integer> rangeAOEAttackSkillList  = new ArrayList<Integer>(){{

    }};
    private static final List<Integer> meleeAttackSkillList  = new ArrayList<Integer>(){{
        add(497); //Crush of Pain
        add(526); //Enuma Elish
        add(477); //Dark Smash
        add(478); //Double Thrust
        add(476); //Dark Strike
        add(468); //Fallen Attack

    }};
    private static final List<Integer> meleeAOEAttackSkillList  = new ArrayList<Integer>(){{
        add(496); //Slashing Blade
        add(492); //Spread Wing
    }};

    public BerserkerAI(L2Player actor) {
        super(actor);
        spawnLoc = getActor().getLoc();
        targetList = new ArrayList<L2Character>();
        _restrictedSkill = getActor().getAllSkills().stream().filter(s -> restrictedSkills.contains(s.getId())).collect(Collectors.toList());
        _rangeAttackSkillList = getActor().getAllSkills().stream().filter(s -> rangeAttackSkillList.contains(s.getId())).collect(Collectors.toList());
        _rangeAOEAttackSkillList = getActor().getAllSkills().stream().filter(s -> rangeAOEAttackSkillList.contains(s.getId())).collect(Collectors.toList());
        _meleeAttackSkillList = getActor().getAllSkills().stream().filter(s -> meleeAttackSkillList.contains(s.getId())).collect(Collectors.toList());
        _meleeAOEAttackSkillList = getActor().getAllSkills().stream().filter(s -> meleeAOEAttackSkillList.contains(s.getId())).collect(Collectors.toList());
        _selfBuffSkillList = getActor().getAllSkills().stream().filter(s -> selfBuffSkillList.contains(s.getId())).collect(Collectors.toList());
        _partyBuffSkillList = getActor().getAllSkills().stream().filter(s -> partyBuffSkillList.contains(s.getId())).collect(Collectors.toList());
        _selfHealSkillList = getActor().getAllSkills().stream().filter(s -> selfHealSkillList.contains(s.getId())).collect(Collectors.toList());
        _healSkillList = getActor().getAllSkills().stream().filter(s -> healSkillList.contains(s.getId())).collect(Collectors.toList());
        _rangeDebuffSkillList = getActor().getAllSkills().stream().filter(s -> rangeDebuffSkillList.contains(s.getId())).collect(Collectors.toList());
        _meleeDebuffSkillList = getActor().getAllSkills().stream().filter(s -> meleeDebuffSkillList.contains(s.getId())).collect(Collectors.toList());
        _ultimateSkillList = getActor().getAllSkills().stream().filter(s -> ultimateSkillList.contains(s.getId())).collect(Collectors.toList());
        _pvpBuffSkillList = getActor().getAllSkills().stream().filter(s -> pvpBuffSkillList.contains(s.getId())).collect(Collectors.toList());
        _rushSkillList = getActor().getAllSkills().stream().filter(s -> rushSkillList.contains(s.getId())).collect(Collectors.toList());
        _energySkillList = getActor().getAllSkills().stream().filter(s -> energySkillList.contains(s.getId())).collect(Collectors.toList());


        _attackTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AttackTask(), _botTaskDelay, _botTaskDelay);

    }

    @Override
    public void updateParty(L2Player actor) {
        super.updateParty(actor);
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








}