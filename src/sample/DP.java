package sample;



/**
 * <h1>DP Class</h1>
 * A DP object holds data for each DP. Since one FR can have many alternative DPs, this DP class was made
 * to hold the actual DP text as well as attributes to help associate it with its FR.
 * DP items are stored in the DP table of the database.
 *
 * @author Bridget McLean - bmclean@wpi.edu
 */
public class DP {

    private String dp;
    private int DPId; //matches the DPID field in the Entry object
    private int count; //counts how many DPs are associated with the same FR
    private boolean isPrimary; //0 if it's the primary DP for that FR

    /**
     * Constructor for a DP object
     * @param dp String - the user entered DP
     * @param DPId int - the DPID (unique identifier) of the Entry that the DP is associated with. Note: Each Entry object has
     *             a unique DPID, but since many DPs are associated with one FR, many DPs can share a DPID.
     * @param count int - a unique identifier amongst DPs that have the same DPID. Corresponds to the DP object's index
     *              in the Entry object's dpList. All DPs can be uniquely identified by a combination of
     *              their DPID and their count
     * @param isPrimary int - Denotes which alternative DP has been selected as the one the user would like to use.
     *                  Can only be 0 or 1, 0 meaning it is not "primary" and 1 meaning that it is. No more than
     *                  one alternative DP per every FR can be primary - AKA only 1 DP in the dpList can be primary -
     *                  - AKA no two (or more) DPs with the same DPID can both be primary
     */
    public DP(String dp, int DPId, int count, boolean isPrimary) {
        this.dp = dp;
        this.DPId = DPId;
        this.count = count;
        this.isPrimary = isPrimary;
    }

    /**
     * Getter for the DP attribute
     * @return String - the user entered DP
     */
    public String getDp() {
        return this.dp;
    }

    /**
     * Setter for the DP attribute
     * @param dp String - the new user entered DP
     */
    public void setDp(String dp) {
        this.dp = dp;
    }

    /**
     * Getter for the DPID attribute
     * @return the DPID of the Entry object that the DP is associated with
     */
    public int getDPId() {
        return this.DPId;
    }

    /**
     * Getter for the count attribute
     * @return int - the unique identifier for this object amongst DPs that share its DPID
     */
    public int getCount() {
        return count;
    }

    public void setDPId(int DPId) {
        this.DPId = DPId;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Getter for the isPrimary attribute
     * @return int - 0 if it's not primary, 1 if it is
     */
    public boolean getIsPrimary() {
        return isPrimary;
    }

    /**
     * Setter for the isPrimary attribute
     * @param isPrimary 0 if it's no longer primary, 1 to make it primary
     */
    public void setIsPrimary(boolean isPrimary) {

        this.isPrimary = isPrimary;


    }
}
