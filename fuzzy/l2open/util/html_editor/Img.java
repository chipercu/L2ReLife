package l2open.util.html_editor;

public class Img {

    private String src = "icon.NOICON";
    private int width = 32;
    private int height = 32;

    private String imgHTML = "";

    public Img(){
        initImg();
    }

    public Img(String src){
        this.src = src;
        initImg();
    }

    public Img(String src, int width, int height){
        this.src = src;
        this.width = width;
        this.height = height;
        initImg();
    }

    private void initImg() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<img ")
                .append("src=\"")
                .append(getSrc())
                .append("\" ")
                .append("width=\"")
                .append(getWidth())
                .append("\" ")
                .append("height=\"")
                .append(getHeight())
                .append("\">");

        setImgHTML(stringBuilder.toString());
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
        initImg();
    }

    public String getImgHTML() {
        return imgHTML;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        initImg();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        initImg();
    }

    public void setImgHTML(String imgHTML) {
        this.imgHTML = imgHTML;
    }


    @Override
    public String toString() {
        return imgHTML;
    }
}
