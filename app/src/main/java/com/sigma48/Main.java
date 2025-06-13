package com.sigma48;

import com.sigma48.manager.AuthManager;
import com.sigma48.dao.UserDao;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;

public class Main extends Application {

    public static AuthManager authManager;

    @Override
    public void start(Stage primaryStage) {
        //Custom Font
        try {
            // Load custom fonts dengan debugging
            Font agencyFont = Font.loadFont(getClass().getResourceAsStream("/com/sigma48/fonts/AGENCYB.ttf"), 12);
            if (agencyFont != null) {
                System.out.println("Font AGENCYB.ttf berhasil dimuat: " + agencyFont.getName());
            } else {
                System.err.println("Gagal memuat font: AGENCYB.ttf");
            }

            Font shareTechFont = Font.loadFont(getClass().getResourceAsStream("/com/sigma48/fonts/ShareTechMono.ttf"), 12);
            if (shareTechFont != null) {
                System.out.println("Font ShareTechMono.ttf berhasil dimuat: " + shareTechFont.getName());
            } else {
                System.err.println("Gagal memuat font: ShareTechMono.ttf");
            }
        } catch (Exception e) {
            System.err.println("Exception saat memuat font: " + e.getMessage());
            e.printStackTrace();
        }

        UserDao userDao = new UserDao();
        authManager = new AuthManager(userDao);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/LoginView.fxml"));
            Parent root = loader.load();

            com.sigma48.ui.controller.LoginViewController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            // Muat CSS
            scene.getStylesheets().add(getClass().getResource("/com/sigma48/css/theme.css").toExternalForm());

            primaryStage.setTitle("SIGMA-48");
            primaryStage.setScene(scene);
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Gagal memuat LoginView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}