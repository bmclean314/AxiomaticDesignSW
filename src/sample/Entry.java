package sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * <h1>Entry Class</h1>
 * An Entry object holds data for each row of the TreeView.
 * Entry items are stored in the Entry table of the database.
 *
 * @author Bridget McLean - bmclean@wpi.edu
 */
public class Entry {
    public String FR;
    public int DPID;
    private DP[] dpList;
    public String displayNum;
    public int numChildren;
    public int parentID;
    private CN[] CNList;
    public int selected; //Denotes that this entry was selected for tracing from the CN view
    /**
     * Constructor for an Entry object
     * @param FR String - User entered FR, gets displayed in TreeView row
     * @param dpList Array of DP objects - The list of user entered alternative DPs associated with the FR,
     *               all alternative DPs get displayed in a ComboBox in a row of the TreeView
     * @param displayNum String - the identifying "number" displayed on the screen, displayNum is unique to each
     *                   Entry object
     * @param DPID int - a unique ID number assigned to every Entry object. While the displayNum is mostly used
     *             for display purposes in the UI, the DPID is used to identify Entry objects in the code.
     * @param numChildren int - the number of children this Entry object has in the TreeView
     * @param parentID int - the DPID of the parent of this Entry object
     */
    public Entry(String FR, DP[] dpList, String displayNum, int DPID, int numChildren, int parentID) {
//        this.FR = new SimpleStringProperty(FR);
//        this.DP = new SimpleStringProperty(DP);
        this.FR = FR;
        this.dpList = dpList;
        this.displayNum = displayNum;
        this.DPID = DPID;
        this.numChildren = numChildren;
        this.parentID = parentID;
        this.CNList = new CN[0];
        this.selected = 0;
    }

    /**
     * Getter for the FR attribute
     * @return String - user entered FR
     */
    public String getFR() {
        return FR;
    }

    /**
     * Getter for the dpList attribute
     * @return Array of DP objects - The list of user entered alternative DPs associated with the FR
     */
    public DP[] getDP() {
        return dpList;
    }

    //adds a new DP to the Entry object's DP list, also returns the list

    /**
     * Adds a new DP object to the Entry's DP list and add it to the database
     * @param newDP String - user entered alternative DP
     * @return new dpList with the added object
     */
    public DP[] addDP(String newDP){
        //extend the list and copy everything over
        int len = this.dpList.length;
        DP[] newDPList = new DP[len + 1];
        System.arraycopy(this.dpList, 0, newDPList, 0, this.dpList.length);

        //this.getPrimaryDP().setIsPrimary(false); //change the primaryDP

        newDPList[len] = new DP(newDP, this.dpList[0].getDPId(), len, false); //add the new DP,


        //ADD TO DATABASE DP TABLE HERE
        String url_ = "jdbc:mariadb://localhost:3306/mysql";
        String usr = "root";
        String pwd = "root";
        try {
            Connection myconn = DriverManager.getConnection(url_, usr, pwd);
            Statement stmt = myconn.createStatement();

            String sql = "INSERT INTO DP VALUES(" + this.dpList[0].getDPId() + ", '" + newDP + "', " + len + ", 0);";
            //String sql2 = "UPDATE DP SET isPrimary = 0 WHERE isPrimary = 1 AND DPID = " + this.dpList[0].getDPId() + ";";
            //stmt.executeUpdate(sql2); //sql2 goes first because it changes all the isPrimary to 0
            stmt.executeUpdate(sql); //then add the new DP where isPrimary is 1
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.dpList = newDPList; //set it to the entry object
        return newDPList;
    }

    /**
     * Removes the DP with the given count number from the Entry's DP list. Also adjusts all the remaining DP's counts
     * as necessary.
     * @param count the count number of the DP that should be deleted, also equates to that DP's index in the list.
     */
    public void removeDP(int count){
        DP[] newArr = new DP[this.getDP().length-1];
        System.arraycopy(this.getDP(), 0, newArr, 0, count);
        System.arraycopy(this.getDP(), count+1, newArr, count, newArr.length-count);
        this.setDpList(newArr);

        for(DP dp : newArr){
            if(dp.getCount() > count){
                dp.setCount(dp.getCount()-1);
            }
        }
    }

    /**
     * Getter for the displayNum attribute
     * @return String - the identifying "number" displayed on the screen
     */
    public String getDisplayNum(){
        return this.displayNum;
    }

    /**
     * Setter for the displayNum attribute
     * @param displayNum String - the new identifying "number" displayed on the screen
     */
    public void setDisplayNum(String displayNum){
        this.displayNum = displayNum;
    }

    /**
     * Setter for the FR attribute
     * @param FR String - the new user entered FR
     */
    public void setFR(String FR) {
        //this.FR.set(FR);
        this.FR = FR;
    }

    public void setDP(DP[] dpList) {
        //this.DP.set(DP);
        this.dpList = dpList;
    }

    public int getNumChildren() {
        return numChildren;
    }

    /**
     * Setter for the numChildren attribute
     * @param numChildren the new number of children this Entry has
     */
    public void setNumChildren(int numChildren) {
        this.numChildren = numChildren;
    }

    /**
     * Getter for the DPID attribute
     * @return int - the unique ID number assigned to every Entry object
     */
    public int getDPID() {
        return DPID;
    }

    public void setDPID(int DPID) {
        this.DPID = DPID;
    }


    public DP[] getDpList() {
        return dpList;
    }

    /**
     * Setter for the DpList attribute
     * @param dpList the new list of DPs to be associated with this Entry
     */
    public void setDpList(sample.DP[] dpList) {
        this.dpList = dpList;
    }

    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

    /**
     * Getter for the CNList attribute
     * @return the list of CN objects linked with this FR
     */
    public CN[] getCNList() {
        return CNList;
    }

    public void setCNList(CN[] CNList) {
        this.CNList = CNList;
    }

    /**
     * Finds the primary or "chosen" alternative DP associated with this FR
     * @return the primary DP associated with this FR
     */
    public DP getPrimaryDP(){
        DP temp = new DP("BAD", -1, -1, false);
        for(DP dp : this.dpList){
            if(dp.getIsPrimary()){
                temp = dp;
            }
        }
        if(temp.getDPId() == -1){
            temp = this.dpList[0];
        }

        return temp;
    }

    /**
     * Adds a CN to this Entry's CN list, signifying a link between the CN and the FR.
     *
     * @param cn the CN object to add to the list
     * @return the new CNList with the added CN object
     */
    public CN[] addCN(CN cn){
        CN[] temp = new CN[this.CNList.length+1];
        System.arraycopy(this.CNList, 0, temp, 0, this.CNList.length);
        temp[this.CNList.length] = cn;
        this.CNList = temp;
        return temp;
    }

}
