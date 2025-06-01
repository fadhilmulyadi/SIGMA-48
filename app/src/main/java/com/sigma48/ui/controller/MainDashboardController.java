package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.model.User;
import com.sigma48.model.Role;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class MainDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleSpecificContentLabel;

    @FXML
    private Button logoutButton;

    private User currentUser;

    @FXML
    public void initialize() {
        this.currentUser = Main.authManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Selamat Datang, " + currentUser.getUsername() + "! Role: " + currentUser.getRole().getDisplayName());
            displayRoleSpecificContent(currentUser.getRole());
        } else {
            welcomeLabel.setText("Error: Tidak ada pengguna yang login.");
        }
    }

    private void displayRoleSpecificContent(Role role) {
        switch (role) {
            case DIREKTUR_INTELIJEN:
                roleSpecificContentLabel.setText("Fitur untuk Direktur Intelijen.");
                break;
            case ANALIS_INTELIJEN:
                roleSpecificContentLabel.setText("Fitur untuk Analis Intelijen.");
                break;
            case KOMANDAN_OPERASI:
                roleSpecificContentLabel.setText("Fitur untuk Komandan Operasi.");
                break;
            case AGEN_LAPANGAN:
                roleSpecificContentLabel.setText("Fitur untuk Agen Lapangan.");
                break;
            case TEKNISI_DUKUNGAN:
                roleSpecificContentLabel.setText("Fitur untuk Teknisi Dukungan.");
                break;
            default:
                roleSpecificContentLabel.setText("Dashboard standar.");
                break;
        }
    }

    @FXML
    protected void handleLogoutButtonAction(ActionEvent event) {
        Main.authManager.logout();
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/sigma48/fxml/LoginView.fxml"));
            Scene loginScene = new Scene(loginRoot);
            currentStage.setTitle("SIGMA-48: Mission Dispatch System - Login");
            currentStage.setScene(loginScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}