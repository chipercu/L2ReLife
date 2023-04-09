package npc.model;

import fuzzy.Html_Constructor.tags.Img;
import fuzzy.Html_Constructor.tags.Table;
import fuzzy.Html_Constructor.tags.parameters.Parameters;
import l2open.config.ConfigValue;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.L2Skill;
import l2open.gameserver.model.base.L2Augmentation;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.model.items.L2ItemInstance;
import l2open.gameserver.serverpackets.ExVariationCancelResult;
import l2open.gameserver.serverpackets.InventoryUpdate;
import l2open.gameserver.serverpackets.NpcHtmlMessage;
import l2open.gameserver.serverpackets.SystemMessage;
import l2open.gameserver.tables.SkillTable;
import l2open.gameserver.tables.player.PlayerData;
import l2open.gameserver.templates.L2NpcTemplate;
import l2open.gameserver.templates.OptionDataTemplate;
import l2open.gameserver.xml.loader.XmlOptionDataLoader;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import static fuzzy.Html_Constructor.tags.parameters.Position.LEFT;
import static fuzzy.Html_Constructor.tags.parameters.Position.TOP;


public class UpgradeArmor2Instance extends L2NpcInstance implements Parameters {

    private static final int[] fire = {};
    private static final int[] water = {};
    private static final int[] earth = {};
    private static final int[] wind = {};
    private static final int[] holy = {};
    private static final int[] dark = {};

    private static final int[] skills= {26086, 26087, 26088, 26089, 26090, 26091, 26092, 26093};



    private static final int ADENA = 57;
    private static final String noIcon = "icon.NOIMAGE";


    private static Logger _log = Logger.getLogger(UpgradeArmor2Instance.class.getName());

    public UpgradeArmor2Instance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
    }

    private String td(String content, String ...params){
        return "<td " + Arrays.toString(params) + ">" + content + "</td>";
    }
    private String getImage(L2ItemInstance item, int w, int h){
        return "<img src=\"" + item.getItem().getIcon() + "\" " + width(w) + height(h) + ">";
    }
    private String getImage(L2Skill skill, int w, int h){
        return "<img src=\"" + skill.getIcon() + "\" " + width(w) + height(h) + ">";
    }
    private String getImage(String path, int w, int h){
        return "<img src=\"" + path + "\" " + width(w) + height(h) + ">";
    }

    private String button_minus(int slot){
        return "<button action=\"bypass -h npc_%objectId%_deleteSkill:"+ slot +"\" width=31 height=31 back=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red_Down\" fore=\"L2UI_CT1.MiniMap_DF_MinusBtn_Red\">";
    }
    private String button_plus(int slot){
        return "<button action=\"bypass -h npc_%objectId%_increaseSkillLevel:"+ slot +"\" width=31 height=31 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red_Down\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\">";
    }
    private String addSkillButton(int slot, int skillID){
        return "<button action=\"bypass -h npc_%objectId%_addSkillLevel:"+ slot + ":" + skillID +"\" width=31 height=31 back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red_Down\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\">";
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
        player.sendPacket(new ExVariationCancelResult(1), iu, sm);

        player.broadcastUserInfo(true);
    }
    public void addSkillLevel(int slot,int skillId, L2Player player){
        final L2ItemInstance item = player.getInventory().getPaperdollItem(slot);
        L2Skill skill = null;
        if (item != null){
            switch (skillId){
                case 26086: {
                    int stat34 = 25001;
                    OptionDataTemplate template = XmlOptionDataLoader.getInstance().getTemplate(stat34);
                    if (template != null) {
                        if (template.getSkills().size() > 0)
                            skill = template.getSkills().get(0);
                        if (skill == null && template.getTriggerList().size() > 0)
                            skill = template.getTriggerList().get(0).getSkill();
                    }
                    int stat16 = ConfigValue.JewelAugmentOptionStat;
                    item.setAugmentation(new L2Augmentation(((stat34 << 16) + stat16), skill));
                    break;
                }
                case 26087: {

                    break;
                }
            }
            if (item.isEquipped()){
                item.getAugmentation().applyBoni(player, true);
            }
            player.updateStats();
            player.sendPacket(new InventoryUpdate().addModifiedItem(item));
            player.sendUserInfo(false);
        }
    }

    public void increaseSkillLevel(int slot, L2Player player){
        final L2ItemInstance item = player.getInventory().getPaperdollItem(slot);
        final int augmentationId = item.getAugmentation().getAugmentationId();
        if (item.getAugmentation().getSkill().getLevel() >= 15){
            return;
        }


        removeAugment(item, player);

        int stat34 = (augmentationId >> 16) + 1;
        L2Skill skill = null;

        OptionDataTemplate template = XmlOptionDataLoader.getInstance().getTemplate(stat34);
        if (template != null) {
            if (template.getSkills().size() > 0)
                skill = template.getSkills().get(0);
            if (skill == null && template.getTriggerList().size() > 0)
                skill = template.getTriggerList().get(0).getSkill();
        }

        int stat16 = ConfigValue.JewelAugmentOptionStat;
        item.setAugmentation(new L2Augmentation(((stat34 << 16) + stat16), skill));
        if (item.isEquipped())
            item.getAugmentation().applyBoni(player, true);

        player.updateStats();
        player.sendPacket(new InventoryUpdate().addModifiedItem(item));
        player.sendUserInfo(false);


    }


    public String progress(int lvl){

        final Table table = new Table(1, 15);
        table.setParams(border(0), cellpadding(0), cellspacing(3), align(LEFT), valign(TOP));
        String red = "L2UI_CT1.DeBuffFrame_24";
        String black = "L2UI_CT1.BuffFrame_24_1";
        final String enchanted = new Img(red, 8, 16).build();
        final String no_enchant = new Img(black, 8, 16).build();

        for (int i = 0; i < 15; i++) {
            if (lvl < i){
                table.row(0).col(i).setParams(width(8), height(16), valign(TOP)).insert(no_enchant);
            }else {
                table.row(0).col(i).setParams(width(8), height(16), valign(TOP)).insert(enchanted);
            }
        }

//        StringBuilder sb = new StringBuilder();
//
//        String skill_progress_true = td(getImage("L2UI_CT1.DeBuffFrame_24", 8, 16), width(8), height(16), vlign("top"));
//        String skill_progress_false = td(getImage("L2UI_CT1.BuffFrame_24_1", 8, 16), width(8), height(16), vlign("top"));
//
//        for (int i = 0; i < 15; i++) {
//            if (lvl > i){
//                sb.append(skill_progress_true).append(td("", width(1), height(32), vlign("top")));
//            }else {
//                sb.append(skill_progress_false).append(td("", width(1), height(32), vlign("top")));
//            }
//        }
//        return sb.toString();
        return table.build();
    }

    private String itemCount(L2Player player, L2ItemInstance item, int need){
        final long count = player.getInventory().getItemByItemId(item.getItemId()).getCount();
        return  need + " / " + count;
    }

    private String htmlG1(L2ItemInstance item, int slot){
        StringBuilder result = new StringBuilder();

        L2Skill skill = null;
        String skill_name = "Нет Усиления";
        String skillIcon;
        if (item.isAugmented()){
            skill = item.getAugmentation().getSkill();
            skill_name = skill.getName().substring(0, skill.getName().length() - 9);
            skillIcon = skill.getIcon();
        }else {
            skillIcon = noIcon;
        }

        Table skillTable = new Table(1, 2);
        skillTable.setParams(border(1), cellpadding(0), cellspacing(0), width(160));
        Img skillImg = new Img(skillIcon, 32, 32);
        skillTable.row(0).col(0).insert(skillImg.build());
        skillTable.row(0).col(1).insert(skill_name);

        Table progressTable = new Table(2, 1);
        progressTable.setParams(border(1), cellpadding(0), cellspacing(0), width(160));
        progressTable.row(0).col(0).setParams(height(32)).insert(skillTable.build());
        progressTable.row(1).col(0).insert(progress(skill != null ? skill.getLevel() : 0));


        Table table = new Table(1, 4);
        String tableBackground = "l2ui_ct1.Windows_DF_TooltipBG";
        table.setParams(border(1), cellspacing(0), cellpadding(2) ,width(350), background(tableBackground));
        Img itemImg = new Img(item.getItem().getIcon(), 32, 32);
        table.row(0).col(0).setParams(height(50), width(40)).insert(itemImg.build());
        table.row(0).col(1).insert(progressTable.build());

        table.row(0).col(2).insert(button_plus(slot));
        table.row(0).col(3).insert(button_minus(slot));


//        result.append("<table border=0 cellspacing=0 cellpadding=2 width=320 background=\"l2ui_ct1.Windows_DF_TooltipBG\">")
//                .append("<tr>")
//                .append("<td height=40 width=32>").append(getImage(item, 32, 32)).append("</td>")
//                .append("<td>")
//                .append("<table border=0 cellspacing=0 cellpadding=0 width=160>")
//                .append("<tr>")
//                .append("<td>").append(getImage(skillIcon, 32, 32)).append("</td>")
//                .append("<td>").append(skill_name).append("</td>")
//                .append("</tr>")
//                .append("</table>")
//                .append("</td>")
//                .append("<td >").append(button_plus(slot)).append("</td>")
//                .append("<td >").append(button_minus(slot)).append("</td>")
//                .append("</tr>")
//                .append("<tr>")
//                .append("<td >").append("</td>")
//                .append("<td >")
//                .append(progress(skill != null ? skill.getLevel() : 0))
//                .append("</td>")
//                .append("<td>").append("</td>")
//                .append("<td>").append("</td>")
//                .append("</tr>")
//                .append("<tr></tr>")
//                .append("</table>");
        return table.build();

    }

    public void showUpGradeHtml(L2Player player, int slot){
        NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        final L2ItemInstance item = player.getInventory().getPaperdollItem(slot);
        if (item == null){
            return;
        }

        StringBuilder sb = new StringBuilder();


        L2Skill skill = null;
        String skill_name = "Нет Усиления";
        String skillIcon = null;
        if (item.isAugmented()){
            skill = item.getAugmentation().getSkill();
            skill_name = skill.getName().substring(0, skill.getName().length() - 9);
            skillIcon = skill.getIcon();
        }else {
            skillIcon = noIcon;
        }
        sb.append("<table border=0 cellspacing=0 cellpadding=2 width=348 background=\"l2ui_ct1.Windows_DF_TooltipBG\">")
                .append("<tr>")
                .append("<td height=40 width=32>").append(getImage(item, 32, 32)).append("</td>")
                .append("<td>")
                .append("<table border=0 cellspacing=0 cellpadding=0 width=160>")
                .append("<tr>")
                .append("<td>").append(getImage(skillIcon, 32, 32)).append("</td>")
                .append("<td>").append(skill_name).append("</td>")
                .append("</tr>")
                .append("</table>")
                .append("</td>")
                .append("<td width=36>").append("</td>")
                .append("<td width=36>").append("</td>")
                .append("</tr>")
                .append("<tr>")
                .append("<td >").append("</td>")
                .append("<td >")
                .append(progress(skill != null ? skill.getLevel() : 0))
//                .append("<table border=0 cellspacing=1 cellpadding=0 align=left valign=top>")
//                .append("<tr>").append(progress(skill != null ? skill.getLevel() : 0)).append("</tr>")
//                .append("</table>")
                .append("</td>")
                .append("<td>").append("</td>")
                .append("<td>").append("</td>")
                .append("</tr>")
                .append("<tr></tr>")
                .append("</table>");



        if (!item.isAugmented()){

            sb.append("<table border=0 cellspacing=0 cellpadding=2 width=320 background=\"l2ui_ct1.Windows_DF_TooltipBG\">");
            for (int i = 0; i < skills.length; i++) {
                final L2Skill skill1 = SkillTable.getInstance().getInfo(skills[i], 1);
                skill_name = skill1.getName().substring(0, skill1.getName().length() - 9);
                sb.append("<tr>")
                        .append("<td height=40>")
                        .append("<table border=0 cellspacing=0 cellpadding=0 width=130>")
                        .append("<tr>")
                        .append("<td height=40 width=40>").append(getImage(skill1, 32, 32)).append("</td>")
                        .append("<td width=90 align=left>").append(skill_name).append("</td>")
                        .append("</tr>")
                        .append("</table>")
                        .append("</td>")
                        .append("<td>")
                        .append("<table border=0 cellspacing=0 cellpadding=0 width=64>")
                        .append("<tr>")
                        .append("<td height=40 width=32>").append(getImage(noIcon, 32, 32)).append("</td>")
                        .append("<td align=left>").append(" 1 шт.<br1> 0 шт.").append("</td>")
                        .append("</tr>")
                        .append("</table>")
                        .append("</td>")
                        .append("<td>")
                        .append("<table border=0 cellspacing=0 cellpadding=0 width=64>")
                        .append("<tr>")
                        .append("<td height=40 width=32>").append(getImage(noIcon, 32, 32)).append("</td>")
                        .append("<td align=left>").append(" 2 шт.<br1> 0 шт.").append("</td>")
                        .append("</tr>")
                        .append("</table>")
                        .append("</td>")
                        .append("<td width=32>").append(addSkillButton(slot, skill1.getId())).append("</td>")
                        .append("</tr>");
            }
        }

        player.sendPacket(html.setHtml(sb.toString()));


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
        StringBuilder rows = new StringBuilder();
        for (int slot: slots){
            final L2ItemInstance paperdollItem = player.getInventory().getPaperdollItem(slot);
            if (paperdollItem != null){
                rows.append(htmlG1(paperdollItem, slot));
            }
        }
        player.sendPacket(html.setHtml(rows.toString()));
    }

    public void onBypassFeedback(L2Player player, String command) {
        if (!canBypassCheck(player, this))
            return;
// Показать окно с сэтом - его текущий уровень апгрейда
        if (command.startsWith("UpGrade")) {
            showInitHtml(player);
            // Покозать окно с броней - ее текущий уровень апгрейда
        } else if (command.startsWith("increaseSkillLevel")) {
            StringTokenizer st = new StringTokenizer(command, ":");
            st.nextToken();
            int slot = Integer.parseInt(st.nextToken());
            showUpGradeHtml(player, slot);

        } else if (command.startsWith("addSkillLevel")) {
            StringTokenizer st = new StringTokenizer(command, ":");
            st.nextToken();
            int slot = Integer.parseInt(st.nextToken());
            int skillId = Integer.parseInt(st.nextToken());
            addSkillLevel(slot, skillId, player);
            showInitHtml(player);

        } else if (command.startsWith("deleteSkill")) {
            StringTokenizer st = new StringTokenizer(command, ":");
            st.nextToken();
            int slot = Integer.parseInt(st.nextToken());
            showDownGradeHtml(player, slot);
        } else if (command.startsWith("htmlTest")) {
            NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            final L2ItemInstance paperdollItem = player.getInventory().getPaperdollItem(10);

//            player.sendPacket(html.setHtml(htmlG1(paperdollItem, 10)));

            System.out.println(htmlG1(paperdollItem, 10));


        } else
            super.onBypassFeedback(player, command);
    }
}