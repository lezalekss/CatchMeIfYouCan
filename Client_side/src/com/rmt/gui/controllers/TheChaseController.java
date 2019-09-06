package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.rmt.domain.Question;
import com.rmt.services.CommunicationService;
import com.rmt.services.StageService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class TheChaseController implements Initializable {

    private CommunicationService communicationService = CommunicationService.getCommunicationServiceInstance();
    private StageService stageService = StageService.getStageServiceInstance();

    @FXML
    VBox step;

    @FXML
    Label first, second, third, fourth, fifth, sixth, seventh, eighth, ninth;
    List<Label> steps = new ArrayList<>(9);

    @FXML
    VBox question;

    @FXML
    TextArea questionText;

    @FXML
    JFXButton answerOne, answerTwo, answerThree;

    @FXML
    Label timerLabel;

    @FXML
    ProgressIndicator progressIndicator;

    @FXML
    Label gameFinishedMessage;

    private static final Integer secondsForAnswering = 10;
    private final IntegerProperty timeSeconds = new SimpleIntegerProperty(secondsForAnswering);

    String chaserUsername;
    String runnerUsername;

    boolean isThisChaser;
    boolean isThisRunner;

    private Timeline timeline;

    int chaserPossition = 0;
    int runnerPossition = 4;

    private Question[] questions;
    int i = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.gameFinishedMessage.setOpacity(0);

        this.questionText.setFocusTraversable(false);
        this.questionText.setMouseTransparent(true);

        this.steps.add(first);
        this.steps.add(second);
        this.steps.add(third);
        this.steps.add(fourth);
        this.steps.add(fifth);
        this.steps.add(sixth);
        this.steps.add(seventh);
        this.steps.add(eighth);
        this.steps.add(ninth);

        this.timerLabel.textProperty().bind(this.timeSeconds.asString());

        this.setTestQuestions();
//        this.questions = this.communicationService.getChaseQuestion();

        this.showQuestion(questions[i]);
    }

    public void buttonClicked(ActionEvent event) {
        this.stopTimer();

        String chosenAnswer = ((JFXButton) event.getTarget()).getText();

        boolean gameFinished = this.checkAnswer(chosenAnswer);

        if (gameFinished == false) {
            this.showQuestion(questions[++i]);
        }
    }

    private boolean checkAnswer(String chosenAnswer) {
        boolean isAnswerCorrect = this.questions[i].getCorrectAnswer().equals(chosenAnswer);
        boolean isOpponentCorrect = this.communicationService.exchangeAnswers(isAnswerCorrect);

        if (isThisRunner) {
            if (isAnswerCorrect) {
                this.moveRunner();
                if (runnerPossition == 8) {
                    this.communicationService.gameFinished();
                    this.showGameFinishedMessage("Congratulations! You escaped!");
                    return true;
                }
            }
            if (isOpponentCorrect) {
                moveChaser();
                if (runnerPossition == chaserPossition) {
                    this.communicationService.gameFinished();
                    this.showGameFinishedMessage("Sorry, the chaser got you.");
                    return true;
                }
            }
        } else if (isThisChaser) {
            if (isOpponentCorrect) {
                this.moveRunner();
                if (runnerPossition == 8) {
                    this.communicationService.gameFinished();
                    this.showGameFinishedMessage("Sorry, the runner escaped.");
                    return true;
                }
            }
            if (isAnswerCorrect) {
                moveChaser();
                if (runnerPossition == chaserPossition) {
                    this.communicationService.gameFinished();
                    this.showGameFinishedMessage("Congratulations! You caught the runner!");
                    return true;
                }
            }
        }
        return false;
    }

//    private void colorAnswer(JFXButton button, boolean isAnswerCorrect){
//        if(isAnswerCorrect)
//            button.getStyleClass().add("correct-answer");
//        else
//            button.getStyleClass().add("wrong-answer");
//    }

    private void showGameFinishedMessage(String message) {
        Timeline timer = new Timeline();
        KeyFrame countdown = new KeyFrame(Duration.seconds(5));
        timer.getKeyFrames().add(countdown);
        timer.setOnFinished(e -> {
            try {
                this.stageService.changeScene("com/rmt/gui/fxmls/matchMakingScene.fxml", this.answerOne.getScene(), false);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        this.step.setOpacity(0.2);
        this.question.setOpacity(0.2);
        this.timerLabel.setOpacity(0.2);
        this.gameFinishedMessage.setText(message);
        this.gameFinishedMessage.setOpacity(1);
        timer.playFromStart();
    }

    private void moveRunner() {
        this.steps.get(this.runnerPossition).setText("");
        this.steps.get(++this.runnerPossition).setText(this.runnerUsername);
    }

    private void moveChaser() {
        this.steps.get(this.chaserPossition).setText("");
        this.steps.get(++this.chaserPossition).setText(this.chaserUsername);
//            this.steps.get(this.chaserPossition).getStyleClass().removeAll("chase-step-blue");
        this.steps.get(this.chaserPossition).getStyleClass().add("chase-step-red");
    }

    private void stopTimer() {
        if (this.timeline != null) {
            this.timeline.stop();
        }
    }

    private void resetTimer() {
        this.timeSeconds.set(this.secondsForAnswering);
        this.timeline = new Timeline();
        //to sad radi ovaj delay
//        KeyFrame offset = new KeyFrame(Duration.seconds(1), new KeyValue(this.timeSeconds, secondsForAnswering));
        KeyFrame start = new KeyFrame(Duration.seconds(secondsForAnswering + 1),
                e -> this.timeIsUp(),
                new KeyValue(this.timeSeconds, 0));
//        this.timeline.getKeyFrames().addAll(offset, start);
        this.timeline.getKeyFrames().addAll(start);
        this.timeline.setDelay(Duration.seconds(2));
        this.timeline.playFromStart();
    }

    private void timeIsUp() {
        boolean gameFinished = this.checkAnswer("");
        if (gameFinished == false) {
            this.showQuestion(questions[++i]);
        }
    }

    private void showQuestion(Question question) {
        this.loadQuestionText(question);
        this.loadQuestionAnswers(question);
        this.resetTimer();
    }

    private void loadQuestionText(Question question) {
        String questionText = question.getQuestionText();

        final IntegerProperty i = new SimpleIntegerProperty(0);
        Timeline timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(
                Duration.millis(40),
                event -> {
                    if (i.get() > questionText.length()) {
                        timeline.stop();
                    } else {
                        this.questionText.setText(questionText.substring(0, i.get()));
                        i.set(i.get() + 1);
                    }
                }
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setOnFinished(e -> this.resetTimer());
        timeline.play();
    }

    private void loadQuestionAnswers(Question question) {
        ArrayList<String> possibleAnswers = new ArrayList(3);
        possibleAnswers.add(question.getPossibleAnswers()[2]);
        possibleAnswers.add(question.getPossibleAnswers()[3]);
        possibleAnswers.add(question.getCorrectAnswer());
        Collections.shuffle(possibleAnswers);

        this.answerOne.setText(possibleAnswers.get(0));
        this.answerTwo.setText(possibleAnswers.get(1));
        this.answerThree.setText(possibleAnswers.get(2));

        this.answerOne.setDisableVisualFocus(true);
    }


    public void setRoles(String roles) {
        String[] infos = roles.split("\n");

        String[] usernames = infos[0].split("#");
        this.chaserUsername = usernames[0];
        this.runnerUsername = usernames[1];
        this.first.setText(this.chaserUsername);
        this.fifth.setText(this.runnerUsername);

        String[] booleans = infos[1].split("#");
        if (booleans[0].equals("true")) {
            this.isThisChaser = true;
            this.isThisRunner = false;
        } else {
            this.isThisRunner = true;
            this.isThisChaser = false;
        }
    }

    private void setTestQuestions() {
        this.questions = new Question[100];
        for (int i = 0; i < 100; i++) {
            questions[i] = new Question();
            questions[i].setQuestionText("Tekst dugog pitanja na koje ne znamo odgovor " + i);
            String[] pa = new String[4];
            pa[0] = "odg 1";
            pa[1] = "odg 2";
            pa[2] = "odg 3";
            pa[3] = "odg 4";
            questions[i].setPossibleAnswers(pa);
            questions[i].setCorrectAnswer("ponudjeni odg 1");
        }
    }
}
