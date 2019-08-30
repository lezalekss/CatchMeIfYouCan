package com.rmt.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class TheChaseController implements Initializable{

    @FXML
    TextArea questionText;

    String chaser;
    String runner;

    @FXML
    Label first, second, third, fourth, fifth, sixth, seventh, eighth, ninth;

//    Label[] steps = new Label[]{first, second, third, fourth, fifth, sixth, seventh, eighth, ninth};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.questionText.setFocusTraversable(false);
        this.questionText.setMouseTransparent(true);
        this.questionText.setText("Kako se zove drugi, po velicini, grad u Libanu?");
    }

    private void setUsernames(){

    }

    public void buttonClicked() {

    }

    public void setRoles(String roles){
        String[] usernames = roles.split("#");
        this.chaser = usernames[0];
        this.runner = usernames[1];
        this.first.setText(this.chaser);
        this.fourth.setText(this.runner);
    }
}
