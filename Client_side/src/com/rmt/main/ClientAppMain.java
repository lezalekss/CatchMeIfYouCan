package com.rmt.main;

import com.rmt.gui.controllers.TheChaseController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientAppMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("com/rmt/gui/fxmls/startScene.fxml"));
        primaryStage.setTitle("Catch me if you can");
        primaryStage.setScene(new Scene(root, 400, 600));
//        primaryStage.setResizable(false);
//        primaryStage.setFullScreenExitHint("");
//        primaryStage.setFullScreen(true);
        primaryStage.show();

//        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("com/rmt/gui/fxmls/theChase.fxml"));
//
//        primaryStage.setScene(new Scene(loader.load()));
//        ((TheChaseController)loader.getController()).setRoles("jeka\nnidza");
//        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
