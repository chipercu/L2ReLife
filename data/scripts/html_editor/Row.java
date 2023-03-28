package html_editor;

import java.util.ArrayList;
import java.util.List;

public class Row {
    public static final String START_ROW = "    <tr>\n";
    public static final String END_ROW = "    </tr>\n";

    private int col_count;
    private List<Col> cols = new ArrayList<>();

    public Row(int col) {
        this.col_count = col;
        initCol();
    }

    private void initCol(){
        for (int i = 0; i < getCol_count(); i++) {
            cols.add(new Col());
        }
    }
    public String printCols(){
        StringBuilder stringBuilder = new StringBuilder();
        for (Col c: cols){
            stringBuilder.append(c.text());
        }
        return stringBuilder.toString();
    }

    public int getCol_count() {
        return col_count;
    }

    public void setCol_count(int col_count) {
        this.col_count = col_count;
    }

    public int getColCount(){
        return cols.size();
    }

    public List<Col> getCols() {
        return cols;
    }

    public void setCols(List<Col> cols) {
        this.cols = cols;
    }


    public Col getColByID(int id){
        return getCols().get(id);
    }

    public void addCol(){
        cols.add(new Col());
    }
}
