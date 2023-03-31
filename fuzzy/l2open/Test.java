package l2open;

import l2open.util.html_editor.HTML_Generator;
import l2open.util.html_editor.Table;

public class Test implements HTML_Generator {
    public static void main(String[] in) {
        System.out.println(new Test().progress(10));
    }


    public String progress(int lvl) {

        final Table table = table();
        table.addParam("border=0 cellspacing=1 cellpadding=0 align=left valign=top");
        table.addRow();

        for (int i = 0; i < 15; i++){
            if (lvl < i){
                table.row(0).addCol().col(i).addParam(width(8), height(16)).setContent(getImage("L2UI_CT1.DeBuffFrame_24", 8, 16));
            }else {
                table.row(0).addCol().col(i).addParam(width(8), height(16)).setContent(getImage("L2UI_CT1.BuffFrame_24_1", 8, 16));
            }
            table.row(0).addCol().col(i + 1).setContent("wert");
//            table.row(0).addCol().col(i + 1).addParam(width(1));
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
        return table.getHTML();
    }

    private String getImage(String path, int w, int h) {
        return "<img src=\"" + path + "\"" + width(w) + height(h) + ">";
    }


}