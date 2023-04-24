package l2open.gameserver.model.base;

public enum UnitType{
    knight("knight"),
    ranger("ranger"),
    warrior("warrior"),
    wizard("wizard"),
    healer("healer"),
    buffer("buffer"),
    none("none");
    private final String type;
    UnitType(String type) {
        this.type = type;
    }
    public String getType(){
      return this.type;
    }

}
