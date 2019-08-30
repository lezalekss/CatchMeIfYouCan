package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;

import com.rmt.services.CommunicationService;
import com.rmt.services.StageService;
import com.rmt.services.WaitingTask;
import javafx.collections.*;
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
    JFXButton switchButton;



    private ObservableSet<String> activePlayersSet;

    private String username;

    private String challengerUsername;

    private CommunicationService communicationService = CommunicationService.getCommunicationServiceInstance();

    private StageService stageService = StageService.getStageServiceInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.switchButton.setText("SWITCH TO WAITING");
//        challenging.setDisable(true);

        Set<String> receivedPlayers = this.communicationService.getActivePlayers();

        if (receivedPlayers.size()>=0) {

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

                boolean challengeSuccessful = this.communicationService.challengeOpponent(newValue.toString());

                if (challengeSuccessful == false) {
                    alert = this.createAlert(Alert.AlertType.ERROR, newValue.toString() + "vise nije aktivan ili je odbio poziv");
                    alert.showAndWait();
                    return;
                }else{
                    try {
                        this.communicationService.tellServerToStartGame(newValue.toString());
                        this.stageService.changeScene("com/rmt/gui/fxmls/quickQuestions.fxml", refreshButton.getScene(), false);
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
                }
        );
    }

    public void onSwitchButtonClicked() {
        if(this.switchButton.getText().equals("SWITCH TO WAITING")){
            this.waitForChallenge();

            this.communicationService.tellServerToSwitchToWaiting();

            this.resetButtonForWaitingState();

            this.switchButton.setText("SWITCH TO CHALLENGING");

        }else if(this.switchButton.getText().equals("SWITCH TO CHALLENGING")){

            this.communicationService.tellServerToSwitchToChallenging();

            this.switchButton.setText("SWITCH TO WAITING");
        }

    }

//    public void switchToChallenging() {
//        this.communicationService.tellServerToSwitchToChallenging();
//    }

    private void resetButtonForWaitingState() {
        this.activePlayersListView.setDisable(true);
        this.refreshButton.setDisable(true);
    }

    // TODO preimenuj metodu
    private void waitForChallenge() {
        WaitingTask waitingTask = new WaitingTask();
        waitingTask.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if(newValue.equals("shutdown")){
                    this.resetButtonsForChallengingState();
                }else{
                    this.challengerUsername = newValue;
                    this.challengeReceived();
                }
            }
        });
        this.communicationService.startWaitingTask(waitingTask);
    }

    private void resetButtonsForChallengingState() {
        activePlayersListView.setDisable(false);
        refreshButton.setDisable(false);
    }

    private void challengeReceived() {

//        this.refreshButton.setDisable(true);
//        this.logOutButton.setDisable(true);
//        this.activePlayersListView.setDisable(true);


        boolean accepted = this.showChallengeReceivingDialog(this.challengerUsername);


        if (accepted) {
            this.communicationService.challengeAccepted(this.challengerUsername);
            try {
                stageService.changeScene("com/rmt/gui/fxmls/quickQuestions.fxml", refreshButton.getScene(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.communicationService.challengeRejected(this.challengerUsername);

//            this.refreshButton.setDisable(false);
//            this.logOutButton.setDisable(false);
//            this.activePlayersListView.setDisable(false);

            this.waitForChallenge();
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

    public void onLogoutButtonClicked(ActionEvent event) throws IOException {
        this.communicationService.logout();
        this.stageService.changeScene("com/rmt/gui/fxmls/login.fxml", event);
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
