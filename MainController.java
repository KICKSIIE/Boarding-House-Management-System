package com.example.systempractice;

import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainController {
    private final AccountService accounts = new AccountService(database_server::getConnection);
    @FXML private VBox loginPane;
    @FXML private VBox signupPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField signupUsernameField;
    @FXML private TextField signupEmailField;
    @FXML private PasswordField signupPasswordField;
    @FXML private PasswordField signupConfirmPasswordField;
    @FXML private Label loginErrorLabel;
    @FXML private Label errorLabel;

    @FXML private void onLoginButtonClick() {
        String email = usernameField.getText().trim();
        String password = passwordField.getText();
        loginErrorLabel.setVisible(false);
        if (email.isEmpty() || password.isEmpty()) {
            show(loginErrorLabel, "Email and password must not be empty.");
            return;
        }
        try {
            if (!accounts.login(email, password)) {
                show(loginErrorLabel, "Incorrect email or password.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tenant.fxml"));
            Parent page = loader.load();
            TenantController controller = loader.getController();
            controller.setEmail(email);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(page, 640, 700));
        } catch (SQLException e) {
            show(loginErrorLabel, "Unable to connect to the account database. Check its configuration.");
        } catch (IOException e) {
            show(loginErrorLabel, "Unable to open the tenant page.");
        }
    }

    @FXML protected void onLabelClick() {
        loginPane.setVisible(false); loginPane.setManaged(false);
        signupPane.setVisible(true); signupPane.setManaged(true);
        loginErrorLabel.setVisible(false);
    }

    @FXML protected void onBackToLoginClick() {
        signupPane.setVisible(false); signupPane.setManaged(false);
        loginPane.setVisible(true); loginPane.setManaged(true);
        errorLabel.setVisible(false);
    }

    @FXML private void onSignupButtonClick() {
        errorLabel.setVisible(false);
        String password = signupPasswordField.getText();
        if (!password.equals(signupConfirmPasswordField.getText())) {
            show(errorLabel, "Passwords do not match.");
            return;
        }
        try {
            AccountService.SignupResult result = accounts.signup(signupEmailField.getText(), signupUsernameField.getText(), password);
            switch (result) {
                case INVALID_EMAIL -> show(errorLabel, "Enter a valid email address.");
                case INVALID_USERNAME -> show(errorLabel, "Enter a username of up to 100 characters.");
                case INVALID_PASSWORD -> show(errorLabel, "Password must be 8 to 256 characters.");
                case USERNAME_TAKEN -> show(errorLabel, "Username is already taken.");
                case EMAIL_TAKEN -> show(errorLabel, "Email is already registered.");
                case CREATED -> {
                    usernameField.setText(signupEmailField.getText().trim());
                    signupPasswordField.clear(); signupConfirmPasswordField.clear();
                    onBackToLoginClick();
                    show(loginErrorLabel, "Account created. Please log in.");
                }
            }
        } catch (SQLException e) {
            show(errorLabel, "Unable to create the account. Check the database configuration.");
        }
    }

    private void show(Label label, String message) { label.setText(message); label.setVisible(true); }
}
