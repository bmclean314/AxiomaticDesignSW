package sample;

public class CN {

    private String cn;
    private String displayID;
    private String parentID;
    private Entry[] FRList;

    public CN(String cn, String displayID, String parentID) {
        this.cn = cn;
        this.displayID = displayID;
        this.parentID = parentID;
        this.FRList = new Entry[0];
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getDisplayID() {
        return displayID;
    }

    public void setDisplayID(String displayID) {
        this.displayID = displayID;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public Entry[] getFRList() {
        return FRList;
    }

    public void setFRList(Entry[] FRList) {
        this.FRList = FRList;
    }

    public Entry[] addFR(Entry entry){
        Entry[] temp = new Entry[this.FRList.length+1];
        System.arraycopy(this.FRList, 0, temp, 0, this.FRList.length);
        temp[this.FRList.length] = entry;
        this.FRList = temp;
        return temp;
    }
}
