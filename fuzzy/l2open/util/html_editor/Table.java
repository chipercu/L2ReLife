package l2open.util.html_editor;

import java.util.ArrayList;
import java.util.List;

public class Table implements HTML_Generator{

    private int row;
    private int col;

    private static final String START_COL = "\n<table ";
    private static final String END_TABLE = "</table>\n";
    private static final String CLOSE_PARAM = ">\n";

    private List<String> params = new ArrayList<>();

    private List<Row> rows = new ArrayList<>();

    public Table(int row, int col) {
        this.row = row;
        this.col = col;
        initTable();
    }
    public Table(){

    }


    public void initTable(){
        for (int i = 0; i < getRow(); i++) {
            rows.add(new Row(getCol()));
        }
    }
    public Table addRow(){
        rows.add(new Row());
        return this;
    }


    public String getHTML(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(START_COL);
        for (String s: params){
            stringBuilder.append(s);
        }
        stringBuilder.append(CLOSE_PARAM);

        for (Row r: rows){
            stringBuilder.append(Row.START_ROW)
                    .append(r.printCols())
                    .append(Row.END_ROW);
        }
        stringBuilder.append(END_TABLE);
        return stringBuilder.toString();
    }

    public Table addParam(String param){
        getParams().add(param);
        return this;
    }

    public Row row(int index){
        return getRowById(index);
    }


    public List<String> getParams() {
        return params;
    }

    public Row getRowById(int id){
        return rows.get(id);
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public List<Row> getRows() {
        return  rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    @Override
    public String toString() {
        return getHTML();
    }
}
