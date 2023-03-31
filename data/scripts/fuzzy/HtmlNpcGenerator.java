package fuzzy;

import l2open.gameserver.model.L2Player;
import l2open.gameserver.model.instances.L2NpcInstance;
import l2open.gameserver.serverpackets.NpcHtmlMessage;

import java.util.Arrays;

/**
 * @author FuzzY
 */

public class HtmlNpcGenerator {
    private L2Player player;
    private NpcHtmlMessage html;
    private StringBuilder builder;


    public HtmlNpcGenerator(L2Player player, L2NpcInstance npc){
        this.player = player;
        this.html = new NpcHtmlMessage(player, npc);
        this.builder = new StringBuilder();
    }


    private String width(int size){
        return "width=" + size + " ";
    }
    private String height(int size){
        return "height=" + size + " ";
    }
    private String align(String pos){
        return "align=" + pos + " ";
    }
    private String vlign(String pos){
        return "valign=" + pos + " ";
    }





    private String addTable(int rows, int cols, String ...params){
        StringBuilder sb = new StringBuilder();
        sb.append("<table ").append(Arrays.toString(params)).append(">")


                .append("</table>");
        return sb.toString();
    }





}
