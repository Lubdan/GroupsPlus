package lubdan.groupsplus.util;

public class PData {
    private String UUID;
    private String GroupLeaderUUID;
    private boolean recieveinvites;
    private boolean leader;

    public PData(String UUID, String GUUID, boolean recieveinvites, boolean leader){
        this.UUID = UUID;
        this.GroupLeaderUUID = GUUID;
        this.recieveinvites = recieveinvites;
        this.leader = leader;
    }


    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getGroupLeaderUUID() {
        return GroupLeaderUUID;
    }

    public void setGroupLeaderUUID(String groupLeaderUUID) {
        GroupLeaderUUID = groupLeaderUUID;
    }



    public boolean isRecieveinvites() {
        return recieveinvites;
    }

    public void setRecieveinvites(boolean recieveinvites) {
        this.recieveinvites = recieveinvites;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }


}
