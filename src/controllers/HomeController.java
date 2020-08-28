package controllers;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;
import sample.CN;
import sample.DP;
import sample.Entry;
import sample.Main;

/**
 * <h1>Home Controller Class</h1>
 * This class is the controller for the "home"/main page of the application: the spreadsheet view where users
 * can work on their decomposition
 *
 * @author Bridget McLean - bmclean@wpi.edu
 */

public class HomeController extends ControllerClass implements Initializable {

    @FXML
    private AnchorPane rootPane;

    private Separator sep = new Separator();

    private TreeTableView<Entry> mytreetable = new TreeTableView<>();

    private TreeTableColumn<Entry, String> treeColDP = new TreeTableColumn<>();
    private TreeTableColumn<Entry, String> treeColFR = new TreeTableColumn<>();
    private TreeTableColumn<Entry, String> treeColID = new TreeTableColumn<>();

    private Button matrixButton = new Button();

    private Button cnButton = new Button();

    private Button traceBttn = new Button();

    private Button saveBttn = new Button();

    private Button reloadBttn = new Button();

    private ObservableList<TreeItem<Entry>> treeData = FXCollections.observableArrayList(); //all items in the treeview

    private int autoDPID = -1; //this variable holds the next DPID at any time




    /**
     * This function is called when the .fxml it's linked to is loaded. This initialize function is what makes this
     * class a controller
     *
     * @param url The location used to resolve relative paths for the root object
     * @param rb  The resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

//////////////////////////////////////// initialize all the ui stuff ////////////////////////////////////

        rootPane.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        rootPane.setMinSize(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

        mytreetable.setPrefSize(288, 341);
        mytreetable.setLayoutX(166);
        mytreetable.setLayoutY(61);

        rootPane.getChildren().add(mytreetable); //add TreeTableView to AnchorPane

        AnchorPane.setTopAnchor(mytreetable, 30.0);
        AnchorPane.setLeftAnchor(mytreetable, 0.0);
        AnchorPane.setRightAnchor(mytreetable, 0.0);
        AnchorPane.setBottomAnchor(mytreetable, 0.0);


        matrixButton.setLayoutX(14);
        matrixButton.setLayoutY(1);
        matrixButton.setText("Matrix");
        matrixButton.setPrefSize(65, 10);

        cnButton.setLayoutX(80);
        cnButton.setLayoutY(1);
        cnButton.setText("CN's");
        cnButton.setPrefSize(65, 10);

        traceBttn.setLayoutX(146);
        traceBttn.setLayoutY(1);
        traceBttn.setText("Trace");
        traceBttn.setPrefSize(65, 10);

        saveBttn.setLayoutX(212);
        saveBttn.setLayoutY(1);
        saveBttn.setText("Save");
        saveBttn.setPrefSize(65, 10);

        reloadBttn.setLayoutX(278);
        reloadBttn.setLayoutY(1);
        reloadBttn.setText("Reload");
        reloadBttn.setPrefSize(65, 10);

        rootPane.getChildren().addAll(matrixButton, cnButton, traceBttn, saveBttn, reloadBttn); //add buttons to AnchorPane

        treeColID.setPrefWidth(56.0);
        treeColID.setText("#");
        treeColFR.setPrefWidth(500);
        treeColFR.setText("Functional Requirements");
        treeColDP.setPrefWidth(245);
        treeColDP.setText("Design Parameters");

        mytreetable.getColumns().add(treeColID);
        mytreetable.getColumns().add(treeColFR);
        mytreetable.getColumns().add(treeColDP);

        sep.setLayoutX(91);
        sep.setLayoutY(29);

        AnchorPane.setLeftAnchor(sep, 0.0);
        AnchorPane.setRightAnchor(sep, 0.0);


/////////////////////////////Initialize database on Application/Pull from the database when scene is reopened//////////////


        if (Main.switchesMatrix == 0 && Main.switchesCN == 0) { //if this is the first time opening the home screen
            DP dp0 = new DP(" ", 0, getAutoDPID(), true); //make a fresh dp
            DP[] dpList0 = new DP[1]; //make a fresh dpList
            dpList0[0] = dp0; //put the dp in the dpList
            Entry FRDP0ent = new Entry(" ", dpList0, "0", autoDPID, 0, -1); //make a fresh entry object
            TreeItem<Entry> FRDP0 = new TreeItem<>(FRDP0ent); //turn it into a tree item
            FRDP = new Entry[1]; //extend the FRDP list (it's initialized to 0 because the Entry's are dynamically added from the database when the scene reopens)
            FRDP[0] = FRDP0ent; //add the Entry object to the FRDP list
            mytreetable.setRoot(FRDP0); //puts the tree item object at the top

//            String sql = "DELETE FROM Entry;"; //delete everything from the previous session in the database
//            String sql2 = "DELETE FROM DP;";
            String sql = "DROP TABLE IF EXISTS Entry";
            String sql2 = "DROP TABLE IF EXISTS DP";

            String sql5 = "CREATE TABLE Entry(FR mediumtext, DPID int(255), displayNum mediumtext, numChildren int(255), parentID int(255));";
            String sql6 = "CREATE TABLE DP(DPID int(255), DP mediumtext, count int(255), isPrimary int(11));";

            //add the 0 row to the database
            String sql3 = "INSERT INTO Entry VALUES('" + FRDP0ent.FR + "', " + FRDP0ent.DPID + ", '" + FRDP0ent.displayNum + "', " + FRDP0ent.numChildren + ", " + FRDP0ent.parentID + ");";
            String sql4 = "INSERT INTO DP VALUES(" + dp0.getDPId() + ", '" + dp0.getDp() + "', " + dp0.getCount() + ", " + dp0.getIsPrimary() + ");";

            executeDatabaseU(sql);
            executeDatabaseU(sql2);
            executeDatabaseU(sql5);
            executeDatabaseU(sql6);
            executeDatabaseU(sql3);
            executeDatabaseU(sql4);


        } else { //if we've already been to the homescreen, have to populate it from database

            refreshData(); //pulls data from the database and puts it all in the FRDP array
            autoDPID = FRDP.length - 1; //make sure the autoDPID is right
            buildTree(); //put all the Entry objects into the treeview
        }


//////////////////////////////// Display the text in the treeview and make it editable ////////////////////////////////

        //displays text in the FR column
        treeColFR.setCellValueFactory(param-> new SimpleStringProperty(param.getValue().getValue().getFR()));

        //displays text in the DP column
        treeColDP.setCellValueFactory(param-> new SimpleStringProperty(param.getValue().getValue().getDP()[0].getDp())); //puts all the DPs in the DP column

        //displays text in the ID column
        treeColID.setCellValueFactory(param-> new SimpleStringProperty(param.getValue().getValue().getDisplayNum())); //puts all the DPs in the DP column



        //gives you a text field to write/edit in the FR column
        treeColFR.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());


        //this stage is used for the popup window when you add a DP. It used to be declared inside the cell factory
        //inside the "if t1.equals("Add New DP..") statement, but under certain circumstances multiple comboboxes
        //trigger the ChangeListener which makes the stage popup more than once. Declaring the stage out here and
        //putting dialog.show() in an if statement to check if its showing kinda puts a bandaid on this problem.
        //update: I had to do the same bandaid technique for the Change DP popup too. Now I have the Add DP popup and
        //the Change DP popup both on this stage, which should be fine because you'll never need to add/change a dp at
        //the same time
        Stage dialog = new Stage(); //create the popup window


        //everything you're about to see here is what it took to get editable dropdown comboboxes
        //the updateItem function I'm overriding is what allows me to add my own editable comboboxes
        //javafx has a thing that turns cells into comboboxcells automatically, but it was difficult for me to figure out -->
        // --> how to allow the user to enter a DP in and save it as an option in the combobox
        //the changed() function I'm overriding is what allows me to listen for the user selecting an option from the combobox, -->
        // --> this way, I can tell when they select "Edit DPs...", and set off all the events to make the popup
        treeColDP.setCellFactory(tc -> {
            ComboBox<String> combo = new ComboBox<>(); //make the combobox
            combo.setMaxWidth(1.7976931348623157E308); //this number is equal to max width of the cell
            return new TreeTableCell<>() { //make the new cell
                @Override
                protected void updateItem(String item, boolean empty) {//item is the string being shown in the cell, empty is if it's empty or not
                    super.updateItem(item, empty); //inherit everything from the original updateItem function

                    if (empty) {
                        setGraphic(null);
                    }
                    else {
                        Entry entry = this.getTreeTableRow().getItem(); //get the current row item

                        if (entry == null) {
                            //System.out.println("The selected item is null");
                        }
                        else {
                            ObservableList<String> allDPList = FXCollections.observableArrayList(getList(entry.getDP(), false)); //get an observable list of all the DP strings for this FR
                            combo.getItems().clear(); //make sure nothing is in the combobox
                            combo.getItems().addAll(allDPList); //add all the DPs to the combobox
                            setGraphic(combo); //sets the cell's graphic as the combo box

                            combo.setValue(entry.getPrimaryDP().getDp()); //show the primary DP, if there is one

                            combo.valueProperty().addListener(new ChangeListener<String>() {
                                //t is what the combobox was on, t1 is what the combobox is being changed to
                                @Override
                                public void changed(ObservableValue ov, String t, String t1) {


                                    if (t1 == null || t == null) { //sometimes they're just null, ignore it
                                    }

                                    else if (t1.equals("Edit DPs...")) { //if the user wants to add/edit/delete a new DP

                                        DPEditor(dialog, entry, combo); //open a popup

                                    }
                                }
                            });
                        }
                    }
                }
            };
        });

        //commits edits to FRs
        treeColFR.setOnEditCommit(event->{
                Entry oldFR = event.getRowValue().getValue(); //gets the Entry object that is being edited
                oldFR.setFR(event.getNewValue()); //sets the new FR
                String temp = event.getNewValue();
                temp = temp.replace("'","''"); //if there's an apostrophe in user input this stops the database from error-ing out

                //update the FR in the database
                String sql = "UPDATE Entry SET FR = '" + temp + "' WHERE DPID = " + oldFR.DPID + ";";
                executeDatabaseU(sql);
        });


        mytreetable.setEditable(true);

////////////////////////////// Set up right-click event and subsequent contextMenu /////////////////////////////////////////////////

        ContextMenu contextMenu = new ContextMenu(); //create the contextmenu


        MenuItem addRowItem = new MenuItem("Add Sibling"); //create menu item for adding a row
        addRowItem.setOnAction(event-> addNewSibling());


        MenuItem addChildItem = new MenuItem("Add Child"); //create menu item for adding a child
        addChildItem.setOnAction(event-> addNewChild());

        MenuItem deleteRowItem = new MenuItem("Delete Row"); //create menu item for deleting a row
        deleteRowItem.setOnAction(event -> deleteRow());

        contextMenu.getItems().addAll(addRowItem, addChildItem, deleteRowItem); //add menu items to the contextmenu
        contextMenu.setAutoHide(true);

        //closes the context menu when you click outside of it
        mytreetable.setOnMouseClicked(event->{
            if(event.getButton() == MouseButton.PRIMARY) {
                if (contextMenu.isShowing()) {
                    contextMenu.hide();
                }
            }
        });

        // When user right-clicks on the FRs or DPs
        mytreetable.setOnContextMenuRequested(event-> {
                TreeItem<Entry> clicked = mytreetable.getSelectionModel().getSelectedItem(); //get the item that was clicked
                if (clicked.getValue().displayNum.equals("0")) { //can't add a sibling to or delete the 0 row
                    addRowItem.setDisable(true);
                    deleteRowItem.setDisable(true);
                } else {
                    addRowItem.setDisable(false);
                    deleteRowItem.setDisable(false);
                }

                contextMenu.show(mytreetable, event.getScreenX(), event.getScreenY()); //show the context menu at the X Y location of the right click
        });


///////////////////////////////////// Set up the buttons ////////////////////////////////////////////////

        //go to matrix scene when button is clicked
        matrixButton.setOnAction(event-> {
                try {
                    Main.switchesMatrix++;
                    goToMatrix(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });

        //go to CN scene when button is clicked
        cnButton.setOnAction(event-> {
                try {
                    Main.switchesCN++;
                    goToCN(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });

        //trace to a CN when the button is clicked and an FR is selected
        traceBttn.setOnAction(event-> {
                try {
                    //Main.switchesCN++;
                    traceToCN(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });

        saveBttn.setOnAction(event-> {
            try{
                saveDatabase();

            } catch (Exception e){
                e.printStackTrace();
            }
        });

        reloadBttn.setOnAction(event-> {
            try{
                reloadSession();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        });


    } //end of initialize




///////////////////////////////////// Functions ////////////////////////////////////

/// Functions for Adding/Deleting Rows
/// Functions for Switching Scenes
/// Tracing Functions
////// Tracing from FR to CN
////// Tracing from CN to FR
/// Random Functions






//////////////////////////////////// Functions for Adding/Deleting Rows ////////////////////////////////////////

    /**
     * Adds a new row to the same level as the item that was right-clicked on
     */
    private void addNewSibling() {
        TreeItem<Entry> clicked = mytreetable.getSelectionModel().getSelectedItem(); //get the item that was clicked
        TreeItem<Entry> parent = clicked.getParent(); //find its parent
        String parentID = parent.getValue().getDisplayNum(); //get ID of parent
        int numChild = parent.getChildren().size(); //find how many children parent has


        String newID;
        if (parentID.equals("0")) { //if the parent is FRDP0
            newID = Integer.toString(numChild + 1); //then the new row will be the number of children plus 1
        } else { //if the parent isn't FRDP0, then it follows a naming convention-> parentID.numChild+1
            //ex: parent is 1.1, already has 2 children, new child will be 1.1.3
            newID = parentID + "." + Integer.toString(numChild + 1);
        }

        addRowInfo(newID, numChild, parent);
    }

    //Adds a new row to the level below the selected item

    /**
     * Adds a new row to the level below the item that was right-clicked on
     */
    private void addNewChild() {
        TreeItem<Entry> clicked = mytreetable.getSelectionModel().getSelectedItem(); //get the item that was clicked
        String clickedID = clicked.getValue().getDisplayNum(); //get the ID of the item that was clicked
        String newID;
        int numChild = clicked.getChildren().size(); //find how many children the clicked item has
        if (clickedID.equals("0")) { //if the clicked item is FRDP0
            newID = Integer.toString(numChild + 1); //then the new row will be the number of children plus 1
        } else {//if the parent isn't FRDP0, then it follows a naming convention-> clickedID.numChild+1
            //ex: parent is 1.1, already has 2 children, new child will be 1.1.3
            newID = clickedID + "." + Integer.toString(numChild + 1);
        }

        int numDots = newID.length() - newID.replace(".", "").length();
        int depth = numDots+1;

        boolean tof = recursiveExpand(-1, mytreetable.getRoot(), depth);
        if(tof){
            treeColID.setPrefWidth(treeColID.getPrefWidth() + 20);
        }

        addRowInfo(newID, numChild, clicked);

    }


    /**
     * Removes the row that was right-clicked on (and all of its children)
     */
    private void deleteRow() {
        TreeItem<Entry> clicked = mytreetable.getSelectionModel().getSelectedItem(); //get the item that was clicked
        TreeItem<Entry> parent = clicked.getParent(); //get its parent


        //popup ensures no one accidentally deletes half their tree
        Label lab = new Label("Are you sure you want to delete this row and all its children?");
        Button delete = new Button("Yes");
        Button cancel = new Button("Cancel");
        Stage stage = new Stage();
        VBox box = new VBox(20);
        HBox hbox = new HBox(20);
        box.setAlignment(Pos.CENTER);
        hbox.setAlignment(Pos.CENTER);
        lab.setWrapText(true);
        lab.setMaxWidth(275);

        box.getChildren().addAll(lab, hbox);
        hbox.getChildren().addAll(delete, cancel);
        Scene scene = new Scene(box, 300, 200);
        stage.setScene(scene);
        stage.show();


        cancel.setOnAction(event-> stage.close());


        delete.setOnAction(event-> {
                parent.getChildren().remove(clicked); //remove it (and all its children) from the tree

                //now we have to delete it and all its children from the database
                Entry entry = clicked.getValue(); //get the Entry item that was clicked
                Entry parentEnt = parent.getValue(); //get the Entry item of the parent of the item that was clicked
                parentEnt.setNumChildren(parentEnt.numChildren - 1);
                String sql6 = "UPDATE Entry SET numChildren = numChildren-1 WHERE DPID = " + parentEnt.DPID + ";";
                executeDatabaseU(sql6);

                Entry[] delEnts = new Entry[1]; //this array will hold all the entries we have to delete
                delEnts[0] = entry; //throw the clicked entry object in there
                delEnts = recursiveDelete(delEnts, entry); //find all the children to delete

                //sort the array to allow for easy removal of everything
                //why does this make it easier? -> other parts of the application rely on the DPIDs being
                //sequential. When an entry is deleted, all the entries with a DPID higher than the one deleted need their
                //DPIDs adjusted (subtracted by 1) to keep them sequential. However, if we go through an unsorted array of
                //entries and delete them from the database as we go, then the DPIDs of the entries in the array won't match
                //the DPIDs of the entries in the database (because we keep subtracting 1 from the DPIDs in the database).
                //By sorting the array, we can start with the entry with the highest DPID and work our way down to the lowest,
                //that way every entry that needs to be deleted will have the correct DPID in the database until it is deleted

                Arrays.sort(delEnts, Comparator.comparingInt(Entry :: getDPID));

                autoDPID = autoDPID - delEnts.length; //reset the DPID auto incrementer so the next new entry has the right DPID


                for (int i = delEnts.length - 1; i >= 0; i--) { //go through delEnts backwards and delete the entries 1 by 1

                    Entry ent = delEnts[i];

                    String sql = "DELETE FROM Entry WHERE DPID = " + ent.DPID + ";";
                    String sql2 = "DELETE FROM DP WHERE DPID = " + ent.DPID + ";";
                    String sql7 = "DELETE FROM CNFRLink WHERE DPID = " + ent.DPID + ";";
                    String sql5 = "UPDATE Entry SET parentID = parentID-1 WHERE parentID > " + ent.DPID + ";";
                    String sql3 = "UPDATE Entry SET DPID = DPID-1 WHERE DPID > " + ent.DPID + ";";
                    String sql4 = "UPDATE DP SET DPID = DPID-1 WHERE DPID > " + ent.DPID + ";";
                    String sql8 = "UPDATE CNFRLink SET DPID = DPID-1 WHERE DPID > " + ent.DPID + ";";

                    String sql9 = "DELETE FROM MatrixCell WHERE DPIDfr = " + ent.DPID + ";";
                    String sql10 = "DELETE FROM MatrixCell WHERE DPIDdp = " + ent.DPID + ";";
                    String sql11 = "UPDATE MatrixCell SET DPIDfr = DPIDfr-1 WHERE DPIDfr > " + ent.DPID + ";";
                    String sql12 = "UPDATE MatrixCell SET DPIDdp = DPIDdp-1 WHERE DPIDdp > " + ent.DPID + ";";

                    String sql13 = "SELECT matrixcolumn FROM MatrixCell WHERE DPIDdp = " + ent.DPID + ";";
                    int rowcol = getResult(sql13);
                    String sql14 = "UPDATE MatrixCell SET matrixrow = matrixrow-1 WHERE matrixrow > " + rowcol + ";";
                    String sql15 = "UPDATE MatrixCell SET matrixcolumn = matrixcolumn-1 WHERE matrixcolumn > " + rowcol + ";";

                    executeDatabaseU(sql);
                    executeDatabaseU(sql2);
                    executeDatabaseU(sql7);
                    executeDatabaseU(sql5);
                    executeDatabaseU(sql3);
                    executeDatabaseU(sql4);
                    executeDatabaseU(sql8);

                    executeDatabaseU(sql9);
                    executeDatabaseU(sql10);
                    executeDatabaseU(sql11);
                    executeDatabaseU(sql12);
                    executeDatabaseU(sql14);
                    executeDatabaseU(sql15);
                }

                recursiveDisplayNum(parent, parent.getValue().displayNum);

                refreshData(); //updates the entry objects so they match the database
                buildTree(); //rebuilds the tree with the new entry objects
                stage.close();
        });
    }

    /**
     * Adds the information for a new row into the TreeTableView, the FRDP list, and the database.
     *
     * @param newID    the displayNum for the new row. Ex: 1, 1.1, 1.2.5....
     * @param numChild the number of children the parent has before adding the new row
     * @param clicked  the TreeItem for the parent that we are adding a child to
     */
    private void addRowInfo(String newID, int numChild, TreeItem<Entry> clicked) {
        DP newDP = new DP(" ", getAutoDPID(), 0, true);
        DP[] dpList = new DP[1];
        dpList[0] = newDP;

        Entry entry = new Entry(" ", dpList, newID, autoDPID, 0, clicked.getValue().DPID);
        clicked.getChildren().add(new TreeItem<>(entry)); //add new blank row with appropriate ID
        clicked.setExpanded(true);
        clicked.getValue().setNumChildren(numChild + 1);

        String sql = "INSERT INTO Entry VALUES('" + entry.FR + "', " + entry.DPID + ", '" + entry.displayNum + "', " + entry.numChildren + ", " + entry.parentID + ");";
        String sql2 = "INSERT INTO DP VALUES(" + newDP.getDPId() + ", '" + newDP.getDp() + "', " + newDP.getCount() + ", " + newDP.getIsPrimary() + ");";
        String sql3 = "UPDATE Entry SET numChildren = '" + (numChild + 1) + "' WHERE DPID = " + clicked.getValue().DPID + ";";

        executeDatabaseU(sql);
        executeDatabaseU(sql2);
        executeDatabaseU(sql3);


        refreshData();

    }


    /**
     * Recursively finds all children, grandchildren, etc. of the given entry. Called in the deleteRow() function
     * @param delEnts array of entry objects to be deleted
     * @param entry the entry object whose children we're looking for
     * @return the delEnts array with all the Entries to be deleted
     */
    private Entry[] recursiveDelete(Entry[] delEnts, Entry entry) { //The given parent entry will already be in list
        for (Entry ent : this.FRDP) { //for each ent in the FRDP list
            if (entry.DPID == ent.parentID) { //if the given entry is the parent of the ent
                delEnts = recursiveDelete(delEnts, ent); //find this ent's children
                delEnts = addEnt(delEnts, ent); //add this ent to the delete list
            }
        }
        return delEnts; //return the delete list
    }

    /**
     * Recursively changes the displayNum of the given TreeItem<Entry> object and all its children. Called in the
     * deleteRow() function
     * @param parent the TreeItem<Entry> object that needs its displayNum changed
     * @param pDisplayNum the parent's new displayNum. The first time the function is called, pDisplayNum is just passed in
     * as parent.getValue().displayNum, because the parent of the deleted object will keep it's display number. In the
     * recursive calls after the first time, pDisplayNum is the newID variable
     */
    private void recursiveDisplayNum(TreeItem<Entry> parent, String pDisplayNum) {
        //I used the TreeItem instead of the Entry item so I could get this list of children
        ObservableList<TreeItem<Entry>> children = parent.getChildren();
        String sql;
        if (parent.getValue().DPID == 0) { //displayNum is set differently for children of 0
            for (TreeItem<Entry> entry : children) {
                int i = children.indexOf(entry);
                sql = "UPDATE Entry SET displayNum = '" + (i + 1) + "' WHERE displayNum = '" + entry.getValue().displayNum + "';";
                executeDatabaseU(sql);
                recursiveDisplayNum(entry, Integer.toString(i + 1));
            }
        } else {
            for (TreeItem<Entry> entry : children) {
                int i = children.indexOf(entry);
                String newID = pDisplayNum + "." + Integer.toString(i + 1);
                sql = "UPDATE Entry SET displayNum = '" + newID + "' WHERE displayNum = '" + entry.getValue().displayNum + "';";
                executeDatabaseU(sql);
                recursiveDisplayNum(entry, newID);
            }
        }
    }

    /**
     * Called when adding a new child. Recursively goes through the TreeTableView to see if there are any nodes
     * at the given depth. If there are no nodes at the given depth, the function returns true to indicate that
     * the ID column of the TreeTableView must be expanded (made wider) so there is enough room to display the ID.
     * @param currDepth int to keep track of what layer of the TreeTableView we're currently searching through
     * @param parent TreeItem<Entry> the node whose children we're looking at
     * @param depth int - the depth at which we are checking for nodes, the depth of the new child being added
     * @return true if there are no nodes at the given depth, false if there are nodes at the given depth
     */
    private boolean recursiveExpand(int currDepth, TreeItem<Entry> parent, int depth){

        currDepth++; //what level of the tree we're currently on

        if(currDepth >= depth){ //if currDepth is greater than or equal to the depth of the new child,
            return false; //then the column was already expanded to accomodate this depth so we don't need to expand it more
        }
        else{ //if the current depth is less than the depth of the new child
            ObservableList<TreeItem<Entry>> children = parent.getChildren(); //search its children

            for (TreeItem<Entry> child : children) {
                boolean tof = recursiveExpand(currDepth, child, depth); //see if the children are at the new child's depth
                if(!tof){ //if it returns false, it means there's already a node at the new child's depth
                    return false; //so we don't need to expand the column
                }
            }
        }
        return true; //if we get here, it means we went through every node in the tree and none were at the new child's depth
    }                //so expand the column













////////////////////////////////////// Functions for Switching Scenes /////////////////////////////////////////

    /**
     * Called when the "Matrix" button is pressed, opens the matrix scene
     *
     * @param event the button press event
     * @throws IOException ?
     */
    private void goToMatrix(ActionEvent event) throws IOException {

        //loads the scene
        FXMLLoader loader = new FXMLLoader();
        //System.out.println("about to load");
        loader.setLocation(getClass().getResource("/FXML/rand.fxml"));
        //loader.setController(MatrixController.getMatrixInstance());
        Parent P = loader.load();
        Scene s = new Scene(P, 1000, 750);

        //shows the scene
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(s);
        window.show();

    }


    /**
     * Called when the "CN's" button is pressed, opens the Customer Needs scene
     *
     * @param event the button press event
     * @throws IOException ?
     */
    private void goToCN(ActionEvent event) throws IOException {

        //loads the scene
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/FXML/CustomerNeeds.fxml"));
        Parent P = loader.load();
        Scene s = new Scene(P, 1000, 500);

        //shows the scene
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(s);
        window.show();

    }











///////////////////////////////////////// Tracing Functions ///////////////////////////////////////////////

//////////////// Tracing from FR (HomeController) to CN (CNController)


    private void traceToCN(ActionEvent event) throws IOException {
        //NOTE: Don't get confused by my comments and my bad naming conventions. In the comments below,
        //  when I mention "CNList", I am referring to the CN[] array in an Entry object.
        //  the Entry.CNList only holds the CNs that have been linked to that Entry object
        //  when I mention "cnList", I am referring to the global CN[] array declared at the top of this class
        //  the cnList holds ALL the CN objects that have been created/displayed on the TreeTableView


        refreshData(); //refreshes the Entry object information from the database, fills the FRDP list
        refreshCNData(); //refreshes the CN object information from the database, fills the cnList

        if(mytreetable.getSelectionModel().getSelectedItem() == null) {
            String error = "Error: There is no FR selected. Please select the FR you would like to trace.";
            errorPopup(error);
        }
        else {
            Entry clickedEnt = mytreetable.getSelectionModel().getSelectedItem().getValue();
            //the Entry we grab from the tree doesn't get updated by the matchCNFR() function, so it doesn't
            //  have any CNs in its CNList. To access the CNList for this Entry, we have to use its DPID to find the updated
            //  CN object in the cnList
            for (Entry ent : this.FRDP) {
                if (ent.DPID == clickedEnt.DPID) {
                    clickedEnt = ent;
                }
            }
            CN cn;
            if (clickedEnt.getCNList().length > 1) { //if the selected FR can be traced to more than one CN
                cn = pickCNPopup(clickedEnt); //create a popup and have the user select which CN they'd like to trace to
                if(cn == null){
                    String error = "Error: No CN selected. Tracing could not be completed.";
                    errorPopup(error);
                    return;
                }
            }
            else if (clickedEnt.getCNList().length == 1) { //if the selected FR can only be traced to 1 CN
                cn = clickedEnt.getCNList()[0]; //grab that CN
            }
            else { //if there are no CNs linked to this FR, tell the user to link it to CNs
                String error = "Error: The selected FR is not linked to any CNs.";
                errorPopup(error);
                return;
            }

            //load up the FR spreadsheet (home) page and focus on the selected FR
            Main.switchesCN++;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/FXML/CustomerNeeds.fxml"));
            Parent P = loader.load();
            Scene s = new Scene(P, 1000, 500);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(s);

            CNController cont = loader.getController();
            cont.findCN0(cn.getDisplayID()); //call this function in the HomeController to focus on the FR


            window.show();
        }
    }


    /**
     * Called by traceToFR() to open a popup with a list of FRs that the user can trace to. Records the user's selection
     * @param entry the Entry that the user selected to trace from
     */
    private CN pickCNPopup(Entry entry){
        //open a popup with a list of CNs linked to the selected FR and have the user pick which one to trace to
        Stage stage = new Stage();
        VBox dialogVbox = new VBox(20);
        TableView<CN> list = new TableView<>();
        TableColumn<CN, String> displayNumCol = new TableColumn<>("#");
        TableColumn<CN, String> frCol = new TableColumn<>("CN");
        frCol.setPrefWidth(250);

        displayNumCol.setCellValueFactory(p->new SimpleStringProperty(p.getValue().getDisplayID()));

        frCol.setCellValueFactory(p-> new SimpleStringProperty(p.getValue().getCn()));

        list.getColumns().add(displayNumCol);
        list.getColumns().add(frCol);

        ObservableList<CN> obsEntry = FXCollections.observableArrayList(entry.getCNList());
        list.getItems().addAll(obsEntry);

        Label lab = new Label("There is more than one CN linked with this FR. Please choose which CN you'd like to trace to");
        Button b = new Button("Submit");
        lab.setWrapText(true);
        lab.setMaxWidth(300);
        list.setLayoutY(250);
        list.setPrefHeight(175);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.getChildren().addAll(lab, list, b); //add list to VBox

        CN[] returnCN = new CN[1];
        b.setOnAction(event-> {
                if(list.getSelectionModel().getSelectedItem() == null){
                    String error = "Error: Please select the CN you would like to trace to.";
                    errorPopup(error);
                }
                else {
                    CN cn = list.getSelectionModel().getSelectedItem();
                    returnCN[0] = cn;
                    stage.close();
                }
        });

        Scene dialogScene = new Scene(dialogVbox, 300, 300);
        stage.setScene(dialogScene);
        if (!stage.isShowing()) {
            stage.showAndWait(); //show the popup window
        }

    return returnCN[0];

    }


//////////////// Tracing from CN (CNController) to FR (HomeController)

    /**
     * Called from the CNController when the user wants to trace a CN to an FR on this page
     * @param DPID the DPID of the FR they are tracing
     */
    void findFR0(int DPID){
        Entry e = new Entry("BAD", new DP[0], " ", -1, -1, -1);
        TreeItem<Entry> frdp0 = new TreeItem<>(e); //dummy value for the first tree item
        for(TreeItem<Entry> entry : treeData){
            if(entry.getValue().DPID == 0){ //find the actual object for the first item
                frdp0 = entry; //save it
                break;
            }
        }
        int[] rowArr = {0, 0};
        //We needed to get first item to start at the top of the tree to recursively search through children
        //and find the row number of the FR we are tracing. In the TreeTableView, a parent is always followed by
        //its children, so we need to start at the top and search through the tree BFS style to get the row number
        rowArr = recursiveFindFR(frdp0, rowArr, DPID); //pass that root tree item into the recursive function to find what row we want to focus on
        mytreetable.getFocusModel().focus(rowArr[1], treeColFR); //focus on that row
        mytreetable.scrollTo(rowArr[1]); //scroll to it
    }

    /**
     * Called by the findFR0() function to recursively search through the TreeTableView using Depth First Search
     * to find the row number of the FR we are tracing to.
     * @param parent the TreeItem<Entry> object whose children we need to search
     * @param row the row number of the parent object we passed in
     * @param DPID the ID number of the TreeItem<Entry> object we are searching for
     * @return the row number of the TreeItem<Entry> object we are searching for
     */
    private int[] recursiveFindFR(TreeItem<Entry> parent, int[] row, int DPID){
        ObservableList<TreeItem<Entry>> children = parent.getChildren(); //get all the children of the given parent

        //System.out.println(parent.getValue().DPID);
        //System.out.println(row[1]);

        if(DPID == 0){ //if we're tracing to the first row, just go there
            row[0] = 1;
            return row;
        }
        for(TreeItem<Entry> entry : children){
            //System.out.println("Looking at " + parent.getValue().displayNum + "'s child: " + entry.getValue().DPID);
            row[1]++; //every time we look at a new child, it's another row down
            if(entry.getValue().DPID == DPID){ //is this the FR we're tracing to?
                row[0] = 1;
                break;
            }
            else{
                row = recursiveFindFR(entry, row, DPID); //if not, look at this one's children
                if(row[0] == 1){break;}
            }
        }
        return row;

    }














///////////////////////////////////// Random Functions ////////////////////////////////////////////////

    /**
     * Adds the given entry to the end of the given array of entries
     * @param entList array of entries
     * @param entry entry to add to given array of entries
     * @return the new array with the given entry object appended to the end
     */
    private Entry[] addEnt(Entry[] entList, Entry entry) {
        int len = entList.length;
        Entry[] temp = new Entry[len + 1];
        System.arraycopy(entList, 0, temp, 0, len);
        temp[len] = entry;
        return temp;
    }

    /**
     * Auto-counter that keeps track of the next new Entry's DPID, keeping all the DPIDs sequential
     * @return the DPID for the next new Entry
     */
    private int getAutoDPID() {
        autoDPID++;
        return autoDPID;
    }

    /**
     * Uses the FRDP list and pulls from the database to tuen the Entry objects into TreeItems and build the TreeView
     */
    private void buildTree() {

        if (treeData.size() != 0) {
            treeData.clear();
        }

        String url_ = "jdbc:mariadb://localhost:3306/mysql";
        String usr = "root";
        String pwd = "root";
        try {
            Connection myconn = DriverManager.getConnection(url_, usr, pwd);
            Statement stmt = myconn.createStatement();


            for (Entry ent : this.FRDP) {
                treeData.add(new TreeItem<>(ent)); //turn all the entries into treeitems
            }

            //build the tree
            for (TreeItem<Entry> entry : treeData) { //for each entry object
                String get = "SELECT * FROM Entry WHERE parentID = " + entry.getValue().DPID; //find its children
                ResultSet res = stmt.executeQuery(get);

                if (entry.getValue().DPID == 0) { //when it finds FR0
                    mytreetable.setRoot(entry); //set it at the top
                }

                //we have all the children in the ResultSet, but we need to associate them-->
                // -->to the treeitem that was already made for it and save it somewhere
                while (res.next()) { //so for each child that we found in the database
                    for (TreeItem<Entry> treeitem : treeData) { //go through our list of tree items
                        if (res.getInt("DPID") == treeitem.getValue().DPID) { //find a tree item with the DPID that matches the DPID of the child in the database
                            entry.getChildren().add(treeitem); //add all children to the parent
                            entry.setExpanded(true);
                            break; //dont need to look through treeitem list anymore, break into while loop
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Turns a list of DP objects into a list of dp Strings. Used for populating the comboboxes in the DP column.
     *
     * @param dpList the list of DP objects to extract the strings from
     * @param remove false when the returned list should start with add/change dp options, true when returned list should only be dp strings
     * @return list of strings to put in the DP column
     */
    private String[] getList(DP[] dpList, boolean remove) {
        int len = dpList.length;

        if (!remove) {
            String[] dpString = new String[len + 1];
            dpString[0] = "Edit DPs...";
            for (int i = 0; i < len; i++) {
                dpString[i + 1] = dpList[i].getDp();
            }
            return dpString;
        } else {
            String[] dpString = new String[len];
            for (int i = 0; i < len; i++) {
                dpString[i] = dpList[i].getDp();

            }
            return dpString;
        }
    }


    /**
     * Creates a popup window for users to add new DPs and see, edit, or delete existing DPs
     *
     * @param dialog the stage that the popup window is displayed on. It is declared outside of this function because
     *               I found that sometimes the stage would be declared more than once, making many popup windows
     * @param entry the Entry object to which the DPs that we're editing belong to
     * @param combo the combobox that displays all the DPs in the TreeTableView
     */
    private void DPEditor(Stage dialog, Entry entry, ComboBox combo){
        VBox dialogVbox = new VBox(20);
        //dialogVbox.setAlignment(Pos.CENTER);

        //First, set up a table with all this Entry's DPs
        ObservableList<DP> tableList =  FXCollections.observableArrayList(entry.getDP()); //display all this Entry's DPs
        TableView<DP> table = new TableView<>();

        TableColumn<DP, String> displayNumCol = new TableColumn<>("#");
        TableColumn<DP, String> dpCol = new TableColumn<>("DP");
        TableColumn<DP, CheckBox> checkCol = new TableColumn<>("Primary");

        displayNumCol.setPrefWidth(60);
        dpCol.setPrefWidth(270);
        checkCol.setPrefWidth(60);

        displayNumCol.setCellValueFactory(p-> new SimpleStringProperty(Integer.toString(p.getValue().getCount())));
        dpCol.setCellValueFactory(p-> new SimpleStringProperty(p.getValue().getDp()));

        table.getColumns().add(displayNumCol);
        table.getColumns().add(dpCol);
        table.getColumns().add(checkCol);
        table.getItems().addAll(tableList);

        //Allows users to edit a DP
        table.setEditable(true);
        dpCol.setCellFactory(TextFieldTableCell.forTableColumn());

        //this adds checkboxes to the "Primary" column
        checkCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DP, CheckBox>, ObservableValue<CheckBox>>() {

            @Override
            public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<DP, CheckBox> arg0) {
                DP dp = arg0.getValue(); //gets the DP object that was checked

                CheckBox checkBox = new CheckBox();

                checkBox.selectedProperty().setValue(dp.getIsPrimary()); //checkbox is based off the DP's "isPrimary" boolean value

                checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    public void changed(ObservableValue<? extends Boolean> ov,
                                        Boolean old_val, Boolean new_val) {

                        if (new_val) { //if it's being checked AKA switched from false to true

                            for(DP d : entry.getDP()) { //go through all the DPs
                                if (d.getCount() == dp.getCount()) { //if this is the DP that is changing to primary
                                    d.setIsPrimary(new_val); //change it
                                }
                                else if(d.getIsPrimary()){ //if this is the old primary DP
                                    d.setIsPrimary(false); //change it
                                }
                            }

                            //refresh the data in the list so the checkboxes update
                            table.getItems().clear();
                            tableList.clear();
                            tableList.addAll(entry.getDP());
                            table.getItems().addAll(tableList);


                            //update changes in the database
                            String sql = "UPDATE DP SET isPrimary = 0 WHERE isPrimary = 1 AND DPID = " + dp.getDPId() + ";";
                            String sql2 = "UPDATE DP SET isPrimary = 1 WHERE DPID = " + dp.getDPId() + " AND COUNT = " + dp.getCount() + ";";
                            executeDatabaseU(sql);
                            executeDatabaseU(sql2);
                        }
                        else{ //if they're trying to uncheck a DP
                            checkBox.selectedProperty().setValue(old_val); //don't let them, there should always be a primary dp
                        }


                    }
                });

                return new SimpleObjectProperty<CheckBox>(checkBox);

            }

        });

        //When a user edits a DP
        dpCol.setOnEditCommit(event->{
            DP oldDP = event.getRowValue(); //gets the DP object that is being edited
            combo.getItems().remove(oldDP.getDp()); //remove old value from combobox on the TreeTableView
            oldDP.setDp(event.getNewValue()); //sets the new DP
            combo.getItems().add(event.getNewValue()); //adds the new DP to the combobox on the TreeTableView

            String temp = event.getNewValue();
            temp = temp.replace("'","''"); //if the user puts an apostrophe this stops an error from the database

            //update the FR in the database
            String sql = "UPDATE DP SET DP = '" + temp + "' WHERE DPID = " + oldDP.getDPId() + " AND count = " + oldDP.getCount() + ";";
            executeDatabaseU(sql);
        });


        Button add = new Button(); //add a DP
        Button rem = new Button(); //remove a DP
        Button done = new Button(); //close the popup
        add.setText("+");
        add.setPrefSize(30, 30);
        rem.setText("-");
        rem.setPrefSize(30, 30);
        done.setText("Done");
        dialogVbox.getChildren().addAll(table, add, rem, done);
        Scene dialogScene = new Scene(dialogVbox, 400, 400);
        dialog.setScene(dialogScene);
        if (!dialog.isShowing()) {
            dialog.show(); //show the popup window
        }

        //position the buttons
        rem.setTranslateX(30);
        rem.setTranslateY(-67);
        add.setTranslateY(-20);
        add.setPrefSize(20, 20);
        rem.setPrefSize(20,20);

        //Adding a DP
        add.setOnAction(event-> {
            try {
                entry.addDP(" ");//update the DP list in the Entry object by adding a blank DP
                int len = entry.getDP().length;
                table.getItems().add(entry.getDP()[len-1]); //add the blank DP row  to the table

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        //Removing a DP
        rem.setOnAction(event-> {
            try {
                DP selected = table.getSelectionModel().getSelectedItem();
                combo.getItems().remove(selected.getDp());
                table.getItems().remove(selected);
                String sql = "DELETE FROM DP WHERE DPID = " + selected.getDPId() + " AND count = " + selected.getCount();
                executeDatabaseU(sql);

                String sql2 = "UPDATE DP SET count = count-1 WHERE DPID = " + selected.getDPId() + " AND count > " + selected.getCount() + ";";
                executeDatabaseU(sql2);

                entry.removeDP(selected.getCount());
                table.getItems().clear();
                tableList.clear();
                tableList.addAll(entry.getDP());
                table.getItems().addAll(tableList);

            } catch (Exception e) {
                e.printStackTrace();
            }


        });

        //Close the popup
        done.setOnAction(event-> {
            try{
                combo.setValue(entry.getPrimaryDP().getDp()); //display the primary DP
                dialog.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        });
    }


    /**
     * Saves everything from the database into csv file that can later be reloaded into the application so
     * the user can save their decompositions
     */
    private void saveDatabase(){
        try{
            //make a popup for users to name their file, or save it to an existing file
            Stage stage = new Stage();
            VBox dialogVbox = new VBox(20);
            dialogVbox.setAlignment(Pos.CENTER);
            Label lab = new Label("Please enter a name for your new file or select a previously saved file.");
            TextField txt = new TextField();
            Button b = new Button("Submit");
            lab.setWrapText(true);
            lab.setMaxWidth(375);
            dialogVbox.setAlignment(Pos.CENTER);

            //get the directory where the csv files will be saved
            File dir = new File("previous_sessions"); //PATH FOR JAR
            //File dir = new File("src/previous_sessions"); //PATH FOR INTELLIJ
            String[] pathnames = dir.list(); //get a list of csv files already saved in the directory
            ObservableList<String> tableList =  FXCollections.observableArrayList(pathnames); //display the file names in the list
            ListView<String> list = new ListView<>(tableList);

            dialogVbox.getChildren().addAll(lab, txt, list, b);
            //name[0] = name of file they entered/selected
            //name[1] = true if they selected existing file, false if they entered a new file name
            String[] name = new String[2];

            b.setOnAction(e-> {
                if(txt.getText().equals("") || txt.getText().equals(" ")){ //if they didn't enter text
                    String selected = list.getSelectionModel().getSelectedItem(); //they must have selected something
                    if(selected == null){ //throw an error if they didn't select anything
                        String error = "Error: Please enter a name to save this as a new file, or select an existing file to save to.";
                        errorPopup(error);
                        return;
                    }
                    else{ //if they did select something
                        name[0] = selected; //save the file name they selected
                        name[1] = "true"; //indicates that they selected a file
                    }
                }
                else{ //if they entered text
                    for(String s : pathnames){
                        if((txt.getText() + ".csv").equals(s)){
                            String error = "Error: A file with that name already exists. Please enter a different name.";
                            errorPopup(error);
                            return;
                        }
                    }
                    name[0] = txt.getText(); //save it
                    name[1] = "false";
                }
                stage.close();

            });

            Scene dialogScene = new Scene(dialogVbox, 400, 500);
            stage.setScene(dialogScene);
            stage.showAndWait(); //wait for the user to select/enter file name

            if(name[0] == null){
                return;
            }

            File file;
            if(name[1].equals("true")){ //if they selected an existing file to save to
                //file = new File("src/previous_sessions/"+name[0]); //PATH FOR INTELLIJ
                file = new File("previous_sessions/"+name[0]); //PATH FOR JAR
                boolean booly = file.delete(); //delete the file that already exists, we're gonna replace it
                //System.out.println("File Deleted: " + booly);
            }
            else{ //if they entered a new name, save it and throw ".csv" on the end
                file = new File("previous_sessions/" + name[0] + ".csv"); //PATH FOR JAR
                //file = new File("src/previous_sessions/" + name[0] + ".csv"); //PATH FOR INTELLIJ
            }
            boolean bool = file.createNewFile(); //make a new file with the given/chosen name
            System.out.println("File Created: " + bool);

            String url_ = "jdbc:mariadb://localhost:3306/mysql";
            String usr = "root";
            String pwd = "root";
            Connection myconn = DriverManager.getConnection(url_, usr, pwd);
            Statement stmt = myconn.createStatement();

            //get EVERYTHING from the database
            String sql = "SELECT * FROM Entry";
            String sql2 = "SELECT * FROM DP";
            String sql3 = "SELECT * FROM MatrixCell";
            String sql4 = "SELECT * FROM CN";
            String sql5 = "SELECT * FROM CNFRLink";
            ResultSet rs = stmt.executeQuery(sql);
            ResultSet rs2 = stmt.executeQuery(sql2);
            ResultSet rs3 = stmt.executeQuery(sql3);
            ResultSet rs4 = stmt.executeQuery(sql4);
            ResultSet rs5 = stmt.executeQuery(sql5);

            PrintWriter writer = new PrintWriter(file);

            int firstRow = 1; //1 means this is the first row and we need to put the column names
            while(rs.next()){ //ENTRY TABLE
                if(firstRow == 1){
                    writer.append("* \n"); // asterisk (*) indicates beginning of new table, helpful for reloading
                    writer.append("FR");
                    writer.append(",");
                    writer.append("DPID");
                    writer.append(",");
                    writer.append("displayNum");
                    writer.append(",");
                    writer.append("numChildren");
                    writer.append(",");
                    writer.append("parentID");
                    writer.append("\n");
                    writer.append(rs.getString("FR"));
                    writer.append(",");
                    writer.append(Integer.toString(rs.getInt("DPID")));
                    writer.append(",");
                    writer.append(rs.getString("displayNum"));
                    writer.append(",");
                    writer.append(Integer.toString(rs.getInt("numChildren")));
                    writer.append(",");
                    writer.append(Integer.toString(rs.getInt("parentID")));
                    writer.append("\n");
                    firstRow = 0; //0 means it's not the first row
                }
                else{ //if it's not the first row, just put in the data
                    writer.append(rs.getString("FR"));
                    writer.append(",");
                    writer.append(Integer.toString(rs.getInt("DPID")));
                    writer.append(",");
                    writer.append(rs.getString("displayNum"));
                    writer.append(",");
                    writer.append(Integer.toString(rs.getInt("numChildren")));
                    writer.append(",");
                    writer.append(Integer.toString(rs.getInt("parentID")));
                    writer.append("\n");
                }
            }

            firstRow = 1;
            while(rs2.next()){ //DP TABLE
                if(firstRow == 1){
                    writer.append("\n");
                    writer.append("* \n");
                    writer.append("DPID");
                    writer.append(",");
                    writer.append("DP");
                    writer.append(",");
                    writer.append("count");
                    writer.append(",");
                    writer.append("isPrimary");
                    writer.append("\n");
                    writer.append(Integer.toString(rs2.getInt("DPID")));
                    writer.append(",");
                    writer.append(rs2.getString("DP"));
                    writer.append(",");
                    writer.append(Integer.toString(rs2.getInt("count")));
                    writer.append(",");
                    writer.append(Integer.toString(rs2.getInt("isPrimary")));
                    writer.append("\n");
                    firstRow = 0;
                }
                else{
                    writer.append(Integer.toString(rs2.getInt("DPID")));
                    writer.append(",");
                    writer.append(rs2.getString("DP"));
                    writer.append(",");
                    writer.append(Integer.toString(rs2.getInt("count")));
                    writer.append(",");
                    writer.append(Integer.toString(rs2.getInt("isPrimary")));
                    writer.append("\n");
                }
            }

            firstRow = 1;
            while(rs3.next()){ //MATRIXCELL TABLE
                if(firstRow == 1){
                    writer.append("\n");
                    writer.append("* \n");
                    writer.append("matrixrow");
                    writer.append(",");
                    writer.append("matrixcolumn");
                    writer.append(",");
                    writer.append("DPIDfr");
                    writer.append(",");
                    writer.append("equation");
                    writer.append(",");
                    writer.append("DPCount");
                    writer.append(",");
                    writer.append("DPIDdp");
                    writer.append(",");
                    writer.append("symbol");
                    writer.append("\n");
                    writer.append(Integer.toString(rs3.getInt("matrixrow")));
                    writer.append(",");
                    writer.append(Integer.toString(rs3.getInt("matrixcolumn")));
                    writer.append(",");
                    writer.append(Integer.toString(rs3.getInt("DPIDfr")));
                    writer.append(",");
                    writer.append(rs3.getString("equation"));
                    writer.append(",");
                    writer.append(Integer.toString(rs3.getInt("DPcount")));
                    writer.append(",");
                    writer.append(Integer.toString(rs3.getInt("DPIDdp")));
                    writer.append(",");
                    writer.append(rs3.getString("symbol"));
                    writer.append("\n");
                    firstRow = 0;
                }
                else{
                    writer.append(Integer.toString(rs3.getInt("matrixrow")));
                    writer.append(",");
                    writer.append(Integer.toString(rs3.getInt("matrixcolumn")));
                    writer.append(",");
                    writer.append(Integer.toString(rs3.getInt("DPIDfr")));
                    writer.append(",");
                    writer.append(rs3.getString("equation"));
                    writer.append(",");
                    writer.append(Integer.toString(rs3.getInt("DPcount")));
                    writer.append(",");
                    writer.append(Integer.toString(rs3.getInt("DPIDdp")));
                    writer.append(",");
                    writer.append(rs3.getString("symbol"));
                    writer.append("\n");
                }
            }

            firstRow = 1;
            while(rs4.next()){ //CN TABLE
                if(firstRow == 1){
                    writer.append("\n");
                    writer.append("* \n");
                    writer.append("cn");
                    writer.append(",");
                    writer.append("displayID");
                    writer.append(",");
                    writer.append("parentID");
                    writer.append("\n");
                    writer.append(rs4.getString("cn"));
                    writer.append(",");
                    writer.append(rs4.getString("displayID"));
                    writer.append(",");
                    writer.append(rs4.getString("parentID"));
                    writer.append("\n");
                    firstRow = 0;
                }
                else{
                    writer.append(rs4.getString("cn"));
                    writer.append(",");
                    writer.append(rs4.getString("displayID"));
                    writer.append(",");
                    writer.append(rs4.getString("parentID"));
                    writer.append("\n");
                }
            }

            firstRow = 1;
            while(rs5.next()){ //CNFRLINK TABLE
                if(firstRow == 1){
                    writer.append("\n");
                    writer.append("* \n");
                    writer.append("DPID");
                    writer.append(",");
                    writer.append("CNdisplayID");
                    writer.append("\n");
                    writer.append(Integer.toString(rs5.getInt("DPID")));
                    writer.append(",");
                    writer.append(rs5.getString("CNdisplayID"));
                    writer.append("\n");
                    firstRow = 0;
                }
                else{
                    writer.append(Integer.toString(rs5.getInt("DPID")));
                    writer.append(",");
                    writer.append(rs5.getString("CNdisplayID"));
                    writer.append("\n");
                }
            }

            writer.flush();
            writer.close();
            myconn.close();

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Allows user to open a previously saved session back into the application
     */
    private void reloadSession(){
        //go to the directory where previous sessions are saved
        File dir = new File("previous_sessions"); //PATH FOR JAR
        //File dir = new File("src/previous_sessions"); //PATH FOR INTELLIJ
        String[] pathnames = dir.list(); //get a list of all the previously saved files
        Stage stage = new Stage();
        VBox dialogVbox = new VBox(20);
        Label lab = new Label("Please select the file you would like to reload.");
        ObservableList<String> tableList =  FXCollections.observableArrayList(pathnames); //display all files
        ListView<String> list = new ListView<>(tableList);
        Button butt = new Button("Done");
        dialogVbox.getChildren().addAll(lab, list, butt);
        Scene dialogScene = new Scene(dialogVbox, 400, 400);
        stage.setScene(dialogScene);
        if (!stage.isShowing()) {
            stage.show(); //show the popup window
        }

        butt.setOnAction(event-> {
            String chosenFile = list.getSelectionModel().getSelectedItem(); //get the selected file
            File[] temp = new File[1];
            if(chosenFile == null){ //throw an error if they didn't select a file
                String error = "Error: No file was selected to reload. Please select a file.";
                errorPopup(error);
            }
            else { //if they selected a file
                File reloadFile = new File(chosenFile); //save it so we can open and read it
                temp[0] = reloadFile;
                stage.close();
            }

            try {
                List<List<String>> records = new ArrayList<>();
                BufferedReader br = new BufferedReader(new FileReader("previous_sessions/"+temp[0])); //PATH FOR JAR
                //BufferedReader br = new BufferedReader(new FileReader("src/previous_sessions/"+temp[0])); //PATH FOR INTELLIJ
                String line;
                while ((line = br.readLine()) != null) { //for each line
                    String[] values = line.split(","); //save each line as an array of Strings where each string is a value
                    records.add(Arrays.asList(values)); //add the array to a list of arrays, where each array is a line of values from the CSV
                }

                //make sure the database is clean
                String drop1 = "DROP TABLE IF EXISTS Entry";
                String drop2 = "DROP TABLE IF EXISTS DP";
                String drop3 = "DROP TABLE IF EXISTS MatrixCell";
                String drop4 = "DROP TABLE IF EXISTS CN";
                String drop5 = "DROP TABLE IF EXISTS CNFRLink";

                String create1 = "CREATE TABLE Entry(FR mediumtext, DPID int(255), displayNum mediumtext, numChildren int(255), parentID int(255));";
                String create2 = "CREATE TABLE DP(DPID int(255), DP mediumtext, count int(255), isPrimary int(11));";
                String create3 = "CREATE TABLE MatrixCell(matrixrow int(255), matrixcolumn int(255), DPIDfr int(255), equation mediumtext, DPcount int(255), DPIDdp int(255), symbol char(1));";
                String create4 = "CREATE TABLE CN(cn mediumtext, displayID mediumtext, parentID mediumtext);";
                String create5 = "CREATE TABLE CNFRLink(DPID int(255), CNdisplayID mediumtext);";

                executeDatabaseU(drop1);
                executeDatabaseU(drop2);
                executeDatabaseU(drop3);
                executeDatabaseU(drop4);
                executeDatabaseU(drop5);

                executeDatabaseU(create1);
                executeDatabaseU(create2);
                executeDatabaseU(create3);
                executeDatabaseU(create4);
                executeDatabaseU(create5);

                int asterisk = 0; //changes to 1 when an asterisk is found, means next line is column titles. changes to 0 after the line with column titles is passed
                int table = 0; //counts what table we're on
                for(List<String> row : records){
                    if(row.get(0).equals("* ")){ //indicates new table, have to skip this row and the next one
                        asterisk = 1;
                        table++;
                    }
                    else if(asterisk == 1){ //means this row is just the column titles, skip it
                        asterisk = 0;
                    }
                    else if(row.size() == 1){ //just a new line character, skip it
                        //System.out.println("What is it?: " + row.get(0));
                    }
                    else{ //if it's a row of data
                        if(table == 1){ //ENTRY TABLE
                            //System.out.println("row.get(0): " + row.get(0));
                            String sql = "INSERT INTO ENTRY VALUES('" + row.get(0) + "', " + row.get(1) + ", '" + row.get(2) + "', " + row.get(3) + ", " + row.get(4) + ");";
                            executeDatabaseU(sql);
                        }
                        else if (table == 2){ //DP TABLE
                            String sql = "INSERT INTO DP VALUES(" + row.get(0) + ", '" + row.get(1)+ "', " + row.get(2)+ ", " + row.get(3) + ");";
                            executeDatabaseU(sql);
                        }
                        else if (table == 3){ //MATRIXCELL TABLE
                            String sql = "INSERT INTO MatrixCell VALUES(" + row.get(0) + ", " + row.get(1) + ", " + row.get(2) + ", '" + row.get(3) + "', " + row.get(4) + ", " + row.get(5) + ", '" + row.get(6) + "');";
                            executeDatabaseU(sql);
                        }
                        else if (table == 4){ //CN TABLE
                            String sql = "INSERT INTO CN VALUES('" + row.get(0) + "', '" + row.get(1) + "', '" + row.get(2) + "');";
                            executeDatabaseU(sql);
                        }
                        else if (table == 5){ //CNFRLINK TABLE
                            String sql = "INSERT INTO CNFRLink VALUES(" + row.get(0) + ", '" + row.get(0) + "');";
                            executeDatabaseU(sql);
                        }
                    }
                }

                //update the switches values so they don't clear the databases when the user goes to a new page for the first time
                Main.switchesCN = 2;
                Main.switchesMatrix = 2;
                refreshData(); //pull the Entry data from the database
                autoDPID = FRDP.length - 1;
                refreshCNData(); //pull the CN data from the database
                buildTree(); //build the tree
            }
            catch(Exception e){
                e.printStackTrace();
            }


    });
    }


    /**
     * Called from deleteRow() function to determine the row/column that the Entry being deleted is in in the Matrix
     * @param sql Query to be executed in database, query will ask for matrixrow/matrixcolumn of row with a certain DPID
     * @return row or column index of deleted Entry, or -1 if there is an error
     */
    private int getResult(String sql){
        String url_ = "jdbc:mariadb://localhost:3306/mysql";
        String usr = "root";
        String pwd = "root";
        try {
            Connection myconn = DriverManager.getConnection(url_, usr, pwd);
            Statement stmt = myconn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()){
                return rs.getInt(1);
            }
            else{
                return -1;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }


}

