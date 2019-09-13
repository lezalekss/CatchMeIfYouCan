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
        this.rulesText.setText("Catch me if you can je igra znanja u kojoj je jedan 'chaser' a drugi 'runner'. Igra počinje povezivanjem protivnika i prostim odgovarnjem na brza pitanja - učesnici dobijaju 60 sekundi tokom kojih treba da odgovore tačno na što više pitanja. Nakon prvog dela igre, obračunavaju se tačni odgovori i takmičar koji ima više istih postaje 'chaser', odnosno osoba koja juri, dok drugi učesnik postaje 'runner', tj. osoba koja beži.\n" +
                "Ukoliko oba igrača imaju jednak broj poena, 'chaser' je igrač koji je izazvao protivnika. Glavni deo igre počinje nakon toga, kada se odvija proces 'hvatanja protivnika'. Naime, učesnici dobijaju jedno po jedno pitanje sa ponuđenim odgovorima na koje treba da odgovore u roku od 10 sekundi, a ispred njih se nalazi tabla sa 9 polja, na kojoj je 'chaser' na samom vrhu, dok se 'runner' nalazi na sredini. Cilj igre za 'chaser'-a jeste da uhvati protivnika, dok je 'runner'-u cilj da pobegne od svog oponenta. Nakon što učesnici odgovore na pitanje, njihov odgovor se evaulira i u slučaju da su odgovorili tačno, oni se pomeraju naniže na tabli. Ukoliko neko ne da tačan odgovor, on ostaje na mestu na kom je bio. Daljim odgovaranjem na pitanja, učesnici se kreću po tabli, a sama igra se može završiti na dva načina:\n" +
                "1) pobeda 'chaser'-a - kada on stigne 'runner'-a i 'pregazi ga'\n" +
                "2) pobeda 'runner'-a - kada on uspe da dođe do samog kraja table pre nego što ga 'chaser' stigne");
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
