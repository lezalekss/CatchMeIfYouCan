package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.rmt.domain.Question;
import com.rmt.services.CommunicationService;
import com.rmt.services.StageService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class QuickQuestions implements Initializable {

    @FXML
    private JFXButton answerOne;

    @FXML
    private JFXButton answerTwo;

    @FXML
    private JFXButton answerThree;

    @FXML
    private JFXButton answerFour;

    @FXML
    private TextArea questionText;

    private Question[] questions;
    private int currentIndex = 0;
    private int seconds = 20;

    private CommunicationService communicationService = CommunicationService.getCommunicationServiceInstance();
    private StageService stageService = StageService.getStageServiceInstance();

    private int numberOfCorrectAnswers = 0;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.questionText.setFocusTraversable(false);
        this.questionText.setMouseTransparent(true);
        //ucitaj pitanja
//        this.questions = communicationService.loadQuickQuestions();
        this.setTestQuestions();
        this.displayQuestion();
        this.startTimer(seconds);
    }

    private void startTimer(int seconds){
        final Task<Void> countdown = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(seconds*1000);
                return null;
            }
        };
        countdown.setOnSucceeded(event -> {
            try {
                stageService.changeScene("com/rmt/gui/fxmls/theChase.fxml", this.answerOne.getScene());
                System.out.println(this.numberOfCorrectAnswers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Thread timer = new Thread(countdown);
        timer.start();
    }


    public void buttonClicked(ActionEvent event) {
        JFXButton selectedButton = (JFXButton) event.getTarget();
        if(selectedButton.getText().equals(this.questions[currentIndex].getCorrectAnswer())){
            ++this.numberOfCorrectAnswers;
        }
        ++currentIndex;
        this.displayQuestion();
    }


    private void displayQuestion() {
        Question question = this.questions[this.currentIndex];

        this.loadQuestionText(question);
        this.loadClosedQuestion(question);

        this.answerOne.setDisableVisualFocus(true);
    }

    private void loadQuestionText(Question question) {
        String questionText = question.getQuestionText();

        final IntegerProperty i = new SimpleIntegerProperty(0);
        Timeline timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(
                Duration.millis(20),
                event -> {
                    if (i.get() > questionText.length()) {
                        timeline.stop();
                    } else {
                        this.questionText.setText(questionText.substring(0, i.get()));
                        i.set(i.get() + 1);
                    }
                });
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void loadClosedQuestion(Question question) {
        String[] answers = question.getPossibleAnswers();

        this.answerOne.setText(answers[0]);
        this.answerTwo.setText(answers[1]);
        this.answerThree.setText(answers[2]);
        this.answerFour.setText(answers[3]);

        this.showButtons();
    }


    private void showButtons() {
        this.answerOne.setVisible(true);
        this.answerTwo.setVisible(true);
        this.answerThree.setVisible(true);
        this.answerFour.setVisible(true);
    }

    private void setTestQuestions(){
        this.questions= new Question[100];
        for (int i=0;i<100;i++) {
            questions[i] = new Question();
            questions[i].setQuestionText("pitanje "+i);
            questions[i].setCorrectAnswer("odg 3");
            String[] pa = new String[4];
            pa[0] = "ponudjeni odg 1";
            pa[1] = "ponudjeni odg 2";
            pa[2] = "ponudjeni odg 3";
            pa[3] = "ponudjeni odg 4";
            questions[i].setPossibleAnswers(pa);
            questions[i].setCorrectAnswer("ponudjeni odg 1");
        }
    }
}
