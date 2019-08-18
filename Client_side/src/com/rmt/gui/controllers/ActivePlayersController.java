package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXListView;
import com.rmt.services.CommunicationService;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class ActivePlayersController implements Initializable {

    @FXML
    JFXListView activePlayersListView;

    private ObservableSet<String> activePlayersSet;

    private CommunicationService communicationService = CommunicationService.getCommunicationServiceInstance();

    private String username;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Set<String> receivedPlayers = this.communicationService.getActivePlayers();

        if (receivedPlayers != null) {

            //populating set with usernames
            this.activePlayersSet = FXCollections.observableSet(receivedPlayers);

            //populating listview with usernames from set
            this.activePlayersListView.getItems().addAll(activePlayersSet);

        }
        this.addSetChangedListener();

        this.addPlayerSelectedListener();

        this.communicationService.updatePlayers(this.activePlayersSet);

    }

    private void addPlayerSelectedListener() {
        this.activePlayersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Alert alert = this.createAlert(newValue.toString());
            Optional<ButtonType> answer = alert.showAndWait();
            if (answer.get() == ButtonType.OK) {
                this.communicationService.stopUpdating();
                this.communicationService.challengeOpponent(newValue.toString());
            }
        });
    }

    private void addSetChangedListener() {
        this.activePlayersSet.addListener((SetChangeListener<? super String>) c -> {
                    this.activePlayersSet.remove(this.username);
                    this.activePlayersListView.getItems().setAll(this.activePlayersSet);
                    System.out.println("Set updated in controller");
                }

        );
    }

    private Alert createAlert(String opponentUsername) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Izazivanje");
        alert.setHeaderText("Da li zelite da izazovete " + opponentUsername + "?");
        return alert;
    }

    public void onLogoutButtonClicked(ActionEvent event) {
        HashSet<String> set = new HashSet<>();
        set.add("jeka");
        set.add("nidza");
        set.add("Proba");
//        this.activePlayersSet.add("Proba");
        this.activePlayersSet.addAll(set);
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
