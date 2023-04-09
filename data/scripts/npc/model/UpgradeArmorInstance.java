package npc.model;

import fuzzy.Html_Constructor.tags.Button;
import fuzzy.Html_Constructor.tags.Font;
import fuzzy.Html_Constructor.tags.Img;
import fuzzy.Html_Constructor.tags.Table;
import fuzzy.Html_Constructor.tags.parameters.Color;
import fuzzy.Html_Constructor.tags.parameters.Parameters;
import l2open.config.ConfigValue;
import l2open.gameserver.model.L2ObjectsStorage;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.base.L2Augmentation;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.items.L2ItemInstance;
import l2open.gameserver.serverpackets.InventoryUpdate;
import l2open.gameserver.serverpackets.NpcHtmlMessage;
import l2open.gameserver.serverpackets.SystemMessage;
import l2open.gameserver.tables.SkillTable;
import l2open.gameserver.tables.player.PlayerData;
import l2open.gameserver.templates.L2EtcItem;
import l2open.gameserver.templates.L2Item;
import l2open.gameserver.templates.L2NpcTemplate;
import l2open.gameserver.templates.OptionDataTemplate;
import l2open.gameserver.xml.ItemTemplates;
import l2open.gameserver.xml.loader.XmlOptionDataLoader;
import l2open.util.Rnd;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import static fuzzy.Html_Constructor.tags.parameters.Position.*;


public class UpgradeArmorInstance extends L2NpcInstance implements Parameters {

    private static final int[] staticStat = {10, 20, 30 , 40, 55, 70, 85, 100, 130, 160, 190, 235, 280, 340, 400};
    private static final double[] percentStat = {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5};
    private static final double[] static2Stat = {1, 2, 3 , 4, 5.5, 7, 8.5, 10, 13, 16, 19, 23.5, 28, 34, 40};

    private static final int ESSENS_D = 41001;
    private static final int ESSENS_C = 41002;
    private static final int ESSENS_B = 41003;
    private static final int ESSENS_A = 41004;
    private static final int ESSENS_S = 41005;

    private static final L2Item ess_d = ItemTemplates.getInstance().getTemplate(ESSENS_D);
    private static final L2Item ess_c = ItemTemplates.getInstance().getTemplate(ESSENS_C);
    private static final L2Item ess_b = ItemTemplates.getInstance().getTemplate(ESSENS_B);
    private static final L2Item ess_a = ItemTemplates.getInstance().getTemplate(ESSENS_A);
    private static final L2Item ess_s = ItemTemplates.getInstance().getTemplate(ESSENS_S);

    private static final int[] skills= {26086, 26087, 26088, 26089, 26090, 26091, 26092, 26093};



    private static final int ADENA = 57;
    private static final int ADENA_PRICE = 57;
    private static final int arena_coefficient = 1;





    private static final String noIcon = "icon.NOIMAGE";


    private static Logger _log = Logger.getLogger(UpgradeArmorInstance.class.getName());

    public UpgradeArmorInstance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
    }

    private String getImage(L2ItemInstance item, int w, int h){
        return "<img src=\"" + item.getItem().getIcon() + "\" " + width(w) + height(h) + ">";
    }

    public void removeAugment(L2ItemInstance item, L2Player player){
        item.getAugmentation().removeBoni(player, true);
        // remove the augmentation
        PlayerData.getInstance().removeAugmentation(item);
        // send inventory update
        InventoryUpdate iu = new InventoryUpdate();
        iu.addModifiedItem(item);
        // send system message
        SystemMessage sm = new SystemMessage(SystemMessage.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1);
        sm.addItemName(item.getItemId());
//        player.sendPacket(new ExVariationCancelResult(1), iu, sm);

        player.broadcastUserInfo(true);
    }
    public void setAugment(L2Player player, L2ItemInstance item, int id){
        OptionDataTemplate template = XmlOptionDataLoader.getInstance().getTemplate(id);
        L2Skill skill = null;
        if (template != null) {
            if (template.getSkills().size() > 0){
                skill = template.getSkills().get(0);
            }
            if (skill == null && template.getTriggerList().size() > 0){
                skill = template.getTriggerList().get(0).getSkill();
            }
        }
        int stat16 = ConfigValue.JewelAugmentOptionStat;
        item.setAugmentation(new L2Augmentation(((id << 16) + stat16), skill));

        if (item.isEquipped()){
            item.getAugmentation().applyBoni(player, true);
        }
        player.updateStats();
        player.sendPacket(new InventoryUpdate().addModifiedItem(item));
        player.sendUserInfo(false);
    }

    public void addSkill(L2Player player, int skillId, int itemObjectId){
        final L2ItemInstance item = player.getInventory().getItemByObjectId(itemObjectId);
        if (item != null){
            if (skillId == 26086) {
                setAugment(player, item, 25001);
            } else if (skillId == 26087) {
                setAugment(player, item, 25016);
            } else if (skillId == 26088) {
                setAugment(player, item, 25031);
            } else if (skillId == 26089) {
                setAugment(player, item, 25046);
            } else if (skillId == 26090) {
                setAugment(player, item, 25061);
            } else if (skillId == 26091) {
                setAugment(player, item, 25076);
            } else if (skillId == 26092) {
                setAugment(player, item, 25091);
            } else if (skillId == 26093) {
                setAugment(player, item, 25106);
            }

        }
    }

    public void increaseSkillLevel(L2Player player, int itemObjectId){
        final L2ItemInstance item = player.getInventory().getItemByObjectId(itemObjectId);
        final int augmentationId = item.getAugmentation().getAugmentationId();
        if (item.getAugmentation().getSkill().getLevel() >= 15){
            return;
        }
        removeAugment(item, player);
        int nextSkill = (augmentationId >> 16) + 1;
        setAugment(player, item, nextSkill);
    }


    public String progress(int lvl){

        final Table table = new Table(1, 15);
        table.setParams(cellspacing(3), width(220));
        String red = "L2UI_CT1.DeBuffFrame_24";
        String green = "L2UI_CT1.BuffFrame_24_3";
        String pink = "L2UI_NewTex.ServerBTN_Cancel_Over0001";
        String black = "L2UI_CT1.BuffFrame_24_1";
        String hz = "L2UI_SCRYDE.cyber_button_DF";
        final String enchanted = new Img(hz, 8, 16).build();
        final String no_enchant = new Img(black, 8, 16).build();

        for (int i = 0; i < 15; i++) {
            if (lvl <= i){
                table.row(0).col(i).setParams(width(8), height(16), valign(TOP)).insert(no_enchant);
            }else {
                table.row(0).col(i).setParams(width(8), height(16), valign(TOP)).insert(enchanted);
            }
        }
        return table.build();
    }

    private String htmlG1(L2ItemInstance item, boolean showButton){

            L2Skill skill = null;

            if (item.isAugmented()){
                skill = item.getAugmentation().getSkill();
            }

            int border = 0;
            String tableBackground = "l2ui_ct1.Windows_DF_TooltipBG";

            Img armor_img = new Img(item.getItem().getIcon(), 32, 32);
            Img skill_img = new Img(skill != null ? skill.getIcon(): noIcon, 32, 32);

            Table armorTable = new Table(3, 1);
            armorTable.row(0).col(0).setParams(height(17));
            armorTable.row(1).col(0).insert(armor_img.build());

            Table skillTable = new Table(1, 2);
            skillTable.setParams(width(240));
            skillTable.row(0).col(0).setParams(width(34), height(34), align(CENTER), valign(TOP)).insert(skill != null ? skill_img.build() : "");
            skillTable.row(0).col(1).setParams(width(206)).insert(skill != null ? new Font( Color.GOLD, skill.getName() + " : " + skill.getLevel() + " ур.").build() : new Font( Color.RED, "Нет Усиления").build());

            Table progressTable = new Table(2, 1);
            progressTable.row(0).col(0).setParams(height(5));
            progressTable.row(0).col(0).insert(progress(skill != null ? skill.getLevel() : 0));

            Table skill_progress = new Table(2, 1);
            skill_progress.setParams(width(246));
            skill_progress.row(0).col(0).setParams(height(34)).insert(skillTable.build());
            skill_progress.row(1).col(0).setParams(height(34)).insert(progressTable.build());

            Table buttonTable = new Table(2, 1).setParams(height(68));
            String back = "L2UI_CT1.SystemMenuWnd_df_Macro_Down";
            String fore = "L2UI_CT1.SystemMenuWnd_df_Macro";
            String action = "increaseSkillLevel:"+ item.getObjectId();
            final Button confirmButton = new Button(actionNpc(action), 32, 32, back, fore);
            buttonTable.row(0).col(0).setParams(height(17));
            buttonTable.row(1).col(0).insert(confirmButton.build());

            Table table = new Table(1, 3).setParams(border(border));
            table.setParams(width(320), background(tableBackground));
            table.row(0).col(0).setParams(width(40), height(68), align(CENTER), valign(TOP)).insert(armorTable.build());
            table.row(0).col(1).setParams(width(246)).insert(skill_progress.build());
            table.row(0).col(2).setParams(width(34)).insert(showButton ? buttonTable.build() : "");

            return table.build();
    }


    public void showUpGradeHtml(L2Player player, int itemObjectId, int skillId){
        NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        final L2ItemInstance item = player.getInventory().getItemByObjectId(itemObjectId);

        if (item == null){
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(htmlG1(item, false));

        if (!item.isAugmented()){
            L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
            sb.append(htmlSkillSelect(player, skill, itemObjectId));
        }else {
            final L2Skill currentSkill = item.getAugmentation().getSkill();
            final L2Skill nextSkill = SkillTable.getInstance().getInfo(currentSkill.getId(), currentSkill.getLevel() + 1);

            L2Item firstEssens = getEssens(item)[0] != 0 ? ItemTemplates.getInstance().getTemplate(getEssens(item)[0]) : null;
            int firstEssensCount = getEssens(item)[1];
            L2Item secondEssens = getEssens(item)[2] != 0 ? ItemTemplates.getInstance().getTemplate(getEssens(item)[2]) : null;
            int secondEssensCount = getEssens(item)[3];
            int adenaCount = getEssens(item)[4];


            String back = "L2UI_CT1.Icon_df_Min_MacroEdit_Down";
            String fore = "L2UI_CT1.Icon_df_Min_MacroEdit";
            String action = "upSkill:" + itemObjectId + ":" + nextSkill.getId();
            final Button confirmButton = new Button(actionNpc(action), 32, 32, back, fore);
            Table updateTable = new Table(1, 5).setParams(border(0));
            updateTable.row(0).col(0).setParams(align(CENTER), valign(TOP),height(34), width(40))
                    .insert(firstEssens != null ? new Img(firstEssens.getIcon() , 32, 32).build() : "");

            updateTable.row(0).col(1).setParams(width(70))
                    .insert(new Font(Color.GOLD, "  " + firstEssensCount).build());

            updateTable.row(0).col(2).setParams(align(CENTER), valign(TOP),width(40))
                    .insert(secondEssens != null ? new Img(secondEssens.getIcon() , 32, 32).build() : "");

            updateTable.row(0).col(3).setParams(width(130))
                    .insert(secondEssens != null ? new Font(Color.GOLD, "  " + secondEssensCount).build() : "");

            updateTable.row(0).col(4).setParams(align(CENTER), valign(TOP),width(40)).insert(confirmButton.build());


            Table table = new Table(5, 1).setParams(width(320), border(0));
            table.row(0).col(0).setParams(height(50)).insert(new Font(Color.RED, getSkillInfo(currentSkill)).build());
            table.row(1).col(0).insert(separator(320, 5));
            table.row(2).col(0).setParams(height(50)).insert(new Font(Color.GOLD, getSkillInfo(nextSkill)).build());
            table.row(3).col(0).insert(separator(320));
            table.row(4).col(0).setParams(height(50)).insert(updateTable.build());

            sb.append(table.build());
        }

        player.sendPacket(html.setHtml(sb.toString()));
    }

    public int[] getEssens(L2ItemInstance armor){

        int[] items = new int[5];

        if (armor.isAugmented()){
            final short level = armor.getAugmentation().getSkill().getLevel();
            if (level == 1) {
                items[0] = ess_d.getItemId();
                items[1] = 2;
                items[2] = 0;
                items[3] = 0;
            } else if (level == 2) {
                items[0] = ess_d.getItemId();
                items[1] = 3;
                items[2] = 0;
                items[3] = 0;
            } else if (level == 3) {
                items[0] = ess_c.getItemId();
                items[1] = 1;
                items[2] = ess_d.getItemId();
                items[3] = 1;
            } else if (level == 4) {
                items[0] = ess_c.getItemId();
                items[1] = 2;
                items[2] = ess_d.getItemId();
                items[3] = 2;
            } else if (level == 5) {
                items[0] = ess_c.getItemId();
                items[1] = 3;
                items[2] = ess_d.getItemId();
                items[3] = 3;
            } else if (level == 6) {
                items[0] = ess_b.getItemId();
                items[1] = 1;
                items[2] = ess_c.getItemId();
                items[3] = 1;
            } else if (level == 7) {
                items[0] = ess_b.getItemId();
                items[1] = 2;
                items[2] = ess_c.getItemId();
                items[3] = 2;
            } else if (level == 8) {
                items[0] = ess_b.getItemId();
                items[1] = 3;
                items[2] = ess_c.getItemId();
                items[3] = 3;
            } else if (level == 9) {
                items[0] = ess_a.getItemId();
                items[1] = 1;
                items[2] = ess_b.getItemId();
                items[3] = 1;
            } else if (level == 10) {
                items[0] = ess_a.getItemId();
                items[1] = 2;
                items[2] = ess_b.getItemId();
                items[3] = 2;
            } else if (level == 11) {
                items[0] = ess_a.getItemId();
                items[1] = 3;
                items[2] = ess_b.getItemId();
                items[3] = 3;
            } else if (level == 12) {
                items[0] = ess_s.getItemId();
                items[1] = 1;
                items[2] = ess_a.getItemId();
                items[3] = 1;
            } else if (level == 13) {
                items[0] = ess_s.getItemId();
                items[1] = 2;
                items[2] = ess_a.getItemId();
                items[3] = 2;
            } else if (level == 14) {
                items[0] = ess_s.getItemId();
                items[1] = 3;
                items[2] = ess_a.getItemId();
                items[3] = 3;
            }
            items[4] = (ADENA_PRICE * level) * arena_coefficient;
        }else {
            items[0] = ess_d.getItemId();
            items[1] = 1;
            items[2] = 0;
            items[3] = 0;
            items[4] = ADENA_PRICE * arena_coefficient;
        }
        return items;
    }

    public String htmlSkillSelect(L2Player player, L2Skill skill, int itemObjectId){
        String forePrevLeft = "L2UI_CT1.ICON_DF_CHARACTERTURN_LEFT";
        String backPrevLeft = "L2UI_CT1.ICON_DF_CHARACTERTURN_LEFT_Down";
        String foreNextLeft = "L2UI_CT1.ICON_DF_CHARACTERTURN_RIGHT";
        String backNextLeft = "L2UI_CT1.ICON_DF_CHARACTERTURN_RIGHT_DOWN";
        Img skill_img = new Img(skill.getIcon(), 32, 32);
        int nextSkill;
        int prevSkill;

        final L2ItemInstance item = player.getInventory().getItemByObjectId(itemObjectId);

        L2Item firstEssens = ItemTemplates.getInstance().getTemplate(getEssens(item)[0]);
        int firstEssensCount = getEssens(item)[1];
        L2Item secondEssens = getEssens(item)[2] != 0 ? ItemTemplates.getInstance().getTemplate(getEssens(item)[2]) : null;
        int secondEssensCount = getEssens(item)[3];
        int adenaCount = getEssens(item)[4];

        switch (skill.getId()){
            case 26086: {
                nextSkill = 26087;
                prevSkill = 26093;
                break;
            }
            case 26093: {
                nextSkill = 26086;
                prevSkill = 26092;
                break;
            }
            default:{
                nextSkill = skill.getId() + 1;
                prevSkill = skill.getId() - 1;
            }
        }
        final Button prev = new Button(actionNpc("nextSkill:" + prevSkill + ":" + itemObjectId), 64, 64, backPrevLeft, forePrevLeft);
        final Button next = new Button(actionNpc("nextSkill:" + nextSkill + ":" + itemObjectId), 64, 64, backNextLeft, foreNextLeft);

        Table skillSelector = new Table(1, 3).setParams(height(64));
        skillSelector.row(0).col(0).setParams(align(RIGHT), valign(CENTER), width(100))
                .insert(prev.build());

        skillSelector.row(0).col(1).setParams(align(CENTER), valign(CENTER), width(120)).insert(skill_img.build());

        skillSelector.row(0).col(2).setParams(align(LEFT), valign(CENTER), width(100))
                .insert(next.build());

        String back = "L2UI_CT1.Icon_df_Min_MacroEdit_Down";
        String fore = "L2UI_CT1.Icon_df_Min_MacroEdit";
        String action = "addSkill:" + skill.getId() + ":" +  itemObjectId;
        final Button confirmButton = new Button(actionNpc(action), 32, 32, back, fore);
        Table updateTable = new Table(1, 5).setParams(border(0));
        updateTable.row(0).col(0).setParams(align(CENTER), valign(TOP),height(34), width(40)).insert(new Img(firstEssens.getIcon(), 32, 32).build());
        updateTable.row(0).col(1).setParams(width(70)).insert(new Font(Color.GOLD, "51515").build());
        updateTable.row(0).col(2).setParams(align(CENTER), valign(TOP),width(40)).insert(new Img(noIcon, 32, 32).build());
        updateTable.row(0).col(3).setParams(width(130)).insert(new Font(Color.GOLD, "151515").build());
        updateTable.row(0).col(4).setParams(align(CENTER), valign(TOP),width(40)).insert(confirmButton.build());


        Table newSkill = new Table(5, 1).setParams(border(0), width(320));
        newSkill.row(0).col(0).setParams(height(64)).insert(skillSelector.build());
        newSkill.row(1).col(0).setParams(width(320)).insert(separator(320));
        newSkill.row(2).col(0).setParams(align(CENTER), height(64))
                .insert(new Font(Color.GOLD, getSkillInfo(skill)).build());
        newSkill.row(3).col(0).setParams(width(320)).insert(separator(320));
        newSkill.row(4).col(0).setParams(height(64)).insert(updateTable.build());

        return newSkill.build();
    }


    public String getSkillInfo(L2Skill skill){
        String info = "<br>" + skill.getName() + " : " + skill.getLevel()  + " ур. <br>";
        int skillId = skill.getId();
        if (skillId == 26086) {
            return  info + " Увеличивает физическую защиту на " + staticStat[skill.getLevel() - 1] + " ед.";
        } else if (skillId == 26087) {
            return  info + " Увеличивает магическую защиту на " + staticStat[skill.getLevel() - 1] + " ед.";
        } else if (skillId == 26088) {
            return  info + " Увеличивает защиту от Оглушения на " + percentStat[skill.getLevel() - 1] + " %.";
        } else if (skillId == 26089) {
            return  info + " Увеличивает защиту от Паралича на " + percentStat[skill.getLevel() - 1] + " %.";
        } else if (skillId == 26090) {
            return  info + " Увеличивает шанс Крит. Атаки на " + static2Stat[skill.getLevel() - 1] + " ед.";
        } else if (skillId == 26091) {
            return  info + " Увеличивает защиту от Крит. Атаки на " + staticStat[skill.getLevel() - 1] + " ед.";
        } else if (skillId == 26092) {
            return  info + " Увеличивает защиту ко Сну и Удержанию на " + static2Stat[skill.getLevel() - 1] + " ед.";
        } else if (skillId == 26093) {
            return  info + " Увеличивает защиту Отравлению и Кровотечению на " + static2Stat[skill.getLevel() - 1] + " ед.";
        }
        return info;
    }

    public void showDownGradeHtml(L2Player player, int slot){
        NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        final L2ItemInstance item = player.getInventory().getPaperdollItem(slot);
        if (item == null){
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table border=1 cellspacing=0 cellpadding=2 width=320 background=\"l2ui_ct1.Windows_DF_TooltipBG\">")
                .append("<tr>")
                .append("<td>").append("</td>")
                .append("<td>").append("</td>")
                .append("<td height=40 width=32>").append(getImage(item, 32, 32)).append("</td>")
                .append("<td>").append("</td>")
                .append("<td>").append("</td>")
                .append("</tr>")
                .append("<tr>")
                .append("<td>").append("</td>")
                .append("<td>").append("</td>")
                .append("<td height=40 width=32>").append(getImage(item, 32, 32)).append("</td>")
                .append("<td>").append("</td>")
                .append("<td>").append("</td>")
                .append("</tr>")
                .append("<tr>")
                .append("<td>").append("</td>")
                .append("<td>").append("</td>")
                .append("<td height=40 width=32>").append(getImage(item, 32, 32)).append("</td>")
                .append("<td>").append("</td>")
                .append("<td>").append("</td>")
                .append("</tr>")
                .append("<tr>")
                .append("<td>").append("</td>")
                .append("<td>").append("</td>")
                .append("<td height=40 width=32>").append(getImage(item, 32, 32)).append("</td>")
                .append("<td>").append("</td>")
                .append("<td>").append("</td>")
                .append("</tr>");

        player.sendPacket(html.setHtml(sb.toString()));

    }

    public void showInitHtml(L2Player player){
        NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        int [] slots = {6, 10, 11, 9, 12};
        StringBuilder sb = new StringBuilder();
        for (int slot: slots){
            final L2ItemInstance paperdollItem = player.getInventory().getPaperdollItem(slot);
            if (paperdollItem != null){
                sb.append(htmlG1(paperdollItem, true));
            }
        }
        player.sendPacket(html.setHtml(sb.toString()));
    }


    public void onBypassFeedback(L2Player player, String command) {
        if (!canBypassCheck(player, this))
            return;
        if (command.startsWith("UpGrade")) {
            showInitHtml(player);
        } else if (command.startsWith("increaseSkillLevel")) {
            StringTokenizer st = new StringTokenizer(command, ":");
            st.nextToken();
            int itemObjectId = Integer.parseInt(st.nextToken());
            showUpGradeHtml(player, itemObjectId, skills[Rnd.get(skills.length)]);
        } else if (command.startsWith("nextSkill")) {
            StringTokenizer st = new StringTokenizer(command, ":");
            st.nextToken();
            int skillId = Integer.parseInt(st.nextToken());
            int slot = Integer.parseInt(st.nextToken());
            showUpGradeHtml(player, slot, skillId);
        } else if (command.startsWith("addSkill")) {
            StringTokenizer st = new StringTokenizer(command, ":");
            st.nextToken();
            int skillId = Integer.parseInt(st.nextToken());
            int itemObjectId = Integer.parseInt(st.nextToken());
            addSkill(player, skillId, itemObjectId);
            showUpGradeHtml(player, itemObjectId, skillId);
        } else if (command.startsWith("upSkill")) {
            StringTokenizer st = new StringTokenizer(command, ":");
            st.nextToken();
            int itemObjectId = Integer.parseInt(st.nextToken());
            int skillId = Integer.parseInt(st.nextToken());
            increaseSkillLevel(player, itemObjectId);
            showUpGradeHtml(player, itemObjectId, skillId);

        } else if (command.startsWith("deleteSkill")) {
            StringTokenizer st = new StringTokenizer(command, ":");
            st.nextToken();
            int slot = Integer.parseInt(st.nextToken());
            showDownGradeHtml(player, slot);
        } else if (command.startsWith("htmlTest")) {
            NpcHtmlMessage html = new NpcHtmlMessage(player, this);

            player.sendPacket(html.setHtml(""));

        } else
            super.onBypassFeedback(player, command);
    }
}