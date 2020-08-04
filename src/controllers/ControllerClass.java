package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.CN;
import sample.DP;
import sample.Entry;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * <h1>Controller Class</h1>
 * This class was made to be inherited by the HomeController and MatrixController classes.
 * Here, both controllers can access methods that they both need to use. Mostly, the functions in this class
 * update/pull from the database, as both controllers rely on accessing the database.
 *
 * @author Bridget McLean - bmclean@wpi.edu
 */
public class ControllerClass {

    public Entry[] FRDP = new Entry[0];

    public CN[] cnList = new CN[0];


    /**
     * Pulls everything from the Entry table in the database and puts each row into Entry objects.
     * Entry objects are stored in the FRDP array
     */
    public void refreshData(){

        this.FRDP = new Entry[0]; //clear the old array

        DP[][] dpDatabase = fromDatabase(); //2d array of DPs organized by DPID

        String url_ = "jdbc:mariadb://localhost:3306/mysql";
        String usr = "root";
        String pwd = "root";
        try {
            Connection myconn = DriverManager.getConnection(url_, usr, pwd);
            Statement stmt = myconn.createStatement();

            String sql = "SELECT * FROM Entry";
            ResultSet rs = stmt.executeQuery(sql);


            while (rs.next()){ //for each Entry object from the database
                int id = rs.getInt("DPID"); //get its DPID
                if(dpDatabase[id][0].getDPId() == id){ //DPID corresponds to row in 2d array
                    Entry[] newEnts = new Entry[this.FRDP.length+1]; //extend the list
                    System.arraycopy(this.FRDP, 0, newEnts, 0, this.FRDP.length); //copy it over
                    //put the Entry from the database into an object and add it to the end of the list
                    System.out.println("dpDatabase[id].length: " + dpDatabase[id].length);
                    newEnts[this.FRDP.length] = new Entry(rs.getString("FR"), dpDatabase[id], rs.getString("displayNum"), rs.getInt("DPID"), rs.getInt("numChildren"), rs.getInt("parentID"));
                    this.FRDP = newEnts;
                }
                else{
                    System.out.println("Initialize in MatrixController: DPID and dpDatabase index aren't the same but you thought they were.");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //I feel like this function is really confusing even with my line comments so I'm going to try and explain
    //my thought process behind this
    //The main purpose of this function is to pull all the DP data out of the database, turn them into objects, and sort them.
    //Each DP belongs to an FR, and there can be many DPs for each FR. This function creates a list of DPs for each FR,
    //and puts all those DP liststogether into a list (2d-array) where the index corresponds to the FR's DPID number.
    //This way, when I pull the FR data out of the database and turn it into objects, I will have a list
    //of it's DPs already made for each FR object.

    /**
     * Sorts all the DPs in the DP database table into a 2D array, where every column holds all the DPs
     * whose DPID is the same as the column index
     * @return the 2d array
     */
    public DP[][] fromDatabase() {

        DP[][] dpDatabase = new DP[0][0];

        String url_ = "jdbc:mariadb://localhost:3306/mysql";
        String usr = "root";
        String pwd = "root";
        try {
            Connection myconn = DriverManager.getConnection(url_, usr, pwd);
            Statement stmt = myconn.createStatement();

            String sql = "SELECT MAX(DPID) FROM DP"; //find out how many DP lists we need to make (for each FR)

            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int max = rs.getInt(1);
                //System.out.println("MAX: " + max);

                for (int i = 0; i < max + 1; i++) { //for each list we need to make
                    String query = "SELECT * FROM DP WHERE DPID = " + i + ";"; //select all DPs with the same ID
                    ResultSet rs2 = stmt.executeQuery(query);

                    //this list will hold all the DPs we just found that all have the same ID
                    //it starts at length 0 because we build it as we go along
                    DP[] dp = new DP[0];

                    //the while loop iterates through all the DPs with the same DPID that we just pulled from the database
                    //for each entry from the database
                    while (rs2.next()) {
                        int len = dp.length;
                        DP[] newDP = new DP[len + 1]; //extend the list so we have room to add the next DP
                        for (int j = 0; j < len; j++) { //for everything already in dp[] from previous loops
                            newDP[j] = dp[j]; //copy everything over
                        }
                        //make a dp object with the row data from the database and add it to the end of the new list
                        newDP[len] = new DP(rs2.getString("DP"), rs2.getInt("DPID"), rs2.getInt("count"), rs2.getInt("isPrimary"));

                        dp = newDP; //save the extended list to the dp list so we can access it next loop
                    }
                    //now we need to add our new DP list to the array of DP lists
                    int len2 = dpDatabase.length;
                    DP[][] newDPDatabase = new DP[len2 + 1][dp.length + 1]; //make the new, longer list
                    for (int k = 0; k < len2; k++) {
                        newDPDatabase[k] = dpDatabase[k]; //copy all the other lists over
                    }
                    newDPDatabase[len2] = dp; //add our new DP list to the end of the array of DP lists
                    dpDatabase = newDPDatabase; //save the extended list to the dp list so we can access it next loop

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dpDatabase;
    }

    /**
     * Connects to the database and executes an update using the given command String
     * @param sql String that contains an sql command to be executed in the database. Note: command
     *            must be of type "INSERT"/"UPDATE"/"DELETE" -- this function will not execute queries.
     */
    public void executeDatabaseU(String sql) {

        String url = "jdbc:mariadb://localhost:3306/mysql";
        String usr = "root";
        String pwd = "root";
        try {
            Connection myconn = DriverManager.getConnection(url, usr, pwd);
            Statement stmt = myconn.createStatement();

            System.out.println(sql);
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Pulls everything from the CN table in the database and puts each row into CN objects.
     * CN objects are stored in the cnList array
     */
    public void refreshCNData(){

        this.cnList = new CN[0]; //clear the old array

        //DP[][] dpDatabase = fromDatabase(); //2d array of DPs organized by DPID

        String url_ = "jdbc:mariadb://localhost:3306/mysql";
        String usr = "root";
        String pwd = "root";
        try {
            Connection myconn = DriverManager.getConnection(url_, usr, pwd);
            Statement stmt = myconn.createStatement();

            String sql = "SELECT * FROM CN";
            ResultSet rs = stmt.executeQuery(sql);


            while (rs.next()){ //for each Entry object from the database
                CN[] newCNs = new CN[this.cnList.length+1]; //extend the list
                System.arraycopy(this.cnList, 0, newCNs, 0, this.cnList.length); //copy it over
                //put the Entry from the database into an object and add it to the end of the list
                newCNs[this.cnList.length] = new CN(rs.getString("cn"), rs.getString("displayID"), rs.getString("parentID"));
                this.cnList = newCNs;


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        matchCNFR();
    }

    /**
     * Uses the CNFRLink table in the database to match CN objects to Entry objects.
     * Fills all the CN objects' FRLists and fills all the Entry objects' CNLists
     */
    private void matchCNFR(){
        String url_ = "jdbc:mariadb://localhost:3306/mysql";
        String usr = "root";
        String pwd = "root";
        try {
            Connection myconn = DriverManager.getConnection(url_, usr, pwd);
            Statement stmt = myconn.createStatement();

            //A CN can be fulfilled by many FRs, and an FR can fulfill many CNs. In the database, many-to-many
            //  relationship is represented with a table that contains the primary keys. The CNFRLink contains
            //  the DPID of the FR and the displayID of the CN so each object can be identified and linked together
            String sql = "SELECT * FROM CNFRLink"; //Find all the links we need to make
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()) {
                for (CN cn : this.cnList) { //find the CN in the CNList with the displayID from the CNFRLink table
                    for (Entry entry : this.FRDP) { //find the FR in the FRList with the DPID from the CNFRLink table
                        if((cn.getDisplayID().equals(rs.getString("CNdisplayID"))) && (entry.DPID == rs.getInt("DPID"))){
                            cn.addFR(entry); //add the entry to the CN's list of FRs
                            entry.addCN(cn); //add the CN to the Entry's list of CNs
                            System.out.println("Added");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**
//     * Function called by home button event handler that changes the scene back to the spreadsheet/tree view
//     * @param event fired by home button
//     * @throws IOException ?
//     */
//    public void goToHome(ActionEvent event) throws IOException {
//
//        //loads the scene
//        FXMLLoader loader = new FXMLLoader();
//        loader.setLocation(getClass().getResource("../FXML/sample.fxml"));
//        Parent P = loader.load();
//        Scene s = new Scene(P);
//
//        //shows the scene
//        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        window.setScene(s);
//        window.show();
//    }
}
