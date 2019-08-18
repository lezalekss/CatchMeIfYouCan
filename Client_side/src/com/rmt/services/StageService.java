package com.rmt.services;

import com.rmt.gui.controllers.ActivePlayersController;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class StageService {

    private static StageService serviceInstance = null;

    private StageService(){}

    public static StageService getStageServiceInstance(){
        if(serviceInstance == null){
            serviceInstance = new StageService();
        }
        return serviceInstance;
    }

    public void changeScene(String newScene, ActionEvent event) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource(newScene));
        Stage currentScene = (Stage) ((Node)(event.getSource())).getScene().getWindow();

        currentScene.getScene().setRoot(parent);
        currentScene.show();
    }

    public void quit(ActionEvent event){
        Stage currentScene = (Stage) ((Node)(event.getSource())).getScene().getWindow();
        currentScene.close();
    }

    public void changeToActivePlayersScene(ActionEvent event, String username) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("com/rmt/gui/fxmls/activePlayersScene.fxml"));
        Stage currentStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        currentStage.getScene().setRoot(loader.load());
        ((ActivePlayersController)loader.getController()).setUsername(username);
    }
}
