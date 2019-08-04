package com.rmt.gui.controllers;

import com.rmt.services.CommunicationService;
import com.rmt.services.StageService;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    private StageService stageService;
    private CommunicationService communicationService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.stageService = StageService.getStageServiceInstance();
        this.communicationService = CommunicationService.getCommunicationServiceInstance();
    }

    public void onBackButtonClicked(ActionEvent event) throws IOException {
        this.stageService.changeScene("com/rmt/gui/fxmls/startScene.fxml", event);
    }
}
