package com.rmt.gui.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.rmt.services.CommunicationService;
import com.rmt.services.StageService;
import com.rmt.services.ValidationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    private StageService stageService;
    private CommunicationService communicationService;
    private ValidationService validationService;

    @FXML
    JFXTextField usernameField;
    @FXML
    JFXPasswordField passwordField;
    @FXML
    JFXButton loginButton;
    @FXML
    JFXButton registerButton;
    @FXML
    Label invalidUsername;
    @FXML
    Label invalidPassword;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.stageService = StageService.getStageServiceInstance();
        this.communicationService = CommunicationService.getCommunicationServiceInstance();
        this.validationService = ValidationService.getValidationServiceInstance();

        this.loginButton.setDisable(true);
        this.registerButton.setDisable(true);
    }

    public void onBackButtonClicked(ActionEvent event) throws IOException {
        this.stageService.changeScene("com/rmt/gui/fxmls/startScene.fxml", event);
    }

    public void onRegisterButtonClicked(ActionEvent event) {
        String username = this.usernameField.getText();
        System.out.println(username);
        String password = this.passwordField.getText();
        if (this.validatePassword(password) == false) {
            return;
        }
        try {
            boolean registrationSuccessful = this.communicationService.register(username, password);
            if (registrationSuccessful) {
                try {
                    //this.stageService.changeScene("com/rmt/gui/fxmls/matchMakingScene.fxml", event);
                    this.stageService.changeToMatchmakingScene(event, username);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                this.invalidUsername.setText("Username already exists.");
            }
        } catch (IOException e) {
            showErrorAlert();
        }
    }

    private void showErrorAlert() {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Greska u komunikaciji sa serverom");
        error.setContentText("Pokusajte ponovo.");
        Optional<ButtonType> answer = error.showAndWait();
        if(answer.get() == ButtonType.OK){
            this.stageService.changeScene("com/rmt/gui/fxmls/startScene.fxml", this.loginButton.getScene(), false);
        }
    }

    public void onLoginButtonClicked(ActionEvent event) {
        String username = this.usernameField.getText();
        String password = this.passwordField.getText();
        try {
            String answer = this.communicationService.login(username, password);
            if (answer.equals("OK")) {
                try {
                    this.stageService.changeScene("com/rmt/gui/fxmls/matchMakingScene.fxml", event);
                    //this.stageService.changeToMatchmakingScene(event, usernameField);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                this.setIncorrectInfoStyle();
            }
        } catch (IOException e) {
            this.showErrorAlert();
        }
    }

    private boolean validatePassword(String password) {
        if (this.validationService.passwordTooShort(password)) {
            this.setInvalidPasswordMessage("Password too short");
            return false;
        }
        if (this.validationService.passwordTooLong(password)) {
            this.setInvalidPasswordMessage("Password too long");
            return false;
        }
        if (this.validationService.validatePasswordDigits(password) == false) {
            this.setInvalidPasswordMessage("Password has to contain at least one digit.");
            return false;
        }
        if (this.validationService.validatePasswordUpperAndLowerCases(password) == false) {
            this.setInvalidPasswordMessage("Password has to contain at least one upper and one lower case.");
            return false;
        }
        return true;
    }


    private void setIncorrectInfoStyle() {
        this.usernameField.getStyleClass().add("incorrect-info");
        this.passwordField.getStyleClass().add("incorrect-info");
    }

    private void setInvalidPasswordMessage(String message) {
        this.invalidPassword.setText(message);
        this.passwordField.getStyleClass().add("incorrect-info");
    }

    public void handleEmptyPasswordField() {
        String password = this.passwordField.getText().trim();
        boolean disableButton = password.isEmpty();
        this.loginButton.setDisable(disableButton);
        this.registerButton.setDisable(disableButton);
    }

    public void handleUsernameField() {
        String username = this.usernameField.getText().trim();

        boolean tooShort = username.length() < 3;
        boolean tooLong = username.length() > 10;

        if (tooShort) {
            this.invalidUsername.setText("Username is too short.");
            this.usernameField.getStyleClass().add("incorrect-info");
        } else if (tooLong) {
            this.invalidUsername.setText("Username is too long.");
            this.usernameField.getStyleClass().add("incorrect-info");
        } else {
            this.invalidUsername.setText("");
            this.usernameField.getStyleClass().removeAll("incorrect-info");
        }
        boolean disable = tooShort || tooLong;
        this.loginButton.setDisable(disable);
        this.registerButton.setDisable(disable);
    }
}
