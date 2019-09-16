package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.rmt.domain.Message;
import com.rmt.domain.Question;
import com.rmt.services.CommunicationService;
import com.rmt.services.StageService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

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
    private Label gameFinishedMessage;

    @FXML
    private VBox gameFinished;

    private StringProperty gameFinishedMessageText = new SimpleStringProperty();

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
    int currentQuestionIndex = -1;

    boolean isAnswerCorrect;
    boolean isOpponentCorrect;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.gameFinished.setVisible(false);
        this.bindAnswerButtons();
        this.gameFinishedMessage.textProperty().bind(this.gameFinishedMessageText);

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

//        this.setTestQuestions();
        try {
            this.questions = this.communicationService.loadChaseQuestions();
        } catch (IOException e) {
            this.timeline.stop();
            this.showErrorAlert();
        }
        this.showQuestion();
    }

    public void buttonClicked(ActionEvent event) {
        this.stopTimer();
        JFXButton selectedButton = (JFXButton) event.getSource();

        String chosenAnswer = selectedButton.getText();

        this.questionText.setVisible(false);
        this.showAnswerButtons(false);
        this.progressIndicator.setVisible(true);

        this.isAnswerCorrect = this.questions[currentQuestionIndex].getCorrectAnswer().equals(chosenAnswer);
        try {
            this.communicationService.sendChaseAnswer(this.isAnswerCorrect);
        } catch (IOException e) {
            this.timeline.stop();
            this.showErrorAlert();
        }
        this.startMessageWaiter();

//        boolean gameFinished = this.checkAnswers(chosenAnswer);
//
//        if (gameFinished == false) {
//            this.showQuestion();
//        }
    }

    private void startMessageWaiter() {
        Task<Message> errorWaiter = new Task<Message>() {
            @Override
            protected Message call() {
                Message message = null;
                try {
                    message = communicationService.waitForMessage();
                } catch (IOException e) {
                    timeline.stop();
                    showErrorAlert();
                }
                System.out.println(message);
                return message;
            }
        };
        errorWaiter.valueProperty().addListener((observable, oldMessage, newMessage) -> {
            if (newMessage != null && newMessage.getType() == Message.MessageType.ERROR) {
                timeline.stop(); //ne poziva onFinished, jeeej
//                this.communicationService.gameFinished();
                this.showGameFinishedMessage(newMessage.getMessageText());
            } else if (newMessage != null && newMessage.getType() == Message.MessageType.EXCHANGE_ANSWERS) {
//                this.startMessageWaiter();
                if (newMessage.getMessageText().equals("true")) {
                    this.isOpponentCorrect = true;
                } else {
                    this.isOpponentCorrect = false;
                }
                this.checkAnswers();
            }
        });
        Thread waiter = new Thread(errorWaiter);
        waiter.setDaemon(true);
        waiter.start();
    }

    private void checkAnswers() {
        boolean showNextQuestion = true;
        if (isThisRunner) {
            if (this.isAnswerCorrect) {
                this.moveRunner();
                if (runnerPossition == 8) {
                    try {
                        this.communicationService.gameFinished();
                    } catch (IOException e) {
                        this.timeline.stop();
                        this.showErrorAlert();
                    }
                    this.showGameFinishedMessage("Congratulations! You escaped!");
                    showNextQuestion = false;
                }
            }
            if (this.isOpponentCorrect) {
                moveChaser();
                if (runnerPossition == chaserPossition) {
                    try {
                        this.communicationService.gameFinished();
                    } catch (IOException e) {
                        this.timeline.stop();
                        this.showErrorAlert();
                    }
                    this.showGameFinishedMessage("Sorry, the chaser got you.");
                    showNextQuestion = false;
                }
            }
        } else if (isThisChaser) {
            if (this.isOpponentCorrect) {
                this.moveRunner();
                if (runnerPossition == 8) {
                    try {
                        this.communicationService.gameFinished();
                    } catch (IOException e) {
                        this.timeline.stop();
                        this.showErrorAlert();
                    }
                    this.showGameFinishedMessage("Sorry, the runner escaped.");
                    showNextQuestion = false;
                }
            }
            if (this.isAnswerCorrect) {
                moveChaser();
                if (runnerPossition == chaserPossition) {
                    try {
                        this.communicationService.gameFinished();
                    } catch (IOException e) {
                        this.timeline.stop();
                        this.showErrorAlert();
                    }
                    this.showGameFinishedMessage("Congratulations! You caught the runner!");
                    showNextQuestion = false;
                }
            }
        }
        if (showNextQuestion) {
            this.showQuestion();
        }
    }

//    private void colorAnswer(JFXButton button, boolean isAnswerCorrect){
//        if(isAnswerCorrect)
//            button.getStyleClass().add("correct-answer");
//        else
//            button.getStyleClass().add("wrong-answer");
//    }

    private void showGameFinishedMessage(String message) {
        this.step.setOpacity(0.2);
        this.question.setOpacity(0.2);
        this.timerLabel.setOpacity(0.2);
        this.progressIndicator.setVisible(false);
        this.gameFinishedMessageText.setValue(message);
        this.gameFinished.setOpacity(1);
        this.gameFinished.setVisible(true);
    }

    public void goBackOnMatchMakingScene() {
        this.stageService.changeScene("com/rmt/gui/fxmls/matchMakingScene.fxml", this.answerOne.getScene(), false);
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
        this.timeline.getKeyFrames().add(start);
        this.timeline.setDelay(Duration.seconds(2));
        this.timeline.playFromStart();
    }

    private void timeIsUp() {
        this.isAnswerCorrect = false;
        try {
            this.communicationService.sendChaseAnswer(this.isAnswerCorrect);
        } catch (IOException e) {
            this.timeline.stop();
            this.showErrorAlert();
        }
        this.startMessageWaiter();
//        boolean gameFinished = this.checkAnswers();
//        if (gameFinished == false) {
//            this.showQuestion();
//        }
    }

    private void showQuestion() {
        ++currentQuestionIndex;
        if (currentQuestionIndex == this.questions.length) {
            try {
                this.questions = this.communicationService.loadChaseQuestions();
            } catch (IOException e) {
                this.timeline.stop();
                this.showErrorAlert();
            }
            currentQuestionIndex = 0;
        }
        Question question = this.questions[currentQuestionIndex];
        this.loadQuestionText(question);
        this.loadQuestionAnswers(question);
        this.resetTimer();
    }

    private void loadQuestionText(Question question) {
        this.questionText.setVisible(true);
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
        this.progressIndicator.setVisible(false);
        showAnswerButtons(true);
        ArrayList<String> possibleAnswers = new ArrayList(3);
        possibleAnswers.add(question.getPossibleAnswers()[0]);
        possibleAnswers.add(question.getPossibleAnswers()[1]);
        possibleAnswers.add(question.getCorrectAnswer());
        Collections.shuffle(possibleAnswers);

        this.answerOne.setText(possibleAnswers.get(0));
        this.answerTwo.setText(possibleAnswers.get(1));
        this.answerThree.setText(possibleAnswers.get(2));

        this.answerOne.setDisableVisualFocus(true);

    }

    private void showAnswerButtons(boolean show) {
        this.answerOne.setVisible(show);
        this.answerTwo.setVisible(show);
        this.answerThree.setVisible(show);
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

    private void bindAnswerButtons() {
        this.answerOne.disableProperty().bind(Bindings.or(answerTwo.pressedProperty(), answerThree.pressedProperty()));
        this.answerTwo.disableProperty().bind(Bindings.or(answerOne.pressedProperty(), answerThree.pressedProperty()));
        this.answerThree.disableProperty().bind(Bindings.or(answerOne.pressedProperty(), answerTwo.pressedProperty()));
    }

    private void showErrorAlert() {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Greska u komunikaciji sa serverom");
        error.setContentText("Pokusajte ponovo.");
        Optional<ButtonType> answer = error.showAndWait();
        if (answer.get() == ButtonType.OK) {
            this.stageService.changeScene("com/rmt/gui/fxmls/startScene.fxml", this.progressIndicator.getScene(), false);
        }
    }
}
