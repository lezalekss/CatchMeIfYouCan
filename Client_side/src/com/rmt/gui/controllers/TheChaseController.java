package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.rmt.domain.Question;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

public class TheChaseController implements Initializable{

    @FXML
    TextArea questionText;

    String chaser;
    String runner;

    @FXML
    Label first, second, third, fourth, fifth, sixth, seventh, eighth, ninth;

//    Label[] steps = new Label[]{first, second, third, fourth, fifth, sixth, seventh, eighth, ninth};

    @FXML
    JFXButton answerOne, answerTwo, answerThree;

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

    private void loadQuestionText(Question question){
        String questionText = question.getQuestionText();

        final IntegerProperty i = new SimpleIntegerProperty(0);
        Timeline timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(
                Duration.millis(20),
                event -> {
                    if(i.get() > questionText.length()){
                        timeline.stop();
                    }else {
                        this.questionText.setText(questionText.substring(0, i.get()));
                        i.set(i.get()+1);
                    }
                }
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void loadQuestionAnswers(Question question){
        ArrayList<String> possibleAnswers = new ArrayList(3);
        possibleAnswers.add(question.getPossibleAnswers()[0]);
        possibleAnswers.add(question.getPossibleAnswers()[1]);
        possibleAnswers.add(question.getCorrectAnswer());
        Collections.shuffle(possibleAnswers);

        this.answerOne.setText(possibleAnswers.get(0));
        this.answerOne.setText(possibleAnswers.get(0));
        this.answerOne.setText(possibleAnswers.get(0));
    }

    public void setRoles(String roles){
        String[] usernames = roles.split("#");
        this.chaser = usernames[0];
        this.runner = usernames[1];
        this.first.setText(this.chaser);
        this.fourth.setText(this.runner);
    }
}
