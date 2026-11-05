package com.example.systempractice;

import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class TenantController {
    private final TenantService service = new TenantService(database_server::getConnection);
    private String email;
    @FXML private Label welcomeLabel;
    @FXML private Label roomLabel;
    @FXML private Label statusLabel;
    @FXML private ListView<String> paymentList;
    @FXML private ListView<String> requestList;
    @FXML private TextArea requestDescription;

    @FXML private void initialize() {
        paymentList.setCellFactory(list -> wrappedCell());
        requestList.setCellFactory(list -> wrappedCell());
    }

    private ListCell<String> wrappedCell() {
        ListCell<String> cell = new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
            }
        };
        cell.setWrapText(true);
        cell.setPrefWidth(0);
        return cell;
    }

    public void setEmail(String email) {
        this.email = email;
        welcomeLabel.setText("Welcome, " + email);
        refresh();
    }

    @FXML private void refresh() {
        try {
            var room = service.room(email);
            var payments = service.payments(email);
            var requests = service.requests(email);
            roomLabel.setText(room.map(r -> "Room " + r.number() + " — " + r.description() + " | Monthly rent: " + r.monthlyRent().toPlainString()).orElse("No room assigned yet."));
            paymentList.getItems().setAll(payments.stream().map(p -> p.paidOn() + " | " + p.amount().toPlainString() + (p.note().isBlank() ? "" : " | " + p.note())).toList());
            requestList.getItems().setAll(requests.stream().map(r -> r.createdAt().toLocalDate() + " | " + r.status() + " | " + r.description()).toList());
            statusLabel.setText("");
        } catch (SQLException e) {
            roomLabel.setText("Room details unavailable.");
            paymentList.getItems().clear(); requestList.getItems().clear();
            statusLabel.setText("Unable to load tenant details. Check the database setup.");
        }
    }

    @FXML private void submitRequest() {
        try {
            service.submitRequest(email, requestDescription.getText());
            requestDescription.clear();
            refresh();
            statusLabel.setText(statusLabel.getText().isEmpty() ? "Maintenance request submitted."
                    : "Request submitted, but tenant details could not be refreshed.");
        } catch (IllegalArgumentException e) {
            statusLabel.setText(e.getMessage());
        } catch (SQLException e) {
            statusLabel.setText("Unable to submit the request. Check the database setup.");
        }
    }

    @FXML private void onLogout() throws IOException {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login_page.fxml"));
        stage.setScene(new Scene(loader.load(), 640, 700));
    }
}
