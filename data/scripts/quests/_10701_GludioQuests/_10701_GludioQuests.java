package quests._10701_GludioQuests;

import l2open.extensions.scripts.ScriptFile;
import quests.TownsQuests.TownsQuests;

public class _10701_GludioQuests extends TownsQuests implements ScriptFile {

    public _10701_GludioQuests() {
        addStartNpc(GLUDIO_CAPITAN);
        addTalkId();
        addAttackId();
        addKillId();
        addQuestItem();
    }


}
