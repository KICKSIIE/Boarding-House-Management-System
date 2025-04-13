package com.example.systempractice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainController {

    PasswordHasher PasswordHasher = new PasswordHasher();
    @FXML private VBox loginPane;
    @FXML private VBox signupPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField signupUsernameField;
    @FXML private TextField signupEmailField;
    @FXML private PasswordField signupPasswordField;
    @FXML private PasswordField signupConfirmPasswordField;
    @FXML private Label loginErrorLabel;

    @FXML
    private void onLoginButtonClick() {
        String email = usernameField.getText().trim();
        String password = passwordField.getText();

        loginErrorLabel.setText("");
        loginErrorLabel.setVisible(false);

        if (email.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Email and password must not be empty.");
            loginErrorLabel.setVisible(true);
            return;
        }

        try (Connection conn = database_server.getConnection()) {
            String sql = "SELECT password FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                String hashedPassword = PasswordHasher.hashPassword(password);

                if (hashedPassword != null && hashedPassword.equals(storedHashedPassword)) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("tenant.fxml"));
                    Parent tenantPage = loader.load();
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(new Scene(tenantPage));
                    stage.show();
                } else {
                    loginErrorLabel.setText("Incorrect password.");
                    loginErrorLabel.setVisible(true);
                }
            } else {
                loginErrorLabel.setText("No account found with that email.");
                loginErrorLabel.setVisible(true);
            }
        } catch (SQLException | IOException e) {
            loginErrorLabel.setText("Login failed: " + e.getMessage());
            loginErrorLabel.setVisible(true);
        }
    }

    @FXML
    protected void onLabelClick() {
        // Hide login pane and show signup pane
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        signupPane.setVisible(true);
        signupPane.setManaged(true);

        // Hide login error label when switching to signup
        loginErrorLabel.setVisible(false);
        loginErrorLabel.setText("");
    }

    @FXML
    protected void onBackToLoginClick() {
        // Hide signup pane and show login pane
        signupPane.setVisible(false);
        signupPane.setManaged(false);
        loginPane.setVisible(true);
        loginPane.setManaged(true);

        // Also hide signup error label if you have one
        errorLabel.setVisible(false);
        errorLabel.setText("");

    }

    @FXML
    private Label errorLabel; // To display error messages

    @FXML
    private void onSignupButtonClick() {
        String email = signupEmailField.getText();
        String username = signupUsernameField.getText();
        String password = signupPasswordField.getText();
        String confirmPassword = signupConfirmPasswordField.getText();

        // Clear any previous error messages
        errorLabel.setText("");
        errorLabel.setVisible(false);  // Hide the label initially

        // Validate Email (check for '@' symbol)
        if (!email.contains("@")) {
            errorLabel.setText("Email must contain '@'");
            errorLabel.setVisible(true); // Show the error label
            return;
        }

        // Validate Passwords Match
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            errorLabel.setVisible(true); // Show the error label
            return;
        }

        // Check if username already exists in the database
        try (Connection conn = database_server.getConnection()) {
            String checkUsernameQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(checkUsernameQuery);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                errorLabel.setText("Username is already taken.");
                errorLabel.setVisible(true); // Show the error label
                return;
            }

            // If no errors, proceed to insert the new account
            String hashedPassword = PasswordHasher.hashPassword(password);

            String sql = "INSERT INTO users (email, username, password) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(sql);
            insertStmt.setString(1, email);
            insertStmt.setString(2, username);
            insertStmt.setString(3, hashedPassword); // Store the password before storing
            insertStmt.executeUpdate();

            errorLabel.setText("Account created successfully!");
            errorLabel.setVisible(true); // Show the success message
        } catch (SQLException e) {
            errorLabel.setText("Error: " + e.getMessage());
            errorLabel.setVisible(true); // Show the error label
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

