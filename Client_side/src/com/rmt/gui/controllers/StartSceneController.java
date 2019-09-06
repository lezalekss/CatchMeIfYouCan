package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.rmt.services.CommunicationService;
import com.rmt.services.StageService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StartSceneController implements Initializable {

    private CommunicationService communicationService;
    private StageService stageService;

    @FXML
    ImageView logo;

    @FXML
    JFXButton rulesButton;

    @FXML
    VBox buttons;

    @FXML
    VBox rules;

    @FXML
    JFXTextArea rulesText;

    @FXML
    JFXButton closeRules;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.communicationService = CommunicationService.getCommunicationServiceInstance();
        this.stageService = StageService.getStageServiceInstance();

        this.rulesButton.setDisableVisualFocus(true);
        this.rules.setVisible(false);
    }

    public void onRulesButtonClicked(ActionEvent event) {
        this.logo.setOpacity(0.1);
        this.buttons.setOpacity(0.1);
        this.rules.setVisible(true);
        this.rulesText.setText("Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?");
        this.fullscreen(event);
    }


    private void fullscreen(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) (event.getSource())).getScene().getWindow();
        if (currentStage.isFullScreen()) {
            currentStage.setFullScreen(false);
        } else {
            currentStage.setFullScreenExitHint("");
            currentStage.setFullScreen(true);
        }
    }

    public void onCloseRulesButtonClicked(ActionEvent event) {
        this.rules.setVisible(false);
        this.logo.setOpacity(1);
        this.buttons.setOpacity(1);
        this.fullscreen(event);
    }

    public void onConnectButtonClicked(ActionEvent event) {
        try {
            boolean connectionSuccessful = this.communicationService.connect();
            if (connectionSuccessful) {
                this.stageService.changeScene("com/rmt/gui/fxmls/login.fxml", event);
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Neuspesno povezivanje");
                error.setContentText("Pokusajte ponovo.");
                error.showAndWait();
            }
        } catch (IOException e) {
            //throw by changeScene() method
        }
    }

    public void onQuitButtonClicked(ActionEvent event) {
//        Stage currentScene = (Stage) ((Node)(event.getSource())).getScene().getWindow();
//        Stage currentScene = (Stage) (this.quitButton).getScene().getWindow();
        this.stageService.quit(event);
    }
}
