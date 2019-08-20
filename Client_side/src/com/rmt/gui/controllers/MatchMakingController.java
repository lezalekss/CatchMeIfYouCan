package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.rmt.services.CommunicationService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.net.URL;
import java.util.*;

public class MatchMakingController implements Initializable {

    @FXML
    JFXListView activePlayersListView;

    @FXML
    JFXButton logOutButton;

    @FXML
    JFXButton refreshButton;

    private ObservableSet<String> activePlayersSet;

    private CommunicationService communicationService = CommunicationService.getCommunicationServiceInstance();

    private String username;

    private String challengerUsername;

    //value is changed by WaititngChallenge thread and Contoller is reacting in Listener
    private BooleanProperty challengeReceived = new SimpleBooleanProperty(false);

    //value is changed by Controller and WaititngChallenge thread is reacting in Listener
    private BooleanProperty challengeSent = new SimpleBooleanProperty(false);


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.challengeReceived.setValue(false);

        this.challengeSent.setValue(false);

        Set<String> receivedPlayers = this.communicationService.getActivePlayers();

        if (receivedPlayers != null) {

            //wrap set with observable
            this.activePlayersSet = FXCollections.observableSet(receivedPlayers);

            //populating listview with usernames from set
            this.activePlayersListView.getItems().addAll(activePlayersSet);

        }

        this.addSetChangedListener();

        this.addPlayerSelectedListener();

        this.addChallengeReceivedListener();

        this.communicationService.waitToBeChallenged(this.challengeSent, this.challengeReceived,
          this.challengerUsername);

    }

    private void addChallengeReceivedListener() {
        this.challengeReceived.addListener((observable, oldValue, newValue) -> {
            if (newValue == true) {

                System.out.println("Contoler uvaio challenge received da je true");

                this.refreshButton.setVisible(true);
                this.logOutButton.setDisable(true);
                this.activePlayersListView.setDisable(true);

                System.out.println("izgasio dugmice");

                boolean accepted = this.showChallengeReceivingDialog(this.challengerUsername);

                System.out.println("Izazov prihvacen? "+accepted);

                if (accepted) {
                    this.communicationService.challengeAccepted();
                    //promeni na scenu na igru
                } else {
                    this.communicationService.challengeRejected();

                    this.refreshButton.setVisible(false);
                    this.logOutButton.setDisable(false);
                    this.activePlayersListView.setDisable(false);

                    this.communicationService.waitToBeChallenged(this.challengeSent, this.challengeReceived,
                            this.challengerUsername);

                    this.challengeReceived.setValue(false);
                }
            }
        });
    }

    private void addPlayerSelectedListener() {
        this.activePlayersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Alert alert = this.createAlert(Alert.AlertType.CONFIRMATION, "Da li zelite da izazovete "+newValue.toString()+"?");
            Optional<ButtonType> answer = alert.showAndWait();
            if (answer.get() == ButtonType.OK) {
                System.out.println("Izazvan \n");

                //this shuts down challenge waiter
                this.challengeSent.setValue(true);
                System.out.println("Promenjena vred challenge sent \n");

                boolean challengeSuccessful = this.communicationService.challengeOpponent(newValue.toString());


                if(challengeSuccessful == false){
                    alert = this.createAlert(Alert.AlertType.ERROR, newValue.toString()+" vise nije aktivan. Pokusajte ponovo.");
                    alert.showAndWait();
                    return;
                }
                //prihvacen izazov, comm service vratio true, igra moze da pocne



            }
        });
    }

    private void addSetChangedListener() {
        this.activePlayersSet.addListener((SetChangeListener<? super String>) c -> {
                    //this.activePlayersSet.remove(this.username);
                    this.activePlayersListView.getItems().setAll(this.activePlayersSet);
                    System.out.println("Set updated in controller");
                }

        );
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
