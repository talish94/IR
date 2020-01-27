package Main;


import Part_1.Indexer;
import Part_1.Parse;
import Part_1.ReadFile;
import Part_1.Document;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;


public class Main extends Application {




    public static void main(String[] args) {

        launch(args);
//        String term ="Summary";
//        System.out.println(term+":"+term.matches("^[A-Z][A-Za-z0-9]*$"));






    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent mainWindow = FXMLLoader.load(getClass().getResource("/Main.fxml"));
        primaryStage.setScene(new Scene(mainWindow , 600, 400));
        primaryStage.show();
    }

}



