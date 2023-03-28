package html_editor;

import java.util.ArrayList;
import java.util.List;

public class Col {


    private static final String START_COL = "       <td";
    private static final String END_COL = "</td>\n";
    private static final String CLOSE_PARAM = ">";
    private String content = "";
    private static final String COL_HEIGHT_DEFAULT = " height=\"32\"";
    private static final String COL_WIDTH_DEFAULT = " width=\"32\"";



    private List<String> params = new ArrayList<>();


    public Col(){
        params.add(COL_WIDTH_DEFAULT);
        params.add(COL_HEIGHT_DEFAULT);
    }
    public String text(){
        StringBuilder param = new StringBuilder();
        if (!params.isEmpty()){
            for (String s: params){
                param.append(s);
            }
        }
        return START_COL + param.toString() + CLOSE_PARAM + getContent() + END_COL;

    }


    public String getContent() {
        return content;
    }

    public Col setContent(String content) {
        this.content = content;
        return this;
    }

    public Col addParam(String param){
        getParams().add(param);
        return this;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }
}
