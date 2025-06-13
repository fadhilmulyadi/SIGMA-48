package com.sigma48.ui.controller;

import com.sigma48.Main;
import com.sigma48.util.SoundUtils;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class LoginViewController {

    @FXML
    private StackPane backgroundPane;

    @FXML
    private ImageView agencyLogoView;

    @FXML
    private TextField idAgenField;

    @FXML
    private PasswordField kodeAksesField;

    @FXML
    private Button loginButton;

    @FXML
    private HBox accessDeniedBox;

    @FXML
    private Label accessDeniedLabel;

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        loadLogo();
        loadBackground();
        accessDeniedBox.setVisible(false); // Sembunyikan pesan error awal
        accessDeniedBox.setManaged(false); // Agar tidak mengambil tempat saat tersembunyi
    }

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        String idAgen = idAgenField.getText();
        String kodeAkses = kodeAksesField.getText();

        boolean loginSuccess = Main.authManager.login(idAgen, kodeAkses);
        
        if (loginSuccess) {
            navigateToMainDashboard();
        } else {
            kodeAksesField.clear();
            SoundUtils.playSound("access_denied.mp3");
            showAccessDenied("ACCESS DENIED");
        }
    }

    private void loadLogo() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/com/sigma48/images/logo.png"));
            agencyLogoView.setImage(logo);
        } catch (Exception e) {
            System.err.println("Gagal memuat logo lembaga: " + e.getMessage());
        }
    }

    private void loadBackground() {
        try {
            String imagePath = getClass().getResource("/com/sigma48/images/background.png").toExternalForm();
            backgroundPane.setStyle(
                "-fx-background-image: url('" + imagePath + "');" +
                "-fx-background-size: cover;" +
                "-fx-background-repeat: no-repeat;" +
                "-fx-background-position: center center;"
            );
        } catch (Exception e) {
            System.err.println("Gagal memuat background: " + e.getMessage());
        }
    }

    private void showAccessDenied(String message) {
    // Sembunyikan dulu, jaga-jaga kalau sebelumnya masih tampil
        accessDeniedBox.setVisible(false);
        accessDeniedBox.setManaged(false);

        // Buat delay sebelum menampilkan box
        PauseTransition delayBeforeShow = new PauseTransition(Duration.seconds(0.6));
        delayBeforeShow.setOnFinished(event -> {
            accessDeniedLabel.setText(message.toUpperCase());
            accessDeniedBox.setManaged(true);
            accessDeniedBox.setVisible(true);
            accessDeniedBox.setOpacity(1.0);

            // Delay untuk menyembunyikan kembali
            PauseTransition delayBeforeHide = new PauseTransition(Duration.seconds(2.0));
            delayBeforeHide.setOnFinished(e -> {
                accessDeniedBox.setVisible(false);
                accessDeniedBox.setManaged(false);
            });
            delayBeforeHide.play();
        });

        delayBeforeShow.play();
    }

    private void navigateToMainDashboard() {
        try {
            Stage currentStage = (Stage) loginButton.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/MainDashboardView.fxml"));
            Parent dashboardRoot = loader.load();

            Scene dashboardScene = new Scene(dashboardRoot);

            URL cssUrl = getClass().getResource("/com/sigma48/css/theme.css");
            if (cssUrl != null) {
                dashboardScene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS berhasil diterapkan ke Scene Dashboard.");
            } else {
                System.err.println("Peringatan: File theme.css tidak ditemukan untuk Scene Dashboard!");
            }

            currentStage.setTitle("SIGMA-48: Dashboard");
            currentStage.setScene(dashboardScene);
            currentStage.setFullScreen(true);

        } catch (IOException e) {
            e.printStackTrace();
            // Tampilkan pesan error jika dashboard gagal dimuat
            showAccessDenied("Gagal memuat dashboard aplikasi.");
        }
    }
}