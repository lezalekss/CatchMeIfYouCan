package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXTextField;
import com.rmt.services.CommunicationService;
import com.rmt.services.StageService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    private StageService stageService;
    private CommunicationService communicationService;

    @FXML
    JFXTextField username;
    @FXML
    JFXTextField password;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.stageService = StageService.getStageServiceInstance();
        this.communicationService = CommunicationService.getCommunicationServiceInstance();
    }

    public void onBackButtonClicked(ActionEvent event) throws IOException {
        this.stageService.changeScene("com/rmt/gui/fxmls/startScene.fxml", event);
    }

    public void onRegisterButtonClicked(ActionEvent event){
        String username = this.username.getText();
        System.out.println(username);
        String password = this.password.getText();
        boolean registrationSuccessful = this.communicationService.register(username, password);
        if(registrationSuccessful){
            try {
                //this.stageService.changeScene("com/rmt/gui/fxmls/matchMakingScene.fxml", event);
                this.stageService.changeToActivePlayersScene(event, username);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration failed");
            alert.setContentText("Username already exists.");
            alert.showAndWait();
        }
    }

    public void onLoginButtonClicked(ActionEvent event){
        String username = this.username.getText();
        String password = this.password.getText();
        String answer = this.communicationService.login(username, password);
        if(answer.equals("OK")){
            try {
                this.stageService.changeScene("com/rmt/gui/fxmls/matchMakingScene.fxml", event);
                //this.stageService.changeToActivePlayersScene(event, username);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login failed");
            alert.setContentText(answer);
            alert.showAndWait();
        }
    }


}
