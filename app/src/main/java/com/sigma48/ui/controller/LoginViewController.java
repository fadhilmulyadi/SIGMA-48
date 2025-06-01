package com.sigma48.ui.controller;

import com.sigma48.Main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginViewController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username dan password tidak boleh kosong.");
            return;
        }

        boolean loginSuccess = Main.authManager.login(username, password);

        if (loginSuccess) {
            statusLabel.setText("Login berhasil!");
            statusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            navigateToMainDashboard();
        } else {
            statusLabel.setText("Username atau password salah.");
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            passwordField.clear();
        }
    }

    private void navigateToMainDashboard() {
        try {
            Stage currentStage = (Stage) loginButton.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/MainDashboardView.fxml"));
            Parent dashboardRoot = loader.load();

            Scene dashboardScene = new Scene(dashboardRoot);
            
            currentStage.setTitle("SIGMA-48: Dashboard");
            currentStage.setScene(dashboardScene);
            currentStage.setResizable(true);
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Gagal memuat dashboard.");
        }
    }

    @FXML
    public void initialize() {
        statusLabel.setText("");
    }
}