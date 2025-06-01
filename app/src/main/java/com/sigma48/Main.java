package com.sigma48;

import com.sigma48.manager.AuthManager;
import com.sigma48.dao.UserDao;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static AuthManager authManager; 

    @Override
    public void start(Stage primaryStage) {
        UserDao userDao = new UserDao(); 
        authManager = new AuthManager(userDao); 

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sigma48/fxml/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            primaryStage.setTitle("SIGMA-48: Mission Dispatch System - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}