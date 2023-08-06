package l2open.gameserver.model;

import l2open.gameserver.model.base.UnitType;
import l2open.util.Location;

public class UnitLoc {

    private UnitType unitType;
    private Boolean isOccupied;
    private Integer priority;
    private Location loc;
    private L2Player unit;

    public UnitLoc(UnitType unitType, Integer priority, Location loc) {
        this.unitType = unitType;
        this.priority = priority;
        this.loc = loc;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public Boolean isOccupied() {
        return isOccupied;
    }

    public UnitLoc setOccupied() {
        isOccupied = true;
        return this;
    }
    public UnitLoc setFree() {
        isOccupied = false;
        return this;
    }

    public L2Player getUnit() {
        return unit;
    }

    public void setUnit(L2Player unit) {
        this.unit = unit;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }
}
