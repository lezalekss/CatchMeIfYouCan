package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.rmt.services.CommunicationService;
import com.rmt.services.StageService;
import com.rmt.services.WaitingTask;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MatchMakingController implements Initializable {

    @FXML
    JFXListView activePlayersListView;

    @FXML
    JFXButton logOutButton;

    @FXML
    JFXButton refreshButton;

    @FXML
    JFXButton challenging;
    @FXML
    JFXButton waitingForChallenge;


    private ObservableSet<String> activePlayersSet;

    private String username;

    private String challengerUsername;

    private CommunicationService communicationService = CommunicationService.getCommunicationServiceInstance();

    private StageService stageService = StageService.getStageServiceInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        challenging.setDisable(true);

        Set<String> receivedPlayers = this.communicationService.getActivePlayers();

        if (receivedPlayers != null) {

            //wrap set with observable
            this.activePlayersSet = FXCollections.observableSet(receivedPlayers);

            //populating listview with usernames from set
            this.activePlayersListView.getItems().addAll(activePlayersSet);

        }

        this.addSetChangedListener();

        this.addPlayerSelectedListener();

    }


    private void addPlayerSelectedListener() {
        this.activePlayersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Alert alert = this.createAlert(Alert.AlertType.CONFIRMATION, "Da li zelite da izazovete " + newValue.toString() + "?");
            Optional<ButtonType> answer = alert.showAndWait();
            if (answer.get() == ButtonType.OK) {
                System.out.println("Izazvan \n");

                boolean challengeSuccessful = this.communicationService.challengeOpponent(newValue.toString());

                if (challengeSuccessful == false) {
                    alert = this.createAlert(Alert.AlertType.ERROR, newValue.toString() + "vise nije aktivan ili je odbio poziv");
                    alert.showAndWait();
                    return;
                }else{
                    try {
                        stageService.changeScene("com/rmt/gui/fxmls/play.fxml", refreshButton.getScene());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void addSetChangedListener() {
        this.activePlayersSet.addListener((SetChangeListener<? super String>) c -> {
                    //this.activePlayersSet.remove(this.username);
                    this.activePlayersListView.getItems().clear();
                    this.activePlayersListView.getItems().setAll(this.activePlayersSet);
                    System.out.println("Set updated in controller");
                }
        );
    }

    public void switchToWaitingForChallenge(ActionEvent event) {

        this.waitToBeChallenged();

        this.communicationService.switchToWaitingForChallenge();

        this.activePlayersListView.setDisable(true);
        this.refreshButton.setDisable(true);
        this.challenging.setDisable(false);
        this.waitingForChallenge.setDisable(true);
    }

    private void waitToBeChallenged() {
        WaitingTask waitingTask = new WaitingTask();
        waitingTask.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.challengerUsername = newValue;
                this.challengeReceived();
            }
        });
        this.communicationService.waitToBeChallenged(waitingTask);

    }

    public void switchToChallenging(ActionEvent event) {

        this.communicationService.switchToChallenging();

        this.activePlayersListView.setDisable(false);
        this.refreshButton.setDisable(false);
        this.challenging.setDisable(true);
        this.waitingForChallenge.setDisable(false);
    }

    private void challengeReceived() {
        System.out.println("Contoler uvaio challenge received da je true");

        this.refreshButton.setVisible(true);
        this.logOutButton.setDisable(true);
        this.activePlayersListView.setDisable(true);

        System.out.println("izgasio dugmice");

        boolean accepted = this.showChallengeReceivingDialog(this.challengerUsername);

        System.out.println("Izazov prihvacen? " + accepted);

        if (accepted) {
            this.communicationService.challengeAccepted(this.challengerUsername);
            try {
                stageService.changeScene("com/rmt/gui/fxmls/play.fxml", refreshButton.getScene());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.communicationService.challengeRejected(this.challengerUsername);

            this.refreshButton.setDisable(false);
            this.logOutButton.setDisable(false);
            this.activePlayersListView.setDisable(false);

            this.waitToBeChallenged();
        }
    }

    private Alert createAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Matchmaking");
        alert.setHeaderText(message);
        return alert;
    }

    private boolean showChallengeReceivingDialog(String challengerUsername) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Matchmaking");
        alert.setHeaderText(challengerUsername + " Vas izaziva.");
        alert.setContentText("Da li prihvatate izazov?");

        ButtonType yes = new ButtonType("YES");
        ButtonType no = new ButtonType("NO");

        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> answer = alert.showAndWait();

        if (answer.get() == yes)
            return true;
        else return false;
    }

    public void onLogoutButtonClicked(ActionEvent event) {
        this.communicationService.logout();
    }

    public void onRefreshButtonClicked(ActionEvent event) {
//        this.activePlayersListView.setMouseTransparent( true );
//        this.activePlayersListView.setFocusTraversable( false );

        this.activePlayersListView.setDisable(true);

        this.communicationService.updatePlayers(activePlayersSet);

        this.activePlayersListView.setDisable(false);

//        this.activePlayersListView.setMouseTransparent( false );
//        this.activePlayersListView.setFocusTraversable( true );
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
