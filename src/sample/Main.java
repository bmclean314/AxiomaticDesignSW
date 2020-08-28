package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main extends Application {

    //0 on application start when database needs to be cleared
    //1+ when application changes scenes and data in database needs to stay
    public static int switchesMatrix = 0;
    public static int switchesCN = 0;


    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Axiomatic Design Software");
        primaryStage.setScene(new Scene(root, 1000, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}

