package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
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
        //rootPane.setPrefSize(400, 600);

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

        rootPane.getChildren().addAll(matrixButton, cnButton, traceBttn); //add button to AnchorPane

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
            DP dp0 = new DP(" ", 0, getAutoDPID(), 1); //make a fresh dp
            DP[] dpList0 = new DP[1]; //make a fresh dpList
            dpList0[0] = dp0; //put the dp in the dpList
            Entry FRDP0ent = new Entry(" ", dpList0, "0", autoDPID, 0, -1); //make a fresh entry object
            TreeItem<Entry> FRDP0 = new TreeItem<>(FRDP0ent); //turn it into a tree item
            Entry[] temp = new Entry[1]; //extend the FRDP list (it's initialized to 0 because the Entry's are dynamically added from the database when the scene reopens)
            FRDP = temp;
            FRDP[0] = FRDP0ent; //add the Entry object to the FRDP list
            mytreetable.setRoot(FRDP0); //puts the tree item object at the top

            String sql = "DELETE FROM Entry;"; //delete everything from the previous session in the database
            String sql2 = "DELETE FROM DP;";

            //add the 0 row to the database
            String sql3 = "INSERT INTO Entry VALUES('" + FRDP0ent.FR + "', " + FRDP0ent.DPID + ", '" + FRDP0ent.displayNum + "', " + FRDP0ent.numChildren + ", " + FRDP0ent.parentID + ");";
            String sql4 = "INSERT INTO DP VALUES(" + dp0.getDPId() + ", '" + dp0.getDp() + "', " + dp0.getCount() + ", " + dp0.getIsPrimary() + ");";

            executeDatabaseU(sql);
            executeDatabaseU(sql2);
            executeDatabaseU(sql3);
            executeDatabaseU(sql4);


        } else { //if we've already been to the homescreen, have to populate it from database

            refreshData(); //pulls data from the database and puts it all in the FRDP array
            autoDPID = FRDP.length - 1; //make sure the autoDPID is right
            buildTree(); //put all the Entry objects into the treeview
        }


//////////////////////////////// Display the text in the treeview and make it editable ////////////////////////////////

        //displays text in the FR column
        treeColFR.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Entry, String> param) {
                return new SimpleStringProperty(param.getValue().getValue().getFR());
            }
        });

        //displays text in the DP column
        treeColDP.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Entry, String> param) {
                return new SimpleStringProperty(param.getValue().getValue().getDP()[0].getDp()); //puts all the DPs in the DP column
            }
        });

        //displays text in the ID column
        treeColID.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Entry, String> param) {
                return new SimpleStringProperty(param.getValue().getValue().getDisplayNum()); //puts all the DPs in the DP column
            }
        });


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
        // --> this way, I can tell when they select "add new DP...", and set off all the events to make the popup
        //TODO: Remove DPs, Edit DPs, set primary DP checkbox.
        treeColDP.setCellFactory(tc -> {
            ComboBox<String> combo = new ComboBox<>(); //make the combobox
            combo.setMaxWidth(1.7976931348623157E308); //this number is equal to max width of the cell
            TreeTableCell<Entry, String> cell = new TreeTableCell<Entry, String>() { //make the new cell
                @Override
                protected void updateItem(String item, boolean empty) {//item is the string being shown in the cell, empty is if it's empty or not
                    super.updateItem(item, empty); //inherit everything from the original updateItem function

                    if (empty) {
                        setGraphic(null);
                    }
                    else {
                        Entry entry = this.getTreeTableRow().getItem(); //get the current row item

                        if (entry == null) {
                            System.out.println("The selected item is null");
                        }
                        else {
                            System.out.println("entry.displayNum: " + entry.displayNum);
                            ObservableList<String> allDPList = FXCollections.observableArrayList(getList(entry.getDP(), false)); //get an observable list of all the DP strings for this FR
                            combo.getItems().clear(); //make sure nothing is in the combobox
                            combo.getItems().addAll(allDPList); //add all the DPs to the combobox
                            //combo.getSelectionModel().select(2); //preview the 3rd item in the combobox (skips "Add DP" and "Change DP")
                            setGraphic(combo); //sets the cell's graphic as the combo box

//                        for (int i = 0; i < entry.getDP().length; i++) {
//                            if (entry.getDP()[i].getIsPrimary() == 1) {
//                                combo.setValue(entry.getDP()[i].getDp()); //display the dp marked as primary
//                            }
//                        }
                            combo.setValue(entry.getPrimaryDP().getDp());

                            combo.valueProperty().addListener(new ChangeListener<String>() {
                                //t is what the combobox was on, t1 is what the combobox is being changed to
                                @Override
                                public void changed(ObservableValue ov, String t, String t1) {
                                    if (t1 == null || t == null) {
                                    } else if (t1.equals("Add New DP...")) { //if the user wants to add a new DP
                                        VBox dialogVbox = new VBox(20);
                                        dialogVbox.setAlignment(Pos.CENTER);
                                        TextField txt = new TextField("Enter new DP..."); //create textfield to enter new DP
                                        Button butt = new Button(); //create the submit button to close/go back
                                        butt.setText("Submit");

                                        dialogVbox.getChildren().add(txt); //add textfield to VBox
                                        dialogVbox.getChildren().add(butt); //add button to VBox
                                        Scene dialogScene = new Scene(dialogVbox, 300, 150);
                                        dialog.setScene(dialogScene);
                                        if (!dialog.isShowing()) {
                                            dialog.show(); //show the popup window
                                        }

                                        butt.setOnAction(new EventHandler<ActionEvent>() { //when they submit their new DP
                                            @Override
                                            public void handle(ActionEvent event) {
                                                try {
                                                    String newDP = txt.getText(); //get the text they entered

                                                    if (entry.getDP()[0].getDp().equals(" ")) { //if this is the first DP
                                                        entry.getDP()[0].setDp(newDP); //update the DP list in the Entry object (right now it's "")
                                                        combo.getItems().remove(" "); //Remove the blank option from the combobox
                                                        updateDP(entry, newDP); //update the dp in the database

                                                    } else {
                                                        entry.addDP(newDP);//update the DP list in the Entry object
                                                    }

                                                    combo.getItems().add(newDP); //add the new DP to the combobox
                                                    combo.setValue(newDP); //set it to the display value
                                                    dialog.close(); //close the popup window

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        });

                                    } else if (t1.equals("Change DP...")) { //if the user wants to change a dp
                                        VBox dialogVbox2 = new VBox(20);
                                        dialogVbox2.setAlignment(Pos.CENTER);
                                        ComboBox dpCombo = new ComboBox(); //make a combobox with all the dps in it
                                        dpCombo.getItems().addAll(FXCollections.observableArrayList(getList(entry.getDP(), true)));
                                        dialogVbox2.getChildren().add(dpCombo);

                                        TextField txt2 = new TextField(""); //create textfield to enter changed DP
                                        Button butt2 = new Button(); //create the submit button to close/go back
                                        butt2.setText("Submit");

                                        dpCombo.valueProperty().addListener(new ChangeListener<String>() { //listens for the user selecting a dp from the combobox
                                            @Override
                                            public void changed(ObservableValue ov, String t, String t1) {
                                                txt2.setText(t1); //and puts that dp in the textfield so they can edit it
                                            }
                                        });

                                        //first update the combobox, then update the Entry object
                                        butt2.setOnAction(new EventHandler<ActionEvent>() { //when they submit their edited DP
                                            @Override
                                            public void handle(ActionEvent event) {
                                                try {
                                                    String changeDP = txt2.getText();
                                                    //this will change the placement of the DP being edited
                                                    //if this is a problem I can try to change later
                                                    combo.getItems().remove(dpCombo.getValue()); //remove the old value from the combobox
                                                    combo.getItems().add(changeDP); //add the edited value

                                                    dpCombo.getItems().remove(t1); //remove the old value from the dp combobox
                                                    dpCombo.getItems().add(changeDP); //add the new value to the dp combobox

                                                    for (int i = 0; i < entry.getDP().length; i++) {
                                                        if (entry.getDP()[i].getDp().equals(dpCombo.getValue())) {
                                                            entry.getDP()[i].setDp(changeDP);
                                                            entry.getDP()[i].setIsPrimary(1);
                                                        } else if (entry.getDP()[i].getIsPrimary() == 1) {
                                                            entry.getDP()[i].setIsPrimary(0);
                                                        }
                                                    }
                                                    updateDP(entry, changeDP); //update the dp in the database
                                                    combo.setValue(changeDP); //set it to the display value
                                                    dialog.close(); //close the popup window
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                        dialogVbox2.getChildren().addAll(txt2, butt2);
                                        Scene scene = new Scene(dialogVbox2, 300, 150);
                                        dialog.setScene(scene);

                                        if (!dialog.isShowing()) {
                                            dialog.show(); //show the popup window
                                        }

                                    } else { //if they're switching from one DP to another
                                        for (int i = 0; i < entry.getDP().length; i++) {
                                            if (entry.getDP()[i].getDp().equals(t)) {
                                                //System.out.println(entry.getDP()[i].getIsPrimary());
                                                entry.getDP()[i].setIsPrimary(0);
                                            } else if (entry.getDP()[i].getDp().equals(t1)) {
                                                entry.getDP()[i].setIsPrimary(1);
                                                String sql = "UPDATE DP SET isPrimary = 0 WHERE isPrimary = 1 AND DPID = " + entry.getDP()[i].getDPId() + ";";
                                                String sql2 = "UPDATE DP SET isPrimary = 1 WHERE DPID = " + entry.getDP()[i].getDPId() + " AND COUNT = " + entry.getDP()[i].getCount() + ";";
                                                executeDatabaseU(sql);
                                                executeDatabaseU(sql2);


                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            };
            return cell; //return the new cell
        });

        //commits edits to FRs
        treeColFR.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Entry, String>>() {
            @Override
            public void handle(final TreeTableColumn.CellEditEvent<Entry, String> event) {
                Entry oldFR = event.getRowValue().getValue(); //gets the Entry object that is being edited
                oldFR.setFR(event.getNewValue()); //sets the new FR

                //update the FR in the database
                String sql = "UPDATE Entry SET FR = '" + event.getNewValue() + "' WHERE DPID = " + oldFR.DPID + ";";
                executeDatabaseU(sql);

            }
        });


        mytreetable.setEditable(true);

////////////////////////////// Set up right-click event and subsequent contextMenu /////////////////////////////////////////////////

        //TODO: When you click outside the context menu, it disappears
        ContextMenu contextMenu = new ContextMenu(); //create the contextmenu


        MenuItem addRowItem = new MenuItem("Add Row"); //create menu item for adding a row
        addRowItem.setOnAction(new EventHandler<ActionEvent>() { //link the menu item to the addNewSibling() function

            @Override
            public void handle(ActionEvent event) {
                addNewSibling();
            }
        });


        MenuItem addChildItem = new MenuItem("Add Child"); //create menu item for adding a child
        addChildItem.setOnAction(new EventHandler<ActionEvent>() {//link the menu item to the addNewChild() function

            @Override
            public void handle(ActionEvent event) {
                addNewChild();
            }
        });

        MenuItem deleteRowItem = new MenuItem("Delete Row"); //create menu item for deleting a row
        deleteRowItem.setOnAction(new EventHandler<ActionEvent>() {//link the menu item to the addNewChild() function

            @Override
            public void handle(ActionEvent event) {
                deleteRow();
            }
        });


        contextMenu.getItems().addAll(addRowItem, addChildItem, deleteRowItem); //add menu items to the contextmenu

        // When user right-clicks on the FRs or DPs
        mytreetable.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

            @Override
            public void handle(ContextMenuEvent event) {
                TreeItem<Entry> clicked = mytreetable.getSelectionModel().getSelectedItem(); //get the item that was clicked
                if (clicked.getValue().displayNum.equals("0")) { //can't add a sibling to or delete the 0 row
                    addRowItem.setDisable(true);
                    deleteRowItem.setDisable(true);
                } else {
                    addRowItem.setDisable(false);
                    deleteRowItem.setDisable(false);
                }

                contextMenu.show(mytreetable, event.getScreenX(), event.getScreenY()); //show the context menu at the X Y location of the right click
            }
        });

///////////////////////////////////// Set up the buttons ////////////////////////////////////////////////

        matrixButton.setOnAction(new EventHandler<ActionEvent>() {//on button click, go to matrix scene
            @Override
            public void handle(ActionEvent event) {
                try {
                    Main.switchesMatrix++;
                    goToMatrix(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        cnButton.setOnAction(new EventHandler<ActionEvent>() {//on button click, go to CN scene
            @Override
            public void handle(ActionEvent event) {
                try {
                    Main.switchesCN++;
                    goToCN(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        traceBttn.setOnAction(new EventHandler<ActionEvent>() {//on button click, go to home scene
            @Override
            public void handle(ActionEvent event) {
                try {
                    //Main.switchesCN++;
                    traceToCN(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
        box.getChildren().addAll(lab, delete, cancel);
        Scene scene = new Scene(box, 300, 200);
        stage.setScene(scene);
        stage.show();


        cancel.setOnAction(new EventHandler<ActionEvent>() { //if they cancel
            @Override
            public void handle(ActionEvent event) {
                stage.close();
            }
        });


        delete.setOnAction(new EventHandler<ActionEvent>() { //when they submit their edited DP
            @Override
            public void handle(ActionEvent event) {
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

                Arrays.sort(delEnts, new Comparator<Entry>() {
                    @Override
                    public int compare(Entry o1, Entry o2) {
                        return Integer.compare(o1.DPID, o2.DPID);
                    }
                });

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

                    executeDatabaseU(sql);
                    executeDatabaseU(sql2);
                    executeDatabaseU(sql7);
                    executeDatabaseU(sql5);
                    executeDatabaseU(sql3);
                    executeDatabaseU(sql4);
                    executeDatabaseU(sql8);
                }

                recursiveDisplayNum(parent, parent.getValue().displayNum);

                refreshData(); //updates the entry objects so they match the database
                buildTree(); //rebuilds the tree with the new entry objects
                stage.close();
            }
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
        DP newDP = new DP(" ", getAutoDPID(), 0, 1);
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

    //pDisplayNum is the parent's new displayNum. The first time the function is called, pDisplayNum is just passed in
    // as parent.getValue().displayNum, because the parent of the deleted object will keep it's display number. In the
    //recursive calls after the first time, pDisplayNum is the newID variable

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
        String sql = "";
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
        System.out.println("about to load");
        loader.setLocation(getClass().getResource("../FXML/rand.fxml"));
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
        //System.out.println("about to load");
        loader.setLocation(getClass().getResource("../FXML/CustomerNeeds.fxml"));
        //loader.setController(MatrixController.getMatrixInstance());
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

        //int clickedEntID = mytreetable.getSelectionModel().getSelectedItem().getValue().DPID; //get the CN that is selected
        //Entry clickedEnt = new Entry("Dummy", new DP[0], "", -1, -1, -1);
        if(mytreetable.getSelectionModel().getSelectedItem() == null) {
            Stage stage = new Stage();
            VBox dialogVbox = new VBox(20);
            dialogVbox.setAlignment(Pos.CENTER);
            Label lab = new Label("Error: There is no FR selected. Please select the FR you would like to trace.");
            Button b = new Button("OK");
            lab.setWrapText(true);
            lab.setMaxWidth(275);
            dialogVbox.setAlignment(Pos.CENTER);
            dialogVbox.getChildren().addAll(lab, b);
            b.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    stage.close();
                }
            });

            Scene dialogScene = new Scene(dialogVbox, 300, 200);
            stage.setScene(dialogScene);
            stage.show();
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
                    Stage stage2 = new Stage();
                    VBox dialogVbox2 = new VBox(20);
                    Label lab2 = new Label("Error: No CN selected. Tracing could not be completed.");
                    lab2.setMaxWidth(275);
                    lab2.setWrapText(true);
                    dialogVbox2.setAlignment(Pos.CENTER);
                    Button b2 = new Button("OK");
                    dialogVbox2.getChildren().addAll(lab2, b2);
                    b2.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            stage2.close();
                        }
                    });

                    Scene dialogScene = new Scene(dialogVbox2, 300, 200);
                    stage2.setScene(dialogScene);
                    stage2.show();
                    return;
                }
            }
            else if (clickedEnt.getCNList().length == 1) { //if the selected FR can only be traced to 1 CN
                cn = clickedEnt.getCNList()[0]; //grab that CN
            }
            else { //if there are no CNs linked to this FR, tell the user to link it to CNs
                Stage stage = new Stage();
                VBox dialogVbox = new VBox(20);
                Label lab = new Label("Error: The selected FR is not linked to any CNs.");
                Button b = new Button("OK");
                lab.setWrapText(true);
                lab.setMaxWidth(275);
                dialogVbox.setAlignment(Pos.CENTER);
                dialogVbox.getChildren().addAll(lab, b);
                b.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        stage.close();
                    }
                });

                Scene dialogScene = new Scene(dialogVbox, 300, 200);
                stage.setScene(dialogScene);
                stage.show();
                return;
            }

            //load up the FR spreadsheet (home) page and focus on the selected FR
            Main.switchesCN++;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../FXML/CustomerNeeds.fxml"));
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
     * @throws IOException
     */
    private CN pickCNPopup(Entry entry) throws IOException{
        //open a popup with a list of CNs linked to the selected FR and have the user pick which one to trace to
        Stage stage = new Stage();
        VBox dialogVbox = new VBox(20);
        TableView<CN> list = new TableView<>();
        TableColumn<CN, String> displayNumCol = new TableColumn<>("#");
        TableColumn<CN, String> frCol = new TableColumn<>("CN");
        frCol.setPrefWidth(250);

        displayNumCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CN, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<CN, String> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                return new SimpleStringProperty(p.getValue().getDisplayID());
            }
        });

        frCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<CN, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<CN, String> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                return new SimpleStringProperty(p.getValue().getCn());
            }
        });

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
        b.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                if(list.getSelectionModel().getSelectedItem() == null){
                    Stage stage2 = new Stage();
                    VBox dialogVbox2 = new VBox(20);
                    Label lab2 = new Label("Error: Please select the CN you would like to trace to.");
                    lab2.setMaxWidth(275);
                    lab2.setWrapText(true);
                    dialogVbox2.setAlignment(Pos.CENTER);
                    Button b2 = new Button("OK");
                    dialogVbox2.getChildren().addAll(lab2, b2);
                    b2.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            stage2.close();
                        }
                    });

                    Scene dialogScene = new Scene(dialogVbox2, 300, 200);
                    stage2.setScene(dialogScene);
                    stage2.show();
                }
                else {
                    CN cn = list.getSelectionModel().getSelectedItem();
                    returnCN[0] = cn;
                    stage.close();
                }

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
    public void findFR0(int DPID){
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
        //System.out.println("HC row: " + row);
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
    public int[] recursiveFindFR(TreeItem<Entry> parent, int[] row, int DPID){
        ObservableList<TreeItem<Entry>> children = parent.getChildren(); //get all the children of the given parent

        System.out.println(parent.getValue().DPID);
        System.out.println(row[1]);

        if(DPID == 0){ //if we're tracing to the first row, just go there
            row[0] = 1;
            return row;
        }
        for(TreeItem<Entry> entry : children){
            System.out.println("Looking at " + parent.getValue().displayNum + "'s child: " + entry.getValue().DPID);
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
            String[] dpString = new String[len + 2];
            dpString[0] = "Add New DP...";
            dpString[1] = "Change DP...";
            for (int i = 0; i < len; i++) {
                dpString[i + 2] = dpList[i].getDp();
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
     * Updates the database with a change made to a DP by the user
     *
     * @param entry    the entry object in the row that was edited
     * @param changeDP the string that holds the new value that the dp is being changed to
     */

    private void updateDP(Entry entry, String changeDP) {
        String sql = "UPDATE DP SET DP = '" + changeDP + "' WHERE DPID = " + entry.DPID + " AND count = " + entry.getDP()[0].getCount();
        String sql3 = "UPDATE DP SET isPrimary = 1 WHERE DPID = " + entry.DPID + " AND count = " + entry.getDP()[0].getCount();
        String sql2 = "UPDATE DP SET isPrimary = 0 WHERE isPrimary = 1 AND DPID = " + entry.getDPID() + ";";

        //the order that these are executed matters (I think)
        executeDatabaseU(sql2);
        executeDatabaseU(sql);
        executeDatabaseU(sql3);


    }

}

