package l2open.util.html_editor;

public interface HTML_Generator {

    enum ALIGN{
        CENTER("center"),
        LEFT("left");


        final String value;
        ALIGN(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    default Table table(int row, int col){
        return new Table(row, col);
    }
    default Table table(){
        return new Table();
    }


    default String height(int size){
        return " height=" + size;
    }

    default String width(int size){
        return " width=" + size;
    }

    default String align(ALIGN pos){
        return " align=\"" + pos.getValue() + "\"";
    }
    default String vlign(String pos){
        return " valign=\"" + pos + "\"";
    }




}
