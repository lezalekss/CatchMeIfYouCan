package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.rmt.services.CommunicationService;
import com.rmt.services.StageService;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StartSceneController implements Initializable {

    private CommunicationService communicationService;
    private StageService stageService;

//    @FXML JFXButton quitButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.communicationService = CommunicationService.getCommunicationServiceInstance();
        this.stageService = StageService.getStageServiceInstance();
    }

    public void onConnectButtonClicked(ActionEvent event){
        try {
            boolean connectionSuccessful = this.communicationService.connect();
            if(connectionSuccessful){
              this.stageService.changeScene("com/rmt/gui/fxmls/login.fxml", event);
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Neuspesno povezivanje");
                error.setContentText("Pokusajte ponovo.");
                error.showAndWait();
            }
        } catch (IOException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Greska prilikom povezivanja");
            error.setContentText("Dogodila se greska prilikom uspostavljanja veze sa serverom. Pokusajte ponovo.");
            error.showAndWait();
        }
    }

    public void onQuitButtonClicked(ActionEvent event){
//        Stage currentScene = (Stage) ((Node)(event.getSource())).getScene().getWindow();
//        Stage currentScene = (Stage) (this.quitButton).getScene().getWindow();
        this.stageService.quit(event);
    }
}
