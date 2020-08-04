package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;
import sample.DP;
import sample.Entry;
import sample.Main;
import sample.TooltippedTableCell;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

/**
 * <h1>Matrix Controller Class</h1>
 * This class is the controller for the "matrix" page of the application: the grid where users can set equations
 * and see relationships between all their DPs and FRs
 *
 * @author Bridget McLean - bmclean@wpi.edu
 */
public class MatrixController extends ControllerClass implements Initializable {

    @FXML
    private AnchorPane root;

    @FXML
    private GridPane grid;

    @FXML
    private ScrollPane scroll;

    @FXML
    private Button homeButton;

    //@FXML
    private TableView<Entry> table = new TableView<>();

    //private Entry[] ents = new Entry[0];
    private Entry[] orgEnts = new Entry[1];




    //this gets called when the FXML gets loaded
    /**
     * This function is called when the .fxml it's linked to is loaded. This initialize function is what makes this
     * class a controller
     *
     * @param url The location used to resolve relative paths for the root object
     * @param rb  The resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //reload from MatrixCell database if switches >= 1 here
        if(Main.switchesMatrix == 1){ //if this is the first time on this view,
            String sql = "DELETE FROM MatrixCell;"; //get rid of the data in the database from last time
            executeDatabaseU(sql);
        }

///////////////////////////////// Initialize UI elements, grab and organize data from database //////////////////

        table.setLayoutX(720.0);
        table.setLayoutY(-1);

        AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setRightAnchor(table, 0.0);
        AnchorPane.setTopAnchor(table, 0.0);

        root.getChildren().add(table);

        //handle action for home button
        homeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Main.switchesMatrix++;
                    goToHome(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        refreshData(); //pull from the database
        orgEnts[0] = FRDP[0]; //FRDP inherited from ControllerClass, FRDP contains all the Entry objects (unordered)
        organizeEntries(FRDP[0]); //organize the entries so each entry's children follow it in the list


/////////////////////////////////////////////// Build the matrix ///////////////////////////////////////////////////

        ObservableList<Entry> obsEntry = FXCollections.observableArrayList(orgEnts);

        for(int y = 0; y < orgEnts.length+1; y++){ //for each DP (Row)
            for(int x = 0; x < orgEnts.length+1; x++){ //and for each FR (Column)

                //FIRST COLUMN
                if(x==0 && y !=0){
                    Label lab = new Label();
                    lab.setText("DP" + orgEnts[y-1].displayNum); //set the labels for the DPs

                    grid.setRowIndex(lab, y);
                    grid.setColumnIndex(lab, x);
                    grid.getChildren().add(lab);
                }

                //FIRST ROW
                else if(y == 0 && x != 0){ //if it's the first ROW
                    Label lab = new Label();
                    lab.setText("FR" + orgEnts[x-1].displayNum);
                    grid.setRowIndex(lab, y);
                    grid.setColumnIndex(lab, x);
                    grid.getChildren().add(lab);

                }

                //TOP LEFT CORNER
                else if(x==0 && y==0){ //if it's the lop left corner
                    Label lab = new Label(); //do nothing
                }

                //THE REST OF THE MATRIX
                else { //if it's actually a cell in the matrix
                    TextField tf = new TextField(); //each cell is a textfield
                    tf.setPrefHeight(50);
                    tf.setPrefWidth(50);
                    tf.setAlignment(Pos.CENTER);
                    tf.setEditable(false);

                    //FIRST TIME GOING TO MATRIX - INITIALIZE
                    if(Main.switchesMatrix == 1){
                        //every cell automatically gets a row in the database
                        String sql = "INSERT INTO MatrixCell VALUES(" + y + ", " + x + ", " + orgEnts[x-1].DPID + ", " + "' '" + ", " + orgEnts[y-1].getPrimaryDP().getCount() + ", " + orgEnts[y-1].DPID + ", " + " 'O');";
                        tf.setFont(Font.font("Verdana", FontPosture.ITALIC, 12)); //italic when equation hasn't been set (this is initialization so no equations have been set yet)
                        //executeDatabaseU(sql);

                        if(x==y){ //if the cell is along the diagonal, we can assume the DP already influences the FR
                            tf.setText("X"); //so it will have an X
                            sql = "INSERT INTO MatrixCell VALUES(" + y + ", " + x + ", " + orgEnts[x-1].DPID + ", " + "' '" + ", " + orgEnts[y-1].getPrimaryDP().getCount() + ", " + orgEnts[y-1].DPID + ", " + " 'X');";
                        }
                        else{
                            tf.setText("O");
                        }

                        executeDatabaseU(sql);
                    }

                    //RETURNING TO THE MATRIX
                    else{ //if we're returning to this page, there might be saved data we have to reload in from the database
                        //so get the data that's saved for this cell (if any)
                        String sql = "SELECT * FROM MatrixCell WHERE matrixrow = " + y + " AND matrixcolumn = " + x + ";";
                        String url_ = "jdbc:mariadb://localhost:3306/mysql";
                        String usr = "root";
                        String pwd = "root";
                        try {
                            Connection myconn = DriverManager.getConnection(url_, usr, pwd);
                            Statement stmt = myconn.createStatement();

                            ResultSet rs = stmt.executeQuery(sql); //only returns one row (or nothing)


                            //CELL IS IN DATABASE
                            if(rs.next()){ //if there's data saved for this cell, initialize it with those settings
                                if(rs.getString("equation").equals(" ")){ //if an equation HASN'T been set for this cell
                                    tf.setFont(Font.font("Verdana", FontPosture.ITALIC, 12)); //make it italic
                                }
                                else{ //if an equation HAS been set for this cell
                                    tf.setFont(Font.font("Verdana", FontWeight.BOLD, 12)); //make it bold
                                }
                                tf.setText(rs.getString("symbol")); //set the text as the symbol saved in the database
                            }


                            //CELL IS NOT IN DATABASE
                            //TODO: add this cell to the database
                            else{ //if there's no data saved for this cell (AKA, if the FR/DP have been added since the last time the user visited the matrix)
                                tf.setFont(Font.font("Verdana", FontPosture.ITALIC, 12)); //there's no equation saved so the font is italic
                                if(x==y){ //if it's a primary DP, put a big X
                                    tf.setText("X");
                                }
                                else { //if it's not a primary DP, put a big O
                                    tf.setText("O");
                                }
                            }
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }


                    //SET CLICK FUNCTIONALITY FOR PRIMARY DPs
                    //TODO: Blue for all good, Green for in progress, Red for something's wrong. Would probably happen on click.
                    if (x == y) { //if its a primary DP, then it can only be X or O

                        //when you right click without setting a context menu, a default context menu appears
                        //I wanted to get rid of that, so I set the textfield to an empty context menu here
                        ContextMenu temp = new ContextMenu();
                        tf.setContextMenu(temp);


                        tf.setOnMouseClicked(e -> {
                            if(e.getButton() == MouseButton.SECONDARY) { //when you right-click
                                if (tf.getText().equals("X")) { //if there's an X there
                                    tf.setText("O"); //set it to O and update the database
                                    String sql = "UPDATE MatrixCell SET symbol = 'O' WHERE matrixrow = " + grid.getRowIndex(tf) + " AND matrixcolumn = " + grid.getColumnIndex(tf) + ";";
                                    executeDatabaseU(sql);
                                } else if (tf.getText().equals("O")) { //if there's an O there
                                    tf.setText("X"); //put a big X and update the database
                                    String sql = "UPDATE MatrixCell SET symbol = 'X' WHERE matrixrow = " + grid.getRowIndex(tf) + " AND matrixcolumn = " + grid.getColumnIndex(tf) + ";";
                                    executeDatabaseU(sql);

                                }
                            }
                            //when you double-click
                            else if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                                equationPopup(tf); //open the equation popup
                            }

                        });
                    }


                    //SET CLICK FUNCTIONALITY FOR MINOR DPs
                    else { //if it's not a primary DP

                        //this context menu will allow the user to change the symbol in the matrix
                        ContextMenu matrixSymbols = new ContextMenu();

                        MenuItem Oitem = new MenuItem("O"); //O - no influence (default)
                        Oitem.setOnAction(new EventHandler<ActionEvent>() { //link the menu item to the addNewSibling() function

                            @Override
                            public void handle(ActionEvent event) {
                                tf.setText("O");
                            }
                        });


                        MenuItem oitem = new MenuItem("o"); //o - minor influence
                        oitem.setOnAction(new EventHandler<ActionEvent>() { //link the menu item to the addNewSibling() function

                            @Override
                            public void handle(ActionEvent event) {
                                tf.setText("o");
                            }
                        });


                        MenuItem xitem = new MenuItem("x"); //x - significant influence from minor DP
                        xitem.setOnAction(new EventHandler<ActionEvent>() { //link the menu item to the addNewSibling() function

                            @Override
                            public void handle(ActionEvent event) {
                                tf.setText("x");
                            }
                        });


                        matrixSymbols.getItems().addAll(Oitem, oitem, xitem); //put the items in the contextmenu


                        tf.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() { //when you RIGHT-CLICK on a textfield

                            @Override
                            public void handle(ContextMenuEvent event) {
                                matrixSymbols.show(tf, event.getScreenX(), event.getScreenY()); //show the context menu where the user clicked
                            }
                        });

                        //TODO change to double click, make a add/edit/delete equations popup
                        //NOTE: using context menu event to add possible add eq/change eq later
                        tf.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                                    equationPopup(tf);
                                }

                            }
                        });
                    }

                    // Iterate the Index using the loops
                    grid.setRowIndex(tf, y); //put the textfields in the correct row and column
                    grid.setColumnIndex(tf, x);
                    grid.getChildren().add(tf); //add it to the gridpane //set the textfield in the correct cell of the GridPane
                }
            }
        }

///////////////////////////////// Set up the table that lists the FRs and DPs ///////////////////////////

        //create and label the table columns
        TableColumn<Entry, String> displayNumCol = new TableColumn<>("#");
        TableColumn<Entry, String> frCol = new TableColumn<>("FR");
        TableColumn<Entry, String> dpCol = new TableColumn<>("DP");

        //initialize column width
        displayNumCol.setPrefWidth(75);
        frCol.setPrefWidth(79);
        dpCol.setPrefWidth(100);

        //makes full text appear when you hover over cell
        //this functionality is provided by github user Bradley Turek
        //more details along with the link to the source code on github can be found in the TooltippedTableCell class
        displayNumCol.setCellFactory(TooltippedTableCell.forTableColumn());
        frCol.setCellFactory(TooltippedTableCell.forTableColumn());
        dpCol.setCellFactory(TooltippedTableCell.forTableColumn());



        //puts all the data into the cells
        displayNumCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Entry, String> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                return new SimpleStringProperty(p.getValue().displayNum);
            }
        });

        frCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Entry, String> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                return new SimpleStringProperty(p.getValue().FR);
            }
        });

        dpCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Entry, String> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                return new SimpleStringProperty(p.getValue().getDP()[0].getDp());
            }
        });


        //add the columns and data to the table
        table.getColumns().add(displayNumCol);
        table.getColumns().add(frCol);
        table.getColumns().add(dpCol);

        table.getItems().addAll(obsEntry);



    } //end of initialize

    /**
     * Function called by home button event handler that changes the scene back to the spreadsheet/tree view
     * @param event fired by home button
     * @throws IOException ?
     */
    private void goToHome(ActionEvent event) throws IOException {

        //loads the scene
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../FXML/sample.fxml"));
        Parent P = loader.load();
        Scene s = new Scene(P, 1000, 500);

        //shows the scene
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(s);
        window.show();


    }

    //time to organize the entries so a parent is always followed by all its children

    /**
     * Recursively organizes all of the entry objects so a parent is always followed by all its children, similar to how
     * the rows are organized in the treeview. The way the objects are displayed stays consistent between the
     * treeview and the matrix view
     * @param entry the parent object in the list whose children we need to find
     */
    private void organizeEntries(Entry entry){
        int numChild = entry.numChildren;
        for(int i = 0; i < numChild; i++){ //for each child of the given Entry object
            String displayNum;
            if(entry.displayNum.equals("0")){
                displayNum = Integer.toString(i+1);
            }
            else{
                displayNum = entry.displayNum + "." + (i+1); //make its display ID
            }
            for(Entry ent : FRDP){ //search through the list of entries
                if(ent.displayNum.equals(displayNum)){ //find the child with the same display ID
                    orgEnts = addToOrg(ent); //add it to the orgEnts (organized Entries) list
                    organizeEntries(ent); //do the same to the child
                }
            }
        }


    }

    /**
     * Adds the given entry object to the end of the organized entry array
     * @param ent Entry object that needs to be added to list
     * @return the new organized entry list with the new entry added to the end
     */
    private Entry[] addToOrg(Entry ent){
        Entry[] temp = new Entry[orgEnts.length+1];
        System.arraycopy(orgEnts, 0, temp, 0, orgEnts.length);
        temp[orgEnts.length] = ent;
        orgEnts = temp;
        return orgEnts;
    }



    private void equationPopup(TextField tf){
        //make a popup with a textfield to enter an equation and a submit button
        Stage popup = new Stage();
        VBox vbox = new VBox(20);
        Label label = new Label();
        TextField text = new TextField();
        Button butt = new Button("Submit");
        vbox.getChildren().addAll(label, text, butt);
        Scene scene = new Scene(vbox, 300, 200);

        label.setText("Enter your Equation: ");

        butt.setOnAction(new EventHandler<ActionEvent>() { //when they submit their equation
            @Override
            public void handle(ActionEvent event) {
                String eq = text.getText(); //get the equation
                popup.close(); //close the popup window
                //NOTE: grid starts at 1, not 0. Keep that in mind with these variables
                int row = grid.getRowIndex(tf);
                int column = grid.getColumnIndex(tf);

                //change the text to bold and update the database
                tf.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                String sql2 = "UPDATE MatrixCell SET equation = '" + eq + "' WHERE matrixrow = " + row + " AND matrixcolumn = " + column + ";";
                executeDatabaseU(sql2);

            }
        });
        popup.setScene(scene);
        popup.show();
    }

}


