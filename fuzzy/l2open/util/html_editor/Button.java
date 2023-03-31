package l2open.util.html_editor;

public class Button {

    private String value = "";
    private String action = "";
    private int width = 32;
    private int height = 32;

    private String defaultBack = "L2UI_ct1.button_df";
    private String defaultFore = "L2UI_ct1.button_df";
    private String buttonHTML = "";


    public Button() {
        initButton();
    }

    public Button(String value) {
        this.value = value;
        initButton();
    }

    public Button(String value, String action) {
        this.value = value;
        this.action = action;
        initButton();
    }

    public Button(String value, String action, int width, int height) {
        this.value = value;
        this.action = action;
        this.width = width;
        this.height = height;
        initButton();
    }

    public Button(String value, String action, int width, int height, String defaultBack, String defaultFore) {
        this.value = value;
        this.action = action;
        this.width = width;
        this.height = height;
        this.defaultBack = defaultBack;
        this.defaultFore = defaultFore;
        initButton();
    }

    private void initButton() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<button ")
                .append("value=\"")
                .append(getValue())
                .append("\" ")
                .append("action=\"")
                .append(getAction())
                .append("\" ")
                .append("width=\"")
                .append(getWidth())
                .append("\" ")
                .append("height=\"")
                .append(getHeight())
                .append("\" ")
                .append("back=\"")
                .append(getDefaultBack())
                .append("\" ")
                .append("fore=\"")
                .append(getDefaultFore())
                .append("\">");
        setButtonHTML(stringBuilder.toString());
    }

    public String getButtonHTML() {
        return buttonHTML;
    }

    public void setButtonHTML(String buttonHTML) {
        this.buttonHTML = buttonHTML;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getDefaultBack() {
        return defaultBack;
    }

    public void setDefaultBack(String defaultBack) {
        this.defaultBack = defaultBack;
    }

    public String getDefaultFore() {
        return defaultFore;
    }

    public void setDefaultFore(String defaultFore) {
        this.defaultFore = defaultFore;
    }
}
