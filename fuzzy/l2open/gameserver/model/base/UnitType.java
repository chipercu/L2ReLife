package l2open.gameserver.model.base;

public enum UnitType{
    KNIGHT("knight"),
    ARCHER("archer"),
    WIZARD("wizard"),
    HEALER("healer"),
    SUPORT("suport"),
    WARRIOR("warrior"),
    DAGGER("dagger"),
    NONE("none");
    private final String type;
    UnitType(String type) {
        this.type = type;
    }
    public String getType(){
      return this.type;
    }

}
