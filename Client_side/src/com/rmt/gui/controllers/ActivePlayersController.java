package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXListView;
import com.rmt.services.CommunicationService;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

public class ActivePlayersController implements Initializable {

    @FXML
    JFXListView activePlayersListView;

    private ObservableSet<String> activePlayersSet;

    private CommunicationService communicationService = CommunicationService.getCommunicationServiceInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Set<String> receivedPlayers = this.communicationService.getActivePlayers();
        if(receivedPlayers!=null){
            //populating set with usernames
            this.activePlayersSet = FXCollections.observableSet(receivedPlayers);
            //populating listview with usernames from set
            this.activePlayersListView.getItems().addAll(activePlayersSet);
//        FXCollections.synchronizedObservableMap()

            this.activePlayersSet.addListener((SetChangeListener<? super String>) c -> {
            this.activePlayersListView.getItems().setAll(this.activePlayersSet);
        });
        }
    }
}
