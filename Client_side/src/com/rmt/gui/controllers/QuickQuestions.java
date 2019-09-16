package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.rmt.domain.Message;
import com.rmt.domain.Question;
import com.rmt.services.CommunicationService;
import com.rmt.services.StageService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class QuickQuestions implements Initializable {

    @FXML
    private ImageView logo;

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

    @FXML
    private Label gameFinishedMessage;

    @FXML
    private VBox gameFinished;

    private StringProperty gameFinishedMessageText = new SimpleStringProperty();

    private CommunicationService communicationService = CommunicationService.getCommunicationServiceInstance();
    private StageService stageService = StageService.getStageServiceInstance();

    private Question[] questions;
    private int currentQuestionIndex = 0;
    private int secondsForAnswering = 15;

    private int numberOfCorrectAnswers = 0;
    private PauseTransition timer;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.gameFinished.setVisible(false);
        this.gameFinishedMessage.textProperty().bind(gameFinishedMessageText);
        this.questionText.setFocusTraversable(false);
        this.questionText.setMouseTransparent(true);

        try {
            this.questions = communicationService.loadQuickQuestions();
        } catch (IOException e) {
            this.timer.stop();
            this.showErrorAlert();
        }
//        this.setTestQuestions();
        this.displayQuestion();
        this.startTimer(secondsForAnswering);
        this.startErrorWaiter();
    }

    private void startTimer(int seconds) {
        this.timer = new PauseTransition(Duration.seconds(seconds));
        timer.setOnFinished(event -> {
            try {
                System.out.println("istekao tajmer");
                this.communicationService.sendAnswers(this.numberOfCorrectAnswers);
            } catch (IOException e) {
                System.out.println("exception u istekao timer i salje tacne odg");
                this.showErrorAlert();
            }
        });
        timer.playFromStart();
    }

    private void startErrorWaiter() {
        Task<String> errorWaiter = new Task<String>() {
            @Override
            protected String call() {
                Message message = null;
                try {
                    message = communicationService.waitForMessage();
                } catch (IOException e) {
                    timer.stop();
                    showErrorAlert();
                }
                if (message.getType() == Message.MessageType.ERROR) {
                    timer.stop();
                    try {
                        communicationService.sendAnswers(-1);
                    } catch (IOException e) {
                        timer.stop();
                        showErrorAlert();
                    }
//                    System.out.println(message.getType()+" "+message.getMessageText());
                    return message.getMessageText();
                } else if (message.getType() == Message.MessageType.ANSWERS) {
//                    System.out.println("Answers received successfuly, error waiter se gasi");
                    return message.getMessageText();
                }
                return null;
            }
        };
        errorWaiter.valueProperty().addListener((observable, oldMessage, newMessage) -> {
            if (newMessage != null && !newMessage.contains("#")) {
                this.showGameFinishedMessage(newMessage);
            } else if (newMessage != null && newMessage.contains("#")) {
                this.stageService.changeToChaseScene(this.answerOne.getScene(), newMessage);
            }
        });
        Thread waiter = new Thread(errorWaiter);
        waiter.setDaemon(true);
        waiter.start();
    }

    private void showGameFinishedMessage(String message) {
        this.logo.setOpacity(0.2);
        this.questionText.setOpacity(0.2);
        this.answerOne.setOpacity(0.2);
        this.answerTwo.setOpacity(0.2);
        this.answerThree.setOpacity(0.2);
        this.answerFour.setOpacity(0.2);
        this.gameFinishedMessageText.setValue(message);
        this.gameFinished.setOpacity(1);
        this.gameFinished.setVisible(true);
    }

    public void goBackOnMatchMakingScene() {
        this.stageService.changeScene("com/rmt/gui/fxmls/matchMakingScene.fxml", this.answerOne.getScene(), false);
    }


    public void answerButtonClicked(ActionEvent event) {
        JFXButton selectedButton = (JFXButton) event.getTarget();
        if (selectedButton.getText().equals(this.questions[currentQuestionIndex].getCorrectAnswer())) {
            ++this.numberOfCorrectAnswers;
        }
        ++currentQuestionIndex;
        this.displayQuestion();
    }


    private void displayQuestion() {
        if (currentQuestionIndex == this.questions.length) {
            try {
                this.questions = this.communicationService.loadQuickQuestions();
            } catch (IOException e) {
                this.timer.stop();
                this.showErrorAlert();
            }
            currentQuestionIndex = 0;
        }

        Question question = this.questions[this.currentQuestionIndex];

        this.loadQuestionText(question);
        this.loadQuestionAnswers(question);
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

    private void loadQuestionAnswers(Question question) {
        ArrayList<String> answers = new ArrayList<>();
        answers.addAll(Arrays.asList(question.getPossibleAnswers()));
        answers.add(question.getCorrectAnswer());
        Collections.shuffle(Arrays.asList(answers));

        this.answerOne.setText(answers.get(0));
        this.answerTwo.setText(answers.get(1));
        this.answerThree.setText(answers.get(2));
        this.answerFour.setText(answers.get(3));
    }

    private void showErrorAlert() {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Greska u komunikaciji sa serverom");
        error.setContentText("Pokusajte ponovo.");
        Optional<ButtonType> answer = error.showAndWait();
        if (answer.get() == ButtonType.OK) {
            this.stageService.changeScene("com/rmt/gui/fxmls/startScene.fxml", this.gameFinished.getScene(), false);
        }
    }
}
