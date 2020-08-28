package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sample.CN;
import sample.Entry;
import sample.Main;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;


/**
 * <h1>CN Controller Class</h1>
 * This class is the controller for the customer needs page of the application: a spreadsheet similar to the
 * home page where users can enter the customer needs that their FRs are based on. Here, users can link the CNs
 * with the FRs and trace to them
 *
 * @author Bridget McLean - bmclean@wpi.edu
 */
public class CNController extends ControllerClass implements Initializable {

    @FXML
    private AnchorPane anchor;

    private TreeTableView<CN> treetable = new TreeTableView<>();
    private TreeTableColumn<CN, String> treeColCN = new TreeTableColumn<>();
    private TreeTableColumn<CN, String> treeColID = new TreeTableColumn<>();
    private ObservableList<TreeItem<CN>> treeDataCN = FXCollections.observableArrayList(); //all items in the treeview

    private Button homeBttn = new Button();
    private Button traceBttn = new Button();


    /**
     * This function is called when the .fxml it's linked to is loaded. This initialize function is what makes this
     * class a controller
     *
     * @param url The location used to resolve relative paths for the root object
     * @param rb  The resources used to localize the root object
     */
    public void initialize(URL url, ResourceBundle rb){


///////////////////////////////////// Initialize UI Elements //////////////////////////////////////////
        anchor.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        anchor.setMinSize(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

        treetable.setLayoutX(166);
        treetable.setLayoutY(61);

        anchor.getChildren().add(treetable);
        AnchorPane.setTopAnchor(treetable, 30.0);
        AnchorPane.setLeftAnchor(treetable, 0.0);
        AnchorPane.setRightAnchor(treetable, 0.0);
        AnchorPane.setBottomAnchor(treetable, 0.0);

        homeBttn.setLayoutX(14);
        homeBttn.setLayoutY(1);
        homeBttn.setText("Home");
        homeBttn.setPrefSize(65, 10);

        traceBttn.setLayoutX(80);
        traceBttn.setLayoutY(1);
        traceBttn.setText("Trace");
        traceBttn.setPrefSize(65, 10);

        anchor.getChildren().addAll(homeBttn, traceBttn);

        treeColID.setPrefWidth(100);
        treeColID.setText("#");
        treeColCN.setPrefWidth(800);
        treeColCN.setText("Customer Needs");

        treetable.getColumns().add(treeColID);
        treetable.getColumns().add(treeColCN);


///////////////////////////////// Initialize/Pull from Database //////////////////////////////////////
        if(Main.switchesCN == 1){ //if this is our first time on the CN page
            CN cn0 = new CN(" ", "0", "-1"); //create the 0th CN object
            TreeItem<CN> cn0Tree = new TreeItem<>(cn0);
            treetable.setRoot(cn0Tree); //put it in the tree

            //delete database from previous session and fill in data for 0th CN
            String sql = "DROP TABLE IF EXISTS CN";
            String sql4 = "CREATE TABLE CN(cn mediumtext, displayID mediumtext, parentID mediumtext);";
            String sql2 = "INSERT INTO CN VALUES('" + cn0.getCn() + "', " + cn0.getDisplayID() + ", " + cn0.getParentID() + ");";
            String sql3 = "DROP TABLE IF EXISTS CNFRLink";
            String sql5 = "CREATE TABLE CNFRLink(DPID int(255), CNdisplayID mediumtext);";



            executeDatabaseU(sql);
            executeDatabaseU(sql4);
            executeDatabaseU(sql2);
            executeDatabaseU(sql3);
            executeDatabaseU(sql5);

            refreshData();
        }
        else{
            refreshData(); //get all the FR objects
            refreshCNData(); //get all the CN objects
            buildTree();
        }


//////////////////////////////// Display the text in the treeview and make it editable ////////////////////////////////
        //displays text in the displayID column
        treeColID.setCellValueFactory(param-> new SimpleStringProperty(param.getValue().getValue().getDisplayID())); //puts all the DPs in the DP column

        //displays text in the CN column
        treeColCN.setCellValueFactory(param-> new SimpleStringProperty(param.getValue().getValue().getCn()));

        treeColCN.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());

        //commits edits to CNs
        treeColCN.setOnEditCommit(event-> {
                CN oldCN = event.getRowValue().getValue(); //gets the CN object that is being edited
                oldCN.setCn(event.getNewValue()); //sets the new CN
                String temp = event.getNewValue();
                temp = temp.replace("'","''"); //stops error in database when user puts apostrophe
                //update the CN in the database
                String sql = "UPDATE CN SET cn = '" + temp + "' WHERE displayID = '" + oldCN.getDisplayID() + "';";
                executeDatabaseU(sql);
        });

        treetable.setEditable(true);


////////////////////////////// Set up right-click event and subsequent contextMenu /////////////////////////////////////////////////

        ContextMenu contextMenu = new ContextMenu(); //create the contextmenu


        MenuItem addRowItem = new MenuItem("Add Sibling"); //create menu item for adding a sibling
        addRowItem.setOnAction(event-> addNewSibling());

        MenuItem addChildItem = new MenuItem("Add Child"); //create menu item for adding a child
        addChildItem.setOnAction(event-> addNewChild());

        MenuItem deleteRowItem = new MenuItem("Delete Row"); //create menu item for deleting a row
        deleteRowItem.setOnAction(event-> deleteRow());

        MenuItem linkRowItem = new MenuItem("Link to FR"); //create menu item for linking a CN to an FR
        linkRowItem.setOnAction(event-> {
                CN clicked = treetable.getSelectionModel().getSelectedItem().getValue(); //get the CN that was clicked
                linkToFR(clicked);
        });

        contextMenu.getItems().addAll(addRowItem, addChildItem, deleteRowItem, linkRowItem); //add menu items to the contextmenu

        //closes the context menu when you click outside of it
        treetable.setOnMouseClicked(event->{
            if(event.getButton() == MouseButton.PRIMARY) {
                if (contextMenu.isShowing()) {
                    contextMenu.hide();
                }
            }
        });

        // When user right-clicks on the FRs or DPs
        treetable.setOnContextMenuRequested(event-> {
                TreeItem<CN> clicked = treetable.getSelectionModel().getSelectedItem(); //get the item that was clicked
                if (clicked.getValue().getDisplayID().equals("0")) { //can't add a sibling to or delete the 0 row
                    addRowItem.setDisable(true);
                    deleteRowItem.setDisable(true);
                } else {
                    addRowItem.setDisable(false);
                    deleteRowItem.setDisable(false);
                }

                contextMenu.show(treetable, event.getScreenX(), event.getScreenY()); //show the context menu at the X Y location of the right click
        });

///////////////////////////////////// Set up the buttons ////////////////////////////////////////////////

        homeBttn.setOnAction(event-> {
                try {
                    Main.switchesCN++;
                    goToHome(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });

        traceBttn.setOnAction(event-> {
                try {
                    //Main.switchesCN++;
                    traceToFR(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });



    } //end of initialize

    /**
     * Function called by home button event handler that changes the scene back to the spreadsheet/tree view
     * @param event fired by home button
     * @throws IOException ?
     */
    private void goToHome(ActionEvent event) throws IOException {

        //loads the scene
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/FXML/sample.fxml"));
        Parent P = loader.load();
        Scene s = new Scene(P, 1000, 500);

        //shows the scene
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(s);
        window.show();
    }


    /**
     * This function is called when the user presses the "Trace" button while a CN is selected, allowing the user to
     * trace the selected CN to the FRs it is linked to. If the CN is only linked to one FR, the user is automatically
     * traced to that FR. If the CN is linked to more than one FR, a popup appears with a list of all those linked
     * FRs, allowing the user to choose which FR they would like to trace to.
     * @param event When the user clicks the "Trace" button
     * @throws IOException ?
     */
    private void traceToFR(ActionEvent event) throws IOException {
        //NOTE: Don't get confused by my comments and my bad naming conventions. In the comments below,
        //  when I mention "CNList", I am referring to the CN[] array in an Entry object.
        //  the Entry.CNList only holds the CNs that have been linked to that Entry object
        //  when I mention "cnList", I am referring to the global CN[] array declared at the top of this class
        //  the cnList holds ALL the CN objects that have been created/displayed on the TreeTableView


        refreshData(); //refreshes the Entry object information from the database, fills the FRDP list
        refreshCNData(); //refreshes the CN object information from the database, fills the cnList

        if(treetable.getSelectionModel().getSelectedItem() == null) {
            String error = "Error: There is no CN selected. Please select the CN you would like to trace.";
            errorPopup(error);
        }
        else {
            String clickedCNID = treetable.getSelectionModel().getSelectedItem().getValue().getDisplayID(); //get the CN that is selected
            CN clickedCN = new CN("Dummy", "", "");
            //the CN we grab from the tree doesn't get updated by the matchCNFR() function, so it doesn't
            //  have any FRs in its FRList. To access the FRList for this CN, we have to use its ID to find the updated
            //  CN object in the cnList
            for (CN cn : this.cnList) {
                if (cn.getDisplayID().equals(clickedCNID)) {
                    clickedCN = cn;
                }
            }
            Entry entry;
            if (clickedCN.getFRList().length > 1) { //if the selected CN can be traced to more than one FR
                entry = pickFRPopup(clickedCN); //create a popup and have the user select which FR they'd like to trace to
                if(entry == null){
                    String error = "Error: No FR selected. Tracing could not be completed.";
                    errorPopup(error);
                    return;
                }
            } else if (clickedCN.getFRList().length == 1) {
                entry = clickedCN.getFRList()[0];
            } else {
                String error = "Error: The selected CN is not linked to any FRs.";
                errorPopup(error);
                return;
            }


            //load up the FR spreadsheet (home) page and focus on the selected FR
            Main.switchesCN++;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/FXML/sample.fxml"));
            Parent P = loader.load();
            Scene s = new Scene(P, 1000, 500);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(s);

            HomeController cont = loader.getController();
            cont.findFR0(entry.DPID); //call this function in the HomeController to focus on the FR

            window.show();
        }

    }

    //Call this function to link an FR to the clicked CN
    //Two labels: "Please pick an FR to link to this CN" -- possibly display the CN as well
                //"There are many FRs linked with this CN. Please choose which FR you'd like to view"

    /**
     * This function is called when the user selects the "Link to FR" option in the ContextMenu when you right click
     * on a CN. This allows the user to link the CN they right-clicked on to an FR. A popup appears with all the
     * FRs, allowing the user to select one. This link is saved in the CNFR database table.
     * @param cn the CN object that the user right clicked on to link an FR
     */
    private void linkToFR(CN cn){
        //Open a popup window with a list of all FRs and their displayNum
        Stage stage1 = new Stage();
        VBox dialogVbox = new VBox(20);
        TableView<Entry> list = new TableView<>();
        TableColumn<Entry, String> displayNumCol = new TableColumn<>("#");
        TableColumn<Entry, String> frCol = new TableColumn<>("FR");

        displayNumCol.setCellValueFactory(p-> new SimpleStringProperty(p.getValue().displayNum));
        frCol.setCellValueFactory(p-> new SimpleStringProperty(p.getValue().FR));

        list.getColumns().add(displayNumCol);
        list.getColumns().add(frCol);
        list.setPlaceholder(new Label("This CN has already been linked to all existing FRs"));

        ObservableList<Entry> obsEntry = FXCollections.observableArrayList(); //will hold FRs being displayed in the list



        //Only display unlinked FRs
        //First, the CN passed into this function doesn't have an updated FRList
        //Only the CNs in the cnList are updated
        for(CN cn0 : this.cnList){
            if(cn0.getDisplayID().equals(cn.getDisplayID())){ //find the CN in the updated list
                cn.setFRList(cn0.getFRList()); //get it's FRList
            }
        }
        //Now that we know the FRs that this CN is already linked to, we need to filter them out and make sure they're
        //->not displayed in the popup
        for(Entry ent : this.FRDP) { //so go through all the FRs
            boolean inList = false; //indicates if the ent that we're on is already linked to the CN (true) or not (false)
            if (cn.getFRList().length == 0) { //if the CN isn't linked to any FRs already
                obsEntry.add(ent); //then we can just add all the FRs to the display list
            }
            else { //if the CN is linked to some FRs
                for (Entry linkedEnt : cn.getFRList()) { //then for each linked FR
                    if (ent.DPID == linkedEnt.DPID) { //if this ent is already linked to the CN
                        inList = true; //change this to true
                        break; //we confirmed that the ent was already linked, so break out and start looking at the next ent in the FRDP list
                    }
                    else {
                        //do nothing
                        //if we were to add the ent to obsEntry here, then it would add it multiple times: once for
                        //-> every Entry in the FRList until the DPIDs match and it breaks out
                    }
                }
                if(!inList){ //if this is still false, it means the ent isn't linked and we can add it
                    obsEntry.add(ent); //put it outside the loop so it will only add the ent once: after it's already
                                       //gone through the FRList and confirmed it hasn't already been linked
                }
            }
        }

        list.getItems().addAll(obsEntry);

        Label lab = new Label("Please pick an FR to link to this CN");
        Button b = new Button("Submit");
        lab.setWrapText(true);
        lab.setMaxWidth(275);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.getChildren().addAll(lab, list, b); //add list to VBox

        b.setOnAction(event-> {
                if(list.getSelectionModel().getSelectedItem() == null){
                    String error = "Error: There is no FR selected. Please select the FR you would like to link to this CN.";
                    errorPopup(error);
                }
                else {
                    Entry entry = list.getSelectionModel().getSelectedItem();
                    String sql = "INSERT INTO CNFRLink VALUES(" + entry.DPID + ", '" + cn.getDisplayID() + "');";
                    executeDatabaseU(sql);
                    refreshData();
                    refreshCNData();
                    stage1.close();
                }
        });
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        stage1.setScene(dialogScene);
        if (!stage1.isShowing()) {
            stage1.show(); //show the popup window
        }
    }


    /**
     * Called by traceToFR() to open a popup with a list of FRs that the user can trace to. Records the user's selection
     * @param cn the CN that the user selected to trace from
     */
    private Entry pickFRPopup(CN cn){
        //open a popup with a list of FRs linked to the selected CN and have the user pick which one to trace to
        Stage stage = new Stage();
        VBox dialogVbox = new VBox(20);
        TableView<Entry> list = new TableView<>();
        TableColumn<Entry, String> displayNumCol = new TableColumn<>("#");
        TableColumn<Entry, String> frCol = new TableColumn<>("FR");
        frCol.setPrefWidth(250);

        displayNumCol.setCellValueFactory(p-> new SimpleStringProperty(p.getValue().displayNum));
        frCol.setCellValueFactory(p-> new SimpleStringProperty(p.getValue().FR));

        list.getColumns().add(displayNumCol);
        list.getColumns().add(frCol);

        ObservableList<Entry> obsEntry = FXCollections.observableArrayList(cn.getFRList());
        list.getItems().addAll(obsEntry);

        Label lab = new Label("There is more than one FR linked with this CN. Please choose which FR you'd like to view");
        Button b = new Button("Submit");
        lab.setWrapText(true);
        lab.setMaxWidth(300);
        list.setLayoutY(250);
        list.setPrefHeight(175);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.getChildren().addAll(lab, list, b); //add list to VBox

        Entry[] returnFR = new Entry[1];
        b.setOnAction(event-> {
            if(list.getSelectionModel().getSelectedItem() == null){
                String error = "Error: Please select the FR you would like to trace to.";
                errorPopup(error);
            }
            else {
                Entry entry = list.getSelectionModel().getSelectedItem();
                returnFR[0] = entry;
                stage.close();
            }
        });
        Scene dialogScene = new Scene(dialogVbox, 300, 300);
        stage.setScene(dialogScene);
        if (!stage.isShowing()) {
            stage.showAndWait(); //show the popup window
        }

        return returnFR[0];

    }


    /**
     * Adds a new row to the same level as the item that was right-clicked on
     */
    private void addNewSibling() {
        TreeItem<CN> clicked = treetable.getSelectionModel().getSelectedItem(); //get the item that was clicked
        TreeItem<CN> parent = clicked.getParent(); //find its parent
        String parentID = parent.getValue().getDisplayID(); //get ID of parent
        int numChild = parent.getChildren().size(); //find how many children parent has


        String newID;
        if (parentID.equals("0")) { //if the parent is CN0
            newID = Integer.toString(numChild + 1); //then the new row will be the number of children plus 1
        } else { //if the parent isn't CN0, then it follows a naming convention-> parentID.numChild+1
            //ex: parent is 1.1, already has 2 children, new child will be 1.1.3
            newID = parentID + "." + Integer.toString(numChild + 1);
        }

        CN cn = new CN(" ", newID, parentID); //make the new CN object
        parent.getChildren().add(new TreeItem<>(cn)); //add new blank row with new CN object
        parent.setExpanded(true);

        //update the database
        String sql = "INSERT INTO CN VALUES('" + cn.getCn() + "', '" + cn.getDisplayID() + "', '" + cn.getParentID() + "');";
        executeDatabaseU(sql);
        //refreshCNData(); //updates the CN objects so they match the database
        addCN(cnList, cn);
    }


    /**
     * Adds a new row to the level below the item that was right-clicked on
     */
    private void addNewChild() {
        TreeItem<CN> clicked = treetable.getSelectionModel().getSelectedItem(); //get the item that was clicked
        String clickedID = clicked.getValue().getDisplayID(); //get the ID of the item that was clicked
        String newID;
        int numChild = clicked.getChildren().size(); //find how many children the clicked item has
        if (clickedID.equals("0")) { //if the clicked item is CN0
            newID = Integer.toString(numChild + 1); //then the new row will be the number of children plus 1
        } else {//if the parent isn't CN0, then it follows a naming convention-> clickedID.numChild+1
            //ex: parent is 1.1, already has 2 children, new child will be 1.1.3
            newID = clickedID + "." + Integer.toString(numChild + 1);
        }
        CN cn = new CN(" ", newID, clickedID); //make the new CN object
        clicked.getChildren().add(new TreeItem<>(cn)); //add new blank row with new CN object
        clicked.setExpanded(true);

        //update the database
        String sql = "INSERT INTO CN VALUES('" + cn.getCn() + "', '" + cn.getDisplayID() + "', '" + cn.getParentID() + "');";
        executeDatabaseU(sql);
        //refreshCNData(); //updates the CN objects so they match the database
        addCN(cnList, cn);
    }

    /**
     * Removes the row that was right-clicked on (and all of its children)
     */
    private void deleteRow() {
        TreeItem<CN> clicked = treetable.getSelectionModel().getSelectedItem(); //get the item that was clicked
        TreeItem<CN> parent = clicked.getParent(); //get its parent


        //popup ensures no one accidentally deletes half their tree
        Label lab = new Label("Are you sure you want to delete this row and all its children?");
        Button delete = new Button("Yes");
        Button cancel = new Button("Cancel");
        Stage stage = new Stage();
        VBox box = new VBox(20);
        box.getChildren().addAll(lab, delete, cancel);
        Scene scene = new Scene(box, 300, 500);
        stage.setScene(scene);
        stage.show();


        cancel.setOnAction(event-> stage.close());


        delete.setOnAction(event-> {
                parent.getChildren().remove(clicked); //remove it (and all its children) from the tree

                //now we have to delete it and all its children from the database
                CN cn = clicked.getValue(); //get the CN item that was clicked

                CN[] delCNs = new CN[1]; //this array will hold all the CNs we have to delete
                delCNs[0] = cn; //throw the clicked entry object in there
                delCNs = recursiveDelete(delCNs, cn); //find all the children to delete

                for (int i = delCNs.length - 1; i >= 0; i--) { //go through delCNs backwards and delete the entries 1 by 1

                    CN c = delCNs[i];
                    String sql = "DELETE FROM CN WHERE displayID = '" + c.getDisplayID() + "';";
                    executeDatabaseU(sql);
                }

                recursiveDisplayNum(parent, parent.getValue().getDisplayID()); //change all the display numbers

                refreshData();
                refreshCNData(); //updates the CN objects so they match the database
                //matchCNFR();
                buildTree(); //rebuilds the tree with the new entry objects
                stage.close();
        });
    }

    /**
     * Recursively finds all children, grandchildren, etc. of the given CN. Called in the deleteRow() function
     * @param delCNs array of CN objects to be deleted
     * @param cn the CN object whose children we're looking for
     * @return the delCNs array with all the CNs to be deleted
     */
    private CN[] recursiveDelete(CN[] delCNs, CN cn) { //The given parent CN will already be in list
        //System.out.println(this.cnList.length);
        for (CN c : this.cnList) { //for each c in the CN list
            if (cn.getDisplayID().equals(c.getParentID())) { //if the given cn is the parent of c
                delCNs = recursiveDelete(delCNs, c); //find this CN's children
                //System.out.println("hello");
                delCNs = addCN(delCNs, c); //add this CN to the delete list
            }
        }
        return delCNs; //return the delete list
    }


    /**
     * Recursively changes the displayNum of the given TreeItem<CN> object and all its children. Called in the
     * deleteRow() function
     * @param parent the TreeItem<CN> object that needs its displayNum changed
     * @param pDisplayID the parent's new displayNum. The first time the function is called, pDisplayNum is just passed in
     * as parent.getValue().displayNum, because the parent of the deleted object will keep it's display number. In the
     * recursive calls after the first time, pDisplayNum is the newID variable
     */
    private void recursiveDisplayNum(TreeItem<CN> parent, String pDisplayID) {
        //I used the TreeItem instead of the CN object so I could get this list of children
        ObservableList<TreeItem<CN>> children = parent.getChildren();
        String sql;
        if (parent.getValue().getDisplayID().equals("0")) { //displayID is set differently for children of 0
            for (TreeItem<CN> cn : children) {
                int i = children.indexOf(cn);
                sql = "UPDATE CN SET displayID = '" + (i + 1) + "' WHERE displayID = '" + cn.getValue().getDisplayID() + "';";
                String sql2 = "UPDATE CNFRLink SET CNdisplayID = '" + (i + 1) + "' WHERE CNdisplayID = '" + cn.getValue().getDisplayID() + "';";
                executeDatabaseU(sql);
                executeDatabaseU(sql2);
                recursiveDisplayNum(cn, Integer.toString(i + 1));
            }
        } else {
            for (TreeItem<CN> cn : children) {
                int i = children.indexOf(cn);
                String newID = pDisplayID + "." + Integer.toString(i + 1);
                sql = "UPDATE CN SET displayID = '" + newID + "' WHERE displayID = '" + cn.getValue().getDisplayID() + "';";
                String sql2 = "UPDATE CN SET parentID = '" + pDisplayID + "' WHERE displayID = '" + newID + "';";
                String sql3 = "UPDATE CNFRLink SET CNdisplayID = '" + newID + "' WHERE CNdisplayID = '" + cn.getValue().getDisplayID() + "';";

                executeDatabaseU(sql);
                executeDatabaseU(sql2);
                executeDatabaseU(sql3);
                recursiveDisplayNum(cn, newID);
            }
        }
    }

    /**
     * Adds the given CN to the end of the given array of entries
     * @param cnList array of CN objects
     * @param cn CN to add to given array of CNs
     * @return the new array with the given CN object appended to the end
     */
    private CN[] addCN(CN[] cnList, CN cn) {
        int len = cnList.length;
        CN[] temp = new CN[len + 1];
        System.arraycopy(cnList, 0, temp, 0, len);
        temp[len] = cn;
        return temp;
    }

    /**
     * Uses the cnList list and pulls from the database to turn the CN objects into TreeItems and build the TreeView
     */
    private void buildTree() {

        if (treeDataCN.size() != 0) {
            treeDataCN.clear();
        }

        String url_ = "jdbc:mariadb://localhost:3306/mysql";
        String usr = "root";
        String pwd = "root";
        try {
            Connection myconn = DriverManager.getConnection(url_, usr, pwd);
            Statement stmt = myconn.createStatement();


            for (CN cn : this.cnList) {
                treeDataCN.add(new TreeItem<>(cn)); //turn all the entries into treeitems
            }

            //build the tree
            for (TreeItem<CN> cn : treeDataCN) { //for each entry object
                String get = "SELECT * FROM CN WHERE parentID = '" + cn.getValue().getDisplayID() + "';"; //find its children
                ResultSet res = stmt.executeQuery(get);

                if (cn.getValue().getDisplayID().equals("0")) { //when it finds FR0
                    treetable.setRoot(cn); //set it at the top
                }

                //we have all the children in the ResultSet, but we need to associate them-->
                // -->to the treeitem that was already made for it and save it somewhere
                while (res.next()) { //so for each child that we found in the database
                    for (TreeItem<CN> treeitem : treeDataCN) { //go through our list of tree items
                        if (res.getString("displayID").equals(treeitem.getValue().getDisplayID())) { //find a tree item with the DPID that matches the DPID of the child in the database
                            cn.getChildren().add(treeitem); //add all children to the parent
                            cn.setExpanded(true);
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
     * Called from the HomeController when the user wants to trace an FR to a CN on this page
     * @param displayID the displayID of the CN they are tracing
     */
    void findCN0(String displayID){
        CN cn = new CN("BAD", "", "");
        TreeItem<CN> cn0 = new TreeItem<>(cn); //dummy value for the first tree item
        for(TreeItem<CN> cnloop : treeDataCN){
            if(cnloop.getValue().getDisplayID().equals("0")){ //find the actual object for the first item
                cn0 = cnloop; //save it
                break;
            }
        }
        //We needed to get first item to start at the top of the tree to recursively search through children
        //and find the row number of the CN we are tracing. In the TreeTableView, a parent is always followed by
        //its children, so we need to start at the top and search through the tree BFS style to get the row number
        int row = recursiveFindCN(cn0, 0, displayID); //pass that root tree item into the recursive function to find what row we want to focus on
        //System.out.println("row: " + row);
        treetable.getFocusModel().focus(row, treeColCN); //focus on that row
        treetable.scrollTo(row); //scroll to it
    }

    /**
     * Called by the findCN0() function to recursively search through the TreeTableView using Depth First Search
     * to find the row number of the CN we are tracing to.
     * @param parent the TreeItem<CN> object whose children we need to search
     * @param row the row number of the parent object we passed in
     * @param displayID the ID of the TreeItem<CN> object we are searching for
     * @return the row number of the TreeItem<CN> object we are searching for
     */
    private int recursiveFindCN(TreeItem<CN> parent, int row, String displayID){
        ObservableList<TreeItem<CN>> children = parent.getChildren(); //get all the children of the given parent

        if(displayID.equals("0")){ //if we're tracing to the first row, just go there
            return row;
        }
        for(TreeItem<CN> cn : children){
            row++; //every time we look at a new child, it's another row down
            if(cn.getValue().getDisplayID().equals(displayID)){ //is this the FR we're tracing to?
                return row; //if yes, return its row number
            }
            else{
                row = recursiveFindCN(cn, row, displayID); //if not, look at this one's children
            }
        }
        return row;

    }

}
