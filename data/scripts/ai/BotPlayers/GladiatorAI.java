package ai.BotPlayers;

import l2open.common.ThreadPoolManager;
import l2open.gameserver.model.L2Character;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GladiatorAI extends FakePlayerAI {


    private static final List<Integer> energySkillList  = new ArrayList<Integer>(){{
        add(8); //Sonic Focus
    }};
        private static final List<Integer> rushSkillList  = new ArrayList<Integer>(){{
            add(994); //Rush
            add(995); //Rush Impact
    }};
        private static final List<Integer> pvpBuffSkillList  = new ArrayList<Integer>(){{
            add(917); //Final Secret
            add(451); //Sonic Move
    }};
        private static final List<Integer> ultimateSkillList  = new ArrayList<Integer>(){{
            add(287); //Lionheart
            add(440); //Braveheart
    }};
        private static final List<Integer> meleeDebuffSkillList  = new ArrayList<Integer>(){{
            add(775); //Weapon Blockade
    }};

    private static final List<Integer> rangeDebuffSkillList  = new ArrayList<Integer>(){{

    }};

    private static final List<Integer> healSkillList  = new ArrayList<Integer>(){{

    }};

    private static final List<Integer> selfHealSkillList  = new ArrayList<Integer>(){{
        add(121); //Battle Roar
    }};

    private static final List<Integer> partyBuffSkillList  = new ArrayList<Integer>(){{

    }};
    private static final List<Integer> selfBuffSkillList  = new ArrayList<Integer>(){{
        add(297); //Duelist Spirit
        add(78); //War Cry
    }};
    private static final List<Integer> restrictedSkills  = new ArrayList<Integer>(){{
        add(80); //Detect Beast Weakness
        add(87); //Detect Animal Weakness
        add(88); //Detect Dragon Weakness
        add(104); //Detect Plant Weakness
        add(75); //Detect Insect Weakness
        add(359); //Eye of Hunter
        add(360); //Eye of Slayer
        }};
    private static final List<Integer> rangeAttackSkillList  = new ArrayList<Integer>(){{
        add(6); //Sonic Blaster
        add(345); //Sonic Rage
        add(56); //Power Shot
    }};
    private static final List<Integer> rangeAOEAttackSkillList  = new ArrayList<Integer>(){{
        add(7); //Sonic Storm
    }};
    private static final List<Integer> meleeAttackSkillList  = new ArrayList<Integer>(){{
        add(1); //Triple Slash
        add(3); //Power Strike
        add(260); //Hammer Crush
        add(5); //Double Sonic Slash
        add(261); //Triple Sonic Slash
        add(16); //Mortal Blow
        add(190); //Fatal Strike
        add(100); //Stun Attack
        add(255); //Power Smash
    }};
    private static final List<Integer> meleeAOEAttackSkillList  = new ArrayList<Integer>(){{
        add(245); //Wild Sweep
        add(9); //Sonic Buster
    }};

    public GladiatorAI(L2Player actor) {
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