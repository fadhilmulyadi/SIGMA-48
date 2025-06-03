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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainDashboardController {

    @FXML
    private BorderPane mainDashboardPane;
    
    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox navigationMenuBox;

    @FXML
    private Button logoutButton;

    @FXML
    private StackPane contentAreaPane;

    private User currentUser;

    @FXML
    public void initialize() {
        this.currentUser = Main.authManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Selamat Datang, " + currentUser.getUsername() + "! Role: " + currentUser.getRole().getDisplayName());
            setupNavigationMenu(currentUser.getRole());
        } else {
            welcomeLabel.setText("Error: Tidak ada pengguna yang login.");
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

    private void addButtonToMenu(String buttonText, Runnable action) {
        Button button = new Button(buttonText);
        button.setOnAction(event -> action.run());
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("nav-button");
        navigationMenuBox.getChildren().add(button);
    }

    private void setupNavigationMenu(Role role) {
        navigationMenuBox.getChildren().clear();

        switch (role) {
            case DIREKTUR_INTELIJEN:
                addButtonToMenu("Lihat Semua Misi", () -> loadView("/com/sigma48/fxml/MissionListView.fxml"));
                addButtonToMenu("Buat Misi Baru", () -> loadView("/com/sigma48/fxml/MissionCreateForm.fxml"));
                break;
            case ANALIS_INTELIJEN:
                addButtonToMenu("Lihat Semua Misi", () -> loadView("/com/sigma48/fxml/MissionListView.fxml"));
                addButtonToMenu("Buat Misi Baru", () -> loadView("/com/sigma48/fxml/MissionCreateForm.fxml"));
                break;
            case KOMANDAN_OPERASI:
                addButtonToMenu("Lihat Semua Misi", () -> loadView("/com/sigma48/fxml/MissionListView.fxml"));
                break;
            case AGEN_LAPANGAN:
                break;
            case TEKNISI_DUKUNGAN:
                break;
            default:
                break;
        }
    }

    private void loadView(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file: " + fxmlPath);
                contentAreaPane.getChildren().setAll(new Label("Error: Gagal memuat tampilan. File tidak ditemukan: " + fxmlPath));
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            contentAreaPane.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            contentAreaPane.getChildren().setAll(new Label("Error: Gagal memuat tampilan " + fxmlPath));
        }
    }
}