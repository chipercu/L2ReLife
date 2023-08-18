package services.PartyMaker;

import java.util.ArrayList;
import java.util.List;

public class PartyMakerGroup {

    private int minLevel;
    private int maxLevel;
    private int creatorId;
    private List<Integer> acceptedPlayers;
    private List<Integer> candidates;

    private String description;
    private String instance;

    public PartyMakerGroup(int minLevel, int maxLevel, int creatorId, String description, String instance) {
        this.acceptedPlayers = new ArrayList<>();
        this.candidates = new ArrayList<>();
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.creatorId = creatorId;
        this.description = description;
        this.instance = instance;
    }

    public List<Integer> getAcceptedPlayers() {
        return acceptedPlayers;
    }

    public void setAcceptedPlayers(List<Integer> acceptedPlayers) {
        this.acceptedPlayers = acceptedPlayers;
    }

    public List<Integer> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Integer> candidates) {
        this.candidates = candidates;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public String getDescription() {
        return description;
    }
}
